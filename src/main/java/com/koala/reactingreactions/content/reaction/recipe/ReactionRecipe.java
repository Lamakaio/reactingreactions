package com.koala.reactingreactions.content.reaction.recipe;

import com.koala.reactingreactions.content.multiblock.MultiblockRecipe;
import com.koala.reactingreactions.registry.CRRRecipeTypes;

public class ReactionRecipe extends MultiblockRecipe<ReactionRecipeParams> {
    public final float minRpm;
    public final float maxRpm;

    public ReactionRecipe(ReactionRecipeParams params) {
        super(CRRRecipeTypes.REACTION, params);
        this.minRpm = params.minRpm;
        this.maxRpm = params.maxRpm;
    }

    /** Recipes without a speed band, like fermentations, need no stirring. */
    public boolean needsStirring() {
        return maxRpm > 0;
    }

    public boolean acceptsSpeed(float rpm) {
        return !needsStirring() || (rpm >= minRpm && rpm <= maxRpm);
    }
}
