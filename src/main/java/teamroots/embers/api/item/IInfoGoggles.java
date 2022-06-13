package teamroots.embers.api.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public interface IInfoGoggles {
    boolean shouldDisplayInfo(PlayerEntity player, ItemStack stack, EntityEquipmentSlot slot);
}
