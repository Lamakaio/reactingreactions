package com.koala.reactingreactions.content.equipment.charging;

import com.koala.reactingreactions.content.compat.WornAccessories;
import com.koala.reactingreactions.content.toxic.Leaks;
import com.koala.reactingreactions.item.FluidTankHolder;
import com.koala.reactingreactions.registry.CRRItems;
import com.koala.reactingreactions.registry.CRRParticles;
import com.koala.reactingreactions.registry.CRRTags;
import com.simibubi.create.AllTags;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.fluid.CombinedTankWrapper;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.List;

/** Tops up the tank items and backtanks of a player standing on it. */
public class ChargingPadBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    private static final BehaviourType<SmartFluidTankBehaviour> TANK_A_TYPE = new BehaviourType<>("crr_charging_pad_a");
    private static final BehaviourType<SmartFluidTankBehaviour> TANK_B_TYPE = new BehaviourType<>("crr_charging_pad_b");

    private SmartFluidTankBehaviour tankA;
    private SmartFluidTankBehaviour tankB;
    private IFluidHandler fluidCapability;

    public ChargingPadBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        tankA = new SmartFluidTankBehaviour(TANK_A_TYPE, this, 1, TANK_CAPACITY_MB, false);
        tankB = new SmartFluidTankBehaviour(TANK_B_TYPE, this, 1, TANK_CAPACITY_MB, false);
        behaviours.add(tankA);
        behaviours.add(tankB);
        fluidCapability = new TaggedFluidHandler(new CombinedTankWrapper(tankA.getCapability(), tankB.getCapability()), CRRTags.RECHARGEABLE);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event, BlockEntityType<ChargingPadBlockEntity> type) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, type, (be, ctx) -> be.fluidCapability);
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) {
            return;
        }
        AABB standingOn = new AABB(worldPosition).expandTowards(0, 1, 0).inflate(0, 0, 0);
        for (Player player : level.getEntitiesOfClass(Player.class, standingOn)) {
            if (!player.onGround()) {
                continue;
            }
            FluidStack charged = FluidStack.EMPTY;
            // Held items and worn armor are live stacks; an accessory slot needs its stack written back.
            for (var slot : EquipmentSlot.values()) {
                charged = orFirst(charged, chargeOne(player.getItemBySlot(slot), fluidCapability));
            }
            // Without a slot mod, worn thrusters sit in the chest slot and were charged above.
            var thrusters = WornAccessories.hasSlotMod() ? WornAccessories.find(player, CRRItems.AEROZINE_THRUSTERS.get()) : null;
            if (thrusters != null) {
                charged = orFirst(charged, chargeOne(thrusters.stack(), fluidCapability));
                thrusters.save();
            }
            if (!charged.isEmpty() && level.getGameTime() % 3 == 0) {
                chargingRing((ServerLevel) level, player, charged);
            }
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(Component.literal(" - Charging Pad").withStyle(ChatFormatting.GRAY));
        addTankTooltip(tooltip, tankA);
        addTankTooltip(tooltip, tankB);
        return true;
    }

    private static final int TANK_CAPACITY_MB = 4000;
    private static final int CHARGE_RATE_MB_PER_TICK = 40;

    /** This mod's tank items, and Create's backtanks (which take breathable gas as air, see CreateBacktankAir). */
    static boolean isChargeable(ItemStack stack) {
        return stack.getItem() instanceof FluidTankHolder || AllTags.AllItemTags.PRESSURIZED_AIR_SOURCES.matches(stack);
    }

    private static FluidStack orFirst(FluidStack first, FluidStack next) {
        return first.isEmpty() ? next : first;
    }

    /** A ring of the fluid's colour rising around the player's feet. */
    private static void chargingRing(ServerLevel level, Player player, FluidStack fluid) {
        int colour = Leaks.colorOf(fluid.getFluid());
        double phase = level.getGameTime() * 0.3;
        for (int i = 0; i < 4; i++) {
            double angle = phase + i * Math.PI / 2;
            level.sendParticles(CRRParticles.haze(colour, 0.35F), player.getX() + Math.cos(angle) * 0.45, player.getY() + 0.05,
                    player.getZ() + Math.sin(angle) * 0.45, 0, 0, 0.06, 0, 1);
        }
    }

    /** Tops {@code stack}'s own internal tank up from whichever of {@code source}'s tanks holds a matching fluid, returning what went in. */
    private static FluidStack chargeOne(ItemStack stack, IFluidHandler source) {
        if (stack.isEmpty() || !isChargeable(stack)) {
            return FluidStack.EMPTY;
        }
        var target = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (target == null) {
            return FluidStack.EMPTY;
        }
        for (int i = 0; i < source.getTanks(); i++) {
            FluidStack available = source.getFluidInTank(i);
            if (available.isEmpty()) {
                continue;
            }
            FluidStack offer = available.copyWithAmount(Math.min(CHARGE_RATE_MB_PER_TICK, available.getAmount()));
            int accepted = target.fill(offer, IFluidHandler.FluidAction.SIMULATE);
            if (accepted <= 0) {
                continue;
            }
            FluidStack drained = source.drain(offer.copyWithAmount(accepted), IFluidHandler.FluidAction.EXECUTE);
            if (!drained.isEmpty()) {
                target.fill(drained.copy(), IFluidHandler.FluidAction.EXECUTE);
            }
            return drained;
        }
        return FluidStack.EMPTY;
    }

    private static void addTankTooltip(List<Component> tooltip, SmartFluidTankBehaviour tank) {
        FluidStack fluid = tank.getPrimaryHandler().getFluid();
        if (fluid.isEmpty()) {
            return;
        }
        tooltip.add(Component.literal(" - ").withStyle(ChatFormatting.GRAY)
                .append(fluid.getHoverName().copy().withStyle(ChatFormatting.WHITE))
                .append(Component.literal(" (" + fluid.getAmount() + "/" + tank.getPrimaryHandler().getCapacity() + " mb)")
                        .withStyle(ChatFormatting.GRAY)));
    }
}
