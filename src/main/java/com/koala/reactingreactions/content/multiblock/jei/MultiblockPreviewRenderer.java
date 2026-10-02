package com.koala.reactingreactions.content.multiblock.jei;

import com.koala.reactingreactions.content.multiblock.jei.MultiblockPreview.Cell;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;

import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Draws a machine's smallest valid build as a cut-away isometric view above its recipes, with the side items in a column on
 * the right. Each block is projected by hand at the GUI's block-item angle, with its true depth so overlaps sort correctly.
 */
public final class MultiblockPreviewRenderer {
    /** How far the recipe grid of the machine pages is pushed down to make room for the preview (Create's layout already leaves space above its slots). */
    public static final int HEIGHT = 60;
    /** The preview's height on pages with their own layout; Create's layout already leaves most of this space empty. */
    public static final int FULL_HEIGHT = 108;

    private static final float TILT_DEG = 30f;
    private static final float YAW_DEG = 225f;
    /** The box the structure is fitted into, left of the side slots. */
    private static final double BOX_WIDTH = 132;
    private static final double BOX_HEIGHT = 98;
    private static final int MODEL_CENTER_X = 78;
    private static final int MODEL_CENTER_Y = 52;

    private MultiblockPreviewRenderer() {
    }

    public static void addSideSlots(IRecipeLayoutBuilder builder, MultiblockPreview preview) {
        int y = 2;
        for (ItemStack stack : preview.sideItems()) {
            builder.addSlot(RecipeIngredientRole.CATALYST, 154, y).addItemStack(stack);
            y += 18;
        }
    }

    /** Draws the structure only: the build rules are on the machine's information page, not here. */
    public static void draw(MultiblockPreview preview, GuiGraphics graphics) {
        renderStructure(preview, graphics, MODEL_CENTER_X, MODEL_CENTER_Y);
    }

    private static void renderStructure(MultiblockPreview recipe, GuiGraphics graphics, int centerX, int centerY) {
        double cx = recipe.sizeX() / 2.0;
        double cy = recipe.sizeY() / 2.0;
        double cz = recipe.sizeZ() / 2.0;
        double yaw = Math.toRadians(YAW_DEG);
        double tilt = Math.toRadians(TILT_DEG);

        record Projected(Cell cell, double sx, double sy, double depth) {
        }
        List<Projected> projected = new ArrayList<>();
        for (Cell cell : recipe.cells()) {
            // Cell centre relative to the structure centre, rotated by yaw (Y) then tilt (X).
            double x = cell.x() + 0.5 - cx;
            double yy = cell.y() + 0.5 - cy;
            double z = cell.z() + 0.5 - cz;
            double x1 = x * Math.cos(yaw) + z * Math.sin(yaw);
            double z1 = -x * Math.sin(yaw) + z * Math.cos(yaw);
            double y2 = yy * Math.cos(tilt) - z1 * Math.sin(tilt);
            double z2 = yy * Math.sin(tilt) + z1 * Math.cos(tilt);
            // atLocal is applied before the GUI's Y flip, so screen-down is -y2.
            projected.add(new Projected(cell, x1, -y2, z2));
        }
        projected.sort(Comparator.comparingDouble(Projected::depth));
        // Fit the structure to the box: as big as fits, centred on its own extent.
        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        for (Projected p : projected) {
            minX = Math.min(minX, p.sx());
            maxX = Math.max(maxX, p.sx());
            minY = Math.min(minY, p.sy());
            maxY = Math.max(maxY, p.sy());
        }
        double blockPx = Math.max(6, Math.min(16, Math.min(BOX_WIDTH / (maxX - minX + 1.6), BOX_HEIGHT / (maxY - minY + 1.6))));
        double midX = (minX + maxX) / 2;
        double midY = (minY + maxY) / 2;

        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 100);
        for (Projected p : projected) {
            // Each block is rendered around its own centre, then shifted by the projected offset.
            GuiGameElement.of(p.cell().state())
                    .scale(blockPx)
                    .rotateBlock(TILT_DEG, YAW_DEG, 0)
                    .atLocal(p.sx() - midX - 0.5, p.sy() - midY + 0.5, p.depth() - 0.5)
                    .at(centerX, centerY)
                    .render(graphics);
        }
        graphics.pose().popPose();
        // Draw the 3D blocks now, or EMI may draw the following 2D elements first and hide them behind the blocks.
        graphics.flush();
    }
}
