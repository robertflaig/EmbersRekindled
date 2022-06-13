package teamroots.embers.itemmod;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import teamroots.embers.api.EmbersAPI;
import teamroots.embers.api.itemmod.ItemModUtil;
import teamroots.embers.api.itemmod.ModifierBase;
import teamroots.embers.util.EmberInventoryUtil;

import teamroots.embers.api.itemmod.ModifierBase.EnumType;

public class ModifierIntelligentApparatus extends ModifierBase {

    public ModifierIntelligentApparatus() {
        super(EnumType.ARMOR, "intelligent_apparatus", 4.0, true);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onXPDrop(LivingExperienceDropEvent event) {
        if (event.getAttackingPlayer() != null) {
            PlayerEntity player = event.getAttackingPlayer();
            int level = ItemModUtil.getArmorModifierLevel(player, EmbersAPI.INTELLIGENT_APPARATUS);
            if (level > 0 && EmberInventoryUtil.getEmberTotal(player) >= cost) {
                EmberInventoryUtil.removeEmber(player, cost);
                event.setDroppedExperience(event.getDroppedExperience() * (level + 1));
            }
        }
    }
}
