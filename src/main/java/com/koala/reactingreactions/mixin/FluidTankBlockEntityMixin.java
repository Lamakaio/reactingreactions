package com.koala.reactingreactions.mixin;

import com.koala.reactingreactions.Config;
import com.koala.reactingreactions.content.toxic.Leaks;
import com.koala.reactingreactions.content.toxic.TankSealing;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Create's Fluid Tanks: the controller leaks for the whole multiblock, so a big tank does not leak once per block. A Gasket
 * (TankSealing) seals the whole tank: the controller keeps the flag.
 */
@Mixin(value = FluidTankBlockEntity.class, remap = false)
public abstract class FluidTankBlockEntityMixin implements TankSealing.Sealable {
    @Unique
    private boolean crr$sealed;

    @Override
    public boolean crr$isSealed() {
        return crr$sealed;
    }

    @Override
    public void crr$setSealed(boolean sealed) {
        crr$sealed = sealed;
    }

    @Inject(method = "tick", at = @At("TAIL"), require = 0)
    private void crr$leak(CallbackInfo ci) {
        FluidTankBlockEntity self = (FluidTankBlockEntity) (Object) this;
        Level level = self.getLevel();
        if (level == null || level.isClientSide || !Config.toxicityEnabled() || !self.isController() || crr$sealed) {
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

    @Inject(method = "read", at = @At("TAIL"), require = 0)
    private void crr$readSealed(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        crr$sealed = compound.getBoolean("CrrSealed");
    }

    @Inject(method = "write", at = @At("TAIL"), require = 0)
    private void crr$writeSealed(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        if (crr$sealed) {
            compound.putBoolean("CrrSealed", true);
        }
    }

    @Inject(method = "addToGoggleTooltip", at = @At("RETURN"), require = 0)
    private void crr$sealedTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> cir) {
        if (TankSealing.isSealed((FluidTankBlockEntity) (Object) this)) {
            tooltip.add(Component.literal("    Sealed with a Gasket: no leaks").withStyle(ChatFormatting.GRAY));
        }
    }
}
