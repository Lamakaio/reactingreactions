package com.koala.reactingreactions.content.ponder;

import com.koala.reactingreactions.content.multiblock.attachment.MachineAttachment;
import com.koala.reactingreactions.content.multiblock.MultiblockControllerBlockEntity;
import com.koala.reactingreactions.content.ponder.PonderSchematicExporter.Build;
import com.koala.reactingreactions.content.ponder.PonderSchematicExporter.Scene;

import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;

/**
 * The builds behind {@code assets/reactingreactions/ponder/*.nbt}, written by {@link PonderSchematicExporter}. The storyboards in
 * {@link CRRPonderScenes} point at these coordinates, so a change here goes with a change there. Layer 0 is the floor; the
 * viewer starts looking at the north and west faces, so machine fronts face that way and motors sit behind them.
 */
final class PonderSchematics {
    private static final String CRR = "reactingreactions:";
    private static final String[] VEINS = {"rich_oil_vein", "rich_asurine_vein", "rich_crimsite_vein", "rich_ochrum_vein",
            "rich_veridium_vein", "rich_scoria_vein", "rich_tuff_vein", "rich_granite_vein", "rich_diorite_vein"};

    private PonderSchematics() {
    }

    static List<Scene> all() {
        List<Scene> scenes = new ArrayList<>(List.of(
                new Scene("derrick", 7, 9, 7, b -> rig(b, "create:asurine", CRR + "rich_asurine_vein", CRR + "mineral_drill_head_titanium")),
                new Scene("oil_drill", 7, 9, 7, b -> rig(b, CRR + "oil_shale", CRR + "rich_oil_vein", CRR + "oil_drill_head")),
                new Scene("mineral_drill", 7, 9, 7, b -> rig(b, "create:asurine", CRR + "rich_asurine_vein", CRR + "mineral_drill_head_titanium")),
                new Scene("rich_veins", 5, 2, 5, PonderSchematics::richVeins),
                new Scene("reaction_chamber", 5, 7, 5, b -> {
                    b.floor(5);
                    reactionChamber(b, 1, 1);
                    b.fillTank(2, 3, 1, CRR + "sulfuric_acid", 13000);
                }),
                new Scene("electrolysis_vat", 7, 5, 7, PonderSchematics::electrolysisVat),
                new Scene("distillation_tower", 5, 8, 5, b -> distillationTower(b, CRR + "crude_oil")),
                new Scene("airless_oven", 5, 4, 5, b -> {
                    b.floor(5);
                    shell(b, CRR + "airless_oven_wall", 1, 1, 1, 3, 3, 3);
                    b.set(2, 2, 1, CRR + "airless_oven_controller");
                }),
                new Scene("machine_attachments", 7, 7, 7, PonderSchematics::machineAttachments),
                new Scene("small_machines", 5, 5, 5, b -> {
                    b.floor(5);
                    b.set(1, 1, 2, CRR + "small_electrolyser");
                    b.set(3, 1, 2, CRR + "fermentation_barrel");
                    // The Small Reaction Chamber at the back: burner, both halves, the shaft into its top.
                    b.set(2, 1, 4, "create:blaze_burner");
                    b.set(2, 2, 4, CRR + "small_reaction_chamber", "half=lower", "facing=north");
                    b.set(2, 3, 4, CRR + "small_reaction_chamber", "half=upper", "facing=north");
                    b.set(2, 4, 4, "create:shaft", "axis=y");
                }),
                new Scene("ore_processing", 7, 7, 7, PonderSchematics::oreProcessing),
                new Scene("leaks", 7, 3, 7, PonderSchematics::leaks),
                new Scene("staying_safe", 7, 3, 7, PonderSchematics::stayingSafe),
                new Scene("petrochemistry", 7, 8, 7, PonderSchematics::petrochemistry),
                new Scene("lithium_brine", 7, 8, 7, PonderSchematics::lithiumBrine),
                new Scene("titanium", 7, 6, 7, PonderSchematics::titanium),
                new Scene("ore_dusts", 9, 6, 9, PonderSchematics::oreDusts),
                new Scene("plastics", 9, 7, 9, PonderSchematics::plastics),
                new Scene("polymetallic_nodule", 5, 6, 5, PonderSchematics::polymetallicNodule),
                new Scene("acetylene_lamp", 7, 1, 7, b -> b.floor(7))));
        // With Create Diesel Generators, distilling runs on its tower: these variants are shown instead (see CRRPonderScenes).
        if (ModList.get().isLoaded("createdieselgenerators")) {
            scenes.add(new Scene("distillation_tower_cdg", 5, 7, 5, PonderSchematics::dieselGeneratorsTower));
            scenes.add(new Scene("lithium_brine_cdg", 7, 6, 7, PonderSchematics::lithiumBrineDieselGenerators));
        }
        if (ModList.get().isLoaded("aeronautics_bundled")) {
            scenes.add(new Scene("gas_diffuser", 7, 6, 7, PonderSchematics::gasDiffuser));
        }
        return scenes;
    }

