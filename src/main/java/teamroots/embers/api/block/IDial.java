package teamroots.embers.api.block;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public interface IDial {
    List<String> getDisplayInfo(World world, BlockPos pos, IBlockState state);

    void updateTEData(World world, BlockState state, BlockPos pos);

    String getDialType();
}
