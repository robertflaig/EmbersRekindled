package teamroots.embers.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import teamroots.embers.tileentity.TileEntityDawnstoneAnvil;

import java.util.Arrays;

import AxisAlignedBB;

public class BlockDawnstoneAnvil extends BlockTEBase {
	public static AxisAlignedBB AABB_Z = new AxisAlignedBB(0, 0, 0.25, 1, 1, 0.75);
	public static AxisAlignedBB AABB_X = new AxisAlignedBB(0.25, 0, 0, 0.75, 1, 1);

	public static final PropertyDirection facing = PropertyDirection.create("facing", Arrays.asList(Direction.HORIZONTALS));

	public BlockDawnstoneAnvil(Material material, String name, boolean addToTab) {
		super(material, name, addToTab);
	}

    @Override
	public AxisAlignedBB getBoundingBox(BlockState state, IBlockAccess worldIn, BlockPos pos)
    {
		Direction facing = state.getValue(BlockDawnstoneAnvil.facing);
		if (facing.getAxis() == Direction.Axis.Z){
        	return AABB_X;
        }
        return AABB_Z;
    }
	
	@Override
	public BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, facing);
	}
	
	@Override
	public int getMetaFromState(IBlockState state){
		return state.getValue(facing).getHorizontalIndex();
	}
	
	@Override
	public boolean canPlaceBlockOnSide(World world, BlockPos pos, Direction side){
		return side == Direction.UP || side == Direction.DOWN;
	}
	
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, Direction face, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer){
		IBlockState state = getDefaultState();
		state = state.withProperty(facing, placer.getHorizontalFacing());
		return state;
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta){
		return getDefaultState().withProperty(facing, Direction.getHorizontal(meta));
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityDawnstoneAnvil();
	}
}
