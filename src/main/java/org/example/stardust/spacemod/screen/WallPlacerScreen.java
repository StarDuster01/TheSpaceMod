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
import org.example.stardust.spacemod.block.entity.WallPlacerBlockEntity;
import org.example.stardust.spacemod.networking.ModMessages;

public class WallPlacerScreen extends HandledScreen<WallPlacerScreenHandler> {

    private static final Identifier TEXTURE =
            new Identifier(SpaceMod.MOD_ID,"textures/gui/wallplacer_gui.png");

    public WallPlacerScreen(WallPlacerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    public ButtonWidget button1;
    public ButtonWidget button2;
    public ButtonWidget button3;

    @Override
    protected void init() {
        super.init();
        titleY = 10;
        playerInventoryTitleY = 10;
        button1 = ButtonWidget.builder(Text.literal("PLACING TOGGLE"), button -> {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeBlockPos(handler.getBlockEntity().getPos());
            ClientPlayNetworking.send(ModMessages.TOGGLE_WALL_PLACING_ID, buf);
        }).dimensions(width / 2 -100, 20, 200, 20).tooltip(Tooltip.of(Text.literal("Click this Button to Toggle Construction"))).build();
        addDrawableChild(button1);
        button2 = ButtonWidget.builder(Text.literal("WALL TOGGLE"), button -> {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeBlockPos(handler.getBlockEntity().getPos());
            ClientPlayNetworking.send(ModMessages.PLACE_WALL_ID, buf);
        }).dimensions(width / 2 -200, 20 +80, 100, 20).tooltip(Tooltip.of(Text.literal("Click this Button to Toggle Wall Placing"))).build();
        addDrawableChild(button2);
        button3 = ButtonWidget.builder(Text.literal("TOWER TOGGLE"), button -> {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeBlockPos(handler.getBlockEntity().getPos());
            ClientPlayNetworking.send(ModMessages.PLACE_TOWER_ID, buf);
        }).dimensions(width / 2 -200, 20 +40, 100, 20).tooltip(Tooltip.of(Text.literal("Click this Button to Toggle Tower PLacing"))).build();
        addDrawableChild(button3);



    }
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context,mouseX,mouseY);
    }
 @Override
 protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
     WallPlacerBlockEntity blockEntity = this.handler.getBlockEntity();
     long energyAmount = (int) blockEntity.energyStorage.getAmount();
     drawPowerInfo(context, blockEntity);
     drawIsOnOff(context, blockEntity);
 }
    private void drawPowerInfo(DrawContext context, WallPlacerBlockEntity blockEntity) {
        long energyAmount = (int) blockEntity.energyStorage.getAmount();
        long energyPerBlock = (int) WallPlacerBlockEntity.getEnergyPerBlock();

        Text powertext;
        int powercolor;

        if (energyAmount > 0) {
            powertext = Text.of("Stored: " + energyAmount + "J" + " Can Place " + energyAmount / energyPerBlock + " More Blocks");
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

    private void drawIsOnOff(DrawContext context, WallPlacerBlockEntity blockEntity) {
        Text poweredtext;
        int poweredcolor;
        boolean Powered = blockEntity.isPlacingActive();
        if (Powered) {
            poweredtext = Text.of("PLACING ACTIVE");
            poweredcolor = 0x00FF00;

        } else {
            poweredtext = Text.of("PLACING OFF");
            poweredcolor = 0xFF0000; // RED in RGB
        }

        int poweredTextWidth = textRenderer.getWidth(poweredtext);
        int powerX = - poweredTextWidth / 2; // adjust as needed
        int powerY = -10 + 40; // adjust as needed
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
