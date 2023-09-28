package org.example.stardust.spacemod.entity.custom;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EntityView;
import net.minecraft.world.World;
import org.example.stardust.spacemod.entity.ModEntities;
import org.example.stardust.spacemod.event.KeyInputHandler;
import org.example.stardust.spacemod.networking.ModMessages;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;

import java.util.Optional;
import java.util.UUID;

import static net.minecraft.client.util.InputUtil.isKeyPressed;

public class GriffinEntity extends TameableEntity implements InventoryChangedListener,
        Saddleable, JumpingMount, GeoEntity {

    private static final TrackedData<Boolean> SADDLED = DataTracker.registerData(GriffinEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> IN_AIR = DataTracker.registerData(GriffinEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private float jumpStrength = 1.0f;
    private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    private float flyingSpeed = 1.0f;



    public GriffinEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
        this.dataTracker.startTracking(SADDLED, false);
        this.dataTracker.startTracking(IN_AIR, false); // Assuming Griffin starts not in the air
    }

    public static DefaultAttributeContainer setAttributes() {
        return AnimalEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 16.0f)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0f)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED, 2.0f)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.4f)
                .add(EntityAttributes.HORSE_JUMP_STRENGTH, 3.0f)
                .build();

    }

    // Call this method when the player interacts with the Griffin to attempt to tame it
    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);


        if (!this.isTamed() && stack.isOf(Items.GOLDEN_APPLE)) {
            if (!player.isCreative()) stack.decrement(1);

            if (this.random.nextInt(3) == 0) { // 33% chance to get tamed
                this.setOwner(player);
                return ActionResult.SUCCESS;
            } else {

                this.playSound(SoundEvents.ENTITY_HORSE_EAT, 1.0f, 1.0f);
                return ActionResult.CONSUME;
            }
        }

        if (this.isTamed() && this.isSaddled()) {
            player.startRiding(this);
            return super.interactMob(player, hand);
        }

        return super.interactMob(player, hand);
    }


    @Override
    public void saddle(@Nullable SoundCategory sound) {
        this.dataTracker.set(SADDLED, true);
        if (sound != null) {
            this.playSound(SoundEvents.ENTITY_HORSE_SADDLE, 0.4f, 1.0f);
        }
    }



    @Override
    public boolean canBeSaddled() {
        return !this.isSaddled() && this.isTamed();
    }

    @Override
    public boolean isSaddled() {
        return this.dataTracker.get(SADDLED);
    }

    public boolean isInAir() {
        return this.dataTracker.get(IN_AIR);
    }
    public void setInAir(boolean inAir) {
        this.dataTracker.set(IN_AIR, inAir);
    }


    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 0, this::predicate));

    }
    private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> tAnimationState) {
        if (this.isInAir()) {
            // if Griffin is in the air, set the animation to griffin.fly
            tAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.griffin.flying", Animation.LoopType.LOOP));
        } else if (tAnimationState.isMoving()) {
            tAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.griffin.walk", Animation.LoopType.LOOP));
        } else if (isAttacking()) {
            tAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.griffin.attack", Animation.LoopType.PLAY_ONCE));
        } else {
            tAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.griffin.idle", Animation.LoopType.LOOP));
        }
        return PlayState.CONTINUE;
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return getFirstPassenger() instanceof LivingEntity entity ? entity : null;
    }

    @Override
    public boolean isLogicalSideForUpdatingMovement() {
        return true;
    }

    // Adjust the rider's position while riding
    @Override
    public void updatePassengerPosition(Entity entity, PositionUpdater moveFunction) {
        if (entity instanceof LivingEntity passenger) {
            moveFunction.accept(entity, getX(), getY() + 1.0f , getZ());

            this.prevPitch = passenger.prevPitch;
        }
    }

    @Override
    protected Vec3d getControlledMovementInput(PlayerEntity controllingPlayer, Vec3d movementInput) {
        if (this.isOnGround() && this.jumpStrength == 0.0f && !this.jumping) {
            return Vec3d.ZERO;
        }
        float y = controllingPlayer.upwardSpeed;
        float f = controllingPlayer.sidewaysSpeed * 0.5f;
        float g = controllingPlayer.forwardSpeed;
        if (g <= 0.0f) {
            g *= 0.25f;
        }

        return new Vec3d(f, y, g);
    }

    private boolean isKeyPressed(int keyCode) {
        if(this.getWorld().isClient) { // Check if we're on the client side
            return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), keyCode);
        }
        return false;
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
        // Do nothing, whether or not a player is riding
    }


    @Override
    public boolean isClimbing() {

        return false;
    }

    // Other fields...
    private boolean ascending;  // Add this field to your GriffinEntity class

    // Other methods...

    public boolean isAscending() {
        return this.ascending;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }



    @Override
    public void travel(Vec3d movementInput) {
        if (this.isAlive()) {
            if (this.hasPassengers() && this.getControllingPassenger() instanceof PlayerEntity) {
                PlayerEntity passenger = (PlayerEntity) this.getControllingPassenger();
                this.prevYaw = getYaw();
                this.prevPitch = getPitch();
                setYaw(passenger.getYaw());
                setPitch(passenger.getPitch() * 0.5f);
                setRotation(getYaw(), getPitch());
                this.bodyYaw = this.getYaw();
                this.headYaw = this.bodyYaw;
                float x = passenger.sidewaysSpeed * 0.5F;
                float z = passenger.forwardSpeed;

                if (!this.isInAir()) {
                    z *= 0.2; // Increase forward speed when in air. Adjust as needed.
                    x *= 0.2;
                }


                if (this.getWorld().isClient) {
                    double verticalSpeed = 0.0D; // Initialize verticalSpeed to 0.0D
                    // Handle Client-side logic here.
                    if (KeyInputHandler.flyUpKey.isPressed()) {
                        setAscending(true);
                        verticalSpeed = 0.4D;
                        PacketByteBuf buf = PacketByteBufs.create();
                        buf.writeDouble(verticalSpeed); // send the verticalSpeed to the server.
                        ClientPlayNetworking.send(ModMessages.GRIFFIN_MOVEMENT_ID, buf);
                    } else if (KeyInputHandler.flyDownKey.isPressed()) {
                        setAscending(false);
                        verticalSpeed = -0.4D;
                        PacketByteBuf buf = PacketByteBufs.create();
                        buf.writeDouble(verticalSpeed); // send the verticalSpeed to the server.
                        ClientPlayNetworking.send(ModMessages.GRIFFIN_MOVEMENT_ID, buf);
                    }
                    // only change velocity if a key is pressed
                    if(verticalSpeed != 0.0D) {
                        this.setVelocity(this.getVelocity().x, verticalSpeed, this.getVelocity().z);
                    }

                    this.setMovementSpeed(1.3f);
                    super.travel(new Vec3d(x, 0, z)); // Set vertical component of movement input to 0
                }
                if(!this.getWorld().isClient) {
                    if (this.isInAir() && this.isAscending()) {
                        this.setVelocity(this.getVelocity().x, 1.3D, this.getVelocity().z);
                    }
                }
            } else {
                super.travel(movementInput);
            }
        }
    }







    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        if (this.isInAir()) { // If Griffin is in the air, don't apply fall damage.
            return false;
        }
        if (this.hasPassengers()) {
            for (Entity entity : this.getPassengerList()) {
                entity.handleFallDamage(fallDistance, damageMultiplier, damageSource);
            }
            return false;
        }
        return super.handleFallDamage(fallDistance, damageMultiplier, damageSource);
    }


    @Override
    public void tick() {
        super.tick();

        // Check if the griffin is in the air and set IN_AIR accordingly
        if (!this.isOnGround() && !this.isTouchingWater()) {
            this.setInAir(true); // Set IN_AIR to true if Griffin is in the air.
        } else {
            this.setInAir(false); // Set IN_AIR to false if Griffin is on the ground or in water.
        }

        // Reset fall distance if in the air
        if (this.isInAir()) {
            this.fallDistance = 0; // Reset the fall distance of the Griffin
            for (Entity entity : this.getPassengerList()) {
                entity.fallDistance = 0; // Reset the fall distance of the passengers
            }
        }
    }



    @Override
    public EntityView method_48926() {
        return this.getWorld();
    }
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void setJumpStrength(int strength) {

    }

    @Override
    public boolean canJump() {
        return false;
    }

    @Override
    public void startJumping(int height) {

    }

    @Override
    public void stopJumping() {

    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    @Override
    public void onInventoryChanged(Inventory sender) {

    }
}
