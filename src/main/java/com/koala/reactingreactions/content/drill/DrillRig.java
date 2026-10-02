package com.koala.reactingreactions.content.drill;

import com.koala.reactingreactions.content.multiblock.MachineTiers;
import com.koala.reactingreactions.registry.CRRBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * The tower rig shared by the Derrick (oil) and the Mineral Drill: three 3x3 layers below a controller, with a column of
 * {@link DrillPipeBlock} through the centre ending in whichever head block the controller expects. On every layer a
 * {@link DerrickBlock} stands in each corner; the top and bottom layers also have a {@link DerrickTrussBlock} on each side of
 * the pipe. Formed, the blocks show their pieces of the whole tower (see {@link MachineTiers#PART}, cut by
 * {@code tools/machine_models.py}).
 */
public final class DrillRig {
    private DrillRig() {
    }

    private static boolean is(Level level, BlockPos pos, Block block) {
        return level.getBlockState(pos).is(block);
    }

    /** Fills {@code io} with the derrick blocks (not trusses) and returns whether the tower below {@code controllerPos} is complete. */
    public static boolean checkTower(Level level, BlockPos controllerPos, List<BlockPos> io) {
        var derrick = CRRBlocks.DERRICK_BLOCK.get();
        var truss = CRRBlocks.DERRICK_TRUSS.get();
        var pipe = CRRBlocks.DRILL_PIPE.get();
        for (int layer = 1; layer <= 3; layer++) {
            BlockPos centre = controllerPos.below(layer);
            if (!is(level, centre, pipe)) {
                return false;
            }
            for (int sx = -1; sx <= 1; sx += 2) {
                for (int sz = -1; sz <= 1; sz += 2) {
                    BlockPos corner = centre.offset(sx, 0, sz);
                    if (!is(level, corner, derrick)) {
                        return false;
                    }
                    io.add(corner);
                }
            }
            if (layer != 2) {
                for (Direction side : Direction.Plane.HORIZONTAL) {
                    if (!is(level, centre.relative(side), truss)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /** Gives the tower's blocks their formed pieces, or takes them back. The tower is modelled from its lowest north-west corner. */
    public static void setLook(Level level, BlockPos controllerPos, boolean formed) {
        BlockPos min = controllerPos.offset(-1, -3, -1);
        for (int layer = 1; layer <= 3; layer++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos pos = controllerPos.offset(dx, -layer, dz);
                    BlockState state = level.getBlockState(pos);
                    if ((dx == 0 && dz == 0) || !state.hasProperty(MachineTiers.PART)
                            || !(state.is(CRRBlocks.DERRICK_BLOCK.get()) || state.is(CRRBlocks.DERRICK_TRUSS.get()))) {
                        continue;
                    }
                    BlockPos rel = pos.subtract(min);
                    int part = formed ? 1 + (rel.getY() * 3 + rel.getZ()) * 3 + rel.getX() : 0;
                    if (state.getValue(MachineTiers.PART) != part) {
                        level.setBlock(pos, state.setValue(MachineTiers.PART, part), Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
    }

    /** Length of the pipe column hanging from the block right under the tower's lowest layer (4 below the controller), and the block after it (the head, whatever it turns out to be). */
    public static int pipeLength(Level level, BlockPos controllerPos, int maxLength) {
        int length = 0;
        BlockPos pos = controllerPos.below(4);
        while (length < maxLength && is(level, pos, CRRBlocks.DRILL_PIPE.get())) {
            length++;
            pos = pos.below();
        }
        return length;
    }

    public static BlockPos headPos(BlockPos controllerPos, int pipeLength) {
        return controllerPos.below(4 + pipeLength);
    }
}
