package teamroots.embers.item;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import teamroots.embers.SoundManager;
import teamroots.embers.api.event.EmberProjectileEvent;
import teamroots.embers.api.item.IProjectileWeapon;
import teamroots.embers.api.projectile.EffectDamage;
import teamroots.embers.api.projectile.IProjectilePreset;
import teamroots.embers.api.projectile.ProjectileRay;
import teamroots.embers.damage.DamageEmber;
import teamroots.embers.network.PacketHandler;
import teamroots.embers.network.message.MessageCannonBeamFX;
import teamroots.embers.util.EmberInventoryUtil;

import java.util.ArrayList;
import java.util.List;

public class ItemIgnitionCannon extends ItemBase implements IProjectileWeapon {
	public static double EMBER_COST = 25.0;
	public static int COOLDOWN = 10;
	public static double MAX_CHARGE = 20;
	public static float DAMAGE = 7.0f;
	public static double MAX_SPREAD = 30.0;
	public static float MAX_DISTANCE = 96.0f;


	public ItemIgnitionCannon() {
		super("ignition_cannon", true);
		this.setMaxStackSize(1);
	}
	
	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase entity, int timeLeft){
		if (!world.isRemote) {
			double charge = (Math.min(MAX_CHARGE, getMaxItemUseDuration(stack) - timeLeft)) / MAX_CHARGE;
			double handmod = entity.getActiveHand() == Hand.MAIN_HAND ? 1.0 : -1.0;
			handmod *= entity.getPrimaryHand() == HandSide.RIGHT ? 1.0 : -1.0;
			double posX = entity.posX + entity.getLookVec().x + handmod * (entity.width / 2.0) * Math.sin(Math.toRadians(-entity.rotationYaw - 90));
			double posY = entity.posY + entity.getEyeHeight() - 0.2 + entity.getLookVec().y;
			double posZ = entity.posZ + entity.getLookVec().z + handmod * (entity.width / 2.0) * Math.cos(Math.toRadians(-entity.rotationYaw - 90));

			double targX = entity.posX + entity.getLookVec().x * MAX_DISTANCE + (MAX_SPREAD * (1.0 - charge) * (itemRand.nextFloat() - 0.5));
			double targY = entity.posY + entity.getLookVec().y * MAX_DISTANCE + (MAX_SPREAD * (1.0 - charge) * (itemRand.nextFloat() - 0.5));
			double targZ = entity.posZ + entity.getLookVec().z * MAX_DISTANCE + (MAX_SPREAD * (1.0 - charge) * (itemRand.nextFloat() - 0.5));

			EffectDamage effect = new EffectDamage(DAMAGE, DamageEmber.EMBER_DAMAGE_SOURCE_FACTORY, 1, 1.0f);
			ProjectileRay ray = new ProjectileRay(entity, new Vec3d(posX, posY, posZ), new Vec3d(targX, targY, targZ), false, effect);

			EmberProjectileEvent event = new EmberProjectileEvent(entity, stack, charge, ray);
			MinecraftForge.EVENT_BUS.post(event);
			if (!event.isCanceled()) {
				for (IProjectilePreset projectile : event.getProjectiles()) {
					projectile.shoot(world);
				}
			}

			world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundManager.BLAZING_RAY_FIRE, SoundCategory.PLAYERS, 1.0f, 1.0f);
		}

		stack.getTagCompound().setInteger("cooldown", COOLDOWN);
	}
	
	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged){
		return slotChanged || newStack.getItem() != oldStack.getItem();
	}
	
	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean selected){
		if (!stack.hasTagCompound()){
			stack.setTagCompound(new CompoundNBT());
			stack.getTagCompound().setInteger("cooldown", 0);
		}
		else {
			if (stack.getTagCompound().getInteger("cooldown") > 0){
				stack.getTagCompound().setInteger("cooldown", stack.getTagCompound().getInteger("cooldown")-1);
			}
		}
	}
	
	@Override
	public int getMaxItemUseDuration(ItemStack stack){
		return 72000;
	}
	
	@Override
	public EnumAction getItemUseAction(ItemStack stack){
		return EnumAction.BOW;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand){
		ItemStack stack = player.getHeldItem(hand);
		if (stack.getTagCompound().getInteger("cooldown") <= 0 || player.capabilities.isCreativeMode){
			if(EmberInventoryUtil.getEmberTotal(player) >= EMBER_COST || player.capabilities.isCreativeMode) {
				EmberInventoryUtil.removeEmber(player, EMBER_COST);
				player.setActiveHand(hand);
				return new ActionResult<>(EnumActionResult.SUCCESS, stack);
			}
			else
			{
				world.playSound(null,player.posX,player.posY,player.posZ, SoundManager.BLAZING_RAY_EMPTY, SoundCategory.PLAYERS, 1.0f, 1.0f);
				return new ActionResult<>(EnumActionResult.FAIL, stack);
			}
		}
		return new ActionResult<>(EnumActionResult.PASS, stack); //OFFHAND FIRE ENABLED BOYS
	}
}
