package com.koala.reactingreactions.content.toxic;

import com.koala.reactingreactions.Config;
import com.koala.reactingreactions.content.compat.WornAccessories;
import com.koala.reactingreactions.registry.CRRItems;
import com.simibubi.create.content.equipment.armor.BacktankUtil;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** What protects a player from toxic compounds; all of it is worn in accessory slots (Accessories or Curios) (the filter is carried). */
public final class ToxicPpe {
    private ToxicPpe() {
    }

    private static boolean wearing(Player player, Item item) {
        return WornAccessories.isEquipped(player, item);
    }

    public static boolean wearsOxygenMask(Player player) {
        return wearing(player, CRRItems.OXYGEN_MASK.get());
    }

    public static boolean hasGloves(Player player) {
        return wearing(player, CRRItems.CHEMICAL_GLOVES.get());
    }

    public static boolean hasBoots(Player player) {
        return wearing(player, CRRItems.CHEMICAL_BOOTS.get());
    }

    /** A worn Gas Mask that also has a Carbon Filter to work with (if filters are required). */
    public static boolean hasWorkingMask(Player player) {
        if (!wearing(player, CRRItems.GAS_MASK.get())) {
            return false;
        }
        return !Config.bool(Config.FILTERS_CONSUMED, true) || findFilter(player) != null;
    }

    // Game time of each player's last Oxygen Mask breath: one unit of backtank air per second of use, like Create's Diving Helmet.
    private static final Map<UUID, Long> LAST_BREATH = new HashMap<>();

    /** A worn Oxygen Mask with air left in a worn Create backtank. Spends one unit of air per second of use. */
    public static boolean breathesSuppliedAir(Player player) {
        if (!wearing(player, CRRItems.OXYGEN_MASK.get())) {
            return false;
        }
        var backtanks = BacktankUtil.getAllWithAir(player);
        if (backtanks.isEmpty()) {
            return false;
        }
        long now = player.level().getGameTime();
        Long last = LAST_BREATH.get(player.getUUID());
        if (last == null || now - last >= 20 || now < last) {
            LAST_BREATH.put(player.getUUID(), now);
            // The list is sorted emptiest first; taking from the fullest drains several tanks evenly.
            BacktankUtil.consumeAir(player, backtanks.getLast(), 1);
        }
        return true;
    }

    public static ItemStack findFilter(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(CRRItems.CARBON_FILTER.get())) {
                return stack;
            }
        }
        return null;
    }
}
