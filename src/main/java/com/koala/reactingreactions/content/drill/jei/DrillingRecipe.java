package com.koala.reactingreactions.content.drill.jei;

import com.koala.reactingreactions.content.multiblock.jei.MultiblockPreview;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * One row of the recipe viewer for the Derrick / Mineral Drill. These are not real recipes (the rig has no recipe type): the
 * viewer builds them from the heads and rich veins, so they always match what the drill does.
 *
 * @param head       the head on the end of the pipe
 * @param vein       the rich vein it has to touch
 * @param consumable the lubricant or coolant the rig drinks
 * @param outputItem the dust, or empty for oil
 * @param outputFluid crude oil, or empty for dust
 */
public record DrillingRecipe(ItemStack head, ItemStack vein, FluidStack consumable, ItemStack outputItem, FluidStack outputFluid,
                             MultiblockPreview preview) {
}
