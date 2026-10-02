package com.koala.reactingreactions.content.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Finds a closed hollow box of shell blocks around a controller placed anywhere on it: flood-fills the connected shell,
 * takes its bounding box, then checks that every face cell is shell and every inside cell is open.
 */
public final class HollowBoxScanner {
    // Well above the largest legal shell, so a stray wall network gives up quickly.
    private static final int FLOOD_FILL_CAP = 2000;

    private HollowBoxScanner() {
    }

    public record Result(BlockPos min, int sizeX, int sizeY, int sizeZ) {
        public int footprintArea() {
            return sizeX * sizeZ;
        }

        public int volume() {
            return sizeX * sizeY * sizeZ;
        }
    }

    /** {@code footprint} checks the x and z sizes beyond the plain bounds. */
    public static Result scan(Level level, BlockPos controllerPos, Predicate<BlockState> isWall, Predicate<BlockState> isController,
                              Predicate<BlockState> isInteriorOpen, int minSide, int maxSide, int minHeight, int maxHeight,
                              BiPredicate<Integer, Integer> footprint) {
        Predicate<BlockState> isShell = state -> isWall.test(state) || isController.test(state);
        Set<BlockPos> shell = floodFillShell(level, controllerPos, isShell);
        if (shell == null || shell.isEmpty()) {
            return null;
        }
        BlockPos.MutableBlockPos min = new BlockPos.MutableBlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        BlockPos.MutableBlockPos max = new BlockPos.MutableBlockPos(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
        for (BlockPos pos : shell) {
            min.set(Math.min(min.getX(), pos.getX()), Math.min(min.getY(), pos.getY()), Math.min(min.getZ(), pos.getZ()));
            max.set(Math.max(max.getX(), pos.getX()), Math.max(max.getY(), pos.getY()), Math.max(max.getZ(), pos.getZ()));
        }
        int sizeX = max.getX() - min.getX() + 1;
        int sizeY = max.getY() - min.getY() + 1;
        int sizeZ = max.getZ() - min.getZ() + 1;
        if (sizeX < minSide || sizeX > maxSide || sizeZ < minSide || sizeZ > maxSide || sizeY < minHeight || sizeY > maxHeight
                || !footprint.test(sizeX, sizeZ)) {
            return null;
        }
        Result result = new Result(min.immutable(), sizeX, sizeY, sizeZ);
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    BlockState state = level.getBlockState(result.min().offset(x, y, z));
                    if (!(isShellOffset(result, x, y, z) ? isShell : isInteriorOpen).test(state)) {
                        return null;
                    }
                }
            }
        }
        return result;
    }

    /** The controller of the shell {@code wallPos} is part of, or null. */
    public static BlockPos findController(Level level, BlockPos wallPos, Predicate<BlockState> isWall, Predicate<BlockState> isController) {
        Set<BlockPos> shell = floodFillShell(level, wallPos, state -> isWall.test(state) || isController.test(state));
        if (shell == null) {
            return null;
        }
        for (BlockPos pos : shell) {
            if (isController.test(level.getBlockState(pos))) {
                return pos;
            }
        }
        return null;
    }

    public static void forEachShellCell(Result result, Consumer<BlockPos> consumer) {
        for (int x = 0; x < result.sizeX(); x++) {
            for (int y = 0; y < result.sizeY(); y++) {
                for (int z = 0; z < result.sizeZ(); z++) {
                    if (isShellOffset(result, x, y, z)) {
                        consumer.accept(result.min().offset(x, y, z));
                    }
                }
            }
        }
    }

    public static boolean isShellCell(Result result, BlockPos pos) {
        int x = pos.getX() - result.min().getX();
        int y = pos.getY() - result.min().getY();
        int z = pos.getZ() - result.min().getZ();
        return x >= 0 && x < result.sizeX() && y >= 0 && y < result.sizeY() && z >= 0 && z < result.sizeZ() && isShellOffset(result, x, y, z);
    }

    private static boolean isShellOffset(Result result, int x, int y, int z) {
        return x == 0 || x == result.sizeX() - 1 || y == 0 || y == result.sizeY() - 1 || z == 0 || z == result.sizeZ() - 1;
    }

    /** Null when the shell grows past {@link #FLOOD_FILL_CAP}. */
    private static Set<BlockPos> floodFillShell(Level level, BlockPos seed, Predicate<BlockState> isShell) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        visited.add(seed.immutable());
        queue.add(seed.immutable());
        while (!queue.isEmpty()) {
            if (visited.size() > FLOOD_FILL_CAP) {
                return null;
            }
            BlockPos current = queue.poll();
            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (!visited.contains(next) && isShell.test(level.getBlockState(next))) {
                    visited.add(next);
                    queue.add(next);
                }
            }
        }
        return visited;
    }
}
