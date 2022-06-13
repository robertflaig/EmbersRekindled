package teamroots.embers.block;

import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.client.model.ModelLoader;
import teamroots.embers.Embers;

public class BlockStairsBase extends BlockStairs implements IModeledBlock, IBlock {
	public boolean isOpaqueCube = true, isFullCube = true;
	public BlockRenderLayer layer = BlockRenderLayer.SOLID;
	public Item itemBlock = null;
	public BlockStairsBase(BlockState state, String name, boolean addToTab){
		super(state);
		this.useNeighborBrightness = true;
		setUnlocalizedName(name);
		setRegistryName(Embers.MODID+":"+name);
		if (addToTab){
			setCreativeTab(Embers.tab);
		}
		itemBlock = new ItemBlock(this).setRegistryName(this.getRegistryName());
    }
	
	public BlockStairsBase setIsOpaqueCube(boolean b){
		isOpaqueCube = b;
		return this;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state){
		return isOpaqueCube;
	}
	
	public BlockStairsBase setIsFullCube(boolean b){
		isFullCube = b;
		return this;
	}
	
	@Override
	public boolean isFullCube(IBlockState state){
		return isFullCube;
	}
	
	public BlockStairsBase setHarvestProperties(String toolType, int level){
		super.setHarvestLevel(toolType, level);
		return this;
	}
	
	@Override
	public void initModel(){
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName().toString()));
	}

	@Override
	public Item getItemBlock() {
		return itemBlock;
	}
}
