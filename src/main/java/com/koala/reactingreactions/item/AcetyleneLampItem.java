package com.koala.reactingreactions.item;

import com.koala.reactingreactions.Config;
import com.koala.reactingreactions.registry.CRRFluids;

import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

/**
 * Lights up around its holder, and can be supercharged from its internal acetylene tank (see {@code CRRAcetyleneLampEvents}).
 * Filled by a Spout or a Charging Pad; Create's Capacity enlarges the tank.
 */
public class AcetyleneLampItem extends FluidTankItem {
    public static final String SUPERCHARGED_UNTIL_KEY = "SuperchargedUntil";
    public static final int SUPERCHARGE_DURATION_TICKS = 600;
    /** The tank size unless the config changes it; a freshly crafted lamp comes this full. */
    public static final int DEFAULT_CAPACITY_MB = 2000;

    public AcetyleneLampItem(Properties properties) {
        super(properties, CRRFluids.ACETYLENE.get().getSource(), () -> Config.number(Config.ACETYLENE_LAMP_CAPACITY_MB, DEFAULT_CAPACITY_MB), null);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        int cost = Config.number(Config.ACETYLENE_LAMP_MB_PER_SUPERCHARGE, 500);
        if (FluidTankHolder.contents(stack).getAmount() < cost) {
            return InteractionResultHolder.fail(stack);
        }
        FluidTankHolder.take(stack, cost);
        long superchargedUntil = level.getGameTime() + SUPERCHARGE_DURATION_TICKS;
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putLong(SUPERCHARGED_UNTIL_KEY, superchargedUntil));
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 1.4F);
        return InteractionResultHolder.success(stack);
    }

    public static boolean isSupercharged(ItemStack stack, long gameTime) {
        long until = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getLong(SUPERCHARGED_UNTIL_KEY);
        return gameTime < until;
    }
}
