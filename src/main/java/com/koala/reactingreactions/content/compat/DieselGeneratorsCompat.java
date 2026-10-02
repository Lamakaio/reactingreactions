package com.koala.reactingreactions.content.compat;

import com.koala.reactingreactions.registry.CRRFluids;
import com.tterrag.registrate.util.entry.FluidEntry;

import net.neoforged.fml.ModList;

import java.util.List;

/** When Create: Diesel Generators is installed, its oil and fuel economy replaces this mod's own. */
public final class DieselGeneratorsCompat {
    public static final String MODID = "createdieselgenerators";

    private DieselGeneratorsCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MODID);
    }

    /** Our fluids that become unobtainable with it installed, hidden from recipe viewers. */
    public static List<FluidEntry<?>> replacedFluids() {
        return List.of(CRRFluids.DIESEL, CRRFluids.CRUDE_OIL, CRRFluids.SEED_OIL, CRRFluids.ETHANOL);
    }
}