    /**
     * A drilling rig over 4 layers of stone with a vein of {@code rock} running diagonally through them: Rich Veins at (3,0,3)
     * and (4,0,4), the head at (3,1,3), pipe up the middle, the three tower layers at y 4-6, the controller at (3,7,3) with a motor
     * on top.
     */
    static void rig(Build b, String rock, String vein, String head) {
        for (int x = 0; x <= 6; x++) {
            for (int y = 0; y <= 3; y++) {
                for (int z = 0; z <= 6; z++) {
                    // Three wide along the x = z diagonal at the bottom, narrowing to one wide, and gone at the surface.
                    int off = Math.abs(x - z);
                    boolean inVein = y <= 1 ? off <= 1 : y == 2 && off == 0 && x >= 2 && x <= 5;
                    b.set(x, y, z, inVein ? rock : "minecraft:stone");
                }
            }
        }
        b.set(3, 0, 3, vein, "richness=5");
        b.set(4, 0, 4, vein, "richness=3");
        b.set(3, 1, 3, head);
        b.fill(3, 2, 3, 3, 6, 3, CRR + "drill_pipe");
        for (int y = 4; y <= 6; y++) {
            for (int dx = -1; dx <= 1; dx += 2) {
                for (int dz = -1; dz <= 1; dz += 2) {
                    b.set(3 + dx, y, 3 + dz, CRR + "derrick_block");
                }
            }
            if (y != 5) {
                b.set(3, y, 2, CRR + "derrick_truss").set(3, y, 4, CRR + "derrick_truss").set(2, y, 3, CRR + "derrick_truss").set(4, y, 3, CRR + "derrick_truss");
            }
        }
        b.set(3, 7, 3, CRR + "derrick_controller", "facing=north");
        b.set(3, 8, 3, "create:creative_motor", "facing=down");
    }

    /** Every Rich Vein on a 3x3, richness counting up from 1. */
    private static void richVeins(Build b) {
        b.floor(5);
        for (int i = 0; i < VEINS.length; i++) {
            b.set(1 + i % 3, 1, 1 + i / 3, CRR + VEINS[i], "richness=" + (1 + i % 5));
        }
    }

    /**
     * The small (3x3x4) chamber with its corner at (x, 2, z): Blaze Burners in an X on layer 1, the controller in the middle of
     * the north face, the stirring shaft through the roof and a motor on top.
     */
    static void reactionChamber(Build b, int x, int z) {
        int roof = 5;
        for (int[] cell : new int[][] {{0, 0}, {2, 0}, {1, 1}, {0, 2}, {2, 2}}) {
            b.set(x + cell[0], 1, z + cell[1], "create:blaze_burner");
        }
        shell(b, CRR + "reaction_chamber_wall", x, 2, z, x + 2, roof, z + 2);
        b.set(x + 1, 3, z, CRR + "reaction_chamber_controller");
        b.set(x + 1, roof, z + 1, CRR + "steel_encased_shaft", "axis=y");
        b.set(x + 1, roof + 1, z + 1, "create:shaft", "axis=y");
        b.set(x + 1, roof + 2, z + 1, "create:creative_motor", "facing=down");
    }

    /** The small (5x3x3) vat from (1,1,2): graphite electrodes at x 2 and 4, their terminals in the end walls, the controller at (3,2,2). */
    private static void electrolysisVat(Build b) {
        b.floor(7);
        shell(b, CRR + "electrolysis_vat_wall", 1, 1, 2, 5, 3, 4);
        for (int x : new int[] {2, 4}) {
            b.set(x, 2, 3, CRR + "graphite_electrode", "facing=up");
        }
        b.set(1, 2, 3, CRR + "electrolysis_vat_terminal").set(5, 2, 3, CRR + "electrolysis_vat_terminal");
        b.set(3, 2, 2, CRR + "electrolysis_vat_controller");
    }

