package teamroots.embers.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import EnumActionResult;

public class ItemDebug extends ItemBase {
	public ItemDebug() {
		super("debug", false);
	}
	
	@Override
	public EnumActionResult onItemUse(
	PlayerEntity player, World world, BlockPos pos, Hand hand, Direction face, float hitX, float hitY, float hitZ){
		if (Blocks.TORCH.canPlaceBlockOnSide(world, pos, face)){
			((ItemBlock)Item.getItemFromBlock(Blocks.TORCH)).onItemUse(player, world, pos, hand, face, hitX, hitY, hitZ);
		}
		return EnumActionResult.SUCCESS;
	}
}
