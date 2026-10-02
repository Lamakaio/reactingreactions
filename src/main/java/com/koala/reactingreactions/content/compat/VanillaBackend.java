package com.koala.reactingreactions.content.compat;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

/**
 * {@link WornAccessories} without a slot mod: accessories go in the armor slot their item names (see AccessoryItem), and the
 * rest count while held in the offhand.
 */
final class VanillaBackend implements WornAccessories.Backend {
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET,
            EquipmentSlot.OFFHAND};

    @Override
    public boolean isEquipped(Player player, Item item) {
        return find(player, item) != null;
    }

    @Override
    public WornAccessories.Worn find(Player player, Item item) {
        for (EquipmentSlot slot : SLOTS) {
            var stack = player.getItemBySlot(slot);
            if (stack.is(item)) {
                return new WornAccessories.Worn(stack, changed -> player.setItemSlot(slot, changed));
            }
        }
        return null;
    }
}
