package teamroots.embers.item;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import teamroots.embers.Embers;
import teamroots.embers.RegistryManager;
import teamroots.embers.api.alchemy.AspectList;

import java.text.DecimalFormat;
import java.util.List;

public class ItemAlchemicWaste extends ItemBase {
	public static final String TAG_IRON = "ironInaccuracy";
	public static final String TAG_DAWNSTONE = "dawnstoneInaccuracy";
	public static final String TAG_COPPER = "copperInaccuracy";
	public static final String TAG_SILVER = "silverInaccuracy";
	public static final String TAG_LEAD = "leadInaccuracy";

	public static final String TAG_INACCURACY = "inaccuracy";
	public static final String TAG_TOTAL_ASH = "totalAsh";

	public ItemAlchemicWaste() {
		super("alchemic_waste", true);
		this.setMaxStackSize(1);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced){
		if (stack.hasTagCompound()){
			AspectList inaccuracies;
			CompoundNBT tagCompound = stack.getTagCompound();
			if (tagCompound.contains(TAG_INACCURACY)) {
				inaccuracies = new AspectList();
				inaccuracies.deserializeNBT(tagCompound.getCompoundTag(TAG_INACCURACY));
			} else { //Legacy handling
				inaccuracies = AspectList.createStandard(
						(int)tagCompound.getDouble(TAG_IRON),
						(int)tagCompound.getDouble(TAG_DAWNSTONE),
						(int)tagCompound.getDouble(TAG_COPPER),
						(int)tagCompound.getDouble(TAG_SILVER),
						(int)tagCompound.getDouble(TAG_LEAD)
				);
			}
			DecimalFormat inaccuracyFormat = Embers.proxy.getDecimalFormat("embers.decimal_format.inaccuracy");
			//For the sake of all that is holy do not replace with collect call
			for (String aspect : inaccuracies.getAspects()) {
				tooltip.add(I18n.format("embers.tooltip.accuracy",I18n.format("embers.aspect."+aspect),inaccuracyFormat.format(inaccuracies.getAspect(aspect))));
			}
		}
	}

	@Deprecated
	public static ItemStack create(int ironInaccuracy, int copperInaccuracy, int silverInaccuracy, int dawnstoneInaccuracy, int leadInaccuracy, int totalAsh) {
		return create(AspectList.createStandard(ironInaccuracy,dawnstoneInaccuracy,copperInaccuracy,silverInaccuracy,leadInaccuracy));
	}

	public static ItemStack create(AspectList inaccuracies) {
		ItemStack stack = new ItemStack(RegistryManager.alchemic_waste,1);
		CompoundNBT tagCompound = new CompoundNBT();
		tagcompound.put(TAG_INACCURACY,inaccuracies.serializeNBT());
		tagCompound.setInteger(TAG_TOTAL_ASH,inaccuracies.getTotal());
		stack.setTagCompound(tagCompound);
		return stack;
	}
}
