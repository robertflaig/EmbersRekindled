package teamroots.embers.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
//import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.block.AnvilBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.ModelLoader;
import teamroots.embers.Embers;

public class BlockBase extends Block implements IModeledBlock, IBlock {
	AnvilBlock testing = new AnvilBlock(Properties.create(Material.ANVIL));
	public Item itemBlock = null;
	public boolean isOpaqueCube = true, isFullCube = true, isBeaconBase = false;
	public BlockRenderLayer layer = BlockRenderLayer.SOLID;
	public BlockBase(Material material, String name, boolean addToTab){
		super(Block.Properties.create(material));
		//setUnlocalizedName(name);
		setRegistryName(Embers.MODID+":"+name,name);
		if (addToTab){
			BlockItem.BLOCK_TO_ITEM;
			setCreativeTab(Embers.tab);
		}
		itemBlock = (new BlockItem(this).setRegistryName(this.getRegistryName()));
    }

	public BlockBase(Material material){
		super(material);
	}
	
	public BlockBase setIsOpaqueCube(boolean b){
		isOpaqueCube = b;
		return this;
	}
	
	public BlockBase setBeaconBase(boolean b){
		isBeaconBase = b;
		return this;
	}
	
	@Override
	public boolean isBeaconBase(IBlockAccess world, BlockPos pos, BlockPos beacon){
		return isBeaconBase;
	}

	@Override
	public boolean isBeaconBase(){
		return isBeaconBase;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state){
		return isOpaqueCube;
	}
	
	public BlockBase setIsFullCube(boolean b){
		isFullCube = b;
		return this;
	}
	
	@Override
	public boolean isFullCube(IBlockState state){
		return isFullCube;
	}
	
	@Override
	public boolean isFullBlock(IBlockState state){
		return isFullCube;
	}
	
	public BlockBase setHarvestProperties(String toolType, int level){
		super.setHarvestLevel(toolType, level);
		return this;
	}
	
	@Override
	public void initModel(){
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName().toString(),"inventory"));
	}

	@Override
	public Item getItemBlock() {
		return itemBlock;
	}
}
