package teamroots.embers.compat;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import com.google.common.collect.Lists;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.registries.ForgeRegistryEntry;
import teamroots.embers.Embers;
import teamroots.embers.RegistryManager;
import teamroots.embers.api.alchemy.AspectList;
import teamroots.embers.api.alchemy.AspectList.AspectRangeList;
import teamroots.embers.api.capabilities.EmbersCapabilities;
import teamroots.embers.api.power.IEmberCapability;
import teamroots.embers.block.BlockExplosionPedestal;
import teamroots.embers.item.ItemEmberStorage;
import teamroots.embers.item.bauble.*;
import teamroots.embers.recipe.AlchemyRecipe;
import teamroots.embers.recipe.RecipeRegistry;
import teamroots.embers.research.ResearchBase;
import teamroots.embers.research.ResearchManager;
import teamroots.embers.research.subtypes.ResearchShowItem;
import teamroots.embers.tileentity.TileEntityExplosionPedestal;


public class BaublesIntegration {
    public static Block explosion_pedestal;
    public static CustomRecipe recipeManager;
    public static Item ember_amulet, ember_belt, ember_ring, mantle_bulb;
    public static Item radiant_crown, rocket_booster, ashen_amulet, glimmer_charm, nonbeliever_amulet, dawnstone_mail, explosion_charm, climbers_belt, crystal_lenses;

    public static ResourceLocation getRL(String s) {
        return new ResourceLocation(Embers.MODID, s);
    }

    public static void registerRecipes(CustomRecipe RegistryEvent.Register<Recipe> event) {
        event.getRegistry().register(new ShapedRecipe(getRL("ember_ring"), new ItemStack(ember_ring, 1), true, new Object[]{
                "CN ",
                "N N",
                " N ",
                'C', RegistryManager.ember_cluster,
                'N', "nuggetDawnstone"}).setMirrored(true).setRegistryName(getRL("ember_ring")));
        event.getRegistry().register(new ShapedRecipe(getRL("ember_amulet"), new ItemStack(ember_amulet, 1), true, new Object[]{
                " L ",
                "L L",
                "NCN",
                'C', RegistryManager.ember_cluster,
                'N', "nuggetDawnstone",
                'L', Items.LEATHER}).setRegistryName(getRL("ember_amulet")));
        event.getRegistry().register(new ShapedRecipe(getRL("ember_belt"), new ItemStack(ember_belt, 1), true, new Object[]{
                "LIL",
                "L L",
                "PCP",
                'C', RegistryManager.ember_cluster,
                'I', "ingotDawnstone",
                'P', "plateDawnstone",
                'L', Items.LEATHER}).setRegistryName(getRL("ember_belt")));
        event.getRegistry().register(new ShapedRecipe(getRL("ember_bulb"), new ItemStack(mantle_bulb, 1), true, new Object[]{
                " CI",
                "GSG",
                " G ",
                'I', "ingotLead",
                'S', RegistryManager.ember_cluster,
                'C', "plateDawnstone",
                'G', "blockGlass"}).setMirrored(true).setRegistryName(getRL("ember_bulb")));
        event.getRegistry().register(new ShapedRecipe(getRL("ashen_amulet"), new ItemStack(ashen_amulet, 1), true, new Object[]{
                " L ",
                "L L",
                "NCN",
                'C', "dustAsh",
                'N', RegistryManager.archaic_brick,
                'L', Items.LEATHER}).setRegistryName(getRL("ashen_amulet")));
        /*event.getRegistry().register(new ShapedRecipe(getRL("explosion_charm"), new ItemStack(explosion_charm, 1), true, new Object[]{
                "LB ",
                "BCB",
                " B ",
                'L', Items.LEATHER,
                'C', RegistryManager.ember_cluster,
                'B', RegistryManager.archaic_brick}).setMirrored(true).setRegistryName(getRL("explosion_charm")));*/
        event.getRegistry().register(new ShapedRecipe(getRL("dawnstone_mail"), new ItemStack(dawnstone_mail, 1), true, new Object[]{
                "P P",
                "PPP",
                "PPP",
                'P', "plateDawnstone"}).setMirrored(true).setRegistryName(getRL("dawnstone_mail")));
        /*event.getRegistry().register(new ShapedRecipe(getRL("nonbeliever_amulet"), new ItemStack(nonbeliever_amulet, 1), true, new Object[]{
                " L ",
                "L L",
                "NCN",
                'C', RegistryManager.eldritch_insignia,
                'N', RegistryManager.archaic_circuit,
                'L', Items.LEATHER}).setRegistryName(getRL("nonbeliever_amulet")));*/

        Ingredient leather = Ingredient.of(Items.LEATHER);
        Ingredient archaicBrick = Ingredient.of(RegistryManager.archaic_brick);
        Ingredient eldritchInsignia = Ingredient.of(RegistryManager.eldritch_insignia);
        Ingredient archaicCircuit = Ingredient.of(RegistryManager.archaic_circuit);

        RecipeRegistry.alchemyRecipes.add(new AlchemyRecipe(new AspectRangeList().setRange("iron", 8, 32).setRange("copper", 8, 32), Ingredient.fromItem(RegistryManager.ember_cluster), Lists.newArrayList(archaicBrick, leather, archaicBrick, archaicBrick), new ItemStack(explosion_charm, 1)));
        RecipeRegistry.alchemyRecipes.add(new AlchemyRecipe(new AspectRangeList().setRange("dawnstone", 32, 64).setRange("silver", 32, 64), Ingredient.fromItem(ashen_amulet), Lists.newArrayList(archaicCircuit, archaicBrick, archaicCircuit, eldritchInsignia), new ItemStack(nonbeliever_amulet, 1)));
        RecipeRegistry.alchemyRecipes.add(new AlchemyRecipe(new AspectRangeList().setRange("copper", 16, 24).setRange("silver", 16, 24),
                Ingredient.fromItem(RegistryManager.dust_ember),
                Lists.newArrayList( Ingredient.EMPTY, Ingredient.fromItem(explosion_charm), Ingredient.EMPTY, Ingredient.fromStacks(new ItemStack(RegistryManager.alchemy_pedestal))),
                new ItemStack(explosion_pedestal,1)));
    }

