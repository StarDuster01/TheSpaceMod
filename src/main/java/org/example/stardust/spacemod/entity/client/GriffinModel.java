package org.example.stardust.spacemod.entity.client;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.example.stardust.spacemod.SpaceMod;
import org.example.stardust.spacemod.entity.custom.GriffinEntity;
import org.example.stardust.spacemod.entity.custom.UnicornEntity;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class GriffinModel extends GeoModel<GriffinEntity> {
    @Override
    public Identifier getModelResource(GriffinEntity animatable) {
        return new Identifier(SpaceMod.MOD_ID, "geo/griffin.geo.json");
    }

    @Override
    public Identifier getTextureResource(GriffinEntity animatable) {
        return new Identifier(SpaceMod.MOD_ID, "textures/entity/griffin.png");
    }

    @Override
    public Identifier getAnimationResource(GriffinEntity animatable) {
        return new Identifier(SpaceMod.MOD_ID,"animations/griffin.animation.json");
    }


    // Makes the head rotate to look at player
    @Override
    public void setCustomAnimations(GriffinEntity animatable, long instanceId, AnimationState<GriffinEntity> animationState) {
        CoreGeoBone head = getAnimationProcessor().getBone("head");

        if (head!= null) {
            EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            head.setRotX(entityData.headPitch()* MathHelper.RADIANS_PER_DEGREE);
            head.setRotY(entityData.netHeadYaw()* MathHelper.RADIANS_PER_DEGREE);
        }
        super.setCustomAnimations(animatable, instanceId, animationState);
    }
}
