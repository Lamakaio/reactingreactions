package com.koala.reactingreactions.content.toxic;

import com.koala.reactingreactions.registry.CRRSounds;
import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.content.fluids.particle.FluidParticleData;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** A liquid leak drips for a couple of seconds from under the leaking block onto its pool, so the source can be found. */
public final class LeakDrips {
    private static final int DURATION_TICKS = 40;
    private static final int MAX_PER_LEVEL = 64;
    private static final Map<ResourceKey<Level>, List<Drip>> ACTIVE = new ConcurrentHashMap<>();

    private static final class Drip {
        final BlockPos source;
        final Vec3 from;
        final Vec3 landing;
        final FluidParticleData particle;
        int ticksLeft = DURATION_TICKS;

        Drip(BlockPos source, Vec3 from, Vec3 landing, Fluid fluid) {
            this.source = source;
            this.from = from;
            this.landing = landing;
            this.particle = new FluidParticleData(AllParticleTypes.FLUID_DRIP.get(), new FluidStack(fluid, 1000));
        }
    }

    private LeakDrips() {
    }

    /** Drips from the bottom of {@code source} down to {@code landing}, the spot on the floor the pool is at. */
    public static void start(ServerLevel level, BlockPos source, Vec3 landing, Fluid fluid) {
        // An empty fluid cannot be sent in a particle packet: the client would be disconnected.
        if (fluid == Fluids.EMPTY) {
            return;
        }
        List<Drip> drips = ACTIVE.computeIfAbsent(level.dimension(), key -> new ArrayList<>());
        if (drips.size() < MAX_PER_LEVEL) {
            drips.add(new Drip(source, new Vec3(landing.x, source.getY() - 0.02, landing.z), landing, fluid));
        }
    }

    /** Every tick per level. */
    public static void tick(ServerLevel level) {
        List<Drip> drips = ACTIVE.get(level.dimension());
        if (drips == null || drips.isEmpty()) {
            return;
        }
        var iterator = drips.iterator();
        while (iterator.hasNext()) {
            Drip drip = iterator.next();
            if (--drip.ticksLeft <= 0 || !level.isLoaded(drip.source)) {
                iterator.remove();
                continue;
            }
            // Faster at first, slowing to a few last drops.
            int every = drip.ticksLeft > DURATION_TICKS / 2 ? 3 : 7;
            if (drip.ticksLeft % every == 0) {
                level.sendParticles(drip.particle, drip.from.x, drip.from.y, drip.from.z, 1, 0.05, 0, 0.05, 0);
            }
            if (drip.ticksLeft % 12 == 0) {
                level.playSound(null, drip.landing.x, drip.landing.y, drip.landing.z, CRRSounds.LEAK_DRIP.get(), SoundSource.BLOCKS, 0.3F,
                        0.8F + level.random.nextFloat() * 0.4F);
            }
        }
    }

    public static void clearAll() {
        ACTIVE.clear();
    }
}
