package com.koala.reactingreactions.item;

import com.koala.reactingreactions.content.compat.WornAccessories;
import com.simibubi.create.AllEnchantments;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.function.IntSupplier;

import org.jetbrains.annotations.Nullable;

/**
 * A non-armor item with a fixed-fluid internal tank (the Aerozine Thrusters). Worn as an accessory, or in {@code fallbackSlot}
 * when no accessory slot mod is installed.
 */
public class FluidTankItem extends Item implements FluidTankHolder {
    private final Fluid fluid;
    private final IntSupplier capacityMb;
    private final @Nullable EquipmentSlot fallbackSlot;

    /** {@code capacityMb} is a supplier, not a plain value, so it can read a config option live (this constructor runs at registration, well before any config is loaded). */
    public FluidTankItem(Properties properties, Fluid fluid, IntSupplier capacityMb, @Nullable EquipmentSlot fallbackSlot) {
        super(properties);
        this.fluid = fluid;
        this.capacityMb = capacityMb;
        this.fallbackSlot = fallbackSlot;
    }

    @Override
    public @Nullable EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return WornAccessories.hasSlotMod() ? null : fallbackSlot;
    }

    @Override
    public int baseTankCapacityMb() {
        return capacityMb.getAsInt();
    }

    @Override
    public boolean acceptsFluid(FluidStack stack) {
        return stack.getFluid().isSame(fluid);
    }

    @Override
    public Fluid tankFluid() {
        return fluid;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return isTankBarVisible(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return tankBarWidth(stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return tankBarColor(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(tankTooltipLine(stack));
    }

    /** Vanilla only enchants items with durability. */
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 10;
    }

    /** Create's Capacity enlarges the tank, so a table and an anvil offer it. */
    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return enchantment.is(AllEnchantments.CAPACITY) || super.supportsEnchantment(stack, enchantment);
    }

    @Override
    public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        return enchantment.is(AllEnchantments.CAPACITY) || super.isPrimaryItemFor(stack, enchantment);
    }
}
