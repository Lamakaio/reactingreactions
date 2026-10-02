package com.koala.reactingreactions.content.multiblock.jei;

import com.koala.reactingreactions.content.electrolysis.ElectrodeBlockBase;
import com.koala.reactingreactions.content.induction.InductionHeaterCoil;
import com.koala.reactingreactions.content.multiblock.jei.MultiblockPreview.Cell;
import com.koala.reactingreactions.registry.CRRBlocks;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The smallest valid build of each machine, with the wall facing the viewer left open above the floor. */
public final class MultiblockPreviews {
    private MultiblockPreviews() {
    }

    private static final Map<List<ResourceLocation>, MultiblockPreview> VATS = new HashMap<>();
    private static MultiblockPreview tower;
    private static MultiblockPreview oven;
    private static MultiblockPreview chamber;

    /** The vat preview for the electrodes a recipe allows (empty = any); built on first use and cached. */
    public static MultiblockPreview vat(List<ResourceLocation> allowed) {
        return VATS.computeIfAbsent(allowed, MultiblockPreviews::buildVat);
    }

    public static MultiblockPreview chamber() {
        if (chamber == null) chamber = buildChamber();
        return chamber;
    }

    public static MultiblockPreview oven() {
        if (oven == null) oven = buildOven();
        return oven;
    }

    public static MultiblockPreview tower() {
        if (tower == null) tower = buildTower();
        return tower;
    }

    private static MultiblockPreview buildVat(List<ResourceLocation> allowed) {
        // 5 long (x) x 3 wide (z) x 3 tall: solid floor and roof, walled rim.
        int sx = 5, sy = 3, sz = 3;
        BlockState wall = CRRBlocks.ELECTROLYSIS_VAT_WALL.get().defaultBlockState();
        BlockState controller = CRRBlocks.ELECTROLYSIS_VAT_CONTROLLER.get().defaultBlockState();
        BlockState terminalState = CRRBlocks.ELECTROLYSIS_VAT_TERMINAL.get().defaultBlockState();
        List<ItemStack> electrodes = new ArrayList<>();
        for (var entry : List.of(CRRBlocks.GRAPHITE_ELECTRODE, CRRBlocks.GOLD_STEEL_ELECTRODE, CRRBlocks.LEAD_ELECTRODE)) {
            if (allowed.isEmpty() || allowed.contains(entry.getId())) electrodes.add(new ItemStack(entry.get()));
        }
        // The model shows the first electrode that works for this recipe.
        BlockState electrode = ((BlockItem) electrodes.get(0).getItem()).getBlock()
                .defaultBlockState().setValue(ElectrodeBlockBase.FACING, Direction.UP);

        List<Cell> cells = new ArrayList<>();
        for (int x = 0; x < sx; x++) {
            for (int y = 0; y < sy; y++) {
                for (int z = 0; z < sz; z++) {
                    boolean rim = x == 0 || x == sx - 1 || z == 0 || z == sz - 1;
                    boolean shell = y == 0 || y == sy - 1 || rim;
                    if (!shell) continue;
                    boolean isController = x == 2 && y == 1 && z == 0;
                    // The terminals sit in the end walls, beside the electrode columns.
                    boolean terminal = y == 1 && z == 1 && (x == 0 || x == sx - 1);
                    cells.add(new Cell(x, y, z, isController ? controller : terminal ? terminalState : wall));
                }
            }
        }
        // Two electrode columns in the interior, from the floor up to the roof, beside the terminals.
        for (int y = 1; y < sy - 1; y++) {
            cells.add(new Cell(1, y, 1, electrode));
            cells.add(new Cell(3, y, 1, electrode));
        }

        List<Component> notes = List.of();

        return new MultiblockPreview("multiblock_preview/electrolysis_vat",
                CRRBlocks.ELECTROLYSIS_VAT_CONTROLLER.get().getName(),
                new ItemStack(CRRBlocks.ELECTROLYSIS_VAT_CONTROLLER.get()), sx, sy, sz, cells, electrodes, notes);
    }

