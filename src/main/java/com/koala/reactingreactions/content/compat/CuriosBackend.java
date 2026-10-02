package com.koala.reactingreactions.content.compat;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import top.theillusivec4.curios.api.CuriosApi;

/** {@link WornAccessories} through Curios. Only loaded when it is installed. */
final class CuriosBackend implements WornAccessories.Backend {
    @Override
    public boolean isEquipped(Player player, Item item) {
        return CuriosApi.getCuriosInventory(player).map(inventory -> inventory.isEquipped(item)).orElse(false);
    }

    @Override
    public WornAccessories.Worn find(Player player, Item item) {
        return CuriosApi.getCuriosInventory(player).flatMap(inventory -> inventory.findFirstCurio(item).map(result -> {
            var slot = result.slotContext();
            return new WornAccessories.Worn(result.stack(), stack -> inventory.getStacksHandler(slot.identifier())
                    .ifPresent(handler -> handler.getStacks().setStackInSlot(slot.index(), stack)));
        })).orElse(null);
    }
}
