package com.koala.reactingreactions.content.toxic;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;

/** Adds the toxicity line to the tooltip of a toxic item shown in a recipe slot. */
public final class ToxicityJei {
    public static final IRecipeSlotRichTooltipCallback TOOLTIP = ToxicityJei::addTooltip;

    private ToxicityJei() {
    }

    private static void addTooltip(IRecipeSlotView slot, ITooltipBuilder tooltip) {
        // Fluids get their formula and toxicity from JeiFluidTooltipMixin, for every JEI fluid tooltip; only toxic items are added here.
        slot.getDisplayedIngredient(VanillaTypes.ITEM_STACK).ifPresent(stack -> {
            float toxicity = Toxicity.carriedToxicity(stack);
            if (toxicity > 0) {
                tooltip.add(ToxicityTooltips.line(toxicity, ToxicFluid.HARMLESS));
            }
        });
    }
}
