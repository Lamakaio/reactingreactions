package com.koala.reactingreactions.content.multiblock;

import com.koala.reactingreactions.content.multiblock.attachment.MachineAttachment;
import com.koala.reactingreactions.content.multiblock.attachment.OutletValveBlockEntity;
import com.koala.reactingreactions.content.toxic.LeakInfo;
import com.koala.reactingreactions.content.toxic.TankSealing;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A machine with input and output tanks and item slots that runs one recipe type. Pipes and hoppers fill the inputs and
 * drain the outputs, and each distinct output fluid gets its own tank.
 */
public abstract class ProcessingMachineBlockEntity<R extends ProcessingRecipe<RecipeInput, ?>> extends SmartBlockEntity
        implements IHaveGoggleInformation {
    // Each tank needs its own behaviour type, or only one of them is ticked, saved and synced.
    private static final BehaviourType<SmartFluidTankBehaviour>[] INPUT_TYPES = types("crr_fluid_in", 2);
    private static final BehaviourType<SmartFluidTankBehaviour>[] OUTPUT_TYPES = types("crr_fluid_out", 6);
    private static final int SEARCH_COOLDOWN_TICKS = 10;

    protected SmartFluidTankBehaviour[] fluidInputs;
    protected SmartFluidTankBehaviour[] fluidOutputs;
    protected final ItemStackHandler itemInputs;
    protected final ItemStackHandler itemOutputs;
    private IFluidHandler fluidCapability;
    private IItemHandlerModifiable itemCapability;
    protected int activeFluidOutputs;
    @Nullable
    protected R recipe;
    /** Ticks' worth of work done on the current recipe: a fraction when power limits it (see {@link #workThisTick}). */
    protected float timer;
    /** Why the machine is idle although it holds something (server), or the synced copy (client); null when working or empty. */
    @Nullable
    private String blocker;
    private int searchCooldown;
    protected float progressFraction;
    private int lastSyncedProgressStep = -1;
    /** Whether the recipe advanced this tick; synced so clients can animate. */
    protected boolean running;
    // Client only: eases toward the synced progress, so a dial's needle moves smoothly.
    private float shownProgress;
    private float previousShownProgress;

    protected ProcessingMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int itemInputCount, int itemOutputCount) {
        super(type, pos, state);
        itemInputs = new ItemStackHandler(itemInputCount);
        itemOutputs = new ItemStackHandler(itemOutputCount);
        activeFluidOutputs = fluidOutputs.length;
        refreshCapabilities();
    }

    @SuppressWarnings("unchecked")
    private static BehaviourType<SmartFluidTankBehaviour>[] types(String prefix, int count) {
        BehaviourType<SmartFluidTankBehaviour>[] types = new BehaviourType[count];
        for (int i = 0; i < count; i++) {
            types[i] = new BehaviourType<>(prefix + i);
        }
        return types;
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event, BlockEntityType<? extends ProcessingMachineBlockEntity<?>> type) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, type, (be, ctx) -> {
            // An Outlet Valve on the controller pulls through its own filtered outlet.
            IFluidHandler valve = be.getLevel() == null ? null : OutletValveBlockEntity.sourceFor(be.getLevel(), be.getBlockPos(), ctx);
            return valve != null ? valve : be.getFluidCapabilityAt(be.getBlockPos());
        });
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type, (be, ctx) -> be.getItemCapability());
    }

    // Called from SmartBlockEntity's constructor, so these must not depend on subclass fields.
    protected abstract int fluidInputCount();

    protected abstract int fluidOutputCount();

    protected abstract int tankCapacity();

    protected abstract String name();

    protected abstract RecipeType<R> recipeType();

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        fluidInputs = new SmartFluidTankBehaviour[fluidInputCount()];
        for (int i = 0; i < fluidInputs.length; i++) {
            fluidInputs[i] = new SmartFluidTankBehaviour(INPUT_TYPES[i], this, 1, tankCapacity(), false);
            behaviours.add(fluidInputs[i]);
        }
        fluidOutputs = new SmartFluidTankBehaviour[fluidOutputCount()];
        for (int i = 0; i < fluidOutputs.length; i++) {
            fluidOutputs[i] = new SmartFluidTankBehaviour(OUTPUT_TYPES[i], this, 1, tankCapacity(), false).forbidInsertion();
            behaviours.add(fluidOutputs[i]);
        }
    }

    public IFluidHandler getFluidCapability() {
        return fluidCapability;
    }

    /** The fluid handler a pipe on this block reaches. */
    @Nullable
    public IFluidHandler getFluidCapabilityAt(BlockPos pos) {
        return fluidCapability;
    }

    public IItemHandlerModifiable getItemCapability() {
        return itemCapability;
    }

    protected void refreshCapabilities() {
        // Outputs first, so a pipe pulling from the machine gets products before unreacted inputs.
        IFluidHandler[] outputs = new IFluidHandler[activeFluidOutputs];
        for (int o = 0; o < outputs.length; o++) {
            outputs[o] = fluidOutputs[o].getCapability();
        }
        fluidCapability = new InputFillOutputDrainWrapper(outputs, inputHandlers(), this::acceptsNewFluid);
        itemCapability = new CombinedInvWrapper(new NonInsertableItemHandler(itemOutputs), itemInputs);
    }

    protected IFluidHandler[] inputHandlers() {
        IFluidHandler[] inputs = new IFluidHandler[fluidInputs.length];
        for (int i = 0; i < inputs.length; i++) {
            inputs[i] = fluidInputs[i].getCapability();
        }
        return inputs;
    }

    /** Whether a fluid not yet in the inputs may come in: some recipe must take it along with every fluid already there. */
    protected boolean acceptsNewFluid(FluidStack incoming) {
        if (level == null) {
            return true;
        }
        List<FluidStack> present = new ArrayList<>();
        for (SmartFluidTankBehaviour input : fluidInputs) {
            FluidStack contained = input.getPrimaryHandler().getFluid();
            if (!contained.isEmpty()) {
                present.add(contained);
            }
        }
        return FluidCompatibility.accepts(level.getRecipeManager(), recipeType(), incoming, present);
    }

    // ---- recipes ----

    /** Whether the machine can process at all, such as being formed. */
    protected boolean isReady() {
        return true;
    }

    /** Whether a recipe's heat, stirring or power needs are met right now. */
    protected boolean canRunNow(R candidate) {
        return true;
    }

    /** Extra specificity when several recipes match, see {@link RecipePicker}. */
    protected int recipeBonus(R candidate) {
        return 0;
    }

    /** Extra input conditions beyond fluids and items. */
    protected boolean matchesExtra(R candidate) {
        return true;
    }

    /** What keeps a recipe whose ingredients are all in from running now: the heat, stirring or power it needs. */
    protected String whyNotNow(R recipe) {
        return "Waiting for its heat or power";
    }

    /** Why a recipe whose ingredients are all in is not one this machine can run (its {@link #matchesExtra} refused it). */
    protected String whyNotExtra(R recipe) {
        return "This machine cannot make that";
    }

    /** What keeps a running recipe from progressing ({@link #workThisTick} gave nothing). */
    protected String whyNoWork(R recipe) {
        return "Not enough power";
    }

    /**
     * Best effort, for an idle machine holding something: the first recipe its inputs are enough for, and what else that
     * recipe lacks. Runs only when a recipe search has just failed, so at most twice a second.
     */
    @Nullable
    private String diagnoseIdle() {
        if (!holdsAnything()) {
            return null;
        }
        for (var holder : level.getRecipeManager().getAllRecipesFor(recipeType())) {
            R candidate = holder.value();
            if (!hasIngredients(candidate)) {
                continue;
            }
            if (candidate.getFluidResults().size() > activeFluidOutputs) {
                return candidate.getFluidResults().size() <= fluidOutputs.length
                        ? "Needs " + candidate.getFluidResults().size() + " output tanks (has " + activeFluidOutputs + "): add an Outlet Manifold"
                        : "Makes " + candidate.getFluidResults().size() + " fluids, more than this machine can hold";
            }
            if (candidate.getRollableResults().size() > itemOutputs.getSlots()) {
                return "Makes more items than this machine can hold";
            }
            if (!matchesExtra(candidate)) {
                return whyNotExtra(candidate);
            }
        }
        return "No recipe takes these inputs";
    }

    private boolean holdsAnything() {
        for (SmartFluidTankBehaviour input : fluidInputs) {
            if (!input.getPrimaryHandler().getFluid().isEmpty()) {
                return true;
            }
        }
        for (int i = 0; i < itemInputs.getSlots(); i++) {
            if (!itemInputs.getStackInSlot(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void setBlocker(@Nullable String reason) {
        if (!Objects.equals(reason, blocker)) {
            blocker = reason;
            sendData();
        }
    }

    /** How many ticks of progress this tick brings, at most 1: less when the machine is short of power. */
    protected float workThisTick(R recipe) {
        return 1;
    }

    protected int duration(R recipe) {
        return recipe.getProcessingDuration() > 0 ? recipe.getProcessingDuration() : 100;
    }

    protected void onServerTick() {
    }

    // ---- attachments ----

    /**
     * Whether the attachment at {@code at} works on this machine. A single-block machine has one attachment slot, for a Gauge or
     * an Outlet Valve: the first one found against it (see {@link #attachmentSlot}).
     */
    public boolean acceptsAttachment(BlockPos at, MachineAttachment.Kind kind) {
        return (kind == MachineAttachment.Kind.GAUGE || kind == MachineAttachment.Kind.OUTLET_VALVE) && at.equals(attachmentSlot());
    }

    /** The blocks the machine is made of, whose sides attachments mount on. */
    protected List<BlockPos> bodyBlocks() {
        return List.of(worldPosition);
    }

    /** The Gauge or Outlet Valve holding a single-block machine's one attachment slot, or null. */
    @Nullable
    protected BlockPos attachmentSlot() {
        if (level == null) {
            return null;
        }
        for (BlockPos body : bodyBlocks()) {
            for (Direction side : Direction.Plane.HORIZONTAL) {
                BlockPos at = body.relative(side);
                BlockState state = level.getBlockState(at);
                if (state.getBlock() instanceof MachineAttachment attachment && MachineAttachment.mountedTowards(state) == side.getOpposite()
                        && (attachment.kind() == MachineAttachment.Kind.GAUGE || attachment.kind() == MachineAttachment.Kind.OUTLET_VALVE)) {
                    return at;
                }
            }
        }
        return null;
    }

    /** A gauge's comparator signal: the progress, or how full the fullest output is. */
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

    /** What is in the one attachment slot (multiblocks list their own slots). */
    protected void addAttachmentTooltip(List<Component> tooltip) {
        BlockPos slot = attachmentSlot();
        if (slot != null) {
            tooltip.add(Component.literal(" - Attachment: ").withStyle(ChatFormatting.GRAY)
                    .append(level.getBlockState(slot).getBlock().getName().copy().withStyle(ChatFormatting.WHITE)).append(" (1/1)"));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null) {
            return;
        }
        if (level.isClientSide) {
            previousShownProgress = shownProgress;
            shownProgress += (progressFraction - shownProgress) * 0.25F;
            if (running) {
                onClientWorkingTick(level.random);
            }
            return;
        }
        boolean wasRunning = running;
        handleRecipe();
        onServerTick();
        BlockPos gauge = level.getGameTime() % 20 == 0 ? attachmentSlot() : null;
        if (gauge != null) {
            level.updateNeighbourForOutputSignal(gauge, level.getBlockState(gauge).getBlock());
        }
        // The clips last a little over two seconds, so they overlap slightly instead of leaving gaps.
        if (running && level.getGameTime() % 40 == 0 && workingSound() != null) {
            level.playSound(null, worldPosition, workingSound(), SoundSource.BLOCKS, 0.35F, 0.95F + level.random.nextFloat() * 0.1F);
        }
        // Progress only exists server-side; clients get it in 5% steps for the goggles.
        int progressStep = recipe == null ? 0 : (int) (progressFraction * 20);
        if (progressStep != lastSyncedProgressStep || running != wasRunning) {
            lastSyncedProgressStep = progressStep;
            sendData();
        }
    }

    /** Looped while a recipe advances, or null for a silent machine. */
    @Nullable
    protected SoundEvent workingSound() {
        return null;
    }

    /** Client side, every tick while a recipe advances: particles. */
    protected void onClientWorkingTick(RandomSource random) {
    }

    public boolean isRunning() {
        return running;
    }

    public float shownProgress(float partialTicks) {
        return previousShownProgress + (shownProgress - previousShownProgress) * partialTicks;
    }

    private void handleRecipe() {
        running = false;
        if (!isReady()) {
            recipe = null;
            setBlocker(null);
            return;
        }
        if (recipe != null && !matchesInputs(recipe)) {
            resetRecipe();
        }
        if (recipe == null) {
            // Searching every recipe of the type is the costly part, so an idle machine only retries twice a second.
            if (searchCooldown > 0) {
                searchCooldown--;
                return;
            }
            recipe = RecipePicker.pick(level, recipeType(), this::matchesInputs, this::canRunNow, this::recipeBonus);
            timer = 0;
            if (recipe == null) {
                searchCooldown = SEARCH_COOLDOWN_TICKS;
                // A recipe whose needs are not met is still picked (and held below), so none here means the inputs fall short.
                setBlocker(diagnoseIdle());
            }
        }
        if (recipe == null) {
            progressFraction = 0;
            return;
        }
        if (!canRunNow(recipe)) {
            setBlocker(whyNotNow(recipe));
            // Hold, unless another recipe for the same inputs can run now.
            if (searchCooldown > 0) {
                searchCooldown--;
                return;
            }
            searchCooldown = SEARCH_COOLDOWN_TICKS;
            R better = RecipePicker.reconsider(level, recipe, recipeType(), this::matchesInputs, this::canRunNow, this::recipeBonus);
            if (better != recipe) {
                recipe = better;
                timer = 0;
            }
            return;
        }
        if (!outputsFit(recipe)) {
            setBlocker("Its outputs are full: empty them");
            return;
        }
        float work = workThisTick(recipe);
        if (work <= 0) {
            setBlocker(whyNoWork(recipe));
            return;
        }
        setBlocker(null);
        timer += work;
        running = true;
        int duration = duration(recipe);
        progressFraction = Math.min(1, timer / duration);
        if (timer >= duration) {
            craft(recipe);
            resetRecipe();
        }
    }

    private void resetRecipe() {
        recipe = null;
        timer = 0;
        progressFraction = 0;
    }

    protected boolean matchesInputs(R candidate) {
        return candidate.getFluidResults().size() <= activeFluidOutputs && candidate.getRollableResults().size() <= itemOutputs.getSlots()
                && matchesExtra(candidate) && hasIngredients(candidate);
    }

    /** Whether every ingredient of the recipe is in the inputs, in the amounts it needs. */
    private boolean hasIngredients(R candidate) {
        if (candidate.getFluidIngredients().size() > fluidInputs.length) {
            return false;
        }
        for (SizedFluidIngredient ingredient : candidate.getFluidIngredients()) {
            if (findFluidSlot(ingredient) < 0) {
                return false;
            }
        }
        // A repeated ingredient is listed once per unit, and each entry needs its own item.
        int[] taken = new int[itemInputs.getSlots()];
        for (Ingredient ingredient : candidate.getIngredients()) {
            boolean found = false;
            for (int i = 0; i < itemInputs.getSlots() && !found; i++) {
                ItemStack stack = itemInputs.getStackInSlot(i);
                if (ingredient.test(stack) && stack.getCount() > taken[i]) {
                    taken[i]++;
                    found = true;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    private int findFluidSlot(SizedFluidIngredient ingredient) {
        for (int i = 0; i < fluidInputs.length; i++) {
            FluidStack contained = fluidInputs[i].getPrimaryHandler().getFluid();
            if (!contained.isEmpty() && contained.getAmount() >= ingredient.amount() && ingredient.test(contained)) {
                return i;
            }
        }
        return -1;
    }

    private boolean outputsFit(R recipe) {
        boolean[] claimed = new boolean[activeFluidOutputs];
        for (FluidStack result : recipe.getFluidResults()) {
            int tank = outputTankFor(result, claimed);
            if (tank < 0 || fluidOutputs[tank].getPrimaryHandler().fill(result, FluidAction.SIMULATE) < result.getAmount()) {
                return false;
            }
            claimed[tank] = true;
        }
        for (ProcessingOutput output : recipe.getRollableResults()) {
            ItemStack stack = output.getStack().copy();
            for (int i = 0; i < itemOutputs.getSlots() && !stack.isEmpty(); i++) {
                stack = itemOutputs.insertItem(i, stack, true);
            }
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /** The output tank a fluid goes to: one already holding it, else the first empty one not yet claimed. */
    private int outputTankFor(FluidStack fluid, boolean[] claimed) {
        for (int o = 0; o < activeFluidOutputs; o++) {
            if (FluidStack.isSameFluidSameComponents(fluidOutputs[o].getPrimaryHandler().getFluid(), fluid)) {
                return o;
            }
        }
        for (int o = 0; o < activeFluidOutputs; o++) {
            if (!claimed[o] && fluidOutputs[o].getPrimaryHandler().getFluid().isEmpty()) {
                return o;
            }
        }
        return -1;
    }

    private void craft(R recipe) {
        for (SizedFluidIngredient ingredient : recipe.getFluidIngredients()) {
            int slot = findFluidSlot(ingredient);
            if (slot >= 0) {
                fluidInputs[slot].getPrimaryHandler().drain(ingredient.amount(), FluidAction.EXECUTE);
            }
        }
        for (Ingredient ingredient : recipe.getIngredients()) {
            for (int i = 0; i < itemInputs.getSlots(); i++) {
                if (ingredient.test(itemInputs.getStackInSlot(i))) {
                    itemInputs.extractItem(i, 1, false);
                    break;
                }
            }
        }
        for (ProcessingOutput output : recipe.getRollableResults()) {
            ItemStack stack = output.rollOutput(level.random);
            for (int i = 0; i < itemOutputs.getSlots() && !stack.isEmpty(); i++) {
                stack = itemOutputs.insertItem(i, stack, false);
            }
        }
        boolean[] claimed = new boolean[activeFluidOutputs];
        for (FluidStack result : recipe.getFluidResults()) {
            int tank = outputTankFor(result, claimed);
            if (tank >= 0) {
                claimed[tank] = true;
                fluidOutputs[tank].getPrimaryHandler().fill(result.copy(), FluidAction.EXECUTE);
            }
        }
        setChanged();
        sendData();
    }

    // ---- saving ----

    @Override
    protected void read(CompoundTag compound, Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        itemInputs.deserializeNBT(registries, compound.getCompound("ItemInputs"));
        itemOutputs.deserializeNBT(registries, compound.getCompound("ItemOutputs"));
        if (clientPacket) {
            progressFraction = compound.getFloat("Progress");
            running = compound.getBoolean("Running");
            blocker = compound.contains("Blocker") ? compound.getString("Blocker") : null;
        }
    }

    @Override
    public void write(CompoundTag compound, Provider registries, boolean clientPacket) {
        compound.put("ItemInputs", itemInputs.serializeNBT(registries));
        compound.put("ItemOutputs", itemOutputs.serializeNBT(registries));
        if (clientPacket) {
            compound.putFloat("Progress", recipe == null ? 0 : progressFraction);
            compound.putBoolean("Running", running);
            if (blocker != null) {
                compound.putString("Blocker", blocker);
            }
        }
        super.write(compound, registries, clientPacket);
    }

    @Override
    public void destroy() {
        super.destroy();
        for (ItemStackHandler slots : new ItemStackHandler[] {itemInputs, itemOutputs}) {
            for (int i = 0; i < slots.getSlots(); i++) {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), slots.getStackInSlot(i));
            }
        }
    }

    // ---- goggles ----

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return addToGoggleTooltipAt(tooltip, isPlayerSneaking, worldPosition);
    }

    /** Goggle info when looking at {@code looked}, this block or a part of its machine. */
    public boolean addToGoggleTooltipAt(List<Component> tooltip, boolean isPlayerSneaking, BlockPos looked) {
        if (leakTightness() > 0) {
            LeakInfo.append(tooltip, this);
        }
        if (!addHeader(tooltip)) {
            return true;
        }
        if (blocker != null && !running) {
            tooltip.add(Component.literal(" - Stopped: " + blocker).withStyle(ChatFormatting.GOLD));
        }
        addStatusTooltip(tooltip, looked);
        addAttachmentTooltip(tooltip);
        addTankTooltip(tooltip, looked);
        if (progressFraction > 0) {
            tooltip.add(Component.literal(" - Progress: " + (int) (progressFraction * 100) + "%").withStyle(ChatFormatting.GRAY));
        }
        return true;
    }

    /** The machine's name line; false stops the tooltip there. */
    protected boolean addHeader(List<Component> tooltip) {
        tooltip.add(Component.literal(" - " + name()).withStyle(ChatFormatting.GRAY));
        return true;
    }

    /** 1 for a plain machine, 0 for one sealed with a Gasket: scales the chance its toxic contents leak. */
    public float leakTightness() {
        return TankSealing.isSealed(this) ? 0 : 1;
    }

    /** Machine-specific lines, such as heat or voltage. */
    protected void addStatusTooltip(List<Component> tooltip, BlockPos looked) {
    }

    protected void addTankTooltip(List<Component> tooltip, BlockPos looked) {
        addFluidLines(tooltip, "Input", fluidInputs, fluidInputs.length);
        addFluidLines(tooltip, "Output", fluidOutputs, activeFluidOutputs);
    }

    protected static void addFluidLines(List<Component> tooltip, String label, SmartFluidTankBehaviour[] tanks, int count) {
        for (int i = 0; i < count; i++) {
            FluidStack fluid = tanks[i].getPrimaryHandler().getFluid();
            if (!fluid.isEmpty()) {
                tooltip.add(Component.literal(" - " + label + ": ").withStyle(ChatFormatting.GRAY)
                        .append(fluid.getHoverName().copy().withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(" (" + fluid.getAmount() + "/" + tanks[i].getPrimaryHandler().getCapacity() + " mb)")
                                .withStyle(ChatFormatting.GRAY)));
            }
        }
    }
}
