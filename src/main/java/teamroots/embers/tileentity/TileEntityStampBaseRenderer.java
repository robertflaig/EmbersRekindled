package teamroots.embers.tileentity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;
import teamroots.embers.block.BlockStampBase;

import java.util.Random;

public class TileEntityStampBaseRenderer extends TileEntitySpecialRenderer<TileEntityStampBase> {
	int blue, green, red, alpha;
	int lightx, lighty;
	double minU, minV, maxU, maxV, diffU, diffV;
	public TileEntityStampBaseRenderer(){
		super();
	}
	
	@Override
	public void render(TileEntityStampBase tile, double x, double y, double z, float partialTicks, int destroyStage, float tileAlpha){
		if (tile != null && !tile.getWorld().isAirBlock(tile.getPos())){
			if (tile.getWorld().getBlockState(tile.getPos()).getBlock() instanceof BlockStampBase){
				Direction face = tile.getWorld().getBlockState(tile.getPos()).getValue(BlockStampBase.facing);
				int capacity = tile.getCapacity();
	            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
				FluidStack fluidStack = tile.getFluidStack();

				if (fluidStack != null){
					Fluid fluid = fluidStack.getFluid();
					int amount = fluidStack.getAmount();
					int c = fluid.getColor(fluidStack);
		            blue = c & 0xFF;
		            green = (c >> 8) & 0xFF;
		            red = (c >> 16) & 0xFF;
		            alpha = (c >> 24) & 0xFF;
		            
		            TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fluid.getStill(fluidStack).toString());
		            diffU = maxU-minU;
		            diffV = maxV-minV;

					minU = sprite.getMinU()+diffU*0.25;
					maxU = sprite.getMaxU()-diffU*0.25;
					minV = sprite.getMinV()+diffV*0.25;
					maxV = sprite.getMaxV()-diffV*0.25;

					int i = getWorld().getCombinedLight(tile.getPos(), fluid.getLuminosity(fluidStack));
					lightx = i >> 0x10 & 0xFFFF;
					lighty = i & 0xFFFF;

					GlStateManager.disableCull();
					GlStateManager.disableLighting();
					GlStateManager.enableBlend();
					GlStateManager.enableAlpha();

					Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

					GL11.glPushMatrix();
					GL11.glTranslated(x, y, z);
					GL11.glTranslated(0.5, 0.5, 0.5);
					if (face == Direction.UP){
                        GL11.glRotated(180, 1, 0, 0);
                    }

					if (face == Direction.NORTH){
                        GL11.glRotated(90, 1, 0, 0);
                    }

					if (face == Direction.WEST){
                        GL11.glRotated(90, 0, 1, 0);
                        GL11.glRotated(90, 1, 0, 0);
                    }

					if (face == Direction.SOUTH){
                        GL11.glRotated(180, 0, 1, 0);
                        GL11.glRotated(90, 1, 0, 0);
                    }

					if (face == Direction.EAST){
                        GL11.glRotated(270, 0, 1, 0);
                        GL11.glRotated(90, 1, 0, 0);
                    }
					GL11.glTranslated(-0.5, -0.5, -0.5);

					Tessellator tess = Tessellator.getInstance();
					BufferBuilder buffer = tess.getBuffer();
					buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
					buffer.pos(0.25, 0.75+0.1875*((float)amount/(float)capacity), 0.25).tex(minU, minV).lightmap(lightx,lighty).color(red,green,blue,alpha).endVertex();
					buffer.pos(0.75, 0.75+0.1875*((float)amount/(float)capacity), 0.25).tex(maxU, minV).lightmap(lightx,lighty).color(red,green,blue,alpha).endVertex();
					buffer.pos(0.75, 0.75+0.1875*((float)amount/(float)capacity), 0.75).tex(maxU, maxV).lightmap(lightx,lighty).color(red,green,blue,alpha).endVertex();
					buffer.pos(0.25, 0.75+0.1875*((float)amount/(float)capacity), 0.75).tex(minU, maxV).lightmap(lightx,lighty).color(red,green,blue,alpha).endVertex();
					tess.draw();
					GL11.glPopMatrix();

					GlStateManager.disableAlpha();
					GlStateManager.disableBlend();
					GlStateManager.enableLighting();
					GlStateManager.enableCull();
				}
				if (!tile.inputs.getStackInSlot(0).isEmpty()){
					GL11.glPushMatrix();
					//EntityItem item = new EntityItem(Minecraft.getMinecraft().world,x,y,z,new ItemStack(tile.inputs.getStackInSlot(0).getItem(),1, tile.inputs.getStackInSlot(0).getMetadata()));
					//item.hoverStart = 0;
					GL11.glTranslated(x, y, z);
					GL11.glTranslated(0.5, 0.5+0.25, 0.5);
					
					if (face == Direction.UP){
						GL11.glRotated(180, 1, 0, 0);
					}
					
					if (face == Direction.NORTH){
						GL11.glRotated(90, 1, 0, 0);
					}
					
					if (face == Direction.WEST){
						GL11.glRotated(90, 0, 1, 0);
						GL11.glRotated(90, 1, 0, 0);
					}
					
					if (face == Direction.SOUTH){
						GL11.glRotated(180, 0, 1, 0);
						GL11.glRotated(90, 1, 0, 0);
					}
					
					if (face == Direction.EAST){
						GL11.glRotated(270, 0, 1, 0);
						GL11.glRotated(90, 1, 0, 0);
					}

					Random random = new Random();
					ItemStack stack = tile.inputs.getStackInSlot(0);
					random.setSeed((long)(stack.isEmpty() ? 187 : Item.getIdFromItem(stack.getItem()) + stack.getMetadata()));
					GL11.glScaled(1.0, 1.0, 1.0);
					for(int j = 0; j < Math.min(stack.getCount(),6); j++) {
						GlStateManager.pushMatrix();
						float f7 = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
						float f9 = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
						float f6 = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
						if(stack.getCount() > 1)
							GlStateManager.translate(f7, f9, f6);
						Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.GROUND);
						GlStateManager.popMatrix();
					}
					//Minecraft.getMinecraft().getRenderManager().renderEntity(item, 0, 0, 0, 0, 0, true);
					GL11.glPopMatrix();
				}
			}
		}
	}
}
