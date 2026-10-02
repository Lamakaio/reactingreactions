package com.koala.reactingreactions.content.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

/**
 * Fixed-size machines: each tier is one size, and once formed every shell block shows its own piece of the whole machine.
 * A machine is modelled with its controller on the north side, so a tier's {@code width} runs along the controller's side and
 * its {@code depth} away from it. A block's {@code part} is 1 + its tier's base + its position in that model; {@code facing}
 * is the side the controller is on. The models are cut by {@code tools/machine_models.py}, which must use the same numbering.
 */
public final class MachineTiers {
    /**
     * One size of a machine: what it has before attachments ({@link com.koala.reactingreactions.content.multiblock.attachment.MachineAttachments}),
     * and how many attachments it takes. {@code capacity} is each tank's, in mB.
     */
    public record Tier(int width, int depth, int height, int outputs, int capacity, int slots) {
        public static Tier square(int side, int height, int outputs, int capacity, int slots) {
            return new Tier(side, side, height, outputs, capacity, slots);
        }
    }

    /** A formed machine's tier and the side its controller is on. */
    public record Fit(int index, Tier tier, Direction facing) {
    }

    public static final List<Tier> REACTION_CHAMBER = List.of(Tier.square(3, 4, 1, 4000, 4), Tier.square(5, 5, 2, 8000, 8));
    public static final List<Tier> AIRLESS_OVEN = List.of(Tier.square(3, 3, 1, 4000, 4));
    public static final List<Tier> ELECTROLYSIS_VAT = List.of(new Tier(5, 3, 3, 2, 4000, 4), new Tier(7, 5, 4, 3, 8000, 8));
    /** 0 is unformed; enough for every position of the machine with the most (both Electrolysis Vat tiers: 45 + 140). */
    public static final IntegerProperty PART = IntegerProperty.create("part", 0, 185);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    /** Formed with an Outlet Manifold installed: copper pipes run over the machine. */
    public static final BooleanProperty PIPED = BooleanProperty.create("piped");
    /** Formed with a Gasket installed: thicker, bolted joints. */
    public static final BooleanProperty SEALED = BooleanProperty.create("sealed");

    private MachineTiers() {
    }

    /** The tier this structure is, with its controller in the middle of a side one block above the floor; null if none fits. */
    @Nullable
    public static Fit fit(List<Tier> tiers, HollowBoxScanner.Result structure, BlockPos controller) {
        Direction facing = controllerSide(structure, controller);
        if (facing == null) {
            return null;
        }
        boolean across = facing.getAxis() == Direction.Axis.X;
        int width = across ? structure.sizeZ() : structure.sizeX();
        int depth = across ? structure.sizeX() : structure.sizeZ();
        for (int i = 0; i < tiers.size(); i++) {
            Tier tier = tiers.get(i);
            if (tier.width() == width && tier.depth() == depth && tier.height() == structure.sizeY()) {
                return new Fit(i, tier, facing);
            }
        }
        return null;
    }

    @Nullable
    private static Direction controllerSide(HollowBoxScanner.Result structure, BlockPos controller) {
        BlockPos rel = controller.subtract(structure.min());
        int lastX = structure.sizeX() - 1;
        int lastZ = structure.sizeZ() - 1;
        if (rel.getY() != 1) {
            return null;
        }
        boolean midX = lastX % 2 == 0 && rel.getX() == lastX / 2;
        boolean midZ = lastZ % 2 == 0 && rel.getZ() == lastZ / 2;
        if (midX && rel.getZ() == 0) return Direction.NORTH;
        if (midX && rel.getZ() == lastZ) return Direction.SOUTH;
        if (midZ && rel.getX() == 0) return Direction.WEST;
        if (midZ && rel.getX() == lastX) return Direction.EAST;
        return null;
    }

    /** A point of the machine as modelled (in pixels) in the world, for effects at a modelled part. */
    public static Vec3 toWorld(HollowBoxScanner.Result structure, Fit fit, double x, double y, double z) {
        double w = fit.tier().width() * 16;
        double d = fit.tier().depth() * 16;
        double wx, wz;
        switch (fit.facing()) {
            case SOUTH -> { wx = w - x; wz = d - z; }
            case EAST -> { wx = d - z; wz = x; }
            case WEST -> { wx = z; wz = w - x; }
            default -> { wx = x; wz = z; }
        }
        return Vec3.atLowerCornerOf(structure.min()).add(wx / 16, y / 16, wz / 16);
    }

    /** The block at a modelled position. */
    public static BlockPos cell(HollowBoxScanner.Result structure, Fit fit, int mx, int my, int mz) {
        return BlockPos.containing(toWorld(structure, fit, mx * 16 + 8, my * 16 + 8, mz * 16 + 8));
    }

    private static int part(List<Tier> tiers, Fit fit, HollowBoxScanner.Result structure, BlockPos pos) {
        int w = fit.tier().width();
        int d = fit.tier().depth();
        int wx = pos.getX() - structure.min().getX();
        int wy = pos.getY() - structure.min().getY();
        int wz = pos.getZ() - structure.min().getZ();
        // Back from the world to the model (the blockstate turns it clockwise).
        int mx, mz;
        switch (fit.facing()) {
            case SOUTH -> { mx = w - 1 - wx; mz = d - 1 - wz; }
            case EAST -> { mx = wz; mz = d - 1 - wx; }
            case WEST -> { mx = w - 1 - wz; mz = wx; }
            default -> { mx = wx; mz = wz; }
        }
        int base = 0;
        for (int i = 0; i < fit.index(); i++) {
            Tier t = tiers.get(i);
            base += t.width() * t.depth() * t.height();
        }
        return 1 + base + (wy * d + mz) * w + mx;
    }

    /** Gives every shell block of a formed machine its piece, with the pipes and joints of its installed upgrades. */
    public static void apply(Level level, List<Tier> tiers, Fit fit, HollowBoxScanner.Result structure, Predicate<BlockState> isPart, boolean piped,
                             boolean sealed) {
        HollowBoxScanner.forEachShellCell(structure, pos -> {
            BlockState state = level.getBlockState(pos);
            if (isPart.test(state) && state.hasProperty(PART)) {
                BlockState wanted = state.setValue(PART, part(tiers, fit, structure, pos)).setValue(FACING, fit.facing())
                        .setValue(PIPED, piped).setValue(SEALED, sealed);
                if (wanted != state) {
                    level.setBlock(pos, wanted, Block.UPDATE_CLIENTS);
                }
            }
        });
    }

    /** Back to plain blocks. */
    public static void clear(Level level, HollowBoxScanner.Result structure, Predicate<BlockState> isPart) {
        HollowBoxScanner.forEachShellCell(structure, pos -> {
            BlockState state = level.getBlockState(pos);
            if (isPart.test(state) && state.hasProperty(PART) && state.getValue(PART) != 0) {
                level.setBlock(pos, state.setValue(PART, 0).setValue(PIPED, false).setValue(SEALED, false), Block.UPDATE_CLIENTS);
            }
        });
    }
}
