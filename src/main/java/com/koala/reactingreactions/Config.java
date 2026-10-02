package com.koala.reactingreactions;

import com.koala.reactingreactions.item.AcetyleneLampItem;

import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * The mod's settings (a per-world, synced SERVER config: {@code reactingreactions-server.toml}). Everything about toxic compounds is
 * behind {@link #toxicityEnabled()}. Values are read through small helpers that fall back to the defaults when the config is not
 * loaded yet (for example on the client before it joins a world), instead of throwing.
 */
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    static {
        BUILDER.push("toxicity");
    }

    public static final ModConfigSpec.BooleanValue TOXICITY_ENABLED = BUILDER
            .comment("Master switch for toxic compounds: leaks, pools, vents, contamination and the personal toxicity gauge.")
            .define("enabled", true);
    public static final ModConfigSpec.DoubleValue LEAK_RATE = BUILDER
            .comment("Multiplier on how often tanks, machines and pipes holding toxic compounds leak.")
            .defineInRange("leakRate", 1.0, 0.0, 100.0);
    public static final ModConfigSpec.BooleanValue LEAKS_LOSE_FLUID = BUILDER
            .comment("Whether a leaking tank or machine actually loses the leaked millibuckets (pipes never do).")
            .define("leaksLoseFluid", true);
    public static final ModConfigSpec.IntValue LEAK_AMOUNT_MB = BUILDER
            .comment("Millibuckets lost (and spilled, for liquids) by one leak.")
            .defineInRange("leakAmountMb", 5, 1, 1000);
    public static final ModConfigSpec.BooleanValue POOLS = BUILDER
            .comment("Whether liquid leaks leave pools on the floor.")
            .define("pools", true);
    public static final ModConfigSpec.IntValue MAX_POOLS_PER_CHUNK = BUILDER
            .comment("Most leak pools that can exist in one chunk.")
            .defineInRange("maxPoolsPerChunk", 6, 0, 256);
    public static final ModConfigSpec.BooleanValue VENTS = BUILDER
            .comment("Whether gas leaks vent as small sprays.")
            .define("vents", true);
    public static final ModConfigSpec.BooleanValue EXPLOSIONS = BUILDER
            .comment("Whether flammable and explosive pools and vents burn and explode when exposed to fire.")
            .define("explosions", true);
    public static final ModConfigSpec.BooleanValue EXPLOSIONS_DESTROY_BLOCKS = BUILDER
            .comment("Whether those explosions and fires destroy blocks. Off: they only hurt entities.")
            .define("explosionsDestroyBlocks", false);
    public static final ModConfigSpec.DoubleValue EXPLOSION_POWER_CAP = BUILDER
            .comment("Largest explosion power a pool or vent can cause.")
            .defineInRange("explosionPowerCap", 3.0, 0.0, 16.0);
    public static final ModConfigSpec.BooleanValue IGNITION_SPAWNS_FIRE = BUILDER
            .comment("Whether a pool or vent that ignites sets fire to some surrounding blocks, like a flint and steel would.")
            .define("ignitionSpawnsFire", true);
    public static final ModConfigSpec.BooleanValue EXPLOSION_FIREBALLS = BUILDER
            .comment("Whether explosions of pools and vents throw a few fireballs in random directions.")
            .define("explosionFireballs", true);
    public static final ModConfigSpec.DoubleValue MIN_AIR_TOXICITY = BUILDER
            .comment("Toxicity (0-10) from which a compound pollutes the air when it leaks, pools or is vented: below it, no toxic air",
                    "or haze. Fire and explosions of flammable ones happen either way.")
            .defineInRange("minAirToxicity", 3.0, 0.0, 10.0);
    public static final ModConfigSpec.IntValue GAS_VENT_RATE_MB = BUILDER
            .comment("Millibuckets of gas a Gas Vent releases per second.")
            .defineInRange("gasVentRateMb", 500, 1, 100_000);
    public static final ModConfigSpec.DoubleValue GAS_VENT_POLLUTION = BUILDER
            .comment("How much a Gas Vent pollutes, as a fraction of what leaking the same amount would (1 = as much as a leak).")
            .defineInRange("gasVentPollution", 0.25, 0.0, 10.0);
    public static final ModConfigSpec.IntValue FLOOR_DRAIN_RADIUS = BUILDER
            .comment("Horizontal radius, in blocks, in which a Floor Drain absorbs pools.")
            .defineInRange("floorDrainRadius", 6, 1, 16);
    public static final ModConfigSpec.IntValue CONTAMINATION_RADIUS = BUILDER
            .comment("How many blocks around a leak the air is contaminated.")
            .defineInRange("contaminationRadius", 3, 0, 8);
    public static final ModConfigSpec.BooleanValue DAMAGE_PLANTS = BUILDER
            .comment("Whether contaminated air kills grass, crops and other plants.")
            .define("damagePlants", true);
    public static final ModConfigSpec.BooleanValue DAMAGE_ANIMALS = BUILDER
            .comment("Whether contaminated air and pools hurt animals and other mobs.")
            .define("damageAnimals", true);
    public static final ModConfigSpec.BooleanValue GAUGE_ENABLED = BUILDER
            .comment("Whether players have a personal toxicity gauge (and the HUD for it).")
            .define("gaugeEnabled", true);
    public static final ModConfigSpec.DoubleValue GAUGE_DECAY_PER_MINUTE = BUILDER
            .comment("How much the gauge (0-100) falls by itself per minute.")
            .defineInRange("gaugeDecayPerMinute", 1.0, 0.0, 100.0);
    public static final ModConfigSpec.BooleanValue LETHAL_AT_MAX = BUILDER
            .comment("Whether a full gauge is deadly.")
            .define("lethalAtMax", true);
    public static final ModConfigSpec.BooleanValue FILTERS_CONSUMED = BUILDER
            .comment("Whether the gas mask needs, and slowly uses up, Carbon Filters from the inventory.")
            .define("filtersConsumed", true);
    public static final ModConfigSpec.IntValue SCRUBBER_OPEN_RADIUS = BUILDER
            .comment("Radius of a working Atmospheric Scrubber in the open.")
            .defineInRange("scrubberOpenRadius", 6, 0, 32);
    public static final ModConfigSpec.IntValue SCRUBBER_ROOM_RADIUS = BUILDER
            .comment("Radius of a working Atmospheric Scrubber in a roofed and floored room (airtight block above and below).")
            .defineInRange("scrubberRoomRadius", 16, 0, 64);

    static {
        BUILDER.pop();
        BUILDER.push("equipment");
    }

    public static final ModConfigSpec.IntValue EXO_HELMET_CAPACITY_MB = BUILDER
            .comment("Composite Exo-Helmet nitrogen tank, in millibuckets.")
            .defineInRange("exoHelmetCapacityMb", 1000, 1, 1_000_000);
    public static final ModConfigSpec.DoubleValue EXO_HELMET_MB_PER_TICK = BUILDER
            .comment("Nitrogen spent per tick of night vision. The default tank lasts about 80 minutes.")
            .defineInRange("exoHelmetMbPerTick", 0.01, 0.0, 1000.0);
    public static final ModConfigSpec.IntValue EXO_CHESTPLATE_CAPACITY_MB = BUILDER
            .comment("Composite Exo-Chestplate aerozine tank, in millibuckets.")
            .defineInRange("exoChestplateCapacityMb", 2000, 1, 1_000_000);
    public static final ModConfigSpec.DoubleValue EXO_CHESTPLATE_MB_PER_TICK = BUILDER
            .comment("Aerozine spent per tick of flight. The default tank with Capacity III (4000 mB) lasts about 30 minutes.")
            .defineInRange("exoChestplateMbPerTick", 0.11, 0.0, 1000.0);
    public static final ModConfigSpec.IntValue EXO_LEGGINGS_CAPACITY_MB = BUILDER
            .comment("Composite Exo-Leggings hydrogen tank, in millibuckets.")
            .defineInRange("exoLeggingsCapacityMb", 1000, 1, 1_000_000);
    public static final ModConfigSpec.DoubleValue EXO_LEGGINGS_MB_PER_TICK = BUILDER
            .comment("Hydrogen spent per tick of speed boost, flying or swimming.")
            .defineInRange("exoLeggingsMbPerTick", 0.05, 0.0, 1000.0);
    public static final ModConfigSpec.IntValue EXO_BOOTS_CAPACITY_MB = BUILDER
            .comment("Composite Exo-Boots mineral oil tank, in millibuckets.")
            .defineInRange("exoBootsCapacityMb", 1000, 1, 1_000_000);
    public static final ModConfigSpec.DoubleValue EXO_BOOTS_MB_PER_BLOCK = BUILDER
            .comment("Mineral oil spent per block of fall the boots absorb, past the first three.")
            .defineInRange("exoBootsMbPerBlock", 2.0, 0.0, 1000.0);
    public static final ModConfigSpec.IntValue MULTITOOL_CAPACITY_MB = BUILDER
            .comment("Plasma Multitool drill grease tank, in millibuckets.")
            .defineInRange("multitoolCapacityMb", 1000, 1, 1_000_000);
    public static final ModConfigSpec.DoubleValue MULTITOOL_MB_PER_BLOCK = BUILDER
            .comment("Drill grease spent per extra block mined in an area or vein.")
            .defineInRange("multitoolMbPerBlock", 0.1, 0.0, 1000.0);
    public static final ModConfigSpec.IntValue NEON_BLADE_CAPACITY_MB = BUILDER
            .comment("Neon Blade tank, in millibuckets.")
            .defineInRange("neonBladeCapacityMb", 1000, 1, 1_000_000);
    public static final ModConfigSpec.DoubleValue NEON_BLADE_MB_PER_HIT = BUILDER
            .comment("Neon spent per hit, and twice that per projectile blocked.")
            .defineInRange("neonBladeMbPerHit", 0.2, 0.0, 1000.0);
    public static final ModConfigSpec.IntValue AEROZINE_THRUSTERS_CAPACITY_MB = BUILDER
            .comment("Aerozine Thrusters tank capacity, in millibuckets.")
            .defineInRange("aerozineThrustersCapacityMb", 1000, 1, 1_000_000);
    public static final ModConfigSpec.IntValue AEROZINE_THRUSTERS_MB_PER_JUMP = BUILDER
            .comment("Aerozine spent per double jump - at the default capacity, a full tank is 1000 jumps.")
            .defineInRange("aerozineThrustersMbPerJump", 1, 1, 1_000_000);
    public static final ModConfigSpec.IntValue ACETYLENE_LAMP_CAPACITY_MB = BUILDER
            .comment("Acetylene Lamp tank capacity, in millibuckets.")
            .defineInRange("acetyleneLampCapacityMb", AcetyleneLampItem.DEFAULT_CAPACITY_MB, 1, 1_000_000);
    public static final ModConfigSpec.IntValue ACETYLENE_LAMP_MB_PER_SUPERCHARGE = BUILDER
            .comment("Acetylene spent per supercharge - at the default capacity, a full tank is 4 supercharges.")
            .defineInRange("acetyleneLampMbPerSupercharge", 500, 1, 1_000_000);

    static {
        BUILDER.pop();
        BUILDER.push("steamTurbine");
    }

    // Speed scales with the steam drained, up to full at the configured rate.
    public static final ModConfigSpec.IntValue STEAM_TURBINE_TANK_CAPACITY_MB = BUILDER
            .comment("Capacity of the Steam Turbine's own internal tank, in millibuckets.")
            .defineInRange("tankCapacityMb", 4000, 1, 1_000_000);
    public static final ModConfigSpec.IntValue STEAM_TURBINE_MB_PER_TICK = BUILDER
            .comment("Millibuckets of reactingreactions:steam the turbine drains per tick at full speed.")
            .defineInRange("steamMbPerTick", 20, 1, 100_000);
    public static final ModConfigSpec.IntValue STEAM_TURBINE_MAX_RPM = BUILDER
            .comment("Rotation speed (RPM) the turbine generates when fed steam at its full drain rate.")
            .defineInRange("maxRpm", 64, 1, 512);
    public static final ModConfigSpec.IntValue STEAM_TURBINE_STRESS_CAPACITY_SU = BUILDER
            .comment("Stress capacity (SU) the turbine provides to its kinetic network at full speed.")
            .defineInRange("stressCapacitySu", 512, 1, 100_000);

    static {
        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();

    private Config() {
    }

    /** The master flag, cached: it is read on every tick by the leak mixins, so it must be cheaper than a config lookup. */
    private static volatile boolean toxicityEnabled = true;

    public static boolean toxicityEnabled() {
        return toxicityEnabled;
    }

    /** Refreshes the cached flag whenever the config file is loaded or changed. */
    public static void onConfigEvent(ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            toxicityEnabled = bool(TOXICITY_ENABLED, true);
        }
    }

    public static boolean bool(ModConfigSpec.BooleanValue value, boolean fallback) {
        try {
            return value.get();
        } catch (IllegalStateException notLoaded) {
            return fallback;
        }
    }

    public static double number(ModConfigSpec.DoubleValue value, double fallback) {
        try {
            return value.get();
        } catch (IllegalStateException notLoaded) {
            return fallback;
        }
    }

    public static int number(ModConfigSpec.IntValue value, int fallback) {
        try {
            return value.get();
        } catch (IllegalStateException notLoaded) {
            return fallback;
        }
    }
}
