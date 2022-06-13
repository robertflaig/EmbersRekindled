package teamroots.embers.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import teamroots.embers.tileentity.TileEntityDropper;

import AxisAlignedBB;

public class BlockDropper extends BlockTEBase {
	public static AxisAlignedBB AABB_BASE = new AxisAlignedBB(0.25,0.625,0.25,0.75,1.0,0.75);
	
	public BlockDropper(Material material, String name, boolean addToTab) {
		super(material, name, addToTab);
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos){
		return AABB_BASE;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityDropper();
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, BlockState state, BlockPos pos, Direction face) {
		return BlockFaceShape.UNDEFINED;
	}
}
