package org.example.stardust.spacemod.block.custom;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;

import net.minecraft.item.ItemPlacementContext;

import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

import org.example.stardust.spacemod.block.entity.ConductorBlockEntity;

import org.jetbrains.annotations.Nullable;

public class ConductorBlock extends BlockWithEntity implements BlockEntityProvider {
    @Override
    public boolean isShapeFullCube(BlockState state, BlockView world, BlockPos pos) {
        return false;
    }

    public ConductorBlock(Settings settings) {
        super(settings);
    }

    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    private static final VoxelShape CORE_SHAPE = Block.createCuboidShape(5, 5, 5, 11, 11, 11);
    private static final VoxelShape WEST_SHAPE = Block.createCuboidShape(0, 5, 5, 5, 11, 11);
    private static final VoxelShape EAST_SHAPE = Block.createCuboidShape(11, 5, 5, 16, 11, 11);
    private static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(5, 5, 0, 11, 11, 5);
    private static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(5, 5, 11, 11, 11, 16);
    private static final VoxelShape BOTTOM_SHAPE = Block.createCuboidShape(5, 0, 5, 11, 5, 11);
    private static final VoxelShape TOP_SHAPE = Block.createCuboidShape(5, 11, 5, 11, 16, 11);

    private static final VoxelShape SHAPE = VoxelShapes.union(
            CORE_SHAPE,
            WEST_SHAPE,
            EAST_SHAPE,
            NORTH_SHAPE,
            SOUTH_SHAPE,
            BOTTOM_SHAPE,
            TOP_SHAPE
    );

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }


    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ConductorBlockEntity(pos, state);
    }
}
