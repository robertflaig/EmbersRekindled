package teamroots.embers.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import teamroots.embers.Embers;
import teamroots.embers.api.capabilities.EmbersCapabilities;
import teamroots.embers.api.power.IEmberCapability;
import teamroots.embers.tileentity.TileEntityEmberGauge;
import teamroots.embers.util.DecimalFormats;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class BlockEmberGauge extends BlockBaseGauge {
	public static final String DIAL_TYPE = "ember";

	public BlockEmberGauge(Material material, String name, boolean addToTab) {
		super(material, name, addToTab); 
	}

	@Override
	protected void getTEData(Direction facing, ArrayList<String> text, TileEntity tileEntity) {
		if (tileEntity.hasCapability(EmbersCapabilities.EMBER_CAPABILITY, facing)){
			IEmberCapability handler = tileEntity.getCapability(EmbersCapabilities.EMBER_CAPABILITY, facing);
			if (handler != null){
				text.add(formatEmber(handler.getEmber(), handler.getEmberCapacity()));
			}
		}
	}

	public static String formatEmber(double ember, double emberCapacity) {
		DecimalFormat emberFormat = Embers.proxy.getDecimalFormat("embers.decimal_format.ember");
		return I18n.format("embers.tooltip.emberdial.ember", emberFormat.format(ember), emberFormat.format(emberCapacity));
	}

	@Override
	public String getDialType() {
		return DIAL_TYPE;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityEmberGauge();
	}
}
