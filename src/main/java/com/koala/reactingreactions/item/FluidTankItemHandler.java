package com.koala.reactingreactions.item;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

/** The fluid handler of a {@link FluidTankHolder} item. It only fills: pipes and spouts can top it up but never drain it. */
public class FluidTankItemHandler implements IFluidHandlerItem {
    private final ItemStack stack;
    private final FluidTankHolder holder;

    public FluidTankItemHandler(ItemStack stack, FluidTankHolder holder) {
        this.stack = stack;
        this.holder = holder;
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
        return FluidTankHolder.contents(stack);
    }

    @Override
    public int getTankCapacity(int tank) {
        return holder.tankCapacityMb(stack);
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return holder.acceptsFluid(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !holder.acceptsFluid(resource)) {
            return 0;
        }
        FluidStack current = FluidTankHolder.contents(stack);
        if (!current.isEmpty() && !FluidStack.isSameFluidSameComponents(current, resource)) {
            return 0;
        }
        int room = holder.tankCapacityMb(stack) - current.getAmount();
        int accepted = Math.min(room, resource.getAmount());
        if (accepted <= 0) {
            return 0;
        }
        if (action.execute()) {
            FluidStack updated = current.isEmpty() ? resource.copyWithAmount(accepted) : current.copyWithAmount(current.getAmount() + accepted);
            FluidTankHolder.setContents(stack, updated);
        }
        return accepted;
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
