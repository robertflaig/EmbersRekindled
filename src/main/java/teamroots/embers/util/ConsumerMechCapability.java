package teamroots.embers.util;

import mysticalmechanics.api.GearHelper;
import mysticalmechanics.api.IGearBehavior;
import mysticalmechanics.api.IMechCapability;
import mysticalmechanics.tileentity.TileEntityMergebox;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;

public class ConsumerMechCapability implements IMechCapability {
    public double[] power = new double[6];
    public double[] powerExternal = new double[6];
    double maxPower;
    boolean dirty = true;
    boolean additive = false; //Whether power input will be added together or the largest will be chosen

    public void setAdditive(boolean additive) {
        this.additive = additive;
    }

    public void markDirty() {
        dirty = true;
    }

    public double getInternalPower(Direction from) {
        if (from != null && isInput(from))
            return power[from.getIndex()];
        else
            return 0;
    }

    public double getExternalPower(Direction from) {
        if(from != null && isInput(from))
            return powerExternal[from.getIndex()];
        else
            return 0;
    }

    @Override
    public double getPower(Direction Direction) {
        if (Direction == null) {
            if (dirty) {
                recalculateMax();
                dirty = false;
            }
            return maxPower;
        }
        return power[Direction.getIndex()];
    }

    @Override
    public void setPower(double value, Direction Direction) {
        if (Direction == null)
            for (int i = 0; i < 6; i++)
                power[i] = value;
        else {
            double oldPower = power[Direction.getIndex()];
            this.power[Direction.getIndex()] = value;
            if (oldPower != value) {
                dirty = true;
                onPowerChange();
            }
        }
    }

    private void recalculateMax() {
        maxPower = 0;
        for (Direction facing : Direction.VALUES) {
            double power = getPower(facing);
            if (additive)
                maxPower += power;
            else
                maxPower = Math.max(power, maxPower);
        }
    }

    @Override
    public void onPowerChange() {

    }

    @Override
    public boolean isOutput(Direction from) {
        return false;
    }

    @Override
    public void write(CompoundNBT tag) {
        for (Direction facing : Direction.VALUES) {
            int index = facing.getIndex();
            tag.setDouble("mech_power"+ index, power[index]);
            tag.setDouble("mech_power_external"+ index, powerExternal[index]);
        }
    }

    @Override
    public void read(CompoundNBT tag) {
        for (Direction facing : Direction.VALUES) {
            int index = facing.getIndex();
            power[index] = tag.getDouble("mech_power"+ index);
            powerExternal[index] = tag.getDouble("mech_power_external"+ index);
        }
    }
}
