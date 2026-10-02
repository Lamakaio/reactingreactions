package com.koala.reactingreactions.content.multiblock.attachment;

import com.koala.reactingreactions.content.multiblock.HollowBoxScanner;
import com.koala.reactingreactions.content.multiblock.MultiblockControllerBlockEntity;
import com.koala.reactingreactions.content.multiblock.MultiblockWallBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * What is mounted on a formed machine, counted on each of its scans. A tier takes {@code slots} attachments; past that,
 * the extra ones do nothing.
 */
public record MachineAttachments(int outlets, int expansionTanks, int gaskets, float pumpBonus, List<BlockPos> gauges, int mounted, int slots) {
    public static final MachineAttachments NONE = new MachineAttachments(0, 0, 0, 0, List.of(), 0, 0);
    /** What one pump adds to the speed at full rotation, and the rotation that counts as full. */
    private static final float PUMP_BONUS = 0.5F;
    private static final float PUMP_FULL_RPM = 256;

    /** {@code installedOutlets} and {@code installedGaskets} are upgrades used on the machine: they take slots first. */
    public static MachineAttachments scan(Level level, HollowBoxScanner.Result structure, int slots, int installedOutlets, int installedGaskets) {
        int outlets = installedOutlets, tanks = 0, gaskets = installedGaskets, mounted = installedOutlets + installedGaskets;
        float pumps = 0;
        List<BlockPos> gauges = new ArrayList<>();
        List<BlockPos> shell = new ArrayList<>();
        HollowBoxScanner.forEachShellCell(structure, shell::add);
        for (BlockPos wall : shell) {
            for (Direction side : Direction.Plane.HORIZONTAL) {
                BlockPos at = wall.relative(side);
                BlockState state = level.getBlockState(at);
                if (contains(structure, at) || !(state.getBlock() instanceof MachineAttachment attachment)
                        || MachineAttachment.mountedTowards(state) != side.getOpposite()) {
                    continue;
                }
                if (mounted++ >= slots) {
                    continue;
                }
                switch (attachment.kind()) {
                    case OUTLET -> outlets++;
                    case EXPANSION_TANK -> tanks++;
                    case GASKET -> gaskets++;
                    case GAUGE -> gauges.add(at.immutable());
                    case CIRCULATION_PUMP -> {
                        if (level.getBlockEntity(at) instanceof KineticBlockEntity pump && !pump.isOverStressed()) {
                            pumps += PUMP_BONUS * Math.min(Math.abs(pump.getSpeed()), PUMP_FULL_RPM) / PUMP_FULL_RPM;
                        }
                    }
                }
            }
        }
        return new MachineAttachments(outlets, tanks, gaskets, pumps, List.copyOf(gauges), mounted, slots);
    }

    private static boolean contains(HollowBoxScanner.Result structure, BlockPos pos) {
        BlockPos rel = pos.subtract(structure.min());
        return rel.getX() >= 0 && rel.getY() >= 0 && rel.getZ() >= 0 && rel.getX() < structure.sizeX() && rel.getY() < structure.sizeY()
                && rel.getZ() < structure.sizeZ();
    }

    /** How much faster recipes run. */
    public float speedFactor() {
        return 1 + pumpBonus;
    }

    public boolean sealed() {
        return gaskets > 0;
    }

    /** The formed machine an attachment at {@code pos} is mounted on, or null. */
    @Nullable
    public static MultiblockControllerBlockEntity<?> machineFor(Level level, BlockPos pos, BlockState state) {
        BlockEntity behind = level.getBlockEntity(pos.relative(MachineAttachment.mountedTowards(state)));
        MultiblockControllerBlockEntity<?> controller = behind instanceof MultiblockWallBlockEntity wall ? wall.getController()
                : behind instanceof MultiblockControllerBlockEntity<?> own ? own : null;
        return controller != null && controller.getStructure() != null ? controller : null;
    }
}
