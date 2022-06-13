package teamroots.embers.tileentity;

import mysticalmechanics.api.*;
import mysticalmechanics.tileentity.TileEntityMergebox;
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
import teamroots.embers.api.capabilities.EmbersCapabilities;
import teamroots.embers.api.tile.IExtraCapabilityInformation;
import teamroots.embers.block.BlockMechActuator;
import teamroots.embers.upgrade.UpgradeActuator;
import teamroots.embers.util.ConsumerMechCapability;
import teamroots.embers.util.Misc;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class TileEntityMechActuator extends TileEntity implements ITickableTileEntity, ITileEntityBase, IGearbox, IExtraCapabilityInformation {
    public UpgradeActuator upgrade;
    public GearHelperTile[] gears = new mysticalmechanics.api.GearHelperTile[6];

    public boolean shouldUpdate;

    private Random random = new Random();
    public ConsumerMechCapability capability = new ConsumerMechCapability() {
        @Override
        public void onPowerChange() {
            TileEntityMechActuator box = TileEntityMechActuator.this;
            shouldUpdate = true;
            box.markDirty();
        }

        @Override
        public double getVisualPower(Direction from) {
            GearHelper gearHelper = getGearHelper(from);
            if (gearHelper != null && gearHelper.isEmpty()) {
                return 0;
            }

            double unchangedPower = getExternalPower(from);

            if (gearHelper == null)
                return unchangedPower;

            IGearBehavior behavior = gearHelper.getBehavior();
            return behavior.transformVisualPower(TileEntityMechActuator.this, from, gearHelper.getGear(), gearHelper.getData(), unchangedPower);
        }

        @Override
        public double getPower(Direction from) {
            GearHelper gearHelper = getGearHelper(from);
            if (gearHelper != null && gearHelper.isEmpty()) {
                return 0;
            }
            return super.getPower(from);
        }

        @Override
        public void setPower(double value, Direction from) {
            GearHelper gearHelper = getGearHelper(from);
            if (isInput(from) && gearHelper.isEmpty()) {
                super.setPower(0, from);
            }
            if(isInput(from)) {
                powerExternal[from.getIndex()] = value;
                if(!gearHelper.isEmpty()) {
                    IGearBehavior behavior = gearHelper.getBehavior();
                    value = behavior.transformPower(TileEntityMechActuator.this, from,gearHelper.getGear(),gearHelper.getData(),value);
                    super.setPower(value, from);
                }
            }
        }

        @Override
        public boolean isInput(Direction from) {
            return canAttachGear(from);
        }
    };

    private GearHelper getGearHelper(Direction facing) {
        if (facing == null)
            return null;
        return gears[facing.getIndex()];
    }

    public TileEntityMechActuator() {
        upgrade = new UpgradeActuator(this);
        for(int i = 0; i < gears.length; i++)
            gears[i] = new GearHelperTile(this, Direction.getFront(i));
        capability.setAdditive(true); //Possible balance mistake but we shall see
    }

    public void updateNeighbors() {
        for (Direction f : Direction.VALUES) {
            MysticalMechanicsAPI.IMPL.pullPower(this, f, capability, !getGear(f).isEmpty());
        }
        markDirty();
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        for (int i = 0; i < 6; i++) {
            tag.setTag("side" + i, gears[i].write(new CompoundNBT()));
        }
        capability.write(tag);
        for (int i = 0; i < 6; i++) {
            tag.setDouble("mech_power" + i, capability.power[i]);
        }
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        for (int i = 0; i < 6; i++) {
            gears[i].read(tag.getCompoundTag("side" + i));
        }
        readLegacyGears(tag);
        for (int i = 0; i < 6; i++) {
            capability.power[i] = tag.getDouble("mech_power" + i);
        }
        capability.read(tag);
        capability.markDirty();
    }

    private void readLegacyGears(CompoundNBT tag) {
        for (int i = 0; i < 6; i++) {
            if(tag.contains("gear"+i))
                gears[i].setGear(new ItemStack(tag.getCompoundTag("gear" + i)));
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

    public Direction getFacing() {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof BlockMechActuator)
            return state.getValue(BlockMechActuator.facing);
        return null;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable Direction facing) {
        if (capability == EmbersCapabilities.UPGRADE_PROVIDER_CAPABILITY)
            return getFacing().getOpposite() == facing;
        if (capability == MysticalMechanicsAPI.MECH_CAPABILITY)
            return facing == null || canAttachGear(facing);
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (capability == EmbersCapabilities.UPGRADE_PROVIDER_CAPABILITY && getFacing().getOpposite() == facing)
            return (T) upgrade;
        if (capability == MysticalMechanicsAPI.MECH_CAPABILITY && (facing == null || canAttachGear(facing)))
            return (T) this.capability;
        return super.getCapability(capability, facing);
    }

    private double getGearInPower(Direction facing) {
        return capability.getExternalPower(facing);
    }

    private double getGearOutPower(Direction facing) {
        return capability.getInternalPower(facing);
    }

    @Override
    public void update() {
        if (shouldUpdate) {
            updateNeighbors();
            shouldUpdate = false;
        }
        for (Direction facing : Direction.VALUES) {
            int i = facing.getIndex();
            if (world.isRemote) {
                gears[i].visualUpdate(getGearInPower(facing), capability.getVisualPower(facing));
            }
            gears[i].tick(getGearInPower(facing), getGearOutPower(facing));
            if (gears[i].isDirty())
                shouldUpdate = true;
        }
    }

    @Override
    public boolean activate(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ) {
        ItemStack heldItem = player.getHeldItem(hand);
        if (!heldItem.isEmpty() && canAttachGear(side, heldItem)) {
            if (getGear(side).isEmpty() && MysticalMechanicsAPI.IMPL.isValidGear(heldItem)) {
                ItemStack gear = heldItem.copy();
                gear.setCount(1);
                attachGear(side, gear, player);
                heldItem.shrink(1);
                if (heldItem.isEmpty()) {
                    player.setHeldItem(hand, ItemStack.EMPTY);
                }
                capability.onPowerChange();
                return true;
            }
        } else if (!getGear(side).isEmpty()) {
            ItemStack gear = detachGear(side, player);
            if (!world.isRemote) {
                world.spawnEntity(new EntityItem(world, player.posX, player.posY + player.height / 2.0f, player.posZ, gear));
            }
            capability.onPowerChange();
            return true;
        }
        return false;
    }

    @Override
    public void onHarvest(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        for (int i = 0; i < 6; i++) {
            ItemStack stack = gears[i].detach(player);
            if (!world.isRemote) {
                world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack));
            }
        }
        capability.setPower(0f, null);
        updateNeighbors();
    }

    @Override
    public void markDirty() {
        super.markDirty();
        Misc.syncTE(this);
    }

    @Override
    public void attachGear(Direction facing, ItemStack stack, @Nullable PlayerEntity player) {
        if (!canAttachGear(facing))
            return;
        gears[facing.getIndex()].attach(player, stack);
        markDirty();
    }

    @Override
    public ItemStack detachGear(Direction facing, @Nullable PlayerEntity player) {
        if (!canAttachGear(facing))
            return ItemStack.EMPTY;
        ItemStack stack = gears[facing.getIndex()].detach(player);
        markDirty();
        return stack;
    }

    public ItemStack getGear(Direction facing) {
        if (!canAttachGear(facing))
            return ItemStack.EMPTY;
        return gears[facing.getIndex()].getGear();
    }

    @Override
    public boolean canAttachGear(Direction facing, ItemStack stack) {
        return canAttachGear(facing);
    }

    @Override
    public boolean canAttachGear(Direction facing) {
        return facing != null && getFacing().getAxis() != facing.getAxis();
    }

    @Override
    public int getConnections() {
        return 1;
    }

    @Override
    public boolean hasCapabilityDescription(Capability<?> capability) {
        return false;
    }

    @Override
    public void addCapabilityDescription(List<String> strings, Capability<?> capability, Direction facing) {
        //NOOP
    }
}
