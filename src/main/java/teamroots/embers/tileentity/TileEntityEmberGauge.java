package teamroots.embers.tileentity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import teamroots.embers.api.capabilities.EmbersCapabilities;
import teamroots.embers.api.power.IEmberCapability;
import teamroots.embers.api.tile.IExtraDialInformation;
import teamroots.embers.block.BlockEmberGauge;

public class TileEntityEmberGauge extends TileEntityBaseGauge {
    @Override
    public int calculateComparatorValue(TileEntity tileEntity, Direction facing) {
        int comparatorValue = 0;
        if(tileEntity.hasCapability(EmbersCapabilities.EMBER_CAPABILITY,facing)) {
            IEmberCapability capability = tileEntity.getCapability(EmbersCapabilities.EMBER_CAPABILITY,facing);
            double fill = capability.getEmber() / capability.getEmberCapacity();
            comparatorValue = fill > 0 ? (int) (1 + fill * 14) : 0;
        }
        if(tileEntity instanceof IExtraDialInformation)
            comparatorValue = ((IExtraDialInformation) tileEntity).getComparatorData(facing,comparatorValue,getDialType());
        return comparatorValue;
    }

    @Override
    public String getDialType() {
        return BlockEmberGauge.DIAL_TYPE;
    }
}
