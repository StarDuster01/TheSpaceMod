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

        // The explosion size is 4, so we iterate from -4 to +4 in all three dimensions
        int explosionSize = 4;
        for (int x = -explosionSize; x <= explosionSize; x++) {
            for (int y = -explosionSize; y <= explosionSize; y++) {
                for (int z = -explosionSize; z <= explosionSize; z++) {
                    BlockPos blockPos = explosionPos.add(x, y, z);
                    BlockState blockState = world.getBlockState(blockPos);
                    Block block = blockState.getBlock();

                    // Check if the block is not air and is breakable
                    if (!block.isTransparent(blockState, world, blockPos) && block != Blocks.BEDROCK) {
                        // Drop the block as an item
                        block.dropStacks(blockState, world, blockPos, null, null, ItemStack.EMPTY);

                        // Remove the block from the world
                        world.removeBlock(blockPos, false);
                    }
                }
            }
        }

        // Create the explosion
        world.createExplosion(this, this.getX(), this.getBodyY(0.0625D), this.getZ(), 0.0F, World.ExplosionSourceType.BLOCK);
    }

}
