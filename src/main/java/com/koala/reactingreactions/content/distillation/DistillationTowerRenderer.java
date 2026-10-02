package com.koala.reactingreactions.content.distillation;

import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.content.render.FlatQuads;
import com.koala.reactingreactions.content.multiblock.HollowBoxScanner;
import com.koala.reactingreactions.content.multiblock.MultiblockRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

/** Each row holds its own tank's fluid, under a sieve tray with its downcomer gap on alternating sides. */
public class DistillationTowerRenderer extends MultiblockRenderer<DistillationTowerControllerBlockEntity> {
    private static final ResourceLocation TRAY = ReactingReactions.asResource("block/scrubber_front");

    public DistillationTowerRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderContents(DistillationTowerControllerBlockEntity be, HollowBoxScanner.Result structure, PoseStack ms, MultiBufferSource buffer,
                                  int light) {
        for (int row = 0; row < structure.sizeY(); row++) {
            DistillationTowerControllerBlockEntity.RowFluid shown = be.getRowFluid(row);
            float bottom = row == 0 ? HULL : row;
            float top = row == structure.sizeY() - 1 ? row + 1 - HULL : row + 1;
            renderFluid(shown.fluid(), shown.fill(), be.getBlockPos(), structure, HULL, bottom, top, ms, buffer, light);
        }
        renderTrays(be, structure, ms, buffer, light);
    }

    private static void renderTrays(DistillationTowerControllerBlockEntity be, HollowBoxScanner.Result structure, PoseStack ms, MultiBufferSource buffer,
                                    int light) {
        var sprite = FlatQuads.sprite(TRAY);
        var consumer = FlatQuads.consumer(buffer);
        BlockPos min = structure.min();
        ms.pushPose();
        ms.translate(min.getX() - be.getBlockPos().getX(), min.getY() - be.getBlockPos().getY(), min.getZ() - be.getBlockPos().getZ());
        for (int row = 2; row < structure.sizeY(); row++) {
            boolean gapEast = row % 2 == 0;
            ms.pushPose();
            ms.translate(0, row, 0);
            // Lays the quads' xy plane flat: local y becomes world z.
            ms.mulPose(Axis.XP.rotationDegrees(90));
            for (int x = 0; x < structure.sizeX(); x++) {
                for (int z = 0; z < structure.sizeZ(); z++) {
                    float x0 = x == 0 ? HULL : x;
                    float x1 = x == structure.sizeX() - 1 ? x + 1 - HULL : x + 1;
                    // The downcomer: the last 0.4 of the tray on one side is left open.
                    if (gapEast && x == structure.sizeX() - 1) {
                        x1 = x + 0.6F;
                    } else if (!gapEast && x == 0) {
                        x0 = 0.4F;
                    }
                    float z0 = z == 0 ? HULL : z;
                    float z1 = z == structure.sizeZ() - 1 ? z + 1 - HULL : z + 1;
                    FlatQuads.quad(ms, consumer, sprite, x0, z0, x1, z1, 0, light);
                }
            }
            ms.popPose();
        }
        ms.popPose();
    }
}
