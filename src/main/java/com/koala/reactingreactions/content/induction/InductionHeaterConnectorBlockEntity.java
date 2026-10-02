package com.koala.reactingreactions.content.induction;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The controller of an Induction Heater, and its electrical connection. It finds the rectangle it sits in, sets its resistance
 * (proportional to the number of blocks), reads the voltage across it and sets every plate to the matching heat level. The plates
 * carry Create's own {@code HEAT_LEVEL} property, so anything that reads a Blaze Burner's heat (Create's basins and mixers, this mod's
 * chambers and towers) treats them as a burner.
 */
public class InductionHeaterConnectorBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    public static final double OHMS_PER_BLOCK = 10.0;
    public static final double OPEN_CIRCUIT_OHMS = 1.0e7;
    private static final int REFERENCE_BLOCKS = 9;
    private static final double REFERENCE_OHMS = REFERENCE_BLOCKS * OHMS_PER_BLOCK;

    /**
     * Steady-state power, in watts, that Electro Energetics' Resistive Heater needs for each heat level: its heat settles at
     * power * 0.05 / (1 - 0.977) / 16000, whatever its voltage or resistance.
     */
    private static final double RESISTIVE_HEATER_WATTS_SMOULDERING = 736;
    private static final double RESISTIVE_HEATER_WATTS_KINDLED = 4416;
    private static final double RESISTIVE_HEATER_WATTS_SEETHING = 5888;
    /** This many resistive heaters, each pushed to a level, is what the reference-size induction heater is balanced against. */
    private static final int REFERENCE_HEATER_COUNT = 5;
    /** The induction heater should use a bit less power than the equivalent resistive heaters, not the same or more. */
    private static final double BALANCE_RATIO = 0.9;

    /**
     * Voltage for each heat level on the smallest (9 block) heater. Resistance grows with size, so bigger heaters scale this
     * up to draw the same power per block.
     */
    public static final double SMOULDERING_VOLTS = targetVolts(RESISTIVE_HEATER_WATTS_SMOULDERING);
    public static final double KINDLED_VOLTS = targetVolts(RESISTIVE_HEATER_WATTS_KINDLED);
    public static final double SEETHING_VOLTS = targetVolts(RESISTIVE_HEATER_WATTS_SEETHING);

    private static double targetVolts(double perHeaterWatts) {
        return Math.sqrt(BALANCE_RATIO * REFERENCE_HEATER_COUNT * perHeaterWatts * REFERENCE_OHMS);
    }

    /** The voltage a heater of {@code blocks} blocks needs for a level that takes {@code baseVolts} at 9 blocks. */
    public static double scaled(double baseVolts, int blocks) {
        return baseVolts * Math.max(REFERENCE_BLOCKS, blocks) / REFERENCE_BLOCKS;
    }

    private volatile double measuredVoltage;
    private InductionHeaterShape shape;
    private BlazeBurnerBlock.HeatLevel heat = BlazeBurnerBlock.HeatLevel.NONE;
    private int sizeX;
    private int sizeZ;
    private int count;
    private final List<BlockPos> heatedCells = new ArrayList<>();

    public InductionHeaterConnectorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    public static BlazeBurnerBlock.HeatLevel heatFor(double volts, int blocks) {
        if (volts >= scaled(SEETHING_VOLTS, blocks)) {
            return BlazeBurnerBlock.HeatLevel.SEETHING;
        }
        if (volts >= scaled(KINDLED_VOLTS, blocks)) {
            return BlazeBurnerBlock.HeatLevel.KINDLED;
        }
        if (volts >= scaled(SMOULDERING_VOLTS, blocks)) {
            return BlazeBurnerBlock.HeatLevel.SMOULDERING;
        }
        return BlazeBurnerBlock.HeatLevel.NONE;
    }

    public double resistance() {
        return shape == null ? OPEN_CIRCUIT_OHMS : shape.count() * OHMS_PER_BLOCK;
    }

    public void setMeasuredVoltage(double volts) {
        measuredVoltage = volts;
    }

    public boolean isFormed() {
        return shape != null;
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level == null || level.isClientSide) {
            return;
        }
        InductionHeaterShape found = InductionHeaterShape.scan(level, worldPosition);
        BlazeBurnerBlock.HeatLevel newHeat = found == null ? BlazeBurnerBlock.HeatLevel.NONE : heatFor(measuredVoltage, found.count());
        boolean changed = (found == null) != (shape == null) || newHeat != heat || (found != null && found.count() != count);
        // Cells that left the heater cool down.
        for (BlockPos old : heatedCells) {
            if (found == null || !found.cells().contains(old)) {
                reset(old);
            }
        }
        shape = found;
        heat = newHeat;
        heatedCells.clear();
        if (found != null) {
            sizeX = found.sizeX();
            sizeZ = found.sizeZ();
            count = found.count();
            heatedCells.addAll(found.cells());
            int minX = found.min().getX();
            int minZ = found.min().getZ();
            int maxX = minX + found.sizeX() - 1;
            int maxZ = minZ + found.sizeZ() - 1;
            for (BlockPos cell : found.cells()) {
                setCell(cell, newHeat, minX, maxX, minZ, maxZ);
            }
        }
        if (changed) {
            setChanged();
            sendData();
        }
    }

    /** Sets a cell's heat and its part of the one big coil (the loop segments through it). */
    private void setCell(BlockPos pos, BlazeBurnerBlock.HeatLevel value, int minX, int maxX, int minZ, int maxZ) {
        BlockState state = level.getBlockState(pos);
        if (!state.hasProperty(BlazeBurnerBlock.HEAT_LEVEL)) {
            return;
        }
        BlockState updated = state.setValue(BlazeBurnerBlock.HEAT_LEVEL, value);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            updated = updated.setValue(InductionHeaterCoil.of(direction), InductionHeaterCoil.connects(pos.getX(), pos.getZ(), direction, minX, maxX, minZ, maxZ));
        }
        updated = updated.setValue(InductionHeaterCoil.CIRCLE, InductionHeaterCoil.isCircle(pos.getX(), pos.getZ(), minX, maxX, minZ, maxZ));
        if (level.getBlockEntity(pos) instanceof InductionHeaterPlateBlockEntity plate) {
            plate.setControllerPos(worldPosition);
        }
        if (updated != state) {
            level.setBlock(pos, updated, Block.UPDATE_ALL);
        }
    }

    /** A cell that is no longer part of a heater: cold, and no coil. */
    private void reset(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.hasProperty(BlazeBurnerBlock.HEAT_LEVEL)) {
            return;
        }
        BlockState updated = state.setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.NONE).setValue(InductionHeaterCoil.CIRCLE, false);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            updated = updated.setValue(InductionHeaterCoil.of(direction), false);
        }
        if (level.getBlockEntity(pos) instanceof InductionHeaterPlateBlockEntity plate) {
            plate.setControllerPos(null);
        }
        if (updated != state) {
            level.setBlock(pos, updated, Block.UPDATE_ALL);
        }
    }

    @Override
    public void remove() {
        if (level != null && !level.isClientSide) {
            for (BlockPos cell : heatedCells) {
                reset(cell);
            }
        }
        super.remove();
    }

    @Override
    protected void read(CompoundTag compound, Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        sizeX = compound.getInt("SizeX");
        sizeZ = compound.getInt("SizeZ");
        count = compound.getInt("Count");
        measuredVoltage = compound.getDouble("Voltage");
        heat = BlazeBurnerBlock.HeatLevel.values()[Math.min(compound.getInt("Heat"), BlazeBurnerBlock.HeatLevel.values().length - 1)];
    }

    @Override
    public void write(CompoundTag compound, Provider registries, boolean clientPacket) {
        compound.putInt("SizeX", sizeX);
        compound.putInt("SizeZ", sizeZ);
        compound.putInt("Count", shape == null ? 0 : count);
        compound.putDouble("Voltage", measuredVoltage);
        compound.putInt("Heat", heat.ordinal());
        super.write(compound, registries, clientPacket);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        // On the client the shape is not scanned: the synced count says whether it is formed.
        if (count <= 0) {
            tooltip.add(Component.literal(" - Induction Heater (not formed)").withStyle(ChatFormatting.RED));
            tooltip.add(Component.literal(" - Needs a filled rectangle, at least 3x3, with one connector").withStyle(ChatFormatting.GRAY));
            return true;
        }
        double ohms = count * OHMS_PER_BLOCK;
        tooltip.add(Component.literal(String.format(" - Induction Heater (%dx%d, %d blocks)", sizeX, sizeZ, count)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal(String.format(" - Resistance: %.0f ohm", ohms)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal(String.format(" - Voltage: %.0f V", measuredVoltage)).withStyle(measuredVoltage >= scaled(SMOULDERING_VOLTS, count) ? ChatFormatting.GRAY : ChatFormatting.RED));
        tooltip.add(Component.literal(String.format(" - Heat: %s (kindled at %.0f V, seething at %.0f V)", heat.name().toLowerCase(Locale.ROOT), scaled(KINDLED_VOLTS, count), scaled(SEETHING_VOLTS, count)))
                .withStyle(heat == BlazeBurnerBlock.HeatLevel.NONE ? ChatFormatting.RED : ChatFormatting.GOLD));
        return true;
    }
}
