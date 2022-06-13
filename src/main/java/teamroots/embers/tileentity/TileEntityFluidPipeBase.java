package teamroots.embers.tileentity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import teamroots.embers.Embers;
import teamroots.embers.particle.ParticleUtil;
import teamroots.embers.util.EnumPipeConnection;
import teamroots.embers.util.Misc;
import teamroots.embers.util.PipePriorityMap;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Random;

public abstract class TileEntityFluidPipeBase extends TileEntity implements ITileEntityBase, ITickableTileEntity, IFluidPipeConnectable, IFluidPipePriority {
    public static final int PRIORITY_BLOCK = 0;
    public static final int PRIORITY_PIPE = PRIORITY_BLOCK;
    public static final int MAX_PUSH = 120;

    Random random = new Random();
    boolean[] from = new boolean[Direction.VALUES.length];
    boolean clogged = false;
    public FluidTank tank;
    Direction lastTransfer;
    boolean syncTank;
    boolean syncCloggedFlag;
    boolean syncTransfer;
    int ticksExisted;
    int lastRobin;

    protected TileEntityFluidPipeBase() {
        initFluidTank();
    }

    protected void initFluidTank() {
        tank = new FluidTank(getCapacity()) {
            @Override
            protected void onContentsChanged() {
                markDirty();
            }
        };
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        if (requiresSync()) {
            CompoundNBT updateTag = getSyncTag();
            resetSync();
            return new SUpdateTileEntityPacket(getPos(), 0, updateTag);
        }
        return null;
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        read(pkt.getNbtCompound());
    }

    abstract int getCapacity();

    @Override
    public int getPriority(Direction facing) {
        return PRIORITY_PIPE;
    }

    public abstract EnumPipeConnection getInternalConnection(Direction facing);

    abstract void setInternalConnection(Direction facing, EnumPipeConnection connection);

    /**
     * @param facing
     * @return Whether items can be transferred through this side
     */
    abstract boolean isConnected(Direction facing);

    public void setFrom(Direction facing, boolean flag) {
        from[facing.getIndex()] = flag;
    }

    public void resetFrom() {
        for (Direction facing : Direction.VALUES) {
            setFrom(facing, false);
        }
    }

    protected boolean isFrom(Direction facing) {
        return from[facing.getIndex()];
    }

    protected boolean isAnySideUnclogged()
    {
        for (Direction facing : Direction.VALUES) {
            if (!isConnected(facing))
                continue;
            TileEntity tile = world.getTileEntity(pos.offset(facing));
            if (tile instanceof TileEntityFluidPipeBase && !((TileEntityFluidPipeBase) tile).clogged)
                return true;
        }
        return false;
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            ticksExisted++;
            boolean fluidMoved = false;
            FluidStack passStack = this.tank.drain(MAX_PUSH, false);
            if (passStack != null) {
                PipePriorityMap<Integer, Direction> possibleDirections = new PipePriorityMap<>();
                IFluidHandler[] fluidHandlers = new IFluidHandler[Direction.VALUES.length];

                for (Direction facing : Direction.VALUES) {
                    if (!isConnected(facing))
                        continue;
                    if (isFrom(facing))
                        continue;
                    TileEntity tile = world.getTileEntity(pos.offset(facing));
                    if (tile != null && tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite())) {
                        IFluidHandler handler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite());
                        int priority = PRIORITY_BLOCK;
                        if (tile instanceof IFluidPipePriority)
                            priority = ((IFluidPipePriority) tile).getPriority(facing.getOpposite());
                        if (isFrom(facing.getOpposite()))
                            priority -= 5; //aka always try opposite first
                        possibleDirections.put(priority, facing);
                        fluidHandlers[facing.getIndex()] = handler;
                    }
                }

