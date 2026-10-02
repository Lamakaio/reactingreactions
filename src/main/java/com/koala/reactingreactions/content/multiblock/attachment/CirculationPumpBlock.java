package com.koala.reactingreactions.content.multiblock.attachment;

import com.koala.reactingreactions.registry.CRRBlockEntities;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.block.IBE;

import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Stirs a machine's contents around with the rotation it gets from behind, speeding its recipes up. Faces the machine. */
public class CirculationPumpBlock extends HorizontalKineticBlock implements IBE<KineticBlockEntity>, MachineAttachment {
    private static final VoxelShaper SHAPE = VoxelShaper.forHorizontal(Shapes.or(Block.box(3, 2, 3, 13, 13, 13), Block.box(4, 4, 13, 12, 12, 16),
            Block.box(6, 6, 0, 10, 10, 3)), Direction.SOUTH);

    public CirculationPumpBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Kind kind() {
        return Kind.CIRCULATION_PUMP;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        Direction towards = face.getAxis().isHorizontal() ? face.getOpposite() : context.getHorizontalDirection();
        return defaultBlockState().setValue(HORIZONTAL_FACING, towards);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE.get(state.getValue(HORIZONTAL_FACING));
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(HORIZONTAL_FACING).getOpposite();
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getAxis();
    }

    @Override
    public Class<KineticBlockEntity> getBlockEntityClass() {
        return KineticBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
        return CRRBlockEntities.CIRCULATION_PUMP.get();
    }
}
