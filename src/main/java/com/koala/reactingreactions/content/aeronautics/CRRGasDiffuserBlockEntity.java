package com.koala.reactingreactions.content.aeronautics;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;

import dev.eriksonn.aeronautics.content.blocks.hot_air.hot_air_burner.HotAirBurnerBlockEntity;
import dev.eriksonn.aeronautics.content.blocks.hot_air.lifting_gas.LiftingGasType;
import dev.eriksonn.aeronautics.index.AeroLiftingGasTypes;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import java.util.List;

/** A Hot Air Burner whose gas is whichever of helium or hydrogen fills its tank. */
public class CRRGasDiffuserBlockEntity extends HotAirBurnerBlockEntity {
    private static final int TANK_CAPACITY_MB = 4000;
    private static final int DRAIN_PER_LAZY_TICK_MB = 10;
    private static final ResourceLocation HELIUM_FLUID_ID = ResourceLocation.fromNamespaceAndPath("reactingreactions", "helium");
    private static final ResourceLocation HYDROGEN_FLUID_ID = ResourceLocation.fromNamespaceAndPath("reactingreactions", "hydrogen");

    // Set in addBehaviours, which the super constructor calls before field initialisers run.
    private SmartFluidTankBehaviour tank;

    public CRRGasDiffuserBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        tank = SmartFluidTankBehaviour.single(this, TANK_CAPACITY_MB).forbidExtraction();
        behaviours.add(tank);
    }

    public SmartFluidTankBehaviour getTank() {
        return tank;
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (this.level != null && !this.level.isClientSide && canOutputGas()) {
            FluidStack fluid = tank.getPrimaryHandler().getFluid();
            tank.getCapability().drain(new FluidStack(fluid.getFluid(), DRAIN_PER_LAZY_TICK_MB), FluidAction.EXECUTE);
        }
    }

    @Override
    public boolean canOutputGas() {
        return super.canOutputGas() && suppliedGas() != null;
    }

    @Override
    public LiftingGasType getLiftingGasType() {
        LiftingGasType supplied = suppliedGas();
        return supplied != null ? supplied : AeroLiftingGasTypes.DEFAULT_GAS.get();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        FluidStack fluidStack = tank.getPrimaryHandler().getFluid();
        LiftingGasType supplied = suppliedGas();
        Component gasName = supplied != null ? supplied.getName() : Component.translatable("reactingreactions.gas_diffuser.no_gas");
        tooltip.add(Component.literal(" - ")
                .append(gasName)
                .append(Component.literal(": " + fluidStack.getAmount() + "/" + TANK_CAPACITY_MB + "mb"))
                .withStyle(ChatFormatting.GRAY));
        return true;
    }

    /** The gas type matching whatever fluid currently sits in the tank, or null if empty/unrecognized. */
    private LiftingGasType suppliedGas() {
        FluidStack fluidStack = tank.getPrimaryHandler().getFluid();
        if (fluidStack.isEmpty()) {
            return null;
        }
        ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(fluidStack.getFluid());
        if (HELIUM_FLUID_ID.equals(fluidId)) {
            return CRRLiftingGasTypes.HELIUM.get();
        }
        if (HYDROGEN_FLUID_ID.equals(fluidId)) {
            return CRRLiftingGasTypes.HYDROGEN.get();
        }
        return null;
    }
}
