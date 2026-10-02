package com.koala.reactingreactions.content.aeronautics;

import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.registry.CRRRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;

import dev.eriksonn.aeronautics.content.blocks.hot_air.lifting_gas.LiftingGasType;

import net.minecraft.network.chat.Component;

/** Helium and hydrogen as balloon gases, lifting more than hot air and steam, and hydrogen more than helium. */
public class CRRLiftingGasTypes {
    public static final LiftingGasTypeEntry HELIUM = register("helium", 2.5);
    public static final LiftingGasTypeEntry HYDROGEN = register("hydrogen", 3.5);

    private static LiftingGasTypeEntry register(String name, double liftStrength) {
        var entry = CRRRegistrate.REGISTRATE.liftingGasType(name, () -> new Simple(name, liftStrength));
        return new LiftingGasTypeEntry(entry);
    }

    public static void init() {
        // Forces this class's static fields to load, matching the pattern
        // already used by CRRRegistrate/CRRFluids.
    }

    public record LiftingGasTypeEntry(RegistryEntry<LiftingGasType, Simple> entry) {
        public LiftingGasType get() {
            return entry.get();
        }
    }

    private static final class Simple implements LiftingGasType {
        private final String name;
        private final double liftStrength;

        private Simple(String name, double liftStrength) {
            this.name = name;
            this.liftStrength = liftStrength;
        }

        @Override
        public Component getName() {
            return Component.translatable("lifting_gas." + ReactingReactions.MODID + "." + name);
        }

        @Override
        public double getFillingTime() {
            return 180.0;
        }

        @Override
        public double getEmptyingTime() {
            return 180.0;
        }

        @Override
        public double getLiftStrength() {
            return liftStrength;
        }

        @Override
        public double getResponsivenessAdjustmentFactor() {
            return 5.0;
        }

        @Override
        public double getResponsivenessAdjustmentRange() {
            return 0.05;
        }
    }
}
