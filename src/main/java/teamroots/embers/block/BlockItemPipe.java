package teamroots.embers.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import teamroots.embers.tileentity.TileEntityItemPipe;
import teamroots.embers.util.EnumPipeConnection;
import teamroots.embers.util.Misc;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class BlockItemPipe extends BlockTEBase {

    public BlockItemPipe(Material material, String name, boolean addToTab) {
        super(material, name, addToTab);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityItemPipe();
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        TileEntityItemPipe p = (TileEntityItemPipe) world.getTileEntity(pos);
        p.updateNeighbors(world);
        p.markDirty();
    }

    @Override
    public boolean isSideSolid(BlockState state, IBlockAccess world, BlockPos pos, Direction side) {
        if (world.getTileEntity(pos) instanceof TileEntityItemPipe) {
            return ((TileEntityItemPipe) world.getTileEntity(pos)).getInternalConnection(side) == EnumPipeConnection.NONE;
        }
        return true;
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        if (world.getTileEntity(pos) instanceof TileEntityItemPipe) {
            ((TileEntityItemPipe) world.getTileEntity(pos)).updateNeighbors(world);
            world.getTileEntity(pos).markDirty();
        }
    }

    @Override
    public RayTraceResult collisionRayTrace(BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull Vec3d start, @Nonnull Vec3d end) {
        List<AxisAlignedBB> subBoxes = new ArrayList<>();

        subBoxes.add(new AxisAlignedBB(0.375, 0.375, 0.375, 0.625, 0.625, 0.625));

        if (world.getTileEntity(pos) instanceof TileEntityItemPipe) {
            TileEntityItemPipe pipe = ((TileEntityItemPipe) world.getTileEntity(pos));

            if (pipe.getInternalConnection(Direction.UP) != EnumPipeConnection.NONE)
                subBoxes.add(new AxisAlignedBB(0.375, 0.625, 0.375, 0.625, 1.0, 0.625));
            if (pipe.getInternalConnection(Direction.DOWN) != EnumPipeConnection.NONE)
                subBoxes.add(new AxisAlignedBB(0.375, 0.0, 0.375, 0.625, 0.375, 0.625));
            if (pipe.getInternalConnection(Direction.NORTH) != EnumPipeConnection.NONE)
                subBoxes.add(new AxisAlignedBB(0.375, 0.375, 0.0, 0.625, 0.625, 0.375));
            if (pipe.getInternalConnection(Direction.SOUTH) != EnumPipeConnection.NONE)
                subBoxes.add(new AxisAlignedBB(0.375, 0.375, 0.625, 0.625, 0.625, 1.0));
            if (pipe.getInternalConnection(Direction.WEST) != EnumPipeConnection.NONE)
                subBoxes.add(new AxisAlignedBB(0.0, 0.375, 0.375, 0.375, 0.625, 0.625));
            if (pipe.getInternalConnection(Direction.EAST) != EnumPipeConnection.NONE)
                subBoxes.add(new AxisAlignedBB(0.625, 0.375, 0.375, 1.0, 0.625, 0.625));
        }

        return Misc.raytraceMultiAABB(subBoxes, pos, start, end);
    }

    @Override
    public AxisAlignedBB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos) {
        double x1 = 0.375;
        double y1 = 0.375;
        double z1 = 0.375;
        double x2 = 0.625;
        double y2 = 0.625;
        double z2 = 0.625;

        if (source.getTileEntity(pos) instanceof TileEntityItemPipe) {
            TileEntityItemPipe pipe = ((TileEntityItemPipe) source.getTileEntity(pos));
            if (pipe.getInternalConnection(Direction.UP) != EnumPipeConnection.NONE) {
                y2 = 1;
            }
            if (pipe.getInternalConnection(Direction.DOWN) != EnumPipeConnection.NONE) {
                y1 = 0;
            }
            if (pipe.getInternalConnection(Direction.NORTH) != EnumPipeConnection.NONE) {
                z1 = 0;
            }
            if (pipe.getInternalConnection(Direction.SOUTH) != EnumPipeConnection.NONE) {
                z2 = 1;
            }
            if (pipe.getInternalConnection(Direction.WEST) != EnumPipeConnection.NONE) {
                x1 = 0;
            }
            if (pipe.getInternalConnection(Direction.EAST) != EnumPipeConnection.NONE) {
                x2 = 1;
            }
        }

        return new AxisAlignedBB(x1, y1, z1, x2, y2, z2);
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, BlockState state, BlockPos pos, Direction face) {
        if (worldIn.getTileEntity(pos) instanceof TileEntityItemPipe) {
            TileEntityItemPipe pipe = ((TileEntityItemPipe) worldIn.getTileEntity(pos));
            if (pipe.getInternalConnection(face) != EnumPipeConnection.NONE) {
                return BlockFaceShape.MIDDLE_POLE;
            }
        }
        return BlockFaceShape.UNDEFINED;
    }
}
