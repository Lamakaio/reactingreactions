package com.koala.reactingreactions.content.toxic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Data map value ({@code reactingreactions:toxic_item} on items): a solid that is unhealthy to carry around. */
public record ToxicItem(float toxicity) {
    public static final Codec<ToxicItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("toxicity").forGetter(ToxicItem::toxicity)).apply(instance, ToxicItem::new));
}
