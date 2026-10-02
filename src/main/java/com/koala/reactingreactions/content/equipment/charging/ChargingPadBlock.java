package com.koala.reactingreactions.content.equipment.charging;

import com.koala.reactingreactions.content.multiblock.FluidContainerInteraction;
import com.koala.reactingreactions.registry.CRRBlockEntities;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A trapdoor-thin plate: standing on it fills your tank items, see {@link ChargingPadBlockEntity}. Buckets fill or drain its tanks. */
public class ChargingPadBlock extends Block implements IBE<ChargingPadBlockEntity> {
    public static final int HEIGHT_PX = 3;
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, HEIGHT_PX, 16);

    public ChargingPadBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return SHAPE;
    }

    /** A fluid container fills or drains the pad's tanks; a tank item in hand is left to the stand-on-it charging instead. */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hitResult) {
        if (!ChargingPadBlockEntity.isChargeable(stack)) {
            var containerResult = FluidContainerInteraction.tryInteract(player, hand, level, pos, hitResult);
            if (containerResult != null) {
                return containerResult;
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public Class<ChargingPadBlockEntity> getBlockEntityClass() {
        return ChargingPadBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ChargingPadBlockEntity> getBlockEntityType() {
        return CRRBlockEntities.CHARGING_PAD.get();
    }
}
