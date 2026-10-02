package com.koala.reactingreactions.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * "Super" fertilizer: instantly bonemeals every valid target in a radius
 * around the clicked block in a single use, instead of one block per click.
 */
public class SuperBoneMealItem extends Item {
    private static final int RADIUS = 4;

    public SuperBoneMealItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        BlockPos center = context.getClickedPos();
        boolean appliedAny = false;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-RADIUS, -RADIUS, -RADIUS), center.offset(RADIUS, RADIUS, RADIUS))) {
            BlockState state = serverLevel.getBlockState(pos);
            if (state.getBlock() instanceof BonemealableBlock bonemealable
                    && bonemealable.isValidBonemealTarget(serverLevel, pos, state)
                    && bonemealable.isBonemealSuccess(serverLevel, serverLevel.random, pos, state)) {
                bonemealable.performBonemeal(serverLevel, serverLevel.random, pos, state);
                serverLevel.levelEvent(1505, pos, 15);
                appliedAny = true;
            }
        }
        if (appliedAny) {
            context.getItemInHand().shrink(1);
            Player player = context.getPlayer();
            if (player != null) {
                player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
