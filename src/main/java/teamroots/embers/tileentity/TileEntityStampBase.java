package teamroots.embers.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.TileFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import teamroots.embers.EventManager;
import teamroots.embers.api.tile.IExtraCapabilityInformation;
import teamroots.embers.block.BlockStampBase;
import teamroots.embers.util.Misc;

import javax.annotation.Nullable;
import java.util.List;

public class TileEntityStampBase extends TileFluidHandler implements ITileEntityBase, IExtraCapabilityInformation {
	public static int capacity = (Fluid.BUCKET_VOLUME*3) / 2; //1500
	public ItemStackHandler inputs = new ItemStackHandler(1){
        @Override
        protected void onContentsChanged(int slot) {
        	TileEntityStampBase.this.markDirty();
        }
	};
	
	public TileEntityStampBase(){
		super();
		tank = new FluidTank(capacity) {
			@Override
			protected void onContentsChanged() {
				TileEntityStampBase.this.markDirty();
			}
		};
		tank.setTileEntity(this);
		tank.setCanFill(true);
		tank.setCanDrain(true);
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, Direction facing){
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
			return true;
		}
		return super.hasCapability(capability, facing);
	}
	
	public Direction getFacing(){
		return getWorld().getBlockState(getPos()).getValue(BlockStampBase.facing);
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, Direction facing){
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
			return (T)inputs;
		}
		return super.getCapability(capability, facing);
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
		ItemStack heldItem = player.getHeldItem(hand);
		if (!heldItem.isEmpty()){
			boolean didFill = FluidUtil.interactWithFluidHandler(player, hand, this.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side));

			if (didFill){
				this.markDirty();
				return true;
			} else {
				player.setHeldItem(hand, this.inputs.insertItem(0,heldItem,false));
				markDirty();
				return true;
			}
		}
		else {
			if (!inputs.getStackInSlot(0).isEmpty() && !world.isRemote){
				world.spawnEntity(new EntityItem(world,player.posX,player.posY,player.posZ,inputs.getStackInSlot(0)));
				inputs.setStackInSlot(0, ItemStack.EMPTY);
				markDirty();
				return true;
			}
		}
		return false;
	}
	
	public IFluidHandler getTank(){
		return tank;
	}
	
	public int getCapacity(){
		return tank.getCapacity();
	}
	
	public int getAmount(){
		return tank.getFluidAmount();
	}

	public Fluid getFluid(){
		if (tank.getFluid() != null){
			return tank.getFluid().getFluid();
		}
		return null;
	}

	public FluidStack getFluidStack() {
		return tank.getFluid();
	}

	@Override
	public CompoundNBT write(CompoundNBT tag){
		super.write(tag);
		tag.setTag("inputs", inputs.serializeNBT());
		return tag;
	}
	
	@Override
	public void read(CompoundNBT tag){
		super.read(tag);
		inputs.deserializeNBT(tag.getCompoundTag("inputs"));
	}

	@Override
	public void onHarvest(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		this.remove();
		Misc.spawnInventoryInWorld(world, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, inputs);
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
			strings.add(IExtraCapabilityInformation.formatCapability(EnumIOType.INPUT,"embers.tooltip.goggles.fluid", I18n.format("embers.tooltip.goggles.fluid.metal")));
	}
}
