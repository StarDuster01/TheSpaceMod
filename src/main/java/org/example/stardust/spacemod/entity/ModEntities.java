package org.example.stardust.spacemod.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.example.stardust.spacemod.SpaceMod;
import org.example.stardust.spacemod.entity.custom.FormicEntity;
import org.example.stardust.spacemod.entity.custom.GiantSalamanderEntity;
import org.example.stardust.spacemod.entity.custom.GriffinEntity;
import org.example.stardust.spacemod.entity.custom.UnicornEntity;
import org.example.stardust.spacemod.item.entity.MiningExplosiveEntity;

public class ModEntities {

    public static final EntityType<UnicornEntity> UNICORN = Registry.register(Registries.ENTITY_TYPE,
            new Identifier(SpaceMod.MOD_ID, "unicorn"), FabricEntityTypeBuilder.create(SpawnGroup.CREATURE,
                    UnicornEntity::new).dimensions(EntityDimensions.fixed(1.5f,1.5f)).build());
    public static final EntityType<FormicEntity> FORMIC = Registry.register(Registries.ENTITY_TYPE,
            new Identifier(SpaceMod.MOD_ID, "formic"), FabricEntityTypeBuilder.create(SpawnGroup.CREATURE,
                    FormicEntity::new).dimensions(EntityDimensions.fixed(1.5f,1.5f)).build());

    public static final EntityType<GriffinEntity> GRIFFIN = Registry.register(Registries.ENTITY_TYPE,
            new Identifier(SpaceMod.MOD_ID, "griffin"), FabricEntityTypeBuilder.create(SpawnGroup.CREATURE,
                    GriffinEntity::new).dimensions(EntityDimensions.fixed(1.5f,1.5f)).build());

    public static final EntityType<GiantSalamanderEntity> GIANT_SALAMANDER= Registry.register(Registries.ENTITY_TYPE,
            new Identifier(SpaceMod.MOD_ID, "giant_salamander"), FabricEntityTypeBuilder.create(SpawnGroup.CREATURE,
                    GiantSalamanderEntity::new).dimensions(EntityDimensions.fixed(1.0f,1.0f)).build());

    public static final EntityType<MiningExplosiveEntity> MINING_EXPLOSIVE_ENTITY_ENTITY_TYPE = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(SpaceMod.MOD_ID, "mining_explosive"),
            FabricEntityTypeBuilder.<MiningExplosiveEntity>create(SpawnGroup.MISC, MiningExplosiveEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
                    .trackRangeBlocks(4).trackedUpdateRate(10)
                    .build()
    );
}
