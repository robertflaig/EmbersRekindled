package teamroots.embers.tileentity;

import net.minecraft.util.Direction;
import teamroots.embers.util.EnumPipeConnection;

public interface IItemPipeConnectable {
    EnumPipeConnection getConnection(Direction facing);
}
