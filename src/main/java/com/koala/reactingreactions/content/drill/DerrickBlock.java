package com.koala.reactingreactions.content.drill;

import com.koala.reactingreactions.content.multiblock.FluidContainerInteraction;
import com.koala.reactingreactions.content.multiblock.MachineTiers;
import com.koala.reactingreactions.registry.CRRBlockEntities;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

/** The corner and mid blocks of a Derrick: they carry the structure and pass fluid I/O through to the controller. */
public class DerrickBlock extends Block implements IBE<DerrickBlockEntity> {
    public DerrickBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(MachineTiers.PART, 0));
    }

    /** Its piece of the formed tower (see {@link DrillRig#setLook}); 0 when not part of one. */
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MachineTiers.PART);
    }

    @Override
    public Class<DerrickBlockEntity> getBlockEntityClass() {
        return DerrickBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DerrickBlockEntity> getBlockEntityType() {
        return CRRBlockEntities.DERRICK_BLOCK.get();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hitResult) {
        ItemInteractionResult result = FluidContainerInteraction.tryInteract(player, hand, level, pos, hitResult);
        return result != null ? result : super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
}
