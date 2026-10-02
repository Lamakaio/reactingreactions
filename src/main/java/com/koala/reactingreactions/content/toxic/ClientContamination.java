package com.koala.reactingreactions.content.toxic;

import com.koala.reactingreactions.network.CRRContaminationPayload;
import com.koala.reactingreactions.registry.CRRParticles;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Client side of contamination: the cells the server last sent, the toxic haze drawn in them, the level at the player's feet, and
 * the stains badly contaminated air leaves on the ground for a while after it clears.
 */
public final class ClientContamination {
    private static final int LIGHT_HAZE = 0xB8D86A;
    private static final int DENSE_HAZE = 0x8CA81E;
    private static final Map<Long, Float> CELLS = new HashMap<>();
    /** Ground block below a badly contaminated cell, and how dark its stain is (1 fades to 0 over two minutes). */
    private static final Map<Long, Float> STAINS = new HashMap<>();
    private static final float STAIN_FROM_LEVEL = 1.0F;
    private static final float STAIN_FADE_PER_TICK = 1 / 2400F;
    private static final int MAX_STAINS = 512;
    private static long receivedAt = Long.MIN_VALUE;
    // Stains belong to one world: they are dropped on a change of dimension or server.
    private static Level stainedLevel;

    private ClientContamination() {
    }

    public static void receive(CRRContaminationPayload payload) {
        CELLS.clear();
        for (int i = 0; i < payload.positions().size() && i < payload.levels().size(); i++) {
            CELLS.put(payload.positions().get(i), payload.levels().get(i));
            stain(payload.positions().get(i), payload.levels().get(i));
        }
        Minecraft minecraft = Minecraft.getInstance();
        receivedAt = minecraft.level == null ? Long.MIN_VALUE : minecraft.level.getGameTime();
    }

    private static boolean fresh(Minecraft minecraft) {
        return minecraft.level != null && minecraft.level.getGameTime() - receivedAt < 60;
    }

    private static void stain(long cell, float level) {
        Minecraft minecraft = Minecraft.getInstance();
        if (level < STAIN_FROM_LEVEL || minecraft.level == null || (STAINS.size() >= MAX_STAINS && !STAINS.containsKey(cell))) {
            return;
        }
        BlockPos air = BlockPos.of(cell);
        BlockPos ground = air.below();
        if (!minecraft.level.getBlockState(air).getCollisionShape(minecraft.level, air).isEmpty()
                || !minecraft.level.getBlockState(ground).isFaceSturdy(minecraft.level, ground, Direction.UP)) {
            return;
        }
        STAINS.merge(ground.asLong(), Math.min(1.0F, level / 4.0F), Math::max);
    }

    public static boolean hasStains() {
        return !STAINS.isEmpty();
    }

    /** Each stained ground block (as {@link BlockPos#asLong}) and how dark it is. */
    public static void forEachStain(BiConsumer<Long, Float> action) {
        STAINS.forEach(action);
    }

    /** Contamination at the player's feet or head, 0 if the last update is stale. */
    public static float levelAtPlayer() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !fresh(minecraft)) {
            return 0;
        }
        BlockPos feet = minecraft.player.blockPosition();
        return Math.max(CELLS.getOrDefault(feet.asLong(), 0.0F), CELLS.getOrDefault(feet.above().asLong(), 0.0F));
    }

    /** Haze drifting through the contaminated cells: denser, yellower and more opaque where the air is worse. */
    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != stainedLevel) {
            STAINS.clear();
            stainedLevel = minecraft.level;
        }
        if (minecraft.level != null && !minecraft.isPaused()) {
            STAINS.replaceAll((key, darkness) -> darkness - STAIN_FADE_PER_TICK);
            STAINS.values().removeIf(darkness -> darkness <= 0);
        }
        if (minecraft.level == null || minecraft.isPaused() || CELLS.isEmpty() || !fresh(minecraft) || minecraft.level.getGameTime() % 2 != 0) {
            return;
        }
        var random = minecraft.level.random;
        int spawned = 0;
        for (Map.Entry<Long, Float> cell : CELLS.entrySet()) {
            if (spawned >= 12) {
                break;
            }
            float strength = Math.min(1.0F, cell.getValue() / 4.0F);
            if (random.nextFloat() > Math.min(1.0F, cell.getValue() * 0.25F)) {
                continue;
            }
            BlockPos pos = BlockPos.of(cell.getKey());
            int colour = FastColor.ARGB32.lerp(strength, LIGHT_HAZE, DENSE_HAZE);
            minecraft.level.addParticle(CRRParticles.haze(colour, 0.12F + strength * 0.25F), pos.getX() + random.nextDouble(),
                    pos.getY() + random.nextDouble() * 0.8, pos.getZ() + random.nextDouble(), 0, 0, 0);
            spawned++;
        }
    }

    public static void clear() {
        CELLS.clear();
        STAINS.clear();
        receivedAt = Long.MIN_VALUE;
    }
}
