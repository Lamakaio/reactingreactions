package com.koala.reactingreactions.mixin;

import com.koala.reactingreactions.content.toxic.TankSealing;
import com.koala.reactingreactions.registry.CRRItems;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;

import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** A sealed Create Fluid Tank's controller drops its Gasket when broken (TankSealing). */
@Mixin(value = SmartBlockEntity.class, remap = false)
public abstract class SmartBlockEntityMixin {
    @Inject(method = "destroy", at = @At("HEAD"), require = 0)
    private void crr$dropGasket(CallbackInfo ci) {
        if ((Object) this instanceof FluidTankBlockEntity tank && tank.isController() && tank instanceof TankSealing.Sealable sealable
                && sealable.crr$isSealed() && tank.getLevel() != null && !tank.getLevel().isClientSide) {
            sealable.crr$setSealed(false);
            Containers.dropItemStack(tank.getLevel(), tank.getBlockPos().getX(), tank.getBlockPos().getY(), tank.getBlockPos().getZ(),
                    new ItemStack(CRRItems.GASKET.get()));
        }
    }
}
