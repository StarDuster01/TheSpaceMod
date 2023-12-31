package org.example.stardust.spacemod.screen;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import org.example.stardust.spacemod.SpaceMod;

public class ModScreenHandlers {

    public static final ScreenHandlerType<DoomFurnaceScreenHandler> DOOM_FURNACE_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, new Identifier(SpaceMod.MOD_ID,"doom_furnace_screen_handler"),
            new ExtendedScreenHandlerType<>(DoomFurnaceScreenHandler::new));
    public static final ScreenHandlerType<CoalGeneratorScreenHandler> COAL_GENERATOR_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, new Identifier(SpaceMod.MOD_ID,"coal_generator_screen_handler"),
                    new ExtendedScreenHandlerType<>(CoalGeneratorScreenHandler::new));

    public static final ScreenHandlerType<MediumCoalGeneratorScreenHandler> MEDIUM_COAL_GENERATOR_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, new Identifier(SpaceMod.MOD_ID,"medium_coal_generator_screen_handler"),
                    new ExtendedScreenHandlerType<>(MediumCoalGeneratorScreenHandler::new));
    public static final ScreenHandlerType<FusionReactorScreenHandler> FUSION_REACTOR_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, new Identifier(SpaceMod.MOD_ID,"fusion_reactor_screen_handler"),
                    new ExtendedScreenHandlerType<>(FusionReactorScreenHandler::new));
    public static final ScreenHandlerType<ExcavatorScreenHandler> EXCAVATOR_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, new Identifier(SpaceMod.MOD_ID,"excavator_screen_handler"),
                    new ExtendedScreenHandlerType<>(ExcavatorScreenHandler::new));
    public static final ScreenHandlerType<AlienPowerCoreScreenHandler> ALIEN_POWER_CORE_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, new Identifier(SpaceMod.MOD_ID,"alien_power_core_screen_handler"),
                    new ExtendedScreenHandlerType<>(AlienPowerCoreScreenHandler::new));

    public static final ScreenHandlerType<IronGeneratorScreenHandler> IRON_GENERATOR_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, new Identifier(SpaceMod.MOD_ID,"iron_generator_screen_handler"),
                    new ExtendedScreenHandlerType<>(IronGeneratorScreenHandler::new));
    public static final ScreenHandlerType<RangeSpawnerScreenHandler> RANGE_SPAWNER_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, new Identifier(SpaceMod.MOD_ID,"range_spawner_screen_handler"),
                    new ExtendedScreenHandlerType<>(RangeSpawnerScreenHandler::new));
    public static final ScreenHandlerType<MiningBoreScreenHandler> MINING_BORE_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, new Identifier(SpaceMod.MOD_ID,"mining_bore_screen_handler"),
                    new ExtendedScreenHandlerType<>(MiningBoreScreenHandler::new));

    public static final ScreenHandlerType<WallPlacerScreenHandler> WALLPLACER_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, new Identifier(SpaceMod.MOD_ID,"wallplacer_screen_handler"),
                    new ExtendedScreenHandlerType<>(WallPlacerScreenHandler::new));


    public static void registerScreenHandler() {
        SpaceMod.LOGGER.info("Registering Sreen Handler for" + SpaceMod.MOD_ID);
    }
}
