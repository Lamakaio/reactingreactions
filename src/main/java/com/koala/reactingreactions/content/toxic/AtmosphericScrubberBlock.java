package com.koala.reactingreactions.content.toxic;

import com.koala.reactingreactions.content.multiblock.FluidContainerInteraction;
import com.koala.reactingreactions.registry.CRRBlockEntities;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Rotation enters through the back face, like the Derrick's controller. Right-click with Activated Carbon or a Lye bucket to load it. */
public class AtmosphericScrubberBlock extends HorizontalKineticBlock implements IBE<AtmosphericScrubberBlockEntity> {
    public AtmosphericScrubberBlock(Properties properties) {
        super(properties);
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
    public Class<AtmosphericScrubberBlockEntity> getBlockEntityClass() {
        return AtmosphericScrubberBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends AtmosphericScrubberBlockEntity> getBlockEntityType() {
        return CRRBlockEntities.ATMOSPHERIC_SCRUBBER.get();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hitResult) {
        ItemInteractionResult fluid = FluidContainerInteraction.tryInteract(player, hand, level, pos, hitResult);
        if (fluid != null) {
            return fluid;
        }
        if (level.getBlockEntity(pos) instanceof AtmosphericScrubberBlockEntity scrubber && scrubber.getCarbon().isItemValid(0, stack)) {
            ItemStack left = scrubber.getCarbon().insertItem(0, stack.copy(), level.isClientSide);
            if (left.getCount() != stack.getCount()) {
                if (!level.isClientSide && !player.getAbilities().instabuild) {
                    stack.setCount(left.getCount());
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
}
