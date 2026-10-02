package com.koala.reactingreactions.item;

import com.koala.reactingreactions.registry.CRRDataComponents;
import com.simibubi.create.AllEnchantments;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

/**
 * Any item with an internal fluid tank (the Aerozine Chestplate and Thrusters): a single-fluid capacity plus a filter for
 * what it accepts. Create's Capacity enchantment enlarges the tank the same way it does a backtank. 
 */
public interface FluidTankHolder {
    /** The tank size before enchantments. */
    int baseTankCapacityMb();

    /** Each level of Create's Capacity adds a third of the base size, matching Create's backtank (900 air, +300 per level). */
    default int tankCapacityMb(ItemStack stack) {
        int level = 0;
        for (var entry : stack.getTagEnchantments().entrySet()) {
            if (entry.getKey().is(AllEnchantments.CAPACITY)) {
                level = entry.getIntValue();
            }
        }
        int base = baseTankCapacityMb();
        return base + base * level / 3;
    }

    boolean acceptsFluid(FluidStack stack);

    /** Always shown, even empty */
    default boolean isTankBarVisible(ItemStack stack) {
        return true;
    }

    default int tankBarWidth(ItemStack stack) {
        return Math.round(13.0F * contents(stack).getAmount() / tankCapacityMb(stack));
    }

    default int tankBarColor(ItemStack stack) {
        float fraction = Mth.clamp(contents(stack).getAmount() / (float) tankCapacityMb(stack), 0.0F, 1.0F);
        return Mth.hsvToRgb(fraction / 3.0F, 1.0F, 1.0F);
    }

    /** tooltip */
    default Component tankTooltipLine(ItemStack stack) {
        FluidStack contents = contents(stack);
        String fluidName = contents.isEmpty() ? "" : " " + contents.getHoverName().getString();
        return Component.literal(contents.getAmount() + " / " + tankCapacityMb(stack) + " mB" + fluidName)
                .withStyle(ChatFormatting.GRAY);
    }

    static FluidStack contents(ItemStack stack) {
        return stack.getOrDefault(CRRDataComponents.FLUID_TANK.get(), SimpleFluidContent.EMPTY).copy();
    }

    static void setContents(ItemStack stack, FluidStack fluid) {
        if (fluid.isEmpty()) {
            stack.remove(CRRDataComponents.FLUID_TANK.get());
        } else {
            stack.set(CRRDataComponents.FLUID_TANK.get(), SimpleFluidContent.copyOf(fluid));
        }
    }

    /** Takes up to {@code wanted} mB out of the stack's tank; returns how much it took. */
    static int take(ItemStack stack, int wanted) {
        FluidStack current = contents(stack);
        if (current.isEmpty() || wanted <= 0) {
            return 0;
        }
        int taken = Math.min(current.getAmount(), wanted);
        setContents(stack, current.copyWithAmount(current.getAmount() - taken));
        return taken;
    }
}
