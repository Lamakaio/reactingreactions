package com.koala.reactingreactions.content.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Reaction Chambers' stirrer: a whisk head turning with the shaft, on a pole up to the roof. Both models are 16px units
 * (tools/machine_models.py); the head is 12px across and 9.5px tall at scale 1.
 */
public final class Whisk {
    private Whisk() {
    }

    /** Positions in blocks from the pose's origin: the whisk's axis at (x, z), its head's base at {@code bottom}, the pole up to {@code top}. */
    public static void render(Level level, BlockState state, float rpm, double x, double z, double bottom, double top, float scale, PoseStack ms,
                              MultiBufferSource buffer, int light) {
        float angle = AnimationTickHolder.getRenderTime(level) * rpm * 6.0F / 10.0F % 360.0F;
        double headTop = bottom + scale * 9.5 / 16F;
        ms.pushPose();
        ms.translate(x, bottom, z);
        ms.mulPose(Axis.YP.rotationDegrees(angle));
        ms.scale(scale, scale, scale);
        ms.translate(-0.5, 0, -0.5);
        CachedBuffers.partial(CRRPartialModels.WHISK_HEAD, state).light(light).renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));
        ms.popPose();
        if (top > headTop) {
            ms.pushPose();
            ms.translate(x, headTop, z);
            ms.mulPose(Axis.YP.rotationDegrees(angle));
            ms.scale(scale, (float) (top - headTop), scale);
            ms.translate(-0.5, 0, -0.5);
            CachedBuffers.partial(CRRPartialModels.WHISK_POLE, state).light(light).renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));
            ms.popPose();
        }
    }
}
