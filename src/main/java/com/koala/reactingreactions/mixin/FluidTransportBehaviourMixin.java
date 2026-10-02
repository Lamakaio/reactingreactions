package com.koala.reactingreactions.mixin;

import com.koala.reactingreactions.Config;
import com.koala.reactingreactions.content.fluids.PlasticPipeBlock;
import com.koala.reactingreactions.content.toxic.Leaks;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.PipeConnection;
import com.simibubi.create.content.fluids.pipes.EncasedPipeBlock;
import com.simibubi.create.content.fluids.pipes.GlassFluidPipeBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Every Create pipe, pump and valve: what flows through it can leak (only the environment is affected, never the flow). */
@Mixin(value = FluidTransportBehaviour.class, remap = false)
public abstract class FluidTransportBehaviourMixin {
    @Inject(method = "tick", at = @At("TAIL"), require = 0)
    private void crr$leak(CallbackInfo ci) {
        FluidTransportBehaviour self = (FluidTransportBehaviour) (Object) this;
        Level level = self.getWorld();
        if (level == null || level.isClientSide || !Config.toxicityEnabled()) {
            return;
        }
        BlockPos pos = self.getPos();
        if (!Leaks.shouldCheck(level, pos)) {
            return;
        }
        for (Direction direction : Direction.values()) {
            PipeConnection.Flow flow = self.getFlow(direction);
            if (flow != null && !flow.fluid.isEmpty()) {
                Block block = level.getBlockState(pos).getBlock();
                // Encased pipes and Plastic Pipes are sealed; glass pipes are tighter than plain copper.
                float tightness = block instanceof EncasedPipeBlock || block instanceof PlasticPipeBlock ? 0
                        : block instanceof GlassFluidPipeBlock ? 0.5F : 1.0F;
                Leaks.check(level, pos, flow.fluid, 0.5F, tightness, null);
                return;
            }
        }
    }
}
