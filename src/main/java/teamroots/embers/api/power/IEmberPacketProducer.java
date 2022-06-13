package teamroots.embers.api.power;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public interface IEmberPacketProducer {
	void setTargetPosition(BlockPos pos, Direction side);
}
