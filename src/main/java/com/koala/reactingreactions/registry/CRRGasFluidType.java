package com.koala.reactingreactions.registry;

import com.simibubi.create.AllFluids;
import com.tterrag.registrate.builders.FluidBuilder.FluidTypeFactory;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

public class CRRGasFluidType extends AllFluids.TintedFluidType {
    private final int color;
    private final boolean gas;

    public CRRGasFluidType(FluidType.Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture, int color, boolean gas) {
        super(properties, stillTexture, flowingTexture);
        this.color = color;
        this.gas = gas;
    }

    public static FluidTypeFactory create(int color, boolean gas) {
        return (properties, still, flowing) -> new CRRGasFluidType(properties, still, flowing, color, gas);
    }

    /** The fluid's colour (ARGB) */
    public int color() {
        return color;
    }

    @Override
    protected int getTintColor(FluidStack stack) {
        return color;
    }

    @Override
    protected int getTintColor(FluidState state, BlockAndTintGetter world, BlockPos pos) {
        return color;
    }

    @Override
    public int getDensity() {
        // Gases are lighter than air (Create fills their tanks from the top); liquids are water-like.
        return gas ? -1 : 1000;
    }
}
