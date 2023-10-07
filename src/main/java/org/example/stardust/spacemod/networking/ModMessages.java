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
import org.example.stardust.spacemod.block.entity.WallPlacerBlockEntity;
import org.example.stardust.spacemod.entity.custom.GriffinEntity;
import org.example.stardust.spacemod.networking.packet.ExampleC2SPacket;
import org.joml.Vector2i;

public class ModMessages {

    public static final Identifier GRIFFIN_MOVEMENT_ID = new Identifier(SpaceMod.MOD_ID, "griffin_movement");
    public static final Identifier TOGGLE_MINING_ID = new Identifier(SpaceMod.MOD_ID, "toggle_mining");

    public static final Identifier EXCAVATOR_UPDATE_ID = new Identifier(SpaceMod.MOD_ID, "excavator_update");
    public static final Identifier WALL_PLACER_UPDATE_ID = new Identifier(SpaceMod.MOD_ID, "wall_placer_update");
    public static final Identifier TOGGLE_WALL_PLACING_ID = new Identifier(SpaceMod.MOD_ID, "toggle_wall_placing");
    public static final Identifier PLACE_WALL_ID = new Identifier(SpaceMod.MOD_ID, "place_wall");
    public static final Identifier PLACE_TOWER_ID = new Identifier(SpaceMod.MOD_ID, "place_tower");
    public static final Identifier CHANGE_MINING_AREA_ID = new Identifier(SpaceMod.MOD_ID, "change_mining_area");
    public static final Identifier EXCAVATOR_AREA_UPDATE_ID = new Identifier(SpaceMod.MOD_ID, "excavator_area_update");





    public static void sendExcavatorUpdate(ServerPlayerEntity player, BlockPos pos, long energy, boolean isMiningActive) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(pos);
        buf.writeLong(energy);
        buf.writeBoolean(isMiningActive);
        ServerPlayNetworking.send(player, EXCAVATOR_UPDATE_ID, buf);
    }
    public static void sendExcavatorAreaUpdate(ServerPlayerEntity player, BlockPos pos, Vector2i dimensions) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(pos);
        buf.writeInt(dimensions.x);
        buf.writeInt(dimensions.y);
        ServerPlayNetworking.send(player, EXCAVATOR_AREA_UPDATE_ID, buf);
    }

    public static void sendWallPlacerUpdate(ServerPlayerEntity player, BlockPos pos, long energy, boolean isPlacingActive) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(pos);
        buf.writeLong(energy);
        buf.writeBoolean(isPlacingActive);
        ServerPlayNetworking.send(player, WALL_PLACER_UPDATE_ID, buf);
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

        ServerPlayNetworking.registerGlobalReceiver(TOGGLE_WALL_PLACING_ID, (server, player, handler, buf, sender) -> {
            BlockPos blockPos = buf.readBlockPos();
            server.execute(() -> {
                BlockEntity blockEntity = player.getWorld().getBlockEntity(blockPos);
                if (blockEntity instanceof WallPlacerBlockEntity) {
                    ((WallPlacerBlockEntity) blockEntity).setPlacingActive(!((WallPlacerBlockEntity) blockEntity).isPlacingActive());
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(WALL_PLACER_UPDATE_ID, (client, player, buf, sender) -> {
            BlockPos blockPos = buf.readBlockPos();
            long energy = buf.readLong();
            boolean placingActive = buf.readBoolean();

            client.execute(() -> {
                World clientWorld = MinecraftClient.getInstance().world;
                if (clientWorld != null) {
                    BlockEntity blockEntity = clientWorld.getBlockEntity(blockPos);
                    if (blockEntity instanceof WallPlacerBlockEntity) {
                        ((WallPlacerBlockEntity) blockEntity).energyStorage.setAmountDirectly(energy);
                        ((WallPlacerBlockEntity) blockEntity).setPlacingActive(placingActive);
                    }
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(PLACE_WALL_ID, (server, player, handler, buf, sender) -> {
            BlockPos blockPos = buf.readBlockPos();
            server.execute(() -> {
                BlockEntity blockEntity = player.getWorld().getBlockEntity(blockPos);
                if (blockEntity instanceof WallPlacerBlockEntity) {
                    ((WallPlacerBlockEntity) blockEntity).placeWall();
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(PLACE_TOWER_ID, (server, player, handler, buf, sender) -> {
            BlockPos blockPos = buf.readBlockPos();
            server.execute(() -> {
                BlockEntity blockEntity = player.getWorld().getBlockEntity(blockPos);
                if (blockEntity instanceof WallPlacerBlockEntity) {
                    ((WallPlacerBlockEntity) blockEntity).placeTower();
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(ModMessages.CHANGE_MINING_AREA_ID, (server, player, handler, buf, responseSender) -> {
            BlockPos blockPos = buf.readBlockPos();
            Vector2i dimensions = new Vector2i(buf.readInt(), buf.readInt());
            server.execute(() -> {
                BlockEntity blockEntity = player.getWorld().getBlockEntity(blockPos);
                if (blockEntity instanceof ExcavatorBlockEntity) {
                    ((ExcavatorBlockEntity) blockEntity).setMiningAreaDimensions(dimensions);
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(EXCAVATOR_AREA_UPDATE_ID, (client, player, buf, sender) -> {
            BlockPos blockPos = buf.readBlockPos();
            Vector2i dimensions = new Vector2i(buf.readInt(), buf.readInt());

            client.execute(() -> {
                World clientWorld = MinecraftClient.getInstance().world;
                if (clientWorld != null) {
                    BlockEntity blockEntity = clientWorld.getBlockEntity(blockPos);
                    if (blockEntity instanceof ExcavatorBlockEntity) {
                        ((ExcavatorBlockEntity) blockEntity).setMiningAreaDimensions(dimensions);
                    }
                }
            });
        });



    }







    public static void registerS2CPackets() {


    }
}

