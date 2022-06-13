package thaumcraft.api.casters;

import java.util.ArrayList;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTTagList;

public abstract class FocusModSplit extends FocusMod {
	
	private ArrayList<FocusPackage> packages = new ArrayList<>(); 

	public final ArrayList<FocusPackage> getSplitPackages() {
		return packages;
	}
	
	public void deserialize(CompoundNBT nbt) {
		NBTTagList nodelist = nbt.getTagList("packages", (byte)10);
		packages.clear();
		for (int x=0;x<nodelist.tagCount();x++) {
			CompoundNBT nodenbt = (CompoundNBT) nodelist.getCompoundTagAt(x);
			FocusPackage fp = new FocusPackage();
			fp.deserialize(nodenbt);
			packages.add(fp);
		}
	}
	
	public CompoundNBT serialize() {
		CompoundNBT nbt = new CompoundNBT();
		NBTTagList nodelist = new NBTTagList();
		for (FocusPackage node:packages) {
			nodelist.appendTag(node.serialize());
		}
		nbt.setTag("packages", nodelist);		
		return nbt;
	}
	
	@Override
	public float getPowerMultiplier() {
		return .75f;
	}

}