    /**
     * The small Reaction Chamber from (2,2,2) with the mounted attachments: a gauge north at (4,3,1) beside the controller, an
     * Expansion Tank west at (1,3,3), a Circulation Pump south at (2,3,5), driven by a motor behind it, and an Outlet Valve east at
     * (5,2,3) filling a tank on a casing. The storyboard installs
     * the Outlet Manifold and Gasket.
     */
    private static void machineAttachments(Build b) {
        b.floor(7);
        reactionChamber(b, 2, 2);
        b.set(4, 3, 1, CRR + "machine_gauge", "facing=south");
        b.set(1, 3, 3, CRR + "expansion_tank", "facing=east");
        b.set(2, 3, 5, CRR + "circulation_pump", "facing=north");
        b.set(2, 3, 6, "create:creative_motor", "facing=north");
        b.set(5, 2, 3, CRR + "outlet_valve", "facing=east");
        b.set(6, 1, 3, "create:andesite_casing");
        b.set(6, 2, 3, "create:fluid_tank");
    }

    /** A 3x3 tower of {@code fluid}, 5 tall, from (1,2,1) over Blaze Burners, the controller in the floor row at (2,2,1). */
    private static void distillationTower(Build b, String fluid) {
        b.floor(5);
        for (int[] cell : new int[][] {{1, 1}, {3, 1}, {2, 2}, {1, 3}, {3, 3}}) {
            b.set(cell[0], 1, cell[1], "create:blaze_burner");
        }
        shell(b, CRR + "distillation_tower_wall", 1, 2, 1, 3, 6, 3);
        b.set(2, 2, 1, CRR + "distillation_tower_controller");
        b.fillTank(2, 2, 1, fluid, 20000);
    }



    /**
     * Crushing wheels at (0,3,1) and (2,3,1) over a depot of dust at (1,1,1), motors behind them; a superheated Reaction
     * Chamber from (4,2,2) with oxygen in it.
     */
    private static void oreProcessing(Build b) {
        b.floor(7);
        b.set(0, 3, 1, "create:crushing_wheel", "axis=z");
        b.set(2, 3, 1, "create:crushing_wheel", "axis=z");
        b.set(0, 3, 2, "create:creative_motor", "facing=north");
        b.set(2, 3, 2, "create:creative_motor", "facing=north");
        b.set(1, 1, 1, "create:depot");
        reactionChamber(b, 4, 2);
        b.insert(1, 1, 1, CRR + "asurine_dust", 8);
        b.fillTank(5, 3, 2, CRR + "oxygen", 13000);
    }

    /**
     * A 4-tall Reaction Chamber from (2,2,2) fed by tanks of naphtha at (0,1,2) and steam at (0,1,4) through pipes rising at x 1, its
     * ethylene and propylene piped down at x 5 into tanks at (6,1,2) and (6,1,4), to the right of the chamber.
     */
    private static void petrochemistry(Build b) {
        b.floor(7);
        reactionChamber(b, 2, 2);
        for (int z : new int[] {2, 4}) {
            b.set(0, 1, z, "create:fluid_tank");
            b.fill(1, 1, z, 1, 2, z, "create:fluid_pipe");
            b.fill(5, 1, z, 5, 2, z, "create:fluid_pipe");
            b.set(6, 1, z, "create:fluid_tank");
        }
        b.fillTank(0, 1, 2, CRR + "ethane", 8000);
        b.fillTank(0, 1, 4, CRR + "steam", 8000);
        b.fillTank(6, 1, 2, CRR + "ethylene", 4000);
        b.fillTank(6, 1, 4, CRR + "hydrogen", 4000);
        b.fillTank(3, 3, 2, CRR + "ethane", 4000);
        // Two products need a second output tank: an Outlet Manifold goes in once the chamber has formed.
        b.after(3, 3, 2, (level, pos) -> {
            if (level.getBlockEntity(pos) instanceof MultiblockControllerBlockEntity<?> chamber) {
                chamber.installUpgrade(MachineAttachment.Kind.OUTLET);
            }
        });
    }

    /**
     * A distillation tower from (1,2,1) full of water, its third row piped down along x 4 into a heated Basin at (5,2,2) under a
     * Mixer at (5,4,2).
     */
    private static void lithiumBrine(Build b) {
        b.floor(7);
        distillationTower(b, "minecraft:water");
        b.fill(4, 2, 2, 4, 4, 2, "create:fluid_pipe");
        mixer(b, 5, 2);
        b.fillTank(5, 2, 2, CRR + "strong_brine", 1000);
    }

