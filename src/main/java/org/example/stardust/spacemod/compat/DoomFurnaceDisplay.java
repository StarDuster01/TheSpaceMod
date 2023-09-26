package org.example.stardust.spacemod.compat;


import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import org.example.stardust.spacemod.recipe.DoomFurnaceRecipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DoomFurnaceDisplay extends BasicDisplay {


    public DoomFurnaceDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs) {
        super(inputs, outputs);
    }

    public DoomFurnaceDisplay(DoomFurnaceRecipe recipe) {
        super(getInputList(recipe), List.of(EntryIngredient.of(EntryStacks.of(recipe.getOutput(null)))));
    }

    private static List<EntryIngredient> getInputList(DoomFurnaceRecipe recipe) {
        if(recipe == null) return Collections.emptyList();
        List<EntryIngredient> list = new ArrayList<>();
        list.add(EntryIngredients.ofIngredient(recipe.getIngredients().get(0)));
        return list;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return DoomFurnaceCategory.DOOM_FURNACE;
    }
}