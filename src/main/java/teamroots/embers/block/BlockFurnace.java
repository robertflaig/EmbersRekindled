package teamroots.embers.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import teamroots.embers.tileentity.ITileEntityBase;
import teamroots.embers.tileentity.TileEntityFurnaceBottom;
import teamroots.embers.tileentity.TileEntityFurnaceTop;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import AxisAlignedBB;

public class BlockFurnace extends BlockTEBase {
	public static AxisAlignedBB AABB_BASE = new AxisAlignedBB(0,0,0,1,0.25,1);
	public static AxisAlignedBB AABB_SIDE_WEST = new AxisAlignedBB(0,0,0,0.25,1.0,1.0);
	public static AxisAlignedBB AABB_SIDE_EAST = new AxisAlignedBB(0.75,0,0,1.0,1.0,1.0);
	public static AxisAlignedBB AABB_SIDE_NORTH = new AxisAlignedBB(0,0,0,1.0,1.0,0.25);
	public static AxisAlignedBB AABB_SIDE_SOUTH = new AxisAlignedBB(0,0,0.75,1.0,1.0,1.0);
	public static final PropertyBool isTop = PropertyBool.create("top");
	
	public BlockFurnace(Material material, String name, boolean addToTab) {
		super(material, name, addToTab);
	}

    @Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean b)
    {
    	if (state.getValue(isTop)){
	        addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_SIDE_WEST);
	        addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_SIDE_NORTH);
	        addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_SIDE_EAST);
	        addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_SIDE_SOUTH);
    	}
    	else {
    		super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, b);
    	}
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
		((ITileEntityBase)world.getTileEntity(pos)).breakBlock(world,pos,state,null);
		world.setBlockToAir(pos);
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
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune){
		return new ArrayList<ItemStack>();
	}
	
	@Override
	public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player){
		if (!world.isRemote && !player.capabilities.isCreativeMode){
			world.spawnEntity(new EntityItem(world,pos.getX()+0.5,pos.getY()+0.5,pos.getZ()+0.5,new ItemStack(this,1,0)));
		}
		if (this.getMetaFromState(state) == 0){
			if (world.getTileEntity(pos.up()) instanceof ITileEntityBase){
				((ITileEntityBase)world.getTileEntity(pos.up())).breakBlock(world, pos.up(), state, player);
			}
			world.setBlockToAir(pos.up());
		}
		else {
			if (world.getTileEntity(pos.down()) instanceof ITileEntityBase){
				((ITileEntityBase)world.getTileEntity(pos.down())).breakBlock(world, pos.down(), state, player);
			}
			world.setBlockToAir(pos.down());
		}
		((ITileEntityBase)world.getTileEntity(pos)).breakBlock(world,pos,state,player);
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
			return new TileEntityFurnaceTop();
		}
		return new TileEntityFurnaceBottom();
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ){
		return ((ITileEntityBase)world.getTileEntity(pos)).activate(world,pos,state,player,hand,side,hitX,hitY,hitZ);
	}
}
