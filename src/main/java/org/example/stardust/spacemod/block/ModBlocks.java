package org.example.stardust.spacemod.block;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import org.example.stardust.spacemod.SpaceMod;
import org.example.stardust.spacemod.block.custom.AltarBlock;
import org.example.stardust.spacemod.block.custom.CannonBlock;
import org.example.stardust.spacemod.block.custom.SpeedBlock;

public class ModBlocks {
    public static final Block BLOODY_STONE = registerBlock("bloody_stone",
            new Block(FabricBlockSettings.copyOf(Blocks.STONE).sounds(BlockSoundGroup.MOSS_BLOCK)));

    public static final Block ALTAR_BLOCK = registerBlock("altar_block",
            new AltarBlock(FabricBlockSettings.copyOf(Blocks.STONE).sounds(BlockSoundGroup.STONE)));

    public static final Block CANNON_BLOCK = registerBlock("cannon_block",
            new CannonBlock(FabricBlockSettings.copyOf(Blocks.STONE).sounds(BlockSoundGroup.STONE)));

    public static final Block SPEED_BLOCK = registerBlock("speed_block",
            new SpeedBlock(FabricBlockSettings.copyOf(Blocks.STONE).sounds(BlockSoundGroup.STONE)));



    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, new Identifier(SpaceMod.MOD_ID, name), block);
    }

    private static Item registerBlockItem(String name, Block block) {
        return Registry.register(Registries.ITEM, new Identifier(SpaceMod.MOD_ID, name),
                new BlockItem(block, new FabricItemSettings()));
    }

    public static void registerModBlocks() {
        SpaceMod.LOGGER.info("Registering ModBlocks for " + SpaceMod.MOD_ID);
    }

}