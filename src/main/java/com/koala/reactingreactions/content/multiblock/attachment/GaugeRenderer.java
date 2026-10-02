package com.koala.reactingreactions.content.multiblock.attachment;

import com.koala.reactingreactions.content.render.CRRPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;

import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

/** The gauge's needle, sweeping 270 degrees across the dial as the reading goes from 0 to 15. */
public class GaugeRenderer extends SafeBlockEntityRenderer<GaugeBlockEntity> {
    public GaugeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(GaugeBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        Direction towards = MachineAttachment.mountedTowards(be.getBlockState());
        ms.pushPose();
        // The model faces a machine to the south; the blockstate turns it like this.
        ms.translate(0.5, 0.5, 0.5);
        ms.mulPose(Axis.YP.rotationDegrees(-(towards.toYRot() - Direction.SOUTH.toYRot())));
        // The dial's face is at z 8.75; the needle sits just in front of it, pivoting at its centre.
        ms.translate(0, 0, 8.5 / 16 - 0.5);
        // Seen from in front (looking along +z of the turned model), a positive turn about z is clockwise.
        ms.mulPose(Axis.ZP.rotationDegrees(-135 + 270 * be.needle()));
        ms.translate(-0.5, -0.5, 0);
        CachedBuffers.partial(CRRPartialModels.GAUGE_NEEDLE, be.getBlockState()).light(light).renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));
        ms.popPose();
    }
}
