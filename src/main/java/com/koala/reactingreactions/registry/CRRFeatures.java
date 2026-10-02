package com.koala.reactingreactions.registry;

import com.koala.reactingreactions.content.drill.RichVein;

import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.content.drill.RichVeinFeature;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Custom worldgen feature types (the placed/configured features themselves are datagen'd, see {@code MiscData}). */
public class CRRFeatures {
    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, ReactingReactions.MODID);

    public static final DeferredHolder<Feature<?>, RichVeinFeature> RICH_VEINS = FEATURES.register("rich_veins",
            () -> new RichVeinFeature(NoneFeatureConfiguration.CODEC));

    public static final DataMapType<Block, RichVein> RICH_VEIN = DataMapType
            .builder(ReactingReactions.asResource("rich_vein"), Registries.BLOCK, RichVein.CODEC).build();

    public static void register(IEventBus modEventBus) {
        FEATURES.register(modEventBus);
        modEventBus.addListener((RegisterDataMapTypesEvent event) -> event.register(RICH_VEIN));
    }
}
