package com.koala.reactingreactions.content.toxic;

import com.koala.reactingreactions.Config;
import com.koala.reactingreactions.registry.CRRAdvancements;
import com.koala.reactingreactions.registry.CRRSounds;
import com.koala.reactingreactions.registry.CRRTags;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Fire and explosions of flammable pools and vents. Without {@code explosionsDestroyBlocks} they only hurt entities. */
public final class Ignition {
    private Ignition() {
    }

    /** Whether something in or next to the box would set off a flammable compound: a torch, fire, lava... (the tag), a burning entity. */
    public static boolean exposed(ServerLevel level, AABB box) {
        AABB area = box.inflate(1.0);
        for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(area.minX, area.minY, area.minZ), BlockPos.containing(area.maxX, area.maxY, area.maxZ))) {
            if (level.getBlockState(pos).is(CRRTags.IGNITES_TOXIC)) {
                return true;
            }
        }
        for (Entity entity : level.getEntities((Entity) null, area, Entity::isOnFire)) {
            return true;
        }
        return false;
    }

    /** Whether flammable leaks burn at all (a server option). */
    public static boolean enabled() {
        return Config.bool(Config.EXPLOSIONS, true);
    }

    /** Burns (and, for explosive compounds or big pools, explodes) at a point. {@code amount} is millibuckets involved. */
    public static void ignite(ServerLevel level, Vec3 at, ToxicFluid fluid, int amount) {
        if (!enabled()) {
            return;
        }
        boolean destroy = Config.bool(Config.EXPLOSIONS_DESTROY_BLOCKS, false);
        double cap = Config.number(Config.EXPLOSION_POWER_CAP, 3.0);
        // Both grow with the amount over a full pool's range, so a big spill is much worse than a drip.
        double size = Math.sqrt(Math.min(1.0, amount / (double) LeakPoolEntity.MAX_AMOUNT));
        double radius = 1.5 + 2.5 * size;
        level.playSound(null, BlockPos.containing(at), CRRSounds.IGNITE_WHOOSH.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        level.sendParticles(ParticleTypes.FLAME, at.x, at.y + 0.2, at.z, 20, radius * 0.3, 0.2, radius * 0.3, 0.02);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, at.x, at.y + 0.4, at.z, 8, radius * 0.3, 0.3, radius * 0.3, 0.02);
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, new AABB(at, at).inflate(8))) {
            CRRAdvancements.grant(player, "bad_idea");
        }
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(radius))) {
            entity.igniteForSeconds(4);
            entity.hurt(level.damageSources().inFire(), 2.0F);
        }
        if (Config.bool(Config.IGNITION_SPAWNS_FIRE, true)) {
            spawnFires(level, BlockPos.containing(at), (int) Math.ceil(radius), 3 + level.random.nextInt(4));
        }
        if (fluid.explosive() || amount >= 400) {
            // Explosive fluids (hydrogen, acetylene, aerozine) blow up harder than ones that merely burn.
            float power = (float) (cap * (0.25 + 0.75 * size) * (fluid.explosive() ? 1.0 : 0.6));
            if (power > 0) {
                level.explode(null, at.x, at.y, at.z, power, destroy ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE);
            }
            if (Config.bool(Config.EXPLOSION_FIREBALLS, true)) {
                throwFireballs(level, at, 3 + level.random.nextInt(3));
            }
        }
    }

    /**
     * A gas burning off steadily where it is let out (a Gas Vent's flare): flames and smoke, whatever stands in it set alight, and now
     * and then a fire nearby, but never an explosion or fireballs, whatever the gas.
     */
    public static void flare(ServerLevel level, Vec3 at, Vec3 direction) {
        if (!enabled()) {
            return;
        }
        Vec3 tip = at.add(direction.scale(0.5));
        level.sendParticles(ParticleTypes.FLAME, tip.x, tip.y, tip.z, 12, 0.15, 0.25, 0.15, 0.03);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, tip.x, tip.y + 0.6, tip.z, 2, 0.2, 0.2, 0.2, 0.01);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(1.0))) {
            entity.igniteForSeconds(4);
        }
        if (level.random.nextInt(4) == 0) {
            level.playSound(null, BlockPos.containing(at), CRRSounds.IGNITE_WHOOSH.get(), SoundSource.BLOCKS, 0.4F, 1.3F);
        }
        if (Config.bool(Config.IGNITION_SPAWNS_FIRE, true) && level.random.nextInt(8) == 0) {
            spawnFires(level, BlockPos.containing(at), 1, 1);
        }
    }

    /** Sets fire to a few random spots around, like a flint and steel: each spot drops onto the first surface that can burn. */
    private static void spawnFires(ServerLevel level, BlockPos centre, int radius, int count) {
        for (int attempt = 0; attempt < count * 4 && count > 0; attempt++) {
            BlockPos column = centre.offset(level.random.nextInt(radius * 2 + 1) - radius, 1, level.random.nextInt(radius * 2 + 1) - radius);
            for (int down = 0; down <= 3; down++) {
                BlockPos pos = column.below(down);
                if (BaseFireBlock.canBePlacedAt(level, pos, Direction.UP)) {
                    level.setBlockAndUpdate(pos, BaseFireBlock.getState(level, pos));
                    count--;
                    break;
                }
            }
        }
    }

    /** A few blaze-style fireballs flung in random directions. */
    private static void throwFireballs(ServerLevel level, Vec3 at, int count) {
        for (int i = 0; i < count; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double up = 0.2 + level.random.nextDouble() * 0.5;
            Vec3 velocity = new Vec3(Math.cos(angle) * 0.35, up * 0.35, Math.sin(angle) * 0.35);
            SmallFireball fireball = new SmallFireball(level, at.x, at.y + 0.5, at.z, velocity);
            level.addFreshEntity(fireball);
        }
    }
}
