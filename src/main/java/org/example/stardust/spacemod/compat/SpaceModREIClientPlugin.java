package org.example.stardust.spacemod.compat;


import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import org.example.stardust.spacemod.block.ModBlocks;
import org.example.stardust.spacemod.recipe.DoomFurnaceRecipe;
import org.example.stardust.spacemod.screen.DoomFurnaceScreen;

import java.awt.*;

public class SpaceModREIClientPlugin implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new DoomFurnaceCategory());

        registry.addWorkstations(DoomFurnaceCategory.DOOM_FURNACE, EntryStacks.of(ModBlocks.DOOM_FURNACE_BLOCK));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerRecipeFiller(DoomFurnaceRecipe.class,
                DoomFurnaceRecipe.Type.INSTANCE, DoomFurnaceDisplay::new);
    }

    @Override
    public void registerScreens(ScreenRegistry registry) {
        registry.registerClickArea(screen -> new Rectangle(((screen.width - 176) / 2) + 78,
                        ((screen.height - 166) / 2) + 30, 20, 25),
                DoomFurnaceScreen.class,
                DoomFurnaceCategory.DOOM_FURNACE);
    }
}