                for (int key : possibleDirections.keySet()) {
                    ArrayList<Direction> list = possibleDirections.get(key);
                    for (int i = 0; i < list.size(); i++) {
                        Direction facing = list.get((i + lastRobin) % list.size());
                        IFluidHandler handler = fluidHandlers[facing.getIndex()];
                        fluidMoved = pushStack(passStack, facing, handler);
                        if (lastTransfer != facing) {
                            syncTransfer = true;
                            lastTransfer = facing;
                            markDirty();
                        }
                        if (fluidMoved) {
                            lastRobin++;
                            break;
                        }
                    }
                    if (fluidMoved)
                        break;
                }
            }

            //if (fluidMoved)
            //    resetFrom();
            if (tank.getFluidAmount() <= 0) {
                if (lastTransfer != null && !fluidMoved) {
                    syncTransfer = true;
                    lastTransfer = null;
                    markDirty();
                }
                fluidMoved = true;
                resetFrom();
            }
            if (clogged == fluidMoved) {
                clogged = !fluidMoved;
                syncCloggedFlag = true;
                markDirty();
            }
        } else if (Embers.proxy.isPlayerWearingGoggles()) {
            if (lastTransfer != null) {
                for (int i = 0; i < 3; i++) {
                    float dist = random.nextFloat() * 0.0f;
                    int lifetime = 10;
                    float vx = lastTransfer.getFrontOffsetX() / (float) (lifetime / (1 - dist));
                    float vy = lastTransfer.getFrontOffsetY() / (float) (lifetime / (1 - dist));
                    float vz = lastTransfer.getFrontOffsetZ() / (float) (lifetime / (1 - dist));
                    float x = pos.getX() + 0.4f + random.nextFloat() * 0.2f + lastTransfer.getFrontOffsetX() * dist;
                    float y = pos.getY() + 0.4f + random.nextFloat() * 0.2f + lastTransfer.getFrontOffsetY() * dist;
                    float z = pos.getZ() + 0.4f + random.nextFloat() * 0.2f + lastTransfer.getFrontOffsetZ() * dist;
                    float r = clogged ? 255f : 16f;
                    float g = clogged ? 16f : 255f;
                    float b = 16f;
                    float size = random.nextFloat() * 2 + 2;
                    ParticleUtil.spawnParticlePipeFlow(world, x, y, z, vx, vy, vz, r, g, b, 0.5f, size, lifetime);
                }
            }
        }
    }

    private boolean pushStack(FluidStack passStack, Direction facing, IFluidHandler handler) {
        int added = handler.fill(passStack, false);
        if (added > 0) {
            handler.fill(passStack, true);
            this.tank.drain(added, true);
            passStack.amount -= added;
            return passStack.amount <= 0;
        }

        if (isFrom(facing))
            setFrom(facing, false);
        return false;
    }

    protected void resetSync() {
        syncTank = false;
        syncCloggedFlag = false;
        syncTransfer = false;
    }

    protected boolean requiresSync() {
        return syncTank || syncCloggedFlag || syncTransfer;
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    protected CompoundNBT getSyncTag() {
        CompoundNBT compound = new CompoundNBT();
        if (syncTank)
            writeTank(compound);
        if (syncCloggedFlag)
            writeCloggedFlag(compound);
        if (syncTransfer)
            writeLastTransfer(compound);
        return compound;
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        writeTank(tag);
        writeCloggedFlag(tag);
        writeLastTransfer(tag);
        for (Direction facing : Direction.VALUES)
            tag.setBoolean("from" + facing.getIndex(), from[facing.getIndex()]);
        tag.setInteger("lastRobin",lastRobin);
        return tag;
    }

    private void writeCloggedFlag(CompoundNBT tag) {
        tag.setBoolean("clogged", clogged);
    }

    private void writeLastTransfer(CompoundNBT tag) {
        tag.setInteger("lastTransfer", Misc.writeNullableFacing(lastTransfer));
    }

    private void writeTank(CompoundNBT tag) {
        tag.setTag("tank", tank.write(new CompoundNBT()));
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        if (tag.contains("clogged"))
            clogged = tag.getBoolean("clogged");
        if (tag.contains("tank"))
            tank.read(tag.getCompoundTag("tank"));
        if (tag.contains("lastTransfer"))
            lastTransfer = Misc.readNullableFacing(tag.getInteger("lastTransfer"));
        for (Direction facing : Direction.VALUES)
            if (tag.contains("from" + facing.getIndex()))
                from[facing.getIndex()] = tag.getBoolean("from" + facing.getIndex());
        if (tag.contains("lastRobin"))
            lastRobin = tag.getInteger("lastRobin");
    }
}
