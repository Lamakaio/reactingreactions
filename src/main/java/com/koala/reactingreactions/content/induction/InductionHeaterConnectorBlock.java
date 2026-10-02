package com.koala.reactingreactions.content.induction;

import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.george_vi.electroenergetics.foundation.device.ElectricalDeviceBlock;
import com.koala.reactingreactions.registry.CRRElectricalDevices;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

/** The heater connector with its two Electro Energetics terminals. */
public class InductionHeaterConnectorBlock extends InductionHeaterConnectorBlockBase implements ElectricalDeviceBlock<InductionHeaterDevice> {
    public InductionHeaterConnectorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public SimulatedDeviceType<InductionHeaterDevice> getDevice() {
        return CRRElectricalDevices.INDUCTION_HEATER_DEVICE.get();
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int node) {
        // Two terminals side by side on the face the block points at, slightly inside it.
        Direction facing = state.getValue(FACING);
        double side = node == 0 ? -0.25 : 0.25;
        if (facing.getAxis() == Direction.Axis.Y) {
            // On the top or bottom face the two terminals sit side by side along x.
            return new Vec3(0.5 + side, 0.5 + facing.getStepY() * 0.47, 0.5);
        }
        return new Vec3(0.5 + facing.getStepX() * 0.47 - facing.getStepZ() * side, 0.5, 0.5 + facing.getStepZ() * 0.47 + facing.getStepX() * side);
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return Map.of(0, getNodePosition(level, pos, state, 0), 1, getNodePosition(level, pos, state, 1));
    }
}
