package com.koala.reactingreactions.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Tincture of iodine in a spray bottle: use it to cure your own Poison and Wither, or use it on a creature to cure theirs. A use
 * is only spent when something was actually cured; the bottle holds {@code durability} uses. Sprayed on a zombie villager, it
 * has a {@link #ZOMBIE_CURE_CHANCE} chance of starting vanilla's cure (as a golden apple on a weakened one does), and always
 * spends a use trying.
 */
public class IodineSprayItem extends Item {
    private static final int COOLDOWN_TICKS = 10;
    public static final float ZOMBIE_CURE_CHANCE = 0.1F;

    public IodineSprayItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        return spray(stack, player, player, hand) ? InteractionResultHolder.sidedSuccess(stack, level.isClientSide) : InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (target instanceof ZombieVillager zombie && !zombie.isConverting()) {
            if (!player.level().isClientSide) {
                if (zombie.getRandom().nextFloat() < ZOMBIE_CURE_CHANCE) {
                    // Vanilla's own cure: same timer, shaking and trade discount as the golden apple.
                    zombie.startConverting(player.getUUID(), zombie.getRandom().nextInt(2401) + 3600);
                }
                spend(stack, player, zombie, hand);
            }
            return InteractionResult.sidedSuccess(player.level().isClientSide);
        }
        return spray(stack, player, target, hand) ? InteractionResult.sidedSuccess(player.level().isClientSide) : InteractionResult.PASS;
    }

    private static boolean spray(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!target.hasEffect(MobEffects.POISON) && !target.hasEffect(MobEffects.WITHER)) {
            return false;
        }
        Level level = target.level();
        if (!level.isClientSide) {
            target.removeEffect(MobEffects.POISON);
            target.removeEffect(MobEffects.WITHER);
            spend(stack, player, target, hand);
        }
        return true;
    }

    private static void spend(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        target.level().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.3F, 1.8F);
        if (!player.getAbilities().instabuild) {
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
        }
        player.getCooldowns().addCooldown(stack.getItem(), COOLDOWN_TICKS);
    }
}
