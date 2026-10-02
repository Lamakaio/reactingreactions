package com.koala.reactingreactions.event;

import com.koala.reactingreactions.Config;
import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.content.compat.WornAccessories;
import com.koala.reactingreactions.content.laser.LaserTargets;
import com.koala.reactingreactions.content.toxic.ToxicPpe;
import com.koala.reactingreactions.item.ExoArmorItem;
import com.koala.reactingreactions.item.FluidTankHolder;
import com.koala.reactingreactions.network.CRRLaserPayload;
import com.koala.reactingreactions.registry.CRRItems;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Buffs from worn or held gear: titanium armour and tools, the masks, and the flight pack's fuel. */
public class CRREquipmentEvents {
    private static final ResourceLocation TITANIUM_ATTACK_SPEED_ID =
            ReactingReactions.asResource("titanium_sword_attack_speed");
    // Each player's fractional flight fuel, see #consumeFractional.
    private static final Map<UUID, Double> AEROZINE_FLIGHT_CARRY = new HashMap<>();
    // Saved on the player, so flight the chestplate gave is still taken back after a restart.
    private static final String FLIGHT_GRANTED = ReactingReactions.MODID + ":exo_flight";

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || player.isRemoved()) {
            return;
        }

        applyTitaniumSetBonus(player);

        ItemStack mainhand = player.getMainHandItem();
        if (isTitaniumMiningTool(mainhand)) {
            keepEffect(player, MobEffects.DIG_SPEED, 0, 60);
        }

        applyTitaniumSwordAttackSpeed(player, mainhand);
        tickAerozineFlight(player);
        tickOxygenMaskWaterBreathing(player);
        tickLaserPointer(player);
    }

    /** While a Laser Pointer is held, sends where it points to nearby players and to the pets that chase the dot. */
    private static void tickLaserPointer(Player player) {
        boolean holding = player.getMainHandItem().is(CRRItems.LASER_POINTER.get()) || player.getOffhandItem().is(CRRItems.LASER_POINTER.get());
        if (!holding) {
            LaserTargets.clear(player.getUUID());
            return;
        }
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(LaserTargets.RANGE));
        var hit = player.level().clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE, player));
        Vec3 target = hit.getType() == HitResult.Type.MISS ? end : hit.getLocation();
        var level = (ServerLevel) player.level();
        LaserTargets.update(player.getUUID(), level, target);
        // AndSelf: a player does not track themselves.
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                new CRRLaserPayload(player.getUUID(), target.x, target.y, target.z));
    }

    // Per titanium or Composite Exo- piece worn: Speed I, then Regeneration I, Speed II, Resistance I.
    private static void applyTitaniumSetBonus(Player player) {
        int pieces = 0;
        for (EquipmentSlot slot : new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            if (isTitaniumArmorPiece(player.getItemBySlot(slot))) {
                pieces++;
            }
        }
        if (pieces <= 0) {
            return;
        }
        int speedAmplifier = pieces >= 3 ? 1 : 0;
        keepEffect(player, MobEffects.MOVEMENT_SPEED, speedAmplifier, 60);
        if (pieces >= 2) {
            keepEffect(player, MobEffects.REGENERATION, 0, 60);
        }
        if (pieces >= 4) {
            keepEffect(player, MobEffects.DAMAGE_RESISTANCE, 0, 60);
        }
    }

    private static boolean isTitaniumArmorPiece(ItemStack stack) {
        return stack.is(CRRItems.TITANIUM_HELMET.get()) || stack.is(CRRItems.TITANIUM_CHESTPLATE.get())
                || stack.is(CRRItems.TITANIUM_LEGGINGS.get()) || stack.is(CRRItems.TITANIUM_BOOTS.get())
                || stack.getItem() instanceof ExoArmorItem;
    }

    private static boolean isTitaniumMiningTool(ItemStack stack) {
        return stack.is(CRRItems.TITANIUM_PICKAXE.get()) || stack.is(CRRItems.TITANIUM_AXE.get())
                || stack.is(CRRItems.TITANIUM_SHOVEL.get()) || stack.is(CRRItems.TITANIUM_HOE.get());
    }

    private static void applyTitaniumSwordAttackSpeed(Player player, ItemStack mainhand) {
        AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed == null) {
            return;
        }
        boolean holdingTitaniumSword = mainhand.is(CRRItems.TITANIUM_SWORD.get());
        boolean hasModifier = attackSpeed.getModifier(TITANIUM_ATTACK_SPEED_ID) != null;
        if (holdingTitaniumSword && !hasModifier) {
            attackSpeed.addTransientModifier(new AttributeModifier(TITANIUM_ATTACK_SPEED_ID, 0.5D, AttributeModifier.Operation.ADD_VALUE));
        } else if (!holdingTitaniumSword && hasModifier) {
            attackSpeed.removeModifier(TITANIUM_ATTACK_SPEED_ID);
        }
    }

    private static void tickAerozineFlight(Player player) {
        if (player.isCreative() || player.isSpectator()) {
            player.getPersistentData().remove(FLIGHT_GRANTED);
            AEROZINE_FLIGHT_CARRY.remove(player.getUUID());
            return;
        }
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        var abilities = player.getAbilities();
        boolean canFly = chest.is(CRRItems.EXO_CHESTPLATE.get()) && CRRItems.EXO_CHESTPLATE.get().enabled(chest, "flight")
                && FluidTankHolder.contents(chest).getAmount() > 0;
        if (canFly) {
            if (!abilities.mayfly) {
                abilities.mayfly = true;
                player.getPersistentData().putBoolean(FLIGHT_GRANTED, true);
                player.onUpdateAbilities();
            }
            if (abilities.flying) {
                consumeFractional(AEROZINE_FLIGHT_CARRY, player, Config.number(Config.EXO_CHESTPLATE_MB_PER_TICK, 0.11), chest);
                return;
            }
        } else if (player.getPersistentData().getBoolean(FLIGHT_GRANTED)) {
            player.getPersistentData().remove(FLIGHT_GRANTED);
            // Only take back flight the chestplate gave.
            abilities.mayfly = false;
            abilities.flying = false;
            player.onUpdateAbilities();
        }
        AEROZINE_FLIGHT_CARRY.remove(player.getUUID());
    }

    /** Water breathing while submerged, from a worn Create backtank. The toxic-air half is in {@link #onMobEffectApplicable}. */
    private static void tickOxygenMaskWaterBreathing(Player player) {
        if (player.isEyeInFluid(FluidTags.WATER) && ToxicPpe.breathesSuppliedAir(player)) {
            keepEffect(player, MobEffects.WATER_BREATHING, 0, 60);
        }
    }

    /**
     * Keeps a gear effect on. Re-adding an effect every tick resends it to the client each time, so it is only refreshed once
     * it has run down by a second.
     */
    public static void keepEffect(Player player, Holder<MobEffect> effect, int amplifier, int duration) {
        MobEffectInstance current = player.getEffect(effect);
        if (current == null || current.getAmplifier() < amplifier || current.getDuration() <= duration - 20) {
            player.addEffect(new MobEffectInstance(effect, duration, amplifier, true, false, true));
        }
    }

    /** Takes a possibly fractional amount, carrying the fraction until a whole millibucket is due. Returns whether one was taken. */
    public static boolean consumeFractional(Map<UUID, Double> carry, Player player, double ratePerCall, ItemStack stack) {
        // An empty tank must not build up a backlog that a refill would then pay all at once.
        if (FluidTankHolder.contents(stack).isEmpty()) {
            carry.remove(player.getUUID());
            return false;
        }
        double accumulated = carry.merge(player.getUUID(), ratePerCall, Double::sum);
        int whole = (int) Math.floor(accumulated);
        if (whole <= 0) {
            return false;
        }
        int taken = FluidTankHolder.take(stack, whole);
        carry.put(player.getUUID(), accumulated - whole);
        return taken > 0;
    }

    @SubscribeEvent
    public static void onMobEffectApplicable(MobEffectEvent.Applicable event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        var effect = event.getEffectInstance().getEffect();
        if (!effect.is(MobEffects.POISON) && !effect.is(MobEffects.WITHER)) {
            return;
        }
        if (WornAccessories.isEquipped(player, CRRItems.GAS_MASK.get())) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
            return;
        }
        // The Oxygen Mask gives the same immunity while a worn backtank has air: this Poison/Wither is the "bad air" signal.
        if (ToxicPpe.breathesSuppliedAir(player)) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

}
