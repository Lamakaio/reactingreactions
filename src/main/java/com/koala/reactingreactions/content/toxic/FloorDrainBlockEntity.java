package com.koala.reactingreactions.content.toxic;

import com.koala.reactingreactions.Config;
import com.koala.reactingreactions.registry.CRRSounds;
import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.fluids.particle.FluidParticleData;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.List;

/** Absorbs any pool in an area around it (radius set in the config) into its tank, which pipes and pumps can empty. */
public class FloorDrainBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    private static final int CAPACITY_MB = 8000;
    private SmartFluidTankBehaviour tank;

    public FloorDrainBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event, BlockEntityType<FloorDrainBlockEntity> type) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, type, (be, ctx) -> be.tank.getCapability());
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        tank = new SmartFluidTankBehaviour(SmartFluidTankBehaviour.OUTPUT, this, 1, CAPACITY_MB, false).forbidInsertion();
        behaviours.add(tank);
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide || level.getGameTime() % 10 != 0) {
            return;
        }
        for (LeakPoolEntity pool : level.getEntitiesOfClass(LeakPoolEntity.class, new AABB(worldPosition).inflate(Config.number(Config.FLOOR_DRAIN_RADIUS, 6), 3, Config.number(Config.FLOOR_DRAIN_RADIUS, 6)))) {
            FluidStack offered = new FluidStack(pool.getFluid(), pool.getAmount());
            // Filled directly on the tank: the capability is extraction-only from outside.
            int taken = tank.getPrimaryHandler().fill(offered, IFluidHandler.FluidAction.EXECUTE);
            if (taken > 0) {
                drainEffects((ServerLevel) level, pool, taken);
                pool.addAmount(-taken);
            }
        }
    }

    /** The pool runs to the drain and swirls into its grate, with a gurgle. */
    private void drainEffects(ServerLevel level, LeakPoolEntity pool, int taken) {
        var particle = new FluidParticleData(AllParticleTypes.FLUID_PARTICLE.get(), new FluidStack(pool.getFluid(), 1000));
        Vec3 grate = new Vec3(worldPosition.getX() + 0.5, worldPosition.getY() + (getBlockState().getValue(BlockStateProperties.HALF) == Half.TOP ? 1.02 : 0.2),
                worldPosition.getZ() + 0.5);
        var random = level.random;
        for (int i = 0; i < Math.min(8, 2 + taken / 50); i++) {
            Vec3 from = pool.position().add((random.nextDouble() - 0.5) * pool.getBbWidth(), 0.05, (random.nextDouble() - 0.5) * pool.getBbWidth());
            Vec3 toward = grate.subtract(from).normalize().scale(0.15);
            level.sendParticles(particle, from.x, from.y, from.z, 0, toward.x, 0.05, toward.z, 1);
        }
        for (int i = 0; i < 6; i++) {
            double angle = i * Math.PI / 3 + level.getGameTime() * 0.4;
            Vec3 at = grate.add(Math.cos(angle) * 0.35, 0, Math.sin(angle) * 0.35);
            // Along the circle and a little inward: a whirlpool.
            level.sendParticles(particle, at.x, at.y, at.z, 0, -Math.sin(angle) * 0.08 - Math.cos(angle) * 0.03, 0.02, Math.cos(angle) * 0.08 - Math.sin(angle) * 0.03, 1);
        }
        level.playSound(null, worldPosition, CRRSounds.POOL_SPLASH.get(), SoundSource.BLOCKS, 0.4F, 0.55F + random.nextFloat() * 0.1F);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        LeakInfo.append(tooltip, this);
        FluidStack stored = tank.getPrimaryHandler().getFluid();
        tooltip.add(Component.literal(" - Floor Drain: " + stored.getAmount() + "/" + CAPACITY_MB + " mb"
                + (stored.isEmpty() ? "" : " " + stored.getHoverName().getString())).withStyle(ChatFormatting.GRAY));
        return true;
    }
}
