package teamroots.embers.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import teamroots.embers.Embers;

import java.util.ArrayList;

public class BlockDoubleSlabBase extends BlockSlab implements IModeledBlock, IBlock {
	public Item itemBlock = null;
	private Block slab;
	public boolean isOpaqueCube = true, isFullCube = true;
	public static AxisAlignedBB FULL_AABB = new AxisAlignedBB(0,0,0,1,1,1);
	public BlockRenderLayer layer = BlockRenderLayer.SOLID;
	
	public BlockDoubleSlabBase(Material material, String name, boolean addToTab){
		super(material);
		setUnlocalizedName(name);
		setRegistryName(Embers.MODID+":"+name);
		if (addToTab){
			setCreativeTab(Embers.tab);
		}
		itemBlock = (new ItemBlock(this).setRegistryName(this.getRegistryName()));
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list){
		
	}
	
	public BlockDoubleSlabBase setIsOpaqueCube(boolean b){
		isOpaqueCube = b;
		return this;
	}

	public void setSlab(Block slab) {
		this.slab = slab;
	}

	@Override
	public ArrayList<ItemStack> getDrops(IBlockAccess world, BlockPos pos, BlockState state, int fortune) {
		ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
		drops.add(new ItemStack(Item.getItemFromBlock(this.slab), 1));
		drops.add(new ItemStack(Item.getItemFromBlock(this.slab), 1));
		return drops;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state){
		return isOpaqueCube;
	}
	
	public BlockDoubleSlabBase setIsFullCube(boolean b){
		isFullCube = b;
		return this;
	}
	
	@Override
	public boolean isFullCube(IBlockState state){
		return isFullCube;
	}
	
	public BlockDoubleSlabBase setHarvestProperties(String toolType, int level){
		super.setHarvestLevel(toolType, level);
		return this;
	}
	
    @SideOnly(Side.CLIENT)
	protected static boolean isHalfSlab(IBlockState state){
		return true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void initModel(){
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName().toString()));
	}
	
	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, World world, BlockPos pos, PlayerEntity player){
		return new ItemStack(Item.getItemFromBlock(this.slab));
	}

	@Override
	public IBlockState getStateFromMeta(int meta){
		IBlockState iblockstate = this.getDefaultState();
		if (!this.isDouble())
			iblockstate = iblockstate.withProperty(HALF, (meta) == 0 ?
				                                             BlockSlab.EnumBlockHalf.BOTTOM :
				                                             BlockSlab.EnumBlockHalf.TOP);

		return iblockstate;
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(BlockState state, IBlockAccess world, BlockPos pos){
		return FULL_AABB;
	}

	@Override
	public int getMetaFromState(IBlockState state){
		return state.getValue(HALF) == EnumBlockHalf.BOTTOM ? 0 : 1; 
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, HALF);
	}

	@Override
	public boolean isDouble()
	{
		return false;
	}

	@Override
	public String getUnlocalizedName(int meta)
	{
		return null;
	}

	@Override
	public IProperty getVariantProperty()
	{
		return HALF;
	}

	@Override
	public Comparable<?> getTypeForItem(ItemStack stack) {
		return 0;
	}

	@Override
	public Item getItemBlock() {
		return itemBlock;
	}
}