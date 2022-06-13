package thaumcraft.api.casters;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface ICaster {
	
	public abstract float getConsumptionModifier(ItemStack is, PlayerEntity player, boolean crafting);

	public abstract boolean consumeVis(ItemStack is, PlayerEntity player, float amount, boolean crafting, boolean simulate);

	public abstract Item getFocus(ItemStack stack); 

	public abstract ItemStack getFocusStack(ItemStack stack);

	public abstract void setFocus(ItemStack stack, ItemStack focus);

	public ItemStack getPickedBlock(ItemStack stack);

}