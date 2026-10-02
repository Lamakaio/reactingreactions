package com.koala.reactingreactions.datagen;

import com.koala.reactingreactions.ReactingReactions;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;

/**
 * Item models made of plain boxes, each face one 4x4 swatch of {@code item/worn_equipment} (indices 0-15) or {@code _2} (16-31),
 * drawn by {@code tools/draw_icons.py}. Boxes are rows of {x0, y0, z0, x1, y1, z1, swatch}, with an optional 1 after them for a
 * box that glows.
 */
public final class SwatchModels {
    private static final ResourceLocation SHEET = ReactingReactions.asResource("item/worn_equipment");
    private static final ResourceLocation SHEET_2 = ReactingReactions.asResource("item/worn_equipment_2");

    public static final int RUBBER = 0, RUBBER_LIGHT = 1, LENS = 2, CANISTER = 3, BRASS = 4, STEEL = 5, DARK_STEEL = 6, OXYGEN = 7,
            CLOAK = 8, LINING = 9, FIN = 10, FIN_STRAP = 11, LEATHER = 12, DARK_LEATHER = 13, TANK = 14, NOZZLE = 15,
            GOLD = 16, RUBY = 17, GLOVE = 18, CUFF = 19, BOOT = 20, DARK_BOOT = 21, ANKLET = 22, WHITE = 23, CHAIN = 24, HELIUM = 25,
            NEON_CYAN = 26, NEON_RED = 27, DARK_GLASS = 31;

    private SwatchModels() {
    }

    /** Adds the boxes to {@code model}, all turned by {@code zDegrees} (a multiple of 22.5) around its centre. */
    public static ItemModelBuilder boxes(ItemModelBuilder model, float zDegrees, float[][]... boxLists) {
        model.texture("t", SHEET).texture("t2", SHEET_2).texture("particle", SHEET);
        for (float[][] boxes : boxLists) {
            for (float[] b : boxes) {
                int swatch = (int) b[6];
                String texture = swatch < 16 ? "#t" : "#t2";
                float u = swatch % 4 * 4;
                float v = swatch % 16 / 4 * 4;
                var element = model.element().from(b[0], b[1], b[2]).to(b[3], b[4], b[5]).allFaces((d, f) -> f.texture(texture).uvs(u, v, u + 4, v + 4));
                if (b.length > 7 && b[7] == 1) {
                    element.emissivity(15, 15);
                }
                if (zDegrees != 0) {
                    element.rotation().angle(zDegrees).axis(Direction.Axis.Z).origin(8, 8, 8).end();
                }
                element.end();
            }
        }
        return model;
    }
}
