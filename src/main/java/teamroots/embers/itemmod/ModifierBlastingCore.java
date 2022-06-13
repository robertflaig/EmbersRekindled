package teamroots.embers.itemmod;

import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
//import net.minecraft.util.Direction;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
//import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
//import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import teamroots.embers.api.EmbersAPI;
import teamroots.embers.api.itemmod.ItemModUtil;
import teamroots.embers.api.itemmod.ModifierBase;
import teamroots.embers.util.EmberInventoryUtil;
import teamroots.embers.util.Misc;

import java.util.*;

import teamroots.embers.api.itemmod.ModifierBase.EnumType;

public class ModifierBlastingCore extends ModifierBase {

	public ModifierBlastingCore() {
		super(EnumType.TOOL_OR_ARMOR,"blasting_core",2.0,true);
		MinecraftForge.EVENT_BUS.register(this);
	}

	private double getChanceBonus(double resonance) {
		if(resonance > 1)
			return 1 + (resonance - 1) * 0.5;
		else
			return resonance;
	}

	@SubscribeEvent
	public void onDrops(BreakEvent event){
		World world = event.getWorld();
		BlockPos pos = event.getPos();
		if (event.getPlayer() != null){
			if (!event.getPlayer().getHeldItem(Hand.MAIN_HAND).isEmpty()){
				ItemStack s = event.getPlayer().getHeldItem(Hand.MAIN_HAND);
				int blastingLevel = ItemModUtil.getModifierLevel(s, EmbersAPI.BLASTING_CORE);
				if (blastingLevel > 0 && EmberInventoryUtil.getEmberTotal(event.getPlayer()) >= cost){
					world.createExplosion(event.getPlayer(), pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, 0.5f, true);
					double resonance = EmbersAPI.getEmberEfficiency(s);
					double chance = (double) blastingLevel / (blastingLevel + 1) * getChanceBonus(resonance);

					for(BlockPos toExplode : getBlastCube(world, pos, event.getPlayer(), chance)) {
						BlockState state = world.getBlockState(toExplode);
						if (state.getBlockHardness(world, toExplode) >= 0 && event.getPlayer().canHarvestBlock(world.getBlockState(toExplode))){
							world.destroyBlock(toExplode, true);
							world.notifyBlockUpdate(toExplode, state, Blocks.AIR.getDefaultState(), 8);
						}
					}
					EmberInventoryUtil.removeEmber(event.getPlayer(), cost);
				}
			}
		}
	}

	public Iterable<BlockPos> getBlastAdjacent(World world, BlockPos pos, PlayerEntity player, double chance) {
		ArrayList<BlockPos> posList = new ArrayList<>();
		for (int i = 0; i < 6; i ++){
			Direction face2 = Direction.byIndex(i);
			Direction face = Direction.byIndex(i);
			if (Misc.random.nextDouble() < chance){
				posList.add(pos.offset(face));
			}
		}
		return posList;
	}

	public Iterable<BlockPos> getBlastCube(World world, BlockPos pos, PlayerEntity player, double chance) {
		ArrayList<BlockPos> posList = new ArrayList<>();
		for (Direction facePrimary : Direction.values()){
			if (Misc.random.nextDouble() < chance){
				BlockPos posPrimary = pos.offset(facePrimary);
				posList.add(posPrimary);

				for (Direction faceSecondary : Direction.values()){
					if(faceSecondary.getAxis() == facePrimary.getAxis())
						continue;
					if (Misc.random.nextDouble() < chance - 0.5){
						BlockPos posSecondary = posPrimary.offset(faceSecondary);
						posList.add(posSecondary);

						for (Direction faceTertiary : Direction.values()){
							if(faceTertiary.getAxis() == facePrimary.getAxis() || faceTertiary.getAxis() == faceSecondary.getAxis())
								continue;
							if (Misc.random.nextDouble() < chance - 1.0){
								BlockPos posTertiary = posSecondary.offset(faceTertiary);
								posList.add(posTertiary);
							}
						}
					}
				}
			}
		}
		return posList;
	}

	private HashSet<Entity> blastedEntities = new HashSet<>();
	
