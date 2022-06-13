package teamroots.embers.tileentity;

import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
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
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import teamroots.embers.*;
import teamroots.embers.api.capabilities.EmbersCapabilities;
import teamroots.embers.api.event.EmberEvent;
import teamroots.embers.api.power.IEmberCapability;
import teamroots.embers.api.tile.IBin;
import teamroots.embers.api.tile.IExtraCapabilityInformation;
import teamroots.embers.api.upgrades.IUpgradeProvider;
import teamroots.embers.api.upgrades.UpgradeUtil;
import teamroots.embers.network.PacketHandler;
import teamroots.embers.network.message.MessageAshenAmuletFX;
import teamroots.embers.particle.ParticleUtil;
import teamroots.embers.power.DefaultEmberCapability;
import teamroots.embers.util.Misc;
import teamroots.embers.util.sound.ISoundController;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class TileEntityCinderPlinth extends TileEntity implements ITileEntityBase, ITickableTileEntity, ISoundController, IExtraCapabilityInformation {
    public static double EMBER_COST = 0.5;
    public static int PROCESS_TIME = 40;
    public IEmberCapability capability = new DefaultEmberCapability();
    int angle = 0;
    int turnRate = 0;
    int progress = 0;
    public ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            // We need to tell the tile entity that something has changed so
            // that the chest contents is persisted
            TileEntityCinderPlinth.this.markDirty();
        }
    };
    Random random = new Random();

    public static final int SOUND_PROCESS = 1;
    public static final int[] SOUND_IDS = new int[]{SOUND_PROCESS};

    HashSet<Integer> soundsPlaying = new HashSet<>();

    public TileEntityCinderPlinth() {
        super();
        capability.setEmberCapacity(4000);
        capability.setEmber(0);
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        capability.write(tag);
        tag.setInteger("progress", 0);
        tag.setTag("inventory", inventory.serializeNBT());
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        capability.read(tag);
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
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        if (capability == EmbersCapabilities.EMBER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, Direction facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) this.inventory;
        }
        if (capability == EmbersCapabilities.EMBER_CAPABILITY) {
            return (T) this.capability;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        Misc.syncTE(this);
    }

    @Override
    public boolean activate(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand,
                            Direction side, float hitX, float hitY, float hitZ) {
        ItemStack heldItem = player.getHeldItem(hand);
        if (!heldItem.isEmpty()) {
            player.setHeldItem(hand, this.inventory.insertItem(0, heldItem, false));
            markDirty();
            return true;
        } else {
            if (!inventory.getStackInSlot(0).isEmpty()) {
                if (!getWorld().isRemote) {
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
        Misc.spawnInventoryInWorld(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, inventory);
        world.setTileEntity(pos, null);
    }

    @Override
    public void update() {
        turnRate = 1;
        List<IUpgradeProvider> upgrades = UpgradeUtil.getUpgrades(world, pos, Direction.VALUES);
        UpgradeUtil.verifyUpgrades(this, upgrades);
        if (UpgradeUtil.doTick(this, upgrades))
            return;
        if (getWorld().isRemote)
            handleSound();

        if (shouldWork()) {
            boolean cancel = UpgradeUtil.doWork(this, upgrades);
            if (!cancel) {
                progress++;
                if (getWorld().isRemote) {
                    ParticleUtil.spawnParticleSmoke(getWorld(), (float) getPos().getX() + 0.5f, (float) getPos().getY() + 0.875f, (float) getPos().getZ() + 0.5f, 0.0125f * (random.nextFloat() - 0.5f), 0.025f * (random.nextFloat() + 1.0f), 0.0125f * (random.nextFloat() - 0.5f), 72, 72, 72, 0.6f, 3.0f + random.nextFloat(), 48);
                }
                double emberCost = UpgradeUtil.getTotalEmberConsumption(this, EMBER_COST, upgrades);
                UpgradeUtil.throwEvent(this, new EmberEvent(this, EmberEvent.EnumType.CONSUME, emberCost), upgrades);
                capability.removeAmount(emberCost, true);
                if (progress > UpgradeUtil.getWorkTime(this, PROCESS_TIME, upgrades)) {
                    progress = 0;
                    TileEntity tile = getWorld().getTileEntity(getPos().down());
                    List<ItemStack> outputs = Lists.newArrayList(new ItemStack(RegistryManager.dust_ash, 1));
                    UpgradeUtil.transformOutput(this, outputs, upgrades);
                    inventory.extractItem(0, 1, false);
                    for (ItemStack remainder : outputs) {
                        if (tile instanceof IBin) {
                            remainder = ((IBin) tile).getInventory().insertItem(0, remainder, false);
                        }
                        if (!remainder.isEmpty() && !getWorld().isRemote) {
                            getWorld().spawnEntity(new EntityItem(getWorld(), getPos().getX() + 0.5, getPos().getY() + 1.0, getPos().getZ() + 0.5, remainder));
                        }
                    }
                    if(!getWorld().isRemote) {
                        AxisAlignedBB aabb = new AxisAlignedBB(
                                pos.getX() + 0.3,
                                pos.getY() + 0.9,
                                pos.getZ() + 0.3,
                                pos.getX() + 0.7,
                                pos.getY() + 1.3,
                                pos.getZ() + 0.7);
                        PacketHandler.INSTANCE.sendToAll(new MessageAshenAmuletFX(aabb));
                    }
                }
                markDirty();
            }
        } else {
            if (progress != 0) {
                progress = 0;
                markDirty();
            }
        }
        angle += turnRate;
    }

    private boolean shouldWork() {
        return !inventory.getStackInSlot(0).isEmpty() && capability.getEmber() > 0;
    }

    @Override
    public void playSound(int id) {
        switch (id) {
            case SOUND_PROCESS:
                Embers.proxy.playMachineSound(this, SOUND_PROCESS, SoundManager.PLINTH_LOOP, SoundCategory.BLOCKS, true, 1.0f, 1.0f, (float) pos.getX() + 0.5f, (float) pos.getY() + 0.5f, (float) pos.getZ() + 0.5f);
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
        return id == SOUND_PROCESS && shouldWork();
    }

    @Override
    public boolean hasCapabilityDescription(Capability<?> capability) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
    }

    @Override
    public void addCapabilityDescription(List<String> strings, Capability<?> capability, Direction facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            strings.add(IExtraCapabilityInformation.formatCapability(EnumIOType.INPUT, "embers.tooltip.goggles.item", null));
            strings.add(IExtraCapabilityInformation.formatCapability(EnumIOType.OUTPUT, "embers.tooltip.goggles.item", I18n.format("embers.tooltip.goggles.item.ash")));
        }
    }
}
