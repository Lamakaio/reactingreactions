package com.koala.reactingreactions.content.equipment;

import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.registry.CRRFluids;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.armor.BacktankBlockEntity;
import com.simibubi.create.content.equipment.armor.BacktankUtil;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Bridges Create's backtank air and this mod's fluids, through NeoForge capabilities only (no mixin):
 * <ul>
 * <li>a placed backtank takes Oxygen or Compressed Air from pipes as air, and gives its air back out as Compressed Air, so a
 * spinning backtank is the air compressor feeding air separation;</li>
 * <li>a backtank item takes Oxygen or Compressed Air from a Spout or a Charging Pad as air.</li>
 * </ul>
 * One unit of backtank air (a second of breathing) is {@link #MB_PER_AIR} mB of gas.
 */
public final class CreateBacktankAir {
    public static final int MB_PER_AIR = 10;

    private static Field capacityEnchantLevel;

    private CreateBacktankAir() {
    }

    public static boolean isBreathable(FluidStack stack) {
        return stack.getFluid().isSame(CRRFluids.OXYGEN.get().getSource()) || stack.getFluid().isSame(CRRFluids.COMPRESSED_AIR.get().getSource());
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, AllBlockEntityTypes.BACKTANK.get(), (be, side) -> new PlacedHandler(be));
        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new ItemHandler(stack),
                AllItems.COPPER_BACKTANK.get(), AllItems.NETHERITE_BACKTANK.get());
    }

    /** The placed backtank's capacity, which depends on the Capacity enchantment it was placed with (a private field in Create). */
    private static int maxAir(BacktankBlockEntity be) {
        try {
            if (capacityEnchantLevel == null) {
                capacityEnchantLevel = BacktankBlockEntity.class.getDeclaredField("capacityEnchantLevel");
                capacityEnchantLevel.setAccessible(true);
            }
            return BacktankUtil.maxAir(capacityEnchantLevel.getInt(be));
        } catch (ReflectiveOperationException | RuntimeException e) {
            ReactingReactions.LOGGER.debug("Could not read a backtank's capacity level, assuming none", e);
            return BacktankUtil.maxAirWithoutEnchants();
        }
    }

    private static FluidStack air(int units) {
        return units <= 0 ? FluidStack.EMPTY : new FluidStack(CRRFluids.COMPRESSED_AIR.get().getSource(), units * MB_PER_AIR);
    }

    /** How many whole air units {@code resource} can add to a tank holding {@code current} of {@code max}. */
    private static int unitsToAdd(FluidStack resource, int current, int max) {
        if (resource.isEmpty() || !isBreathable(resource)) {
            return 0;
        }
        return Math.max(0, Math.min(max - current, resource.getAmount() / MB_PER_AIR));
    }

    /**
     * mB already drained from each placed backtank's current air unit (0-9). Create's pipes probe with 1 mB and a slow pump moves
     * only a few mB a tick, so the placed handler works to the millibucket: a unit of air is only used up once all 10 mB of it
     * have gone. Not saved: at most 9 mB is lost on a reload.
     */
    private static final Map<BacktankBlockEntity, Integer> DRAINED_FROM_UNIT = new WeakHashMap<>();

    private record PlacedHandler(BacktankBlockEntity be) implements IFluidHandler {
        private int storedMb() {
            return Math.max(0, be.getAirLevel() * MB_PER_AIR - DRAINED_FROM_UNIT.getOrDefault(be, 0));
        }

        /** Stores exactly {@code mb}: whole units as backtank air, the rest as a partly drained last unit. */
        private void setStoredMb(int mb) {
            int units = (mb + MB_PER_AIR - 1) / MB_PER_AIR;
            DRAINED_FROM_UNIT.put(be, units * MB_PER_AIR - mb);
            if (units != be.getAirLevel()) {
                be.setAirLevel(units);
                be.sendData();
            }
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            int mb = storedMb();
            return mb <= 0 ? FluidStack.EMPTY : new FluidStack(CRRFluids.COMPRESSED_AIR.get().getSource(), mb);
        }

        @Override
        public int getTankCapacity(int tank) {
            return maxAir(be) * MB_PER_AIR;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return isBreathable(stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || !isBreathable(resource)) {
                return 0;
            }
            int stored = storedMb();
            int accepted = Math.max(0, Math.min(resource.getAmount(), maxAir(be) * MB_PER_AIR - stored));
            if (accepted > 0 && action.execute()) {
                setStoredMb(stored + accepted);
            }
            return accepted;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return resource.getFluid().isSame(CRRFluids.COMPRESSED_AIR.get().getSource()) ? drain(resource.getAmount(), action) : FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            int stored = storedMb();
            int taken = Math.min(stored, Math.max(0, maxDrain));
            if (taken <= 0) {
                return FluidStack.EMPTY;
            }
            if (action.execute()) {
                setStoredMb(stored - taken);
            }
            return new FluidStack(CRRFluids.COMPRESSED_AIR.get().getSource(), taken);
        }
    }

    /** Fill-only, like this mod's own tank items: worn air is for breathing, not for pouring back out. */
    private record ItemHandler(ItemStack stack) implements IFluidHandlerItem {
        private int current() {
            return BacktankUtil.getAir(stack);
        }

        @Override
        public ItemStack getContainer() {
            return stack;
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return air(current());
        }

        @Override
        public int getTankCapacity(int tank) {
            return BacktankUtil.maxAir(stack) * MB_PER_AIR;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack fluid) {
            return isBreathable(fluid);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            int units = unitsToAdd(resource, current(), BacktankUtil.maxAir(stack));
            if (units > 0 && action.execute()) {
                stack.set(AllDataComponents.BACKTANK_AIR, current() + units);
            }
            return units * MB_PER_AIR;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    }
}
