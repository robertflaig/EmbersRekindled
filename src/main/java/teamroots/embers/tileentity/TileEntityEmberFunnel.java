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
import teamroots.embers.api.capabilities.EmbersCapabilities;
import teamroots.embers.api.power.IEmberCapability;
import teamroots.embers.api.power.IEmberPacketReceiver;
import teamroots.embers.block.BlockEmberEmitter;
import teamroots.embers.entity.EntityEmberPacket;
import teamroots.embers.power.DefaultEmberCapability;
import teamroots.embers.util.Misc;

import javax.annotation.Nullable;

public class TileEntityEmberFunnel extends TileEntity implements ITileEntityBase, ITickableTileEntity, IEmberPacketReceiver {
    public static final int TRANSFER_SPEED = 100; //It has 2000 capacity c'mon it needs to push super fast
    public IEmberCapability capability = new DefaultEmberCapability(){
        @Override
        public void onContentsChanged() {
            markDirty();
        }

        @Override
        public boolean acceptsVolatile() {
            return false;
        }
    };
    long ticksExisted = 0L;

    public TileEntityEmberFunnel()
    {
        this.capability.setEmberCapacity(2000.0D);
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
    public boolean isFull(){
        return capability.getEmber() >= capability.getEmberCapacity();
    }

    @Override
    public boolean onReceive(EntityEmberPacket packet) {
        return true;
    }

    @Override
    public void update() {
        this.ticksExisted ++;
        Direction facing = world.getBlockState(pos).getValue(BlockEmberEmitter.facing);
        BlockPos attachPos = pos.offset(facing.getOpposite());
        TileEntity attachTile = world.getTileEntity(attachPos);
        if (ticksExisted % 2 == 0 && attachTile != null){
            if (attachTile.hasCapability(EmbersCapabilities.EMBER_CAPABILITY, facing)){
                IEmberCapability cap = attachTile.getCapability(EmbersCapabilities.EMBER_CAPABILITY, facing);
                if (cap != null){
                    if (cap.getEmber() < cap.getEmberCapacity() && capability.getEmber() > 0){
                        double added = cap.addAmount(Math.min(TRANSFER_SPEED,capability.getEmber()), true);
                        double removed = capability.removeAmount(added, true);
                        //markDirty();
                        if (!world.isRemote){
                            attachTile.markDirty();
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean activate(World world, BlockPos blockPos, IBlockState iBlockState, PlayerEntity PlayerEntity, Hand Hand, Direction Direction, float v, float v1, float v2) {
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
    public void markDirty() {
        super.markDirty();
        Misc.syncTE(this);
    }
}

