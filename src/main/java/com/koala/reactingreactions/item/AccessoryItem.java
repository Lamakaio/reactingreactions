package com.koala.reactingreactions.item;

import com.koala.reactingreactions.content.compat.WornAccessories;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

/**
 * An accessory. Worn in Accessories or Curios slots; with neither installed, it goes in {@code fallbackSlot} instead (null
 * for the ones that count when held in the offhand, see {@link WornAccessories}).
 */
public class AccessoryItem extends Item {
    private final @Nullable EquipmentSlot fallbackSlot;

    public AccessoryItem(Properties properties, @Nullable EquipmentSlot fallbackSlot) {
        super(properties);
        this.fallbackSlot = fallbackSlot;
    }

    @Override
    public @Nullable EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return WornAccessories.hasSlotMod() ? null : fallbackSlot;
    }
}
