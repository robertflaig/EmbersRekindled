package teamroots.embers.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraftforge.common.MinecraftForge;
import teamroots.embers.ConfigManager;
import teamroots.embers.api.capabilities.EmbersCapabilities;
import teamroots.embers.api.event.EmberRemoveEvent;
import teamroots.embers.api.item.IHeldEmberCell;
import teamroots.embers.api.item.IInventoryEmberCell;
import teamroots.embers.api.power.IEmberCapability;
import teamroots.embers.compat.BaublesIntegration;

public class EmberInventoryUtil {
    public static double getEmberCapacityTotal(PlayerEntity player) {
        double amount = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack.hasCapability(EmbersCapabilities.EMBER_CAPABILITY, null)) {
                IEmberCapability capability = stack.getCapability(EmbersCapabilities.EMBER_CAPABILITY, null);
                if (capability instanceof IInventoryEmberCell)
                    amount += capability.getEmberCapacity();
            }
        }
        ItemStack offhandItem = player.getHeldItem(Hand.OFF_HAND);
        if (offhandItem.hasCapability(EmbersCapabilities.EMBER_CAPABILITY, null)) {
            IEmberCapability capability = offhandItem.getCapability(EmbersCapabilities.EMBER_CAPABILITY, null);
            if (capability instanceof IHeldEmberCell)
                amount += capability.getEmberCapacity();
        }
        ItemStack heldItem = player.getHeldItem(Hand.MAIN_HAND);
        if (heldItem.hasCapability(EmbersCapabilities.EMBER_CAPABILITY, null)) {
            IEmberCapability capability = heldItem.getCapability(EmbersCapabilities.EMBER_CAPABILITY, null);
            if (capability instanceof IHeldEmberCell)
                amount += capability.getEmberCapacity();
        }
        if (ConfigManager.isBaublesIntegrationEnabled()) {
            amount += BaublesIntegration.getEmberCapacityTotal(player);
        }
        return amount;
    }

    public static double getEmberTotal(PlayerEntity player) {
        double amount = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack.hasCapability(EmbersCapabilities.EMBER_CAPABILITY, null)) {
                IEmberCapability capability = stack.getCapability(EmbersCapabilities.EMBER_CAPABILITY, null);
                if (capability instanceof IInventoryEmberCell)
                    amount += capability.getEmber();
            }
        }
        ItemStack offhandItem = player.getHeldItem(Hand.OFF_HAND);
        if (offhandItem.hasCapability(EmbersCapabilities.EMBER_CAPABILITY, null)) {
            IEmberCapability capability = offhandItem.getCapability(EmbersCapabilities.EMBER_CAPABILITY, null);
            if (capability instanceof IHeldEmberCell)
                amount += capability.getEmber();
        }
        ItemStack heldItem = player.getHeldItem(Hand.MAIN_HAND);
        if (heldItem.hasCapability(EmbersCapabilities.EMBER_CAPABILITY, null)) {
            IEmberCapability capability = heldItem.getCapability(EmbersCapabilities.EMBER_CAPABILITY, null);
            if (capability instanceof IHeldEmberCell)
                amount += capability.getEmber();
        }
        if (ConfigManager.isBaublesIntegrationEnabled()) {
            amount += BaublesIntegration.getEmberTotal(player);
        }
        return amount;
    }

    public static void removeEmber(PlayerEntity player, double amount) {
        EmberRemoveEvent event = new EmberRemoveEvent(player, amount);
        MinecraftForge.EVENT_BUS.post(event);
        double temp = event.getFinal();

        ItemStack offhandItem = player.getHeldItem(Hand.OFF_HAND);
        if (offhandItem.hasCapability(EmbersCapabilities.EMBER_CAPABILITY, null)) {
            IEmberCapability capability = offhandItem.getCapability(EmbersCapabilities.EMBER_CAPABILITY, null);
            if (capability instanceof IHeldEmberCell)
                temp -= capability.removeAmount(temp, true);
        }
        ItemStack heldItem = player.getHeldItem(Hand.MAIN_HAND);
        if (heldItem.hasCapability(EmbersCapabilities.EMBER_CAPABILITY, null)) {
            IEmberCapability capability = heldItem.getCapability(EmbersCapabilities.EMBER_CAPABILITY, null);
            if (capability instanceof IHeldEmberCell)
                temp -= capability.removeAmount(temp, true);
        }
        if (ConfigManager.isBaublesIntegrationEnabled()) {
            temp = BaublesIntegration.removeEmber(player, temp);
        }
        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack.hasCapability(EmbersCapabilities.EMBER_CAPABILITY, null)) {
                IEmberCapability capability = stack.getCapability(EmbersCapabilities.EMBER_CAPABILITY, null);
                if (capability instanceof IInventoryEmberCell)
                    temp -= capability.removeAmount(temp, true);
            }
        }
    }
}
