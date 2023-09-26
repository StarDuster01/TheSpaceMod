package org.example.stardust.spacemod.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.example.stardust.spacemod.item.ModItems;

public class AltarBlock extends Block {
    public AltarBlock(Settings settings) {
        super(settings);
    }


    @Override
    public void onSteppedOn(World world, BlockPos pos,BlockState state, Entity entity) {
        if (entity instanceof PigEntity || entity instanceof ChickenEntity || entity instanceof CowEntity || entity instanceof SheepEntity) {
            long timeOfDay = world.getTimeOfDay() % 24000L;
            if (timeOfDay >= 5000L && timeOfDay <= 7000L) {
                LightningEntity lightningBolt = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
                lightningBolt.setPos(pos.getX(), pos.getY(), pos.getZ());
                world.spawnEntity(lightningBolt);
                entity.kill();
                world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(ModItems.BLOOD)));
            }
        }
        super.onSteppedOn(world, pos, state, entity);
    }
}
