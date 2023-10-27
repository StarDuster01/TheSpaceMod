package org.example.stardust.spacemod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import org.example.stardust.spacemod.block.ModBlocks;
import org.example.stardust.spacemod.entity.ModEntities;
import org.example.stardust.spacemod.entity.client.GiantSalamanderRenderer;
import org.example.stardust.spacemod.entity.client.GriffinRenderer;
import org.example.stardust.spacemod.entity.client.UnicornRenderer;
import org.example.stardust.spacemod.entity.client.FormicRenderer;
import org.example.stardust.spacemod.entity.custom.GriffinEntity;
import org.example.stardust.spacemod.event.KeyInputHandler;
import org.example.stardust.spacemod.networking.ModMessages;
import org.example.stardust.spacemod.screen.*;

public class SpaceModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Client Specific Logic Here

        EntityRendererRegistry.register(ModEntities.UNICORN, UnicornRenderer::new);
        EntityRendererRegistry.register(ModEntities.FORMIC, FormicRenderer::new);
        EntityRendererRegistry.register(ModEntities.GRIFFIN, GriffinRenderer::new);
        EntityRendererRegistry.register(ModEntities.GIANT_SALAMANDER, GiantSalamanderRenderer::new);
        EntityRendererRegistry.register(ModEntities.MINING_EXPLOSIVE_ENTITY_ENTITY_TYPE, (context) -> new FlyingItemEntityRenderer(context));


        HandledScreens.register(ModScreenHandlers.DOOM_FURNACE_SCREEN_HANDLER, DoomFurnaceScreen::new);
        HandledScreens.register(ModScreenHandlers.COAL_GENERATOR_SCREEN_HANDLER, CoalGeneratorScreen::new);
        HandledScreens.register(ModScreenHandlers.MEDIUM_COAL_GENERATOR_SCREEN_HANDLER, MediumCoalGeneratorScreen::new);
        HandledScreens.register(ModScreenHandlers.EXCAVATOR_SCREEN_HANDLER, ExcavatorScreen::new);
        HandledScreens.register(ModScreenHandlers.IRON_GENERATOR_SCREEN_HANDLER, IronGeneratorScreen::new);
        HandledScreens.register(ModScreenHandlers.MINING_BORE_SCREEN_HANDLER, MiningBoreScreen::new);
        HandledScreens.register(ModScreenHandlers.WALLPLACER_SCREEN_HANDLER, WallPlacerScreen::new);
        HandledScreens.register(ModScreenHandlers.FUSION_REACTOR_SCREEN_HANDLER, FusionReactorScreen::new);
        HandledScreens.register(ModScreenHandlers.ALIEN_POWER_CORE_SCREEN_HANDLER, AlienPowerCoreScreen::new);

        HandledScreens.register(ModScreenHandlers.RANGE_SPAWNER_SCREEN_HANDLER, RangeSpawnerScreen::new);


        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.CONDUCTOR_BLOCK, RenderLayer.getCutoutMipped());


        ModMessages.registerS2CPackets();
        KeyInputHandler.register();






    }
}