    /**
     * Two Mixers side by side, Basins at (1,2,3) and (4,2,3) joined by a pump at (2,2,3) and a pipe: the first making titanium
     * tetrachloride from crimsite dust, coal coke and bleach, the second titanium from it and magnesium.
     */
    private static void titanium(Build b) {
        b.floor(7);
        mixer(b, 1, 3);
        mixer(b, 4, 3);
        b.set(2, 2, 3, "create:mechanical_pump", "facing=east");
        b.set(3, 2, 3, "create:fluid_pipe");
        b.fillTank(1, 2, 3, CRR + "bleach", 1000);
        b.insert(1, 2, 3, CRR + "crimsite_dust", 4);
        b.insert(1, 2, 3, CRR + "coal_coke", 4);
        b.fillTank(4, 2, 3, CRR + "titanium_tetrachloride", 1000);
        b.insert(4, 2, 3, CRR + "magnesium", 4);
    }

    /**
     * The other ore dusts' routes, left to right as seen: a Mixer at (2,_,6) for granite, a blast furnace at (1,1,1) for crimsite, and a
     * small Electrolysis Vat from (4,1,4) with gold-steel electrodes at x 5 and 7 for ochrum.
     */
    private static void oreDusts(Build b) {
        b.floor(9);
        mixer(b, 1, 1);
        b.insert(1, 2, 1, CRR + "crimsite_dust", 8);
        b.insert(1, 2, 1, CRR + "coal_coke", 4);
        mixer(b, 2, 6);
        b.insert(2, 2, 6, CRR + "granite_dust", 8);
        shell(b, CRR + "electrolysis_vat_wall", 4, 1, 4, 8, 3, 6);
        for (int x : new int[] {5, 7}) {
            b.set(x, 2, 5, CRR + "gold_steel_electrode", "facing=up");
        }
        b.set(4, 2, 5, CRR + "electrolysis_vat_terminal").set(8, 2, 5, CRR + "electrolysis_vat_terminal");
        b.set(6, 2, 4, CRR + "electrolysis_vat_controller");
        b.insert(6, 2, 4, CRR + "ochrum_dust", 8);
    }

    /**
     * Plastic from ethylene: a tank at (0,1,5) feeding a Reaction Chamber from (1,2,4) holding silica (quartz dust), its liquid HDPE piped to a
     * casting Basin at (6,1,2) under a Mixer, and a depot of pellets at (8,1,2).
     */
    private static void plastics(Build b) {
        b.floor(9);
        reactionChamber(b, 1, 4);
        b.set(0, 1, 5, "create:fluid_tank");
        b.set(0, 2, 5, "create:fluid_pipe");
        b.fill(4, 2, 5, 6, 2, 5, "create:fluid_pipe");
        b.fill(6, 2, 3, 6, 2, 4, "create:fluid_pipe");
        b.set(6, 1, 3, "create:fluid_pipe");
        b.set(6, 1, 2, "create:basin");
        b.set(6, 3, 2, "create:mechanical_mixer");
        b.set(6, 4, 2, "create:creative_motor", "facing=down");
        b.set(8, 1, 2, "create:depot");
        b.fillTank(0, 1, 5, CRR + "ethylene", 8000);
        b.fillTank(2, 3, 4, CRR + "ethylene", 2000);
        b.insert(2, 3, 4, CRR + "silica", 8);
        b.fillTank(6, 1, 2, CRR + "liquid_hdpe", 500);
        b.insert(8, 1, 2, CRR + "hdpe_pellets", 8);
    }

    /** A nodule on the floor at (1,1,3), Crushing Wheels over a depot of crushed nodules at (1,1,1), and a Mixer beside them at (4,_,1). */
    private static void polymetallicNodule(Build b) {
        b.floor(5);
        b.set(1, 1, 3, CRR + "polymetallic_nodule");
        b.set(0, 3, 1, "create:crushing_wheel", "axis=z");
        b.set(2, 3, 1, "create:crushing_wheel", "axis=z");
        b.set(0, 3, 2, "create:creative_motor", "facing=north");
        b.set(2, 3, 2, "create:creative_motor", "facing=north");
        b.set(1, 1, 1, "create:depot");
        mixer(b, 4, 1);
        b.insert(1, 1, 1, CRR + "crushed_polymetallic_nodule", 4);
        b.insert(4, 2, 1, CRR + "crushed_polymetallic_nodule", 4);
    }

