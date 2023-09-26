package org.example.stardust.spacemod.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.example.stardust.spacemod.SpaceMod;
import org.example.stardust.spacemod.entity.custom.UnicornEntity;

public class ModEntities {

    public static final EntityType<UnicornEntity> UNICORN = Registry.register(Registries.ENTITY_TYPE,
            new Identifier(SpaceMod.MOD_ID, "unicorn"), FabricEntityTypeBuilder.create(SpawnGroup.CREATURE,
                    UnicornEntity::new).dimensions(EntityDimensions.fixed(1.5f,1.5f)).build());
}
