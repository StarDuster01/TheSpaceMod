package org.example.stardust.spacemod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import org.example.stardust.spacemod.entity.ModEntities;
import org.example.stardust.spacemod.entity.client.GriffinRenderer;
import org.example.stardust.spacemod.entity.client.UnicornRenderer;
import org.example.stardust.spacemod.entity.client.FormicRenderer;
import org.example.stardust.spacemod.entity.custom.GriffinEntity;
import org.example.stardust.spacemod.event.KeyInputHandler;
import org.example.stardust.spacemod.networking.ModMessages;
import org.example.stardust.spacemod.screen.CoalGeneratorScreenHandler;
import org.example.stardust.spacemod.screen.DoomFurnaceScreen;
import org.example.stardust.spacemod.screen.CoalGeneratorScreen;
import org.example.stardust.spacemod.screen.ModScreenHandlers;

public class SpaceModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Client Specific Logic Here

        EntityRendererRegistry.register(ModEntities.UNICORN, UnicornRenderer::new);
        EntityRendererRegistry.register(ModEntities.FORMIC, FormicRenderer::new);
        EntityRendererRegistry.register(ModEntities.GRIFFIN, GriffinRenderer::new);


        HandledScreens.register(ModScreenHandlers.DOOM_FURNACE_SCREEN_HANDLER, DoomFurnaceScreen::new);
        HandledScreens.register(ModScreenHandlers.COAL_GENERATOR_SCREEN_HANDLER, CoalGeneratorScreen::new);

        ModMessages.registerS2CPackets();
        KeyInputHandler.register();






    }
}
