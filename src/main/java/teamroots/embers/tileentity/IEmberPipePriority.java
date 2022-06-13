package teamroots.embers.tileentity;

import net.minecraft.util.Direction;

public interface IEmberPipePriority {
    int getPriority(Direction facing);
}
