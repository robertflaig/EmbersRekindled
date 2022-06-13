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
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import teamroots.embers.Embers;
import teamroots.embers.EventManager;
import teamroots.embers.SoundManager;
import teamroots.embers.api.tile.IExtraCapabilityInformation;
import teamroots.embers.util.ItemUtil;
import teamroots.embers.util.Misc;
import teamroots.embers.util.sound.ISoundController;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class TileEntityAlchemyPedestal extends TileEntity implements ITileEntityBase, ITickableTileEntity, ISoundController, IExtraCapabilityInformation {
	int angle = 0;
	int turnRate = 0;
	int progress = 0;
	int ash = 0;
	int stackAsh = 0;
	int stackItem = 1;
	public ItemStackHandler inventory;
	IItemHandler externalInventory;
	Random random = new Random();

	public static final int SOUND_PROCESS = 1;
	public static final int[] SOUND_IDS = new int[]{SOUND_PROCESS};

	HashSet<Integer> soundsPlaying = new HashSet<>();
	int active = 0;
	
	public TileEntityAlchemyPedestal(){
		super();
		inventory = new ItemStackHandler(2){
            @Override
            protected void onContentsChanged(int slot) {
                // We need to tell the tile entity that something has changed so
                // that the chest contents is persisted
                TileEntityAlchemyPedestal.this.markDirty();
            }

            @Override
            public int getSlotLimit(int slot) {
                return slot == stackItem ? 1 : super.getSlotLimit(slot);
            }

            @Nonnull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return super.extractItem(slot, amount, simulate);
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                return slot == stackAsh && !ItemUtil.matchesOreDict(stack,"dustAsh") ? this.insertItem(slot + 1, stack, simulate) : super.insertItem(slot, stack, simulate);
            }
        };
		externalInventory = Misc.makeBlockedItemHandler(inventory,this::isNotActive,this::isNotActive);
	}

	private boolean isNotActive() {
		return !isActive();
	}

	public boolean isActive() {
		return this.active > 0;
	}

	public void setActive(int time) {
		active = time;
	}

	@Override
	public CompoundNBT write(CompoundNBT tag){
		super.write(tag);
		tag.setInteger("progress", 0);
		tag.setTag("inventory", inventory.serializeNBT());
		return tag;
	}
	
	@Override
	public void read(CompoundNBT tag){
		super.read(tag);
		ash = tag.getInteger("ash");
		progress = tag.getInteger("progress");
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
	public boolean hasCapability(Capability<?> capability, Direction facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, Direction facing){
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
			return (T)this.externalInventory;
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public void markDirty() {
		super.markDirty();
		Misc.syncTE(this);
	}

	@Override
	public boolean activate(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ) {
		ItemStack heldItem = player.getHeldItem(hand);
		if (!heldItem.isEmpty()){
			boolean isAsh = ItemUtil.matchesOreDict(heldItem,"dustAsh");

			if (isAsh)
				player.setHeldItem(hand, externalInventory.insertItem(stackAsh,heldItem,false));
			else
				player.setHeldItem(hand, externalInventory.insertItem(stackItem,heldItem,false));
			markDirty();
			return true;
		}
		else {
			ItemStack ashStack = externalInventory.getStackInSlot(stackAsh);
			ItemStack itemStack = externalInventory.getStackInSlot(stackItem);
			if (!ashStack.isEmpty()){
				if (!world.isRemote){
					player.setHeldItem(hand, externalInventory.extractItem(stackAsh, ashStack.getCount(), false));
					markDirty();
				}
				return true;
			}
			else if (!itemStack.isEmpty()) {
				if (!world.isRemote) {
					player.setHeldItem(hand, externalInventory.extractItem(stackItem, itemStack.getCount(), false));
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
		if(getWorld().isRemote)
			handleSound();
		active--;
	}

	@Override
	public void playSound(int id) {
		switch (id) {
			case SOUND_PROCESS:
				Embers.proxy.playMachineSound(this, SOUND_PROCESS, SoundManager.PEDESTAL_LOOP, SoundCategory.BLOCKS, true, 0.1f, 1.0f, (float)pos.getX()+0.5f,(float)pos.getY()+1.0f,(float)pos.getZ()+0.5f);
				break;
		}
		soundsPlaying.add(id);
	}

	@Override
	public void stopSound(int id) {
		soundsPlaying.remove(id);
	}

	@Override
	public boolean isSoundPlaying(int id) {
		return soundsPlaying.contains(id);
	}

	@Override
	public int[] getSoundIDs() {
		return SOUND_IDS;
	}

	@Override
	public boolean shouldPlaySound(int id) {
		return id == SOUND_PROCESS && isActive();
	}

	@Override
	public boolean hasCapabilityDescription(Capability<?> capability) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
	}

	@Override
	public void addCapabilityDescription(List<String> strings, Capability<?> capability, Direction facing) {
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			strings.add(IExtraCapabilityInformation.formatCapability(EnumIOType.BOTH,"embers.tooltip.goggles.item", I18n.format("embers.tooltip.goggles.item.ash")));
	}
}
