package org.example.stardust.spacemod.item.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.example.stardust.spacemod.entity.ModEntities;
import org.example.stardust.spacemod.item.ModItems;

public class MiningExplosiveEntity extends ThrownItemEntity {


    public MiningExplosiveEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    public MiningExplosiveEntity(World world, LivingEntity owner) {
        super(ModEntities.MINING_EXPLOSIVE_ENTITY_ENTITY_TYPE, owner, world);
    }
    public MiningExplosiveEntity(World world, double x, double y, double z) {
        super(ModEntities.MINING_EXPLOSIVE_ENTITY_ENTITY_TYPE, x, y, z, world);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.MINING_EXPLOSIVE;
    }

    @Environment(EnvType.CLIENT)
    private ParticleEffect getParticleParameters() {
        ItemStack itemStack = this.getItem();
        return (ParticleEffect)(itemStack.isEmpty() ? ParticleTypes.ITEM_SNOWBALL : new ItemStackParticleEffect(ParticleTypes.ITEM, itemStack));
    }

    @Environment(EnvType.CLIENT)
    public void handleStatus(byte status) { // Also not entirely sure, but probably also has to do with the particles. This method (as well as the previous one) are optional, so if you don't understand, don't include this one.
        if (status == 3) {
            ParticleEffect particleEffect = this.getParticleParameters();

            for(int i = 0; i < 8; ++i) {
                this.getWorld().addParticle(particleEffect, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }

    }

    protected void onEntityHit(EntityHitResult entityHitResult) { // called on entity hit.
    }

    public void customMiningExplosion() {
        ExplosionBehavior customExplosionBehavior = new ExplosionBehavior() {
            @Override
            public boolean canDestroyBlock(Explosion explosion, BlockView world, BlockPos pos, BlockState state, float power) {
                Block block = state.getBlock();
                return block == Blocks.DIRT ||
                        block == Blocks.STONE ||
                        block == Blocks.GRASS_BLOCK ||
                        block == Blocks.COBBLESTONE ||
                        block == Blocks.ANDESITE ||
                        block == Blocks.POLISHED_ANDESITE ||
                        block == Blocks.DIORITE ||
                        block == Blocks.POLISHED_DIORITE ||
                        block == Blocks.GRANITE ||
                        block == Blocks.POLISHED_GRANITE ||
                        block == Blocks.INFESTED_STONE ||
                        block == Blocks.INFESTED_COBBLESTONE ||
                        block == Blocks.DEEPSLATE ||
                        block == Blocks.COBBLED_DEEPSLATE ||
                        block == Blocks.POLISHED_DEEPSLATE;
            }
        };
        this.getWorld().createExplosion(
                this,
                null,
                customExplosionBehavior,
                this.getX(),
                this.getY(),
                this.getZ(),
                4.0F, // explosion power similar to TNT, adjust as needed
                false, // do not create fire
                World.ExplosionSourceType.TNT
        );


    }



    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (!this.getWorld().isClient) {
            this.getWorld().sendEntityStatus(this, (byte)3);
            this.customMiningExplosion();
            this.kill();
        }

    }
}
