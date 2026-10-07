package com.koala.reactingreactions.gametest;

import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.content.airlessoven.AirlessOvenControllerBlockEntity;
import com.koala.reactingreactions.content.electrolysis.ElectrodeBlockBase;
import com.koala.reactingreactions.content.compat.DieselGeneratorsCompat;
import com.koala.reactingreactions.content.multiblock.MachineTiers;
import com.koala.reactingreactions.content.multiblock.MultiblockControllerBlockEntity;
import com.koala.reactingreactions.content.multiblock.attachment.AttachmentBlock;
import com.koala.reactingreactions.content.multiblock.attachment.MachineAttachment;
import com.koala.reactingreactions.content.multiblock.attachment.OutletValveBlock;
import com.koala.reactingreactions.content.multiblock.attachment.OutletValveBlockEntity;
import com.koala.reactingreactions.content.multiblock.MultiblockWallBlockEntity;
import com.koala.reactingreactions.registry.CRRBlocks;
import com.koala.reactingreactions.registry.CRRFluids;
import com.koala.reactingreactions.registry.CRRItems;
import com.koala.reactingreactions.registry.CRRRecipeTypes;
import com.koala.reactingreactions.content.reaction.SmallReactionChamberBlock;
import com.koala.reactingreactions.content.reaction.SmallReactionChamberBlockEntity;
import com.koala.reactingreactions.content.toxic.TankSealing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@GameTestHolder(ReactingReactions.MODID)
@PrefixGameTestTemplate(false)
public class MultiblockTests {
    private static final String EMPTY = "empty";
    // A shell from MIN, `length` long on x and 3 on y and z; the controller sits in the north face, probed walls in the south face.
    private static final BlockPos MIN = new BlockPos(2, 1, 2);
    private static final BlockPos CONTROLLER = MIN.offset(1, 1, 0);
    private static final BlockPos PROBE = MIN.offset(1, 1, 2);
    private static final BlockPos PROBE_FLOOR_ROW = MIN.offset(1, 0, 2);
    private static final BlockPos BROKEN = MIN.offset(0, 1, 1);

    private static void buildShell(GameTestHelper helper, Block wall, Block controller, int length) {
        buildShell(helper, wall, controller, length, 3);
    }

    private static void buildShell(GameTestHelper helper, Block wall, Block controller, int length, int height) {
        for (BlockPos pos : BlockPos.betweenClosed(MIN, MIN.offset(length - 1, height - 1, 2))) {
            BlockPos rel = pos.subtract(MIN);
            boolean interior = rel.getX() > 0 && rel.getX() < length - 1 && rel.getY() > 0 && rel.getY() < height - 1 && rel.getZ() == 1;
            helper.setBlock(pos, interior ? Blocks.AIR : wall);
        }
        helper.setBlock(CONTROLLER, controller);
        // A Reaction Chamber forms only with its stirring shaft in the middle of the roof.
        if (controller == CRRBlocks.REACTION_CHAMBER_CONTROLLER.get()) {
            helper.setBlock(MIN.offset(length / 2, height - 1, 1), CRRBlocks.STEEL_ENCASED_SHAFT.get().defaultBlockState()
                    .setValue(BlockStateProperties.AXIS, Direction.Axis.Y));
        }
    }

    /** Our steel versions of other mods' engine and electrical recipes replace theirs (we load after them). */
    @GameTest(template = EMPTY)
    public static void otherModsRecipesTakeSteel(GameTestHelper helper) {
        for (String id : new String[] {"createdieselgenerators:crafting/huge_diesel_engine", "electroenergetics:crafting/stator"}) {
            String mod = id.substring(0, id.indexOf(':'));
            if (!ModList.get().isLoaded(mod)) {
                continue;
            }
            var recipe = helper.getLevel().getRecipeManager().byKey(ResourceLocation.parse(id));
            helper.assertTrue(recipe.isPresent() && recipe.get().value().getIngredients().stream()
                    .anyMatch(ingredient -> ingredient.test(new ItemStack(CRRItems.STEEL_SHEET.get()))), id + " should take a steel sheet");
        }
        helper.succeed();
    }

