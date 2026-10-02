package com.koala.reactingreactions.content.compat;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.util.function.Consumer;

/**
 * The accessory slots, from whichever slot mod is installed: Accessories first, then Curios. With neither, accessories are
 * worn in armor slots or held in the offhand (see {@link VanillaBackend}). Each mod's backend is its own class, only loaded
 * once its mod is known to be present.
 */
public final class WornAccessories {
    /** A worn stack, and how to write it back to its slot after changing it. */
    public record Worn(ItemStack stack, Consumer<ItemStack> writeBack) {
        public void save() {
            writeBack.accept(stack);
        }
    }

    interface Backend {
        boolean isEquipped(Player player, Item item);

        Worn find(Player player, Item item);
    }

    private static final String ACCESSORIES = "accessories";
    private static final String CURIOS = "curios";

    private static Backend backend;

    private WornAccessories() {
    }

    private static Backend backend() {
        if (backend == null) {
            if (ModList.get().isLoaded(ACCESSORIES)) {
                backend = new AccessoriesBackend();
            } else if (ModList.get().isLoaded(CURIOS)) {
                backend = new CuriosBackend();
            } else {
                backend = new VanillaBackend();
            }
        }
        return backend;
    }

    /** Whether Accessories or Curios provides the slots; without either, accessories go in vanilla slots. */
    public static boolean hasSlotMod() {
        return ModList.get().isLoaded(ACCESSORIES) || ModList.get().isLoaded(CURIOS);
    }

    public static boolean isEquipped(Player player, Item item) {
        return backend().isEquipped(player, item);
    }

    /** The first worn {@code item}, or null. */
    public static Worn find(Player player, Item item) {
        return backend().find(player, item);
    }
}
