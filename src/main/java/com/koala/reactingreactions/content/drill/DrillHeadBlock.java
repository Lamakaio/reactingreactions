package com.koala.reactingreactions.content.drill;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The last block of a drill pipe. While its rig drills, the controller sets {@link #SPINNING}: the block then draws nothing and
 * {@link DerrickRenderer} draws its model turning with the rod.
 */
public class DrillHeadBlock extends Block {
    public static final BooleanProperty SPINNING = BooleanProperty.create("spinning");
    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 16, 13);

    public DrillHeadBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(SPINNING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SPINNING);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return state.getValue(SPINNING) ? RenderShape.INVISIBLE : RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
