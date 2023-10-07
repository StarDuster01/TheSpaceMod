package org.example.stardust.spacemod.world;

import net.fabricmc.fabric.api.biome.v1.BiomeModification;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.BiomeKeys;
import org.example.stardust.spacemod.entity.ModEntities;

public class ModEntityGeneration {

    public static void addSpawns() {
        // Add the spawn
        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(BiomeKeys.DARK_FOREST), SpawnGroup.CREATURE, ModEntities.UNICORN, 35, 1,2);

        //Spawn Restriction
        SpawnRestriction.register(ModEntities.UNICORN, SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);

    }
}

