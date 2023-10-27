package org.example.stardust.spacemod.item.custom;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Equipment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.example.stardust.spacemod.block.ModBlocks;
public class UraniumRod extends Item implements Equipment {
    public UraniumRod(Settings settings) {
        super(settings);
    }
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        if (!world.isClient) {
            System.out.println("UraniumRod is attempting to form the MediumCoalGeneratorBlock at position: " + pos);
            if (isMultiblockFormed(world, pos)) {
                System.out.println("Detected 4x4 iron block structure. Replacing with MediumCoalGeneratorBlock...");
                replaceMultiblockWithGenerator(world, pos);
                return ActionResult.SUCCESS;
            } else {
                System.out.println("Failed to detect 4x4 iron block structure.");
            }
        }
        return ActionResult.PASS;
    }
    private boolean isMultiblockFormed(World world, BlockPos pos) {
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                for (int z = 0; z < 4; z++) {
                    BlockPos checkPos = pos.add(x, y, z);
                    Block block = world.getBlockState(checkPos).getBlock();
                    if (block != Blocks.IRON_BLOCK) {
                        System.out.println("Block at " + checkPos + " is not an iron block. It's: " + block);
                        return false;
                    }
                }
            }
        }
        return true;
    }
    private void replaceMultiblockWithGenerator(World world, BlockPos pos) {
        // Remove all iron blocks
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                for (int z = 0; z < 4; z++) {
                    BlockPos replacePos = pos.add(x, y, z);
                    world.setBlockState(replacePos, Blocks.AIR.getDefaultState());
                }
            }
        }
        BlockPos middlePos = pos.add(1, 1, 1);
        world.setBlockState(middlePos, ModBlocks.MEDIUM_COAL_GENERATOR_BLOCK.getDefaultState());
    }
    @Override
    public EquipmentSlot getSlotType() {
        return EquipmentSlot.HEAD;
    }
    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient && entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            if (player.getEquippedStack(EquipmentSlot.HEAD).getItem() instanceof UraniumRod) {
                applyNightVision(player);
            }
        }
    }

    private void applyNightVision(PlayerEntity player) {
        StatusEffectInstance currentEffect = player.getStatusEffect(StatusEffects.NIGHT_VISION);
        if (currentEffect == null || currentEffect.getDuration() <= 1024) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 220, 0, true, false, true));

        }
    }
}



