package com.koala.reactingreactions.content.electrolysis;

import com.koala.reactingreactions.content.compat.ElectroEnergeticsCompat;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.List;

/** The voltage Electro Energetics measures across an electrolyser, synced to clients for the goggles. */
public class Voltmeter {
    private double measured;
    private double lastSynced;

    /** Set every simulation tick by the machine's electrical device. */
    public void set(double voltage) {
        measured = voltage;
    }

    /** Without Electro Energetics nothing supplies a voltage, so electrolysis runs for free. */
    public boolean reaches(double minVoltage) {
        return !ElectroEnergeticsCompat.isLoaded() || measured >= minVoltage;
    }

    public boolean needsSync() {
        if (Math.abs(measured - lastSynced) <= 0.1) {
            return false;
        }
        lastSynced = measured;
        return true;
    }

    public void addTooltip(List<Component> tooltip) {
        if (ElectroEnergeticsCompat.isLoaded()) {
            tooltip.add(Component.literal(String.format(" - Voltage: %.1f V", measured)).withStyle(measured > 0 ? ChatFormatting.GRAY : ChatFormatting.RED));
        }
    }

    public void read(CompoundTag compound, boolean clientPacket) {
        if (clientPacket) {
            measured = compound.getDouble("MeasuredVoltage");
        }
    }

    public void write(CompoundTag compound, boolean clientPacket) {
        if (clientPacket) {
            compound.putDouble("MeasuredVoltage", measured);
        }
    }
}
