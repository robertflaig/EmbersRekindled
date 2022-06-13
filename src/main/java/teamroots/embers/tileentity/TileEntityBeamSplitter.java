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
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import teamroots.embers.EventManager;
import teamroots.embers.api.capabilities.EmbersCapabilities;
import teamroots.embers.api.power.IEmberCapability;
import teamroots.embers.api.power.IEmberPacketProducer;
import teamroots.embers.api.power.IEmberPacketReceiver;
import teamroots.embers.block.BlockBeamSplitter;
import teamroots.embers.entity.EntityEmberPacket;
import teamroots.embers.power.DefaultEmberCapability;
import teamroots.embers.util.Misc;

import javax.annotation.Nullable;
import java.util.Random;

public class TileEntityBeamSplitter extends TileEntity implements ITileEntityBase, ITickableTileEntity, IEmberPacketProducer, IEmberPacketReceiver {
	public IEmberCapability capability = new DefaultEmberCapability() {
		@Override
		public boolean acceptsVolatile() {
			return false;
		}
	};
	Random random = new Random();
	public BlockPos targetLeft = null;
	public BlockPos targetRight = null;
	long ticksExisted = 0;
	public TileEntityBeamSplitter(){
		super();
		capability.setEmberCapacity(400);
	}
	
	@Override
	public CompoundNBT write(CompoundNBT tag){
		super.write(tag);
		capability.write(tag);
		if (targetLeft != null){
			tag.setInteger("targetLeftX", targetLeft.getX());
			tag.setInteger("targetLeftY", targetLeft.getY());
			tag.setInteger("targetLeftZ", targetLeft.getZ());
		}
		if (targetRight != null){
			tag.setInteger("targetRightX", targetRight.getX());
			tag.setInteger("targetRightY", targetRight.getY());
			tag.setInteger("targetRightZ", targetRight.getZ());
		}
		return tag;
	}
	
	@Override
	public void read(CompoundNBT tag){
		super.read(tag);
		capability.read(tag);
		if (tag.contains("targetLeftX")){
			targetLeft = new BlockPos(tag.getInteger("targetLeftX"), tag.getInteger("targetLeftY"), tag.getInteger("targetLeftZ"));
		}
		if (tag.contains("targetRightX")){
			targetRight = new BlockPos(tag.getInteger("targetRightX"), tag.getInteger("targetRightY"), tag.getInteger("targetRightZ"));
		}
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
	public void tick() {
		this.ticksExisted ++;
		if (ticksExisted % 20 == 0 && !getWorld().isRemote && this.capability.getEmber() > 0) {
			TileEntity tileLeft = targetLeft != null ? getWorld().getTileEntity(targetLeft) : null;
			TileEntity tileRight = targetRight != null ? getWorld().getTileEntity(targetRight) : null;
			boolean sendLeft = (tileLeft instanceof IEmberPacketReceiver && !((IEmberPacketReceiver) tileLeft).isFull());
			boolean sendRight = (tileRight instanceof IEmberPacketReceiver && !((IEmberPacketReceiver) tileRight).isFull());
			if (!sendLeft && !sendRight)
				return;
			double amount = this.capability.getEmber();
			if (sendLeft && sendRight)
				amount /= 2.0;
			boolean isXAligned = getWorld().getBlockState(getPos()).getValue(BlockBeamSplitter.isXAligned);
			if (sendLeft) {
				EntityEmberPacket packetLeft = new EntityEmberPacket(getWorld());
				if (isXAligned) {
					packetLeft.initCustom(getPos(), targetLeft, 0, -0.01, -0.5, amount);
				} else {
					packetLeft.initCustom(getPos(), targetLeft, -0.5, -0.01, 0, amount);
				}
				getWorld().spawnEntity(packetLeft);
			}
			if (sendRight) {
				EntityEmberPacket packetRight = new EntityEmberPacket(getWorld());
				if (isXAligned) {
					packetRight.initCustom(getPos(), targetRight, 0, -0.01, 0.5, amount);
				} else {
					packetRight.initCustom(getPos(), targetRight, 0.5, -0.01, 0, amount);
				}
				getWorld().spawnEntity(packetRight);
			}
			this.capability.setEmber(0);
			markDirty();
		}
	}

	@Override
	public boolean isFull() {
		return capability.getEmber() >= capability.getEmberCapacity();
	}

	@Override
	public boolean onReceive(EntityEmberPacket packet) {
		return true;
	}

	@Override
	public void markDirty() {
		super.markDirty();
		Misc.syncTE(this);
	}

	@Override
	public void setTargetPosition(BlockPos pos, Direction side) {
		if(pos.equals(getPos()))
			return;
		IBlockState state = getWorld().getBlockState(getPos());
		if (state.getValue(BlockBeamSplitter.isXAligned)){
			if (side == Direction.NORTH){
				targetLeft = pos;
				markDirty();
			}
			if (side == Direction.SOUTH){
				targetRight = pos;
				markDirty();
			}
		}
		else {
			if (side == Direction.WEST){
				targetLeft = pos;
				markDirty();
			}
			if (side == Direction.EAST){
				targetRight = pos;
				markDirty();
			}
		}
	}
}
