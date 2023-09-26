package org.example.stardust.spacemod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import org.example.stardust.spacemod.entity.ModEntities;
import org.example.stardust.spacemod.entity.client.UnicornRenderer;

public class SpaceModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Client Specific Logic Here

        EntityRendererRegistry.register(ModEntities.UNICORN, UnicornRenderer::new);

    }
}