    public static void registerAll() //éw
    {
        RegistryManager.blocks.add(explosion_pedestal = (new BlockExplosionPedestal(Material.STONE, "explosion_pedestal",true)).setIsFullCube(false).setIsOpaqueCube(false).setHarvestProperties("pickaxe", 0).setHardness(1.6f));

        RegistryManager.items.add(ember_ring = new ItemEmberRing("ember_ring", true));
        RegistryManager.items.add(ember_belt = new ItemEmberBelt("ember_belt", true));
        RegistryManager.items.add(ember_amulet = new ItemEmberAmulet("ember_amulet", true));
        RegistryManager.items.add(mantle_bulb = new ItemEmberBulb());
        RegistryManager.items.add(dawnstone_mail = new ItemDawnstoneMail("dawnstone_mail", true));
        RegistryManager.items.add(ashen_amulet = new ItemAshenAmulet("ashen_amulet", true));
        RegistryManager.items.add(nonbeliever_amulet = new ItemNonbelieverAmulet("nonbeliever_amulet", true));
        RegistryManager.items.add(explosion_charm = new ItemExplosionCharm("explosion_charm", true));

        //GameRegistry.registerTileEntity(TileEntityExplosionPedestal.class, Embers.MODID+":tile_entity_explosion_pedestal");
        BlockEntity.addEntityType(TileEntityExplosionPedestal.class, Embers.MODID+":tile_entity_explosion_pedestal");
    }

    public static void init() {

    }

    @SideOnly(Side.CLIENT)
    public static void registerClientSide() {
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new ItemEmberStorage.ColorHandler(), mantle_bulb);
    }

    public static double getEmberCapacityTotal(Player player) {
        IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);
        double amount = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack bauble = handler.getStackInSlot(i);
            if (bauble.hasCapability(EmbersCapabilities.EMBER_CAPABILITY, null)) {
                IEmberCapability capability = bauble.getCapability(EmbersCapabilities.EMBER_CAPABILITY, null);
                amount += capability.getEmberCapacity();
            }
        }
        return amount;
    }

    public static double getEmberTotal(Player player) {
        IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);
        double amount = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack bauble = handler.getStackInSlot(i);
            if (bauble.hasCapability(EmbersCapabilities.EMBER_CAPABILITY, null)) {
                IEmberCapability capability = bauble.getCapability(EmbersCapabilities.EMBER_CAPABILITY, null);
                amount += capability.getEmber();
            }
        }
        return amount;
    }

    public static double removeEmber(Player player, double amount) {
        IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack bauble = handler.getStackInSlot(i);
            if (bauble.hasCapability(EmbersCapabilities.EMBER_CAPABILITY, null)) {
                IEmberCapability capability = bauble.getCapability(EmbersCapabilities.EMBER_CAPABILITY, null);
                amount -= capability.removeAmount(amount, true);
            }
        }
        return amount;
    }

    public static void initBaublesCategory() {
        ItemStack fullBulb = ((ItemEmberBulb)mantle_bulb).withFill(((ItemEmberBulb)mantle_bulb).getCapacity());

        ResearchManager.cost_reduction = new ResearchShowItem("cost_reduction", new ItemStack(ember_amulet), 5, 5).addItem(new ResearchShowItem.DisplayItem(new ItemStack(ember_amulet), new ItemStack(ember_belt), new ItemStack(ember_ring)));
        ResearchManager.mantle_bulb = new ResearchBase("mantle_bulb", fullBulb, 7, 3);
        ResearchManager.explosion_charm = new ResearchBase("explosion_charm", new ItemStack(explosion_charm), 9, 2);
        ResearchManager.nonbeliever_amulet = new ResearchBase("nonbeliever_amulet", new ItemStack(nonbeliever_amulet), 1, 3);
        ResearchManager.ashen_amulet = new ResearchBase("ashen_amulet", new ItemStack(ashen_amulet), 4, 3);
        ResearchManager.dawnstone_mail = new ResearchBase("dawnstone_mail", new ItemStack(dawnstone_mail), 3, 7);
        ResearchManager.explosion_pedestal = new ResearchBase("explosion_pedestal", new ItemStack(explosion_pedestal), 11, 1).addAncestor(ResearchManager.explosion_charm);

        ResearchManager.subCategoryBaubles.addResearch(ResearchManager.cost_reduction);
        ResearchManager.subCategoryBaubles.addResearch(ResearchManager.mantle_bulb);
        ResearchManager.subCategoryBaubles.addResearch(ResearchManager.explosion_charm);
        ResearchManager.subCategoryBaubles.addResearch(ResearchManager.nonbeliever_amulet);
        ResearchManager.subCategoryBaubles.addResearch(ResearchManager.ashen_amulet);
        ResearchManager.subCategoryBaubles.addResearch(ResearchManager.dawnstone_mail);
        ResearchManager.subCategoryBaubles.addResearch(ResearchManager.explosion_pedestal);
    }

    public static void loadConfig() {
        //NOOP
    }
}
