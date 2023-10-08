package org.example.stardust.spacemod.entity.client;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.example.stardust.spacemod.SpaceMod;
import org.example.stardust.spacemod.entity.custom.FormicEntity;
import org.example.stardust.spacemod.entity.custom.GiantSalamanderEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GiantSalamanderRenderer extends GeoEntityRenderer<GiantSalamanderEntity> {
    public GiantSalamanderRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new GiantSalamanderModel());
    }

    @Override
    public Identifier getTextureLocation(GiantSalamanderEntity animatable) {
        return new Identifier(SpaceMod.MOD_ID, "textures/entity/giant_salamander.png");
    }

    @Override
    public void render(GiantSalamanderEntity entity, float entityYaw, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight) {

        if(entity.isBaby()) {
            poseStack.scale(0.2f,0.2f,0.2f);
        }
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
