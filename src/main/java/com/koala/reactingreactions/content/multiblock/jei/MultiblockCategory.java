package com.koala.reactingreactions.content.multiblock.jei;

import com.koala.reactingreactions.content.multiblock.MultiblockRecipe;
import com.koala.reactingreactions.content.toxic.ToxicityJei;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.CreateLang;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;

import net.createmod.catnip.data.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;
import java.util.List;

/** Create's Basin layout: inputs on the left, outputs on the right, with the machine preview above. */
public class MultiblockCategory<R extends MultiblockRecipe<?>> extends CreateRecipeCategory<R> {
    private static final int OY = MultiblockPreviewRenderer.HEIGHT;

    private final MultiblockDisplay<R> display;

    public MultiblockCategory(Info<R> info, MultiblockDisplay<R> display) {
        super(info);
        this.display = display;
    }

    /** The preview's side items, in a column on the right. */
    public static void addSideSlots(IRecipeLayoutBuilder builder, MultiblockPreview preview) {
        int y = 2;
        for (ItemStack stack : preview.sideItems()) {
            builder.addSlot(RecipeIngredientRole.CATALYST, 154, y).addItemStack(stack);
            y += 18;
        }
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, R recipe, IFocusGroup focuses) {
        addSideSlots(builder, display.preview().apply(recipe));
        List<Pair<Ingredient, MutableInt>> condensedIngredients = ItemHelper.condenseIngredients(recipe.getIngredients());
        int inputCount = condensedIngredients.size() + recipe.getFluidIngredients().size();
        int inputXOffset = inputCount < 3 ? (3 - inputCount) * 19 / 2 : 0;
        int i = 0;
        for (Pair<Ingredient, MutableInt> pair : condensedIngredients) {
            List<ItemStack> stacks = new ArrayList<>();
            for (ItemStack stack : pair.getFirst().getItems()) {
                stacks.add(stack.copyWithCount(pair.getSecond().getValue()));
            }
            builder.addSlot(RecipeIngredientRole.INPUT, 17 + inputXOffset + i % 3 * 19, 51 + OY - i / 3 * 19)
                    .setBackground(getRenderedSlot(), -1, -1)
                    .addItemStacks(stacks);
            i++;
        }
        for (SizedFluidIngredient fluidIngredient : recipe.getFluidIngredients()) {
            addFluidSlot(builder, 17 + inputXOffset + i % 3 * 19, 51 + OY - i / 3 * 19, fluidIngredient).addRichTooltipCallback(ToxicityJei.TOOLTIP);
            i++;
        }

        int outputCount = recipe.getRollableResults().size() + recipe.getFluidResults().size();
        i = 0;
        for (ProcessingOutput output : recipe.getRollableResults()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, outputX(i, outputCount), outputY(i))
                    .setBackground(getRenderedSlot(output), -1, -1)
                    .addItemStack(output.getStack())
                    .addRichTooltipCallback(addStochasticTooltip(output));
            i++;
        }
        for (FluidStack fluidResult : recipe.getFluidResults()) {
            addFluidSlot(builder, outputX(i, outputCount), outputY(i), fluidResult).addRichTooltipCallback(ToxicityJei.TOOLTIP);
            i++;
        }
    }

    private static int outputX(int i, int count) {
        return 142 - (count % 2 != 0 && i == count - 1 ? 0 : (i % 2 == 0 ? 10 : -9));
    }

    private static int outputY(int i) {
        return -19 * (i / 2) + 51 + OY;
    }

    @Override
    public void draw(R recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        MultiblockPreviewRenderer.draw(display.preview().apply(recipe), graphics);
        int vRows = (1 + recipe.getFluidResults().size() + recipe.getRollableResults().size()) / 2;
        if (vRows <= 2) {
            AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 136, -19 * (vRows - 1) + 32 + OY);
        }
        HeatCondition heat = recipe.getRequiredHeat();
        Component note = display.note().apply(recipe);
        boolean lit = heat != HeatCondition.NONE || note != null;
        (lit ? AllGuiTextures.JEI_LIGHT : AllGuiTextures.JEI_SHADOW).render(graphics, 81, 58 + OY + (lit ? 30 : 10));
        if (heat != HeatCondition.NONE) {
            AllGuiTextures.JEI_HEAT_BAR.render(graphics, 4, 80 + OY);
            graphics.drawString(Minecraft.getInstance().font, CreateLang.translateDirect(heat.getTranslationKey()), 9, 86 + OY, heat.getColor(), false);
        }
        if (note != null) {
            graphics.drawString(Minecraft.getInstance().font, note, 9, 86 + OY + 14 * MultiblockDisplay.noteRow(recipe), 0x404040, false);
        }
    }
}
