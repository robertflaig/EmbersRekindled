package teamroots.embers.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import teamroots.embers.Embers;
import teamroots.embers.api.filter.FilterExisting;
import teamroots.embers.api.filter.FilterItem;
import teamroots.embers.api.filter.IFilter;
import teamroots.embers.api.item.IFilterItem;
import teamroots.embers.api.tile.IOrderDestination;
import teamroots.embers.api.tile.IOrderSource;
import teamroots.embers.api.tile.ITargetable;
import teamroots.embers.block.BlockItemRequisition;
import teamroots.embers.item.ItemTinkerHammer;
import teamroots.embers.util.EnumPipeConnection;
import teamroots.embers.util.FilterUtil;
import teamroots.embers.util.Misc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class TileEntityItemRequisition extends TileEntity implements ITileEntityBase, ITickableTileEntity, IItemPipePriority, IItemPipeConnectable, ITargetable, IOrderSource, ISpecialFilter {
    public static final int PRIORITY_REQUEST = -100;

    EnumPipeConnection[] connections = new EnumPipeConnection[Direction.VALUES.length];
    public IItemHandler itemHandler;
    public ItemStack filterItem = ItemStack.EMPTY;
    public int filterSize = 0;
    IFilter filter = FilterUtil.FILTER_ANY;

    public int currentOrder;
    public BlockPos target = null;
    double angle = 0;
    double turnRate = 1;
    boolean lastPowered;

    public TileEntityItemRequisition() {
        itemHandler = new IItemHandler() {
            @Override
            public int getSlots() {
                return 1;
            }

            @Nonnull
            @Override
            public ItemStack getStackInSlot(int slot) {
                return ItemStack.EMPTY;
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                if (!acceptsItem(stack))
                    return stack;
                TileEntity attached = getAttached();
                if (attached != null && attached.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, getFacing().getOpposite())) {
                    IItemHandler handler = attached.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, getFacing().getOpposite());
                    if(isIntelligent()) {
                        ItemStack split = stack.copy();
                        for (int j = 0; j < handler.getSlots() && !split.isEmpty(); j++) {
                            split = handler.insertItem(j, split, simulate);
                        }
                        return split;
                    } else {
                        int count = countItems(handler);
                        int filterSize = getFilterSize() - count;
                        if (filterSize > 0) {
                            ItemStack whole = stack.copy();
                            ItemStack split = whole.splitStack(filterSize);

                            for (int j = 0; j < handler.getSlots() && !split.isEmpty(); j++) {
                                split = handler.insertItem(j, split, simulate);
                            }

                            ItemStack merged = merge(whole, split);
                            return merged;
                        }
                    }
                }
                return stack;
            }

            ItemStack merge(ItemStack first, ItemStack second) {
                if (!ItemHandlerHelper.canItemStacksStack(first, second))
                    return first;
                first = first.copy();
                first.grow(second.getCount());
                return first;
            }

            @Nonnull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return ItemStack.EMPTY;
            }

            @Override
            public int getSlotLimit(int slot) {
                return 64;
            }
        };
    }

    public void updateConnections() {
        for (Direction facing : Direction.VALUES) {
            setInternalConnection(facing, getConnection(world, getPos().offset(facing), facing));
        }
        markDirty();
    }

    public EnumPipeConnection getInternalConnection(Direction facing) {
        return connections[facing.getIndex()] != null ? connections[facing.getIndex()] : EnumPipeConnection.NONE;
    }

    void setInternalConnection(Direction facing, EnumPipeConnection connection) {
        connections[facing.getIndex()] = connection;
    }

    @Override
    public EnumPipeConnection getConnection(Direction facing) {
        return facing == getFacing() ? EnumPipeConnection.NONE : EnumPipeConnection.PIPE;
    }

    public EnumPipeConnection getConnection(IBlockAccess world, BlockPos pos, Direction side) {
        TileEntity tile = world.getTileEntity(pos);
        if (side == getFacing() || tile instanceof TileEntityItemRequisition) {
            return EnumPipeConnection.NONE;
        } else if (tile instanceof IItemPipeConnectable) {
            return ((IItemPipeConnectable) tile).getConnection(side.getOpposite());
        } else if (tile != null && tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite())) {
            return EnumPipeConnection.PIPE;
        } else if (Misc.isValidLever(world, pos, side)) {
            return EnumPipeConnection.LEVER;
        }
        return EnumPipeConnection.NONE;
    }

    public boolean isIntelligent() { //Aka takes anything that is already in the inventory
        return filter instanceof FilterExisting;
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


    public int getFilterSize() {
        return filterSize;
    }

    public boolean acceptsItem(ItemStack stack) {
        TileEntity attached = getAttached();
        if(attached != null) {
            IItemHandler itemHandler = attached.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, getFacing().getOpposite());
            if(itemHandler != null)
                return filter.acceptsItem(stack, itemHandler);
        }
        return false;
    }

    private Direction getFacing() {
        IBlockState state = getWorld().getBlockState(getPos());
        return state.getValue(BlockItemRequisition.facing);
    }

    public TileEntity getAttached() {
        return world.getTileEntity(pos.offset(getFacing()));
    }

    public TileEntityItemExtractor getSource() {
        if (target == null)
            return null;
        TileEntity tile = world.getTileEntity(target);
        if (tile instanceof TileEntityItemExtractor)
            return (TileEntityItemExtractor) tile;
        return null;
    }

    @Override
    public IItemHandler getItemHandler() {
        TileEntity attached = getAttached();
        if(attached != null) {
            IItemHandler itemHandler = attached.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, getFacing().getOpposite());
            if(itemHandler != null)
                return itemHandler;
        }
        return null;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable Direction facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != getFacing())
            return true;
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != getFacing())
            return (T) itemHandler;
        return super.getCapability(capability, facing);
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            boolean isPowered = false;
            //if (!isIntelligent()) {
                isPowered = getWorld().isBlockPowered(getPos());
                int filterSize = getFilterSize();
                TileEntity attached = getAttached();
                int count = 0;
                if (attached != null && attached.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, getFacing().getOpposite())) {
                    IItemHandler handler = attached.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, getFacing().getOpposite());
                    count = countItems(handler);
                }
                //if (isPowered && !lastPowered) //TODO
                //    forceOrder(filterSize);
                if (currentOrder < filterSize - count) {
                    order(filterSize - count - currentOrder);
                } else if (currentOrder > filterSize - count) {
                    currentOrder = Math.max(0, filterSize - count); //Reduce order by however much we inserted
                }
            //}
            lastPowered = isPowered;
        } else {
            angle += turnRate;
        }
    }

    public int countItems(IItemHandler handler) {
        int count = 0;
        for (int j = 0; j < handler.getSlots(); j++) {
            ItemStack countStack = handler.getStackInSlot(j);
            if (acceptsItem(countStack))
                count += countStack.getCount();
        }
        return count;
    }

    public void order(int orderSize) {
        IOrderDestination source = getSource();
        if (source != null) {
            source.order(this, filter, orderSize);
            currentOrder += orderSize;
        }
    }

    public void resetOrder() {
        IOrderDestination source = getSource();
        if (source != null) {
            source.resetOrder(this);
        }
        currentOrder = 0;
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        tag.setInteger("order", currentOrder);
        if (!filterItem.isEmpty()) {
            tag.setTag("filter", filterItem.write(new CompoundNBT()));
            tag.setInteger("filterSize", filterSize);
        } else {
            tag.setString("filter", "empty");
        }
        if (target != null) {
            tag.setInteger("targetX", target.getX());
            tag.setInteger("targetY", target.getY());
            tag.setInteger("targetZ", target.getZ());
        }
        tag.setInteger("up", getInternalConnection(Direction.UP).getIndex());
        tag.setInteger("down", getInternalConnection(Direction.DOWN).getIndex());
        tag.setInteger("north", getInternalConnection(Direction.NORTH).getIndex());
        tag.setInteger("south", getInternalConnection(Direction.SOUTH).getIndex());
        tag.setInteger("west", getInternalConnection(Direction.WEST).getIndex());
        tag.setInteger("east", getInternalConnection(Direction.EAST).getIndex());
        tag.setBoolean("lastPowered", lastPowered);
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        currentOrder = tag.getInteger("order");
        if (tag.contains("filter")) {
            filterItem = new ItemStack(tag.getCompoundTag("filter"));
            filterSize = tag.getInteger("filterSize");
        }
        if (tag.contains("targetX")) {
            target = new BlockPos(tag.getInteger("targetX"), tag.getInteger("targetY"), tag.getInteger("targetZ"));
        }
        if (tag.contains("up"))
            setInternalConnection(Direction.UP, EnumPipeConnection.fromIndex(tag.getInteger("up")));
        if (tag.contains("down"))
            setInternalConnection(Direction.DOWN, EnumPipeConnection.fromIndex(tag.getInteger("down")));
        if (tag.contains("north"))
            setInternalConnection(Direction.NORTH, EnumPipeConnection.fromIndex(tag.getInteger("north")));
        if (tag.contains("south"))
            setInternalConnection(Direction.SOUTH, EnumPipeConnection.fromIndex(tag.getInteger("south")));
        if (tag.contains("west"))
            setInternalConnection(Direction.WEST, EnumPipeConnection.fromIndex(tag.getInteger("west")));
        if (tag.contains("east"))
            setInternalConnection(Direction.EAST, EnumPipeConnection.fromIndex(tag.getInteger("east")));
        lastPowered = tag.getBoolean("lastPowered");
        setupFilter();
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
        if (heldItem.getItem() instanceof ItemTinkerHammer)
            return false;
        if (player.isSneaking())
            return false;
        if (!world.isRemote) {
            if (heldItem != ItemStack.EMPTY) {
                if (!filterItem.isItemEqual(heldItem)) {
                    filterItem = heldItem.copy();
                    filterSize = filterItem.getCount();
                } else
                    filterSize += heldItem.getCount();
            } else {
                filterItem = ItemStack.EMPTY;
                filterSize = 0;
            }
            setupFilter();
            resetOrder();
            markDirty();
            return true;
        } else {
            return true;
        }
    }

    @Override
    public void onHarvest(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        resetOrder();
    }

    @Override
    public void setTarget(BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof IOrderDestination) {
            resetOrder();
            target = pos;
        }
    }

    @Override
    public void markDirty() {
        super.markDirty();
        Misc.syncTE(this);
    }

    public void addDescription(List<String> strings) {
        TileEntityItemExtractor source = getSource();
        if (source != null) {
            BlockPos pos = source.getPos();
            IBlockState blockState = world.getBlockState(pos);
            strings.add(Embers.proxy.formatLocalize("embers.tooltip.item_request.linked", blockState.getBlock().getLocalizedName(), pos.getX(), pos.getY(), pos.getZ()));
        }
        strings.add(Embers.proxy.formatLocalize("embers.tooltip.item_request.filter", filter.formatFilter(), getFilterSize()));
        if (currentOrder > 0)
            strings.add(Embers.proxy.formatLocalize("embers.tooltip.item_request.order", currentOrder));
        else
            strings.add(Embers.proxy.formatLocalize("embers.tooltip.item_request.order.none"));
    }

    @Override
    public int getPriority(Direction facing) {
        return PRIORITY_REQUEST;
    }

    @Override
    public IFilter getSpecialFilter() {
        return FilterUtil.FILTER_EXISTING;
    }
}
