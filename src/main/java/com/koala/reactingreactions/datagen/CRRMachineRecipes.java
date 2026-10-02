package com.koala.reactingreactions.datagen;

import static com.koala.reactingreactions.datagen.Data.*;

/** Recipes for this mod's own machines: electrolysis, distillation, the airless oven and the fermenters. */
final class CRRMachineRecipes {
    private CRRMachineRecipes() {
    }

    static void register(Data data) {
        {
            Data d = data.ns("reactingreactions");
            d.recipe("electrolysis_recipe/aluminum_hydroxide_electrolysis", "reactingreactions:electrolysis_recipe")
                            .time(200)
                            .minVoltage(2000.0)
                            .electrodes("reactingreactions:graphite_electrode")
                            .in("reactingreactions:aluminum_hydroxide")
                .out("reactingreactions:aluminum_ingot");
            d.recipe("electrolysis_recipe/bleach", "reactingreactions:electrolysis_recipe")
                            .time(200)
                            .minVoltage(150.0)
                            .in(fluid("reactingreactions:strong_brine", 1000))
                .fluidOut("reactingreactions:bleach", 500);
            // Dilute brine makes weak bleach at low voltage, as in on-site water-treatment cells: an early bleach source.
            d.recipe("electrolysis_recipe/bleach_from_weak_brine", "reactingreactions:electrolysis_recipe")
                            .time(200)
                            .minVoltage(60.0)
                            .in(fluid("reactingreactions:weak_brine", 1000))
                .fluidOut("reactingreactions:bleach", 50);
            d.recipe("electrolysis_recipe/lye", "reactingreactions:electrolysis_recipe")
                            .time(200)
                            .minVoltage(200.0)
                            .electrodes("reactingreactions:gold_steel_electrode")
                            .in(fluid("reactingreactions:strong_brine", 1000))
                .fluidOut("reactingreactions:lye", 500);
            d.recipe("electrolysis_recipe/magnesium", "reactingreactions:electrolysis_recipe")
                            .time(200)
                            .minVoltage(400.0)
                            .electrodes("reactingreactions:graphite_electrode")
                            .in(fluid("reactingreactions:magnesium_chloride", 500))
                .out("reactingreactions:magnesium");
            d.recipe("electrolysis_recipe/ochrum_dust", "reactingreactions:electrolysis_recipe")
                            .time(200)
                            .minVoltage(600.0)
                            .electrodes("reactingreactions:gold_steel_electrode")
                            .in("reactingreactions:ochrum_dust")
                .out("minecraft:gold_nugget", 3)
                .out("reactingreactions:lead_nugget", 3);
            // Electrowinning: ore dust leached in sulfuric acid, metal plated out on lead anodes. Most of the acid is regenerated.
            d.recipe("electrolysis_recipe/zinc_electrowinning", "reactingreactions:electrolysis_recipe")
                            .time(200)
                            .minVoltage(300.0)
                            .electrodes("reactingreactions:lead_electrode")
                            .in("reactingreactions:asurine_dust", fluid("reactingreactions:sulfuric_acid", 250))
                .out("create:zinc_nugget", 6)
                .outChance("reactingreactions:rare_earth_dust", 0.35)
                .fluidOut("reactingreactions:sulfuric_acid", 200);
            d.recipe("electrolysis_recipe/copper_electrowinning", "reactingreactions:electrolysis_recipe")
                            .time(200)
                            .minVoltage(200.0)
                            .electrodes("reactingreactions:lead_electrode")
                            .in("reactingreactions:veridium_dust", fluid("reactingreactions:sulfuric_acid", 250))
                .out("minecraft:copper_ingot")
                .fluidOut("reactingreactions:sulfuric_acid", 200);
            d.recipe("electrolysis_recipe/water_electrolysis", "reactingreactions:electrolysis_recipe")
                            .time(200)
                            .minVoltage(120.0)
                            .electrodes("reactingreactions:graphite_electrode", "reactingreactions:gold_steel_electrode")
                            .in(fluid("minecraft:water", 1000))
                .fluidOut("reactingreactions:hydrogen", 1000)
                .fluidOut("reactingreactions:oxygen", 500);
            d.recipe("electrolysis_recipe/purified_water_electrolysis", "reactingreactions:electrolysis_recipe")
                            .time(200)
                            .minVoltage(100.0)
                            .electrodes("reactingreactions:graphite_electrode", "reactingreactions:gold_steel_electrode")
                            .in(fluid("reactingreactions:purified_water", 1000))
                .fluidOut("reactingreactions:hydrogen", 1200)
                .fluidOut("reactingreactions:oxygen", 600);
            d.recipe("electrolysis_recipe/water_to_oxygen", "reactingreactions:electrolysis_recipe")
                            .time(200)
                            .minVoltage(80.0)
                            .in(fluid("minecraft:water", 1000))
                .fluidOut("reactingreactions:oxygen", 500);
            // With Create Diesel Generators, distillation runs on its own tower instead.
            d.recipe("distillation_recipe/crude_oil_fractionation", "reactingreactions:distillation_recipe")
                            .heat("heated")
                            .time(400)
                            .in(fluid("reactingreactions:crude_oil", 1000))
                .fluidOut("reactingreactions:naphtha", 400)
                .fluidOut("reactingreactions:lpg", 300)
                .fluidOut("reactingreactions:diesel", 300)
                .ifModNotLoaded("createdieselgenerators");
            // Its diesel output is split evenly into diesel and gasoline, like Create Diesel Generators' own recipe. It takes CDG's
            // crude oil: with CDG installed, ours has no source.
            d.recipe("distillation_recipe/crude_oil_fractionation_cdg", "createdieselgenerators:distillation")
                            .heat("heated")
                            .time(400)
                            .in(fluidStack("createdieselgenerators:crude_oil", 1000))
                .fluidOut("reactingreactions:naphtha", 400)
                .fluidOut("createdieselgenerators:diesel", 150)
                .fluidOut("createdieselgenerators:gasoline", 150)
                .fluidOut("reactingreactions:lpg", 300)
                .ifModLoaded("createdieselgenerators");
            // Air separation: compressed air (drained from a spinning Create backtank) is cold-distilled, no heat, into
            // roughly air's real make-up. The oil-free source of nitrogen for the ammonia chain.
            d.recipe("distillation_recipe/air_separation", "reactingreactions:distillation_recipe")
                            .time(400)
                            .in(fluid("reactingreactions:compressed_air", 1000))
                .fluidOut("reactingreactions:nitrogen", 780)
                .fluidOut("reactingreactions:oxygen", 210)
                .fluidOut("reactingreactions:neon", 5)
                .dieselGeneratorsTwin("createdieselgenerators:distillation");
            d.recipe("distillation_recipe/hydrocarbon_gas", "reactingreactions:distillation_recipe")
                            .heat("superheated")
                            .time(400)
                            .in(fluid("reactingreactions:hydrocarbon_gas", 500))
                .fluidOut("reactingreactions:methane", 100)
                .fluidOut("reactingreactions:ethane", 50)
                .fluidOut("reactingreactions:propane", 50)
                .fluidOut("reactingreactions:butane", 50)
                .fluidOut("reactingreactions:solvent", 250)
                .dieselGeneratorsTwin("createdieselgenerators:distillation");
            d.recipe("distillation_recipe/lpg_separation", "reactingreactions:distillation_recipe")
                            .heat("heated")
                            .time(400)
                            .in(fluid("reactingreactions:lpg", 500))
                .fluidOut("reactingreactions:contaminated_hydrocarbon_gas", 300)
                .fluidOut("reactingreactions:nitrogen", 100)
                .fluidOut("reactingreactions:helium", 50)
                .fluidOut("reactingreactions:neon", 50)
                .dieselGeneratorsTwin("createdieselgenerators:distillation");
            d.recipe("distillation_recipe/naphtha_fractionation", "reactingreactions:distillation_recipe")
                            .heat("heated")
                            .time(400)
                            .in(fluid("reactingreactions:naphtha", 500))
                .fluidOut("reactingreactions:mineral_oil", 300)
                .fluidOut("reactingreactions:solvent", 100)
                .dieselGeneratorsTwin("createdieselgenerators:distillation");
            d.recipe("distillation_recipe/resin", "reactingreactions:distillation_recipe")
                            .heat("heated")
                            .time(400)
                            .in(fluid("reactingreactions:resin", 500))
                .fluidOut("reactingreactions:naphtha", 30)
                .fluidOut("reactingreactions:solvent", 50)
                .dieselGeneratorsTwin("createdieselgenerators:distillation");
            // Brine: each step runs heated at 400 ticks, or superheated ten times faster. The fast recipes give off a tenth
            // of the steam, so superheat speeds up the brine without multiplying steam (and turbine power) per second.
            d.recipe("distillation_recipe/weak_brine", "reactingreactions:distillation_recipe")
                            .heat("heated")
                            .time(400)
                            .in(fluid("minecraft:water", 1000))
                .fluidOut("reactingreactions:weak_brine", 10)
                .fluidOut("reactingreactions:steam", 990)
                .dieselGeneratorsTwin("createdieselgenerators:distillation");
            d.recipe("distillation_recipe/weak_brine_superheated", "reactingreactions:distillation_recipe")
                            .heat("superheated")
                            .time(40)
                            .in(fluid("minecraft:water", 1000))
                .fluidOut("reactingreactions:weak_brine", 10)
                .fluidOut("reactingreactions:steam", 99)
                .dieselGeneratorsTwin("createdieselgenerators:distillation");
            d.recipe("distillation_recipe/strong_brine", "reactingreactions:distillation_recipe")
                            .heat("heated")
                            .time(400)
                            .in(fluid("reactingreactions:weak_brine", 1000))
                .fluidOut("reactingreactions:strong_brine", 100)
                .fluidOut("reactingreactions:steam", 900)
                .dieselGeneratorsTwin("createdieselgenerators:distillation");
            d.recipe("distillation_recipe/strong_brine_superheated", "reactingreactions:distillation_recipe")
                            .heat("superheated")
                            .time(40)
                            .in(fluid("reactingreactions:weak_brine", 1000))
                .fluidOut("reactingreactions:strong_brine", 100)
                .fluidOut("reactingreactions:steam", 90)
                .dieselGeneratorsTwin("createdieselgenerators:distillation");
            d.recipe("airless_oven_recipe/carbon_monoxide_from_wood", "reactingreactions:airless_oven_recipe")
                            .time(400)
                            .in("#minecraft:logs_that_burn")
                .fluidOut("reactingreactions:carbon_monoxide", 400)
                .out("minecraft:charcoal");
            // Bone char: bones pyrolysed without air give a fine filter carbon (and a little ammonia, from bone oil).
            d.recipe("airless_oven_recipe/bone_char", "reactingreactions:airless_oven_recipe")
                            .time(300)
                            .in(x(3, "minecraft:bone"))
                .out("reactingreactions:activated_carbon")
                .fluidOut("reactingreactions:ammonia", 50);
            // Lime kiln: coke burns the limestone to quicklime, which gives off its carbon dioxide.
            d.recipe("airless_oven_recipe/quicklime", "reactingreactions:airless_oven_recipe")
                            .time(400)
                            // Any calcium carbonate rock: Create's limestone, or vanilla's calcite and dripstone.
                            .in(x(2, any("create:limestone", "minecraft:calcite", "minecraft:dripstone_block")), "reactingreactions:coal_coke")
                .fluidOut("reactingreactions:carbon_dioxide", 250)
                .out("reactingreactions:quicklime", 2);
            d.recipe("airless_oven_recipe/coal_coke", "reactingreactions:airless_oven_recipe")
                            .time(600)
                            .in(x(2, "minecraft:coal"))
                .out("reactingreactions:coal_coke")
                .out("reactingreactions:coal_coke")
                // Coking also drives off coal tar, whose light oil is naphtha.
                .fluidOut("reactingreactions:naphtha", 50)
                .outChance("reactingreactions:slag", 0.3);
        }
    }
}
