package teamroots.embers.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import teamroots.embers.RegistryManager;
import teamroots.embers.tileentity.TileEntityHeatCoil;

public class BlockHeatCoil extends BlockTEBase {
	
	public BlockHeatCoil(Material material, String name, boolean addToTab) {
		super(material, name, addToTab);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityHeatCoil();
	}
	
	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos){
		if (world.isAirBlock(pos.east())
			&& world.isAirBlock(pos.west())
			&& world.isAirBlock(pos.north())
			&& world.isAirBlock(pos.south())
			&& world.isAirBlock(pos.east().north())
			&& world.isAirBlock(pos.east().south())
			&& world.isAirBlock(pos.west().north())
			&& world.isAirBlock(pos.west().south())){
			return true;
		}
		return false;
	}
	
	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state){
		world.setBlockState(pos.north(), RegistryManager.mech_edge.getStateFromMeta(0));
		world.setBlockState(pos.north().west(), RegistryManager.mech_edge.getStateFromMeta(1));
		world.setBlockState(pos.west(), RegistryManager.mech_edge.getStateFromMeta(2));
		world.setBlockState(pos.south().west(), RegistryManager.mech_edge.getStateFromMeta(3));
		world.setBlockState(pos.south(), RegistryManager.mech_edge.getStateFromMeta(4));
		world.setBlockState(pos.south().east(), RegistryManager.mech_edge.getStateFromMeta(5));
		world.setBlockState(pos.east(), RegistryManager.mech_edge.getStateFromMeta(6));
		world.setBlockState(pos.north().east(), RegistryManager.mech_edge.getStateFromMeta(7));
	}
}
