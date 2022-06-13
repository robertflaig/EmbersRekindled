package teamroots.embers.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import teamroots.embers.ConfigManager;
import teamroots.embers.api.capabilities.EmbersCapabilities;
import teamroots.embers.api.tile.IExtraCapabilityInformation;
import teamroots.embers.block.BlockGeoSeparator;
import teamroots.embers.particle.ParticleUtil;
import teamroots.embers.upgrade.UpgradeGeoSeparator;
import teamroots.embers.util.FluidColorHelper;
import teamroots.embers.util.Misc;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class TileEntityGeoSeparator extends TileEntityOpenTank implements ITileEntityBase, ITickableTileEntity, IExtraCapabilityInformation {
	Random random = new Random();
	protected UpgradeGeoSeparator upgrade;

	public TileEntityGeoSeparator(){
		super();

		tank = new FluidTank(getCapacity()) {
			@Override
			public void onContentsChanged(){
				TileEntityGeoSeparator.this.markDirty();
			}

			@Override
			public int fill(FluidStack resource, boolean doFill) {
				if(Misc.isGaseousFluid(resource)) {
					setEscapedFluid(resource);
					return resource.getAmount();
				}
				return super.fill(resource, doFill);
			}
		};
		tank.setTileEntity(this);
		tank.setCanFill(true);
		tank.setCanDrain(true);

		upgrade = new UpgradeGeoSeparator(this);
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
	public boolean hasCapability(Capability<?> capability, @Nullable Direction facing)
	{
		if(capability == EmbersCapabilities.UPGRADE_PROVIDER_CAPABILITY) {
			return facing == getFacing();
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	@Nullable
	public <T> T getCapability(Capability<T> capability, @Nullable Direction facing)
	{
		if(capability == EmbersCapabilities.UPGRADE_PROVIDER_CAPABILITY && facing == getFacing()) {
			return (T) upgrade;
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public boolean activate(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand,
			Direction side, float hitX, float hitY, float hitZ) {
		return false;
	}

	public Direction getFacing() {
		IBlockState state = world.getBlockState(pos);
		return state.getValue(BlockGeoSeparator.facing);
	}

	public FluidStack getFluidStack() {
		return tank.getFluid();
	}

	public int getCapacity(){
		return ConfigManager.geoSeparatorCapacity;
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

	@Override
	public boolean hasCapabilityDescription(Capability<?> capability) {
		return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
	}

	@Override
	public void addCapabilityDescription(List<String> strings, Capability<?> capability, Direction facing) {
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
			strings.add(IExtraCapabilityInformation.formatCapability(EnumIOType.OUTPUT,"embers.tooltip.goggles.fluid",I18n.format("embers.tooltip.goggles.fluid.metal")));
	}

	@Override
	protected void updateEscapeParticles() {
		Color fluidColor = new Color(FluidColorHelper.getColor(lastEscaped), true);
		Random random = new Random();
		for (int i = 0; i < 3; i++) {
			float xOffset = 0.5f + (random.nextFloat() - 0.5f) * 2 * 0.2f;
			float yOffset = 0.3f;
			float zOffset = 0.5f + (random.nextFloat() - 0.5f) * 2 * 0.2f;

			ParticleUtil.spawnParticleVapor(world, pos.getX() + xOffset, pos.getY() + yOffset, pos.getZ() + zOffset, 0, 1 / 20f, 0, fluidColor.getRed() / 255f, fluidColor.getGreen() / 255f, fluidColor.getBlue() / 255f, fluidColor.getAlpha() / 255f, 4, 2, 20);
		}
	}

	@Override
	public void tick() {
		if (world.isRemote && shouldEmitParticles())
			updateEscapeParticles();
	}
}
