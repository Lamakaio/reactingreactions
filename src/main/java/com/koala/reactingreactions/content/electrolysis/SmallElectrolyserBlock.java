package com.koala.reactingreactions.content.electrolysis;

import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.george_vi.electroenergetics.foundation.device.ElectricalDeviceBlock;
import com.koala.reactingreactions.registry.CRRElectricalDevices;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

/** The small electrolyser with its two Electro Energetics terminals. */
public class SmallElectrolyserBlock extends SmallElectrolyserBlockBase implements ElectricalDeviceBlock<SmallElectrolyserDevice> {
    public SmallElectrolyserBlock(Properties properties) {
        super(properties);
    }

    @Override
    public SimulatedDeviceType<SmallElectrolyserDevice> getDevice() {
        return CRRElectricalDevices.SMALL_ELECTROLYSER_DEVICE.get();
    }

    /** Local (0-1) offsets, like {@link ElectrodeBlock#getNodePosition}: two terminals on the top face. */
    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int node) {
        return node == 0 ? new Vec3(0.25, 0.97, 0.5) : new Vec3(0.75, 0.97, 0.5);
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return Map.of(0, getNodePosition(level, pos, state, 0), 1, getNodePosition(level, pos, state, 1));
    }
}
