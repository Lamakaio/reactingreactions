package com.koala.reactingreactions.content.compat;

import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import io.wispforest.accessories.pond.AccessoriesAPIAccess;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

/** {@link WornAccessories} through Accessories. Only loaded when it is installed. */
final class AccessoriesBackend implements WornAccessories.Backend {
    @Override
    public boolean isEquipped(Player player, Item item) {
        AccessoriesCapability capability = ((AccessoriesAPIAccess) player).accessoriesCapability();
        return capability != null && capability.isEquipped(item);
    }

    @Override
    public WornAccessories.Worn find(Player player, Item item) {
        AccessoriesCapability capability = ((AccessoriesAPIAccess) player).accessoriesCapability();
        SlotEntryReference entry = capability == null ? null : capability.getFirstEquipped(item);
        return entry == null ? null : new WornAccessories.Worn(entry.stack(), stack -> entry.reference().setStack(stack));
    }
}
