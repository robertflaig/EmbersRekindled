package teamroots.embers.api.filter;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;

import ResourceLocation;

public interface IFilter {
    ResourceLocation getType();

    boolean acceptsItem(ItemStack stack);

    default boolean acceptsItem(ItemStack stack, IItemHandler handler) {
        return acceptsItem(stack);
    }

    String formatFilter();

    CompoundNBT write(CompoundNBT tag);

    void read(CompoundNBT tag);
}
