package com.koala.reactingreactions.datagen;

import static com.koala.reactingreactions.datagen.Data.*;

/**
 * Everything the Reaction Chamber runs. Recipes with an RPM band need the chamber's steel-encased shaft to turn within it
 * (three tiers: gentle scrubbing and dissolving at 8-24 RPM, heated synthesis at 24-64, violent superheated reactions at
 * 64-128). The fermentations have no band, so the single-block Fermentation Barrel can run them too. Every gas-consuming
 * or gas-producing recipe lives here, not on Create's mixer.
 */
final class CRRReactionRecipes {
    private CRRReactionRecipes() {
    }

    static void register(Data data) {
        Data d = data.ns("reactingreactions");
        d.recipe("reaction_recipe/acetylene", "reactingreactions:reaction_recipe")
                .in("reactingreactions:calcium_carbide", fluid("minecraft:water", 250))
                .fluidOut("reactingreactions:acetylene", 250)
                .rpm(8, 24);
        d.recipe("reaction_recipe/aerozine", "reactingreactions:reaction_recipe")
                .heat("heated")
                .time(300)
                .in(fluid("reactingreactions:ammonia", 500), fluid("reactingreactions:bleach", 500))
                .fluidOut("reactingreactions:aerozine", 400)
                .fluidOut("minecraft:water", 300)
                .rpm(24, 64);
        d.recipe("reaction_recipe/ammonia", "reactingreactions:reaction_recipe")
                .heat("heated")
                .in(fluid("reactingreactions:hydrogen", 1000), fluid("reactingreactions:nitrogen", 500))
                .fluidOut("reactingreactions:ammonia", 1000)
                .rpm(24, 64);
        d.recipe("reaction_recipe/ammonium_nitrate", "reactingreactions:reaction_recipe")
                .in(fluid("reactingreactions:ammonia", 250), fluid("reactingreactions:nitric_acid", 250))
                .out("reactingreactions:ammonium_nitrate", 2)
                .rpm(8, 24);
        d.recipe("reaction_recipe/asurine_dust", "reactingreactions:reaction_recipe")
                .heat("superheated")
                .in("reactingreactions:asurine_dust", fluid("reactingreactions:oxygen", 250))
                .out("create:zinc_nugget", 5)
                .outChance("reactingreactions:rare_earth_dust", 0.35)
                .rpm(64, 128);
        // Recipes making ethanol or diesel get a twin making Create Diesel Generators' own; inputs accept its fluids through the common tags.
        d.recipe("reaction_recipe/ethanol_dehydration", "reactingreactions:reaction_recipe")
                .heat("heated")
                .in(fluid("reactingreactions:ethanol", 500), fluid("reactingreactions:sulfuric_acid", 250))
                .fluidOut("reactingreactions:ethylene", 500)
                .fluidOut("reactingreactions:carbon_dioxide", 250)
                .out("reactingreactions:sulfur_dust")
                .rpm(24, 64);
        d.recipe("reaction_recipe/ethanol_from_ethane", "reactingreactions:reaction_recipe")
                .heat("heated")
                .in(fluid("reactingreactions:ethane", 500), fluid("reactingreactions:oxygen", 250))
                .fluidOut("reactingreactions:ethanol", 400)
                .rpm(24, 64)
                .dieselGeneratorsTwin();
        d.recipe("reaction_recipe/ethylene_to_propylene", "reactingreactions:reaction_recipe")
                .heat("heated")
                .in(fluid("reactingreactions:ethylene", 500), "reactingreactions:lead_nugget")
                .fluidOut("reactingreactions:propylene", 500)
                .rpm(24, 64);
        d.recipe("reaction_recipe/fertilizer", "reactingreactions:reaction_recipe")
                .heat("heated")
                .in(fluid("reactingreactions:ammonia", 500), fluid("reactingreactions:carbon_dioxide", 250))
                .out("reactingreactions:fertilizer", 2)
                .rpm(24, 64);
        d.recipe("reaction_recipe/fischer_tropsch", "reactingreactions:reaction_recipe")
                .heat("heated")
                .in(fluid("reactingreactions:hydrogen", 500), fluid("reactingreactions:carbon_monoxide", 500), "minecraft:iron_nugget")
                .fluidOut("reactingreactions:diesel", 500)
                .rpm(24, 64)
                .dieselGeneratorsTwin();
        // Geothermal steam: lava boils water with no burner at all, and cools to basalt. Stirred, so the wooden barrel can't run it.
        d.recipe("reaction_recipe/geothermal_steam", "reactingreactions:reaction_recipe")
                .in(fluid("minecraft:lava", 250), fluid("minecraft:water", 1000))
                .fluidOut("reactingreactions:steam", 1000)
                .outChance("minecraft:basalt", 0.5)
                .rpm(8, 24);
        // Blaze roasting: blaze powder stands in for oxygen, only heated rather than superheated. An early route, with about
        // two thirds of the oxygen route's yield (two dusts per powder).
        d.recipe("reaction_recipe/blaze_roasting_asurine_dust", "reactingreactions:reaction_recipe")
                .heat("heated")
                .in(x(2, "reactingreactions:asurine_dust"), "minecraft:blaze_powder")
                .out("create:zinc_nugget", 6)
                .outChance("reactingreactions:rare_earth_dust", 0.4)
                .rpm(24, 64);
        d.recipe("reaction_recipe/blaze_roasting_scoria_dust", "reactingreactions:reaction_recipe")
                .heat("heated")
                .in(x(2, "reactingreactions:scoria_dust"), "minecraft:blaze_powder")
                .out("reactingreactions:lead_nugget", 8)
                .outChance("reactingreactions:sulfur_dust", 0.8)
                .rpm(24, 64);
        d.recipe("reaction_recipe/blaze_roasting_tuff_dust", "reactingreactions:reaction_recipe")
                .heat("heated")
                .in(x(2, "reactingreactions:tuff_dust"), "minecraft:blaze_powder")
                .out("reactingreactions:nickel_nugget", 8)
                .rpm(24, 64);
        d.recipe("reaction_recipe/blaze_roasting_veridium_dust", "reactingreactions:reaction_recipe")
                .heat("heated")
                .in(x(2, "reactingreactions:veridium_dust"), "minecraft:blaze_powder")
                .out("minecraft:copper_ingot")
                .outChance("minecraft:copper_ingot", 0.5)
                .rpm(24, 64);
        // Rare earths from redstone (Y2O3:Eu) and glowstone (SrAl2O4:Eu,Dy): acid leaching, then solvent extraction to separate
        // them, which is how real rare earths are refined.
        d.recipe("reaction_recipe/rare_earth_from_redstone", "reactingreactions:reaction_recipe")
                .heat("heated")
                .in(x(4, "minecraft:redstone"), fluid("reactingreactions:sulfuric_acid", 250), fluid("reactingreactions:solvent", 100))
                .out("reactingreactions:rare_earth_dust")
                .rpm(24, 64);
        d.recipe("reaction_recipe/rare_earth_from_glowstone", "reactingreactions:reaction_recipe")
                .heat("heated")
                .in(x(4, "minecraft:glowstone_dust"), fluid("reactingreactions:sulfuric_acid", 250), fluid("reactingreactions:solvent", 100))
                .out("reactingreactions:rare_earth_dust")
                .outChance("reactingreactions:alumina_dust", 0.5)
                .rpm(24, 64);
        d.recipe("reaction_recipe/iron_oxide", "reactingreactions:reaction_recipe")
                .heat("superheated")
                .in("minecraft:iron_ingot", fluid("reactingreactions:oxygen", 250))
                .out("reactingreactions:iron_oxide", 2)
                .rpm(64, 128);
        d.recipe("reaction_recipe/liquid_nylon", "reactingreactions:reaction_recipe")
                .heat("heated")
                .in(fluid("reactingreactions:ethylene", 500), fluid("reactingreactions:ammonia", 250))
                .fluidOut("reactingreactions:liquid_nylon", 500)
                .rpm(24, 64);
        d.recipe("reaction_recipe/naphtha", "reactingreactions:reaction_recipe")
                .heat("heated")
                .in(fluid("reactingreactions:naphtha", 500), fluid("reactingreactions:steam", 250))
                .fluidOut("reactingreactions:ethylene", 250)
                .fluidOut("reactingreactions:propylene", 250)
                .rpm(24, 64);
        d.recipe("reaction_recipe/nitric_acid", "reactingreactions:reaction_recipe")
                .heat("heated")
                .in(fluid("reactingreactions:ammonia", 500), fluid("reactingreactions:oxygen", 500))
                .fluidOut("reactingreactions:nitric_acid", 500)
                .rpm(24, 64);
        d.recipe("reaction_recipe/pig_iron", "reactingreactions:reaction_recipe")
                .heat("superheated")
                .in("reactingreactions:pig_iron", fluid("reactingreactions:oxygen", 250))
                .outChance("reactingreactions:steel_ingot", 0.7)
                .outChance("minecraft:iron_ingot", 0.3)
                .rpm(64, 128);
        d.recipe("reaction_recipe/plastic_from_ethylene", "reactingreactions:reaction_recipe")
                .heat("heated")
                .in(fluid("reactingreactions:ethylene", 500), "reactingreactions:silica")
                .fluidOut("reactingreactions:liquid_hdpe", 500)
                .rpm(24, 64);
        d.recipe("reaction_recipe/plastic_from_propylene", "reactingreactions:reaction_recipe")
                .heat("heated")
                .in(fluid("reactingreactions:propylene", 500), "reactingreactions:aluminum_nugget")
                .fluidOut("reactingreactions:liquid_polypropylene", 500)
                .rpm(24, 64);
        d.recipe("reaction_recipe/propylene_to_ethylene", "reactingreactions:reaction_recipe")
                .heat("heated")
                .in(fluid("reactingreactions:propylene", 500), "reactingreactions:lead_nugget")
                .fluidOut("reactingreactions:ethylene", 500)
                .rpm(24, 64);
        d.recipe("reaction_recipe/biodiesel", "reactingreactions:reaction_recipe")
                .heat("heated")
                .in(fluid("reactingreactions:seed_oil", 500), fluid("reactingreactions:ethanol", 250), "reactingreactions:sulfur_dust")
                .fluidOut("reactingreactions:diesel", 500)
                .rpm(24, 64)
                .ifModNotLoaded("createdieselgenerators");
        // With Create Diesel Generators, its own biodiesel, ethanol and plant oil instead.
        d.recipe("reaction_recipe/biodiesel_cdg", "reactingreactions:reaction_recipe")
                .heat("heated")
                .in(fluid("createdieselgenerators:plant_oil", 500), fluid("createdieselgenerators:ethanol", 250), "reactingreactions:sulfur_dust")
                .fluidOut("createdieselgenerators:biodiesel", 500)
                .rpm(24, 64)
                .ifModLoaded("createdieselgenerators");
        d.recipe("reaction_recipe/water_gas_shift", "reactingreactions:reaction_recipe")
                .heat("heated")
                .in(fluid("reactingreactions:carbon_monoxide", 250), fluid("reactingreactions:steam", 250))
                .fluidOut("reactingreactions:hydrogen", 250)
                .fluidOut("reactingreactions:carbon_dioxide", 250)
                .rpm(24, 64);
        d.recipe("reaction_recipe/coal_gasification", "reactingreactions:reaction_recipe")
                .heat("superheated")
                .in("minecraft:coal", fluid("reactingreactions:steam", 250))
                .fluidOut("reactingreactions:carbon_monoxide", 500)
                .fluidOut("reactingreactions:hydrogen", 500)
                .rpm(64, 128);
        d.recipe("reaction_recipe/ethanol_from_ethylene", "reactingreactions:reaction_recipe")
                .heat("heated")
                .in(fluid("reactingreactions:ethylene", 250), fluid("reactingreactions:steam", 250))
                .fluidOut("reactingreactions:ethanol", 250)
                .rpm(24, 64)
                .dieselGeneratorsTwin();
        d.recipe("reaction_recipe/ethane_from_ethylene", "reactingreactions:reaction_recipe")
                .heat("heated")
                .in(fluid("reactingreactions:ethylene", 250), fluid("reactingreactions:hydrogen", 250), "reactingreactions:nickel_nugget")
                .fluidOut("reactingreactions:ethane", 250)
                .rpm(24, 64);
        d.recipe("reaction_recipe/propane_from_propylene", "reactingreactions:reaction_recipe")
                .heat("heated")
                .in(fluid("reactingreactions:propylene", 250), fluid("reactingreactions:hydrogen", 250), "reactingreactions:nickel_nugget")
                .fluidOut("reactingreactions:propane", 250)
                .rpm(24, 64);
        d.recipe("reaction_recipe/ethylene_from_acetylene", "reactingreactions:reaction_recipe")
                .heat("heated")
                .in(fluid("reactingreactions:acetylene", 250), fluid("reactingreactions:hydrogen", 250), "reactingreactions:nickel_nugget")
                .fluidOut("reactingreactions:ethylene", 250)
                .rpm(24, 64);
        d.recipe("reaction_recipe/hydrogen_combustion", "reactingreactions:reaction_recipe")
                .heat("heated")
                .in(fluid("reactingreactions:hydrogen", 500), fluid("reactingreactions:oxygen", 250))
                .fluidOut("reactingreactions:steam", 500)
                .rpm(24, 64);
        d.recipe("reaction_recipe/carbon_monoxide_combustion", "reactingreactions:reaction_recipe")
                .heat("heated")
                .in(fluid("reactingreactions:carbon_monoxide", 500), fluid("reactingreactions:oxygen", 250))
                .fluidOut("reactingreactions:carbon_dioxide", 500)
                .rpm(24, 64);
        d.recipe("reaction_recipe/sabatier", "reactingreactions:reaction_recipe")
                .heat("heated")
                .in(fluid("reactingreactions:carbon_dioxide", 250), fluid("reactingreactions:hydrogen", 1000))
                .fluidOut("reactingreactions:methane", 250)
                .fluidOut("minecraft:water", 500)
                .rpm(24, 64);
        d.recipe("reaction_recipe/scoria_dust", "reactingreactions:reaction_recipe")
                .heat("superheated")
                .in("reactingreactions:scoria_dust", fluid("reactingreactions:oxygen", 250))
                .out("reactingreactions:lead_nugget", 6)
                .outChance("reactingreactions:sulfur_dust", 0.6)
                .rpm(64, 128);
        d.recipe("reaction_recipe/scrub_hydrocarbon_gas", "reactingreactions:reaction_recipe")
                .in(fluid("reactingreactions:contaminated_hydrocarbon_gas", 500), fluid("reactingreactions:solvent", 250))
                .fluidOut("reactingreactions:hydrocarbon_gas", 500)
                .fluidOut("reactingreactions:sulfuric_acid", 100)
                .rpm(8, 24);
        d.recipe("reaction_recipe/scrub_methane", "reactingreactions:reaction_recipe")
                .in(fluid("reactingreactions:contaminated_methane", 500), fluid("reactingreactions:solvent", 250))
                .fluidOut("reactingreactions:methane", 500)
                .fluidOut("reactingreactions:sulfuric_acid", 100)
                .rpm(8, 24);
        d.recipe("reaction_recipe/solvent_from_butane", "reactingreactions:reaction_recipe")
                .heat("superheated")
                .in(fluid("reactingreactions:butane", 500), fluid("reactingreactions:steam", 250))
                .fluidOut("reactingreactions:solvent", 250)
                .rpm(64, 128);
        d.recipe("reaction_recipe/solvent_from_propane", "reactingreactions:reaction_recipe")
                .heat("superheated")
                .in(fluid("reactingreactions:propane", 500), fluid("reactingreactions:steam", 250))
                .fluidOut("reactingreactions:solvent", 250)
                .rpm(64, 128);
        d.recipe("reaction_recipe/steam", "reactingreactions:reaction_recipe")
                .heat("heated")
                .in(fluid("minecraft:water", 250))
                .fluidOut("reactingreactions:steam", 250)
                .rpm(8, 24);
        d.recipe("reaction_recipe/steam_methane_reforming", "reactingreactions:reaction_recipe")
                .heat("superheated")
                .in(fluid("reactingreactions:methane", 250), fluid("reactingreactions:steam", 250))
                .fluidOut("reactingreactions:hydrogen", 1000)
                .fluidOut("reactingreactions:carbon_dioxide", 250)
                .rpm(64, 128);
        // Closes the sulfur loop: sulfur scrubbed out of gas streams and smelted from ores becomes acid again.
        d.recipe("reaction_recipe/sulfuric_acid", "reactingreactions:reaction_recipe")
                .heat("heated")
                .in("reactingreactions:sulfur_dust", fluid("reactingreactions:oxygen", 500), fluid("minecraft:water", 250))
                .fluidOut("reactingreactions:sulfuric_acid", 500)
                .rpm(24, 64);
        // Uses for materials that had none: diesel, lithium, nickel and rare earths (as consumed catalysts).
        d.recipe("reaction_recipe/diesel_reforming", "reactingreactions:reaction_recipe")
                .heat("superheated")
                .in(fluid("reactingreactions:diesel", 250), fluid("reactingreactions:steam", 500))
                .fluidOut("reactingreactions:hydrogen", 1000)
                .fluidOut("reactingreactions:carbon_monoxide", 500)
                .rpm(64, 128);
        d.recipe("reaction_recipe/lithium_hydrolysis", "reactingreactions:reaction_recipe")
                .in("reactingreactions:lithium_nugget", fluid("minecraft:water", 250))
                .fluidOut("reactingreactions:hydrogen", 250)
                .fluidOut("reactingreactions:lye", 125)
                .rpm(8, 24);
        d.recipe("reaction_recipe/steam_methane_reforming_nickel", "reactingreactions:reaction_recipe")
                .heat("superheated")
                .in(fluid("reactingreactions:methane", 250), fluid("reactingreactions:steam", 250), "reactingreactions:nickel_nugget")
                .fluidOut("reactingreactions:hydrogen", 2000)
                .fluidOut("reactingreactions:carbon_dioxide", 250)
                .rpm(64, 128);
        d.recipe("reaction_recipe/fischer_tropsch_rare_earth", "reactingreactions:reaction_recipe")
                .heat("heated")
                .in(fluid("reactingreactions:hydrogen", 500), fluid("reactingreactions:carbon_monoxide", 500), "reactingreactions:rare_earth_dust")
                .fluidOut("reactingreactions:diesel", 1000)
                .rpm(24, 64)
                .dieselGeneratorsTwin();
        d.recipe("reaction_recipe/titanium_dioxide", "reactingreactions:reaction_recipe")
                .heat("heated")
                .in(fluid("reactingreactions:titanium_tetrachloride", 250), fluid("reactingreactions:oxygen", 250))
                .out("reactingreactions:titanium_dioxide", 2)
                .rpm(24, 64);
        d.recipe("reaction_recipe/tuff_dust", "reactingreactions:reaction_recipe")
                .heat("superheated")
                .in("reactingreactions:tuff_dust", fluid("reactingreactions:oxygen", 250))
                .out("reactingreactions:nickel_nugget", 6)
                .outChance("minecraft:iron_nugget", 3, 0.5)
                .rpm(64, 128);
        d.recipe("reaction_recipe/veridium_dust", "reactingreactions:reaction_recipe")
                .heat("superheated")
                .in("reactingreactions:veridium_dust", fluid("reactingreactions:oxygen", 250))
                .out("minecraft:copper_ingot")
                .outChance("minecraft:iron_nugget", 3, 0.5)
                .rpm(64, 128);
        d.recipe("reaction_recipe/biogas", "reactingreactions:reaction_recipe")
                .time(400)
                .in(x(2, "reactingreactions:biomass"))
                .fluidOut("reactingreactions:contaminated_methane", 500)
                .fluidOut("reactingreactions:carbon_dioxide", 250)
                .rpm(8, 24);
        // The only reactions the Fermentation Barrel can run; with Create Diesel Generators they run in its Bulk Fermenter.
        d.recipe("reaction_recipe/ethanol_from_sugar", "reactingreactions:reaction_recipe")
                .time(1200)
                .in("reactingreactions:yeast", x(4, "minecraft:sugar"))
                .fluidOut("reactingreactions:ethanol", 500)
                .dieselGeneratorsTwin("createdieselgenerators:bulk_fermenting");
        d.recipe("reaction_recipe/methane_from_plants", "reactingreactions:reaction_recipe")
                .time(1200)
                .in(x(4, "#reactingreactions:plant_matter"), fluid("minecraft:water", 500))
                .fluidOut("reactingreactions:methane", 250)
                .out("reactingreactions:fertilizer")
                .dieselGeneratorsTwin("createdieselgenerators:bulk_fermenting");
        d.recipe("reaction_recipe/white_vinegar", "reactingreactions:reaction_recipe")
                .time(1200)
                .in("reactingreactions:yeast", fluid("reactingreactions:ethanol", 250), fluid("minecraft:water", 250))
                .fluidOut("reactingreactions:white_vinegar", 500)
                .dieselGeneratorsTwin("createdieselgenerators:bulk_fermenting");
        // Gems grown by controlled heat run at the hardest tier. Emerald: flux growth of BeO, Al2O3, SiO2 and Cr2O3 (the colour).
        d.recipe("reaction_recipe/emerald", "reactingreactions:reaction_recipe")
                .heat("superheated")
                .in("reactingreactions:beryllium_oxide", "reactingreactions:alumina_dust", "reactingreactions:silica", "reactingreactions:chromium_dust")
                .out("minecraft:emerald")
                .rpm(64, 128);
        // Lapis has no real synthesis, so this recombines its minerals: lazurite (a sodium aluminosilicate with sulfur) and
        // pyrite flecks (the iron nugget).
        d.recipe("reaction_recipe/lapis_lazuli", "reactingreactions:reaction_recipe")
                .heat("superheated")
                .in("reactingreactions:salt", "reactingreactions:alumina_dust", "reactingreactions:silica", "reactingreactions:sulfur_dust", "minecraft:iron_nugget")
                .out("minecraft:lapis_lazuli", 4)
                .rpm(64, 128);
        // Ruby: Verneuil flame fusion of Al2O3 and Cr2O3 in an oxyhydrogen flame.
        d.recipe("reaction_recipe/ruby", "reactingreactions:reaction_recipe")
                .heat("superheated")
                .in("reactingreactions:alumina_dust", "reactingreactions:chromium_dust")
                .out("reactingreactions:ruby")
                .rpm(64, 128);
    }
}
