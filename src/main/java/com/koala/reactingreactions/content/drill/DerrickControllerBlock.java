package com.koala.reactingreactions.content.drill;

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

/**
 * The top of a Derrick: a top drive, turned by a shaft coming down into it from above. It holds the lubricant and crude oil
 * tanks.
 */
public class DerrickControllerBlock extends HorizontalKineticBlock implements IBE<DerrickControllerBlockEntity> {
    public DerrickControllerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == Direction.UP;
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return Direction.Axis.Y;
    }

    @Override
    public Class<DerrickControllerBlockEntity> getBlockEntityClass() {
        return DerrickControllerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DerrickControllerBlockEntity> getBlockEntityType() {
        return CRRBlockEntities.DERRICK_CONTROLLER.get();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hitResult) {
        ItemInteractionResult result = FluidContainerInteraction.tryInteract(player, hand, level, pos, hitResult);
        return result != null ? result : super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
}
