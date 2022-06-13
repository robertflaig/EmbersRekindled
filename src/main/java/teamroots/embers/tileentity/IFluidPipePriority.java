package teamroots.embers.tileentity;

import net.minecraft.util.Direction;

public interface IFluidPipePriority {
    int getPriority(Direction facing);
}
