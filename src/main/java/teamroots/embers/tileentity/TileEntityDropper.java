package teamroots.embers.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.item.EntityItem;
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
import teamroots.embers.util.EnumPipeConnection;
import teamroots.embers.util.Misc;

import javax.annotation.Nullable;
import java.util.Random;

public class TileEntityDropper extends TileEntity implements ITileEntityBase, ITickableTileEntity, IItemPipeConnectable, IItemPipePriority {
	public ItemStackHandler inventory = new ItemStackHandler(1){
        @Override
        protected void onContentsChanged(int slot) {
        	TileEntityDropper.this.markDirty();
        }
	};
	Random random = new Random();
	
	public TileEntityDropper(){
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
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing == Direction.UP){
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	public void markDirty() {
		super.markDirty();
		Misc.syncTE(this);
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, Direction facing){
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing == Direction.UP){
			return (T)this.inventory;
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public boolean activate(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand,
			Direction side, float hitX, float hitY, float hitZ) {
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
		if (!inventory.getStackInSlot(0).isEmpty() && !getWorld().isRemote){
			ItemStack stack = inventory.extractItem(0, 1, false);
			EntityItem item = new EntityItem(getWorld(),getPos().getX()+0.5,getPos().getY(),getPos().getZ()+0.5,stack);
			item.motionX = 0;
			item.motionY = -0.1;
			item.motionZ = 0;
			getWorld().spawnEntity(item);
			//markDirty();
		}
	}

	@Override
	public EnumPipeConnection getConnection(Direction facing) {
		if(facing == Direction.UP)
			return EnumPipeConnection.PIPE;
		return EnumPipeConnection.NONE;
	}

	@Override
	public int getPriority(Direction facing) {
		return 50;
	}
}
