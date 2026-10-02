package com.koala.reactingreactions.content.drill;

import com.koala.reactingreactions.content.multiblock.MachineTiers;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/** Bracing of the Derrick. Purely structural: it takes no part in fluid I/O. */
public class DerrickTrussBlock extends Block {
    public DerrickTrussBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(MachineTiers.PART, 0));
    }

    /** Its piece of the formed tower (see {@link DrillRig#setLook}); 0 when not part of one. */
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MachineTiers.PART);
    }
}
