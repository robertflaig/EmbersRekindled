package teamroots.embers.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import teamroots.embers.EventManager;
import teamroots.embers.api.tile.IExtraCapabilityInformation;
import teamroots.embers.api.tile.IExtraDialInformation;
import teamroots.embers.api.upgrades.IUpgradeProvider;
import teamroots.embers.api.upgrades.IUpgradeProxy;
import teamroots.embers.api.upgrades.UpgradeUtil;
import teamroots.embers.util.Misc;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TileEntityMechCore extends TileEntity implements ITileEntityBase, IExtraDialInformation, IExtraCapabilityInformation, IUpgradeProxy {
	Random random = new Random();
	
	public TileEntityMechCore(){
		super();
	}
	
	@Override
	public CompoundNBT write(CompoundNBT tag){
		return super.write(tag);
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

	public TileEntity getAttachedMultiblock() {
		if (getWorld().getTileEntity(getPos().offset(Direction.DOWN)) instanceof IMultiblockMachine){
			return getWorld().getTileEntity(getPos().offset(Direction.DOWN));
		}
		if (getWorld().getTileEntity(getPos().offset(Direction.UP)) instanceof IMultiblockMachine){
			return getWorld().getTileEntity(getPos().offset(Direction.UP));
		}
		if (getWorld().getTileEntity(getPos().offset(Direction.WEST)) instanceof IMultiblockMachine){
			return getWorld().getTileEntity(getPos().offset(Direction.WEST));
		}
		if (getWorld().getTileEntity(getPos().offset(Direction.EAST)) instanceof IMultiblockMachine){
			return getWorld().getTileEntity(getPos().offset(Direction.EAST));
		}
		if (getWorld().getTileEntity(getPos().offset(Direction.NORTH)) instanceof IMultiblockMachine){
			return getWorld().getTileEntity(getPos().offset(Direction.NORTH));
		}
		if (getWorld().getTileEntity(getPos().offset(Direction.SOUTH)) instanceof IMultiblockMachine){
			return getWorld().getTileEntity(getPos().offset(Direction.SOUTH));
		}
		return null;
	}

	public Direction getAttachedSide() {
		if (getWorld().getTileEntity(getPos().offset(Direction.DOWN)) instanceof IMultiblockMachine){
			return Direction.DOWN;
		}
		if (getWorld().getTileEntity(getPos().offset(Direction.UP)) instanceof IMultiblockMachine){
			return Direction.UP;
		}
		if (getWorld().getTileEntity(getPos().offset(Direction.WEST)) instanceof IMultiblockMachine){
			return Direction.WEST;
		}
		if (getWorld().getTileEntity(getPos().offset(Direction.EAST)) instanceof IMultiblockMachine){
			return Direction.EAST;
		}
		if (getWorld().getTileEntity(getPos().offset(Direction.NORTH)) instanceof IMultiblockMachine){
			return Direction.NORTH;
		}
		if (getWorld().getTileEntity(getPos().offset(Direction.SOUTH)) instanceof IMultiblockMachine){
			return Direction.SOUTH;
		}
		return null;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, Direction facing){
		TileEntity multiblock = getAttachedMultiblock();
		if(multiblock != null)
			return multiblock.hasCapability(capability, facing);
		return super.hasCapability(capability, facing);
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, Direction facing){
		TileEntity multiblock = getAttachedMultiblock();
		if(multiblock != null)
			return multiblock.getCapability(capability, facing);
		return super.getCapability(capability, facing);
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
	public void markDirty() {
		super.markDirty();
		Misc.syncTE(this);
	}

	@Override
	public void addDialInformation(Direction facing, List<String> information, String dialType) {
		TileEntity multiblock = getAttachedMultiblock();
		if(multiblock instanceof IExtraDialInformation)
			((IExtraDialInformation) multiblock).addDialInformation(facing,information,dialType);
	}

	@Override
	public boolean hasCapabilityDescription(Capability<?> capability) {
		TileEntity multiblock = getAttachedMultiblock();
		if(multiblock instanceof IExtraCapabilityInformation)
			return ((IExtraCapabilityInformation) multiblock).hasCapabilityDescription(capability);
		return false;
	}

	@Override
	public void addCapabilityDescription(List<String> strings, Capability<?> capability, Direction facing) {
		TileEntity multiblock = getAttachedMultiblock();
		if(multiblock instanceof IExtraCapabilityInformation)
			((IExtraCapabilityInformation) multiblock).addCapabilityDescription(strings,capability,facing);
	}

	@Override
	public void addOtherDescription(List<String> strings, Direction facing) {
		TileEntity multiblock = getAttachedMultiblock();
		if(multiblock instanceof IExtraCapabilityInformation)
			((IExtraCapabilityInformation) multiblock).addOtherDescription(strings,facing);
	}

	@Override
	public void collectUpgrades(List<IUpgradeProvider> upgrades) {
		for (Direction facing : Direction.VALUES) {
			if(isSocket(facing))
				UpgradeUtil.collectUpgrades(world,pos.offset(facing),facing.getOpposite(),upgrades);
		}
	}

	@Override
	public boolean isSocket(Direction facing) {
		Direction attachedSide = getAttachedSide();
		return facing != attachedSide;
	}

	@Override
	public boolean isProvider(Direction facing) {
		Direction attachedSide = getAttachedSide();
		return facing == attachedSide;
	}
}
