package com.koala.reactingreactions.content.reaction;

import com.koala.reactingreactions.content.render.Whisk;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;

import net.createmod.catnip.platform.NeoForgeCatnipServices;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;

import static com.koala.reactingreactions.content.reaction.SmallReactionChamberBlockEntity.FLUID_BOTTOM;
import static com.koala.reactingreactions.content.reaction.SmallReactionChamberBlockEntity.FLUID_HEIGHT;
import static com.koala.reactingreactions.content.reaction.SmallReactionChamberBlockEntity.FLUID_INSET;

/** The contents seen through the window, and the whisk hanging from the top, turning with the shaft. */
public class SmallReactionChamberRenderer extends SafeBlockEntityRenderer<SmallReactionChamberBlockEntity> {
    private static final float MIN_VISIBLE_FILL = 1 / 16F;
    // The whisk inside the hollow (tools/machine_models.py): 9px across, from 6px up to under the lid.
    private static final float WHISK_SCALE = 0.75F;
    private static final float WHISK_BOTTOM = 6 / 16F;
    private static final float WHISK_TOP = 26 / 16F;

    public SmallReactionChamberRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(SmallReactionChamberBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        renderWhisk(be, ms, buffer, light);
        SmartFluidTankBehaviour tank = be.renderedTank();
        if (tank == null) {
            return;
        }
        FluidStack fluid = tank.getPrimaryHandler().getFluid();
        float fill = Math.max(MIN_VISIBLE_FILL, Math.min(1F, fluid.getAmount() / (float) Math.max(1, tank.getPrimaryHandler().getCapacity())));
        float top = FLUID_BOTTOM + FLUID_HEIGHT;
        // Gases fill from the top down.
        boolean gas = fluid.getFluidType().isLighterThanAir();
        float y1 = gas ? top - fill * FLUID_HEIGHT : FLUID_BOTTOM;
        float y2 = gas ? top : FLUID_BOTTOM + fill * FLUID_HEIGHT;
        NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(fluid, FLUID_INSET, y1, FLUID_INSET, 1 - FLUID_INSET, y2, 1 - FLUID_INSET, buffer, ms,
                light, false, true);
    }

    /** Our whisk, small enough to clear the narrow vessel, from just above the floor up into the lid. */
    private static void renderWhisk(SmallReactionChamberBlockEntity be, PoseStack ms, MultiBufferSource buffer, int light) {
        Whisk.render(be.getLevel(), be.getBlockState(), be.stirSpeed(), 0.5, 0.5, WHISK_BOTTOM, WHISK_TOP, WHISK_SCALE, ms, buffer, light);
    }

    /** Both halves. */
    @Override
    public AABB getRenderBoundingBox(SmallReactionChamberBlockEntity be) {
        return new AABB(be.getBlockPos()).expandTowards(0, 1, 0);
    }
}
