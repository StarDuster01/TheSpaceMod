package org.example.stardust.spacemod.entity.custom;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EntityView;
import net.minecraft.world.World;
import org.example.stardust.spacemod.entity.ModEntities;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;

import java.util.UUID;

public class UnicornEntity extends TameableEntity implements GeoEntity, Saddleable {

    private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);


    public UnicornEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }
public static DefaultAttributeContainer.Builder setAttributes() {
        return AnimalEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH,16.0D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE,8.0f)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED,2.0f)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.4f);
}

    private static final int LOVE_MODE_DURATION = 600;
    private int inLove;
    private UUID playerInLoveUuid;

    @Override
    protected void initGoals() {

        //Goals
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new AnimalMateGoal(this,1.0D));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.add(3, new WanderAroundFarGoal(this, 0.75f, 1));
        this.goalSelector.add(4, new LookAroundGoal(this));
        //Targets of Goals
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, ChickenEntity.class, true));
    }


    //Creates a Child when the createChild method is called
    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return ModEntities.UNICORN.create(world);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 0, this::predicate));

    }

    // The Predicate controls what animations occur under what circumstances
    private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> tAnimationState) {

        // Checks if Entity is moving, if it is, play movement animation model.walk
        if (tAnimationState.isMoving()) {
            tAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.model.walk", Animation.LoopType.LOOP));
            return PlayState.CONTINUE;
        }

        // The idle animation will play when nothing else is happening
        tAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.model.idle", Animation.LoopType.LOOP));
        return PlayState.CONTINUE;
    }




    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        Item item = itemStack.getItem();

        if(item == Items.SADDLE && this.canBeSaddled()) {
            if (!this.getWorld().isClient) {
                this.saddle(SoundCategory.NEUTRAL);
                itemStack.decrement(1); // Decrease the saddle count by 1
                return ActionResult.SUCCESS;
            }
            return ActionResult.CONSUME;
        }

        Item itemforTaming = Items.APPLE;
        Item itemforBreeding = Items.SUGAR;
        World world = player.getWorld();

        if(item == itemforTaming && !isTamed()) {
            if(this.getWorld().isClient()) {
                return ActionResult.CONSUME;
            }else{
                if (player.getAbilities().creativeMode) {
                    itemStack.decrement(1);
                }

                if(this.random.nextInt(3) == 0) {
                    this.setOwner(player);
                    this.getWorld().sendEntityStatus(this, (byte) 7);
                }else{
                    this.getWorld().sendEntityStatus(this,(byte) 6);
                }
                return ActionResult.SUCCESS;
            }
        }

        if (item == itemforBreeding && this.canEat(itemStack)) {
            if (!world.isClient) {
                if (this.isTamed() && this.getBreedingAge() == 0 && !this.isInLove()) {
                    if (!player.getAbilities().creativeMode) {
                        itemStack.decrement(1);
                    }
                    this.setInLove(player);
                    return ActionResult.SUCCESS;
                }
            } else {
                return ActionResult.CONSUME;
            }
        }

        if (this.isTamed() && this.isSaddled() && hand == Hand.MAIN_HAND) { // Added check for isSaddled
            if (!player.isSneaking()) {
                player.startRiding(this);
            }

            return ActionResult.SUCCESS;
        }


        return super.interactMob(player, hand);
    }


    @Override
    public void tickMovement() {
        super.tickMovement();
        Entity passenger = this.getPrimaryPassenger();
        if (this.isControlledByPlayer()) {
            PlayerEntity controllingPlayer = (PlayerEntity) passenger;
            this.setYaw(controllingPlayer.getYaw());
            this.prevYaw = this.getYaw();
            this.setPitch(controllingPlayer.getPitch());
            this.setRotation(this.getYaw(), this.getPitch());
            this.bodyYaw = this.headYaw = this.getYaw();
            float sidewaysSpeed = controllingPlayer.sidewaysSpeed * 0.5f;
            float forwardSpeed = controllingPlayer.forwardSpeed;
            if (forwardSpeed <= 0.0f) {
                forwardSpeed *= 0.25f;  // Adjusting the backward speed of the unicorn
            }
            this.setVelocity(sidewaysSpeed, this.getVelocity().y, forwardSpeed);
            // Call the jump method when the unicorn should jump.
            if (this.jumpStrength > 0.0f && !this.isInAir() && this.isOnGround()) {
                double jumpVelocity = this.jumpStrength * this.getJumpStrength() * this.getJumpVelocityMultiplier();
                double modifiedJumpVelocity = jumpVelocity + this.getJumpBoostVelocityModifier();
                this.setVelocity(this.getVelocity().x, modifiedJumpVelocity, this.getVelocity().z);
                this.setInAir(true);
            }
            // Keep this to retain the momentum while jumping
            if (this.isInAir() && this.isInWater()) {
                this.setInAir(false);
            }
        }
    }
    public void setInAir(boolean inAir) {
        this.inAir = inAir;
    }

    private int clipClopTickCounter = 0;
    @Override
    public void tick() {
        super.tick();
        if(!this.getWorld().isClient) {
            // Play clip-clop sound when the unicorn is being ridden and moving
            if(this.hasPassengers() && this.isMoving()) {
                clipClopTickCounter++;
                if(clipClopTickCounter >= 10) { // Play sound every 10 ticks
                    this.playSound(SoundEvents.ENTITY_HORSE_GALLOP, 0.5F, 1.0F); // Adjust volume and pitch
                    clipClopTickCounter = 0;
                }
            }
        }
    }

    private boolean inAir = false;
    private final double jumpStrength = 0.5;

    @Override
    protected Vec3d getControlledMovementInput(PlayerEntity controllingPlayer, Vec3d movementInput) {
        if (this.isOnGround() && this.jumpStrength == 0.0f && !this.jumping) {
            return Vec3d.ZERO;
        }

        float strafe = controllingPlayer.sidewaysSpeed * 0.5f;
        float forward = controllingPlayer.forwardSpeed;
        if(forward <= 0.0f) forward *= 0.25f;

        float yaw = this.getYaw() * ((float)Math.PI / 180F); // Convert to radians
        double sinYaw = Math.sin(yaw);
        double cosYaw = Math.cos(yaw);

        // Note the swapping of strafe and forward and the sign change to get them correctly aligned
        double x = -strafe * cosYaw - forward * sinYaw;
        double z = -forward * cosYaw + strafe * sinYaw;

        return new Vec3d(x, 0.0, z);
    }




    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.hasPassengers()) { //
            return SoundEvents.ENTITY_HORSE_AMBIENT;
        }
        return super.getAmbientSound();
    }

    protected Vec2f getControlledRotation(LivingEntity controllingPassenger) {
        return new Vec2f(controllingPassenger.getPitch() * 0.5f, controllingPassenger.getYaw());
    }

    protected void jump() {
        if (this.isControlledByPlayer()) {
            Vec3d velocity = this.getVelocity();
            this.setVelocity(velocity.x, this.getJumpStrength() * this.jumpStrength, velocity.z);
            this.setInAir(true);
        }
    }


    private boolean canBeControlledByRider() {
        return this.isSaddled() && this.isTamed();
    }





    private void setInLove(PlayerEntity player) {
        this.inLove = LOVE_MODE_DURATION;
        this.playerInLoveUuid = player.getUuid();
        if (!this.getWorld().isClient) {
            this.getWorld().sendEntityStatus(this, (byte)18);
        }
    }


    public boolean canEat(ItemStack stack) {
        return stack.getItem() == Items.SUGAR;
    }

    private boolean saddled;

    @Nullable
    @Override
    public UUID getOwnerUuid() {
        return null;
    }

    @Override
    public EntityView method_48926() {
        return null;
    }

    @Override
    public boolean canBeSaddled() {
        return !this.isSaddled();
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    private boolean isMoving() {
        return this.getVelocity().x != 0 || this.getVelocity().z != 0;
    }



    @Override
    public void saddle(@Nullable SoundCategory sound) {
        this.saddled = true;
      /
        }


    @Override
    public boolean isSaddled() {
        return this.saddled;
    }
    @Nullable
    public Entity getPrimaryPassenger() {
        return this.hasPassengers() ? this.getPassengerList().get(0) : null;
    }
    // Return true if the unicorn is being controlled by a player
    public boolean isControlledByPlayer() {
        Entity passenger = this.getPrimaryPassenger();
        return passenger instanceof PlayerEntity;
    }
    // Return true if the unicorn is in the air
    public boolean isInAir() {
        return this.inAir;
    }
    public double getJumpStrength() {
        return this.jumpStrength;
    }
    // Return true if the unicorn is in water
    public boolean isInWater() {
        return this.isTouchingWater();
    }
}
