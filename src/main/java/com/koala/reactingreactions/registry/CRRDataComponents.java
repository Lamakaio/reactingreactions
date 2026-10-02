package com.koala.reactingreactions.registry;

import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.item.ExoSettings;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CRRDataComponents {
    private static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, ReactingReactions.MODID);

    /** The contents of a {@link com.koala.reactingreactions.item.FluidTankHolder}'s tank. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SimpleFluidContent>> FLUID_TANK =
            COMPONENTS.register("fluid_tank", () -> DataComponentType.<SimpleFluidContent>builder()
                    .persistent(SimpleFluidContent.CODEC)
                    .networkSynchronized(SimpleFluidContent.STREAM_CODEC)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ExoSettings>> EXO_SETTINGS =
            COMPONENTS.register("exo_settings", () -> DataComponentType.<ExoSettings>builder()
                    .persistent(ExoSettings.CODEC)
                    .networkSynchronized(ExoSettings.STREAM_CODEC)
                    .build());

    private CRRDataComponents() {
    }

    public static void register(IEventBus modEventBus) {
        COMPONENTS.register(modEventBus);
    }
}
