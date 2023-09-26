package org.example.stardust.spacemod.item.custom;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.SwordItem;
import net.minecraft.world.World;
import org.example.stardust.spacemod.item.ModItems;

public class SacrificialKnife extends SwordItem {

    public SacrificialKnife(ToolMaterial material, int attackDamage, float attackSpeed, Settings settings) {
        super(material, attackDamage, attackSpeed, settings);
    }


}
