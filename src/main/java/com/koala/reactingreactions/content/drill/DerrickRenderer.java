package com.koala.reactingreactions.content.drill;

import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.content.render.FlatQuads;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;

import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Draws the input shaft (like any Create shaft) and, while the rig is formed and turning, the drill rod spinning down the pipe, and
 * the head at its end turning with it while it drills.
 */
public class DerrickRenderer extends ShaftRenderer<DerrickControllerBlockEntity> {
    private static final ResourceLocation ROD = ReactingReactions.asResource("block/derrick_truss");

    public DerrickRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(DerrickControllerBlockEntity be, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, poseStack, buffer, light, overlay);
        if (!be.isFormed() || be.getSpeed() == 0 || be.getLevel() == null) {
            return;
        }
        float degrees = AnimationTickHolder.getRenderTime(be.getLevel()) * be.getSpeed() * 3.0F / 10.0F % 360.0F;
        // From the bottom of the last pipe block to the controller's floor.
        float bottom = -(3 + be.getPipeLength());
        int rodLight = LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().below(2));
        FlatQuads.rod(poseStack, buffer, ROD, 0.16F, bottom, 0.0F, degrees, rodLight);
        renderHead(be, degrees, poseStack, buffer);
    }

    /** The head's own model, turned like the rod (the block itself draws nothing while {@code SPINNING}). */
    private static void renderHead(DerrickControllerBlockEntity be, float degrees, PoseStack poseStack, MultiBufferSource buffer) {
        BlockPos head = DrillRig.headPos(be.getBlockPos(), be.getPipeLength());
        BlockState state = be.getLevel().getBlockState(head);
        if (!(state.getBlock() instanceof DrillHeadBlock) || !state.getValue(DrillHeadBlock.SPINNING)) {
            return;
        }
        BlockPos offset = head.subtract(be.getBlockPos());
        poseStack.pushPose();
        poseStack.translate(offset.getX() + 0.5, offset.getY(), offset.getZ() + 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(degrees));
        poseStack.translate(-0.5, 0, -0.5);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state.setValue(DrillHeadBlock.SPINNING, false), poseStack, buffer,
                LevelRenderer.getLightColor(be.getLevel(), head), OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(DerrickControllerBlockEntity be) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(DerrickControllerBlockEntity be) {
        // The rod reaches far below the block: make sure it is not culled when only the controller is out of view.
        return new AABB(be.getBlockPos()).inflate(2, 0, 2).expandTowards(0, -100, 0);
    }
}
