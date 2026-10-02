package com.koala.reactingreactions.content.induction;

import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** The Induction Heater as a resistor between the connector's two terminals; its resistance grows with the number of blocks. */
public class InductionHeaterDevice extends SimpleElectricalDevice {
    private InductionHeaterConnectorBlockEntity be;

    public InductionHeaterDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    private InductionHeaterConnectorBlockEntity blockEntity() {
        if (be != null && be.isRemoved()) {
            be = null;
        }
        if (be == null && level.isLoaded(pos) && level.getBlockEntity(pos) instanceof InductionHeaterConnectorBlockEntity found) {
            be = found;
        }
        return be;
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        InductionHeaterConnectorBlockEntity entity = blockEntity();
        bridges.builder(pos).resistor(0, 1, entity == null ? InductionHeaterConnectorBlockEntity.OPEN_CIRCUIT_OHMS : entity.resistance());
    }

    @Override
    public void postTick(SimulationResults results) {
        InductionHeaterConnectorBlockEntity entity = blockEntity();
        if (entity != null) {
            entity.setMeasuredVoltage(Math.abs(results.getVoltageAt(pos, 0, 1)));
        }
    }
}