    /** Without the shaft in its roof, a Reaction Chamber does not form, and says why. */
    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void reactionChamberNeedsItsShaft(GameTestHelper helper) {
        buildShell(helper, CRRBlocks.REACTION_CHAMBER_WALL.get(), CRRBlocks.REACTION_CHAMBER_CONTROLLER.get(), 3, 4);
        helper.setBlock(MIN.offset(1, 3, 1), CRRBlocks.REACTION_CHAMBER_WALL.get());
        helper.runAfterDelay(30, () -> {
            MultiblockControllerBlockEntity<?> controller = helper.getBlockEntity(CONTROLLER);
            helper.assertTrue(controller.getStructure() == null, "a chamber without its stirring shaft should not form");
            List<Component> tooltip = new ArrayList<>();
            controller.addToGoggleTooltip(tooltip, false);
            helper.assertTrue(tooltip.stream().anyMatch(line -> line.getString().contains("Steel Encased Shaft")), "the goggles should say what is missing");
            helper.succeed();
        });
    }

    /**
     * Forms, lets a wall reach the controller's tanks with {@code input} (a fluid its recipes take, or null for a machine taking
     * none, which must refuse it), then unforms when a wall is broken.
     */
    private static void formsAndUnforms(GameTestHelper helper, Block wall, Block controller, int length, BlockPos inputWall, @Nullable Fluid input) {
        formsAndUnforms(helper, wall, controller, length, 3, inputWall, input);
    }

