package com.koala.reactingreactions.content.drill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;

import java.util.List;

/** Data map value on a rock: the Rich Vein its veins hide, the chance a vein gets one, and how rich it rolls (weights for 1 to 5). */
public record RichVein(Block rich, float chance, int minVeinSize, List<Integer> richnessWeights) {
    private static final List<Integer> DEFAULT_WEIGHTS = List.of(15, 25, 30, 20, 10);

    public static final Codec<RichVein> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("rich").forGetter(RichVein::rich),
            Codec.floatRange(0, 1).optionalFieldOf("chance", 1.0F).forGetter(RichVein::chance),
            Codec.intRange(1, 4096).optionalFieldOf("min_vein_size", 6).forGetter(RichVein::minVeinSize),
            Codec.intRange(0, 1000).listOf(RichOreVeinBlock.MAX_LEVEL, RichOreVeinBlock.MAX_LEVEL).optionalFieldOf("richness_weights", DEFAULT_WEIGHTS)
                    .forGetter(RichVein::richnessWeights)).apply(instance, RichVein::new));

    public int rollRichness(RandomSource random) {
        int total = richnessWeights.stream().mapToInt(Integer::intValue).sum();
        int roll = random.nextInt(Math.max(1, total));
        for (int i = 0; i < richnessWeights.size(); i++) {
            roll -= richnessWeights.get(i);
            if (roll < 0) {
                return i + 1;
            }
        }
        return RichOreVeinBlock.MAX_LEVEL;
    }
}
