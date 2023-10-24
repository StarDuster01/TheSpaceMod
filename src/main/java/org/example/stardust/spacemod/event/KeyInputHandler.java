package org.example.stardust.spacemod.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.client.util.InputUtil;
import org.example.stardust.spacemod.networking.ModMessages;
import org.lwjgl.glfw.GLFW;

public class KeyInputHandler {

    public static final String KEY_CATEGORY_SPACEMOD = "key.category.spacemod.spacemod";

    public static final String KEY_FLYMOB_UP = "key.spacemod.fly.up";
    public static final String KEY_FLYMOB_DOWN = "key.spacemod.fly.down";

    public static KeyBinding flyUpKey;
    public static KeyBinding flyDownKey;

    public static void registerKeyInputs() {
        ClientTickEvents.END_CLIENT_TICK.register(client ->  {
            if(flyUpKey.wasPressed()) {
            }
            if(flyDownKey.wasPressed()) {
            }
        });


    }

    public static void register() {
        flyUpKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
           KEY_FLYMOB_UP,
           InputUtil.Type.KEYSYM,
                   GLFW.GLFW_KEY_LEFT_ALT,
                KEY_CATEGORY_SPACEMOD
        ));
        flyDownKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_FLYMOB_DOWN,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                KEY_CATEGORY_SPACEMOD ));

        registerKeyInputs();

    }
}
