package teamroots.embers.power;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;
import teamroots.embers.Embers;

public class DefaultEmberCapability implements IEmberCapability {
	public static boolean allAcceptVolatile = false;

	private double ember = 0;
	private double capacity = 0;
	@Override
	public double getEmber() {
		return ember;
	}

	@Override
	public double getEmberCapacity() {
		return capacity;
	}

	@Override
	public void setEmber(double value) {
		ember = value;
	}

	@Override
	public void setEmberCapacity(double value) {
		capacity = value;
	}

	@Override
	public double addAmount(double value, boolean doAdd) {
		double added = Math.min(capacity - ember,value);
		double newEmber = ember + added;
		if (doAdd){
			if(newEmber != ember)
				onContentsChanged();
			ember += added;
		}
		return added;
	}

	@Override
	public double removeAmount(double value, boolean doRemove) {
		double removed = Math.min(ember,value);
		double newEmber = ember - removed;
		if (doRemove){
			if(newEmber != ember)
				onContentsChanged();
			ember -= removed;
		}
		return removed;
	}

	@Override
	public void write(CompoundNBT tag) {
		tag.setDouble(Embers.MODID+":ember", ember);
		tag.setDouble(Embers.MODID+":emberCapacity", capacity);
	}

	@Override
	public void read(CompoundNBT tag) {
		if (tag.contains(Embers.MODID+":ember")){
			ember = tag.getDouble(Embers.MODID+":ember");
		}
		if (tag.contains(Embers.MODID+":emberCapacity")){
			capacity = tag.getDouble(Embers.MODID+":emberCapacity");
		}
	}

	@Override
	public void onContentsChanged() {

	}

	@Override
	public boolean acceptsVolatile() {
		return allAcceptVolatile;
	}
}
