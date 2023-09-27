package org.example.stardust.spacemod;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import org.example.stardust.spacemod.block.ModBlocks;
import org.example.stardust.spacemod.block.entity.ModBlockEntities;
import org.example.stardust.spacemod.entity.ModEntities;
import org.example.stardust.spacemod.entity.custom.FormicEntity;
import org.example.stardust.spacemod.entity.custom.UnicornEntity;
import org.example.stardust.spacemod.item.ModItemGroups;
import org.example.stardust.spacemod.item.ModItems;
import org.example.stardust.spacemod.recipe.ModRecipes;
import org.example.stardust.spacemod.screen.ModScreenHandlers;
import org.example.stardust.spacemod.world.gen.ModWorldGeneration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class SpaceMod implements ModInitializer{
    public static final String MOD_ID = "spacemod";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    @Override
    public void onInitialize() {
        ModItemGroups.registerItemGroups();
        ModItems.registerModItems();
        ModBlocks.registerModBlocks();
        ModWorldGeneration.generateModWorldGen();
        ModBlockEntities.registerBlockEntities();
        ModScreenHandlers.registerScreenHandler();
        ModRecipes.registerRecipes();

        FabricDefaultAttributeRegistry.register(ModEntities.UNICORN, UnicornEntity.setAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.FORMIC, FormicEntity.setAttributes());




    }
}
