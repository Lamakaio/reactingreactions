package com.koala.reactingreactions.content.reaction.recipe;

import com.koala.reactingreactions.content.multiblock.MultiblockRecipe;
import com.koala.reactingreactions.registry.CRRRecipeTypes;
import com.simibubi.create.content.processing.recipe.HeatCondition;

import java.util.Locale;

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

    /** The goggles' reason when the stirring is off: the band it needs and the speed it gets. */
    public String stirringShortfall(float rpm) {
        return String.format("Stirring must be %.0f-%.0f RPM (now %.0f)", minRpm, maxRpm, rpm);
    }

    /** The goggles' reason when the heat is too low. */
    public static String heatShortfall(HeatCondition required) {
        return "Needs " + required.name().toLowerCase(Locale.ROOT) + " Blaze Burners below";
    }
}
