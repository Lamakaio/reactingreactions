package com.koala.reactingreactions.content.multiblock;

import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.content.render.FlatQuads;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;

import net.createmod.catnip.platform.NeoForgeCatnipServices;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;

/** Draws a multiblock's fluid the way Create draws a tank: across the whole footprint, filled to the tank's level. */
public class MultiblockRenderer<T extends MultiblockControllerBlockEntity<?>> extends SafeBlockEntityRenderer<T> {
    /** Create's own tank hull thickness. */
    private static final ResourceLocation DIAL = ReactingReactions.asResource("block/controller_dial");
    public static final float HULL = 1 / 16f + 1 / 128f;
    private static final float MIN_VISIBLE_FILL = 1 / 16f;

    public MultiblockRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(T be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        HollowBoxScanner.Result structure = be.getStructure();
        if (structure != null) {
            renderContents(be, structure, ms, buffer, light);
            renderDials(be, structure, partialTicks, ms, buffer, light, dialDepth(be));
        }
    }

    /** A progress dial in the top corner of each outer side of the controller, its needle sweeping 270 degrees. */
    private static void renderDials(MultiblockControllerBlockEntity<?> be, HollowBoxScanner.Result structure, float partialTicks, PoseStack ms,
                                    MultiBufferSource buffer, int light, float depth) {
        var sprite = FlatQuads.sprite(DIAL);
        var consumer = FlatQuads.consumer(buffer);
        float degrees = -135 + 270 * be.shownProgress(partialTicks);
        forEachOutwardSide(be, structure, ms, () -> {
            // Seen from outside, local -x is to the right; x0 > x1 keeps the dial unmirrored.
            FlatQuads.quad(ms, consumer, sprite, -1 / 16F, 1 / 16F, -7 / 16F, 7 / 16F, -0.502F - depth, light);
            ms.pushPose();
            ms.translate(-4 / 16F, 4 / 16F, -0.504F - depth);
            ms.mulPose(Axis.ZP.rotationDegrees(degrees));
            // The needle's colour: one of the dial's own red ticks.
            FlatQuads.quad(ms, consumer, sprite, -0.25F / 16, -0.5F / 16, 0.25F / 16, 2.25F / 16, 0, light, 13 / 16F, 8 / 16F, 14 / 16F, 9 / 16F);
            ms.popPose();
        });
    }

    /** How far in front of the controller's face the dial sits: formed models all have a panel 1px proud for it. */
    protected float dialDepth(T be) {
        BlockState state = be.getBlockState();
        return state.hasProperty(MachineTiers.PART) && state.getValue(MachineTiers.PART) > 0 ? 1 / 16F : 0;
    }

    /** Runs {@code draw} once per horizontal side of the controller facing out of the structure, with local -z pointing out. */
    protected static void forEachOutwardSide(MultiblockControllerBlockEntity<?> be, HollowBoxScanner.Result structure, PoseStack ms, Runnable draw) {
        BlockPos min = structure.min();
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos out = be.getBlockPos().relative(side);
            boolean outside = out.getX() < min.getX() || out.getZ() < min.getZ()
                    || out.getX() >= min.getX() + structure.sizeX() || out.getZ() >= min.getZ() + structure.sizeZ();
            if (outside) {
                ms.pushPose();
                ms.translate(0.5, 0.5, 0.5);
                ms.mulPose(Axis.YP.rotationDegrees(180.0F - side.toYRot()));
                draw.run();
                ms.popPose();
            }
        }
    }

    /** Across the whole box like a Create tank; once formed, in the formed model's hollow inside, seen through its windows. */
    protected void renderContents(T be, HollowBoxScanner.Result structure, PoseStack ms, MultiBufferSource buffer, int light) {
        boolean formed = be.getBlockState().hasProperty(MachineTiers.PART) && be.getBlockState().getValue(MachineTiers.PART) > 0;
        float[] box = formed ? formedFluidBox(be, structure) : new float[] {HULL, HULL, structure.sizeY() - HULL};
        renderFluid(be.getRenderedFluid(), be.getRenderedFill(), be.getBlockPos(), structure, box[0], box[1], box[2], ms, buffer, light);
    }

    /** Where the fluid goes in the formed model: {inset from the sides, bottom, top}, in blocks. By default the shell's inside. */
    protected float[] formedFluidBox(T be, HollowBoxScanner.Result structure) {
        return new float[] {1, 1, structure.sizeY() - 1};
    }

    /** Fills rows {@code bottom} to {@code top} (0 is the floor row's bottom), {@code inset} in from the sides; gases fill from the top down. */
    protected static void renderFluid(FluidStack fluid, float fill, BlockPos controllerPos, HollowBoxScanner.Result structure, float inset, float bottom,
                                      float top, PoseStack ms, MultiBufferSource buffer, int light) {
        if (fluid.isEmpty()) {
            return;
        }
        float level = Math.max(MIN_VISIBLE_FILL, Math.min(1f, fill)) * (top - bottom);
        boolean gas = fluid.getFluidType().isLighterThanAir();
        renderBox(fluid, controllerPos, structure, inset, gas ? top - level : bottom, gas ? top : bottom + level, ms, buffer, light);
    }

    /** A fluid box {@code inset} in from the structure's sides, from {@code y1} to {@code y2}. */
    protected static void renderBox(FluidStack fluid, BlockPos controllerPos, HollowBoxScanner.Result structure, float inset, float y1, float y2,
                                    PoseStack ms, MultiBufferSource buffer, int light) {
        BlockPos min = structure.min();
        ms.pushPose();
        // The pose stack starts at the controller, which can be anywhere on the shell (fixed-size machines aside).
        ms.translate(min.getX() - controllerPos.getX(), min.getY() - controllerPos.getY(), min.getZ() - controllerPos.getZ());
        NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(fluid, inset, y1, inset, structure.sizeX() - inset, y2, structure.sizeZ() - inset, buffer, ms, light, false, true);
        ms.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(T be) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(T be) {
        HollowBoxScanner.Result structure = be.getStructure();
        if (structure == null) {
            return super.getRenderBoundingBox(be);
        }
        BlockPos min = structure.min();
        // One block over the roof too, for moving parts on top (the stirrer's fan).
        return new AABB(min.getX(), min.getY(), min.getZ(), min.getX() + structure.sizeX(), min.getY() + structure.sizeY() + 1, min.getZ() + structure.sizeZ());
    }
}
