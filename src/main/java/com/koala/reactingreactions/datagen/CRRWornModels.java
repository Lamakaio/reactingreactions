package com.koala.reactingreactions.datagen;

import com.tterrag.registrate.providers.RegistrateItemModelProvider;

import static com.koala.reactingreactions.datagen.SwatchModels.*;

/**
 * The 3D models of worn equipment, {@code models/worn/<item>.json}, drawn by {@code WornModels}. Coordinates are pixels around an
 * anchor on the body, always with +y up and +z forward: the face's centre at (8, 8, 0), the middle of the back at (8, 8, 0) with the
 * body at +z, the centre under a foot or a hand at (8, 0, 8), the top or bottom of the chest's front at (8, 16, 0) or (8, 0, 0).
 * The wearer's right is -x. The boxes are {@link SwatchModels}.
 */
public final class CRRWornModels {
    // A rubber strap around the head, shared by both masks.
    private static final float[][] HEAD_STRAP = {{3.75F, 8, -8.25F, 4, 9, 0, RUBBER}, {12, 8, -8.25F, 12.25F, 9, 0, RUBBER},
            {4, 8, -8.25F, 12, 9, -8, RUBBER}};

    private CRRWornModels() {
    }

    public static void provide(RegistrateItemModelProvider prov) {
        model(prov, "gas_mask", HEAD_STRAP, new float[][] {
                {4.5F, 4.25F, 0.05F, 11.5F, 11, 0.5F, RUBBER_LIGHT},
                {5, 8, 0.5F, 7.5F, 10.5F, 1, LENS}, {8.5F, 8, 0.5F, 11, 10.5F, 1, LENS},
                {6, 4, 0.5F, 10, 7.5F, 2, RUBBER},
                {6.5F, 4.5F, 2, 9.5F, 7, 4, CANISTER}, {7, 5, 4, 9, 6.5F, 4.5F, DARK_STEEL}});
        model(prov, "oxygen_mask", HEAD_STRAP, new float[][] {
                {5.5F, 4, 0.05F, 10.5F, 7.5F, 1.5F, OXYGEN},
                {7, 4.5F, 1.5F, 9, 6.5F, 2.5F, STEEL},
                {7.5F, 1.5F, 1.5F, 8.5F, 4.5F, 2.5F, DARK_STEEL}});
        model(prov, "aerozine_thrusters", new float[][] {
                {4.5F, 9, -0.5F, 11.5F, 10.5F, -0.05F, DARK_LEATHER}, {4.5F, 4, -0.5F, 11.5F, 5.5F, -0.05F, DARK_LEATHER},
                {4.5F, 3.5F, -4, 7.5F, 12.5F, -0.5F, TANK}, {8.5F, 3.5F, -4, 11.5F, 12.5F, -0.5F, TANK},
                {5, 12.5F, -3.5F, 7, 13.5F, -1, STEEL}, {9, 12.5F, -3.5F, 11, 13.5F, -1, STEEL},
                {5, 2, -3.5F, 7, 3.5F, -1, NOZZLE}, {9, 2, -3.5F, 11, 3.5F, -1, NOZZLE},
                {7.5F, 6, -3, 8.5F, 10, -1.5F, STEEL}});
        model(prov, "flame_retardant_cloak", new float[][] {
                {3.5F, -1, -1.25F, 12.5F, 14, -0.6F, CLOAK},
                {3.5F, 14, -1.25F, 12.5F, 14.6F, 0, LINING},
                // Folds hanging down from the collar.
                {5, -1, -1.6F, 6, 13.5F, -1.25F, LINING}, {7.5F, -1, -1.6F, 8.5F, 13.5F, -1.25F, LINING}, {10, -1, -1.6F, 11, 13.5F, -1.25F, LINING}});
        model(prov, "spring_boots", new float[][] {
                {5.75F, 1.5F, 5.75F, 10.25F, 5.5F, 10.25F, LEATHER}, {5.75F, 1.5F, 10.25F, 10.25F, 3.5F, 11.25F, DARK_LEATHER},
                {5.75F, 1, 5.75F, 10.25F, 1.5F, 11.25F, DARK_STEEL}, {6.5F, 0, 6.5F, 9.5F, 1, 9.5F, STEEL}});
        model(prov, "diving_fins", new float[][] {
                {5.75F, 0.5F, 6, 10.25F, 2.5F, 10.25F, FIN_STRAP},
                {5.25F, 0, 10.25F, 10.75F, 0.6F, 15.5F, FIN}, {5.75F, 0, 15.5F, 10.25F, 0.5F, 16.5F, FIN_STRAP}});
        model(prov, "chemical_gloves", new float[][] {
                {5.75F, 0, 5.75F, 10.25F, 4.5F, 10.25F, GLOVE}, {5.5F, 4.5F, 5.5F, 10.5F, 6, 10.5F, CUFF}});
        model(prov, "chemical_boots", new float[][] {
                {5.75F, 0.75F, 5.75F, 10.25F, 6, 10.25F, BOOT}, {5.6F, 0, 5.6F, 10.4F, 0.75F, 11.25F, DARK_BOOT},
                {5.75F, 0.75F, 10.25F, 10.25F, 3, 11.25F, BOOT}, {5.5F, 5.5F, 5.5F, 10.5F, 6.5F, 10.5F, DARK_BOOT}});
        model(prov, "racing_anklet", new float[][] {
                {5.8F, 2, 5.8F, 10.2F, 3, 10.2F, ANKLET},
                // A little wing on the outer side.
                {4.5F, 2, 7, 5.8F, 4, 9.5F, WHITE}, {4.75F, 4, 7.5F, 5.8F, 4.75F, 9.5F, WHITE}});
        model(prov, "digging_ring", new float[][] {
                {5.7F, 1.2F, 5.7F, 10.3F, 1.8F, 10.3F, GOLD}, {5.2F, 1, 7.25F, 5.7F, 2, 8.75F, RUBY}});
        model(prov, "magnesium_knuckle", new float[][] {
                {5.6F, 0.5F, 10.25F, 10.4F, 2.5F, 10.9F, STEEL},
                {6, 1, 10.9F, 7, 2, 11.4F, STEEL}, {7.5F, 1, 10.9F, 8.5F, 2, 11.4F, STEEL}, {9, 1, 10.9F, 10, 2, 11.4F, STEEL}});
        model(prov, "helium_locket", new float[][] {
                {4.5F, 15.4F, 0.05F, 11.5F, 16, 0.5F, CHAIN}, {7.75F, 12, 0.05F, 8.25F, 15.4F, 0.5F, CHAIN},
                {6.75F, 9.5F, 0.1F, 9.25F, 12, 0.9F, GOLD}, {7.25F, 10, 0.9F, 8.75F, 11.5F, 1.1F, HELIUM}});
        model(prov, "anchor_charm", new float[][] {
                {5, 1.5F, 0.05F, 5.75F, 5, 0.7F, DARK_STEEL}, {4.25F, 4, 0.05F, 6.5F, 4.5F, 0.7F, DARK_STEEL},
                {4, 1.5F, 0.05F, 6.75F, 2, 0.7F, DARK_STEEL}, {4.9F, 5, 0.05F, 5.85F, 5.75F, 0.7F, STEEL}});
    }

    private static void model(RegistrateItemModelProvider prov, String name, float[][]... boxLists) {
        SwatchModels.boxes(prov.getBuilder("worn/" + name), 0, boxLists);
    }
}
