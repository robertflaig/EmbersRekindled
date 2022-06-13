package teamroots.embers.tileentity;

import net.minecraft.util.Direction;
import teamroots.embers.util.EnumPipeConnection;

public interface IEmberPipeConnectable {
    EnumPipeConnection getConnection(Direction facing);
}
