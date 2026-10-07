package com.koala.reactingreactions.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koala.reactingreactions.content.toxic.ToxicDefaults;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/** Vanilla loot table overrides, tags, worldgen, biome modifiers and data maps (block loot is Registrate's). */
final class MiscData {
    private static final String[] COLORS = {"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray",
            "cyan", "purple", "blue", "brown", "green", "red", "black"};

    private MiscData() {
    }

    static void register(Data data) {
        lootTables(data);
        tags(data);
        worldgen(data);
        richVeins(data);
        advancements(data);
        dataMaps(data);
        dieselGeneratorsFuelTypes(data);
        dieselGeneratorsRecipeOverrides(data);
    }

    // ---- loot tables -------------------------------------------------------------------------------------------

    private static void lootTables(Data data) {
        // Block loot tables (a block dropping itself) come from Registrate, next to each block's registration.
        fishingJunk(data.ns("minecraft"));
    }

    /**
     * Vanilla's fishing junk table with the Polymetallic Nodule added (weight 8). Vanilla's own entries are repeated
     * here because a data pack file replaces the vanilla one wholesale.
     */
    private static void fishingJunk(Data d) {
        d.raw("loot_table", "gameplay/fishing/junk", """
                {"type": "minecraft:fishing", "random_sequence": "minecraft:gameplay/fishing/junk",
                 "pools": [{"bonus_rolls": 0.0, "rolls": 1.0, "entries": [
                   {"type": "minecraft:item", "name": "minecraft:lily_pad", "weight": 17},
                   {"type": "minecraft:item", "name": "minecraft:leather_boots", "weight": 10, "functions": [
                     {"function": "minecraft:set_damage", "add": false, "damage": {"type": "minecraft:uniform", "min": 0.0, "max": 0.9}}]},
                   {"type": "minecraft:item", "name": "minecraft:leather", "weight": 10},
                   {"type": "minecraft:item", "name": "minecraft:bone", "weight": 10},
                   {"type": "minecraft:item", "name": "minecraft:potion", "weight": 10, "functions": [
                     {"function": "minecraft:set_potion", "id": "minecraft:water"}]},
                   {"type": "minecraft:item", "name": "minecraft:string", "weight": 5},
                   {"type": "minecraft:item", "name": "minecraft:fishing_rod", "weight": 2, "functions": [
                     {"function": "minecraft:set_damage", "add": false, "damage": {"type": "minecraft:uniform", "min": 0.0, "max": 0.9}}]},
                   {"type": "minecraft:item", "name": "minecraft:bowl", "weight": 10},
                   {"type": "minecraft:item", "name": "minecraft:stick", "weight": 5},
                   {"type": "minecraft:item", "name": "minecraft:ink_sac", "functions": [
                     {"function": "minecraft:set_count", "add": false, "count": 10.0}]},
                   {"type": "minecraft:item", "name": "minecraft:tripwire_hook", "weight": 10},
                   {"type": "minecraft:item", "name": "minecraft:rotten_flesh", "weight": 10},
                   {"type": "minecraft:item", "name": "minecraft:bamboo", "weight": 10, "conditions": [
                     {"condition": "minecraft:location_check", "predicate": {"biomes": ["minecraft:jungle", "minecraft:sparse_jungle", "minecraft:bamboo_jungle"]}}]},
                   {"type": "minecraft:item", "name": "reactingreactions:polymetallic_nodule", "weight": 8}]}]}
                """);
    }

    // ---- tags --------------------------------------------------------------------------------------------------

