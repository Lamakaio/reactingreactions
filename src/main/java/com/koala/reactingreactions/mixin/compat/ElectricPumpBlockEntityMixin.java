package com.koala.reactingreactions.mixin.compat;

import com.koala.reactingreactions.content.toxic.TankSealing;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/** Electro Energetics' pump has its own goggle tooltip, without Create's: the line of a Gasket goes there too. */
@Mixin(targets = "com.george_vi.electroenergetics.content.electric_pump.ElectricPumpBlockEntity", remap = false)
public abstract class ElectricPumpBlockEntityMixin {
    @Inject(method = "addToGoggleTooltip", at = @At("RETURN"), cancellable = true, require = 0)
    private void crr$sealedTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> cir) {
        if (TankSealing.isSealed((BlockEntity) (Object) this)) {
            TankSealing.appendTooltip(tooltip, (BlockEntity) (Object) this);
            cir.setReturnValue(true);
        }
    }
}
