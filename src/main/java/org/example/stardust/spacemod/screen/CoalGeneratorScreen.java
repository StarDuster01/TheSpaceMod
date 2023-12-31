package org.example.stardust.spacemod.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.example.stardust.spacemod.SpaceMod;
import org.example.stardust.spacemod.block.entity.CoalGeneratorBlockEntity;
import org.example.stardust.spacemod.screen.renderer.EnergyInfoArea;
import org.example.stardust.spacemod.screen.renderer.FluidStackRenderer;
import org.example.stardust.spacemod.util.MouseUtil;

import java.util.Optional;

public class CoalGeneratorScreen extends HandledScreen<CoalGeneratorScreenHandler> {

    private static final Identifier TEXTURE =
            new Identifier(SpaceMod.MOD_ID,"textures/gui/coal_generator_gui.png");


    public CoalGeneratorScreen(CoalGeneratorScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }






    @Override
    protected void init() {
        super.init();
        titleY = 10;
        playerInventoryTitleY = 10;

    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context,mouseX,mouseY);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {


        CoalGeneratorBlockEntity blockEntity = this.handler.getBlockEntity();
        Text text = Text.of("Insert Coal For Power");

        long energyAmount = (int) blockEntity.energyStorage.getAmount(); // Update energyAmount every frame
        Text powertext;
        int powercolor;

        if (blockEntity.isNoFuel() && energyAmount == 0) {
            powertext = Text.of("NO POWER");
            powercolor = 0xFF0000; // RED in RGB
        } else if (energyAmount > 0) {
            powertext = Text.of("Output: 2000Watts. Stored: " + energyAmount + "J");
            powercolor = 0x00FF00; // GREEN in RGB
        } else {
            // Handle the case when there is fuel but energyAmount is 0.
            powertext = Text.of("Fuel Present, No Power"); // Add your custom message here
            powercolor = 0xFFFF00; // YELLOW in RGB (custom color for this case)
        }

        int powerTextWidth = textRenderer.getWidth(powertext);
        int powerX = 139 - powerTextWidth / 2;
        int powerY = 20;

        context.drawCenteredTextWithShadow(textRenderer, powertext, powerX, powerY, powercolor);

        int grey = 8421504;
        context.drawCenteredTextWithShadow(this.textRenderer, text, 80, 35, grey);
    }


    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);

    }

// Checks if the mouse is above a defined area
private boolean isMouseAboveArea(int pMouseX, int pMouseY, int x, int y, int offsetX, int offsetY, FluidStackRenderer renderer) {
    return MouseUtil.isMouseOver(pMouseX, pMouseY, x + offsetX, y + offsetY, renderer.getWidth(), renderer.getHeight());
}
    private boolean isMouseAboveArea(int pMouseX, int pMouseY, int x, int y, int offsetX, int offsetY, int width, int height) {
        return MouseUtil.isMouseOver(pMouseX, pMouseY, x + offsetX, y + offsetY, width, height);
    }


}
