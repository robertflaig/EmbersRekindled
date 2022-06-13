package teamroots.embers.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
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
import teamroots.embers.tileentity.TileEntityActivatorBottom;
import teamroots.embers.tileentity.TileEntityActivatorTop;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import AxisAlignedBB;

public class BlockActivator extends BlockTEBase {
	public static AxisAlignedBB AABB_BASE = new AxisAlignedBB(0,0,0,1,0.25,1);
	public static AxisAlignedBB AABB_SIDE_WEST = new AxisAlignedBB(0,0,0,0.25,1.0,1.0);
	public static AxisAlignedBB AABB_SIDE_EAST = new AxisAlignedBB(0.75,0,0,1.0,1.0,1.0);
	public static AxisAlignedBB AABB_SIDE_NORTH = new AxisAlignedBB(0,0,0,1.0,1.0,0.25);
	public static AxisAlignedBB AABB_SIDE_SOUTH = new AxisAlignedBB(0,0,0.75,1.0,1.0,1.0);
	public static final PropertyBool isTop = PropertyBool.create("top");

	public BlockActivator(Material material, String name, boolean addToTab) {
		super(material, name, addToTab);
	}

    @Override
	public void addCollisionBoxToList(BlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean b)
    {
    	if (state.getValue(isTop)){
	        addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_SIDE_WEST);
	        addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_SIDE_NORTH);
	        addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_SIDE_EAST);
	        addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_SIDE_SOUTH);
    	}
    	else {
    		super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn,b);
    	}
    }
	
	@Override
	public BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, isTop);
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
		return new ArrayList<ItemStack>();
	}
	
	@Override
	public void onBlockHarvested(World world, BlockPos pos, BlockState state, PlayerEntity player){
		if (state.getValue(isTop) && world.getBlockState(pos.down()).getBlock() == this || !state.getValue(isTop) && world.getBlockState(pos.up()).getBlock() == this){
			if (!world.isRemote && !player.capabilities.isCreativeMode){
				world.spawnEntity(new EntityItem(world,pos.getX()+0.5,pos.getY()+0.5,pos.getZ()+0.5,new ItemStack(this,1,0)));
			}
		}
		if (this.getMetaFromState(state) == 0){
			world.setBlockToAir(pos.up());
		}
		else {
			world.setBlockToAir(pos.down());
		}
		((ITileEntityBase)world.getTileEntity(pos)).onHarvest(world,pos,state,player);
	}
	
	@Override
	public void onBlockExploded(World world, BlockPos pos, Explosion explosion){
		if (!world.isRemote){
			world.spawnEntity(new EntityItem(world,pos.getX()+0.5,pos.getY()+0.5,pos.getZ()+0.5,new ItemStack(this,1,0)));
		}
		IBlockState state = world.getBlockState(pos);
		if (this.getMetaFromState(state) == 0){
			world.setBlockToAir(pos.up());
		}
		else {
			world.setBlockToAir(pos.down());
		}
		((ITileEntityBase)world.getTileEntity(pos)).onHarvest(world,pos,state,null);
		world.setBlockToAir(pos);
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
			return new TileEntityActivatorTop();
		}
		return new TileEntityActivatorBottom();
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ){
		return ((ITileEntityBase)world.getTileEntity(pos)).activate(world,pos,state,player,hand,side,hitX,hitY,hitZ);
	}
}
