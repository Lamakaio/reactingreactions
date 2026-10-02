package com.koala.reactingreactions.content.induction;

import com.koala.reactingreactions.registry.CRRBlockEntities;
import com.koala.reactingreactions.registry.CRRParticles;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/**
 * A plate of an Induction Heater. It carries Create's Blaze Burner {@code HEAT_LEVEL} property, which the heater's connector sets, so
 * Create's machines (and this mod's) read it as a heat source. It is not a fire: nothing near it counts as ignited.
 */
public class InductionHeaterPlateBlock extends Block implements IBE<InductionHeaterPlateBlockEntity> {
    public InductionHeaterPlateBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultCoil(stateDefinition.any().setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.NONE)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlazeBurnerBlock.HEAT_LEVEL, InductionHeaterCoil.NORTH, InductionHeaterCoil.EAST, InductionHeaterCoil.SOUTH, InductionHeaterCoil.WEST, InductionHeaterCoil.CIRCLE);
    }

    /** The coil segments all start off; the connector turns on the ones that make up the heater's coil. */
    static BlockState defaultCoil(BlockState state) {
        return state.setValue(InductionHeaterCoil.NORTH, false).setValue(InductionHeaterCoil.EAST, false)
                .setValue(InductionHeaterCoil.SOUTH, false).setValue(InductionHeaterCoil.WEST, false).setValue(InductionHeaterCoil.CIRCLE, false);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        heatEffects(state, level, pos, random);
    }

    /** Heat shimmer over a hot plate, and sparks off the coil once it is white-hot. */
    static void heatEffects(BlockState state, Level level, BlockPos pos, RandomSource random) {
        int heat = state.getValue(BlazeBurnerBlock.HEAT_LEVEL).ordinal();
        if (heat == 0) {
            return;
        }
        if (random.nextInt(5) < heat) {
            level.addParticle(CRRParticles.haze(0xFFE8D0, 0.04F + heat * 0.02F), pos.getX() + random.nextDouble(), pos.getY() + 1.05,
                    pos.getZ() + random.nextDouble(), 0, 0.02, 0);
        }
        if (state.getValue(BlazeBurnerBlock.HEAT_LEVEL) == BlazeBurnerBlock.HeatLevel.SEETHING && random.nextInt(6) == 0) {
            level.addParticle(ParticleTypes.ELECTRIC_SPARK, pos.getX() + random.nextDouble(), pos.getY() + 1.02, pos.getZ() + random.nextDouble(),
                    (random.nextDouble() - 0.5) * 0.1, 0.05, (random.nextDouble() - 0.5) * 0.1);
        }
    }

    @Override
    public Class<InductionHeaterPlateBlockEntity> getBlockEntityClass() {
        return InductionHeaterPlateBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends InductionHeaterPlateBlockEntity> getBlockEntityType() {
        return CRRBlockEntities.INDUCTION_HEATER_PLATE.get();
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        scald(state, entity);
        super.stepOn(level, pos, state, entity);
    }

    /** Standing on a hot coil hurts like a magma block, much more when it is superheated (sneaking and fire-proof entities are spared). */
    static void scald(BlockState state, Entity entity) {
        if (!(entity instanceof LivingEntity living) || entity.isSteppingCarefully() || entity.fireImmune()) {
            return;
        }
        float damage = switch (state.getValue(BlazeBurnerBlock.HEAT_LEVEL)) {
            case NONE, SMOULDERING -> 0.0F;
            case FADING, KINDLED -> 1.0F;
            case SEETHING -> 4.0F;
        };
        if (damage > 0) {
            living.hurt(entity.level().damageSources().hotFloor(), damage);
        }
    }
}
