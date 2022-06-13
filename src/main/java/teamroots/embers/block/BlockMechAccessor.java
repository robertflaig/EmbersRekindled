package teamroots.embers.block;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import teamroots.embers.item.ItemTinkerHammer;
import teamroots.embers.tileentity.TileEntityMechAccessor;

public class BlockMechAccessor extends BlockTEBase implements ITileEntityProvider {
	public static final PropertyDirection facing = PropertyDirection.create("facing");
	
	public BlockMechAccessor(Material material, String name, boolean addToTab) {
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
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityMechAccessor();
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, Hand hand) {
		if(placer != null && placer.isSneaking())
			facing = facing.getOpposite();
		return getDefaultState().withProperty(BlockMechAccessor.facing, facing);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, BlockState state, PlayerEntity playerIn, Hand hand, Direction facing, float hitX, float hitY, float hitZ) {
		ItemStack heldItem = playerIn.getHeldItem(hand);
		if (heldItem.getItem() instanceof ItemTinkerHammer && !playerIn.isSneaking() && state.getBlock() == this){
			worldIn.setBlockState(pos,state.cycleProperty(BlockMechAccessor.facing));
			return true;
		}
		return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
	}
	
	@Override
	public boolean isSideSolid(BlockState state, IBlockAccess world, BlockPos pos, Direction side){
		return side != state.getValue(facing);
	}
}
