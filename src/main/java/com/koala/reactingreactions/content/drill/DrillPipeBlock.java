package com.koala.reactingreactions.content.drill;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A length of drill pipe: stacked straight down from a Derrick's floor into an oil vein. */
public class DrillPipeBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 16, 12);

    public DrillPipeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
