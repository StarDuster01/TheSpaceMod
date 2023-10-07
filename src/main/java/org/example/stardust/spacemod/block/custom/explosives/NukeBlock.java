package org.example.stardust.spacemod.block.custom.explosives;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.example.stardust.spacemod.block.entity.explosives.NukeEntity;
import org.example.stardust.spacemod.block.entity.explosives.TntXEntity;
import org.jetbrains.annotations.Nullable;
import org.example.stardust.spacemod.block.entity.explosives.NukeEntity;

public class NukeBlock extends Block {

    public NukeBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (world.isReceivingRedstonePower(pos)) {
            primeNuke(world, pos, null);
            world.removeBlock(pos, false);
        }
    }

    // other necessary overrides for interaction, like onUse, onBreak, etc.


    private static void primeNuke(World world, BlockPos pos, @Nullable LivingEntity igniter) {
        if (world.isClient) {
            return;
        }
        NukeEntity nukeEntity = new NukeEntity(world, (double)pos.getX() +0.5, pos.getY(), (double)pos.getZ() +0.5, igniter);
        world.spawnEntity(nukeEntity); //
        world.playSound(null, nukeEntity.getX(), nukeEntity.getY(), nukeEntity.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0f, 1.0f);
        world.emitGameEvent(igniter, GameEvent.PRIME_FUSE, pos);
    }
}

