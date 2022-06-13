package teamroots.embers.tileentity;

import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import teamroots.embers.EventManager;
import teamroots.embers.SoundManager;
import teamroots.embers.api.capabilities.EmbersCapabilities;
import teamroots.embers.api.power.IEmberCapability;
import teamroots.embers.api.power.IEmberPacketProducer;
import teamroots.embers.api.power.IEmberPacketReceiver;
import teamroots.embers.entity.EntityEmberPacket;
import teamroots.embers.power.DefaultEmberCapability;
import teamroots.embers.util.Misc;

import javax.annotation.Nullable;
import java.util.Random;

public class TileEntityRelay extends TileEntity implements ITileEntityBase, IEmberPacketProducer, IEmberPacketReceiver, ITickableTileEntity {
	public IEmberCapability capability = new DefaultEmberCapability();
	public BlockPos target = null;
	public long ticksExisted = 0;
	Random random = new Random();
	int offset = random.nextInt(40);
	boolean polled = false;
	public static enum EnumConnection{
		NONE, LEVER
	}
	
	public static EnumConnection connectionFromInt(int value){
		switch (value){
		case 0:
			return EnumConnection.NONE;
		case 1:
			return EnumConnection.LEVER;
		}
		return EnumConnection.NONE;
	}
	
	public EnumConnection up = EnumConnection.NONE, down = EnumConnection.NONE, north = EnumConnection.NONE, south = EnumConnection.NONE, east = EnumConnection.NONE, west = EnumConnection.NONE;
	
	public TileEntityRelay(){
		super();
		capability.setEmberCapacity(0);
	}
	
	public void updateNeighbors(IBlockAccess world){
		up = getConnection(world,getPos().up(),Direction.DOWN);
		down = getConnection(world,getPos().down(),Direction.UP);
		north = getConnection(world,getPos().north(),Direction.NORTH);
		south = getConnection(world,getPos().south(),Direction.SOUTH);
		west = getConnection(world,getPos().west(),Direction.WEST);
		east = getConnection(world,getPos().east(),Direction.EAST);
	}
	
	@Override
	public CompoundNBT write(CompoundNBT tag){
		super.write(tag);
		tag.setInteger("up", up.ordinal());
		tag.setInteger("down", down.ordinal());
		tag.setInteger("north", north.ordinal());
		tag.setInteger("south", south.ordinal());
		tag.setInteger("west", west.ordinal());
		tag.setInteger("east", east.ordinal());
		if (target != null){
			tag.setInteger("targetX", target.getX());
			tag.setInteger("targetY", target.getY());
			tag.setInteger("targetZ", target.getZ());
		}
		capability.write(tag);
		return tag;
	}
	
	@Override
	public void read(CompoundNBT tag){
		super.read(tag);
		up = connectionFromInt(tag.getInteger("up"));
		down = connectionFromInt(tag.getInteger("down"));
		north = connectionFromInt(tag.getInteger("north"));
		south = connectionFromInt(tag.getInteger("south"));
		west = connectionFromInt(tag.getInteger("west"));
		east = connectionFromInt(tag.getInteger("east"));
		if (tag.contains("targetX")){
			target = new BlockPos(tag.getInteger("targetX"), tag.getInteger("targetY"), tag.getInteger("targetZ"));
		}
		capability.read(tag);
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
	
	public EnumConnection getConnection(IBlockAccess world, BlockPos pos, Direction side){
		if (world.getBlockState(pos).getBlock() == Blocks.LEVER){
			Direction face = world.getBlockState(pos).getValue(BlockLever.FACING).getFacing();
			if (face == side || face == Direction.DOWN && side == Direction.UP || face == Direction.UP && side == Direction.DOWN){
				return EnumConnection.LEVER;
			}
		}
		return EnumConnection.NONE;
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
	public boolean hasCapability(Capability<?> capability, Direction facing){
		if (capability == EmbersCapabilities.EMBER_CAPABILITY){
			return true;
		}
		return super.hasCapability(capability, facing);
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, Direction facing){
		if (capability == EmbersCapabilities.EMBER_CAPABILITY){
			return (T)this.capability;
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public void setTargetPosition(BlockPos pos, Direction side) {
		if (pos.compareTo(getPos()) != 0){
			capability.setEmberCapacity(200);
			target = pos;
			markDirty();
		}
	}

	@Override
	public boolean isFull() {
		polled = true;
		if (target != null){
			TileEntity tile = getWorld().getTileEntity(target);
			if (tile instanceof TileEntityRelay){
				if (((TileEntityRelay)tile).target != null && !((TileEntityRelay)tile).polled){
					if (((TileEntityRelay)tile).target.compareTo(getPos()) != 0){
						return ((IEmberPacketReceiver)tile).isFull();
					}
				}
			}
			else if (tile instanceof IEmberPacketReceiver) {
				return ((IEmberPacketReceiver) tile).isFull();
			}
		}
		return true;
	}

	@Override
	public boolean onReceive(EntityEmberPacket packet) {
		if (target != null){
			packet.dest = target;
			packet.lifetime = 80;
		}
		else {
			packet.dest = getPos();
		}
		getWorld().playSound(null, pos, SoundManager.EMBER_RELAY, SoundCategory.BLOCKS, 1.0f, 1.0f);
		return false;
	}
	
	@Override
	public void tick(){
		this.polled = false;
	}

	@Override
	public void markDirty() {
		super.markDirty();
		Misc.syncTE(this);
	}
}
