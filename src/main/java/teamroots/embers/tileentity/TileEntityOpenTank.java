package teamroots.embers.tileentity;

import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.CampfireTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.TileFluidHandler;
import teamroots.embers.particle.ParticleUtil;
import teamroots.embers.util.FluidColorHelper;

import java.awt.*;
import java.util.Random;

//import CompoundNBT;

public abstract class TileEntityOpenTank extends TileFluidHandler {
    FluidStack lastEscaped = null;
    long lastEscapedTickServer;
    long lastEscapedTickClient;

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        CompoundNBT compound = super.write(tag);
        if(lastEscaped != null) {
            compound.put("lastEscaped",lastEscaped.writeToNBT(new CompoundNBT()));
            compound.putLong("lastEscapedTick",lastEscapedTickServer);
        }
        return compound;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        if(tag.contains("lastEscaped")) {
            lastEscaped = FluidStack.loadFluidStackFromNBT(tag.getCompound("lastEscaped"));
            lastEscapedTickServer = tag.getLong("lastEscapedTick");
        }
    }

    public void setEscapedFluid(FluidStack stack) {
        lastEscaped = stack;
        lastEscapedTickServer = world.getDayTime();
        markDirty();
    }

    protected boolean shouldEmitParticles() {
        if(lastEscaped == null)
            return false;
        if(lastEscapedTickClient < lastEscapedTickServer) {
            lastEscapedTickClient = lastEscapedTickServer;
            return true;
        }
        long dTime = world.getDayTime() - lastEscapedTickClient;
        if(dTime < lastEscaped.getAmount()+5)
            return true;
        return false;
    }

    protected abstract void updateEscapeParticles();


}
