package com.koala.reactingreactions.datagen;

import static com.koala.reactingreactions.datagen.Data.*;

/** Recipes added to (or replacing ones in) other mods' namespaces. */
final class CompatRecipes {
    private CompatRecipes() {
    }

    static void register(Data data) {
        {
            Data d = data.ns("aeronautics").requiringMod("aeronautics");
            // Envelopes of nylon instead of wool: two nylon fibres over two sticks make white ones, and eight envelopes round a dye
            // take its colour (each recipe replaces Aeronautics' own, at the same id).
            d.shaped("white_envelope", "misc", res("aeronautics:white_envelope", 4),
                            "NS", "SN")
                            .key('N', "reactingreactions:nylon_fiber")
                            .key('S', "minecraft:stick")
                            .group("aeronautics:envelope");
            for (String colour : new String[] {"orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple",
                    "blue", "brown", "green", "red", "black"}) {
                d.shaped(colour + "_envelope", "misc", res("aeronautics:" + colour + "_envelope", 8),
                                "EEE", "EDE", "EEE")
                                .key('E', "#aeronautics:envelope")
                                .key('D', "#c:dyes/" + colour)
                                .group("aeronautics:envelope");
            }
        }
        {
            Data d = data.ns("create");
            d.recipe("crushing/asurine", "create:crushing")
                            .time(250)
                            .in("create:asurine")
                .out("reactingreactions:asurine_dust")
                .outChance("reactingreactions:asurine_dust", 0.5);
            d.recipe("crushing/asurine_recycling", "create:crushing")
                            .time(250)
                            .in("#create:stone_types/asurine")
                .out("reactingreactions:asurine_dust");
            d.recipe("crushing/crimsite", "create:crushing")
                            .time(250)
                            .in("create:crimsite")
                .out("reactingreactions:crimsite_dust")
                .outChance("reactingreactions:crimsite_dust", 0.5);
            d.recipe("crushing/crimsite_recycling", "create:crushing")
                            .time(250)
                            .in("#create:stone_types/crimsite")
                .out("reactingreactions:crimsite_dust");
            d.recipe("crushing/diorite", "create:crushing")
                            .time(350)
                            .in("minecraft:diorite")
                .out("reactingreactions:diorite_dust")
                .outChance("reactingreactions:diorite_dust", 0.5)
                .outChance("minecraft:quartz", 0.25);
            d.recipe("crushing/ochrum", "create:crushing")
                            .time(250)
                            .in("create:ochrum")
                .out("reactingreactions:ochrum_dust")
                .outChance("reactingreactions:ochrum_dust", 0.5);
            d.recipe("crushing/ochrum_recycling", "create:crushing")
                            .time(250)
                            .in("#create:stone_types/ochrum")
                .out("reactingreactions:ochrum_dust");
            d.recipe("crushing/polymetallic_nodule", "create:crushing")
                            .time(350)
                            .in("reactingreactions:polymetallic_nodule")
                .out("reactingreactions:crushed_polymetallic_nodule", 2)
                .outChance("minecraft:iron_nugget", 0.2)
                .outChance("create:copper_nugget", 0.15);
            d.recipe("crushing/tuff", "create:crushing")
                            .time(250)
                            .in("minecraft:tuff")
                .out("reactingreactions:tuff_dust")
                .outChance("reactingreactions:tuff_dust", 0.5);
            d.recipe("crushing/tuff_recycling", "create:crushing")
                            .time(350)
                            .in("#create:stone_types/tuff")
                .out("reactingreactions:tuff_dust");
            d.recipe("crushing/veridium", "create:crushing")
                            .time(250)
                            .in("create:veridium")
                .out("reactingreactions:veridium_dust")
                .outChance("reactingreactions:veridium_dust", 0.5);
            d.recipe("crushing/veridium_recycling", "create:crushing")
                            .time(250)
                            .in("#create:stone_types/veridium")
                .out("reactingreactions:veridium_dust");
            d.recipe("filling/bleach_bottle", "create:filling")
                            .in("minecraft:glass_bottle", fluid("reactingreactions:bleach", 250))
                .out("reactingreactions:bleach_bottle");
            d.recipe("splashing/gravel", "create:splashing")
                            .in("minecraft:gravel")
                .outChance("minecraft:flint", 0.25);
            d.recipe("splashing/red_sand", "create:splashing")
                            .in("minecraft:red_sand")
                // Borax is a desert evaporite: washing badlands sand is its primary source.
                .outChance("reactingreactions:borax", 0.1)
                .outChance("minecraft:dead_bush", 0.05);
            d.recipe("splashing/soul_sand", "create:splashing")
                            .in("minecraft:soul_sand")
                .outChance("minecraft:quartz", 4, 0.125);
        }
        {
            Data d = data.ns("offroad").requiringMod("offroad");
            d.shaped("large_tire", "misc", res("offroad:large_tire", 1),
                            "HBH", "BSB", "HBH")
                            .key('B', "create:belt_connector")
                            .key('S', "create:shaft")
                            .key('H', "reactingreactions:hdpe_sheet");
            d.shaped("monstrous_tire", "misc", res("offroad:monstrous_tire", 1),
                            "HKH", "KSK", "HKH")
                            .key('K', "minecraft:dried_kelp_block")
                            .key('S', "create:shaft")
                            .key('H', "reactingreactions:hdpe_sheet");
        }
        {
            Data d = data.ns("simulated").requiringMod("simulated");
            d.shaped("altitude_sensor", "misc", res("simulated:altitude_sensor", 1),
                            " P ", "TST", " A ")
                            .key('A', "create:andesite_casing")
                            .key('P', "minecraft:paper")
                            .key('S', "#c:plates/iron")
                            .key('T', "reactingreactions:circuit_board");
            d.shapeless("black_handle", "misc", res("simulated:black_handle", 1),
                            "simulated:iron_handle", "#c:dyes/black")
                            .group("simulated:handle_variants");
            d.shapeless("blue_handle", "misc", res("simulated:blue_handle", 1),
                            "simulated:iron_handle", "#c:dyes/blue")
                            .group("simulated:handle_variants");
            d.shapeless("brown_handle", "misc", res("simulated:brown_handle", 1),
                            "simulated:iron_handle", "#c:dyes/brown")
                            .group("simulated:handle_variants");
            d.shapeless("cyan_handle", "misc", res("simulated:cyan_handle", 1),
                            "simulated:iron_handle", "#c:dyes/cyan")
                            .group("simulated:handle_variants");
            d.shaped("gimbal_sensor", "misc", res("simulated:gimbal_sensor", 1),
                            " C ", "TGT", " B ")
                            .key('B', "create:brass_casing")
                            .key('C', "minecraft:compass")
                            .key('G', "simulated:gyroscopic_mechanism")
                            .key('T', "reactingreactions:circuit_board");
            d.shapeless("gray_handle", "misc", res("simulated:gray_handle", 1),
                            "simulated:iron_handle", "#c:dyes/gray")
                            .group("simulated:handle_variants");
            d.shapeless("green_handle", "misc", res("simulated:green_handle", 1),
                            "simulated:iron_handle", "#c:dyes/green")
                            .group("simulated:handle_variants");
            d.shaped("laser_sensor", "misc", res("simulated:laser_sensor", 1),
                            " G ", "TAT", " C ")
                            .key('A', "#simulated:laser_point_lens")
                            .key('C', "create:andesite_casing")
                            .key('G', "minecraft:tinted_glass")
                            .key('T', "reactingreactions:circuit_board");
            d.shapeless("light_blue_handle", "misc", res("simulated:light_blue_handle", 1),
                            "simulated:iron_handle", "#c:dyes/light_blue")
                            .group("simulated:handle_variants");
            d.shapeless("light_gray_handle", "misc", res("simulated:light_gray_handle", 1),
                            "simulated:iron_handle", "#c:dyes/light_gray")
                            .group("simulated:handle_variants");
            d.shapeless("lime_handle", "misc", res("simulated:lime_handle", 1),
                            "simulated:iron_handle", "#c:dyes/lime")
                            .group("simulated:handle_variants");
            d.shapeless("magenta_handle", "misc", res("simulated:magenta_handle", 1),
                            "simulated:iron_handle", "#c:dyes/magenta")
                            .group("simulated:handle_variants");
            d.shaped("optical_sensor", "misc", res("simulated:optical_sensor", 1),
                            " A ", "TCT", " B ")
                            .key('A', "#c:gems/amethyst")
                            .key('B', "create:brass_casing")
                            .key('C', "create:electron_tube")
                            .key('T', "reactingreactions:circuit_board");
            d.shapeless("orange_handle", "misc", res("simulated:orange_handle", 1),
                            "simulated:iron_handle", "#c:dyes/orange")
                            .group("simulated:handle_variants");
            d.shapeless("pink_handle", "misc", res("simulated:pink_handle", 1),
                            "simulated:iron_handle", "#c:dyes/pink")
                            .group("simulated:handle_variants");
            d.shapeless("purple_handle", "misc", res("simulated:purple_handle", 1),
                            "simulated:iron_handle", "#c:dyes/purple")
                            .group("simulated:handle_variants");
            d.shapeless("red_handle", "misc", res("simulated:red_handle", 1),
                            "simulated:iron_handle", "#c:dyes/red")
                            .group("simulated:handle_variants");
            d.recipe("sequenced_assembly/engine_assembly", "create:sequenced_assembly")
                            .ingredient("#c:plates/titanium")
                .outChance("simulated:engine_assembly", 50.0)
                .outChance("create:iron_sheet", 16.0)
                .outChance("minecraft:iron_nugget", 15.0)
                .outChance("create:industrial_iron_block", 10.0)
                .outChance("minecraft:iron_bars", 8.0)
                .out("minecraft:iron_helmet")
                            .sequence("simulated:incomplete_engine_assembly", 8, cut(), press());
            d.recipe("sequenced_assembly/gyroscopic_mechanism", "create:sequenced_assembly")
                            .ingredient("#c:plates/titanium")
                .outChance("simulated:gyroscopic_mechanism", 200.0)
                .outChance("create:iron_sheet", 8.0)
                .outChance("create:andesite_alloy", 8.0)
                .outChance("create:brass_nugget", 3.0)
                .outChance("create:crushed_raw_iron", 2.0)
                .out("minecraft:compass")
                            .sequence("simulated:incomplete_gyroscopic_mechanism", 5, deploy("create:cogwheel"), deploy("create:shaft"), deploy("#c:nuggets/brass"));
            d.shaped("velocity_sensor", "misc", res("simulated:velocity_sensor", 1),
                            " P ", "TBT", " A ")
                            .key('A', "create:andesite_casing")
                            .key('B', "minecraft:barrel")
                            .key('P', "create:propeller")
                            .key('T', "reactingreactions:circuit_board");
            d.shapeless("white_handle", "misc", res("simulated:white_handle", 1),
                            "simulated:iron_handle", "#c:dyes/white")
                            .group("simulated:handle_variants");
            d.shapeless("yellow_handle", "misc", res("simulated:yellow_handle", 1),
                            "simulated:iron_handle", "#c:dyes/yellow")
                            .group("simulated:handle_variants");
        }
        // A bit of steel in the bigger engines and the electrical parts (the basic ones stay as they are, so neither mod is gated
        // early). Each replaces the mod's own recipe, at the same id.
        {
            Data d = data.ns("createdieselgenerators").requiringMod("createdieselgenerators");
            d.shaped("crafting/large_diesel_engine", "misc", res("createdieselgenerators:large_diesel_engine", 1),
                            " A ", "SDS", " B ")
                            .key('A', "reactingreactions:steel_sheet")
                            .key('D', "createdieselgenerators:diesel_engine")
                            .key('B', "minecraft:polished_blackstone_slab")
                            .key('S', "#c:plates/brass");
            d.shaped("crafting/huge_diesel_engine", "misc", res("createdieselgenerators:huge_diesel_engine", 1),
                            "AFA", "SES", "PBP")
                            .key('E', "create:steam_engine")
                            .key('B', "#c:storage_blocks/brass")
                            .key('S', "#c:plates/brass")
                            .key('F', "minecraft:flint_and_steel")
                            .key('A', "reactingreactions:steel_sheet")
                            .key('P', "create:fluid_pipe");
            d.shaped("crafting/engine_turbocharger", "misc", res("createdieselgenerators:engine_turbocharger", 1),
                            "AZF", "SPS", "AZA")
                            .key('A', "create:andesite_alloy")
                            .key('S', "reactingreactions:steel_sheet")
                            .key('Z', "#c:ingots/zinc")
                            .key('P', "create:propeller")
                            .key('F', "create:fluid_pipe");
        }
        {
            Data d = data.ns("electroenergetics").requiringMod("electroenergetics");
            d.shaped("crafting/stator", "misc", res("electroenergetics:stator", 1),
                            "AS", "MS", "AS")
                            .key('A', "create:andesite_alloy")
                            .key('M', "electroenergetics:magnet")
                            .key('S', "reactingreactions:steel_sheet");
            // Transformer cores are laminated electrical steel.
            d.recipe("stonecutting/transformer_core_lamination", "minecraft:stonecutting")
                            .ingredient("reactingreactions:steel_sheet")
                            .field("result", "{\"id\":\"electroenergetics:transformer_core_lamination\",\"count\":1}");
        }
    }
}
