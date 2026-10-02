package com.koala.reactingreactions.content.toxic;

import com.koala.reactingreactions.Config;
import com.koala.reactingreactions.content.drill.RichOreVeinBlock;
import com.koala.reactingreactions.network.CRRContaminationPayload;
import com.koala.reactingreactions.registry.CRRAdvancements;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Ticks contamination and each player's toxicity gauge. Every handler returns at once while the config flag is off. */
public class ToxicityEvents {
    /** How many seconds of exposure one Carbon Filter lasts. */
    private static final int FILTER_LIFETIME_SECONDS = 360;
    private static final float SLEEP_RELIEF = 25.0F;
    private static final Map<UUID, Integer> FILTER_EXPOSURE = new ConcurrentHashMap<>();
    private static final Set<UUID> SENT_NON_EMPTY = ConcurrentHashMap.newKeySet();

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            Vents.tick(level);
            LeakDrips.tick(level);
            if (level.getGameTime() % 20 == 0 && Config.toxicityEnabled()) {
                Contamination.tick(level);
            }
        }
    }

    /** Sends each player the contaminated cells around them (and one empty update when it clears). */
    @SubscribeEvent
    public static void onPlayerSync(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().getGameTime() % 10 != 0 || !Config.toxicityEnabled()) {
            return;
        }
        var cells = Contamination.nearby(player.level(), player.blockPosition(), 12, 48);
        boolean had = SENT_NON_EMPTY.contains(player.getUUID());
        if (cells.isEmpty() && !had) {
            return;
        }
        if (cells.isEmpty()) {
            SENT_NON_EMPTY.remove(player.getUUID());
        } else {
            SENT_NON_EMPTY.add(player.getUUID());
        }
        List<Long> positions = new ArrayList<>();
        List<Float> levels = new ArrayList<>();
        for (var cell : cells) {
            positions.add(cell.getKey().asLong());
            levels.add(cell.getValue());
        }
        PacketDistributor.sendToPlayer(player, new CRRContaminationPayload(positions, levels));
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        Contamination.clearAll();
        Vents.clearAll();
        LeakDrips.clearAll();
        Scrubbers.clearAll();
        FILTER_EXPOSURE.clear();
        SENT_NON_EMPTY.clear();
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player && event.getState().getBlock() instanceof RichOreVeinBlock) {
            CRRAdvancements.grant(player, "rich_vein");
        }
    }

    @SubscribeEvent
    public static void onWakeUp(PlayerWakeUpEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer && Config.toxicityEnabled() && player.getSleepTimer() >= 100) {
            Toxicity.setGauge(player, Toxicity.gauge(player) - SLEEP_RELIEF);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().getGameTime() % 20 != 0
                || !Config.toxicityEnabled() || !Config.bool(Config.GAUGE_ENABLED, true) || player.isCreative() || player.isSpectator()) {
            return;
        }
        float delta = -(float) Config.number(Config.GAUGE_DECAY_PER_MINUTE, 1.0) / 60.0F;

        // Handling toxic compounds: filled buckets and toxic solids in the inventory (gloves make it safe).
        if (player.level().getGameTime() % 40 == 0 && !ToxicPpe.hasGloves(player)) {
            float carried = 0;
            for (ItemStack stack : player.getInventory().items) {
                carried += Toxicity.carriedToxicity(stack) * stack.getCount();
            }
            carried += Toxicity.carriedToxicity(player.getOffhandItem());
            delta += Math.min(carried, 60) * 0.012F;
        }

        // Breathing contaminated air (a working mask filters most of it, and uses its filter up; supplied air blocks all of it).
        float air = Math.max(Contamination.levelAt(player.level(), player.blockPosition()), Contamination.levelAt(player.level(), player.blockPosition().above()));
        if (air > 0.2F && !ToxicPpe.breathesSuppliedAir(player)) {
            boolean masked = ToxicPpe.hasWorkingMask(player);
            delta += Math.min(air, 6.0F) * (masked ? 0.03F : 0.35F);
            if (masked && Config.bool(Config.FILTERS_CONSUMED, true) && FILTER_EXPOSURE.merge(player.getUUID(), 1, Integer::sum) >= FILTER_LIFETIME_SECONDS) {
                FILTER_EXPOSURE.remove(player.getUUID());
                ItemStack filter = ToxicPpe.findFilter(player);
                if (filter != null) {
                    filter.shrink(1);
                }
            }
        }

        float gauge = Toxicity.gauge(player);
        Toxicity.setGauge(player, gauge + delta);
        if (Toxicity.gauge(player) > 1.0F) {
            CRRAdvancements.grant(player, "bad_air");
        }
        applyEffects(player, Toxicity.gauge(player));
    }

    private static void applyEffects(ServerPlayer player, float gauge) {
        if (gauge >= 25) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0, true, true, true));
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 60, 0, true, true, true));
        }
        if (gauge >= 50) {
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0, true, true, true));
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, true, true, true));
        }
        if (gauge >= 75 && player.level().getGameTime() % 60 == 0) {
            player.hurt(Toxicity.damageSource(player.level()), 1.0F);
        }
        if (gauge >= Toxicity.GAUGE_MAX && Config.bool(Config.LETHAL_AT_MAX, true)) {
            player.hurt(Toxicity.damageSource(player.level()), 4.0F);
        }
    }
}
