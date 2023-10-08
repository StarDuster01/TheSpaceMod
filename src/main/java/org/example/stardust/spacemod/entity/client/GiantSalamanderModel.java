package org.example.stardust.spacemod.entity.client;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.example.stardust.spacemod.SpaceMod;
import org.example.stardust.spacemod.entity.custom.FormicEntity;
import org.example.stardust.spacemod.entity.custom.GiantSalamanderEntity;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class GiantSalamanderModel extends GeoModel<GiantSalamanderEntity> {
    @Override
    public Identifier getModelResource(GiantSalamanderEntity animatable) {
        return new Identifier(SpaceMod.MOD_ID, "geo/giant_salamander.geo.json");
    }

    @Override
    public Identifier getTextureResource(GiantSalamanderEntity animatable) {
        return new Identifier(SpaceMod.MOD_ID, "textures/entity/giant_salamander.png");
    }

    @Override
    public Identifier getAnimationResource(GiantSalamanderEntity animatable) {
        return new Identifier(SpaceMod.MOD_ID, "animations/giant_salamander.animation.json");
    }


}