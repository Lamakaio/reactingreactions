package com.koala.reactingreactions.content.info;

import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.api.Formulas;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A description and (where it makes sense) a chemical formula for every item, block and fluid of this mod. The formulas of real
 * compounds are the real ones; the dusts and other made-up materials get a plausible mineral or compound picked from what they yield
 * in the recipes (an invented one is marked with "~"). This drives the JEI information pages, the item tooltips and the lang file.
 *
 * <p>Format of a row: {@code id | formula (may be empty) | description}. The id is the registry path in this mod's namespace; a
 * fluid's bucket and canister share the fluid's row.
 */
public final class CompoundInfo {
    public record Entry(String id, String formula, String description) {
        public boolean hasFormula() {
            return !formula.isEmpty();
        }

        public String langKey() {
            return "reactingreactions.info." + id;
        }

        /** The formula alone. A leading "~" in the table marks an invented formula; it is only a marker for us and is not shown. */
        public Component formulaLine() {
            boolean invented = formula.startsWith("~");
            return Component.literal(invented ? formula.substring(1) : formula);
        }
    }

    private static final String TABLE = """
            acetylene | C2H2 | Fuel gas. Explosive.
            aerozine | N2H4 + (CH3)2N2H2 | Rocket propellant. Toxic, explosive.
            ammonia | NH3 | Base of nitric acid, fertilizer and nylon.
            bleach | NaOCl | From brine. Cleans and whitens.
            butane | C4H10 | Light petroleum gas. Hydrocracks into ethane.
            carbon_dioxide | CO2 | Byproduct of reforming and fermentation.
            carbon_monoxide | CO | Poisonous gas from the Airless Oven.
            contaminated_hydrocarbon_gas | C1-C4 + H2S | Sour gas. Scrub with solvent.
            contaminated_methane | CH4 + H2S | Sour biogas. Scrub with solvent.
            coolant | C2H5OH + H2O (aq) | Cools the drills. Water and ethanol.
            crude_oil | CnH2n+2 (n = 5-40) | Pumped from Rich Oil Veins. Distill it.
            diesel | C12H26 | Heavy fuel.
            ethane | C2H6 | Light gas. Cracks into ethylene.
            ethanol | C2H5OH | From fermentation or ethane.
            ethylene | C2H4 | Plastic building block.
            helium | He | Lifting noble gas.
            hydrocarbon_gas | C1-C4 mix | Distills into light gases.
            hydrogen | H2 | A poor fuel, storing electrolysis' energy at a loss. Explosive.
            liquid_hdpe | (C2H4)n | Molten polyethylene.
            liquid_nylon | [NH(CH2)6NHCO(CH2)4CO]n | Molten nylon 6,6.
            liquid_polypropylene | (C3H6)n | Molten polypropylene.
            lithium_brine | LiCl (aq) | Yields lithium.
            lpg | C3H8 + C4H10 | Separates into noble gases.
            lye | NaOH | From brine electrolysis, or kelp ash and quicklime. Boosts scrubbers.
            magnesium_chloride | MgCl2 | Electrolysed into magnesium.
            methane | CH4 | Natural gas. Reforms into hydrogen.
            mineral_oil | CnH2n+2 (n = 15-40) | Lubricates the Derrick.
            drill_grease | ~CnH2n+2 + MgO | Mineral oil thickened with magma cream or wax. Drill coolant that lasts four times longer.
            naphtha | C5H12-C10H22 | Cracks into ethane and propane.
            compressed_air | N2 + O2 | Air from a spinning Create backtank. Distils into nitrogen and oxygen.
            neon | Ne | Noble gas.
            nitric_acid | HNO3 | Corrosive. Makes ammonium nitrate.
            nitrogen | N2 | Inert gas. Feeds ammonia.
            oxygen | O2 | Drives oxidation reactions.
            propane | C3H8 | Light petroleum gas. Cracks into propylene.
            propylene | C3H6 | Polypropylene building block.
            purified_water | H2O | Mineral-free water.
            resin | C20H30O2 | Plant resin. Base of varnish.
            seed_oil | C57H104O6 | Paint carrier. Lubricates the Derrick.
            solvent | C3H6O | Scrubs sour gas.
            steam | H2O | Cracks hydrocarbons.
            strong_brine | NaCl (aq, 26%) | Electrolysed into lye and bleach.
            sulfuric_acid | H2SO4 | Very corrosive.
            titanium_tetrachloride | TiCl4 | Reduced into titanium.
            weak_brine | NaCl (aq, 3.5%) | Evaporates into strong brine.
            white_vinegar | CH3COOH (aq) | Patinates copper.
            activated_carbon | C | Filter material.
            alumina_dust | Al2O3 | Dissolves in lye.
            aluminum_hydroxide | Al(OH)3 | Electrolysed into aluminium.
            aluminum_ingot | Al | Light metal.
            aluminum_nugget | Al | A ninth of an ingot.
            ammonium_nitrate | NH4NO3 | Fertilizer and explosive.
            asurine_dust | ~Zn4Si2O7(OH)2·H2O | Zinc ore.
            biomass | | Ferments into biogas.
            bleach_bottle | NaOCl | Cleans up leak pools.
            borax | Na2B4O7·10H2O | Glass flux.
            bromine | Br2 | Fire retardant. Toxic.
            calcium_carbide | CaC2 | Makes acetylene with water.
            coal_coke | C | Reducing agent: makes pig iron, the start of steel.
            crimsite_dust | ~FeTiO3 | Titanium ore.
            crushed_polymetallic_nodule | ~MnO2·(Ni,Cu,Co) | Refines into manganese and nickel.
            diorite_dust | ~CaAl2Si2O8 | For refractory bricks and grey paint.
            granite_dust | ~KAlSi3O8·SiO2 | Refines into alumina.
            hdpe_pellets | (C2H4)n | Nine make a sheet.
            hdpe_sheet | (C2H4)n | Tough plastic.
            iron_oxide | Fe2O3 | Red pigment.
            lead_ingot | Pb | Soft heavy metal. Toxic.
            lead_nugget | Pb | A ninth of an ingot.
            lithium_ingot | Li | Lightest metal.
            lithium_nugget | Li | A ninth of an ingot.
            magnesium | Mg | Burns bright.
            manganese | Mn | Hard metal for alloys.
            manganese_steel | Fe + Mn (12%) | Very tough steel.
            nickel_ingot | Ni | Corrosion-resistant metal.
            nickel_nugget | Ni | A ninth of an ingot. Catalyst for reforming.
            nylon_fiber | [NH(CH2)6NHCO(CH2)4CO]n | Strong synthetic fibre.
            ochrum_dust | ~Au2PbS2 | Gold and lead ore.
            pig_iron | Fe + C (4%) | Brittle iron.
            rare_earth_dust | (Ce,La)PO4 | Rare earth phosphate.
            salt | NaCl | Table salt.
            mineral_salt | NaCl + CaSO4 | Rock salt from crushed limestone. Mill it into salt.
            quicklime | CaO | Limestone burnt in the Airless Oven. For lye, carbide and steel flux.
            scoria_dust | ~PbS | Lead and sulfur ore.
            silica | SiO2 | Quartz. Plastic catalyst.
            silicon_board | Si | Circuit base.
            slag | CaSiO3·FeO·Al2O3 | Smelting waste.
            soap | C17H35COONa | Cleans pools. Clears bad effects.
            steel_ingot | Fe + C (<2%) | Hard iron alloy.
            steel_sheet | Fe + C | Pressed steel. Plating for machines.
            sulfur_dust | S8 | Makes acid and gunpowder.
            titanium | Ti | Strong light metal.
            titanium_dioxide | TiO2 | White pigment.
            titanium_sheet | Ti | Rolled titanium.
            tuff_dust | ~(Fe,Ni)9S8 | Nickel and iron ore.
            varnish | | Wood coating. Use on planks to varnish them, or on stripped wood to restore its bark.
            veridium_dust | ~CuFeS2 | Copper and iron ore.
            yeast | | Ferments sugar.
            incomplete_diamond | ~C+Ni | Carbon in a diamond-growth press cycle.
            netherite_alloy_dust | ~Ti+Fe+Ni+Mn | A powder-metallurgy superalloy blend.
            incomplete_netherite_ingot | ~Ti+Fe+Ni+Mn | Superalloy in a consolidation press cycle.
            beryllium_oxide | BeO | Emerald's beryllium source.
            chromium_dust | Cr2O3 | Colours synthetic emerald and ruby.
            ruby | Al2O3:Cr | Synthetic corundum. Never found, only made.
            white_paint | TiO2 in triglyceride | Works as white dye.
            red_paint | Fe2O3 in triglyceride | Works as red dye.
            orange_paint | dye in triglyceride | Works as orange dye.
            yellow_paint | dye in triglyceride | Works as yellow dye.
            lime_paint | dye in triglyceride | Works as lime dye.
            green_paint | dye in triglyceride | Works as green dye.
            cyan_paint | dye in triglyceride | Works as cyan dye.
            light_blue_paint | dye in triglyceride | Works as light blue dye.
            blue_paint | dye in triglyceride | Works as blue dye.
            purple_paint | dye in triglyceride | Works as purple dye.
            magenta_paint | dye in triglyceride | Works as magenta dye.
            pink_paint | dye in triglyceride | Works as pink dye.
            brown_paint | dye in triglyceride | Works as brown dye.
            gray_paint | dye in triglyceride | Works as gray dye.
            light_gray_paint | dye in triglyceride | Works as light gray dye.
            black_paint | dye in triglyceride | Works as black dye.
            super_bone_meal | Ca5(PO4)3OH + CH4N2O | Grows crops in an area.
            circuit_board | Si + Cu + Au | Finished circuit.
            incomplete_circuit_board | Si + Cu | Circuit in assembly.
            charcoal_tablet | C | Lowers toxicity a little.
            antidote | C + C6H12O6 | Lowers toxicity a lot.
            iodine | I2 | Leached from kelp.
            iodine_tablets | KI | Lowers toxicity. 10 uses.
            iodine_spray | I2 + C2H5OH | Cures poison and wither, on you or a creature. May cure a zombie villager. 10 uses.
            carbon_filter | C | Gas Mask filter. Wears out while used.
            gas_mask | | Filters toxic air with a Carbon Filter. Blocks poison and wither.
            chemical_gloves | | Safe handling of toxic items.
            chemical_boots | | Safe walking in leak pools.
            flame_retardant_cloak | | Blocks fire damage.
            chemical_flask | | A thrown flask of a toxic fluid. Spills 500 mB where it breaks.
            blast_flask | | A thrown flask of a flammable fluid, primed with gunpowder. Goes up where it breaks.
            acetylene_lamp | | Handheld lamp with an acetylene tank. Supercharge it to ignite nearby undead.
            composite_exo_helmet | | Night vision on nitrogen, and works as Engineer's and Aviator's Goggles. Unbreakable, counts for the titanium set bonus.
            composite_exo_chestplate | | Flight on aerozine. Unbreakable, counts for the titanium set bonus. The full set with an Oxygen Mask blocks toxicity and harmful effects.
            composite_exo_leggings | | Faster flying and swimming on hydrogen. Unbreakable, counts for the titanium set bonus.
            composite_exo_boots | | No fall damage and a full-block step, on mineral oil. Unbreakable, counts for the titanium set bonus.
            plasma_multitool | | Pickaxe, axe and shovel, with area mining on drill grease. Unbreakable.
            neon_blade | | A long, hard-hitting plasma blade on neon. Blocks projectiles. Unbreakable.
            rare_earth_magnet | NdFeB | A strong permanent magnet.
            composite_plating | | Titanium, netherite and ceramic armor plate.
            control_unit | | A rugged control computer.
            servo_actuator | | A magnetic servo motor.
            ruby_lens | Al2O3 | A polished ruby optic.
            aerozine_thrusters | | Back thrusters. Double jump.
            anchor_charm | | No knockback.
            digging_ring | | Faster mining.
            diving_fins | | Water breathing, dolphin's grace.
            helium_locket | | Slow falling.
            magnesium_knuckle | | Sets hit targets on fire.
            racing_anklet | | Speed while sprinting.
            spring_boots | | Jump boost.
            titanium_helmet | Ti | Light, strong armour. Each piece worn adds to a set bonus: Speed, then Regeneration, then Resistance.
            titanium_chestplate | Ti | Light, strong armour. Each piece worn adds to a set bonus: Speed, then Regeneration, then Resistance.
            titanium_leggings | Ti | Light, strong armour. Each piece worn adds to a set bonus: Speed, then Regeneration, then Resistance.
            titanium_boots | Ti | Light, strong armour. Each piece worn adds to a set bonus: Speed, then Regeneration, then Resistance.
            titanium_sword | Ti | Durable tool.
            titanium_pickaxe | Ti | Durable tool.
            titanium_axe | Ti | Durable tool.
            titanium_shovel | Ti | Durable tool.
            titanium_hoe | Ti | Durable tool.
            titanium_bow | Ti | Nylon-strung bow.
            airless_oven_controller | | Controls a 3x3x3 Airless Oven. Makes coke and charcoal.
            airless_oven_wall | | Airless Oven wall.
            atmospheric_scrubber | | Needs rotation and Activated Carbon. Clears contamination, stops gas vents. Stronger in a room.
            derrick_block | | Derrick frame. Passes fluids and items to the controller.
            derrick_controller | | Top of a Derrick or Mineral Drill. The head decides the job.
            derrick_truss | | Derrick bracing.
            distillation_tower_controller | | Controls a Distillation Tower.
            distillation_tower_wall | | Distillation Tower wall.
            drill_pipe | | Stack from the Derrick down to a vein.
            electrolysis_vat_controller | | Controls an Electrolysis Vat.
            electrolysis_vat_terminal | | Goes in an Electrolysis Vat's end wall, beside an electrode. Wire attaches here.
            electrolysis_vat_wall | | Electrolysis Vat wall.
            fermentation_barrel | | Slow single-block fermenter.
            floor_drain | | Absorbs nearby leak pools into a tank.
            gas_vent | | Releases gases into the air: piped ones, or a tank's over a bucket under full when placed on it. Toxic ones pollute, flammable ones burn off near a flame.
            gas_diffuser | | Fills envelopes with lifting gas.
            gold_steel_electrode | Au + Fe | Gold-plated steel electrode.
            graphite_electrode | C | Graphite electrode.
            lead_electrode | Pb | Electrowins zinc and copper from acid.
            mineral_drill_head_steel | | Drills tuff, scoria, granite and diorite veins. Needs coolant.
            mineral_drill_head_titanium | Ti | Drills every rock vein, twice as fast. Needs coolant.
            mineral_drill_head_diamond | C | Drills asurine, crimsite, ochrum and veridium veins. Needs coolant.
            oil_drill_head | | Pumps from a Rich Oil Vein. Needs lubricant.
            anfo_charge | NH4NO3 + CnH2n+2 | Mining charge. Redstone sets it off, fire does not. Breaks blocks, spares creatures.
            oxygen_mask | | Breathes from a worn Create backtank: water breathing, no toxic air.
            charging_pad | | Plate that fills the tank items and backtanks of whoever stands on it.
            refractory_brick | Al2O3 + SiO2 | Fired fireclay. Lines the Airless Oven.
            incomplete_lead_acid_battery | Pb + H2SO4 | Half-built lead-acid battery.
            incomplete_lithium_battery | Li + C | Half-built lithium battery.
            laser_pointer | Al2O3:Cr | Ruby laser. Right-click to measure distance.
            purger | | Shift-right-click a block to empty it. Items drop, fluids and gases are destroyed.
            neon_lamp | Ne | Glowing neon tube.
            steam_turbine | | Turns steam into rotation.
            oil_shale | ~(C10H16O)n + CaCO3 | Kerogen-rich rock.
            polymetallic_nodule | ~MnO2·(Ni,Cu,Co) | Sea-floor metal nodules.
            reaction_chamber_controller | | Controls a Reaction Chamber.
            reaction_chamber_wall | | Reaction Chamber wall.
            plastic_pipe | (C2H4)n | A fluid pipe of HDPE: it never leaks.
            outlet_manifold | | Use on a formed Reaction Chamber, Electrolysis Vat or Airless Oven: one more output tank, and copper pipes on it.
            expansion_tank | | Mount on a formed Reaction Chamber, Electrolysis Vat or Airless Oven: +4000 mB in every tank.
            machine_gauge | | Mount on a machine of this mod: comparator signal of progress, or of output fill (wrench to switch).
            small_reaction_chamber | | A two-block Reaction Chamber: two fluids in, one out, at half speed. Takes a Gauge or an Outlet Valve.
            gasket | | Use on a formed machine, or anything else that can leak (tanks, basins, pipes, pumps): it no longer leaks. Sneak with an empty hand to take it out.
            outlet_valve | | Mount on a machine of this mod: pushes a chosen fluid out into what is in front of it, or along pipes, for free.
            circulation_pump | | Mount on a formed Reaction Chamber, Electrolysis Vat or Airless Oven and turn it: up to 50% faster recipes at 256 RPM.
            induction_heater_plate | Cu + Fe | A plate of an Induction Heater. Fill a rectangle, at least 3x3, with one connector.
            induction_heater_connector | Cu + Fe | Controls an Induction Heater and holds both wire terminals. Heat rises with voltage.
            reinforced_glass | SiO2·B2O3 | Blast-resistant glass.
            rich_asurine_vein | ~Zn4Si2O7(OH)2·H2O | Rare rich deposit. Titanium head drills it.
            rich_crimsite_vein | ~FeTiO3 | Rare rich deposit. Titanium head drills it.
            rich_ochrum_vein | ~Au2PbS2 | Rare rich deposit. Titanium head drills it.
            rich_veridium_vein | ~CuFeS2 | Rare rich deposit. Titanium head drills it.
            rich_scoria_vein | ~PbS | Rare rich deposit. Steel head drills it.
            rich_tuff_vein | ~(Fe,Ni)9S8 | Rare rich deposit. Steel head drills it.
            rich_granite_vein | ~KAlSi3O8·SiO2 | Rare rich deposit. Steel head drills it.
            rich_diorite_vein | ~CaAl2Si2O8 | Rare rich deposit. Steel head drills it.
            rich_oil_vein | ~(C10H16O)n | Rare rich deposit. Oil head pumps it.
            small_electrolyser | | Simple electrolyser. No structure.
            steel_casing | Fe + C | Frame of steel machines.
            steel_encased_shaft | Fe + C | Drives the Reaction Chamber stirrer.
            varnished_planks | | Airtight wood.
            """;

