package com.koala.reactingreactions.content.toxic;

import com.koala.reactingreactions.event.CRRExoEvents;

import com.koala.reactingreactions.Config;
import com.koala.reactingreactions.registry.CRRToxicity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

/** Lookups and the player gauge API for toxic compounds. Everything here is inert while the config flag is off. */
public final class Toxicity {
    public static final float GAUGE_MAX = 100.0F;

    private Toxicity() {
    }

    // ---- compounds ---------------------------------------------------------------------------------------------

    public static ToxicFluid of(Fluid fluid) {
        if (fluid == null || fluid.isSame(Fluids.EMPTY)) {
            return ToxicFluid.HARMLESS;
        }
        Fluid source = fluid instanceof FlowingFluid flowing ? flowing.getSource() : fluid;
        ToxicFluid data = source.builtInRegistryHolder().getData(CRRToxicity.TOXIC_FLUID);
        return data == null ? ToxicFluid.HARMLESS : data;
    }

    public static ToxicFluid of(FluidStack stack) {
        return stack.isEmpty() ? ToxicFluid.HARMLESS : of(stack.getFluid());
    }

    public static boolean isGas(Fluid fluid) {
        return fluid.getFluidType().isLighterThanAir();
    }

    /** Toxicity of a carried item: a filled bucket takes its fluid's, other items use the item data map. */
    public static float carriedToxicity(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        Item item = stack.getItem();
        if (item instanceof BucketItem bucket) {
            return of(bucket.content).toxicity();
        }
        ToxicItem data = item.builtInRegistryHolder().getData(CRRToxicity.TOXIC_ITEM);
        return data == null ? 0 : data.toxicity();
    }

    // ---- the gauge ---------------------------------------------------------------------------------------------

    public static float gauge(Player player) {
        return player.getData(CRRToxicity.GAUGE);
    }

    public static void setGauge(Player player, float value) {
        float clamped = Math.max(0, Math.min(GAUGE_MAX, value));
        if (clamped != player.getData(CRRToxicity.GAUGE)) {
            player.setData(CRRToxicity.GAUGE, clamped);
        }
    }

    public static void addToGauge(Player player, float delta) {
        if (delta > 0 && CRRExoEvents.sealed(player)) {
            return;
        }
        if (Config.toxicityEnabled() && Config.bool(Config.GAUGE_ENABLED, true)) {
            setGauge(player, gauge(player) + delta);
        }
    }

    public static DamageSource damageSource(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(CRRToxicity.TOXICITY_DAMAGE));
    }
}
