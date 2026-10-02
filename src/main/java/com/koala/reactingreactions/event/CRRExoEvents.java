package com.koala.reactingreactions.event;

import com.koala.reactingreactions.Config;
import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.content.toxic.ToxicPpe;
import com.koala.reactingreactions.item.AreaMining;
import com.koala.reactingreactions.item.ExoArmorItem;
import com.koala.reactingreactions.item.FluidTankHolder;
import com.koala.reactingreactions.item.NeonBladeItem;
import com.koala.reactingreactions.item.PlasmaMultitoolItem;
import com.koala.reactingreactions.registry.CRRItems;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Holder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** The Composite Exo- armor's effects, the full set's seal, the multitool's area mining and the blade's blocking. */
public class CRRExoEvents {
    private static final ResourceLocation STEP_UP = ReactingReactions.asResource("exo_step_up");
    private static final ResourceLocation SWIM_BOOST = ReactingReactions.asResource("exo_swim_boost");
    private static final float FLYING_SPEED = 0.05F;
    private static final Map<UUID, Double> HELMET_CARRY = new HashMap<>();
    private static final Map<UUID, Double> LEGGINGS_CARRY = new HashMap<>();
    private static final Set<UUID> FLY_BOOSTED = new HashSet<>();

    private static ItemStack worn(Player player, EquipmentSlot slot, ExoArmorItem item) {
        ItemStack stack = player.getItemBySlot(slot);
        return stack.is(item) ? stack : ItemStack.EMPTY;
    }

    private static boolean fuelled(ItemStack stack) {
        return FluidTankHolder.contents(stack).getAmount() > 0;
    }

