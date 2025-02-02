package teamroots.embers.itemmod;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import teamroots.embers.api.EmbersAPI;
import teamroots.embers.api.itemmod.ItemModUtil;
import teamroots.embers.api.itemmod.ModifierBase;

import teamroots.embers.api.itemmod.ModifierBase.EnumType;

public class ModifierEldritchInsignia extends ModifierBase {

	public ModifierEldritchInsignia() {
		super(EnumType.ARMOR,"eldritch_insignia",0.0,true);
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void onEntityTarget(LivingSetAttackTargetEvent event){
		if (event.getTarget() instanceof PlayerEntity){
			int level = ItemModUtil.getArmorModifierLevel((PlayerEntity) event.getTarget(), EmbersAPI.ELDRITCH_INSIGNIA);
			if ((event.getEntityLiving().getLastDamageSource() == null 
					|| event.getEntityLiving().getLastDamageSource() != null && event.getEntityLiving().getLastDamageSource().getTrueSource() == null
					|| event.getEntityLiving().getLastDamageSource() != null && event.getEntityLiving().getLastDamageSource().getTrueSource() != null && event.getEntityLiving().getLastDamageSource().getTrueSource().getUniqueID().compareTo(event.getTarget().getUniqueID()) != 0
					) 
					&& event.getEntity().getEntityId() % (3+level) >= 2){
				if (level > 0 && !(event.getEntityLiving() instanceof PlayerEntity)/* || event.getEntityLiving() instanceof PlayerEntity && ((PlayerEntity)event.getEntityLiving()).getGameProfile().getName().compareToIgnoreCase("yrsegal") == 0*/){
					((EntityLiving)event.getEntityLiving()).setAttackTarget(null);
					//EmberInventoryUtil.removeEmber((PlayerEntity)event.getTarget(), cost);
				}
			}
		}
	}
}
