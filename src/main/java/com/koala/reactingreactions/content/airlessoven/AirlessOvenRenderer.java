package com.koala.reactingreactions.content.airlessoven;

import com.koala.reactingreactions.content.render.FlatQuads;
import com.koala.reactingreactions.content.multiblock.HollowBoxScanner;
import com.koala.reactingreactions.content.multiblock.MachineTiers;
import com.koala.reactingreactions.content.multiblock.MultiblockRenderer;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** The oven's walls are solid bricks, so its contents fill only the interior. While it bakes, fire shows in the controller's door. */
public class AirlessOvenRenderer extends MultiblockRenderer<AirlessOvenControllerBlockEntity> {
    private static final ResourceLocation FIRE = ResourceLocation.withDefaultNamespace("block/fire_0");

    public AirlessOvenRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderContents(AirlessOvenControllerBlockEntity be, HollowBoxScanner.Result structure, PoseStack ms, MultiBufferSource buffer, int light) {
        if (!be.getRenderedFluid().isEmpty()) {
            renderBox(be.getRenderedFluid(), be.getBlockPos(), structure, 1, 1, structure.sizeY() - 1, ms, buffer, light);
        }
        if (be.isRunning()) {
            renderFirebox(be, structure, ms, buffer);
        }
    }

    /** Fire filling the door opening, on each side facing out of the oven: the controller texture's, or the formed model's firebox. */
    private static void renderFirebox(AirlessOvenControllerBlockEntity be, HollowBoxScanner.Result structure, PoseStack ms, MultiBufferSource buffer) {
        var sprite = FlatQuads.sprite(FIRE);
        var consumer = FlatQuads.consumer(buffer);
        if (formed(be)) {
            forEachOutwardSide(be, structure, ms,
                    () -> FlatQuads.quad(ms, consumer, sprite, 0, -6 / 16F, 6 / 16F, 3 / 16F, -0.501F, LightTexture.FULL_BRIGHT));
            return;
        }
        forEachOutwardSide(be, structure, ms,
                () -> FlatQuads.quad(ms, consumer, sprite, -3 / 16F, -4 / 16F, 3 / 16F, 3 / 16F, -0.501F, LightTexture.FULL_BRIGHT));
    }

    private static boolean formed(AirlessOvenControllerBlockEntity be) {
        return be.getBlockState().getValue(MachineTiers.PART) > 0;
    }

}