	@SubscribeEvent
	public void onHit(LivingHurtEvent event){
		if(!blastedEntities.contains(event.getEntity()) && event.getSource().getTrueSource() != event.getEntity() && event.getSource().getImmediateSource() != event.getEntity())
		try {
			if (event.getSource().getTrueSource() instanceof PlayerEntity) {
				PlayerEntity damager = (PlayerEntity) event.getSource().getTrueSource();
				blastedEntities.add(damager);
				ItemStack s = damager.getHeldItemMainhand();
				if (!s.isEmpty()) {
					int blastingLevel = ItemModUtil.getModifierLevel(s, EmbersAPI.BLASTING_CORE);
					if (blastingLevel > 0 && EmberInventoryUtil.getEmberTotal(damager) >= cost) {
						double resonance = EmbersAPI.getEmberEfficiency(s);
						float strength = (float) ((resonance + 1) * (Math.atan(0.6 * (blastingLevel)) / (Math.PI)));
						event.getEntityLiving().

						EmberInventoryUtil.removeEmber(damager, cost);
						List<LivingEntity> entities = damager.world.getEntitiesWithinAABB(LivingEntity.class, new AxisAlignedBB(event.getEntityLiving().func_226277_ct_()/* posX */- 4.0 * strength, event.getEntityLiving().func_226278_cu_() /* posY */ - 4.0 * strength, event.getEntityLiving().func_226281_cx_() /* posZ  */ - 4.0 * strength,
								event.getEntityLiving().func_226277_ct_()/* posX */ + 4.0 * strength, event.getEntityLiving().func_226278_cu_() /* posY */ + 4.0 * strength, event.getEntityLiving().func_226281_cx_() /* posZ  */ + 4.0 * strength));
						for (LivingEntity e : entities) {
							if (!Objects.equals(e.getUniqueID(), damager.getUniqueID())) {
								e.attackEntityFrom(DamageSource.causeExplosionDamage(damager), event.getAmount() * strength);
								e.hurtResistantTime = 0;
							}
						}
						event.getEntityLiving().world.createExplosion(event.getEntityLiving(), event.getEntityLiving().func_226277_ct_()/* posX */, event.getEntityLiving().func_226278_cu_() /* posY */ + event.getEntityLiving().getHeight() / 2.0, event.getEntityLiving().func_226281_cx_() /* posZ  */, 0.5f, true);
					}
				}
			}
			if (event.getEntity() instanceof PlayerEntity) {
				PlayerEntity damager = (PlayerEntity) event.getEntity();
				int blastingLevel = ItemModUtil.getArmorModifierLevel(damager, EmbersAPI.BLASTING_CORE);

				if (blastingLevel > 0 && EmberInventoryUtil.getEmberTotal(damager) >= cost) {
					float strength = (float) (2.0 * (Math.atan(0.6 * (blastingLevel)) / (Math.PI)));
					EmberInventoryUtil.removeEmber(damager, cost);
					List<LivingEntity> entities = damager.world.getEntitiesWithinAABB(LivingEntity.class, new AxisAlignedBB(event.getEntityLiving().func_226277_ct_()/* posX */- 4.0 * strength, event.getEntityLiving().func_226278_cu_() /* posY */ - 4.0 * strength, event.getEntityLiving().func_226281_cx_() /* posZ  */ - 4.0 * strength,
							event.getEntityLiving().func_226277_ct_()/* posX */+ 4.0 * strength, event.getEntityLiving().func_226278_cu_() /* posY */ + 4.0 * strength, event.getEntityLiving().func_226281_cx_() /* posZ  */ + 4.0 * strength));
					for (LivingEntity e : entities) {
						if (!Objects.equals(e.getUniqueID(), event.getEntity().getUniqueID())) {
							blastedEntities.add(e);
							e.attackEntityFrom(DamageSource.causeExplosionDamage(damager), event.getAmount() * strength * 0.25f);
							e.knockBack(event.getEntity(), 2.0f * strength, -e.func_226277_ct_()/* posX */+ damager.func_226277_ct_()/* posX */, -e.func_226281_cx_() /* posZ  */ + damager.func_226281_cx_() /* posZ  */);
							e.hurtResistantTime = 0;
						}
					}
					event.getEntityLiving().world.createExplosion(event.getEntityLiving(), event.getEntityLiving().func_226277_ct_()/* posX */, event.getEntityLiving().func_226278_cu_() /* posY */ + event.getEntityLiving().getHeight() / 2.0, event.getEntityLiving().func_226281_cx_() /* posZ  */, 0.5f, Explosion.Mode.BREAK);
				}
			}
		} finally {
			blastedEntities.clear();
		}
	}
	
}
