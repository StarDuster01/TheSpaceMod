package org.example.stardust.spacemod;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.impl.biome.modification.BuiltInRegistryKeys;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;

import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import org.example.stardust.spacemod.block.ModBlocks;
import org.example.stardust.spacemod.block.entity.ModBlockEntities;
import org.example.stardust.spacemod.entity.ModEntities;
import org.example.stardust.spacemod.entity.custom.FormicEntity;
import org.example.stardust.spacemod.entity.custom.GiantSalamanderEntity;
import org.example.stardust.spacemod.entity.custom.GriffinEntity;
import org.example.stardust.spacemod.entity.custom.UnicornEntity;
import org.example.stardust.spacemod.item.ModItemGroups;
import org.example.stardust.spacemod.item.ModItems;
import org.example.stardust.spacemod.networking.ModMessages;
import org.example.stardust.spacemod.recipe.ModRecipes;
import org.example.stardust.spacemod.screen.ModScreenHandlers;
import org.example.stardust.spacemod.sounds.ModSounds;
import org.example.stardust.spacemod.util.ModRegistries;
import org.example.stardust.spacemod.world.gen.ModTrunkPlacers;
import org.example.stardust.spacemod.world.gen.ModWorldGeneration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public class SpaceMod implements ModInitializer{
    public static final String MOD_ID = "spacemod";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Identifier GIANT_JUNGLE_TREE_ID = new Identifier("spacemod", "giant_jungle_tree");

    @Override
    public void onInitialize() {
        ModItemGroups.registerItemGroups();
        ModItems.registerModItems();
        ModBlocks.registerModBlocks();
        ModWorldGeneration.generateModWorldGeneration();
        ModBlockEntities.registerBlockEntities();
        ModScreenHandlers.registerScreenHandler();
        ModRecipes.registerRecipes();
        ModRegistries.registerModStuffs();
        ModSounds.registerSounds();
        ModTrunkPlacers.init();


        FabricDefaultAttributeRegistry.register(ModEntities.UNICORN, UnicornEntity.setAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.FORMIC, FormicEntity.setAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.GRIFFIN, GriffinEntity.setAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.GIANT_SALAMANDER, GiantSalamanderEntity.setAttributes());






        ModMessages.registerC2SPackets();




    }


}
