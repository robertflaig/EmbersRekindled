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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import teamroots.embers.EventManager;
import teamroots.embers.api.capabilities.EmbersCapabilities;
import teamroots.embers.api.power.IEmberCapability;
import teamroots.embers.api.power.IEmberPacketReceiver;
import teamroots.embers.block.BlockEmberEmitter;
import teamroots.embers.entity.EntityEmberPacket;
import teamroots.embers.power.DefaultEmberCapability;
import teamroots.embers.util.Misc;

import javax.annotation.Nullable;
import java.util.Random;

public class TileEntityReceiver extends TileEntity implements ITileEntityBase, ITickableTileEntity, IEmberPacketReceiver {
	public static final int TRANSFER_RATE = 10;

	public IEmberCapability capability = new DefaultEmberCapability(){
		@Override
		public void onContentsChanged() {
			markDirty();
		}

		@Override
		public boolean acceptsVolatile() {
			return false;
		}
	};
	Random random = new Random();
	long ticksExisted = 0;

	public TileEntityReceiver(){
		super();
		capability.setEmberCapacity(2000);
	}
	
	@Override
	public CompoundNBT write(CompoundNBT tag){
		super.write(tag);
		capability.write(tag);
		return tag;
	}
	
	@Override
	public void read(CompoundNBT tag){
		super.read(tag);
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
		BlockPos pos = getPos();
		IBlockState state = getWorld().getBlockState(pos);
		Direction facing = state.getValue(BlockEmberEmitter.facing);
		TileEntity attachedTile = getWorld().getTileEntity(pos.offset(facing.getOpposite()));
		if (ticksExisted % 2 == 0 && attachedTile != null){
			if (attachedTile.hasCapability(EmbersCapabilities.EMBER_CAPABILITY, facing)){
				IEmberCapability cap = attachedTile.getCapability(EmbersCapabilities.EMBER_CAPABILITY, facing);
				if (cap != null){
					if (cap.getEmber() < cap.getEmberCapacity() && capability.getEmber() > 0){
						double added = cap.addAmount(Math.min(TRANSFER_RATE,capability.getEmber()), true);
						capability.removeAmount(added, true);
						//markDirty();
						if (!getWorld().isRemote){
							attachedTile.markDirty();
						}
					}
				}
			}
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
	public boolean isFull(){
		return capability.getEmber() >= capability.getEmberCapacity();
	}

	@Override
	public boolean onReceive(EntityEmberPacket packet) {
		return true;
	}

	@Override
	public void markDirty() {
		super.markDirty();
		Misc.syncTE(this);
	}
}
