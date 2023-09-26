package org.example.stardust.spacemod.item.custom;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class CloakingDeviceItem extends Item {

    public CloakingDeviceItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if(!world.isClient) {
            ItemStack itemStack = user.getStackInHand(hand);

            if(user.hasStatusEffect(StatusEffects.INVISIBILITY)) {
                user.removeStatusEffect(StatusEffects.INVISIBILITY);
            } else {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, Integer.MAX_VALUE, 0, false, false, true));
            }
            itemStack.damage(1, user, (playerEntity) -> playerEntity.sendToolBreakStatus(hand));
        }

        return TypedActionResult.success(user.getStackInHand(hand));
    }
}