    private static MultiblockPreview buildTower() {
        // 3x3 footprint, 3 tall: solid floor and roof, walled rim.
        int sx = 3, sy = 3, sz = 3;
        BlockState wall = CRRBlocks.DISTILLATION_TOWER_WALL.get().defaultBlockState();
        BlockState controller = CRRBlocks.DISTILLATION_TOWER_CONTROLLER.get().defaultBlockState();

        List<Cell> cells = new ArrayList<>();
        for (int x = 0; x < sx; x++) {
            for (int y = 0; y < sy; y++) {
                for (int z = 0; z < sz; z++) {
                    boolean rim = x == 0 || x == sx - 1 || z == 0 || z == sz - 1;
                    boolean shell = y == 0 || y == sy - 1 || rim;
                    if (!shell) continue;
                    boolean isController = x == 1 && y == 1 && z == 0;
                    cells.add(new Cell(x, y, z, isController ? controller : wall));
                }
            }
        }
        // The heat source sits under the floor, one block below the structure.
        BlockState burner = AllBlocks.BLAZE_BURNER.getDefaultState().setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.KINDLED);
        for (int i = 0; i < sx; i++) {
            cells.add(new Cell(i, -1, i, burner));
            if (sx - 1 - i != i) cells.add(new Cell(sx - 1 - i, -1, i, burner));
        }

        List<Component> notes = List.of();

