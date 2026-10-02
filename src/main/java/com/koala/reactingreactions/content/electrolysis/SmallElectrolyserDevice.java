package com.koala.reactingreactions.content.electrolysis;

import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** A fixed resistor between the two terminals, reporting its voltage to the block entity. */
public class SmallElectrolyserDevice extends SimpleElectricalDevice {
    private static final double RESISTANCE_OHMS = 250.0;

    private SmallElectrolyserBlockEntity be;

    public SmallElectrolyserDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        bridges.builder(pos).resistor(0, 1, RESISTANCE_OHMS);
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
                be.voltmeter().set(voltage);
            }
        }
    }
}
