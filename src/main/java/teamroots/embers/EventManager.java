package teamroots.embers;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.Profile;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.terraingen.ChunkGeneratorEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import teamroots.embers.block.IDial;
import teamroots.embers.item.IEmberChargedTool;
import teamroots.embers.item.ItemAshenCloak;
import teamroots.embers.item.ItemEmberGauge;
import teamroots.embers.item.ItemGrandhammer;
import teamroots.embers.network.PacketHandler;
import teamroots.embers.network.message.MessageEmberBurstFX;
import teamroots.embers.network.message.MessageEmberGenOffset;
import teamroots.embers.network.message.MessageTEUpdate;
import teamroots.embers.network.message.MessageTyrfingBurstFX;
import teamroots.embers.proxy.ClientProxy;
import teamroots.embers.research.ResearchBase;
import teamroots.embers.research.ResearchManager;
import teamroots.embers.tileentity.ITileEntityBase;
import teamroots.embers.tileentity.ITileEntitySpecialRendererLater;
import teamroots.embers.tileentity.TileEntityDawnstoneAnvil;
import teamroots.embers.tileentity.TileEntityKnowledgeTable;
import teamroots.embers.util.BlockTextureUtil;
import teamroots.embers.util.EmberGenUtil;
import teamroots.embers.util.EmberInventoryUtil;
import teamroots.embers.util.FluidTextureUtil;
import teamroots.embers.util.Misc;
import teamroots.embers.util.RenderUtil;
import teamroots.embers.world.EmberWorldData;

public class EventManager {
	double gaugeAngle = 0;
	public static boolean hasRenderedParticles = false;
	Random random = new Random();
	public static float emberEyeView = 0;
	public static ResearchBase lastResearch = null;
	public static float frameTime = 0;
	public static float frameCounter = 0;
	public static long prevTime = 0;
	public static EnumHand lastHand = EnumHand.MAIN_HAND;
	public static float starlightRed = 255;
	public static float starlightGreen = 32;
	public static float starlightBlue = 255;
	public static float tickCounter = 0;
	public static double currentEmber = 0;
	public static boolean allowPlayerRenderEvent = true;
	public static int ticks = 0;
	
	public static Map<BlockPos, TileEntity> toUpdate = new HashMap<BlockPos, TileEntity>();
	
	static EntityPlayer clientPlayer = null;
	
