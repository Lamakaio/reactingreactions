package com.koala.reactingreactions.item;

import com.koala.reactingreactions.content.toxic.Toxicity;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/** A medicine: eaten or drunk, it lowers the player's toxicity gauge by a fixed amount. A damageable stack is a multi-use pack. */
public class ToxinReliefItem extends Item {
    private final float relief;
    private final boolean drink;
    private final boolean returnsBottle;

    public ToxinReliefItem(Properties properties, float relief, boolean drink, boolean returnsBottle) {
        super(properties);
        this.relief = relief;
        this.drink = drink;
        this.returnsBottle = returnsBottle;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            Toxicity.setGauge(player, Toxicity.gauge(player) - relief);
        }
        if (entity instanceof Player player && player.getAbilities().instabuild) {
            return stack;
        }
        if (stack.isDamageableItem()) {
            // A multi-use pack loses one use instead.
            stack.hurtAndBreak(1, entity, LivingEntity.getSlotForHand(entity.getUsedItemHand()));
            return stack;
        }
        stack.shrink(1);
        if (returnsBottle) {
            ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
            if (stack.isEmpty()) {
                return bottle;
            }
            if (entity instanceof Player player && !player.getInventory().add(bottle)) {
                player.drop(bottle, false);
            }
        }
        return stack;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return drink ? UseAnim.DRINK : UseAnim.EAT;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return drink ? 32 : 16;
    }
}
