package teamroots.embers.tileentity;

import net.minecraft.block.state.BlockFaceShape;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import teamroots.embers.api.capabilities.EmbersCapabilities;
import teamroots.embers.api.tile.IExtraCapabilityInformation;
import teamroots.embers.api.tile.IExtraDialInformation;
import teamroots.embers.block.BlockFluidGauge;
import teamroots.embers.block.BlockStirling;
import teamroots.embers.particle.ParticleUtil;
import teamroots.embers.upgrade.UpgradeStirling;
import teamroots.embers.util.Misc;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class TileEntityStirling extends TileEntity implements ITickableTileEntity, ITileEntityBase, IExtraDialInformation, IExtraCapabilityInformation {
    public int activeTicks = 0;
    public UpgradeStirling upgrade;
    public FluidTank tank = new FluidTank(4000) {
        @Override
        public boolean canFillFluidType(FluidStack fluid) {
            return fluid != null && fluid.getFluid() == FluidRegistry.getFluid("steam");
        }
    };
    private Random random = new Random();

    public TileEntityStirling() {
        upgrade = new UpgradeStirling(this);
    }

    @Override
    public CompoundNBT write(CompoundNBT tag){
        super.write(tag);
        CompoundNBT tankTag = new CompoundNBT();
        tank.write(tankTag);
        tag.setTag("tank", tankTag);
        tag.setInteger("active",activeTicks);
        return tag;
    }

    @Override
    public void read(CompoundNBT tag){
        super.read(tag);
        tank.read(tag.getCompoundTag("tank"));
        activeTicks = tag.getInteger("active");
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

    public Direction getFacing()
    {
        IBlockState state = world.getBlockState(pos);
        if(state.getBlock() instanceof BlockStirling)
            return state.getValue(BlockStirling.FACING);
        return null;
    }

    public void setActive(int ticks) {
        activeTicks = Math.max(ticks,activeTicks);
        markDirty();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable Direction facing) {
        if (capability == EmbersCapabilities.UPGRADE_PROVIDER_CAPABILITY)
            return getFacing() == facing;
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return getFacing().getOpposite() == facing || facing == null;
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (capability == EmbersCapabilities.UPGRADE_PROVIDER_CAPABILITY && getFacing() == facing)
            return (T) upgrade;
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && (getFacing().getOpposite() == facing || facing == null))
            return (T) tank;
        return super.getCapability(capability, facing);
    }

    @Override
    public void update() {
        activeTicks--;
        IBlockState state = world.getBlockState(pos);
        if(activeTicks > 0 && world.isRemote && state.getBlock() instanceof BlockStirling) {
            Direction facing = state.getValue(BlockStirling.FACING);
            float frontoffset = -0.6f;
            float yoffset = 0.2f;
            float wideoffset = 0.5f;
            float breadthoffset = 0.4f;
            Vec3d frontOffset = new Vec3d(0.5 - facing.getFrontOffsetX() * frontoffset, 0.5 - facing.getFrontOffsetY() * frontoffset, 0.5 - facing.getFrontOffsetZ() * frontoffset);
            Vec3d baseOffset = new Vec3d(0.5 - facing.getFrontOffsetX() * yoffset, 0.5 - facing.getFrontOffsetY() * yoffset, 0.5 - facing.getFrontOffsetZ() * yoffset);
            Direction[] planars;
            switch(facing.getAxis()) {
                case X:
                    planars = new Direction[] {Direction.DOWN,Direction.UP,Direction.NORTH,Direction.SOUTH}; break;
                case Y:
                    planars = new Direction[] {Direction.EAST,Direction.WEST,Direction.NORTH,Direction.SOUTH}; break;
                case Z:
                    planars = new Direction[] {Direction.DOWN,Direction.UP,Direction.EAST,Direction.WEST}; break;
                default:
                    planars = null; break;
            }
            for(Direction planar : planars) {
                IBlockState sideState = world.getBlockState(pos.offset(planar));
                if(sideState.getBlockFaceShape(world,pos.offset(planar),planar.getOpposite()) != BlockFaceShape.UNDEFINED)
                    continue;
                Direction cross = facing.rotateAround(planar.getAxis());
                float x1 = getPos().getX() + (float) baseOffset.x + planar.getFrontOffsetX() * wideoffset;
                float y1 = getPos().getY() + (float) baseOffset.y + planar.getFrontOffsetY() * wideoffset;
                float z1 = getPos().getZ() + (float) baseOffset.z + planar.getFrontOffsetZ() * wideoffset;
                float x2 = getPos().getX() + (float) frontOffset.x + planar.getFrontOffsetX() * wideoffset + cross.getFrontOffsetX() * (random.nextFloat()-0.5f) * 2 * breadthoffset;
                float y2 = getPos().getY() + (float) frontOffset.y + planar.getFrontOffsetY() * wideoffset + cross.getFrontOffsetY() * (random.nextFloat()-0.5f) * 2 * breadthoffset;
                float z2 = getPos().getZ() + (float) frontOffset.z + planar.getFrontOffsetZ() * wideoffset + cross.getFrontOffsetZ() * (random.nextFloat()-0.5f) * 2 * breadthoffset;
                int lifetime = 24 + random.nextInt(8);
                //float motionx = facing.getFrontOffsetX() * (1.0f/lifetime) - 0.01f + random.nextFloat() * 0.02f;
                //float motiony = facing.getFrontOffsetY() * (1.0f/lifetime) - 0.01f + random.nextFloat() * 0.02f;
                //float motionz = facing.getFrontOffsetZ() * (1.0f/lifetime) - 0.01f + random.nextFloat() * 0.02f;
                float motionx = (x2 - x1) / lifetime;
                float motiony = (y2 - y1) / lifetime;
                float motionz = (z2 - z1) / lifetime;
                ParticleUtil.spawnParticleVapor(getWorld(), x1, y1, z1, motionx, motiony, motionz, 255, 64, 16, 1.0f, 2.0f, 3.0f, lifetime);
            }
            float x = getPos().getX() + (float) frontOffset.x;
            float y = getPos().getY() + (float) frontOffset.y;
            float z = getPos().getZ() + (float) frontOffset.z;
            int lifetime = 16 + random.nextInt(16);
            float motionx = (Math.abs(facing.getFrontOffsetX()) - 1) * (random.nextFloat()-0.5f) * 2 * wideoffset / lifetime;
            float motiony = (Math.abs(facing.getFrontOffsetY()) - 1) * (random.nextFloat()-0.5f) * 2 * wideoffset / lifetime;
            float motionz = (Math.abs(facing.getFrontOffsetZ()) - 1) * (random.nextFloat()-0.5f) * 2 * wideoffset / lifetime;

            ParticleUtil.spawnParticleVapor(getWorld(), x, y, z, motionx, motiony, motionz, 255, 64, 16, 1.0f, 3.0f, 4.0f, lifetime*2);
        }
    }

    @Override
    public boolean activate(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ) {
        return false;
    }

    @Override
    public void onHarvest(World world, BlockPos pos, BlockState state, PlayerEntity player) {
    }

    public FluidStack getFluidStack() {
        return tank.getFluid();
    }

    public int getCapacity() {
        return tank.getCapacity();
    }

    @Override
    public void markDirty() {
        super.markDirty();
        Misc.syncTE(this);
    }

    @Override
    public void addDialInformation(Direction facing, List<String> information, String dialType) {
        if(BlockFluidGauge.DIAL_TYPE.equals(dialType)) {
            information.clear();
            information.add(BlockFluidGauge.formatFluidStack(getFluidStack(),getCapacity()));
        }
    }

    @Override
    public boolean hasCapabilityDescription(Capability<?> capability) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
    }

    @Override
    public void addCapabilityDescription(List<String> strings, Capability<?> capability, Direction facing) {
        if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            strings.add(IExtraCapabilityInformation.formatCapability(EnumIOType.INPUT,"embers.tooltip.goggles.fluid",I18n.format("embers.tooltip.goggles.fluid.steam")));
    }
}