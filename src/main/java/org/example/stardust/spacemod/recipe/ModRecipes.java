package org.example.stardust.spacemod.recipe;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.example.stardust.spacemod.SpaceMod;

public class ModRecipes {
    public static void registerRecipes() {
        Registry.register(Registries.RECIPE_SERIALIZER, new Identifier(SpaceMod.MOD_ID, DoomFurnaceRecipe.Serializer.ID),
                DoomFurnaceRecipe.Serializer.INSTANCE);
        Registry.register(Registries.RECIPE_TYPE, new Identifier(SpaceMod.MOD_ID, DoomFurnaceRecipe.Type.ID),
                DoomFurnaceRecipe.Type.INSTANCE);

    }
}