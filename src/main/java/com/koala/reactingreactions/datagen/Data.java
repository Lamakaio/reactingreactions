package com.koala.reactingreactions.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.content.compat.DieselGeneratorsCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Collects every generated data-pack file, with a thin JSON DSL: most recipes use other mods' recipe types, which the
 * generator only writes and never loads. A string ingredient is an item id, or a tag when it starts with {@code #}.
 */
public final class Data {
    public record Entry(String namespace, String dir, String path, JsonElement json) {
    }

    private final List<Entry> entries;
    private final String namespace;
    /** When set, every recipe written through this view only loads while that mod is installed. */
    private final String requiredMod;

    /** Filled in when the entries are read, after every recipe has been fully built. */
    private final List<Runnable> twins;

    public Data() {
        this(new ArrayList<>(), ReactingReactions.MODID, null, new ArrayList<>());
    }

    private Data(List<Entry> entries, String namespace, String requiredMod, List<Runnable> twins) {
        this.entries = entries;
        this.namespace = namespace;
        this.requiredMod = requiredMod;
        this.twins = twins;
    }

    public List<Entry> entries() {
        twins.forEach(Runnable::run);
        twins.clear();
        return entries;
    }

    /** A view that writes into the same output but under another namespace (e.g. {@code "minecraft"} to override a vanilla recipe). */
    public Data ns(String namespace) {
        return new Data(entries, namespace, null, twins);
    }

    /** The same view, but every recipe written through it gets {@link RecipeJson#ifModLoaded} for {@code modid} (for recipes in an optional mod's namespace). */
    public Data requiringMod(String modid) {
        return new Data(entries, namespace, modid, twins);
    }

    public void add(String dir, String path, JsonElement json) {
        for (Entry e : entries) {
            if (e.namespace().equals(namespace) && e.dir().equals(dir) && e.path().equals(path)) {
                throw new IllegalStateException("Duplicate data file " + namespace + ":" + dir + "/" + path);
            }
        }
        entries.add(new Entry(namespace, dir, path, json));
    }

    // ---- ingredient / result helpers --------------------------------------------------------------------------

    /**
     * Item inputs written as their common tag, so equivalent items from other mods work too (another mod's steel, a quartz dust
     * for silica). Only genuinely interchangeable materials: nylon fibre is tagged c:strings but stays an item here, or plain
     * string would replace it.
     */
    public static final Map<String, String> ITEM_TAGS = Map.ofEntries(
            Map.entry("reactingreactions:steel_ingot", "c:ingots/steel"), Map.entry("reactingreactions:lead_ingot", "c:ingots/lead"),
            Map.entry("reactingreactions:nickel_ingot", "c:ingots/nickel"), Map.entry("reactingreactions:aluminum_ingot", "c:ingots/aluminum"),
            Map.entry("reactingreactions:lithium_ingot", "c:ingots/lithium"), Map.entry("reactingreactions:titanium", "c:ingots/titanium"),
            Map.entry("reactingreactions:magnesium", "c:ingots/magnesium"), Map.entry("reactingreactions:manganese", "c:ingots/manganese"),
            Map.entry("reactingreactions:lead_nugget", "c:nuggets/lead"), Map.entry("reactingreactions:nickel_nugget", "c:nuggets/nickel"),
            Map.entry("reactingreactions:aluminum_nugget", "c:nuggets/aluminum"), Map.entry("reactingreactions:lithium_nugget", "c:nuggets/lithium"),
            Map.entry("reactingreactions:steel_sheet", "c:plates/steel"), Map.entry("reactingreactions:titanium_sheet", "c:plates/titanium"),
            Map.entry("reactingreactions:hdpe_sheet", "c:plates/plastic"), Map.entry("reactingreactions:ruby", "c:gems/ruby"),
            Map.entry("reactingreactions:sulfur_dust", "c:dusts/sulfur"), Map.entry("reactingreactions:salt", "c:dusts/salt"),
            Map.entry("reactingreactions:borax", "c:dusts/borax"), Map.entry("reactingreactions:rare_earth_dust", "c:dusts/rare_earth"),
            Map.entry("reactingreactions:silica", "c:dusts/quartz"), Map.entry("reactingreactions:coal_coke", "c:coal_coke"),
            Map.entry("reactingreactions:slag", "c:slag"),
            Map.entry("minecraft:iron_ingot", "c:ingots/iron"), Map.entry("minecraft:iron_nugget", "c:nuggets/iron"),
            Map.entry("minecraft:copper_ingot", "c:ingots/copper"), Map.entry("minecraft:gold_ingot", "c:ingots/gold"),
            Map.entry("minecraft:gold_nugget", "c:nuggets/gold"), Map.entry("create:zinc_ingot", "c:ingots/zinc"),
            Map.entry("create:zinc_nugget", "c:nuggets/zinc"), Map.entry("create:copper_nugget", "c:nuggets/copper"),
            Map.entry("create:iron_sheet", "c:plates/iron"), Map.entry("create:copper_sheet", "c:plates/copper"),
            Map.entry("minecraft:quartz", "c:gems/quartz"), Map.entry("minecraft:amethyst_shard", "c:gems/amethyst"),
            Map.entry("minecraft:diamond", "c:gems/diamond"), Map.entry("minecraft:emerald", "c:gems/emerald"),
            Map.entry("minecraft:redstone", "c:dusts/redstone"), Map.entry("minecraft:glowstone_dust", "c:dusts/glowstone"),
            Map.entry("minecraft:gunpowder", "c:gunpowders"), Map.entry("minecraft:bone", "c:bones"),
            Map.entry("minecraft:ender_pearl", "c:ender_pearls"), Map.entry("minecraft:glass", "c:glass_blocks/colorless"),
            Map.entry("minecraft:sand", "c:sands/colorless"), Map.entry("minecraft:red_sand", "c:sands/red"),
            Map.entry("minecraft:barrel", "c:barrels/wooden"), Map.entry("minecraft:slime_ball", "c:slime_balls"),
            Map.entry("minecraft:string", "c:strings"), Map.entry("minecraft:obsidian", "c:obsidians"),
            Map.entry("minecraft:stick", "c:rods/wooden"), Map.entry("minecraft:copper_block", "c:storage_blocks/copper"));

    /**
     * This mod's fluids by their common tag (c:<name>, except 10% brine as c:brine and solvent, acetone, as c:acetone). Fluid
     * inputs use the tag, and MiscData writes the tag files from this same map.
     */
    public static final Map<String, String> FLUID_TAGS = fluidTags();

    private static Map<String, String> fluidTags() {
        Map<String, String> tags = new LinkedHashMap<>();
        for (String fluid : new String[] {"hydrogen", "oxygen", "nitrogen", "helium", "neon", "steam", "methane", "ethane", "propane", "butane",
                "ethylene", "propylene", "acetylene", "ammonia", "carbon_dioxide", "carbon_monoxide", "sulfuric_acid", "nitric_acid",
                "naphtha", "lpg", "mineral_oil", "bleach", "lye", "resin", "coolant", "compressed_air", "aerozine", "lithium_brine",
                "magnesium_chloride", "titanium_tetrachloride", "white_vinegar", "purified_water", "drill_grease",
                "diesel", "ethanol", "crude_oil"}) {
            tags.put("reactingreactions:" + fluid, "c:" + fluid);
        }
        tags.put("reactingreactions:strong_brine", "c:brine");
        tags.put("reactingreactions:solvent", "c:acetone");
        tags.put("reactingreactions:seed_oil", "c:plantoil");
        return tags;
    }

    /** An ingredient written as a JSON element: "#tag", "item:id" (its common tag when it has one), or an already-built element. */
    public static JsonElement ing(Object o) {
        if (o instanceof JsonElement e) {
            return e;
        }
        String s = (String) o;
        if (ITEM_TAGS.containsKey(s)) {
            s = "#" + ITEM_TAGS.get(s);
        }
        JsonObject obj = new JsonObject();
        if (s.startsWith("#")) {
            obj.addProperty("tag", s.substring(1));
        } else {
            obj.addProperty("item", s);
        }
        return obj;
    }

    /** A list of alternatives for one ingredient slot. */
    public static JsonElement any(Object... alternatives) {
        JsonArray array = new JsonArray();
        for (Object a : alternatives) {
            array.add(ing(a));
        }
        return array;
    }

    /** A Create/NeoForge fluid ingredient: {@code fluid("minecraft:water", 250)}. */
    public static JsonElement fluid(String id, int amountMb) {
        JsonObject obj = new JsonObject();
        String tag = FLUID_TAGS.get(id);
        if (tag != null) {
            // Any fluid in the common tag, so another mod's hydrogen or ethanol works too.
            obj.addProperty("type", "neoforge:tag");
            obj.addProperty("amount", amountMb);
            obj.addProperty("tag", tag);
        } else {
            obj.addProperty("type", "neoforge:single");
            obj.addProperty("amount", amountMb);
            obj.addProperty("fluid", id);
        }
        return obj;
    }

    /** A fluid ingredient in Create's older {@code fluid_stack} shape, which Create Diesel Generators' recipes read. */
    public static JsonElement fluidStack(String id, int amountMb) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "fluid_stack");
        obj.addProperty("fluid", id);
        obj.addProperty("amount", amountMb);
        return obj;
    }

    /** Our fluids that Create Diesel Generators replaces with its own. */
    private static final Map<String, String> DIESEL_GENERATORS_FLUIDS = Map.of("reactingreactions:ethanol", "createdieselgenerators:ethanol",
            "reactingreactions:diesel", "createdieselgenerators:diesel", "reactingreactions:seed_oil", "createdieselgenerators:plant_oil",
            "reactingreactions:crude_oil", "createdieselgenerators:crude_oil");

    private static JsonElement dieselGeneratorsFluidStack(JsonObject ingredient) {
        String fluid = ingredient.has("fluid") ? ingredient.get("fluid").getAsString() : FLUID_TAGS.entrySet().stream()
                .filter(e -> e.getValue().equals(ingredient.get("tag").getAsString())).findFirst().orElseThrow().getKey();
        return fluidStack(DIESEL_GENERATORS_FLUIDS.getOrDefault(fluid, fluid), ingredient.get("amount").getAsInt());
    }

    /** Repeats an ingredient {@code n} times where it is used in an ingredient list. */
    public record Rep(int n, Object ingredient) {
    }

    public static Rep x(int n, Object ingredient) {
        return new Rep(n, ingredient);
    }

    /** A crafting result: {@code res("minecraft:stick", 4)}. */
    public record Result(String id, Integer count) {
    }

    public static Result res(String id) {
        return new Result(id, null);
    }

    public static Result res(String id, int count) {
        return new Result(id, count);
    }

    public static JsonElement json(String raw) {
        return JsonParser.parseString(raw);
    }

    /** One step of a sequenced assembly, built around its transitional item. */
    public interface Step {
        JsonObject build(String transitional);
    }

    public static Step press() {
        return transitional -> step("create:pressing", transitional);
    }

    public static Step cut() {
        return transitional -> step("create:cutting", transitional);
    }

    public static Step deploy(Object ingredient) {
        return transitional -> {
            JsonObject step = step("create:deploying", transitional);
            step.getAsJsonArray("ingredients").add(ing(ingredient));
            return step;
        };
    }

    public static Step fill(String fluid, int amountMb) {
        return transitional -> {
            JsonObject step = step("create:filling", transitional);
            step.getAsJsonArray("ingredients").add(fluid(fluid, amountMb));
            return step;
        };
    }

    public static List<Step> presses(int count) {
        return Collections.nCopies(count, press());
    }

    private static JsonObject step(String type, String transitional) {
        JsonObject step = new JsonObject();
        step.addProperty("type", type);
        JsonArray ingredients = new JsonArray();
        JsonObject item = new JsonObject();
        item.addProperty("item", transitional);
        ingredients.add(item);
        step.add("ingredients", ingredients);
        JsonArray results = new JsonArray();
        JsonObject result = new JsonObject();
        result.addProperty("id", transitional);
        results.add(result);
        step.add("results", results);
        return step;
    }

    // ---- recipes --------------------------------------------------------------------------------------------------

    public RecipeJson recipe(String path, String type) {
        RecipeJson r = new RecipeJson(type, this, path);
        add("recipe", path, r.json);
        if (requiredMod != null) {
            r.ifModLoaded(requiredMod);
        }
        return r;
    }

    public RecipeJson shapeless(String path, String category, Result result, Object... ingredients) {
        RecipeJson r = recipe(path, "minecraft:crafting_shapeless");
        r.json.addProperty("category", category);
        r.in(ingredients);
        r.craftingResult(result);
        return r;
    }

    public RecipeJson shaped(String path, String category, Result result, String... pattern) {
        RecipeJson r = recipe(path, "minecraft:crafting_shaped");
        r.json.addProperty("category", category);
        JsonArray rows = new JsonArray();
        for (String row : pattern) {
            rows.add(row);
        }
        r.json.add("pattern", rows);
        r.craftingResult(result);
        return r;
    }

    /** Fluent builder over one recipe's JSON; every method returns itself. */
    public static final class RecipeJson {
        private final JsonObject json = new JsonObject();

        private final Data data;
        private final String path;

        private RecipeJson(String type, Data data, String path) {
            json.addProperty("type", type);
            this.data = data;
            this.path = path;
        }

        /** A twin for when Create Diesel Generators is installed, making its own ethanol, diesel and plant oil instead of ours. */
        public RecipeJson dieselGeneratorsTwin() {
            return dieselGeneratorsTwin(null);
        }

        /** The same, run by one of its machines ({@code type}), whose recipes read fluids as {@link #fluidStack}. */
        public RecipeJson dieselGeneratorsTwin(String type) {
            ifModNotLoaded(DieselGeneratorsCompat.MODID);
            JsonObject twin = new JsonObject();
            data.add("recipe", path + "_cdg", twin);
            data.twins.add(() -> {
                json.entrySet().forEach(e -> twin.add(e.getKey(), e.getValue().deepCopy()));
                if (type != null) {
                    twin.addProperty("type", type);
                    JsonArray ingredients = new JsonArray();
                    for (JsonElement ingredient : twin.getAsJsonArray("ingredients")) {
                        ingredients.add(ingredient instanceof JsonObject o && o.has("amount") ? dieselGeneratorsFluidStack(o) : ingredient);
                    }
                    twin.add("ingredients", ingredients);
                }
                for (JsonElement result : twin.getAsJsonArray("results")) {
                    JsonObject o = result.getAsJsonObject();
                    o.addProperty("id", DIESEL_GENERATORS_FLUIDS.getOrDefault(o.get("id").getAsString(), o.get("id").getAsString()));
                }
                twin.add("neoforge:conditions", JsonParser.parseString(
                        "[{\"type\": \"neoforge:mod_loaded\", \"modid\": \"" + DieselGeneratorsCompat.MODID + "\"}]"));
            });
            return this;
        }

        public RecipeJson group(String group) {
            json.addProperty("group", group);
            return this;
        }

        /** Shaped-recipe key: {@code key('#', "minecraft:planks")}. */
        public RecipeJson key(char symbol, Object ingredient) {
            JsonObject key = json.has("key") ? json.getAsJsonObject("key") : new JsonObject();
            key.add(String.valueOf(symbol), ing(ingredient));
            json.add("key", key);
            return this;
        }

        /** Appends ingredients (items, "#tags", fluids, {@link #x} repeats). */
        public RecipeJson in(Object... ingredients) {
            JsonArray array = json.has("ingredients") ? json.getAsJsonArray("ingredients") : new JsonArray();
            for (Object o : ingredients) {
                if (o instanceof Rep rep) {
                    for (int i = 0; i < rep.n(); i++) {
                        array.add(ing(rep.ingredient()));
                    }
                } else {
                    array.add(ing(o));
                }
            }
            json.add("ingredients", array);
            return this;
        }

        /** A single ingredient under the key {@code "ingredient"} (Create's cutting/pressing/crushing style). */
        public RecipeJson ingredient(Object ingredient) {
            json.add("ingredient", ing(ingredient));
            return this;
        }

        public RecipeJson heat(String requirement) {
            json.addProperty("heat_requirement", requirement);
            return this;
        }

        public RecipeJson time(int ticks) {
            json.addProperty("processing_time", ticks);
            return this;
        }

        /** The stirring speed band (absolute RPM of the shaft on top of the Reaction Chamber) a reaction needs. */
        public RecipeJson rpm(int min, int max) {
            json.addProperty("min_rpm", min);
            json.addProperty("max_rpm", max);
            return this;
        }

        public RecipeJson minVoltage(double volts) {
            json.addProperty("min_voltage", volts);
            return this;
        }

        public RecipeJson electrodes(String... ids) {
            JsonArray array = new JsonArray();
            for (String id : ids) {
                array.add(id);
            }
            json.add("electrodes", array);
            return this;
        }

        /** An item result of a processing recipe. */
        public RecipeJson out(String id) {
            return addResult(itemResult(id, null, null));
        }

        public RecipeJson out(String id, int count) {
            return addResult(itemResult(id, count, null));
        }

        public RecipeJson outChance(String id, double chance) {
            return addResult(itemResult(id, null, chance));
        }

        public RecipeJson outChance(String id, int count, double chance) {
            return addResult(itemResult(id, count, chance));
        }

        /** An item with its tank holding {@code amountMb} of {@code fluidId} (the fluid_tank component). */
        public RecipeJson outWithTank(String id, String fluidId, int amountMb) {
            JsonObject tank = new JsonObject();
            tank.addProperty("id", fluidId);
            tank.addProperty("amount", amountMb);
            JsonObject components = new JsonObject();
            components.add("reactingreactions:fluid_tank", tank);
            JsonObject o = itemResult(id, null, null);
            o.add("components", components);
            return addResult(o);
        }

        /** A fluid result of a processing recipe. */
        public RecipeJson fluidOut(String id, int amountMb) {
            JsonObject o = new JsonObject();
            o.addProperty("amount", amountMb);
            o.addProperty("id", id);
            return addResult(o);
        }

        /** A sequenced assembly's steps; each {@code steps} entry is a {@link Step} or a list of them. */
        public RecipeJson sequence(String transitional, int loops, Object... steps) {
            json.addProperty("loops", loops);
            JsonArray array = new JsonArray();
            for (Object step : steps) {
                for (Object one : step instanceof List<?> list ? list : List.of(step)) {
                    array.add(((Step) one).build(transitional));
                }
            }
            json.add("sequence", array);
            JsonObject item = new JsonObject();
            item.addProperty("id", transitional);
            json.add("transitional_item", item);
            return this;
        }

        /** Sets any other top-level field from raw JSON text. */
        public RecipeJson field(String name, String rawJson) {
            json.add(name, JsonParser.parseString(rawJson));
            return this;
        }

        /** Loads only while {@code modid} is installed. */
        public RecipeJson ifModLoaded(String modid) {
            return field("neoforge:conditions", "[{\"type\": \"neoforge:mod_loaded\", \"modid\": \"" + modid + "\"}]");
        }

        /** Loads only while {@code modid} is absent. */
        public RecipeJson ifModNotLoaded(String modid) {
            return field("neoforge:conditions", "[{\"type\": \"neoforge:not\", \"value\": {\"type\": \"neoforge:mod_loaded\", \"modid\": \"" + modid + "\"}}]");
        }

        private static JsonObject itemResult(String id, Integer count, Double chance) {
            JsonObject o = new JsonObject();
            if (chance != null) {
                o.addProperty("chance", chance);
            }
            if (count != null) {
                o.addProperty("count", count);
            }
            o.addProperty("id", id);
            return o;
        }

        private RecipeJson addResult(JsonObject result) {
            JsonArray array = json.has("results") ? json.getAsJsonArray("results") : new JsonArray();
            array.add(result);
            json.add("results", array);
            return this;
        }

        private void craftingResult(Result result) {
            JsonObject o = new JsonObject();
            if (result.count() != null) {
                o.addProperty("count", result.count());
            }
            o.addProperty("id", result.id());
            json.add("result", o);
        }
    }

    // ---- other data ------------------------------------------------------------------------------------------------

    /** A file of arbitrary JSON under the given directory of this namespace's data pack (e.g. {@code "worldgen/placed_feature"}). */
    public void raw(String dir, String path, String rawJson) {
        add(dir, path, JsonParser.parseString(rawJson));
    }
}
