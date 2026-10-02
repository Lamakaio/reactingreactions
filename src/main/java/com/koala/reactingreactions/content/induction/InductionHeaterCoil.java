package com.koala.reactingreactions.content.induction;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * The heater's one big coil, drawn across all its plates as concentric rectangular loops. Each block shows only the line segments
 * of the loop that passes through it: from its centre to every side where the next block of the same loop is. A block's loop is
 * its distance to the nearest edge of the rectangle, so the outermost blocks form the outer loop, the next ones the loop inside, and so on.
 */
public final class InductionHeaterCoil {
    public static final BooleanProperty NORTH = BooleanProperty.create("coil_north");
    public static final BooleanProperty EAST = BooleanProperty.create("coil_east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("coil_south");
    public static final BooleanProperty WEST = BooleanProperty.create("coil_west");
    /** A circle on the block instead of loop segments: the innermost blocks, where the loop is too thin to be a ring. */
    public static final BooleanProperty CIRCLE = BooleanProperty.create("coil_circle");

    private InductionHeaterCoil() {
    }

    public static BooleanProperty of(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            default -> throw new IllegalArgumentException("horizontal only: " + direction);
        };
    }

    /** The loop a cell belongs to: 0 for the outer ring, counting inwards. */
    public static int loop(int x, int z, int minX, int maxX, int minZ, int maxZ) {
        return Math.min(Math.min(x - minX, maxX - x), Math.min(z - minZ, maxZ - z));
    }

    /**
     * Whether a cell's loop is too thin to close into a ring (one block wide or high, as the middle of a 3x3, or of a 3x4): its
     * blocks each get a circle instead.
     */
    public static boolean isCircle(int x, int z, int minX, int maxX, int minZ, int maxZ) {
        int loop = loop(x, z, minX, maxX, minZ, maxZ);
        int width = (maxX - minX + 1) - 2 * loop;
        int depth = (maxZ - minZ + 1) - 2 * loop;
        return width < 2 || depth < 2;
    }

    /** Whether the loop through a cell continues towards {@code direction}: the neighbour is inside the rectangle and in the same loop. */
    public static boolean connects(int x, int z, Direction direction, int minX, int maxX, int minZ, int maxZ) {
        if (isCircle(x, z, minX, maxX, minZ, maxZ)) {
            return false;
        }
        int nx = x + direction.getStepX();
        int nz = z + direction.getStepZ();
        if (nx < minX || nx > maxX || nz < minZ || nz > maxZ) {
            return false;
        }
        return loop(nx, nz, minX, maxX, minZ, maxZ) == loop(x, z, minX, maxX, minZ, maxZ);
    }
}
