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
import org.example.stardust.spacemod.block.entity.IronGeneratorBlockEntity;
import org.example.stardust.spacemod.networking.ModMessages;
import org.joml.Vector2i;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class IronGeneratorScreen extends HandledScreen<IronGeneratorScreenHandler> {

    private static final Identifier TEXTURE =
            new Identifier(SpaceMod.MOD_ID,"textures/gui/iron_generator_gui.png");

    public IronGeneratorScreen(IronGeneratorScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    public ButtonWidget button1;
  //  private Text currentBlockType = Text.of("Default Block");
    private ButtonWidget blockTypeButton;

    private static final List<Text> BLOCK_TYPES = Arrays.asList(
            Text.of("Iron"),
            Text.of("Gold"),
            Text.of("Diamond"),
            Text.of("Lapis"),
            Text.of("Redstone"),
            Text.of("Emerald")
    );
    private int currentBlockTypeIndex = 0; // Default to the first type, "Iron"




    @Override
    protected void init() {
        super.init();
        titleY = 10;
        playerInventoryTitleY = 10;

        button1 = ButtonWidget.builder(Text.literal("MINING TOGGLE"), button -> {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeBlockPos(handler.getBlockEntity().getPos());
            ClientPlayNetworking.send(ModMessages.TOGGLE_MINING_ID, buf);
        }).dimensions(width / 2 -100, 20, 200, 20).tooltip(Tooltip.of(Text.literal("Click this Button to Toggle Mining"))).build();

        addDrawableChild(button1);
        createResourceButton("Iron", width / 2 - 200, 60);
        createResourceButton("Gold", width / 2 - 200, 80);
        createResourceButton("Diamond", width / 2 - 200, 100);
        createResourceButton("Emerald", width / 2 - 200, 120);
        createResourceButton("Netherite", width / 2 - 200, 140);
    }

    private static final HashMap<String, Integer> RESOURCE_TO_INT_MAP = new HashMap<String, Integer>() {{
        put("Iron", 0);
        put("Gold", 1);
        put("Diamond", 2);
        put("Emerald", 3);
        put("Netherite", 4);
    }};


    private void createResourceButton(String resource, int x, int y) {
        ButtonWidget button = ButtonWidget.builder(Text.literal(resource), b -> {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeBlockPos(handler.getBlockEntity().getPos());
            buf.writeInt(RESOURCE_TO_INT_MAP.get(resource));
            ClientPlayNetworking.send(ModMessages.CHANGE_GENERATED_RESOURCE_ID, buf);
            // Update block entity
            this.handler.getBlockEntity().setCurrentResourceType(RESOURCE_TO_INT_MAP.get(resource));
        }).dimensions(x, y, 100, 20).build();
        addDrawableChild(button);
    }
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context,mouseX,mouseY);
      //  System.out.println("Rendering IronGeneratorScreen");
    }
 @Override
 protected void drawForeground(DrawContext context, int mouseX, int mouseY) {

     IronGeneratorBlockEntity blockEntity = this.handler.getBlockEntity();
     long energyAmount = (int) blockEntity.energyStorage.getAmount();
     drawPowerInfo(context, blockEntity);
     drawIsActive(context, blockEntity); // Add this line
     drawSelectedResourceType(context, blockEntity);
 }

    private void drawSelectedResourceType(DrawContext context, IronGeneratorBlockEntity blockEntity) {
        int resourceTypeIndex = blockEntity.getCurrentResourceType();
        Text resourceText = BLOCK_TYPES.get(resourceTypeIndex);
        int resourceTextWidth = textRenderer.getWidth(resourceText);
        int resourceX = 139 - resourceTextWidth / 2; // adjust as needed
        int resourceY = 25; // adjust as needed
        context.drawCenteredTextWithShadow(textRenderer, resourceText, resourceX, resourceY, 0xFFFFFF);
    }
    private void drawPowerInfo(DrawContext context, IronGeneratorBlockEntity blockEntity) {
        long energyAmount = (int) blockEntity.energyStorage.getAmount();
       // System.out.println("Energy Amount: " + energyAmount);
        Text powertext;
        int powercolor;

        if (energyAmount > 0) {
            powertext = Text.of("Stored: " + energyAmount);
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
    private void drawIsActive(DrawContext context, IronGeneratorBlockEntity blockEntity) {
        Text activeText;
        int activeColor;

        boolean isActive = blockEntity.isGeneratorActive(); // Assuming your block entity has this method.
        if (isActive) {
            activeText = Text.of("GENERATOR ACTIVE");
            activeColor = 0x00FF00; // GREEN in RGB
        } else {
            activeText = Text.of("GENERATOR INACTIVE");
            activeColor = 0xFF0000; // RED in RGB
        }

        int activeTextWidth = textRenderer.getWidth(activeText);
        int activeX = 139 - activeTextWidth / 2; // adjust as needed
        int activeY = 5; // adjust as needed, placing it slightly below the power info
        context.drawCenteredTextWithShadow(textRenderer, activeText, activeX, activeY, activeColor);
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


    public void setBlockTypeIndex(int index) {
        this.currentBlockTypeIndex = index;
        if (this.blockTypeButton != null) { // Just a safety check in case the button isn't initialized yet
            this.blockTypeButton.setMessage(BLOCK_TYPES.get(currentBlockTypeIndex));
        }
    }

}
