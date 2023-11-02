package org.example.stardust.spacemod.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.example.stardust.spacemod.SpaceMod;
import org.example.stardust.spacemod.block.entity.FusionReactorBlockEntity;
import org.example.stardust.spacemod.screen.renderer.FluidStackRenderer;
import org.example.stardust.spacemod.util.MouseUtil;

public class FusionReactorScreen extends HandledScreen<FusionReactorScreenHandler> {

    private static final Identifier TEXTURE =
            new Identifier(SpaceMod.MOD_ID,"textures/gui/coal_generator_gui.png");


    public FusionReactorScreen(FusionReactorScreenHandler handler, PlayerInventory inventory, Text title) {
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

        FusionReactorBlockEntity blockEntity = this.handler.getBlockEntity();
        Text text = Text.of("Insert Nether Star For Power");

        long energyAmount = (int) blockEntity.getAmount();
       // System.out.println("Current energy screen can detect is: " + energyAmount);

        Text powertext;
        int powercolor;

        if (blockEntity.isNoFuel() && energyAmount == 0) {
            powertext = Text.of("NO POWER");
            powercolor = 0xFF0000; // RED in RGB
        } else if (energyAmount > 0) {
            powertext = Text.of("Output: #Watts. Stored: " + energyAmount + "J");
            powercolor = 0x00FF00; // GREEN in RGB
        } else {
            powertext = Text.of("Fuel Present, No Power");
            powercolor = 0xFFFF00;
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
