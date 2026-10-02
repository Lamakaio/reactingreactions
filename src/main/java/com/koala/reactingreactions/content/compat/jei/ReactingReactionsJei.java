package com.koala.reactingreactions.content.compat.jei;

import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.content.compat.DieselGeneratorsCompat;
import com.koala.reactingreactions.content.drill.jei.DrillingCategory;
import com.koala.reactingreactions.content.drill.jei.DrillingRecipes;
import com.koala.reactingreactions.content.induction.jei.InductionCategory;
import com.koala.reactingreactions.content.info.InfoPages;
import com.koala.reactingreactions.content.multiblock.MultiblockRecipe;
import com.koala.reactingreactions.content.multiblock.jei.MultiblockCategory;
import com.koala.reactingreactions.content.multiblock.jei.MultiblockDisplay;
import com.koala.reactingreactions.content.multiblock.jei.MultiblockPreviewRenderer;
import com.koala.reactingreactions.content.multiblock.jei.MultiblockPreviews;
import com.koala.reactingreactions.registry.CRRBlocks;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.tterrag.registrate.util.entry.FluidEntry;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class ReactingReactionsJei implements IModPlugin {
    private static final ResourceLocation ID = ReactingReactions.asResource("jei_plugin");

    private final List<CreateRecipeCategory<?>> multiblocks = new ArrayList<>();

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        multiblocks.clear();
        for (MultiblockDisplay<?> display : MultiblockDisplay.ALL) {
            multiblocks.add(category(display));
        }
        registration.addRecipeCategories(multiblocks.toArray(CreateRecipeCategory[]::new));
        registration.addRecipeCategories(new DrillingCategory(registration.getJeiHelpers().getGuiHelper()),
                new InductionCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    private static <R extends MultiblockRecipe<?>> CreateRecipeCategory<R> category(MultiblockDisplay<R> display) {
        var builder = new CreateRecipeCategory.Builder<>(display.recipeClass())
                .addTypedRecipes(display.type())
                .itemIcon(display.icon())
                .emptyBackground(176, 98 + MultiblockPreviewRenderer.HEIGHT);
        for (var workstation : display.workstations()) {
            builder.catalyst(workstation::get);
        }
        // The 1-arg overload would put it under Create's namespace.
        return builder.build(ReactingReactions.asResource(display.name()), info -> new MultiblockCategory<>(info, display));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        multiblocks.forEach(category -> category.registerRecipes(registration));
        registration.addRecipes(DrillingCategory.TYPE, DrillingRecipes.all());
        registration.addRecipes(InductionCategory.TYPE, List.of(new InductionCategory.Info(MultiblockPreviews.inductionHeater())));
        InfoPages.register(registration);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        multiblocks.forEach(category -> category.registerCatalysts(registration));
        for (ItemLike drillBlock : DrillingRecipes.workstations()) {
            registration.addRecipeCatalyst(new ItemStack(drillBlock), DrillingCategory.TYPE);
        }
        registration.addRecipeCatalyst(new ItemStack(CRRBlocks.INDUCTION_HEATER_CONNECTOR.get()), InductionCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(CRRBlocks.INDUCTION_HEATER_PLATE.get()), InductionCategory.TYPE);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        if (!DieselGeneratorsCompat.isLoaded()) {
            return;
        }
        List<FluidStack> fluids = new ArrayList<>();
        List<ItemStack> buckets = new ArrayList<>();
        for (FluidEntry<?> fluid : DieselGeneratorsCompat.replacedFluids()) {
            fluids.add(new FluidStack(fluid.get(), FluidType.BUCKET_VOLUME));
            fluid.getBucket().ifPresent(bucket -> buckets.add(new ItemStack((Item) bucket)));
        }
        jeiRuntime.getIngredientManager().removeIngredientsAtRuntime(NeoForgeTypes.FLUID_STACK, fluids);
        jeiRuntime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, buckets);
    }
}
