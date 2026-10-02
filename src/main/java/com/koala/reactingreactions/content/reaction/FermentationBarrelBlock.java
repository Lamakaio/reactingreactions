package com.koala.reactingreactions.content.reaction;

import com.koala.reactingreactions.content.multiblock.FluidContainerInteraction;
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
import net.minecraft.world.phys.BlockHitResult;

/** The small-scale, single-block fermenter: needs no power, just time (see {@link FermentationBarrelBlockEntity}). */
public class FermentationBarrelBlock extends Block implements IBE<FermentationBarrelBlockEntity> {
    public FermentationBarrelBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Class<FermentationBarrelBlockEntity> getBlockEntityClass() {
        return FermentationBarrelBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FermentationBarrelBlockEntity> getBlockEntityType() {
        return CRRBlockEntities.FERMENTATION_BARREL.get();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hitResult) {
        ItemInteractionResult result = FluidContainerInteraction.tryInteract(player, hand, level, pos, hitResult);
        return result != null ? result : super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
}
