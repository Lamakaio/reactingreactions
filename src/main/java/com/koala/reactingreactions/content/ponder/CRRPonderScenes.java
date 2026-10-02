package com.koala.reactingreactions.content.ponder;

import org.jetbrains.annotations.Nullable;
import com.koala.reactingreactions.content.compat.DieselGeneratorsCompat;
import com.koala.reactingreactions.content.drill.DerrickControllerBlockEntity;
import com.koala.reactingreactions.content.drill.DrillRig;
import com.koala.reactingreactions.content.multiblock.MachineTiers;
import com.koala.reactingreactions.content.multiblock.MultiblockControllerBlockEntity;
import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.content.fluids.particle.FluidParticleData;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.content.toxic.LeakPoolEntity;
import com.koala.reactingreactions.content.toxic.ToxicDefaults;
import com.koala.reactingreactions.registry.CRREntities;
import com.koala.reactingreactions.registry.CRRItems;
import com.koala.reactingreactions.registry.CRRParticles;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.EntityElement;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.api.scene.PonderStoryBoard;
import net.createmod.ponder.api.scene.Selection;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Ponder scenes, each built on a schematic under {@code assets/reactingreactions/ponder/}. Those are written by
 * {@link PonderSchematicExporter} from the builds in {@link PonderSchematics}, whose coordinates the scenes here point at.
 */
public class CRRPonderScenes {
    /** The Ponder tags: the icons at the top left of a scene, each opening a list of every item it covers. */
    private enum Category {
        DRILLING("drilling", "derrick_controller", "Drilling", "Rigs that pump oil and drill Rich Veins for ore dust"),
        MACHINES("chemical_machines", "reaction_chamber_controller", "Chemical Machines", "The machines that react, split, distil and bake"),
        CHEMISTRY("chemistry", "lithium_ingot", "Chemistry", "From raw materials to metals, fuels and plastics"),
        TOXIC("toxic_compounds", "gas_mask", "Toxic Compounds", "Leaks, contamination, and how to stay safe"),
        EQUIPMENT("equipment", "acetylene_lamp", "Equipment", "Gear and blocks that run on this mod's fluids");

        final ResourceLocation id;
        final String icon;
        final String title;
        final String description;

        Category(String path, String icon, String title, String description) {
            this.id = ReactingReactions.asResource(path);
            this.icon = icon;
            this.title = title;
            this.description = description;
        }
    }

    /**
     * One scene. {@code dieselGenerators} picks the variant for whether Create Diesel Generators is installed (null: always). Components
     * are item paths in this mod, or full ids for other mods' items.
     */
    private record Entry(String name, Category category, List<String> components, PonderStoryBoard board, @Nullable Boolean dieselGenerators) {
        Entry(String name, Category category, List<String> components, PonderStoryBoard board) {
            this(name, category, components, board, null);
        }

        /** Datagen registers every variant, so all their text gets written. */
        boolean applies() {
            return dieselGenerators == null || dieselGenerators == DieselGeneratorsCompat.isLoaded() || DatagenModLoader.isRunningDataGen();
        }
    }

    // Components are bare item paths: helper.asLocation always prefixes our own namespace.
    private static final List<Entry> SCENES = List.of(
            new Entry("derrick", Category.DRILLING, List.of("derrick_controller", "derrick_block", "derrick_truss", "drill_pipe"), CRRPonderScenes::derrick),
            new Entry("oil_drill", Category.DRILLING, List.of("oil_drill_head", "rich_oil_vein"), CRRPonderScenes::oilDrill),
            new Entry("mineral_drill", Category.DRILLING, List.of("mineral_drill_head_steel", "mineral_drill_head_titanium", "mineral_drill_head_diamond"),
                    CRRPonderScenes::mineralDrill),
            new Entry("rich_veins", Category.DRILLING, List.of("rich_oil_vein", "rich_asurine_vein", "rich_crimsite_vein", "rich_ochrum_vein",
                    "rich_veridium_vein", "rich_scoria_vein", "rich_tuff_vein", "rich_granite_vein", "rich_diorite_vein"), CRRPonderScenes::richVeins),
            new Entry("reaction_chamber", Category.MACHINES, List.of("reaction_chamber_controller", "reaction_chamber_wall", "steel_encased_shaft"),
                    CRRPonderScenes::reactionChamber),
            new Entry("electrolysis_vat", Category.MACHINES, List.of("electrolysis_vat_controller", "electrolysis_vat_wall", "electrolysis_vat_terminal"),
                    CRRPonderScenes::electrolysisVat),
            new Entry("distillation_tower", Category.MACHINES, List.of("distillation_tower_controller", "distillation_tower_wall"),
                    CRRPonderScenes::distillationTower, false),
            new Entry("distillation_tower_cdg", Category.MACHINES, List.of("createdieselgenerators:distillation_controller",
                    "createdieselgenerators:crude_oil_bucket"), CRRPonderScenes::dieselGeneratorsTower, true),
            new Entry("airless_oven", Category.MACHINES, List.of("airless_oven_controller", "airless_oven_wall"), CRRPonderScenes::airlessOven),
            new Entry("machine_attachments", Category.MACHINES, List.of("outlet_manifold", "expansion_tank", "machine_gauge", "gasket", "circulation_pump"),
                    CRRPonderScenes::machineAttachments),
            new Entry("small_machines", Category.MACHINES, List.of("small_electrolyser", "fermentation_barrel"), CRRPonderScenes::smallMachines),
            new Entry("ore_processing", Category.CHEMISTRY, List.of("asurine_dust", "veridium_dust", "tuff_dust", "scoria_dust"),
                    CRRPonderScenes::oreProcessing),
            new Entry("ore_dusts", Category.CHEMISTRY, List.of("ochrum_dust", "crimsite_dust", "granite_dust", "veridium_dust"),
                    CRRPonderScenes::oreDusts),
            // Cracking then plastics: together, the way from naphtha to a sheet.
            new Entry("petrochemistry", Category.CHEMISTRY, List.of("naphtha_bucket", "steam_bucket", "ethylene_bucket", "propylene_bucket"),
                    CRRPonderScenes::petrochemistry),
            new Entry("plastics", Category.CHEMISTRY, List.of("naphtha_bucket", "ethylene_bucket", "propylene_bucket", "liquid_hdpe_bucket",
                    "liquid_polypropylene_bucket", "hdpe_pellets", "hdpe_sheet"), CRRPonderScenes::plastics),
            new Entry("lithium_brine", Category.CHEMISTRY, List.of("weak_brine_bucket", "strong_brine_bucket", "lithium_brine_bucket", "lithium_nugget", "borax"),
                    CRRPonderScenes::lithiumBrine, false),
            new Entry("lithium_brine_cdg", Category.CHEMISTRY, List.of("weak_brine_bucket", "strong_brine_bucket", "lithium_brine_bucket",
                    "lithium_nugget", "borax"), CRRPonderScenes::lithiumBrineDieselGenerators, true),
            new Entry("titanium", Category.CHEMISTRY, List.of("titanium", "titanium_tetrachloride_bucket", "magnesium", "titanium_dioxide", "crimsite_dust"),
                    CRRPonderScenes::titanium),
            new Entry("polymetallic_nodule", Category.CHEMISTRY, List.of("polymetallic_nodule", "crushed_polymetallic_nodule", "manganese"),
                    CRRPonderScenes::polymetallicNodule),
            // Leaks first, then what to do about them: the drain and the vent show both, one after the other.
            new Entry("leaks", Category.TOXIC, List.of("floor_drain", "gas_vent"), CRRPonderScenes::leaks),
            new Entry("staying_safe", Category.TOXIC, List.of("floor_drain", "gas_vent", "gas_mask", "chemical_gloves", "chemical_boots", "carbon_filter",
                    "charcoal_tablet", "antidote", "atmospheric_scrubber"), CRRPonderScenes::stayingSafe),
            new Entry("acetylene_lamp", Category.EQUIPMENT, List.of("acetylene_lamp"), CRRPonderScenes::acetyleneLamp),
            new Entry("gas_diffuser", Category.EQUIPMENT, List.of("gas_diffuser", "helium_bucket", "hydrogen_bucket"), CRRPonderScenes::gasDiffuser));

