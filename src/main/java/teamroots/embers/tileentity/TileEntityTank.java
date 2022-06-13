package teamroots.embers.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.TileFluidHandler;
import teamroots.embers.RegistryManager;
import teamroots.embers.particle.ParticleUtil;
import teamroots.embers.util.FluidColorHelper;
import teamroots.embers.util.Misc;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Random;

public class TileEntityTank extends TileEntityOpenTank implements ITileEntityBase, ITickableTileEntity {
	public static int capacity = Fluid.BUCKET_VOLUME*16;


	
	public TileEntityTank(){
		super();
		tank = new FluidTank(capacity) {
			@Override
			public void onContentsChanged(){
				TileEntityTank.this.markDirty();
			}

			@Override
			public int fill(FluidStack resource, boolean doFill) {
				if(Misc.isGaseousFluid(resource)) {
					setEscapedFluid(resource);
					return resource.getAmount();
				}
				return super.fill(resource, doFill);
			}
		};
		tank.setTileEntity(this);
		tank.setCanFill(true);
		tank.setCanDrain(true);
	}

	@Override
	public void tick() {
		if (world.isRemote && shouldEmitParticles())
			updateEscapeParticles();
	}

	@Override
	protected void updateEscapeParticles() {
		Color fluidColor = new Color(FluidColorHelper.getColor(lastEscaped), true);
		Random random = new Random();
		for (int i = 0; i < 3; i++) {
			float xOffset = 0.5f + (random.nextFloat() - 0.5f) * 2 * 0.2f;
			float yOffset = 0.9f;
			float zOffset = 0.5f + (random.nextFloat() - 0.5f) * 2 * 0.2f;

			ParticleUtil.spawnParticleVapor(world, pos.getX() + xOffset, pos.getY() + yOffset, pos.getZ() + zOffset, 0, 1 / 20f, 0, fluidColor.getRed() / 255f, fluidColor.getGreen() / 255f, fluidColor.getBlue() / 255f, fluidColor.getAlpha() / 255f, 4, 2, 20);
		}
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
		ItemStack heldItem = player.getHeldItem(hand);
		if (!heldItem.isEmpty()){
			boolean didFill = FluidUtil.interactWithFluidHandler(player, hand, world, pos, side);
			this.markDirty();
			return didFill;
		}
		return false;
	}
	
	public int getCapacity(){
		return tank.getCapacity();
	}
	
	public int getAmount(){
		return tank.getFluidAmount();
	}
	
	public FluidTank getTank(){
		return tank;
	}
	
	public Fluid getFluid(){
		if (tank.getFluid() != null){
			return tank.getFluid().getFluid();
		}
		return null;
	}

	public FluidStack getFluidStack() {
		return tank.getFluid();
	}

	@Override
	public void onHarvest(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		this.remove();
		if (!world.isRemote && (player == null || !player.capabilities.isCreativeMode)){
			ItemStack toDrop = new ItemStack(RegistryManager.block_tank,1);
			if (getTank().getFluidAmount() > 0){
				CompoundNBT tag = new CompoundNBT();
				getTank().write(tag);
				toDrop.setTagCompound(tag);
			}
			world.spawnEntity(new EntityItem(world,pos.getX()+0.5,pos.getY()+0.5,pos.getZ()+0.5,toDrop));
		}
		world.setTileEntity(pos, null);
	}

	@Override
	public void markDirty() {
		super.markDirty();
		Misc.syncTE(this);
	}
}
