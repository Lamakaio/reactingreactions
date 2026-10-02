package com.koala.reactingreactions.content.toxic;

import com.koala.reactingreactions.Config;
import com.koala.reactingreactions.registry.CRRTags;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Which Atmospheric Scrubbers are working right now, and whether a leak point is covered by one. A scrubber covers a small radius in
 * the open, and a larger one when both it and the point are "in a room": an airtight block above and below (a cheap column check
 * instead of a room search).
 */
public final class Scrubbers {
    private static final int ROOM_SCAN = 8;
    private static final Map<ResourceKey<Level>, Set<BlockPos>> ACTIVE = new ConcurrentHashMap<>();

    private Scrubbers() {
    }

    public static void setActive(ServerLevel level, BlockPos pos, boolean active) {
        Set<BlockPos> set = ACTIVE.computeIfAbsent(level.dimension(), key -> ConcurrentHashMap.newKeySet());
        if (active) {
            set.add(pos.immutable());
        } else {
            set.remove(pos);
        }
    }

    /** Whether a working scrubber covers this point (gas leaks there are stopped). */
    public static boolean covers(Level level, BlockPos point) {
        Set<BlockPos> set = ACTIVE.get(level.dimension());
        if (set == null || set.isEmpty()) {
            return false;
        }
        int openRadius = Config.number(Config.SCRUBBER_OPEN_RADIUS, 6);
        int roomRadius = Config.number(Config.SCRUBBER_ROOM_RADIUS, 16);
        Boolean pointRoomed = null;
        for (BlockPos scrubber : set) {
            int dx = Math.abs(scrubber.getX() - point.getX());
            int dz = Math.abs(scrubber.getZ() - point.getZ());
            int dy = Math.abs(scrubber.getY() - point.getY());
            if (dx <= openRadius && dy <= openRadius && dz <= openRadius) {
                return true;
            }
            if (dx <= roomRadius && dz <= roomRadius && dy <= ROOM_SCAN * 2) {
                if (pointRoomed == null) {
                    pointRoomed = isInRoom(level, point);
                }
                if (pointRoomed && isInRoom(level, scrubber)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** An airtight block above and one below within a few blocks (the first solid thing in each direction must be airtight). */
    public static boolean isInRoom(Level level, BlockPos pos) {
        return airtightFirst(level, pos, 1) && airtightFirst(level, pos, -1);
    }

    private static boolean airtightFirst(Level level, BlockPos pos, int direction) {
        for (int i = 1; i <= ROOM_SCAN; i++) {
            BlockState state = level.getBlockState(pos.offset(0, direction * i, 0));
            if (state.isAir()) {
                continue;
            }
            return state.is(CRRTags.AIRTIGHT);
        }
        return false;
    }

    public static void clearAll() {
        ACTIVE.clear();
    }
}
