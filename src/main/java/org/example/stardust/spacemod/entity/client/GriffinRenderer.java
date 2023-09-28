package org.example.stardust.spacemod.entity.client;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.example.stardust.spacemod.SpaceMod;
import org.example.stardust.spacemod.entity.custom.GriffinEntity;
import org.example.stardust.spacemod.entity.custom.UnicornEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GriffinRenderer extends GeoEntityRenderer<GriffinEntity> {
    public GriffinRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new GriffinModel());
    }

    @Override
    public Identifier getTextureLocation(GriffinEntity animatable) {
        return new Identifier(SpaceMod.MOD_ID, "textures/entity/griffin.png");
    }

    @Override
    public void render(GriffinEntity entity, float entityYaw, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight) {

        if(entity.isBaby()) {
            poseStack.scale(0.4f,0.4f,0.4f);
        }

        poseStack.scale(2.5f,2.5f,2.5f);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
