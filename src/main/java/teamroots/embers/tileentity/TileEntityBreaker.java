package teamroots.embers.tileentity;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.BlockPistonMoving;
import net.minecraft.block.BlockStructure;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.common.FMLCommonHandler;
import teamroots.embers.EventManager;
import teamroots.embers.api.tile.IBin;
import teamroots.embers.block.BlockBreaker;
import teamroots.embers.util.Misc;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Random;
import java.util.UUID;

public class TileEntityBreaker extends TileEntity implements ITileEntityBase, ITickableTileEntity {
	int ticksExisted = 0;
	Random random = new Random();
	WeakReference<FakePlayer> fakePlayer;
	
	public TileEntityBreaker(){
		super();
	}
	
	@Override
	public CompoundNBT write(CompoundNBT tag){
		super.write(tag);
		return tag;
	}
	
	@Override
	public void read(CompoundNBT tag){
		super.read(tag);
	}

	@Override
	public CompoundNBT getUpdateTag() {
		return write(new CompoundNBT());
	}

	@Nullable
	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(getPos(), 0, getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		read(pkt.getNbtCompound());
	}

	@Override
	public void onLoad() {
		if(!world.isRemote)
			initFakePlayer();
	}

	protected void initFakePlayer() {
		FakePlayer player = FakePlayerFactory.get((WorldServer) world, new GameProfile(new UUID(13, 13), "embers_breaker"));
		player.connection = new NetHandlerPlayServer(FMLCommonHandler.instance().getMinecraftServerInstance(), new NetworkManager(EnumPacketDirection.SERVERBOUND), player) {
			@Override
			public void sendPacket(Packet packetIn)
			{

			}
		};
		fakePlayer = new WeakReference<>(player);
	}

	@Override
	public boolean activate(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand,
			Direction side, float hitX, float hitY, float hitZ) {
		return false;
	}

	@Override
	public void onHarvest(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		this.remove();
		world.setTileEntity(pos, null);
	}

	@Override
	public void tick() {
		ticksExisted ++;
		IBlockState state = world.getBlockState(pos);
		if (ticksExisted % 20 == 0 && isActive() && state.getBlock() instanceof BlockBreaker && !world.isRemote){
			Direction facing = getFacing();
			mineBlock(pos.offset(facing));
		}
	}

	public boolean isActive() {
		return !getWorld().isBlockPowered(getPos());
	}

	protected void mineBlock(BlockPos breakPos) {

		FakePlayer player = fakePlayer.get();
		if(player == null)
			return;
		//player.interactionManager.tryHarvestBlock(breakPos);
		int exp = net.minecraftforge.common.ForgeHooks.onBlockBreakEvent(world, player.interactionManager.getGameType(), player, breakPos);
		if (exp != -1) {
			IBlockState state = world.getBlockState(breakPos);
			TileEntity tile = this.world.getTileEntity(breakPos);
			Block block = state.getBlock();

			NonNullList<ItemStack> drops = null;

			if (state.getBlockHardness(world,pos) < 0 || (isBlacklisted(block) && !player.canUseCommandBlock()))
			{
				world.notifyBlockUpdate(breakPos, state, state, 3);
			}
			else
			{
				world.playEvent(player, 2001, breakPos, Block.getStateId(state));
				boolean flag1;

				if (player.isCreative())
				{
					removeBlock(player,breakPos,false);
				}
				else
				{
					flag1 = removeBlock(player, breakPos, true);
					if (flag1) {
						EventManager.captureDrops(true);
						state.getBlock().harvestBlock(world, player, breakPos, state, tile, ItemStack.EMPTY);
						drops = EventManager.captureDrops(false);
					}
				}
			}

			if(drops != null)
				collectDrops(drops);
		}
	}

	private boolean isBlacklisted(Block block) {
		return block instanceof BlockCommandBlock || block instanceof BlockStructure;
	}

	public Direction getFacing() {
		IBlockState state = world.getBlockState(pos);
		return state.getValue(BlockBreaker.facing);
	}

	private void collectDrops(NonNullList<ItemStack> stacks) {
		Direction facing = getFacing();
		BlockPos frontPos = getPos().offset(facing);
		BlockPos binPos = getPos().offset(facing.getOpposite());
		TileEntity bin = getWorld().getTileEntity(binPos);
		boolean capture = bin instanceof IBin;
		for (ItemStack stack : stacks){
			if (capture){
				ItemStack remainder = ((IBin)bin).getInventory().insertItem(0, stack, false);
				if (!remainder.isEmpty() && !getWorld().isRemote){
					EntityItem item = new EntityItem(getWorld(), frontPos.getX()+0.5, frontPos.getY()+1.0625f, frontPos.getZ()+0.5,remainder);
					getWorld().spawnEntity(item);
				}
				bin.markDirty();
				markDirty();
			}
			else {
				EntityItem item = new EntityItem(getWorld(), frontPos.getX()+0.5, frontPos.getY()+1.0625f, frontPos.getZ()+0.5,stack);
				getWorld().spawnEntity(item);
			}
		}
	}

	private boolean removeBlock(FakePlayer player, BlockPos pos, boolean canHarvest) {
		IBlockState state = this.world.getBlockState(pos);
		boolean flag = state.getBlock().removedByPlayer(state, world, pos, player, canHarvest);
		if (flag) {
			state.getBlock().onBlockDestroyedByPlayer(this.world, pos, state);
		}
		return flag;
	}

	@Override
	public void markDirty() {
		super.markDirty();
		Misc.syncTE(this);
	}
}