    /**
     * Formulas only (no descriptions) for compounds from other mods and vanilla, keyed by full id, or by an item tag with a
     * leading "#". A fluid's bucket shares the fluid's row. Other mods' own tooltips are otherwise left alone. Raw biological
     * materials (wood, honey, bone) get none. Redstone and glowstone borrow real rare-earth phosphors: the red of old TV
     * screens (Y2O3:Eu) and the modern glow-in-the-dark pigment (SrAl2O4:Eu,Dy).
     */
    private static final String FOREIGN = """
            minecraft:water | H2O
            minecraft:ice | H2O
            minecraft:packed_ice | H2O
            minecraft:blue_ice | H2O
            minecraft:snow_block | H2O
            minecraft:snowball | H2O
            minecraft:lava | ~SiO2 + FeO + MgO
            minecraft:iron_ingot | Fe
            minecraft:iron_nugget | Fe
            minecraft:iron_block | Fe
            minecraft:raw_iron | Fe2O3
            minecraft:iron_ore | Fe2O3
            minecraft:deepslate_iron_ore | Fe2O3
            minecraft:gold_ingot | Au
            minecraft:gold_nugget | Au
            minecraft:gold_block | Au
            minecraft:raw_gold | Au
            minecraft:copper_ingot | Cu
            minecraft:copper_block | Cu
            minecraft:raw_copper | CuFeS2
            minecraft:copper_ore | CuFeS2
            minecraft:exposed_copper | Cu + Cu2O
            minecraft:weathered_copper | Cu2O
            minecraft:oxidized_copper | Cu2CO3(OH)2
            minecraft:coal | C
            minecraft:charcoal | C
            minecraft:coal_block | C
            minecraft:diamond | C
            minecraft:diamond_block | C
            minecraft:emerald | Be3Al2Si6O18
            minecraft:lapis_lazuli | ~Na8Al6Si6O24S2
            minecraft:quartz | SiO2
            minecraft:amethyst_shard | SiO2
            minecraft:glass | SiO2
            minecraft:sand | SiO2
            minecraft:red_sand | SiO2 + Fe2O3
            minecraft:gravel | ~SiO2
            minecraft:flint | SiO2
            minecraft:obsidian | ~SiO2
            minecraft:clay_ball | Al2Si2O5(OH)4
            minecraft:clay | Al2Si2O5(OH)4
            minecraft:brick | ~Al2O3 + SiO2
            minecraft:calcite | CaCO3
            minecraft:dripstone_block | CaCO3
            minecraft:pointed_dripstone | CaCO3
            minecraft:bone_meal | Ca5(PO4)3OH
            minecraft:redstone | ~Y2O3:Eu
            minecraft:redstone_block | ~Y2O3:Eu
            minecraft:glowstone_dust | ~SrAl2O4:Eu,Dy
            minecraft:glowstone | ~SrAl2O4:Eu,Dy
            minecraft:sugar | C12H22O11
            minecraft:gunpowder | KNO3 + S + C
            minecraft:tnt | C7H5N3O6
            create:zinc_ingot | Zn
            create:zinc_nugget | Zn
            create:zinc_block | Zn
            create:raw_zinc | ZnS
            create:zinc_ore | ZnS
            create:deepslate_zinc_ore | ZnS
            create:crushed_raw_zinc | ZnS
            create:crushed_raw_iron | Fe2O3
            create:crushed_raw_gold | Au
            create:crushed_raw_copper | CuFeS2
            create:crushed_raw_aluminum | Al(OH)3
            create:crushed_raw_lead | PbS
            create:crushed_raw_nickel | (Fe,Ni)9S8
            create:crushed_raw_osmium | Os
            create:crushed_raw_platinum | Pt
            create:crushed_raw_quicksilver | HgS
            create:crushed_raw_silver | Ag2S
            create:crushed_raw_tin | SnO2
            create:crushed_raw_uranium | UO2
            create:brass_ingot | CuZn
            create:brass_nugget | CuZn
            create:brass_sheet | CuZn
            create:brass_block | CuZn
            create:copper_nugget | Cu
            create:copper_sheet | Cu
            create:iron_sheet | Fe
            create:golden_sheet | Au
            create:industrial_iron_block | Fe
            create:limestone | CaCO3
            create:asurine | ~Zn4Si2O7(OH)2·H2O
            create:crimsite | ~FeTiO3
            create:ochrum | ~Au2PbS2
            create:veridium | ~CuFeS2
            create:scoria | ~PbS
            create:rose_quartz | SiO2
            create:polished_rose_quartz | SiO2
            create:powdered_obsidian | ~SiO2
            createdieselgenerators:diesel | C12H26
            createdieselgenerators:gasoline | C8H18
            createdieselgenerators:ethanol | C2H5OH
            createdieselgenerators:plant_oil | C57H104O6
            createdieselgenerators:biodiesel | C19H36O2
            createdieselgenerators:crude_oil | CnH2n+2 (n = 5-40)
            electroenergetics:copper_wire | Cu
            electroenergetics:iron_wire | Fe
            electroenergetics:electrum_wire | AuAg
            electroenergetics:plant_oil | C57H104O6
            electroenergetics:transformer_oil | CnH2n+2 (n = 15-40)
            """;

