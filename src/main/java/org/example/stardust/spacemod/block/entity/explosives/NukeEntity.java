package org.example.stardust.spacemod.block.entity.explosives;

import blue.endless.jankson.annotation.Nullable;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NukeEntity extends TntEntity {
    public NukeEntity(World world, double x, double y, double z, @Nullable LivingEntity igniter) {
        super(world, x, y, z, igniter);
        this.setFuse(80);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getFuse() <= 0) {
            this.explode();
            this.remove(RemovalReason.DISCARDED);
        }
    }

    protected void explode() {
        World world = this.getWorld();
        BlockPos explosionCenter = new BlockPos((int) this.getX(), (int) this.getBodyY(0.0625D), (int) this.getZ());
        int radius = 64;  // Set the radius of the sphere of destruction

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos currentPos = explosionCenter.add(x, y, z);
                    double distanceSquared = explosionCenter.getSquaredDistance(currentPos);
                    if (distanceSquared <= radius * radius) {  // Inside the sphere
                        world.setBlockState(currentPos, Blocks.AIR.getDefaultState(), 2 | 16);
                    }
                }
            }
        }
    }
}
