package com.koala.reactingreactions.content.drill.jei;

import com.koala.reactingreactions.content.multiblock.jei.MultiblockPreviews;
import com.koala.reactingreactions.registry.CRRBlocks;
import com.koala.reactingreactions.registry.CRRFluids;
import com.koala.reactingreactions.registry.CRRItems;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

/** Builds the drilling rows from the heads and rich veins, so the viewer always agrees with what the rig does. */
public final class DrillingRecipes {
    private DrillingRecipes() {
    }

    /** The blocks that show drilling recipes. */
    public static List<ItemLike> workstations() {
        return List.of(CRRBlocks.DERRICK_CONTROLLER.get(), CRRBlocks.OIL_DRILL_HEAD.get(), CRRBlocks.MINERAL_DRILL_HEAD_STEEL.get(),
                CRRBlocks.MINERAL_DRILL_HEAD_TITANIUM.get(), CRRBlocks.MINERAL_DRILL_HEAD_DIAMOND.get());
    }

    public static List<DrillingRecipe> all() {
        List<DrillingRecipe> recipes = new ArrayList<>();
        // Oil: the oil head on a rich oil vein, with either lubricant.
        for (var lubricant : new Fluid[] { CRRFluids.SEED_OIL.get().getSource(), CRRFluids.MINERAL_OIL.get().getSource() }) {
            recipes.add(new DrillingRecipe(new ItemStack(CRRBlocks.OIL_DRILL_HEAD.get()), new ItemStack(CRRBlocks.RICH_OIL_VEIN.get()),
                    new FluidStack(lubricant, 100), ItemStack.EMPTY, new FluidStack(CRRFluids.CRUDE_OIL.get().getSource(), 1000),
                    MultiblockPreviews.drill(CRRBlocks.OIL_DRILL_HEAD.get(), CRRBlocks.RICH_OIL_VEIN.get(), sideItems())));
        }
        mineral(recipes, CRRBlocks.MINERAL_DRILL_HEAD_STEEL.get(), CRRBlocks.RICH_TUFF_VEIN.get(), CRRItems.TUFF_DUST.get());
        mineral(recipes, CRRBlocks.MINERAL_DRILL_HEAD_STEEL.get(), CRRBlocks.RICH_GRANITE_VEIN.get(), CRRItems.GRANITE_DUST.get());
        mineral(recipes, CRRBlocks.MINERAL_DRILL_HEAD_STEEL.get(), CRRBlocks.RICH_DIORITE_VEIN.get(), CRRItems.DIORITE_DUST.get());
        mineral(recipes, CRRBlocks.MINERAL_DRILL_HEAD_STEEL.get(), CRRBlocks.RICH_SCORIA_VEIN.get(), CRRItems.SCORIA_DUST.get());
        mineral(recipes, CRRBlocks.MINERAL_DRILL_HEAD_TITANIUM.get(), CRRBlocks.RICH_ASURINE_VEIN.get(), CRRItems.ASURINE_DUST.get());
        mineral(recipes, CRRBlocks.MINERAL_DRILL_HEAD_TITANIUM.get(), CRRBlocks.RICH_CRIMSITE_VEIN.get(), CRRItems.CRIMSITE_DUST.get());
        mineral(recipes, CRRBlocks.MINERAL_DRILL_HEAD_TITANIUM.get(), CRRBlocks.RICH_OCHRUM_VEIN.get(), CRRItems.OCHRUM_DUST.get());
        mineral(recipes, CRRBlocks.MINERAL_DRILL_HEAD_TITANIUM.get(), CRRBlocks.RICH_VERIDIUM_VEIN.get(), CRRItems.VERIDIUM_DUST.get());
        // The diamond bit drills every vein above; one row per vein so it shows up from any dust.
        int steelAndTitanium = recipes.size();
        for (int i = 0; i < steelAndTitanium; i++) {
            DrillingRecipe r = recipes.get(i);
            if (!r.outputItem().isEmpty() && r.consumable().getFluid().isSame(CRRFluids.COOLANT.get().getSource())) {
                mineral(recipes, CRRBlocks.MINERAL_DRILL_HEAD_DIAMOND.get(), ((BlockItem) r.vein().getItem()).getBlock(), r.outputItem().getItem());
            }
        }
        return recipes;
    }

    private static void mineral(List<DrillingRecipe> recipes, Block head, Block vein, Item dust) {
        // Every coolant, shown as what the same stretch of drilling costs: ethanol coolant, 1% brine (twice as fast), drill grease (a quarter).
        recipes.add(new DrillingRecipe(new ItemStack(head), new ItemStack(vein), new FluidStack(CRRFluids.COOLANT.get().getSource(), 100), new ItemStack(dust),
                FluidStack.EMPTY, MultiblockPreviews.drill(head, vein, sideItems())));
        recipes.add(new DrillingRecipe(new ItemStack(head), new ItemStack(vein), new FluidStack(CRRFluids.WEAK_BRINE.get().getSource(), 200), new ItemStack(dust),
                FluidStack.EMPTY, MultiblockPreviews.drill(head, vein, sideItems())));
        recipes.add(new DrillingRecipe(new ItemStack(head), new ItemStack(vein), new FluidStack(CRRFluids.DRILL_GREASE.get().getSource(), 25), new ItemStack(dust),
                FluidStack.EMPTY, MultiblockPreviews.drill(head, vein, sideItems())));
    }

    private static List<ItemStack> sideItems() {
        return List.of(new ItemStack(CRRBlocks.DRILL_PIPE.get()), new ItemStack(CRRBlocks.DERRICK_BLOCK.get()), new ItemStack(CRRBlocks.DERRICK_TRUSS.get()));
    }
}
