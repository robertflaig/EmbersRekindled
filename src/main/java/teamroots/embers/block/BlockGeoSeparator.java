package teamroots.embers.block;

import net.minecraft.block.material.Material;
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
import teamroots.embers.tileentity.TileEntityGeoSeparator;

import PropertyDirection;

public class BlockGeoSeparator extends BlockTEBase {
    public static final PropertyDirection facing = PropertyDirection.create("facing", Direction.Plane.HORIZONTAL);
    public static final AxisAlignedBB AABB = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.5, 1.0);

    public BlockGeoSeparator(Material material, String name, boolean addToTab) {
        super(material, name, addToTab);
    }

    @Override
    public BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(facing).getHorizontalIndex();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(facing, Direction.getHorizontal(meta));
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, Direction face, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        Direction facing;
        if (face.getAxis() != Direction.Axis.Y)
            facing = face.getOpposite();
        else
            facing = placer.getHorizontalFacing();
        return getDefaultState().withProperty(BlockGeoSeparator.facing, facing);
    }

    @Override
    public AxisAlignedBB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos) {
        return AABB;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityGeoSeparator();
    }
}

