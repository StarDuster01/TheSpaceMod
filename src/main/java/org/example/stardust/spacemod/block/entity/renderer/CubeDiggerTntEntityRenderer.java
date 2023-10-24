package org.example.stardust.spacemod.block.entity.renderer;


import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Identifier;
import org.example.stardust.spacemod.block.entity.explosives.CubeDiggerTntEntity;


public class CubeDiggerTntEntityRenderer extends EntityRenderer<CubeDiggerTntEntity> {

    private static final Identifier CUBE_DIGGER_TNT_TEXTURE = new Identifier("textures/entity/cube_digger_tnt.png");

    public CubeDiggerTntEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(CubeDiggerTntEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(CubeDiggerTntEntity entity) {
        return CUBE_DIGGER_TNT_TEXTURE;
    }
}

