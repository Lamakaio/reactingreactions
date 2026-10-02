package com.koala.reactingreactions.registry;

import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.george_vi.electroenergetics.foundation.device.SimpleNonTickingElectricalDevice;
import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.content.electrolysis.ElectrolysisVatDevice;
import com.koala.reactingreactions.content.electrolysis.SmallElectrolyserDevice;
import com.koala.reactingreactions.content.induction.InductionHeaterDevice;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class CRRElectricalDevices {
    private static final ResourceKey<? extends Registry<SimulatedDeviceType<?>>> SIMULATED_DEVICE_TYPE_KEY = CEERegistries.SIMULATED_DEVICE_TYPE.key();

    private static final DeferredRegister<SimulatedDeviceType<?>> DEVICES =
            DeferredRegister.create(SIMULATED_DEVICE_TYPE_KEY, ReactingReactions.MODID);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<ElectrolysisVatDevice>> ELECTROLYSIS_VAT_DEVICE =
            DEVICES.register("electrolysis_vat", () -> new SimulatedDeviceType<>(
                    ReactingReactions.asResource("electrolysis_vat"),
                    (type, level, pos, sd) -> new ElectrolysisVatDevice(level, pos, sd, type),
                    List.of(CRRBlocks.ELECTROLYSIS_VAT_CONTROLLER.get())));

    // A passive terminal, for every electrode material.
    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<SimpleNonTickingElectricalDevice>> ELECTRODE_DEVICE =
            DEVICES.register("electrode", () -> new SimulatedDeviceType<>(
                    ReactingReactions.asResource("electrode"),
                    (type, level, pos, sd) -> new SimpleNonTickingElectricalDevice(level, pos, sd, type),
                    List.of(CRRBlocks.GRAPHITE_ELECTRODE.get(), CRRBlocks.GOLD_STEEL_ELECTRODE.get(),
                            CRRBlocks.LEAD_ELECTRODE.get())));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<SmallElectrolyserDevice>> SMALL_ELECTROLYSER_DEVICE =
            DEVICES.register("small_electrolyser", () -> new SimulatedDeviceType<>(
                    ReactingReactions.asResource("small_electrolyser"),
                    (type, level, pos, sd) -> new SmallElectrolyserDevice(level, pos, sd, type),
                    List.of(CRRBlocks.SMALL_ELECTROLYSER.get())));

    // An Electrolysis Vat terminal: a plain wire terminal (the vat's own device bridges the electrode beside it to it).
    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<SimpleNonTickingElectricalDevice>> VAT_TERMINAL_DEVICE =
            DEVICES.register("vat_terminal", () -> new SimulatedDeviceType<>(
                    ReactingReactions.asResource("vat_terminal"),
                    (type, level, pos, sd) -> new SimpleNonTickingElectricalDevice(level, pos, sd, type),
                    List.of(CRRBlocks.ELECTROLYSIS_VAT_TERMINAL.get())));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<InductionHeaterDevice>> INDUCTION_HEATER_DEVICE =
            DEVICES.register("induction_heater", () -> new SimulatedDeviceType<>(
                    ReactingReactions.asResource("induction_heater"),
                    (type, level, pos, sd) -> new InductionHeaterDevice(level, pos, sd, type),
                    List.of(CRRBlocks.INDUCTION_HEATER_CONNECTOR.get())));

    public static void register(IEventBus modEventBus) {
        DEVICES.register(modEventBus);
    }
}
