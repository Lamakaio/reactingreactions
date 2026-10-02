package com.koala.reactingreactions.item;

import com.simibubi.create.AllEnchantments;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.function.IntSupplier;

/** An armor piece with a fixed-fluid internal tank. */
public class FluidTankArmorItem extends ArmorItem implements FluidTankHolder {
    private final Fluid fluid;
    private final IntSupplier capacityMb;

    public FluidTankArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties, Fluid fluid,
            IntSupplier capacityMb) {
        super(material, type, properties);
        this.fluid = fluid;
        this.capacityMb = capacityMb;
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