    private static final Map<String, Entry> FOREIGN_ENTRIES = new LinkedHashMap<>();
    private static final Map<TagKey<Item>, Entry> FOREIGN_TAGS = new LinkedHashMap<>();

    private static final Map<String, Entry> ENTRIES = new LinkedHashMap<>();

    static {
        for (String line : TABLE.strip().split("\\n")) {
            String[] parts = line.strip().split("\\|", 3);
            String id = parts[0].strip();
            ENTRIES.put(id, new Entry(id, parts[1].strip(), parts[2].strip()));
        }
        for (String line : FOREIGN.strip().split("\\n")) {
            String[] parts = line.strip().split("\\|", 2);
            String id = parts[0].strip();
            Entry entry = new Entry(id, parts[1].strip(), "");
            if (id.startsWith("#")) {
                FOREIGN_TAGS.put(TagKey.create(Registries.ITEM,
                        ResourceLocation.parse(id.substring(1))), entry);
            } else {
                FOREIGN_ENTRIES.put(id, entry);
            }
        }
    }

    private CompoundInfo() {
    }

    public static Map<String, Entry> all() {
        return ENTRIES;
    }

    public static Entry get(String id) {
        return ENTRIES.get(id);
    }

    /** The row for any item, block or fluid id: this mod's own table, or the formula-only one for vanilla and other mods. */
    public static Entry find(ResourceLocation id) {
        Entry own = ReactingReactions.MODID.equals(id.getNamespace()) ? ENTRIES.get(id.getPath()) : FOREIGN_ENTRIES.get(id.toString());
        // A formula set from a script (see Formulas) wins; an empty one hides the line.
        String scripted = Formulas.byId(id.toString());
        if (scripted != null) {
            return new Entry(id.toString(), scripted, own == null ? "" : own.description());
        }
        return own;
    }

    /** The row for an item: by id, then as a fluid's bucket (x_bucket shares fluid x's row), then by item tag. */
    public static Entry findForItem(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        Entry entry = find(id);
        if (entry == null && id.getPath().endsWith("_bucket")) {
            entry = find(id.withPath(id.getPath().substring(0, id.getPath().length() - "_bucket".length())));
        }
        if (entry == null) {
            for (var tag : Formulas.byTag().entrySet()) {
                if (stack.is(TagKey.create(Registries.ITEM, ResourceLocation.parse(tag.getKey())))) {
                    return new Entry("#" + tag.getKey(), tag.getValue(), "");
                }
            }
            for (var tag : FOREIGN_TAGS.entrySet()) {
                if (stack.is(tag.getKey())) {
                    return tag.getValue();
                }
            }
        }
        return entry;
    }
}
