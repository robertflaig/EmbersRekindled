package thaumcraft.api.crafting;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Container;

public class ContainerDummy extends Container{

	@Override
	public boolean canInteractWith(PlayerEntity var1) {
		return false;
	}

}
