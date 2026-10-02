package com.koala.reactingreactions.content.toxic;

import com.koala.reactingreactions.Config;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

/** The goggle line that tells which toxic contents a machine is leaking. */
public final class LeakInfo {
    private LeakInfo() {
    }

    public static void append(List<Component> tooltip, SmartBlockEntity be) {
        if (!Config.toxicityEnabled()) {
            return;
        }
        for (BlockEntityBehaviour behaviour : be.getAllBehaviours()) {
            if (behaviour instanceof SmartFluidTankBehaviour tanks) {
                FluidStack fluid = tanks.getPrimaryHandler().getFluid();
                ToxicFluid data = Toxicity.of(fluid);
                if (data.toxicity() > 0) {
                    tooltip.add(Component.literal(String.format(" - Leaks slowly: %s (toxicity %.0f)", fluid.getHoverName().getString(), data.toxicity()))
                            .withStyle(data.toxicity() >= 7 ? ChatFormatting.RED : ChatFormatting.GOLD));
                }
            }
        }
    }
}