    /** All four Composite Exo- pieces and an Oxygen Mask: immune to toxicity and harmful effects. */
    public static boolean sealed(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(CRRItems.EXO_HELMET.get()) && player.getItemBySlot(EquipmentSlot.CHEST).is(CRRItems.EXO_CHESTPLATE.get())
                && player.getItemBySlot(EquipmentSlot.LEGS).is(CRRItems.EXO_LEGGINGS.get()) && player.getItemBySlot(EquipmentSlot.FEET).is(CRRItems.EXO_BOOTS.get())
                && ToxicPpe.wearsOxygenMask(player);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || player.isSpectator()) {
            return;
        }
        tickHelmet(player);
        tickLeggings(player);
        tickBoots(player);
        if (sealed(player)) {
            player.getActiveEffects().stream().filter(effect -> effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL).map(MobEffectInstance::getEffect)
                    .toList().forEach(player::removeEffect);
        }
    }

    private static void tickHelmet(Player player) {
        ItemStack helmet = worn(player, EquipmentSlot.HEAD, CRRItems.EXO_HELMET.get());
        if (!helmet.isEmpty() && CRRItems.EXO_HELMET.get().enabled(helmet, "night_vision") && fuelled(helmet)) {
            // Past 200 ticks, so the screen never flashes as it runs out.
            CRREquipmentEvents.keepEffect(player, MobEffects.NIGHT_VISION, 0, 220);
            CRREquipmentEvents.consumeFractional(HELMET_CARRY, player, Config.number(Config.EXO_HELMET_MB_PER_TICK, 0.01), helmet);
        }
    }

    private static void tickLeggings(Player player) {
        ItemStack legs = worn(player, EquipmentSlot.LEGS, CRRItems.EXO_LEGGINGS.get());
        boolean on = !legs.isEmpty() && CRRItems.EXO_LEGGINGS.get().enabled(legs, "boost") && fuelled(legs);
        boolean flying = on && player.getAbilities().flying;
        boolean elytra = on && player.isFallFlying();
        boolean swimming = on && player.isInWater();
        if (flying && !FLY_BOOSTED.contains(player.getUUID())) {
            player.getAbilities().setFlyingSpeed(FLYING_SPEED * 2);
            player.onUpdateAbilities();
            FLY_BOOSTED.add(player.getUUID());
        } else if (!flying && FLY_BOOSTED.remove(player.getUUID())) {
            player.getAbilities().setFlyingSpeed(FLYING_SPEED);
            player.onUpdateAbilities();
        }
        if (elytra) {
            Vec3 look = player.getLookAngle();
            player.setDeltaMovement(player.getDeltaMovement().add(look.scale(0.02)));
            player.hurtMarked = true;
        }
        modifier(player, Attributes.WATER_MOVEMENT_EFFICIENCY, SWIM_BOOST, 1.0, swimming);
        modifier(player, NeoForgeMod.SWIM_SPEED, SWIM_BOOST, 1.0, swimming);
        if (flying || elytra || swimming) {
            CRREquipmentEvents.consumeFractional(LEGGINGS_CARRY, player, Config.number(Config.EXO_LEGGINGS_MB_PER_TICK, 0.05), legs);
        }
    }

    private static void tickBoots(Player player) {
        ItemStack boots = worn(player, EquipmentSlot.FEET, CRRItems.EXO_BOOTS.get());
        modifier(player, Attributes.STEP_HEIGHT, STEP_UP, 0.5, !boots.isEmpty() && CRRItems.EXO_BOOTS.get().enabled(boots, "step_up") && fuelled(boots));
    }

    private static void modifier(Player player, Holder<Attribute> attribute, ResourceLocation id, double amount, boolean on) {
        var instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        if (on && !instance.hasModifier(id)) {
            instance.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
        } else if (!on && instance.hasModifier(id)) {
            instance.removeModifier(id);
        }
    }

    /** The boots take the whole fall if their oil covers it. */
    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide || event.getDistance() <= 3) {
            return;
        }
        ItemStack boots = worn(player, EquipmentSlot.FEET, CRRItems.EXO_BOOTS.get());
        int cost = (int) Math.ceil((event.getDistance() - 3) * Config.number(Config.EXO_BOOTS_MB_PER_BLOCK, 2.0));
        if (!boots.isEmpty() && FluidTankHolder.contents(boots).getAmount() >= cost) {
            FluidTankHolder.take(boots, cost);
            event.setDamageMultiplier(0);
        }
    }

    @SubscribeEvent
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (event.getEntity() instanceof Player player && event.getEffectInstance().getEffect().value().getCategory() == MobEffectCategory.HARMFUL && sealed(player)) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player && player.getMainHandItem().getItem() instanceof PlasmaMultitoolItem) {
            AreaMining.breakExtra(player, player.getMainHandItem(), event.getPos());
        }
    }

    /** Pickup mode: drops go straight into the inventory. */
    @SubscribeEvent
    public static void onDrops(BlockDropsEvent event) {
        ItemStack tool = event.getTool();
        if (!(event.getBreaker() instanceof Player player) || !(tool.getItem() instanceof PlasmaMultitoolItem item) || !item.enabled(tool, "pickup")) {
            return;
        }
        event.getDrops().removeIf(drop -> {
            player.getInventory().add(drop.getItem());
            return drop.getItem().isEmpty();
        });
    }

    /** A lit Neon Blade held up turns projectiles back, if it faces them. */
    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getRayTraceResult() instanceof EntityHitResult hit) || !(hit.getEntity() instanceof Player player) || !player.isUsingItem()) {
            return;
        }
        ItemStack blade = player.getUseItem();
        Projectile projectile = event.getProjectile();
        if (!(blade.getItem() instanceof NeonBladeItem) || !NeonBladeItem.lit(blade)
                || player.getLookAngle().dot(projectile.getDeltaMovement().normalize()) > -0.3) {
            return;
        }
        event.setCanceled(true);
        projectile.deflect(ProjectileDeflection.REVERSE, player, player, true);
        if (!player.isCreative()) {
            NeonBladeItem.spend(player, blade, 2);
        }
    }
}