    private static void formsAndUnforms(GameTestHelper helper, Block wall, Block controller, int length, int height, BlockPos inputWall,
                                        @Nullable Fluid input) {
        buildShell(helper, wall, controller, length, height);
        helper.runAfterDelay(30, () -> {
            MultiblockControllerBlockEntity<?> controllerBe = helper.getBlockEntity(CONTROLLER);
            helper.assertTrue(controllerBe.getStructure() != null, "the shell should form");
            helper.assertTrue(((MultiblockWallBlockEntity) helper.getBlockEntity(PROBE)).getController() == controllerBe, "a wall should learn its controller");
            IFluidHandler viaWall = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(inputWall), Direction.SOUTH);
            helper.assertTrue(viaWall != null, "a wall should expose the controller's fluid handler");
            int filled = viaWall.fill(new FluidStack(input == null ? Fluids.WATER : input, 500), IFluidHandler.FluidAction.EXECUTE);
            helper.assertTrue(filled == (input == null ? 0 : 500), "a fluid piped into a wall should reach the controller's tanks if a recipe takes it, got " + filled);
            helper.setBlock(BROKEN, Blocks.AIR);
            helper.runAfterDelay(30, () -> {
                helper.assertTrue(controllerBe.getStructure() == null, "breaking a wall should unform it");
                helper.succeed();
            });
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void airlessOvenForms(GameTestHelper helper) {
        formsAndUnforms(helper, CRRBlocks.AIRLESS_OVEN_WALL.get(), CRRBlocks.AIRLESS_OVEN_CONTROLLER.get(), 3, PROBE, null);
    }

    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void distillationTowerForms(GameTestHelper helper) {
        formsAndUnforms(helper, CRRBlocks.DISTILLATION_TOWER_WALL.get(), CRRBlocks.DISTILLATION_TOWER_CONTROLLER.get(), 3, PROBE_FLOOR_ROW,
                // With Diesel Generators, distillation recipes are its own tower's, so ours takes nothing.
                DieselGeneratorsCompat.isLoaded() ? null : CRRFluids.COMPRESSED_AIR.get().getSource());
    }

    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void reactionChamberForms(GameTestHelper helper) {
        formsAndUnforms(helper, CRRBlocks.REACTION_CHAMBER_WALL.get(), CRRBlocks.REACTION_CHAMBER_CONTROLLER.get(), 3, 4, PROBE, Fluids.WATER);
    }

    /** The small vat: the controller mid-side, two electrodes with their terminals in the end walls; water comes in, a missing terminal unforms it. */
    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void electrolysisVatForms(GameTestHelper helper) {
        buildShell(helper, CRRBlocks.ELECTROLYSIS_VAT_WALL.get(), CRRBlocks.ELECTROLYSIS_VAT_WALL.get(), 5, 3);
        BlockPos controllerPos = MIN.offset(2, 1, 0);
        helper.setBlock(controllerPos, CRRBlocks.ELECTROLYSIS_VAT_CONTROLLER.get());
        for (int x : new int[] {1, 3}) {
            helper.setBlock(MIN.offset(x, 1, 1), CRRBlocks.GRAPHITE_ELECTRODE.get().defaultBlockState().setValue(ElectrodeBlockBase.FACING, Direction.UP));
        }
        // The terminals go in the end walls, beside the electrodes.
        helper.setBlock(MIN.offset(0, 1, 1), CRRBlocks.ELECTROLYSIS_VAT_TERMINAL.get());
        helper.setBlock(MIN.offset(4, 1, 1), CRRBlocks.ELECTROLYSIS_VAT_TERMINAL.get());
        helper.runAfterDelay(30, () -> {
            MultiblockControllerBlockEntity<?> controller = helper.getBlockEntity(controllerPos);
            helper.assertTrue(controller.getStructure() != null, "the vat should form");
            helper.assertTrue(helper.getBlockState(PROBE).getValue(MachineTiers.PART) > 0, "a wall should show its piece");
            IFluidHandler viaWall = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(PROBE), Direction.SOUTH);
            helper.assertTrue(viaWall != null && viaWall.fill(new FluidStack(Fluids.WATER, 500), IFluidHandler.FluidAction.EXECUTE) == 500,
                    "water piped into a wall should reach the vat");
            helper.setBlock(MIN.offset(0, 1, 1), CRRBlocks.ELECTROLYSIS_VAT_WALL.get());
            helper.runAfterDelay(30, () -> {
                helper.assertTrue(controller.getStructure() == null, "with a plain wall in place of a terminal the vat should not stay formed");
                helper.succeed();
            });
        });
    }

    /** A second fluid only comes in if a recipe takes it with the first: water and hydrogen never react, water and lava do. */
    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void reactionChamberTakesOnlyCompatibleFluids(GameTestHelper helper) {
        buildShell(helper, CRRBlocks.REACTION_CHAMBER_WALL.get(), CRRBlocks.REACTION_CHAMBER_CONTROLLER.get(), 3, 4);
        helper.runAfterDelay(30, () -> {
            IFluidHandler fluids = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(PROBE), Direction.SOUTH);
            helper.assertTrue(fluids.fill(new FluidStack(Fluids.WATER, 500), IFluidHandler.FluidAction.EXECUTE) == 500, "water should come in");
            helper.assertTrue(fluids.fill(new FluidStack(CRRFluids.HYDROGEN.get().getSource(), 500), IFluidHandler.FluidAction.EXECUTE) == 0,
                    "hydrogen has no recipe with water, so it should be refused");
            helper.assertTrue(fluids.fill(new FluidStack(Fluids.LAVA, 500), IFluidHandler.FluidAction.EXECUTE) == 500, "lava reacts with water, so it should come in");
            helper.succeed();
        });
    }

    /** Formed, each shell block shows its piece of the whole chamber; a 3x3x3 is not a tier and stays plain. */
    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void reactionChamberShowsItsTierModel(GameTestHelper helper) {
        buildShell(helper, CRRBlocks.REACTION_CHAMBER_WALL.get(), CRRBlocks.REACTION_CHAMBER_CONTROLLER.get(), 3, 4);
        helper.runAfterDelay(30, () -> {
            BlockState controller = helper.getBlockState(CONTROLLER);
            // Position (1, 1, 0) of the first tier, counted from the controller's (north) side.
            helper.assertTrue(controller.getValue(MachineTiers.PART) == 1 + (1 * 3 + 0) * 3 + 1, "the controller should show its piece, got "
                    + controller.getValue(MachineTiers.PART));
            helper.assertTrue(controller.getValue(MachineTiers.FACING) == Direction.NORTH, "the machine should face the controller's side");
            helper.assertTrue(helper.getBlockState(PROBE).getValue(MachineTiers.PART) > 0, "a wall should show its piece");
            helper.setBlock(BROKEN, Blocks.AIR);
            helper.runAfterDelay(30, () -> {
                helper.assertTrue(helper.getBlockState(PROBE).getValue(MachineTiers.PART) == 0, "unformed walls should be plain again");
                helper.succeed();
            });
        });
    }

