package com.koala.reactingreactions.registry;

import com.koala.reactingreactions.Config;
import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.content.airlessoven.AirlessOvenControllerBlockEntity;
import com.koala.reactingreactions.content.compat.ElectroEnergeticsCompat;
import com.koala.reactingreactions.content.distillation.DistillationTowerControllerBlockEntity;
import com.koala.reactingreactions.content.drill.DerrickBlock;
import com.koala.reactingreactions.content.drill.DerrickControllerBlock;
import com.koala.reactingreactions.content.drill.DerrickTrussBlock;
import com.koala.reactingreactions.content.drill.DrillPipeBlock;
import com.koala.reactingreactions.content.drill.MineralDrillHeadBlock;
import com.koala.reactingreactions.content.drill.OilDrillHeadBlock;
import com.koala.reactingreactions.content.drill.RichOreVeinBlock;
import com.koala.reactingreactions.content.electrolysis.ElectrodeBlock;
import com.koala.reactingreactions.content.electrolysis.ElectrodeBlockBase;
import com.koala.reactingreactions.content.electrolysis.ElectrolysisVatControllerBlock;
import com.koala.reactingreactions.content.electrolysis.ElectrolysisVatControllerBlockBase;
import com.koala.reactingreactions.content.electrolysis.ElectrolysisVatControllerBlockEntity;
import com.koala.reactingreactions.content.electrolysis.ElectrolysisVatTerminalBlock;
import com.koala.reactingreactions.content.electrolysis.ElectrolysisVatTerminalBlockBase;
import com.koala.reactingreactions.content.electrolysis.SmallElectrolyserBlock;
import com.koala.reactingreactions.content.electrolysis.SmallElectrolyserBlockBase;
import com.koala.reactingreactions.content.equipment.charging.ChargingPadBlock;
import com.koala.reactingreactions.content.explosive.AnfoChargeBlock;
import com.koala.reactingreactions.content.fluids.PlasticPipeBlock;
import com.koala.reactingreactions.content.fluids.PlasticPipeModel;
import com.koala.reactingreactions.content.induction.InductionHeaterConnectorBlock;
import com.koala.reactingreactions.content.induction.InductionHeaterConnectorBlockBase;
import com.koala.reactingreactions.content.induction.InductionHeaterPlateBlock;
import com.koala.reactingreactions.content.multiblock.MultiblockConnectedTextures;
import com.koala.reactingreactions.content.multiblock.MultiblockPartBlock;
import com.koala.reactingreactions.content.multiblock.MultiblockWallBlockEntity;
import com.koala.reactingreactions.content.multiblock.attachment.AttachmentBlock;
import com.koala.reactingreactions.content.multiblock.attachment.CirculationPumpBlock;
import com.koala.reactingreactions.content.multiblock.attachment.GaugeBlock;
import com.koala.reactingreactions.content.multiblock.attachment.MachineAttachment;
import com.koala.reactingreactions.content.reaction.FermentationBarrelBlock;
import com.koala.reactingreactions.content.reaction.ReactionChamberControllerBlockEntity;
import com.koala.reactingreactions.content.steam.SteamTurbineBlock;
import com.koala.reactingreactions.content.steel.SteelEncasedShaftBlock;
import com.koala.reactingreactions.content.toxic.AtmosphericScrubberBlock;
import com.koala.reactingreactions.content.toxic.FloorDrainBlock;
import com.koala.reactingreactions.content.toxic.GasVentBlock;
import com.koala.reactingreactions.datagen.CRRBlockModels;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.decoration.encasing.CasingBlock;
import com.simibubi.create.content.decoration.encasing.EncasedCTBehaviour;
import com.simibubi.create.content.decoration.encasing.EncasingRegistry;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedShaftBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.BuilderTransformers;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.ModelGen;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.model.generators.ModelFile;

import java.util.List;
import java.util.function.Supplier;

public class CRRBlocks {
    private static final CRRRegistrate REGISTRATE = CRRRegistrate.REGISTRATE;

    // ---- building blocks ----

