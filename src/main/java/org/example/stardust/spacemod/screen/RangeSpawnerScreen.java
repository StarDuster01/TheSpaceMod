package org.example.stardust.spacemod.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.example.stardust.spacemod.SpaceMod;
import org.example.stardust.spacemod.block.entity.RangeSpawnerBlockEntity;
import net.minecraft.client.gui.widget.TextFieldWidget;

import org.joml.Vector2i;

import static org.example.stardust.spacemod.networking.ModMessages.TOGGLE_RANGE_SPAWNER_ID;


public class RangeSpawnerScreen extends HandledScreen<RangeSpawnerScreenHandler> {
    private TextFieldWidget commandTextField;


    private static final Identifier TEXTURE =
            new Identifier(SpaceMod.MOD_ID,"textures/gui/range_spawner_gui.png");

    public RangeSpawnerScreen(RangeSpawnerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    public ButtonWidget button1;

    @Override
    protected void init() {
        super.init();
        titleY = 10;
        playerInventoryTitleY = 10;
        button1 = ButtonWidget.builder(Text.literal("POWER TOGGLE"), button -> {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeBlockPos(handler.getBlockEntity().getPos());
            ClientPlayNetworking.send(TOGGLE_RANGE_SPAWNER_ID, buf);
        }).dimensions(width / 2 -100, 20, 200, 20).tooltip(Tooltip.of(Text.literal("Click this Button to Toggle Machine On"))).build();
        commandTextField = new TextFieldWidget(textRenderer, width / 2 - 150 / 2, height / 2 - 50, 150, 20, Text.literal("Command"));
        this.addDrawableChild(commandTextField);

    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (commandTextField.isActive()) { // Check if text field is active

            // Handle the "Escape" key
            if (keyCode == 256) {  // 256 is the key code for "Escape"
                commandTextField.setFocused(false); // Deactivate the text field
                return true;
            }

            if (keyCode == 257) {  // 257 is the key code for "Enter"
                handleCommand(commandTextField.getText());
                commandTextField.setFocused(false); // Deactivate the text field after command is processed
                commandTextField.setText(""); // Clear the text field
                return true;
            }

            // If it's the "E" key
            if (keyCode == 69) {
                return true;
            }

            return commandTextField.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }





    private void handleCommand(String command) {
        String[] parts = command.split(" ");
        if (parts.length > 0) {
            switch (parts[0].toUpperCase()) {
                case "AIRSTRIKE":
                    if (parts.length == 4) { // We expect AIRSTRIKE X Z FUSE_TIME
                        try {
                            int x = Integer.parseInt(parts[1]);
                            int z = Integer.parseInt(parts[2]);
                            int fuseTime = Integer.parseInt(parts[3]);

                            // Send packet to server to handle the airstrike.
                            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                            buf.writeBlockPos(handler.getBlockEntity().getPos());
                            buf.writeInt(x);
                            buf.writeInt(z);
                            buf.writeInt(fuseTime);
                            ClientPlayNetworking.send(new Identifier(SpaceMod.MOD_ID, "air_strike_command"), buf);

                        } catch (NumberFormatException e) {
                            this.client.player.sendMessage(Text.translatable("Invalid Range Spawner Command: " + command), false);
                        }
                    } else {
                        this.client.player.sendMessage(Text.translatable("Invalid Range Spawner Command: " + command), false);
                    }
                    break;
                default:
                    this.client.player.sendMessage(Text.translatable("Unknown Range Spawner Command"), false);
                    break;
            }
        }
    }




    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context,mouseX,mouseY);

    }
 @Override
 protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
     RangeSpawnerBlockEntity blockEntity = this.handler.getBlockEntity();
     long energyAmount = (int) blockEntity.energyStorage.getAmount();
     drawPowerInfo(context, blockEntity);
     drawIsOnOff(context, blockEntity);
 }


    private void drawPowerInfo(DrawContext context, RangeSpawnerBlockEntity blockEntity) {
        long energyAmount = (int) blockEntity.energyStorage.getAmount();
       // System.out.println("Energy Amount: " + energyAmount);
        Text powertext;
        int powercolor;

        if (energyAmount > 0) {
            powertext = Text.of("Stored: " + energyAmount + "J");
            powercolor = 0x00FF00; // GREEN in RGB

        } else {
            powertext = Text.of("NO POWER");
            powercolor = 0xFF0000; // RED in RGB
        }

        int powerTextWidth = textRenderer.getWidth(powertext);
        int powerX = 139 - powerTextWidth / 2; // adjust as needed
        int powerY = -10; // adjust as needed
        context.drawCenteredTextWithShadow(textRenderer, powertext, powerX, powerY, powercolor);
    }

    private void drawIsOnOff(DrawContext context, RangeSpawnerBlockEntity blockEntity) {
        Text poweredtext;
        int poweredcolor;
        boolean Powered = blockEntity.isPowered();
        if (Powered) {
            poweredtext = Text.of("MACHINE TOGGLED ON");
            poweredcolor = 0x00FF00;

        } else {
            poweredtext = Text.of("MACHINE TOGGLED OFF");
            poweredcolor = 0xFF0000; // RED in RGB
        }

        int poweredTextWidth = textRenderer.getWidth(poweredtext);
        int powerX = - poweredTextWidth / 2; // adjust as needed
        int powerY = 0; // adjust as needed
        context.drawCenteredTextWithShadow(textRenderer, poweredtext, powerX, powerY, poweredcolor);
    }


    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - backgroundWidth) / 2; // Centering the background on the screen
        int y = (height - backgroundHeight) / 2; // Centering the background on the screen
        int backgroundWidth = 175;
        int backgroundHeight = 165;
        int u = 0;
        int v = 0;
        context.drawTexture(TEXTURE, x, y, u, v, backgroundWidth, backgroundHeight);
    }


}
