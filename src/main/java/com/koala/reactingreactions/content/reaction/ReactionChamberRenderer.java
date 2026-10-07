package com.koala.reactingreactions.content.reaction;

import com.koala.reactingreactions.content.multiblock.HollowBoxScanner;
import com.koala.reactingreactions.content.multiblock.MachineTiers;
import com.koala.reactingreactions.content.multiblock.MultiblockRenderer;
import com.koala.reactingreactions.content.render.CRRPartialModels;
import com.koala.reactingreactions.content.render.Whisk;
import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/** Adds the whisk, hanging from the roof shaft and turning with it, and the stirrer motor's fan. */
public class ReactionChamberRenderer extends MultiblockRenderer<ReactionChamberControllerBlockEntity> {
    public ReactionChamberRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderContents(ReactionChamberControllerBlockEntity be, HollowBoxScanner.Result structure, PoseStack ms, MultiBufferSource buffer,
                                  int light) {
        renderWhisk(be, structure, ms, buffer);
        renderFan(be, structure, ms, buffer);
        super.renderContents(be, structure, ms, buffer, light);
    }

    /** Square, the fluid has to stay inside the formed model's stepped corners: inset per tier (tools/machine_models.py). */
    private static final float[] FLUID_INSET = {5.5F / 16, 7.5F / 16};

    /** The formed chamber's walls are thin: the fluid fills its octagonal hollow, from the floor plate to under the roof. */
    @Override
    protected float[] formedFluidBox(ReactionChamberControllerBlockEntity be, HollowBoxScanner.Result structure) {
        MachineTiers.Fit fit = be.getFit();
        return new float[] {FLUID_INSET[fit == null ? 0 : fit.index()], 9.5F / 16, structure.sizeY() - 5.5F / 16};
    }

    /** How far up the formed model the stirrer motor's fan sits, per tier, in pixels (tools/machine_models.py). */
    private static final int[] FAN_BASE = {78, 94};

    /** The fan on top of the formed chamber's stirrer motor, turning with the shaft. */
    private static void renderFan(ReactionChamberControllerBlockEntity be, HollowBoxScanner.Result structure, PoseStack ms, MultiBufferSource buffer) {
        MachineTiers.Fit fit = be.getFit();
        if (fit == null || be.getBlockState().getValue(MachineTiers.PART) == 0) {
            return;
        }
        BlockPos shaft = structure.min().offset(structure.sizeX() / 2, structure.sizeY(), structure.sizeZ() / 2);
        BlockPos controller = be.getBlockPos();
        float angle = AnimationTickHolder.getRenderTime(be.getLevel()) * be.stirSpeed() * 6.0F / 10.0F % 360.0F / 180.0F * (float) Math.PI;
        ms.pushPose();
        ms.translate(shaft.getX() - controller.getX(), structure.min().getY() - controller.getY() + FAN_BASE[fit.index()] / 16F,
                shaft.getZ() - controller.getZ());
        CachedBuffers.partial(CRRPartialModels.STIRRER_FAN, be.getBlockState()).rotateCentered(angle, Direction.UP)
                .light(LevelRenderer.getLightColor(be.getLevel(), shaft)).renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));
        ms.popPose();
    }

    /** The whisk's size per tier, clear of the walls of each tier's hollow (tools/machine_models.py). */
    private static final float[] WHISK_SCALE = {2.5F, 4F};
    // From just above the floor plate up to the roof.
    private static final float WHISK_BOTTOM = 12 / 16F;
    private static final float WHISK_ROOF = 5.5F / 16;

    private static void renderWhisk(ReactionChamberControllerBlockEntity be, HollowBoxScanner.Result structure, PoseStack ms, MultiBufferSource buffer) {
        MachineTiers.Fit fit = be.getFit();
        BlockPos controller = be.getBlockPos();
        BlockPos min = structure.min();
        double x = min.getX() + structure.sizeX() / 2.0 - controller.getX();
        double z = min.getZ() + structure.sizeZ() / 2.0 - controller.getZ();
        double floor = min.getY() - controller.getY();
        int light = LevelRenderer.getLightColor(be.getLevel(), min.offset(structure.sizeX() / 2, 1, structure.sizeZ() / 2));
        Whisk.render(be.getLevel(), be.getBlockState(), be.stirSpeed(), x, z, floor + WHISK_BOTTOM, floor + structure.sizeY() - WHISK_ROOF,
                WHISK_SCALE[fit == null ? 0 : fit.index()], ms, buffer, light);
    }
}
