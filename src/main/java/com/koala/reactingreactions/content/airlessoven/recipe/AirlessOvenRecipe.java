package com.koala.reactingreactions.content.airlessoven.recipe;

import com.koala.reactingreactions.content.multiblock.MultiblockRecipe;
import com.koala.reactingreactions.registry.CRRRecipeTypes;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;

public class AirlessOvenRecipe extends MultiblockRecipe<ProcessingRecipeParams> {
    public AirlessOvenRecipe(ProcessingRecipeParams params) {
        super(CRRRecipeTypes.AIRLESS_OVEN, params);
    }

    @Override
    protected int getMaxInputCount() {
        return 4;
    }

    @Override
    protected boolean canRequireHeat() {
        return false;
    }
}
