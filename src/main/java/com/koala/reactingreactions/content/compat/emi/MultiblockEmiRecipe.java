package com.koala.reactingreactions.content.compat.emi;

import com.koala.reactingreactions.content.multiblock.MultiblockRecipe;
import com.koala.reactingreactions.content.multiblock.jei.MultiblockCategory;
import com.koala.reactingreactions.content.multiblock.jei.MultiblockDisplay;
import com.koala.reactingreactions.content.multiblock.jei.MultiblockPreviewRenderer;
import com.koala.reactingreactions.content.toxic.FluidChemistryTooltip;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.CreateLang;

import dev.emi.emi.api.neoforge.NeoForgeEmiIngredient;
import dev.emi.emi.api.neoforge.NeoForgeEmiStack;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

import net.createmod.catnip.data.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;
import java.util.List;

/** The EMI counterpart of {@link MultiblockCategory}, laid out the same way. */
public class MultiblockEmiRecipe<R extends MultiblockRecipe<?>> implements EmiRecipe {
    private static final int OY = MultiblockPreviewRenderer.HEIGHT;

    private final R recipe;
    private final MultiblockDisplay<R> display;
    private final EmiRecipeCategory category;
    private final ResourceLocation id;
    private final List<EmiIngredient> itemInputs = new ArrayList<>();
    private final List<SizedFluidIngredient> fluidInputs = new ArrayList<>();
    private final List<ProcessingOutput> itemOutputs;
    private final List<FluidStack> fluidOutputs;

    public MultiblockEmiRecipe(EmiRecipeCategory category, MultiblockDisplay<R> display, R recipe, ResourceLocation id) {
        this.category = category;
        this.display = display;
        this.recipe = recipe;
        this.id = id;
        for (Pair<Ingredient, MutableInt> pair : ItemHelper.condenseIngredients(recipe.getIngredients())) {
            itemInputs.add(EmiIngredient.of(pair.getFirst(), pair.getSecond().getValue()));
        }
        fluidInputs.addAll(recipe.getFluidIngredients());
        itemOutputs = recipe.getRollableResults();
        fluidOutputs = recipe.getFluidResults();
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
        List<EmiIngredient> all = new ArrayList<>(itemInputs);
        for (SizedFluidIngredient fluid : fluidInputs) {
            all.add(NeoForgeEmiIngredient.of(fluid));
        }
        return all;
    }

    @Override
    public List<EmiStack> getOutputs() {
        List<EmiStack> all = new ArrayList<>();
        for (ProcessingOutput output : itemOutputs) {
            all.add(EmiStack.of(output.getStack()).setChance(output.getChance()));
        }
        for (FluidStack fluid : fluidOutputs) {
            all.add(NeoForgeEmiStack.of(fluid));
        }
        return all;
    }

    @Override
    public int getDisplayWidth() {
        return 176;
    }

    @Override
    public int getDisplayHeight() {
        return 98 + OY;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int y = 2;
        for (ItemStack stack : display.preview().apply(recipe).sideItems()) {
            widgets.addSlot(EmiStack.of(stack), 154, y).catalyst(true);
            y += 18;
        }

        int inputCount = itemInputs.size() + fluidInputs.size();
        int inputXOffset = inputCount < 3 ? (3 - inputCount) * 19 / 2 : 0;
        int i = 0;
        for (EmiIngredient ingredient : itemInputs) {
            widgets.addSlot(ingredient, 16 + inputXOffset + i % 3 * 19, 50 + OY - i / 3 * 19);
            i++;
        }
        for (SizedFluidIngredient fluid : fluidInputs) {
            widgets.addSlot(NeoForgeEmiIngredient.of(fluid), 16 + inputXOffset + i % 3 * 19, 50 + OY - i / 3 * 19);
            i++;
        }

        int outputCount = itemOutputs.size() + fluidOutputs.size();
        i = 0;
        for (ProcessingOutput output : itemOutputs) {
            int x = 141 - (outputCount % 2 != 0 && i == outputCount - 1 ? 0 : (i % 2 == 0 ? 10 : -9));
            int y2 = -19 * (i / 2) + 50 + OY;
            widgets.addSlot(EmiStack.of(output.getStack()).setChance(output.getChance()), x, y2);
            i++;
        }
        for (FluidStack fluid : fluidOutputs) {
            int x = 141 - (outputCount % 2 != 0 && i == outputCount - 1 ? 0 : (i % 2 == 0 ? 10 : -9));
            int y2 = -19 * (i / 2) + 50 + OY;
            var slot = widgets.addSlot(NeoForgeEmiStack.of(fluid), x, y2);
            for (Component line : FluidChemistryTooltip.linesFor(fluid)) {
                slot.appendTooltip(line);
            }
            i++;
        }

        widgets.addDrawable(0, 0, getDisplayWidth(), getDisplayHeight(), (graphics, mouseX, mouseY, delta) -> {
            MultiblockPreviewRenderer.draw(display.preview().apply(recipe), graphics);
            int vRows = (1 + fluidOutputs.size() + itemOutputs.size()) / 2;
            if (vRows <= 2) {
                AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 135, -19 * (vRows - 1) + 31 + OY);
            }
            HeatCondition heat = recipe.getRequiredHeat();
            Component note = display.note().apply(recipe);
            boolean lit = heat != HeatCondition.NONE || note != null;
            (lit ? AllGuiTextures.JEI_LIGHT : AllGuiTextures.JEI_SHADOW).render(graphics, 80, 57 + OY + (lit ? 30 : 10));
            if (heat != HeatCondition.NONE) {
                AllGuiTextures.JEI_HEAT_BAR.render(graphics, 3, 79 + OY);
                graphics.drawString(Minecraft.getInstance().font, CreateLang.translateDirect(heat.getTranslationKey()), 8, 85 + OY, heat.getColor(), false);
            }
            if (note != null) {
                graphics.drawString(Minecraft.getInstance().font, note, 8, 85 + OY + 14 * MultiblockDisplay.noteRow(recipe), 0x404040, false);
            }
        });
    }
}
