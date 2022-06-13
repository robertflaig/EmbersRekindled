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
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import teamroots.embers.EventManager;
import teamroots.embers.SoundManager;
import teamroots.embers.block.BlockInfernoForgeEdge;
import teamroots.embers.util.Misc;

import javax.annotation.Nullable;

public class TileEntityInfernoForgeOpening extends TileEntity implements ITileEntityBase, ITickableTileEntity {
	public boolean isOpen;
	public boolean prevState;
	public float openAmount;
	public float lastOpenAmount;
	
	public TileEntityInfernoForgeOpening(){
		super();
	}
	
	@Override
	public CompoundNBT write(CompoundNBT tag){
		super.write(tag);
		tag.setBoolean("isOpen", isOpen);
		tag.setBoolean("prevState", prevState);
		tag.setFloat("openAmount", openAmount);
		return tag;
	}
	
	@Override
	public void read(CompoundNBT tag){
		super.read(tag);
		isOpen = tag.getBoolean("isOpen");
		prevState = tag.getBoolean("prevState");
		openAmount = tag.getFloat("openAmount");
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
	public void markDirty() {
		super.markDirty();
		Misc.syncTE(this);
	}

	public void toggle() {
		if(isOpen)
			close();
		else
			open();
	}

	public void open() {
		isOpen = true;
		prevState = false;
		world.playSound(null,getPos().getX()+0.5,getPos().getY()+0.5,getPos().getZ()+0.5, SoundManager.INFERNO_FORGE_OPEN, SoundCategory.BLOCKS, 1.0f, 1.0f);
		markDirty();
	}

	public void close() {
		isOpen = false;
		prevState = true;
		world.playSound(null,getPos().getX()+0.5,getPos().getY()+0.5,getPos().getZ()+0.5, SoundManager.INFERNO_FORGE_CLOSE, SoundCategory.BLOCKS, 1.0f, 1.0f);
		markDirty();
	}

	@Override
	public boolean activate(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand,
			Direction side, float hitX, float hitY, float hitZ) {
		if (!player.isSneaking()){
			if (world.getTileEntity(pos.down()) instanceof TileEntityInfernoForge){
				TileEntityInfernoForge forge = getForge(world, pos);
				if (forge != null && forge.progress == 0){
					if(isOpen && forge.capability.getEmber() <= 0) { //Syke bitch, not enough ember
						if(!world.isRemote)
							player.sendStatusMessage(new TextComponentTranslation("embers.tooltip.forge.cannot_start"),true);
						return true;
					}
					toggle();
					return true;
				}
			}
		}
		return false;
	}

	private TileEntityInfernoForge getForge(World world, BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos.down());
		return tile instanceof TileEntityInfernoForge ? (TileEntityInfernoForge) tile : null;
	}

	@Override
	public void onHarvest(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		this.remove();
		BlockInfernoForgeEdge.breakBlockSafe(world, pos.down(), player);
		world.setTileEntity(pos, null);
	}

	@Override
	public void tick() {
		lastOpenAmount = openAmount;
		if (isOpen && !prevState && openAmount < 1.0f){
			openAmount = 0.5f+0.5f*openAmount;
			if (openAmount > 0.99f){
				openAmount = 1.0f;
				prevState = isOpen;
				markDirty();
			}
		}
		if (!isOpen && prevState && openAmount > 0.0f){
			openAmount = 0.5f*openAmount;
			if (openAmount < 0.01f){
				openAmount = 0.0f;
				TileEntityInfernoForge forge = getForge(world, pos);
				if(forge != null)
					forge.updateProgress();
				prevState = isOpen;
				markDirty();
			}
		}
	}
}
