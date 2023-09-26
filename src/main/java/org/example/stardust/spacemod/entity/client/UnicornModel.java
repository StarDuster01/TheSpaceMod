package org.example.stardust.spacemod.entity.client;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.example.stardust.spacemod.SpaceMod;
import org.example.stardust.spacemod.entity.custom.UnicornEntity;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class UnicornModel extends GeoModel<UnicornEntity> {
    @Override
    public Identifier getModelResource(UnicornEntity animatable) {
        return new Identifier(SpaceMod.MOD_ID, "geo/unicorn.geo.json");
    }

    @Override
    public Identifier getTextureResource(UnicornEntity animatable) {
        return new Identifier(SpaceMod.MOD_ID, "textures/entity/unicorn.png");
    }

    @Override
    public Identifier getAnimationResource(UnicornEntity animatable) {
        return new Identifier(SpaceMod.MOD_ID,"animations/unicorn.animation.json");
    }


    // Makes the head rotate to look at player
    @Override
    public void setCustomAnimations(UnicornEntity animatable, long instanceId, AnimationState<UnicornEntity> animationState) {
        CoreGeoBone head = getAnimationProcessor().getBone("head");

        if (head!= null) {
            EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            head.setRotX(entityData.headPitch()* MathHelper.RADIANS_PER_DEGREE);
            head.setRotY(entityData.netHeadYaw()* MathHelper.RADIANS_PER_DEGREE);
        }
        super.setCustomAnimations(animatable, instanceId, animationState);
    }
}
