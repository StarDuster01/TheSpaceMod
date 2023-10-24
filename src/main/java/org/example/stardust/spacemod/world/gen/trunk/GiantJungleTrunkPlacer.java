package org.example.stardust.spacemod.world.gen.trunk;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.trunk.GiantTrunkPlacer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class GiantJungleTrunkPlacer extends GiantTrunkPlacer {
    public static final Codec<GiantJungleTrunkPlacer> CODEC = RecordCodecBuilder.create(instance ->
            fillTrunkPlacerFields(instance).apply(instance, GiantJungleTrunkPlacer::new));
    public static final int WIDTH = 4;

    public GiantJungleTrunkPlacer(int baseHeight, int heightRandA, int heightRandB) {
        super(baseHeight, heightRandA, heightRandB);
    }

    @Override
    public List<FoliagePlacer.TreeNode> generate(TestableWorld world, BiConsumer<BlockPos, BlockState> replacer, Random random, int height, BlockPos startPos, TreeFeatureConfig config) {
        for (int dx = 0; dx < WIDTH; dx++) {
            for (int dz = 0; dz < WIDTH; dz++) {
                BlockPos groundPos = startPos.add(dx, -1, dz);
                setToDirt(world, replacer, random, groundPos, config);
            }
        }

        // Place a 5x5 square of logs for the height of the tree
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int dx = 0; dx < WIDTH; dx++) {
            for (int dz = 0; dz < WIDTH; dz++) {
                for (int dy = 0; dy < height; dy++) {
                    mutable.set(startPos.getX() + dx, startPos.getY() + dy, startPos.getZ() + dz);
                    this.setLog(world, replacer, random, mutable, config);
                }
            }
        }

        // Branch generation (modified to resemble but be bigger than MegaJungleTrunkPlacer)
        List<FoliagePlacer.TreeNode> treeNodes = new ArrayList<>();
        for (int i = height - 2; i > 0; i -= 2 + random.nextInt(4)) {
            float f = random.nextFloat() * ((float)Math.PI * 2.5F); // Adjusted for a larger rotation
            int j = 0;
            int k = 0;
            for (int l = 0; l < 7; ++l) {  // Increase loop count for longer branches
                j = (int)(2.5f + MathHelper.cos(f) * l);  // Adjusted coefficient for wider spread
                k = (int)(2.5f + MathHelper.sin(f) * l);  // Adjusted coefficient for wider spread
                BlockPos branchPos = startPos.add(j, i - 3 + l / 2, k);
                this.setLog(world, replacer, random, branchPos, config);
                treeNodes.add(new FoliagePlacer.TreeNode(branchPos.up(l), -2, false));
            }
            treeNodes.add(new FoliagePlacer.TreeNode(startPos.add(j, i, k), -2, false));
        }

        treeNodes.add(new FoliagePlacer.TreeNode(startPos.up(height), 0, true));
        return treeNodes;
    }

    private void setLog(TestableWorld world, BiConsumer<BlockPos, BlockState> replacer, Random random, BlockPos position, TreeFeatureConfig config) {
        BlockPos.Mutable mutablePosition = position instanceof BlockPos.Mutable ? (BlockPos.Mutable) position : position.mutableCopy();
        this.trySetState(world, replacer, random, mutablePosition, config);
    }

}