    private static void tags(Data data) {
        // This mod's paints count as dyes of their colour, so every dye recipe accepts them.
        Data c = data.ns("c");
        for (String colour : COLORS) {
            tag(c, "item", "dyes/" + colour, "reactingreactions:" + colour + "_paint");
        }
        tag(c, "item", "fertilizers", "reactingreactions:super_bone_meal");
        // Common (c:) tags, so other mods' recipes and machines recognise this mod's materials. Each item joins its subtag
        // (c:ingots/lead) and the parent (c:ingots), as NeoForge's conventions ask.
        commonItemTags(c);

        // Accessories slots.
        Data acc = data.ns("accessories");
        tag(acc, "item", "anklet", "reactingreactions:racing_anklet");
        tag(acc, "item", "back", "reactingreactions:aerozine_thrusters");
                tag(acc, "item", "cape", "reactingreactions:flame_retardant_cloak");
        tag(acc, "item", "charm", "reactingreactions:anchor_charm");
        tag(acc, "item", "face", "reactingreactions:gas_mask", "reactingreactions:oxygen_mask");
        tag(acc, "item", "hand", "reactingreactions:magnesium_knuckle", "reactingreactions:chemical_gloves");
        tag(acc, "item", "necklace", "reactingreactions:helium_locket");
        tag(acc, "item", "ring", "reactingreactions:digging_ring");
        tag(acc, "item", "shoes", "reactingreactions:spring_boots", "reactingreactions:chemical_boots", "reactingreactions:diving_fins");

        // The same items in Curios' preset slots, which players only get when some mod assigns them. Curios has no anklet or feet slot, so those are ours.
        Data curios = data.ns("curios");
        tag(curios, "item", "anklet", "reactingreactions:racing_anklet");
        tag(curios, "item", "back", "reactingreactions:aerozine_thrusters");
                tag(curios, "item", "body", "reactingreactions:flame_retardant_cloak");
        tag(curios, "item", "charm", "reactingreactions:anchor_charm");
        tag(curios, "item", "head", "reactingreactions:gas_mask", "reactingreactions:oxygen_mask");
        tag(curios, "item", "hands", "reactingreactions:magnesium_knuckle", "reactingreactions:chemical_gloves");
        tag(curios, "item", "necklace", "reactingreactions:helium_locket");
        tag(curios, "item", "ring", "reactingreactions:digging_ring");
        tag(curios, "item", "feet", "reactingreactions:spring_boots", "reactingreactions:chemical_boots", "reactingreactions:diving_fins");
        for (String slot : new String[] {"anklet", "feet"}) {
            data.raw("curios/slots", slot, """
                    {"size": 1}
                    """);
        }
        data.raw("curios/entities", "players", """
                {"entities": ["player"], "slots": ["anklet", "back", "body", "charm", "feet", "hands", "head", "necklace", "ring"]}
                """);

        // Sable mass classes: untagged blocks count as normal weight, right for the metal machines, so only the others are listed.
        Data sable = data.ns("sable");
        tag(sable, "block", "super_light", "reactingreactions:reinforced_glass", "reactingreactions:derrick_truss");
        tag(sable, "block", "light", "reactingreactions:varnished_planks", "reactingreactions:neon_lamp", "reactingreactions:drill_pipe");
        tag(sable, "block", "heavy", "reactingreactions:oil_shale", "reactingreactions:polymetallic_nodule",
                "reactingreactions:rich_asurine_vein", "reactingreactions:rich_crimsite_vein", "reactingreactions:rich_ochrum_vein",
                "reactingreactions:rich_veridium_vein", "reactingreactions:rich_scoria_vein", "reactingreactions:rich_tuff_vein",
                "reactingreactions:rich_granite_vein", "reactingreactions:rich_diorite_vein", "reactingreactions:rich_oil_vein");

        // Fluids the Charging Pad accepts: aerozine for the flight gear, acetylene for the lamp, breathable gas for Create's backtanks.
        tag(data, "fluid", "rechargeable", "reactingreactions:aerozine", "reactingreactions:acetylene", "reactingreactions:oxygen",
                "reactingreactions:compressed_air", "reactingreactions:nitrogen", "reactingreactions:hydrogen", "reactingreactions:mineral_oil",
                "reactingreactions:drill_grease", "reactingreactions:neon");
        // Create's Capacity enchantment applies to this tag (and Create only reads backtank air from items that have some),
        // so the tank items can take Capacity for a bigger tank.
        tag(data.ns("create"), "item", "pressurized_air_sources", "reactingreactions:aerozine_thrusters", "reactingreactions:acetylene_lamp",
                "reactingreactions:composite_exo_helmet", "reactingreactions:composite_exo_chestplate", "reactingreactions:composite_exo_leggings",
                "reactingreactions:composite_exo_boots", "reactingreactions:plasma_multitool", "reactingreactions:neon_blade");
        // Vanilla's enchantability tags, so the enchanting table and anvil take the titanium and chase gear.
        Data mc = data.ns("minecraft");
        tag(mc, "item", "head_armor", "reactingreactions:titanium_helmet", "reactingreactions:composite_exo_helmet");
        // Like Aeronautics' Aviator's Goggles; the Create goggle overlay they also give is registered in ExoClient.
        tag(mc, "item", "freeze_immune_wearables", "reactingreactions:composite_exo_helmet");
        tag(mc, "item", "chest_armor", "reactingreactions:titanium_chestplate", "reactingreactions:composite_exo_chestplate");
        tag(mc, "item", "leg_armor", "reactingreactions:titanium_leggings", "reactingreactions:composite_exo_leggings");
        tag(mc, "item", "foot_armor", "reactingreactions:titanium_boots", "reactingreactions:composite_exo_boots");
        tag(mc, "item", "pickaxes", "reactingreactions:titanium_pickaxe", "reactingreactions:plasma_multitool");
        tag(mc, "item", "axes", "reactingreactions:titanium_axe");
        tag(mc, "item", "shovels", "reactingreactions:titanium_shovel");
        tag(mc, "item", "hoes", "reactingreactions:titanium_hoe");
        tag(mc, "item", "swords", "reactingreactions:titanium_sword", "reactingreactions:neon_blade");
        tag(mc, "item", "enchantable/bow", "reactingreactions:titanium_bow");

        // Create Diesel Generators' fuels and distillation read these tags, so joining them is all its compat needs.
        commonFluidTags(c);
        // Every fluid that burns as fuel.
        tag(c, "fluid", "fuel", "reactingreactions:seed_oil", "reactingreactions:mineral_oil", "reactingreactions:crude_oil",
                "reactingreactions:ethanol", "reactingreactions:diesel", "reactingreactions:naphtha",
                "reactingreactions:methane", "reactingreactions:hydrogen", "reactingreactions:lpg", "reactingreactions:acetylene");

        // Anything a plant-and-water fermentation or biomass recipe accepts as "a plant".
        tag(data, "item", "plant_matter", "#minecraft:leaves", "#minecraft:saplings", "#minecraft:flowers", "minecraft:wheat",
                "minecraft:sugar_cane", "minecraft:cactus", "minecraft:kelp", "minecraft:seagrass", "minecraft:vine", "minecraft:fern",
                "minecraft:large_fern", "minecraft:short_grass", "minecraft:tall_grass", "minecraft:bamboo", "minecraft:carrot",
                "minecraft:potato", "minecraft:beetroot", "minecraft:pumpkin", "minecraft:melon_slice", "minecraft:sweet_berries",
                "minecraft:lily_pad");
    }

