package com.koala.reactingreactions.content.ponder;

import com.george_vi.electroenergetics.CEEWireTypes;
import com.george_vi.electroenergetics.foundation.device.ElectricalDeviceBlock;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/** Electro Energetics wiring for the showcase world; only loaded when Electro Energetics is installed. */
final class ShowcaseElectrics {
    private ShowcaseElectrics() {
    }

    /** A creative (lossless) wire between node {@code nodeA} of the block at {@code a} and node {@code nodeB} of the one at {@code b}. */
    static void wire(ServerLevel level, BlockPos a, int nodeA, BlockPos b, int nodeB) {
        // A block placed by code only gets its wire nodes when a wire tool first asks for them.
        for (BlockPos pos : new BlockPos[] {a, b}) {
            if (level.getBlockState(pos).getBlock() instanceof ElectricalDeviceBlock<?> device) {
                device.ensureNodesExist(level, pos, level.getBlockState(pos));
            }
        }
        InfrastructureSavedData.load(level).connect(new InWorldNode(nodeA, a), new InWorldNode(nodeB, b), CEEWireTypes.CREATIVE.get());
    }
}
