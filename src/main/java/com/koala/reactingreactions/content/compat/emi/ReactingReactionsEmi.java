package com.koala.reactingreactions.content.compat.emi;

import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.content.compat.DieselGeneratorsCompat;
import com.koala.reactingreactions.content.drill.emi.DrillingEmiRecipe;
import com.koala.reactingreactions.content.drill.jei.DrillingRecipes;
import com.koala.reactingreactions.content.induction.emi.InductionEmiRecipe;
import com.koala.reactingreactions.content.info.InfoPages;
import com.koala.reactingreactions.content.multiblock.MultiblockRecipe;
import com.koala.reactingreactions.content.multiblock.jei.MultiblockDisplay;
import com.koala.reactingreactions.content.multiblock.jei.MultiblockPreviews;
import com.koala.reactingreactions.registry.CRRBlocks;
import com.tterrag.registrate.util.entry.FluidEntry;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;

import java.util.List;

/** Found only by EMI's own classpath scan, so nothing here loads without EMI. */
@EmiEntrypoint
public class ReactingReactionsEmi implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        for (MultiblockDisplay<?> display : MultiblockDisplay.ALL) {
            addMultiblock(registry, display);
        }

        EmiRecipeCategory drilling = new EmiRecipeCategory(ReactingReactions.asResource("drilling"), EmiStack.of(CRRBlocks.DERRICK_CONTROLLER.get()));
        registry.addCategory(drilling);
        for (var drillingRecipe : DrillingRecipes.all()) {
            registry.addRecipe(new DrillingEmiRecipe(drilling, drillingRecipe));
        }
        for (ItemLike drillBlock : DrillingRecipes.workstations()) {
            registry.addWorkstation(drilling, EmiStack.of(drillBlock));
        }

        EmiRecipeCategory induction = new EmiRecipeCategory(ReactingReactions.asResource("induction_heater"), EmiStack.of(CRRBlocks.INDUCTION_HEATER_CONNECTOR.get()));
        registry.addCategory(induction);
        registry.addRecipe(new InductionEmiRecipe(induction, MultiblockPreviews.inductionHeater()));
        registry.addWorkstation(induction, EmiStack.of(CRRBlocks.INDUCTION_HEATER_CONNECTOR.get()));
        registry.addWorkstation(induction, EmiStack.of(CRRBlocks.INDUCTION_HEATER_PLATE.get()));

        if (DieselGeneratorsCompat.isLoaded()) {
            for (FluidEntry<?> fluid : DieselGeneratorsCompat.replacedFluids()) {
                registry.removeEmiStacks(EmiStack.of(fluid.get()));
                fluid.getBucket().ifPresent(bucket -> registry.removeEmiStacks(EmiStack.of((Item) bucket)));
            }
        }

        for (InfoPages.Page page : InfoPages.build()) {
            if (page.items().isEmpty()) {
                continue;
            }
            List<EmiIngredient> stacks = page.items().stream().<EmiIngredient>map(EmiStack::of).toList();
            registry.addRecipe(new EmiInfoRecipe(stacks, page.lines(),
                    // A leading "/" tells EMI there is no real recipe behind this id.
                    ReactingReactions.asResource("/info/" + BuiltInRegistries.ITEM.getKey(page.items().get(0).getItem()).getPath())));
        }
    }

    private static <R extends MultiblockRecipe<?>> void addMultiblock(EmiRegistry registry, MultiblockDisplay<R> display) {
        EmiRecipeCategory category = new EmiRecipeCategory(ReactingReactions.asResource(display.name()), EmiStack.of(display.icon()));
        registry.addCategory(category);
        for (RecipeHolder<R> holder : registry.getRecipeManager().getAllRecipesFor(display.type().get())) {
            registry.addRecipe(new MultiblockEmiRecipe<>(category, display, holder.value(), holder.id()));
        }
        for (var workstation : display.workstations()) {
            registry.addWorkstation(category, EmiStack.of(workstation.get()));
        }
    }
}