        return new MultiblockPreview("multiblock_preview/distillation_tower",
                CRRBlocks.DISTILLATION_TOWER_CONTROLLER.get().getName(),
                new ItemStack(CRRBlocks.DISTILLATION_TOWER_CONTROLLER.get()), sx, sy + 1, sz, cells,
                List.of(new ItemStack(AllBlocks.BLAZE_BURNER.get())), notes);
    }

    private static MultiblockPreview buildOven() {
        // Fixed 3x3x3 brick shell with a solid floor and roof; no heat source needed.
        int size = 3;
        BlockState wall = CRRBlocks.AIRLESS_OVEN_WALL.get().defaultBlockState();
        BlockState controller = CRRBlocks.AIRLESS_OVEN_CONTROLLER.get().defaultBlockState();
        List<Cell> cells = new ArrayList<>();
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    boolean rim = x == 0 || x == size - 1 || z == 0 || z == size - 1;
                    if (!(y == 0 || y == size - 1 || rim)) continue;
                    cells.add(new Cell(x, y, z, x == 1 && y == 1 && z == 0 ? controller : wall));
                }
            }
        }
        return new MultiblockPreview("multiblock_preview/airless_oven",
                CRRBlocks.AIRLESS_OVEN_CONTROLLER.get().getName(),
                new ItemStack(CRRBlocks.AIRLESS_OVEN_CONTROLLER.get()), size, size, size, cells,
                List.of(), List.of());
    }

    private static MultiblockPreview buildChamber() {
        // The small reaction chamber: a 3x3x4 shell with a solid floor and roof; a steel-encased shaft takes the roof's centre cell
        // and Blaze Burners sit under the floor in a cross (both diagonals, like the Distillation Tower) for the heated reactions.
        int size = 3;
        int height = 4;
        BlockState wall = CRRBlocks.REACTION_CHAMBER_WALL.get().defaultBlockState();
        BlockState controller = CRRBlocks.REACTION_CHAMBER_CONTROLLER.get().defaultBlockState();
        BlockState shaft = CRRBlocks.STEEL_ENCASED_SHAFT.get().defaultBlockState();
        List<Cell> cells = new ArrayList<>();
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < size; z++) {
                    boolean rim = x == 0 || x == size - 1 || z == 0 || z == size - 1;
                    if (!(y == 0 || y == height - 1 || rim)) continue;
                    BlockState state = x == 1 && y == 1 && z == 0 ? controller : (x == 1 && z == 1 && y == height - 1 ? shaft : wall);
                    cells.add(new Cell(x, y, z, state));
                }
            }
        }
        BlockState burner = AllBlocks.BLAZE_BURNER.getDefaultState().setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.KINDLED);
        for (int i = 0; i < size; i++) {
            cells.add(new Cell(i, -1, i, burner));
            if (size - 1 - i != i) cells.add(new Cell(size - 1 - i, -1, i, burner));
        }
        return new MultiblockPreview("multiblock_preview/reaction_chamber",
                CRRBlocks.REACTION_CHAMBER_CONTROLLER.get().getName(),
                new ItemStack(CRRBlocks.REACTION_CHAMBER_CONTROLLER.get()), size, height + 1, size, cells,
                List.of(new ItemStack(CRRBlocks.STEEL_CASING.get()), new ItemStack(AllBlocks.SHAFT.get()), new ItemStack(AllBlocks.BLAZE_BURNER.get())), List.of());
    }

    /** A Derrick or Mineral Drill: the tower, a longer pipe, the given head on its end and the rich vein right under the head. */
    public static MultiblockPreview drill(Block head, Block vein, List<ItemStack> sideItems) {
        // The pipe below the tower is this many blocks long; the head hangs at y = 1 with the vein under it at y = 0.
        final int extraPipe = 4;
        final int base = 1 + extraPipe;
        BlockState derrick = CRRBlocks.DERRICK_BLOCK.get().defaultBlockState();
        BlockState truss = CRRBlocks.DERRICK_TRUSS.get().defaultBlockState();
        BlockState pipe = CRRBlocks.DRILL_PIPE.get().defaultBlockState();
        BlockState controller = CRRBlocks.DERRICK_CONTROLLER.get().defaultBlockState();
        List<Cell> cells = new ArrayList<>();
        cells.add(new Cell(2, 0, 2, vein.defaultBlockState()));
        cells.add(new Cell(2, 1, 2, head.defaultBlockState()));
        for (int y = 2; y <= base; y++) {
            cells.add(new Cell(2, y, 2, pipe));
        }
        // The tower: derrick blocks in the corners of every layer, trusses on the sides of the top and bottom layers, pipe up the middle.
        for (int layer = 1; layer <= 3; layer++) {
            int y = base + layer;
            cells.add(new Cell(2, y, 2, pipe));
            for (int x = 1; x <= 3; x += 2) {
                for (int z = 1; z <= 3; z += 2) {
                    cells.add(new Cell(x, y, z, derrick));
                }
            }
            if (layer != 2) {
                cells.add(new Cell(1, y, 2, truss));
                cells.add(new Cell(3, y, 2, truss));
                cells.add(new Cell(2, y, 1, truss));
                cells.add(new Cell(2, y, 3, truss));
            }
        }
        cells.add(new Cell(2, base + 4, 2, controller));
        return new MultiblockPreview("multiblock_preview/drill_" + BuiltInRegistries.BLOCK.getKey(head).getPath(),
                CRRBlocks.DERRICK_CONTROLLER.get().getName(),
                new ItemStack(CRRBlocks.DERRICK_CONTROLLER.get()), 5, base + 5, 5, cells, sideItems, List.of());
    }

    /** The smallest Induction Heater: a 3x3 of plates with the connector in the middle, shown kindled. */
    public static MultiblockPreview inductionHeater() {
        var plate = CRRBlocks.INDUCTION_HEATER_PLATE.get().defaultBlockState()
                .setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.KINDLED);
        var connector = CRRBlocks.INDUCTION_HEATER_CONNECTOR.get().defaultBlockState()
                .setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.KINDLED);
        List<Cell> cells = new ArrayList<>();
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                var state = x == 1 && z == 1 ? connector : plate;
                for (Direction side : Direction.Plane.HORIZONTAL) {
                    state = state.setValue(InductionHeaterCoil.of(side),
                            InductionHeaterCoil.connects(x, z, side, 0, 2, 0, 2));
                }
                state = state.setValue(InductionHeaterCoil.CIRCLE,
                        InductionHeaterCoil.isCircle(x, z, 0, 2, 0, 2));
                cells.add(new Cell(x, 0, z, state));
            }
        }
        return new MultiblockPreview("multiblock_preview/induction_heater",
                CRRBlocks.INDUCTION_HEATER_CONNECTOR.get().getName(),
                new ItemStack(CRRBlocks.INDUCTION_HEATER_CONNECTOR.get()), 3, 1, 3, cells,
                List.of(new ItemStack(CRRBlocks.INDUCTION_HEATER_PLATE.get()),
                        new ItemStack(CRRBlocks.INDUCTION_HEATER_CONNECTOR.get())), List.of());
    }
}