    private static final String[][] METALS = {
            {"aluminum", "aluminum_ingot", "aluminum_nugget"}, {"lead", "lead_ingot", "lead_nugget"}, {"lithium", "lithium_ingot", "lithium_nugget"},
            {"nickel", "nickel_ingot", "nickel_nugget"}, {"steel", "steel_ingot", null}, {"titanium", "titanium", null},
            {"magnesium", "magnesium", null}, {"manganese", "manganese", null}};

    private static void commonItemTags(Data c) {
        Map<String, List<String>> tags = new LinkedHashMap<>();
        BiConsumer<String, String> add = (path, item) -> tags.computeIfAbsent(path, k -> new ArrayList<>()).add("reactingreactions:" + item);
        for (String[] metal : METALS) {
            add.accept("ingots/" + metal[0], metal[1]);
            add.accept("ingots", metal[1]);
            if (metal[2] != null) {
                add.accept("nuggets/" + metal[0], metal[2]);
                add.accept("nuggets", metal[2]);
            }
        }
        for (String[] plate : new String[][] {{"steel", "steel_sheet"}, {"titanium", "titanium_sheet"}, {"plastic", "hdpe_sheet"}}) {
            add.accept("plates/" + plate[0], plate[1]);
            add.accept("plates", plate[1]);
        }
        add.accept("gems/ruby", "ruby");
        add.accept("gems", "ruby");
        // Dusts by material; silica is quartz dust, the name other mods use for it.
        for (String[] dust : new String[][] {{"sulfur", "sulfur_dust"}, {"salt", "salt"}, {"borax", "borax"}, {"rare_earth", "rare_earth_dust"},
                {"alumina", "alumina_dust"}, {"iron_oxide", "iron_oxide"}, {"chromium", "chromium_dust"}, {"quartz", "silica"},
                {"granite", "granite_dust"}, {"diorite", "diorite_dust"}, {"tuff", "tuff_dust"}, {"scoria", "scoria_dust"},
                {"asurine", "asurine_dust"}, {"crimsite", "crimsite_dust"}, {"ochrum", "ochrum_dust"}, {"veridium", "veridium_dust"}}) {
            add.accept("dusts/" + dust[0], dust[1]);
            add.accept("dusts", dust[1]);
        }
        add.accept("coal_coke", "coal_coke");
        add.accept("slag", "slag");
        add.accept("strings", "nylon_fiber");
        tags.forEach((path, items) -> tag(c, "item", path, items.toArray(String[]::new)));
    }

