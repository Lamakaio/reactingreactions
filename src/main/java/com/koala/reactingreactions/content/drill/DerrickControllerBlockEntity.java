package com.koala.reactingreactions.content.drill;

import com.koala.reactingreactions.content.compat.DieselGeneratorsCompat;
import com.koala.reactingreactions.content.multiblock.InputFillOutputDrainWrapper;
import com.koala.reactingreactions.content.multiblock.NonInsertableItemHandler;
import com.koala.reactingreactions.content.toxic.LeakInfo;
import com.koala.reactingreactions.registry.CRRBlocks;
import com.koala.reactingreactions.registry.CRRFluids;
import com.koala.reactingreactions.registry.CRRItems;
import com.koala.reactingreactions.registry.CRRSounds;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tops a {@link DrillRig}. The head at the end of the pipe decides the job: an oil head pumps crude oil and drinks lubricant,
 * a mineral head pulls up rock dust and drinks coolant. Needs rotation into its back face.
 */
public class DerrickControllerBlockEntity extends KineticBlockEntity {
    public static final float MIN_RPM = DrillRates.MIN_RPM;
    private static final float MAX_RPM = DrillRates.MAX_RPM;
    private static final int MAX_PIPE_LENGTH = 96;
    /** A deposit at least this rich runs the rig at its full rate; a poorer one is proportionally slower. */
    private static final int FULL_RICHNESS = 30;
    /** Crude oil per tick, or dust items per tick (once past 1/tick), at 32 RPM in a full-richness vein; linear up to {@link #MAX_RPM}. */
    private static final double BASE_OIL_RATE = DrillRates.BASE_OIL_RATE;
    /** One dust item costs as long as 500 mb of oil at the same RPM/richness, matching {@link #BASE_OIL_RATE}'s 0.5 mb/tick. */
    private static final double BASE_ITEM_RATE = DrillRates.BASE_ITEM_RATE;
    private static final int OIL_CAPACITY_MB = 8000;
    private static final int LUBRICANT_CAPACITY_MB = 2000;
    private static final int COOLANT_CAPACITY_MB = 1000;
    /** One millibucket of lubricant/coolant per this many working ticks. */
    private static final int LUBRICANT_TICKS_PER_MB = 60;
    private static final int COOLANT_TICKS_PER_MB = 40;
    private static final int OUTPUT_SLOT_CAP = 64;

    /** The Rich Vein blocks a steel bit can drill, and the dust each one yields. */
    private static final Map<Block, ItemStack> STEEL_ROCKS = new LinkedHashMap<>();
    /** The Rich Vein blocks a diamond bit can drill (the richer set), and the dust each one yields. */
    private static final Map<Block, ItemStack> MID_ROCKS = new LinkedHashMap<>();
    /** The titanium bit drills every rock, faster. */
    private static final Map<Block, ItemStack> ALL_ROCKS = new LinkedHashMap<>();

    static {
        STEEL_ROCKS.put(CRRBlocks.RICH_TUFF_VEIN.get(), new ItemStack(CRRItems.TUFF_DUST.get()));
        STEEL_ROCKS.put(CRRBlocks.RICH_GRANITE_VEIN.get(), new ItemStack(CRRItems.GRANITE_DUST.get()));
        STEEL_ROCKS.put(CRRBlocks.RICH_DIORITE_VEIN.get(), new ItemStack(CRRItems.DIORITE_DUST.get()));
        STEEL_ROCKS.put(CRRBlocks.RICH_SCORIA_VEIN.get(), new ItemStack(CRRItems.SCORIA_DUST.get()));
        MID_ROCKS.put(CRRBlocks.RICH_ASURINE_VEIN.get(), new ItemStack(CRRItems.ASURINE_DUST.get()));
        MID_ROCKS.put(CRRBlocks.RICH_OCHRUM_VEIN.get(), new ItemStack(CRRItems.OCHRUM_DUST.get()));
        MID_ROCKS.put(CRRBlocks.RICH_VERIDIUM_VEIN.get(), new ItemStack(CRRItems.VERIDIUM_DUST.get()));
        MID_ROCKS.put(CRRBlocks.RICH_CRIMSITE_VEIN.get(), new ItemStack(CRRItems.CRIMSITE_DUST.get()));
        ALL_ROCKS.putAll(STEEL_ROCKS);
        ALL_ROCKS.putAll(MID_ROCKS);
    }

