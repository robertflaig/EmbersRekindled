package teamroots.embers.tileentity;

import mysticalmechanics.api.IGearBehavior;
import mysticalmechanics.api.IGearbox;
import mysticalmechanics.api.MysticalMechanicsAPI;
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

public class TileEntityMechActuatorSingle extends TileEntityMechActuator {

    @Override
    public boolean canAttachGear(Direction facing) {
        return facing == getFacing();
    }
}
