package teamroots.embers.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import teamroots.embers.SoundManager;
import teamroots.embers.item.ItemTinkerHammer;
import teamroots.embers.util.EnumPipeConnection;
import teamroots.embers.util.Misc;

import javax.annotation.Nullable;
import java.util.Random;

public class TileEntityFluidExtractor extends TileEntityFluidPipeBase {
    Random random = new Random();
    EnumPipeConnection[] connections = new EnumPipeConnection[Direction.VALUES.length];
    IFluidHandler[] sideHandlers;
    boolean syncConnections;
    boolean active;
    public static final int MAX_DRAIN = 120;

    public TileEntityFluidExtractor() {
        super();
    }

    @Override
    protected void initFluidTank() {
        super.initFluidTank();
        sideHandlers = new IFluidHandler[Direction.VALUES.length];
        for (Direction facing : Direction.VALUES) {
            sideHandlers[facing.getIndex()] = new IFluidHandler() {
                @Override
                public IFluidTankProperties[] getTankProperties() {
                    return tank.getTankProperties();
                }

                @Override
                public int fill(FluidStack resource, boolean doFill) {
                    if (active)
                        return 0;
                    if (doFill)
                        setFrom(facing, true);
                    return tank.fill(resource, doFill);
                }

                @Nullable
                @Override
                public FluidStack drain(FluidStack resource, boolean doDrain) {
                    return tank.drain(resource, doDrain);
                }

                @Nullable
                @Override
                public FluidStack drain(int maxDrain, boolean doDrain) {
                    return tank.drain(maxDrain, doDrain);
                }
            };
        }
    }

    public void updateNeighbors(IBlockAccess world) {
        for (Direction facing : Direction.VALUES) {
            setInternalConnection(facing, getConnection(world, getPos().offset(facing), facing));
        }
        syncConnections = true;
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        writeConnections(tag);
        return tag;
    }

    private void writeConnections(CompoundNBT tag) {
        tag.setInteger("up", getInternalConnection(Direction.UP).getIndex());
        tag.setInteger("down", getInternalConnection(Direction.DOWN).getIndex());
        tag.setInteger("north", getInternalConnection(Direction.NORTH).getIndex());
        tag.setInteger("south", getInternalConnection(Direction.SOUTH).getIndex());
        tag.setInteger("west", getInternalConnection(Direction.WEST).getIndex());
        tag.setInteger("east", getInternalConnection(Direction.EAST).getIndex());
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        if (tag.contains("up"))
            setInternalConnection(Direction.UP, EnumPipeConnection.fromIndex(tag.getInteger("up")));
        if (tag.contains("down"))
            setInternalConnection(Direction.DOWN, EnumPipeConnection.fromIndex(tag.getInteger("down")));
        if (tag.contains("north"))
            setInternalConnection(Direction.NORTH, EnumPipeConnection.fromIndex(tag.getInteger("north")));
        if (tag.contains("south"))
            setInternalConnection(Direction.SOUTH, EnumPipeConnection.fromIndex(tag.getInteger("south")));
        if (tag.contains("west"))
            setInternalConnection(Direction.WEST, EnumPipeConnection.fromIndex(tag.getInteger("west")));
        if (tag.contains("east"))
            setInternalConnection(Direction.EAST, EnumPipeConnection.fromIndex(tag.getInteger("east")));
    }

    @Override
    public CompoundNBT getSyncTag() {
        CompoundNBT compound = super.getUpdateTag();
        if (syncConnections)
            writeConnections(compound);
        return compound;
    }

    @Override
    protected boolean requiresSync() {
        return syncConnections || super.requiresSync();
    }

    @Override
    protected void resetSync() {
        super.resetSync();
        syncConnections = false;
    }

    @Override
    int getCapacity() {
        return 240;
    }

    public EnumPipeConnection getConnection(Direction side) {
        if (getInternalConnection(side) == EnumPipeConnection.FORCENONE)
            return EnumPipeConnection.NEIGHBORNONE;
        return EnumPipeConnection.PIPE;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, Direction facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, Direction facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            if (facing == null)
                return (T) this.tank;
            else
                return (T) this.sideHandlers[facing.getIndex()];
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public EnumPipeConnection getInternalConnection(Direction facing) {
        return connections[facing.getIndex()] != null ? connections[facing.getIndex()] : EnumPipeConnection.NONE;
    }

    @Override
    void setInternalConnection(Direction facing, EnumPipeConnection connection) {
        connections[facing.getIndex()] = connection;
    }

    @Override
    boolean isConnected(Direction facing) {
        return getInternalConnection(facing).canTransfer();
    }

    public EnumPipeConnection getConnection(IBlockAccess world, BlockPos pos, Direction side) {
        TileEntity tile = world.getTileEntity(pos);
        if (getInternalConnection(side) == EnumPipeConnection.FORCENONE) {
            return EnumPipeConnection.FORCENONE;
        } else if (tile instanceof TileEntityFluidExtractor) {
            return EnumPipeConnection.NONE;
        } else if (tile instanceof IFluidPipeConnectable) {
            return ((IFluidPipeConnectable) tile).getConnection(side.getOpposite());
        } else if (tile != null && tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite())) {
            return EnumPipeConnection.BLOCK;
        } else if (Misc.isValidLever(world, pos, side)) {
            return EnumPipeConnection.LEVER;
        }
        return EnumPipeConnection.NONE;
    }

