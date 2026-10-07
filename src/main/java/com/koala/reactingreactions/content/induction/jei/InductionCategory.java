package com.koala.reactingreactions.content.induction.jei;

import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.content.multiblock.jei.MultiblockCategory;
import com.koala.reactingreactions.content.multiblock.jei.MultiblockPreview;
import com.koala.reactingreactions.content.multiblock.jei.MultiblockPreviewRenderer;
import com.koala.reactingreactions.registry.CRRBlocks;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** The Induction Heater page: its structure, and the rules for building and powering it. */
public class InductionCategory implements IRecipeCategory<InductionCategory.Info> {
    /** The single row of this page: only the structure to draw. */
    public record Info(MultiblockPreview preview) {
    }

    public static final RecipeType<Info> TYPE = RecipeType.create(ReactingReactions.MODID, "induction_heater", Info.class);
    private static final int OY = MultiblockPreviewRenderer.FULL_HEIGHT;

    private final IDrawable background;
    private final IDrawable icon;

    public InductionCategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(176, OY);
        this.icon = helper.createDrawableItemStack(new ItemStack(CRRBlocks.INDUCTION_HEATER_CONNECTOR.get()));
    }

    @Override
    public RecipeType<Info> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("reactingreactions.jei.induction");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public ResourceLocation getRegistryName(Info recipe) {
        return ReactingReactions.asResource("induction_heater");
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Info recipe, IFocusGroup focuses) {
        MultiblockCategory.addSideSlots(builder, recipe.preview());
    }

    @Override
    public void draw(Info recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        MultiblockPreviewRenderer.draw(recipe.preview(), graphics);
    }
}
