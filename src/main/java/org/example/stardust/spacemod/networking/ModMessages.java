package org.example.stardust.spacemod.networking;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.example.stardust.spacemod.SpaceMod;
import org.example.stardust.spacemod.entity.custom.GriffinEntity;
import org.example.stardust.spacemod.networking.packet.ExampleC2SPacket;

public class ModMessages {

    public static final Identifier EXAMPLE_ID = new Identifier(SpaceMod.MOD_ID, "example");
    public static final Identifier GRIFFIN_MOVEMENT_ID = new Identifier(SpaceMod.MOD_ID, "griffin_movement");
        public static void registerC2SPackets() {
            ServerPlayNetworking.registerGlobalReceiver(GRIFFIN_MOVEMENT_ID, (server, player, handler, buf, sender) -> {
                double verticalSpeed = buf.readDouble();
                server.execute(() -> {
                    Entity entity = player.getVehicle();
                    if (entity instanceof GriffinEntity) {
                        GriffinEntity griffin = (GriffinEntity) entity;
                        griffin.setVelocity(griffin.getVelocity().x, verticalSpeed, griffin.getVelocity().z);
                    }
                });
            });
        }

    public static void registerS2CPackets() {


    }
}
