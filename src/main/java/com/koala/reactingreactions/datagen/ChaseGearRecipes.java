package com.koala.reactingreactions.datagen;

import static com.koala.reactingreactions.datagen.Data.*;

/**
 * The chase gear: components made by sequenced assembly from the mod's late materials, then each piece assembled on its titanium
 * counterpart with a vanilla trophy and a priming fill of its fluid.
 */
final class ChaseGearRecipes {
    private static final String CRR = "reactingreactions:";

    private ChaseGearRecipes() {
    }

    static void register(Data data) {
        Data d = data.ns("reactingreactions");
        // Neodymium magnets are rare earths, iron and boron sintered together.
        d.recipe("reaction_recipe/rare_earth_magnet", CRR + "reaction_recipe")
                .heat("superheated")
                .in(x(2, CRR + "rare_earth_dust"), "minecraft:iron_ingot", CRR + "borax")
                .out(CRR + "rare_earth_magnet")
                .rpm(64, 128);

        component(d, "composite_plating", CRR + "titanium_sheet", 3,
                deploy(CRR + "beryllium_oxide"), deploy("minecraft:netherite_ingot"), deploy(CRR + "hdpe_sheet"), press(), press());
        component(d, "control_unit", CRR + "circuit_board", 3,
                deploy(CRR + "circuit_board"), deploy(CRR + "rare_earth_magnet"), deploy(CRR + "aluminum_ingot"), deploy(CRR + "lithium_ingot"), cut());
        component(d, "servo_actuator", CRR + "manganese_steel", 3,
                deploy(CRR + "rare_earth_magnet"), deploy("create:precision_mechanism"), deploy(CRR + "nylon_fiber"), fill(CRR + "mineral_oil", 250), press());
        component(d, "ruby_lens", CRR + "ruby", 2,
                cut(), deploy(CRR + "reinforced_glass"), fill(CRR + "neon", 100), cut());

        chase(d, "composite_exo_helmet", CRR + "titanium_helmet", "minecraft:echo_shard", CRR + "nitrogen",
                "composite_plating", "ruby_lens", "control_unit");
        chase(d, "composite_exo_chestplate", CRR + "titanium_chestplate", "minecraft:elytra", CRR + "aerozine",
                "composite_plating", "composite_plating", "servo_actuator", "control_unit", "aerozine_thrusters");
        chase(d, "composite_exo_leggings", CRR + "titanium_leggings", "minecraft:heart_of_the_sea", CRR + "hydrogen",
                "composite_plating", "composite_plating", "servo_actuator", "servo_actuator", "control_unit");
        chase(d, "composite_exo_boots", CRR + "titanium_boots", "minecraft:heavy_core", CRR + "mineral_oil",
                "composite_plating", "servo_actuator", "servo_actuator", "control_unit");
        chase(d, "plasma_multitool", CRR + "titanium_pickaxe", "minecraft:nether_star", CRR + "drill_grease",
                "titanium_axe", "titanium_shovel", "mineral_drill_head_diamond", "servo_actuator", "control_unit");
        chase(d, "neon_blade", CRR + "titanium_sword", "minecraft:nether_star", CRR + "neon",
                "ruby_lens", "ruby_lens", "composite_plating", "control_unit");
    }

    private static void component(Data d, String name, String base, int loops, Step... steps) {
        d.recipe("sequenced_assembly/" + name, "create:sequenced_assembly")
                .ingredient(base)
                .out(CRR + name)
                .sequence(CRR + "incomplete_" + name, loops, (Object[]) steps);
    }

    /** One pass: the parts, a netherite ingot and a circuit board, the trophy, a priming fill and a final press. */
    private static void chase(Data d, String name, String base, String trophy, String fluid, String... parts) {
        Object[] steps = new Object[parts.length + 5];
        for (int i = 0; i < parts.length; i++) {
            steps[i] = deploy(CRR + parts[i]);
        }
        steps[parts.length] = deploy("minecraft:netherite_ingot");
        steps[parts.length + 1] = deploy(CRR + "circuit_board");
        steps[parts.length + 2] = deploy(trophy);
        steps[parts.length + 3] = fill(fluid, 250);
        steps[parts.length + 4] = press();
        d.recipe("sequenced_assembly/" + name, "create:sequenced_assembly")
                .ingredient(base)
                .out(CRR + name)
                .sequence(CRR + "incomplete_" + name, 1, steps);
    }
}
