package teamroots.embers.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import teamroots.embers.Embers;
import teamroots.embers.SoundManager;
import teamroots.embers.util.sound.ISoundController;

import java.util.HashSet;

public class TileEntityFieldChart extends TileEntity implements ITileEntityBase, ISoundController, ITickableTileEntity {
	public static final int SOUND_LOOP = 1;
	public static final int[] SOUND_IDS = new int[]{SOUND_LOOP};

	HashSet<Integer> soundsPlaying = new HashSet<>();

	@Override
	public void onHarvest(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		this.remove();
		world.setTileEntity(pos, null);
	}

	@Override
	public boolean activate(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand,
			Direction side, float hitX, float hitY, float hitZ) {
		return false;
	}

	@Override
	public void tick() {
		if (getWorld().isRemote)
			handleSound();
	}

	@Override
	public void playSound(int id) {
		switch (id) {
			case SOUND_LOOP:
				Embers.proxy.playMachineSound(this, SOUND_LOOP, SoundManager.FIELD_CHART_LOOP, SoundCategory.BLOCKS, true, 1.0f, 1.0f, (float) pos.getX() + 0.5f, (float) pos.getY() + 0.5f, (float) pos.getZ() + 0.5f);
				break;
		}
		soundsPlaying.add(id);
	}

	@Override
	public void stopSound(int id) {
		soundsPlaying.remove(id);
	}

	@Override
	public boolean isSoundPlaying(int id) {
		return soundsPlaying.contains(id);
	}

	@Override
	public int[] getSoundIDs() {
		return SOUND_IDS;
	}

	@Override
	public boolean shouldPlaySound(int id) {
		return id == SOUND_LOOP;
	}
}
