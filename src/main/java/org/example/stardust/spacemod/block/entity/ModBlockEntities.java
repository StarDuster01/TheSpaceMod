package org.example.stardust.spacemod.block.entity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import org.example.stardust.spacemod.SpaceMod;
import net.minecraft.util.Identifier;
import org.example.stardust.spacemod.block.ModBlocks;
import team.reborn.energy.api.EnergyStorage;

public class ModBlockEntities {

    public static final BlockEntityType<DoomFurnaceBlockEntity> DOOM_FURNACE_BE =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(SpaceMod.MOD_ID, "doom_furnace_block"),
                    FabricBlockEntityTypeBuilder.create(DoomFurnaceBlockEntity::new,
                            ModBlocks.DOOM_FURNACE_BLOCK).build(null));



    public static void registerBlockEntities() {
        SpaceMod.LOGGER.info("Registering Block Entities for" + SpaceMod.MOD_ID);

        EnergyStorage.SIDED.registerForBlockEntity((blockEntity, direction) -> blockEntity.energyStorage, DOOM_FURNACE_BE); // Allows the machine to accept energy from the sides
        FluidStorage.SIDED.registerForBlockEntity((blockEntity, direction) -> blockEntity.fluidStorage, DOOM_FURNACE_BE); // Allows the machine to accept fluid from the sides
    }
}
