package com.koala.reactingreactions.content.multiblock.attachment;

import com.koala.reactingreactions.content.multiblock.MultiblockControllerBlockEntity;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.createmod.catnip.math.VoxelShaper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A non-kinetic machine attachment, placed against a wall (facing it). The gauge kind also reads out a comparator signal. */
public class AttachmentBlock extends HorizontalDirectionalBlock implements MachineAttachment, IWrenchable {
    public enum Reading implements StringRepresentable {
        PROGRESS("progress"), FILL("fill");

        private final String name;

        Reading(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public static final EnumProperty<Reading> READING = EnumProperty.create("reading", Reading.class);

    private final Kind kind;
    private final VoxelShaper shape;

    /** {@code shape} is drawn with the machine to the south. */
    public AttachmentBlock(Properties properties, Kind kind, VoxelShape shape) {
        super(properties);
        this.kind = kind;
        this.shape = VoxelShaper.forHorizontal(shape, Direction.SOUTH);
        BlockState state = stateDefinition.any().setValue(FACING, Direction.NORTH);
        registerDefaultState(kind == Kind.GAUGE ? state.setValue(READING, Reading.PROGRESS) : state);
    }

    @Override
    public Kind kind() {
        return kind;
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        throw new UnsupportedOperationException("not data-driven");
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        // Called before the constructor's fields are set, so the gauge's property is added to every kind and simply unused.
        builder.add(READING);
    }

    /** Facing the wall it was placed against. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        Direction towards = face.getAxis().isHorizontal() ? face.getOpposite() : context.getHorizontalDirection();
        return defaultBlockState().setValue(FACING, towards);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape.get(state.getValue(FACING));
    }

    /** A wrench switches a gauge between reading progress and reading how full the fullest output is. */
    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if (kind != Kind.GAUGE) {
            return IWrenchable.super.onWrenched(state, context);
        }
        Reading next = state.getValue(READING) == Reading.PROGRESS ? Reading.FILL : Reading.PROGRESS;
        Level level = context.getLevel();
        if (!level.isClientSide) {
            level.setBlock(context.getClickedPos(), state.setValue(READING, next), Block.UPDATE_ALL);
            if (context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(Component.literal(next == Reading.PROGRESS ? "Gauge reads progress" : "Gauge reads output fill"), true);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return kind == Kind.GAUGE;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        MultiblockControllerBlockEntity<?> machine = MachineAttachments.machineFor(level, pos, state);
        return machine == null ? 0 : machine.gaugeSignal(state.getValue(READING) == Reading.FILL);
    }
}
