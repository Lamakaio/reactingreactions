package com.koala.reactingreactions.datagen;

import static com.koala.reactingreactions.datagen.Data.*;

import com.koala.reactingreactions.content.toxic.ToxicDefaults;
import com.koala.reactingreactions.item.AcetyleneLampItem;
import com.koala.reactingreactions.item.ChemicalFlaskItem;

/** The mod's own crafting-table recipes (item, block and machine crafting, plus the standalone one-offs). */
final class CRRCraftingRecipes {
    private CRRCraftingRecipes() {
    }

    static void register(Data data) {
        flasks(data.ns("reactingreactions"));
        {
            Data d = data.ns("reactingreactions");
            d.shaped("crafting/acetylene_lamp", "equipment", res("reactingreactions:acetylene_lamp"),
                            "HGH", "GAG", "HGH")
                            .key('H', "reactingreactions:hdpe_sheet")
                            .key('G', "minecraft:glass")
                            .key('A', "reactingreactions:acetylene_bucket")
                            // It comes out with a full tank.
                            .field("result", """
                                    {"id": "reactingreactions:acetylene_lamp", "count": 1,
                                     "components": {"reactingreactions:fluid_tank": {"id": "reactingreactions:acetylene", "amount": %d}}}
                                    """.formatted(AcetyleneLampItem.DEFAULT_CAPACITY_MB));
            d.shaped("crafting/aerozine_thrusters", "equipment", res("reactingreactions:aerozine_thrusters"),
                            "HTH", "TAT", "HTH")
                            .key('H', "reactingreactions:hdpe_sheet")
                            .key('T', "reactingreactions:titanium_sheet")
                            .key('A', "reactingreactions:aerozine_bucket");
            d.shaped("crafting/airless_oven_controller", "misc", res("reactingreactions:airless_oven_controller", 1),
                    " P ", "WFW", " W ")
                    .key('P', "create:fluid_pipe")
                    .key('W', "reactingreactions:airless_oven_wall")
                    .key('F', "minecraft:blast_furnace");
            d.shaped("crafting/airless_oven_wall", "misc", res("reactingreactions:airless_oven_wall", 4),
                            " R ", "RSR", " R ")
                            .key('R', "reactingreactions:refractory_brick")
                            .key('S', "create:iron_sheet");
            d.shaped("crafting/aluminum_ingot_from_nuggets", "misc", res("reactingreactions:aluminum_ingot", 1),
                    "NNN", "NNN", "NNN")
                    .key('N', "reactingreactions:aluminum_nugget");
            d.shapeless("crafting/aluminum_nugget_from_ingot", "misc", res("reactingreactions:aluminum_nugget", 9),
                            "reactingreactions:aluminum_ingot");
            d.shaped("crafting/anchor_charm", "equipment", res("reactingreactions:anchor_charm"),
                    "N", "N", "M")
                    .key('N', "reactingreactions:nylon_fiber")
                    .key('M', "reactingreactions:manganese_steel");
            d.shaped("crafting/carbon_filter", "equipment", res("reactingreactions:carbon_filter"),
                    "P", "C", "P")
                    .key('P', "minecraft:paper")
                    .key('C', "reactingreactions:activated_carbon");
            d.shaped("crafting/digging_ring", "equipment", res("reactingreactions:digging_ring"),
                    " T ", "S S", " S ")
                    .key('T', "reactingreactions:titanium")
                    .key('S', "reactingreactions:steel_ingot");
            d.shaped("crafting/distillation_tower_controller", "misc", res("reactingreactions:distillation_tower_controller", 1),
                    " C ", "SCS", "SCS")
                    .key('C', "minecraft:copper_ingot")
                    .key('S', "reactingreactions:steel_sheet");
            d.shaped("crafting/distillation_tower_wall", "misc", res("reactingreactions:distillation_tower_wall", 4),
                    "C", "S")
                    .key('C', "minecraft:copper_ingot")
                    .key('S', "reactingreactions:steel_sheet");
            d.shaped("crafting/diving_fins", "equipment", res("reactingreactions:diving_fins"),
                    "N N", " T ")
                    .key('N', "reactingreactions:nylon_fiber")
                    .key('T', "reactingreactions:titanium_sheet");
            // Like the Reaction Chamber's: on steel casing, the vat's with its window glass, the controller with copper for its leads.
            d.shaped("crafting/electrolysis_vat_controller", "misc", res("reactingreactions:electrolysis_vat_controller", 1),
                    "C", "K", "W")
                    .key('C', "reactingreactions:steel_casing")
                    .key('K', "minecraft:copper_block")
                    .key('W', "reactingreactions:electrolysis_vat_wall");
            d.shaped("crafting/electrolysis_vat_wall", "misc", res("reactingreactions:electrolysis_vat_wall", 4),
                    " S ", "GCG", " S ")
                    .key('S', "reactingreactions:steel_sheet")
                    .key('G', "reactingreactions:reinforced_glass")
                    .key('C', "reactingreactions:steel_casing");
            d.shaped("crafting/fermentation_barrel", "misc", res("reactingreactions:fermentation_barrel", 1),
                    "C", "B", "C")
                    .key('C', "minecraft:copper_ingot")
                    .key('B', "minecraft:barrel");
            d.shaped("crafting/firework_rocket_ammonium_nitrate", "misc", res("minecraft:firework_rocket", 3),
                            "A", "P", "A")
                            .key('P', "minecraft:paper")
                            .key('A', "reactingreactions:ammonium_nitrate");
            d.shapeless("crafting/firework_star_magnesium", "misc", res("minecraft:firework_star"),
                            "reactingreactions:ammonium_nitrate", "reactingreactions:magnesium");
            d.shaped("crafting/flame_retardant_cloak", "equipment", res("reactingreactions:flame_retardant_cloak"),
                            "HNH", "NBN", "HNH")
                            .key('H', "reactingreactions:hdpe_sheet")
                            .key('B', "reactingreactions:bromine")
                            .key('N', "reactingreactions:nylon_fiber");
            d.shaped("crafting/gas_diffuser", "misc", res("reactingreactions:gas_diffuser", 1),
                            "S S", "SCS", "ATA")
                            .key('A', "create:andesite_alloy")
                            .key('C', "#aeronautics:burner_fire")
                            .key('T', "create:fluid_tank")
                            .key('S', "#c:plates/iron")
                            .ifModLoaded("aeronautics_bundled");
            d.shaped("crafting/gas_mask", "equipment", res("reactingreactions:gas_mask"),
                            "HNH", "AAA", "NAN")
                            .key('A', "reactingreactions:activated_carbon")
                            .key('N', "reactingreactions:nylon_fiber")
                            .key('H', "reactingreactions:hdpe_sheet");
            // A glass visor with an air hose: it breathes from a worn Create backtank, see ToxicPpe.
            d.shaped("crafting/oxygen_mask", "equipment", res("reactingreactions:oxygen_mask"),
                    "HGH", "NPN")
                    .key('H', "reactingreactions:hdpe_sheet")
                    .key('G', "minecraft:glass")
                    .key('N', "reactingreactions:nylon_fiber")
                    .key('P', "create:fluid_pipe");
            d.shaped("crafting/charging_pad", "misc", res("reactingreactions:charging_pad"),
                            "TCT", "SSS")
                            .key('S', "reactingreactions:steel_sheet")
                            .key('T', "create:fluid_tank")
                            .key('C', "reactingreactions:circuit_board");
            d.shaped("crafting/gold_steel_electrode", "misc", res("reactingreactions:gold_steel_electrode", 1),
                    "G", "S")
                    .key('G', "minecraft:gold_ingot")
                    .key('S', "reactingreactions:steel_ingot");
            d.shaped("crafting/graphite_electrode_from_carbon", "misc", res("reactingreactions:graphite_electrode", 1),
                            "CC", "CC")
                            .key('C', "reactingreactions:activated_carbon");
            d.shaped("crafting/graphite_electrode_from_coke", "misc", res("reactingreactions:graphite_electrode", 1),
                            "K", "K", "K")
                            .key('K', "reactingreactions:coal_coke");
            d.shaped("crafting/hdpe_sheet", "misc", res("reactingreactions:hdpe_sheet", 1),
                    "PPP", "PPP", "PPP")
                    .key('P', "reactingreactions:hdpe_pellets");
            d.shaped("crafting/helium_locket", "equipment", res("reactingreactions:helium_locket"),
                    " N ", "HBH")
                    .key('N', "reactingreactions:nylon_fiber")
                    .key('H', "reactingreactions:hdpe_sheet")
                    .key('B', "reactingreactions:helium_bucket");
            // Steel casing: the basis for the steel machines (Create's andesite casing plus steel).
            d.shaped("crafting/steel_casing", "misc", res("reactingreactions:steel_casing", 1),
                    "S", "C", "S")
                    .key('S', "reactingreactions:steel_sheet")
                    .key('C', "create:andesite_casing");
            // Reaction Chamber: steel walls around a steel casing; the controller carries Create's whisk.
            d.shaped("crafting/reaction_chamber_wall", "misc", res("reactingreactions:reaction_chamber_wall", 4),
                    " S ", "SCS", " S ")
                    .key('S', "reactingreactions:steel_sheet")
                    .key('C', "reactingreactions:steel_casing");
            // A whisk over a window over a wall: the small chamber, top to bottom.
            d.shaped("crafting/small_reaction_chamber", "misc", res("reactingreactions:small_reaction_chamber", 1),
                    "K", "G", "W")
                    .key('K', "create:whisk")
                    .key('G', "create:framed_glass")
                    .key('W', "reactingreactions:reaction_chamber_wall");
            d.shaped("crafting/reaction_chamber_controller", "misc", res("reactingreactions:reaction_chamber_controller", 1),
                    "C", "K", "W")
                    .key('C', "reactingreactions:steel_casing")
                    .key('K', "create:whisk")
                    .key('W', "reactingreactions:reaction_chamber_wall");
            // Plastic Pipe: a row of HDPE sheets, like Create's pipe from a sheet.
            d.shaped("crafting/plastic_pipe", "misc", res("reactingreactions:plastic_pipe", 16),
                    "HHH")
                    .key('H', "reactingreactions:hdpe_sheet");
            // Machine attachments, each laid out like the part it is.
            d.shaped("crafting/outlet_manifold", "misc", res("reactingreactions:outlet_manifold", 1),
                    "PPP", "SVS")
                    .key('P', "create:fluid_pipe")
                    .key('S', "reactingreactions:steel_sheet")
                    .key('V', "create:fluid_valve");
            d.shaped("crafting/expansion_tank", "misc", res("reactingreactions:expansion_tank", 1),
                    " S ", "STS", " P ")
                    .key('S', "reactingreactions:steel_sheet")
                    .key('T', "create:fluid_tank")
                    .key('P', "create:fluid_pipe");
            d.shaped("crafting/machine_gauge", "misc", res("reactingreactions:machine_gauge", 1),
                    "BGB", " C ", " S ")
                    .key('B', "create:brass_sheet")
                    .key('G', "minecraft:glass_pane")
                    .key('C', "minecraft:comparator")
                    .key('S', "reactingreactions:steel_sheet");
            d.shaped("crafting/gasket", "misc", res("reactingreactions:gasket", 1),
                    "NHN", "H H", "NHN")
                    .key('N', "create:brass_nugget")
                    .key('H', "reactingreactions:hdpe_sheet");
            // The valve, its filter and the flange that bolts it on.
            d.shaped("crafting/outlet_valve", "misc", res("reactingreactions:outlet_valve", 1),
                    "VFS")
                    .key('V', "create:fluid_valve")
                    .key('F', "create:smart_fluid_pipe")
                    .key('S', "reactingreactions:steel_sheet");
            d.shaped("crafting/circulation_pump", "misc", res("reactingreactions:circulation_pump", 1),
                    "SMS", " K ")
                    .key('S', "reactingreactions:steel_sheet")
                    .key('M', "create:mechanical_pump")
                    .key('K', "create:shaft");
            // Derrick: steel blocks around a brass casing, iron-bar trusses, a rotation-input controller and a drill head.
            d.shaped("crafting/derrick_block", "misc", res("reactingreactions:derrick_block", 4),
                    " S ", "SCS", " S ")
                    .key('S', "reactingreactions:steel_sheet")
                    .key('C', "create:brass_casing");
            d.shaped("crafting/derrick_truss", "misc", res("reactingreactions:derrick_truss", 4),
                    "S S", "BBB", "S S")
                    .key('S', "reactingreactions:steel_ingot")
                    .key('B', "minecraft:iron_bars");
            d.shaped("crafting/derrick_controller", "misc", res("reactingreactions:derrick_controller", 1),
                    "S", "D", "P")
                    .key('S', "create:shaft")
                    .key('D', "reactingreactions:derrick_block")
                    .key('P', "create:fluid_pipe");
            d.shaped("crafting/steam_turbine", "misc", res("reactingreactions:steam_turbine", 1),
                    "SAS", " K ")
                    .key('S', "reactingreactions:steel_sheet")
                    .key('A', "create:andesite_alloy")
                    .key('K', "create:shaft");
            d.shaped("crafting/laser_pointer", "misc", res("reactingreactions:laser_pointer", 1),
                    " R ", "SGS", " S ")
                    .key('R', "reactingreactions:ruby")
                    .key('G', "reactingreactions:reinforced_glass")
                    .key('S', "reactingreactions:steel_ingot");
            // Laid out like the tool: nozzle, valve, grip.
            d.shaped("crafting/purger", "misc", res("reactingreactions:purger", 1),
                    "P", "V", "H")
                    .key('P', "create:fluid_pipe")
                    .key('V', "create:fluid_valve")
                    .key('H', "reactingreactions:hdpe_sheet");
            d.shaped("crafting/drill_pipe", "misc", res("reactingreactions:drill_pipe", 4),
                    "S", "S", "S")
                    .key('S', "reactingreactions:steel_sheet");
            // Create Diesel Generators replaces oil drilling, so the oil head is not craftable with it.
            d.shaped("crafting/oil_drill_head", "misc", res("reactingreactions:oil_drill_head", 1),
                    "P", "D")
                    .key('P', "reactingreactions:drill_pipe")
                    .key('D', "create:mechanical_drill")
                .ifModNotLoaded("createdieselgenerators");
            d.shaped("crafting/mineral_drill_head_steel", "misc", res("reactingreactions:mineral_drill_head_steel", 1),
                    "S", "P", "S")
                    .key('S', "reactingreactions:steel_ingot")
                    .key('P', "reactingreactions:drill_pipe");
            // The top head: titanium teeth on a netherite core.
            d.shaped("crafting/mineral_drill_head_titanium", "misc", res("reactingreactions:mineral_drill_head_titanium", 1),
                    "TNT", " P ")
                    .key('T', "reactingreactions:titanium")
                    .key('N', "minecraft:netherite_ingot")
                    .key('P', "reactingreactions:drill_pipe");
            d.shaped("crafting/mineral_drill_head_diamond", "misc", res("reactingreactions:mineral_drill_head_diamond", 1),
                    "D", "P", "D")
                    .key('D', "minecraft:diamond")
                    .key('P', "reactingreactions:drill_pipe");
            // Toxic-compound gear: PPE from HDPE and nylon, and the two medicines that lower the toxicity gauge.
            d.shaped("crafting/chemical_gloves", "equipment", res("reactingreactions:chemical_gloves"),
                    "H H", " N ")
                    .key('H', "reactingreactions:hdpe_sheet")
                    .key('N', "reactingreactions:nylon_fiber");
            d.shaped("crafting/chemical_boots", "equipment", res("reactingreactions:chemical_boots"),
                    "H H", "N N")
                    .key('H', "reactingreactions:hdpe_sheet")
                    .key('N', "reactingreactions:nylon_fiber");
            d.shapeless("crafting/charcoal_tablet", "misc", res("reactingreactions:charcoal_tablet", 4),
                    "reactingreactions:activated_carbon", "minecraft:sugar");
            d.shaped("crafting/iodine_tablets", "misc", res("reactingreactions:iodine_tablets"),
                    "SIS")
                    .key('S', "minecraft:sugar")
                    .key('I', "reactingreactions:iodine");
            d.shapeless("crafting/antidote", "misc", res("reactingreactions:antidote", 1),
                    x(2, "reactingreactions:activated_carbon"), "minecraft:honey_bottle");
            // Atmospheric Scrubber and Floor Drain.
            d.shaped("crafting/atmospheric_scrubber", "misc", res("reactingreactions:atmospheric_scrubber", 1),
                    " B ", "ASA", " K ")
                    .key('B', "minecraft:iron_bars")
                    .key('A', "reactingreactions:activated_carbon")
                    .key('S', "reactingreactions:steel_casing")
                    .key('K', "create:shaft");
            d.shaped("crafting/floor_drain", "misc", res("reactingreactions:floor_drain", 1),
                    "BBB", " P ")
                    .key('B', "minecraft:iron_bars")
                    .key('P', "create:fluid_pipe");
            // A chimney: a grille over a column of pipe.
            d.shaped("crafting/gas_vent", "misc", res("reactingreactions:gas_vent", 2),
                    "B", "P", "P")
                    .key('B', "minecraft:iron_bars")
                    .key('P', "create:fluid_pipe");
            // Induction Heater: copper coils over steel plates; the connector adds a circuit board and more copper.
            d.shaped("crafting/induction_heater_plate", "misc", res("reactingreactions:induction_heater_plate", 4),
                    "CCC", "SSS")
                    .key('C', "minecraft:copper_ingot")
                    .key('S', "reactingreactions:steel_sheet");
            d.shaped("crafting/induction_heater_connector", "misc", res("reactingreactions:induction_heater_connector", 1),
                    " B ", "CPC")
                    .key('B', "reactingreactions:circuit_board")
                    .key('C', "minecraft:copper_ingot")
                    .key('P', "reactingreactions:induction_heater_plate");
            d.shaped("crafting/nickel_ingot_from_nuggets", "misc", res("reactingreactions:nickel_ingot", 1),
                    "NNN", "NNN", "NNN")
                    .key('N', "reactingreactions:nickel_nugget");
            d.shapeless("crafting/nickel_nugget_from_ingot", "misc", res("reactingreactions:nickel_nugget", 9),
                            "reactingreactions:nickel_ingot");
            // Vat terminal: a wall with a copper junction on its side, where the wire attaches.
            d.shaped("crafting/electrolysis_vat_terminal", "misc", res("reactingreactions:electrolysis_vat_terminal", 1),
                    "WC")
                    .key('C', "minecraft:copper_ingot")
                    .key('W', "reactingreactions:electrolysis_vat_wall");
            // Two ingots, so the electrode does not compete with the ingot-to-nuggets recipe.
            d.shaped("crafting/lead_electrode", "misc", res("reactingreactions:lead_electrode", 1),
                            "L", "L")
                            .key('L', "reactingreactions:lead_ingot");
            d.shaped("crafting/lead_ingot_from_nuggets", "misc", res("reactingreactions:lead_ingot", 1),
                    "NNN", "NNN", "NNN")
                    .key('N', "reactingreactions:lead_nugget");
            d.shapeless("crafting/lead_nugget_from_ingot", "misc", res("reactingreactions:lead_nugget", 9),
                            "reactingreactions:lead_ingot");
            d.shapeless("crafting/lithium_nugget_from_ingot", "misc", res("reactingreactions:lithium_nugget", 9),
                            "reactingreactions:lithium_ingot");
            d.shaped("crafting/lithium_ingot_from_nuggets", "misc", res("reactingreactions:lithium_ingot", 1),
                    "NNN", "NNN", "NNN")
                    .key('N', "reactingreactions:lithium_nugget");
            d.shaped("crafting/magnesium_knuckle", "equipment", res("reactingreactions:magnesium_knuckle"),
                    "MTM")
                    .key('M', "reactingreactions:magnesium")
                    .key('T', "reactingreactions:titanium");
            d.shaped("crafting/racing_anklet", "equipment", res("reactingreactions:racing_anklet"),
                    "T T", " N ")
                    .key('T', "reactingreactions:titanium")
                    .key('N', "reactingreactions:nylon_fiber");
            d.shaped("crafting/silicon_board", "misc", res("reactingreactions:silicon_board", 1),
                    "SHS")
                    .key('S', "reactingreactions:silica")
                    .key('H', "reactingreactions:hdpe_sheet");
            d.shaped("crafting/small_electrolyser", "misc", res("reactingreactions:small_electrolyser", 1),
                            "III", "GCG", "III")
                            .key('I', "minecraft:iron_ingot")
                            .key('G', "minecraft:copper_ingot")
                            .key('C', "create:copper_casing");
            d.shaped("crafting/spring_boots", "equipment", res("reactingreactions:spring_boots"),
                            "N N", "T T")
                            .key('T', "reactingreactions:titanium_sheet")
                            .key('N', "reactingreactions:nylon_fiber");
            d.shaped("crafting/titanium_axe", "equipment", res("reactingreactions:titanium_axe"),
                            "XX", "XS", " S")
                            .key('X', "reactingreactions:titanium")
                            .key('S', "minecraft:stick");
            d.shaped("crafting/titanium_boots", "equipment", res("reactingreactions:titanium_boots"),
                            "X X", "X X")
                            .key('X', "reactingreactions:titanium_sheet");
            d.shaped("crafting/titanium_bow", "equipment", res("reactingreactions:titanium_bow"),
                            " XN", "X N", " XN")
                            .key('X', "reactingreactions:titanium")
                            .key('N', "reactingreactions:nylon_fiber");
            d.shaped("crafting/titanium_chestplate", "equipment", res("reactingreactions:titanium_chestplate"),
                            "X X", "XXX", "XXX")
                            .key('X', "reactingreactions:titanium_sheet");
            d.shaped("crafting/titanium_helmet", "equipment", res("reactingreactions:titanium_helmet"),
                            "XXX", "X X")
                            .key('X', "reactingreactions:titanium_sheet");
            d.shaped("crafting/titanium_hoe", "equipment", res("reactingreactions:titanium_hoe"),
                            "XX", " S", " S")
                            .key('X', "reactingreactions:titanium")
                            .key('S', "minecraft:stick");
            d.shaped("crafting/titanium_leggings", "equipment", res("reactingreactions:titanium_leggings"),
                            "XXX", "X X", "X X")
                            .key('X', "reactingreactions:titanium_sheet");
            d.shaped("crafting/titanium_pickaxe", "equipment", res("reactingreactions:titanium_pickaxe"),
                            "XXX", " S ", " S ")
                            .key('X', "reactingreactions:titanium")
                            .key('S', "minecraft:stick");
            d.shaped("crafting/titanium_shovel", "equipment", res("reactingreactions:titanium_shovel"),
                            "X", "S", "S")
                            .key('X', "reactingreactions:titanium")
                            .key('S', "minecraft:stick");
            d.shaped("crafting/titanium_sword", "equipment", res("reactingreactions:titanium_sword"),
                            " X ", " X ", " S ")
                            .key('X', "reactingreactions:titanium")
                            .key('S', "minecraft:stick");
            d.shaped("crafting/tnt_ammonium_nitrate", "misc", res("minecraft:tnt"),
                            "ASA", "SAS", "ASA")
                            .key('A', "reactingreactions:ammonium_nitrate")
                            .key('S', "minecraft:sand");
            d.shaped("crafting/varnished_planks", "building", res("reactingreactions:varnished_planks", 8),
                    "PPP", "PVP", "PPP")
                    .key('P', "#minecraft:planks")
                    .key('V', "reactingreactions:varnish");
            // Yeast multiplies in sugar.
            d.shapeless("crafting/yeast_propagation", "misc", res("reactingreactions:yeast", 3),
                            "reactingreactions:yeast", "minecraft:sugar");
            d.shapeless("crafting/yeast", "misc", res("reactingreactions:yeast", 2),
                            x(2, "minecraft:sweet_berries"), "minecraft:sugar");
        }
    }

