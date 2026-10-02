package com.koala.reactingreactions.content.drill;

/** The drill rig's speed constants and rate formulas, kept free of registry references so text and datagen can use them safely. */
public final class DrillRates {
    public static final float MIN_RPM = 32;
    public static final float MAX_RPM = 128;
    /** Crude oil per tick at 32 RPM in a full-richness vein; linear in RPM up to {@link #MAX_RPM}. */
    public static final double BASE_OIL_RATE = 0.5;
    /** One dust item costs as long as 500 mB of oil at the same RPM and richness. */
    public static final double BASE_ITEM_RATE = 1.0 / 1000;
    /** The diamond bit drills every rock this much faster than the steel and titanium bits. */
    public static final double DIAMOND_SPEED = 2.0;

    private DrillRates() {
    }

    /** Crude oil per second at a given RPM in a full-richness vein. */
    public static double oilMbPerSecond(float rpm) {
        return 20 * BASE_OIL_RATE * Math.min(rpm, MAX_RPM) / MIN_RPM;
    }

    /** Seconds per dust item at a given RPM in a full-richness vein. */
    public static double secondsPerItem(float rpm) {
        return 1.0 / (20 * BASE_ITEM_RATE * Math.min(rpm, MAX_RPM) / MIN_RPM);
    }
}
