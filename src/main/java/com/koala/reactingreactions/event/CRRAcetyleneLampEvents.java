package com.koala.reactingreactions.event;

import com.koala.reactingreactions.item.AcetyleneLampItem;
import com.koala.reactingreactions.registry.CRRItems;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The Acetylene Lamp: a held lamp lights its surroundings through an invisible light block that follows the player, and
 * a supercharged one ignites nearby undead and stops hostile spawns around the holder.
 */
public class CRRAcetyleneLampEvents {
    private static final int LAMP_LIGHT_LEVEL = 15;
    private static final int SUPERCHARGE_IGNITE_RADIUS = 12;
    private static final int SUPERCHARGE_NO_SPAWN_RADIUS = 30;

    /** Tracks the invisible light block each player's held lamp is currently projecting, so it can follow/be removed. */
    private static final Map<UUID, BlockPos> TRACKED_LIGHT_POS = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        ItemStack lampStack = heldLamp(player);
        if (lampStack == null) {
            removeTrackedLight(level, player.getUUID());
            return;
        }

        updateTrackedLight(level, player);

        if (AcetyleneLampItem.isSupercharged(lampStack, level.getGameTime()) && level.getGameTime() % 20 == 0) {
            igniteNearbyUndead(level, player.blockPosition());
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity().level() instanceof ServerLevel level) {
            removeTrackedLight(level, event.getEntity().getUUID());
        }
    }

    private static ItemStack heldLamp(Player player) {
        if (player.getMainHandItem().is(CRRItems.ACETYLENE_LAMP.get())) {
            return player.getMainHandItem();
        }
        if (player.getOffhandItem().is(CRRItems.ACETYLENE_LAMP.get())) {
            return player.getOffhandItem();
        }
        return null;
    }

    private static void updateTrackedLight(ServerLevel level, Player player) {
        BlockPos desired = player.blockPosition();
        BlockPos previous = TRACKED_LIGHT_POS.get(player.getUUID());
        if (desired.equals(previous)) {
            return;
        }
        if (previous != null) {
            clearIfOurLight(level, previous);
        }
        if (level.getBlockState(desired).isAir()) {
            level.setBlock(desired, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, LAMP_LIGHT_LEVEL), 3);
            TRACKED_LIGHT_POS.put(player.getUUID(), desired);
        } else {
            TRACKED_LIGHT_POS.remove(player.getUUID());
        }
    }

    private static void removeTrackedLight(ServerLevel level, UUID playerId) {
        BlockPos pos = TRACKED_LIGHT_POS.remove(playerId);
        if (pos != null) {
            clearIfOurLight(level, pos);
        }
    }

    private static void clearIfOurLight(ServerLevel level, BlockPos pos) {
        if (level.getBlockState(pos).is(Blocks.LIGHT)) {
            level.removeBlock(pos, false);
        }
    }

    private static void igniteNearbyUndead(ServerLevel level, BlockPos center) {
        var box = new AABB(center).inflate(SUPERCHARGE_IGNITE_RADIUS);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e.getType().is(EntityTypeTags.UNDEAD) && !e.fireImmune())) {
            entity.igniteForSeconds(6.0F);
        }
    }

    @SubscribeEvent
    public static void onSpawnPlacementCheck(MobSpawnEvent.SpawnPlacementCheck event) {
        if (event.getEntityType().getCategory() != MobCategory.MONSTER || !event.getDefaultResult()) {
            return;
        }
        ServerLevelAccessor level = event.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (isNearSuperchargedLamp(serverLevel, event.getPos())) {
            event.setResult(MobSpawnEvent.SpawnPlacementCheck.Result.FAIL);
        }
    }

    private static boolean isNearSuperchargedLamp(ServerLevel level, BlockPos pos) {
        var box = new AABB(pos).inflate(SUPERCHARGE_NO_SPAWN_RADIUS);
        for (Player player : level.getEntitiesOfClass(Player.class, box, p -> true)) {
            ItemStack lamp = heldLamp(player);
            if (lamp != null && AcetyleneLampItem.isSupercharged(lamp, level.getGameTime())) {
                return true;
            }
        }
        return false;
    }
}
