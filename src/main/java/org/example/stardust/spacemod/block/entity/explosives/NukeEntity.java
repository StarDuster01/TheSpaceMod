package org.example.stardust.spacemod.block.entity.explosives;

import blue.endless.jankson.annotation.Nullable;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.TntEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NukeEntity extends TntEntity {
    private int currentRadius = 1;
    private boolean exploded = false;
    private static final int MAX_RADIUS = 64;

    public NukeEntity(World world, double x, double y, double z, @Nullable LivingEntity igniter) {
        super(world, x, y, z, igniter);
        this.setFuse(80);
    }

    @Override
    public void tick() {
        if (!this.hasNoGravity()) {
            this.setVelocity(this.getVelocity().add(0.0, -0.04, 0.0));
        }
        this.move(MovementType.SELF, this.getVelocity());
        this.setVelocity(this.getVelocity().multiply(0.98));
        if (this.isOnGround()) {
            this.setVelocity(this.getVelocity().multiply(0.7, -0.5, 0.7));
        }
        int i = this.getFuse() - 1;
        this.setFuse(i);
        if (i <= 0) {
            if (!this.getWorld().isClient) {
                this.explodeFromCenter(currentRadius);
                currentRadius++;
                if (currentRadius > MAX_RADIUS) {
                    this.remove(RemovalReason.DISCARDED);
                }
            }
        } else {
            this.updateWaterState();
            if (this.getWorld().isClient) {
                this.getWorld().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }

    protected void explodeFromCenter(int currentRadius) {
        World world = this.getWorld();
        BlockPos explosionCenter = new BlockPos((int) this.getX(), (int) this.getBodyY(0.0625D), (int) this.getZ());

        int blocksRemoved = 0; // debug
        for (int x = -currentRadius; x <= currentRadius; x++) {
            for (int y = -currentRadius; y <= currentRadius; y++) {
                for (int z = -currentRadius; z <= currentRadius; z++) {
                    BlockPos currentPos = explosionCenter.add(x, y, z);
                    double distanceSquared = explosionCenter.getSquaredDistance(currentPos);
                    if (distanceSquared <= currentRadius * currentRadius && distanceSquared > (currentRadius-1) * (currentRadius-1)) {
                        world.setBlockState(currentPos, Blocks.AIR.getDefaultState(), 2 | 16);
                        blocksRemoved++;
                    }
                }
            }
        }
    }
}