    /** The c: fluid tag files, from the same map fluid inputs use (Data.FLUID_TAGS). */
    private static void commonFluidTags(Data c) {
        Data.FLUID_TAGS.forEach((fluid, tag) -> tag(c, "fluid", tag.substring("c:".length()), fluid));
    }

    /** A tag file; entries starting with # reference other tags. */
    private static void tag(Data d, String kind, String path, String... values) {
        JsonObject json = new JsonObject();
        json.addProperty("replace", false);
        JsonArray array = new JsonArray();
        for (String v : values) {
            array.add(v);
        }
        json.add("values", array);
        d.add("tags/" + kind, path, json);
    }

    // ---- worldgen ----------------------------------------------------------------------------------------------

    private static void worldgen(Data data) {
        // Oil: huge veins deep underground, a mix of oil shale and liquid crude oil sources. One ore blob is capped at 64
        // blocks, so each vein is many overlapping blobs (the placement's count), packed into a narrow height band.
        data.raw("worldgen/configured_feature", "oil_shale_vein_huge", """
                {"type": "minecraft:ore", "config": {"size": 64, "discard_chance_on_air_exposure": 0.5, "targets": [
                  {"target": {"predicate_type": "minecraft:random_block_match", "block": "minecraft:stone", "probability": 0.3},
                   "state": {"Name": "reactingreactions:crude_oil", "Properties": {"level": "0"}}},
                  {"target": {"predicate_type": "minecraft:random_block_match", "block": "minecraft:deepslate", "probability": 0.3},
                   "state": {"Name": "reactingreactions:crude_oil", "Properties": {"level": "0"}}},
                  {"target": {"predicate_type": "minecraft:random_block_match", "block": "minecraft:stone", "probability": 0.12},
                   "state": {"Name": "minecraft:tuff"}},
                  {"target": {"predicate_type": "minecraft:random_block_match", "block": "minecraft:deepslate", "probability": 0.12},
                   "state": {"Name": "minecraft:tuff"}},
                  {"target": {"predicate_type": "minecraft:tag_match", "tag": "minecraft:stone_ore_replaceables"},
                   "state": {"Name": "reactingreactions:oil_shale"}},
                  {"target": {"predicate_type": "minecraft:tag_match", "tag": "minecraft:deepslate_ore_replaceables"},
                   "state": {"Name": "reactingreactions:oil_shale"}}]}}
                """);
        data.raw("worldgen/placed_feature", "oil_shale_vein_huge", """
                {"feature": "reactingreactions:oil_shale_vein_huge", "placement": [
                  {"type": "minecraft:rarity_filter", "chance": 32},
                  {"type": "minecraft:count", "count": {"type": "minecraft:uniform", "min_inclusive": 9, "max_inclusive": 14}},
                  {"type": "minecraft:in_square"},
                  {"type": "minecraft:height_range", "height": {"type": "minecraft:uniform",
                    "min_inclusive": {"absolute": -42}, "max_inclusive": {"absolute": -16}}},
                  {"type": "minecraft:biome"}]}
                """);
        // Create Diesel Generators' own oil generation replaces this when it is installed.
        data.raw("neoforge/biome_modifier", "oil_deposits", """
                {"neoforge:conditions": [{"type": "neoforge:not", "value": {"type": "neoforge:mod_loaded", "modid": "createdieselgenerators"}}],
                 "type": "neoforge:add_features", "biomes": "#minecraft:is_overworld",
                 "features": ["reactingreactions:oil_shale_vein_huge"], "step": "underground_ores"}
                """);

        // Polymetallic nodules: scattered patches lying waterlogged on the ocean floor.
        data.raw("worldgen/configured_feature", "polymetallic_nodules", """
                {"type": "minecraft:random_patch", "config": {"tries": 24, "xz_spread": 5, "y_spread": 2, "feature": {
                  "feature": {"type": "minecraft:simple_block", "config": {"to_place": {"type": "minecraft:simple_state_provider",
                    "state": {"Name": "reactingreactions:polymetallic_nodule", "Properties": {"waterlogged": "true"}}}}},
                  "placement": [{"type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:all_of", "predicates": [
                    {"type": "minecraft:matching_blocks", "blocks": "minecraft:water"},
                    {"type": "minecraft:would_survive", "state": {"Name": "reactingreactions:polymetallic_nodule", "Properties": {"waterlogged": "true"}}}]}}]}}}
                """);
        data.raw("worldgen/placed_feature", "polymetallic_nodules", """
                {"feature": "reactingreactions:polymetallic_nodules", "placement": [
                  {"type": "minecraft:rarity_filter", "chance": 2},
                  {"type": "minecraft:in_square"},
                  {"type": "minecraft:heightmap", "heightmap": "OCEAN_FLOOR_WG"},
                  {"type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:matching_fluids", "fluids": "minecraft:water"}},
                  {"type": "minecraft:biome"}]}
                """);
        data.raw("neoforge/biome_modifier", "polymetallic_nodule", """
                {"type": "neoforge:add_features", "biomes": "#minecraft:is_ocean",
                 "features": ["reactingreactions:polymetallic_nodules"], "step": "vegetal_decoration"}
                """);
    }

