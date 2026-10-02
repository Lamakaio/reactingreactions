package com.koala.reactingreactions.content.multiblock;

import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.content.electrolysis.ElectrodeInfoBlockEntity;
import com.koala.reactingreactions.content.multiblock.attachment.MachineAttachment;
import com.koala.reactingreactions.content.multiblock.attachment.MachineAttachments;
import com.koala.reactingreactions.registry.CRRItems;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

/** Controller of a hollow-box machine: scans its shell, and sizes its tanks and outputs to it. */
public abstract class MultiblockControllerBlockEntity<R extends ProcessingRecipe<RecipeInput, ?>> extends ProcessingMachineBlockEntity<R> {
    public record Spec<R extends ProcessingRecipe<RecipeInput, ?>>(String name, Predicate<BlockState> shell, Predicate<BlockState> controller,
            Predicate<BlockState> interior, int minSide, int maxSide, int minHeight, int maxHeight, BiPredicate<Integer, Integer> footprint,
            int itemInputs, int itemOutputs, boolean windows, Supplier<RecipeType<R>> recipeType) {
        public boolean isPart(BlockState state) {
            return shell.test(state) || controller.test(state);
        }
    }

    public static final int FLUID_INPUTS = 2;
    public static final int FLUID_OUTPUTS = 6;
    private static final int CAPACITY_PER_BLOCK_MB = 250;
    private static final int EXPANSION_TANK_MB = 4000;
    private static final int BASELINE_FOOTPRINT_AREA = 9;

    protected final Spec<R> spec;
    @Nullable
    protected HollowBoxScanner.Result structure;
    protected MachineAttachments attachments = MachineAttachments.NONE;
    // Worked out on the server (it depends on attachments) and synced, so the goggles show it.
    private int tankCapacity = CAPACITY_PER_BLOCK_MB;
    // The capability is built in the constructor, before a save's output count is read: the first scan rebuilds it.
    private boolean capabilitiesStale = true;
    /** Upgrades used on the machine (Outlet Manifolds, Gaskets), in the order they went in. */
    private final List<MachineAttachment.Kind> upgrades = new ArrayList<>();

