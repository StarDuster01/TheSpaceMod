package org.example.stardust.spacemod.world.gen;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.world.gen.GenerationStep;
import org.example.stardust.spacemod.world.ModPlacedFeatures;
import org.example.stardust.spacemod.world.biome.ModBiomes;

public class ModOreGeneration {
    public static void generateOres() {
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(ModBiomes.FORMIC_BIOME),
                GenerationStep.Feature.UNDERGROUND_ORES, ModPlacedFeatures.GIANT_DIAMOND_ORE_PLACED_KEY);
    }
}