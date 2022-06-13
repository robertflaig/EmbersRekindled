package teamroots.embers.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import teamroots.embers.ConfigManager;
import teamroots.embers.api.tile.IExtraCapabilityInformation;
import teamroots.embers.api.tile.IExtraDialInformation;
import teamroots.embers.block.BlockFluidGauge;
import teamroots.embers.particle.ParticleUtil;
import teamroots.embers.recipe.FluidReactionRecipe;
import teamroots.embers.recipe.RecipeRegistry;
import teamroots.embers.util.EnumPipeConnection;
import teamroots.embers.util.Misc;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.Random;

public class TileEntityReactionChamber extends TileEntity implements ITileEntityBase, ITickableTileEntity, IExtraDialInformation, IExtraCapabilityInformation, IFluidPipeConnectable {
	Random random = new Random();
	protected FluidTank fluidTank = new FluidTank(getCapacity());
	protected FluidTank gasTank = new FluidTank(getCapacity());
	FluidStack lastReaction;

	protected IFluidHandler fluidInterface = new IFluidHandler() {
		@Override
		public IFluidTankProperties[] getTankProperties() {

			return new IFluidTankProperties[] {
					fluidTank.getTankProperties()[0],
					gasTank.getTankProperties()[0]
			};
		}

		@Override
		public int fill(FluidStack resource, boolean doFill) {
			return fluidTank.fill(resource, doFill);
		}

		@Nullable
		@Override
		public FluidStack drain(FluidStack resource, boolean doDrain) {
			return gasTank.drain(resource, doDrain);
		}

		@Nullable
		@Override
		public FluidStack drain(int maxDrain, boolean doDrain) {
			return gasTank.drain(maxDrain, doDrain);
		}
	};

	public TileEntityReactionChamber(){
		super();
		fluidTank.setTileEntity(this);
		gasTank.setTileEntity(this);
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
	public void read(CompoundNBT tag)
	{
		super.read(tag);
		fluidTank.read(tag.getCompoundTag("fluidTank"));
		gasTank.read(tag.getCompoundTag("gasTank"));
		lastReaction = FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("lastReaction"));
	}

	@Override
	public CompoundNBT write(CompoundNBT tag)
	{
		tag = super.write(tag);
		tag.setTag("fluidTank",fluidTank.write(new CompoundNBT()));
		tag.setTag("gasTank",gasTank.write(new CompoundNBT()));
		if(lastReaction != null)
			tag.setTag("lastReaction", lastReaction.write(new CompoundNBT()));
		return tag;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable Direction facing)
	{
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return facing != null;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	@Nullable
	public <T> T getCapability(Capability<T> capability, @Nullable Direction facing)
	{
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return (T) fluidInterface;
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public boolean activate(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand,
			Direction side, float hitX, float hitY, float hitZ) {
		return false;
	}

	public int getCapacity(){
		return ConfigManager.miniBoilerCapacity;
	}
	
	public int getFluidAmount(){
		return fluidTank.getFluidAmount();
	}

	public int getGasAmount(){
		return gasTank.getFluidAmount();
	}

	public FluidTank getFluidTank(){
		return fluidTank;
	}

	public FluidTank getGasTank(){
		return gasTank;
	}

	public Fluid getFluid(){
		if (fluidTank.getFluid() != null){
			return fluidTank.getFluid().getFluid();
		}
		return null;
	}

	public Fluid getGas(){
		if (gasTank.getFluid() != null){
			return gasTank.getFluid().getFluid();
		}
		return null;
	}

	public FluidStack getFluidStack() {
		return fluidTank.getFluid();
	}

	public FluidStack getGasStack() {
		return gasTank.getFluid();
	}

	public void boil(int amount)
	{
		FluidStack fluid = getFluidStack();
		FluidReactionRecipe recipe = RecipeRegistry.getFluidReactionRecipe(fluid);
		if(recipe != null && fluid.amount > 0) {
			int fluidBoiled = MathHelper.clamp(amount,1,fluid.amount);

			if(fluidBoiled > 0) {
				fluid = fluidTank.drain(fluidBoiled,false);
				FluidStack gas = recipe.getResult(fluid);
				if(gas != null) {
					fluidTank.drain(fluidBoiled,true);
					gas.amount -= gasTank.fill(gas,true);
				}
				lastReaction = fluid;
				markDirty();
			}
		} else {
			lastReaction = null;
			markDirty();
		}
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
	public void addDialInformation(Direction facing, List<String> information, String dialType) {
		/*if(BlockFluidGauge.DIAL_TYPE.equals(dialType) && facing.getAxis() != Direction.Axis.Y) {
			information.add(BlockFluidGauge.formatFluidStack(getGasStack(),getCapacity()));
			information.add(BlockFluidGauge.formatFluidStack(getFluidStack(),getCapacity()));
		}*/
	}

	@Override
	public int getComparatorData(Direction facing, int data, String dialType) {
		if(BlockFluidGauge.DIAL_TYPE.equals(dialType) && facing.getAxis() != Direction.Axis.Y) {
			double fill = getGasAmount() / (double)getCapacity();
			return fill > 0 ? (int) (1 + fill * 14) : 0;
		}
		return data;
	}

	@Override
	public void tick() {
		if(world.isRemote)
			spawnParticles();
		if(!world.isRemote)
			boil(20);
	}

	public void spawnParticles() {
		/*double gasRatio = getGasAmount() / (double)getCapacity();
		int spouts = 0;
		if(gasRatio > 0.8)
			spouts = 3;
		else if(gasRatio > 0.5)
			spouts = 2;
		else if(gasRatio > 0.25)
			spouts = 1;
		Misc.spawnClogParticles(world,pos,spouts,0.5f);*/
		FluidReactionRecipe recipe = RecipeRegistry.getFluidReactionRecipe(lastReaction);
		if(recipe != null) {
			Color fluidColor = recipe.getColor();
			Random random = new Random();
			for (int i = 0; i < 5; i++) {
				float xOffset = 0.5f + (random.nextFloat() - 0.5f) * 2 * 0.4f;
				float yOffset = 1.0f;
				float zOffset = 0.5f + (random.nextFloat() - 0.5f) * 2 * 0.4f;

				ParticleUtil.spawnParticleVapor(world, pos.getX() + xOffset, pos.getY() + yOffset, pos.getZ() + zOffset, 0, 1 / 20f, 0, fluidColor.getRed() / 255f, fluidColor.getGreen() / 255f, fluidColor.getBlue() / 255f, fluidColor.getAlpha() / 255f, 4, 2, 20);
			}
		}
	}

	@Override
	public boolean hasCapabilityDescription(Capability<?> capability) {
		return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
	}

	@Override
	public void addCapabilityDescription(List<String> strings, Capability<?> capability, Direction facing) {
		strings.add(IExtraCapabilityInformation.formatCapability(EnumIOType.BOTH,"embers.tooltip.goggles.fluid",null));
	}

	@Override
	public EnumPipeConnection getConnection(Direction facing) {
		return EnumPipeConnection.BLOCK;
	}
}
