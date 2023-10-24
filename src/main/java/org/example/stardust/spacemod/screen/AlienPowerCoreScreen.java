package org.example.stardust.spacemod.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.example.stardust.spacemod.SpaceMod;
import org.example.stardust.spacemod.block.entity.AlienPowerCoreBlockEntity;
import org.example.stardust.spacemod.networking.ModMessages;
import org.example.stardust.spacemod.screen.renderer.FluidStackRenderer;
import org.example.stardust.spacemod.util.MouseUtil;

public class AlienPowerCoreScreen extends HandledScreen<AlienPowerCoreScreenHandler> {

    private static final Identifier TEXTURE =
            new Identifier(SpaceMod.MOD_ID,"textures/gui/range_spawner_gui.png");
    public AlienPowerCoreScreen(AlienPowerCoreScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    private TextFieldWidget commandTextField;


    @Override
    protected void init() {
        super.init();
        titleY = 10;
        playerInventoryTitleY = 10;
        commandTextField = new TextFieldWidget(textRenderer, width / 2 - 150 / 2, height / 2 - 50, 150, 20, Text.literal("Command"));
        this.addDrawableChild(commandTextField);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (commandTextField.isActive()) {
            if (keyCode == 256) {  // "Escape" key
                commandTextField.setFocused(false);
                return true;
            }

            if (keyCode == 257) {  // "Enter" key
                handleCommand(commandTextField.getText());
                commandTextField.setFocused(false);
                commandTextField.setText("");  // Clear the text field
                return true;
            }

            return commandTextField.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void handleCommand(String input) {

        if ("42".equals(input)) {
            this.client.player.sendMessage(Text.of("You have answered correctly"), false);
            BlockPos pos = this.handler.getBlockEntity().getPos();
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeBlockPos(this.handler.getBlockEntity().getPos());
            ClientPlayNetworking.send(new Identifier(SpaceMod.MOD_ID, "power_core_unlock_command"), buf);
            ModMessages.sendPowerCoreUnlockCommand(this.client.player, pos);
        }
    }




    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        commandTextField.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context,mouseX,mouseY);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        AlienPowerCoreBlockEntity blockEntity = this.handler.getBlockEntity();
        Text text = Text.of("The answer to life the universe and everything");
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
private boolean isMouseAboveArea(int pMouseX, int pMouseY, int x, int y, int offsetX, int offsetY, FluidStackRenderer renderer) {
    return MouseUtil.isMouseOver(pMouseX, pMouseY, x + offsetX, y + offsetY, renderer.getWidth(), renderer.getHeight());
}
    private boolean isMouseAboveArea(int pMouseX, int pMouseY, int x, int y, int offsetX, int offsetY, int width, int height) {
        return MouseUtil.isMouseOver(pMouseX, pMouseY, x + offsetX, y + offsetY, width, height);
    }


}
