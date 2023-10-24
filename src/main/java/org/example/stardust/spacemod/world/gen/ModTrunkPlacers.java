package org.example.stardust.spacemod.world.gen;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.trunk.TrunkPlacerType;
import org.example.stardust.spacemod.SpaceMod;
import org.example.stardust.spacemod.world.gen.trunk.GiantJungleTrunkPlacer;

public class ModTrunkPlacers {
    public static final TrunkPlacerType<GiantJungleTrunkPlacer> GIANT_JUNGLE_TRUNK_PLACER;

    static {
        GIANT_JUNGLE_TRUNK_PLACER = Registry.register(Registries.TRUNK_PLACER_TYPE,
                new Identifier(SpaceMod.MOD_ID, "giant_jungle_trunk_placer"),
                new TrunkPlacerType<>(GiantJungleTrunkPlacer.CODEC)
        );
    }

    public static void init() {
        SpaceMod.LOGGER.info("Registering Trunk Placers for" + SpaceMod.MOD_ID);
    }
}
