package com.koala.reactingreactions.content.toxic;

import com.koala.reactingreactions.Config;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

/** Adds a toxicity line to the tooltip of anything toxic: solids, and any container (bucket, canister, tank item) holding a toxic fluid. */
public class ToxicityTooltips {
    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (!Config.toxicityEnabled()) {
            return;
        }
        ItemStack stack = event.getItemStack();
        float toxicity = 0;
        ToxicFluid fluidData = null;
        FluidStack contained = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
        if (!contained.isEmpty()) {
            fluidData = Toxicity.of(contained);
            toxicity = fluidData.toxicity();
        }
        if (toxicity <= 0) {
            toxicity = Toxicity.carriedToxicity(stack);
            if (toxicity > 0 && fluidData == null) {
                fluidData = ToxicFluid.HARMLESS;
            }
        }
        if (toxicity <= 0) {
            return;
        }
        event.getToolTip().add(line(toxicity, fluidData));
    }

    /** "Toxicity: 8 / 10, flammable", coloured by how bad it is. */
    public static Component line(float toxicity, ToxicFluid data) {
        ChatFormatting colour = toxicity >= 7 ? ChatFormatting.RED : toxicity >= 4 ? ChatFormatting.GOLD : ChatFormatting.YELLOW;
        StringBuilder line = new StringBuilder(String.format("Toxicity: %.0f / 10", toxicity));
        if (data.flammable()) {
            line.append(", flammable");
        }
        if (data.explosive()) {
            line.append(", explosive");
        }
        return Component.literal(line.toString()).withStyle(colour);
    }
}
