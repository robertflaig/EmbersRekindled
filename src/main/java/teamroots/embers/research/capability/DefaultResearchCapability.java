package teamroots.embers.research.capability;

import net.minecraft.nbt.CompoundNBT;

import java.util.HashMap;
import java.util.Map;

public class DefaultResearchCapability implements IResearchCapability {
    Map<String,Boolean> Checkmarks = new HashMap<>();

    @Override
    public void setCheckmark(String research, boolean checked) {
        Checkmarks.put(research,checked);
    }

    @Override
    public boolean isChecked(String research) {
        return Checkmarks.getOrDefault(research,false);
    }

    @Override
    public Map<String, Boolean> getCheckmarks() {
        return Checkmarks;
    }

    @Override
    public void write(CompoundNBT tag) {
        CompoundNBT checkmarks = new CompoundNBT();
        for (Map.Entry<String,Boolean> entry : Checkmarks.entrySet()) {
            checkmarks.setBoolean(entry.getKey(),entry.getValue());
        }
        tag.setTag("checkmarks",checkmarks);
    }

    @Override
    public void read(CompoundNBT tag) {
        CompoundNBT checkmarks = tag.getCompoundTag("checkmarks");
        Checkmarks.clear();
        for (String key : checkmarks.keySet()) {
            Checkmarks.put(key,checkmarks.getBoolean(key));
        }
    }
}
