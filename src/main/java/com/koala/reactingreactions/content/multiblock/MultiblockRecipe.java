package com.koala.reactingreactions.content.multiblock;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** A recipe run by a multiblock controller, which matches it against its own tanks and slots rather than through {@link #matches}. */
public abstract class MultiblockRecipe<P extends ProcessingRecipeParams> extends ProcessingRecipe<RecipeInput, P> {
    protected MultiblockRecipe(IRecipeTypeInfo type, P params) {
        super(type, params);
    }

    @Override
    protected int getMaxInputCount() {
        return 6;
    }

    @Override
    protected int getMaxOutputCount() {
        return 4;
    }

    @Override
    protected int getMaxFluidInputCount() {
        return MultiblockControllerBlockEntity.FLUID_INPUTS;
    }

    @Override
    protected int getMaxFluidOutputCount() {
        return MultiblockControllerBlockEntity.FLUID_OUTPUTS;
    }

    @Override
    public boolean matches(RecipeInput input, Level level) {
        return false;
    }

    @Override
    protected boolean canSpecifyDuration() {
        return true;
    }

    @Override
    protected boolean canRequireHeat() {
        return true;
    }

    public static class Serializer<P extends ProcessingRecipeParams, R extends MultiblockRecipe<P>> implements RecipeSerializer<R> {
        private final MapCodec<R> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, R> streamCodec;

        public Serializer(Factory<P, R> factory, MapCodec<P> paramsCodec, StreamCodec<RegistryFriendlyByteBuf, P> paramsStreamCodec) {
            this.codec = ProcessingRecipe.codec(factory, paramsCodec);
            this.streamCodec = ProcessingRecipe.streamCodec(factory, paramsStreamCodec);
        }

        @Override
        public MapCodec<R> codec() {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, R> streamCodec() {
            return streamCodec;
        }
    }
}
