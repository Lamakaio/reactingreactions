package com.koala.reactingreactions.content.electrolysis;

import com.koala.reactingreactions.content.multiblock.HollowBoxScanner;
import com.koala.reactingreactions.content.multiblock.MultiblockRenderer;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/** The formed vat's walls are thin: its electrolyte fills the hollow inside (tools/machine_models.py), seen through the windows. */
public class ElectrolysisVatRenderer extends MultiblockRenderer<ElectrolysisVatControllerBlockEntity> {
    public ElectrolysisVatRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected float[] formedFluidBox(ElectrolysisVatControllerBlockEntity be, HollowBoxScanner.Result structure) {
        return new float[] {2 / 16F, 4.5F / 16, structure.sizeY() - 4.5F / 16};
    }
}
