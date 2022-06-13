package teamroots.embers.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBucket;
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
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.TileFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import teamroots.embers.Embers;
import teamroots.embers.EventManager;
import teamroots.embers.SoundManager;
import teamroots.embers.api.EmbersAPI;
import teamroots.embers.api.event.DialInformationEvent;
import teamroots.embers.api.event.EmberEvent;
import teamroots.embers.api.misc.IMetalCoefficient;
import teamroots.embers.api.tile.IExtraCapabilityInformation;
import teamroots.embers.api.tile.IExtraDialInformation;
import teamroots.embers.api.upgrades.IUpgradeProvider;
import teamroots.embers.api.upgrades.UpgradeUtil;
import teamroots.embers.block.BlockEmberGauge;
import teamroots.embers.network.PacketHandler;
import teamroots.embers.network.message.MessageEmberActivationFX;
import teamroots.embers.util.Misc;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TileEntityBoilerBottom extends TileFluidHandler implements ITileEntityBase, ITickableTileEntity, IExtraDialInformation, IExtraCapabilityInformation {
    public static final float BASE_MULTIPLIER = 1.5f;
    public static final int FLUID_CONSUMED = 25;
    public static final float PER_BLOCK_MULTIPLIER = 0.375f;
    public static final int PROCESS_TIME = 20;
    public static int capacity = Fluid.BUCKET_VOLUME * 8;
    Random random = new Random();
    int progress = -1;
    public ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            TileEntityBoilerBottom.this.markDirty();
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (EmbersAPI.getEmberValue(stack) == 0) {
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }
    };
    private List<IUpgradeProvider> upgrades = new ArrayList<>();

    public TileEntityBoilerBottom() {
        super();
        tank = new FluidTank(capacity);
        tank.setTileEntity(this);
        tank.setCanFill(true);
        tank.setCanDrain(true);
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        tag.setTag("inventory", inventory.serializeNBT());
        tag.setInteger("progress", progress);
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        inventory.deserializeNBT(tag.getCompoundTag("inventory"));
        if (tag.contains("progress")) {
            progress = tag.getInteger("progress");
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
        boolean didFill = FluidUtil.interactWithFluidHandler(player, hand, tank);
        if (didFill) {
            this.markDirty();
            return true;
        }
        return false;
    }

    @Override
    public void onHarvest(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        this.remove();
        Misc.spawnInventoryInWorld(getWorld(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, inventory);
        world.setTileEntity(pos, null);
    }

    public double getMultiplier() {
        IBlockState metalState = world.getBlockState(pos.down());
        IMetalCoefficient metalCoefficient = EmbersAPI.getMetalCoefficient(metalState);
        double metalMultiplier = metalCoefficient != null ? metalCoefficient.getCoefficient(metalState) : 0.0;
        double totalMult = BASE_MULTIPLIER;
        for (Direction facing : Direction.HORIZONTALS) {
            IBlockState state = world.getBlockState(pos.down().offset(facing));
            if (state.getBlock() == Blocks.LAVA || state.getBlock() == Blocks.FLOWING_LAVA || state.getBlock() == Blocks.FIRE) {
                totalMult += PER_BLOCK_MULTIPLIER * metalMultiplier;
            }
        }
        return totalMult;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, Direction facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, Direction facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) this.inventory;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void update() {
        upgrades = UpgradeUtil.getUpgrades(world, pos, Direction.HORIZONTALS);
        UpgradeUtil.verifyUpgrades(this, upgrades);
        if (UpgradeUtil.doTick(this, upgrades))
            return;

        TileEntity tile = getWorld().getTileEntity(getPos().up());
        int i = random.nextInt(inventory.getSlots());
        ItemStack emberStack = inventory.getStackInSlot(i);
        if (tank.getFluid() != null && tank.getFluid().getFluid() == FluidRegistry.WATER && tank.getFluidAmount() >= FLUID_CONSUMED && !emberStack.isEmpty()) {
            boolean cancel = UpgradeUtil.doWork(this, upgrades);
            if (!cancel && tile instanceof TileEntityBoilerTop) {
                TileEntityBoilerTop top = (TileEntityBoilerTop) tile;
                progress++;
                if (progress > UpgradeUtil.getWorkTime(this, PROCESS_TIME, upgrades)) {
                    progress = 0;
                    double emberValue = EmbersAPI.getEmberValue(emberStack);
                    double ember = UpgradeUtil.getTotalEmberProduction(this, emberValue * getMultiplier(), upgrades);
                    if (ember > 0 && top.capability.getEmber() + ember <= top.capability.getEmberCapacity()) {
                        if (!world.isRemote) {
                            world.playSound(null, getPos().getX() + 0.5, getPos().getY() + 1.5, getPos().getZ() + 0.5, SoundManager.PRESSURE_REFINERY, SoundCategory.BLOCKS, 1.0f, 1.0f);
                            PacketHandler.INSTANCE.sendToAll(new MessageEmberActivationFX(getPos().getX() + 0.5f, getPos().getY() + 1.5f, getPos().getZ() + 0.5f));
                        }
                        UpgradeUtil.throwEvent(this, new EmberEvent(this, EmberEvent.EnumType.PRODUCE, ember), upgrades);
                        top.capability.addAmount(ember, true);
                        inventory.extractItem(i, 1, false);
                        tank.drain(FLUID_CONSUMED, true);
                        markDirty();
                        top.markDirty();
                    }
                }
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
    public void addDialInformation(Direction facing, List<String> information, String dialType) {
        if(BlockEmberGauge.DIAL_TYPE.equals(dialType)) {
            DecimalFormat multiplierFormat = Embers.proxy.getDecimalFormat("embers.decimal_format.ember_multiplier");
            double multiplier = getMultiplier();
            information.add(I18n.format("embers.tooltip.dial.ember_multiplier",multiplierFormat.format(multiplier)));
        }
        UpgradeUtil.throwEvent(this, new DialInformationEvent(this, information, dialType), upgrades);
    }

    @Override
    public boolean hasCapabilityDescription(Capability<?> capability) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
    }

    @Override
    public void addCapabilityDescription(List<String> strings, Capability<?> capability, Direction facing) {
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            strings.add(IExtraCapabilityInformation.formatCapability(EnumIOType.INPUT,"embers.tooltip.goggles.item",I18n.format("embers.tooltip.goggles.item.ember")));
        if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            strings.add(IExtraCapabilityInformation.formatCapability(EnumIOType.INPUT,"embers.tooltip.goggles.fluid",I18n.format("embers.tooltip.goggles.fluid.water")));
    }
}
