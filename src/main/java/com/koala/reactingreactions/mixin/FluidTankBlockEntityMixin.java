package com.koala.reactingreactions.mixin;

import com.koala.reactingreactions.Config;
import com.koala.reactingreactions.content.toxic.Leaks;
import com.koala.reactingreactions.content.toxic.TankSealing;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Create's Fluid Tanks: the controller leaks for the whole multiblock, so a big tank does not leak once per block. A Gasket
 * (TankSealing) seals the whole tank.
 */
@Mixin(value = FluidTankBlockEntity.class, remap = false)
public abstract class FluidTankBlockEntityMixin {
    @Inject(method = "tick", at = @At("TAIL"), require = 0)
    private void crr$leak(CallbackInfo ci) {
        FluidTankBlockEntity self = (FluidTankBlockEntity) (Object) this;
        Level level = self.getLevel();
        if (level == null || level.isClientSide || !Config.toxicityEnabled() || !self.isController() || TankSealing.isSealed(self)) {
            return;
        }
        if (!Leaks.shouldCheck(level, self.getBlockPos())) {
            return;
        }
        FluidTank tank = self.getTankInventory();
        FluidStack fluid = tank.getFluid();
        if (!fluid.isEmpty()) {
            Leaks.check(level, self.getBlockPos(), fluid, tank.getFluidAmount() / (float) Math.max(1, tank.getCapacity()), 1.0F, tank);
        }
    }

    @Inject(method = "addToGoggleTooltip", at = @At("RETURN"), require = 0)
    private void crr$sealedTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> cir) {
        TankSealing.appendTooltip(tooltip, (FluidTankBlockEntity) (Object) this);
    }
}
