package com.koala.reactingreactions.content.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Window walls for formed multiblocks, like a Create fluid tank: the controller turns the middle of each side face into a
 * window and marks which faces touch the outside, so only those get drawn. Purely visual.
 */
public final class MultiblockWindows {
    /** Rounded at the bottom on the floor row and at the top on the roof row. */
    public enum WindowShape implements StringRepresentable {
        NONE("none"), MIDDLE("middle"), BOTTOM("bottom"), TOP("top");

        private final String name;

        WindowShape(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public static final EnumProperty<WindowShape> WINDOW = EnumProperty.create("window", WindowShape.class);
    /** Per face: whether it touches the outside of the machine. */
    public static final Map<Direction, BooleanProperty> OUTER = new EnumMap<>(Direction.class);

    static {
        for (Direction direction : Direction.values()) {
            OUTER.put(direction, BooleanProperty.create("outer_" + direction.getName()));
        }
    }

    public static void addProperties(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WINDOW);
        OUTER.values().forEach(builder::add);
    }

    /** Not part of a formed machine: no window, every face drawn. */
    public static BlockState withDefaults(BlockState state) {
        state = state.setValue(WINDOW, WindowShape.NONE);
        for (BooleanProperty flag : OUTER.values()) {
            state = state.setValue(flag, true);
        }
        return state;
    }

    private static WindowShape shapeFor(HollowBoxScanner.Result structure, BlockPos pos, BlockPos controller) {
        // The controller's column stays plain, so the controller reads as part of the wall.
        if (pos.getX() == controller.getX() && pos.getZ() == controller.getZ()) {
            return WindowShape.NONE;
        }
        int x = pos.getX() - structure.min().getX();
        int y = pos.getY() - structure.min().getY();
        int z = pos.getZ() - structure.min().getZ();
        boolean sideX = x == 0 || x == structure.sizeX() - 1;
        boolean sideZ = z == 0 || z == structure.sizeZ() - 1;
        if (sideX == sideZ) {
            return WindowShape.NONE;
        }
        return y == 0 ? WindowShape.BOTTOM : y == structure.sizeY() - 1 ? WindowShape.TOP : WindowShape.MIDDLE;
    }

    private static boolean isInside(HollowBoxScanner.Result structure, BlockPos pos) {
        int x = pos.getX() - structure.min().getX();
        int y = pos.getY() - structure.min().getY();
        int z = pos.getZ() - structure.min().getZ();
        return x >= 0 && y >= 0 && z >= 0 && x < structure.sizeX() && y < structure.sizeY() && z < structure.sizeZ();
    }

    public static void apply(Level level, HollowBoxScanner.Result structure, Predicate<BlockState> isPart, BlockPos controller) {
        HollowBoxScanner.forEachShellCell(structure, pos -> {
            BlockState state = level.getBlockState(pos);
            if (!isPart.test(state) || !state.hasProperty(WINDOW)) {
                return;
            }
            BlockState wanted = state.setValue(WINDOW, shapeFor(structure, pos, controller));
            for (Direction direction : Direction.values()) {
                wanted = wanted.setValue(OUTER.get(direction), !isInside(structure, pos.relative(direction)));
            }
            if (wanted != state) {
                level.setBlock(pos, wanted, Block.UPDATE_CLIENTS);
            }
        });
    }

    public static void clear(Level level, HollowBoxScanner.Result structure, Predicate<BlockState> isPart) {
        HollowBoxScanner.forEachShellCell(structure, pos -> {
            BlockState state = level.getBlockState(pos);
            if (isPart.test(state) && state.hasProperty(WINDOW) && state != withDefaults(state)) {
                level.setBlock(pos, withDefaults(state), Block.UPDATE_CLIENTS);
            }
        });
    }
}
