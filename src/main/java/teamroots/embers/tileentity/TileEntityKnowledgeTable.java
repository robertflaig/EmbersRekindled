package teamroots.embers.tileentity;

import net.minecraft.block.BlockState;
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
import teamroots.embers.util.Misc;

import javax.annotation.Nullable;
import java.util.Random;

public class TileEntityKnowledgeTable extends TileEntity implements ITileEntityBase, ITickableTileEntity {
	int angle = 0;
	int turnRate = 0;
	public ItemStackHandler inventory = new ItemStackHandler(1){
        @Override
        protected void onContentsChanged(int slot) {
            // We need to tell the tile entity that something has changed so
            // that the chest contents is persisted
        	TileEntityKnowledgeTable.this.markDirty();
        }
	};
	Random random = new Random();
	
	public TileEntityKnowledgeTable(){
		super();
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
		inventory.deserializeNBT(tag.getCompoundTag("inventory"));
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
	public boolean activate(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand,
			Direction side, float hitX, float hitY, float hitZ) {
		ItemStack heldItem = player.getHeldItem(hand);
		if (!heldItem.isEmpty()){
			player.setHeldItem(hand, this.inventory.insertItem(0,heldItem,false));
			markDirty();
			return true;
		}
		else {
			if (!inventory.getStackInSlot(0).isEmpty()){
				if (!getWorld().isRemote){
					player.setHeldItem(hand, inventory.extractItem(0, inventory.getStackInSlot(0).getCount(), false));
					markDirty();
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public void onHarvest(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		this.remove();
		Misc.spawnInventoryInWorld(world, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, inventory);
		world.setTileEntity(pos, null);
	}

	@Override
	public void tick() {
		turnRate = 1;
		angle += turnRate;
	}

	@Override
	public void markDirty() {
		super.markDirty();
		Misc.syncTE(this);
	}
}
