package org.example.stardust.spacemod.networking;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.example.stardust.spacemod.SpaceMod;
import org.example.stardust.spacemod.block.entity.CoalGeneratorBlockEntity;
import org.example.stardust.spacemod.block.entity.ExcavatorBlockEntity;
import org.example.stardust.spacemod.entity.custom.GriffinEntity;
import org.example.stardust.spacemod.networking.packet.ExampleC2SPacket;

public class ModMessages {

    public static final Identifier GRIFFIN_MOVEMENT_ID = new Identifier(SpaceMod.MOD_ID, "griffin_movement");
    public static final Identifier TOGGLE_MINING_ID = new Identifier(SpaceMod.MOD_ID, "toggle_mining");

    public static final Identifier EXCAVATOR_UPDATE_ID = new Identifier(SpaceMod.MOD_ID, "excavator_update");

    public static void sendExcavatorUpdate(ServerPlayerEntity player, BlockPos pos, long energy, boolean isMiningActive) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(pos);
        buf.writeLong(energy);
        buf.writeBoolean(isMiningActive);
        ServerPlayNetworking.send(player, EXCAVATOR_UPDATE_ID, buf);
    }






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
        ServerPlayNetworking.registerGlobalReceiver(TOGGLE_MINING_ID, (server, player, handler, buf, sender) -> {
            BlockPos blockPos = buf.readBlockPos();
            server.execute(() -> {
                BlockEntity blockEntity = player.getWorld().getBlockEntity(blockPos);
                if (blockEntity instanceof ExcavatorBlockEntity) {
                    ((ExcavatorBlockEntity) blockEntity).setMiningActive(!((ExcavatorBlockEntity) blockEntity).isMiningActive());
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(EXCAVATOR_UPDATE_ID, (client, player, buf, sender) -> {
            BlockPos blockPos = buf.readBlockPos();
            long energy = buf.readLong();
            boolean miningActive = buf.readBoolean();

            client.execute(() -> {
                World clientWorld = MinecraftClient.getInstance().world;
                if (clientWorld != null) {
                    BlockEntity blockEntity = clientWorld.getBlockEntity(blockPos);
                    if (blockEntity instanceof ExcavatorBlockEntity) {
                        ((ExcavatorBlockEntity) blockEntity).energyStorage.setAmountDirectly(energy);
                        ((ExcavatorBlockEntity) blockEntity).setMiningActive(miningActive);
                    }
                }
            });
        });





    }

    public static void registerS2CPackets() {


    }
}

