package com.koala.reactingreactions.content.electrolysis;

import com.koala.reactingreactions.content.compat.ElectroEnergeticsCompat;
import com.koala.reactingreactions.content.electrolysis.recipe.ElectrolysisRecipe;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The voltage Electro Energetics measures across an electrolyser and the power it draws, synced to clients for the goggles.
 * Electrolysis costs energy as real cells do: a recipe passes {@link #CHARGE_PER_TICK} coulombs per tick of its base time, at
 * its minimum voltage, so a higher-voltage recipe costs more (water's bucket of hydrogen, at 1250 V, takes 4 MJ), however fast
 * the machine could run it. While a recipe is selected the cell is a load drawing {@code nominalAmps} at that voltage: its
 * power grows with the recipe's voltage, and more voltage than that means more power and faster work, up to the machine's own
 * speed. That keeps electrolysis, then burning its hydrogen, from making energy.
 */
public class Voltmeter {
    public static final double CHARGE_PER_TICK = 4;
    /** Barely a load while idle, so the open voltage shows. */
    private static final double IDLE_RESISTANCE_OHMS = 10_000;

    private final double nominalAmps;
    private double measured;
    private double power;
    private double lastSynced;

    public Voltmeter(double nominalAmps) {
        this.nominalAmps = nominalAmps;
    }

    /** The resistance the cell presents: sized to the selected recipe, or idle. */
    public double loadResistance(@Nullable ElectrolysisRecipe recipe) {
        return recipe == null || recipe.minVoltage <= 0 ? IDLE_RESISTANCE_OHMS : recipe.minVoltage / nominalAmps;
    }

    /** Set every simulation tick by the machine's electrical device. */
    public void set(double voltage, double resistanceOhms) {
        measured = voltage;
        power = voltage * voltage / resistanceOhms;
    }

    /** Without Electro Energetics nothing supplies a voltage, so electrolysis runs for free. */
    public boolean reaches(double minVoltage) {
        return !ElectroEnergeticsCompat.isLoaded() || measured >= minVoltage;
    }

    /** The share of a tick's work the power drawn pays for, when the recipe runs in {@code duration} ticks at most. */
    public float workThisTick(ElectrolysisRecipe recipe, int duration) {
        if (!ElectroEnergeticsCompat.isLoaded() || recipe.minVoltage <= 0) {
            return 1;
        }
        int baseTicks = recipe.getProcessingDuration() > 0 ? recipe.getProcessingDuration() : 100;
        double perWorkTick = CHARGE_PER_TICK * recipe.minVoltage * baseTicks / duration;
        return (float) Math.min(1, power / 20 / perWorkTick);
    }

    /** What keeps a recipe from running: too little voltage once the cell draws its load. */
    public String shortfall(ElectrolysisRecipe recipe) {
        return String.format("Needs %.0f V under load (now %.0f V), about %.1f kW", recipe.minVoltage, measured, recipe.minVoltage * nominalAmps / 1000);
    }

    public double nominalAmps() {
        return nominalAmps;
    }

    public double power() {
        return power;
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
            tooltip.add(Component.literal(String.format(" - Voltage: %.1f V, drawing %.1f kW", measured, power / 1000))
                    .withStyle(measured > 0 ? ChatFormatting.GRAY : ChatFormatting.RED));
        }
    }

    public void read(CompoundTag compound, boolean clientPacket) {
        if (clientPacket) {
            measured = compound.getDouble("MeasuredVoltage");
            power = compound.getDouble("MeasuredPower");
        }
    }

    public void write(CompoundTag compound, boolean clientPacket) {
        if (clientPacket) {
            compound.putDouble("MeasuredVoltage", measured);
            compound.putDouble("MeasuredPower", power);
        }
    }
}
