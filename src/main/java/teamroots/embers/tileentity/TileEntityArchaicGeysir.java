package teamroots.embers.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BreakableBlock;
import net.minecraft.block.GlassBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.CampfireTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import teamroots.embers.particle.ParticleUtil;
import teamroots.embers.util.Misc;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Random;

public class TileEntityArchaicGeysir extends TileEntityOpenTank implements ITileEntityBase, ITickableTileEntity, IMultiblockMachine {
	int ticksExisted = 0;
	long charge = 0;

	@Override
	public AxisAlignedBB getRenderBoundingBox(){
		return super.getRenderBoundingBox().expand(4.0, 256.0, 4.0);
	}

	public TileEntityArchaicGeysir(){
		super();
		tank = new FluidTank(Integer.MAX_VALUE){
			@Override
			public void onContentsChanged(){
				TileEntityArchaicGeysir.this.markDirty();
			}

			@Override
			public int fill(FluidStack resource, FluidAction action) {
				if(Misc.isGaseousFluid(resource)) {
					setEscapedFluid(resource);
					return resource.getAmount();
				}
				return 0;
			}
		};
		tank.writeToNBT(this.getTileData());
		//tank.setCanFill(true);
		//tank.setCanDrain(false);
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
	public void onHarvest(World world, BlockPos pos, BlockState state, PlayerEntity player){
		this.remove();
		world.setBlockState(pos.add(1,0,0), Blocks.AIR.getDefaultState());
		world.setBlockState(pos.add(0,0,1), Blocks.AIR.getDefaultState());
		world.setBlockState(pos.add(-1,0,0), Blocks.AIR.getDefaultState());
		world.setBlockState(pos.add(0,0,-1), Blocks.AIR.getDefaultState());
		world.setBlockState(pos.add(1,0,-1), Blocks.AIR.getDefaultState());
		world.setBlockState(pos.add(-1,0,1), Blocks.AIR.getDefaultState());
		world.setBlockState(pos.add(1,0,1), Blocks.AIR.getDefaultState());
		world.setBlockState(pos.add(-1,0,-1), Blocks.AIR.getDefaultState());
		world.setTileEntity(pos, null);
	}
/*	public void onHarvest(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		this.remove();
		world.setBlockToAir(pos.add(1,0,0));
		world.setBlockToAir(pos.add(0,0,1));
		world.setBlockToAir(pos.add(-1,0,0));
		world.setBlockToAir(pos.add(0,0,-1));
		world.setBlockToAir(pos.add(1,0,-1));
		world.setBlockToAir(pos.add(-1,0,1));
		world.setBlockToAir(pos.add(1,0,1));
		world.setBlockToAir(pos.add(-1,0,-1));
		world.setTileEntity(pos, null);
	}*/

	@Override
	public void tick() {
		ticksExisted ++;
		if (world.isRemote && true)
			updateEscapeParticles();
		lastEscapedTickServer = lastEscapedTickClient = world.getGameTime();
	}

	@Override
	protected void updateEscapeParticles() {
		Color fluidColor = new Color(99,100,135);
		Random random = new Random();
		float force = 0.5f;
		for (int i = 0; i < 15; i++) {
			float xOffset = 0.5f + (random.nextFloat() - 0.5f) * 2 * 0.6f * force;
			float yOffset = 1.1f+0.4f*force;
			float zOffset = 0.5f + (random.nextFloat() - 0.5f) * 2 * 0.6f * force;

			double angle = random.nextDouble()*2*Math.PI;
			float velocity = random.nextFloat()*0.2f;

			velocity *= force;
			float xVel = (float)Math.sin(angle)*velocity;
			float yVel = velocity*5.0f;
			float zVel = (float)Math.cos(angle)*velocity;

			ParticleUtil.spawnParticleVapor(world, pos.getX() + xOffset, pos.getY() + yOffset, pos.getZ() + zOffset, xVel, yVel, zVel, fluidColor.getRed() / 255f, fluidColor.getGreen() / 255f, fluidColor.getBlue() / 255f, fluidColor.getAlpha() / 255f, 6*force, 20*force, 40);
		}
	}

	@Override
	public void markDirty() {
		super.markDirty();
		Misc.syncTE(this);
	}
}
