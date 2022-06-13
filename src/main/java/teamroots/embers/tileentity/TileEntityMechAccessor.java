package teamroots.embers.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import teamroots.embers.EventManager;
import teamroots.embers.api.tile.IExtraCapabilityInformation;
import teamroots.embers.api.tile.IExtraDialInformation;
import teamroots.embers.block.BlockMechAccessor;
import teamroots.embers.util.Misc;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class TileEntityMechAccessor extends TileEntity implements ITileEntityBase, IExtraDialInformation, IExtraCapabilityInformation {
    static HashSet<Class<? extends TileEntity>> ACCESSIBLE_TILES = new HashSet<>();

    public static void registerAccessibleTile(Class<? extends TileEntity> type) {
        ACCESSIBLE_TILES.add(type);
    }

    public static boolean canAccess(TileEntity tile) {
        Class<? extends TileEntity> tileClass = tile.getClass();
        return tile instanceof IMultiblockMachine || ACCESSIBLE_TILES.stream().anyMatch(type -> type.isAssignableFrom(tileClass));
    }

    Random random = new Random();

    public TileEntityMechAccessor() {
        super();
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        return super.write(tag);
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
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
    public boolean hasCapability(Capability<?> capability, Direction facing) {
        Direction accessFace = getFacing();
        TileEntity tile = getAttachedMultiblock(accessFace);
        return (tile != null && canAccess(tile) && tile.hasCapability(capability, accessFace)) || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, Direction facing) {
        Direction accessFace = getFacing();
        TileEntity tile = getAttachedMultiblock(accessFace);
        return tile != null && canAccess(tile) ? tile.getCapability(capability, accessFace) : super.getCapability(capability,facing);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
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
    public void markDirty() {
        super.markDirty();
        Misc.syncTE(this);
    }

    public Direction getFacing() {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof BlockMechAccessor)
            return state.getValue(BlockMechAccessor.facing);
        return null;
    }

    public TileEntity getAttachedMultiblock(Direction facing) {
        if (facing != null) {
            TileEntity tileEntity = world.getTileEntity(pos.offset(facing.getOpposite()));
            return tileEntity;
        }
        return null;
    }

    @Override
    public void addDialInformation(Direction facing, List<String> information, String dialType) {
        Direction accessFace = getFacing();
        TileEntity tile = getAttachedMultiblock(accessFace);
        if (tile instanceof IExtraDialInformation && canAccess(tile))
            ((IExtraDialInformation) tile).addDialInformation(accessFace, information, dialType);
    }

    @Override
    public boolean hasCapabilityDescription(Capability<?> capability) {
        Direction accessFace = getFacing();
        TileEntity tile = getAttachedMultiblock(accessFace);
        if (tile instanceof IExtraCapabilityInformation && canAccess(tile))
            return ((IExtraCapabilityInformation) tile).hasCapabilityDescription(capability);
        return false;
    }

    @Override
    public void addCapabilityDescription(List<String> strings, Capability<?> capability, Direction facing) {
        Direction accessFace = getFacing();
        TileEntity tile = getAttachedMultiblock(accessFace);
        if (tile instanceof IExtraCapabilityInformation && canAccess(tile))
            ((IExtraCapabilityInformation) tile).addCapabilityDescription(strings, capability, accessFace);
    }

    @Override
    public void addOtherDescription(List<String> strings, Direction facing) {
        Direction accessFace = getFacing();
        TileEntity tile = getAttachedMultiblock(accessFace);
        if (tile instanceof IExtraCapabilityInformation && canAccess(tile))
            ((IExtraCapabilityInformation) tile).addOtherDescription(strings, accessFace);
    }
}
