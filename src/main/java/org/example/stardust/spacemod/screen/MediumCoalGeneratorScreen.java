package org.example.stardust.spacemod.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.example.stardust.spacemod.SpaceMod;
import org.example.stardust.spacemod.block.entity.MediumCoalGeneratorBlockEntity;
import org.example.stardust.spacemod.screen.renderer.FluidStackRenderer;
import org.example.stardust.spacemod.util.MouseUtil;

public class MediumCoalGeneratorScreen extends HandledScreen<MediumCoalGeneratorScreenHandler> {

    private static final Identifier TEXTURE =
            new Identifier(SpaceMod.MOD_ID,"textures/gui/medium_coal_generator_gui.png");


    public MediumCoalGeneratorScreen(MediumCoalGeneratorScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();

    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context,mouseX,mouseY);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {

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
}
