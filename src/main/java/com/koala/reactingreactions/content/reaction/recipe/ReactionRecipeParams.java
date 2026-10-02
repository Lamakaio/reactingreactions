package com.koala.reactingreactions.content.reaction.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Function;
import java.util.function.Supplier;

/** Adds the stirring band, in absolute RPM of the roof shaft; a recipe without one needs no stirring. */
public class ReactionRecipeParams extends ProcessingRecipeParams {
    public float minRpm;
    public float maxRpm;

    public static final MapCodec<ReactionRecipeParams> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    codec((Supplier<ReactionRecipeParams>) ReactionRecipeParams::new).forGetter(Function.identity()),
                    Codec.FLOAT.optionalFieldOf("min_rpm", 0f).forGetter(p -> p.minRpm),
                    Codec.FLOAT.optionalFieldOf("max_rpm", 0f).forGetter(p -> p.maxRpm))
            .apply(instance, (params, min, max) -> {
                params.minRpm = min;
                params.maxRpm = max;
                return params;
            }));

    public static final StreamCodec<RegistryFriendlyByteBuf, ReactionRecipeParams> STREAM_CODEC =
            streamCodec((Supplier<ReactionRecipeParams>) ReactionRecipeParams::new);

    @Override
    protected void encode(RegistryFriendlyByteBuf buffer) {
        super.encode(buffer);
        ByteBufCodecs.FLOAT.encode(buffer, minRpm);
        ByteBufCodecs.FLOAT.encode(buffer, maxRpm);
    }

    @Override
    protected void decode(RegistryFriendlyByteBuf buffer) {
        super.decode(buffer);
        minRpm = ByteBufCodecs.FLOAT.decode(buffer);
        maxRpm = ByteBufCodecs.FLOAT.decode(buffer);
    }
}
