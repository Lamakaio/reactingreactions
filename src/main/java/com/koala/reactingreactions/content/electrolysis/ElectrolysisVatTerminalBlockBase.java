package com.koala.reactingreactions.content.electrolysis;

import com.koala.reactingreactions.content.multiblock.MachineTiers;
import com.koala.reactingreactions.registry.CRRBlockEntities;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/**
 * The vat's terminal, in an end wall beside an electrode column. Everything but the Electro Energetics wiring, so it loads
 * without that mod; registered as is when it is absent. Formed, it shows its piece of the vat, the junction box included.
 */
public class ElectrolysisVatTerminalBlockBase extends Block implements IBE<ElectrodeInfoBlockEntity> {
    public ElectrolysisVatTerminalBlockBase(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(MachineTiers.PART, 0).setValue(MachineTiers.FACING, Direction.NORTH)
                .setValue(MachineTiers.PIPED, false).setValue(MachineTiers.SEALED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MachineTiers.PART, MachineTiers.FACING, MachineTiers.PIPED, MachineTiers.SEALED);
    }

    @Override
    public Class<ElectrodeInfoBlockEntity> getBlockEntityClass() {
        return ElectrodeInfoBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ElectrodeInfoBlockEntity> getBlockEntityType() {
        return CRRBlockEntities.ELECTRODE_INFO.get();
    }

    /** Which way the junction box faces, out of the vat's end wall; null when not part of a formed vat. */
    public static Direction outward(BlockState state) {
        int part = state.getValue(MachineTiers.PART) - 1;
        if (part < 0) {
            return null;
        }
        int width = MachineTiers.ELECTROLYSIS_VAT.get(0).width();
        int first = width * MachineTiers.ELECTROLYSIS_VAT.get(0).depth() * MachineTiers.ELECTROLYSIS_VAT.get(0).height();
        if (part >= first) {
            part -= first;
            width = MachineTiers.ELECTROLYSIS_VAT.get(1).width();
        }
        // Modelled with the controller to the north, the west end wall is x 0.
        Direction facing = state.getValue(MachineTiers.FACING);
        return part % width == 0 ? facing.getCounterClockWise() : facing.getClockWise();
    }
}
