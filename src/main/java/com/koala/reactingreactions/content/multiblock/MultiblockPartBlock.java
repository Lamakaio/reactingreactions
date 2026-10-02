package com.koala.reactingreactions.content.multiblock;

import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/** A wall or controller of a hollow-box machine. Buckets and bottles used on it fill or drain the machine. */
public class MultiblockPartBlock<T extends BlockEntity> extends Block implements IBE<T> {
    private final Class<T> blockEntityClass;
    private final Supplier<? extends BlockEntityType<? extends T>> blockEntityType;
    @Nullable
    private final Supplier<MultiblockControllerBlockEntity.Spec<?>> spec;

    /** {@code spec} is only needed on walls, to find their controller. */
    public MultiblockPartBlock(Properties properties, Class<T> blockEntityClass, Supplier<? extends BlockEntityType<? extends T>> blockEntityType,
                               @Nullable Supplier<MultiblockControllerBlockEntity.Spec<?>> spec) {
        super(properties);
        this.blockEntityClass = blockEntityClass;
        this.blockEntityType = blockEntityType;
        this.spec = spec;
    }

    @Nullable
    public MultiblockControllerBlockEntity.Spec<?> spec() {
        return spec == null ? null : spec.get();
    }

    @Override
    public Class<T> getBlockEntityClass() {
        return blockEntityClass;
    }

    @Override
    public BlockEntityType<? extends T> getBlockEntityType() {
        return blockEntityType.get();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                              BlockHitResult hitResult) {
        ItemInteractionResult result = FluidContainerInteraction.tryInteract(player, hand, level, pos, hitResult);
        return result != null ? result : super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    /** A see-through shell whose side walls turn into windows while the machine is formed. */
    public static class Windowed<T extends BlockEntity> extends MultiblockPartBlock<T> {
        public Windowed(Properties properties, Class<T> blockEntityClass, Supplier<? extends BlockEntityType<? extends T>> blockEntityType,
                        @Nullable Supplier<MultiblockControllerBlockEntity.Spec<?>> spec) {
            super(properties, blockEntityClass, blockEntityType, spec);
            registerDefaultState(MultiblockWindows.withDefaults(stateDefinition.any()));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            MultiblockWindows.addProperties(builder);
        }

        @Override
        protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
            return true;
        }

        @Override
        protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
            return 1.0F;
        }
    }

    /** A shell block of a fixed-size machine, showing its piece of the whole machine while formed (see {@link MachineTiers}). */
    public static class Tiered<T extends BlockEntity> extends MultiblockPartBlock<T> {
        public Tiered(Properties properties, Class<T> blockEntityClass, Supplier<? extends BlockEntityType<? extends T>> blockEntityType,
                      @Nullable Supplier<MultiblockControllerBlockEntity.Spec<?>> spec) {
            super(properties, blockEntityClass, blockEntityType, spec);
            registerDefaultState(stateDefinition.any().setValue(MachineTiers.PART, 0).setValue(MachineTiers.FACING, Direction.NORTH)
                    .setValue(MachineTiers.PIPED, false).setValue(MachineTiers.SEALED, false));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(MachineTiers.PART, MachineTiers.FACING, MachineTiers.PIPED, MachineTiers.SEALED);
        }

        /** Sneaking with an empty hand on a formed machine takes its last installed upgrade back out. */
        @Override
        protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
            if (!player.isShiftKeyDown() || state.getValue(MachineTiers.PART) == 0) {
                return super.useWithoutItem(state, level, pos, player, hitResult);
            }
            MultiblockControllerBlockEntity<?> machine = MultiblockControllerBlockEntity.of(level, pos);
            if (machine == null || level.isClientSide) {
                return machine == null ? InteractionResult.PASS : InteractionResult.SUCCESS;
            }
            ItemStack removed = machine.removeUpgrade();
            if (removed.isEmpty()) {
                return InteractionResult.PASS;
            }
            player.getInventory().placeItemBackInInventory(removed);
            return InteractionResult.SUCCESS;
        }
    }
}
