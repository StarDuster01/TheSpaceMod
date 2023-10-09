package org.example.stardust.spacemod.block.entity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import org.example.stardust.spacemod.SpaceMod;
import net.minecraft.util.Identifier;
import org.example.stardust.spacemod.block.ModBlocks;
import org.example.stardust.spacemod.block.custom.ExcavatorBlock;
import team.reborn.energy.api.EnergyStorage;

public class ModBlockEntities {

    public static final BlockEntityType<DoomFurnaceBlockEntity> DOOM_FURNACE_BE =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(SpaceMod.MOD_ID, "doom_furnace_block"),
                    FabricBlockEntityTypeBuilder.create(DoomFurnaceBlockEntity::new,
                            ModBlocks.DOOM_FURNACE_BLOCK).build(null));

    public static final BlockEntityType<CoalGeneratorBlockEntity> COAL_GENERATOR_BE =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(SpaceMod.MOD_ID, "coal_generator_block"),
                    FabricBlockEntityTypeBuilder.create(CoalGeneratorBlockEntity::new,
                            ModBlocks.COAL_GENERATOR_BLOCK).build(null));
    public static final BlockEntityType<FusionReactorBlockEntity> FUSION_REACTOR_BE =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(SpaceMod.MOD_ID, "fusion_reactor_block"),
                    FabricBlockEntityTypeBuilder.create(FusionReactorBlockEntity::new,
                            ModBlocks.FUSION_REACTOR_BLOCK).build(null));

    public static final BlockEntityType<ExcavatorBlockEntity> EXCAVATOR_BLOCK_BE =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(SpaceMod.MOD_ID, "excavator_block"),
                    FabricBlockEntityTypeBuilder.create(ExcavatorBlockEntity::new,
                            ModBlocks.EXCAVATOR_BLOCK).build(null));
    public static final BlockEntityType<WallPlacerBlockEntity> WALLPLACER_BLOCK_BE =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(SpaceMod.MOD_ID, "wallplacer_block"),
                    FabricBlockEntityTypeBuilder.create(WallPlacerBlockEntity::new,
                            ModBlocks.WALLPLACER).build(null));



    public static void registerBlockEntities() {
        SpaceMod.LOGGER.info("Registering Block Entities for" + SpaceMod.MOD_ID);

        EnergyStorage.SIDED.registerForBlockEntity((blockEntity, direction) -> blockEntity.energyStorage, DOOM_FURNACE_BE); // Allows the machine to accept energy from the sides
        EnergyStorage.SIDED.registerForBlockEntity((blockEntity, direction) -> blockEntity.energyStorage, EXCAVATOR_BLOCK_BE); // Allows the machine to accept energy from the sides
        EnergyStorage.SIDED.registerForBlockEntity((blockEntity, direction) -> blockEntity.energyStorage, WALLPLACER_BLOCK_BE); // Allows the machine to accept energy from the sides IF A MACHINE DOES NOT WORK THIS IS PROBABLY THE ISSUE
        FluidStorage.SIDED.registerForBlockEntity((blockEntity, direction) -> blockEntity.fluidStorage, DOOM_FURNACE_BE); // Allows the machine to accept fluid from the sides
    }
}
