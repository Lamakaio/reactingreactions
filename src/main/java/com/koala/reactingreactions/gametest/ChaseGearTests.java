package com.koala.reactingreactions.gametest;

import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.item.AreaMining;
import com.koala.reactingreactions.item.FluidTankHolder;
import com.koala.reactingreactions.registry.CRRFluids;
import com.koala.reactingreactions.registry.CRRItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ReactingReactions.MODID)
@PrefixGameTestTemplate(false)
public class ChaseGearTests {
    private static final String EMPTY = "empty";

    private static ItemStack filled(ItemStack stack, Fluid fluid, int amount) {
        FluidTankHolder.setContents(stack, new FluidStack(fluid, amount));
        return stack;
    }

    @GameTest(template = EMPTY)
    public static void settingsDefaultAndValidate(GameTestHelper helper) {
        var item = CRRItems.PLASMA_MULTITOOL.get();
        ItemStack tool = new ItemStack(item);
        helper.assertTrue(item.setting(tool, "shape") == 0 && !item.enabled(tool, "pickup"), "defaults: single block, no pickup");
        item.set(tool, "shape", 3);
        item.set(tool, "shape", 99);
        item.set(tool, "nonsense", 1);
        helper.assertTrue(item.setting(tool, "shape") == 3, "a valid value sticks, invalid ones are ignored");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void multitoolSwitchesFortuneAndSilk(GameTestHelper helper) {
        var enchantments = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var fortune = enchantments.getOrThrow(Enchantments.FORTUNE);
        var silk = enchantments.getOrThrow(Enchantments.SILK_TOUCH);
        ItemStack tool = new ItemStack(CRRItems.PLASMA_MULTITOOL.get());
        helper.assertTrue(tool.getEnchantmentLevel(fortune) == 3 && tool.getEnchantmentLevel(silk) == 0, "Fortune III by default");
        CRRItems.PLASMA_MULTITOOL.get().set(tool, "drops", 1);
        helper.assertTrue(tool.getEnchantmentLevel(fortune) == 0 && tool.getEnchantmentLevel(silk) == 1, "Silk Touch mode drops Fortune");
        helper.assertTrue(tool.getAllEnchantments(enchantments).getLevel(silk) == 1, "loot sees it too");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void multitoolShapes(GameTestHelper helper) {
        var item = CRRItems.PLASMA_MULTITOOL.get();
        ItemStack tool = filled(new ItemStack(item), CRRFluids.DRILL_GREASE.get().getSource(), 1000);
        for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(2, 2, 4), new BlockPos(6, 6, 6))) {
            helper.setBlock(pos, Blocks.STONE);
        }
        helper.setBlock(new BlockPos(3, 4, 4), Blocks.OBSIDIAN);
        BlockPos origin = helper.absolutePos(new BlockPos(4, 4, 4));
        item.set(tool, "shape", 1);
        var square = AreaMining.extraBlocks(helper.getLevel(), tool, origin, Direction.NORTH, false);
        helper.assertTrue(square.size() == 7, "3x3 minus the origin and the obsidian should be 7, got " + square.size());
        item.set(tool, "shape", 4);
        var tunnel = AreaMining.extraBlocks(helper.getLevel(), tool, origin, Direction.NORTH, false);
        helper.assertTrue(tunnel.size() == 5, "a 1x2 tunnel 3 deep is 6 blocks with the origin, got " + (tunnel.size() + 1));
        FluidTankHolder.setContents(tool, FluidStack.EMPTY);
        helper.assertTrue(AreaMining.extraBlocks(helper.getLevel(), tool, origin, Direction.NORTH, false).isEmpty(), "no grease, no area");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void bootsTakeTheFall(GameTestHelper helper) {
        // A plain mock player: a mock server player trips KubeJS's login sync.
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack boots = filled(new ItemStack(CRRItems.EXO_BOOTS.get()), CRRFluids.MINERAL_OIL.get().getSource(), 100);
        player.setItemSlot(EquipmentSlot.FEET, boots);
        float health = player.getHealth();
        player.causeFallDamage(13, 1.0F, player.damageSources().fall());
        helper.assertTrue(player.getHealth() == health, "fueled boots should take the whole fall");
        int left = FluidTankHolder.contents(player.getItemBySlot(EquipmentSlot.FEET)).getAmount();
        helper.assertTrue(left == 80, "10 blocks past the first 3 at 2 mB each should leave 80 mB, got " + left);
        FluidTankHolder.setContents(player.getItemBySlot(EquipmentSlot.FEET), FluidStack.EMPTY);
        player.causeFallDamage(13, 1.0F, player.damageSources().fall());
        helper.assertTrue(player.getHealth() < health, "empty boots should not");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void bladeRetractsWithoutNeon(GameTestHelper helper) {
        ItemStack blade = new ItemStack(CRRItems.NEON_BLADE.get());
        double unlit = damage(blade);
        filled(blade, CRRFluids.NEON.get().getSource(), 100);
        double lit = damage(blade);
        helper.assertTrue(lit > unlit + 5, "the lit blade should hit much harder, got " + lit + " vs " + unlit);
        helper.succeed();
    }

    private static double damage(ItemStack stack) {
        double[] total = {0};
        stack.forEachModifier(EquipmentSlotGroup.MAINHAND, (attribute, modifier) -> {
            if (attribute.is(Attributes.ATTACK_DAMAGE)) {
                total[0] += modifier.amount();
            }
        });
        return total[0];
    }
}
