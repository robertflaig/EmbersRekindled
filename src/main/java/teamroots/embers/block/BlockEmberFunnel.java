package teamroots.embers.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import teamroots.embers.tileentity.ITileEntityBase;
import teamroots.embers.tileentity.TileEntityEmberFunnel;

import javax.annotation.Nullable;

import PropertyDirection;

public class BlockEmberFunnel extends BlockTEBase {
    public static final PropertyDirection facing = PropertyDirection.create("facing");

    public BlockEmberFunnel(Material material, String name, boolean addToTab) {
        super(material, name, addToTab);
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ){
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof ITileEntityBase) {
            return ((ITileEntityBase) tileEntity).activate(world, pos, state, player, hand, side, hitX, hitY, hitZ);
        }
        return false;
    }

    @Override
    public void onBlockHarvested(World world, BlockPos pos, BlockState state, PlayerEntity player){
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof ITileEntityBase) {
            ((ITileEntityBase) tileEntity).onHarvest(world,pos,state,player);
        }
    }

    @Override
    public BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(facing).getIndex();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(facing, Direction.getFront(meta));
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, Direction face, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return getDefaultState().withProperty(facing, face);
    }

    @Override
    public boolean isSideSolid(BlockState state, IBlockAccess world, BlockPos pos, Direction side) {
        return false;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityEmberFunnel();
    }

}
