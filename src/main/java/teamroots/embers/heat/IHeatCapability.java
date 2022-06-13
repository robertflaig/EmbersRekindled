package teamroots.embers.heat;

import net.minecraft.nbt.CompoundNBT;

public interface IHeatCapability {
	public double getHeat();
	public double getHeatCapacity();
	public void setHeat(double value);
	public void setHeatCapacity(double value);
	public double addAmount(double value, boolean doAdd);
	public double removeAmount(double value, boolean doRemove);
	public void write(CompoundNBT tag);
	public void read(CompoundNBT tag);
}
