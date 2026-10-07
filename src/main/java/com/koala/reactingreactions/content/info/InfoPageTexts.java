package com.koala.reactingreactions.content.info;

import com.koala.reactingreactions.content.drill.DrillRates;

import java.util.List;

/**
 * The JEI information pages that remain: how Rich Veins spawn and work, and how each multiblock is built. Each page is a list of
 * short paragraphs; the lang keys are {@code reactingreactions.jei.page.<page id>.<n>} and the page is attached to every listed item.
 * (The single-block machines' pages are built from the live recipes, see {@link InfoPages}.)
 */
public final class InfoPageTexts {
    public record Page(String id, List<String> items, List<String> lines) {
        public String langKey(int index) {
            return "reactingreactions.jei.page." + id + "." + index;
        }
    }

    private InfoPageTexts() {
    }

    public static final List<Page> PAGES = List.of(
            new Page("rich_veins", List.of("rich_oil_vein", "rich_asurine_vein", "rich_crimsite_vein", "rich_ochrum_vein", "rich_veridium_vein",
                    "rich_scoria_vein", "rich_tuff_vein", "rich_granite_vein", "rich_diorite_vein"), List.of(
                    "Rich Veins generate inside natural veins, after everything else: about 9 in 10 veins of Create's rocks (asurine, crimsite, ochrum, veridium, scoria), 3 in 10 tuff, granite and diorite blobs, and every oil vein.",
                    "Each has a richness from 1 to 5, rolled when it generates. Level 4 and 5 drill at full speed.",
                    "A drill head touching one drills it: the oil head for oil, the steel head for tuff, scoria, granite and diorite, the diamond head for asurine, crimsite, ochrum and veridium. Plain rock or plain oil shale does nothing.",
                    "They glow faintly. Mining one is very slow and it survives explosions. It drops a stack of the plain rock, never itself, so it cannot be moved.",
                    "Operators can find the nearest one with /richvein.")),
            new Page("reaction_chamber", List.of("reaction_chamber_controller", "reaction_chamber_wall", "steel_encased_shaft"), List.of(
                    "A hollow, roofed box of Reaction Chamber walls, in two sizes: 3x3 and 4 tall, or 5x5 and 5 tall.",
                    "Place the controller in the middle of a side, one block above the floor. Once formed, it turns into a full machine.",
                    "A steel-encased shaft takes the middle of the roof. Its speed must fit the running recipe's RPM band.",
                    "Heated recipes need Blaze Burners or an Induction Heater under the floor, in an X shape.",
                    "Fluids and items go in and out through the walls.",
                    "The small one has one output tank and takes 4 attachments; the large one has two and takes 8.")),
            new Page("machine_attachments", List.of("outlet_manifold", "expansion_tank", "machine_gauge", "gasket", "circulation_pump", "outlet_valve"), List.of(
                    "For a formed Reaction Chamber, Electrolysis Vat or Airless Oven. Outlet Manifolds and Gaskets are used on the machine and go into it; sneak with an empty hand to take the last one out. The others mount against the outside of a wall.",
                    "Small sizes take 4, large ones 8; single-block machines take 1, a Gauge or an Outlet Valve. Any more do nothing. A Gasket takes no slot.",
                    "Outlet Manifold: one more output tank. Expansion Tank: +4000 mB in every tank.",
                    "Machine Gauge: a comparator signal of the progress, or of how full the fullest output is. A Wrench switches it.",
                    "Gasket: toxic contents no longer leak; it also seals anything else that can leak, from tanks to pipes. Circulation Pump: faster recipes, by half at 256 RPM.",
                    "Outlet Valve: lets a fluid out into a tank or machine in front of it, or along the pipes in front like a pump, for free. A filled container used on it picks the fluid, an empty hand cycles through the machine's, sneaking resets it to any output.",
                    "Encased pipes and Plastic Pipes never leak; glass pipes leak half as much as plain ones.")),
            new Page("electrolysis_vat", List.of("electrolysis_vat_controller", "electrolysis_vat_wall", "electrolysis_vat_terminal", "graphite_electrode",
                    "gold_steel_electrode", "lead_electrode"), List.of(
                    "A roofed box of Electrolysis Vat walls in two sizes: 5x3 and 3 tall, or 7x5 and 4 tall. Place the controller in the middle of a long side, one block above the floor.",
                    "Stand two electrode columns inside, from the floor to just under the roof, one next to each end wall on the middle line.",
                    "A terminal sits in each end wall, beside its column, one block above the floor. Attach the wires there.",
                    "The voltage across the electrodes must reach the recipe's minimum. Some recipes only accept certain electrodes.",
                    "Pipe in the fluid to split. The results leave through the walls.")),
            new Page("distillation_tower", List.of("distillation_tower_controller", "distillation_tower_wall"), List.of(
                    "A hollow tower of Distillation Tower walls, at least 3x3 and 3 tall. Place the controller anywhere on the shell.",
                    "Heat comes from Blaze Burners or an Induction Heater beneath it.",
                    "The floor row takes the input. The second row does nothing. From the third row up, each row is one output.",
                    "Taller towers give more outputs.")),
            new Page("airless_oven", List.of("airless_oven_controller", "airless_oven_wall"), List.of(
                    "A hollow 3x3x3 box of Airless Oven walls. Place the controller in the middle of a side, one block above the floor.",
                    "It heats itself: no burner needed.")),
            new Page("derrick", List.of("derrick_controller", "derrick_block", "derrick_truss", "drill_pipe", "oil_drill_head", "mineral_drill_head_steel", "mineral_drill_head_titanium",
                    "mineral_drill_head_diamond"), List.of(
                    "A 3x3 tower around a column of Drill Pipe: Derrick Blocks in the corners of three layers, Trusses on the sides of the top and bottom layers. Formed, it turns into a full derrick.",
                    "Feed rotation down into the controller from a shaft above it, at least 32 RPM, up to 128 RPM for full speed.",
                    "Run Drill Pipe from the frame down to a head touching a Rich Vein. The head decides the job.",
                    "The oil head sips lubricant (seed oil or mineral oil). The mineral heads sip coolant, 1% brine (used twice as fast) or drill grease (lasts four times longer). Feed either through the controller or any Derrick Block.",
                    String.format("Full-richness vein: oil %.0f mB/s at %.0f RPM up to %.0f mB/s at %.0f RPM; dust 1 per %.0f s at %.0f RPM down to 1 per %.1f s at %.0f RPM. A poorer vein is slower.",
                            DrillRates.oilMbPerSecond(32), 32.0, DrillRates.oilMbPerSecond(128), 128.0,
                            DrillRates.secondsPerItem(32), 32.0, DrillRates.secondsPerItem(128), 128.0),
                    String.format("The titanium head drills every vein, %.0fx as fast.", DrillRates.TITANIUM_SPEED),
                    "Fluids and items go through the controller and any Derrick Block, never the trusses.")),
            new Page("induction_heater", List.of("induction_heater_plate", "induction_heater_connector"), List.of(
                    "A filled rectangle of Induction Heater Plates on one level, at least 3x3 and at most 16x16, with exactly one Connector anywhere in it.",
                    "The connector holds both wire terminals. It points toward you when placed, in any of the six directions.",
                    "Resistance is 10 ohm per block. A bigger heater needs proportionally more voltage for the same heat, so the power per block stays the same.",
                    "It works as a Blaze Burner under machines. Heated coils hurt what stands on them.")));
}
