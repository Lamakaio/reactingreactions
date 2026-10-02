package com.koala.reactingreactions.content.toxic;

import com.koala.reactingreactions.content.info.CompoundInfo;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

/** The formula and toxicity lines on fluid tooltips, for JEI (through a mixin) and EMI. */
public final class FluidChemistryTooltip {
    private FluidChemistryTooltip() {
    }

    /** The formula goes right after the name, the toxicity line last. */
    public static void append(List<Component> tooltip, FluidStack stack) {
        Component formula = formulaLine(stack);
        if (formula != null) {
            tooltip.add(Math.min(1, tooltip.size()), formula);
        }
        Component toxicityLine = toxicityLine(stack);
        if (toxicityLine != null) {
            tooltip.add(toxicityLine);
        }
    }

    /** The lines {@link #append} adds, for EMI, which adds them one at a time. */
    public static List<Component> linesFor(FluidStack stack) {
        List<Component> lines = new ArrayList<>();
        Component formula = formulaLine(stack);
        if (formula != null) {
            lines.add(formula);
        }
        Component toxicityLine = toxicityLine(stack);
        if (toxicityLine != null) {
            lines.add(toxicityLine);
        }
        return lines;
    }

    private static Component formulaLine(FluidStack stack) {
        CompoundInfo.Entry entry = ourEntry(stack);
        return entry != null && entry.hasFormula() ? entry.formulaLine().copy().withStyle(ChatFormatting.DARK_AQUA) : null;
    }

    private static Component toxicityLine(FluidStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        ToxicFluid toxic = Toxicity.of(stack);
        return toxic.toxicity() > 0 ? ToxicityTooltips.line(toxic.toxicity(), toxic) : null;
    }

    private static CompoundInfo.Entry ourEntry(FluidStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        return CompoundInfo.find(BuiltInRegistries.FLUID.getKey(stack.getFluid()));
    }
}
