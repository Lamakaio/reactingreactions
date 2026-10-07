package com.koala.reactingreactions.content.multiblock.attachment;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** A block mounted against the outside of a formed machine's wall, changing how the machine works (see {@link MachineAttachments}). */
public interface MachineAttachment {
    enum Kind {
        /** One more separate output tank. */
        OUTLET,
        /** More capacity in every tank. */
        EXPANSION_TANK,
        /** A comparator signal: progress or fill. */
        GAUGE,
        /** No slow leaks of toxic contents. */
        GASKET,
        /** Faster recipes, with the rotation it is given. */
        CIRCULATION_PUMP,
        /** Lets a chosen fluid (or any) out into whatever is in front of it. */
        OUTLET_VALVE
    }

    Kind kind();

    /** The side the machine is on: attachments face it, except the Outlet Valve, a pump facing away from it. */
    static Direction mountedTowards(BlockState state) {
        return state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) ? state.getValue(BlockStateProperties.HORIZONTAL_FACING)
                : state.getValue(BlockStateProperties.FACING).getOpposite();
    }
}
