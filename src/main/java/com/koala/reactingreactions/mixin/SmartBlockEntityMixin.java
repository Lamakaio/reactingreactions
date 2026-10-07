package com.koala.reactingreactions.mixin;

import com.koala.reactingreactions.content.toxic.TankSealing;
import com.koala.reactingreactions.registry.CRRItems;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The Gasket flag of a sealed tank or pump (TankSealing): saved, synced, and dropped as a Gasket when the block is broken. */
@Mixin(value = SmartBlockEntity.class, remap = false)
public abstract class SmartBlockEntityMixin implements TankSealing.Sealable {
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

    @Inject(method = "destroy", at = @At("HEAD"), require = 0)
    private void crr$dropGasket(CallbackInfo ci) {
        SmartBlockEntity self = (SmartBlockEntity) (Object) this;
        if (crr$sealed && self.getLevel() != null && !self.getLevel().isClientSide) {
            crr$sealed = false;
            BlockPos pos = self.getBlockPos();
            Containers.dropItemStack(self.getLevel(), pos.getX(), pos.getY(), pos.getZ(), new ItemStack(CRRItems.GASKET.get()));
        }
    }
}
