package com.koala.reactingreactions.content.drill.jei;

import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.content.multiblock.jei.MultiblockCategory;
import com.koala.reactingreactions.content.multiblock.jei.MultiblockPreviewRenderer;
import com.koala.reactingreactions.content.toxic.ToxicityJei;
import com.koala.reactingreactions.registry.CRRBlocks;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** The Derrick / Mineral Drill page: the structure on top, then head + vein + coolant on the left and what comes out on the right. */
public class DrillingCategory implements IRecipeCategory<DrillingRecipe> {
    public static final RecipeType<DrillingRecipe> TYPE = RecipeType.create(ReactingReactions.MODID, "drilling", DrillingRecipe.class);
    private static final int OY = MultiblockPreviewRenderer.FULL_HEIGHT;

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;

    public DrillingCategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(176, OY + 34);
        this.icon = helper.createDrawableItemStack(new ItemStack(CRRBlocks.DERRICK_CONTROLLER.get()));
        this.slot = helper.getSlotDrawable();
    }

    @Override
    public RecipeType<DrillingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("reactingreactions.jei.drilling");
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
    public ResourceLocation getRegistryName(DrillingRecipe recipe) {
        return ReactingReactions.asResource("drilling/"
                + BuiltInRegistries.ITEM.getKey(recipe.head().getItem()).getPath() + "/"
                + BuiltInRegistries.ITEM.getKey(recipe.vein().getItem()).getPath());
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DrillingRecipe recipe, IFocusGroup focuses) {
        MultiblockCategory.addSideSlots(builder, recipe.preview());
        builder.addSlot(RecipeIngredientRole.INPUT, 12, OY + 8).setBackground(slot, -1, -1).addItemStack(recipe.vein());
        builder.addSlot(RecipeIngredientRole.CATALYST, 36, OY + 8).setBackground(slot, -1, -1).addItemStack(recipe.head());
        builder.addSlot(RecipeIngredientRole.INPUT, 60, OY + 8).setBackground(slot, -1, -1)
                .addFluidStack(recipe.consumable().getFluid(), recipe.consumable().getAmount())
                .addRichTooltipCallback(ToxicityJei.TOOLTIP);
        if (!recipe.outputItem().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 128, OY + 8).setBackground(slot, -1, -1).addItemStack(recipe.outputItem());
        } else {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 128, OY + 8).setBackground(slot, -1, -1)
                    .addFluidStack(recipe.outputFluid().getFluid(), recipe.outputFluid().getAmount())
                    .addRichTooltipCallback(ToxicityJei.TOOLTIP);
        }
    }

    @Override
    public void draw(DrillingRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        MultiblockPreviewRenderer.draw(recipe.preview(), graphics);
        // The arrow between what goes in and what comes out.
        graphics.drawString(Minecraft.getInstance().font, "->", 100, OY + 12, 0x404040, false);
    }
}
