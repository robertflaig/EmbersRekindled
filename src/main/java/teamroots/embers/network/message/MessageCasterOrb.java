package teamroots.embers.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import teamroots.embers.SoundManager;
import teamroots.embers.api.EmbersAPI;
import teamroots.embers.api.event.EmberProjectileEvent;
import teamroots.embers.api.itemmod.ItemModUtil;
import teamroots.embers.api.projectile.EffectArea;
import teamroots.embers.api.projectile.EffectDamage;
import teamroots.embers.api.projectile.IProjectilePreset;
import teamroots.embers.api.projectile.ProjectileFireball;
import teamroots.embers.damage.DamageEmber;
import teamroots.embers.entity.EntityEmberProjectile;
import teamroots.embers.itemmod.ModifierCasterOrb;
import teamroots.embers.util.EmberInventoryUtil;

import java.util.Random;
import java.util.UUID;

public class MessageCasterOrb implements IMessage {
    public static Random random = new Random();
    double lookX = 0;
    double lookY = 0;
    double lookZ = 0;

    public MessageCasterOrb() {
        super();
    }

    public MessageCasterOrb(double lookX, double lookY, double lookZ) {
        super();
        this.lookX = lookX;
        this.lookY = lookY;
        this.lookZ = lookZ;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        lookX = buf.readDouble();
        lookY = buf.readDouble();
        lookZ = buf.readDouble();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeDouble(lookX);
        buf.writeDouble(lookY);
        buf.writeDouble(lookZ);
    }

    public static class MessageHolder implements IMessageHandler<MessageCasterOrb, IMessage> {
        @Override
        public IMessage onMessage(final MessageCasterOrb message, final MessageContext ctx) {
            PlayerEntity player = ctx.getServerHandler().player;
            WorldServer world = ctx.getServerHandler().player.getServerWorld();
            world.addScheduledTask(() -> {
                ItemStack heldStack = player.getHeldItemMainhand();
                if (ItemModUtil.hasHeat(heldStack)) {
                    int level = ItemModUtil.getModifierLevel(heldStack, EmbersAPI.CASTER_ORB);
                    UUID uuid = player.getUniqueID();
                    if (level > 0 && EmberInventoryUtil.getEmberTotal(player) > EmbersAPI.CASTER_ORB.cost && !ModifierCasterOrb.hasCooldown(uuid)) {
                        float handmod = player.getPrimaryHand() == HandSide.RIGHT ? 1.0f : -1.0f;
                        float offX = handmod * 0.5f * (float) Math.sin(Math.toRadians(-player.rotationYaw - 90));
                        float offZ = handmod * 0.5f * (float) Math.cos(Math.toRadians(-player.rotationYaw - 90));
                        EmberInventoryUtil.removeEmber(player, EmbersAPI.CASTER_ORB.cost);
                        double lookDist = Math.sqrt(message.lookX * message.lookX + message.lookY * message.lookY + message.lookZ * message.lookZ);
                        if (lookDist == 0)
                            return;
                        double xVel = (message.lookX / lookDist) * 0.5;
                        double yVel = (message.lookY / lookDist) * 0.5;
                        double zVel = (message.lookZ / lookDist) * 0.5;
                        double xOrigin = player.posX + offX;
                        double yOrigin = player.posY + player.getEyeHeight();
                        double zOrigin = player.posZ + offZ;

                        double resonance = EmbersAPI.getEmberEfficiency(heldStack);
                        double value = 8.0 * (Math.atan(0.6 * (level)) / (1.25));
                        value *= resonance;
                        EffectDamage effect = new EffectDamage((float) value, DamageEmber.EMBER_DAMAGE_SOURCE_FACTORY, 1, 1.0);
                        ProjectileFireball fireball = new ProjectileFireball(player, new Vec3d(xOrigin, yOrigin, zOrigin), new Vec3d(xVel, yVel, zVel), value, 160, effect);
                        EmberProjectileEvent event = new EmberProjectileEvent(player, heldStack, 0.0, fireball);
                        MinecraftForge.EVENT_BUS.post(event);
                        if (!event.isCanceled()) {
                            for (IProjectilePreset projectile : event.getProjectiles()) {
                                projectile.shoot(world);
                            }
                        }
                        world.playSound(null, xOrigin, yOrigin, zOrigin, SoundManager.FIREBALL, SoundCategory.PLAYERS, 1.0f, 1.0f);
                        ModifierCasterOrb.setCooldown(uuid, 20);
                    }
                }
            });
            return null;
        }
    }

}
