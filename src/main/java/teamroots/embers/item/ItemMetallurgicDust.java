package teamroots.embers.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import teamroots.embers.util.OreTransmutationUtil;

import EnumActionResult;

public class ItemMetallurgicDust extends ItemBase {
    public ItemMetallurgicDust(String name, boolean addToTab) {
        super(name, addToTab);
    }

    @Override
    public EnumActionResult onItemUseFirst(PlayerEntity player, World worldIn, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, Hand hand) {
        if (worldIn.isRemote) {
            return EnumActionResult.SUCCESS;
        } else {
            ItemStack itemstack = player.getHeldItem(hand);

            if (!player.canPlayerEdit(pos, facing, itemstack)) {
                return EnumActionResult.FAIL;
            } else {
                boolean success = OreTransmutationUtil.transmuteOres(worldIn,pos);

                if(success) {
                    if (!player.capabilities.isCreativeMode) {
                        itemstack.shrink(1);
                    }

                    return EnumActionResult.SUCCESS;
                }
            }
        }

        return EnumActionResult.PASS;
    }
}
