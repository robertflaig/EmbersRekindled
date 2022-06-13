package teamroots.embers.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import teamroots.embers.tileentity.TileEntityReceiver;

import PropertyDirection;

public class BlockEmberReceiver extends BlockTEBase {
	public static final PropertyDirection facing = PropertyDirection.create("facing");
	
	public BlockEmberReceiver(Material material, String name, boolean addToTab) {
		super(material, name, addToTab);
	}
	
	@Override
	public BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, facing);
	}
	
	@Override
	public int getMetaFromState(IBlockState state){
		return state.getValue(facing).getIndex();
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta){
		return getDefaultState().withProperty(facing,Direction.getFront(meta));
	}
	
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, Direction face, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer){
		return getDefaultState().withProperty(facing, face);
	}
	
	@Override
	public boolean isSideSolid(BlockState state, IBlockAccess world, BlockPos pos, Direction side){
		return false; //side != state.getValue(facing);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityReceiver();
	}
	
	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos){
		if (world.isAirBlock(pos.offset(state.getValue(facing),-1))){
			world.setBlockToAir(pos);
			this.dropBlockAsItem(world, pos, state, 0);
		}
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos){
		switch (state.getValue(facing)){
		case UP:
			return new AxisAlignedBB(0.25,0,0.25,0.75,0.5,0.75);
		case DOWN:
			return new AxisAlignedBB(0.25,0.5,0.25,0.75,1.0,0.75);
		case NORTH:
			return new AxisAlignedBB(0.25,0.25,0.5,0.75,0.75,1.0);
		case SOUTH:
			return new AxisAlignedBB(0.25,0.25,0,0.75,0.75,0.5);
		case WEST:
			return new AxisAlignedBB(0.5,0.25,0.25,1.0,0.75,0.75);
		case EAST:
			return new AxisAlignedBB(0.0,0.25,0.25,0.5,0.75,0.75);
		}
		return new AxisAlignedBB(0.25,0,0.25,0.75,0.5,0.75);
	}
	
	@Override
	public void onBlockHarvested(World world, BlockPos pos, BlockState state, PlayerEntity player){
		//super.onBlockHarvested(world, pos, state, player);
	}
}
