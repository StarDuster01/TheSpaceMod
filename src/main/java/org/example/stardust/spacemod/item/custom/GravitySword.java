package org.example.stardust.spacemod.item.custom;

import gravity_changer.GravityComponent;
import gravity_changer.api.GravityChangerAPI;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.util.math.Direction;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GravitySword extends SwordItem {

    // Create a thread pool for scheduling
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public GravitySword(ToolMaterial material, int attackDamage, float attackSpeed, Settings settings) {
        super(material, attackDamage, attackSpeed, settings);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Direction currentGravity = GravityChangerAPI.getGravityDirection(target);
        Direction reversedGravity = currentGravity.getOpposite();
        GravityChangerAPI.setBaseGravityDirection(target, reversedGravity);
        scheduler.schedule(() -> {
            // Check if the entity is still valid (not dead) before changing its gravity
            if (!target.isRemoved()) {
                GravityChangerAPI.setBaseGravityDirection(target, currentGravity);
            }
        }, 1, TimeUnit.SECONDS);

        return super.postHit(stack, target, attacker);
    }
}
