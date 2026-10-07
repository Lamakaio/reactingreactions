package com.koala.reactingreactions.content.electrolysis;

import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.electrical_properties.ElectricalProperties;
import com.george_vi.electroenergetics.simulation.infrastructure.InWorldNodeData;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Joins the vat's two electrode columns through the electrolyte's resistance (sized to the running recipe, see Voltmeter). Each column is first bridged into one node,
 * since every electrode block is its own node, and so is the terminal in the end wall beside it, where the wire attaches.
 */
public class ElectrolysisVatDevice extends SimpleElectricalDevice {
    // Not zero: an ideal short would make the circuit solver's matrix singular.
    private static final double COLUMN_LINK_RESISTANCE_OHMS = 0.001;

    /** The electrolyte's resistance this tick, sized by the vat to its recipe (Voltmeter). */
    private double resistance = 10_000;

    public ElectrolysisVatDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        ElectrolysisVatControllerBlockEntity vat = vat();
        BlockPos a = vat == null ? null : vat.getElectrodeA();
        BlockPos b = vat == null ? null : vat.getElectrodeB();
        if (a == null || b == null) {
            return;
        }
        bridgeColumn(bridges, vat.getElectrodeColumn(a));
        bridgeColumn(bridges, vat.getElectrodeColumn(b));
        bridgeToTerminal(bridges, vat, a);
        bridgeToTerminal(bridges, vat, b);
        resistance = vat.loadResistance();
        bridges.bridge(new InWorldNode(0, a), new InWorldNode(0, b), resistor(resistance));
    }

    private void bridgeToTerminal(BridgeCollector bridges, ElectrolysisVatControllerBlockEntity vat, BlockPos top) {
        BlockPos terminal = vat.getTerminalFor(top);
        if (terminal != null) {
            bridgeToRegisteredNodesAt(bridges, new InWorldNode(0, top), terminal);
        }
    }

    private void bridgeToRegisteredNodesAt(BridgeCollector bridges, InWorldNode from, BlockPos at) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        List<InWorldNodeData> registered = InfrastructureSavedData.load(serverLevel).getNodesAt(at);
        if (registered.isEmpty()) {
            bridges.bridge(from, new InWorldNode(0, at), resistor(COLUMN_LINK_RESISTANCE_OHMS));
            return;
        }
        for (InWorldNodeData data : registered) {
            bridges.bridge(from, data.node, resistor(COLUMN_LINK_RESISTANCE_OHMS));
        }
    }

    private void bridgeColumn(BridgeCollector bridges, List<BlockPos> column) {
        for (int i = 0; i + 1 < column.size(); i++) {
            bridges.bridge(new InWorldNode(0, column.get(i)), new InWorldNode(0, column.get(i + 1)), resistor(COLUMN_LINK_RESISTANCE_OHMS));
        }
    }

    /** Bridged as properties: EE 1.1 and 1.2 differ in their plain-resistance bridge, but both have this one. */
    private static ElectricalProperties resistor(double ohms) {
        return ElectricalProperties.resistor(ohms);
    }

    @Override
    public void postTick(SimulationResults results) {
        ElectrolysisVatControllerBlockEntity vat = vat();
        if (vat == null) {
            return;
        }
        BlockPos a = vat.getElectrodeA();
        BlockPos b = vat.getElectrodeB();
        vat.voltmeter().set(a != null && b != null ? Math.abs(results.getVoltageAt(new InWorldNode(0, a), new InWorldNode(0, b))) : 0, resistance);
    }

    @Nullable
    private ElectrolysisVatControllerBlockEntity vat() {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof ElectrolysisVatControllerBlockEntity vat ? vat : null;
    }
}
