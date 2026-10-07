package com.koala.reactingreactions.item;

import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;

/** A bow strung with nylon on a titanium limb: its arrows hit half again as hard. */
public class TitaniumBowItem extends BowItem {
    private static final double DAMAGE_MULTIPLIER = 1.5;

    public TitaniumBowItem(Properties properties) {
        super(properties);
    }

    @Override
    public AbstractArrow customArrow(AbstractArrow arrow, ItemStack projectileStack, ItemStack weaponStack) {
        arrow.setBaseDamage(arrow.getBaseDamage() * DAMAGE_MULTIPLIER);
        return super.customArrow(arrow, projectileStack, weaponStack);
    }
}
