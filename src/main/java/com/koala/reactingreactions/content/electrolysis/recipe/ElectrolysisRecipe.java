package com.koala.reactingreactions.content.electrolysis.recipe;

import com.koala.reactingreactions.content.multiblock.MultiblockRecipe;
import com.koala.reactingreactions.registry.CRRRecipeTypes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class ElectrolysisRecipe extends MultiblockRecipe<ElectrolysisRecipeParams> {
    public final double minVoltage;
    public final List<ResourceLocation> electrodes;

    public ElectrolysisRecipe(ElectrolysisRecipeParams params) {
        super(CRRRecipeTypes.ELECTROLYSIS, params);
        this.minVoltage = params.minVoltage;
        this.electrodes = List.copyOf(params.electrodes);
    }

    /** An empty electrode list allows any electrode. */
    public boolean allowsElectrode(Block block) {
        return electrodes.isEmpty() || electrodes.contains(BuiltInRegistries.BLOCK.getKey(block));
    }

    @Override
    protected boolean canRequireHeat() {
        return false;
    }
}
