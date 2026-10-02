package com.koala.reactingreactions.content.multiblock;

import com.simibubi.create.foundation.fluid.CombinedTankWrapper;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * A machine's tanks from outside: fills go to the input tanks, drains take outputs first, then inputs. A fluid not yet in
 * the inputs only comes in if {@code acceptsNew} allows it.
 */
public class InputFillOutputDrainWrapper extends CombinedTankWrapper {
    private final IFluidHandler[] inputs;
    private final IFluidHandler[] outputs;
    private final int outputTankCount;
    private final Predicate<FluidStack> acceptsNew;

    public InputFillOutputDrainWrapper(IFluidHandler[] outputs, IFluidHandler[] inputs, Predicate<FluidStack> acceptsNew) {
        super(concat(outputs, inputs));
        this.inputs = inputs;
        this.acceptsNew = acceptsNew;
        this.outputs = outputs;
        int count = 0;
        for (IFluidHandler output : outputs) count += output.getTanks();
        this.outputTankCount = count;
    }

    private static IFluidHandler[] concat(IFluidHandler[] first, IFluidHandler[] second) {
        IFluidHandler[] all = new IFluidHandler[first.length + second.length];
        System.arraycopy(first, 0, all, 0, first.length);
        System.arraycopy(second, 0, all, first.length, second.length);
        return all;
    }

    /**
     * The single tank a container should be filled from: the first non-empty
     * output tank, otherwise the first non-empty input tank, or null when the
     * whole machine is empty. Used by right-click filling of buckets and tanks.
     */
    @Nullable
    public IFluidHandler drainSource() {
        for (IFluidHandler output : outputs) {
            if (!output.getFluidInTank(0).isEmpty()) return output;
        }
        for (IFluidHandler input : inputs) {
            if (!input.getFluidInTank(0).isEmpty()) return input;
        }
        return null;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        // One input tank per distinct fluid: join the tank already holding it (refusing once
        // that one is full), otherwise the first empty tank that takes it (tanks may only accept certain fluids).
        for (IFluidHandler input : inputs) {
            FluidStack contained = input.getFluidInTank(0);
            if (!contained.isEmpty() && FluidStack.isSameFluidSameComponents(contained, resource)) {
                return input.fill(resource, action);
            }
        }
        if (resource.isEmpty() || !acceptsNew.test(resource)) {
            return 0;
        }
        for (IFluidHandler input : inputs) {
            if (input.getFluidInTank(0).isEmpty()) {
                int filled = input.fill(resource, action);
                if (filled > 0) {
                    return filled;
                }
            }
        }
        return 0;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return tank >= outputTankCount && super.isFluidValid(tank, stack);
    }
}
