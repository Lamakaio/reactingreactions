package com.koala.reactingreactions.item;

import com.koala.reactingreactions.Config;
import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.event.CRREquipmentEvents;
import com.koala.reactingreactions.registry.CRRFluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** A long plasma blade: hits hard with extra reach while it has neon, and retracts into a weak hilt without. Blocks projectiles. */
public class NeonBladeItem extends FluidTankItem implements ExoSettings.Configurable {
    /** Already given by its modes, so not offered by a table or an anvil. */
    private static final Set<ResourceKey<Enchantment>> REDUNDANT = Set.of(Enchantments.SWEEPING_EDGE, Enchantments.LOOTING);

    public static final int CAPACITY_MB = 1000;
    private static final List<ExoSettings.Option> OPTIONS = List.of(ExoSettings.Option.toggle("sweep", true), ExoSettings.Option.toggle("looting", true));
    private static final Map<UUID, Double> NEON_CARRY = new HashMap<>();
    private static final ItemAttributeModifiers LIT = modifiers(13, 2.0);
    private static final ItemAttributeModifiers UNLIT = modifiers(3, 0.5);

    public NeonBladeItem(Properties properties) {
        super(properties, CRRFluids.NEON.get().getSource(), () -> Config.number(Config.NEON_BLADE_CAPACITY_MB, CAPACITY_MB), null);
    }

    private static ItemAttributeModifiers modifiers(double damage, double reach) {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, damage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(ReactingReactions.asResource("neon_blade_reach"), reach,
                        AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build();
    }

    /** Spends {@code hits} hits' worth of neon, carrying fractions of a millibucket. */
    public static void spend(Player player, ItemStack stack, int hits) {
        CRREquipmentEvents.consumeFractional(NEON_CARRY, player, hits * Config.number(Config.NEON_BLADE_MB_PER_HIT, 0.2), stack);
    }

    public static boolean lit(ItemStack stack) {
        return FluidTankHolder.contents(stack).getAmount() > 0;
    }

    @Override
    public List<ExoSettings.Option> exoOptions() {
        return OPTIONS;
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        return lit(stack) ? LIT : UNLIT;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide && attacker instanceof Player player && !player.isCreative()) {
            spend(player, stack, 1);
        }
        return true;
    }

    private Map<ResourceKey<Enchantment>, Integer> builtIn(ItemStack stack) {
        boolean on = lit(stack);
        return Map.of(Enchantments.SWEEPING_EDGE, on && enabled(stack, "sweep") ? 3 : 0, Enchantments.LOOTING, on && enabled(stack, "looting") ? 3 : 0);
    }

    @Override
    public int getEnchantmentLevel(ItemStack stack, Holder<Enchantment> enchantment) {
        return BuiltInEnchantments.level(stack, enchantment, builtIn(stack));
    }

    @Override
    public ItemEnchantments getAllEnchantments(ItemStack stack, HolderLookup.RegistryLookup<Enchantment> lookup) {
        return BuiltInEnchantments.all(stack, lookup, builtIn(stack));
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility ability) {
        return ability == ItemAbilities.SWORD_DIG || ability == ItemAbilities.SWORD_SWEEP && lit(stack) && enabled(stack, "sweep");
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !player.isCreative();
    }

    /** Held up to block projectiles, see {@code CRRExoEvents}. */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!lit(stack) || player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
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
