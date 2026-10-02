package com.koala.reactingreactions.registry;

import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.content.airlessoven.recipe.AirlessOvenRecipe;
import com.koala.reactingreactions.content.distillation.recipe.DistillationRecipe;
import com.koala.reactingreactions.content.electrolysis.recipe.ElectrolysisRecipe;
import com.koala.reactingreactions.content.electrolysis.recipe.ElectrolysisRecipeParams;
import com.koala.reactingreactions.content.multiblock.MultiblockRecipe;
import com.koala.reactingreactions.content.reaction.recipe.ReactionRecipe;
import com.koala.reactingreactions.content.reaction.recipe.ReactionRecipeParams;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe.Factory;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class CRRRecipeTypes {
    private static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, ReactingReactions.MODID);
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, ReactingReactions.MODID);

    public static final Entry<ElectrolysisRecipe> ELECTROLYSIS =
            register("electrolysis_recipe", ElectrolysisRecipe::new, ElectrolysisRecipeParams.CODEC, ElectrolysisRecipeParams.STREAM_CODEC);
    // Unused when Create Diesel Generators is installed: its own tower takes over.
    public static final Entry<DistillationRecipe> DISTILLATION = register("distillation_recipe", DistillationRecipe::new);
    public static final Entry<AirlessOvenRecipe> AIRLESS_OVEN = register("airless_oven_recipe", AirlessOvenRecipe::new);
    public static final Entry<ReactionRecipe> REACTION =
            register("reaction_recipe", ReactionRecipe::new, ReactionRecipeParams.CODEC, ReactionRecipeParams.STREAM_CODEC);

    private CRRRecipeTypes() {
    }

    private static <R extends MultiblockRecipe<ProcessingRecipeParams>> Entry<R> register(String name, Factory<ProcessingRecipeParams, R> factory) {
        return register(name, factory, ProcessingRecipeParams.CODEC, ProcessingRecipeParams.STREAM_CODEC);
    }

    private static <P extends ProcessingRecipeParams, R extends MultiblockRecipe<P>> Entry<R> register(String name, Factory<P, R> factory,
            MapCodec<P> codec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
        return new Entry<>(name, () -> new MultiblockRecipe.Serializer<>(factory, codec, streamCodec));
    }

    public static void register(IEventBus modEventBus) {
        TYPES.register(modEventBus);
        SERIALIZERS.register(modEventBus);
    }

    public static final class Entry<R extends Recipe<?>> implements IRecipeTypeInfo {
        private final ResourceLocation id;
        private final DeferredHolder<RecipeType<?>, RecipeType<R>> type;
        private final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<R>> serializer;

        private Entry(String name, Supplier<? extends RecipeSerializer<R>> serializer) {
            this.id = ReactingReactions.asResource(name);
            this.type = TYPES.register(name, () -> RecipeType.simple(id));
            this.serializer = SERIALIZERS.register(name, serializer);
        }

        public RecipeType<R> get() {
            return type.get();
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends RecipeSerializer<?>> T getSerializer() {
            return (T) serializer.get();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <I extends RecipeInput, T extends Recipe<I>> RecipeType<T> getType() {
            return (RecipeType<T>) type.get();
        }
    }
}
