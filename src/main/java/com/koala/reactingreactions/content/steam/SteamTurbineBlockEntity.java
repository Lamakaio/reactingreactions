package com.koala.reactingreactions.content.steam;

import com.koala.reactingreactions.Config;
import com.koala.reactingreactions.registry.CRRFluids;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.List;

/** Turns steam from its tank into rotation; the stress capacity comes from {@code BlockStressValues}. */
public class SteamTurbineBlockEntity extends GeneratingKineticBlockEntity {
    private SmartFluidTankBehaviour tank;
    private IFluidHandler steamOnlyCapability;
    private float generatedSpeed;

    public SteamTurbineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event, BlockEntityType<SteamTurbineBlockEntity> type) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, type, (be, ctx) -> be.steamOnlyCapability);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        tank = new SmartFluidTankBehaviour(SmartFluidTankBehaviour.TYPE, this, 1,
                Config.number(Config.STEAM_TURBINE_TANK_CAPACITY_MB, 4000), false);
        behaviours.add(tank);
        steamOnlyCapability = new SteamOnlyFluidHandler(tank.getCapability());
    }

    @Override
    public float getGeneratedSpeed() {
        return generatedSpeed;
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) {
            return;
        }
        int target = Config.number(Config.STEAM_TURBINE_MB_PER_TICK, 20);
        FluidStack drained = target > 0
                ? tank.getPrimaryHandler().drain(target, IFluidHandler.FluidAction.EXECUTE)
                : FluidStack.EMPTY;
        float fraction = target > 0 ? Math.min(1.0F, drained.getAmount() / (float) target) : 0.0F;
        float wanted = fraction * Config.number(Config.STEAM_TURBINE_MAX_RPM, 64);
        if (wanted != generatedSpeed) {
            generatedSpeed = wanted;
            updateGeneratedRotation();
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        FluidStack stored = tank.getPrimaryHandler().getFluid();
        int capacity = Config.number(Config.STEAM_TURBINE_TANK_CAPACITY_MB, 4000);
        tooltip.add(Component.literal(" - Steam Turbine: " + stored.getAmount() + "/" + capacity + " mb")
                .withStyle(ChatFormatting.GRAY));
        return true;
    }

    /** Only accepts steam. */
    private static final class SteamOnlyFluidHandler implements IFluidHandler {
        private final IFluidHandler delegate;

        private SteamOnlyFluidHandler(IFluidHandler delegate) {
            this.delegate = delegate;
        }

        private static boolean isSteam(FluidStack stack) {
            return stack.getFluid().isSame(CRRFluids.STEAM.get().getSource());
        }

        @Override
        public int getTanks() {
            return delegate.getTanks();
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return delegate.getFluidInTank(tank);
        }

        @Override
        public int getTankCapacity(int tank) {
            return delegate.getTankCapacity(tank);
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return isSteam(stack) && delegate.isFluidValid(tank, stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return isSteam(resource) ? delegate.fill(resource, action) : 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return delegate.drain(resource, action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return delegate.drain(maxDrain, action);
        }
    }
}
