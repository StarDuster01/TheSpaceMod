package org.example.stardust.spacemod.util;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.kyrptonaught.customportalapi.api.CustomPortalBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.block.ComposterBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.VillagerProfession;
import org.example.stardust.spacemod.SpaceMod;
import org.example.stardust.spacemod.item.ModItems;

public class ModRegistries {
    public static void registerModStuffs() {
        createPortal();
    }

    private static void createPortal() {
        CustomPortalBuilder.beginPortal()
                .frameBlock(Blocks.NETHERITE_BLOCK)
                .lightWithItem(ModItems.PURE_BLOOD)
                .destDimID(new Identifier(SpaceMod.MOD_ID, "spacedim"))
                .tintColor(0x0077BE)
                .registerPortal();
    }
}
