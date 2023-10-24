package org.example.stardust.spacemod.networking;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.example.stardust.spacemod.SpaceMod;
import org.example.stardust.spacemod.block.entity.*;
import org.example.stardust.spacemod.entity.custom.GriffinEntity;
import org.example.stardust.spacemod.networking.packet.ExampleC2SPacket;
import org.example.stardust.spacemod.screen.IronGeneratorScreen;
import org.joml.Vector2i;

public class ModMessages {

    public static final Identifier GRIFFIN_MOVEMENT_ID = new Identifier(SpaceMod.MOD_ID, "griffin_movement");
    public static final Identifier TOGGLE_MINING_ID = new Identifier(SpaceMod.MOD_ID, "toggle_mining");
    public static final Identifier POWER_CORE_UNLOCK_ID = new Identifier(SpaceMod.MOD_ID, "power_core_unlock_command");
    public static final Identifier TOGGLE_RANGE_SPAWNER_ID = new Identifier(SpaceMod.MOD_ID, "toggle_range_spawner");
    public static final Identifier TOGGLE_BORING_ID = new Identifier(SpaceMod.MOD_ID, "toggle_boring");
    public static final Identifier CHANGE_BORING_AREA_ID = new Identifier(SpaceMod.MOD_ID, "change_boring_area");
    public static final Identifier CHANGE_GENERATED_RESOURCE_ID = new Identifier(SpaceMod.MOD_ID, "change_resource");
    public static final Identifier IRON_GENERATOR_SYNC_ID = new Identifier(SpaceMod.MOD_ID, "iron_generator_sync");

    public static final Identifier EXCAVATOR_UPDATE_ID = new Identifier(SpaceMod.MOD_ID, "excavator_update");
    public static final Identifier IRON_GENERATOR_UPDATE_ID = new Identifier(SpaceMod.MOD_ID, "iron_generator_update");
    public static final Identifier WALL_PLACER_UPDATE_ID = new Identifier(SpaceMod.MOD_ID, "wall_placer_update");
    public static final Identifier TOGGLE_WALL_PLACING_ID = new Identifier(SpaceMod.MOD_ID, "toggle_wall_placing");
    public static final Identifier PLACE_WALL_ID = new Identifier(SpaceMod.MOD_ID, "place_wall");
    public static final Identifier PLACE_TOWER_ID = new Identifier(SpaceMod.MOD_ID, "place_tower");
    public static final Identifier CHANGE_MINING_AREA_ID = new Identifier(SpaceMod.MOD_ID, "change_mining_area");
    public static final Identifier EXCAVATOR_AREA_UPDATE_ID = new Identifier(SpaceMod.MOD_ID, "excavator_area_update");
    public static final Identifier MINING_BORE_UPDATE_ID = new Identifier(SpaceMod.MOD_ID, "mining_bore_update");
    public static final Identifier MINING_BORE_AREA_UPDATE_ID = new Identifier(SpaceMod.MOD_ID, "mining_bore_area_update");
    public static final Identifier RANGE_SPAWNER_UPDATE_ID = new Identifier(SpaceMod.MOD_ID, "range_spawner_update");

    public static final Identifier DELETE_RANGE_SPAWNER_ID = new Identifier(SpaceMod.MOD_ID, "delete_range_spawner");
    public static final Identifier SELF_DESTRUCT_ID = new Identifier(SpaceMod.MOD_ID, "self_destruct_command");
    public static final Identifier AIRSTRIKE_ID = new Identifier(SpaceMod.MOD_ID, "air_strike_command");






