package com.koala.reactingreactions.content.toxic;

import com.koala.reactingreactions.Config;
import com.koala.reactingreactions.registry.CRRParticles;
import com.koala.reactingreactions.registry.CRRSounds;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A gas vent lasts a few seconds: a steady spray of tinted particles at the leak, contaminating the air while it lasts. A working
 * scrubber cuts it short, and a flammable gas ignites if something sets it off (checked while it is going).
 */
public final class Vents {
    private static final int DURATION_TICKS = 80;
    private static final int MAX_PER_LEVEL = 64;
    private static final Map<ResourceKey<Level>, List<Vent>> ACTIVE = new ConcurrentHashMap<>();

    private static final class Vent {
        final BlockPos pos;
        final Vec3 at;
        /** Where the jet leaves the block, and which way it points: out of the first open side. */
        final Vec3 nozzle;
        final Vec3 direction;
        final int rgb;
        final ToxicFluid toxic;
        final int amount;
        int ticksLeft = DURATION_TICKS;

        Vent(BlockPos pos, Direction side, int rgb, ToxicFluid toxic, int amount) {
            this.pos = pos;
            this.at = Vec3.atCenterOf(pos);
            this.direction = Vec3.atLowerCornerOf(side.getNormal());
            this.nozzle = at.add(direction.scale(0.55));
            this.rgb = rgb;
            this.toxic = toxic;
            this.amount = amount;
        }
    }

    // Sides tried in order for the jet: sideways first, as from a seam, then up, then down.
    private static final Direction[] JET_SIDES = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN};

    private Vents() {
    }

    public static void start(ServerLevel level, BlockPos pos, int color, ToxicFluid toxic, int amount) {
        List<Vent> vents = ACTIVE.computeIfAbsent(level.dimension(), key -> new ArrayList<>());
        if (vents.size() >= MAX_PER_LEVEL) {
            return;
        }
        Direction side = Direction.UP;
        // Starts at a random side so neighbouring leaks do not all point the same way.
        int first = level.random.nextInt(4);
        for (int i = 0; i < JET_SIDES.length; i++) {
            Direction candidate = JET_SIDES[i < 4 ? (first + i) % 4 : i];
            BlockPos out = pos.relative(candidate);
            if (level.getBlockState(out).getCollisionShape(level, out).isEmpty()) {
                side = candidate;
                break;
            }
        }
        vents.add(new Vent(pos, side, color & 0xFFFFFF, toxic, amount));
        level.playSound(null, pos, CRRSounds.GAS_HISS.get(), SoundSource.BLOCKS, 0.6F, 0.9F + level.random.nextFloat() * 0.2F);
    }

    /** Every tick per level. */
    public static void tick(ServerLevel level) {
        List<Vent> vents = ACTIVE.get(level.dimension());
        if (vents == null || vents.isEmpty()) {
            return;
        }
        boolean enabled = Config.toxicityEnabled();
        var iterator = vents.iterator();
        while (iterator.hasNext()) {
            Vent vent = iterator.next();
            if (!enabled || --vent.ticksLeft <= 0 || !level.isLoaded(vent.pos) || Scrubbers.covers(level, vent.pos)) {
                iterator.remove();
                continue;
            }
            jet(level, vent);
            if (vent.ticksLeft % 20 == 0) {
                Contamination.addFrom(level, vent.pos, vent.toxic, 0.4F + vent.toxic.toxicity() * 0.1F);
            }
            if (vent.ticksLeft % 10 == 0 && vent.toxic.flammable() && Ignition.exposed(level, new AABB(vent.pos))) {
                Ignition.ignite(level, vent.at, vent.toxic, vent.amount);
                iterator.remove();
            }
        }
    }

    /** The spray, weakening as the pressure drops: a fast narrow jet, and clouds where it slows down. */
    private static void jet(ServerLevel level, Vent vent) {
        spray(level, vent.nozzle, vent.direction, vent.rgb, vent.ticksLeft / (float) DURATION_TICKS, vent.ticksLeft % 4 == 0);
    }

    /**
     * One tick of gas spraying out of {@code nozzle} towards {@code direction}: a fast narrow jet weakening with {@code pressure}
     * (0-1), plus clouds where it slows down when {@code cloud}. Shared by leaks and the Gas Vent.
     */
    public static void spray(ServerLevel level, Vec3 nozzle, Vec3 direction, int rgb, float pressure, boolean cloud) {
        var random = level.random;
        if (random.nextFloat() < 0.3F + pressure) {
            for (int i = 0; i < 2; i++) {
                double speed = 0.08 + 0.14 * pressure;
                Vec3 velocity = direction.scale(speed).add((random.nextDouble() - 0.5) * 0.05, (random.nextDouble() - 0.5) * 0.05 + 0.01,
                        (random.nextDouble() - 0.5) * 0.05);
                // A count of 0 sends one particle with exactly this velocity.
                level.sendParticles(CRRParticles.haze(rgb, 0.55F), nozzle.x, nozzle.y, nozzle.z, 0, velocity.x, velocity.y, velocity.z, 1);
            }
        }
        if (cloud) {
            Vec3 at = nozzle.add(direction.scale(0.6 + pressure));
            level.sendParticles(CRRParticles.haze(rgb, 0.3F), at.x, at.y, at.z, 2, 0.3, 0.2, 0.3, 0.005);
        }
    }

    public static void clearAll() {
        ACTIVE.clear();
    }
}
