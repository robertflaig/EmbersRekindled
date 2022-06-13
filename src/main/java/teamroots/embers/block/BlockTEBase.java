package teamroots.embers.block;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import teamroots.embers.tileentity.ITileEntityBase;

public class BlockTEBase extends BlockBase {
	public BlockTEBase(Material material, String name, boolean addToTab) {
		super(material, name, addToTab);
	}

	public BlockTEBase(Material material) {
		super(material);
	}

	@Override
	public TileEntity createTileEntity(World worldIn, int meta) {
		return null;
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ){
		return ((ITileEntityBase)world.getTileEntity(pos)).activate(world,pos,state,player,hand,side,hitX,hitY,hitZ);
	}
	
	@Override
	public void onBlockHarvested(World world, BlockPos pos, BlockState state, PlayerEntity player){
		((ITileEntityBase)world.getTileEntity(pos)).onHarvest(world,pos,state,player);
	}
}
