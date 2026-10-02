package com.koala.reactingreactions.content.electrolysis;

import com.george_vi.electroenergetics.devices.device.DeviceBlock;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.koala.reactingreactions.registry.CRRElectricalDevices;

/** The vat controller with its Electro Energetics device, which measures the voltage across the electrodes. */
public class ElectrolysisVatControllerBlock extends ElectrolysisVatControllerBlockBase implements DeviceBlock<ElectrolysisVatDevice> {
    public ElectrolysisVatControllerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public SimulatedDeviceType<ElectrolysisVatDevice> getDevice() {
        return CRRElectricalDevices.ELECTROLYSIS_VAT_DEVICE.get();
    }
}
