package com.koala.reactingreactions.mixin.compat;

import com.koala.reactingreactions.content.toxic.FluidChemistryTooltip;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.fluids.FluidStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * JEI builds every fluid tooltip through one method. Adding to it
 * shows this mod's fluids' chemical formula and toxicity everywhere, which JEI's plugin API cannot do I think ? 
 * Does nothing without JEI.
 */
@Mixin(targets = "mezz.jei.neoforge.platform.FluidHelper", remap = false)
public abstract class JeiFluidTooltipMixin {
    @Inject(method = "getTooltip(Ljava/util/List;Lnet/neoforged/neoforge/fluids/FluidStack;Lnet/minecraft/world/item/TooltipFlag;)V", at = @At("TAIL"), require = 0)
    private void crr$addFormula(List<Component> tooltip, FluidStack stack, TooltipFlag flag, CallbackInfo ci) {
        FluidChemistryTooltip.append(tooltip, stack);
    }
}
