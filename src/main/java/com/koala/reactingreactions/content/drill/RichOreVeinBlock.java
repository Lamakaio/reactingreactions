package com.koala.reactingreactions.content.drill;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * The only block a drill recognises as a vein, so a vein cannot be faked by placing rock. It drops ordinary rock, never
 * itself, and is slow to mine. Its {@link #RICHNESS} (1-5), rolled at generation, sets the drilling rate.
 */
public class RichOreVeinBlock extends Block {
    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 5;
    public static final IntegerProperty RICHNESS = IntegerProperty.create("richness", MIN_LEVEL, MAX_LEVEL);
    /** Richness points per level, on the same scale as the controller's full-rate threshold (30). */
    private static final int POINTS_PER_LEVEL = 8;

    public RichOreVeinBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(RICHNESS, 3));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RICHNESS);
    }

    /** The deposit's richness on the controller's scale (8 to 40; 30 and above is a full-rate deposit). */
    public static int richnessOf(BlockState state) {
        return state.getValue(RICHNESS) * POINTS_PER_LEVEL;
    }
}
