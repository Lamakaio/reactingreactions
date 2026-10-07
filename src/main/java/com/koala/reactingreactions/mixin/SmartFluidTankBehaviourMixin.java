package com.koala.reactingreactions.mixin;

import com.koala.reactingreactions.Config;
import com.koala.reactingreactions.content.multiblock.ProcessingMachineBlockEntity;
import com.koala.reactingreactions.content.toxic.Leaks;
import com.koala.reactingreactions.content.toxic.TankSealing;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Every machine tank made with Create's smart tank behaviour (this mod's machines, Create's basins and more): a toxic content can leak. */
@Mixin(value = SmartFluidTankBehaviour.class, remap = false)
public abstract class SmartFluidTankBehaviourMixin {
    @Inject(method = "tick", at = @At("TAIL"), require = 0)
    private void crr$leak(CallbackInfo ci) {
        SmartFluidTankBehaviour self = (SmartFluidTankBehaviour) (Object) this;
        Level level = self.getWorld();
        if (level == null || level.isClientSide || !Config.toxicityEnabled()) {
            return;
        }
        BlockPos pos = self.getPos();
        if (!Leaks.shouldCheck(level, pos)) {
            return;
        }
        SmartFluidTank tank = self.getPrimaryHandler();
        FluidStack fluid = tank.getFluid();
        if (!fluid.isEmpty()) {
            float tightness = self.blockEntity instanceof ProcessingMachineBlockEntity<?> machine ? machine.leakTightness()
                    : TankSealing.isSealed(self.blockEntity) ? 0 : 1.0F;
            Leaks.check(level, pos, fluid, tank.getFluidAmount() / (float) Math.max(1, tank.getCapacity()), tightness, tank);
        }
    }
}
