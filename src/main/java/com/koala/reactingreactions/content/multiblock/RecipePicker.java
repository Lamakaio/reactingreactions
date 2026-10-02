package com.koala.reactingreactions.content.multiblock;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;

import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/**
 * Picks among the recipes matching a machine's inputs: first those that can run right now, then the most specific (most
 * ingredients, plus a machine's own bonus). Recipe-manager order is arbitrary, so the first match could never run.
 */
public final class RecipePicker {
    private RecipePicker() {
    }

    @Nullable
    public static <R extends ProcessingRecipe<RecipeInput, ?>> R pick(Level level, RecipeType<R> type, Predicate<R> matchesInputs,
                                                            Predicate<R> runnableNow, ToIntFunction<R> bonus) {
        R best = null;
        int bestScore = Integer.MIN_VALUE;
        for (RecipeHolder<R> holder : level.getRecipeManager().getAllRecipesFor(type)) {
            R candidate = holder.value();
            if (!matchesInputs.test(candidate)) {
                continue;
            }
            int specificity = candidate.getIngredients().size() + candidate.getFluidIngredients().size() + bonus.applyAsInt(candidate);
            int score = (runnableNow.test(candidate) ? 1000 : 0) + specificity;
            if (score > bestScore) {
                best = candidate;
                bestScore = score;
            }
        }
        return best;
    }

    @Nullable
    public static <R extends ProcessingRecipe<RecipeInput, ?>> R pick(Level level, RecipeType<R> type, Predicate<R> matchesInputs, Predicate<R> runnableNow) {
        return pick(level, type, matchesInputs, runnableNow, r -> 0);
    }

    /**
     * For a machine holding a recipe it cannot currently run: once a second, look for one it can run with the same inputs
     * and switch to it. Returns the recipe to keep (possibly the same one).
     */
    public static <R extends ProcessingRecipe<RecipeInput, ?>> R reconsider(Level level, R current, RecipeType<R> type, Predicate<R> matchesInputs,
                                                                  Predicate<R> runnableNow, ToIntFunction<R> bonus) {
        if (level.getGameTime() % 20 != 0) {
            return current;
        }
        R better = pick(level, type, matchesInputs, runnableNow, bonus);
        return better != null && runnableNow.test(better) ? better : current;
    }
}
