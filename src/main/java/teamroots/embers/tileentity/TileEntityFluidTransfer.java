package teamroots.embers.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import teamroots.embers.block.BlockFluidTransfer;
import teamroots.embers.util.EnumPipeConnection;
import teamroots.embers.util.Misc;

import java.util.Random;

public class TileEntityFluidTransfer extends TileEntityFluidPipeBase {
    public static final int PRIORITY_TRANSFER = -10;
    public FluidStack filterFluid = null;
    Random random = new Random();
    boolean syncFilter;
    IFluidHandler outputSide;

    public TileEntityFluidTransfer() {
        super();
    }

    @Override
    protected void initFluidTank() {
        tank = new FluidTank(getCapacity()) {
            @Override
            protected void onContentsChanged() {
                markDirty();
            }

            @Override
            public int fill(FluidStack resource, boolean doFill) {
                if(filterFluid != null) {
                    if(resource != null) {
                        if (filterFluid.tag != null ? resource.isFluidEqual(filterFluid) : resource.getFluid() == filterFluid.getFluid()) {
                            return super.fill(resource, doFill);
                        }
                    }
                    return 0;
                }
                return super.fill(resource, doFill);
            }
        };
        outputSide = Misc.makeRestrictedFluidHandler(tank,false,true);
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        writeFilter(tag);
        return tag;
    }

    private void writeFilter(CompoundNBT tag) {
        if (filterFluid != null) {
            tag.setTag("filter", filterFluid.write(new CompoundNBT()));
        } else {
            tag.setString("filter", "empty");
        }
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        if (tag.contains("filter")) {
            filterFluid = FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("filter"));
        }
    }

    @Override
    public CompoundNBT getSyncTag() {
        CompoundNBT compound = super.getUpdateTag();
        if (syncFilter)
            writeFilter(compound);
        return compound;
    }

    @Override
    protected boolean requiresSync() {
        return syncFilter || super.requiresSync();
    }

    @Override
    protected void resetSync() {
        super.resetSync();
        syncFilter = false;
    }

    @Override
    int getCapacity() {
        return 240;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, Direction facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            if (facing == null || facing.getAxis() == getFacing().getAxis())
                return true;
            else
                return false;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, Direction facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            Direction transferFacing = getFacing();
            if (facing == transferFacing)
                return (T) this.outputSide;
            else if (facing == null || facing.getAxis() == transferFacing.getAxis())
                return (T) this.tank;
            else
                return null;
        }
        return super.getCapability(capability, facing);
    }

    private Direction getFacing() {
        IBlockState state = getWorld().getBlockState(getPos());
        return state.getValue(BlockFluidTransfer.facing);
    }

    @Override
    public boolean activate(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand,
                            Direction side, float hitX, float hitY, float hitZ) {
        ItemStack heldItem = player.getHeldItem(hand);
        if (!world.isRemote) {
            if (FluidUtil.getFluidHandler(heldItem) != null) {
                this.filterFluid = FluidUtil.getFluidContained(heldItem);
                world.setBlockState(pos, state.withProperty(BlockFluidTransfer.filter, true), 10);
            } else {
                this.filterFluid = null;
                world.setBlockState(pos, state.withProperty(BlockFluidTransfer.filter, false), 10);
            }
            syncFilter = true;
            markDirty();
            return true;
        }
        return true;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }

    @Override
    public void onHarvest(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        this.remove();
        world.setTileEntity(pos, null);
    }

    @Override
    public void update() {
        if (world.isRemote && clogged && isAnySideUnclogged())
            Misc.spawnClogParticles(world,pos,2, 0.7f);
        super.update();
    }

    @Override
    public int getPriority(Direction facing) {
        return PRIORITY_TRANSFER;
    }

    @Override
    public EnumPipeConnection getInternalConnection(Direction facing) {
        return EnumPipeConnection.NONE;
    }

    @Override
    void setInternalConnection(Direction facing, EnumPipeConnection connection) {
        //NOOP
    }

    @Override
    boolean isConnected(Direction facing) {
        return getFacing().getAxis() == facing.getAxis();
    }

    @Override
    protected boolean isFrom(Direction facing) {
        return facing == getFacing().getOpposite();
    }

    @Override
    public void markDirty() {
        super.markDirty();
        Misc.syncTE(this);
    }

    @Override
    public EnumPipeConnection getConnection(Direction facing) {
        if(getFacing().getAxis() == facing.getAxis())
            return EnumPipeConnection.PIPE;
        return EnumPipeConnection.NONE;
    }
}
