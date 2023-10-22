package org.example.stardust.spacemod.misc;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface IToolDrop {

    ItemStack getToolDrop(PlayerEntity p0);
}