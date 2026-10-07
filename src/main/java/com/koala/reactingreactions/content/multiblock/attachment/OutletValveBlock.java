package com.koala.reactingreactions.content.multiblock.attachment;

import com.koala.reactingreactions.registry.CRRBlockEntities;
import com.simibubi.create.content.fluids.pump.PumpBlock;
import com.simibubi.create.content.fluids.pump.PumpBlockEntity;

import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

import java.util.List;

/**
 * The Outlet Valve: a Create pump with no shaft (as Electro Energetics' pump is), facing away from the machine wall it is placed
 * against, that runs on its own while within its machine's attachment slots (see {@link OutletValveBlockEntity}). Its filter: a
 * filled container used on it sets that fluid; an empty hand cycles through the fluids in the machine and back to any output;
 * sneaking with an empty hand resets it to any output.
 */
public class OutletValveBlock extends PumpBlock implements MachineAttachment {
    private final VoxelShaper shape;

    /** {@code shape} is drawn with the machine to the south. */
    public OutletValveBlock(Properties properties, VoxelShape shape) {
        super(properties);
        this.shape = VoxelShaper.forHorizontal(shape, Direction.SOUTH);
    }

    @Override
    public Kind kind() {
        return Kind.OUTLET_VALVE;
    }

    /** Facing out, away from the wall it was placed against. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        Direction out = face.getAxis().isHorizontal() ? face : context.getHorizontalDirection().getOpposite();
        return defaultBlockState().setValue(FACING, out);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Never placed facing up or down, but every state needs a shape (the game caches them all).
        Direction towards = MachineAttachment.mountedTowards(state);
        return shape.get(towards.getAxis().isHorizontal() ? towards : Direction.SOUTH);
    }

    // No shaft and no cog: it runs by itself. A wrench does not turn it round, off its machine.
    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return false;
    }

    @Override
    public boolean isSmallCog() {
        return false;
    }

    @Override
    public boolean isLargeCog() {
        return false;
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        return originalState;
    }

    /** The wall behind hands the valve its filtered outlet (see OutletValveBlockEntity#sourceFor): tell it to look again. */
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        level.invalidateCapabilities(pos.relative(MachineAttachment.mountedTowards(state)));
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        level.invalidateCapabilities(pos.relative(MachineAttachment.mountedTowards(state)));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                              BlockHitResult hit) {
        FluidStack held = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
        if (held.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        setFilter(level, pos, player, held.getFluid());
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof OutletValveBlockEntity valve)) {
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown()) {
            setFilter(level, pos, player, Fluids.EMPTY);
        } else {
            List<Fluid> choices = valve.machineFluids();
            int at = choices.indexOf(valve.filter());
            // The next fluid in the machine; past the last one, back to any output.
            setFilter(level, pos, player, at + 1 < choices.size() ? choices.get(at + 1) : Fluids.EMPTY);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void setFilter(Level level, BlockPos pos, Player player, Fluid fluid) {
        if (level.isClientSide || !(level.getBlockEntity(pos) instanceof OutletValveBlockEntity valve)) {
            return;
        }
        valve.setFilter(fluid);
        player.displayClientMessage(Component.literal("Outlet Valve lets out: ").append(OutletValveBlockEntity.describe(fluid)), true);
    }

    @Override
    public BlockEntityType<? extends PumpBlockEntity> getBlockEntityType() {
        return CRRBlockEntities.OUTLET_VALVE.get();
    }
}
