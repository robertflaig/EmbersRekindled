package teamroots.embers.research.capability;

import net.minecraft.nbt.CompoundNBT;

import java.util.Map;

public interface IResearchCapability {
    void setCheckmark(String research, boolean checked);
    boolean isChecked(String research);
    Map<String,Boolean> getCheckmarks();
    void write(CompoundNBT tag);
    void read(CompoundNBT tag);
}
