package com.koala.reactingreactions.content.toxic;

import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.content.render.FlatQuads;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;

import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

/** The scrubber's input shaft, plus a fan spinning in front of its grate while it turns. */
public class AtmosphericScrubberRenderer extends ShaftRenderer<AtmosphericScrubberBlockEntity> {
    private static final ResourceLocation BLADE = ReactingReactions.asResource("block/scrubber_side");

    public AtmosphericScrubberRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(AtmosphericScrubberBlockEntity be, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, poseStack, buffer, light, overlay);
        if (be.getSpeed() == 0 || be.getLevel() == null) {
            return;
        }
        Direction facing = be.getBlockState().getValue(HorizontalKineticBlock.HORIZONTAL_FACING);
        float degrees = AnimationTickHolder.getRenderTime(be.getLevel()) * be.getSpeed() * 3.0F / 10.0F % 360.0F;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        // Local -z points out of the front face.
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - facing.toYRot()));
        int frontLight = LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().relative(facing));
        FlatQuads.fan(poseStack, buffer, BLADE, 4, 0.34F, 0.14F, -0.445F, degrees, frontLight);
        poseStack.popPose();
    }
}
