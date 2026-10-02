package com.koala.reactingreactions.content.toxic;

import com.koala.reactingreactions.Config;
import com.koala.reactingreactions.registry.CRRSounds;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.List;

/**
 * Takes gases from a pipe at its back (opposite its outlet) and lets them out, a second's worth at a time: a steady spray and contamination
 * like a leak's (scaled by {@code gasVentPollution}, and caught by a working scrubber). A flammable gas meeting a flame at the outlet
 * burns off like a flare instead of polluting, without exploding.
 */
public class GasVentBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    private static final int CAPACITY_MB = 1000;
    private SmartFluidTankBehaviour tank;
    /** The gas let out in the last second, for the spray between releases. */
    private FluidStack lastVented = FluidStack.EMPTY;

    public GasVentBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event, BlockEntityType<GasVentBlockEntity> type) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, type,
                (be, side) -> side == be.outlet().getOpposite() ? be.tank.getCapability() : null);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        tank = new SmartFluidTankBehaviour(SmartFluidTankBehaviour.INPUT, this, 1, CAPACITY_MB, false).forbidExtraction();
        tank.getPrimaryHandler().setValidator(fluid -> Toxicity.isGas(fluid.getFluid()));
        behaviours.add(tank);
    }

    private Direction outlet() {
        return getBlockState().getValue(GasVentBlock.FACING);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        Direction outlet = outlet();
        // Under the rain cap, where the stack's mouth is.
        Vec3 nozzle = Vec3.atCenterOf(worldPosition).add(Vec3.atLowerCornerOf(outlet.getNormal()).scale(0.3));
        if (!lastVented.isEmpty()) {
            Vents.spray(serverLevel, nozzle, Vec3.atLowerCornerOf(outlet.getNormal()), Leaks.colorOf(lastVented.getFluid()),
                    Math.min(1, lastVented.getAmount() / 250F), level.getGameTime() % 4 == 0);
        }
        if (level.getGameTime() % 20 != 0) {
            return;
        }
        FluidStack vented = tank.getPrimaryHandler().drain(Config.number(Config.GAS_VENT_RATE_MB, 500), IFluidHandler.FluidAction.EXECUTE);
        lastVented = vented;
        if (vented.isEmpty()) {
            return;
        }
        release(serverLevel, worldPosition.relative(outlet), outlet, vented);
        if (level.getGameTime() % 60 == 0) {
            level.playSound(null, worldPosition, CRRSounds.GAS_HISS.get(), SoundSource.BLOCKS, 0.3F, 0.8F + level.random.nextFloat() * 0.2F);
        }
    }

    /** What a leak of the same gas would do at {@code into}, its contamination scaled down by the config. */
    private static void release(ServerLevel level, BlockPos into, Direction outlet, FluidStack vented) {
        if (!Config.toxicityEnabled()) {
            return;
        }
        ToxicFluid toxic = Toxicity.of(vented);
        if (toxic.flammable() && Ignition.exposed(level, new AABB(into))) {
            // Burnt off as it comes out, like a flare: no pollution, and no explosion even for explosive gases.
            Ignition.flare(level, Vec3.atCenterOf(into), Vec3.atLowerCornerOf(outlet.getNormal()));
            return;
        }
        if (!Scrubbers.covers(level, into)) {
            Contamination.addFrom(level, into, toxic, Leaks.contaminationOf(toxic, vented.getAmount()) * (float) Config.number(Config.GAS_VENT_POLLUTION, 0.25));
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        FluidStack held = tank.getPrimaryHandler().getFluid();
        tooltip.add(Component.literal(" - Gas Vent: " + Config.number(Config.GAS_VENT_RATE_MB, 500) + " mb/s"
                + (held.isEmpty() ? "" : ", " + held.getHoverName().getString())).withStyle(ChatFormatting.GRAY));
        return true;
    }
}
