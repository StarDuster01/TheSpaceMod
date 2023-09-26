package org.example.stardust.spacemod.item.custom;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FrostWand extends Item {
    private static final UUID FROST_WAND_MODIFIER_UUID = UUID.randomUUID(); // Unique identifier for the speed modifier
    private static final double ALMOST_ZERO_SPEED = 0.0001D;


    public FrostWand(Settings settings) {
        super(settings);
    }



    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
        ItemStack itemStack = playerEntity.getStackInHand(hand);

        if (!world.isClient) {
            BlockHitResult hitResult = raycast(world, playerEntity, RaycastContext.FluidHandling.SOURCE_ONLY);
            BlockPos pos = hitResult.getBlockPos();

            if (world.getBlockState(pos).isOf(Blocks.WATER)) {
                world.setBlockState(pos, Blocks.ICE.getDefaultState());
                itemStack.damage(1, playerEntity, (p) -> p.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
                return TypedActionResult.success(itemStack);
            }
        }
        return TypedActionResult.pass(itemStack);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.getWorld().isClient) {
            EntityAttributeInstance movementSpeedAttribute = target.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
            if (movementSpeedAttribute != null) {
                EntityAttributeModifier modifier = new EntityAttributeModifier(FROST_WAND_MODIFIER_UUID, "Frost wand speed reduction", ALMOST_ZERO_SPEED - movementSpeedAttribute.getBaseValue(), EntityAttributeModifier.Operation.ADDITION);
                movementSpeedAttribute.addTemporaryModifier(modifier);

                ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
                scheduler.schedule(() -> {
                    if (target.isAlive()) {
                        movementSpeedAttribute.removeModifier(FROST_WAND_MODIFIER_UUID);
                    }
                    scheduler.shutdown();
                }, 4, TimeUnit.SECONDS);

                stack.damage(1, attacker, (p) -> p.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
            }
        }
        return true;
    }

}
