package com.koala.reactingreactions.content.induction;

import com.koala.reactingreactions.registry.CRRBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** The flat rectangle of plates and one connector that makes up an Induction Heater. */
public record InductionHeaterShape(BlockPos min, int sizeX, int sizeZ, List<BlockPos> cells) {
    public static final int MIN_SIDE = 3;
    public static final int MAX_SIDE = 16;

    public int count() {
        return cells.size();
    }

    private static boolean isPart(BlockState state) {
        return state.is(CRRBlocks.INDUCTION_HEATER_PLATE.get()) || state.is(CRRBlocks.INDUCTION_HEATER_CONNECTOR.get());
    }

    /**
     * The heater the block at {@code start} belongs to, or null when it is not a valid one: the connected plates and connectors on
     * one level must fill a rectangle of at least 3x3 exactly, with exactly one connector.
     */
    public static InductionHeaterShape scan(Level level, BlockPos start) {
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        seen.add(start);
        int minX = start.getX();
        int maxX = start.getX();
        int minZ = start.getZ();
        int maxZ = start.getZ();
        int connectors = 0;
        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            BlockState state = level.getBlockState(pos);
            if (state.is(CRRBlocks.INDUCTION_HEATER_CONNECTOR.get())) {
                connectors++;
            }
            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX());
            minZ = Math.min(minZ, pos.getZ());
            maxZ = Math.max(maxZ, pos.getZ());
            if (maxX - minX >= MAX_SIDE || maxZ - minZ >= MAX_SIDE) {
                return null;
            }
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = pos.relative(direction);
                if (!seen.contains(next) && isPart(level.getBlockState(next))) {
                    seen.add(next);
                    queue.add(next);
                }
            }
        }
        int sizeX = maxX - minX + 1;
        int sizeZ = maxZ - minZ + 1;
        if (sizeX < MIN_SIDE || sizeZ < MIN_SIDE || seen.size() != sizeX * sizeZ || connectors != 1) {
            return null;
        }
        return new InductionHeaterShape(new BlockPos(minX, start.getY(), minZ), sizeX, sizeZ, new ArrayList<>(seen));
    }
}
