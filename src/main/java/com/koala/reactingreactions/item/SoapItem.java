package com.koala.reactingreactions.item;

import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/** Eating it clears every harmful effect. */
public class SoapItem extends Item {
    public SoapItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            List<Holder<MobEffect>> toRemove = new ArrayList<>();
            for (MobEffectInstance instance : entity.getActiveEffects()) {
                if (instance.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                    toRemove.add(instance.getEffect());
                }
            }
            toRemove.forEach(entity::removeEffect);
        }
        stack.consume(1, entity);
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 24;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }
}
