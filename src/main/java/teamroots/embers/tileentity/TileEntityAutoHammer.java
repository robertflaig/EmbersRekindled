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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import teamroots.embers.EventManager;
import teamroots.embers.api.capabilities.EmbersCapabilities;
import teamroots.embers.api.event.DialInformationEvent;
import teamroots.embers.api.event.EmberEvent;
import teamroots.embers.api.event.MachineRecipeEvent;
import teamroots.embers.api.power.IEmberCapability;
import teamroots.embers.api.tile.IExtraDialInformation;
import teamroots.embers.api.tile.IHammerable;
import teamroots.embers.api.tile.IMechanicallyPowered;
import teamroots.embers.api.upgrades.IUpgradeProvider;
import teamroots.embers.api.upgrades.UpgradeUtil;
import teamroots.embers.block.BlockAutoHammer;
import teamroots.embers.power.DefaultEmberCapability;
import teamroots.embers.util.Misc;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TileEntityAutoHammer extends TileEntity implements ITileEntityBase, ITickableTileEntity, IMechanicallyPowered, IExtraDialInformation {
	public static final double EMBER_COST = 40.0;
	public static final int PROCESS_TIME = 20;
	public IEmberCapability capability = new DefaultEmberCapability();
	int ticksExisted = 0;
	int progress = -1;
	Random random = new Random();
	private List<IUpgradeProvider> upgrades = new ArrayList<>();

	public TileEntityAutoHammer(){
		super();
		capability.setEmberCapacity(12000);
		capability.setEmber(0);
	}
	
	@Override
	public CompoundNBT write(CompoundNBT tag){
		super.write(tag);
		capability.write(tag);
		tag.setInteger("progress", progress);
		return tag;
	}
	
	@Override
	public void read(CompoundNBT tag){
		super.read(tag);
		capability.read(tag);
		progress = tag.getInteger("progress");
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
	public void tick() {
		IBlockState state = world.getBlockState(getPos());
		Direction facing = state.getValue(BlockAutoHammer.facing);
		upgrades = UpgradeUtil.getUpgrades(world, pos, new Direction[]{facing.getOpposite()});
		UpgradeUtil.verifyUpgrades(this, upgrades);
		if (UpgradeUtil.doTick(this, upgrades))
			return;
		ticksExisted++;
		double ember_cost = UpgradeUtil.getTotalEmberConsumption(this, EMBER_COST, upgrades);
		TileEntity tile = world.getTileEntity(getPos().down().offset(facing));
		if (tile instanceof IHammerable) {
			IHammerable hammerable = (IHammerable) tile;
			boolean redstoneEnabled = getWorld().isBlockPowered(getPos());
			if (hammerable.isValid() && redstoneEnabled && capability.getEmber() >= ember_cost) {
				boolean cancel = UpgradeUtil.doWork(this, upgrades);
				if (!cancel && progress == -1 && ticksExisted % UpgradeUtil.getWorkTime(this,PROCESS_TIME, upgrades) == 0) {
					progress = 10;
					markDirty();
				}
			}
			if (progress > 0) {
				progress--;
				if (progress == 5) {
					if (capability.getEmber() >= ember_cost) {
						UpgradeUtil.throwEvent(this, new EmberEvent(this, EmberEvent.EnumType.CONSUME, ember_cost), upgrades);
						capability.removeAmount(ember_cost, true);
						hammerable.onHit(this);
					}
				}
				markDirty();
			}
		}
		if (progress == 0){
			progress = -1;
			markDirty();
		}
	}

	@Override
	public void markDirty() {
		super.markDirty();
		Misc.syncTE(this);
	}

	@Override
	public double getMechanicalSpeed(double power) {
		return Misc.getDiminishedPower(power,20,1.5/20);
	}

	@Override
	public double getNominalSpeed() {
		return 1;
	}

	@Override
	public double getMinimumPower() {
		return 10;
	}

	@Override
	public void addDialInformation(Direction facing, List<String> information, String dialType) {
		UpgradeUtil.throwEvent(this,new DialInformationEvent(this,information,dialType),upgrades);
	}
}
