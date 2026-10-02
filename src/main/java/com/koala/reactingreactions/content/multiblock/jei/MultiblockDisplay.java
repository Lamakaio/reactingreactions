package com.koala.reactingreactions.content.multiblock.jei;

import com.koala.reactingreactions.content.airlessoven.recipe.AirlessOvenRecipe;
import com.koala.reactingreactions.content.distillation.recipe.DistillationRecipe;
import com.koala.reactingreactions.content.electrolysis.recipe.ElectrolysisRecipe;
import com.koala.reactingreactions.content.multiblock.MultiblockRecipe;
import com.koala.reactingreactions.content.reaction.recipe.ReactionRecipe;
import com.koala.reactingreactions.registry.CRRBlocks;
import com.koala.reactingreactions.registry.CRRRecipeTypes;
import com.simibubi.create.content.processing.recipe.HeatCondition;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/** How a multiblock's recipes show in JEI and EMI: the machine preview above the grid, and a note line below it. */
public record MultiblockDisplay<R extends MultiblockRecipe<?>>(String name, Class<R> recipeClass, CRRRecipeTypes.Entry<R> type,
        List<Supplier<? extends ItemLike>> workstations, Function<R, MultiblockPreview> preview, Function<R, @Nullable Component> note) {

    public static final List<MultiblockDisplay<?>> ALL = List.of(
            new MultiblockDisplay<>("electrolysis", ElectrolysisRecipe.class, CRRRecipeTypes.ELECTROLYSIS,
                    List.of(CRRBlocks.ELECTROLYSIS_VAT_CONTROLLER, CRRBlocks.SMALL_ELECTROLYSER), r -> MultiblockPreviews.vat(r.electrodes),
                    r -> r.minVoltage > 0 ? Component.translatable("reactingreactions.jei.electrolysis.min_voltage", r.minVoltage) : null),
            new MultiblockDisplay<>("distillation", DistillationRecipe.class, CRRRecipeTypes.DISTILLATION,
                    List.of(CRRBlocks.DISTILLATION_TOWER_CONTROLLER), r -> MultiblockPreviews.tower(), r -> null),
            new MultiblockDisplay<>("airless_oven", AirlessOvenRecipe.class, CRRRecipeTypes.AIRLESS_OVEN,
                    List.of(CRRBlocks.AIRLESS_OVEN_CONTROLLER), r -> MultiblockPreviews.oven(), r -> null),
            new MultiblockDisplay<>("reaction", ReactionRecipe.class, CRRRecipeTypes.REACTION,
                    List.of(CRRBlocks.REACTION_CHAMBER_CONTROLLER, CRRBlocks.FERMENTATION_BARREL), r -> MultiblockPreviews.chamber(),
                    r -> r.needsStirring() ? Component.translatable("reactingreactions.jei.reaction.rpm", (int) r.minRpm, (int) r.maxRpm) : null));

    public ItemLike icon() {
        return workstations.get(0).get();
    }

    /** The note sits on the heat line when there is no heat, and above it otherwise. */
    public static int noteRow(MultiblockRecipe<?> recipe) {
        return recipe.getRequiredHeat() == HeatCondition.NONE ? 0 : -1;
    }
}
