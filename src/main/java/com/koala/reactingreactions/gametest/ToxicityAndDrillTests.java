package com.koala.reactingreactions.gametest;

import net.minecraft.world.InteractionResult;
import com.koala.reactingreactions.content.toxic.TankSealing;
import com.koala.reactingreactions.Config;
import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.content.compat.DieselGeneratorsCompat;
import com.koala.reactingreactions.content.drill.DerrickControllerBlockEntity;
import com.koala.reactingreactions.content.drill.DrillRig;
import com.koala.reactingreactions.content.multiblock.MachineTiers;
import com.koala.reactingreactions.content.drill.RichOreVeinBlock;
import com.koala.reactingreactions.content.equipment.CreateBacktankAir;
import com.koala.reactingreactions.content.induction.InductionHeaterCoil;
import com.koala.reactingreactions.content.induction.InductionHeaterConnectorBlockEntity;
import com.koala.reactingreactions.content.induction.InductionHeaterShape;
import com.koala.reactingreactions.content.info.CompoundInfo;
import com.koala.reactingreactions.content.laser.ChaseLaserDotGoal;
import com.koala.reactingreactions.content.laser.LaserTargets;
import com.koala.reactingreactions.content.multiblock.RecipePicker;
import com.koala.reactingreactions.content.reaction.recipe.ReactionRecipe;
import com.koala.reactingreactions.content.steam.SteamTurbineBlockEntity;
import com.koala.reactingreactions.content.toxic.Contamination;
import com.koala.reactingreactions.content.toxic.Ignition;
import com.koala.reactingreactions.content.toxic.ChemicalFlaskEntity;
import com.koala.reactingreactions.content.toxic.LeakPoolEntity;
import com.koala.reactingreactions.item.ChemicalFlaskItem;
import com.koala.reactingreactions.registry.CRRDataComponents;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import com.koala.reactingreactions.content.toxic.Leaks;
import com.koala.reactingreactions.content.toxic.Scrubbers;
import com.koala.reactingreactions.content.toxic.ToxicFluid;
import com.koala.reactingreactions.content.toxic.Toxicity;
import com.koala.reactingreactions.item.FluidTankArmorItem;
import com.koala.reactingreactions.item.FluidTankHolder;
import com.koala.reactingreactions.registry.CRRBlocks;
import com.koala.reactingreactions.registry.CRREntities;
import com.koala.reactingreactions.registry.CRRFluids;
import com.koala.reactingreactions.registry.CRRFeatures;
import net.minecraft.core.registries.BuiltInRegistries;
import com.koala.reactingreactions.registry.CRRItems;
import com.koala.reactingreactions.registry.CRRRecipeTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllEnchantments;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.armor.BacktankBlockEntity;
import com.simibubi.create.content.equipment.armor.BacktankUtil;
import com.simibubi.create.content.fluids.transfer.GenericItemFilling;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

/** Game tests for the toxicity system and the drill rig. Run with {@code ./gradlew runGameTestServer}. Each uses the empty 9x6x9 template. */
@GameTestHolder(ReactingReactions.MODID)
@PrefixGameTestTemplate(false)
public class ToxicityAndDrillTests {
    private static final String EMPTY = "empty";