	public static void markTEForUpdate(BlockPos pos, TileEntity tile){
		if (!toUpdate.containsKey(pos)){
			toUpdate.put(pos, tile);
		}
		else {
			toUpdate.replace(pos, tile);
		}
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onTextureStitchPre(TextureStitchEvent.Pre event){
		FluidTextureUtil.initTextures(event.getMap());
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onTextureStitch(TextureStitchEvent event){
		ResourceLocation particleGlow = new ResourceLocation("embers:entity/particle_mote");
		event.getMap().registerSprite(particleGlow);
		ResourceLocation particleSparkle = new ResourceLocation("embers:entity/particle_star");
		event.getMap().registerSprite(particleSparkle);
		ResourceLocation particleSmoke = new ResourceLocation("embers:entity/particle_smoke");
		event.getMap().registerSprite(particleSmoke);
	}
	
	@SubscribeEvent
	public void onServerTick(WorldTickEvent event){
		if (event.world.provider.getDimensionType() == DimensionType.OVERWORLD){
			if (Misc.random.nextInt(400) == 0){
				EmberGenUtil.offX ++;
				EmberWorldData.get(event.world).markDirty();
			}
			if (Misc.random.nextInt(400) == 0){
				EmberGenUtil.offZ ++;
				EmberWorldData.get(event.world).markDirty();
			}
			PacketHandler.INSTANCE.sendToAll(new MessageEmberGenOffset(EmberGenUtil.offX,EmberGenUtil.offZ));
		}
	}
	
	@SubscribeEvent
	public void onLivingDamage(LivingHurtEvent event){
		if (event.getEntity() instanceof EntityPlayer){
			EntityPlayer player = (EntityPlayer)event.getEntity();
			String source = event.getSource().getDamageType();
			if (source.compareTo("mob") != 0 && source.compareTo("generic") != 0 && source.compareTo("player") != 0 && source.compareTo("arrow") != 0){
				if (player.getHeldItemMainhand() != ItemStack.EMPTY){
					if (player.getHeldItemMainhand().getItem() == RegistryManager.inflictor_gem && player.getHeldItemMainhand().hasTagCompound()){
						player.getHeldItemMainhand().setItemDamage(1);
						player.getHeldItemMainhand().getTagCompound().setString("type", event.getSource().getDamageType());
					}
				}
				if (player.getHeldItemOffhand() != ItemStack.EMPTY){
					if (player.getHeldItemOffhand().getItem() == RegistryManager.inflictor_gem && player.getHeldItemOffhand().hasTagCompound()){
						player.getHeldItemOffhand().setItemDamage(1);
						player.getHeldItemOffhand().getTagCompound().setString("type", event.getSource().getDamageType());
					}
				}
			}
		}
		if (event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.HEAD) != ItemStack.EMPTY &&
				event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.CHEST) != ItemStack.EMPTY &&
				event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.LEGS) != ItemStack.EMPTY &&
				event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.FEET) != ItemStack.EMPTY){
			if (event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() instanceof ItemAshenCloak &&
					event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() instanceof ItemAshenCloak &&
					event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem() instanceof ItemAshenCloak &&
					event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() instanceof ItemAshenCloak){
				float mult = Math.max(0,1.0f-ItemAshenCloak.getDamageMultiplier(event.getSource(), event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.CHEST)));
				if (mult == 0){
					event.setCanceled(true);
				}
				event.setAmount(event.getAmount()*mult);
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onGameOverlayRender(RenderGameOverlayEvent.Post e){
		if (e.getType() == ElementType.TEXT){
			EventManager.frameCounter ++;
			EventManager.frameTime = (System.nanoTime()-prevTime)/1000000000.0f;
			EventManager.prevTime = System.nanoTime();
		}
		EntityPlayer player = Minecraft.getMinecraft().player;
		boolean showBar = false;

		int w = e.getResolution().getScaledWidth();
		int h = e.getResolution().getScaledHeight();
		
		int x = w/2;
		int y = h/2;
		if (player.getHeldItemMainhand() != ItemStack.EMPTY){
			if (player.getHeldItemMainhand().getItem() instanceof ItemEmberGauge){
				showBar = true;
			}
		}
		if (player.getHeldItemOffhand() != ItemStack.EMPTY){
			if (player.getHeldItemOffhand().getItem() instanceof ItemEmberGauge){
				showBar = true;
			}
		}
		
		Tessellator tess = Tessellator.getInstance();
		VertexBuffer b = tess.getBuffer();
		if (showBar){
			World world = player.getEntityWorld();
			if (e.getType() == ElementType.TEXT){
				GlStateManager.disableDepth();
				GlStateManager.disableCull();
				GlStateManager.pushMatrix();
				Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("embers:textures/gui/ember_meter_overlay.png"));
				GlStateManager.color(1f, 1f, 1f, 1f);
				
				int offsetX = 0;
				
				b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
				RenderUtil.drawQuadGui(b, 0, x-16, y-4, x+16, y-4, x+16, y-36, x-16, y-36, 0, 0, 1, 1);
				tess.draw();
				
				double angle = 195.0;
				EmberWorldData data = EmberWorldData.get(world);
				if (player != null){
					//if (data.emberData != null){
						//if (data.emberData.containsKey(""+((int)player.posX) / 16 + " " + ((int)player.posZ) / 16)){
							double ratio = EmberGenUtil.getEmberDensity(world.getSeed(), player.getPosition().getX(), player.getPosition().getZ());
							if (gaugeAngle == 0){
								gaugeAngle = 165.0+210.0*ratio;
							}
							else {
								gaugeAngle = gaugeAngle*0.99+0.01*(165.0+210.0*ratio);
							}
						//}
					//}
				}
				
				Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("embers:textures/gui/ember_meter_pointer.png"));
				GlStateManager.translate(x, y-20, 0);
				GlStateManager.rotate((float)gaugeAngle, 0, 0, 1);
				b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
				RenderUtil.drawQuadGui(b, 0.0, -2.5f, 13.5f, 13.5f, 13.5f, 13.5f, -2.5f, -2.5f, -2.5f, 0, 0, 1, 1);
				tess.draw();
				
				GlStateManager.popMatrix();
				GlStateManager.enableCull();
				GlStateManager.enableDepth();
			}
		}
		World world = player.getEntityWorld();
		RayTraceResult result = player.rayTrace(6.0, e.getPartialTicks());
		
		if (result != null){
			if (result.typeOfHit == RayTraceResult.Type.BLOCK){
				IBlockState state = world.getBlockState(result.getBlockPos());
				if (state.getBlock() instanceof IDial){
					List<String> text = ((IDial)state.getBlock()).getDisplayInfo(world, result.getBlockPos(), state);
					for (int i = 0; i < text.size(); i ++){
						Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(text.get(i), x-Minecraft.getMinecraft().fontRendererObj.getStringWidth(text.get(i))/2, y+40+11*i, 0xFFFFFF);
					}
				}
			}
		}
		Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("minecraft:textures/gui/icons.png"));
		GlStateManager.enableDepth();
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onTick(TickEvent.ClientTickEvent event){
		if (event.side == Side.CLIENT){
			ticks ++;
			ClientProxy.particleRenderer.updateParticles();

			EntityPlayer player = Minecraft.getMinecraft().player;
			if (player != null){
				World world = player.getEntityWorld();
				RayTraceResult result = player.rayTrace(6.0, Minecraft.getMinecraft().getRenderPartialTicks());
				if (result != null){
					if (result.typeOfHit == RayTraceResult.Type.BLOCK){
						IBlockState state = world.getBlockState(result.getBlockPos());
						if (state.getBlock() instanceof IDial){
							((IDial)state.getBlock()).updateTEData(world, state, result.getBlockPos());
						}
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onPlayerRender(RenderPlayerEvent.Pre event){
		if (event.getEntityPlayer() != null){
			if (Minecraft.getMinecraft().inGameHasFocus || event.getEntityPlayer().getUniqueID().compareTo(Minecraft.getMinecraft().player.getUniqueID()) != 0){
				event.setCanceled(!allowPlayerRenderEvent);
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public void onEntityDamaged(LivingHurtEvent event){
		if (event.getSource().damageType == RegistryManager.damage_ember.damageType){
			if (event.getEntityLiving().isPotionActive(Potion.getPotionFromResourceLocation("fire_resistance"))){
				event.setAmount(event.getAmount()*0.5f);
			}
		}
		if (event.getSource().getEntity() != null){
			if (event.getSource().getEntity() instanceof EntityPlayer){
				if (((EntityPlayer)event.getSource().getEntity()).getHeldItemMainhand().getItem() == RegistryManager.tyrfing){
					if (!event.getEntity().world.isRemote){
						PacketHandler.INSTANCE.sendToAll(new MessageTyrfingBurstFX(event.getEntity().posX,event.getEntity().posY+event.getEntity().height/2.0f,event.getEntity().posZ));
					}
					EntityPlayer p = ((EntityPlayer)event.getSource().getEntity());
					event.setAmount((event.getAmount()/4.0f)*(4.0f+(float)event.getEntityLiving().getEntityAttribute(SharedMonsterAttributes.ARMOR).getAttributeValue()*1.0f));
				}
				if (((EntityPlayer)event.getSource().getEntity()).getHeldItemMainhand() != ItemStack.EMPTY){
					if (((EntityPlayer)event.getSource().getEntity()).getHeldItemMainhand().getItem() instanceof IEmberChargedTool){
						if (((IEmberChargedTool)((EntityPlayer)event.getSource().getEntity()).getHeldItemMainhand().getItem()).hasEmber(((EntityPlayer)event.getSource().getEntity()).getHeldItemMainhand()) || ((EntityPlayer)event.getSource().getEntity()).capabilities.isCreativeMode){
							event.getEntityLiving().setFire(1);
							if (!event.getEntityLiving().getEntityWorld().isRemote){
								PacketHandler.INSTANCE.sendToAll(new MessageEmberBurstFX(event.getEntityLiving().posX,event.getEntityLiving().posY+event.getEntityLiving().getEyeHeight()/1.5,event.getEntityLiving().posZ));
								((EntityPlayer)event.getSource().getEntity()).getHeldItemMainhand().getTagCompound().setBoolean("didUse", true);
							}
						}
						else {
							event.setCanceled(true);
						}
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onBlockBreak(BlockEvent.BreakEvent event){
		if (event.getPlayer() != null){
			if (event.getPlayer().getHeldItemMainhand() != ItemStack.EMPTY){
				/*if (event.getPlayer().getHeldItemMainhand().getItem() instanceof IEmberChargedTool){
					PacketHandler.INSTANCE.sendToAll(new MessageEmberBurstFX(event.getPos().getX()+0.5,event.getPos().getY()+0.5,event.getPos().getZ()+0.5));
				}*/
				if (event.getPlayer().getHeldItemMainhand().getItem() instanceof ItemGrandhammer){
					event.setCanceled(true);
					event.getWorld().setBlockToAir(event.getPos());
				}
			}
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onRenderAfterWorld(RenderWorldLastEvent event){
		tickCounter ++;
		if (Embers.proxy instanceof ClientProxy){
			GlStateManager.pushMatrix();
			ClientProxy.particleRenderer.renderParticles(clientPlayer, event.getPartialTicks());
			GlStateManager.popMatrix();
		}
		List<TileEntity> list = Minecraft.getMinecraft().world.loadedTileEntityList;
		GlStateManager.pushMatrix();
		for (int i = 0; i < list.size(); i ++){
			TileEntitySpecialRenderer render = TileEntityRendererDispatcher.instance.getSpecialRenderer(list.get(i));
			if (render instanceof ITileEntitySpecialRendererLater){
				double x = Minecraft.getMinecraft().player.lastTickPosX + Minecraft.getMinecraft().getRenderPartialTicks()*(Minecraft.getMinecraft().player.posX-Minecraft.getMinecraft().player.lastTickPosX);
				double y = Minecraft.getMinecraft().player.lastTickPosY + Minecraft.getMinecraft().getRenderPartialTicks()*(Minecraft.getMinecraft().player.posY-Minecraft.getMinecraft().player.lastTickPosY);
				double z = Minecraft.getMinecraft().player.lastTickPosZ + Minecraft.getMinecraft().getRenderPartialTicks()*(Minecraft.getMinecraft().player.posZ-Minecraft.getMinecraft().player.lastTickPosZ);
				((ITileEntitySpecialRendererLater)render).renderLater(list.get(i), list.get(i).getPos().getX()-x, list.get(i).getPos().getY()-y, list.get(i).getPos().getZ()-z, Minecraft.getMinecraft().getRenderPartialTicks());
			}
		}
		GlStateManager.popMatrix();
		if (Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().gameSettings.thirdPersonView != 0){
			EntityPlayer p = Minecraft.getMinecraft().player;
			if (p.getGameProfile().getName().equalsIgnoreCase("Elucent")){
				if (EmberInventoryUtil.getEmberTotal(p) > 0){
					GlStateManager.pushMatrix();
					GlStateManager.disableLighting();
					GlStateManager.disableCull();
					GlStateManager.enableAlpha();
					GlStateManager.enableBlend();
					GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
					int dfunc = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
					GlStateManager.depthFunc(GL11.GL_LEQUAL);
					int func = GL11.glGetInteger(GL11.GL_ALPHA_TEST_FUNC);
					float ref = GL11.glGetFloat(GL11.GL_ALPHA_TEST_REF);
					GlStateManager.alphaFunc(GL11.GL_ALWAYS, 0);
					GlStateManager.depthMask(false);
					Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(Embers.MODID + ":textures/entity/beam.png"));
					Tessellator tess = Tessellator.getInstance();
					VertexBuffer buff = tess.getBuffer();
					buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
					GlStateManager.translate(0, 1, 0);
					GlStateManager.rotate(90.0f, 1, 0, 0);
					GlStateManager.rotate(p.rotationYaw, 0, 0, 1);
					GlStateManager.rotate(p.rotationPitch, 1, 0, 0);
					RenderUtil.renderAlchemyCircle(buff, 0, 0, 0, 1.0f, 0.25f, 0.0625f, (float)(EmberInventoryUtil.getEmberTotal(p)/EmberInventoryUtil.getEmberCapacityTotal(p)), 0.75f, 4.0f*((float)p.ticksExisted+event.getPartialTicks()));
					RenderUtil.renderAlchemyCircle(buff, 0, 0, 0, 1.0f, 0.25f, 0.0625f, (float)(EmberInventoryUtil.getEmberTotal(p)/EmberInventoryUtil.getEmberCapacityTotal(p)), 0.8f, 4.0f*((float)p.ticksExisted+event.getPartialTicks()));
					RenderUtil.renderAlchemyCircle(buff, 0, 0, 0, 1.0f, 0.25f, 0.0625f, (float)(EmberInventoryUtil.getEmberTotal(p)/EmberInventoryUtil.getEmberCapacityTotal(p)), 0.85f, 4.0f*((float)p.ticksExisted+event.getPartialTicks()));
					tess.draw();
					GlStateManager.depthMask(true);
					GlStateManager.alphaFunc(func, ref);
					GlStateManager.depthFunc(dfunc);
					GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
					GlStateManager.disableBlend();
					GlStateManager.disableAlpha();
					GlStateManager.enableCull();
					GlStateManager.enableLighting();
					GlStateManager.popMatrix();
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent event){
		if (!event.world.isRemote){
			List<TileEntity> tiles = event.world.loadedTileEntityList;
			NBTTagList list = new NBTTagList();
			for (TileEntity t : tiles){
				if (t instanceof ITileEntityBase){
					if (((ITileEntityBase)t).needsUpdate()){
						((ITileEntityBase)t).clean();
						if (!event.world.isRemote){
							list.appendTag(t.getUpdateTag());
						}
					}
				}
			}
			for (TileEntity t : toUpdate.values()){
				if (!event.world.isRemote){
					list.appendTag(t.getUpdateTag());
				}
			}
			NBTTagCompound tag = new NBTTagCompound();
			tag.setTag("data", list);
			PacketHandler.INSTANCE.sendToAll(new MessageTEUpdate(tag));
			toUpdate.clear();
		}
	}
}
