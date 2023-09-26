package org.example.stardust.spacemod.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class SpeedBlock extends Block {

    public SpeedBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void onEntityLand(BlockView world, Entity entity) {
        if (entity instanceof net.minecraft.entity.player.PlayerEntity) {
            // Multiply the player's current speed by 3
            Vec3d velocity = entity.getVelocity();
            double multiplier = 3.0D;
            entity.setVelocity(velocity.x * multiplier, velocity.y, velocity.z * multiplier);
         //   World entityWorld = entity.getEntityWorld();

        }
    }
}
