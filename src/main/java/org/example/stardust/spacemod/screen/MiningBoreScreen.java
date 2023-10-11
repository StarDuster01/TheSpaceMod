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
import org.example.stardust.spacemod.block.entity.ExcavatorBlockEntity;
import org.example.stardust.spacemod.block.entity.MiningBoreBlockEntity;
import org.example.stardust.spacemod.networking.ModMessages;
import org.joml.Vector2i;

public class MiningBoreScreen extends HandledScreen<MiningBoreScreenHandler> {

    private static final Identifier TEXTURE =
            new Identifier(SpaceMod.MOD_ID,"textures/gui/excavator_gui.png");

    public MiningBoreScreen(MiningBoreScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    public ButtonWidget button1;

    @Override
    protected void init() {
        super.init();
        titleY = 10;
        playerInventoryTitleY = 10;
        button1 = ButtonWidget.builder(Text.literal("MINING TOGGLE"), button -> {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeBlockPos(handler.getBlockEntity().getPos());
            ClientPlayNetworking.send(ModMessages.TOGGLE_BORING_ID, buf);
        }).dimensions(width / 2 -100, 20, 200, 20).tooltip(Tooltip.of(Text.literal("Click this Button to Toggle Mining"))).build();
        addDrawableChild(button1);
        createDimensionButton("4x4", new Vector2i(4, 4), width / 2 - 200, 60);
        createDimensionButton("16x16", new Vector2i(16, 16), width / 2 - 200, 80);
        createDimensionButton("32x32", new Vector2i(32, 32), width / 2 - 200, 100);
        createDimensionButton("64x64", new Vector2i(64, 64), width / 2 - 200, 120);
        createDimensionButton("128x128", new Vector2i(128, 128), width / 2 - 200, 140);
    }
    private void createDimensionButton(String label, Vector2i dimensions, int x, int y) {
        ButtonWidget button = ButtonWidget.builder(Text.literal(label), b -> {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeBlockPos(handler.getBlockEntity().getPos());
            buf.writeInt(dimensions.x);
            buf.writeInt(dimensions.y);
            ClientPlayNetworking.send(ModMessages.CHANGE_BORING_AREA_ID, buf);
        }).dimensions(x, y, 100, 20).build();
        addDrawableChild(button);
    }



    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context,mouseX,mouseY);

    }
 @Override
 protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
     MiningBoreBlockEntity blockEntity = this.handler.getBlockEntity();
     long energyAmount = (int) blockEntity.energyStorage.getAmount();
     drawPowerInfo(context, blockEntity);
     drawIsOnOff(context, blockEntity);
     drawQuarrySizeInfo(context, handler.getBlockEntity());
 }

    private void drawQuarrySizeInfo(DrawContext context, MiningBoreBlockEntity blockEntity) {
        Vector2i miningAreaDimensions = blockEntity.getMiningAreaDimensions();
        Text quarrySizeText = Text.of("Current Quarry Size: " + miningAreaDimensions.x + "x" + miningAreaDimensions.y);

        int quarryTextWidth = textRenderer.getWidth(quarrySizeText);
        int quarryX = (width - quarryTextWidth) / 2 -200;
        int quarryY = 110;  // Adjust the Y-position to place the text underneath the buttons

        context.drawCenteredTextWithShadow(textRenderer, quarrySizeText, quarryX, quarryY, 0xFFFFFF);  // White color
    }

    private void drawPowerInfo(DrawContext context, MiningBoreBlockEntity blockEntity) {
        long energyAmount = (int) blockEntity.energyStorage.getAmount();
        long energyPerBlock = (int) MiningBoreBlockEntity.getEnergyPerBlock();
       // System.out.println("Energy Amount: " + energyAmount);
        Text powertext;
        int powercolor;

        if (energyAmount > 0) {
            powertext = Text.of("Stored: " + energyAmount + "J" + " Can mine " + energyAmount / energyPerBlock + " More Blocks");
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

    private void drawIsOnOff(DrawContext context, MiningBoreBlockEntity blockEntity) {
        Text poweredtext;
        int poweredcolor;
        boolean Powered = blockEntity.isMiningActive();
        if (Powered) {
            poweredtext = Text.of("MINING TOGGLED ON");
            poweredcolor = 0x00FF00;

        } else {
            poweredtext = Text.of("MINING TOGGLED OFF");
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
