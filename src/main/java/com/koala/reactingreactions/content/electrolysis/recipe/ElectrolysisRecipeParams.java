package com.koala.reactingreactions.content.electrolysis.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/** Adds the minimum voltage and the allowed electrode blocks (empty means any). */
public class ElectrolysisRecipeParams extends ProcessingRecipeParams {
    public double minVoltage;
    public List<ResourceLocation> electrodes = List.of();

    public static final MapCodec<ElectrolysisRecipeParams> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    codec((Supplier<ElectrolysisRecipeParams>) ElectrolysisRecipeParams::new).forGetter(Function.identity()),
                    Codec.DOUBLE.optionalFieldOf("min_voltage", 0.0).forGetter(p -> p.minVoltage),
                    ResourceLocation.CODEC.listOf().optionalFieldOf("electrodes", List.of()).forGetter(p -> p.electrodes))
            .apply(instance, (params, minVoltage, electrodes) -> {
                params.minVoltage = minVoltage;
                params.electrodes = electrodes;
                return params;
            }));

    public static final StreamCodec<RegistryFriendlyByteBuf, ElectrolysisRecipeParams> STREAM_CODEC =
            streamCodec((Supplier<ElectrolysisRecipeParams>) ElectrolysisRecipeParams::new);

    @Override
    protected void encode(RegistryFriendlyByteBuf buffer) {
        super.encode(buffer);
        ByteBufCodecs.DOUBLE.encode(buffer, minVoltage);
        ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, electrodes);
    }

    @Override
    protected void decode(RegistryFriendlyByteBuf buffer) {
        super.decode(buffer);
        minVoltage = ByteBufCodecs.DOUBLE.decode(buffer);
        electrodes = ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer);
    }
}
