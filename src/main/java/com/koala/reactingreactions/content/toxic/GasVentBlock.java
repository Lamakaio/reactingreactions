package com.koala.reactingreactions.content.toxic;

import com.koala.reactingreactions.registry.CRRBlockEntities;
import com.simibubi.create.foundation.block.IBE;

import net.createmod.catnip.math.VoxelShaper;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A pipe end that lets the gases pumped into its back out into the air, its outlet pointing away from the face it was placed on. */
public class GasVentBlock extends Block implements IBE<GasVentBlockEntity> {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    private static final VoxelShaper SHAPE = VoxelShaper.forDirectional(Shapes.or(Block.box(3.5, 0, 3.5, 12.5, 2, 12.5), Block.box(5, 2, 5, 11, 12, 11),
            Block.box(3, 12, 3, 13, 16, 13)), Direction.UP);

    public GasVentBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE.get(state.getValue(FACING));
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public Class<GasVentBlockEntity> getBlockEntityClass() {
        return GasVentBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends GasVentBlockEntity> getBlockEntityType() {
        return CRRBlockEntities.GAS_VENT.get();
    }
}