    /**
     * Create Diesel Generators fuel types for our fuels that no common tag covers. Its diesel and plant oil entries,
     * scaled by our low, medium and high fuel tiers.
     */
    private static void dieselGeneratorsFuelTypes(Data data) {
        Data cdg = data.ns("createdieselgenerators");
        fuelType(cdg, "mineral_oil", "reactingreactions:mineral_oil", 0.6, 0.4, 64, 1536, 64, 2048, 176, 3072);
        fuelType(cdg, "naphtha", "reactingreactions:naphtha", 0.95, 0.55, 96, 5120, 96, 7168, 224, 14336);
        fuelType(cdg, "methane", "reactingreactions:methane", 1.0, 1.2, 96, 7168, 96, 9216, 224, 18432);
        // Hydrogen is a lossy store of electrolysis' energy, not a source: it burns 10 times as fast as the others, so a bucket
        // gives at most ~2 MJ (huge or turbocharged engine, EE alternator), half the 4 MJ electrolysis took (Voltmeter).
        fuelType(cdg, "hydrogen", "reactingreactions:hydrogen", 1.0, 1.4, 96, 8192, 96, 10240, 224, 20480, 0.5);
        fuelType(cdg, "lpg", "reactingreactions:lpg", 1.0, 1.2, 96, 7168, 96, 9216, 224, 18432);
        fuelType(cdg, "acetylene", "reactingreactions:acetylene", 1.05, 1.5, 96, 8192, 96, 10240, 224, 20480);
    }

