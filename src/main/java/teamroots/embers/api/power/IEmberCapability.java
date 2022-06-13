package teamroots.embers.api.power;

import net.minecraft.nbt.CompoundNBT;

public interface IEmberCapability {
	double getEmber();
	double getEmberCapacity();
	void setEmber(double value);
	void setEmberCapacity(double value);
	double addAmount(double value, boolean doAdd);
	double removeAmount(double value, boolean doRemove);
	void write(CompoundNBT tag);
	void read(CompoundNBT tag);
	void onContentsChanged();
	default boolean acceptsVolatile() {
		return false;
	}
}
