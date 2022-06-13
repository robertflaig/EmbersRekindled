package teamroots.embers.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import teamroots.embers.Embers;
import teamroots.embers.EventManager;
import teamroots.embers.SoundManager;
import teamroots.embers.api.capabilities.EmbersCapabilities;
import teamroots.embers.api.event.EmberEvent;
import teamroots.embers.api.power.IEmberCapability;
import teamroots.embers.api.tile.IEmberInjectable;
import teamroots.embers.api.upgrades.IUpgradeProvider;
import teamroots.embers.api.upgrades.UpgradeUtil;
import teamroots.embers.block.BlockEmberInjector;
import teamroots.embers.particle.ParticleUtil;
import teamroots.embers.power.DefaultEmberCapability;
import teamroots.embers.util.Misc;
import teamroots.embers.util.sound.ISoundController;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class TileEntityEmberInjector extends TileEntity implements ITileEntityBase, ITickableTileEntity, ISoundController {
	public IEmberCapability capability = new DefaultEmberCapability();
	protected int ticksExisted = 0;
	protected int progress = -1;
	protected Random random = new Random();
	public static final double EMBER_COST = 1.0;

	public static final int SOUND_PROCESS = 1;
	public static final int[] SOUND_IDS = new int[]{SOUND_PROCESS};

	HashSet<Integer> soundsPlaying = new HashSet<>();
	boolean isWorking;

	public TileEntityEmberInjector(){
		super();
		capability.setEmberCapacity(24000);
		capability.setEmber(0);
	}

	@Override
	public void markDirty() {
		super.markDirty();
		Misc.syncTE(this);
	}
	
	@Override
	public CompoundNBT write(CompoundNBT tag){
		super.write(tag);
		capability.write(tag);
		return tag;
	}
	
	@Override
	public void read(CompoundNBT tag){
		super.read(tag);
		capability.read(tag);
	}

	@Override
	public CompoundNBT getUpdateTag() {
		return write(new CompoundNBT());
	}

	@Nullable
	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(getPos(), 0, getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		read(pkt.getNbtCompound());
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, Direction facing){
		if (capability == EmbersCapabilities.EMBER_CAPABILITY){
			return true;
		}
		return super.hasCapability(capability, facing);
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, Direction facing){
		if (capability == EmbersCapabilities.EMBER_CAPABILITY){
			return (T)this.capability;
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public boolean activate(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand,
			Direction side, float hitX, float hitY, float hitZ) {
		return false;
	}

	@Override
	public void onHarvest(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		this.remove();
		world.setTileEntity(pos, null);
	}
	
	@Override
	public void tick(){
		List<IUpgradeProvider> upgrades = UpgradeUtil.getUpgrades(world, pos, Direction.VALUES);
		UpgradeUtil.verifyUpgrades(this, upgrades);
		if (UpgradeUtil.doTick(this, upgrades))
			return;
		if(getWorld().isRemote)
			handleSound();
		IBlockState state = world.getBlockState(getPos());
		TileEntity tile = world.getTileEntity(pos.offset(state.getValue(BlockEmberInjector.facing)));
		isWorking = false;
		double emberCost = UpgradeUtil.getTotalEmberConsumption(this,EMBER_COST,upgrades) * UpgradeUtil.getTotalSpeedModifier(this,upgrades);
		if (tile instanceof IEmberInjectable && ((IEmberInjectable) tile).isValid() && capability.getEmber() > emberCost){
			boolean cancel = UpgradeUtil.doWork(this,upgrades);
			if(!cancel) {
				((IEmberInjectable) tile).inject(this, emberCost);
				UpgradeUtil.throwEvent(this, new EmberEvent(this, EmberEvent.EnumType.CONSUME, emberCost), upgrades);
				this.capability.removeAmount(emberCost, true);
				isWorking = true;
				markDirty();
				if (world.isRemote) {
					for (int i = 0; i < 2; i++) {
						ParticleUtil.spawnParticleLineGlow(world, pos.getX() + 0.5f + 0.25f * (random.nextFloat() - 0.5f), pos.getY() + 0.625f, pos.getZ() + 0.5f + 0.25f * (random.nextFloat() - 0.5f), tile.getPos().getX() + 0.5f + state.getValue(BlockEmberInjector.facing).getDirectionVec().getX() + 0.5f * (random.nextFloat() - 0.5f), tile.getPos().getY() + 0.5f + state.getValue(BlockEmberInjector.facing).getDirectionVec().getY() + 0.5f * (random.nextFloat() - 0.5f), tile.getPos().getZ() + 0.5f + state.getValue(BlockEmberInjector.facing).getDirectionVec().getZ() + 0.5f * (random.nextFloat() - 0.5f), 255, 64, 16, 4.0f + random.nextFloat() * 2.0f, 40);
					}
				}
			}
		}
	}

	@Override
	public void playSound(int id) {
		switch (id) {
			case SOUND_PROCESS:
				Embers.proxy.playMachineSound(this, SOUND_PROCESS, SoundManager.INJECTOR_LOOP, SoundCategory.BLOCKS, true, 1.0f, 1.0f, (float)pos.getX()+0.5f,(float)pos.getY()+0.5f,(float)pos.getZ()+0.5f);
				break;
		}
		soundsPlaying.add(id);
	}

	@Override
	public void stopSound(int id) {
		soundsPlaying.remove(id);
	}

	@Override
	public boolean isSoundPlaying(int id) {
		return soundsPlaying.contains(id);
	}

	@Override
	public int[] getSoundIDs() {
		return SOUND_IDS;
	}

	@Override
	public boolean shouldPlaySound(int id) {
		return id == SOUND_PROCESS && isWorking;
	}
}
