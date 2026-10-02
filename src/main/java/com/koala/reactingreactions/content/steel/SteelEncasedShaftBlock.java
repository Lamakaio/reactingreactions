package com.koala.reactingreactions.content.steel;

import com.koala.reactingreactions.registry.CRRBlockEntities;
import com.koala.reactingreactions.registry.CRRBlocks;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedShaftBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A Create shaft in a steel casing, exactly like Create's own andesite and brass encased shafts. A subclass only so it
 * points at this mod's own block entity type (Create's encased-shaft type only accepts Create's own blocks).
 */
public class SteelEncasedShaftBlock extends EncasedShaftBlock {
    public SteelEncasedShaftBlock(Properties properties) {
        super(properties, CRRBlocks.STEEL_CASING::get);
    }

    @Override
    public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
        return CRRBlockEntities.STEEL_ENCASED_SHAFT.get();
    }

    /** Sits in the reaction chamber's roof: like the rest of the shell it lets light through instead of darkening the interior. */
    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }
}
