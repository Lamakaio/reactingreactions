package com.koala.reactingreactions.content.toxic;

import com.koala.reactingreactions.Config;
import com.koala.reactingreactions.registry.CRRParticles;
import com.koala.reactingreactions.registry.CRRTags;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * A small puddle of a leaked liquid on the floor: it does not flow, shrinks slowly, poisons what walks in it, burns or explodes when
 * it is flammable and something sets it off, and can be cleaned up with a cleaning agent such as Soap or drained by a Floor Drain.
 */
public class LeakPoolEntity extends Entity {
    private static final EntityDataAccessor<String> FLUID_ID = SynchedEntityData.defineId(LeakPoolEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> AMOUNT = SynchedEntityData.defineId(LeakPoolEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SHAPE_KIND = SynchedEntityData.defineId(LeakPoolEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SHAPE_SEED = SynchedEntityData.defineId(LeakPoolEntity.class, EntityDataSerializers.INT);
    public static final int MAX_AMOUNT = 1000;

    public LeakPoolEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        entityData.set(SHAPE_KIND, random.nextInt(PoolShape.Kind.values().length));
        entityData.set(SHAPE_SEED, random.nextInt());
    }

    // Client only: the drawn width, easing toward the real one so a pool spreads out instead of popping to size.
    private float shownWidth;
    private float shownWidthBefore;
    private PoolShape shape;
    private String fluidId;
    private Fluid fluid = Fluids.EMPTY;

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(FLUID_ID, "minecraft:empty");
        builder.define(AMOUNT, 0);
        builder.define(SHAPE_KIND, 0);
        builder.define(SHAPE_SEED, 0);
    }

    public Fluid getFluid() {
        // Read every tick and every frame, so only looked up again when the synced id changes.
        String id = entityData.get(FLUID_ID);
        if (!id.equals(fluidId)) {
            Fluid fluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(id));
            this.fluid = fluid == null ? Fluids.EMPTY : fluid;
            fluidId = id;
        }
        return fluid;
    }

    public int getAmount() {
        return entityData.get(AMOUNT);
    }

    /** How full the pool is, 0 to 1. */
    public float fill() {
        return getAmount() / (float) MAX_AMOUNT;
    }

    public PoolShape shape() {
        if (shape == null) {
            shape = new PoolShape(entityData.get(SHAPE_KIND), entityData.get(SHAPE_SEED));
        }
        return shape;
    }

    /** The width to draw, see {@link #shownWidth}. */
    public float shownWidth(float partialTicks) {
        return Mth.lerp(partialTicks, shownWidthBefore, shownWidth);
    }

    public void setContents(Fluid fluid, int amount) {
        entityData.set(FLUID_ID, BuiltInRegistries.FLUID.getKey(fluid).toString());
        entityData.set(AMOUNT, Math.min(MAX_AMOUNT, amount));
        refreshDimensions();
    }

    public void addAmount(int delta) {
        int amount = Math.max(0, Math.min(MAX_AMOUNT, getAmount() + delta));
        entityData.set(AMOUNT, amount);
        if (amount <= 0) {
            discard();
        }
        refreshDimensions();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (AMOUNT.equals(key)) {
            refreshDimensions();
        } else if (SHAPE_KIND.equals(key) || SHAPE_SEED.equals(key)) {
            shape = null;
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        // Its area grows with what is in it: from under half a block to nearly three across when full.
        float width = 0.4F + 2.4F * Mth.sqrt(Mth.clamp(fill(), 0, 1));
        return EntityDimensions.fixed(width, 0.1F);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(CRRTags.CLEANING_AGENTS)) {
            if (!level().isClientSide) {
                // Scrubbing it up is still handling the stuff, unless gloved.
                if (!ToxicPpe.hasGloves(player)) {
                    Toxicity.addToGauge(player, Math.min(20, getAmount() * 0.03F * Toxicity.of(getFluid()).toxicity()));
                }
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                discard();
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return InteractionResult.PASS;
    }

    /** Toxic pools bubble, more often the more toxic they are, and the worst give off fumes in their own colour. */
    private void clientEffects() {
        Fluid fluid = getFluid();
        float half = shownWidth / 2;
        if (fluid == Fluids.EMPTY || half < 0.1F) {
            return;
        }
        ToxicFluid toxic = Toxicity.of(fluid);
        var random = level().random;
        if (random.nextFloat() < 0.02F + toxic.toxicity() * 0.015F) {
            Vec3 at = randomSurfacePoint(half * 0.7F);
            level().addParticle(ParticleTypes.BUBBLE_POP, at.x, at.y, at.z, 0, 0.01, 0);
        }
        if (toxic.toxicity() >= 4 && random.nextFloat() < toxic.toxicity() * 0.008F) {
            Vec3 at = randomSurfacePoint(half * 0.6F);
            int tint = IClientFluidTypeExtensions.of(fluid).getTintColor(new FluidStack(fluid, 1000));
            level().addParticle(CRRParticles.haze(tint, 0.18F), at.x, at.y + 0.05, at.z, 0, 0.008, 0);
        }
    }

    private Vec3 randomSurfacePoint(float radius) {
        double r = Math.sqrt(level().random.nextDouble()) * radius;
        double angle = level().random.nextDouble() * Mth.TWO_PI;
        return new Vec3(getX() + Math.cos(angle) * r, getY() + 0.03, getZ() + Math.sin(angle) * r);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            shownWidthBefore = shownWidth;
            shownWidth += (getBbWidth() - shownWidth) * 0.15F;
            clientEffects();
            return;
        }
        if (!Config.toxicityEnabled() || getFluid() == Fluids.EMPTY || getAmount() <= 0) {
            discard();
            return;
        }
        ServerLevel serverLevel = (ServerLevel) level();
        ToxicFluid toxic = Toxicity.of(getFluid());
        if (tickCount % 10 == 0 && toxic.flammable() && Ignition.exposed(serverLevel, getBoundingBox())) {
            Ignition.ignite(serverLevel, position(), toxic, getAmount());
            discard();
            return;
        }
        if (tickCount % 20 == 0) {
            for (LivingEntity victim : serverLevel.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0, 0.4, 0))) {
                if (victim instanceof Player player) {
                    if (!ToxicPpe.hasBoots(player)) {
                        Toxicity.addToGauge(player, toxic.toxicity() * 0.5F);
                    }
                } else if (victim instanceof Mob && Config.bool(Config.DAMAGE_ANIMALS, true)) {
                    victim.hurt(Toxicity.damageSource(serverLevel), 1.0F);
                }
            }
            Contamination.addFrom(serverLevel, blockPosition(), toxic, toxic.toxicity() * 0.08F);
        }
        // It slowly dries up or soaks in.
        if (tickCount % 200 == 0) {
            addAmount(-2);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(FLUID_ID, tag.getString("Fluid"));
        entityData.set(AMOUNT, tag.getInt("Amount"));
        if (tag.contains("ShapeSeed")) {
            entityData.set(SHAPE_KIND, tag.getInt("ShapeKind"));
            entityData.set(SHAPE_SEED, tag.getInt("ShapeSeed"));
        }
        refreshDimensions();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putString("Fluid", entityData.get(FLUID_ID));
        tag.putInt("Amount", getAmount());
        tag.putInt("ShapeKind", entityData.get(SHAPE_KIND));
        tag.putInt("ShapeSeed", entityData.get(SHAPE_SEED));
    }
}