    /** Upgrades go into the machine and show on it, blocks mount outside; a small chamber takes four in all, gaskets aside. */
    @GameTest(template = EMPTY, timeoutTicks = 150)
    public static void reactionChamberAttachmentsWork(GameTestHelper helper) {
        buildShell(helper, CRRBlocks.REACTION_CHAMBER_WALL.get(), CRRBlocks.REACTION_CHAMBER_CONTROLLER.get(), 3, 4);
        helper.setBlock(MIN.offset(-1, 1, 1), CRRBlocks.EXPANSION_TANK.get().defaultBlockState().setValue(AttachmentBlock.FACING, Direction.EAST));
        helper.setBlock(MIN.offset(1, 2, -1), CRRBlocks.MACHINE_GAUGE.get().defaultBlockState().setValue(AttachmentBlock.FACING, Direction.SOUTH));
        helper.runAfterDelay(30, () -> {
            MultiblockControllerBlockEntity<?> controller = helper.getBlockEntity(CONTROLLER);
            helper.assertTrue(controller.installUpgrade(MachineAttachment.Kind.OUTLET), "an Outlet Manifold should go in");
            helper.assertTrue(controller.installUpgrade(MachineAttachment.Kind.GASKET), "a Gasket should go in");
            IFluidHandler tanks = controller.getFluidCapability();
            helper.assertTrue(tanks.getTanks() == 2 + 2, "the manifold should add a second output tank to the inputs' two, got " + tanks.getTanks());
            helper.assertTrue(tanks.getTankCapacity(0) == 8000, "the expansion tank should take each tank to 8000 mB, got " + tanks.getTankCapacity(0));
            helper.assertTrue(controller.leakTightness() == 0, "the gasket should seal the chamber");
            helper.assertTrue(controller.getAttachments().gauges().size() == 1, "the gauge should be counted");
            BlockState wall = helper.getBlockState(PROBE);
            helper.assertTrue(wall.getValue(MachineTiers.PIPED) && wall.getValue(MachineTiers.SEALED), "the walls should show the pipes and joints");
            helper.assertFalse(controller.installUpgrade(MachineAttachment.Kind.GASKET), "one gasket is enough");
            helper.assertTrue(controller.installUpgrade(MachineAttachment.Kind.OUTLET), "the gasket takes no slot: a fourth attachment fits");
            helper.assertFalse(controller.installUpgrade(MachineAttachment.Kind.OUTLET), "a fifth attachment should not fit the small chamber's four");
            helper.assertTrue(controller.removeUpgrade().is(CRRItems.OUTLET_MANIFOLD.get()), "the last upgrade should come back out");
            helper.assertTrue(controller.removeUpgrade().is(CRRItems.GASKET.get()), "then the one before it");
            helper.assertFalse(helper.getBlockState(PROBE).getValue(MachineTiers.SEALED), "without its gasket the joints should be plain again");
            helper.succeed();
        });
    }

