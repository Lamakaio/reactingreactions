package com.koala.reactingreactions.datagen;

import static com.koala.reactingreactions.datagen.Data.*;

/** Chemistry and materials processing on Create's machines (mixing in the basin, crushing, pressing, casting, sequenced assembly). */
final class CRRProcessingRecipes {
    private CRRProcessingRecipes() {
    }

    static void register(Data data) {
        {
            Data d = data.ns("reactingreactions");
            // Steel without gas, so it stays on Create's mixer: the Reaction Chamber (gas reactions only) needs steel casing,
            // so steel itself must not depend on the chamber. The oxygen route (pig_iron in the chamber) is the faster one.
            // Pig iron is iron melted down with coke, as in a blast furnace: every steel route starts at the Airless Oven.
            // Drilled crimsite comes out as dust, which works too (or is kept for titanium).
            for (String[] ore : new String[][] {{"pig_iron", "minecraft:iron_ingot"}, {"pig_iron_from_crimsite", "create:crimsite"},
                    {"pig_iron_from_crimsite_dust", "reactingreactions:crimsite_dust"}}) {
                d.recipe("mixing/" + ore[0], "create:mixing")
                        .heat("heated")
                        .time(200)
                        .in(x(2, ore[1]), "reactingreactions:coal_coke")
                        .out("reactingreactions:pig_iron", 2);
            }
            d.recipe("mixing/steel_from_coke", "create:mixing")
                    .heat("superheated")
                    .time(400)
                    .in("reactingreactions:pig_iron", "reactingreactions:coal_coke")
                    .out("reactingreactions:steel_ingot")
                    .outChance("reactingreactions:slag", 0.4);
            // With quicklime as a flux the impurities leave as slag: faster, and more of the iron ends up as steel (2.5 on
            // average). Create's basin prefers the recipe with more ingredients, so this runs whenever quicklime is in the basin.
            d.recipe("mixing/steel_with_lime_flux", "create:mixing")
                    .heat("superheated")
                    .time(200)
                    .in(x(2, "reactingreactions:pig_iron"), "reactingreactions:coal_coke", "reactingreactions:quicklime")
                    .out("reactingreactions:steel_ingot", 2)
                    .outChance("reactingreactions:steel_ingot", 0.5)
                    .out("reactingreactions:slag");
            d.recipe("mixing/activated_carbon", "create:mixing")
                            .heat("superheated")
                            .in("reactingreactions:coal_coke")
                .out("reactingreactions:activated_carbon");
            d.recipe("mixing/aluminum_hydroxide", "create:mixing")
                            .in("reactingreactions:alumina_dust", fluid("reactingreactions:lye", 250))
                .out("reactingreactions:aluminum_hydroxide", 2);
            d.recipe("mixing/biomass", "create:mixing")
                            .time(200)
                            .in(x(4, "#reactingreactions:plant_matter"), fluid("minecraft:water", 250))
                .out("reactingreactions:biomass", 2);
            d.recipe("mixing/black_paint", "create:mixing")
                            .in("reactingreactions:activated_carbon", fluid("reactingreactions:seed_oil", 250))
                .out("reactingreactions:black_paint", 16);
            d.recipe("mixing/blue_paint", "create:mixing")
                            .in("reactingreactions:asurine_dust", fluid("reactingreactions:seed_oil", 250))
                .out("reactingreactions:blue_paint", 16);
            d.recipe("mixing/bromine", "create:mixing")
                            .in(fluid("reactingreactions:strong_brine", 1000), fluid("reactingreactions:bleach", 250))
                .out("reactingreactions:bromine", 2)
                .out("reactingreactions:salt", 2);
            // Nitric acid nitrates the charcoal-and-sulfur mix: a factory route to gunpowder.
            d.recipe("mixing/gunpowder", "create:mixing")
                            .in("reactingreactions:sulfur_dust", "minecraft:charcoal", fluid("reactingreactions:nitric_acid", 250))
                .out("minecraft:gunpowder", 4);
            // Vinegar is a mild acid: it patinates copper blocks.
            d.recipe("mixing/copper_patina", "create:mixing")
                            .in("minecraft:copper_block", fluid("reactingreactions:white_vinegar", 250))
                .out("minecraft:oxidized_copper");
            d.recipe("mixing/brown_paint", "create:mixing")
                            .in("reactingreactions:scoria_dust", fluid("reactingreactions:seed_oil", 250))
                .out("reactingreactions:brown_paint", 16);
            // CaO + 3C -> CaC2 + CO: quicklime reduced by coke at white heat.
            d.recipe("mixing/calcium_carbide", "create:mixing")
                            .heat("superheated")
                            .in("reactingreactions:quicklime", "reactingreactions:coal_coke")
                .out("reactingreactions:calcium_carbide", 2)
                .fluidOut("reactingreactions:carbon_monoxide", 250);
            // Refractory brick: fireclay tempered with crushed rock, fired hot enough to line the Airless Oven.
            d.recipe("mixing/refractory_brick", "create:mixing")
                            .heat("heated")
                            .in(x(2, "minecraft:clay_ball"), "reactingreactions:diorite_dust")
                .out("reactingreactions:refractory_brick", 2);
            d.recipe("mixing/crude_oil_from_shale", "create:mixing")
                            .heat("heated")
                            .time(300)
                            .in("reactingreactions:oil_shale")
                .fluidOut("reactingreactions:crude_oil", 500)
                .outChance("reactingreactions:slag", 0.5);
            d.recipe("mixing/cyan_paint", "create:mixing")
                            .in("reactingreactions:veridium_dust", "reactingreactions:asurine_dust", fluid("reactingreactions:seed_oil", 250))
                .out("reactingreactions:cyan_paint", 16);
            d.recipe("mixing/granite_dust", "create:mixing")
                            .heat("superheated")
                            .in("reactingreactions:granite_dust")
                .out("reactingreactions:alumina_dust")
                .outChance("minecraft:quartz", 0.5);
            d.recipe("mixing/gray_paint", "create:mixing")
                            .in("reactingreactions:tuff_dust", fluid("reactingreactions:seed_oil", 250))
                .out("reactingreactions:gray_paint", 16);
            d.recipe("mixing/green_paint", "create:mixing")
                            .in("reactingreactions:veridium_dust", fluid("reactingreactions:seed_oil", 250))
                .out("reactingreactions:green_paint", 16);
            d.recipe("mixing/light_blue_paint", "create:mixing")
                            .in("reactingreactions:asurine_dust", "reactingreactions:titanium_dioxide", fluid("reactingreactions:seed_oil", 250))
                .out("reactingreactions:light_blue_paint", 16);
            d.recipe("mixing/light_gray_paint", "create:mixing")
                            .in("reactingreactions:diorite_dust", fluid("reactingreactions:seed_oil", 250))
                .out("reactingreactions:light_gray_paint", 16);
            d.recipe("mixing/lime_paint", "create:mixing")
                            .in("reactingreactions:veridium_dust", "reactingreactions:sulfur_dust", fluid("reactingreactions:seed_oil", 250))
                .out("reactingreactions:lime_paint", 16);
            d.recipe("mixing/lithium_brine", "create:mixing")
                            .heat("heated")
                            .in(fluid("reactingreactions:strong_brine", 1000))
                .fluidOut("reactingreactions:lithium_brine", 400)
                .out("reactingreactions:salt", 4)
                .outChance("reactingreactions:borax", 0.4);
            d.recipe("mixing/lithium_nuggets", "create:mixing")
                            .heat("superheated")
                            .in(fluid("reactingreactions:lithium_brine", 500))
                .out("reactingreactions:lithium_nugget", 2)
                .outChance("reactingreactions:salt", 2, 0.5);
            d.recipe("mixing/magenta_paint", "create:mixing")
                            .in("reactingreactions:crimsite_dust", "reactingreactions:asurine_dust", fluid("reactingreactions:seed_oil", 250))
                .out("reactingreactions:magenta_paint", 16);
            d.recipe("mixing/magnesium_chloride", "create:mixing")
                            .in(fluid("reactingreactions:strong_brine", 1000), fluid("reactingreactions:lye", 250))
                .fluidOut("reactingreactions:magnesium_chloride", 500)
                .out("reactingreactions:salt", 2);
            d.recipe("mixing/manganese_steel", "create:mixing")
                            .heat("superheated")
                            .in("reactingreactions:steel_ingot", "reactingreactions:manganese")
                .out("reactingreactions:manganese_steel", 2);
            d.recipe("mixing/orange_paint", "create:mixing")
                            .in("reactingreactions:crimsite_dust", fluid("reactingreactions:seed_oil", 250))
                .out("reactingreactions:orange_paint", 16);
            d.recipe("mixing/pink_paint", "create:mixing")
                            .in("reactingreactions:granite_dust", fluid("reactingreactions:seed_oil", 250))
                .out("reactingreactions:pink_paint", 16);
            d.recipe("mixing/polymetallic_nodule_refining", "create:mixing")
                            .heat("heated")
                            .in("reactingreactions:crushed_polymetallic_nodule")
                .out("reactingreactions:manganese")
                .outChance("reactingreactions:nickel_ingot", 0.5)
                .outChance("create:zinc_nugget", 0.3)
                .outChance("reactingreactions:rare_earth_dust", 0.3);
            d.recipe("mixing/purified_water", "create:mixing")
                            .in("reactingreactions:aluminum_hydroxide", fluid("minecraft:water", 1000), fluid("reactingreactions:bleach", 50))
                .fluidOut("reactingreactions:purified_water", 1000);
            d.recipe("mixing/purified_water_premium", "create:mixing")
                            .in("reactingreactions:aluminum_hydroxide", "reactingreactions:activated_carbon", fluid("minecraft:water", 1000), fluid("reactingreactions:bleach", 50))
                .fluidOut("reactingreactions:purified_water", 1500);
            d.recipe("mixing/purple_paint", "create:mixing")
                            .in("reactingreactions:asurine_dust", "reactingreactions:iron_oxide", fluid("reactingreactions:seed_oil", 250))
                .out("reactingreactions:purple_paint", 16);
            d.recipe("mixing/red_paint", "create:mixing")
                            .in("reactingreactions:iron_oxide", fluid("reactingreactions:seed_oil", 250))
                .out("reactingreactions:red_paint", 16);
            d.recipe("mixing/reinforced_glass", "create:mixing")
                            .heat("superheated")
                            .in("reactingreactions:silica", "reactingreactions:borax", "reactingreactions:bromine")
                .out("reactingreactions:reinforced_glass");
            d.recipe("mixing/resin_extraction", "create:mixing")
                            .in("minecraft:spruce_log")
                .fluidOut("reactingreactions:resin", 100);
            // Rock dust pressed back into its rock. Crushing gives 1.5 dust on average, so 2 per block cannot loop.
            for (String[] rock : new String[][] {{"asurine", "create:asurine"}, {"crimsite", "create:crimsite"}, {"ochrum", "create:ochrum"},
                    {"veridium", "create:veridium"}, {"scoria", "create:scoria"}, {"tuff", "minecraft:tuff"}, {"granite", "minecraft:granite"},
                    {"diorite", "minecraft:diorite"}}) {
                d.recipe("compacting/" + rock[0] + "_from_dust", "create:compacting")
                        .in(x(2, "reactingreactions:" + rock[0] + "_dust"))
                        .out(rock[1]);
            }
            // Pressed in a basin. Create Diesel Generators' plant oil replaces it when installed.
            d.recipe("compacting/seed_oil", "create:compacting")
                            .in(x(4, "#c:seeds"))
                .fluidOut("reactingreactions:seed_oil", 250)
                .ifModNotLoaded("createdieselgenerators");
            d.recipe("mixing/coolant", "create:mixing")
                            .in(fluid("minecraft:water", 750), fluid("reactingreactions:ethanol", 250))
                .fluidOut("reactingreactions:coolant", 1000);
            d.recipe("mixing/silica", "create:mixing")
                            .heat("superheated")
                            .in(x(2, "minecraft:sand"), fluid("reactingreactions:solvent", 50))
                .out("reactingreactions:silica", 2);
            // Beryllium and chromium by acid digestion of ore dusts, for the gem recipes.
            d.recipe("mixing/beryllium_oxide", "create:mixing")
                            .heat("superheated")
                            .in("reactingreactions:alumina_dust", "reactingreactions:silica", fluid("reactingreactions:sulfuric_acid", 250))
                .out("reactingreactions:beryllium_oxide");
            d.recipe("mixing/chromium_dust", "create:mixing")
                            .heat("superheated")
                            .in("reactingreactions:tuff_dust", fluid("reactingreactions:sulfuric_acid", 250))
                .out("reactingreactions:chromium_dust");
            d.recipe("mixing/slime_ball", "create:mixing")
                            .in("reactingreactions:borax", fluid("reactingreactions:resin", 250))
                .out("minecraft:slime_ball", 2);
            d.recipe("mixing/soap", "create:mixing")
                            .in(fluid("reactingreactions:lye", 250), fluid("reactingreactions:seed_oil", 250))
                .out("reactingreactions:soap", 2);
            d.recipe("mixing/titanium", "create:mixing")
                            .heat("superheated")
                            .in(fluid("reactingreactions:titanium_tetrachloride", 250), "reactingreactions:magnesium")
                .out("reactingreactions:titanium");
            d.recipe("mixing/titanium_tetrachloride", "create:mixing")
                            .heat("superheated")
                            .in("reactingreactions:crimsite_dust", "reactingreactions:coal_coke", fluid("reactingreactions:bleach", 250))
                .fluidOut("reactingreactions:titanium_tetrachloride", 250);
            d.recipe("mixing/varnish", "create:mixing")
                            .in(fluid("reactingreactions:resin", 250), fluid("reactingreactions:solvent", 250))
                .out("reactingreactions:varnish", 16);
            d.recipe("mixing/white_paint", "create:mixing")
                            .in("reactingreactions:titanium_dioxide", fluid("reactingreactions:seed_oil", 250))
                .out("reactingreactions:white_paint", 16);
            d.recipe("mixing/yellow_paint", "create:mixing")
                            .in("reactingreactions:sulfur_dust", fluid("reactingreactions:seed_oil", 250))
                .out("reactingreactions:yellow_paint", 16);
            d.recipe("casting/hdpe_pellets", "create:mixing")
                            .time(100)
                            .in(fluid("reactingreactions:liquid_hdpe", 144))
                .out("reactingreactions:hdpe_pellets");
            d.recipe("casting/nylon_fiber", "create:mixing")
                            .time(100)
                            .in(fluid("reactingreactions:liquid_nylon", 144))
                .out("reactingreactions:nylon_fiber");
            d.recipe("casting/plastic_from_polypropylene", "create:mixing")
                            .time(100)
                            .in(fluid("reactingreactions:liquid_polypropylene", 144))
                .out("reactingreactions:hdpe_sheet");
            d.recipe("crushing/granite", "create:crushing")
                            .time(250)
                            .in("minecraft:granite")
                .out("reactingreactions:granite_dust")
                .outChance("reactingreactions:granite_dust", 0.5);
            d.recipe("crushing/scoria", "create:crushing")
                            .time(250)
                            .in("create:scoria")
                .out("reactingreactions:scoria_dust")
                .outChance("reactingreactions:scoria_dust", 0.5);
            // Lye without electricity: kelp ash causticised with quicklime, the soap-maker's way.
            d.recipe("mixing/lye_from_kelp_ash", "create:mixing")
                            .time(300)
                            .in(x(2, "minecraft:dried_kelp"), "reactingreactions:quicklime", fluid("minecraft:water", 500))
                .fluidOut("reactingreactions:lye", 250);
            // Superphosphate, the first chemical fertilizer: bone meal digested in sulfuric acid.
            d.recipe("mixing/superphosphate", "create:mixing")
                            .in(x(3, "minecraft:bone_meal"), fluid("reactingreactions:sulfuric_acid", 250))
                .out("reactingreactions:super_bone_meal", 2);
            // Amethyst is quartz: milled, it is silica.
            d.recipe("milling/amethyst_shard", "create:milling")
                            .time(100)
                            .in("minecraft:amethyst_shard")
                .out("reactingreactions:silica");
            // Bromine the easy way: magma cream's salts oxidised with bleach.
            d.recipe("mixing/bromine_from_magma_cream", "create:mixing")
                            .in("minecraft:magma_cream", fluid("reactingreactions:bleach", 100))
                .out("reactingreactions:bromine");
            // Phosphors fired from rare earths, as real ones are. Leaching takes 4 redstone or glowstone per rare earth dust, so
            // these give back only 3: a round trip always loses material.
            // Redstone is europium-doped yttrium oxide, fired in air.
            d.recipe("mixing/redstone_from_rare_earths", "create:mixing")
                            .heat("superheated")
                            .in("reactingreactions:rare_earth_dust", fluid("reactingreactions:oxygen", 100))
                .out("minecraft:redstone", 3);
            // Glowstone: a europium-doped aluminate. With lime it is calcium aluminate, itself a real long-glowing phosphor.
            d.recipe("mixing/glowstone_from_rare_earths", "create:mixing")
                            .heat("superheated")
                            .in("reactingreactions:rare_earth_dust", "reactingreactions:alumina_dust", "reactingreactions:quicklime")
                .out("minecraft:glowstone_dust", 3);
            // Steam activation: how real activated charcoal is made, from any tree farm.
            d.recipe("mixing/activated_carbon_from_charcoal", "create:mixing")
                            .heat("superheated")
                            .in("minecraft:charcoal", fluid("reactingreactions:steam", 250))
                .out("reactingreactions:activated_carbon");
            // Glauber's route: saltpetre from gunpowder, distilled with sulfuric acid. Gunpowder is made with 250 mB of nitric
            // acid per 4, so this gives back less (200 mB): no loop gains.
            d.recipe("mixing/nitric_acid_from_gunpowder", "create:mixing")
                            .heat("heated")
                            .in(x(4, "minecraft:gunpowder"), fluid("reactingreactions:sulfuric_acid", 250))
                .fluidOut("reactingreactions:nitric_acid", 200)
                .outChance("reactingreactions:sulfur_dust", 0.5);
            // Volcanic sulfur: crushed netherrack (Create's cinder flour) melted down.
            d.recipe("mixing/sulfur_from_cinder_flour", "create:mixing")
                            .heat("heated")
                            .in(x(3, "create:cinder_flour"))
                .out("reactingreactions:sulfur_dust");
            // Snow and ice melt into near-pure water.
            // Blue ice, being a chore to gather, keeps this as costly as the alum and bleach routes.
            d.recipe("mixing/purified_water_from_ice", "create:mixing")
                            .heat("heated")
                            .in("minecraft:blue_ice")
                .fluidOut("reactingreactions:purified_water", 1000);
            // Hydrothermal quartz: silica regrown in hot water, as industrial quartz is.
            d.recipe("mixing/synthetic_quartz", "create:mixing")
                            .heat("superheated")
                            .in(x(2, "reactingreactions:silica"), fluid("reactingreactions:purified_water", 250))
                .out("minecraft:quartz");
            // Amethyst is quartz coloured by iron.
            d.recipe("mixing/synthetic_amethyst", "create:mixing")
                            .heat("heated")
                            .in("minecraft:quartz", "minecraft:iron_nugget")
                .out("minecraft:amethyst_shard");
            // Clay is kaolinite, an aluminium silicate.
            d.recipe("mixing/clay", "create:mixing")
                            .in("reactingreactions:alumina_dust", "reactingreactions:silica", fluid("minecraft:water", 250))
                .out("minecraft:clay_ball", 2);
            // Real greases are oil thickened with wax: beeswax does it too.
            d.recipe("mixing/drill_grease_from_wax", "create:mixing")
                            .heat("heated")
                            .in("minecraft:honeycomb", fluid("reactingreactions:mineral_oil", 500))
                .fluidOut("reactingreactions:drill_grease", 500);
            // Beeswax dissolved in solvent: a wax finish instead of resin varnish.
            d.recipe("mixing/varnish_from_wax", "create:mixing")
                            .in("minecraft:honeycomb", fluid("reactingreactions:solvent", 250))
                .out("reactingreactions:varnish", 12);
            // Drill grease: mineral oil thickened with magma cream.
            d.recipe("mixing/drill_grease", "create:mixing")
                            .heat("heated")
                            .in("minecraft:magma_cream", fluid("reactingreactions:mineral_oil", 500))
                .fluidOut("reactingreactions:drill_grease", 500);
            // Salt-curing hides.
            d.recipe("mixing/leather", "create:mixing")
                            .in(x(2, "minecraft:rotten_flesh"), fluid("reactingreactions:weak_brine", 250))
                .out("minecraft:leather");
            // Iodine: kelp leached with brine, the iodide oxidised to iodine by a little bleach.
            d.recipe("mixing/iodine", "create:mixing")
                            .heat("heated")
                            .in(x(4, "minecraft:dried_kelp"), fluid("reactingreactions:weak_brine", 250), fluid("reactingreactions:bleach", 50))
                .out("reactingreactions:iodine");
            // Tincture of iodine: iodine dissolved in ethanol, bottled.
            d.recipe("mixing/iodine_spray", "create:mixing")
                            .in("reactingreactions:iodine", "minecraft:glass_bottle", fluid("reactingreactions:ethanol", 250))
                .out("reactingreactions:iodine_spray");
            // Rock salt forms in marine evaporite beds alongside limestone: crushing limestone turns some up.
            d.recipe("crushing/limestone", "create:crushing")
                            .time(250)
                            .in("create:limestone")
                .outChance("reactingreactions:mineral_salt", 0.2);
            d.recipe("milling/mineral_salt", "create:milling")
                            .time(100)
                            .in("reactingreactions:mineral_salt")
                .out("reactingreactions:salt")
                .outChance("reactingreactions:salt", 0.25);
            // 10% brine straight from salt. It must cost more than the ~5 salt the lithium chain gives back per bucket,
            // or salt would loop forever.
            d.recipe("mixing/strong_brine_from_salt", "create:mixing")
                            .in(x(8, "reactingreactions:salt"), fluid("minecraft:water", 1000))
                .fluidOut("reactingreactions:strong_brine", 1000);
            // Salt closes the brine loop; slag stops being a dead end.
            d.recipe("mixing/weak_brine_from_salt", "create:mixing")
                            .in(x(2, "reactingreactions:salt"), fluid("minecraft:water", 1000))
                .fluidOut("reactingreactions:weak_brine", 1000);
            // Plastic recycling: a sheet crushes back into most of its pellets.
            d.recipe("crushing/hdpe_recycling", "create:crushing")
                            .time(150)
                            .in("reactingreactions:hdpe_sheet")
                .out("reactingreactions:hdpe_pellets", 7)
                .outChance("reactingreactions:hdpe_pellets", 0.5);
            d.recipe("crushing/slag", "create:crushing")
                            .time(150)
                            .in("reactingreactions:slag")
                .out("minecraft:gravel")
                .outChance("minecraft:iron_nugget", 0.2);
            d.recipe("pressing/steel_sheet", "create:pressing")
                            .in("reactingreactions:steel_ingot")
                .out("reactingreactions:steel_sheet");
            d.recipe("pressing/titanium_sheet", "create:pressing")
                            .in("reactingreactions:titanium")
                .out("reactingreactions:titanium_sheet");
            // Diamond: HPHT synthesis, carbon and a metal catalyst pressed hard, over 10 loops.
            d.recipe("sequenced_assembly/diamond", "create:sequenced_assembly")
                            .ingredient("reactingreactions:activated_carbon")
                .out("minecraft:diamond")
                            .sequence("reactingreactions:incomplete_diamond", 10, deploy("reactingreactions:activated_carbon"), deploy("reactingreactions:nickel_nugget"), presses(3));
            // A shortcut with a rarer catalyst. It starts with the pearl, so it never shares a first step with the nickel one.
            d.recipe("sequenced_assembly/diamond_ender", "create:sequenced_assembly")
                            .ingredient("reactingreactions:activated_carbon")
                .out("minecraft:diamond")
                            .sequence("reactingreactions:incomplete_diamond", 3, deploy("minecraft:ender_pearl"), deploy("reactingreactions:activated_carbon"), presses(3));
            // Netherite, as a titanium superalloy consolidated by hot isostatic pressing.
            d.recipe("mixing/netherite_alloy_dust", "create:mixing")
                            .heat("superheated")
                            .in("reactingreactions:titanium", "reactingreactions:steel_ingot", "reactingreactions:nickel_ingot", "reactingreactions:manganese")
                .out("reactingreactions:netherite_alloy_dust");
            d.recipe("sequenced_assembly/netherite_ingot", "create:sequenced_assembly")
                            .ingredient("reactingreactions:netherite_alloy_dust")
                .out("minecraft:netherite_ingot")
                            .sequence("reactingreactions:incomplete_netherite_ingot", 4, presses(4));
            // The last deploy before pressing is a quartz (amethyst) oscillator crystal, as real circuits use.
            d.recipe("sequenced_assembly/circuit_board", "create:sequenced_assembly")
                            .ingredient("reactingreactions:silicon_board")
                .out("reactingreactions:circuit_board")
                            .sequence("reactingreactions:incomplete_circuit_board", 1, deploy("electroenergetics:resistor"), deploy("electroenergetics:capacitor"), deploy("minecraft:amethyst_shard"), press())
                            .ifModLoaded("electroenergetics");
            // Without Electro Energetics, copper nuggets stand in for the resistor and capacitor.
            d.recipe("sequenced_assembly/circuit_board_no_ee", "create:sequenced_assembly")
                            .ingredient("reactingreactions:silicon_board")
                .out("reactingreactions:circuit_board")
                            .sequence("reactingreactions:incomplete_circuit_board", 1, deploy("create:copper_nugget"), deploy("create:copper_nugget"), deploy("minecraft:amethyst_shard"), press())
                            .ifModNotLoaded("electroenergetics");
            // Both make Electro Energetics' accumulator, so they need that mod.
            d.recipe("sequenced_assembly/lead_acid_battery", "create:sequenced_assembly")
                            .ingredient("create:andesite_alloy")
                .out("electroenergetics:accumulator")
                            .sequence("reactingreactions:incomplete_lead_acid_battery", 1, deploy("reactingreactions:lead_ingot"), deploy("reactingreactions:lead_ingot"), fill("reactingreactions:sulfuric_acid", 250), press())
                            .ifModLoaded("electroenergetics");
            d.recipe("sequenced_assembly/lithium_battery", "create:sequenced_assembly")
                            .ingredient("create:andesite_alloy")
                .out("electroenergetics:accumulator", 2)
                            .sequence("reactingreactions:incomplete_lithium_battery", 1, deploy("reactingreactions:lithium_ingot"), deploy("reactingreactions:activated_carbon"), fill("reactingreactions:purified_water", 250), press())
                            .ifModLoaded("electroenergetics");
            // ANFO: ammonium nitrate soaked in fuel oil, packed into a keg.
            d.recipe("mixing/anfo_charge", "create:mixing")
                            .in(x(2, "reactingreactions:ammonium_nitrate"), "minecraft:barrel", fluid("reactingreactions:diesel", 125))
                .out("reactingreactions:anfo_charge");
            d.recipe("filling/neon_lamp", "create:filling")
                            .in("reactingreactions:reinforced_glass", fluid("reactingreactions:neon", 250))
                .out("reactingreactions:neon_lamp");
        }
    }
}
