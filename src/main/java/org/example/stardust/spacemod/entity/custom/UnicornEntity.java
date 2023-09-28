package org.example.stardust.spacemod.entity.custom;

import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.HorseArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
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
import net.minecraft.item.HorseArmorItem;
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
import java.util.function.Predicate;

public class UnicornEntity extends AbstractHorseEntity implements GeoEntity {

    private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    public UnicornEntity(EntityType<? extends AbstractHorseEntity> entityType, World world) {
        super(entityType, world);
    }

    private static final UUID UNICORN_ARMOR_BONUS_ID = UUID.fromString("8a72f69b-ef9d-4008-9c4a-9fb564ea9d8b");


    public static DefaultAttributeContainer setAttributes() {
        return AnimalEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 16.0f)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0f)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED, 2.0f)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.4f)
                .add(EntityAttributes.HORSE_JUMP_STRENGTH, 3.0f)
                .build();

    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        if (fallDistance > 1.0f) {
            this.playSound(SoundEvents.ENTITY_HORSE_LAND, 0.4f, 1.0f);
        }
        // Return false, do not play any sound or inflict any damage.
        return false;
    }




    @Override
    protected void updateSaddle() {
        if (this.getWorld().isClient) {
            return;
        }
        super.updateSaddle();
        this.setArmorTypeFromStack(this.items.getStack(1));
        this.setEquipmentDropChance(EquipmentSlot.CHEST, 0.0f);
    }

    private void setArmorTypeFromStack(ItemStack stack) {
        this.equipArmor(stack);
        if (!this.getWorld().isClient) {
            int i;
            this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).removeModifier(UNICORN_ARMOR_BONUS_ID);
            if (this.isHorseArmor(stack) && (i = ((HorseArmorItem)stack.getItem()).getBonus()) != 0) {
                this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).addTemporaryModifier(new EntityAttributeModifier(UNICORN_ARMOR_BONUS_ID, "Unicorn armor bonus", (double)i, EntityAttributeModifier.Operation.ADDITION));
            }
        }
    }



    @Override
    public boolean hasArmorSlot() {
        return true;
    }

    @Override
    public boolean isHorseArmor(ItemStack item) {
        return item.getItem() instanceof HorseArmorItem;
    }
    @Override
    public void onInventoryChanged(Inventory sender) {
        ItemStack itemStack = this.getArmorType();
        super.onInventoryChanged(sender);
        ItemStack itemStack2 = this.getArmorType();
        if (this.age > 20 && this.isHorseArmor(itemStack2) && itemStack != itemStack2) {
            this.playSound(SoundEvents.ENTITY_HORSE_ARMOR, 0.5f, 1.0f);
        }
    }

    public ItemStack getArmorType() {
        return this.getEquippedStack(EquipmentSlot.CHEST);
    }

    private void equipArmor(ItemStack stack) {
        this.equipStack(EquipmentSlot.CHEST, stack);
        this.setEquipmentDropChance(EquipmentSlot.CHEST, 0.0f);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new EscapeDangerGoal(this, 1.2));
        this.goalSelector.add(1, new HorseBondWithPlayerGoal(this, 1.2));
        this.goalSelector.add(2, new AnimalMateGoal(this, 1.0, AbstractHorseEntity.class));
        this.goalSelector.add(4, new FollowParentGoal(this, 1.0));
        this.goalSelector.add(6, new WanderAroundFarGoal(this, 0.7));
        this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
        this.goalSelector.add(8, new LookAroundGoal(this));
        this.goalSelector.add(8, new MeleeAttackGoal(this, 1.2, false));
        this.targetSelector.add(1, new ActiveTargetGoal<>((MobEntity) this, ChickenEntity.class, true));


        if (this.shouldAmbientStand()) {
            this.goalSelector.add(9, new AmbientStandGoal(this));
        }
        this.initCustomGoals();
    }


    private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> tAnimationState) {
        if(tAnimationState.isMoving()) {
            tAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.unicorn.walk", Animation.LoopType.LOOP));


        }
        if(isAttacking()) {
            tAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.unicorn.attack_charge", Animation.LoopType.PLAY_ONCE));
        }
        tAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.unicorn.idle", Animation.LoopType.LOOP));
        return PlayState.CONTINUE;

    }

    @Override
    public EntityView method_48926() {
        return null;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
