package com.koala.reactingreactions.item;

import com.koala.reactingreactions.content.laser.LaserTargets;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Held, it shows a dot where it points (see {@code CRREquipmentEvents.tickLaserPointer}). Used, it measures the distance
 * to the block it points at, raycasting itself since {@code useOn} only reaches as far as a normal click.
 */
public class LaserPointerItem extends Item {
    public LaserPointerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(LaserTargets.RANGE));
        BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (hit.getType() == HitResult.Type.MISS) {
            player.displayClientMessage(Component.literal("Laser measurement: nothing in range").withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.success(stack);
        }
        BlockPos delta = hit.getBlockPos().subtract(player.blockPosition());
        player.displayClientMessage(Component.literal(
                "Laser measurement: x=" + delta.getX() + ", y=" + delta.getY() + ", z=" + delta.getZ())
                .withStyle(ChatFormatting.RED), true);
        return InteractionResultHolder.success(stack);
    }
}
