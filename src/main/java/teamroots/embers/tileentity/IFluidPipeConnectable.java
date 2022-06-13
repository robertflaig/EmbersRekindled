package teamroots.embers.tileentity;

import net.minecraft.util.Direction;
import teamroots.embers.util.EnumPipeConnection;

public interface IFluidPipeConnectable {
    EnumPipeConnection getConnection(Direction facing);
}
