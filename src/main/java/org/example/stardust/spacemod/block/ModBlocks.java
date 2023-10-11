package org.example.stardust.spacemod.block;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.sapling.JungleSaplingGenerator;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import org.example.stardust.spacemod.SpaceMod;
import org.example.stardust.spacemod.block.custom.*;
import org.example.stardust.spacemod.block.custom.explosives.CubeDiggerTntBlock;
import org.example.stardust.spacemod.block.custom.explosives.NukeBlock;
import org.example.stardust.spacemod.block.custom.explosives.TntXBlock;
import org.example.stardust.spacemod.block.custom.explosives.ZombieTntBlock;
import org.example.stardust.spacemod.world.gen.tree.GiantJungleSaplingGenerator;

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

    public static final Block COAL_GENERATOR_BLOCK = registerBlock("coal_generator_block",
            new CoalGeneratorBlock(FabricBlockSettings.copyOf(Blocks.STONE).sounds(BlockSoundGroup.STONE)));

    public static final Block TNTX_BLOCK = registerBlock("tntx_block",
            new TntXBlock(FabricBlockSettings.copyOf(Blocks.TNT).sounds(BlockSoundGroup.WART_BLOCK)));

    public static final Block NUKE_BLOCK = registerBlock("nuke_block",
            new NukeBlock(FabricBlockSettings.copyOf(Blocks.TNT).sounds(BlockSoundGroup.WART_BLOCK)));
    public static final Block CUBE_DIGGER_TNT_BLOCK = registerBlock("cube_digger_tnt_block",
            new CubeDiggerTntBlock(FabricBlockSettings.copyOf(Blocks.TNT).sounds(BlockSoundGroup.WART_BLOCK)));
    public static final Block ZOMBIE_TNT_BLOCK = registerBlock("zombie_tnt_block",
            new ZombieTntBlock(FabricBlockSettings.copyOf(Blocks.TNT).sounds(BlockSoundGroup.WART_BLOCK)));

    public static final Block EXCAVATOR_BLOCK = registerBlock("excavator_block",
            new ExcavatorBlock(FabricBlockSettings.copyOf(Blocks.STONE).sounds(BlockSoundGroup.STONE)));
    public static final Block MINING_BORE_BLOCK = registerBlock("mining_bore_block",
            new MiningBoreBlock(FabricBlockSettings.copyOf(Blocks.STONE).sounds(BlockSoundGroup.STONE)));

    public static final Block RANGE_SPAWNER_BLOCK = registerBlock("range_spawner_block",
            new RangeSpawnerBlock(FabricBlockSettings.copyOf(Blocks.STONE).sounds(BlockSoundGroup.STONE)));
    public static final Block WALLPLACER = registerBlock("wallplacer",
            new WallPlacerBlock(FabricBlockSettings.copyOf(Blocks.STONE).sounds(BlockSoundGroup.STONE)));
    public static final Block FUSION_REACTOR_BLOCK = registerBlock("fusion_reactor_block",
            new FusionReactorBlock(FabricBlockSettings.copyOf(Blocks.STONE).sounds(BlockSoundGroup.STONE)));

    public static final Block GIANT_JUNGLE_SAPLING = registerBlock("giant_jungle_sapling", new SaplingBlock(new GiantJungleSaplingGenerator(), FabricBlockSettings.copyOf(Blocks.JUNGLE_SAPLING).strength(1f)));




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