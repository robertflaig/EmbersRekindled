package teamroots.embers.api.event;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.common.eventhandler.Event;

public class InfoGogglesEvent extends Event {
    PlayerEntity player;
    boolean shouldDisplay;

    public InfoGogglesEvent(PlayerEntity player, boolean shouldDisplay) {
        this.player = player;
        this.shouldDisplay = shouldDisplay;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public boolean shouldDisplay() {
        return shouldDisplay;
    }

    public void setShouldDisplay(boolean shouldDisplay) {
        this.shouldDisplay = shouldDisplay;
    }
}
