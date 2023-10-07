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

    public static final ScreenHandlerType<ExcavatorScreenHandler> EXCAVATOR_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, new Identifier(SpaceMod.MOD_ID,"excavator_screen_handler"),
                    new ExtendedScreenHandlerType<>(ExcavatorScreenHandler::new));

    public static final ScreenHandlerType<WallPlacerScreenHandler> WALLPLACER_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, new Identifier(SpaceMod.MOD_ID,"wallplacer_screen_handler"),
                    new ExtendedScreenHandlerType<>(WallPlacerScreenHandler::new));


    public static void registerScreenHandler() {
        SpaceMod.LOGGER.info("Registering Sreen Handler for" + SpaceMod.MOD_ID);
    }
}
