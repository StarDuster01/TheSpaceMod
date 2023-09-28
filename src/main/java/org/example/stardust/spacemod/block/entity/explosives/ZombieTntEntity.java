package org.example.stardust.spacemod.block.entity.explosives;

import blue.endless.jankson.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ZombieTntEntity extends TntEntity {
    public ZombieTntEntity(World world, double x, double y, double z, @Nullable LivingEntity igniter) {
        super(world, x, y, z, igniter);
        this.setFuse(80);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getFuse() <= 0) {
            this.explodeZombies();
            this.remove(RemovalReason.DISCARDED);
        }
    }

    private void explodeZombies() {
        World world = this.getWorld();
        BlockPos explosionPos = this.getBlockPos();

        if (!world.isClient) {
            // Iterate over all adjacent and diagonal blocks in the horizontal plane
            for (BlockPos adjacentPos : new BlockPos[]{
                    explosionPos.north(),
                    explosionPos.south(),
                    explosionPos.east(),
                    explosionPos.west(),
                    explosionPos.north().east(), // North-East diagonal
                    explosionPos.north().west(), // North-West diagonal
                    explosionPos.south().east(), // South-East diagonal
                    explosionPos.south().west()  // South-West diagonal
            }) {
                // Spawn 2 zombies at each adjacent and diagonal block
                for (int i = 0; i < 2; i++) {
                    ZombieEntity zombie = new ZombieEntity(EntityType.ZOMBIE, (ServerWorld) world);
                    zombie.refreshPositionAndAngles(adjacentPos.getX() + 0.5, adjacentPos.getY(), adjacentPos.getZ() + 0.5, 0, 0);
                    world.spawnEntity(zombie);
                }
            }
        }
        // Your explosion code
        world.createExplosion(this, this.getX(), this.getBodyY(0.0625D), this.getZ(), 1.0F, World.ExplosionSourceType.BLOCK);
    }

}
