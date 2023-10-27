package org.example.stardust.spacemod.block.entity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;

import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.Nullable;

public class ConductorBlockEntity extends BlockEntity {

    public ConductorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CONDUCTOR_BE, pos, state);
    }
    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

}
