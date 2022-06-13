package teamroots.embers.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import teamroots.embers.block.BlockVacuum;
import teamroots.embers.util.EnumPipeConnection;

import java.util.List;
import java.util.Random;

public class TileEntityItemVacuum extends TileEntity implements ITileEntityBase, ITickableTileEntity, IItemPipeConnectable {
    Random random = new Random();

    public TileEntityItemVacuum() {
        super();
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
    }

    @Override
    public boolean activate(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand,
                            Direction side, float hitX, float hitY, float hitZ) {
        return false;
    }

    @Override
    public void onHarvest(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        this.remove();
        world.setTileEntity(pos, null);
    }

    @Override
    public void update() {
        IBlockState state = getWorld().getBlockState(getPos());
        Direction facing = state.getValue(BlockVacuum.facing);
        TileEntity tile = getWorld().getTileEntity(getPos().offset(facing.getOpposite()));
        if (!world.isRemote && world.isBlockPowered(getPos()) && tile != null && tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing)) {
            IItemHandler inventory = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
            Vec3i vec = facing.getDirectionVec();
            AxisAlignedBB suckBB = new AxisAlignedBB(getPos().getX() - 6 + vec.getX() * 6, getPos().getY() - 6 + vec.getY() * 6, getPos().getZ() - 6 + vec.getZ() * 6, getPos().getX() + 7 + vec.getX() * 6, getPos().getY() + 7 + vec.getY() * 6, getPos().getZ() + 7 + vec.getZ() * 6);
            List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, suckBB, entity -> getInsertedSlot(entity.getItem(), inventory) != -1);
            if (items.size() > 0) {
                for (EntityItem item : items) {
                    Vec3d v = new Vec3d(item.posX - (this.getPos().getX() + 0.5), item.posY - (this.getPos().getY() + 0.5), item.posZ - (this.getPos().getZ() + 0.5));
                    v.normalize();
                    item.motionX = (-v.x * 0.25 * 0.2f + item.motionX * 0.8f);
                    item.motionY = (-v.y * 0.25 * 0.2f + item.motionY * 0.8f);
                    item.motionZ = (-v.z * 0.25 * 0.2f + item.motionZ * 0.8f);
                }
            }
            List<EntityItem> nearestItems = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(getPos().getX() - 0.25, getPos().getY() - 0.25, getPos().getZ() - 0.25, getPos().getX() + 1.25, getPos().getY() + 1.25, getPos().getZ() + 1.25));
            if (nearestItems.size() > 0) {
                for (EntityItem item : nearestItems) {
                    if (item.isDead)
                        continue;
                    int slot = getInsertedSlot(item.getItem(), inventory);
                    if (slot != -1) {
                        item.setItem(inventory.insertItem(slot, item.getItem(), false));
                        if (item.getItem().isEmpty()) {
                            item.setDead();
                        }
                    }
                }
            }
        }
    }

    int getInsertedSlot(ItemStack stack, IItemHandler inventory) {
        int slot = -1;
        for (int j = 0; j < inventory.getSlots() && slot == -1; j++) {
            ItemStack added = inventory.insertItem(j, stack, true);
            if (added.getCount() < stack.getCount() || !added.isItemEqual(stack)) {
                slot = j;
            }
        }
        return slot;
    }

    @Override
    public void markDirty() {
        super.markDirty();
    }

    @Override
    public EnumPipeConnection getConnection(Direction facing) {
        IBlockState state = getWorld().getBlockState(getPos());
        Direction face = state.getValue(BlockVacuum.facing);
        return face.getOpposite() == facing ? EnumPipeConnection.PIPE : EnumPipeConnection.NONE;
    }
}
