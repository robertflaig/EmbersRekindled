package thaumcraft.api.research;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.common.eventhandler.Event;
import thaumcraft.api.capabilities.IPlayerKnowledge.EnumKnowledgeType;

/**
 * These events trigger whenever a players gains knowledge or progresses research. 
 * They can be cancelled to prevent the research gain.
 */
public class ResearchEvent extends Event {

	private final PlayerEntity player;
	
	public ResearchEvent(PlayerEntity player)
    {
        this.player = player;
    }

    public PlayerEntity getPlayer()
    {
        return player;
    }
	
	
	public static class Knowledge extends ResearchEvent
    {
		private final EnumKnowledgeType type;
		private final ResearchCategory category;
		private final int amount;
		
		public Knowledge(PlayerEntity player, EnumKnowledgeType type, ResearchCategory category, int amount)
        {
            super(player);
            this.type = type;
            this.category = category;
            this.amount = amount;
        }

		public EnumKnowledgeType getType() {
			return type;
		}

		public ResearchCategory getCategory() {
			return category;
		}

		public int getAmount() {
			return amount;
		}		
		
    }
	
	public static class Research extends ResearchEvent
    {
		private final String researchKey;
		
		public Research(PlayerEntity player, String researchKey)
        {
            super(player);
            this.researchKey = researchKey;
        }
		
		public String getResearchKey() {
			return researchKey;
		}		
    }

	@Override
	public boolean isCancelable() {
		return true;
	}
	
	
}
