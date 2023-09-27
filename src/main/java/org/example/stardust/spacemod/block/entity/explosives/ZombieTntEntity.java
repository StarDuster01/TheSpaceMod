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
            this.explodzombies();
            this.remove(RemovalReason.DISCARDED);
        }
    }

    private void explodzombies() {
        World world = this.getWorld();
        BlockPos explosionPos = this.getBlockPos();

        if(!world.isClient) {
            for(int i = 0; i< 20; i++) {
                ZombieEntity zombie = new ZombieEntity(EntityType.ZOMBIE, world);
                zombie.refreshPositionAndAngles(explosionPos.getX(), explosionPos.getY(), explosionPos.getZ(), 0, 0);
                world.spawnEntity(zombie);

            }
        }
     world.createExplosion(this, this.getX(), this.getBodyY(0.0625D), this.getZ(), 1.0F, World.ExplosionSourceType.BLOCK);
    }
}
