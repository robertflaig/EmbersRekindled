package teamroots.embers.heat;

import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class HeatCapabilityProvider implements ICapabilityProvider {
	private IHeatCapability capability = null;
	
	public HeatCapabilityProvider(){
		capability = new DefaultHeatCapability();
	}
	
	public HeatCapabilityProvider(IHeatCapability capability){
		this.capability = capability;
	}
	
	@CapabilityInject(IHeatCapability.class)
	public static final Capability<IHeatCapability> heatCapability = null;
	
	@Override
	public boolean hasCapability(Capability<?> capability, Direction facing) {
		return capability == heatCapability;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, Direction facing) {
    	if (heatCapability != null && capability == heatCapability) return (T)capability;
    	return null;
	}
}
