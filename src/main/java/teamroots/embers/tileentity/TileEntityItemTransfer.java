package teamroots.embers.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import teamroots.embers.api.filter.FilterItem;
import teamroots.embers.api.filter.IFilter;
import teamroots.embers.api.item.IFilterItem;
import teamroots.embers.block.BlockItemTransfer;
import teamroots.embers.util.EnumPipeConnection;
import teamroots.embers.util.FilterUtil;
import teamroots.embers.util.Misc;

import java.util.Random;

import ItemStack;

public class TileEntityItemTransfer extends TileEntityItemPipeBase {
    public static final int PRIORITY_TRANSFER = -10;
    double angle = 0;
    double turnRate = 1;
    public ItemStack filterItem = ItemStack.EMPTY;
    Random random = new Random();
    boolean syncFilter;
    IItemHandler outputSide;

    IFilter filter = FilterUtil.FILTER_ANY;

    public TileEntityItemTransfer() {
        super();
    }

    @Override
    protected void initInventory() {
        inventory = new ItemStackHandler(1) {
            @Override
            public int getSlotLimit(int slot) {
                return getCapacity();
            }

            @Override
            protected void onContentsChanged(int slot) {
                markDirty();
            }

            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                if (acceptsItem(stack))
                    return super.insertItem(slot, stack, simulate);
                else
                    return stack;
            }
        };
        outputSide = Misc.makeRestrictedItemHandler(inventory,false,true);
    }

    public boolean acceptsItem(ItemStack stack) {
        return filter.acceptsItem(stack);
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        writeFilter(tag);
        return tag;
    }

    private void writeFilter(CompoundNBT tag) {
        if (!filterItem.isEmpty()) {
            tag.setTag("filter", filterItem.write(new CompoundNBT()));
        } else {
            tag.setString("filter", "empty");
        }
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        if (tag.contains("filter")) {
            filterItem = new ItemStack(tag.getCompoundTag("filter"));
        }
        setupFilter();
    }

    @Override
    public CompoundNBT getSyncTag() {
        CompoundNBT compound = super.getUpdateTag();
        if (syncFilter)
            writeFilter(compound);
        return compound;
    }

    @Override
    protected boolean requiresSync() {
        return syncFilter || super.requiresSync();
    }

    @Override
    protected void resetSync() {
        super.resetSync();
        syncFilter = false;
    }

    @Override
    int getCapacity() {
        return 4;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, Direction facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (facing == null || facing.getAxis() == getFacing().getAxis())
                return true;
            else
                return false;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, Direction facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            Direction transferFacing = getFacing();
            if (facing == transferFacing)
                return (T) this.outputSide;
            else if (facing == null || facing.getAxis() == transferFacing.getAxis())
                return (T) this.inventory;
            else
                return null;
        }
        return super.getCapability(capability, facing);
    }

    private Direction getFacing() {
        IBlockState state = getWorld().getBlockState(getPos());
        return state.getValue(BlockItemTransfer.facing);
    }

    @Override
    public boolean activate(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand,
                            Direction side, float hitX, float hitY, float hitZ) {
        ItemStack heldItem = player.getHeldItem(hand);
        if (!world.isRemote) {
            if (heldItem != ItemStack.EMPTY) {
                this.filterItem = heldItem.copy();
                world.setBlockState(pos, state.withProperty(BlockItemTransfer.filter, true), 10);
            } else {
                this.filterItem = ItemStack.EMPTY;
                world.setBlockState(pos, state.withProperty(BlockItemTransfer.filter, false), 10);
            }
            setupFilter();

            syncFilter = true;
            markDirty();
            return true;
        }
        return true;
    }

    private void setupFilter() {
        Item item = this.filterItem.getItem();
        if(item instanceof IFilterItem)
            filter = ((IFilterItem) item).getFilter(this.filterItem);
        else if(!this.filterItem.isEmpty())
            filter = new FilterItem(this.filterItem);
        else
            filter = FilterUtil.FILTER_ANY;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }

    @Override
    public void onHarvest(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        this.remove();
        Misc.spawnInventoryInWorld(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, inventory);
        world.setTileEntity(pos, null);
    }

    @Override
    public void update() {
        if (world.isRemote && clogged && isAnySideUnclogged())
            Misc.spawnClogParticles(world,pos,2, 0.7f);
        angle += turnRate;
        super.update();
    }

    @Override
    public int getPriority(Direction facing) {
        return PRIORITY_TRANSFER;
    }

    @Override
    public EnumPipeConnection getInternalConnection(Direction facing) {
        return EnumPipeConnection.NONE;
    }

    @Override
    void setInternalConnection(Direction facing, EnumPipeConnection connection) {
        //NOOP
    }

    @Override
    boolean isConnected(Direction facing) {
        return getFacing().getAxis() == facing.getAxis();
    }

    @Override
    protected boolean isFrom(Direction facing) {
        return facing == getFacing().getOpposite();
    }

    @Override
    public void markDirty() {
        super.markDirty();
        Misc.syncTE(this);
    }

    @Override
    public EnumPipeConnection getConnection(Direction facing) {
        if(getFacing().getAxis() == facing.getAxis())
            return EnumPipeConnection.PIPE;
        return EnumPipeConnection.NONE;
    }
}
