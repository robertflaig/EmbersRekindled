package teamroots.embers.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import teamroots.embers.tileentity.TileEntityMiniBoiler;
import teamroots.embers.util.Misc;

import PropertyDirection;

public class BlockMiniBoiler extends BlockTEBase {
    public static final PropertyDirection facing = PropertyDirection.create("facing", Direction.Plane.HORIZONTAL);

    public BlockMiniBoiler(Material material, String name, boolean addToTab) {
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
        return getDefaultState().withProperty(BlockMiniBoiler.facing, facing);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityMiniBoiler();
    }
}

