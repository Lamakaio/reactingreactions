package com.koala.reactingreactions.content.ponder;

import com.koala.reactingreactions.content.multiblock.attachment.MachineAttachment;
import com.koala.reactingreactions.content.multiblock.attachment.OutletValveBlockEntity;
import com.koala.reactingreactions.content.multiblock.MultiblockControllerBlockEntity;
import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.content.compat.ElectroEnergeticsCompat;
import com.koala.reactingreactions.content.drill.DerrickControllerBlockEntity;
import com.koala.reactingreactions.content.ponder.PonderSchematicExporter.Build;
import com.simibubi.create.content.fluids.tank.CreativeFluidTankBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the showcase world ({@code ./gradlew runShowcase}): a small connected factory in a fresh superflat world (see
 * build.gradle for its layers), run for a few minutes so its machines are mid-work, then saved. Its log ends with what every
 * machine and chest holds, to check the lines really flow. Only registered when the {@code reactingreactions.buildShowcase}
 * property is set.
 *
 * <p>Coordinates below are relative to the floor: y 0 is the floor layer, machines stand from y 1. The lines:
 * <ul>
 *     <li>Derrick (titanium head over a Rich Asurine Vein, coolant fed in) → dust up a belt → superheated Reaction Chamber</li>
 *     <li>Water → Electrolysis Vat (Electro Energetics battery) → oxygen to that chamber, hydrogen to a Gas Diffuser balloon</li>
 *     <li>Crude oil → Diesel Generators' Distillation Tower → naphtha + steam → second chamber → ethane and propane tanks</li>
 *     <li>Our machines' products leave through Outlet Valves; pumps only feed them and Diesel Generators' tower</li>
 *     <li>An Airless Oven making coke, a leaking corner with a Floor Drain, a Scrubber, and a gallery of the equipment</li>
 * </ul>
 */
public final class ShowcaseWorldBuilder {
    public static final String PROPERTY = "reactingreactions.buildShowcase";
    /** The superflat world's top layer (see build.gradle). */
    private static final int FLOOR_Y = -21;
    private static final int FORM_TICKS = 80;
    /** The property's value is how many seconds the factory runs before it is saved. */
    private static final int RUN_TICKS = 20 * Integer.parseInt(System.getProperty(PROPERTY, "240"));
    private static final String CRR = "reactingreactions:";

    private static Build build;
    private static final Map<String, BlockPos> REPORT = new LinkedHashMap<>();
    private static int ticks = -1;