    /**
     * Turns off Create Diesel Generators' crude oil distillation, so crude oil goes through our fractionation with its
     * naphtha and LPG. The files replace its own (we load after it) and never load as recipes.
     */
    private static void dieselGeneratorsRecipeOverrides(Data data) {
        Data cdg = data.ns("createdieselgenerators");
        cdg.raw("recipe", "distillation/crude_oil", """
                {"neoforge:conditions": [{"type": "neoforge:false"}]}
                """);
        cdg.raw("recipe", "distillation/superheated_crude_oil", """
                {"neoforge:conditions": [{"type": "neoforge:false"}]}
                """);
    }

    private static void fuelType(Data cdg, String path, String fluidId, double soundPitch, double burnerMultiplier,
            double normalSpeed, double normalStrength, double modularSpeed, double modularStrength, double hugeSpeed, double hugeStrength) {
        fuelType(cdg, path, fluidId, soundPitch, burnerMultiplier, normalSpeed, normalStrength, modularSpeed, modularStrength, hugeSpeed, hugeStrength,
                0.05);
    }

    /** {@code burnRate} is in mB a tick, for every engine size (a modular engine burns it per block). */
    private static void fuelType(Data cdg, String path, String fluidId, double soundPitch, double burnerMultiplier,
            double normalSpeed, double normalStrength, double modularSpeed, double modularStrength, double hugeSpeed, double hugeStrength,
            double burnRate) {
        cdg.raw("createdieselgenerators/fuel_type", path, """
                {"neoforge:conditions": [{"type": "neoforge:mod_loaded", "modid": "createdieselgenerators"}],
                 "fluid": "%s", "sound_pitch": %s, "burner_multiplier": %s,
                 "normal": {"speed": %s, "strength": %s, "burn_rate": %s},
                 "modular": {"speed": %s, "strength": %s, "burn_rate": %s},
                 "huge": {"speed": %s, "strength": %s, "burn_rate": %s}}
                """.formatted(fluidId, soundPitch, burnerMultiplier, normalSpeed, normalStrength, burnRate, modularSpeed, modularStrength, burnRate,
                hugeSpeed, hugeStrength, burnRate));
    }

    /**
     * Rich Vein blocks (see {@code RichVeinFeature}): one custom feature, run after all the other underground features, finds the
     * veins of every drillable rock and of oil and gives each a Rich Vein block; it also seals oil sources exposed to air.
     */
    private static void richVeins(Data data) {
        data.raw("worldgen/configured_feature", "rich_veins", """
                {"type": "reactingreactions:rich_veins", "config": {}}
                """);
        data.raw("worldgen/placed_feature", "rich_veins", """
                {"feature": "reactingreactions:rich_veins", "placement": [{"type": "minecraft:biome"}]}
                """);
        // Which rocks hide Rich Veins: Create's in almost every vein, vanilla's blobs are everywhere so only some.
        JsonObject veins = new JsonObject();
        for (String[] rock : new String[][] {{"create:asurine", "rich_asurine_vein", "0.9"}, {"create:crimsite", "rich_crimsite_vein", "0.9"},
                {"create:ochrum", "rich_ochrum_vein", "0.9"}, {"create:veridium", "rich_veridium_vein", "0.9"}, {"create:scoria", "rich_scoria_vein", "0.9"},
                {"minecraft:tuff", "rich_tuff_vein", "0.3"}, {"minecraft:granite", "rich_granite_vein", "0.3"}, {"minecraft:diorite", "rich_diorite_vein", "0.3"}}) {
            JsonObject vein = new JsonObject();
            vein.addProperty("rich", "reactingreactions:" + rock[1]);
            vein.addProperty("chance", Float.parseFloat(rock[2]));
            veins.add(rock[0], vein);
        }
        JsonObject oil = new JsonObject();
        oil.addProperty("rich", "reactingreactions:rich_oil_vein");
        oil.addProperty("min_vein_size", 20);
        veins.add("reactingreactions:oil_shale", oil);
        JsonObject veinMap = new JsonObject();
        veinMap.add("values", veins);
        data.raw("data_maps/block", "rich_vein", veinMap.toString());
        data.raw("neoforge/biome_modifier", "rich_veins", """
                {"type": "neoforge:add_features", "biomes": "#minecraft:is_overworld",
                 "features": ["reactingreactions:rich_veins"], "step": "underground_decoration"}
                """);
    }