    /** The components that exist as items: a fluid may have no bucket. */
    private static List<ResourceLocation> items(Entry entry) {
        return entry.components().stream().map(id -> id.contains(":") ? ResourceLocation.parse(id) : ReactingReactions.asResource(id))
                .filter(BuiltInRegistries.ITEM::containsKey).toList();
    }

    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        for (Entry entry : SCENES) {
            // The Gas Diffuser only exists with Aeronautics.
            if (entry.applies() && !items(entry).isEmpty()) {
                helper.forComponents(items(entry).toArray(ResourceLocation[]::new)).addStoryBoard(entry.name(), entry.board());
            }
        }
    }

    /** Every item with a scene goes in its scene's category. */
    public static void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        for (Category category : Category.values()) {
            helper.registerTag(category.id).addToIndex()
                    .item(BuiltInRegistries.ITEM.get(ReactingReactions.asResource(category.icon)), true, false)
                    .title(category.title).description(category.description).register();
        }
        Map<ResourceLocation, Set<Category>> tagsOf = new LinkedHashMap<>();
        for (Entry entry : SCENES) {
            if (!entry.applies()) {
                continue;
            }
            for (ResourceLocation component : items(entry)) {
                tagsOf.computeIfAbsent(component, id -> new LinkedHashSet<>()).add(entry.category());
            }
        }
        tagsOf.forEach((component, categories) -> categories.forEach(category -> helper.addTagToComponent(component, category.id)));
    }

    /** The scene being built, with shorthand for the steps every scene here uses. */
    private record Story(CreateSceneBuilder scene, SceneBuildingUtil util) {
        static Story start(net.createmod.ponder.api.scene.SceneBuilder builder, SceneBuildingUtil util, String name, String title, int plate) {
            CreateSceneBuilder scene = new CreateSceneBuilder(builder);
            scene.title(name, title);
            scene.configureBasePlate(0, 0, plate);
            scene.showBasePlate();
            scene.idle(10);
            return new Story(scene, util);
        }

        Selection box(int x1, int y1, int z1, int x2, int y2, int z2) {
            return util.select().fromTo(x1, y1, z1, x2, y2, z2);
        }

        Selection at(int x, int y, int z) {
            return util.select().position(x, y, z);
        }

        void show(Selection selection) {
            scene.world().showSection(selection, Direction.DOWN);
            scene.idle(15);
        }

        /** A keyframed text beat pointing at a spot, held long enough to read. */
        void say(String text, Vec3 target) {
            scene.overlay().showText(Math.max(70, 30 + text.length() * 2)).text(text).attachKeyFrame().pointAt(target).placeNearTarget();
            scene.idle(Math.max(80, 40 + text.length() * 2));
        }

        Vec3 face(int x, int y, int z, Direction side) {
            return util.vector().blockSurface(new BlockPos(x, y, z), side);
        }

        Vec3 top(int x, int y, int z) {
            return util.vector().topOf(x, y, z);
        }

        void spin(Selection selection, float rpm) {
            scene.world().setKineticSpeed(selection, rpm);
        }

        /** Once a fixed-size machine (or a Derrick) is complete: it turns into the full machine, with a flash. */
        void assemble(int x, int y, int z) {
            BlockPos controller = new BlockPos(x, y, z);
            scene.addInstruction(ponder -> {
                Level world = ponder.getWorld();
                if (world.getBlockEntity(controller) instanceof MultiblockControllerBlockEntity<?> machine) {
                    machine.showFormed(world);
                } else if (world.getBlockEntity(controller) instanceof DerrickControllerBlockEntity) {
                    DrillRig.setLook(world, controller, true);
                }
                ponder.forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw);
            });
            scene.effects().indicateSuccess(controller);
            scene.idle(20);
        }

        /** Uses an upgrade item on the formed machine at a wall, and the machine shows it ({@code look} set on every block). */
        void upgrade(int x, int y, int z, Item item, BooleanProperty look) {
            BlockPos wall = new BlockPos(x, y, z);
            scene.overlay().showControls(util.vector().topOf(wall), Pointing.DOWN, 40).rightClick().withItem(new ItemStack(item));
            scene.idle(20);
            scene.world().modifyBlocks(util.select().everywhere(), state -> state.hasProperty(look) && state.getValue(MachineTiers.PART) > 0
                    ? state.setValue(look, true) : state, false);
            scene.effects().indicateSuccess(wall);
            scene.idle(20);
        }

        void heat(Selection burners, BlazeBurnerBlock.HeatLevel heat) {
            for (BlockPos pos : burners) {
                scene.world().modifyBlock(pos, state -> state.hasProperty(BlazeBurnerBlock.HEAT_LEVEL)
                        ? state.setValue(BlazeBurnerBlock.HEAT_LEVEL, heat) : state, false);
            }
        }
    }

    // ---- drilling ----------------------------------------------------------------------------------------------------

    /** The rig of {@code PonderSchematics.rig} above its ground (y 1-3): tower layers at y 4-6, controller and motor at y 7. */
    private static void showRig(Story s) {
        s.show(s.box(0, 4, 0, 6, 4, 6));
        s.show(s.box(0, 5, 0, 6, 5, 6));
        s.show(s.box(0, 6, 0, 6, 6, 6));
        s.show(s.box(3, 7, 3, 3, 8, 3));
        s.assemble(3, 7, 3);
    }

    /** Cuts the front of the ground away to show the pipe and head below the rig. */
    private static void revealPipe(Story s) {
        s.scene().world().hideSection(s.box(0, 0, 0, 6, 3, 2), Direction.NORTH);
        s.scene().idle(20);
    }

    private static Selection rigKinetics(Story s) {
        return s.box(3, 7, 3, 3, 8, 3);
    }

    public static void derrick(net.createmod.ponder.api.scene.SceneBuilder builder, SceneBuildingUtil util) {
        Story s = Story.start(builder, util, "derrick", "The Derrick", 7);
        s.show(s.box(0, 1, 0, 6, 3, 6));
        s.show(s.box(0, 4, 0, 6, 4, 6));
        s.say("Derricks are 3x3x3 towers around a column of Drill Pipe: Derrick Blocks in the corners, Trusses on the sides of the top and bottom layers", s.top(3, 4, 3));
        s.show(s.box(0, 5, 0, 6, 5, 6));
        s.show(s.box(0, 6, 0, 6, 6, 6));
        s.say("Fluids and items can be piped in and out through any Derrick Block",
                s.face(2, 4, 2, Direction.NORTH));
        s.show(rigKinetics(s));
        s.assemble(3, 7, 3);
        s.spin(rigKinetics(s), 32);
        s.say("The Derrick Controller sits on top, and needs at least 32 RPM from a shaft coming down into it", s.face(3, 7, 3, Direction.NORTH));
        revealPipe(s);
        s.say("Drill Pipe continues down into the ground, ending in a Drill Head next to a Rich Vein", s.face(3, 2, 3, Direction.NORTH));
        s.say("The type of head decides what the rig produces", s.face(3, 1, 3, Direction.NORTH));
        s.spin(rigKinetics(s), 128);
        s.say("Faster rotation, up to 128 RPM, and richer veins speed up the rig. The vein is never used up", s.face(3, 0, 3, Direction.NORTH));
    }

    public static void oilDrill(net.createmod.ponder.api.scene.SceneBuilder builder, SceneBuildingUtil util) {
        Story s = Story.start(builder, util, "oil_drill", "Pumping Oil", 7);
        s.show(s.box(0, 1, 0, 6, 3, 6));
        revealPipe(s);
        s.say("Rich Oil Veins can be found deep inside veins of Oil Shale", s.face(3, 0, 3, Direction.NORTH));
        s.say("An Oil Drill Head next to the vein will pump Crude Oil", s.face(3, 1, 3, Direction.NORTH));
        showRig(s);
        s.spin(rigKinetics(s), 64);
        s.say("The rig needs a supply of lubricant, such as Seed Oil or Mineral Oil", s.face(2, 4, 2, Direction.NORTH));
        s.say("Crude Oil can then be extracted from any Derrick Block", s.face(3, 7, 3, Direction.NORTH));
    }

    public static void mineralDrill(net.createmod.ponder.api.scene.SceneBuilder builder, SceneBuildingUtil util) {
        Story s = Story.start(builder, util, "mineral_drill", "Drilling Rock", 7);
        s.show(s.box(0, 1, 0, 6, 3, 6));
        showRig(s);
        s.spin(rigKinetics(s), 64);
        revealPipe(s);
        s.say("Mineral Drill Heads turn Rich Veins of stone into dust", s.face(3, 1, 3, Direction.NORTH));
        s.say("Steel heads can drill Tuff, Scoria, Granite and Diorite", s.face(3, 1, 3, Direction.NORTH));
        s.say("Titanium heads are needed for Asurine, Crimsite, Ochrum and Veridium", s.face(3, 0, 3, Direction.NORTH));
        s.say("Mineral drilling consumes Coolant, supplied through any Derrick Block", s.face(2, 4, 2, Direction.NORTH));
        s.say("The dust can be extracted from any Derrick Block", s.face(3, 7, 3, Direction.NORTH));
    }

    public static void richVeins(net.createmod.ponder.api.scene.SceneBuilder builder, SceneBuildingUtil util) {
        Story s = Story.start(builder, util, "rich_veins", "Rich Veins", 5);
        s.show(s.box(1, 1, 1, 3, 1, 3));
        s.say("Rich Veins are rare blocks hidden inside natural deposits of stone and oil shale", s.top(2, 1, 2));
        s.say("Each has a richness from 1 to 5. Richer veins can be drilled faster", s.top(1, 1, 1));
        s.say("They can be recognised by their faint golden glow", s.top(3, 1, 1));
        s.say("Breaking one only drops the plain stone, so drill it instead", s.top(3, 1, 3));
    }

    // ---- machines ----------------------------------------------------------------------------------------------------

    /** The chamber of {@code PonderSchematics.reactionChamber} with its corner at (x, 2, z). */
    private static void buildReactionChamber(Story s, int x, int z, BlazeBurnerBlock.HeatLevel heat, boolean narrate) {
        int roof = 5;
        Selection burners = s.box(x, 1, z, x + 2, 1, z + 2);
        s.show(burners);
        s.show(s.box(x, 2, z, x + 2, roof, z + 2));
        if (narrate) {
            s.say("Reaction Chambers are hollow boxes of Chamber Walls in two sizes: 3x3 and 4 tall, or 5x5 and 5 tall", s.face(x, 3, z + 1, Direction.WEST));
            s.say("The Controller goes in the middle of a side, one block above the floor", s.face(x + 1, 3, z, Direction.NORTH));
        }
        Selection shaft = s.box(x + 1, roof, z + 1, x + 1, roof + 2, z + 1);
        s.show(s.box(x + 1, roof + 1, z + 1, x + 1, roof + 2, z + 1));
        s.assemble(x + 1, 3, z);
        s.spin(shaft, 64);
        if (narrate) {
            s.say("A Steel Encased Shaft in the centre of the roof stirs the contents. Each recipe requires a certain speed", s.top(x + 1, roof, z + 1));
        }
        s.heat(burners, heat);
        s.scene().idle(10);
    }

    public static void reactionChamber(net.createmod.ponder.api.scene.SceneBuilder builder, SceneBuildingUtil util) {
        Story s = Story.start(builder, util, "reaction_chamber", "The Reaction Chamber", 5);
        buildReactionChamber(s, 1, 1, BlazeBurnerBlock.HeatLevel.KINDLED, true);
        s.say("Some recipes require heat from Blaze Burners, placed under the floor in an X pattern", s.face(1, 1, 1, Direction.NORTH));
        s.say("Fluids and items can be inserted into and extracted from any wall", s.face(1, 3, 2, Direction.WEST));
    }

    public static void electrolysisVat(net.createmod.ponder.api.scene.SceneBuilder builder, SceneBuildingUtil util) {
        Story s = Story.start(builder, util, "electrolysis_vat", "The Electrolysis Vat", 7);
        Selection electrodes = s.box(2, 2, 3, 4, 2, 3);
        s.show(s.box(1, 1, 2, 5, 1, 4));
        s.say("Electrolysis Vats are closed boxes in two sizes: 5 by 3 and 3 tall, or 7 by 5 and 4 tall", s.top(3, 1, 3));
        s.show(electrodes);
        s.say("Two columns of Electrodes go inside, from the floor up to the roof, one next to each end", s.face(2, 2, 3, Direction.WEST));
        s.show(s.box(1, 2, 2, 5, 2, 4).substract(electrodes));
        s.say("The Controller goes in the middle of a long side, one block above the floor", s.face(3, 2, 2, Direction.NORTH));
        s.say("A Terminal in each end wall, beside its column, is where the power supply's wires attach", s.face(1, 2, 3, Direction.WEST));
        s.show(s.box(1, 3, 2, 5, 3, 4));
        s.assemble(3, 2, 2);
        s.say("Each recipe requires a minimum voltage", s.face(5, 2, 3, Direction.EAST));
        s.say("Fluids and items can be inserted into and extracted from any wall", s.face(1, 2, 3, Direction.WEST));
        s.say("Some recipes also require a specific Electrode material", s.face(5, 2, 3, Direction.EAST));
    }

    public static void distillationTower(net.createmod.ponder.api.scene.SceneBuilder builder, SceneBuildingUtil util) {
        Story s = Story.start(builder, util, "distillation_tower", "The Distillation Tower", 5);
        Selection burners = s.box(1, 1, 1, 3, 1, 3);
        s.show(burners);
        s.show(s.box(1, 2, 1, 3, 6, 3));
        s.say("Distillation Towers are hollow columns of Tower Walls, at least 3 by 3 and 3 tall", s.face(1, 4, 2, Direction.WEST));
        s.heat(burners, BlazeBurnerBlock.HeatLevel.KINDLED);
        s.say("Blaze Burners underneath provide the heat", s.face(1, 1, 1, Direction.NORTH));
        s.say("Fluids are piped into the bottom row", s.face(2, 2, 1, Direction.NORTH));
        s.say("Starting from the third row, each row outputs one product", s.face(1, 4, 2, Direction.WEST));
        s.say("Taller towers can separate more products", s.top(2, 6, 2));
    }

    public static void airlessOven(net.createmod.ponder.api.scene.SceneBuilder builder, SceneBuildingUtil util) {
        Story s = Story.start(builder, util, "airless_oven", "The Airless Oven", 5);
        s.show(s.box(1, 1, 1, 3, 3, 3));
        s.say("Airless Ovens are hollow 3x3x3 boxes of Oven Walls, the Controller in the middle of a side", s.face(1, 2, 2, Direction.WEST));
        s.assemble(2, 2, 1);
        s.say("They provide their own heat, no burner required", s.face(2, 2, 1, Direction.NORTH));
        s.say("They can be used to bake Coal into Coke, or Wood into Charcoal and Carbon Monoxide", s.top(2, 3, 2));
    }

    /** The build of {@code PonderSchematics.machineAttachments}: the chamber from (2,2,2), one of each attachment round it. */
    public static void machineAttachments(net.createmod.ponder.api.scene.SceneBuilder builder, SceneBuildingUtil util) {
        Story s = Story.start(builder, util, "machine_attachments", "Machine Attachments", 7);
        buildReactionChamber(s, 2, 2, BlazeBurnerBlock.HeatLevel.KINDLED, false);
        s.say("Reaction Chambers, Electrolysis Vats and Airless Ovens take Attachments", s.face(3, 3, 2, Direction.NORTH));
        s.upgrade(4, 4, 2, CRRItems.OUTLET_MANIFOLD.get(), MachineTiers.PIPED);
        s.say("Used on the machine, an Outlet Manifold goes into it and adds an output tank, for recipes with more products",
                s.face(4, 4, 2, Direction.EAST));
        s.show(s.at(1, 3, 3));
        s.say("An Expansion Tank adds 4000 mB to every tank", s.face(1, 3, 3, Direction.WEST));
        s.show(s.at(4, 3, 1));
        s.say("A Machine Gauge gives a comparator signal: the progress, or how full the fullest output is. A Wrench switches it",
                s.face(4, 3, 1, Direction.NORTH));
        s.upgrade(3, 4, 4, CRRItems.GASKET.get(), MachineTiers.SEALED);
        s.say("A Gasket seals the machine: its toxic contents no longer leak. Sneak with an empty hand to take an upgrade back out",
                s.face(3, 4, 4, Direction.SOUTH));
        s.show(s.box(2, 3, 5, 2, 3, 6));
        s.spin(s.box(2, 3, 5, 2, 3, 6), 128);
        s.say("A Circulation Pump speeds recipes up with the rotation it gets, by half at 256 RPM", s.face(2, 3, 5, Direction.SOUTH));
        s.say("Small machines take 4 attachments, large ones 8", s.top(3, 5, 3));
    }

    public static void smallMachines(net.createmod.ponder.api.scene.SceneBuilder builder, SceneBuildingUtil util) {
        Story s = Story.start(builder, util, "small_machines", "Small Machines", 5);
        s.show(s.at(1, 1, 2));
        s.say("Small Electrolysers can process simple electrolysis recipes when powered", s.face(1, 1, 2, Direction.WEST));
        s.show(s.at(3, 1, 2));
        s.say("Fermentation Barrels process small reactions slowly, without any power", s.face(3, 1, 2, Direction.NORTH));
    }

    public static void oreProcessing(net.createmod.ponder.api.scene.SceneBuilder builder, SceneBuildingUtil util) {
        Story s = Story.start(builder, util, "ore_processing", "Roasting Ore Dusts", 7);
        s.show(s.at(1, 1, 1));
        s.show(s.box(0, 3, 1, 2, 3, 2));
        s.spin(s.box(0, 3, 1, 0, 3, 2), 64);
        s.spin(s.box(2, 3, 1, 2, 3, 2), -64);
        s.say("Crushing Wheels grind ore-bearing stones, such as Asurine, into dust", s.top(1, 1, 1));
        buildReactionChamber(s, 4, 2, BlazeBurnerBlock.HeatLevel.SEETHING, false);
        s.say("Some dusts are roasted: superheated with Oxygen in a Reaction Chamber, they give their metals", s.face(5, 3, 2, Direction.NORTH));
        s.say("Asurine gives zinc, Veridium copper, Tuff nickel, and Scoria lead and sulfur", s.top(5, 5, 3));
        s.say("Merely heated, two dusts and Blaze Powder work too, more slowly", s.face(4, 1, 2, Direction.NORTH));
        s.say("Ochrum, Crimsite and Granite each take their own way", s.top(1, 1, 1));
    }

    /** The build of {@code PonderSchematics.oreDusts}: the blast furnace at (1,1,1), the granite Mixer at (2,_,6), the vat from (4,1,4). */
    public static void oreDusts(net.createmod.ponder.api.scene.SceneBuilder builder, SceneBuildingUtil util) {
        Story s = Story.start(builder, util, "ore_dusts", "Other Ore Dusts", 9);
        s.show(s.at(1, 1, 1));
        s.say("Crimsite Dust smelts into Pig Iron, the start of steel", s.face(1, 1, 1, Direction.NORTH));
        Selection mixer = s.box(2, 1, 6, 2, 5, 6);
        s.show(mixer);
        s.heat(mixer, BlazeBurnerBlock.HeatLevel.SEETHING);
        s.spin(s.box(2, 4, 6, 2, 5, 6), 64);
        s.say("Granite Dust, superheated in a Mixer, splits into Alumina and Quartz", s.face(2, 2, 6, Direction.WEST));
        s.show(s.box(4, 1, 4, 8, 3, 6));
        s.assemble(6, 2, 4);
        s.say("Ochrum Dust is electrolysed into gold and lead, with Gold-Steel Electrodes", s.face(6, 2, 4, Direction.NORTH));
        s.say("With Lead Electrodes and Sulfuric Acid, Veridium and Asurine are electrowon into copper and zinc", s.top(6, 3, 5));
    }

    /** The build of {@code PonderSchematics.petrochemistry}: inputs on the left (x 0), the 4-tall chamber, outputs on the right (x 6). */
    public static void petrochemistry(net.createmod.ponder.api.scene.SceneBuilder builder, SceneBuildingUtil util) {
        Story s = Story.start(builder, util, "petrochemistry", "Cracking Naphtha", 7);
        buildReactionChamber(s, 2, 2, BlazeBurnerBlock.HeatLevel.KINDLED, false);
        s.show(s.box(0, 1, 2, 1, 2, 4));
        s.say("Naphtha from a Distillation Tower can be cracked into lighter gases", s.face(0, 1, 2, Direction.WEST));
        s.say("Pipe it into a heated Reaction Chamber together with Steam", s.face(0, 1, 4, Direction.WEST));
        s.spin(s.box(3, 5, 3, 3, 7, 3), 32);
        s.say("The stirring shaft must turn between 24 and 64 RPM", s.top(3, 5, 3));
        s.say("Each product needs its own output tank: an Outlet Manifold used on the chamber adds the second one", s.face(3, 4, 2, Direction.NORTH));
        s.show(s.box(5, 1, 2, 6, 2, 4));
        s.say("Ethylene and Propylene come out together, and can be pumped out of any wall", s.face(6, 1, 2, Direction.NORTH));
        s.say("Both are the base of plastics, and each can be turned into the other", s.top(6, 1, 3));
    }

    /** The build of {@code PonderSchematics.plastics}: the ethylene tank at (0,1,5), the chamber from (1,2,4), the casting Basin at (6,1,2). */
    public static void plastics(net.createmod.ponder.api.scene.SceneBuilder builder, SceneBuildingUtil util) {
        Story s = Story.start(builder, util, "plastics", "Making Plastic", 9);
        buildReactionChamber(s, 1, 4, BlazeBurnerBlock.HeatLevel.KINDLED, false);
        s.show(s.box(0, 1, 5, 0, 2, 5));
        s.spin(s.box(2, 5, 5, 2, 7, 5), 32);
        s.say("Ethylene and Silica (quartz dust), heated and stirred in a Reaction Chamber, give Liquid HDPE", s.face(1, 3, 5, Direction.WEST));
        s.show(s.box(4, 1, 2, 6, 4, 5));
        s.spin(s.box(6, 3, 2, 6, 4, 2), 64);
        s.say("Cast in a Mixer, it sets into HDPE Pellets, which craft into HDPE Sheets", s.face(6, 1, 2, Direction.NORTH));
        s.show(s.at(8, 1, 2));
        s.say("Propylene with Aluminum Nuggets gives Liquid Polypropylene instead, which casts straight into sheets", s.top(8, 1, 2));
        s.say("Plastic can be crushed back into pellets", s.top(8, 1, 2));
    }

    /** The build of {@code PonderSchematics.dieselGeneratorsTower}: a 1x1 tower of five levels on a burner at (2,1,2). */
    public static void dieselGeneratorsTower(net.createmod.ponder.api.scene.SceneBuilder builder, SceneBuildingUtil util) {
        Story s = Story.start(builder, util, "distillation_tower_cdg", "Distilling with Diesel Generators", 5);
        Selection burner = s.at(2, 1, 2);
        s.show(burner);
        s.show(s.box(2, 2, 2, 2, 6, 2));
        s.say("With Create Diesel Generators installed, distilling runs in its Distillation Tower: a Fluid Tank at least 3 tall, "
                + "turned into one with a Distillation Controller", s.face(2, 4, 2, Direction.WEST));
        s.heat(burner, BlazeBurnerBlock.HeatLevel.KINDLED);
        s.say("Blaze Burners underneath provide the heat", s.face(2, 1, 2, Direction.NORTH));
        s.say("Fluids go into the bottom level, and each level above holds one product", s.face(2, 2, 2, Direction.NORTH));
        s.say("Crude Oil gives Naphtha, Diesel, Gasoline and LPG, from the bottom up", s.top(2, 6, 2));
    }

    /** The build of {@code PonderSchematics.lithiumBrineDieselGenerators}: the tower at (1,_,2), the pipe, the Mixer at (4,_,2). */
    public static void lithiumBrineDieselGenerators(net.createmod.ponder.api.scene.SceneBuilder builder, SceneBuildingUtil util) {
        Story s = Story.start(builder, util, "lithium_brine_cdg", "Lithium from Brine", 7);
        Selection burner = s.at(1, 1, 2);
        s.show(burner);
        s.show(s.box(1, 2, 2, 1, 4, 2));
        s.heat(burner, BlazeBurnerBlock.HeatLevel.KINDLED);
        s.say("In a Distillation Tower, Water boils off as Steam, leaving a little Weak Brine on its own level", s.face(1, 3, 2, Direction.NORTH));
        s.say("Distilling the Weak Brine again concentrates it into Strong Brine", s.face(1, 3, 2, Direction.WEST));
        s.show(s.box(2, 2, 2, 3, 3, 2));
        Selection mixer = s.box(4, 1, 2, 4, 5, 2);
        s.show(mixer);
        s.heat(mixer, BlazeBurnerBlock.HeatLevel.KINDLED);
        s.spin(s.box(4, 4, 2, 4, 5, 2), 64);
        s.say("A heated Mixer turns brine into Lithium Brine and Salt, sometimes with Borax", s.face(4, 2, 2, Direction.NORTH));
        s.heat(mixer, BlazeBurnerBlock.HeatLevel.SEETHING);
        s.say("Superheated, Lithium Brine gives Lithium Nuggets", s.face(4, 2, 2, Direction.NORTH));
    }

    public static void lithiumBrine(net.createmod.ponder.api.scene.SceneBuilder builder, SceneBuildingUtil util) {
        Story s = Story.start(builder, util, "lithium_brine", "Lithium from Brine", 7);
        Selection towerBurners = s.box(1, 1, 1, 3, 1, 3);
        s.show(towerBurners);
        s.show(s.box(1, 2, 1, 3, 6, 3));
        s.heat(towerBurners, BlazeBurnerBlock.HeatLevel.KINDLED);
        s.say("Distilling Water boils most of it off as Steam, leaving a little Weak Brine", s.face(2, 2, 1, Direction.NORTH));
        s.say("Distilling Weak Brine again concentrates it into Strong Brine", s.face(1, 4, 2, Direction.WEST));
        s.show(s.box(4, 2, 2, 4, 4, 2));
        Selection mixer = s.box(5, 1, 2, 5, 5, 2);
        s.show(mixer);
        s.heat(mixer, BlazeBurnerBlock.HeatLevel.KINDLED);
        s.spin(s.box(5, 4, 2, 5, 5, 2), 64);
        s.say("A heated Mixer turns brine into Lithium Brine and Salt, sometimes with Borax", s.face(5, 2, 2, Direction.NORTH));
        s.heat(mixer, BlazeBurnerBlock.HeatLevel.SEETHING);
        s.say("Superheated, Lithium Brine gives Lithium Nuggets", s.face(5, 2, 2, Direction.NORTH));
    }

    /** The build of {@code PonderSchematics.titanium}: the tetrachloride Mixer at x 1, the pump and pipe, the titanium Mixer at x 4. */
    public static void titanium(net.createmod.ponder.api.scene.SceneBuilder builder, SceneBuildingUtil util) {
        Story s = Story.start(builder, util, "titanium", "Making Titanium", 7);
        Selection first = s.box(1, 1, 3, 1, 5, 3);
        s.show(first);
        s.heat(first, BlazeBurnerBlock.HeatLevel.SEETHING);
        s.spin(s.box(1, 4, 3, 1, 5, 3), 64);
        s.say("Titanium starts as Titanium Tetrachloride, mixed superheated from Crimsite Dust, Coal Coke and Bleach",
                s.face(1, 2, 3, Direction.NORTH));
        s.show(s.box(2, 2, 3, 3, 2, 3));
        s.spin(s.at(2, 2, 3), 32);
        s.say("It is pumped on to a second Mixer", s.face(2, 2, 3, Direction.NORTH));
        Selection second = s.box(4, 1, 3, 4, 5, 3);
        s.show(second);
        s.heat(second, BlazeBurnerBlock.HeatLevel.SEETHING);
        s.spin(s.box(4, 4, 3, 4, 5, 3), 64);
        s.say("Superheated with a Magnesium ingot, it gives Titanium", s.face(4, 2, 3, Direction.NORTH));
        s.say("Magnesium comes from an Electrolysis Vat: Magnesium Chloride with Graphite Electrodes", s.top(4, 2, 3));
        s.say("Reacted with Oxygen in a Reaction Chamber, the Tetrachloride gives Titanium Dioxide instead", s.top(1, 2, 3));
    }

    public static void polymetallicNodule(net.createmod.ponder.api.scene.SceneBuilder builder, SceneBuildingUtil util) {
        Story s = Story.start(builder, util, "polymetallic_nodule", "Polymetallic Nodules", 5);
        s.show(s.at(1, 1, 3));
        s.say("Polymetallic Nodules lie in small patches on the ocean floor. Fishing sometimes brings one up", s.top(1, 1, 3));
        s.show(s.at(1, 1, 1));
        s.show(s.box(0, 3, 1, 2, 3, 2));
        s.spin(s.box(0, 3, 1, 0, 3, 2), 64);
        s.spin(s.box(2, 3, 1, 2, 3, 2), -64);
        s.say("Crushing Wheels break them into Crushed Nodules", s.top(1, 1, 1));
        Selection mixer = s.box(4, 1, 1, 4, 5, 1);
        s.show(mixer);
        s.heat(mixer, BlazeBurnerBlock.HeatLevel.KINDLED);
        s.spin(s.box(4, 4, 1, 4, 5, 1), 64);
        s.say("A heated Mixer refines them into Manganese", s.face(4, 2, 1, Direction.NORTH));
        s.say("with a chance of Nickel, Zinc and Rare Earth Dust on the side", s.face(4, 2, 1, Direction.WEST));
    }

    public static void gasDiffuser(net.createmod.ponder.api.scene.SceneBuilder builder, SceneBuildingUtil util) {
        Story s = Story.start(builder, util, "gas_diffuser", "The Gas Diffuser", 7);
        s.show(s.at(3, 1, 3));
        s.say("Gas Diffusers work like Aeronautics' burners, but fill balloons with Helium or Hydrogen", s.top(3, 1, 3));
        s.show(s.box(4, 1, 3, 5, 1, 3));
        s.say("The gas is piped into the Diffuser's tank, and slowly used up while it runs", s.face(5, 1, 3, Direction.NORTH));
        s.show(s.box(1, 3, 1, 5, 5, 5));
        s.say("Both lift more than hot air. Hydrogen lifts the most", s.face(1, 4, 3, Direction.WEST));
    }

    public static void acetyleneLamp(net.createmod.ponder.api.scene.SceneBuilder builder, SceneBuildingUtil util) {
        Story s = Story.start(builder, util, "acetylene_lamp", "The Acetylene Lamp", 7);
        ItemStack lamp = new ItemStack(CRRItems.ACETYLENE_LAMP.get());
        s.scene().world().createEntity(level -> {
            ArmorStand stand = new ArmorStand(EntityType.ARMOR_STAND, level);
            stand.setPos(3.5, 1, 3.5);
            stand.setYRot(180);
            stand.setYBodyRot(180);
            stand.setShowArms(true);
            stand.setNoBasePlate(true);
            stand.setItemSlot(EquipmentSlot.MAINHAND, lamp);
            return stand;
        });
        s.scene().idle(10);
        Vec3 hand = s.util().vector().of(3.2, 2.0, 3.5);
        s.say("Held in either hand, the Acetylene Lamp lights up its surroundings", hand);
        List<ElementLink<EntityElement>> undead = new ArrayList<>();
        for (double[] at : new double[][] {{1.0, 5.5}, {6.0, 5.0}}) {
            undead.add(s.scene().world().createEntity(level -> {
                Zombie zombie = new Zombie(EntityType.ZOMBIE, level);
                zombie.setPos(at[0], 1, at[1]);
                zombie.setYRot(at[0] < 3 ? -120 : 120);
                zombie.setYHeadRot(zombie.getYRot());
                zombie.setNoAi(true);
                return zombie;
            }));
        }
        s.scene().idle(20);
        s.scene().overlay().showControls(hand, Pointing.DOWN, 40).rightClick().withItem(lamp);
        s.scene().idle(10);
        s.say("Using it supercharges the lamp for half a minute, burning some of its Acetylene", hand);
        for (ElementLink<EntityElement> zombie : undead) {
            s.scene().world().modifyEntity(zombie, entity -> entity.setRemainingFireTicks(400));
        }
        s.say("While supercharged, undead nearby catch fire, and no monsters spawn around its holder", s.util().vector().of(1.0, 2.2, 5.5));
        s.say("Refill it with Acetylene from a Spout or a Charging Pad", hand);
    }

    // ---- toxic compounds ---------------------------------------------------------------------------------------------

    // The in-game contamination haze (ClientContamination) and the vent colour of contaminated methane (the build's gas tank).
    private static final int HAZE = 0xA8C84A;
    private static final int METHANE = 0x8A9A6A;

    /** A puddle of {@code fluid} centred on (x, 1, z), on top of the floor. */
    private static ElementLink<EntityElement> pool(Story s, double x, double z, String fluid, int amount) {
        return s.scene().world().createEntity(level -> {
            LeakPoolEntity pool = new LeakPoolEntity(CRREntities.LEAK_POOL.get(), level);
            pool.setPos(x, 1.0, z);
            pool.setContents(BuiltInRegistries.FLUID.get(ResourceLocation.parse(fluid)), amount);
            return pool;
        });
    }

    /** Shrinks a pool to nothing over a second, as a drain or a cleaning agent would. */
    private static void drain(Story s, ElementLink<EntityElement> pool) {
        for (int i = 0; i < 10; i++) {
            s.scene().world().modifyEntity(pool, entity -> ((LeakPoolEntity) entity).addAmount(-LeakPoolEntity.MAX_AMOUNT / 10));
            s.scene().idle(2);
        }
    }

    /** A spray of gas from {@code at}, for {@code ticks}. */
    private static void vent(Story s, Vec3 at, int colour, int ticks) {
        s.scene().effects().emitParticles(at, s.scene().effects().simpleParticleEmitter(CRRParticles.haze(colour, 0.5F), new Vec3(0, 0.05, 0)), 3, ticks);
    }

    /** Contamination haze drifting over the floor between two corners, for {@code ticks}. */
    private static void haze(Story s, int x1, int z1, int x2, int z2, int ticks) {
        for (int x = x1; x <= x2; x += 2) {
            for (int z = z1; z <= z2; z += 2) {
                s.scene().effects().emitParticles(s.util().vector().of(x, 1, z),
                        s.scene().effects().particleEmitterWithinBlockSpace(CRRParticles.haze(HAZE, 0.3F), Vec3.ZERO), 0.2F, ticks);
            }
        }
    }

    /** Pours {@code amount} more into a pool over half a second, so it visibly spreads. */
    private static void grow(Story s, ElementLink<EntityElement> pool, int amount) {
        for (int i = 0; i < 5; i++) {
            s.scene().world().modifyEntity(pool, entity -> ((LeakPoolEntity) entity).addAmount(amount / 5));
            s.scene().idle(2);
        }
    }

    /** A burning pool going up: flash, flames and smoke where it was, then fires left burning around it. */
    private static void explode(Story s, ElementLink<EntityElement> pool, Vec3 at, BlockPos... fires) {
        var effects = s.scene().effects();
        effects.emitParticles(at, effects.simpleParticleEmitter(ParticleTypes.FLAME, new Vec3(0, 0.06, 0)), 8, 8);
        s.scene().idle(8);
        effects.emitParticles(at, effects.simpleParticleEmitter(ParticleTypes.EXPLOSION_EMITTER, Vec3.ZERO), 1, 1);
        effects.emitParticles(at, effects.simpleParticleEmitter(ParticleTypes.LARGE_SMOKE, new Vec3(0, 0.08, 0)), 3, 40);
        s.scene().world().modifyEntity(pool, Entity::discard);
        for (BlockPos fire : fires) {
            s.scene().world().setBlock(fire, Blocks.FIRE.defaultBlockState(), false);
        }
    }

    /**
     * The build of {@code PonderSchematics.leaks}: a crude oil tank at (1,1,1) feeding a pipe along y 2 to (4,2,1), a tank of
     * contaminated methane at (5,1,4) and a Floor Drain in the floor at (3,0,4). A dripping pipe, a pool, a gas jet, the haze, a fire,
     * and the drain.
     */
    public static void leaks(net.createmod.ponder.api.scene.SceneBuilder builder, SceneBuildingUtil util) {
        Story s = Story.start(builder, util, "leaks", "Leaks", 7);
        // The whole of the first layers, so the torch and fires placed later show up.
        s.show(s.box(0, 1, 0, 6, 2, 6));
        s.say("Some fluids are toxic, as shown on their tooltip", s.face(1, 1, 1, Direction.NORTH));
        var effects = s.scene().effects();
        effects.emitParticles(s.util().vector().of(3.5, 1.95, 1.5), effects.simpleParticleEmitter(new FluidParticleData(AllParticleTypes.FLUID_DRIP.get(),
                new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("reactingreactions:crude_oil")), 1000)), Vec3.ZERO), 0.25F, 140);
        var oil = pool(s, 3.5, 1.5, "reactingreactions:crude_oil", 40);
        grow(s, oil, 250);
        s.say("Tanks, pipes and machines holding them leak now and then. Liquids drip down and pool on the floor", s.face(3, 2, 1, Direction.NORTH));
        grow(s, oil, 400);
        s.say("The drips lead back to the leak. Glass pipes leak less; Encased and Plastic Pipes never do", s.face(3, 2, 1, Direction.NORTH));
        s.say("A Gasket used on a Fluid Tank seals it. Sneak with an empty hand to take it back", s.face(1, 1, 1, Direction.NORTH));
        effects.emitParticles(s.util().vector().of(4.95, 1.5, 4.5), effects.simpleParticleEmitter(CRRParticles.haze(METHANE, 0.55F),
                new Vec3(-0.15, 0.01, 0)), 2, 140);
        effects.emitParticles(s.util().vector().of(3.8, 1.6, 4.5), effects.particleEmitterWithinBlockSpace(CRRParticles.haze(METHANE, 0.3F), Vec3.ZERO),
                0.3F, 160);
        s.say("Gases jet out of one side with a hiss, then hang in a cloud", s.face(5, 1, 4, Direction.WEST));
        haze(s, 0, 0, 6, 6, 200);
        s.say("Both foul the air, harming anyone nearby, plants and animals. Mild ones, like hydrogen, do not", s.top(3, 0, 3));

        s.scene().world().setBlock(new BlockPos(2, 1, 2), Blocks.TORCH.defaultBlockState(), false);
        s.scene().idle(10);
        s.say("Flammable leaks catch fire near flames, and large ones explode", s.face(2, 1, 2, Direction.NORTH));
        BlockPos[] fires = {new BlockPos(3, 1, 2), new BlockPos(4, 1, 2), new BlockPos(2, 1, 1)};
        explode(s, oil, s.util().vector().of(3.5, 1.1, 1.5), fires);
        s.scene().idle(50);
        for (BlockPos fire : fires) {
            s.scene().world().setBlock(fire, Blocks.AIR.defaultBlockState(), false);
        }
        s.scene().world().setBlock(new BlockPos(2, 1, 2), Blocks.AIR.defaultBlockState(), false);
        s.scene().idle(10);

        var spill = pool(s, 3.5, 5.0, "reactingreactions:crude_oil", 50);
        grow(s, spill, 400);
        s.say("Floor Drains soak up nearby pools into a tank, which pipes can empty", s.top(3, 0, 4));
        drain(s, spill);
        s.scene().idle(20);
    }

    /**
     * The build of {@code PonderSchematics.stayingSafe}: an Atmospheric Scrubber at (2,1,3) facing west with its motor behind, and a
     * Gas Vent on a pipe at (5,2,2), with a pool front left (1,_,5) where nothing hides it. The gauge, protection, the scrubber,
     * cleaning by hand and the vent.
     */
    public static void stayingSafe(net.createmod.ponder.api.scene.SceneBuilder builder, SceneBuildingUtil util) {
        Story s = Story.start(builder, util, "staying_safe", "Staying Safe", 7);
        GaugeOverlayElement gauge = new GaugeOverlayElement();
        s.scene().addInstruction(scene -> scene.addElement(gauge));
        var puddle = pool(s, 1.0, 5.5, "reactingreactions:sulfuric_acid", 800);
        haze(s, 0, 0, 6, 6, 260);
        s.scene().idle(10);
        s.scene().addInstruction(scene -> gauge.setTarget(35));
        s.say("Exposure to toxic compounds fills a gauge above the hunger bar", s.top(1, 0, 5));
        s.scene().addInstruction(scene -> gauge.setTarget(70));
        s.say("Carrying toxic fluids, breathing fouled air or standing in pools raises it", s.top(1, 0, 5));
        s.scene().addInstruction(scene -> gauge.setTarget(95));
        s.say("High levels harm you more and more. A full gauge is deadly", s.top(1, 0, 5));
        s.scene().addInstruction(scene -> gauge.setTarget(20));
        s.say("It goes down slowly, faster with Charcoal Tablets and Antidotes. Gas Masks, Chemical Gloves and Boots protect you",
                s.top(1, 0, 5));

        Selection scrubber = s.box(2, 1, 3, 3, 1, 3);
        s.show(scrubber);
        s.spin(scrubber, 32);
        s.say("Atmospheric Scrubbers clean the air around them, with rotation and Activated Carbon. Lye doubles their effect",
                s.face(2, 1, 3, Direction.WEST));
        s.say("Inside a sealed room, they cover a much larger area", s.top(2, 1, 3));
        s.say("Pools can also be cleaned by hand with Soap, Bleach or a Sponge", s.top(1, 0, 5));
        drain(s, puddle);

        s.show(s.box(5, 1, 2, 5, 2, 2));
        var effects = s.scene().effects();
        effects.emitParticles(s.util().vector().of(5.5, 2.95, 2.5), effects.simpleParticleEmitter(CRRParticles.haze(METHANE, 0.5F),
                new Vec3(0, 0.15, 0)), 2, 100);
        s.say("Gas Vents let piped gases out of the way. Toxic ones still pollute, unless a scrubber covers the vent", s.face(5, 2, 2, Direction.NORTH));
        s.scene().idle(20);
    }
}