    /** A Gas Diffuser at (3,1,3) fed from a tank of helium at (5,1,3), under a 5x5 envelope from y 3 to 5 with an open bottom. */
    private static void gasDiffuser(Build b) {
        b.floor(7);
        b.set(3, 1, 3, CRR + "gas_diffuser");
        b.set(4, 1, 3, "create:fluid_pipe");
        b.set(5, 1, 3, "create:fluid_tank");
        for (int x = 1; x <= 5; x++) {
            for (int z = 1; z <= 5; z++) {
                boolean edge = x == 1 || x == 5 || z == 1 || z == 5;
                if (edge) {
                    b.fill(x, 3, z, x, 4, z, "aeronautics:white_envelope");
                }
                b.set(x, 5, z, "aeronautics:white_envelope");
            }
        }
        b.fillTank(5, 1, 3, CRR + "helium", 8000);
    }


    /** A crude oil tank at (1,1,1) with a pipe up and along y 2 to (4,2,1), a contaminated methane tank at (5,1,4), a drain at (3,0,4). */
    private static void leaks(Build b) {
        b.floor(7);
        b.set(1, 1, 1, "create:fluid_tank");
        b.fill(1, 2, 1, 4, 2, 1, "create:fluid_pipe");
        b.set(5, 1, 4, "create:fluid_tank");
        b.set(3, 0, 4, CRR + "floor_drain", "half=top");
        b.fillTank(1, 1, 1, CRR + "crude_oil", 8000);
        b.fillTank(5, 1, 4, CRR + "contaminated_methane", 4000);
    }

    /** An Atmospheric Scrubber at (2,1,3) facing west with a motor behind it, and a Gas Vent on a pipe at (5,1,2). */
    private static void stayingSafe(Build b) {
        b.floor(7);
        b.set(2, 1, 3, CRR + "atmospheric_scrubber", "facing=west");
        b.set(3, 1, 3, "create:creative_motor", "facing=west");
        b.set(5, 1, 2, "create:fluid_pipe");
        b.set(5, 2, 2, CRR + "gas_vent", "facing=up");
    }

    /** Create Diesel Generators' tower: a 1x1 column of five Distillation Tanks on a Blaze Burner at (2,1,2), crude oil in it. */
    private static void dieselGeneratorsTower(Build b) {
        b.floor(5);
        b.set(2, 1, 2, "create:blaze_burner");
        b.fill(2, 2, 2, 2, 6, 2, "createdieselgenerators:distillation_tank");
        b.fillTank(2, 2, 2, "createdieselgenerators:crude_oil", 8000);
    }

    /**
     * Brine on Create Diesel Generators' tower: a 3-tall column at (1,_,2) on a Blaze Burner, full of water, its weak brine level (y 3)
     * piped down at x 3 into a heated Basin at (4,2,2) under a Mixer.
     */
    private static void lithiumBrineDieselGenerators(Build b) {
        b.floor(7);
        b.set(1, 1, 2, "create:blaze_burner");
        b.fill(1, 2, 2, 1, 4, 2, "createdieselgenerators:distillation_tank");
        b.fill(2, 3, 2, 3, 3, 2, "create:fluid_pipe");
        b.set(3, 2, 2, "create:fluid_pipe");
        mixer(b, 4, 2);
        b.fillTank(1, 2, 2, "minecraft:water", 8000);
        b.fillTank(4, 2, 2, CRR + "strong_brine", 1000);
    }

    /** A Blaze Burner at (x,1,z), a Basin on it, and a Mixer two above the Basin turned by a motor on top. */
    private static void mixer(Build b, int x, int z) {
        b.set(x, 1, z, "create:blaze_burner");
        b.set(x, 2, z, "create:basin");
        b.set(x, 4, z, "create:mechanical_mixer");
        b.set(x, 5, z, "create:creative_motor", "facing=down");
    }

    /** Walls on every face of the box, air inside. */
    static void shell(Build b, String wall, int x1, int y1, int z1, int x2, int y2, int z2) {
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    boolean face = x == x1 || x == x2 || y == y1 || y == y2 || z == z1 || z == z2;
                    b.set(x, y, z, face ? wall : "minecraft:air");
                }
            }
        }
    }
}
