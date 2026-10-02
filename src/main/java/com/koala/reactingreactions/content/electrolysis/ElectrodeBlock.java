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

/** An electrode wires can attach to, used when Electro Energetics is installed. */
public class ElectrodeBlock extends ElectrodeBlockBase implements ElectricalDeviceBlock<SimpleNonTickingElectricalDevice> {
    public ElectrodeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public SimulatedDeviceType<SimpleNonTickingElectricalDevice> getDevice() {
        return CRRElectricalDevices.ELECTRODE_DEVICE.get();
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return Map.of(0, getNodePosition(level, pos, state, 0));
    }

    /** Relative to the block's corner: Electro Energetics adds the block position itself. At the rod's tip. */
    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int node) {
        Direction facing = state.getValue(FACING);
        return new Vec3(0.5, 0.5, 0.5).add(facing.getStepX() * -0.4375, facing.getStepY() * -0.4375, facing.getStepZ() * -0.4375);
    }
}
