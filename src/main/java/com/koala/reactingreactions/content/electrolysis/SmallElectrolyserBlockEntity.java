package com.koala.reactingreactions.content.electrolysis;

import com.koala.reactingreactions.content.electrolysis.recipe.ElectrolysisRecipe;
import com.koala.reactingreactions.content.multiblock.MachineEffects;
import com.koala.reactingreactions.content.multiblock.ProcessingMachineBlockEntity;
import com.koala.reactingreactions.registry.CRRRecipeTypes;
import com.koala.reactingreactions.registry.CRRSounds;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** A slow one-block electrolyser for the any-electrode recipes, so the vat's own materials can be made before it exists. */
public class SmallElectrolyserBlockEntity extends ProcessingMachineBlockEntity<ElectrolysisRecipe> {
    private static final int ITEM_SLOTS = 2;

    private final Voltmeter voltmeter = new Voltmeter();

    public SmallElectrolyserBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, ITEM_SLOTS, ITEM_SLOTS);
    }

    /** The recipes it can run at all: any electrode, and within its tanks and slots. */
    public static boolean canRun(ElectrolysisRecipe candidate) {
        return candidate.electrodes.isEmpty()
                && candidate.getFluidIngredients().size() <= 1
                && candidate.getFluidResults().size() <= 1
                && candidate.getIngredients().size() <= ITEM_SLOTS
                && candidate.getRollableResults().size() <= ITEM_SLOTS;
    }

    @Override
    protected SoundEvent workingSound() {
        return CRRSounds.VAT_BUZZ.get();
    }

    /** Sparks jump between the two terminal posts on the lid. */
    @Override
    protected void onClientWorkingTick(RandomSource random) {
        if (random.nextInt(5) == 0) {
            double x = worldPosition.getX() + (random.nextBoolean() ? 0.25 : 0.75);
            MachineEffects.spark(level, random, x, worldPosition.getY() + 1.05, worldPosition.getZ() + 0.5);
        }
    }

    @Override
    protected int fluidInputCount() {
        return 1;
    }

    @Override
    protected int fluidOutputCount() {
        return 1;
    }

    @Override
    protected int tankCapacity() {
        return 2000;
    }

    @Override
    protected String name() {
        return "Small Electrolyser (any electrode)";
    }

    @Override
    protected RecipeType<ElectrolysisRecipe> recipeType() {
        return CRRRecipeTypes.ELECTROLYSIS.get();
    }

    public Voltmeter voltmeter() {
        return voltmeter;
    }

    @Override
    protected boolean matchesExtra(ElectrolysisRecipe candidate) {
        return canRun(candidate);
    }

    @Override
    protected boolean canRunNow(ElectrolysisRecipe candidate) {
        return voltmeter.reaches(candidate.minVoltage);
    }

    @Override
    protected int duration(ElectrolysisRecipe recipe) {
        return super.duration(recipe) * 2;
    }

    @Override
    protected void onServerTick() {
        if (voltmeter.needsSync()) {
            sendData();
        }
    }

    @Override
    protected void addStatusTooltip(List<Component> tooltip, BlockPos looked) {
        voltmeter.addTooltip(tooltip);
    }

    @Override
    protected void read(CompoundTag compound, Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        voltmeter.read(compound, clientPacket);
    }

    @Override
    public void write(CompoundTag compound, Provider registries, boolean clientPacket) {
        voltmeter.write(compound, clientPacket);
        super.write(compound, registries, clientPacket);
    }
}
