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
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.TileFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import teamroots.embers.EventManager;
import teamroots.embers.api.tile.IExtraCapabilityInformation;
import teamroots.embers.particle.ParticleUtil;
import teamroots.embers.util.FluidColorHelper;
import teamroots.embers.util.Misc;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.Random;

public class TileEntityFurnaceTop extends TileEntityOpenTank implements ITileEntityBase, ITickableTileEntity, IExtraCapabilityInformation {
	public static int capacity = Fluid.BUCKET_VOLUME*4;
	public double angle = 0;
	int ticksExisted = 0;
	
	public ItemStackHandler inventory = new ItemStackHandler(1){
        @Override
        protected void onContentsChanged(int slot) {
            // We need to tell the tile entity that something has changed so
            // that the chest contents is persisted
            TileEntityFurnaceTop.this.markDirty();
        }
	};
	
	public TileEntityFurnaceTop(){
		super();
		tank = new FluidTank(capacity) {
			@Override
			public void onContentsChanged(){
				TileEntityFurnaceTop.this.markDirty();
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
	}
	
	@Override
	public CompoundNBT write(CompoundNBT tag){
		super.write(tag);
		tag.setTag("inventory", inventory.serializeNBT());
		return tag;
	}
	
	@Override
	public void read(CompoundNBT tag){
		super.read(tag);
		if (tag.contains("inventory")){
			inventory.deserializeNBT(tag.getCompoundTag("inventory"));
		}
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
				player.setHeldItem(hand, this.inventory.insertItem(0,heldItem,false));
				markDirty();
				return true;
			}
		}
		else {
			if (!inventory.getStackInSlot(0).isEmpty() && !world.isRemote){
				world.spawnEntity(new EntityItem(world,player.posX,player.posY,player.posZ,inventory.getStackInSlot(0)));
				inventory.setStackInSlot(0, ItemStack.EMPTY);
				markDirty();
				return true;
			}
		}
		return false;
	}

	@Override
	public void markDirty() {
		super.markDirty();
		Misc.syncTE(this);
	}

	public int getCapacity(){
		return tank.getCapacity();
	}

	public FluidStack getFluidStack() {
		return tank.getFluid();
	}

	public FluidTank getTank() {
		return tank;
	}

	@Deprecated
	public Fluid getFluid(){
		if (tank.getFluid() != null){
			return tank.getFluid().getFluid();
		}
		return null;
	}

	@Deprecated
	public int getAmount(){
		return tank.getFluidAmount();
	}


	@Override
	public void onHarvest(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		this.remove();
		Misc.spawnInventoryInWorld(world, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, inventory);
		world.setTileEntity(pos, null);
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, Direction facing){
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
			return true;
		}
		return super.hasCapability(capability, facing);
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, Direction facing){
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
			return (T)this.inventory;
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public void tick() {
		angle ++;
		ticksExisted ++;
		if (ticksExisted % 10 == 0){
			List<EntityItem> items = getWorld().getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(getPos().getX(),getPos().getY(),getPos().getZ(),getPos().getX()+1,getPos().getY()+1.25,getPos().getZ()+1));
			for (int i = 0; i < items.size(); i ++){
				ItemStack stack = inventory.insertItem(0, items.get(i).getItem(), false);
				if (!stack.isEmpty()){
					items.get(i).setItem(stack);
				}
				else {
					getWorld().removeEntity(items.get(i));
				}
			}
		}
		if (world.isRemote && shouldEmitParticles())
			updateEscapeParticles();
	}

	@Override
	protected void updateEscapeParticles() {
		Color fluidColor = new Color(FluidColorHelper.getColor(lastEscaped), true);
		Random random = new Random();
		for (int i = 0; i < 3; i++) {
			float xOffset = 0.5f + (random.nextFloat() - 0.5f) * 2 * 0.2f;
			float yOffset = 0.9f;
			float zOffset = 0.5f + (random.nextFloat() - 0.5f) * 2 * 0.2f;

			ParticleUtil.spawnParticleVapor(world, pos.getX() + xOffset, pos.getY() + yOffset, pos.getZ() + zOffset, 0, 1 / 20f, 0, fluidColor.getRed() / 255f, fluidColor.getGreen() / 255f, fluidColor.getBlue() / 255f, fluidColor.getAlpha() / 255f, 4, 2, 20);
		}
	}

	@Override
	public boolean hasCapabilityDescription(Capability<?> capability) {
		return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
	}

	@Override
	public void addCapabilityDescription(List<String> strings, Capability<?> capability, Direction facing) {
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			strings.add(IExtraCapabilityInformation.formatCapability(EnumIOType.INPUT,"embers.tooltip.goggles.item", null));
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
			strings.add(IExtraCapabilityInformation.formatCapability(EnumIOType.OUTPUT,"embers.tooltip.goggles.fluid", I18n.format("embers.tooltip.goggles.fluid.metal")));
	}
}
