package teamroots.embers.api.misc;

import net.minecraft.block.BlockState;

public interface IMetalCoefficient {
    boolean matches(IBlockState state);

    double getCoefficient(IBlockState state);
}
