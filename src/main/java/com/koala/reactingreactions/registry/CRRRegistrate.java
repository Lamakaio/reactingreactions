package com.koala.reactingreactions.registry;

import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.datagen.CRRWornModels;
import com.simibubi.create.content.fluids.VirtualFluid;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.VirtualFluidBuilder;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import dev.eriksonn.aeronautics.content.blocks.hot_air.lifting_gas.LiftingGasType;
import dev.eriksonn.aeronautics.index.AeroRegistries;

import net.minecraft.resources.ResourceLocation;

public class CRRRegistrate extends CreateRegistrate {
    public static final CRRRegistrate REGISTRATE = create(ReactingReactions.MODID);

    protected CRRRegistrate(String modid) {
        super(modid);
        defaultCreativeTab(ReactingReactions.MAIN_TAB_KEY);
        CRRLang.register(this);
        addDataGenerator(ProviderType.ITEM_MODEL, CRRWornModels::provide);
    }

    public static CRRRegistrate create(String modid) {
        return new CRRRegistrate(modid);
    }

    private static final ResourceLocation GAS_TEXTURE =
            ReactingReactions.asResource("block/fluid_gas_still");

    private static final ResourceLocation LIQUID_TEXTURE =
            ReactingReactions.asResource("block/fluid_liquid_still");

    public FluidBuilder<VirtualFluid, CRRRegistrate> gasFluid(String name, int color) {
        return virtualFluid(name, color, true);
    }

    /** A tank-only liquid: same plumbing as {@link #gasFluid}, but with the liquid texture and normal density. */
    public FluidBuilder<VirtualFluid, CRRRegistrate> liquidFluid(String name, int color) {
        return virtualFluid(name, color, false);
    }

    private FluidBuilder<VirtualFluid, CRRRegistrate> virtualFluid(String name, int color, boolean gas) {
        ResourceLocation texture = gas ? GAS_TEXTURE : LIQUID_TEXTURE;
        return entry(name, callback -> new VirtualFluidBuilder<>(this, (CRRRegistrate) self(), name, callback,
                texture, texture,
                CRRGasFluidType.create(color, gas), VirtualFluid::createSource, VirtualFluid::createFlowing));
    }

    public <T extends LiftingGasType> RegistryEntry<LiftingGasType, T> liftingGasType(String name, NonNullSupplier<T> factory) {
        return simple(name, AeroRegistries.Keys.LIFTING_GAS_TYPE, factory);
    }
}