    public static void sendMiningBoreUpdate(ServerPlayerEntity player, BlockPos pos, long energy, boolean isMiningActive) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(pos);
        buf.writeLong(energy);
        buf.writeBoolean(isMiningActive);
        ServerPlayNetworking.send(player, MINING_BORE_UPDATE_ID, buf);
    }
    public static void sendPowerCoreUnlockCommand(ClientPlayerEntity player, BlockPos pos) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(pos);
        ClientPlayNetworking.send(POWER_CORE_UNLOCK_ID, buf);
    }
    public static void sendMiningBoreAreaUpdate(ServerPlayerEntity player, BlockPos pos, Vector2i dimensions) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(pos);
        buf.writeInt(dimensions.x);
        buf.writeInt(dimensions.y);
        ServerPlayNetworking.send(player, MINING_BORE_AREA_UPDATE_ID, buf);
    }
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
    public static void sendIronGeneratorUpdate(ServerPlayerEntity player, BlockPos pos, long energy, boolean isGeneratorActive, int resourceType) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(pos);
        buf.writeLong(energy);
        buf.writeBoolean(isGeneratorActive);
        buf.writeInt(resourceType);
        ServerPlayNetworking.send(player, IRON_GENERATOR_UPDATE_ID, buf);
    }

    public static void sendWallPlacerUpdate(ServerPlayerEntity player, BlockPos pos, long energy, boolean isPlacingActive) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(pos);
        buf.writeLong(energy);
        buf.writeBoolean(isPlacingActive);
        ServerPlayNetworking.send(player, WALL_PLACER_UPDATE_ID, buf);
    }
    public static void sendRangeSpawnerUpdate(ServerPlayerEntity player, BlockPos pos, long energy) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(pos);
        buf.writeLong(energy);
        ServerPlayNetworking.send(player, RANGE_SPAWNER_UPDATE_ID, buf);
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
        ClientPlayNetworking.registerGlobalReceiver(IRON_GENERATOR_SYNC_ID, (client, player, buf, sender) -> {
            BlockPos blockPos = buf.readBlockPos();
            int blockTypeIndex = buf.readInt();

            client.execute(() -> {
                World clientWorld = MinecraftClient.getInstance().world;
                if (clientWorld != null) {
                    BlockEntity blockEntity = clientWorld.getBlockEntity(blockPos);

                }
            });
        });


        ClientPlayNetworking.registerGlobalReceiver(IRON_GENERATOR_UPDATE_ID, (client, player, buf, sender) -> {
            BlockPos blockPos = buf.readBlockPos();
            long energy = buf.readLong();
            boolean generatorActive = buf.readBoolean();

            client.execute(() -> {
                World clientWorld = MinecraftClient.getInstance().world;
                if (clientWorld != null) {
                    BlockEntity blockEntity = clientWorld.getBlockEntity(blockPos);
                    if (blockEntity instanceof IronGeneratorBlockEntity) {
                        ((IronGeneratorBlockEntity) blockEntity).energyStorage.setAmountDirectly(energy);
                        ((IronGeneratorBlockEntity) blockEntity).setGeneratorActive(generatorActive);
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
        ServerPlayNetworking.registerGlobalReceiver(ModMessages.DELETE_RANGE_SPAWNER_ID, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            server.execute(() -> {
                World world = player.getEntityWorld();
                if (world.getBlockEntity(pos) instanceof RangeSpawnerBlockEntity) {
                    world.removeBlock(pos, false); // Deletes the block. Set the second parameter to true if you want to drop the block as an item.
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(POWER_CORE_UNLOCK_ID, (server, player, handler, buf, responseSender) -> {
            BlockPos blockPos = buf.readBlockPos();
            server.execute(() -> {
                BlockEntity blockEntity = player.getWorld().getBlockEntity(blockPos);
                if (blockEntity instanceof AlienPowerCoreBlockEntity) {
                    ((AlienPowerCoreBlockEntity) blockEntity).unlock(player);
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
        ServerPlayNetworking.registerGlobalReceiver(TOGGLE_RANGE_SPAWNER_ID, (server, player, handler, buf, sender) -> {
            BlockPos blockPos = buf.readBlockPos();
            server.execute(() -> {
                BlockEntity blockEntity = player.getWorld().getBlockEntity(blockPos);
                if (blockEntity instanceof RangeSpawnerBlockEntity) {
                    RangeSpawnerBlockEntity rangeSpawner = (RangeSpawnerBlockEntity) blockEntity;
                    rangeSpawner.isPowered();
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(SELF_DESTRUCT_ID, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            server.execute(() -> {
                World world = player.getEntityWorld();
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof RangeSpawnerBlockEntity) {
                    ((RangeSpawnerBlockEntity) blockEntity).spawnTNTOnTop();
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
        ClientPlayNetworking.registerGlobalReceiver(MINING_BORE_UPDATE_ID, (client, player, buf, sender) -> {
            BlockPos blockPos = buf.readBlockPos();
            long energy = buf.readLong();
            boolean miningActive = buf.readBoolean();

            client.execute(() -> {
                World clientWorld = MinecraftClient.getInstance().world;
                if (clientWorld != null) {
                    BlockEntity blockEntity = clientWorld.getBlockEntity(blockPos);
                    if (blockEntity instanceof MiningBoreBlockEntity) {
                        ((MiningBoreBlockEntity) blockEntity).energyStorage.setAmountDirectly(energy);
                        ((MiningBoreBlockEntity) blockEntity).setMiningActive(miningActive);
                    }
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(RANGE_SPAWNER_UPDATE_ID, (client, player, buf, sender) -> {
            BlockPos blockPos = buf.readBlockPos();
            long energy = buf.readLong();
            client.execute(() -> {
                World clientWorld = MinecraftClient.getInstance().world;
                if (clientWorld != null) {
                    BlockEntity blockEntity = clientWorld.getBlockEntity(blockPos);
                    if (blockEntity instanceof RangeSpawnerBlockEntity) {
                        ((RangeSpawnerBlockEntity) blockEntity).energyStorage.setAmountDirectly(energy);
                    }
                }
            });
        });



        ClientPlayNetworking.registerGlobalReceiver(MINING_BORE_AREA_UPDATE_ID, (client, player, buf, sender) -> {
            BlockPos blockPos = buf.readBlockPos();
            Vector2i dimensions = new Vector2i(buf.readInt(), buf.readInt());

            client.execute(() -> {
                World clientWorld = MinecraftClient.getInstance().world;
                if (clientWorld != null) {
                    BlockEntity blockEntity = clientWorld.getBlockEntity(blockPos);
                    if (blockEntity instanceof MiningBoreBlockEntity) {
                        ((MiningBoreBlockEntity) blockEntity).setMiningAreaDimensions(dimensions);
                    }
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(TOGGLE_BORING_ID, (server, player, handler, buf, sender) -> {
            BlockPos blockPos = buf.readBlockPos();
            server.execute(() -> {
                BlockEntity blockEntity = player.getWorld().getBlockEntity(blockPos);
                if (blockEntity instanceof MiningBoreBlockEntity) {
                    ((MiningBoreBlockEntity) blockEntity).setMiningActive(!((MiningBoreBlockEntity) blockEntity).isMiningActive());
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(AIRSTRIKE_ID, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos(); // This is the position of the block entity
            int x = buf.readInt();
            int z = buf.readInt();
            int fuseTime = buf.readInt();

            server.execute(() -> {
                World world = player.getEntityWorld();
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof RangeSpawnerBlockEntity) {
                    ((RangeSpawnerBlockEntity) blockEntity).airStrike(world, new BlockPos(x, pos.getY(), z), fuseTime);
                }
            });
        });


        ServerPlayNetworking.registerGlobalReceiver(CHANGE_BORING_AREA_ID, (server, player, handler, buf, responseSender) -> {
            BlockPos blockPos = buf.readBlockPos();
            Vector2i dimensions = new Vector2i(buf.readInt(), buf.readInt());
            server.execute(() -> {
                BlockEntity blockEntity = player.getWorld().getBlockEntity(blockPos);
                if (blockEntity instanceof MiningBoreBlockEntity) {
                    ((MiningBoreBlockEntity) blockEntity).setMiningAreaDimensions(dimensions);
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(CHANGE_GENERATED_RESOURCE_ID, (server, player, handler, buf, responseSender) -> {
            BlockPos blockPos = buf.readBlockPos();
            int resourceType = buf.readInt();
            server.execute(() -> {
                BlockEntity blockEntity = player.getWorld().getBlockEntity(blockPos);
                if (blockEntity instanceof IronGeneratorBlockEntity) {
                    ((IronGeneratorBlockEntity) blockEntity).setCurrentResourceType(resourceType);

                }
            });
        });









    }







    public static void registerS2CPackets() {


    }

}

