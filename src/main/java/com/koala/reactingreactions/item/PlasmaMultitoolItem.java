package com.koala.reactingreactions.item;

import com.koala.reactingreactions.Config;
import com.koala.reactingreactions.registry.CRRFluids;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Pickaxe, axe and shovel in one, with Fortune or Silk Touch built in. Area mining is in {@code AreaMining}. */
public class PlasmaMultitoolItem extends FluidTankItem implements ExoSettings.Configurable {
    /** Already given by its modes, so not offered by a table or an anvil. */
    private static final Set<ResourceKey<Enchantment>> REDUNDANT = Set.of(Enchantments.FORTUNE, Enchantments.SILK_TOUCH);

    public static final int CAPACITY_MB = 1000;
    private static final float SPEED = 14.0F;
    private static final List<ExoSettings.Option> OPTIONS = List.of(
            new ExoSettings.Option("shape", AreaMining.SHAPES, 0),
            new ExoSettings.Option("drops", List.of("fortune", "silk_touch"), 0),
            ExoSettings.Option.toggle("pickup", false));

    public PlasmaMultitoolItem(Properties properties) {
        super(properties.component(net.minecraft.core.component.DataComponents.TOOL, tool()),
                CRRFluids.DRILL_GREASE.get().getSource(), () -> Config.number(Config.MULTITOOL_CAPACITY_MB, CAPACITY_MB), null);
    }

    private static Tool tool() {
        return new Tool(List.of(Tool.Rule.deniesDrops(Tiers.NETHERITE.getIncorrectBlocksForDrops()),
                Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_PICKAXE, SPEED), Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_AXE, SPEED),
                Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_SHOVEL, SPEED)), 1.0F, 0);
    }

    @Override
    public List<ExoSettings.Option> exoOptions() {
        return OPTIONS;
    }

    private Map<ResourceKey<Enchantment>, Integer> builtIn(ItemStack stack) {
        boolean silk = setting(stack, "drops") == 1;
        return Map.of(Enchantments.FORTUNE, silk ? 0 : 3, Enchantments.SILK_TOUCH, silk ? 1 : 0);
    }

    @Override
    public int getEnchantmentLevel(ItemStack stack, Holder<Enchantment> enchantment) {
        return BuiltInEnchantments.level(stack, enchantment, builtIn(stack));
    }

    @Override
    public ItemEnchantments getAllEnchantments(ItemStack stack, HolderLookup.RegistryLookup<Enchantment> lookup) {
        return BuiltInEnchantments.all(stack, lookup, builtIn(stack));
    }

    /** Strips like an axe, or else flattens like a shovel. */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        InteractionResult axe = Items.NETHERITE_AXE.useOn(context);
        return axe.consumesAction() ? axe : Items.NETHERITE_SHOVEL.useOn(context);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility ability) {
        return ItemAbilities.DEFAULT_PICKAXE_ACTIONS.contains(ability) || ItemAbilities.DEFAULT_AXE_ACTIONS.contains(ability)
                || ItemAbilities.DEFAULT_SHOVEL_ACTIONS.contains(ability);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 15;
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return !BuiltInEnchantments.redundant(enchantment, REDUNDANT) && super.supportsEnchantment(stack, enchantment);
    }

    @Override
    public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        return !BuiltInEnchantments.redundant(enchantment, REDUNDANT) && super.isPrimaryItemFor(stack, enchantment);
    }
}
