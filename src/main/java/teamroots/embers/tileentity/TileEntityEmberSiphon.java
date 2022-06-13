package teamroots.embers.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import teamroots.embers.api.capabilities.EmbersCapabilities;
import teamroots.embers.upgrade.UpgradeSiphon;

import javax.annotation.Nullable;

public class TileEntityEmberSiphon extends TileEntity implements ITileEntityBase {
    public UpgradeSiphon upgrade;

    public TileEntityEmberSiphon() {
        upgrade = new UpgradeSiphon(this);
    }

    @Override
    public boolean activate(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ) {
        return false;
    }

    @Override
    public void onHarvest(World world, BlockPos pos, BlockState state, PlayerEntity player) {

    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable Direction facing) {
        if (capability == EmbersCapabilities.UPGRADE_PROVIDER_CAPABILITY)
            return facing == Direction.UP;
        if (capability == EmbersCapabilities.EMBER_CAPABILITY && (facing == null || facing.getAxis() != Direction.Axis.Y)) {
            TileEntity tile = world.getTileEntity(pos.up());
            return tile != null && tile.hasCapability(EmbersCapabilities.EMBER_CAPABILITY,Direction.DOWN);
        }
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (capability == EmbersCapabilities.UPGRADE_PROVIDER_CAPABILITY && facing == Direction.UP)
            return (T) upgrade;
        if (capability == EmbersCapabilities.EMBER_CAPABILITY && (facing == null || facing.getAxis() != Direction.Axis.Y)) {
            TileEntity tile = world.getTileEntity(pos.up());
            return (T) tile.getCapability(EmbersCapabilities.EMBER_CAPABILITY,Direction.DOWN);
        }
        return super.getCapability(capability, facing);
    }
}
