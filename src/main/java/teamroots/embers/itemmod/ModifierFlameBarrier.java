package teamroots.embers.itemmod;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import teamroots.embers.RegistryManager;
import teamroots.embers.SoundManager;
import teamroots.embers.api.EmbersAPI;
import teamroots.embers.api.itemmod.ItemModUtil;
import teamroots.embers.api.itemmod.ModifierBase;
import teamroots.embers.network.PacketHandler;
import teamroots.embers.network.message.MessageFlameShieldFX;
import teamroots.embers.util.EmberInventoryUtil;

import teamroots.embers.api.itemmod.ModifierBase.EnumType;

public class ModifierFlameBarrier extends ModifierBase {

	public ModifierFlameBarrier() {
		super(EnumType.ARMOR,"flame_barrier",2.0,true);
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void onHit(LivingHurtEvent event){
		if (event.getEntity() instanceof EntityPlayer && event.getSource().getTrueSource() instanceof EntityLivingBase){
			int blastingLevel = ItemModUtil.getArmorModifierLevel((EntityPlayer) event.getEntity(), EmbersAPI.FLAME_BARRIER);

			float strength = (float)(2.0*(Math.atan(0.6*(blastingLevel))/(Math.PI)));
			if (blastingLevel > 0 && EmberInventoryUtil.getEmberTotal(((EntityPlayer)event.getEntity())) >= cost){
				EmberInventoryUtil.removeEmber(((EntityPlayer)event.getEntity()), cost);
				event.getSource().getTrueSource().attackEntityFrom(RegistryManager.damage_ember, strength*event.getAmount()*0.5f);
				event.getSource().getTrueSource().setFire(blastingLevel+1);
				event.getEntity().playSound(SoundManager.FIREBALL_HIT,1.0f,1.0f);
				event.getEntity().getEntityWorld().playSound(null,event.getEntity().posX, event.getEntity().posY, event.getEntity().posZ, SoundManager.FIREBALL_HIT, SoundCategory.PLAYERS, 1.0f, 1.0f);

				if (!event.getEntity().world.isRemote){
					PacketHandler.INSTANCE.sendToAll(new MessageFlameShieldFX(event.getEntity().posX,event.getEntity().posY+event.getEntity().height/2.0,event.getEntity().posZ));
				}
			}
		}
	}
	
}
