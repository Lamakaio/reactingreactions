package com.koala.reactingreactions.item;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.material.Fluid;

import java.util.List;
import java.util.Set;
import java.util.function.IntSupplier;

/** A Composite Exo- armor piece: a tank armor item with settings. Its effects are in {@code CRRExoEvents}. */
public class ExoArmorItem extends FluidTankArmorItem implements ExoSettings.Configurable {
    private final List<ExoSettings.Option> options;

    public ExoArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties, Fluid fluid, IntSupplier capacityMb,
            List<ExoSettings.Option> options) {
        super(material, type, properties, fluid, capacityMb);
        this.options = options;
    }

    @Override
    public List<ExoSettings.Option> exoOptions() {
        return options;
    }

    /** What the piece already does, not offered by a table or an anvil: the boots cushion falls, the leggings speed up swimming. */
    private Set<ResourceKey<Enchantment>> redundant() {
        return getType() == Type.BOOTS ? Set.of(Enchantments.FEATHER_FALLING, Enchantments.DEPTH_STRIDER) : Set.of();
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return !BuiltInEnchantments.redundant(enchantment, redundant()) && super.supportsEnchantment(stack, enchantment);
    }

    @Override
    public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        return !BuiltInEnchantments.redundant(enchantment, redundant()) && super.isPrimaryItemFor(stack, enchantment);
    }

    /** Vanilla only enchants items with durability. */
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }
}
