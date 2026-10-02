package com.koala.reactingreactions.content.multiblock;

import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.content.fluids.particle.FluidParticleData;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

/** Client-side particles for working machines. */
public final class MachineEffects {
    private MachineEffects() {
    }

    /** A bubble popping at (x, y, z), with a splash of the fluid now and then. */
    public static void bubble(Level level, RandomSource random, FluidStack fluid, double x, double y, double z) {
        level.addParticle(ParticleTypes.BUBBLE_POP, x, y, z, 0, 0.02, 0);
        if (!fluid.isEmpty() && random.nextInt(3) == 0) {
            level.addParticle(new FluidParticleData(AllParticleTypes.FLUID_PARTICLE.get(), fluid), x, y, z,
                    (random.nextDouble() - 0.5) * 0.1, 0.12, (random.nextDouble() - 0.5) * 0.1);
        }
    }

    public static void spark(Level level, RandomSource random, double x, double y, double z) {
        level.addParticle(ParticleTypes.ELECTRIC_SPARK, x, y, z, (random.nextDouble() - 0.5) * 0.2, 0.1, (random.nextDouble() - 0.5) * 0.2);
    }

    /** A puff drifting up from (x, y, z). */
    public static void puff(Level level, RandomSource random, ParticleOptions particle, double x, double y, double z) {
        level.addParticle(particle, x + (random.nextDouble() - 0.5) * 0.4, y, z + (random.nextDouble() - 0.5) * 0.4, 0, 0.05, 0);
    }
}
