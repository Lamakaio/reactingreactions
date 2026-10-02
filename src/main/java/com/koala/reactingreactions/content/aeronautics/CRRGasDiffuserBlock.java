package com.koala.reactingreactions.content.aeronautics;

import com.koala.reactingreactions.registry.CRRAeroBlocks;

import dev.eriksonn.aeronautics.content.blocks.hot_air.hot_air_burner.HotAirBurnerBlock;
import dev.eriksonn.aeronautics.content.blocks.hot_air.hot_air_burner.HotAirBurnerBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidUtil;

/** Aeronautics' Hot Air Burner block, with our block entity and bucket filling. */
public class CRRGasDiffuserBlock extends HotAirBurnerBlock {
    public CRRGasDiffuserBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntityType<? extends HotAirBurnerBlockEntity> getBlockEntityType() {
        return CRRAeroBlocks.GAS_DIFFUSER_BE.get();
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            Direction side = hitResult.getDirection();
            if (FluidUtil.interactWithFluidHandler(player, hand, level, pos, side)) {
                return ItemInteractionResult.sidedSuccess(false);
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
}
