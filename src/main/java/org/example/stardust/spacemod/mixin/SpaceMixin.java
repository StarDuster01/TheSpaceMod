package org.example.stardust.spacemod.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class SpaceMixin {
    @Inject(at = @At("Head"), method = "loadWorld")
    private void init(CallbackInfo info) {
        //Injected at start of server world
    }
}