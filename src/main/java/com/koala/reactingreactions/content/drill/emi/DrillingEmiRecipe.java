package com.koala.reactingreactions.content.drill.emi;

import com.koala.reactingreactions.content.drill.jei.DrillingRecipe;
import com.koala.reactingreactions.content.multiblock.jei.MultiblockPreviewRenderer;
import com.koala.reactingreactions.content.toxic.FluidChemistryTooltip;

import dev.emi.emi.api.neoforge.NeoForgeEmiStack;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class DrillingEmiRecipe implements EmiRecipe {
    private static final int OY = MultiblockPreviewRenderer.FULL_HEIGHT;

    private final EmiRecipeCategory category;
    private final DrillingRecipe recipe;
    private final ResourceLocation id;

    public DrillingEmiRecipe(EmiRecipeCategory category, DrillingRecipe recipe) {
        this.category = category;
        this.recipe = recipe;
        // A leading "/" tells EMI there is no real recipe behind this id.
        this.id = ResourceLocation.fromNamespaceAndPath("reactingreactions", "/drilling/"
                + BuiltInRegistries.ITEM.getKey(recipe.head().getItem()).getPath() + "/"
                + BuiltInRegistries.ITEM.getKey(recipe.vein().getItem()).getPath());
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(EmiStack.of(recipe.vein()), EmiStack.of(recipe.head()), NeoForgeEmiStack.of(recipe.consumable()));
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(recipe.outputItem().isEmpty() ? NeoForgeEmiStack.of(recipe.outputFluid()) : EmiStack.of(recipe.outputItem()));
    }

    @Override
    public int getDisplayWidth() {
        return 176;
    }

    @Override
    public int getDisplayHeight() {
        return OY + 34;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int y = 2;
        for (ItemStack stack : recipe.preview().sideItems()) {
            widgets.addSlot(EmiStack.of(stack), 154, y).catalyst(true);
            y += 18;
        }
        widgets.addSlot(EmiStack.of(recipe.vein()), 11, OY + 7);
        widgets.addSlot(EmiStack.of(recipe.head()), 35, OY + 7).catalyst(true);
        var consumableSlot = widgets.addSlot(NeoForgeEmiStack.of(recipe.consumable()), 59, OY + 7);
        for (Component line : FluidChemistryTooltip.linesFor(recipe.consumable())) {
            consumableSlot.appendTooltip(line);
        }
        if (!recipe.outputItem().isEmpty()) {
            widgets.addSlot(EmiStack.of(recipe.outputItem()), 127, OY + 7);
        } else {
            var outputSlot = widgets.addSlot(NeoForgeEmiStack.of(recipe.outputFluid()), 127, OY + 7);
            for (Component line : FluidChemistryTooltip.linesFor(recipe.outputFluid())) {
                outputSlot.appendTooltip(line);
            }
        }
        widgets.addDrawable(0, 0, getDisplayWidth(), getDisplayHeight(), (graphics, mouseX, mouseY, delta) -> {
            MultiblockPreviewRenderer.draw(recipe.preview(), graphics);
            graphics.drawString(Minecraft.getInstance().font, "->", 99, OY + 11, 0x404040, false);
        });
    }
}
