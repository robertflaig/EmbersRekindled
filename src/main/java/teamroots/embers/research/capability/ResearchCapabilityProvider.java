package teamroots.embers.research.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ResearchCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundNBT> {
    private IResearchCapability capability = null;

    public ResearchCapabilityProvider(){
        capability = new DefaultResearchCapability();
    }

    public ResearchCapabilityProvider(IResearchCapability capability){
        this.capability = capability;
    }

    @CapabilityInject(IResearchCapability.class)
    public static final Capability<IResearchCapability> researchCapability = null;

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable Direction facing) {
        return capability == researchCapability;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        if (researchCapability != null && capability == researchCapability)
            return researchCapability.cast(this.capability);
        return null;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT compound = new CompoundNBT();
        capability.write(compound);
        return compound;
    }

    @Override
    public void deserializeNBT(CompoundNBT compound) {
        capability.read(compound);
    }
}
