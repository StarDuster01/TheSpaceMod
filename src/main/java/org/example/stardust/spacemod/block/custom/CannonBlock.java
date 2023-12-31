package org.example.stardust.spacemod.block.custom;

import net.minecraft.block.*;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CannonBlock extends HorizontalFacingBlock {
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

    public CannonBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }


    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction facing = ctx.getHorizontalPlayerFacing();
        return this.getDefaultState().with(FACING, facing);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos neighborPos, boolean moved) {
        if (world.isClient) return;

        boolean receivingPower = true;

        if (receivingPower) {
            for(Direction direction : Direction.values()) {
                BlockPos neighbor = pos.offset(direction);

                if (world.getBlockState(neighbor).isOf(Blocks.TNT)) {
                    world.setBlockState(neighbor, Blocks.AIR.getDefaultState(), 3);
                    TntEntity tntEntity = new TntEntity(world, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, null);
                    tntEntity.setFuse((short) 60);
                    Direction facing = state.get(FACING);
                    Vec3d velocity = new Vec3d(facing.getOffsetX(), 0.7, facing.getOffsetZ()).normalize().multiply(2.5);
                    tntEntity.setVelocity(velocity);
                    world.spawnEntity(tntEntity);
                    break;
                }
            }
        }
    }



    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        tooltip.add(Text.of("Throws TNT"));
        super.appendTooltip(stack, world, tooltip, options);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

}
