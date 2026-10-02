package com.koala.reactingreactions.content.explosive;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * ANFO blasting charge: a mining explosive. Fire does not set it off (real ANFO needs a detonator), only a redstone signal does.
 * The blast breaks blocks like TNT with every block dropping, but hurts and pushes no entity.
 */
public class AnfoChargeBlock extends Block {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final int FUSE_TICKS = 80;
    public static final float POWER = 5.0F;

    /** Blocks only: no entity damage or knockback. */
    private static final ExplosionDamageCalculator BLOCKS_ONLY = new ExplosionDamageCalculator() {
        @Override
        public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
            return false;
        }

        @Override
        public float getKnockbackMultiplier(Entity entity) {
            return 0.0F;
        }
    };

    public AnfoChargeBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState();
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!oldState.is(state.getBlock())) {
            tryPrime(state, level, pos);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, BlockPos neighborPos, boolean movedByPiston) {
        tryPrime(state, level, pos);
    }

    private void tryPrime(BlockState state, Level level, BlockPos pos) {
        if (level.isClientSide || state.getValue(LIT) || !level.hasNeighborSignal(pos)) {
            return;
        }
        level.setBlock(pos, state.setValue(LIT, true), Block.UPDATE_ALL);
        level.playSound(null, pos, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 0.8F);
        level.scheduleTick(pos, this, FUSE_TICKS);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }
        level.removeBlock(pos, false);
        // TNT interaction: its drop-decay game rule is off by default, so every broken block drops.
        level.explode(null, null, BLOCKS_ONLY, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, POWER, false, Level.ExplosionInteraction.TNT);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT)) {
            level.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5, 0, 0.05, 0);
        }
    }

    @Override
    public boolean dropFromExplosion(Explosion explosion) {
        // Caught in another blast it is torn apart, not set off.
        return false;
    }
}
