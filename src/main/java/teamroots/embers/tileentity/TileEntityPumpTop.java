package teamroots.embers.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
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
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.TileFluidHandler;
import teamroots.embers.EventManager;
import teamroots.embers.api.tile.IExtraCapabilityInformation;
import teamroots.embers.util.EnumPipeConnection;
import teamroots.embers.util.Misc;

import javax.annotation.Nullable;
import java.util.List;

public class TileEntityPumpTop extends TileFluidHandler implements ITileEntityBase, ITickableTileEntity, IFluidPipeConnectable, IExtraCapabilityInformation {
	public static int capacity = Fluid.BUCKET_VOLUME*8;
	public static int TRANSFER_SPEED = Fluid.BUCKET_VOLUME/4;
	
	public TileEntityPumpTop(){
		super();
		tank = new FluidTank(capacity);
		tank.setTileEntity(this);
		tank.setCanFill(true);
		tank.setCanDrain(true);
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
	
	public int getCapacity(){
		return tank.getCapacity();
	}
	
	public int getAmount(){
		return tank.getFluidAmount();
	}
	
	public FluidTank getTank(){
		return tank;
	}
	
	public Fluid getFluid(){
		if (tank.getFluid() != null){
			return tank.getFluid().getFluid();
		}
		return null;
	}

	@Override
	public void onHarvest(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		this.remove();
		world.setTileEntity(pos, null);
	}

	@Override
	public void tick() {
		for (Direction facing : Direction.HORIZONTALS) {
			TileEntity tile = world.getTileEntity(pos.offset(facing));
			if(tile != null) {
				IFluidHandler handler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite());
				if (handler != null) {
					FluidStack stack = tank.drain(TRANSFER_SPEED,false);
					if(stack != null) {
						int pushed = handler.fill(stack, true);
						tank.drain(pushed, true);
					}
				}
			}
		}
	}

	@Override
	public void markDirty() {
		super.markDirty();
		Misc.syncTE(this);
	}

	@Override
	public boolean hasCapabilityDescription(Capability<?> capability) {
		return true;
	}

	@Override
	public void addCapabilityDescription(List<String> strings, Capability<?> capability, Direction facing) {
		strings.add(IExtraCapabilityInformation.formatCapability(EnumIOType.OUTPUT,"embers.tooltip.goggles.fluid",I18n.format("embers.tooltip.goggles.fluid.water")));
	}

	@Override
	public EnumPipeConnection getConnection(Direction facing) {
		if(facing.getAxis() == Direction.Axis.Y)
			return EnumPipeConnection.BLOCK;
		else
			return EnumPipeConnection.PIPE;
	}
}
