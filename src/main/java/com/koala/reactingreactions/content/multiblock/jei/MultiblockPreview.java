package com.koala.reactingreactions.content.multiblock.jei;

import com.koala.reactingreactions.content.multiblock.jei.MultiblockPreview.Cell;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * One JEI "recipe": a minimum-size multiblock laid out as individual block
 * cells (relative coordinates, y up) plus the extra facts to print next to it.
 */
public record MultiblockPreview(String name, Component title, ItemStack controllerItem, int sizeX, int sizeY, int sizeZ,
                                List<Cell> cells, List<ItemStack> sideItems, List<Component> notes) {
    public record Cell(int x, int y, int z, BlockState state) {
    }
}
