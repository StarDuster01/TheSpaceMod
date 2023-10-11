package org.example.stardust.spacemod.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.example.stardust.spacemod.SpaceMod;
import org.example.stardust.spacemod.block.ModBlocks;

public class ModItemGroups {

    // Creative Tab
    public static final ItemGroup Space_Group = Registry.register(Registries.ITEM_GROUP,
            new Identifier(SpaceMod.MOD_ID, "space"),
            FabricItemGroup.builder().displayName(Text.translatable("itemgroup.space"))
                    .icon(() -> new ItemStack(ModItems.BLOOD)).entries((displayContext, entries) -> {
                        entries.add(ModItems.BLOOD);
                        entries.add(ModItems.PURE_BLOOD);
                        entries.add(ModItems.GALLIUM_INGOT);
                        entries.add(ModItems.GRAVITY_SWORD);
                        entries.add(ModItems.GLASS_SWORD);
                        entries.add(ModItems.FROST_WAND);
                        entries.add(ModItems.VIEW_FINDER);
                        entries.add(ModItems.CLOAKING_DEVICE_ITEM);
                        entries.add(ModItems.SACRIFICIAL_KNIFE);
                        entries.add(ModItems.UNICORN_SPAWN_EGG);
                        entries.add(ModItems.FORMIC_SPAWN_EGG);
                        entries.add(ModItems.GRIFFIN_SPAWN_EGG);
                        entries.add(ModItems.GIANT_SALAMANDER_SPAWN_EGG);
                        entries.add(ModBlocks.BLOODY_STONE);
                        entries.add(ModBlocks.NUKE_BLOCK);
                        entries.add(ModBlocks.ALTAR_BLOCK);
                        entries.add(ModBlocks.CANNON_BLOCK);
                        entries.add(ModBlocks.SPEED_BLOCK);
                        entries.add(ModBlocks.DOOM_FURNACE_BLOCK);
                        entries.add(ModBlocks.COAL_GENERATOR_BLOCK);
                        entries.add(ModBlocks.TNTX_BLOCK);
                        entries.add(ModBlocks.CUBE_DIGGER_TNT_BLOCK);
                        entries.add(ModBlocks.ZOMBIE_TNT_BLOCK);
                        entries.add(ModBlocks.EXCAVATOR_BLOCK);
                        entries.add(ModBlocks.WALLPLACER);
                        entries.add(ModBlocks.FUSION_REACTOR_BLOCK);
                        entries.add(ModBlocks.MINING_BORE_BLOCK);
                        entries.add(ModBlocks.RANGE_SPAWNER_BLOCK);





                    }).build());


    public static void registerItemGroups() {
        SpaceMod.LOGGER.info("Registering Item Groups for " + SpaceMod.MOD_ID);
    }
}