    private ShowcaseWorldBuilder() {
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        ServerLevel level = server.overworld();
        GameRules rules = level.getGameRules();
        rules.getRule(GameRules.RULE_DAYLIGHT).set(false, server);
        rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, server);
        rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
        rules.getRule(GameRules.RULE_DOFIRETICK).set(false, server);
        rules.getRule(GameRules.RULE_KEEPINVENTORY).set(true, server);
        level.setDayTime(6000);
        level.setWeatherParameters(6000, 0, false, false);
        level.setDefaultSpawnPos(new BlockPos(11, FLOOR_Y + 1, -5), 0);
        for (int cx = -2; cx <= 3; cx++) {
            for (int cz = -2; cz <= 3; cz++) {
                level.setChunkForced(cx, cz, true);
            }
        }
        ticks = 0;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (ticks < 0) {
            return;
        }
        MinecraftServer server = event.getServer();
        ServerLevel level = server.overworld();
        ticks++;
        try {
            if (ticks == 1) {
                build = new Build();
                layout(build.shifted(0, FLOOR_Y, 0));
                PonderSchematicExporter.place(level, build);
            } else if (ticks == FORM_TICKS) {
                // One broken step should not cost the whole world: log it and carry on.
                for (var step : build.afterForming) {
                    try {
                        step.accept(level);
                    } catch (Exception e) {
                        ReactingReactions.LOGGER.error("Showcase step failed", e);
                    }
                }
                ReactingReactions.LOGGER.info("Showcase built, running it for {} seconds", RUN_TICKS / 20);
            } else if (ticks == FORM_TICKS + RUN_TICKS) {
                report(level);
                for (int cx = -2; cx <= 3; cx++) {
                    for (int cz = -2; cz <= 3; cz++) {
                        level.setChunkForced(cx, cz, false);
                    }
                }
                ticks = -1;
                server.halt(false);
            }
        } catch (Exception e) {
            ReactingReactions.LOGGER.error("Showcase world failed", e);
            ticks = -1;
            server.halt(false);
        }
    }

    // ---- the layout ----------------------------------------------------------------------------------------------------

    private static void layout(Build w) {
        w.fill(-6, 0, -8, 46, 0, 36, "minecraft:smooth_stone");
        w.fill(-6, 0, -2, 46, 0, -1, "minecraft:polished_andesite");
        w.fill(-6, 0, 13, 46, 0, 15, "minecraft:polished_andesite");
        oreLine(w);
        electrolysisLine(w);
        petrochemistry(w);
        airlessOven(w, 16, 18);
        toxicCorner(w, 26, 19);
        gallery(w, 36, 1);
        sign(w, 11, 1, -3, 8, "Create:", "Reacting Reactions", "showcase", "");
    }

    /** The Derrick drilling asurine dust, belted into a superheated Reaction Chamber that also takes the vat's oxygen. */
    private static void oreLine(Build w) {
        PonderSchematics.rig(w.shifted(2, -3, 2), "create:asurine", CRR + "rich_asurine_vein", CRR + "mineral_drill_head_titanium");
        motor(w, 5, 5, 5, 96);
        // Coolant into the tower's north-west corner block.
        creativeTank(w, 1, 1, 4, CRR + "coolant");
        pump(w, 2, 1, 4, Direction.EAST, Direction.UP);
        w.set(3, 1, 4, "create:fluid_pipe");
        // Dust out of the north-east corner block, dropped into a chute (over a belt or depot, the funnel would turn into a belt
        // funnel), lifted by an arm into a funnel on the chamber.
        w.set(7, 1, 4, "create:andesite_funnel", "facing=east", "extracting=true");
        w.set(7, 0, 4, "create:chute");
        arm(w, 10, 1, 3, new BlockPos(7, 0, 4), new BlockPos(11, 2, 3));
        w.set(11, 2, 3, "create:andesite_funnel", "facing=west");
        PonderSchematics.reactionChamber(w, 12, 2);
        heatChamber(w, 12, 2, BlazeBurnerBlock.HeatLevel.SEETHING);
        motor(w, 13, 7, 3, 96);
        // Products out of the south wall through filtered funnels (the dust waiting to react stays), via hoppers into a chest.
        filteredFunnel(w, 13, 2, 5, Direction.SOUTH, "create:zinc_nugget");
        filteredFunnel(w, 12, 2, 5, Direction.SOUTH, CRR + "rare_earth_dust");
        w.set(12, 1, 5, "minecraft:hopper", "facing=east");
        w.set(13, 1, 5, "minecraft:hopper", "facing=south");
        w.set(13, 1, 6, "minecraft:chest", "facing=north");
        sign(w, 6, 1, 0, 8, "Derrick", "drilling asurine", "dust from a", "Rich Vein");
        sign(w, 11, 1, 0, 8, "Reaction Chamber", "dust + oxygen,", "superheated:", "zinc nuggets");
        REPORT.put("derrick", w.at(5, 4, 5));
        REPORT.put("derrick output", w.at(6, 1, 4));
        REPORT.put("dust chute", w.at(7, 0, 4));
        REPORT.put("ore chamber", w.at(13, 3, 2));
        REPORT.put("nugget chest", w.at(13, 1, 6));
    }

    /** Water split by the vat: oxygen to the ore chamber, hydrogen up into a balloon. */
    private static void electrolysisLine(Build w) {
        PonderSchematics.shell(w, CRR + "electrolysis_vat_wall", 17, 1, 2, 21, 3, 4);
        w.set(18, 2, 3, CRR + "graphite_electrode", "facing=up");
        w.set(20, 2, 3, CRR + "gold_steel_electrode", "facing=up");
        // The terminals sit in the end walls beside the electrodes; the pipes use the walls next to them.
        w.set(17, 2, 3, CRR + "electrolysis_vat_terminal");
        w.set(21, 2, 3, CRR + "electrolysis_vat_terminal");
        w.set(19, 2, 2, CRR + "electrolysis_vat_controller");
        creativeTank(w, 23, 2, 4, "minecraft:water");
        w.set(23, 1, 4, "create:andesite_casing");
        pump(w, 22, 2, 4, Direction.WEST, Direction.UP);
        // Oxygen west into the ore chamber, hydrogen south to the Gas Diffuser, each let out by an Outlet Valve.
        valve(w, 16, 2, 4, Direction.WEST, CRR + "oxygen");
        w.set(15, 2, 4, "create:fluid_pipe");
        valve(w, 19, 2, 5, Direction.SOUTH, CRR + "hydrogen");
        w.fill(19, 2, 6, 19, 2, 10, "create:fluid_pipe");
        // What the balloon does not take, and the oxygen the ore chamber does not, go up Gas Vents, so the vat never backs up.
        // Vents take gas at their back, so each stands on a pipe.
        w.set(18, 2, 7, "create:fluid_pipe");
        w.set(18, 3, 7, CRR + "gas_vent", "facing=up");
        valve(w, 20, 2, 1, Direction.NORTH, CRR + "oxygen");
        w.fill(20, 2, 0, 20, 2, -1, "create:fluid_pipe");
        w.set(20, 3, -1, CRR + "gas_vent", "facing=up");
        sign(w, 22, 1, -1, 8, "Gas Vents", "surplus hydrogen", "and oxygen go up", "into the air");
        if (ElectroEnergeticsCompat.isLoaded()) {
            w.set(19, 1, 0, "electroenergetics:creative_battery");
            // Water electrolysis needs 1250 V; twice that runs it at full speed. The setting is in millivolts.
            w.after(19, 1, 0, (level, pos) -> {
                ScrollValueBehaviour voltage = BlockEntityBehaviour.get(level, pos, ScrollValueBehaviour.TYPE);
                if (voltage != null) {
                    voltage.setValue(2_500_000);
                }
            });
            BlockPos battery = w.at(19, 1, 0);
            w.after(17, 2, 3, (level, pos) -> ShowcaseElectrics.wire(level, battery, 0, pos, 0));
            w.after(21, 2, 3, (level, pos) -> ShowcaseElectrics.wire(level, battery, 1, pos, 0));
        }
        if (ModList.get().isLoaded("aeronautics_bundled")) {
            w.set(19, 1, 10, CRR + "gas_diffuser");
            // Like Aeronautics' burners, it only burns with a redstone signal.
            w.set(18, 1, 10, "minecraft:lever", "face=floor", "facing=north", "powered=true");
            for (int x = 17; x <= 21; x++) {
                for (int z = 8; z <= 12; z++) {
                    if (x == 17 || x == 21 || z == 8 || z == 12) {
                        w.fill(x, 3, z, x, 4, z, "aeronautics:white_envelope");
                    }
                    w.set(x, 5, z, "aeronautics:white_envelope");
                }
            }
            REPORT.put("gas diffuser", w.at(19, 1, 10));
            // Aeronautics only lifts envelopes built into a contraption.
            sign(w, 23, 1, 9, 12, "Gas Diffuser", "fills envelopes", "with hydrogen;", "lifts contraptions");
        }
        sign(w, 19, 1, -1, 8, "Electrolysis Vat", "water to oxygen", "and hydrogen,", "split by smart pipes");
        REPORT.put("electrolysis vat", w.at(19, 2, 2));
        REPORT.put("hydrogen vent", w.at(18, 3, 7));
        REPORT.put("oxygen vent", w.at(20, 3, -1));
    }

    /** Crude oil through Diesel Generators' tower; its naphtha cracked with steam into ethane and propane. */
    private static void petrochemistry(Build w) {
        boolean cdg = ModList.get().isLoaded("createdieselgenerators");
        String crude = cdg ? "createdieselgenerators:crude_oil" : CRR + "crude_oil";
        w.set(2, 1, 20, "create:andesite_casing");
        creativeTank(w, 2, 2, 20, crude);
        pump(w, 3, 2, 20, Direction.EAST, Direction.UP);
        burner(w, 4, 1, 20, BlazeBurnerBlock.HeatLevel.KINDLED);
        if (cdg) {
            w.fill(4, 2, 20, 4, 6, 20, "createdieselgenerators:distillation_tank");
            REPORT.put("distillation tower (bottom)", w.at(4, 2, 20));
        }
        // Each level of the tower holds one product: naphtha, diesel, gasoline, LPG from the bottom up.
        pump(w, 5, 3, 20, Direction.EAST, Direction.SOUTH);
        String fuels = cdg ? "createdieselgenerators:" : CRR;
        storage(w, 4, 4, 19, Direction.NORTH, Direction.WEST, 0, "diesel", fuels + "diesel");
        storage(w, 4, 5, 21, Direction.SOUTH, Direction.WEST, 0, "gasoline", fuels + "gasoline");
        // Over the diesel tank, so piped two blocks further out.
        storage(w, 4, 6, 19, Direction.NORTH, Direction.WEST, 2, "LPG", CRR + "lpg");
        // Two fluid outputs, for ethane and propane: an Outlet Manifold, installed once it has formed, adds the second.
        PonderSchematics.reactionChamber(w, 6, 19);
        w.after(7, 3, 19, (level, pos) -> {
            if (level.getBlockEntity(pos) instanceof MultiblockControllerBlockEntity<?> chamber) {
                chamber.installUpgrade(MachineAttachment.Kind.OUTLET);
            }
        });
        heatChamber(w, 6, 19, BlazeBurnerBlock.HeatLevel.KINDLED);
        motor(w, 7, 7, 20, 48);
        w.set(7, 1, 17, "create:andesite_casing");
        creativeTank(w, 7, 2, 17, CRR + "steam");
        pump(w, 7, 2, 18, Direction.SOUTH, Direction.EAST);
        valve(w, 9, 3, 20, Direction.EAST, CRR + "ethane");
        w.set(10, 3, 20, "create:fluid_pipe");
        sinkOnPillar(w, 11, 3, 20, CRR + "ethane");
        valve(w, 7, 3, 22, Direction.SOUTH, CRR + "propane");
        w.set(7, 3, 23, "create:fluid_pipe");
        sinkOnPillar(w, 7, 3, 24, CRR + "propane");
        sign(w, 3, 1, 16, 8, "Distillation Tower", "crude oil into", "naphtha, diesel,", "gasoline and LPG");
        sign(w, 9, 1, 16, 8, "Cracking", "naphtha + steam:", "ethane and", "propane");
        REPORT.put("cracking chamber", w.at(7, 3, 19));
        REPORT.put("ethane tank", w.at(11, 3, 20));
        REPORT.put("propane tank", w.at(7, 3, 24));
    }

    /** A pump drawing from the tower's side at (x, y, z), {@code pipes} pipes on, then a creative tank of {@code fluid} on a pillar. */
    private static void storage(Build w, int x, int y, int z, Direction flow, Direction cogSide, int pipes, String label, String fluid) {
        pump(w, x, y, z, flow, cogSide);
        BlockPos pipe = new BlockPos(x, y, z);
        for (int i = 0; i < pipes; i++) {
            pipe = pipe.relative(flow);
            w.set(pipe.getX(), pipe.getY(), pipe.getZ(), "create:fluid_pipe");
        }
        BlockPos tank = pipe.relative(flow);
        sinkOnPillar(w, tank.getX(), tank.getY(), tank.getZ(), fluid);
        REPORT.put(label + " tank", w.at(tank.getX(), tank.getY(), tank.getZ()));
    }

    /** A 3x3x3 oven baking coal into coke, emptied by a hopper under its west wall into a chest. */
    private static void airlessOven(Build w, int x, int z) {
        PonderSchematics.shell(w, CRR + "airless_oven_wall", x, 1, z, x + 2, 3, z + 2);
        w.set(x + 1, 2, z, CRR + "airless_oven_controller");
        w.insert(x + 1, 2, z, "minecraft:coal", 64);
        filteredFunnel(w, x - 1, 1, z + 1, Direction.WEST, CRR + "coal_coke");
        filteredFunnel(w, x - 1, 1, z, Direction.WEST, CRR + "slag");
        w.set(x - 1, 0, z, "minecraft:hopper", "facing=south");
        w.set(x - 1, 0, z + 1, "minecraft:hopper", "facing=west");
        w.set(x - 2, 0, z + 1, "minecraft:chest", "facing=east");
        // Coking also gives off a little naphtha, let straight into a tank.
        valve(w, x + 3, 2, z + 1, Direction.EAST, CRR + "naphtha");
        sinkOnPillar(w, x + 4, 2, z + 1, CRR + "naphtha");
        sign(w, x + 1, 1, z - 2, 8, "Airless Oven", "coal to coke,", "no burner", "needed");
        REPORT.put("oven", w.at(x + 1, 2, z));
        REPORT.put("coke chest", w.at(x - 2, 0, z + 1));
        REPORT.put("oven naphtha tank", w.at(x + 4, 2, z + 1));
    }

    /** Tanks of toxic fluids left to leak, with a Floor Drain to clean up and a running Atmospheric Scrubber. */
    private static void toxicCorner(Build w, int x, int z) {
        w.set(x, 1, z, "create:fluid_tank");
        w.fill(x + 1, 1, z, x + 3, 1, z, "create:fluid_pipe");
        w.set(x + 4, 1, z, "create:fluid_tank");
        w.fillTank(x, 1, z, CRR + "sulfuric_acid", 8000);
        w.fillTank(x + 4, 1, z, CRR + "crude_oil", 8000);
        w.set(x + 2, 1, z + 4, "create:fluid_tank");
        w.fillTank(x + 2, 1, z + 4, CRR + "contaminated_methane", 8000);
        w.set(x + 2, 0, z + 2, CRR + "floor_drain", "half=top");
        w.set(x + 8, 1, z + 3, CRR + "atmospheric_scrubber", "facing=west");
        w.set(x + 9, 1, z + 3, "create:creative_motor", "facing=west");
        motor(w, x + 9, 1, z + 3, 64);
        w.insert(x + 8, 1, z + 3, CRR + "activated_carbon", 16);
        sign(w, x + 2, 1, z - 2, 8, "Toxic corner", "these tanks leak:", "mind the fumes", "(wear a gas mask)");
        REPORT.put("floor drain", w.at(x + 2, 0, z + 2));
    }

    /** Armor stands with the gear and tools, the accessories in frames, and barrels of every item. */
    private static void gallery(Build w, int x, int z) {
        w.fill(x, 1, z + 12, x + 9, 4, z + 12, "minecraft:polished_andesite");
        stand(w, x + 1, z + 2, "titanium", CRR + "plasma_multitool", CRR + "neon_blade");
        stand(w, x + 3, z + 2, "composite_exo", CRR + "acetylene_lamp", CRR + "laser_pointer");
        stand(w, x + 5, z + 2, "titanium", CRR + "titanium_bow", CRR + "iodine_spray");
        String[] worn = {"gas_mask", "oxygen_mask", "aerozine_thrusters", "flame_retardant_cloak", "spring_boots", "diving_fins",
                "chemical_gloves", "chemical_boots", "racing_anklet", "digging_ring", "magnesium_knuckle", "helium_locket", "anchor_charm"};
        for (int i = 0; i < worn.length; i++) {
            BlockPos frame = w.at(x + i % 7 + 1, 2 + i / 7, z + 11);
            String item = CRR + worn[i];
            w.after(0, 0, 0, (level, unused) -> {
                ItemFrame entity = new ItemFrame(level, frame, Direction.NORTH);
                entity.setItem(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(item))));
                level.addFreshEntity(entity);
            });
        }
        List<Item> items = new ArrayList<>(BuiltInRegistries.ITEM.stream()
                .filter(item -> BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(ReactingReactions.MODID)).toList());
        items.sort((a, b) -> BuiltInRegistries.ITEM.getKey(a).compareTo(BuiltInRegistries.ITEM.getKey(b)));
        for (int chest = 0; chest * 27 < items.size(); chest++) {
            int cx = x + chest % 9;
            int cy = 1 + chest / 9;
            // Barrels open even with another on top.
            w.set(cx, cy, z + 8, "minecraft:barrel", "facing=north");
            List<Item> part = items.subList(chest * 27, Math.min(items.size(), chest * 27 + 27));
            w.after(cx, cy, z + 8, (level, pos) -> {
                var inventory = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
                for (int slot = 0; slot < part.size(); slot++) {
                    inventory.insertItem(slot, new ItemStack(part.get(slot)), false);
                }
            });
        }
        sign(w, x + 4, 1, z - 1, 8, "Equipment", "gear on the stands,", "accessories framed,", "every item in barrels");
    }

    private static void stand(Build w, int x, int z, String armor, String mainHand, String offHand) {
        w.after(x, 1, z, (level, pos) -> {
            ArmorStand stand = new ArmorStand(EntityType.ARMOR_STAND, level);
            stand.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            stand.setYRot(180);
            stand.setShowArms(true);
            stand.setNoBasePlate(true);
            String[] parts = {"helmet", "chestplate", "leggings", "boots"};
            EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
            for (int i = 0; i < parts.length; i++) {
                stand.setItemSlot(slots[i], new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(CRR + armor + "_" + parts[i]))));
            }
            stand.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(mainHand))));
            stand.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(offHand))));
            level.addFreshEntity(stand);
        });
    }

    // ---- pieces ----------------------------------------------------------------------------------------------------------

    /**
     * A Mechanical Pump at (x, y, z) pushing towards {@code flow}, turned by a cogwheel beside it on {@code cogSide} (pumps mesh like
     * small cogwheels) and a creative motor behind that cogwheel.
     */
    private static void pump(Build w, int x, int y, int z, Direction flow, Direction cogSide) {
        w.set(x, y, z, "create:mechanical_pump", "facing=" + flow.getSerializedName());
        BlockPos cog = new BlockPos(x, y, z).relative(cogSide);
        w.set(cog.getX(), cog.getY(), cog.getZ(), "create:cogwheel", "axis=" + flow.getAxis().getSerializedName());
        BlockPos motor = cog.relative(flow.getOpposite());
        w.set(motor.getX(), motor.getY(), motor.getZ(), "create:creative_motor", "facing=" + flow.getSerializedName());
        motor(w, motor.getX(), motor.getY(), motor.getZ(), 128);
    }

    /**
     * A Mechanical Arm taking from {@code from} and depositing into {@code to}. It meshes like a small cogwheel, so it is turned by
     * one on its south side, over a motor.
     */
    private static void arm(Build w, int x, int y, int z, BlockPos from, BlockPos to) {
        w.set(x, y, z, "create:mechanical_arm");
        w.set(x, y, z + 1, "create:cogwheel", "axis=y");
        w.set(x, y - 1, z + 1, "create:creative_motor", "facing=up");
        motor(w, x, y - 1, z + 1, 64);
        BlockPos take = w.at(from.getX(), from.getY(), from.getZ());
        BlockPos deposit = w.at(to.getX(), to.getY(), to.getZ());
        w.after(x, y, z, (level, pos) -> {
            ArmInteractionPoint takePoint = ArmInteractionPoint.create(level, take, level.getBlockState(take));
            ArmInteractionPoint depositPoint = ArmInteractionPoint.create(level, deposit, level.getBlockState(deposit));
            if (takePoint == null || depositPoint == null) {
                throw new IllegalStateException("the arm at " + pos.toShortString() + " cannot reach its points");
            }
            // Points start in deposit mode.
            takePoint.cycleMode();
            ListTag points = new ListTag();
            points.add(takePoint.serialize(pos));
            points.add(depositPoint.serialize(pos));
            BlockEntity arm = level.getBlockEntity(pos);
            CompoundTag tag = arm.saveWithoutMetadata(level.registryAccess());
            tag.put("InteractionPoints", points);
            arm.loadWithComponents(tag, level.registryAccess());
            // Reloading drops it from its kinetic network; join it again.
            if (arm instanceof KineticBlockEntity kinetic) {
                kinetic.detachKinetics();
                kinetic.attachKinetics();
            }
        });
    }

    /** A brass funnel pulling only {@code item} out of the block behind it and dropping it in front. */
    private static void filteredFunnel(Build w, int x, int y, int z, Direction facing, String item) {
        w.set(x, y, z, "create:brass_funnel", "facing=" + facing.getSerializedName(), "extracting=true");
        w.after(x, y, z, (level, pos) -> {
            FilteringBehaviour filter = BlockEntityBehaviour.get(level, pos, FilteringBehaviour.TYPE);
            if (filter != null) {
                filter.setFilter(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(item))));
            }
        });
    }

    private static void motor(Build w, int x, int y, int z, int rpm) {
        w.after(x, y, z, (level, pos) -> {
            if (level.getBlockEntity(pos) instanceof CreativeMotorBlockEntity motor) {
                motor.generatedSpeed.setValue(rpm);
            }
        });
    }

    /** A Blaze Burner that keeps {@code heat} forever, like one fed a Creative Blaze Cake. */
    private static void burner(Build w, int x, int y, int z, BlazeBurnerBlock.HeatLevel heat) {
        w.set(x, y, z, "create:blaze_burner");
        w.after(x, y, z, (level, pos) -> {
            level.setBlockAndUpdate(pos, level.getBlockState(pos).setValue(BlazeBurnerBlock.HEAT_LEVEL, heat));
            if (level.getBlockEntity(pos) instanceof BlazeBurnerBlockEntity burner) {
                burner.isCreative = true;
                burner.setChanged();
            }
        });
    }

    /** Re-places the burners of {@code PonderSchematics.reactionChamber} at (x, z) as creative ones. */
    private static void heatChamber(Build w, int x, int z, BlazeBurnerBlock.HeatLevel heat) {
        for (int[] cell : new int[][] {{0, 0}, {2, 0}, {1, 1}, {0, 2}, {2, 2}}) {
            burner(w, x + cell[0], 1, z + cell[1], heat);
        }
    }

    /** An Outlet Valve on the machine wall behind it, letting {@code fluid} out towards {@code out}, once the machine has formed. */
    private static void valve(Build w, int x, int y, int z, Direction out, String fluid) {
        w.set(x, y, z, CRR + "outlet_valve", "facing=" + out.getSerializedName());
        w.after(x, y, z, (level, pos) -> {
            if (level.getBlockEntity(pos) instanceof OutletValveBlockEntity valve) {
                valve.setFilter(BuiltInRegistries.FLUID.get(ResourceLocation.parse(fluid)));
            }
        });
        String label = fluid.substring(fluid.indexOf(':') + 1) + " valve";
        REPORT.put(REPORT.containsKey(label) ? label + " 2" : label, w.at(x, y, z));
    }

    private static void creativeTank(Build w, int x, int y, int z, String fluid) {
        w.set(x, y, z, "create:creative_fluid_tank");
        w.after(x, y, z, (level, pos) -> {
            if (level.getBlockEntity(pos) instanceof CreativeFluidTankBlockEntity tank
                    && tank.getTankInventory() instanceof CreativeFluidTankBlockEntity.CreativeSmartFluidTank creative) {
                creative.setContainedFluid(new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse(fluid)), 1000));
            }
        });
    }

    /**
     * A product's end of the line: a creative tank showing {@code fluid}, standing on casings down to the floor. It takes whatever is
     * pumped in and never fills, so the line upstream never backs up.
     */
    private static void sinkOnPillar(Build w, int x, int y, int z, String fluid) {
        if (y > 1) {
            w.fill(x, 1, z, x, y - 1, z, "create:andesite_casing");
        }
        creativeTank(w, x, y, z, fluid);
    }

    /** A standing sign; {@code rotation} 0 faces south, 8 north (towards the walkway). */
    private static void sign(Build w, int x, int y, int z, int rotation, String... lines) {
        w.set(x, y, z, "minecraft:oak_sign", "rotation=" + rotation);
        w.after(x, y, z, (level, pos) -> {
            if (level.getBlockEntity(pos) instanceof SignBlockEntity sign) {
                SignText text = sign.getFrontText();
                for (int i = 0; i < Math.min(4, lines.length); i++) {
                    text = text.setMessage(i, Component.literal(lines[i]));
                }
                sign.setText(text, true);
                sign.setText(text, false);
            }
        });
    }

    // ---- the report ------------------------------------------------------------------------------------------------------

    /** Logs the fluids and items each reported block holds. */
    private static void report(ServerLevel level) {
        ReactingReactions.LOGGER.info("Showcase report after {} seconds:", RUN_TICKS / 20);
        REPORT.forEach((label, pos) -> {
            StringBuilder line = new StringBuilder();
            var fluids = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, null);
            for (int i = 0; fluids != null && i < fluids.getTanks(); i++) {
                FluidStack fluid = fluids.getFluidInTank(i);
                if (!fluid.isEmpty()) {
                    line.append(' ').append(BuiltInRegistries.FLUID.getKey(fluid.getFluid()).getPath()).append('=').append(fluid.getAmount());
                }
            }
            var items = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
            for (int i = 0; items != null && i < items.getSlots(); i++) {
                ItemStack stack = items.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    line.append(' ').append(BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath()).append('x').append(stack.getCount());
                }
            }
            if (level.getBlockEntity(pos) instanceof DerrickControllerBlockEntity derrick) {
                line.append(" formed=").append(derrick.isFormed()).append(" richness=").append(derrick.getRichness()).append(" running=")
                        .append(derrick.isRunning());
            }
            if (level.getBlockEntity(pos) instanceof KineticBlockEntity kinetic) {
                line.append(" rpm=").append(kinetic.getSpeed());
            }
            ReactingReactions.LOGGER.info("  {} at {}:{}", label, pos.toShortString(), line.isEmpty() ? " empty" : line);
        });
        ReactingReactions.LOGGER.info("  hold an Acetylene Lamp or a Neon Blade from the chests to try them; worn models show on a player");
    }
}
