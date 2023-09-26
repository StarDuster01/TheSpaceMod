package org.example.stardust.spacemod.entity.client;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.example.stardust.spacemod.SpaceMod;
import org.example.stardust.spacemod.entity.custom.UnicornEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class UnicornRenderer extends GeoEntityRenderer<UnicornEntity> {
    public UnicornRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new UnicornModel());
    }

    @Override
    public Identifier getTextureLocation(UnicornEntity animatable) {
        return new Identifier(SpaceMod.MOD_ID, "textures/entity/unicorn.png");
    }

    @Override
    public void render(UnicornEntity entity, float entityYaw, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight) {

        if(entity.isBaby()) {
            poseStack.scale(0.4f,0.4f,0.4f);
        }
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
