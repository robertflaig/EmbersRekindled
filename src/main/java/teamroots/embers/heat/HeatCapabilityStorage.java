package teamroots.embers.heat;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class HeatCapabilityStorage implements IStorage<IHeatCapability> {

	@Override
	public NBTBase writeNBT(Capability<IHeatCapability> capability, IHeatCapability instance, Direction side) {
		return null;
	}

	@Override
	public void readNBT(Capability<IHeatCapability> capability, IHeatCapability instance, Direction side,
			NBTBase nbt) {
		
	}

}
