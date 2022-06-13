package teamroots.embers.tileentity;

import net.minecraft.block.state.BlockFaceShape;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import teamroots.embers.util.EnumPipeConnection;
import teamroots.embers.util.Misc;

import javax.annotation.Nullable;
import java.util.Random;

public class TileEntityFluidDropper extends TileEntity implements ITileEntityBase, ITickableTileEntity, IFluidPipeConnectable, IFluidPipePriority {
	Random random = new Random();
	FluidTank tank = new FluidTank(1000);

	public TileEntityFluidDropper(){
		super();
	}
	
	@Override
	public CompoundNBT write(CompoundNBT tag){
		super.write(tag);
		tag.setTag("tank", tank.write(new CompoundNBT()));
		return tag;
	}
	
	@Override
	public void read(CompoundNBT tag){
		super.read(tag);
		tank.read(tag.getCompoundTag("tank"));
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
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing == Direction.UP){
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	public void markDirty() {
		super.markDirty();
		Misc.syncTE(this);
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, Direction facing){
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing == Direction.UP){
			return (T)this.tank;
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
		if(!world.isRemote) {
			Direction facing = Direction.DOWN;
			for (int i = 1; i <= 5; i++) {
				BlockPos checkPos = pos.offset(facing, i);
				TileEntity tile = world.getTileEntity(checkPos);
				if (tile != null) {
					IFluidHandler handler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite());
					if (handler != null) {
						FluidStack stack = tank.drain(tank.getCapacity(), false);
						if (stack != null) {
							int pushed = handler.fill(stack, false);
							if (pushed > 0) {
								handler.fill(stack, true);
								tank.drain(pushed, true);
							}
						}
					}
				}
				IBlockState state = world.getBlockState(checkPos);
				if (state.getBlockFaceShape(world, checkPos, Direction.UP) == BlockFaceShape.SOLID || state.getBlockFaceShape(world, checkPos, Direction.DOWN) == BlockFaceShape.SOLID)
					break;
			}
		}
	}

	@Override
	public EnumPipeConnection getConnection(Direction facing) {
		if(facing == Direction.UP)
			return EnumPipeConnection.PIPE;
		return EnumPipeConnection.NONE;
	}

	@Override
	public int getPriority(Direction facing) {
		return 50;
	}
}