    @GameTest(template = EMPTY)
    public static void toxicityDataMap(GameTestHelper helper) {
        ToxicFluid acid = Toxicity.of(CRRFluids.SULFURIC_ACID.get());
        helper.assertTrue(acid.toxicity() == 8.0F && !acid.flammable(), "sulfuric acid should be toxicity 8 and not flammable");
        ToxicFluid hydrogen = Toxicity.of(CRRFluids.HYDROGEN.get());
        helper.assertTrue(hydrogen.flammable() && hydrogen.explosive(), "hydrogen should be flammable and explosive");
        helper.assertTrue(Toxicity.of(Fluids.WATER).toxicity() == 0, "water should be harmless");
        helper.assertTrue(Toxicity.isGas(CRRFluids.HYDROGEN.get()) && !Toxicity.isGas(CRRFluids.CRUDE_OIL.get().getSource()), "hydrogen is a gas, crude oil a liquid");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void gaugeIsClamped(GameTestHelper helper) {
        // A plain mock player: a mock ServerPlayer trips over other mods' join payloads in this environment.
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Toxicity.setGauge(player, 150);
        helper.assertTrue(Toxicity.gauge(player) == 100.0F, "the gauge should cap at 100");
        Toxicity.setGauge(player, -5);
        helper.assertTrue(Toxicity.gauge(player) == 0.0F, "the gauge should not go below 0");
        Toxicity.addToGauge(player, 30);
        Toxicity.addToGauge(player, -10);
        helper.assertTrue(Toxicity.gauge(player) == 20.0F, "adding then removing should net 20");
        helper.succeed();
    }

    /** A Gasket seals a Create Fluid Tank (once), a pump, a pipe and a basin, but not what cannot leak. */
    @GameTest(template = EMPTY)
    public static void gasketSealsCreateTanks(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, AllBlocks.FLUID_TANK.get());
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack gaskets = new ItemStack(CRRItems.GASKET.get(), 2);
        helper.assertTrue(TankSealing.seal(helper.getLevel(), helper.absolutePos(pos), player, gaskets) == InteractionResult.SUCCESS, "the gasket should go on");
        helper.assertTrue(TankSealing.isSealed(helper.getBlockEntity(pos)), "the tank should be sealed");
        helper.assertTrue(gaskets.getCount() == 1, "the gasket should be used up");
        helper.assertTrue(TankSealing.seal(helper.getLevel(), helper.absolutePos(pos), player, gaskets) == InteractionResult.FAIL, "a sealed tank takes no second gasket");
        BlockPos pump = new BlockPos(4, 1, 2);
        helper.setBlock(pump, AllBlocks.MECHANICAL_PUMP.get());
        helper.assertTrue(TankSealing.seal(helper.getLevel(), helper.absolutePos(pump), player, gaskets) == InteractionResult.SUCCESS, "a pump takes a gasket");
        helper.assertTrue(TankSealing.isSealed(helper.getBlockEntity(pump)), "the pump should be sealed");
        // Saved like any block entity, the flag stays.
        var tag = helper.getBlockEntity(pump).saveWithoutMetadata(helper.getLevel().registryAccess());
        helper.assertTrue(tag.getBoolean("CrrSealed"), "the pump's gasket should be saved");
        for (BlockPos other : new BlockPos[] {new BlockPos(2, 1, 4), new BlockPos(4, 1, 4)}) {
            helper.setBlock(other, other.getX() == 2 ? AllBlocks.FLUID_PIPE.get() : AllBlocks.BASIN.get());
            gaskets.setCount(1);
            helper.assertTrue(TankSealing.seal(helper.getLevel(), helper.absolutePos(other), player, gaskets) == InteractionResult.SUCCESS,
                    "a pipe and a basin take a gasket");
        }
        helper.setBlock(new BlockPos(0, 1, 0), AllBlocks.SHAFT.get());
        helper.assertTrue(TankSealing.seal(helper.getLevel(), helper.absolutePos(new BlockPos(0, 1, 0)), player, gaskets) == InteractionResult.PASS,
                "what cannot leak takes no gasket");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void drillTowerIsChecked(GameTestHelper helper) {
        BlockPos controller = new BlockPos(4, 5, 4);
        var derrick = CRRBlocks.DERRICK_BLOCK.get();
        var truss = CRRBlocks.DERRICK_TRUSS.get();
        for (int layer = 1; layer <= 3; layer++) {
            BlockPos centre = controller.below(layer);
            helper.setBlock(centre, CRRBlocks.DRILL_PIPE.get());
            for (int sx = -1; sx <= 1; sx += 2) {
                for (int sz = -1; sz <= 1; sz += 2) {
                    helper.setBlock(centre.offset(sx, 0, sz), derrick);
                }
            }
            if (layer != 2) {
                for (Direction side : Direction.Plane.HORIZONTAL) {
                    helper.setBlock(centre.relative(side), truss);
                }
            }
        }

        List<BlockPos> io = new ArrayList<>();
        helper.assertTrue(DrillRig.checkTower(helper.getLevel(), helper.absolutePos(controller), io), "a complete tower should be recognised");
        helper.assertTrue(io.size() == 12, "12 derrick blocks should carry the I/O, got " + io.size());
        DrillRig.setLook(helper.getLevel(), helper.absolutePos(controller), true);
        // The bottom north-west corner is the tower's first piece.
        helper.assertTrue(helper.getBlockState(controller.offset(-1, -3, -1)).getValue(MachineTiers.PART) == 1, "a formed tower's blocks should show their pieces");
        helper.setBlock(controller.below(3).north(), Blocks.AIR);
        helper.assertFalse(DrillRig.checkTower(helper.getLevel(), helper.absolutePos(controller), new ArrayList<>()), "a missing truss should break it");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void richVeinDataMapLoads(GameTestHelper helper) {
        var asurine = BuiltInRegistries.BLOCK.get(ResourceLocation.parse("create:asurine")).builtInRegistryHolder().getData(CRRFeatures.RICH_VEIN);
        helper.assertTrue(asurine != null && asurine.rich() == CRRBlocks.RICH_ASURINE_VEIN.get(), "asurine should hide Rich Asurine Veins");
        var oil = CRRBlocks.OIL_SHALE.get().builtInRegistryHolder().getData(CRRFeatures.RICH_VEIN);
        helper.assertTrue(oil != null && oil.minVeinSize() == 20 && oil.chance() == 1.0F, "oil shale should keep its own vein size and chance");
        for (int i = 0; i < 200; i++) {
            int richness = asurine.rollRichness(helper.getLevel().random);
            helper.assertTrue(richness >= 1 && richness <= 5, "richness out of range: " + richness);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void richnessScale(GameTestHelper helper) {
        var state = CRRBlocks.RICH_ASURINE_VEIN.get().defaultBlockState();
        helper.assertTrue(RichOreVeinBlock.richnessOf(state.setValue(RichOreVeinBlock.RICHNESS, 1)) == 8, "level 1 is 8 points");
        helper.assertTrue(RichOreVeinBlock.richnessOf(state.setValue(RichOreVeinBlock.RICHNESS, 4)) >= 30, "level 4 is a full-rate deposit");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void drillRatesMatch(GameTestHelper helper) {
        // One dust must take as long as 500 mB of oil at the same speed.
        double oilSeconds = 500.0 / DerrickControllerBlockEntity.oilMbPerSecond(32);
        helper.assertTrue(Math.abs(oilSeconds - DerrickControllerBlockEntity.secondsPerItem(32)) < 0.01, "a dust should cost as long as 500 mB of oil");
        helper.assertTrue(DerrickControllerBlockEntity.secondsPerItem(128) < DerrickControllerBlockEntity.secondsPerItem(32), "more RPM should be faster");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void roomCheckNeedsRoofAndFloor(GameTestHelper helper) {
        BlockPos point = new BlockPos(4, 2, 4);
        helper.setBlock(point.above(2), Blocks.IRON_BLOCK);
        helper.assertFalse(Scrubbers.isInRoom(helper.getLevel(), helper.absolutePos(point)), "a roof alone is not a room");
        helper.setBlock(point.below(2), Blocks.IRON_BLOCK);
        helper.assertTrue(Scrubbers.isInRoom(helper.getLevel(), helper.absolutePos(point)), "an airtight roof and floor make a room");
        helper.setBlock(point.above(2), Blocks.GLASS);
        helper.assertFalse(Scrubbers.isInRoom(helper.getLevel(), helper.absolutePos(point)), "plain glass is not airtight");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void contaminationStopsAtAirtightWalls(GameTestHelper helper) {
        Contamination.clearAll();
        helper.setBlock(new BlockPos(3, 1, 4), Blocks.IRON_BLOCK);
        Contamination.add(helper.getLevel(), helper.absolutePos(new BlockPos(2, 1, 4)), 5.0F);
        helper.assertTrue(Contamination.levelAt(helper.getLevel(), helper.absolutePos(new BlockPos(1, 1, 4))) > 0, "open air next to the source is contaminated");
        helper.assertTrue(Contamination.levelAt(helper.getLevel(), helper.absolutePos(new BlockPos(4, 1, 4))) == 0, "the far side of an airtight wall stays clean");
        Contamination.clearAll();
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void torchIgnites(GameTestHelper helper) {
        AABB near = new AABB(helper.absolutePos(new BlockPos(3, 1, 4)));
        helper.assertFalse(Ignition.exposed(helper.getLevel(), near), "nothing is burning yet");
        helper.setBlock(new BlockPos(4, 1, 4), Blocks.TORCH);
        helper.assertTrue(Ignition.exposed(helper.getLevel(), near), "a torch next to it should ignite flammables");
        helper.succeed();
    }

    /** Drops a flask of crude oil from {@code at} onto a stone platform on layer 1 (the empty template has no floor of its own). */
    private static ChemicalFlaskEntity dropFlask(GameTestHelper helper, ItemEntry<ChemicalFlaskItem> item, BlockPos at) {
        for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(1, 1, 1), new BlockPos(7, 1, 7))) {
            helper.setBlock(pos, Blocks.STONE);
        }
        ItemStack flask = new ItemStack(item.get());
        flask.set(CRRDataComponents.FLUID_TANK.get(), SimpleFluidContent.copyOf(new FluidStack(CRRFluids.CRUDE_OIL.get().getSource(), ChemicalFlaskItem.AMOUNT_MB)));
        BlockPos abs = helper.absolutePos(at);
        ChemicalFlaskEntity entity = new ChemicalFlaskEntity(helper.getLevel(), abs.getX() + 0.5, abs.getY() + 0.5, abs.getZ() + 0.5, flask);
        entity.setDeltaMovement(0, -0.5, 0);
        helper.getLevel().addFreshEntity(entity);
        return entity;
    }

    @GameTest(template = EMPTY)
    public static void chemicalFlaskSpillsAPool(GameTestHelper helper) {
        dropFlask(helper, CRRItems.CHEMICAL_FLASK, new BlockPos(4, 4, 4));
        helper.succeedWhen(() -> {
            var pools = helper.getEntities(CRREntities.LEAK_POOL.get());
            helper.assertTrue(pools.size() == 1, "one pool should form, got " + pools.size());
            helper.assertTrue(pools.getFirst().getAmount() == ChemicalFlaskItem.AMOUNT_MB, "it should hold the flask's 500 mB, got " + pools.getFirst().getAmount());
        });
    }

    @GameTest(template = EMPTY)
    public static void blastFlaskGoesUp(GameTestHelper helper) {
        ChemicalFlaskEntity flask = dropFlask(helper, CRRItems.BLAST_FLASK, new BlockPos(4, 4, 4));
        // Fire on a bare floor soon burns out, so this passes as soon as some is seen.
        helper.succeedWhen(() -> {
            helper.assertTrue(flask.isRemoved(), "the flask should have broken");
            helper.assertTrue(helper.getEntities(CRREntities.LEAK_POOL.get()).isEmpty(), "a blast flask should burn, not pool");
            boolean fire = BlockPos.betweenClosedStream(helper.absolutePos(new BlockPos(0, 1, 0)), helper.absolutePos(new BlockPos(8, 4, 8)))
                    .anyMatch(pos -> helper.getLevel().getBlockState(pos).is(Blocks.FIRE));
            helper.assertTrue(fire, "burning crude oil should leave fires");
        });
    }

    @GameTest(template = EMPTY)
    public static void poolsGrowAndVanish(GameTestHelper helper) {
        LeakPoolEntity pool = helper.spawn(CRREntities.LEAK_POOL.get(), new BlockPos(4, 1, 4));
        pool.setContents(CRRFluids.CRUDE_OIL.get().getSource(), 100);
        pool.addAmount(5000);
        helper.assertTrue(pool.getAmount() == LeakPoolEntity.MAX_AMOUNT, "a pool is capped");
        pool.addAmount(-5000);
        helper.assertTrue(pool.isRemoved(), "an emptied pool disappears");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void scrubbersCoverNearbyLeaks(GameTestHelper helper) {
        BlockPos scrubber = helper.absolutePos(new BlockPos(2, 2, 2));
        Scrubbers.setActive(helper.getLevel(), scrubber, true);
        helper.assertTrue(Scrubbers.covers(helper.getLevel(), helper.absolutePos(new BlockPos(4, 2, 2))), "a working scrubber covers nearby leaks");
        Scrubbers.setActive(helper.getLevel(), scrubber, false);
        helper.assertFalse(Scrubbers.covers(helper.getLevel(), helper.absolutePos(new BlockPos(4, 2, 2))), "a stopped scrubber covers nothing");
        helper.succeed();
    }

    /** The per-tick cost the leak mixins add to every pipe and tank must stay negligible (a million gates well under a second). */
    @GameTest(template = EMPTY)
    public static void leakGateIsCheap(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(4, 1, 4));
        long start = System.nanoTime();
        int passes = 0;
        for (int i = 0; i < 1_000_000; i++) {
            if (Config.toxicityEnabled() && Leaks.shouldCheck(helper.getLevel(), pos)) {
                passes++;
            }
        }
        long millis = (System.nanoTime() - start) / 1_000_000;
        helper.assertTrue(millis < 500, "a million leak gates took " + millis + " ms");
        helper.assertTrue(passes >= 0, "unreachable");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void inductionHeaterShape(GameTestHelper helper) {
        var plate = CRRBlocks.INDUCTION_HEATER_PLATE.get();
        var connector = CRRBlocks.INDUCTION_HEATER_CONNECTOR.get();
        for (int x = 1; x <= 4; x++) {
            for (int z = 1; z <= 3; z++) {
                helper.setBlock(new BlockPos(x, 1, z), plate);
            }
        }
        helper.assertTrue(InductionHeaterShape.scan(helper.getLevel(), helper.absolutePos(new BlockPos(1, 1, 1))) == null, "no connector, no heater");
        helper.setBlock(new BlockPos(3, 1, 2), connector);
        var shape = InductionHeaterShape.scan(helper.getLevel(), helper.absolutePos(new BlockPos(3, 1, 2)));
        helper.assertTrue(shape != null && shape.count() == 12 && shape.sizeX() == 4 && shape.sizeZ() == 3, "a filled 4x3 with one connector is a heater");
        helper.setBlock(new BlockPos(4, 1, 3), Blocks.AIR);
        helper.assertTrue(InductionHeaterShape.scan(helper.getLevel(), helper.absolutePos(new BlockPos(3, 1, 2))) == null, "a hole breaks the rectangle");
        // Voltages are read off the class's own derived constants (see its doc), not hard-coded, so this
        // doesn't go stale when the resistive-heater balance formula they come from changes.
        double smould = InductionHeaterConnectorBlockEntity.SMOULDERING_VOLTS;
        double kindled = InductionHeaterConnectorBlockEntity.KINDLED_VOLTS;
        double seething = InductionHeaterConnectorBlockEntity.SEETHING_VOLTS;
        helper.assertTrue(InductionHeaterConnectorBlockEntity.heatFor(smould, 9) == BlazeBurnerBlock.HeatLevel.SMOULDERING, "smouldering voltage smoulders");
        helper.assertTrue(InductionHeaterConnectorBlockEntity.heatFor(kindled, 9) == BlazeBurnerBlock.HeatLevel.KINDLED, "kindled voltage kindles");
        helper.assertTrue(InductionHeaterConnectorBlockEntity.heatFor(seething, 9) == BlazeBurnerBlock.HeatLevel.SEETHING, "seething voltage superheats");
        // A bigger heater needs proportionally more voltage: the kindled voltage for 9 blocks does not kindle 18.
        helper.assertTrue(InductionHeaterConnectorBlockEntity.heatFor(kindled, 18) == BlazeBurnerBlock.HeatLevel.SMOULDERING, "18 blocks need twice the voltage");
        helper.assertTrue(InductionHeaterConnectorBlockEntity.heatFor(kindled * 2, 18) == BlazeBurnerBlock.HeatLevel.KINDLED, "twice the voltage kindles 18 blocks");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void inductionCoilLoops(GameTestHelper helper) {
        // A 5x4 heater (x 0-4, z 0-3): the corner joins east and south; the next loop inside is the 3x2 block of cells.
        helper.assertTrue(InductionHeaterCoil.connects(0, 0, Direction.EAST, 0, 4, 0, 3), "the corner continues east");
        helper.assertTrue(InductionHeaterCoil.connects(0, 0, Direction.SOUTH, 0, 4, 0, 3), "the corner continues south");
        helper.assertFalse(InductionHeaterCoil.connects(0, 0, Direction.NORTH, 0, 4, 0, 3), "nothing north of the rectangle");
        helper.assertFalse(InductionHeaterCoil.connects(1, 0, Direction.SOUTH, 0, 4, 0, 3), "the outer loop does not join the loop inside");
        helper.assertTrue(InductionHeaterCoil.connects(1, 1, Direction.EAST, 0, 4, 0, 3), "the inner loop runs along");
        helper.assertTrue(InductionHeaterCoil.loop(2, 1, 0, 4, 0, 3) == 1, "the middle is loop 1");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void inductionCoilCircles(GameTestHelper helper) {
        helper.assertTrue(InductionHeaterCoil.isCircle(1, 1, 0, 2, 0, 2), "the middle of a 3x3 is a circle");
        helper.assertFalse(InductionHeaterCoil.isCircle(0, 0, 0, 2, 0, 2), "the edge of a 3x3 is loop segments");
        helper.assertTrue(InductionHeaterCoil.isCircle(1, 1, 0, 2, 0, 3) && InductionHeaterCoil.isCircle(1, 2, 0, 2, 0, 3),
                "both middle blocks of a 3x4 are circles");
        helper.assertFalse(InductionHeaterCoil.isCircle(1, 1, 0, 3, 0, 3), "the inside of a 4x4 is a ring");
        helper.assertFalse(InductionHeaterCoil.connects(1, 1, Direction.SOUTH, 0, 2, 0, 3), "circles have no segments");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void hotCoilsScald(GameTestHelper helper) {
        var plate = CRRBlocks.INDUCTION_HEATER_PLATE.get();
        helper.setBlock(new BlockPos(4, 1, 4), plate.defaultBlockState().setValue(BlazeBurnerBlock.HEAT_LEVEL,
                BlazeBurnerBlock.HeatLevel.SEETHING));
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(4, 2, 4));
        float before = pig.getHealth();
        helper.getLevel().getBlockState(helper.absolutePos(new BlockPos(4, 1, 4))).getBlock().stepOn(helper.getLevel(), helper.absolutePos(new BlockPos(4, 1, 4)),
                helper.getLevel().getBlockState(helper.absolutePos(new BlockPos(4, 1, 4))), pig);
        helper.assertTrue(pig.getHealth() < before, "a superheated coil should hurt what stands on it");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void fluidTankItemsOnlyAcceptTheirOwnFluid(GameTestHelper helper) {
        var chestplate = new ItemStack(CRRItems.EXO_CHESTPLATE.get());
        var handler = chestplate.getCapability(Capabilities.FluidHandler.ITEM);
        helper.assertTrue(handler != null, "a fluid-tank item should expose the capability");
        var water = new FluidStack(Fluids.WATER, 500);
        helper.assertTrue(handler.fill(water, IFluidHandler.FluidAction.EXECUTE) == 0, "water should not fill an Aerozine tank");
        var aerozine = new FluidStack(CRRFluids.AEROZINE.get().getSource(), 500);
        int filled = handler.fill(aerozine, IFluidHandler.FluidAction.EXECUTE);
        helper.assertTrue(filled == 500, "the full 500 mB of Aerozine should fit");
        helper.assertTrue(FluidTankHolder.contents(chestplate).getAmount() == 500, "the tank component should reflect the fill");
        helper.assertTrue(handler.drain(500, IFluidHandler.FluidAction.EXECUTE).isEmpty(), "the capability itself should never let this drain back out");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void createBacktankTakesBreathableGas(GameTestHelper helper) {
        var backtank = new ItemStack(AllItems.COPPER_BACKTANK.get());
        var handler = backtank.getCapability(Capabilities.FluidHandler.ITEM);
        helper.assertTrue(handler != null, "Create's backtank should take gas through a fluid capability");
        var water = new FluidStack(Fluids.WATER, 500);
        helper.assertTrue(handler.fill(water, IFluidHandler.FluidAction.EXECUTE) == 0, "water is not breathable");
        var oxygen = new FluidStack(CRRFluids.OXYGEN.get().getSource(), 500);
        helper.assertTrue(handler.fill(oxygen, IFluidHandler.FluidAction.EXECUTE) == 500, "oxygen should fill the backtank");
        helper.assertTrue(BacktankUtil.getAir(backtank) == 500 / CreateBacktankAir.MB_PER_AIR,
                "the fill should show up as backtank air");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void spoutsFillTankItems(GameTestHelper helper) {
        // Create's Spout fills any item through GenericItemFilling: this is how the aerozine gear and backtanks get filled.
        var level = helper.getLevel();
        var aerozine = new FluidStack(CRRFluids.AEROZINE.get().getSource(), 1000);
        var thrusters = new ItemStack(CRRItems.AEROZINE_THRUSTERS.get());
        helper.assertTrue(GenericItemFilling.canItemBeFilled(level, thrusters), "a spout should accept the thrusters");
        int amount = GenericItemFilling.getRequiredAmountForItem(level, thrusters, aerozine);
        helper.assertTrue(amount == 1000, "the spout should pour its whole 1000 mB of aerozine");
        var filled = GenericItemFilling.fillItem(level, amount, thrusters, aerozine.copy());
        helper.assertTrue(FluidTankHolder.contents(filled).getAmount() == 1000, "the filled thrusters should hold the aerozine");
        var water = new FluidStack(Fluids.WATER, 1000);
        helper.assertTrue(GenericItemFilling.getRequiredAmountForItem(level,
                new ItemStack(CRRItems.AEROZINE_THRUSTERS.get()), water) < 0, "water should not be spouted into them");
        var backtank = new ItemStack(AllItems.COPPER_BACKTANK.get());
        var oxygen = new FluidStack(CRRFluids.OXYGEN.get().getSource(), 1000);
        helper.assertTrue(GenericItemFilling.getRequiredAmountForItem(level, backtank, oxygen) == 1000, "a spout should pour oxygen into a backtank");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void fluidTankBarAndTooltipReflectFillLevel(GameTestHelper helper) {
        var chestplate = new ItemStack(CRRItems.EXO_CHESTPLATE.get());
        var item = (FluidTankArmorItem) chestplate.getItem();
        int capacity = item.tankCapacityMb(chestplate);
        helper.assertTrue(item.isBarVisible(chestplate), "the bar should show even when empty");
        helper.assertTrue(item.getBarWidth(chestplate) == 0, "an empty tank should show an empty bar");
        var aerozine = new FluidStack(CRRFluids.AEROZINE.get().getSource(), capacity);
        FluidTankHolder.setContents(chestplate, aerozine);
        helper.assertTrue(item.getBarWidth(chestplate) == 13, "a full tank should show a full 13-pixel bar");
        List<Component> tooltip = new ArrayList<>();
        item.appendHoverText(chestplate, Item.TooltipContext.EMPTY, tooltip, TooltipFlag.NORMAL);
        boolean hasAmount = tooltip.stream().anyMatch(c -> c.getString().contains(capacity + " / " + capacity));
        helper.assertTrue(hasAmount, "the tooltip should show the exact amount");
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void steamTurbineSpinsFromSteamAlone(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos, CRRBlocks.STEAM_TURBINE.get());
        var fluidCap = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(pos), null);
        helper.assertTrue(fluidCap != null, "the turbine should expose a fluid handler");
        var water = new FluidStack(Fluids.WATER, 1000);
        helper.assertTrue(fluidCap.fill(water, IFluidHandler.FluidAction.EXECUTE) == 0,
                "the turbine should refuse anything that isn't steam");
        var be = (SteamTurbineBlockEntity) helper.getBlockEntity(pos);
        helper.assertTrue(be.getSpeed() == 0, "an empty turbine should not be spinning");
        var steam = new FluidStack(CRRFluids.STEAM.get().getSource(), 4000);
        helper.assertTrue(fluidCap.fill(steam, IFluidHandler.FluidAction.EXECUTE) == 4000,
                "the turbine should accept steam");
        helper.runAfterDelay(5, () -> {
            helper.assertTrue(be.getSpeed() != 0, "a full tank of steam should spin the turbine on its own, with no boiler or heat involved");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void laserTargetsTracksFreshestPoint(GameTestHelper helper) {
        var shooter = UUID.randomUUID();
        var level = (ServerLevel) helper.getLevel();
        helper.assertTrue(LaserTargets.nearestTo(level, Vec3.ZERO, 100) == null,
                "nothing should be tracked before an update");
        var pos = new Vec3(5, 5, 5);
        LaserTargets.update(shooter, level, pos);
        var nearest = LaserTargets.nearestTo(level, Vec3.ZERO, 100);
        helper.assertTrue(pos.equals(nearest), "the just-updated point should be findable");
        helper.assertTrue(LaserTargets.nearestTo(level, Vec3.ZERO, 1) == null,
                "a point outside the search radius should not be found");
        LaserTargets.clear(shooter);
        helper.assertTrue(LaserTargets.nearestTo(level, Vec3.ZERO, 100) == null,
                "clearing a shooter should stop tracking it");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void catsAndWolvesGetTheChaseLaserGoal(GameTestHelper helper) {
        var cat = helper.spawn(EntityType.CAT, new BlockPos(4, 2, 4));
        var wolf = helper.spawn(EntityType.WOLF, new BlockPos(5, 2, 4));
        helper.assertTrue(cat.goalSelector.getAvailableGoals().stream()
                        .anyMatch(wrapped -> wrapped.getGoal() instanceof ChaseLaserDotGoal),
                "a cat joining the level should get the chase-laser goal");
        helper.assertTrue(wolf.goalSelector.getAvailableGoals().stream()
                        .anyMatch(wrapped -> wrapped.getGoal() instanceof ChaseLaserDotGoal),
                "a wolf joining the level should get the chase-laser goal");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void recipePickerPrefersRunnableAndSpecific(GameTestHelper helper) {
        // Water plus calcium carbide matches both "steam" (heated) and "acetylene" (unheated). Unheated at 16 RPM, only acetylene can run.
        var water = new FluidStack(Fluids.WATER, 1000);
        var carbide = new ItemStack(CRRItems.CALCIUM_CARBIDE.get());
        Predicate<ReactionRecipe> matches = r ->
                r.getFluidIngredients().stream().allMatch(f -> f.test(water) && f.amount() <= water.getAmount())
                        && r.getIngredients().stream().allMatch(i -> i.test(carbide));
        var picked = RecipePicker.pick(helper.getLevel(),
                CRRRecipeTypes.REACTION.get(), matches,
                r -> r.acceptsSpeed(16) && r.getRequiredHeat() == HeatCondition.NONE);
        helper.assertTrue(picked != null && !picked.getFluidResults().isEmpty()
                && picked.getFluidResults().get(0).getFluid().isSame(CRRFluids.ACETYLENE.get().getSource()),
                "the runnable, more specific acetylene recipe should win over steam");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void placedBacktankFeedsPipesToTheMillibucket(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos, AllBlocks.COPPER_BACKTANK.get());
        var be = (BacktankBlockEntity) helper.getBlockEntity(pos);
        be.setAirLevel(100);
        var handler = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
                helper.absolutePos(pos), Direction.NORTH);
        helper.assertTrue(handler != null, "a placed backtank should expose a fluid handler to pipes");
        var sim = IFluidHandler.FluidAction.SIMULATE;
        var exec = IFluidHandler.FluidAction.EXECUTE;
        // Create's pipes find the fluid with a 1 mB probe.
        var probe = handler.drain(1, sim);
        helper.assertTrue(probe.getAmount() == 1 && probe.getFluid().isSame(CRRFluids.COMPRESSED_AIR.get().getSource()),
                "a 1 mB probe should see compressed air");
        helper.assertTrue(handler.drain(25, exec).getAmount() == 25, "small drains should come out whole");
        helper.assertTrue(handler.getFluidInTank(0).getAmount() == 975, "the tank should hold exactly what is left");
        helper.assertTrue(be.getAirLevel() == 98, "air should only drop per whole unit drained");
        var oxygen = new FluidStack(CRRFluids.OXYGEN.get().getSource(), 5);
        helper.assertTrue(handler.fill(oxygen, exec) == 5 && handler.getFluidInTank(0).getAmount() == 980, "small fills should add up too");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void superheatedBrineDistillationIsPreferred(GameTestHelper helper) {
        var water = new FluidStack(Fluids.WATER, 1000);
        var burner = BlazeBurnerBlock.HeatLevel.SEETHING;
        var picked = RecipePicker.pick(helper.getLevel(),
                CRRRecipeTypes.DISTILLATION.get(),
                r -> r.getIngredients().isEmpty() && r.getFluidIngredients().stream().allMatch(f -> f.test(water) && f.amount() <= water.getAmount()),
                r -> r.getRequiredHeat().testBlazeBurner(burner),
                r -> r.getRequiredHeat().ordinal());
        if (DieselGeneratorsCompat.isLoaded()) {
            // With Create Diesel Generators, brine runs on its tower, which also prefers the hottest recipe.
            helper.assertTrue(picked == null, "our tower should be inert while CDG is installed");
        } else {
            helper.assertTrue(picked != null && picked.getRequiredHeat() == HeatCondition.SUPERHEATED
                    && picked.getProcessingDuration() == 40, "a superheated tower should run the fast brine recipe");
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void gasDiffuserTakesHelium(GameTestHelper helper) {
        // Looked up by id: the block only exists with Aeronautics.
        var diffuser = BuiltInRegistries.BLOCK.getOptional(ReactingReactions.asResource("gas_diffuser"));
        if (diffuser.isEmpty()) {
            helper.succeed();
            return;
        }
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos, diffuser.get());
        IFluidHandler tank = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(pos), Direction.UP);
        helper.assertTrue(tank != null && tank.fill(new FluidStack(CRRFluids.HELIUM.get().getSource(), 1000), IFluidHandler.FluidAction.EXECUTE) == 1000,
                "a placed Gas Diffuser should take helium");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void chaseGearSkipsEnchantmentsItHas(GameTestHelper helper) {
        var enchantments = helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        ItemStack multitool = new ItemStack(CRRItems.PLASMA_MULTITOOL.get());
        for (var redundant : List.of(Enchantments.FORTUNE, Enchantments.SILK_TOUCH, Enchantments.UNBREAKING, Enchantments.MENDING)) {
            helper.assertFalse(multitool.supportsEnchantment(enchantments.getHolderOrThrow(redundant)), "the multitool already has " + redundant.location());
        }
        helper.assertTrue(multitool.supportsEnchantment(enchantments.getHolderOrThrow(Enchantments.EFFICIENCY)), "the multitool still takes Efficiency");
        ItemStack boots = new ItemStack(CRRItems.EXO_BOOTS.get());
        helper.assertFalse(boots.supportsEnchantment(enchantments.getHolderOrThrow(Enchantments.FEATHER_FALLING)), "the boots already cushion falls");
        helper.assertTrue(boots.supportsEnchantment(enchantments.getHolderOrThrow(Enchantments.PROTECTION)), "the boots still take Protection");
        var capacity = enchantments.getHolderOrThrow(AllEnchantments.CAPACITY);
        helper.assertTrue(new ItemStack(CRRItems.ACETYLENE_LAMP.get()).isPrimaryItemFor(capacity) && multitool.isPrimaryItemFor(capacity),
                "tank items take Capacity from a table");
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void gasVentReleasesGasAndPollutes(GameTestHelper helper) {
        BlockPos vent = new BlockPos(2, 2, 2);
        helper.setBlock(vent, CRRBlocks.GAS_VENT.get());
        IFluidHandler tank = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(vent), Direction.DOWN);
        helper.assertTrue(tank.fill(new FluidStack(Fluids.WATER, 100), IFluidHandler.FluidAction.EXECUTE) == 0, "a vent refuses liquids");
        helper.assertTrue(tank.fill(new FluidStack(CRRFluids.CONTAMINATED_METHANE.get().getSource(), 400), IFluidHandler.FluidAction.EXECUTE) == 400,
                "a vent takes gases");
        for (Direction side : new Direction[] {Direction.UP, Direction.NORTH}) {
            helper.assertTrue(helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(vent), side) == null,
                    "gas only goes in at the back, not at " + side);
        }
        helper.succeedWhen(() -> {
            helper.assertTrue(tank.getFluidInTank(0).isEmpty(), "the gas should be let out");
            helper.assertTrue(Contamination.levelAt(helper.getLevel(), helper.absolutePos(vent.above())) > 0, "toxic gas pollutes the air at the outlet");
        });
    }

    /** A vent sitting on a tank lets out what is over a bucket under full, without a pump, and leaves the rest. */
    @GameTest(template = EMPTY)
    public static void gasVentTakesATanksExcess(GameTestHelper helper) {
        BlockPos tankPos = new BlockPos(2, 1, 2);
        helper.setBlock(tankPos, AllBlocks.FLUID_TANK.get());
        helper.setBlock(tankPos.above(), CRRBlocks.GAS_VENT.get());
        IFluidHandler tank = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(tankPos), Direction.UP);
        int capacity = tank.getTankCapacity(0);
        tank.fill(new FluidStack(CRRFluids.HYDROGEN.get().getSource(), capacity), IFluidHandler.FluidAction.EXECUTE);
        helper.succeedWhen(() -> helper.assertTrue(tank.getFluidInTank(0).getAmount() == capacity - 1000,
                "the tank should be left a bucket under full, not " + tank.getFluidInTank(0).getAmount()));
    }

    @GameTest(template = EMPTY)
    public static void derrickTakesCoolantAndLubricantByPipe(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos, CRRBlocks.DERRICK_CONTROLLER.get());
        IFluidHandler tanks = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(pos), null);
        // The lubricant tank comes first and refuses coolant: the coolant must still find its own tank.
        helper.assertTrue(tanks.fill(new FluidStack(CRRFluids.COOLANT.get().getSource(), 100), IFluidHandler.FluidAction.EXECUTE) == 100,
                "coolant piped into a Derrick should be taken");
        helper.assertTrue(tanks.fill(new FluidStack(CRRFluids.MINERAL_OIL.get().getSource(), 100), IFluidHandler.FluidAction.EXECUTE) == 100,
                "and lubricant beside it");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void dieselGeneratorsCrudeOilIsFractionated(GameTestHelper helper) {
        if (!DieselGeneratorsCompat.isLoaded()) {
            helper.succeed();
            return;
        }
        // With CDG installed its Pumpjack is the only source of crude oil, so its tower has to take CDG's.
        var crude = new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.fromNamespaceAndPath("createdieselgenerators", "crude_oil")), 1000);
        var recipe = helper.getLevel().getRecipeManager().byKey(ReactingReactions.asResource("distillation_recipe/crude_oil_fractionation_cdg"));
        helper.assertTrue(recipe.isPresent() && recipe.get().value() instanceof ProcessingRecipe<?, ?> processing
                && processing.getFluidIngredients().getFirst().test(crude), "CDG's crude oil should be fractionated");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void iodineCuresAndWearsDown(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(2, 2, 2));
        pig.addEffect(new MobEffectInstance(MobEffects.POISON, 200));
        var spray = new ItemStack(CRRItems.IODINE_SPRAY.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, spray);
        spray.getItem().interactLivingEntity(spray, player, pig, InteractionHand.MAIN_HAND);
        helper.assertTrue(!pig.hasEffect(MobEffects.POISON), "the spray should cure the pig's poison");
        helper.assertTrue(spray.getDamageValue() == 1 && spray.getMaxDamage() == 10, "one of the spray's 10 uses should be spent");
        var healthy = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(4, 2, 4));
        player.getCooldowns().removeCooldown(spray.getItem());
        spray.getItem().interactLivingEntity(spray, player, healthy, InteractionHand.MAIN_HAND);
        helper.assertTrue(spray.getDamageValue() == 1, "nothing to cure should cost no use");
        var tablets = new ItemStack(CRRItems.IODINE_TABLETS.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, tablets);
        Toxicity.setGauge(player, 50);
        player.startUsingItem(InteractionHand.MAIN_HAND);
        var left = tablets.getItem().finishUsingItem(tablets, helper.getLevel(), player);
        helper.assertTrue(Toxicity.gauge(player) == 30, "a tablet should lower the gauge by 20");
        helper.assertTrue(left.getCount() == 1 && left.getDamageValue() == 1, "the pack should lose one use, not disappear");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void iodineSprayCanCureZombieVillagers(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE_VILLAGER, new BlockPos(2, 2, 2));
        var hand = InteractionHand.MAIN_HAND;
        int tries = 0;
        // 10% a try: 200 tries all failing is about a one in a billion chance.
        while (!zombie.isConverting() && tries < 200) {
            var spray = new ItemStack(CRRItems.IODINE_SPRAY.get());
            player.setItemInHand(hand, spray);
            spray.getItem().interactLivingEntity(spray, player, zombie, hand);
            helper.assertTrue(spray.getDamageValue() == 1, "every try should spend a use");
            tries++;
        }
        helper.assertTrue(zombie.isConverting(), "some spray should start the cure");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void formulasCoverOtherModsCompounds(GameTestHelper helper) {
        Function<Item, String> formula = item -> {
            var entry = CompoundInfo.findForItem(new ItemStack(item));
            return entry == null ? null : entry.formula();
        };
        helper.assertTrue("Fe".equals(formula.apply(Items.IRON_INGOT)), "vanilla iron should be Fe");
        helper.assertTrue("H2O".equals(formula.apply(Items.WATER_BUCKET)), "a water bucket should share water's formula");
        helper.assertTrue(formula.apply(Items.OAK_LOG) == null, "raw biological materials like wood get no formula");
        helper.assertTrue("~SrAl2O4:Eu,Dy".equals(formula.apply(Items.GLOWSTONE_DUST)), "glowstone should be the rare-earth phosphor");
        helper.assertTrue("CuZn".equals(formula.apply(AllItems.BRASS_INGOT.get())), "Create brass should be CuZn");
        helper.assertTrue("NaCl".equals(formula.apply(CRRItems.SALT.get())), "this mod's own rows still apply");
        var water = CompoundInfo.find(ResourceLocation.withDefaultNamespace("water"));
        helper.assertTrue(water != null && "H2O".equals(water.formula()), "the water fluid should have a formula too");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void commonTagsReachOtherMods(GameTestHelper helper) {
        BiPredicate<Item, String> in = (item, tag) -> new ItemStack(item)
                .is(TagKey.create(Registries.ITEM, ResourceLocation.parse(tag)));
        helper.assertTrue(in.test(CRRItems.STEEL_INGOT.get(), "c:ingots/steel"), "steel should be a c:ingots/steel");
        helper.assertTrue(in.test(CRRItems.STEEL_INGOT.get(), "c:ingots"), "and a c:ingots");
        helper.assertTrue(in.test(CRRItems.LEAD_NUGGET.get(), "c:nuggets/lead"), "lead nuggets should be tagged");
        helper.assertTrue(in.test(CRRItems.SULFUR_DUST.get(), "c:dusts/sulfur"), "sulfur should be a c:dusts/sulfur");
        var hydrogen = new FluidStack(CRRFluids.HYDROGEN.get().getSource(), 1);
        helper.assertTrue(hydrogen.is(TagKey.create(Registries.FLUID, ResourceLocation.parse("c:hydrogen"))), "hydrogen should be c:hydrogen");
        helper.succeed();
    }
}
