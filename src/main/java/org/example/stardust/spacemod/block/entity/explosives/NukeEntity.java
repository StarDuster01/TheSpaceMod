package org.example.stardust.spacemod.block.entity.explosives;

import blue.endless.jankson.annotation.Nullable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.world.World;

public class NukeEntity extends TntEntity {
    public NukeEntity(World world, double x, double y, double z, @Nullable LivingEntity igniter) {
        super(world, x, y, z, igniter);
        this.setFuse(80);
    }

    @Override
    public void tick() {
        super.tick();

        if(this.getFuse() <=0) {
            this.explode();
            this.remove(RemovalReason.DISCARDED);
        }
    }

    protected void explode() {
        // Example: Explosion with customized power
        this.getWorld().createExplosion(this, this.getX(), this.getBodyY(0.0625D), this.getZ(), 128.0F, World.ExplosionSourceType.BLOCK);
    }
}
