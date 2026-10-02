package com.koala.reactingreactions.content.laser;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Where every laser pointer in use is aimed, server side. */
public final class LaserTargets {
    /** How far a laser pointer reaches, for both the dot and measuring. */
    public static final double RANGE = 64.0;
    /** How many ticks a target stays "fresh" once its holder stops updating it (dropped the item, logged off, ...). */
    private static final int FRESH_TICKS = 20;

    private record Target(ServerLevel level, Vec3 pos, long updatedAt) {
    }

    private static final Map<UUID, Target> TARGETS = new HashMap<>();

    private LaserTargets() {
    }

    public static void update(UUID shooter, ServerLevel level, Vec3 pos) {
        TARGETS.put(shooter, new Target(level, pos, level.getGameTime()));
    }

    public static void clear(UUID shooter) {
        TARGETS.remove(shooter);
    }

    /** The closest fresh laser target to {@code from} in the same level, within {@code radius}, or null if none. */
    public static Vec3 nearestTo(ServerLevel level, Vec3 from, double radius) {
        Vec3 best = null;
        double bestDistSq = radius * radius;
        for (Target target : TARGETS.values()) {
            if (target.level() != level || level.getGameTime() - target.updatedAt() > FRESH_TICKS) {
                continue;
            }
            double distSq = target.pos().distanceToSqr(from);
            if (distSq <= bestDistSq) {
                best = target.pos();
                bestDistSq = distSq;
            }
        }
        return best;
    }
}
