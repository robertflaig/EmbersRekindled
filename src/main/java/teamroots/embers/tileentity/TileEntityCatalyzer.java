package teamroots.embers.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
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
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import teamroots.embers.EventManager;
import teamroots.embers.api.EmbersAPI;
import teamroots.embers.api.misc.ICoefficientFuel;
import teamroots.embers.api.tile.IExtraCapabilityInformation;
import teamroots.embers.util.Misc;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class TileEntityCatalyzer extends TileEntity implements ITileEntityBase, ITickableTileEntity, IExtraCapabilityInformation {
	public static final int PROCESS_TIME = 400;
	Random random = new Random();
	int progress = 0;
	double multiplier = 0;
	public ItemStackHandler inventory = new ItemStackHandler(1){
        @Override
        protected void onContentsChanged(int slot) {
        	TileEntityCatalyzer.this.markDirty();
        }
        
        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate){
        	if (EmbersAPI.getCatalysisFuel(stack) == null){
        		return stack;
        	}
        	return super.insertItem(slot, stack, simulate);
        }
	};
	
	public TileEntityCatalyzer(){
		super();
	}
	
	@Override
	public CompoundNBT write(CompoundNBT tag){
		super.write(tag);
		tag.setTag("inventory", inventory.serializeNBT());
		tag.setInteger("progress", progress);
		tag.setDouble("multiplier", multiplier);
		return tag;
	}
	
	@Override
	public void read(CompoundNBT tag){
		super.read(tag);
		inventory.deserializeNBT(tag.getCompoundTag("inventory"));
		if (tag.contains("progress")){
			progress = tag.getInteger("progress");
		}
		multiplier = tag.getDouble("multiplier");
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
		return false;
	}

	@Override
	public void onHarvest(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		this.remove();
		Misc.spawnInventoryInWorld(getWorld(), pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, inventory);
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
		if (progress > 0){
			progress --;
			if (progress == 0){
				multiplier = 0;
			}
			markDirty();
		}
		ItemStack fuel = inventory.getStackInSlot(0);
		if (progress == 0 && !fuel.isEmpty()){
			ICoefficientFuel coefficient = EmbersAPI.getCatalysisFuel(fuel);
			if (coefficient != null){
				multiplier = coefficient.getCoefficient(fuel);
				progress = coefficient.getDuration(fuel);
				inventory.extractItem(0, 1, false);
				markDirty();
			}
		}
	}

	@Override
	public void markDirty() {
		super.markDirty();
		Misc.syncTE(this);
	}

	@Override
	public boolean hasCapabilityDescription(Capability<?> capability) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
	}

	@Override
	public void addCapabilityDescription(List<String> strings, Capability<?> capability, Direction facing) {
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			strings.add(IExtraCapabilityInformation.formatCapability(IExtraCapabilityInformation.EnumIOType.INPUT,"embers.tooltip.goggles.item", I18n.format("embers.tooltip.goggles.item.catalysis")));
	}
}
