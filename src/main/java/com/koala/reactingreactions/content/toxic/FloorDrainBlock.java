package com.koala.reactingreactions.content.toxic;

import com.koala.reactingreactions.registry.CRRBlockEntities;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A drain plate, placed like a slab: on the bottom of its space, or on the top so it sits flush with the floor when it
 * replaces a floor block.
 */
public class FloorDrainBlock extends Block implements IBE<FloorDrainBlockEntity> {
    public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
    private static final VoxelShape BOTTOM = Block.box(0, 0, 0, 16, 3, 16);
    private static final VoxelShape TOP = Block.box(0, 13, 0, 16, 16, 16);

    public FloorDrainBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(HALF, Half.BOTTOM));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HALF);
    }

    /** Like a slab: the top half when placed against a ceiling, or high up on a side. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        boolean top = face == Direction.DOWN
                || (face != Direction.UP && context.getClickLocation().y - context.getClickedPos().getY() > 0.5);
        return defaultBlockState().setValue(HALF, top ? Half.TOP : Half.BOTTOM);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HALF) == Half.TOP ? TOP : BOTTOM;
    }

    @Override
    public Class<FloorDrainBlockEntity> getBlockEntityClass() {
        return FloorDrainBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FloorDrainBlockEntity> getBlockEntityType() {
        return CRRBlockEntities.FLOOR_DRAIN.get();
    }
}
