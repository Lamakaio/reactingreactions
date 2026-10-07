package com.koala.reactingreactions.content.electrolysis;

import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** A resistor between the two terminals, sized by the block entity to its recipe, reporting its voltage and power back. */
public class SmallElectrolyserDevice extends SimpleElectricalDevice {
    private static final double IDLE_RESISTANCE_OHMS = 10_000;

    private SmallElectrolyserBlockEntity be;
    private double resistance = IDLE_RESISTANCE_OHMS;

    public SmallElectrolyserDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        resistance = be != null && !be.isRemoved() ? be.loadResistance() : IDLE_RESISTANCE_OHMS;
        bridges.builder(pos).resistor(0, 1, resistance);
    }

    @Override
    public void postTick(SimulationResults results) {
        double voltage = Math.abs(results.getVoltageAt(pos, 0, 1));
        if (be == null && level.isLoaded(pos) && level.getBlockEntity(pos) instanceof SmallElectrolyserBlockEntity found) {
            be = found;
        }
        if (be != null) {
            if (be.isRemoved()) {
                be = null;
            } else {
                be.voltmeter().set(voltage, resistance);
            }
        }
    }
}
