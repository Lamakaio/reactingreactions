package com.koala.reactingreactions.content.fluids;

import com.koala.reactingreactions.registry.CRRBlockEntities;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A Create fluid pipe of HDPE: it never leaks (see FluidTransportBehaviourMixin). It connects and flows like Create's own, but a
 * Wrench does not turn it into a glass pipe and it cannot be encased. Drawn with Create's pipe models, retextured by
 * {@link PlasticPipeModel}.
 */
public class PlasticPipeBlock extends FluidPipeBlock {
    public PlasticPipeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntityType<? extends FluidPipeBlockEntity> getBlockEntityType() {
        return CRRBlockEntities.PLASTIC_PIPE.get();
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        return tryRemoveBracket(context) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                              BlockHitResult hitResult) {
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
