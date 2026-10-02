package com.koala.reactingreactions.item;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Map;
import java.util.Set;

/** Enchantment levels a chase item's mode adds on top of its own enchantments; a level of 0 hides an enchantment. */
final class BuiltInEnchantments {
    private BuiltInEnchantments() {
    }

    static int level(ItemStack stack, Holder<Enchantment> enchantment, Map<ResourceKey<Enchantment>, Integer> builtIn) {
        int own = stack.getTagEnchantments().getLevel(enchantment);
        for (var entry : builtIn.entrySet()) {
            if (enchantment.is(entry.getKey())) {
                return entry.getValue() == 0 ? 0 : Math.max(own, entry.getValue());
            }
        }
        return own;
    }

    /**
     * Whether a table or an anvil should leave {@code enchantment} out for a chase item that already does it ({@code builtIn}): those,
     * and Unbreaking and Mending since the item is unbreakable.
     */
    static boolean redundant(Holder<Enchantment> enchantment, Set<ResourceKey<Enchantment>> builtIn) {
        return enchantment.is(Enchantments.UNBREAKING) || enchantment.is(Enchantments.MENDING) || builtIn.stream().anyMatch(enchantment::is);
    }

    static ItemEnchantments all(ItemStack stack, HolderLookup.RegistryLookup<Enchantment> lookup, Map<ResourceKey<Enchantment>, Integer> builtIn) {
        ItemEnchantments.Mutable all = new ItemEnchantments.Mutable(stack.getTagEnchantments());
        builtIn.forEach((key, level) -> lookup.get(key).ifPresent(holder -> all.set(holder, level(stack, holder, builtIn))));
        return all.toImmutable();
    }
}
