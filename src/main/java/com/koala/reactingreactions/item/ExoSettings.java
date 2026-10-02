package com.koala.reactingreactions.item;

import com.koala.reactingreactions.registry.CRRDataComponents;
import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The toggles and modes of a chase item, set from the Exo Settings screen. Missing keys use their option's default. */
public record ExoSettings(Map<String, Integer> values) {
    public static final ExoSettings EMPTY = new ExoSettings(Map.of());
    public static final Codec<ExoSettings> CODEC = Codec.unboundedMap(Codec.STRING, Codec.INT).xmap(ExoSettings::new, ExoSettings::values);
    public static final StreamCodec<ByteBuf, ExoSettings> STREAM_CODEC =
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT).map(ExoSettings::new, s -> new HashMap<>(s.values()));

    /** One setting: its key and the names of its values (lang keys {@code reactingreactions.exo.<key>.<value>}). */
    public record Option(String key, List<String> values, int defaultValue) {
        public static Option toggle(String key, boolean on) {
            return new Option(key, List.of("off", "on"), on ? 1 : 0);
        }
    }

    /** An item with settings. */
    public interface Configurable {
        List<Option> exoOptions();

        default int setting(ItemStack stack, String key) {
            for (Option option : exoOptions()) {
                if (option.key().equals(key)) {
                    return stack.getOrDefault(CRRDataComponents.EXO_SETTINGS.get(), EMPTY).values().getOrDefault(key, option.defaultValue());
                }
            }
            throw new IllegalArgumentException("no exo option " + key);
        }

        default boolean enabled(ItemStack stack, String key) {
            return setting(stack, key) != 0;
        }

        /** Sets a value, ignoring unknown keys and out-of-range values (they come from the client). */
        default void set(ItemStack stack, String key, int value) {
            for (Option option : exoOptions()) {
                if (option.key().equals(key) && value >= 0 && value < option.values().size()) {
                    Map<String, Integer> values = new HashMap<>(stack.getOrDefault(CRRDataComponents.EXO_SETTINGS.get(), EMPTY).values());
                    values.put(key, value);
                    stack.set(CRRDataComponents.EXO_SETTINGS.get(), new ExoSettings(Map.copyOf(values)));
                }
            }
        }
    }
}
