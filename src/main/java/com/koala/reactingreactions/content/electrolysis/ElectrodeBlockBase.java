package com.koala.reactingreactions.content.electrolysis;

import com.koala.reactingreactions.registry.CRRBlockEntities;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Everything but the Electro Energetics wiring, so it loads without that mod; registered as is when it is absent. */
public class ElectrodeBlockBase extends Block implements IBE<ElectrodeInfoBlockEntity> {
    @Override
    public Class<ElectrodeInfoBlockEntity> getBlockEntityClass() {
        return ElectrodeInfoBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ElectrodeInfoBlockEntity> getBlockEntityType() {
        return CRRBlockEntities.ELECTRODE_INFO.get();
    }

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private static final VoxelShape Y_AXIS_AABB = Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);
    private static final VoxelShape Z_AXIS_AABB = Block.box(3.0, 3.0, 0.0, 13.0, 13.0, 16.0);
    private static final VoxelShape X_AXIS_AABB = Block.box(0.0, 3.0, 3.0, 16.0, 13.0, 13.0);

    public ElectrodeBlockBase(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getClickedFace();
        BlockState existing = context.getLevel().getBlockState(context.getClickedPos().relative(direction.getOpposite()));
        return existing.is(this) && existing.getValue(FACING) == direction
                ? defaultBlockState().setValue(FACING, direction.getOpposite())
                : defaultBlockState().setValue(FACING, direction);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING).getAxis()) {
            case X -> X_AXIS_AABB;
            case Z -> Z_AXIS_AABB;
            case Y -> Y_AXIS_AABB;
        };
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }
}
