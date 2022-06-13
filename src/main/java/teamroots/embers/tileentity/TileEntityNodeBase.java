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
import teamroots.embers.api.upgrades.IUpgradeProvider;
import teamroots.embers.block.BlockNodeBase;
import teamroots.embers.upgrade.UpgradeConservation;
import teamroots.embers.util.Misc;

import javax.annotation.Nullable;

public abstract class TileEntityNodeBase extends TileEntity implements ITileEntityBase {
	protected IUpgradeProvider upgrade;

	public TileEntityNodeBase() {
		super();
		upgrade = initUpgrade();
	}

	protected abstract IUpgradeProvider initUpgrade();

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable Direction facing)
	{
		if(capability == EmbersCapabilities.UPGRADE_PROVIDER_CAPABILITY) {
			return facing == getFacing();
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	@Nullable
	public <T> T getCapability(Capability<T> capability, @Nullable Direction facing)
	{
		if(capability == EmbersCapabilities.UPGRADE_PROVIDER_CAPABILITY && facing == getFacing()) {
			return (T) upgrade;
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public boolean activate(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand,
							Direction side, float hitX, float hitY, float hitZ) {
		return false;
	}

	public Direction getFacing() {
		IBlockState state = world.getBlockState(pos);
		return state.getValue(BlockNodeBase.facing);
	}

	@Override
	public void onHarvest(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		this.remove();
		world.setTileEntity(pos, null);
	}

	@Override
	public void markDirty() {
		super.markDirty();
		Misc.syncTE(this);
	}
}