    public static EnumPipeConnection reverseForce(EnumPipeConnection connect) {
        if (connect == EnumPipeConnection.FORCENONE) {
            return EnumPipeConnection.NONE;
        } else if (connect != EnumPipeConnection.NONE && connect != EnumPipeConnection.LEVER) {
            return EnumPipeConnection.FORCENONE;
        }
        return EnumPipeConnection.NONE;
    }

    public void reverseConnection(Direction face) {
        EnumPipeConnection connection = getInternalConnection(face);
        setInternalConnection(face, reverseForce(connection));
        TileEntity tile = world.getTileEntity(pos.offset(face));
        if(tile instanceof TileEntityFluidPipe)
            ((TileEntityFluidPipe) tile).updateNeighbors(world);
        if(tile instanceof TileEntityFluidExtractor)
            ((TileEntityFluidExtractor) tile).updateNeighbors(world);
        if (connection == EnumPipeConnection.FORCENONE) {
            world.playSound(null, pos, SoundManager.PIPE_CONNECT, SoundCategory.BLOCKS, 1.0f, 1.0f);
        } else if (connection != EnumPipeConnection.NONE && connection != EnumPipeConnection.LEVER) {
            world.playSound(null, pos, SoundManager.PIPE_DISCONNECT, SoundCategory.BLOCKS, 1.0f, 1.0f);
        }
    }

    @Override
    public boolean activate(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand,
                            Direction side, float hitX, float hitY, float hitZ) {
        ItemStack heldItem = player.getHeldItem(hand);
        if (!heldItem.isEmpty() && heldItem.getItem() instanceof ItemTinkerHammer) {
            if (side == Direction.UP || side == Direction.DOWN) {
                if (Math.abs(hitX - 0.5) > Math.abs(hitZ - 0.5)) {
                    if (hitX < 0.5) {
                        this.reverseConnection(Direction.WEST);
                    } else {
                        this.reverseConnection(Direction.EAST);
                    }
                } else {
                    if (hitZ < 0.5) {
                        this.reverseConnection(Direction.NORTH);
                    } else {
                        this.reverseConnection(Direction.SOUTH);
                    }
                }
            }
            if (side == Direction.EAST || side == Direction.WEST) {
                if (Math.abs(hitY - 0.5) > Math.abs(hitZ - 0.5)) {
                    if (hitY < 0.5) {
                        this.reverseConnection(Direction.DOWN);
                    } else {
                        this.reverseConnection(Direction.UP);
                    }
                } else {
                    if (hitZ < 0.5) {
                        this.reverseConnection(Direction.NORTH);
                    } else {
                        this.reverseConnection(Direction.SOUTH);
                    }
                }
            }
            if (side == Direction.NORTH || side == Direction.SOUTH) {
                if (Math.abs(hitX - 0.5) > Math.abs(hitY - 0.5)) {
                    if (hitX < 0.5) {
                        this.reverseConnection(Direction.WEST);
                    } else {
                        this.reverseConnection(Direction.EAST);
                    }
                } else {
                    if (hitY < 0.5) {
                        this.reverseConnection(Direction.DOWN);
                    } else {
                        this.reverseConnection(Direction.UP);
                    }
                }
            }
            updateNeighbors(world);
            markDirty();
            return true;
        }
        return false;
    }

    @Override
    public void onHarvest(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        this.remove();
        world.setTileEntity(pos, null);
    }

    @Override
    public void update() {
        if (world.isRemote && clogged)
            Misc.spawnClogParticles(world, pos, 1, 0.25f);
        if (!world.isRemote) {
            active = getWorld().isBlockPowered(getPos());
            for (Direction facing : Direction.VALUES) {
                if (!isConnected(facing))
                    continue;
                TileEntity tile = world.getTileEntity(pos.offset(facing));
                if (tile != null && !(tile instanceof TileEntityFluidPipeBase) && tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite())) {
                    if (active) {
                        IFluidHandler handler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite());
                        if (handler.drain(MAX_DRAIN, false) != null) {
                            FluidStack extracted = handler.drain(MAX_DRAIN, false);
                            int filled = this.tank.fill(extracted, false);
                            if (filled > 0) {
                                this.tank.fill(extracted, true);
                                handler.drain(filled, true);
                            }
                        }
                        setFrom(facing, true);
                    } else {
                        setFrom(facing, false);
                    }
                }
            }
        }
        super.update();
    }

    @Override
    public void markDirty() {
        super.markDirty();
        Misc.syncTE(this);
    }
}
