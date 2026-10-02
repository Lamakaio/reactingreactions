package com.koala.reactingreactions.registry;

import com.koala.reactingreactions.content.info.CompoundInfo;
import com.koala.reactingreactions.content.info.InfoPageTexts;
import com.koala.reactingreactions.content.ponder.CRRPonderLang;
import com.tterrag.registrate.providers.ProviderType;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

/**
 * The language entries Registrate cannot derive from a registration: tooltips, the creative tab, configuration and
 * JEI text, goggle readouts and so on. Item, block and fluid names come from their registrations.
 */
final class CRRLang {
    private CRRLang() {
    }

    private static void exoOption(CRRRegistrate r, String key, String name) {
        r.addRawLang("reactingreactions.exo." + key, name);
        r.addRawLang("reactingreactions.exo." + key + ".off", "Off");
        r.addRawLang("reactingreactions.exo." + key + ".on", "On");
    }

    static void register(CRRRegistrate r) {
        // Curios names its slots from this key; the anklet and feet slots are ours (see MiscData).
        r.addRawLang("curios.identifier.anklet", "Anklet");
        r.addRawLang("item.reactingreactions.chemical_flask.filled", "Chemical Flask of %s");
        r.addRawLang("key.categories.reactingreactions", "Create: Reacting Reactions");
        r.addRawLang("key.reactingreactions.exo_settings", "Exo Settings");
        r.addRawLang("reactingreactions.exo.title", "Exo Settings");
        r.addRawLang("reactingreactions.exo.none", "No Composite Exo- gear, Plasma Multitool or Neon Blade on you.");
        exoOption(r, "night_vision", "Night Vision");
        exoOption(r, "flight", "Flight");
        exoOption(r, "boost", "Speed Boost");
        exoOption(r, "step_up", "Step Up");
        exoOption(r, "pickup", "Pickup");
        exoOption(r, "sweep", "Sweeping");
        exoOption(r, "looting", "Looting");
        r.addRawLang("reactingreactions.exo.shape", "Shape");
        String[][] shapes = {{"single", "Single"}, {"3x3", "3x3"}, {"5x5", "5x5"}, {"3x3x3", "3x3x3"}, {"tunnel", "Tunnel"}, {"vein", "Vein"}};
        for (String[] shape : shapes) {
            r.addRawLang("reactingreactions.exo.shape." + shape[0], shape[1]);
        }
        r.addRawLang("reactingreactions.exo.drops", "Drops");
        r.addRawLang("reactingreactions.exo.drops.fortune", "Fortune");
        r.addRawLang("reactingreactions.exo.drops.silk_touch", "Silk Touch");
        r.addRawLang("item.reactingreactions.blast_flask.filled", "Blast Flask of %s");
        r.addRawLang("curios.identifier.feet", "Feet");

        // ---- item tooltips ----
        r.addRawLang("reactingreactions.configuration.toxicity", "Toxic Compounds");
        r.addRawLang("reactingreactions.configuration.toxicity.tooltip", "Leaks, pools, vents, contamination and the toxicity gauge.");
        r.addRawLang("reactingreactions.configuration.enabled", "Enabled");
        r.addRawLang("reactingreactions.configuration.enabled.tooltip", "Master switch for everything about toxic compounds.");
        r.addRawLang("reactingreactions.configuration.leakRate", "Leak Rate");
        r.addRawLang("reactingreactions.configuration.leakRate.tooltip", "Multiplier on how often containers leak.");
        r.addRawLang("reactingreactions.configuration.leaksLoseFluid", "Leaks Lose Fluid");
        r.addRawLang("reactingreactions.configuration.leaksLoseFluid.tooltip", "Leaking tanks and machines lose the leaked fluid.");
        r.addRawLang("reactingreactions.configuration.leakAmountMb", "Leak Amount (mB)");
        r.addRawLang("reactingreactions.configuration.leakAmountMb.tooltip", "Millibuckets lost by one leak.");
        r.addRawLang("reactingreactions.configuration.pools", "Pools");
        r.addRawLang("reactingreactions.configuration.pools.tooltip", "Liquid leaks leave pools.");
        r.addRawLang("reactingreactions.configuration.maxPoolsPerChunk", "Pools Per Chunk");
        r.addRawLang("reactingreactions.configuration.maxPoolsPerChunk.tooltip", "Most pools in one chunk.");
        r.addRawLang("reactingreactions.configuration.vents", "Vents");
        r.addRawLang("reactingreactions.configuration.vents.tooltip", "Gas leaks vent as sprays.");
        r.addRawLang("reactingreactions.configuration.explosions", "Explosions");
        r.addRawLang("reactingreactions.configuration.explosions.tooltip", "Flammable pools and vents burn and explode.");
        r.addRawLang("reactingreactions.configuration.explosionsDestroyBlocks", "Explosions Destroy Blocks");
        r.addRawLang("reactingreactions.configuration.explosionsDestroyBlocks.tooltip", "Explosions break blocks. Off: they only hurt entities.");
        r.addRawLang("reactingreactions.configuration.explosionPowerCap", "Explosion Power Cap");
        r.addRawLang("reactingreactions.configuration.explosionPowerCap.tooltip", "Largest explosion power.");
        r.addRawLang("reactingreactions.configuration.ignitionSpawnsFire", "Ignition Spawns Fire");
        r.addRawLang("reactingreactions.configuration.ignitionSpawnsFire.tooltip", "Ignition sets nearby blocks on fire.");
        r.addRawLang("reactingreactions.configuration.explosionFireballs", "Explosion Fireballs");
        r.addRawLang("reactingreactions.configuration.explosionFireballs.tooltip", "Explosions throw fireballs.");
        r.addRawLang("reactingreactions.configuration.floorDrainRadius", "Floor Drain Radius");
        r.addRawLang("reactingreactions.configuration.floorDrainRadius.tooltip", "Blocks around a Floor Drain that it cleans.");
        r.addRawLang("reactingreactions.configuration.contaminationRadius", "Contamination Radius");
        r.addRawLang("reactingreactions.configuration.contaminationRadius.tooltip", "Blocks around a leak that get contaminated.");
        r.addRawLang("reactingreactions.configuration.damagePlants", "Damage Plants");
        r.addRawLang("reactingreactions.configuration.damagePlants.tooltip", "Contamination kills grass and plants.");
        r.addRawLang("reactingreactions.configuration.damageAnimals", "Damage Animals");
        r.addRawLang("reactingreactions.configuration.damageAnimals.tooltip", "Contamination and pools hurt mobs.");
        r.addRawLang("reactingreactions.configuration.gaugeEnabled", "Toxicity Gauge");
        r.addRawLang("reactingreactions.configuration.gaugeEnabled.tooltip", "Players have a toxicity gauge.");
        r.addRawLang("reactingreactions.configuration.gaugeDecayPerMinute", "Gauge Decay Per Minute");
        r.addRawLang("reactingreactions.configuration.gaugeDecayPerMinute.tooltip", "How much the gauge falls by itself.");
        r.addRawLang("reactingreactions.configuration.lethalAtMax", "Lethal At Max");
        r.addRawLang("reactingreactions.configuration.lethalAtMax.tooltip", "A full gauge is deadly.");
        r.addRawLang("reactingreactions.configuration.filtersConsumed", "Filters Consumed");
        r.addRawLang("reactingreactions.configuration.filtersConsumed.tooltip", "The Gas Mask uses up Carbon Filters.");
        r.addRawLang("reactingreactions.configuration.scrubberOpenRadius", "Scrubber Open Radius");
        r.addRawLang("reactingreactions.configuration.scrubberOpenRadius.tooltip", "Scrubber radius in the open.");
        r.addRawLang("reactingreactions.configuration.scrubberRoomRadius", "Scrubber Room Radius");
        r.addRawLang("reactingreactions.configuration.scrubberRoomRadius.tooltip", "Scrubber radius in a room.");
        r.addRawLang("advancements.reactingreactions.root.title", "Chemistry");
        r.addRawLang("advancements.reactingreactions.root.description", "Industrial chemistry on Create.");
        r.addRawLang("advancements.reactingreactions.steel.title", "First Steel");
        r.addRawLang("advancements.reactingreactions.steel.description", "Make a steel ingot.");
        r.addRawLang("advancements.reactingreactions.reaction_chamber.title", "Reaction Chamber");
        r.addRawLang("advancements.reactingreactions.reaction_chamber.description", "Build a Reaction Chamber controller.");
        r.addRawLang("advancements.reactingreactions.distillation.title", "Distillation");
        r.addRawLang("advancements.reactingreactions.distillation.description", "Build a Distillation Tower controller.");
        r.addRawLang("advancements.reactingreactions.electrolysis.title", "Electrolysis");
        r.addRawLang("advancements.reactingreactions.electrolysis.description", "Build an Electrolysis Vat controller.");
        r.addRawLang("advancements.reactingreactions.plastics.title", "Plastics");
        r.addRawLang("advancements.reactingreactions.plastics.description", "Make an HDPE sheet.");
        r.addRawLang("advancements.reactingreactions.titanium.title", "Light Metal");
        r.addRawLang("advancements.reactingreactions.titanium.description", "Make titanium.");
        r.addRawLang("advancements.reactingreactions.aerozine.title", "Rocketeer");
        r.addRawLang("advancements.reactingreactions.aerozine.description", "Make an aerozine chestplate.");
        r.addRawLang("advancements.reactingreactions.black_gold.title", "Black Gold");
        r.addRawLang("advancements.reactingreactions.black_gold.description", "Get a bucket of crude oil.");
        r.addRawLang("advancements.reactingreactions.derrick.title", "Derrick");
        r.addRawLang("advancements.reactingreactions.derrick.description", "Make a Derrick controller.");
        r.addRawLang("advancements.reactingreactions.rich_vein.title", "Jackpot");
        r.addRawLang("advancements.reactingreactions.rich_vein.description", "Break a Rich Vein.");
        r.addRawLang("advancements.reactingreactions.drilled.title", "Rock Solid");
        r.addRawLang("advancements.reactingreactions.drilled.description", "Get crimsite dust.");
        r.addRawLang("advancements.reactingreactions.bad_air.title", "Bad Air");
        r.addRawLang("advancements.reactingreactions.bad_air.description", "Get toxic exposure on your gauge.");
        r.addRawLang("advancements.reactingreactions.gas_mask.title", "Breathe Easy");
        r.addRawLang("advancements.reactingreactions.gas_mask.description", "Make a Gas Mask.");
        r.addRawLang("advancements.reactingreactions.scrubber.title", "Fresh Air");
        r.addRawLang("advancements.reactingreactions.scrubber.description", "Make an Atmospheric Scrubber.");
        r.addRawLang("advancements.reactingreactions.detox.title", "Detox");
        r.addRawLang("advancements.reactingreactions.detox.description", "Make an Antidote.");
        r.addRawLang("advancements.reactingreactions.bad_idea.title", "Bad Idea");
        r.addRawLang("advancements.reactingreactions.bad_idea.description", "Set off a toxic leak near you.");
        r.addRawLang("subtitles.reactingreactions.drill_rumble", "Drill rumbles");
        r.addRawLang("subtitles.reactingreactions.scrubber_hum", "Scrubber hums");
        r.addRawLang("subtitles.reactingreactions.gas_hiss", "Gas hisses");
        r.addRawLang("subtitles.reactingreactions.ignite_whoosh", "Whoosh");
        r.addRawLang("subtitles.reactingreactions.pool_splash", "Liquid splashes");
        r.addRawLang("subtitles.reactingreactions.leak_drip", "Leak drips");
        r.addRawLang("subtitles.reactingreactions.vat_buzz", "Electrolysis buzzes");
        r.addRawLang("subtitles.reactingreactions.boiling", "Liquid bubbles");
        r.addRawLang("subtitles.reactingreactions.oven_roar", "Oven roars");
        r.addRawLang("reactingreactions.hud.toxic_air", "Toxic air");
        r.addRawLang("config.jade.plugin_reactingreactions.rich_vein", "Rich Vein richness");
        r.addRawLang("config.jade.plugin_reactingreactions.derrick", "Derrick status");
        r.addRawLang("config.jade.plugin_reactingreactions.scrubber", "Scrubber status");
        r.addRawLang("config.jade.plugin_reactingreactions.filled_tanks", "Machine tanks, filled ones only");
        r.addRawLang("config.jade.plugin_reactingreactions.leak_pool", "Leak pool contents");
        r.addRawLang("advancements.reactingreactions.induction.title", "Induction");
        r.addRawLang("advancements.reactingreactions.induction.description", "Make an Induction Heater connector.");
        for (var page : InfoPageTexts.PAGES) {
            for (int i = 0; i < page.lines().size(); i++) {
                r.addRawLang(page.langKey(i), page.lines().get(i));
            }
        }
        r.addRawLang("reactingreactions.jei.page.can", "Can make: %s.");
        r.addRawLang("reactingreactions.jei.page.small_electrolyser.intro", "One block that splits simple fluids with any electrodes. It needs power: each recipe has a minimum voltage.");
        r.addRawLang("reactingreactions.jei.page.small_electrolyser.cannot", "Cannot make: %s. Those need the Electrolysis Vat with specific electrodes, or more than one fluid or a lot of items.");
        r.addRawLang("reactingreactions.jei.page.fermentation_barrel.intro", "One block that needs no power. It is slow, has no stirring and no heat.");
        r.addRawLang("reactingreactions.jei.page.fermentation_barrel.cannot", "Cannot make: %s. Those need the Reaction Chamber (stirring, heat or more slots).");
        r.addRawLang("entity.reactingreactions.leak_pool", "Leak Pool");
        for (var entry : CompoundInfo.all().values()) {
            r.addRawLang(entry.langKey(), entry.description());
        }
        // The hold-shift summary shows the same brief text on the item, the block's item or a fluid's bucket. 
        r.addDataGenerator(ProviderType.LANG, prov -> CRRPonderLang.provide(prov::add));
        r.addDataGenerator(ProviderType.LANG, prov -> {
            for (var entry : CompoundInfo.all().values()) {
                var id = ResourceLocation.fromNamespaceAndPath("reactingreactions", entry.id());
                if (BuiltInRegistries.BLOCK.containsKey(id)) {
                    prov.add("block.reactingreactions." + entry.id() + ".tooltip.summary", entry.description());
                } else if (BuiltInRegistries.ITEM.containsKey(id)) {
                    prov.add("item.reactingreactions." + entry.id() + ".tooltip.summary", entry.description());
                }
                if (BuiltInRegistries.ITEM.containsKey(id.withSuffix("_bucket"))) {
                    prov.add("item.reactingreactions." + entry.id() + "_bucket.tooltip.summary", entry.description());
                }
            }
        });
        r.addRawLang("reactingreactions.jei.induction", "Induction Heater");
        r.addRawLang("reactingreactions.jei.induction.shape", "A filled rectangle, at least 3x3, one connector.");
        r.addRawLang("reactingreactions.jei.induction.resistance", "Resistance: %s ohm per block.");
        r.addRawLang("reactingreactions.jei.induction.heat", "3x3: %s V smoulders, %s V heated, %s V superheated. Bigger needs more.");
        r.addRawLang("reactingreactions.jei.induction.burner", "Works as a Blaze Burner under machines.");
        r.addRawLang("reactingreactions.jei.drilling", "Drilling");
        r.addRawLang("reactingreactions.jei.drilling.rate_oil", "%s mB/s at %s RPM, %s mB/s at %s RPM");
        r.addRawLang("reactingreactions.jei.drilling.rate_item", "1 dust per %s s at %s RPM, %s s at %s RPM");
        r.addRawLang("reactingreactions.jei.drilling.richness", "Rates are for a full-richness vein; poorer is slower.");
        r.addRawLang("reactingreactions.jei.drilling.lubricant", "The rig needs a trickle of lubricant.");
        r.addRawLang("reactingreactions.jei.drilling.coolant", "The rig needs a trickle of coolant.");
        // EMI category titles; JEI takes its titles from the category icons.
        r.addRawLang("emi.category.reactingreactions.electrolysis", "Electrolysis");
        r.addRawLang("emi.category.reactingreactions.distillation", "Distillation");
        r.addRawLang("emi.category.reactingreactions.airless_oven", "Airless Oven");
        r.addRawLang("emi.category.reactingreactions.reaction", "Reaction");
        r.addRawLang("emi.category.reactingreactions.drilling", "Drilling");
        r.addRawLang("emi.category.reactingreactions.induction_heater", "Induction Heater");
        r.addRawLang("death.attack.reactingreactions.toxicity", "%1$s died of toxic exposure");
        // ---- other ----
        r.addRawLang("reactingreactions.jei.reaction.rpm", "%s-%s RPM");
        r.addRawLang("itemGroup.reactingreactions", "Create: Reacting Reactions");
        r.addRawLang("reactingreactions.recipe.electrolysis", "Electrolysis Vat");
        r.addRawLang("reactingreactions.recipe.distillation", "Distillation Tower");
        r.addRawLang("reactingreactions.recipe.airless_oven", "Airless Oven");
        r.addRawLang("reactingreactions.recipe.reaction", "Reaction Chamber");
        r.addRawLang("reactingreactions.jei.electrolysis.min_voltage", "Min. %s V");
        r.addRawLang("reactingreactions.gas_diffuser.no_gas", "No gas supplied");
        r.addRawLang("lifting_gas.reactingreactions.helium", "Helium");
        r.addRawLang("lifting_gas.reactingreactions.hydrogen", "Hydrogen");
        r.addRawLang("goggles.vat.reactingreactions.gold_steel_electrode", "Gold-Steel Electrolysis");
        r.addRawLang("goggles.vat.reactingreactions.lead_electrode", "Lead Electrolysis");
        r.addRawLang("reactingreactions.configuration.title", "Create: Reacting Reactions Settings");
        r.addRawLang("reactingreactions.configuration.section.reactingreactions.server.toml", "Create: Reacting Reactions Settings");
        r.addRawLang("reactingreactions.configuration.section.reactingreactions.server.toml.title", "Create: Reacting Reactions Settings");
        r.addRawLang("reactingreactions.jei.multiblock.title", "Multiblock Structure");
        r.addRawLang("reactingreactions.jei.multiblock.vat.size", "Min. 5x3x3, closed roof, solid floor");
        r.addRawLang("reactingreactions.jei.multiblock.vat.electrodes", "2 electrode columns, terminals in the end walls");
        r.addRawLang("reactingreactions.jei.multiblock.min_voltage", "Min. voltage: %s V");
        r.addRawLang("reactingreactions.jei.multiblock.voltage_per_recipe", "Min. voltage depends on the recipe");
        r.addRawLang("reactingreactions.jei.multiblock.tower.size", "Min. 3x3x3, closed floor and roof");
        r.addRawLang("reactingreactions.jei.multiblock.tower.heat", "Blaze Burner directly beneath the floor");
        r.addRawLang("reactingreactions.jei.multiblock.heat_none", "No heat required by any recipe");
        r.addRawLang("reactingreactions.jei.multiblock.min_heat", "Min. heat: %s");
    }
}