    private SmartFluidTankBehaviour oil;
    private SmartFluidTankBehaviour lubricant;
    private SmartFluidTankBehaviour coolant;
    private final ItemStackHandler output = new ItemStackHandler(1);
    private IFluidHandler fluidCapability;
    private IItemHandlerModifiable itemCapability;

    private final List<BlockPos> ioBlocks = new ArrayList<>();
    private boolean formed;
    private boolean running;
    private int pipeLength;
    private int richness;
    /** Which kind of head is on the end of the pipe right now. */
    private HeadKind headKind = HeadKind.NONE;
    /** What a mineral head is currently pulling out of the ground, for the item output and its break-particle colour; empty when idle or oil. */
    private ItemStack currentOutput = ItemStack.EMPTY;
    private double accumulator;
    private int fluidTimer;

    // Saved by ordinal: only ever append.
    private enum HeadKind { NONE, OIL, MINERAL_STEEL, MINERAL_TITANIUM, MINERAL_DIAMOND }

    public DerrickControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event, BlockEntityType<DerrickControllerBlockEntity> type) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, type, (be, ctx) -> be.getFluidCapability());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type, (be, ctx) -> be.getItemCapability());
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        oil = new SmartFluidTankBehaviour(SmartFluidTankBehaviour.OUTPUT, this, 1, OIL_CAPACITY_MB, false).forbidInsertion();
        lubricant = new SmartFluidTankBehaviour(new com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType<>("crr_lubricant"), this, 1, LUBRICANT_CAPACITY_MB, false)
                .forbidExtraction();
        lubricant.getPrimaryHandler().setValidator(DerrickControllerBlockEntity::isLubricant);
        coolant = new SmartFluidTankBehaviour(new com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType<>("crr_coolant"), this, 1, COOLANT_CAPACITY_MB, false)
                .forbidExtraction();
        coolant.getPrimaryHandler().setValidator(DerrickControllerBlockEntity::isCoolant);
        behaviours.add(oil);
        behaviours.add(lubricant);
        behaviours.add(coolant);
    }

    public static boolean isLubricant(FluidStack fluid) {
        return fluid.getFluid().isSame(CRRFluids.SEED_OIL.get().getSource()) || fluid.getFluid().isSame(CRRFluids.MINERAL_OIL.get().getSource());
    }

    /** What pipes see, from the controller and from every derrick block: lubricant and coolant go in, crude oil comes out. */
    public IFluidHandler getFluidCapability() {
        if (fluidCapability == null) {
            fluidCapability = new InputFillOutputDrainWrapper(new IFluidHandler[] { oil.getCapability() },
                    new IFluidHandler[] { lubricant.getCapability(), coolant.getCapability() }, fluid -> true);
        }
        return fluidCapability;
    }

    /** What pipes/hoppers see for items: only the dust output, from a mineral head. */
    public IItemHandlerModifiable getItemCapability() {
        if (itemCapability == null) {
            // Hoppers and pipes can only take dust out, never put anything in.
            itemCapability = new NonInsertableItemHandler(output);
        }
        return itemCapability;
    }

    /** Crude oil per second at a given RPM in a full-richness vein (for the recipe viewer). */
    public static double oilMbPerSecond(float rpm) {
        return DrillRates.oilMbPerSecond(rpm);
    }

    /** Seconds per dust item at a given RPM in a full-richness vein (for the recipe viewer). */
    public static double secondsPerItem(float rpm) {
        return DrillRates.secondsPerItem(rpm);
    }

    public static float minRpm() {
        return MIN_RPM;
    }

    public static float maxRpm() {
        return MAX_RPM;
    }

    @Override
    public void remove() {
        if (level != null && !level.isClientSide && running) {
            setHeadSpinning(false);
        }
        super.remove();
    }

    /** Hands the head's drawing over to {@link DerrickRenderer} while it turns, and back when it stops. */
    private void setHeadSpinning(boolean spinning) {
        BlockPos head = DrillRig.headPos(worldPosition, pipeLength);
        BlockState state = level.getBlockState(head);
        if (state.getBlock() instanceof DrillHeadBlock && state.getValue(DrillHeadBlock.SPINNING) != spinning) {
            level.setBlock(head, state.setValue(DrillHeadBlock.SPINNING, spinning), Block.UPDATE_CLIENTS);
        }
    }

    public boolean isRunning() {
        return running;
    }

    public int getRichness() {
        return richness;
    }

    /** "Oil", "Steel mineral head", "Titanium mineral head" or "No head", for overlays. */
    public String headDescription() {
        return switch (headKind) {
            case OIL -> "Oil head";
            case MINERAL_STEEL -> "Steel mineral head";
            case MINERAL_TITANIUM -> "Titanium mineral head";
            case MINERAL_DIAMOND -> "Diamond mineral head";
            case NONE -> "No head";
        };
    }

    public int getPipeLength() {
        return pipeLength;
    }

    @Override
    public void destroy() {
        // The tower outlives its controller, so its blocks go back to plain.
        if (level != null && !level.isClientSide) {
            DrillRig.setLook(level, worldPosition, false);
        }
        super.destroy();
    }

    public boolean isFormed() {
        return formed;
    }

    public boolean isIoBlock(BlockPos pos) {
        return ioBlocks.contains(pos);
    }

    private static boolean is(Level level, BlockPos pos, Block block) {
        return level.getBlockState(pos).is(block);
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level == null || level.isClientSide) {
            return;
        }
        List<BlockPos> io = new ArrayList<>();
        boolean nowFormed = DrillRig.checkTower(level, worldPosition, io);
        int length = 0;
        HeadKind kind = HeadKind.NONE;
        int newRichness = 0;
        if (nowFormed) {
            length = DrillRig.pipeLength(level, worldPosition, MAX_PIPE_LENGTH);
            BlockPos head = DrillRig.headPos(worldPosition, length);
            // Create Diesel Generators replaces oil drilling.
            if (is(level, head, CRRBlocks.OIL_DRILL_HEAD.get()) && !DieselGeneratorsCompat.isLoaded()) {
                kind = HeadKind.OIL;
                newRichness = touchingRichness(head, CRRBlocks.RICH_OIL_VEIN.get());
            } else if (is(level, head, CRRBlocks.MINERAL_DRILL_HEAD_STEEL.get())) {
                kind = HeadKind.MINERAL_STEEL;
            } else if (is(level, head, CRRBlocks.MINERAL_DRILL_HEAD_TITANIUM.get())) {
                kind = HeadKind.MINERAL_TITANIUM;
            } else if (is(level, head, CRRBlocks.MINERAL_DRILL_HEAD_DIAMOND.get())) {
                kind = HeadKind.MINERAL_DIAMOND;
            }
        }
        ItemStack newOutput = ItemStack.EMPTY;
        if (kind == HeadKind.MINERAL_STEEL || kind == HeadKind.MINERAL_TITANIUM || kind == HeadKind.MINERAL_DIAMOND) {
            BlockPos head = DrillRig.headPos(worldPosition, length);
            var rocks = switch (kind) {
                case MINERAL_STEEL -> STEEL_ROCKS;
                case MINERAL_DIAMOND -> MID_ROCKS;
                default -> ALL_ROCKS;
            };
            Block found = null;
            int best = 0;
            for (Block vein : rocks.keySet()) {
                int richest = touchingRichness(head, vein);
                if (richest > best) {
                    best = richest;
                    found = vein;
                }
            }
            if (found != null) {
                // Each deposit carries its own richness; nothing is counted or weighted around it.
                newRichness = best;
                newOutput = rocks.get(found);
            }
        }
        boolean changed = nowFormed != formed || length != pipeLength || newRichness != richness || headKind != kind
                || !ItemStack.matches(newOutput, currentOutput);
        formed = nowFormed;
        pipeLength = length;
        richness = newRichness;
        headKind = kind;
        currentOutput = newOutput;
        ioBlocks.clear();
        ioBlocks.addAll(io);
        // Every scan, cheap when nothing changes: keeps the look right after a reload or a block swapped back in.
        DrillRig.setLook(level, worldPosition, formed);
        if (formed) {
            for (BlockPos pos : io) {
                if (level.getBlockEntity(pos) instanceof DerrickBlockEntity derrick) {
                    derrick.setControllerPos(worldPosition);
                }
            }
        }
        if (changed) {
            setChanged();
            sendData();
        }
    }

    /**
     * The richness of the richest {@code vein} block touching the head (the 3x3x3 around it), or 0 if none does. Plain rock and
     * plain oil shale never count: only a Rich Vein block does, see {@link RichOreVeinBlock}.
     */
    private int touchingRichness(BlockPos head, Block vein) {
        int best = 0;
        for (BlockPos pos : BlockPos.betweenClosed(head.offset(-1, -1, -1), head.offset(1, 1, 1))) {
            BlockState state = level.getBlockState(pos);
            if (state.is(vein)) {
                best = Math.max(best, RichOreVeinBlock.richnessOf(state));
            }
        }
        return best;
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null) {
            return;
        }
        if (level.isClientSide) {
            spawnParticles();
            return;
        }
        float rpm = Math.abs(getSpeed());
        boolean canRun = switch (headKind) {
            case OIL -> formed && richness > 0 && rpm >= MIN_RPM && lubricant.getPrimaryHandler().getFluidAmount() > 0
                    && oil.getPrimaryHandler().getFluidAmount() < OIL_CAPACITY_MB;
            case MINERAL_STEEL, MINERAL_TITANIUM, MINERAL_DIAMOND -> formed && richness > 0 && !currentOutput.isEmpty() && rpm >= MIN_RPM
                    && coolant.getPrimaryHandler().getFluidAmount() > 0 && outputHasRoom();
            case NONE -> false;
        };
        if (canRun != running) {
            running = canRun;
            setHeadSpinning(running);
            sendData();
        }
        if (!running) {
            return;
        }
        if (level.getGameTime() % 24 == 0) {
            level.playSound(null, worldPosition, CRRSounds.DRILL_RUMBLE.get(), SoundSource.BLOCKS,
                    0.5F, 0.8F + Math.min(rpm, MAX_RPM) / MAX_RPM * 0.4F);
        }
        if (headKind == HeadKind.OIL) {
            accumulator += BASE_OIL_RATE * (Math.min(rpm, MAX_RPM) / MIN_RPM) * Math.min(1.0, richness / (double) FULL_RICHNESS);
            int whole = (int) accumulator;
            if (whole > 0) {
                accumulator -= whole;
                oil.getPrimaryHandler().fill(new FluidStack(CRRFluids.CRUDE_OIL.get().getSource(), whole), IFluidHandler.FluidAction.EXECUTE);
            }
            if (++fluidTimer >= LUBRICANT_TICKS_PER_MB) {
                fluidTimer = 0;
                lubricant.getPrimaryHandler().drain(1, IFluidHandler.FluidAction.EXECUTE);
            }
        } else {
            double bitSpeed = headKind == HeadKind.MINERAL_TITANIUM ? DrillRates.TITANIUM_SPEED : 1.0;
            accumulator += BASE_ITEM_RATE * bitSpeed * (Math.min(rpm, MAX_RPM) / MIN_RPM) * Math.min(1.0, richness / (double) FULL_RICHNESS);
            int whole = (int) accumulator;
            if (whole > 0) {
                // Only what actually went into the slot is taken off the accumulator (insertOutput returns the leftover).
                ItemStack leftover = insertOutput(currentOutput.copyWithCount(whole));
                accumulator -= whole - leftover.getCount();
                // A full output slot does not bank progress for later.
                accumulator = Math.min(accumulator, 0.999);
            }
            if (++fluidTimer >= coolantTicksPerMb(coolant.getPrimaryHandler().getFluid())) {
                fluidTimer = 0;
                coolant.getPrimaryHandler().drain(1, IFluidHandler.FluidAction.EXECUTE);
            }
        }
    }

    private void spawnParticles() {
        if (!running) {
            return;
        }
        if (headKind == HeadKind.OIL) {
            // The gas flare: several flame particles per tick, since one cannot be made bigger.
            for (int i = 0; i < 3; i++) {
                double x = worldPosition.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.3;
                double z = worldPosition.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.3;
                double y = worldPosition.getY() + 1.05 + level.random.nextDouble() * 0.5;
                level.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0.02, 0);
            }
            if (level.random.nextInt(3) == 0) {
                double x = worldPosition.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.2;
                double z = worldPosition.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.2;
                level.addParticle(ParticleTypes.SMOKE, x, worldPosition.getY() + 1.6, z, 0, 0.03, 0);
            }
        } else if (!currentOutput.isEmpty() && level.getGameTime() % 2 == 0) {
            // Item-break particles of whatever is coming out (the dust itself, since that is what the drill produces).
            for (int i = 0; i < 3; i++) {
                double x = worldPosition.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.3;
                double z = worldPosition.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.3;
                double y = worldPosition.getY() + 1.05 + level.random.nextDouble() * 0.5;
                level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, currentOutput), x, y, z, 0, 0.05, 0);
            }
        }
    }

    /** Ethanol coolant; 1% brine, cheaper but used twice as fast; or drill grease, lasting four times longer. */
    public static boolean isCoolant(FluidStack fluid) {
        return coolantTicksPerMb(fluid) > 0;
    }

    /** Working ticks one millibucket of this coolant lasts, or 0 if it is not a coolant. */
    public static int coolantTicksPerMb(FluidStack fluid) {
        if (fluid.getFluid().isSame(CRRFluids.COOLANT.get().getSource())) {
            return COOLANT_TICKS_PER_MB;
        }
        if (fluid.getFluid().isSame(CRRFluids.WEAK_BRINE.get().getSource())) {
            return COOLANT_TICKS_PER_MB / 2;
        }
        if (fluid.getFluid().isSame(CRRFluids.DRILL_GREASE.get().getSource())) {
            return COOLANT_TICKS_PER_MB * 4;
        }
        return 0;
    }

    /** Whether the output slot can take one more of what is being drilled (a full slot pauses the drill and its coolant use). */
    private boolean outputHasRoom() {
        ItemStack existing = output.getStackInSlot(0);
        return existing.isEmpty() || (ItemStack.isSameItemSameComponents(existing, currentOutput) && existing.getCount() < OUTPUT_SLOT_CAP);
    }

    private ItemStack insertOutput(ItemStack stack) {
        ItemStack existing = output.getStackInSlot(0);
        if (!existing.isEmpty() && (!ItemStack.isSameItemSameComponents(existing, stack) || existing.getCount() >= OUTPUT_SLOT_CAP)) {
            return stack;
        }
        int room = OUTPUT_SLOT_CAP - existing.getCount();
        int accepted = Math.min(room, stack.getCount());
        if (accepted <= 0) {
            return stack;
        }
        ItemStack toStore = existing.isEmpty() ? stack.copyWithCount(accepted) : existing.copyWithCount(existing.getCount() + accepted);
        output.setStackInSlot(0, toStore);
        return stack.copyWithCount(stack.getCount() - accepted);
    }

    @Override
    protected void read(CompoundTag compound, Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        output.deserializeNBT(registries, compound.getCompound("Output"));
        formed = compound.getBoolean("Formed");
        running = compound.getBoolean("Running");
        pipeLength = compound.getInt("PipeLength");
        richness = compound.getInt("Richness");
        headKind = HeadKind.values()[compound.getInt("HeadKind")];
        currentOutput = compound.contains("CurrentOutput") ? ItemStack.parseOptional(registries, compound.getCompound("CurrentOutput")) : ItemStack.EMPTY;
    }

    @Override
    public void write(CompoundTag compound, Provider registries, boolean clientPacket) {
        compound.put("Output", output.serializeNBT(registries));
        compound.putBoolean("Formed", formed);
        compound.putBoolean("Running", running);
        compound.putInt("PipeLength", pipeLength);
        compound.putInt("Richness", richness);
        compound.putInt("HeadKind", headKind.ordinal());
        if (!currentOutput.isEmpty()) {
            compound.put("CurrentOutput", currentOutput.save(registries));
        }
        super.write(compound, registries, clientPacket);
    }

    /** On the controller: everything, including the rotation and stress. */
    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        LeakInfo.append(tooltip, this);
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        return addStatus(tooltip, true);
    }

    /** On a Derrick block: the status, vein and fluids, without the rotation and stress (those belong to the controller). */
    public boolean addFrameTooltip(List<Component> tooltip) {
        LeakInfo.append(tooltip, this);
        return addStatus(tooltip, false);
    }

    private boolean addStatus(List<Component> tooltip, boolean atController) {
        if (!formed) {
            tooltip.add(Component.literal(" - Derrick (not formed)").withStyle(ChatFormatting.RED));
            return true;
        }
        if (headKind == HeadKind.NONE) {
            tooltip.add(Component.literal(" - Derrick (no drill head)").withStyle(ChatFormatting.RED));
            return true;
        }
        boolean oilMode = headKind == HeadKind.OIL;
        tooltip.add(Component.literal(" - " + (oilMode ? "Derrick" : "Mineral Drill") + (running ? " (working)" : "")).withStyle(ChatFormatting.GRAY));
        if (oilMode) {
            tooltip.add(Component.literal(String.format(" - Drill head touching the vein: %s, richness %d / %d", richness > 0 ? "yes" : "no", richness, FULL_RICHNESS))
                    .withStyle(richness > 0 ? ChatFormatting.GRAY : ChatFormatting.RED));
        } else {
            tooltip.add(Component.literal(" - Drilling: " + (currentOutput.isEmpty() ? "nothing reachable" : currentOutput.getHoverName().getString()))
                    .withStyle(currentOutput.isEmpty() ? ChatFormatting.RED : ChatFormatting.GRAY));
            tooltip.add(Component.literal(String.format(" - Vein richness: %d / %d", richness, FULL_RICHNESS))
                    .withStyle(richness > 0 ? ChatFormatting.GRAY : ChatFormatting.RED));
        }
        if (atController) {
            tooltip.add(Component.literal(String.format(" - Rotation: %.0f RPM (needs %.0f+)", Math.abs(getSpeed()), MIN_RPM))
                    .withStyle(Math.abs(getSpeed()) >= MIN_RPM ? ChatFormatting.GRAY : ChatFormatting.RED));
        }
        if (oilMode) {
            FluidStack lube = lubricant.getPrimaryHandler().getFluid();
            tooltip.add(Component.literal(" - Lubricant: " + lube.getAmount() + "/" + LUBRICANT_CAPACITY_MB + " mb (seed oil or mineral oil)")
                    .withStyle(lube.isEmpty() ? ChatFormatting.RED : ChatFormatting.GRAY));
            FluidStack stored = oil.getPrimaryHandler().getFluid();
            tooltip.add(Component.literal(" - Crude oil: " + stored.getAmount() + "/" + OIL_CAPACITY_MB + " mb").withStyle(ChatFormatting.GRAY));
        } else {
            FluidStack water = coolant.getPrimaryHandler().getFluid();
            tooltip.add(Component.literal(" - Coolant: " + water.getAmount() + "/" + COOLANT_CAPACITY_MB + " mb" + (water.isEmpty() ? "" : " " + water.getHoverName().getString()))
                    .withStyle(water.isEmpty() ? ChatFormatting.RED : ChatFormatting.GRAY));
        }
        return true;
    }
}
