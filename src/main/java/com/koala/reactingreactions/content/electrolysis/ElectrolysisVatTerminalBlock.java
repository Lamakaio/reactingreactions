package com.koala.reactingreactions.content.electrolysis;

import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.george_vi.electroenergetics.foundation.device.ElectricalDeviceBlock;
import com.george_vi.electroenergetics.foundation.device.SimpleNonTickingElectricalDevice;
import com.koala.reactingreactions.registry.CRRElectricalDevices;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

/** The vat terminal wires attach to, used when Electro Energetics is installed. */
public class ElectrolysisVatTerminalBlock extends ElectrolysisVatTerminalBlockBase implements ElectricalDeviceBlock<SimpleNonTickingElectricalDevice> {
    public ElectrolysisVatTerminalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public SimulatedDeviceType<SimpleNonTickingElectricalDevice> getDevice() {
        return CRRElectricalDevices.VAT_TERMINAL_DEVICE.get();
    }

    /** Local offsets: formed, at the brass knob of the junction box outside the end wall; otherwise just over the block. */
    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int node) {
        Direction out = outward(state);
        return out == null ? new Vec3(0.5, 1.05, 0.5) : new Vec3(0.5 + 0.75 * out.getStepX(), 0.5, 0.5 + 0.75 * out.getStepZ());
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return Map.of(0, getNodePosition(level, pos, state, 0));
    }
}
