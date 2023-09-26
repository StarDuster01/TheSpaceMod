package org.example.stardust.spacemod.entity.client;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.example.stardust.spacemod.SpaceMod;
import org.example.stardust.spacemod.entity.custom.FormicEntity;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class FormicModel extends GeoModel<FormicEntity> {
    @Override
    public Identifier getModelResource(FormicEntity animatable) {
        return new Identifier(SpaceMod.MOD_ID, "geo/formic.geo.json");
    }

    @Override
    public Identifier getTextureResource(FormicEntity animatable) {
        return new Identifier(SpaceMod.MOD_ID, "textures/entity/formic.png");
    }

    @Override
    public Identifier getAnimationResource(FormicEntity animatable) {
        return new Identifier(SpaceMod.MOD_ID, "animations/formic.animation.json");
    }

    @Override
    public void setCustomAnimations(FormicEntity animatable, long instanceId, AnimationState<FormicEntity> animationState) {
        CoreGeoBone head = getAnimationProcessor().getBone("head");

        if (head != null) {
            EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            head.setRotX(entityData.headPitch() * MathHelper.RADIANS_PER_DEGREE);
            head.setRotY(entityData.netHeadYaw() * MathHelper.RADIANS_PER_DEGREE);
        }
    }
}