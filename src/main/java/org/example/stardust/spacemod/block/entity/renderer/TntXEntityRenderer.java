package org.example.stardust.spacemod.block.entity.renderer;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Identifier;
import org.example.stardust.spacemod.block.entity.TntXEntity;

public class TntXEntityRenderer extends EntityRenderer<TntXEntity> {


    private static final Identifier TNT_TEXTURE = new Identifier("textures/entity/tntx_side.png");

    public TntXEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(TntXEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(TntXEntity entity) {
        return TNT_TEXTURE;
    }
}