    /** {id, icon, parent, frame, trigger item or "impossible", title, description}. The root uses the tick trigger. */
    private static final String[][] ADVANCEMENTS = {
            {"steel", "steel_ingot", "root", "task", "steel_ingot", "First Steel", "Make a steel ingot."},
            {"reaction_chamber", "reaction_chamber_controller", "steel", "task", "reaction_chamber_controller", "Reaction Chamber", "Build a Reaction Chamber controller."},
            {"distillation", "distillation_tower_controller", "steel", "task", "distillation_tower_controller", "Distillation", "Build a Distillation Tower controller."},
            {"electrolysis", "electrolysis_vat_controller", "steel", "task", "electrolysis_vat_controller", "Electrolysis", "Build an Electrolysis Vat controller."},
            {"plastics", "hdpe_sheet", "reaction_chamber", "task", "hdpe_sheet", "Plastics", "Make an HDPE sheet."},
            {"titanium", "titanium", "reaction_chamber", "goal", "titanium", "Light Metal", "Make titanium."},
            {"aerozine", "composite_exo_chestplate", "titanium", "goal", "composite_exo_chestplate", "Rocketeer", "Assemble a Composite Exo-Chestplate."},
            {"black_gold", "crude_oil_bucket", "distillation", "task", "crude_oil_bucket", "Black Gold", "Get a bucket of crude oil."},
            {"derrick", "derrick_controller", "black_gold", "task", "derrick_controller", "Derrick", "Make a Derrick controller."},
            {"rich_vein", "rich_oil_vein", "derrick", "goal", "impossible", "Jackpot", "Break a Rich Vein."},
            {"drilled", "crimsite_dust", "derrick", "task", "crimsite_dust", "Rock Solid", "Get crimsite dust."},
            {"induction", "induction_heater_connector", "steel", "task", "induction_heater_connector", "Induction", "Make an Induction Heater connector."},
            {"bad_air", "bromine", "steel", "task", "impossible", "Bad Air", "Get toxic exposure on your gauge."},
            {"gas_mask", "gas_mask", "bad_air", "task", "gas_mask", "Breathe Easy", "Make a Gas Mask."},
            {"scrubber", "atmospheric_scrubber", "gas_mask", "task", "atmospheric_scrubber", "Fresh Air", "Make an Atmospheric Scrubber."},
            {"detox", "antidote", "bad_air", "task", "antidote", "Detox", "Make an Antidote."},
            {"bad_idea", "acetylene_bucket", "bad_air", "challenge", "impossible", "Bad Idea", "Set off a toxic leak near you."},
    };

    private static void advancements(Data data) {
        data.raw("advancement", "root", """
                {"display": {"icon": {"id": "reactingreactions:reaction_chamber_controller"},
                  "title": {"translate": "advancements.reactingreactions.root.title"},
                  "description": {"translate": "advancements.reactingreactions.root.description"},
                  "background": "minecraft:textures/gui/advancements/backgrounds/stone.png",
                  "show_toast": false, "announce_to_chat": false},
                 "criteria": {"tick": {"trigger": "minecraft:tick"}}}
                """);
        for (String[] a : ADVANCEMENTS) {
            String criteria = a[4].equals("impossible")
                    ? "{\"impossible\": {\"trigger\": \"minecraft:impossible\"}}"
                    : "{\"has_item\": {\"trigger\": \"minecraft:inventory_changed\", \"conditions\": {\"items\": [{\"items\": \"reactingreactions:" + a[4] + "\"}]}}}";
            String requirement = a[4].equals("impossible") ? "impossible" : "has_item";
            data.raw("advancement", a[0], """
                    {"display": {"icon": {"id": "reactingreactions:%s"},
                      "title": {"translate": "advancements.reactingreactions.%s.title"},
                      "description": {"translate": "advancements.reactingreactions.%s.description"},
                      "frame": "%s"},
                     "parent": "reactingreactions:%s",
                     "criteria": %s,
                     "requirements": [["%s"]]}
                    """.formatted(a[1], a[0], a[0], a[3], a[2], criteria, requirement));
        }
    }

