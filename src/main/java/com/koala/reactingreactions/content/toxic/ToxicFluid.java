package com.koala.reactingreactions.content.toxic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Data map value ({@code reactingreactions:toxicity} on fluids): how toxic a compound is (0-10; unlisted fluids are harmless) and whether
 * it burns or explodes. Whether it is a gas or a liquid comes from the fluid itself, not from here.
 */
public record ToxicFluid(float toxicity, boolean flammable, boolean explosive) {
    public static final Codec<ToxicFluid> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("toxicity").forGetter(ToxicFluid::toxicity),
            Codec.BOOL.optionalFieldOf("flammable", false).forGetter(ToxicFluid::flammable),
            Codec.BOOL.optionalFieldOf("explosive", false).forGetter(ToxicFluid::explosive)).apply(instance, ToxicFluid::new));

    public static final ToxicFluid HARMLESS = new ToxicFluid(0, false, false);
}
