package com.koala.reactingreactions.registry;

import com.koala.reactingreactions.content.airlessoven.AirlessOvenControllerBlockEntity;
import com.koala.reactingreactions.content.airlessoven.AirlessOvenRenderer;
import com.koala.reactingreactions.content.distillation.DistillationTowerControllerBlockEntity;
import com.koala.reactingreactions.content.distillation.DistillationTowerRenderer;
import com.koala.reactingreactions.content.drill.DerrickBlockEntity;
import com.koala.reactingreactions.content.drill.DerrickControllerBlockEntity;
import com.koala.reactingreactions.content.drill.DerrickRenderer;
import com.koala.reactingreactions.content.electrolysis.ElectrodeInfoBlockEntity;
import com.koala.reactingreactions.content.electrolysis.ElectrolysisVatControllerBlockEntity;
import com.koala.reactingreactions.content.electrolysis.ElectrolysisVatRenderer;
import com.koala.reactingreactions.content.electrolysis.SmallElectrolyserBlockEntity;
import com.koala.reactingreactions.content.equipment.charging.ChargingPadBlockEntity;
import com.koala.reactingreactions.content.induction.InductionHeaterConnectorBlockEntity;
import com.koala.reactingreactions.content.induction.InductionHeaterPlateBlockEntity;
import com.koala.reactingreactions.content.multiblock.MultiblockControllerBlockEntity;
import com.koala.reactingreactions.content.multiblock.MultiblockWallBlockEntity;
import com.koala.reactingreactions.content.multiblock.attachment.GaugeBlockEntity;
import com.koala.reactingreactions.content.multiblock.attachment.GaugeRenderer;
import com.koala.reactingreactions.content.reaction.FermentationBarrelBlockEntity;
import com.koala.reactingreactions.content.reaction.ReactionChamberControllerBlockEntity;
import com.koala.reactingreactions.content.reaction.ReactionChamberRenderer;
import com.koala.reactingreactions.content.steam.SteamTurbineBlockEntity;
import com.koala.reactingreactions.content.steel.SteelEncasedShaftBlockEntity;
import com.koala.reactingreactions.content.toxic.AtmosphericScrubberBlockEntity;
import com.koala.reactingreactions.content.toxic.AtmosphericScrubberRenderer;
import com.koala.reactingreactions.content.toxic.FloorDrainBlockEntity;
import com.koala.reactingreactions.content.toxic.GasVentBlockEntity;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.builders.BlockEntityBuilder.BlockEntityFactory;
import com.tterrag.registrate.builders.BlockEntityBuilder;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class CRRBlockEntities {
    private static final CRRRegistrate REGISTRATE = CRRRegistrate.REGISTRATE;

    public static final BlockEntityEntry<ChargingPadBlockEntity> CHARGING_PAD =
            blockEntity("charging_pad", ChargingPadBlockEntity::new, CRRBlocks.CHARGING_PAD).register();

    public static final BlockEntityEntry<SteelEncasedShaftBlockEntity> STEEL_ENCASED_SHAFT = REGISTRATE
            .blockEntity("steel_encased_shaft", SteelEncasedShaftBlockEntity::new)
            .visual(() -> SingleAxisRotatingVisual::shaft, false)
            .validBlocks(CRRBlocks.STEEL_ENCASED_SHAFT)
            .renderer(() -> ShaftRenderer::new)
            .register();

    // With Flywheel on, Create's kinetic renderers draw nothing and the visual draws the shaft.
    public static final BlockEntityEntry<SteamTurbineBlockEntity> STEAM_TURBINE = REGISTRATE
            .blockEntity("steam_turbine", SteamTurbineBlockEntity::new)
            .visual(() -> SingleAxisRotatingVisual::shaft, true)
            .validBlocks(CRRBlocks.STEAM_TURBINE)
            .renderer(() -> ShaftRenderer::new)
            .register();

    public static final BlockEntityEntry<KineticBlockEntity> CIRCULATION_PUMP = REGISTRATE
            .blockEntity("circulation_pump", KineticBlockEntity::new)
            .visual(() -> SingleAxisRotatingVisual::shaft, true)
            .validBlocks(CRRBlocks.CIRCULATION_PUMP)
            .renderer(() -> ShaftRenderer::new)
            .register();

    public static final BlockEntityEntry<GaugeBlockEntity> MACHINE_GAUGE = REGISTRATE
            .blockEntity("machine_gauge", GaugeBlockEntity::new)
            .validBlocks(CRRBlocks.MACHINE_GAUGE)
            .renderer(() -> GaugeRenderer::new)
            .register();

    public static final BlockEntityEntry<FluidPipeBlockEntity> PLASTIC_PIPE = REGISTRATE
            .blockEntity("plastic_pipe", FluidPipeBlockEntity::new)
            .validBlocks(CRRBlocks.PLASTIC_PIPE)
            .register();

    // ---- multiblocks ----

    public static final BlockEntityEntry<MultiblockWallBlockEntity> MULTIBLOCK_WALL = blockEntity("multiblock_wall", MultiblockWallBlockEntity::new,
            CRRBlocks.ELECTROLYSIS_VAT_WALL, CRRBlocks.DISTILLATION_TOWER_WALL, CRRBlocks.AIRLESS_OVEN_WALL, CRRBlocks.REACTION_CHAMBER_WALL).register();

    public static final BlockEntityEntry<ElectrolysisVatControllerBlockEntity> ELECTROLYSIS_VAT_CONTROLLER =
            blockEntity("electrolysis_vat_controller", ElectrolysisVatControllerBlockEntity::new, CRRBlocks.ELECTROLYSIS_VAT_CONTROLLER)
                    .renderer(() -> ElectrolysisVatRenderer::new).register();

    public static final BlockEntityEntry<DistillationTowerControllerBlockEntity> DISTILLATION_TOWER_CONTROLLER =
            blockEntity("distillation_tower_controller", DistillationTowerControllerBlockEntity::new, CRRBlocks.DISTILLATION_TOWER_CONTROLLER)
                    .renderer(() -> DistillationTowerRenderer::new).register();

    public static final BlockEntityEntry<AirlessOvenControllerBlockEntity> AIRLESS_OVEN_CONTROLLER =
            blockEntity("airless_oven_controller", AirlessOvenControllerBlockEntity::new, CRRBlocks.AIRLESS_OVEN_CONTROLLER)
                    .renderer(() -> AirlessOvenRenderer::new).register();

    public static final BlockEntityEntry<ReactionChamberControllerBlockEntity> REACTION_CHAMBER_CONTROLLER =
            blockEntity("reaction_chamber_controller", ReactionChamberControllerBlockEntity::new, CRRBlocks.REACTION_CHAMBER_CONTROLLER)
                    .renderer(() -> ReactionChamberRenderer::new).register();

    // ---- other machines ----

    public static final BlockEntityEntry<SmallElectrolyserBlockEntity> SMALL_ELECTROLYSER =
            blockEntity("small_electrolyser", SmallElectrolyserBlockEntity::new, CRRBlocks.SMALL_ELECTROLYSER).register();

    // On every electrode and vat terminal, so the goggles show the vat's electrode info on them.
    public static final BlockEntityEntry<ElectrodeInfoBlockEntity> ELECTRODE_INFO = blockEntity("electrode_info", ElectrodeInfoBlockEntity::new,
            CRRBlocks.GRAPHITE_ELECTRODE, CRRBlocks.GOLD_STEEL_ELECTRODE, CRRBlocks.LEAD_ELECTRODE, CRRBlocks.ELECTROLYSIS_VAT_TERMINAL).register();

    public static final BlockEntityEntry<FermentationBarrelBlockEntity> FERMENTATION_BARREL =
            blockEntity("fermentation_barrel", FermentationBarrelBlockEntity::new, CRRBlocks.FERMENTATION_BARREL).register();

    public static final BlockEntityEntry<DerrickBlockEntity> DERRICK_BLOCK =
            blockEntity("derrick_block", DerrickBlockEntity::new, CRRBlocks.DERRICK_BLOCK).register();

    public static final BlockEntityEntry<DerrickControllerBlockEntity> DERRICK_CONTROLLER = REGISTRATE
            .blockEntity("derrick_controller", DerrickControllerBlockEntity::new)
            .visual(() -> SingleAxisRotatingVisual::shaft, true)
            .validBlocks(CRRBlocks.DERRICK_CONTROLLER)
            .renderer(() -> DerrickRenderer::new)
            .register();

    public static final BlockEntityEntry<AtmosphericScrubberBlockEntity> ATMOSPHERIC_SCRUBBER = REGISTRATE
            .blockEntity("atmospheric_scrubber", AtmosphericScrubberBlockEntity::new)
            .visual(() -> SingleAxisRotatingVisual::shaft, true)
            .validBlocks(CRRBlocks.ATMOSPHERIC_SCRUBBER)
            .renderer(() -> AtmosphericScrubberRenderer::new)
            .register();

    public static final BlockEntityEntry<FloorDrainBlockEntity> FLOOR_DRAIN =
            blockEntity("floor_drain", FloorDrainBlockEntity::new, CRRBlocks.FLOOR_DRAIN).register();
    public static final BlockEntityEntry<GasVentBlockEntity> GAS_VENT =
            blockEntity("gas_vent", GasVentBlockEntity::new, CRRBlocks.GAS_VENT).register();

    public static final BlockEntityEntry<InductionHeaterPlateBlockEntity> INDUCTION_HEATER_PLATE =
            blockEntity("induction_heater_plate", InductionHeaterPlateBlockEntity::new, CRRBlocks.INDUCTION_HEATER_PLATE).register();
    public static final BlockEntityEntry<InductionHeaterConnectorBlockEntity> INDUCTION_HEATER_CONNECTOR =
            blockEntity("induction_heater_connector", InductionHeaterConnectorBlockEntity::new, CRRBlocks.INDUCTION_HEATER_CONNECTOR).register();

    @SafeVarargs
    private static <T extends BlockEntity> BlockEntityBuilder<T, CreateRegistrate> blockEntity(String name, BlockEntityFactory<T> factory,
                                                                                            NonNullSupplier<? extends Block>... blocks) {
        return REGISTRATE.blockEntity(name, factory).validBlocks(blocks);
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(CRRBlockEntities::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        ChargingPadBlockEntity.registerCapabilities(event, CHARGING_PAD.get());
        SteamTurbineBlockEntity.registerCapabilities(event, STEAM_TURBINE.get());
        MultiblockControllerBlockEntity.registerCapabilities(event, ELECTROLYSIS_VAT_CONTROLLER.get());
        MultiblockControllerBlockEntity.registerCapabilities(event, DISTILLATION_TOWER_CONTROLLER.get());
        MultiblockControllerBlockEntity.registerCapabilities(event, AIRLESS_OVEN_CONTROLLER.get());
        MultiblockControllerBlockEntity.registerCapabilities(event, REACTION_CHAMBER_CONTROLLER.get());
        MultiblockWallBlockEntity.registerCapabilities(event, MULTIBLOCK_WALL.get());
        SmallElectrolyserBlockEntity.registerCapabilities(event, SMALL_ELECTROLYSER.get());
        FermentationBarrelBlockEntity.registerCapabilities(event, FERMENTATION_BARREL.get());
        DerrickControllerBlockEntity.registerCapabilities(event, DERRICK_CONTROLLER.get());
        DerrickBlockEntity.registerCapabilities(event, DERRICK_BLOCK.get());
        AtmosphericScrubberBlockEntity.registerCapabilities(event, ATMOSPHERIC_SCRUBBER.get());
        FloorDrainBlockEntity.registerCapabilities(event, FLOOR_DRAIN.get());
        GasVentBlockEntity.registerCapabilities(event, GAS_VENT.get());
    }
}