    /** An Outlet Valve filtered to water lets the chamber's water out into a tank in front, and down a pipe into another, unpumped. */
    @GameTest(template = EMPTY, timeoutTicks = 150)
    public static void outletValveLetsOutItsFluid(GameTestHelper helper) {
        buildShell(helper, CRRBlocks.REACTION_CHAMBER_WALL.get(), CRRBlocks.REACTION_CHAMBER_CONTROLLER.get(), 3, 4);
        BlockPos valve = MIN.offset(3, 1, 1);
        helper.setBlock(valve, CRRBlocks.OUTLET_VALVE.get().defaultBlockState().setValue(OutletValveBlock.FACING, Direction.EAST));
        helper.setBlock(valve.east(), com.simibubi.create.AllBlocks.FLUID_TANK.get());
        BlockPos piped = MIN.offset(1, 1, 3);
        helper.setBlock(piped, CRRBlocks.OUTLET_VALVE.get().defaultBlockState().setValue(OutletValveBlock.FACING, Direction.SOUTH));
        helper.setBlock(piped.south(), com.simibubi.create.AllBlocks.FLUID_PIPE.get());
        helper.setBlock(piped.south(2), com.simibubi.create.AllBlocks.FLUID_PIPE.get());
        helper.setBlock(piped.south(3), com.simibubi.create.AllBlocks.FLUID_TANK.get());
        helper.runAfterDelay(30, () -> {
            MultiblockControllerBlockEntity<?> controller = helper.getBlockEntity(CONTROLLER);
            controller.getFluidCapability().fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
            OutletValveBlockEntity outlet = helper.getBlockEntity(valve);
            outlet.setFilter(Fluids.WATER);
            OutletValveBlockEntity pipedOutlet = helper.getBlockEntity(piped);
            pipedOutlet.setFilter(Fluids.WATER);
            helper.succeedWhen(() -> {
                IFluidHandler tank = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(valve.east()), Direction.WEST);
                helper.assertTrue(tank != null && tank.getFluidInTank(0).getAmount() >= 250, "the valve should have let water into the tank");
                IFluidHandler far = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(piped.south(3)), Direction.NORTH);
                helper.assertTrue(far != null && far.getFluidInTank(0).getAmount() >= 250, "the valve should push water down the pipes, unpumped");
            });
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void reactionChamberNeedsATier(GameTestHelper helper) {
        buildShell(helper, CRRBlocks.REACTION_CHAMBER_WALL.get(), CRRBlocks.REACTION_CHAMBER_CONTROLLER.get(), 3, 3);
        helper.runAfterDelay(30, () -> {
            MultiblockControllerBlockEntity<?> controller = helper.getBlockEntity(CONTROLLER);
            helper.assertTrue(controller.getStructure() == null, "a 3x3x3 chamber is not one of the sizes, so it should not form");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 1000)
    public static void airlessOvenCokesCoal(GameTestHelper helper) {
        buildShell(helper, CRRBlocks.AIRLESS_OVEN_WALL.get(), CRRBlocks.AIRLESS_OVEN_CONTROLLER.get(), 3);
        helper.runAfterDelay(30, () -> {
            var items = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(PROBE), Direction.SOUTH);
            helper.assertTrue(items != null, "a wall should expose the controller's items");
            ItemStack left = new ItemStack(Items.COAL, 2);
            for (int i = 0; i < items.getSlots() && !left.isEmpty(); i++) {
                left = items.insertItem(i, left, false);
            }
            helper.assertTrue(left.isEmpty(), "the oven should take 2 coal");
            helper.succeedWhen(() -> {
                var oven = (AirlessOvenControllerBlockEntity) helper.getBlockEntity(CONTROLLER);
                int coke = 0;
                for (int i = 0; i < oven.getItemCapability().getSlots(); i++) {
                    ItemStack stack = oven.getItemCapability().getStackInSlot(i);
                    if (stack.is(CRRItems.COAL_COKE.get())) {
                        coke += stack.getCount();
                    }
                }
                helper.assertTrue(coke == 2, "2 coal should coke into 2 coke, got " + coke);
                FluidStack naphtha = oven.getFluidCapability().drain(new FluidStack(CRRFluids.NAPHTHA.get().getSource(), 1000), IFluidHandler.FluidAction.SIMULATE);
                helper.assertTrue(naphtha.getAmount() == 50, "coking should also give 50 mB naphtha, got " + naphtha.getAmount());
            });
        });
    }

    /**
     * The Small Reaction Chamber: pipes reach it through its top half, a Gasket used on the top seals it and shows on both
     * halves, breaking the top takes the bottom too, and it runs only some reactions.
     */
    @GameTest(template = EMPTY)
    public static void smallReactionChamberWorks(GameTestHelper helper) {
        BlockPos lower = new BlockPos(2, 1, 2);
        BlockState state = CRRBlocks.SMALL_REACTION_CHAMBER.get().defaultBlockState();
        helper.setBlock(lower, state);
        helper.setBlock(lower.above(), state.setValue(SmallReactionChamberBlock.HALF, DoubleBlockHalf.UPPER));
        IFluidHandler top = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(lower.above()), Direction.UP);
        helper.assertTrue(top != null && top.fill(new FluidStack(Fluids.WATER, 500), IFluidHandler.FluidAction.EXECUTE) == 500,
                "a pipe on the top half should fill the chamber");
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        helper.assertTrue(TankSealing.seal(helper.getLevel(), helper.absolutePos(lower.above()), player, new ItemStack(CRRItems.GASKET.get()))
                == net.minecraft.world.InteractionResult.SUCCESS, "a Gasket used on the top half should go in");
        var recipes = helper.getLevel().getRecipeManager().getAllRecipesFor(CRRRecipeTypes.REACTION.get());
        helper.assertTrue(recipes.stream().anyMatch(h -> SmallReactionChamberBlockEntity.canRun(h.value()))
                && recipes.stream().anyMatch(h -> !SmallReactionChamberBlockEntity.canRun(h.value())), "it should run some reactions, not all");
        helper.succeedWhen(() -> {
            helper.assertBlockProperty(lower, MachineTiers.SEALED, true);
            helper.assertBlockProperty(lower.above(), MachineTiers.SEALED, true);
            helper.destroyBlock(lower.above());
            helper.assertBlockNotPresent(CRRBlocks.SMALL_REACTION_CHAMBER.get(), lower);
        });
    }

    /** A single-block machine has one attachment slot: of two Outlet Valves, only the first lets its fluid out. */
    @GameTest(template = EMPTY, timeoutTicks = 150)
    public static void smallMachinesTakeOneAttachment(GameTestHelper helper) {
        BlockPos lower = new BlockPos(2, 1, 2);
        BlockState state = CRRBlocks.SMALL_REACTION_CHAMBER.get().defaultBlockState();
        helper.setBlock(lower, state);
        helper.setBlock(lower.above(), state.setValue(SmallReactionChamberBlock.HALF, DoubleBlockHalf.UPPER));
        BlockPos first = lower.east();
        BlockPos second = lower.above().west();
        helper.setBlock(first, CRRBlocks.OUTLET_VALVE.get().defaultBlockState().setValue(OutletValveBlock.FACING, Direction.EAST));
        helper.setBlock(second, CRRBlocks.OUTLET_VALVE.get().defaultBlockState().setValue(OutletValveBlock.FACING, Direction.WEST));
        helper.setBlock(first.east(), com.simibubi.create.AllBlocks.FLUID_TANK.get());
        helper.setBlock(second.west(), com.simibubi.create.AllBlocks.FLUID_TANK.get());
        helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(lower), Direction.NORTH)
                .fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
        for (BlockPos valve : new BlockPos[] {first, second}) {
            ((OutletValveBlockEntity) helper.getBlockEntity(valve)).setFilter(Fluids.WATER);
        }
        helper.succeedWhen(() -> {
            IFluidHandler used = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(first.east()), Direction.WEST);
            IFluidHandler spare = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(second.west()), Direction.EAST);
            helper.assertTrue(used != null && used.getFluidInTank(0).getAmount() >= 100, "the first valve should let water out");
            helper.assertTrue(spare != null && spare.getFluidInTank(0).isEmpty(), "a second attachment should do nothing");
        });
    }

    /** Pipes fill the input tanks and hoppers the input slots. */
    @GameTest(template = EMPTY)
    public static void singleBlockMachinesTakeInputs(GameTestHelper helper) {
        BlockPos[] spots = {new BlockPos(1, 1, 1), new BlockPos(4, 1, 1)};
        Block[] machines = {CRRBlocks.FERMENTATION_BARREL.get(), CRRBlocks.SMALL_ELECTROLYSER.get()};
        for (int m = 0; m < machines.length; m++) {
            helper.setBlock(spots[m], machines[m]);
            BlockPos pos = helper.absolutePos(spots[m]);
            IFluidHandler fluids = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, pos, Direction.UP);
            helper.assertTrue(fluids != null, machines[m] + " should expose tanks");
            int filled = fluids.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
            helper.assertTrue(filled == 1000, machines[m] + " should take water, got " + filled);
            var items = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.UP);
            ItemStack left = new ItemStack(Items.SUGAR, 4);
            for (int i = 0; i < items.getSlots() && !left.isEmpty(); i++) {
                left = items.insertItem(i, left, false);
            }
            helper.assertTrue(left.isEmpty(), machines[m] + " should take sugar");
        }
        helper.succeed();
    }
}
