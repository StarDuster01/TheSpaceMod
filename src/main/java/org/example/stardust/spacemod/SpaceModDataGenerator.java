package org.example.stardust.spacemod;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryKeys;
import org.example.stardust.spacemod.datagen.ModWorldGenerator;
import org.example.stardust.spacemod.world.ModConfiguredFeatures;
import org.example.stardust.spacemod.world.ModPlacedFeatures;
import org.example.stardust.spacemod.world.biome.ModBiomes;
import org.example.stardust.spacemod.world.dimension.ModDimensions;

public class SpaceModDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();


    //    pack.addProvider(ModBlockTagProvider::new);
    //    pack.addProvider(ModItemTagProvider::new);
    //    pack.addProvider(ModBlockLootTableGenerator::new);
    //    pack.addProvider(ModModelProvider::new);
     //   pack.addProvider(ModRecipeGenerator::new);
     //   pack.addProvider(ModPaintingVariantTagProvider::new);
     //   pack.addProvider(ModAdvancementProvider::new);
    //    pack.addProvider(ModPOITagProvider::new);
   //     pack.addProvider(ModFluidTagProvider::new);
        pack.addProvider(ModWorldGenerator::new);

    }

    @Override
    public void buildRegistry(RegistryBuilder registryBuilder) {
       // registryBuilder.addRegistry(RegistryKeys.CONFIGURED_FEATURE, ModConfiguredFeatures::bootstrap);
       // registryBuilder.addRegistry(RegistryKeys.PLACED_FEATURE, ModPlacedFeatures::bootstrap);
        registryBuilder.addRegistry(RegistryKeys.BIOME, ModBiomes::bootstrap);
        registryBuilder.addRegistry(RegistryKeys.DIMENSION_TYPE, ModDimensions::bootstrapType);
        registryBuilder.addRegistry(RegistryKeys.CONFIGURED_FEATURE, ModConfiguredFeatures::bootstrap);
        registryBuilder.addRegistry(RegistryKeys.PLACED_FEATURE, ModPlacedFeatures::bootstrap);
    }
}
