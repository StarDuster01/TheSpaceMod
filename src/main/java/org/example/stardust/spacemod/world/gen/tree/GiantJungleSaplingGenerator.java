package org.example.stardust.spacemod.world.gen.tree;

import blue.endless.jankson.annotation.Nullable;
import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import org.example.stardust.spacemod.world.ModConfiguredFeatures;

public class GiantJungleSaplingGenerator extends SaplingGenerator {
    @Nullable
    @Override
    protected RegistryKey<ConfiguredFeature<?, ?>> getTreeFeature(Random random, boolean bees) {
        return ModConfiguredFeatures.GIANT_JUNGLE_TREE_KEY;
    }
}
