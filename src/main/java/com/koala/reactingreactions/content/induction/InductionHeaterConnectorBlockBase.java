package com.koala.reactingreactions.content.induction;

import com.koala.reactingreactions.registry.CRRBlockEntities;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/** Everything but the Electro Energetics wiring, so it loads without that mod; registered as is when it is absent. */
public class InductionHeaterConnectorBlockBase extends Block implements IBE<InductionHeaterConnectorBlockEntity> {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public InductionHeaterConnectorBlockBase(Properties properties) {
        super(properties);
        registerDefaultState(InductionHeaterPlateBlock.defaultCoil(stateDefinition.any().setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.NONE).setValue(FACING, Direction.NORTH)));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        InductionHeaterPlateBlock.heatEffects(state, level, pos, random);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlazeBurnerBlock.HEAT_LEVEL, FACING, InductionHeaterCoil.NORTH, InductionHeaterCoil.EAST, InductionHeaterCoil.SOUTH, InductionHeaterCoil.WEST, InductionHeaterCoil.CIRCLE);
    }

    /** The terminals face the player who placed it, up or down included. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
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
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        InductionHeaterPlateBlock.scald(state, entity);
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public Class<InductionHeaterConnectorBlockEntity> getBlockEntityClass() {
        return InductionHeaterConnectorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends InductionHeaterConnectorBlockEntity> getBlockEntityType() {
        return CRRBlockEntities.INDUCTION_HEATER_CONNECTOR.get();
    }
}
