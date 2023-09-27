package org.example.stardust.spacemod.block.custom.explosives;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.example.stardust.spacemod.block.entity.explosives.CubeDiggerTntEntity;
import org.jetbrains.annotations.Nullable;

public class CubeDiggerTntBlock extends Block {

    public CubeDiggerTntBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (world.isReceivingRedstonePower(pos)) {
            primeCubeDiggerTnt(world, pos, null);
            world.removeBlock(pos, false);
        }
    }

    // other necessary overrides for interaction, like onUse, onBreak, etc.


    private static void primeCubeDiggerTnt(World world, BlockPos pos, @Nullable LivingEntity igniter) {
        if (world.isClient) {
            return;
        }
        CubeDiggerTntEntity cubeDiggerTntEntity = new CubeDiggerTntEntity(world, (double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5, igniter);
        world.spawnEntity(cubeDiggerTntEntity); //
        world.playSound(null, cubeDiggerTntEntity.getX(), cubeDiggerTntEntity.getY(), cubeDiggerTntEntity.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0f, 1.0f);
        world.emitGameEvent(igniter, GameEvent.PRIME_FUSE, pos);
    }
}

