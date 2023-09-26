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
import org.example.stardust.spacemod.block.custom.*;

public class ModBlocks {
    public static final Block BLOODY_STONE = registerBlock("bloody_stone",
            new Block(FabricBlockSettings.copyOf(Blocks.STONE).sounds(BlockSoundGroup.MOSS_BLOCK)));

    public static final Block ALTAR_BLOCK = registerBlock("altar_block",
            new AltarBlock(FabricBlockSettings.copyOf(Blocks.STONE).sounds(BlockSoundGroup.STONE)));

    public static final Block CANNON_BLOCK = registerBlock("cannon_block",
            new CannonBlock(FabricBlockSettings.copyOf(Blocks.STONE).sounds(BlockSoundGroup.STONE)));

    public static final Block SPEED_BLOCK = registerBlock("speed_block",
            new SpeedBlock(FabricBlockSettings.copyOf(Blocks.STONE).sounds(BlockSoundGroup.STONE)));

    public static final Block DOOM_FURNACE_BLOCK = registerBlock("doom_furnace_block",
            new DoomFurnaceBlock(FabricBlockSettings.copyOf(Blocks.STONE).sounds(BlockSoundGroup.STONE)));

    public static final Block TNTX_BLOCK = registerBlock("tntx_block",
            new TntXBlock(FabricBlockSettings.copyOf(Blocks.TNT).sounds(BlockSoundGroup.WART_BLOCK)));

    public static final Block CUBE_DIGGER_TNT_BLOCK = registerBlock("cube_digger_tnt_block",
            new CubeDiggerTntBlock(FabricBlockSettings.copyOf(Blocks.TNT).sounds(BlockSoundGroup.WART_BLOCK)));



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