package org.example.stardust.spacemod.sounds;

import net.minecraft.client.sound.Sound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import org.example.stardust.spacemod.SpaceMod;

public class ModSounds {

    public static final SoundEvent BRUH = registerSoundEvent("bruh");
    public static final SoundEvent NEW_ARIA_MATH = registerSoundEvent("new_aria_math");

    private static SoundEvent registerSoundEvent(String name) {
        Identifier identifier = new Identifier(SpaceMod.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, identifier, SoundEvent.of(identifier));
    }

    public static void registerSounds() {
        SpaceMod.LOGGER.info("Registerting Mod Sounds for" + SpaceMod.MOD_ID);
    }
}
