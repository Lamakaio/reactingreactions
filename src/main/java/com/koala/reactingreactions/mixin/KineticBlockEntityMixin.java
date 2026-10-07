package com.koala.reactingreactions.mixin;

import com.koala.reactingreactions.content.toxic.TankSealing;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/** The goggle line of a pump sealed with a Gasket (TankSealing). */
@Mixin(value = KineticBlockEntity.class, remap = false)
public abstract class KineticBlockEntityMixin {
    @Inject(method = "addToGoggleTooltip", at = @At("RETURN"), cancellable = true, require = 0)
    private void crr$sealedTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> cir) {
        if (TankSealing.isSealed((KineticBlockEntity) (Object) this)) {
            TankSealing.appendTooltip(tooltip, (KineticBlockEntity) (Object) this);
            cir.setReturnValue(true);
        }
    }
}
