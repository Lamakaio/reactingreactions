package com.koala.reactingreactions.content.toxic;

/**
 * This mod's own toxic fluids and items, as written into the {@code reactingreactions:toxicity} and {@code toxic_item} data maps by
 * datagen. The data maps are what the game reads; this table is only for things that need the list before any data is loaded,
 * such as which items open the Ponder scenes on toxic compounds.
 */
public final class ToxicDefaults {
    private ToxicDefaults() {
    }

    /** {toxicity, flammable, explosive} per fluid; anything not listed is harmless. */
    public static final Object[][] FLUIDS = {
            {"carbon_monoxide", 7, true, false}, {"carbon_dioxide", 2, false, false},
            {"methane", 2, true, false}, {"ethane", 2, true, false}, {"propane", 3, true, false}, {"butane", 3, true, false},
            {"ethylene", 3, true, false}, {"propylene", 3, true, false}, {"lpg", 4, true, false},
            {"hydrocarbon_gas", 4, true, false}, {"contaminated_hydrocarbon_gas", 8, true, false}, {"contaminated_methane", 8, true, false},
            {"solvent", 4, true, false}, {"acetylene", 3, true, true}, {"hydrogen", 1, true, true}, {"aerozine", 7, true, true},
            {"ammonia", 6, false, false}, {"nitric_acid", 8, false, false}, {"sulfuric_acid", 8, false, false},
            {"bleach", 6, false, false}, {"lye", 5, false, false}, {"titanium_tetrachloride", 7, false, false},
            {"magnesium_chloride", 2, false, false}, {"weak_brine", 1, false, false}, {"strong_brine", 2, false, false},
            {"lithium_brine", 2, false, false}, {"resin", 2, true, false}, {"liquid_nylon", 3, false, false},
            {"liquid_hdpe", 2, false, false}, {"liquid_polypropylene", 2, false, false},
            {"diesel", 3, true, false}, {"naphtha", 4, true, false}, {"crude_oil", 3, true, false}, {"coolant", 1, false, false}, {"mineral_oil", 1, false, false},
            {"ethanol", 2, true, false}, {"white_vinegar", 1, false, false},
    };

    /** {toxicity} per item: solids that are unhealthy to carry. */
    public static final Object[][] ITEMS = {
            {"bromine", 6}, {"sulfur_dust", 2}, {"lead_ingot", 1}, {"lead_nugget", 1}, {"ammonium_nitrate", 2},
            {"calcium_carbide", 2}, {"manganese", 1}, {"bleach_bottle", 3}, {"lithium_ingot", 1}, {"lithium_nugget", 1},
    };
}
