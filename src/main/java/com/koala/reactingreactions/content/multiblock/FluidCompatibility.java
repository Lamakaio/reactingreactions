package com.koala.reactingreactions.content.multiblock;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Which fluids may share a machine's input tanks: a new fluid comes in only if some recipe takes it together with every fluid
 * already there. Indexed once per recipe manager (a reload makes a new one), and each answer is remembered, so a pack with
 * many recipes stays cheap.
 */
public final class FluidCompatibility {
    private static final Map<RecipeManager, Map<RecipeType<?>, Index>> INDEXES = new WeakHashMap<>();
    /** Set by the Ponder exporter: its scenes show setups other than the one it runs in (with or without Diesel Generators). */
    public static volatile boolean bypass;

    private FluidCompatibility() {
    }

    public static boolean accepts(RecipeManager manager, RecipeType<?> type, FluidStack incoming, List<FluidStack> present) {
        if (bypass) {
            return true;
        }
        Index index;
        synchronized (INDEXES) {
            index = INDEXES.computeIfAbsent(manager, m -> new HashMap<>()).computeIfAbsent(type, t -> new Index(manager, t));
        }
        return index.accepts(incoming, present);
    }

    private static Fluid source(Fluid fluid) {
        return fluid instanceof FlowingFluid flowing ? flowing.getSource() : fluid;
    }

    private record Query(Fluid incoming, List<Fluid> present) {
    }

    private static final class Index {
        /** For each fluid, the fluid ingredients of every recipe taking it. */
        private final Map<Fluid, List<List<SizedFluidIngredient>>> byFluid = new HashMap<>();
        private final Map<Query, Boolean> answers = new ConcurrentHashMap<>();

        @SuppressWarnings({"unchecked", "rawtypes"})
        Index(RecipeManager manager, RecipeType<?> type) {
            for (Object entry : manager.getAllRecipesFor((RecipeType) type)) {
                Recipe<?> recipe = ((RecipeHolder<?>) entry).value();
                if (!(recipe instanceof ProcessingRecipe<?, ?> processing) || processing.getFluidIngredients().isEmpty()) {
                    continue;
                }
                List<SizedFluidIngredient> ingredients = processing.getFluidIngredients();
                for (SizedFluidIngredient ingredient : ingredients) {
                    for (FluidStack option : ingredient.getFluids()) {
                        List<List<SizedFluidIngredient>> recipes = byFluid.computeIfAbsent(source(option.getFluid()), f -> new ArrayList<>());
                        // A tag can list several forms of one fluid.
                        if (recipes.isEmpty() || recipes.getLast() != ingredients) {
                            recipes.add(ingredients);
                        }
                    }
                }
            }
        }

        boolean accepts(FluidStack incoming, List<FluidStack> present) {
            List<Fluid> presentFluids = present.stream().map(stack -> source(stack.getFluid())).toList();
            return answers.computeIfAbsent(new Query(source(incoming.getFluid()), presentFluids), query -> compute(incoming, present));
        }

        private boolean compute(FluidStack incoming, List<FluidStack> present) {
            List<FluidStack> all = new ArrayList<>(present);
            all.add(incoming);
            for (List<SizedFluidIngredient> ingredients : byFluid.getOrDefault(source(incoming.getFluid()), List.of())) {
                if (all.size() <= ingredients.size() && assign(all, 0, ingredients, new boolean[ingredients.size()])) {
                    return true;
                }
            }
            return false;
        }

        /** Whether each fluid from {@code next} on can take its own ingredient (amounts aside). */
        private static boolean assign(List<FluidStack> fluids, int next, List<SizedFluidIngredient> ingredients, boolean[] used) {
            if (next == fluids.size()) {
                return true;
            }
            for (int i = 0; i < ingredients.size(); i++) {
                if (!used[i] && ingredients.get(i).ingredient().test(fluids.get(next))) {
                    used[i] = true;
                    if (assign(fluids, next + 1, ingredients, used)) {
                        return true;
                    }
                    used[i] = false;
                }
            }
            return false;
        }
    }
}
