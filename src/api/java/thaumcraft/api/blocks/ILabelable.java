package thaumcraft.api.blocks;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

/**
 * 
 * @author Azanor
 * 
 * Tile entities or blocks that extend this interface can have jar labels applied to them
 *
 */
public interface ILabelable {

	/**
	 * This method is used by the block or tileentity to do whatever needs doing.	 
	 * @return if true then label will be subtracted from player inventory
	 */
	public boolean applyLabel(PlayerEntity player, BlockPos pos, Direction side, ItemStack labelstack);
	
}
