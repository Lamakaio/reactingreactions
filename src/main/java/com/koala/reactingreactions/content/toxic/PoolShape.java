package com.koala.reactingreactions.content.toxic;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

/**
 * The outline of a leak pool: a wobbly closed curve (its radius at each angle, at most 1) plus a few loose droplets, both scaled
 * by the pool's size when drawn. Each pool keeps a kind and a seed, so its outline stays the same while it lives; the lobes and
 * droplets spread out as it fills.
 */
public final class PoolShape {
    public enum Kind {
        /** Almost round, barely wobbling. */
        PUDDLE,
        /** A few broad lobes, a couple of drops beside it. */
        SPILL,
        /** Stretched out along one direction, drops thrown along it. */
        SPLASH,
        /** Ragged edge and many drops around it. */
        SPLATTER;

        static Kind of(int index) {
            Kind[] kinds = values();
            return kinds[Math.floorMod(index, kinds.length)];
        }
    }

    /** A loose droplet: where it sits, in pool radii from the centre, and its own radius. */
    public record Droplet(float x, float z, float radius) {
    }

    private static final int MAX_HARMONICS = 4;

    private final Kind kind;
    private final int[] frequencies = new int[MAX_HARMONICS];
    private final float[] amplitudes = new float[MAX_HARMONICS];
    private final float[] phases = new float[MAX_HARMONICS];
    private final float stretch;
    private final float stretchAngle;
    private final List<Droplet> droplets = new ArrayList<>();
    // The fill at which each droplet shows up, so a growing spill throws more of them.
    private final List<Float> dropletFill = new ArrayList<>();

    public PoolShape(int kindIndex, int seed) {
        kind = Kind.of(kindIndex);
        RandomSource random = RandomSource.create(seed);
        float[][] bands = switch (kind) {
            // {first frequency, frequency step, largest amplitude}
            case PUDDLE -> new float[][] {{2, 1, 0.06F}};
            case SPILL -> new float[][] {{2, 1, 0.3F}, {5, 2, 0.06F}};
            case SPLASH -> new float[][] {{3, 1, 0.08F}};
            case SPLATTER -> new float[][] {{3, 1, 0.1F}, {7, 2, 0.1F}};
        };
        int harmonic = 0;
        for (float[] band : bands) {
            for (int i = 0; i < 2 && harmonic < MAX_HARMONICS; i++, harmonic++) {
                frequencies[harmonic] = (int) (band[0] + band[1] * i);
                amplitudes[harmonic] = band[2] * (0.5F + random.nextFloat() * 0.5F);
                phases[harmonic] = random.nextFloat() * Mth.TWO_PI;
            }
        }
        stretch = kind == Kind.SPLASH ? 0.55F + random.nextFloat() * 0.15F : 1.0F;
        stretchAngle = random.nextFloat() * Mth.TWO_PI;
        int drops = switch (kind) {
            case PUDDLE -> random.nextInt(2);
            case SPILL -> 2 + random.nextInt(2);
            case SPLASH -> 3 + random.nextInt(2);
            case SPLATTER -> 5 + random.nextInt(3);
        };
        for (int i = 0; i < drops; i++) {
            // Splash drops fly along the long axis; the others anywhere around.
            float angle = kind == Kind.SPLASH
                    ? stretchAngle + (random.nextBoolean() ? 0 : Mth.PI) + (random.nextFloat() - 0.5F) * 0.5F
                    : random.nextFloat() * Mth.TWO_PI;
            float distance = 1.05F + random.nextFloat() * 0.3F;
            float edge = radius(angle, 1.0F);
            droplets.add(new Droplet(Mth.cos(angle) * edge * distance, Mth.sin(angle) * edge * distance, 0.05F + random.nextFloat() * 0.07F));
            dropletFill.add(random.nextFloat() * 0.6F);
        }
    }

    public Kind kind() {
        return kind;
    }

    /**
     * The outline's radius at {@code angle}, as a fraction of the pool's half-width (at most 1). {@code fill} (0-1, how full the
     * pool is) makes the lobes grow: a fresh leak is a rounder blob.
     */
    public float radius(float angle, float fill) {
        float spread = 0.4F + 0.6F * Mth.clamp(fill, 0, 1);
        float wobble = 0;
        float total = 0;
        for (int i = 0; i < MAX_HARMONICS; i++) {
            if (amplitudes[i] > 0) {
                wobble += amplitudes[i] * spread * Mth.sin(frequencies[i] * angle + phases[i]);
                total += amplitudes[i] * spread;
            }
        }
        float r = (1 + wobble) / (1 + total);
        if (stretch < 1) {
            // An ellipse: 1 along the stretch direction, `stretch` across it.
            float c = Mth.cos(angle - stretchAngle);
            float s = Mth.sin(angle - stretchAngle);
            r *= stretch / Mth.sqrt(stretch * stretch * c * c + s * s);
        }
        return r;
    }

    /** The droplets showing at {@code fill}. */
    public List<Droplet> droplets(float fill) {
        List<Droplet> showing = new ArrayList<>();
        for (int i = 0; i < droplets.size(); i++) {
            if (fill >= dropletFill.get(i)) {
                showing.add(droplets.get(i));
            }
        }
        return showing;
    }
}
