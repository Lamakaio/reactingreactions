package com.koala.reactingreactions.content.distillation.recipe;

import com.koala.reactingreactions.content.multiblock.MultiblockRecipe;
import com.koala.reactingreactions.registry.CRRRecipeTypes;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;

public class DistillationRecipe extends MultiblockRecipe<ProcessingRecipeParams> {
    public DistillationRecipe(ProcessingRecipeParams params) {
        super(CRRRecipeTypes.DISTILLATION, params);
    }

    @Override
    protected int getMaxInputCount() {
        return 4;
    }
}
