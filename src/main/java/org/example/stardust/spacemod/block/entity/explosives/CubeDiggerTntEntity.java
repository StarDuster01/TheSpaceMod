package org.example.stardust.spacemod.block.entity.explosives;

import blue.endless.jankson.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CubeDiggerTntEntity extends TntEntity {
    public CubeDiggerTntEntity(World world, double x, double y, double z, @Nullable LivingEntity igniter) {
        super(world, x, y, z, igniter);
        this.setFuse(80);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getFuse() <= 0) {
            this.explodincube();
            this.remove(RemovalReason.DISCARDED);
        }
    }

    private void explodincube() {
        World world = this.getWorld();
        BlockPos explosionPos = this.getBlockPos();
        int explosionSize = 4;
        for (int x = -explosionSize; x <= explosionSize; x++) {
            for (int y = -explosionSize; y <= explosionSize; y++) {
                for (int z = -explosionSize; z <= explosionSize; z++) {
                    BlockPos blockPos = explosionPos.add(x, y, z);
                    BlockState blockState = world.getBlockState(blockPos);
                    Block block = blockState.getBlock();
                    if (!block.isTransparent(blockState, world, blockPos) && block != Blocks.BEDROCK) {
                        block.dropStacks(blockState, world, blockPos, null, null, ItemStack.EMPTY);
                        world.removeBlock(blockPos, false);
                    }
                }
            }
        }
        world.createExplosion(this, this.getX(), this.getBodyY(0.0625D), this.getZ(), 0.0F, World.ExplosionSourceType.BLOCK);
    }

}
