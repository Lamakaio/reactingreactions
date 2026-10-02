package com.koala.reactingreactions.content.toxic;

import com.koala.reactingreactions.Config;
import com.koala.reactingreactions.registry.CRRTags;

import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Short-range contamination of the air around leaks: a sparse per-block map that only exists near an active leak and fades within
 * about half a minute. It is never saved, and it does not pass through airtight blocks, so a sealed room keeps it inside. Plants,
 * grass and animals in a contaminated cell are hurt; players standing in one add to their toxicity gauge (see
 * {@link ToxicityEvents}).
 */
public final class Contamination {
    private static final Map<ResourceKey<Level>, Long2FloatOpenHashMap> CELLS = new ConcurrentHashMap<>();
    private static final int MAX_CELLS_PER_LEVEL = 4096;
    /** Level at or above which plants, grass and animals in a cell are hurt. */
    private static final float HARM_THRESHOLD = 1.0F;
    private static final float FADE_PER_SECOND = 0.82F;
    /** The effect swirl of poison, around animals being hurt. */
    private static final ColorParticleOption SICKNESS = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0xFF8CA81E);

    private Contamination() {
    }

    /** Adds {@code amount} of {@code toxic}'s pollution, unless it is too mild to foul the air ({@code minAirToxicity}). */
    public static void addFrom(ServerLevel level, BlockPos source, ToxicFluid toxic, float amount) {
        if (toxic.toxicity() >= Config.number(Config.MIN_AIR_TOXICITY, 3.0)) {
            add(level, source, amount);
        }
    }

    /** Adds {@code amount} at the leak and, with falloff, to the air around it (not through airtight blocks). */
    public static void add(ServerLevel level, BlockPos source, float amount) {
        if (!Config.toxicityEnabled() || amount <= 0) {
            return;
        }
        int radius = Config.number(Config.CONTAMINATION_RADIUS, 3);
        Long2FloatOpenHashMap cells = CELLS.computeIfAbsent(level.dimension(), key -> new Long2FloatOpenHashMap());
        if (cells.size() > MAX_CELLS_PER_LEVEL) {
            return;
        }
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    int distance = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
                    if (distance > radius + 1) {
                        continue;
                    }
                    BlockPos pos = source.offset(dx, dy, dz);
                    if (distance > 0 && (level.getBlockState(pos).is(CRRTags.AIRTIGHT) || isWalledOff(level, source, pos))) {
                        continue;
                    }
                    cells.addTo(pos.asLong(), amount / (1 + distance));
                }
            }
        }
    }

    /** Whether an airtight block sits directly between the source and a cell (checked on the straight line's midpoint). */
    private static boolean isWalledOff(ServerLevel level, BlockPos source, BlockPos target) {
        BlockPos mid = new BlockPos((source.getX() + target.getX()) / 2, (source.getY() + target.getY()) / 2, (source.getZ() + target.getZ()) / 2);
        return level.getBlockState(mid).is(CRRTags.AIRTIGHT);
    }

    /** The strongest contaminated cells within a cube around a point (at most {@code max}), for the client's particles and warning. */
    public static List<Map.Entry<BlockPos, Float>> nearby(Level level, BlockPos center, int radius, int max) {
        Long2FloatOpenHashMap cells = CELLS.get(level.dimension());
        List<Map.Entry<BlockPos, Float>> found = new ArrayList<>();
        if (cells == null || cells.isEmpty()) {
            return found;
        }
        for (Long2FloatMap.Entry entry : cells.long2FloatEntrySet()) {
            BlockPos pos = BlockPos.of(entry.getLongKey());
            if (Math.abs(pos.getX() - center.getX()) <= radius && Math.abs(pos.getY() - center.getY()) <= radius && Math.abs(pos.getZ() - center.getZ()) <= radius) {
                found.add(Map.entry(pos, entry.getFloatValue()));
            }
        }
        found.sort((a, b) -> Float.compare(b.getValue(), a.getValue()));
        return found.size() > max ? new ArrayList<>(found.subList(0, max)) : found;
    }

    public static float levelAt(Level level, BlockPos pos) {
        Long2FloatOpenHashMap cells = CELLS.get(level.dimension());
        return cells == null ? 0 : cells.get(pos.asLong());
    }

    /** Removes contamination in a cube around a point; used by scrubbers. Returns how much was removed. */
    public static float clear(ServerLevel level, BlockPos center, int radius, float strength) {
        Long2FloatOpenHashMap cells = CELLS.get(level.dimension());
        if (cells == null || cells.isEmpty()) {
            return 0;
        }
        float removed = 0;
        for (Long2FloatMap.Entry entry : cells.long2FloatEntrySet()) {
            BlockPos pos = BlockPos.of(entry.getLongKey());
            if (Math.abs(pos.getX() - center.getX()) <= radius && Math.abs(pos.getY() - center.getY()) <= radius && Math.abs(pos.getZ() - center.getZ()) <= radius) {
                float take = Math.min(entry.getFloatValue(), strength);
                entry.setValue(entry.getFloatValue() - take);
                removed += take;
            }
        }
        return removed;
    }

    /** Once a second per level: fades every cell, drops the empty ones, and hurts what stands in the rest. */
    public static void tick(ServerLevel level) {
        Long2FloatOpenHashMap cells = CELLS.get(level.dimension());
        if (cells == null || cells.isEmpty()) {
            return;
        }
        boolean plants = Config.bool(Config.DAMAGE_PLANTS, true);
        boolean animals = Config.bool(Config.DAMAGE_ANIMALS, true);
        List<Long> harmful = new ArrayList<>();
        var iterator = cells.long2FloatEntrySet().iterator();
        while (iterator.hasNext()) {
            Long2FloatMap.Entry entry = iterator.next();
            float faded = entry.getFloatValue() * FADE_PER_SECOND - 0.02F;
            if (faded <= 0.05F) {
                iterator.remove();
                continue;
            }
            entry.setValue(faded);
            if (faded >= HARM_THRESHOLD) {
                harmful.add(entry.getLongKey());
            }
        }
        for (long key : harmful) {
            BlockPos pos = BlockPos.of(key);
            if (!level.isLoaded(pos)) {
                continue;
            }
            if (plants) {
                BlockState state = level.getBlockState(pos);
                if (isPlant(state)) {
                    // Withering: bits of the plant fall off before it dies.
                    level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, state), pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5,
                            3, 0.3, 0.2, 0.3, 0);
                }
                if (level.random.nextInt(6) == 0) {
                    killPlant(level, pos);
                }
            }
            if (animals) {
                for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(pos))) {
                    mob.hurt(Toxicity.damageSource(level), 1.0F);
                    level.sendParticles(SICKNESS, mob.getX(), mob.getY() + mob.getBbHeight() / 2, mob.getZ(), 4, mob.getBbWidth() / 2,
                            mob.getBbHeight() / 3, mob.getBbWidth() / 2, 1);
                }
            }
        }
    }

    private static boolean isPlant(BlockState state) {
        return state.getBlock() instanceof BushBlock || state.is(BlockTags.LEAVES) || state.is(Blocks.VINE) || state.is(Blocks.CACTUS)
                || state.is(Blocks.SUGAR_CANE) || state.is(Blocks.BAMBOO);
    }

    private static void killPlant(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.GRASS_BLOCK)) {
            level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
        } else if (isPlant(state)) {
            // The usual breaking particles and sound, without drops.
            level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
            level.removeBlock(pos, false);
        }
    }

    public static void clearAll() {
        CELLS.clear();
    }
}
