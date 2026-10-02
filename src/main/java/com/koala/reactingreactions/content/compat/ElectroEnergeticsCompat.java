package com.koala.reactingreactions.content.compat;

import net.neoforged.fml.ModList;

/**
 * Without Electro Energetics, electrolysis needs no voltage, the Induction Heater never heats, and its parts in recipes fall
 * back to copper nuggets. Any class implementing one of its interfaces may only load behind {@link #isLoaded()}: the JVM
 * resolves interfaces when a class loads, and would crash without the mod.
 */
public final class ElectroEnergeticsCompat {
    public static final String MODID = "electroenergetics";

    private ElectroEnergeticsCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MODID);
    }
}
