package com.koala.reactingreactions;

import com.koala.reactingreactions.content.aeronautics.CRRLiftingGasTypes;
import com.koala.reactingreactions.content.compat.DieselGeneratorsCompat;
import com.koala.reactingreactions.content.compat.ElectroEnergeticsCompat;
import com.koala.reactingreactions.content.info.InfoTooltips;
import com.koala.reactingreactions.content.laser.CRRLaserEntityEvents;
import com.koala.reactingreactions.content.laser.ClientLaserPointers;
import com.koala.reactingreactions.content.toxic.ClientContamination;
import com.koala.reactingreactions.content.toxic.TankSealing;
import com.koala.reactingreactions.content.toxic.ToxicityEvents;
import com.koala.reactingreactions.content.toxic.ToxicityTooltips;
import com.koala.reactingreactions.content.ponder.PonderSchematicExporter;
import com.koala.reactingreactions.content.ponder.ShowcaseWorldBuilder;
import com.koala.reactingreactions.event.CRRAccessoryEvents;
import com.koala.reactingreactions.event.CRRExoEvents;
import com.koala.reactingreactions.event.CRRAcetyleneLampEvents;
import com.koala.reactingreactions.event.CRRCommands;
import com.koala.reactingreactions.event.CRREquipmentEvents;
import com.koala.reactingreactions.event.CRRGlassMeltEvents;
import com.koala.reactingreactions.gametest.ToxicityAndDrillTests;
import com.koala.reactingreactions.network.CRRContaminationPayload;
import com.koala.reactingreactions.network.CRRDoubleJumpPayload;
import com.koala.reactingreactions.network.CRRExoSettingPayload;
import com.koala.reactingreactions.network.CRRLaserPayload;
import com.koala.reactingreactions.registry.CRRAeroBlocks;
import com.koala.reactingreactions.registry.CRRArmorMaterials;
import com.koala.reactingreactions.registry.CRRBlockEntities;
import com.koala.reactingreactions.registry.CRRBlocks;
import com.koala.reactingreactions.registry.CRRDataComponents;
import com.koala.reactingreactions.registry.CRRElectricalDevices;
import com.koala.reactingreactions.registry.CRREntities;
import com.koala.reactingreactions.registry.CRRFeatures;
import com.koala.reactingreactions.registry.CRRFluids;
import com.koala.reactingreactions.registry.CRRItems;
import com.koala.reactingreactions.registry.CRRParticles;
import com.koala.reactingreactions.registry.CRRRecipeTypes;
import com.koala.reactingreactions.registry.CRRRegistrate;
import com.koala.reactingreactions.registry.CRRSounds;
import com.koala.reactingreactions.registry.CRRToxicity;
import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import org.slf4j.Logger;

@Mod(ReactingReactions.MODID)
public class ReactingReactions {
    public static final String MODID = "reactingreactions";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // CRRRegistrate puts everything it registers in this tab.
    public static final ResourceKey<CreativeModeTab> MAIN_TAB_KEY =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, asResource("main"));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = CREATIVE_MODE_TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.reactingreactions"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> CRRBlocks.REACTION_CHAMBER_CONTROLLER.get().asItem().getDefaultInstance())
            .build());

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public ReactingReactions(IEventBus modEventBus, ModContainer modContainer) {
        // First: client hooks made before this (item colours, block entity renderers) wait in Registrate for the mod bus, and are
        // lost for good if another mod's Registrate has already flushed its own queue.
        CRRRegistrate.REGISTRATE.registerEventListeners(modEventBus);
        CRRArmorMaterials.register(modEventBus);
        CRRDataComponents.register(modEventBus);
        CRRItems.register(modEventBus);
        CRRBlocks.register();
        CRRBlockEntities.register(modEventBus);
        CRRFluids.register(modEventBus);
        if (ModList.get().isLoaded("aeronautics_bundled")) {
            CRRLiftingGasTypes.init();
            CRRAeroBlocks.register(modEventBus);
        }
        if (ElectroEnergeticsCompat.isLoaded()) {
            CRRElectricalDevices.register(modEventBus);
        }
        CRRRecipeTypes.register(modEventBus);
        CRRFeatures.register(modEventBus);
        CRRToxicity.register(modEventBus);
        CRREntities.register(modEventBus);
        CRRSounds.register(modEventBus);
        CRRParticles.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        NeoForge.EVENT_BUS.register(CRREquipmentEvents.class);
        NeoForge.EVENT_BUS.register(CRRGlassMeltEvents.class);
        NeoForge.EVENT_BUS.register(CRRAccessoryEvents.class);
        NeoForge.EVENT_BUS.register(CRRExoEvents.class);
        NeoForge.EVENT_BUS.register(CRRAcetyleneLampEvents.class);
        NeoForge.EVENT_BUS.register(CRRCommands.class);
        // Dev only: ./gradlew runExportPonder rebuilds the Ponder schematics.
        if (System.getProperty(PonderSchematicExporter.PROPERTY) != null) {
            NeoForge.EVENT_BUS.register(PonderSchematicExporter.class);
        }
        // Dev only: ./gradlew runShowcase builds the showcase world.
        if (System.getProperty(ShowcaseWorldBuilder.PROPERTY) != null) {
            NeoForge.EVENT_BUS.register(ShowcaseWorldBuilder.class);
        }
        NeoForge.EVENT_BUS.register(ToxicityEvents.class);
        NeoForge.EVENT_BUS.register(TankSealing.class);
        NeoForge.EVENT_BUS.register(ToxicityTooltips.class);
        NeoForge.EVENT_BUS.register(InfoTooltips.class);
        NeoForge.EVENT_BUS.register(CRRLaserEntityEvents.class);

        modEventBus.addListener(Config::onConfigEvent);
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener((RegisterGameTestsEvent event) -> event.register(ToxicityAndDrillTests.class));
        NeoForge.EVENT_BUS.addListener(this::registerBrewing);
        modEventBus.addListener(this::registerPayloads);
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
    }

    /** Create Diesel Generators replaces our oil, distillation and fermentation blocks, so they leave the tab (but stay registered). */
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() != MAIN_TAB_KEY || !DieselGeneratorsCompat.isLoaded()) {
            return;
        }
        for (ItemLike replaced : new ItemLike[] {CRRBlocks.RICH_OIL_VEIN.get(), CRRBlocks.OIL_SHALE.get(), CRRBlocks.OIL_DRILL_HEAD.get(),
                CRRBlocks.DISTILLATION_TOWER_CONTROLLER.get(), CRRBlocks.DISTILLATION_TOWER_WALL.get(), CRRBlocks.FERMENTATION_BARREL.get()}) {
            event.remove(new ItemStack(replaced), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }

    // Bromine brews Fire Resistance, like magma cream.
    private void registerBrewing(RegisterBrewingRecipesEvent event) {
        event.getBuilder().addStartMix(CRRItems.BROMINE.get(), Potions.FIRE_RESISTANCE);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(CRRDoubleJumpPayload.TYPE, CRRDoubleJumpPayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> CRRAccessoryEvents.handleDoubleJumpRequest(context.player())));
        registrar.playToServer(CRRExoSettingPayload.TYPE, CRRExoSettingPayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> payload.apply(context.player())));
        registrar.playToClient(CRRContaminationPayload.TYPE, CRRContaminationPayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> ClientContamination.receive(payload)));
        registrar.playToClient(CRRLaserPayload.TYPE, CRRLaserPayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> ClientLaserPointers.receive(payload)));
    }
}
