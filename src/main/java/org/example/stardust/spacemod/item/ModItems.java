package org.example.stardust.spacemod.item;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.example.stardust.spacemod.SpaceMod;
import org.example.stardust.spacemod.entity.ModEntities;
import org.example.stardust.spacemod.item.custom.*;

import javax.tools.Tool;

public class ModItems {
    public static final Item BLOOD = registerItem("blood", new Item(new FabricItemSettings()));
    public static final Item GALLIUM_INGOT = registerItem("gallium_ingot", new Item(new FabricItemSettings()));
    public static final Item UNICORN_SPAWN_EGG = registerItem("unicorn_spawn_egg", new SpawnEggItem(ModEntities.UNICORN, 0xD57E36,0x1D0D00, new FabricItemSettings()));
    public static final Item FORMIC_SPAWN_EGG = registerItem("formic_spawn_egg", new SpawnEggItem(ModEntities.FORMIC, 0x1D0D00,0xD57E36, new FabricItemSettings()));
    public static final Item GRIFFIN_SPAWN_EGG = registerItem("griffin_spawn_egg", new SpawnEggItem(ModEntities.GRIFFIN, 0xE7E7E7, 0xFFB5B5, new FabricItemSettings()));
    public static final Item GIANT_SALAMANDER_SPAWN_EGG = registerItem("giant_salamander_spawn_egg", new SpawnEggItem(ModEntities.GIANT_SALAMANDER, 0xFFB5B5, 0xE7E7E7, new FabricItemSettings()));
    public static final Item PURE_BLOOD = registerItem("pure_blood", new Item(new FabricItemSettings()));
    public static final Item CLOAKING_DEVICE_ITEM = registerItem("cloaking_device_item", new CloakingDeviceItem(new FabricItemSettings().maxDamage(100)));
    public static final Item FROST_WAND = registerItem("frost_wand", new FrostWand(new FabricItemSettings().maxDamage(300)));
    public static final Item SACRIFICIAL_KNIFE = registerItem("sacrificial_knife", new SacrificialKnife(ModToolMaterial.GALLIUM, 3, -2.4F,  new FabricItemSettings()));
    public static final Item GLASS_SWORD = registerItem("glass_sword", new GlassSword(ModToolMaterial.GLASS, 3, -2.4F,  new FabricItemSettings()));
    public static final Item GRAVITY_SWORD = registerItem("gravity_sword", new GravitySword(ToolMaterials.IRON, 3, -2.4F,  new FabricItemSettings()));

    //Adds items to existing creative mode tab
    private static void addItemsToIngredientItemGroup(FabricItemGroupEntries entries) {

    }

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(SpaceMod.MOD_ID, name), item);
    }

    public static void registerModItems() {
        SpaceMod.LOGGER.info("Registering Mod Items for " + SpaceMod.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(ModItems::addItemsToIngredientItemGroup);
    }
}
