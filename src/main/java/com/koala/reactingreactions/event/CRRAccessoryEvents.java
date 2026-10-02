package com.koala.reactingreactions.event;

import com.koala.reactingreactions.Config;
import com.koala.reactingreactions.content.compat.WornAccessories;
import com.koala.reactingreactions.item.FluidTankHolder;
import com.koala.reactingreactions.registry.CRRItems;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Effects of the accessories worn in accessory slots (Accessories or Curios), including the thrusters' double jump. */
public class CRRAccessoryEvents {
    private static final int DOUBLE_JUMP_COOLDOWN_TICKS = 60;
    private static final double DOUBLE_JUMP_STRENGTH = 0.7D;
    private static final Map<UUID, Integer> DOUBLE_JUMP_COOLDOWNS = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        // Cheap checks first: the slot lookups go through the slot mod.
        if (player.isOnFire() && WornAccessories.isEquipped(player, CRRItems.FLAME_RETARDANT_CLOAK.get())) {
            player.clearFire();
        }
        // The effects last 60 ticks, so a refresh every 10 keeps them on.
        if (player.tickCount % 10 == 0) {
            refreshEffects(player);
        }

        DOUBLE_JUMP_COOLDOWNS.computeIfPresent(player.getUUID(), (id, ticksLeft) -> ticksLeft > 0 ? ticksLeft - 1 : null);
    }

    private static void refreshEffects(Player player) {
        if (WornAccessories.isEquipped(player, CRRItems.SPRING_BOOTS.get())) {
            CRREquipmentEvents.keepEffect(player, MobEffects.JUMP, 1, 60);
        }
        if (player.isSprinting() && WornAccessories.isEquipped(player, CRRItems.RACING_ANKLET.get())) {
            CRREquipmentEvents.keepEffect(player, MobEffects.MOVEMENT_SPEED, 1, 60);
        }
        if (WornAccessories.isEquipped(player, CRRItems.DIVING_FINS.get())) {
            CRREquipmentEvents.keepEffect(player, MobEffects.WATER_BREATHING, 0, 60);
            if (player.isInWater()) {
                CRREquipmentEvents.keepEffect(player, MobEffects.DOLPHINS_GRACE, 0, 60);
            }
        }
        if (WornAccessories.isEquipped(player, CRRItems.HELIUM_LOCKET.get())) {
            CRREquipmentEvents.keepEffect(player, MobEffects.SLOW_FALLING, 0, 60);
        }
        if (WornAccessories.isEquipped(player, CRRItems.DIGGING_RING.get())) {
            CRREquipmentEvents.keepEffect(player, MobEffects.DIG_SPEED, 1, 60);
        }
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player player) || player.level().isClientSide()) {
            return;
        }
        if (!event.getContainer().getSource().is(DamageTypeTags.IS_FIRE)) {
            return;
        }
        if (WornAccessories.isEquipped(player, CRRItems.FLAME_RETARDANT_CLOAK.get())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onKnockback(LivingKnockBackEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player player) || player.level().isClientSide()) {
            return;
        }
        if (WornAccessories.isEquipped(player, CRRItems.ANCHOR_CHARM.get())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        if (WornAccessories.isEquipped(player, CRRItems.MAGNESIUM_KNUCKLE.get())) {
            event.getTarget().igniteForSeconds(4.0F);
        }
    }

    /** Called from the server-bound {@code CRRDoubleJumpPayload} handler. */
    public static void handleDoubleJumpRequest(Player player) {
        if (player.level().isClientSide() || player.onGround() || player.isSpectator()) {
            return;
        }
        int cooldown = DOUBLE_JUMP_COOLDOWNS.getOrDefault(player.getUUID(), 0);
        if (cooldown > 0) {
            return;
        }
        WornAccessories.Worn thrusters = WornAccessories.find(player, CRRItems.AEROZINE_THRUSTERS.get());
        if (thrusters == null) {
            return;
        }
        ItemStack stack = thrusters.stack();
        int cost = Config.number(Config.AEROZINE_THRUSTERS_MB_PER_JUMP, 1);
        if (FluidTankHolder.contents(stack).getAmount() < cost) {
            return;
        }
        FluidTankHolder.take(stack, cost);
        thrusters.save();
        player.setDeltaMovement(player.getDeltaMovement().x, DOUBLE_JUMP_STRENGTH, player.getDeltaMovement().z);
        player.hasImpulse = true;
        // A player moves client-side: this sends the new motion back, as knockback does.
        player.hurtMarked = true;
        player.fallDistance = 0.0F;
        DOUBLE_JUMP_COOLDOWNS.put(player.getUUID(), DOUBLE_JUMP_COOLDOWN_TICKS);
        thrusterBurst(player);
    }

    /** Flame and smoke out of the nozzles at the bottom of the worn thrusters, seen by everyone nearby. */
    private static void thrusterBurst(Player player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        Vec3 back = Vec3.directionFromRotation(0, player.yBodyRot).scale(-0.3);
        double x = player.getX() + back.x;
        double y = player.getY() + 0.7;
        double z = player.getZ() + back.z;
        level.sendParticles(ParticleTypes.FLAME, x, y, z, 12, 0.12, 0.05, 0.12, 0.02);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y - 0.2, z, 4, 0.15, 0.05, 0.15, 0.01);
        level.playSound(null, x, y, z, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.4F, 1.6F);
    }
}
