package teamroots.embers.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import teamroots.embers.block.BlockBaseGauge;

public abstract class TileEntityBaseGauge extends TileEntity implements ITileEntityBase, ITickableTileEntity {
    int comparatorValue = 0;

    public int getComparatorValue() {
        return comparatorValue;
    }

    @Override
    public void update() {
        int oldComparatorValue = comparatorValue;
        Direction facing = getFacing();
        TileEntity tileEntity = world.getTileEntity(pos.offset(facing.getOpposite()));
        if(tileEntity != null)
            comparatorValue = calculateComparatorValue(tileEntity,facing);
        if(comparatorValue != oldComparatorValue) {
            world.updateComparatorOutputLevel(pos, world.getBlockState(pos).getBlock());
        }
    }

    private Direction getFacing() {
        IBlockState state = world.getBlockState(pos);
        return state.getValue(BlockBaseGauge.facing);
    }

    @Override
    public boolean activate(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ) {
        return false;
    }

    @Override
    public void onHarvest(World world, BlockPos pos, BlockState state, PlayerEntity player) {

    }

    public abstract int calculateComparatorValue(TileEntity tileEntity, Direction facing);

    public abstract String getDialType();
}
