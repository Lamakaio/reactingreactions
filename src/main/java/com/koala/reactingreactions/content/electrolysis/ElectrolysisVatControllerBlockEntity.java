package com.koala.reactingreactions.content.electrolysis;

import com.koala.reactingreactions.content.electrolysis.recipe.ElectrolysisRecipe;
import com.koala.reactingreactions.content.multiblock.HollowBoxScanner;
import com.koala.reactingreactions.content.multiblock.MachineTiers;
import com.koala.reactingreactions.content.multiblock.MachineEffects;
import com.koala.reactingreactions.content.multiblock.MultiblockControllerBlockEntity;
import com.koala.reactingreactions.registry.CRRBlocks;
import com.koala.reactingreactions.registry.CRRRecipeTypes;
import com.koala.reactingreactions.registry.CRRSounds;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs on the voltage across two full-height electrode columns standing in its interior, each wired to the terminal in the end
 * wall beside it. Comes in two fixed sizes ({@link MachineTiers#ELECTROLYSIS_VAT}), the controller in the middle of a long side
 * one block above the floor.
 */
public class ElectrolysisVatControllerBlockEntity extends MultiblockControllerBlockEntity<ElectrolysisRecipe> {
    private static final int MIN_LONG_SIDE = 5;
    /** Per tier, the (x, y, z) of the two terminals as modelled (tools/machine_models.py, vat_terminals): in the end walls. */
    private static final int[][][] TERMINALS = {{{0, 1, 1}, {4, 1, 1}}, {{0, 1, 2}, {6, 1, 2}}};

    public static final Spec<ElectrolysisRecipe> SPEC = new Spec<>("Electrolysis Vat",
            state -> state.is(CRRBlocks.ELECTROLYSIS_VAT_WALL.get()) || state.is(CRRBlocks.ELECTROLYSIS_VAT_TERMINAL.get()),
            state -> state.is(CRRBlocks.ELECTROLYSIS_VAT_CONTROLLER.get()),
            state -> state.isAir() || state.getBlock() instanceof ElectrodeBlockBase,
            3, 7, 3, 4, (x, z) -> Math.max(x, z) >= MIN_LONG_SIDE, 6, 4, false, CRRRecipeTypes.ELECTROLYSIS::get);

    @Nullable
    private BlockPos electrodeA;
    @Nullable
    private BlockPos electrodeB;
    /** Draws 20 A at a recipe's minimum voltage. */
    private final Voltmeter voltmeter = new Voltmeter(20);

    public ElectrolysisVatControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, SPEC);
    }

    @Override
    protected List<MachineTiers.Tier> tiers() {
        return MachineTiers.ELECTROLYSIS_VAT;
    }

    /** Also needs its two terminals where the formed model draws their junction boxes. */
    @Nullable
    @Override
    protected String shapeRefusal(HollowBoxScanner.Result found) {
        String refusal = super.shapeRefusal(found);
        MachineTiers.Fit fit = fit(found);
        if (refusal != null || fit == null) {
            return refusal;
        }
        for (int[] terminal : TERMINALS[fit.index()]) {
            BlockPos at = MachineTiers.cell(found, fit, terminal[0], terminal[1], terminal[2]);
            if (!level.getBlockState(at).is(CRRBlocks.ELECTROLYSIS_VAT_TERMINAL.get())) {
                return "Needs a Terminal in each end wall, beside its electrode column";
            }
        }
        return null;
    }

    @Override
    protected void onScanned(@Nullable HollowBoxScanner.Result found) {
        rescanElectrodes();
    }

    /** Takes the first two interior columns made of electrodes from floor to roof, and remembers each column's top block. */
    private void rescanElectrodes() {
        electrodeA = null;
        electrodeB = null;
        if (structure == null) {
            return;
        }
        BlockPos anchor = structure.min();
        for (int x = 1; x < structure.sizeX() - 1 && electrodeB == null; x++) {
            for (int z = 1; z < structure.sizeZ() - 1 && electrodeB == null; z++) {
                BlockPos columnTop = findFullElectrodeColumn(anchor, x, z, structure.sizeY());
                if (columnTop == null) {
                    continue;
                }
                if (electrodeA == null) {
                    electrodeA = columnTop;
                } else {
                    electrodeB = columnTop;
                }
            }
        }
        linkElectrodeInfo();
    }

    @Nullable
    private BlockPos findFullElectrodeColumn(BlockPos anchor, int x, int z, int sizeY) {
        for (int y = 1; y < sizeY - 1; y++) {
            if (!(level.getBlockState(anchor.offset(x, y, z)).getBlock() instanceof ElectrodeBlockBase)) {
                return null;
            }
        }
        return anchor.offset(x, sizeY - 2, z);
    }

    /** Tells every electrode and terminal who its controller is, for their goggle info. */
    private void linkElectrodeInfo() {
        BlockPos anchor = structure.min();
        for (int x = 1; x < structure.sizeX() - 1; x++) {
            for (int z = 1; z < structure.sizeZ() - 1; z++) {
                for (int y = 1; y < structure.sizeY(); y++) {
                    if (level.getBlockEntity(anchor.offset(x, y, z)) instanceof ElectrodeInfoBlockEntity info) {
                        info.setControllerPos(worldPosition);
                    }
                }
            }
        }
        for (BlockPos top : new BlockPos[] {electrodeA, electrodeB}) {
            BlockPos terminal = top == null ? null : getTerminalFor(top);
            if (terminal != null && level.getBlockEntity(terminal) instanceof ElectrodeInfoBlockEntity info) {
                info.setControllerPos(worldPosition);
            }
        }
    }

    /** The terminal in the wall beside the column topped by {@code columnTop}, or null. */
    @Nullable
    public BlockPos getTerminalFor(BlockPos columnTop) {
        for (BlockPos pos : getElectrodeColumn(columnTop)) {
            for (Direction side : Direction.Plane.HORIZONTAL) {
                if (level.getBlockState(pos.relative(side)).is(CRRBlocks.ELECTROLYSIS_VAT_TERMINAL.get())) {
                    return pos.relative(side);
                }
            }
        }
        return null;
    }

    @Nullable
    public BlockPos getElectrodeA() {
        return electrodeA;
    }

    @Nullable
    public BlockPos getElectrodeB() {
        return electrodeB;
    }

    /** Every block of the column topped by {@code columnTop}, from the floor up. */
    public List<BlockPos> getElectrodeColumn(BlockPos columnTop) {
        if (structure == null) {
            return List.of();
        }
        List<BlockPos> column = new ArrayList<>(structure.sizeY() - 2);
        for (int y = 1; y < structure.sizeY() - 1; y++) {
            column.add(new BlockPos(columnTop.getX(), structure.min().getY() + y, columnTop.getZ()));
        }
        return column;
    }

    public Voltmeter voltmeter() {
        return voltmeter;
    }

    @Override
    protected void onServerTick() {
        if (voltmeter.needsSync()) {
            sendData();
        }
    }

    @Override
    protected boolean canRunNow(ElectrolysisRecipe candidate) {
        return voltmeter.reaches(candidate.minVoltage);
    }

    /** A recipe asking for specific electrodes wins: the electrodes are how the player picks the product. */
    @Override
    protected int recipeBonus(ElectrolysisRecipe candidate) {
        return candidate.electrodes.isEmpty() ? 0 : 1;
    }

    @Override
    protected boolean matchesExtra(ElectrolysisRecipe candidate) {
        return candidate.electrodes.isEmpty() || electrodeA != null && electrodeB != null
                && candidate.allowsElectrode(level.getBlockState(electrodeA).getBlock())
                && candidate.allowsElectrode(level.getBlockState(electrodeB).getBlock());
    }

    /** Its size does not change the pace, but circulation pumps raise it (given the power to match). */
    @Override
    protected int duration(ElectrolysisRecipe recipe) {
        int base = recipe.getProcessingDuration() > 0 ? recipe.getProcessingDuration() : 100;
        return Math.max(1, Math.round(base / attachments.speedFactor()));
    }

    @Override
    protected String whyNotNow(ElectrolysisRecipe recipe) {
        return voltmeter.shortfall(recipe);
    }

    @Override
    protected String whyNoWork(ElectrolysisRecipe recipe) {
        return "Not enough power: " + voltmeter.shortfall(recipe);
    }

    @Override
    protected String whyNotExtra(ElectrolysisRecipe recipe) {
        return electrodeA == null || electrodeB == null ? "Needs two electrode columns" : "Needs other electrodes for this";
    }

    @Override
    protected float workThisTick(ElectrolysisRecipe recipe) {
        return voltmeter.workThisTick(recipe, duration(recipe));
    }

    /** The resistance the electrolyte presents to the circuit (ElectrolysisVatDevice). */
    public double loadResistance() {
        return voltmeter.loadResistance(recipe);
    }

    @Override
    protected SoundEvent workingSound() {
        return CRRSounds.VAT_BUZZ.get();
    }

    /** Gas fizzes up both electrode columns, and their glands on the roof spark. */
    @Override
    protected void onClientWorkingTick(RandomSource random) {
        double surface = liquidSurface();
        for (BlockPos top : new BlockPos[] {electrodeA, electrodeB}) {
            if (top == null || structure == null) {
                continue;
            }
            if (surface > 1) {
                double y = structure.min().getY() + 1 + random.nextDouble() * (surface - 1);
                MachineEffects.bubble(level, random, FluidStack.EMPTY, top.getX() + 0.2 + random.nextDouble() * 0.6, y,
                        top.getZ() + 0.2 + random.nextDouble() * 0.6);
            }
            if (random.nextInt(8) == 0) {
                MachineEffects.spark(level, random, top.getX() + 0.5, top.getY() + 2.1, top.getZ() + 0.5);
            }
        }
    }

    @Override
    protected void read(CompoundTag compound, Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        electrodeA = NbtUtils.readBlockPos(compound, "ElectrodeA").orElse(null);
        electrodeB = NbtUtils.readBlockPos(compound, "ElectrodeB").orElse(null);
        voltmeter.read(compound, clientPacket);
    }

    @Override
    public void write(CompoundTag compound, Provider registries, boolean clientPacket) {
        if (electrodeA != null) {
            compound.put("ElectrodeA", NbtUtils.writeBlockPos(electrodeA));
        }
        if (electrodeB != null) {
            compound.put("ElectrodeB", NbtUtils.writeBlockPos(electrodeB));
        }
        voltmeter.write(compound, clientPacket);
        super.write(compound, registries, clientPacket);
    }

    /** Shown when the goggles look at an electrode or a terminal. */
    public boolean addElectrodeTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (structure == null) {
            return false;
        }
        tooltip.add(Component.literal(" - Electrolysis Vat electrodes").withStyle(ChatFormatting.GRAY));
        if (electrodeA == null || electrodeB == null) {
            tooltip.add(Component.literal(" - Electrodes: missing").withStyle(ChatFormatting.RED));
            return true;
        }
        tooltip.add(Component.literal(" - Electrodes: ").withStyle(ChatFormatting.GRAY)
                .append(electrodeName(electrodeA).copy().withStyle(ChatFormatting.WHITE))
                .append(Component.literal(" + ").withStyle(ChatFormatting.GRAY))
                .append(electrodeName(electrodeB).copy().withStyle(ChatFormatting.WHITE)));
        voltmeter.addTooltip(tooltip);
        if (isPlayerSneaking) {
            addWiringLine(tooltip, "A", electrodeA);
            addWiringLine(tooltip, "B", electrodeB);
        }
        return true;
    }

    private static void addWiringLine(List<Component> tooltip, String label, BlockPos electrode) {
        tooltip.add(Component.literal(String.format(" - %s: %s, wiring at %s / %s", label,
                electrode.toShortString(), electrode.above().toShortString(), electrode.above(2).toShortString())).withStyle(ChatFormatting.DARK_GRAY));
    }

    private Component electrodeName(BlockPos pos) {
        return level == null ? Component.literal("?") : level.getBlockState(pos).getBlock().getName();
    }
}