    // ---- data maps ---------------------------------------------------------------------------------------------

    private static void toxicity(Data data) {
        JsonObject fluids = new JsonObject();
        for (Object[] row : ToxicDefaults.FLUIDS) {
            JsonObject value = new JsonObject();
            value.addProperty("toxicity", ((Integer) row[1]).floatValue());
            if ((Boolean) row[2]) value.addProperty("flammable", true);
            if ((Boolean) row[3]) value.addProperty("explosive", true);
            fluids.add("reactingreactions:" + row[0], value);
        }
        JsonObject fluidMap = new JsonObject();
        fluidMap.add("values", fluids);
        data.raw("data_maps/fluid", "toxicity", fluidMap.toString());

        JsonObject items = new JsonObject();
        for (Object[] row : ToxicDefaults.ITEMS) {
            JsonObject value = new JsonObject();
            value.addProperty("toxicity", ((Integer) row[1]).floatValue());
            items.add("reactingreactions:" + row[0], value);
        }
        JsonObject itemMap = new JsonObject();
        itemMap.add("values", items);
        data.raw("data_maps/item", "toxic_item", itemMap.toString());

        // The damage type of toxic exposure: it ignores armour.
        data.raw("damage_type", "toxicity", """
                {"message_id": "reactingreactions.toxicity", "exhaustion": 0.0, "scaling": "never"}
                """);
        tag(data.ns("minecraft"), "damage_type", "bypasses_armor", "reactingreactions:toxicity");

        // Blocks that keep contamination in and count as a roof or floor for scrubbers. Trim or extend with a datapack.
        tag(data, "block", "airtight",
                "minecraft:iron_block", "minecraft:copper_block", "minecraft:exposed_copper", "minecraft:weathered_copper",
                "minecraft:oxidized_copper", "minecraft:cut_copper", "minecraft:gold_block", "minecraft:netherite_block",
                "minecraft:iron_door", "minecraft:iron_trapdoor",
                "create:andesite_casing", "create:brass_casing", "create:copper_casing", "create:railway_casing",
                "create:industrial_iron_block", "reactingreactions:steel_casing", "reactingreactions:reinforced_glass",
                "reactingreactions:varnished_planks", "reactingreactions:derrick_block", "reactingreactions:reaction_chamber_wall",
                "reactingreactions:electrolysis_vat_wall", "reactingreactions:distillation_tower_wall", "reactingreactions:airless_oven_wall");
        // What cleans up a leak pool (one is used up per pool).
        tag(data, "item", "cleaning_agents", "reactingreactions:soap", "reactingreactions:bleach_bottle", "minecraft:sponge");
        // Anything that sets off flammable pools and vents.
        tag(data, "block", "ignites_toxic",
                "minecraft:torch", "minecraft:wall_torch", "minecraft:soul_torch", "minecraft:soul_wall_torch", "minecraft:fire",
                "minecraft:soul_fire", "minecraft:campfire", "minecraft:soul_campfire", "minecraft:lava", "minecraft:lantern",
                "minecraft:soul_lantern", "#minecraft:candles", "minecraft:magma_block", "create:blaze_burner");
    }

    private static void dataMaps(Data data) {
        toxicity(data);
        // Magnesium burns as fuel in a superheated Blaze Burner.
        data.ns("create").raw("data_maps/item", "superheated_blaze_burner_fuels", """
                {"values": {"reactingreactions:magnesium": {"burn_time": 3200}}}
                """);
    }
}
