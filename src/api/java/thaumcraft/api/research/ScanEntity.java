package thaumcraft.api.research;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import thaumcraft.api.ThaumcraftApi.EntityTagsNBT;
import thaumcraft.api.ThaumcraftApiHelper;

public class ScanEntity implements IScanThing {
	
	String research;	
	Class entityClass;
	EntityTagsNBT[] NBTData;
	
	/**
	 * false if the specific entity class should be used, or true if anything the inherits from that class is also allowed. 
	 */
	boolean inheritedClasses=false;

	public ScanEntity(String research, Class entityClass, boolean inheritedClasses) {
		this.research = research;
		this.entityClass = entityClass;
		this.inheritedClasses = inheritedClasses;
	}
	
	public ScanEntity(String research, Class entityClass, boolean inheritedClasses, EntityTagsNBT... nbt) {
		this.research = research;
		this.entityClass = entityClass;
		this.inheritedClasses = inheritedClasses;
		this.NBTData = nbt;
	}

	@Override
	public boolean checkThing(PlayerEntity player, Object obj) {		
		if (obj!=null && ((!inheritedClasses && entityClass==obj.getClass()) || 
				(inheritedClasses && entityClass.isInstance(obj)))) {			
			if (NBTData!=null && NBTData.length>0) {
				boolean b = true;
				CompoundNBT tc = new CompoundNBT();
				((Entity)obj).write(tc);
				for (EntityTagsNBT nbt:NBTData) {
					if (!tc.contains(nbt.name) || !ThaumcraftApiHelper.getNBTDataFromId(tc, tc.getTagId(nbt.name), nbt.name).equals(nbt.value)) {
						return false;
					} 					
				} 			
			} 			
			return true;
		}
		return false;
	}

	@Override
	public String getResearchKey(PlayerEntity player, Object object) {
		return research;
	}
	
}
