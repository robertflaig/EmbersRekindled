package teamroots.embers.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import teamroots.embers.Embers;
import teamroots.embers.EventManager;
import teamroots.embers.SoundManager;
import teamroots.embers.api.event.EmberEvent;
import teamroots.embers.api.event.MachineRecipeEvent;
import teamroots.embers.api.tile.IExtraCapabilityInformation;
import teamroots.embers.api.tile.IExtraDialInformation;
import teamroots.embers.api.tile.IMechanicallyPowered;
import teamroots.embers.api.upgrades.IUpgradeProvider;
import teamroots.embers.api.upgrades.UpgradeUtil;
import teamroots.embers.block.BlockFluidGauge;
import teamroots.embers.recipe.FluidMixingRecipe;
import teamroots.embers.recipe.RecipeRegistry;
import teamroots.embers.util.FluidUtil;
import teamroots.embers.util.Misc;
import teamroots.embers.util.sound.ISoundController;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class TileEntityMixerBottom extends TileEntity implements ITileEntityBase, ITickableTileEntity, IMechanicallyPowered, ISoundController, IExtraDialInformation, IExtraCapabilityInformation {
    public static final double EMBER_COST = 2.0;

    public FluidTank north = new FluidTank(8000);
    public FluidTank south = new FluidTank(8000);
    public FluidTank east = new FluidTank(8000);
    public FluidTank west = new FluidTank(8000);
    public FluidTank[] tanks;
    Random random = new Random();
    int progress = -1;
    boolean isWorking;

    public static final int SOUND_PROCESS = 1;
    public static final int[] SOUND_IDS = new int[]{SOUND_PROCESS};

    HashSet<Integer> soundsPlaying = new HashSet<>();
    private List<IUpgradeProvider> upgrades;
    private double powerRatio;

    public TileEntityMixerBottom() {
        super();
        tanks = new FluidTank[]{north, south, east, west};
    }

    public FluidTank[] getTanks() {
        return tanks;
    }

    public ArrayList<FluidStack> getFluids() {
        ArrayList<FluidStack> fluids = new ArrayList<>();
        if (north.getFluid() != null) {
            fluids.add(north.getFluid());
        }
        if (south.getFluid() != null) {
            fluids.add(south.getFluid());
        }
        if (east.getFluid() != null) {
            fluids.add(east.getFluid());
        }
        if (west.getFluid() != null) {
            fluids.add(west.getFluid());
        }
        return fluids;
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        CompoundNBT northTank = new CompoundNBT();
        north.write(northTank);
        tag.setTag("northTank", northTank);
        CompoundNBT southTank = new CompoundNBT();
        south.write(southTank);
        tag.setTag("southTank", southTank);
        CompoundNBT eastTank = new CompoundNBT();
        east.write(eastTank);
        tag.setTag("eastTank", eastTank);
        CompoundNBT westTank = new CompoundNBT();
        west.write(westTank);
        tag.setTag("westTank", westTank);
        tag.setInteger("progress", progress);
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        north.read(tag.getCompoundTag("northTank"));
        south.read(tag.getCompoundTag("southTank"));
        east.read(tag.getCompoundTag("eastTank"));
        west.read(tag.getCompoundTag("westTank"));
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
        return false;
    }

    @Override
    public void onHarvest(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        this.remove();
        world.setTileEntity(pos, null);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, Direction facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != Direction.UP && facing != Direction.DOWN && facing != null) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, Direction facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            switch (facing) {
                case DOWN:
                    break;
                case EAST:
                    return (T) east;
                case NORTH:
                    return (T) north;
                case SOUTH:
                    return (T) south;
                case UP:
                    break;
                case WEST:
                    return (T) west;
            }
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void update() {
        if (getWorld().isRemote)
            handleSound();
        World world = getWorld();
        BlockPos pos = getPos();
        TileEntityMixerTop top = (TileEntityMixerTop) world.getTileEntity(pos.up());
        isWorking = false;
        if (top != null) {
            upgrades = UpgradeUtil.getUpgrades(world, pos.up(), Direction.VALUES);
            UpgradeUtil.verifyUpgrades(this, upgrades);
            if (UpgradeUtil.doTick(this, upgrades))
                return;
            ArrayList<FluidStack> fluids = getFluids();
            FluidMixingRecipe recipe = getRecipe(fluids);
            if (recipe != null)
                powerRatio = recipe.getPowerRatio();
            else
                powerRatio = 0;
            double emberCost = UpgradeUtil.getTotalEmberConsumption(this, EMBER_COST, upgrades);
            if (top.capability.getEmber() >= emberCost) {
                if (recipe != null) {
                    boolean cancel = UpgradeUtil.doWork(this, upgrades);
                    if(!cancel) {
                        IFluidHandler tank = top.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
                        FluidStack output = recipe.getResult(fluids);
                        output = UpgradeUtil.transformOutput(this, output, upgrades);
                        int amount = tank.fill(output, false);
                        if (amount != 0) {
                            UpgradeUtil.throwEvent(this, new MachineRecipeEvent.Success<>(this, recipe), upgrades);
                            isWorking = true;
                            tank.fill(output, true);
                            consumeFluids(recipe);
                            UpgradeUtil.throwEvent(this, new EmberEvent(this, EmberEvent.EnumType.CONSUME, emberCost), upgrades);
                            top.capability.removeAmount(emberCost, true);
                            markDirty();
                            top.markDirty();
                        }
                    }
                }
            }
        }
    }

    private FluidMixingRecipe getRecipe(ArrayList<FluidStack> fluids) {
        FluidMixingRecipe recipe = RecipeRegistry.getMixingRecipe(fluids);
        MachineRecipeEvent<FluidMixingRecipe> event = new MachineRecipeEvent<>(this, recipe);
        UpgradeUtil.throwEvent(this, event, upgrades);
        return event.getRecipe();
    }

    public void consumeFluids(FluidMixingRecipe recipe) {
        for (int j = 0; j < recipe.inputs.size(); j++) {
            FluidStack recipeFluid = recipe.inputs.get(j).copy();
            for (FluidTank tank : tanks) {
                FluidStack tankFluid = tank.getFluid();
                if (recipeFluid != null && tankFluid != null && FluidUtil.areFluidsEqual(recipeFluid.getFluid(),tankFluid.getFluid())) {
                    FluidStack stack = tank.drain(recipeFluid.amount, true);
                    recipeFluid.amount -= stack != null ? stack.amount : 0;
                }
            }
        }
    }

    @Override
    public void playSound(int id) {
        switch (id) {
            case SOUND_PROCESS:
                Embers.proxy.playMachineSound(this, SOUND_PROCESS, SoundManager.MIXER_LOOP, SoundCategory.BLOCKS, true, 1.0f, 1.0f, (float) pos.getX() + 0.5f, (float) pos.getY() + 1.0f, (float) pos.getZ() + 0.5f);
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
        return id == SOUND_PROCESS && isWorking;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        Misc.syncTE(this);
    }

    @Override
    public void addDialInformation(Direction facing, List<String> information, String dialType) {
        if(BlockFluidGauge.DIAL_TYPE.equals(dialType)) {
            information.clear();
            information.add(TextFormatting.BOLD.toString()+I18n.format("embers.tooltip.side.north")+TextFormatting.RESET.toString()+" "+BlockFluidGauge.formatFluidStack(north.getFluid(),north.getCapacity()));
            information.add(TextFormatting.BOLD.toString()+I18n.format("embers.tooltip.side.east")+TextFormatting.RESET.toString()+" "+BlockFluidGauge.formatFluidStack(east.getFluid(),east.getCapacity()));
            information.add(TextFormatting.BOLD.toString()+I18n.format("embers.tooltip.side.south")+TextFormatting.RESET.toString()+" "+BlockFluidGauge.formatFluidStack(south.getFluid(),south.getCapacity()));
            information.add(TextFormatting.BOLD.toString()+I18n.format("embers.tooltip.side.west")+TextFormatting.RESET.toString()+" "+BlockFluidGauge.formatFluidStack(west.getFluid(),south.getCapacity()));
        }
    }

    @Override
    public boolean hasCapabilityDescription(Capability<?> capability) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
    }

    @Override
    public void addCapabilityDescription(List<String> strings, Capability<?> capability, Direction facing) {
        strings.add(IExtraCapabilityInformation.formatCapability(EnumIOType.INPUT,"embers.tooltip.goggles.fluid",I18n.format("embers.tooltip.goggles.fluid.metal")));
    }

    @Override
    public double getMinimumPower() {
        return 20;
    }

    @Override
    public double getMechanicalSpeed(double power) {
        return Misc.getDiminishedPower(power,80,1.5/80);
    }

    @Override
    public double getNominalSpeed() {
        return 1;
    }

    @Override
    public double getStandardPowerRatio() {
        return powerRatio;
    }
}
