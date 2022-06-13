package teamroots.embers.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import teamroots.embers.EventManager;
import teamroots.embers.SoundManager;
import teamroots.embers.api.capabilities.EmbersCapabilities;
import teamroots.embers.api.power.IEmberCapability;
import teamroots.embers.api.power.IEmberPacketProducer;
import teamroots.embers.api.power.IEmberPacketReceiver;
import teamroots.embers.block.BlockEmberPulser;
import teamroots.embers.entity.EntityEmberPacket;
import teamroots.embers.power.DefaultEmberCapability;
import teamroots.embers.util.Misc;

import javax.annotation.Nullable;
import java.util.Random;

public class TileEntityPulser extends TileEntity implements ITileEntityBase, ITickableTileEntity, IEmberPacketProducer {
	public static final double PULL_RATE = 100.0;
	public static final double TRANSFER_RATE = 400.0;

	public IEmberCapability capability = new DefaultEmberCapability() {
		@Override
		public void onContentsChanged() {
			markDirty();
		}

		@Override
		public boolean acceptsVolatile() {
			return false;
		}
	};
	public BlockPos target = null;
	public long ticksExisted = 0;
	Random random = new Random();
	int offset = random.nextInt(40);

	public enum EnumConnection{
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
	
	public TileEntityPulser(){
		super();
		capability.setEmberCapacity(2000);
	}
	
	public void updateNeighbors(IBlockAccess world){
		down = getConnection(world,getPos().down(),Direction.DOWN);
		up = getConnection(world,getPos().up(),Direction.UP);
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
		return Misc.isValidLever(world,pos,side) ? EnumConnection.LEVER : EnumConnection.NONE;
	}

	@Override
	public void markDirty() {
		super.markDirty();
		Misc.syncTE(this);
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
		this.ticksExisted ++;
		IBlockState state = getWorld().getBlockState(getPos());
		Direction facing = state.getValue(BlockEmberPulser.facing);
		TileEntity attachedTile = getWorld().getTileEntity(getPos().offset(facing.getOpposite()));
		if (ticksExisted % 5 == 0 && attachedTile != null){
			if (attachedTile.hasCapability(EmbersCapabilities.EMBER_CAPABILITY, facing)){
				IEmberCapability cap = attachedTile.getCapability(EmbersCapabilities.EMBER_CAPABILITY, facing);
				if (cap.getEmber() > 0 && capability.getEmber() < capability.getEmberCapacity()){
					double removed = cap.removeAmount(PULL_RATE, true);
					capability.addAmount(removed, true);
					//markDirty();
					//attachedTile.markDirty();
				}
			}
		}
		if ((this.ticksExisted+offset) % 20 == 0 && getWorld().isBlockPowered(getPos()) && target != null && !getWorld().isRemote && this.capability.getEmber() > PULL_RATE){
			TileEntity targetTile = getWorld().getTileEntity(target);
			if (targetTile instanceof IEmberPacketReceiver){
				if (!(((IEmberPacketReceiver) targetTile).isFull())){
					EntityEmberPacket packet = new EntityEmberPacket(getWorld());
					Vec3d velocity = getBurstVelocity(facing);
					packet.initCustom(getPos(), target, velocity.x, velocity.y, velocity.z, Math.min(TRANSFER_RATE,capability.getEmber()));
					this.capability.removeAmount(Math.min(TRANSFER_RATE,capability.getEmber()), true);
					getWorld().spawnEntity(packet);
					getWorld().playSound(null, pos, SoundManager.EMBER_EMIT_BIG, SoundCategory.BLOCKS, 1.0f, 1.0f);
					//markDirty();
				}
			}
		}
	}

	private Vec3d getBurstVelocity(Direction facing) {
		switch(facing)
        {
            case DOWN:
                return new Vec3d(0, -0.5, 0);
            case UP:
				return new Vec3d(0, 0.5, 0);
            case NORTH:
				return new Vec3d(0, -0.01, -0.5);
            case SOUTH:
				return new Vec3d(0, -0.01, 0.5);
            case WEST:
				return new Vec3d(-0.5, -0.01, 0);
            case EAST:
				return new Vec3d(0.5, -0.01, 0);
			default:
				return Vec3d.ZERO;
        }
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
		target = pos;
		markDirty();
	}
}
