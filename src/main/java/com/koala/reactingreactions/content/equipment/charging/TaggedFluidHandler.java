package com.koala.reactingreactions.content.equipment.charging;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/** Only fills with fluids in {@code tag}; draining and everything else pass through. */
public class TaggedFluidHandler implements IFluidHandler {
    private final IFluidHandler delegate;
    private final TagKey<Fluid> tag;

    public TaggedFluidHandler(IFluidHandler delegate, TagKey<Fluid> tag) {
        this.delegate = delegate;
        this.tag = tag;
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
        return stack.is(tag) && delegate.isFluidValid(tank, stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return resource.is(tag) ? delegate.fill(resource, action) : 0;
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
