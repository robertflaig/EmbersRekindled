package teamroots.embers.power;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import teamroots.embers.api.power.IEmberCapability;

public class EmberCapabilityStorage implements IStorage<IEmberCapability> {

	@Override
	public NBTBase writeNBT(Capability<IEmberCapability> capability, IEmberCapability instance, Direction side) {
		return null;
	}

	@Override
	public void readNBT(Capability<IEmberCapability> capability, IEmberCapability instance, Direction side,
			NBTBase nbt) {
		
	}

}
