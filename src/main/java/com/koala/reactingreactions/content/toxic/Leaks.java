package com.koala.reactingreactions.content.toxic;

import com.koala.reactingreactions.Config;
import com.koala.reactingreactions.registry.CRREntities;
import com.koala.reactingreactions.registry.CRRGasFluidType;
import com.koala.reactingreactions.registry.CRRSounds;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * What happens when a container holding a toxic compound leaks: gases vent as a small spray, liquids leave a pool on the floor
 * (capped per chunk), and both contaminate the air around. Called from the mixins on Create's tanks and pipes.
 */
public final class Leaks {
    private static final int CHECK_INTERVAL = 40;

    private Leaks() {
    }

    /** Spreads the checks of neighbouring containers over different ticks. */
    public static boolean shouldCheck(Level level, BlockPos pos) {
        return Math.floorMod(level.getGameTime() + pos.hashCode(), CHECK_INTERVAL) == 0;
    }

    /**
     * @param fill      how full the container is (0-1); a fuller one leaks more
     * @param tightness 1 for a plain container, less for a better sealed one
     * @param drain     where the leaked fluid comes from, or null if it is not taken (pipes)
     */
    public static void check(Level level, BlockPos pos, FluidStack fluid, float fill, float tightness, @Nullable IFluidHandler drain) {
        if (!(level instanceof ServerLevel serverLevel) || !Config.toxicityEnabled()) {
            return;
        }
        ToxicFluid toxic = Toxicity.of(fluid);
        if (toxic.toxicity() <= 0) {
            return;
        }
        float chance = toxic.toxicity() * (float) Config.number(Config.LEAK_RATE, 1.0) * tightness * (0.4F + 0.6F * Math.min(1.0F, fill)) * 0.01F;
        if (serverLevel.random.nextFloat() >= chance) {
            return;
        }
        boolean gas = Toxicity.isGas(fluid.getFluid());
        // A working scrubber stops gas from venting near it.
        if (gas && Scrubbers.covers(serverLevel, pos)) {
            return;
        }
        int amount = Config.number(Config.LEAK_AMOUNT_MB, 5);
        if (drain != null && Config.bool(Config.LEAKS_LOSE_FLUID, true)) {
            FluidStack lost = drain.drain(new FluidStack(fluid.getFluid(), amount), IFluidHandler.FluidAction.EXECUTE);
            if (lost.isEmpty()) {
                return;
            }
        }
        Contamination.addFrom(serverLevel, pos, toxic, contaminationOf(toxic, amount));
        // A leak is a vibration, so vanilla Sculk Sensors nearby can raise the alarm.
        serverLevel.gameEvent(null, GameEvent.FLUID_PLACE, pos);
        if (gas) {
            if (Config.bool(Config.VENTS, true)) {
                vent(serverLevel, pos, fluid.getFluid(), toxic, amount);
            }
        } else if (Config.bool(Config.POOLS, true)) {
            pool(serverLevel, pos, fluid.getFluid(), toxic, amount);
        }
    }

    /** How much one release of {@code amount} mB contaminates: a single leak's worth, times how many leaks it amounts to. */
    public static float contaminationOf(ToxicFluid toxic, int amount) {
        return (1.0F + toxic.toxicity() * 0.3F) * Math.max(1, amount / Math.max(1, Config.number(Config.LEAK_AMOUNT_MB, 5)));
    }

    /** The colour of a vent or splash of {@code fluid}: its own for this mod's fluids, grey otherwise. */
    public static int colorOf(Fluid fluid) {
        return fluid.getFluidType() instanceof CRRGasFluidType gasType ? gasType.color() : 0xFF909090;
    }

    /**
     * Releases {@code amount} mB of {@code fluid} at once, as a thrown flask does: it contaminates, and vents if it is a gas or
     * pools on the floor below otherwise (joining a pool already there).
     */
    public static void spill(ServerLevel level, BlockPos pos, Fluid fluid, int amount) {
        ToxicFluid toxic = Toxicity.of(fluid);
        Contamination.addFrom(level, pos, toxic, contaminationOf(toxic, amount));
        level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
        if (Toxicity.isGas(fluid)) {
            if (Config.bool(Config.VENTS, true)) {
                vent(level, pos, fluid, toxic, amount);
            }
        } else if (Config.bool(Config.POOLS, true)) {
            pool(level, pos, fluid, toxic, amount);
        }
    }

    private static void vent(ServerLevel level, BlockPos pos, Fluid fluid, ToxicFluid toxic, int amount) {
        int color = colorOf(fluid);
        // The spray lasts a few seconds (see Vents), and ignites there if a flammable gas meets a spark.
        Vents.start(level, pos, color, toxic, amount);
        if (toxic.flammable() && Ignition.exposed(level, new AABB(pos))) {
            Ignition.ignite(level, Vec3.atCenterOf(pos), toxic, amount);
        }
    }

    private static void pool(ServerLevel level, BlockPos pos, Fluid fluid, ToxicFluid toxic, int amount) {
        // The floor below the leak: the first sturdy top face within a few blocks.
        BlockPos floor = null;
        for (int i = 0; i <= 6; i++) {
            BlockPos below = pos.below(i);
            if (level.getBlockState(below.below()).isFaceSturdy(level, below.below(), Direction.UP) && level.getBlockState(below).getCollisionShape(level, below).isEmpty()) {
                floor = below;
                break;
            }
        }
        if (floor == null) {
            return;
        }
        double x = floor.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.6;
        double z = floor.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.6;
        if (floor.getY() < pos.getY()) {
            LeakDrips.start(level, pos, new Vec3(x, floor.getY(), z), fluid);
        }
        AABB near = new AABB(x - 1.5, floor.getY() - 0.5, z - 1.5, x + 1.5, floor.getY() + 1.5, z + 1.5);
        for (LeakPoolEntity existing : level.getEntitiesOfClass(LeakPoolEntity.class, near)) {
            if (existing.getFluid() == fluid) {
                existing.addAmount(amount);
                return;
            }
        }
        // The cap per chunk: past it, a leak only contaminates.
        AABB chunk = new AABB((pos.getX() >> 4) << 4, level.getMinBuildHeight(), (pos.getZ() >> 4) << 4, ((pos.getX() >> 4) << 4) + 16, level.getMaxBuildHeight(), ((pos.getZ() >> 4) << 4) + 16);
        List<LeakPoolEntity> inChunk = level.getEntitiesOfClass(LeakPoolEntity.class, chunk);
        if (inChunk.size() >= Config.number(Config.MAX_POOLS_PER_CHUNK, 6)) {
            return;
        }
        LeakPoolEntity pool = new LeakPoolEntity(CRREntities.LEAK_POOL.get(), level);
        pool.setPos(x, floor.getY(), z);
        pool.setContents(fluid, amount);
        level.addFreshEntity(pool);
        level.playSound(null, pos, CRRSounds.POOL_SPLASH.get(), SoundSource.BLOCKS, 0.5F, 0.9F + level.random.nextFloat() * 0.2F);
    }
}
