package teamroots.embers.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import teamroots.embers.tileentity.ITileEntityBase;
import teamroots.embers.tileentity.TileEntityAlchemyPedestal;
import teamroots.embers.tileentity.TileEntityExplosionPedestal;

import java.util.ArrayList;
import java.util.List;

import AxisAlignedBB;

public class BlockExplosionPedestal extends BlockTEBase {
	public static AxisAlignedBB AABB_FULL = new AxisAlignedBB(0,0,0,1,1,1);
	public static AxisAlignedBB AABB_HALF = new AxisAlignedBB(0,0,0,1,0.5,1);
	public static final PropertyBool isTop = PropertyBool.create("top");

	public BlockExplosionPedestal(Material material, String name, boolean addToTab) {
		super(material, name, addToTab);
	}
	
	@Override
	public BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, isTop);
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos){
		if (getMetaFromState(state) == 1){
			return AABB_HALF;
		}
		return AABB_FULL;
	}
	
	@Override
	public int getMetaFromState(IBlockState state){
		boolean top = state.getValue(isTop);
		return top ? 1 : 0;
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta){
		return getDefaultState().withProperty(isTop,meta == 1 ? true : false);
	}
	
	@Override
	public void onBlockExploded(World world, BlockPos pos, Explosion explosion){
		if (!world.isRemote){
			world.spawnEntity(new EntityItem(world,pos.getX()+0.5,pos.getY()+0.5,pos.getZ()+0.5,new ItemStack(this,1,0)));
		}
		IBlockState state = world.getBlockState(pos);
		if (this.getMetaFromState(state) == 1 && world.getTileEntity(pos) instanceof ITileEntityBase){
			((ITileEntityBase)world.getTileEntity(pos)).onHarvest(world,pos,state,null);
		}
		else if (world.getTileEntity(pos.up()) instanceof ITileEntityBase){
			((ITileEntityBase)world.getTileEntity(pos.up())).onHarvest(world,pos,state,null);
		}
		if (this.getMetaFromState(state) == 0){
			world.setBlockToAir(pos.up());
		}
		else {
			world.setBlockToAir(pos.down());
		}
		world.setBlockToAir(pos);
	}
	
	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state){
		if (this.getMetaFromState(state) == 0){
			world.setBlockState(pos.up(), this.getDefaultState().withProperty(isTop, true));
		}
		else {
			world.setBlockState(pos.down(), this.getStateFromMeta(0));
		}
	}
	
	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, BlockState state, int fortune){
		return new ArrayList<>();
	}
	
	@Override
	public void onBlockHarvested(World world, BlockPos pos, BlockState state, PlayerEntity player){
		if (state.getValue(isTop) && world.getBlockState(pos.down()).getBlock() == this || !state.getValue(isTop) && world.getBlockState(pos.up()).getBlock() == this){
			if (!world.isRemote && !player.capabilities.isCreativeMode){
				world.spawnEntity(new EntityItem(world,pos.getX()+0.5,pos.getY()+0.5,pos.getZ()+0.5,new ItemStack(this,1,0)));
			}
		}
		if (this.getMetaFromState(state) == 1 && world.getTileEntity(pos) instanceof ITileEntityBase){
			((ITileEntityBase)world.getTileEntity(pos)).onHarvest(world,pos,state,player);
		}
		else if (world.getTileEntity(pos.up()) instanceof ITileEntityBase){
			((ITileEntityBase)world.getTileEntity(pos.up())).onHarvest(world,pos,state,player);
		}
		if (this.getMetaFromState(state) == 0){
			world.setBlockToAir(pos.up());
		}
		else {
			world.setBlockToAir(pos.down());
		}
	}
	
	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos){
		if (world.getBlockState(pos.up()) == Blocks.AIR.getDefaultState()){
			return true;
		}
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		if (meta == 1){
			return new TileEntityExplosionPedestal();
		}
		return null;
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ){
		return false;
	}
}