    public static final BlockEntry<Block> REINFORCED_GLASS = REGISTRATE.block("reinforced_glass", Block::new)
            .transform(TagGen.pickaxeOnly())
            .properties(p -> Properties.of()
                    .mapColor(MapColor.NONE)
                    .strength(0.5F, 1200.0F)
                    .noOcclusion()
                    .isValidSpawn((state, level, pos, type) -> false)
                    .isRedstoneConductor((state, level, pos) -> false)
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)
                    .sound(SoundType.GLASS))
            .simpleItem()
            .register();

    public static final BlockEntry<Block> VARNISHED_PLANKS = REGISTRATE.block("varnished_planks", Block::new)
            .transform(TagGen.axeOnly())
            .properties(p -> Properties.of().mapColor(MapColor.WOOD).strength(2.0F, 3.0F).sound(SoundType.WOOD))
            .simpleItem()
            .register();

    public static final BlockEntry<Block> POLYMETALLIC_NODULE = REGISTRATE.block("polymetallic_nodule", Block::new)
            .tag(BlockTags.MINEABLE_WITH_SHOVEL)
            .properties(p -> Properties.of().mapColor(MapColor.STONE).strength(0.6F).sound(SoundType.GRAVEL))
            .simpleItem()
            .register();

    public static final BlockEntry<Block> OIL_SHALE = REGISTRATE.block("oil_shale", Block::new)
            .transform(TagGen.pickaxeOnly())
            .properties(p -> Properties.of().mapColor(MapColor.DEEPSLATE).strength(3.0F, 3.0F).requiresCorrectToolForDrops().sound(SoundType.STONE))
            .simpleItem()
            .register();

    public static final BlockEntry<Block> NEON_LAMP = REGISTRATE.block("neon_lamp", Block::new)
            .transform(TagGen.pickaxeOnly())
            .properties(p -> Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(0.5F, 1200.0F).lightLevel(state -> 15).sound(SoundType.GLASS))
            .simpleItem()
            .register();

    private static final int ANFO_TINT = 0xC8584A;

    public static final BlockEntry<AnfoChargeBlock> ANFO_CHARGE = REGISTRATE.block("anfo_charge", AnfoChargeBlock::new)
            .transform(TagGen.axeOnly())
            .properties(p -> Properties.of().mapColor(MapColor.COLOR_RED).strength(0.5F).sound(SoundType.WOOD))
            .lang("ANFO Charge")
            .transform(tinted(CRRBlockModels.tintedCube(vanilla("barrel_side"), vanilla("tnt_top"), vanilla("barrel_bottom"), false), ANFO_TINT))
            .register();

    private static final CTSpriteShiftEntry STEEL_CASING_SHIFT = CTSpriteShifter.getCT(AllCTTypes.OMNIDIRECTIONAL,
            ReactingReactions.asResource("block/steel_casing"),
            ReactingReactions.asResource("block/steel_casing_connected"));

    public static final BlockEntry<CasingBlock> STEEL_CASING = REGISTRATE.block("steel_casing", CasingBlock::new)
            .transform(BuilderTransformers.casing(() -> STEEL_CASING_SHIFT))
            .register();

    // ---- equipment ----

    public static final BlockEntry<ChargingPadBlock> CHARGING_PAD = REGISTRATE.block("charging_pad", ChargingPadBlock::new)
            .transform(metal())
            .properties(Properties::noOcclusion)
            .blockstate(CRRBlockModels.of(CRRBlockModels.plate(modLoc("charging_pad"), ChargingPadBlock.HEIGHT_PX)))
            .simpleItem()
            .register();

    // ---- kinetics ----

    public static final double STIRRING_STRESS_IMPACT = 8.0;

    public static final BlockEntry<SteelEncasedShaftBlock> STEEL_ENCASED_SHAFT = REGISTRATE.block("steel_encased_shaft", SteelEncasedShaftBlock::new)
            .initialProperties(SharedProperties::stone)
            .transform(TagGen.pickaxeOnly())
            .properties(Properties::noOcclusion)
            // Right-clicking a Create shaft with a Steel Casing encases it.
            .transform(EncasingRegistry.addVariantTo(() -> AllBlocks.SHAFT.get()))
            .onRegister(block -> BlockStressValues.IMPACTS.register(block, () -> STIRRING_STRESS_IMPACT))
            .loot((p, lb) -> p.dropOther(lb, AllBlocks.SHAFT.get()))
            .onRegister(CreateRegistrate.connectedTextures(() -> new EncasedCTBehaviour(STEEL_CASING_SHIFT)))
            .onRegister(CreateRegistrate.casingConnectivity(
                    (block, cc) -> cc.make(block, STEEL_CASING_SHIFT, (s, f) -> f.getAxis() != s.getValue(EncasedShaftBlock.AXIS))))
            .blockstate((c, p) -> {
                ModelFile model = p.models().getBuilder("block/encased_shaft/block_steel")
                        .parent(new ModelFile.UncheckedModelFile(ResourceLocation.fromNamespaceAndPath("create", "block/encased_shaft/block")))
                        .texture("casing", p.modLoc("block/steel_casing"))
                        .texture("opening", ResourceLocation.fromNamespaceAndPath("create", "block/gearbox"));
                BlockStateGen.axisBlock(c, p, state -> model, true);
            })
            .item()
            .model((c, p) -> p.getBuilder(c.getName())
                    .parent(new ModelFile.UncheckedModelFile(ResourceLocation.fromNamespaceAndPath("create", "block/encased_shaft/item")))
                    .texture("casing", p.modLoc("block/steel_casing"))
                    .texture("opening", ResourceLocation.fromNamespaceAndPath("create", "block/gearbox")))
            .build()
            .register();

    public static final BlockEntry<SteamTurbineBlock> STEAM_TURBINE = REGISTRATE.block("steam_turbine", SteamTurbineBlock::new)
            .transform(metal())
            .properties(Properties::noOcclusion)
            .blockstate((ctx, p) -> p.horizontalBlock(ctx.get(),
                    CRRBlockModels.frontBox(modLoc("steam_turbine_side"), modLoc("steam_turbine_front"), modLoc("steam_turbine_top")).make(ctx.getName(), p)))
            .simpleItem()
            .onRegister(block -> BlockStressValues.CAPACITIES.register(block, () -> Config.number(Config.STEAM_TURBINE_STRESS_CAPACITY_SU, 512)))
            .register();

    // ---- electrolysis ----

    // Fixed-size: wall and controller blockstates, with the formed pieces, come from tools/machine_models.py.
    public static final BlockEntry<MultiblockPartBlock.Tiered<MultiblockWallBlockEntity>> ELECTROLYSIS_VAT_WALL = REGISTRATE
            .block("electrolysis_vat_wall", p -> new MultiblockPartBlock.Tiered<>(p, MultiblockWallBlockEntity.class,
                    () -> CRRBlockEntities.MULTIBLOCK_WALL.get(), () -> ElectrolysisVatControllerBlockEntity.SPEC))
            .transform(metal())
            .properties(Properties::noOcclusion)
            .blockstate(CRRBlockModels.tieredShell("electrolysis_vat_wall", "electrolysis_vat_wall", true))
            .addLayer(() -> RenderType::cutoutMipped)
            .color(() -> () -> (state, level, pos, tintIndex) -> 0x5C6168)
            .onRegister(connected(CRRBlocks::electrolysisVat, "electrolysis_vat_wall", "electrolysis_vat_controller"))
            .simpleItem()
            .register();

    public static final BlockEntry<ElectrolysisVatControllerBlockBase> ELECTROLYSIS_VAT_CONTROLLER = CRRBlocks.<ElectrolysisVatControllerBlockBase>eeBlock(
                    "electrolysis_vat_controller", () -> ElectrolysisVatControllerBlock::new, ElectrolysisVatControllerBlockBase::new)
            .transform(metal())
            .properties(Properties::noOcclusion)
            .lang("Electrolysis Vat")
            .onRegister(connected(CRRBlocks::electrolysisVat, "electrolysis_vat_wall", "electrolysis_vat_controller"))
            .blockstate(CRRBlockModels.tieredShell("electrolysis_vat_controller", "electrolysis_vat_controller", false))
            .simpleItem()
            .register();

    // In an end wall beside an electrode column; the wire attaches at its junction box. Blockstate from tools/machine_models.py.
    public static final BlockEntry<ElectrolysisVatTerminalBlockBase> ELECTROLYSIS_VAT_TERMINAL = CRRBlocks.<ElectrolysisVatTerminalBlockBase>eeBlock(
                    "electrolysis_vat_terminal", () -> ElectrolysisVatTerminalBlock::new, ElectrolysisVatTerminalBlockBase::new)
            .transform(metal())
            .properties(Properties::noOcclusion)
            .blockstate(CRRBlockModels.tieredShell("electrolysis_vat_wall", "electrolysis_vat_wall", true))
            .addLayer(() -> RenderType::cutoutMipped)
            .color(() -> () -> (state, level, pos, tintIndex) -> 0x5C6168)
            .simpleItem()
            .register();

    private static List<Block> electrolysisVat() {
        return List.of(ELECTROLYSIS_VAT_WALL.get(), ELECTROLYSIS_VAT_CONTROLLER.get(), ELECTROLYSIS_VAT_TERMINAL.get());
    }

    public static final BlockEntry<ElectrodeBlockBase> GRAPHITE_ELECTRODE = electrode("graphite_electrode", () -> Blocks.COAL_BLOCK).register();
    public static final BlockEntry<ElectrodeBlockBase> GOLD_STEEL_ELECTRODE = electrode("gold_steel_electrode", () -> Blocks.GOLD_BLOCK)
            .lang("Gold-Steel Electrode").register();
    public static final BlockEntry<ElectrodeBlockBase> LEAD_ELECTRODE = electrode("lead_electrode", () -> Blocks.IRON_BLOCK).register();

    private static BlockBuilder<ElectrodeBlockBase, CreateRegistrate> electrode(String name, Supplier<Block> initial) {
        return CRRBlocks.<ElectrodeBlockBase>eeBlock(name, () -> ElectrodeBlock::new, ElectrodeBlockBase::new)
                .initialProperties(initial::get)
                .transform(TagGen.pickaxeOnly())
                .properties(p -> p.noOcclusion().noCollission().instabreak())
                .blockstate(CRRBlockModels.electrode(modLoc("electrolysis_vat_wall")))
                .simpleItem();
    }

    // Single-block electrolyser for the any-electrode recipes.
    public static final BlockEntry<SmallElectrolyserBlockBase> SMALL_ELECTROLYSER = CRRBlocks.<SmallElectrolyserBlockBase>eeBlock(
                    "small_electrolyser", () -> SmallElectrolyserBlock::new, SmallElectrolyserBlockBase::new)
            .transform(metal())
            .properties(Properties::noOcclusion)
            .blockstate(CRRBlockModels.of(CRRBlockModels.smallElectrolyser(modLoc("steel_casing"), modLoc("small_electrolyser"), modLoc("gold_steel_electrode"))))
            .simpleItem()
            .register();

    // ---- distillation, oven and reactors ----

    public static final BlockEntry<MultiblockPartBlock.Windowed<MultiblockWallBlockEntity>> DISTILLATION_TOWER_WALL = REGISTRATE
            .block("distillation_tower_wall", p -> new MultiblockPartBlock.Windowed<>(p, MultiblockWallBlockEntity.class,
                    () -> CRRBlockEntities.MULTIBLOCK_WALL.get(), () -> DistillationTowerControllerBlockEntity.SPEC))
            .transform(copper())
            .transform(windowedWall("distillation_tower_wall", "distillation_tower_wall_end", 0xD9925F))
            .onRegister(connected(CRRBlocks::distillationTower, "distillation_tower_wall", "distillation_tower_wall_end", "distillation_tower_controller"))
            .simpleItem()
            .register();

    public static final BlockEntry<MultiblockPartBlock.Windowed<DistillationTowerControllerBlockEntity>> DISTILLATION_TOWER_CONTROLLER = REGISTRATE
            .block("distillation_tower_controller", p -> new MultiblockPartBlock.Windowed<>(p, DistillationTowerControllerBlockEntity.class,
                    () -> CRRBlockEntities.DISTILLATION_TOWER_CONTROLLER.get(), null))
            .transform(copper())
            .properties(Properties::noOcclusion)
            .lang("Distillation Tower")
            .blockstate(CRRBlockModels.shellBlock("distillation_tower_controller", "distillation_tower_wall_end"))
            .onRegister(connected(CRRBlocks::distillationTower, "distillation_tower_wall", "distillation_tower_wall_end", "distillation_tower_controller"))
            .simpleItem()
            .register();

    private static List<Block> distillationTower() {
        return List.of(DISTILLATION_TOWER_WALL.get(), DISTILLATION_TOWER_CONTROLLER.get());
    }

    // Fixed-size: their blockstates, with the formed pieces, come from tools/machine_models.py.
    public static final BlockEntry<MultiblockPartBlock.Tiered<MultiblockWallBlockEntity>> AIRLESS_OVEN_WALL = REGISTRATE
            .block("airless_oven_wall", p -> new MultiblockPartBlock.Tiered<>(p, MultiblockWallBlockEntity.class,
                    () -> CRRBlockEntities.MULTIBLOCK_WALL.get(), () -> AirlessOvenControllerBlockEntity.SPEC))
            .transform(copper())
            .properties(Properties::noOcclusion)
            .lang("Airless Oven Brick")
            .blockstate(CRRBlockModels.tieredShell("airless_oven_wall", "airless_oven_wall", false))
            .onRegister(connected(CRRBlocks::airlessOven, "airless_oven_wall", "airless_oven_controller", "airless_oven_controller_top"))
            .simpleItem()
            .register();

    public static final BlockEntry<MultiblockPartBlock.Tiered<AirlessOvenControllerBlockEntity>> AIRLESS_OVEN_CONTROLLER = REGISTRATE
            .block("airless_oven_controller", p -> new MultiblockPartBlock.Tiered<>(p, AirlessOvenControllerBlockEntity.class,
                    () -> CRRBlockEntities.AIRLESS_OVEN_CONTROLLER.get(), null))
            .transform(copper())
            .properties(Properties::noOcclusion)
            .lang("Airless Oven")
            .blockstate(CRRBlockModels.tieredShell("airless_oven_controller", "airless_oven_controller_top", false))
            .onRegister(connected(CRRBlocks::airlessOven, "airless_oven_wall", "airless_oven_controller", "airless_oven_controller_top"))
            .simpleItem()
            .register();

    private static List<Block> airlessOven() {
        return List.of(AIRLESS_OVEN_WALL.get(), AIRLESS_OVEN_CONTROLLER.get());
    }

    // Fixed-size: their blockstates, with the formed pieces, come from tools/machine_models.py.
    public static final BlockEntry<MultiblockPartBlock.Tiered<MultiblockWallBlockEntity>> REACTION_CHAMBER_WALL = REGISTRATE
            .block("reaction_chamber_wall", p -> new MultiblockPartBlock.Tiered<>(p, MultiblockWallBlockEntity.class,
                    () -> CRRBlockEntities.MULTIBLOCK_WALL.get(), () -> ReactionChamberControllerBlockEntity.SPEC))
            .transform(copper())
            .properties(Properties::noOcclusion)
            .blockstate(CRRBlockModels.tieredShell("reaction_chamber_wall", "reaction_chamber_wall", true))
            .addLayer(() -> RenderType::cutoutMipped)
            .color(() -> () -> (state, level, pos, tintIndex) -> 0xC8D4E0)
            .onRegister(connected(CRRBlocks::reactionChamber, "reaction_chamber_wall", "reaction_chamber_controller", "reaction_chamber_controller_top"))
            .simpleItem()
            .register();

    public static final BlockEntry<MultiblockPartBlock.Tiered<ReactionChamberControllerBlockEntity>> REACTION_CHAMBER_CONTROLLER = REGISTRATE
            .block("reaction_chamber_controller", p -> new MultiblockPartBlock.Tiered<>(p, ReactionChamberControllerBlockEntity.class,
                    () -> CRRBlockEntities.REACTION_CHAMBER_CONTROLLER.get(), null))
            .transform(copper())
            .properties(Properties::noOcclusion)
            .lang("Reaction Chamber")
            .blockstate(CRRBlockModels.tieredShell("reaction_chamber_controller", "reaction_chamber_controller_top", false))
            .onRegister(connected(CRRBlocks::reactionChamber, "reaction_chamber_wall", "reaction_chamber_controller", "reaction_chamber_controller_top"))
            .simpleItem()
            .register();

    // ---- machine attachments: mounted against a formed machine's wall; blockstates and models from tools/machine_models.py ----

    public static final BlockEntry<AttachmentBlock> EXPANSION_TANK = attachment("expansion_tank", MachineAttachment.Kind.EXPANSION_TANK,
            Block.box(2, 0, 2, 14, 16, 16)).register();
    public static final BlockEntry<GaugeBlock> MACHINE_GAUGE = REGISTRATE.block("machine_gauge", p -> new GaugeBlock(p, Block.box(3, 3, 8, 13, 13, 16)))
            .transform(copper())
            .properties(Properties::noOcclusion)
            .blockstate((ctx, p) -> { })
            .simpleItem()
            .register();
    private static final double CIRCULATION_PUMP_STRESS_IMPACT = 4.0;
    public static final BlockEntry<CirculationPumpBlock> CIRCULATION_PUMP = REGISTRATE.block("circulation_pump", CirculationPumpBlock::new)
            .transform(copper())
            .properties(Properties::noOcclusion)
            .blockstate((ctx, p) -> { })
            .onRegister(block -> BlockStressValues.IMPACTS.register(block, () -> CIRCULATION_PUMP_STRESS_IMPACT))
            .simpleItem()
            .register();

    private static BlockBuilder<AttachmentBlock, CreateRegistrate> attachment(String name, MachineAttachment.Kind kind, VoxelShape shape) {
        return REGISTRATE.block(name, p -> new AttachmentBlock(p, kind, shape))
                .transform(copper())
                .properties(Properties::noOcclusion)
                .blockstate((ctx, p) -> { })
                .simpleItem();
    }

    // A Create fluid pipe of HDPE that never leaks: Create's models and blockstate, retextured (PlasticPipeModel).
    public static final BlockEntry<PlasticPipeBlock> PLASTIC_PIPE = REGISTRATE.block("plastic_pipe", PlasticPipeBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .properties(p -> p.forceSolidOff().sound(SoundType.WOOD))
            .transform(TagGen.pickaxeOnly())
            .blockstate(BlockStateGen.pipe())
            .onRegister(CreateRegistrate.blockModel(() -> PlasticPipeModel::wrap))
            .item()
            .transform(ModelGen.customItemModel())
            .register();

    private static List<Block> reactionChamber() {
        return List.of(REACTION_CHAMBER_WALL.get(), REACTION_CHAMBER_CONTROLLER.get());
    }

    private static final int BARREL_TINT = 0xB4C6D8;

    public static final BlockEntry<FermentationBarrelBlock> FERMENTATION_BARREL = REGISTRATE.block("fermentation_barrel", FermentationBarrelBlock::new)
            .initialProperties(() -> Blocks.BARREL)
            .transform(TagGen.axeOnly())
            .properties(p -> p.sound(SoundType.WOOD))
            .transform(tinted(CRRBlockModels.tintedCube(vanilla("barrel_side"), vanilla("barrel_top"), vanilla("barrel_bottom"), true), BARREL_TINT))
            .register();

    // ---- derrick ----

    private static final double DERRICK_STRESS_IMPACT = 16.0;

    // Part of the formed tower: their blockstates, with the formed pieces, come from tools/machine_models.py.
    public static final BlockEntry<DerrickBlock> DERRICK_BLOCK = REGISTRATE.block("derrick_block", DerrickBlock::new)
            .transform(metal())
            .properties(Properties::noOcclusion)
            .blockstate(CRRBlockModels.modelOnly((name, p) -> p.models().cubeAll(name, p.modLoc("block/derrick_wall"))))
            .simpleItem()
            .register();

    public static final BlockEntry<DerrickTrussBlock> DERRICK_TRUSS = REGISTRATE.block("derrick_truss", DerrickTrussBlock::new)
            .transform(metal())
            .properties(Properties::noOcclusion)
            .addLayer(() -> RenderType::cutoutMipped)
            .blockstate(CRRBlockModels.modelOnly(CRRBlockModels.boxes(modLoc("derrick_truss"), new int[][] {
                    // posts
                    {0, 0, 0, 2, 16, 2}, {14, 0, 0, 16, 16, 2}, {0, 0, 14, 2, 16, 16}, {14, 0, 14, 16, 16, 16},
                    // bottom rails
                    {2, 0, 0, 14, 2, 2}, {2, 0, 14, 14, 2, 16}, {0, 0, 2, 2, 2, 14}, {14, 0, 2, 16, 2, 14},
                    // top rails
                    {2, 14, 0, 14, 16, 2}, {2, 14, 14, 14, 16, 16}, {0, 14, 2, 2, 16, 14}, {14, 14, 2, 16, 16, 14}})))
            .simpleItem()
            .register();

    public static final BlockEntry<DerrickControllerBlock> DERRICK_CONTROLLER = REGISTRATE.block("derrick_controller", DerrickControllerBlock::new)
            .transform(metal())
            .properties(Properties::noOcclusion)
            .lang("Derrick")
            // A top drive, its model from tools/machine_models.py.
            .blockstate((ctx, p) -> p.horizontalBlock(ctx.get(), p.models().getExistingFile(modLoc("derrick_drive"))))
            .onRegister(block -> BlockStressValues.IMPACTS.register(block, () -> DERRICK_STRESS_IMPACT))
            .item()
            .model((ctx, p) -> p.withExistingParent(ctx.getName(), modLoc("derrick_drive")))
            .build()
            .register();

    // Corner posts and rings, open in the middle so the spinning rod shows.
    public static final BlockEntry<DrillPipeBlock> DRILL_PIPE = REGISTRATE.block("drill_pipe", DrillPipeBlock::new)
            .transform(metal())
            .properties(Properties::noOcclusion)
            .blockstate(CRRBlockModels.of(CRRBlockModels.boxes(modLoc("derrick_pipe"), new int[][] {
                    {4, 0, 4, 6, 16, 6}, {10, 0, 4, 12, 16, 6}, {4, 0, 10, 6, 16, 12}, {10, 0, 10, 12, 16, 12},
                    {6, 0, 4, 10, 2, 6}, {6, 0, 10, 10, 2, 12}, {4, 0, 6, 6, 2, 10}, {10, 0, 6, 12, 2, 10},
                    {6, 14, 4, 10, 16, 6}, {6, 14, 10, 10, 16, 12}, {4, 14, 6, 6, 16, 10}, {10, 14, 6, 12, 16, 10}})))
            .simpleItem()
            .register();

    // Steel reaches tuff, scoria, granite and diorite; titanium the richer Create rocks; diamond every vein, twice as fast.
    public static final BlockEntry<MineralDrillHeadBlock> MINERAL_DRILL_HEAD_STEEL =
            drillHead("mineral_drill_head_steel", "Mineral Drill Head (Steel)", MineralDrillHeadBlock::new, CRRBlockModels.drillBit(modLoc("derrick_wall"), modLoc("drill_bit_steel")));
    public static final BlockEntry<MineralDrillHeadBlock> MINERAL_DRILL_HEAD_TITANIUM =
            drillHead("mineral_drill_head_titanium", "Mineral Drill Head (Titanium)", MineralDrillHeadBlock::new, CRRBlockModels.drillBit(modLoc("derrick_wall"), modLoc("drill_bit_titanium")));
    public static final BlockEntry<MineralDrillHeadBlock> MINERAL_DRILL_HEAD_DIAMOND =
            drillHead("mineral_drill_head_diamond", "Mineral Drill Head (Diamond)", MineralDrillHeadBlock::new, CRRBlockModels.drillBit(modLoc("derrick_wall"), vanilla("diamond_block")));
    public static final BlockEntry<OilDrillHeadBlock> OIL_DRILL_HEAD =
            drillHead("oil_drill_head", "Oil Drill Head", OilDrillHeadBlock::new,
                    CRRBlockModels.rollerBit(modLoc("derrick_pipe"), modLoc("derrick_wall"), modLoc("drill_bit_steel")));

    private static <T extends Block> BlockEntry<T> drillHead(String name, String lang, NonNullFunction<Properties, T> factory, CRRBlockModels.ModelMaker model) {
        return REGISTRATE.block(name, factory)
                .transform(metal())
                .properties(Properties::noOcclusion)
                .lang(lang)
                .blockstate(CRRBlockModels.of(model))
                .simpleItem()
                .register();
    }

    // Generated only inside the natural rock of the same kind; breaking one drops a stack of that rock.
    public static final BlockEntry<RichOreVeinBlock> RICH_ASURINE_VEIN = richVein("asurine", createRock("palettes/stone_types/natural/asurine_0"), rock("create", "asurine"));
    public static final BlockEntry<RichOreVeinBlock> RICH_CRIMSITE_VEIN = richVein("crimsite", createRock("palettes/stone_types/natural/crimsite_0"), rock("create", "crimsite"));
    public static final BlockEntry<RichOreVeinBlock> RICH_OCHRUM_VEIN = richVein("ochrum", createRock("palettes/stone_types/natural/ochrum_0"), rock("create", "ochrum"));
    public static final BlockEntry<RichOreVeinBlock> RICH_VERIDIUM_VEIN = richVein("veridium", createRock("palettes/stone_types/natural/veridium_0"), rock("create", "veridium"));
    public static final BlockEntry<RichOreVeinBlock> RICH_SCORIA_VEIN = richVein("scoria", createRock("palettes/stone_types/scoria"), rock("create", "scoria"));
    public static final BlockEntry<RichOreVeinBlock> RICH_TUFF_VEIN = richVein("tuff", vanilla("tuff"), rock("minecraft", "tuff"));
    public static final BlockEntry<RichOreVeinBlock> RICH_GRANITE_VEIN = richVein("granite", vanilla("granite"), rock("minecraft", "granite"));
    public static final BlockEntry<RichOreVeinBlock> RICH_DIORITE_VEIN = richVein("diorite", vanilla("diorite"), rock("minecraft", "diorite"));
    public static final BlockEntry<RichOreVeinBlock> RICH_OIL_VEIN = richVein("oil", modLoc("oil_shale"), () -> OIL_SHALE.get().asItem());

    private static final int RICH_TINT_MID = 0xFFC000;

    private static BlockEntry<RichOreVeinBlock> richVein(String rock, ResourceLocation texture, ItemLike drop) {
        return REGISTRATE.block("rich_" + rock + "_vein", RichOreVeinBlock::new)
                .properties(p -> Properties.of()
                        .mapColor(MapColor.STONE)
                        .strength(50.0F, 1200.0F)
                        .requiresCorrectToolForDrops()
                        .lightLevel(state -> 6)
                        .sound(SoundType.STONE))
                .addLayer(() -> RenderType::cutout)
                .blockstate(CRRBlockModels.of(CRRBlockModels.richVein(texture, modLoc("rich_vein_flecks"))))
                .color(() -> () -> (state, level, pos, tintIndex) -> richTint(state))
                .item().color(() -> () -> (stack, tintIndex) -> RICH_TINT_MID).build()
                .loot((p, lb) -> p.add(lb, LootTable.lootTable().withPool(LootPool.lootPool()
                        .add(LootItem.lootTableItem(drop).apply(SetItemCountFunction.setCount(ConstantValue.exactly(64.0F)))))))
                .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .register();
    }

    /** The flecks go from pale gold for a poor deposit to deep orange-gold for a rich one. */
    private static int richTint(BlockState state) {
        if (!state.hasProperty(RichOreVeinBlock.RICHNESS)) {
            return RICH_TINT_MID;
        }
        float t = (state.getValue(RichOreVeinBlock.RICHNESS) - RichOreVeinBlock.MIN_LEVEL) / (float) (RichOreVeinBlock.MAX_LEVEL - RichOreVeinBlock.MIN_LEVEL);
        int green = Math.round(0xE8 + (0xA0 - 0xE8) * t);
        int blue = Math.round(0x70 + (0x00 - 0x70) * t);
        return (0xFF << 16) | (green << 8) | blue;
    }

    private static ResourceLocation createRock(String path) {
        return ResourceLocation.fromNamespaceAndPath("create", "block/" + path);
    }

    private static ItemLike rock(String namespace, String path) {
        return () -> BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    // ---- toxic handling ----

    private static final double SCRUBBER_STRESS_IMPACT = 16.0;

    public static final BlockEntry<AtmosphericScrubberBlock> ATMOSPHERIC_SCRUBBER = REGISTRATE.block("atmospheric_scrubber", AtmosphericScrubberBlock::new)
            .transform(metal())
            .properties(Properties::noOcclusion)
            .blockstate((ctx, p) -> p.horizontalBlock(ctx.get(),
                    CRRBlockModels.scrubber(modLoc("scrubber_side"), modLoc("scrubber_front")).make(ctx.getName(), p)))
            .onRegister(block -> BlockStressValues.IMPACTS.register(block, () -> SCRUBBER_STRESS_IMPACT))
            .simpleItem()
            .register();

    public static final BlockEntry<FloorDrainBlock> FLOOR_DRAIN = REGISTRATE.block("floor_drain", FloorDrainBlock::new)
            .transform(metal())
            .properties(Properties::noOcclusion)
            .blockstate(CRRBlockModels.halfPlate(modLoc("floor_drain"), 3))
            .simpleItem()
            .register();

    public static final BlockEntry<GasVentBlock> GAS_VENT = REGISTRATE.block("gas_vent", GasVentBlock::new)
            .transform(metal())
            .properties(Properties::noOcclusion)
            // A vent stack, its model from tools/machine_models.py.
            .blockstate(CRRBlockModels.gasVent())
            .item()
            .model((ctx, p) -> p.withExistingParent(ctx.getName(), modLoc("gas_vent_stack")))
            .build()
            .register();

    // ---- induction heater ----

    public static final BlockEntry<InductionHeaterPlateBlock> INDUCTION_HEATER_PLATE = REGISTRATE.block("induction_heater_plate", InductionHeaterPlateBlock::new)
            .transform(inductionHeater(false))
            .register();

    public static final BlockEntry<InductionHeaterConnectorBlockBase> INDUCTION_HEATER_CONNECTOR = CRRBlocks.<InductionHeaterConnectorBlockBase>eeBlock(
                    "induction_heater_connector", () -> InductionHeaterConnectorBlock::new, InductionHeaterConnectorBlockBase::new)
            .transform(inductionHeater(true))
            .properties(Properties::noOcclusion)
            .register();

    private static <T extends Block> NonNullUnaryOperator<BlockBuilder<T, CreateRegistrate>> inductionHeater(boolean connector) {
        return b -> b.transform(metal())
                .properties(p -> p.lightLevel(CRRBlocks::inductionLight))
                .addLayer(() -> RenderType::cutout)
                .blockstate(CRRBlockModels.inductionHeater(connector))
                .color(() -> () -> (state, level, pos, tintIndex) -> coilTint(state))
                .onRegister(connected(() -> List.of(INDUCTION_HEATER_PLATE.get(), INDUCTION_HEATER_CONNECTOR.get()), "induction_plate"))
                .item().color(() -> () -> (stack, tintIndex) -> 0xFF8A20).build();
    }

    /** The coil glows from dull gray up to white-hot with the heat level. */
    private static int coilTint(BlockState state) {
        if (!state.hasProperty(BlazeBurnerBlock.HEAT_LEVEL)) {
            return 0x707070;
        }
        return switch (state.getValue(BlazeBurnerBlock.HEAT_LEVEL)) {
            case NONE -> 0x707070;
            case SMOULDERING -> 0xC04818;
            case FADING, KINDLED -> 0xFF8A20;
            case SEETHING -> 0xFFF2C8;
        };
    }

    private static int inductionLight(BlockState state) {
        return state.hasProperty(BlazeBurnerBlock.HEAT_LEVEL) ? state.getValue(BlazeBurnerBlock.HEAT_LEVEL).ordinal() * 3 : 0;
    }

    // ---- helpers ----

    private static <T extends Block> NonNullUnaryOperator<BlockBuilder<T, CreateRegistrate>> metal() {
        return b -> b.initialProperties(() -> Blocks.IRON_BLOCK).transform(TagGen.pickaxeOnly()).properties(p -> p.sound(SoundType.METAL));
    }

    private static <T extends Block> NonNullUnaryOperator<BlockBuilder<T, CreateRegistrate>> copper() {
        return b -> b.initialProperties(() -> Blocks.COPPER_BLOCK).transform(TagGen.pickaxeOnly()).properties(p -> p.sound(SoundType.COPPER));
    }

    /** A see-through multiblock wall; {@code tint} colours its window panes. */
    private static <T extends Block> NonNullUnaryOperator<BlockBuilder<T, CreateRegistrate>> windowedWall(String side, String end, int tint) {
        return b -> b.properties(Properties::noOcclusion)
                .blockstate(CRRBlockModels.windowedWall(side, end))
                .addLayer(() -> RenderType::cutoutMipped)
                .color(() -> () -> (state, level, pos, tintIndex) -> tint);
    }

    /** A tinted block with a tinted item using the block's model. */
    private static <T extends Block> NonNullUnaryOperator<BlockBuilder<T, CreateRegistrate>> tinted(CRRBlockModels.ModelMaker model, int tint) {
        return b -> b.blockstate(CRRBlockModels.of(model))
                .color(() -> () -> (state, level, pos, tintIndex) -> tint)
                .item(BlockItem::new)
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), prov.modLoc("block/" + ctx.getName())))
                .color(() -> () -> (stack, tintIndex) -> tint)
                .build();
    }

    private static NonNullConsumer<? super Block> connected(Supplier<List<Block>> family, String... textures) {
        return CreateRegistrate.connectedTextures(() -> MultiblockConnectedTextures.behaviour(family, textures));
    }

    /** Picks the Electro Energetics variant only when that mod is loaded, so its classes are never touched otherwise. */
    private static <T extends Block> BlockBuilder<T, CreateRegistrate> eeBlock(String name, Supplier<NonNullFunction<Properties, T>> withEE,
                                                                            NonNullFunction<Properties, T> plain) {
        return REGISTRATE.block(name, ElectroEnergeticsCompat.isLoaded() ? withEE.get() : plain);
    }

    private static ResourceLocation modLoc(String texture) {
        return ReactingReactions.asResource("block/" + texture);
    }

    private static ResourceLocation vanilla(String texture) {
        return ResourceLocation.withDefaultNamespace("block/" + texture);
    }

    public static void register() {
    }
}
