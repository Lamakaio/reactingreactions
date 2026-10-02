package com.koala.reactingreactions.content.induction.emi;

import com.koala.reactingreactions.content.multiblock.jei.MultiblockPreview;
import com.koala.reactingreactions.content.multiblock.jei.MultiblockPreviewRenderer;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** EMI display for the Induction Heater page: its structure, and the rules for building and powering it (its own information page). */
public class InductionEmiRecipe implements EmiRecipe {
    private static final int OY = MultiblockPreviewRenderer.FULL_HEIGHT;

    private final EmiRecipeCategory category;
    private final MultiblockPreview preview;

    public InductionEmiRecipe(EmiRecipeCategory category, MultiblockPreview preview) {
        this.category = category;
        this.preview = preview;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public ResourceLocation getId() {
        // A leading "/" tells EMI there is no real recipe behind this id.
        return ResourceLocation.fromNamespaceAndPath("reactingreactions", "/induction_heater");
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of();
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of();
    }

    @Override
    public int getDisplayWidth() {
        return 176;
    }

    @Override
    public int getDisplayHeight() {
        return OY;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int y = 2;
        for (ItemStack stack : preview.sideItems()) {
            widgets.addSlot(EmiStack.of(stack), 154, y).catalyst(true);
            y += 18;
        }
        widgets.addDrawable(0, 0, getDisplayWidth(), getDisplayHeight(), (graphics, mouseX, mouseY, delta) -> MultiblockPreviewRenderer.draw(preview, graphics));
    }
}
