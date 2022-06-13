package teamroots.embers.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import teamroots.embers.Embers;
import teamroots.embers.api.filter.IFilter;
import teamroots.embers.api.item.IFilterItem;
import teamroots.embers.gui.GuiHandler;
import teamroots.embers.tileentity.ISpecialFilter;
import teamroots.embers.util.FilterUtil;

import javax.annotation.Nullable;
import java.util.List;

public class ItemGolemEye extends ItemBase implements IFilterItem {
    private static ThreadLocal<Float> eyeOpen = new ThreadLocal<>();

    public static void setEyeOpen(float amt) {
        eyeOpen.set(amt);
    }

    public ItemGolemEye(String name) {
        super(name, true);
        addPropertyOverride(new ResourceLocation(Embers.MODID,"eye_open"), (stack, worldIn, entityIn) -> eyeOpen.get() != null ? eyeOpen.get() : 0);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        playerIn.openGui(Embers.instance, GuiHandler.EYE, worldIn, playerIn.getPosition().getX(), playerIn.getPosition().getY(), playerIn.getPosition().getZ());
        return new ActionResult<>(EnumActionResult.PASS, playerIn.getHeldItem(handIn));
    }

    @Override
    public EnumActionResult onItemUseFirst(PlayerEntity player, World world, BlockPos pos, Direction side, float hitX, float hitY, float hitZ, Hand hand) {
        TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof ISpecialFilter && player.isSneaking()) {
            ItemStack held = player.getHeldItem(hand);
            setFilter(held, ((ISpecialFilter) tile).getSpecialFilter());
            player.setHeldItem(hand,held);
            return EnumActionResult.SUCCESS;
        }
        return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
    }

    public void setFilter(ItemStack filterStack, IFilter filter) {
        CompoundNBT compound = getOrCreateTagCompound(filterStack);
        compound.put("filter",filter.write(new CompoundNBT()));
    }

    @Override
    public IFilter getFilter(ItemStack filterStack) {
        CompoundNBT compound = filterStack.getTagCompound();
        if (compound == null)
            return FilterUtil.FILTER_ANY;
        return FilterUtil.deserializeFilter(compound.getCompoundTag("filter"));
    }

    private CompoundNBT getOrCreateTagCompound(ItemStack filterStack) {
        CompoundNBT compound = filterStack.getTagCompound();
        if(compound == null) {
            compound = new CompoundNBT();
            filterStack.setTagCompound(compound);
        }
        return compound;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (!stack.hasTagCompound())
            return;
        IFilter filter = getFilter(stack);
        tooltip.add(filter.formatFilter());
    }

    public void reset(ItemStack filterStack) {
        CompoundNBT compound = filterStack.getTagCompound();
        if (compound == null)
            return;
        compound.removeTag("comparator");
        compound.removeTag("offset");
    }
}
