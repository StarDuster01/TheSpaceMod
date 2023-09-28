package org.example.stardust.spacemod.networking.packet;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class ExampleC2SPacket {
    public static void receive(MinecraftServer server, ServerPlayerEntity player,
                               ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        server.execute(() -> { // Execute on the main server thread
            if(player.getWorld() instanceof ServerWorld) {
                EntityType.COW.spawn(
                        (ServerWorld) player.getWorld(),
                        null,
                        null,
                        player.getBlockPos(), // You might want to set a proper position
                        SpawnReason.COMMAND,
                        false,
                        false);
            }
        });
    }
}