    protected MultiblockControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Spec<R> spec) {
        super(type, pos, state, spec.itemInputs(), spec.itemOutputs());
        this.spec = spec;
        activeFluidOutputs = 1;
        refreshCapabilities();
    }

    @Override
    protected int fluidInputCount() {
        return FLUID_INPUTS;
    }

    @Override
    protected int fluidOutputCount() {
        return FLUID_OUTPUTS;
    }

    @Override
    protected int tankCapacity() {
        return CAPACITY_PER_BLOCK_MB;
    }

    @Override
    protected String name() {
        return spec.name();
    }

    @Override
    protected RecipeType<R> recipeType() {
        return spec.recipeType().get();
    }

    public Spec<R> getSpec() {
        return spec;
    }

    @Nullable
    public HollowBoxScanner.Result getStructure() {
        return structure;
    }

    // ---- structure ----

    /** How many output tanks a formed structure uses: a fixed-size one's tier, plus one per Outlet Manifold. */
    protected int outputRows(HollowBoxScanner.Result found) {
        MachineTiers.Tier tier = tier(found);
        if (tier != null) {
            return Math.min(FLUID_OUTPUTS, tier.outputs() + attachments.outlets());
        }
        return Math.max(1, Math.min(FLUID_OUTPUTS, found.sizeY() - 2));
    }

    /** Each tank's capacity once formed: a fixed-size one's tier plus its Expansion Tanks, else 250 mB per block of the structure. */
    protected int formedCapacity(HollowBoxScanner.Result found) {
        MachineTiers.Tier tier = tier(found);
        if (tier != null) {
            return tier.capacity() + EXPANSION_TANK_MB * attachments.expansionTanks();
        }
        return CAPACITY_PER_BLOCK_MB * found.volume();
    }

    // ---- fixed-size machines ----

    /** The fixed sizes this machine comes in, or null for a free-sized box. */
    @Nullable
    protected List<MachineTiers.Tier> tiers() {
        return null;
    }

    /** A fixed-size machine's tier and facing, if this structure is one of its sizes. */
    @Nullable
    protected MachineTiers.Fit fit(HollowBoxScanner.Result found) {
        List<MachineTiers.Tier> tiers = tiers();
        return tiers == null ? null : MachineTiers.fit(tiers, found, worldPosition);
    }

    /** The formed machine's tier and facing; null if it is not formed or not a fixed-size machine. */
    @Nullable
    public MachineTiers.Fit getFit() {
        return structure == null ? null : fit(structure);
    }

    @Nullable
    private MachineTiers.Tier tier(HollowBoxScanner.Result found) {
        MachineTiers.Fit fit = fit(found);
        return fit == null ? null : fit.tier();
    }

    /** Beyond the spec's bounds: a fixed-size machine forms only at one of its sizes, with its controller mid-side. */
    protected boolean acceptsShape(HollowBoxScanner.Result found) {
        return tiers() == null || fit(found) != null;
    }

    /** Gives the shell its formed look straight away: for Ponder, whose level never runs the controller's scan. */
    public void showFormed(Level level) {
        List<MachineTiers.Tier> tiers = tiers();
        HollowBoxScanner.Result found = HollowBoxScanner.scan(level, worldPosition, spec.shell(), spec.controller(), spec.interior(),
                spec.minSide(), spec.maxSide(), spec.minHeight(), spec.maxHeight(), spec.footprint());
        MachineTiers.Fit fit = tiers == null || found == null ? null : MachineTiers.fit(tiers, found, worldPosition);
        if (fit != null) {
            MachineTiers.apply(level, tiers, fit, found, spec::isPart, installed(MachineAttachment.Kind.OUTLET) > 0,
                    installed(MachineAttachment.Kind.GASKET) > 0);
        }
    }

    /** Called on every scan to give a fixed-size machine's shell its formed look, or take it back ({@code found} null). */
    protected void updateFormedLook(@Nullable HollowBoxScanner.Result previous, @Nullable HollowBoxScanner.Result found, boolean changed,
                                    boolean upgradesChanged) {
        List<MachineTiers.Tier> tiers = tiers();
        if (tiers == null) {
            return;
        }
        if (previous != null && (found == null || changed)) {
            MachineTiers.clear(level, previous, spec::isPart);
        }
        // After a reload the walls keep their look, so only a new shape, new upgrades or a plain controller needs it applied.
        if (found != null && (changed || upgradesChanged || getBlockState().getValue(MachineTiers.PART) == 0)) {
            MachineTiers.apply(level, tiers, fit(found), found, spec::isPart, installed(MachineAttachment.Kind.OUTLET) > 0,
                    installed(MachineAttachment.Kind.GASKET) > 0);
        }
    }

    // ---- upgrades ----

    /** The formed machine the block at {@code pos} belongs to (its controller, a wall or a terminal), or null. */
    @Nullable
    public static MultiblockControllerBlockEntity<?> of(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        MultiblockControllerBlockEntity<?> machine = be instanceof MultiblockControllerBlockEntity<?> own ? own
                : be instanceof MultiblockWallBlockEntity wall ? wall.getController()
                : be instanceof ElectrodeInfoBlockEntity info ? info.controller() : null;
        return machine != null && machine.getStructure() != null ? machine : null;
    }

    public int installed(MachineAttachment.Kind kind) {
        return (int) upgrades.stream().filter(kind::equals).count();
    }

    /** Takes an upgrade if this is a formed fixed-size machine with a free attachment slot. */
    public boolean installUpgrade(MachineAttachment.Kind kind) {
        if (structure == null || tiers() == null || attachments.mounted() >= attachments.slots()) {
            return false;
        }
        upgrades.add(kind);
        rescanStructure();
        setChanged();
        sendData();
        return true;
    }

    /** Takes the last installed upgrade back out, as its item; empty if there is none. */
    public ItemStack removeUpgrade() {
        if (upgrades.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack item = upgradeItem(upgrades.removeLast());
        rescanStructure();
        setChanged();
        sendData();
        return item;
    }

    private static ItemStack upgradeItem(MachineAttachment.Kind kind) {
        return new ItemStack(kind == MachineAttachment.Kind.OUTLET ? CRRItems.OUTLET_MANIFOLD.get() : CRRItems.GASKET.get());
    }

    @Override
    public void destroy() {
        if (level != null && !level.isClientSide) {
            for (MachineAttachment.Kind kind : upgrades) {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), upgradeItem(kind));
            }
            upgrades.clear();
        }
        // The walls outlive their controller, so they go back to plain blocks.
        if (structure != null && level != null && !level.isClientSide) {
            if (spec.windows()) {
                MultiblockWindows.clear(level, structure, spec::isPart);
            }
            updateFormedLook(structure, null, true, true);
        }
        super.destroy();
    }

    /** How many attachments the formed machine takes: its tier's slots, none for a free-sized one. */
    protected int attachmentSlots(HollowBoxScanner.Result found) {
        MachineTiers.Tier tier = tier(found);
        return tier == null ? 0 : tier.slots();
    }

    public MachineAttachments getAttachments() {
        return attachments;
    }

    /** A gauge's comparator signal: the progress, or how full the fullest output tank is. */
    public int gaugeSignal(boolean fill) {
        if (!fill) {
            return Math.round(progressFraction * 15);
        }
        float fullest = 0;
        for (int o = 0; o < activeFluidOutputs; o++) {
            var tank = fluidOutputs[o].getPrimaryHandler();
            fullest = Math.max(fullest, tank.getFluidAmount() / (float) Math.max(1, tank.getCapacity()));
        }
        return fullest <= 0 ? 0 : 1 + Math.round(fullest * 14);
    }

    @Override
    public float leakTightness() {
        return attachments.sealed() ? 0 : 1;
    }

    @Override
    protected void addStatusTooltip(List<Component> tooltip, BlockPos looked) {
        super.addStatusTooltip(tooltip, looked);
        MachineAttachments a = attachments;
        if (a.slots() == 0) {
            return;
        }
        tooltip.add(Component.literal(" - Attachments: " + Math.min(a.mounted(), a.slots()) + "/" + a.slots()
                + (a.mounted() > a.slots() ? " (" + (a.mounted() - a.slots()) + " unused)" : "")).withStyle(ChatFormatting.GRAY));
        if (a.pumpBonus() > 0) {
            tooltip.add(Component.literal(String.format(" - Circulation: +%.0f%% speed", a.pumpBonus() * 100)).withStyle(ChatFormatting.GRAY));
        }
        if (a.outlets() > 0) {
            tooltip.add(Component.literal(" - Outlet Manifolds: " + a.outlets()).withStyle(ChatFormatting.GRAY));
        }
        if (a.sealed()) {
            tooltip.add(Component.literal(" - Sealed: no leaks").withStyle(ChatFormatting.GRAY));
        }
    }

    /** Called on every scan, formed or not. */
    protected void onScanned(@Nullable HollowBoxScanner.Result found) {
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level != null && !level.isClientSide) {
            rescanStructure();
        }
    }

    private void rescanStructure() {
        HollowBoxScanner.Result found = HollowBoxScanner.scan(level, worldPosition, spec.shell(), spec.controller(), spec.interior(),
                spec.minSide(), spec.maxSide(), spec.minHeight(), spec.maxHeight(), spec.footprint());
        if (found != null && !acceptsShape(found)) {
            found = null;
        }
        MachineAttachments mounted = found == null ? MachineAttachments.NONE
                : MachineAttachments.scan(level, found, attachmentSlots(found), installed(MachineAttachment.Kind.OUTLET), installed(MachineAttachment.Kind.GASKET));
        boolean attachmentsChanged = !mounted.equals(attachments);
        attachments = mounted;
        int outputs = found == null ? 1 : outputRows(found);
        boolean changed = capabilitiesStale || outputs != activeFluidOutputs || !sameShape(found, structure);
        capabilitiesStale = false;
        int capacity = found == null ? CAPACITY_PER_BLOCK_MB : formedCapacity(found);
        HollowBoxScanner.Result previous = structure;
        structure = found;
        if (spec.windows()) {
            if (found != null) {
                MultiblockWindows.apply(level, found, spec::isPart, worldPosition);
            } else if (previous != null) {
                MultiblockWindows.clear(level, previous, spec::isPart);
            }
        }
        updateFormedLook(previous, found, changed, attachmentsChanged);
        onScanned(found);
        // Also every scan: after a reload the structure comes back from disk while the tanks still have their default capacity.
        if (capacity != tankCapacity) {
            tankCapacity = capacity;
            attachmentsChanged = true;
        }
        resizeTanks();
        for (BlockPos gauge : attachments.gauges()) {
            level.updateNeighbourForOutputSignal(gauge, level.getBlockState(gauge).getBlock());
        }
        if (attachmentsChanged && !changed) {
            setChanged();
            sendData();
        }
        if (changed) {
            activeFluidOutputs = outputs;
            refreshCapabilities();
            notifyCapabilityChanged(worldPosition);
            setChanged();
            sendData();
        }
        if (found != null) {
            HollowBoxScanner.forEachShellCell(found, pos -> {
                try {
                    if (!pos.equals(worldPosition) && level.getBlockEntity(pos) instanceof MultiblockWallBlockEntity wall) {
                        wall.setControllerPos(worldPosition);
                        if (changed) {
                            wall.notifyCapabilityChanged();
                        }
                    }
                } catch (Exception e) {
                    ReactingReactions.LOGGER.error("{} failed to update wall at {}", spec.name(), pos, e);
                }
            });
        }
    }

    private static boolean sameShape(@Nullable HollowBoxScanner.Result a, @Nullable HollowBoxScanner.Result b) {
        if (a == null || b == null) {
            return a == b;
        }
        return a.min().equals(b.min()) && a.sizeX() == b.sizeX() && a.sizeY() == b.sizeY() && a.sizeZ() == b.sizeZ();
    }

    private void notifyCapabilityChanged(BlockPos pos) {
        level.invalidateCapabilities(pos);
        level.updateNeighborsAt(pos, level.getBlockState(pos).getBlock());
    }

    private void resizeTanks() {
        int capacity = tankCapacity;
        // Only on change: setCapacity fires the tank's update callback.
        for (SmartFluidTankBehaviour[] tanks : new SmartFluidTankBehaviour[][] {fluidInputs, fluidOutputs}) {
            for (SmartFluidTankBehaviour tank : tanks) {
                if (tank.getPrimaryHandler().getCapacity() != capacity) {
                    tank.getPrimaryHandler().setCapacity(capacity);
                }
            }
        }
    }

    @Override
    protected boolean isReady() {
        return structure != null;
    }

    /** Bigger machines work faster, relative to a 3x3 footprint, and circulation pumps speed them up further. */
    @Override
    protected int duration(R recipe) {
        return Math.max(1, (int) Math.round(super.duration(recipe) * BASELINE_FOOTPRINT_AREA / (double) structure.footprintArea()
                / attachments.speedFactor()));
    }

    // ---- heat, for machines on burners ----

    /** Average burner heat (0-4) under the floor, over both diagonals of the footprint. */
    public double averageHeat() {
        if (structure == null || level == null) {
            return 0;
        }
        BlockPos anchor = structure.min();
        int n = structure.sizeX();
        int cells = 0;
        int total = 0;
        for (int i = 0; i < n; i++) {
            for (int side = 0; side < 2; side++) {
                int j = side == 0 ? i : n - 1 - i;
                if (side == 1 && j == i) {
                    continue; // the centre of an odd footprint is on both diagonals
                }
                cells++;
                BlockState below = level.getBlockState(anchor.offset(i, -1, j));
                if (below.hasProperty(BlazeBurnerBlock.HEAT_LEVEL)) {
                    total += below.getValue(BlazeBurnerBlock.HEAT_LEVEL).ordinal();
                }
            }
        }
        return cells == 0 ? 0 : (double) total / cells;
    }

    public BlazeBurnerBlock.HeatLevel heatLevel() {
        BlazeBurnerBlock.HeatLevel[] levels = BlazeBurnerBlock.HeatLevel.values();
        return levels[Math.min(levels.length - 1, (int) Math.floor(averageHeat()))];
    }

    protected boolean hasHeat(HeatCondition required) {
        return required == HeatCondition.NONE || required.testBlazeBurner(heatLevel());
    }

    protected void addHeatTooltip(List<Component> tooltip) {
        double heat = averageHeat();
        tooltip.add(Component.literal(String.format(" - Heat: %.2f / 4 (%s)", heat, heatLevel().name().toLowerCase(Locale.ROOT)))
                .withStyle(heat > 0 ? ChatFormatting.GRAY : ChatFormatting.RED));
    }

    // ---- rendering ----

    /** A random point inside the shell, {@code y} blocks above the floor row's bottom. */
    protected Vec3 randomInside(RandomSource random, double y) {
        BlockPos min = structure.min();
        return new Vec3(min.getX() + 1 + random.nextDouble() * (structure.sizeX() - 2), min.getY() + y,
                min.getZ() + 1 + random.nextDouble() * (structure.sizeZ() - 2));
    }

    /** Height of the shown liquid's surface above the floor row's bottom, or -1 for none or a gas. */
    protected double liquidSurface() {
        FluidStack fluid = getRenderedFluid();
        if (structure == null || fluid.isEmpty() || fluid.getFluidType().isLighterThanAir()) {
            return -1;
        }
        return MultiblockRenderer.HULL + Math.min(1f, getRenderedFill()) * (structure.sizeY() - 2 * MultiblockRenderer.HULL);
    }

    /** The centre of the roof's top face. */
    protected Vec3 roofTop() {
        BlockPos min = structure.min();
        return new Vec3(min.getX() + structure.sizeX() / 2.0, min.getY() + structure.sizeY(), min.getZ() + structure.sizeZ() / 2.0);
    }

    @Nullable
    private SmartFluidTankBehaviour renderedTank() {
        for (SmartFluidTankBehaviour tank : fluidInputs) {
            if (!tank.getPrimaryHandler().getFluid().isEmpty()) {
                return tank;
            }
        }
        for (int o = 0; o < activeFluidOutputs; o++) {
            if (!fluidOutputs[o].getPrimaryHandler().getFluid().isEmpty()) {
                return fluidOutputs[o];
            }
        }
        return null;
    }

    public FluidStack getRenderedFluid() {
        SmartFluidTankBehaviour tank = renderedTank();
        return tank == null ? FluidStack.EMPTY : tank.getPrimaryHandler().getFluid();
    }

    public float getRenderedFill() {
        SmartFluidTankBehaviour tank = renderedTank();
        return tank == null ? 0 : tank.getPrimaryHandler().getFluid().getAmount() / (float) Math.max(1, tank.getPrimaryHandler().getCapacity());
    }

    // ---- saving ----

    @Override
    protected void read(CompoundTag compound, Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        structure = compound.contains("StructureSizeX")
                ? new HollowBoxScanner.Result(NbtUtils.readBlockPos(compound, "StructureAnchor").orElse(worldPosition),
                        compound.getInt("StructureSizeX"), compound.getInt("StructureSizeY"), compound.getInt("StructureSizeZ"))
                : null;
        activeFluidOutputs = compound.contains("ActiveFluidOutputs") ? compound.getInt("ActiveFluidOutputs") : 1;
        if (compound.contains("TankCapacity")) {
            tankCapacity = compound.getInt("TankCapacity");
        }
        upgrades.clear();
        for (Tag upgrade : compound.getList("Upgrades", Tag.TAG_STRING)) {
            upgrades.add(MachineAttachment.Kind.valueOf(upgrade.getAsString()));
        }
        if (clientPacket) {
            attachments = new MachineAttachments(compound.getInt("Outlets"), compound.getInt("ExpansionTanks"), compound.getInt("Gaskets"),
                    compound.getFloat("PumpBonus"), List.of(), compound.getInt("Mounted"), compound.getInt("Slots"));
            // Capacity is only resized server-side; mirror it so the goggles show the right amount.
            resizeTanks();
        }
    }

    @Override
    public void write(CompoundTag compound, Provider registries, boolean clientPacket) {
        if (structure != null) {
            compound.put("StructureAnchor", NbtUtils.writeBlockPos(structure.min()));
            compound.putInt("StructureSizeX", structure.sizeX());
            compound.putInt("StructureSizeY", structure.sizeY());
            compound.putInt("StructureSizeZ", structure.sizeZ());
        }
        compound.putInt("ActiveFluidOutputs", activeFluidOutputs);
        compound.putInt("TankCapacity", tankCapacity);
        ListTag installed = new ListTag();
        upgrades.forEach(kind -> installed.add(StringTag.valueOf(kind.name())));
        compound.put("Upgrades", installed);
        if (clientPacket) {
            compound.putInt("Outlets", attachments.outlets());
            compound.putInt("ExpansionTanks", attachments.expansionTanks());
            compound.putInt("Gaskets", attachments.gaskets());
            compound.putFloat("PumpBonus", attachments.pumpBonus());
            compound.putInt("Mounted", attachments.mounted());
            compound.putInt("Slots", attachments.slots());
        }
        super.write(compound, registries, clientPacket);
    }

    @Override
    protected boolean addHeader(List<Component> tooltip) {
        if (structure == null) {
            tooltip.add(Component.literal(" - " + spec.name() + " (not formed)").withStyle(ChatFormatting.RED));
            return false;
        }
        tooltip.add(Component.literal(" - " + spec.name() + " (" + structure.sizeX() + "x" + structure.sizeZ() + "x" + structure.sizeY() + ")")
                .withStyle(ChatFormatting.GRAY));
        return true;
    }
}