    /**
     * Chemical Flasks: a Spout pours 500 mB of any toxic fluid into a glass bottle, and the flask keeps which fluid it holds. A
     * flask of a flammable one takes gunpowder to become a Blast Flask of the same fluid.
     */
    private static void flasks(Data d) {
        for (Object[] row : ToxicDefaults.FLUIDS) {
            String name = (String) row[0];
            String fluid = "reactingreactions:" + name;
            String contents = "{\"reactingreactions:fluid_tank\": {\"id\": \"" + fluid + "\", \"amount\": " + ChemicalFlaskItem.AMOUNT_MB + "}}";
            d.raw("recipe", "filling/chemical_flask_" + name, """
                    {"type": "create:filling",
                     "ingredients": [{"item": "minecraft:glass_bottle"}, {"type": "neoforge:single", "amount": %d, "fluid": "%s"}],
                     "results": [{"id": "reactingreactions:chemical_flask", "components": %s}]}
                    """.formatted(ChemicalFlaskItem.AMOUNT_MB, fluid, contents));
            if ((Boolean) row[2]) {
                d.raw("recipe", "crafting/blast_flask_" + name, """
                        {"type": "minecraft:crafting_shapeless", "category": "equipment",
                         "ingredients": [{"type": "neoforge:components", "items": "reactingreactions:chemical_flask", "components": %s},
                                         {"item": "minecraft:gunpowder"}],
                         "result": {"id": "reactingreactions:blast_flask", "count": 1, "components": %s}}
                        """.formatted(contents, contents));
            }
        }
    }
}
