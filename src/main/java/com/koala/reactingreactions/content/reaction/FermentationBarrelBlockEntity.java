package com.koala.reactingreactions.content.reaction;

import com.koala.reactingreactions.content.multiblock.MachineEffects;
import com.koala.reactingreactions.content.multiblock.ProcessingMachineBlockEntity;
import com.koala.reactingreactions.content.reaction.recipe.ReactionRecipe;
import com.koala.reactingreactions.registry.CRRRecipeTypes;
import com.simibubi.create.content.processing.recipe.HeatCondition;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** A slow, unpowered one-block fermenter for the reactions that need neither heat nor stirring. */
public class FermentationBarrelBlockEntity extends ProcessingMachineBlockEntity<ReactionRecipe> {
    private static final int ITEM_INPUTS = 6;
    private static final int ITEM_OUTPUTS = 2;

    public FermentationBarrelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, ITEM_INPUTS, ITEM_OUTPUTS);
    }

    public static boolean canRun(ReactionRecipe candidate) {
        return !candidate.needsStirring()
                && candidate.getRequiredHeat() == HeatCondition.NONE
                && candidate.getFluidIngredients().size() <= 2
                && candidate.getFluidResults().size() <= 1
                && candidate.getIngredients().size() <= ITEM_INPUTS
                && candidate.getRollableResults().size() <= ITEM_OUTPUTS;
    }

    /** Fermenting brew bubbles out of the lid now and then. */
    @Override
    protected void onClientWorkingTick(RandomSource random) {
        if (random.nextInt(10) == 0) {
            MachineEffects.bubble(level, random, fluidInputs[0].getPrimaryHandler().getFluid(), worldPosition.getX() + 0.2 + random.nextDouble() * 0.6,
                    worldPosition.getY() + 1.02, worldPosition.getZ() + 0.2 + random.nextDouble() * 0.6);
        }
    }

    @Override
    protected int fluidInputCount() {
        return 2;
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
        return "Fermentation Barrel";
    }

    @Override
    protected RecipeType<ReactionRecipe> recipeType() {
        return CRRRecipeTypes.REACTION.get();
    }

    @Override
    protected boolean matchesExtra(ReactionRecipe candidate) {
        return canRun(candidate);
    }

    @Override
    protected String whyNotExtra(ReactionRecipe recipe) {
        return recipe.needsStirring() || recipe.getRequiredHeat() != HeatCondition.NONE ? "Needs stirring or heat: use a Reaction Chamber"
                : "Too many ingredients or products for a barrel";
    }

    @Override
    protected int duration(ReactionRecipe recipe) {
        return super.duration(recipe) * 2;
    }

    @Override
    protected void addTankTooltip(List<Component> tooltip, BlockPos looked) {
        super.addTankTooltip(tooltip, looked);
        for (int i = 0; i < itemInputs.getSlots(); i++) {
            ItemStack stack = itemInputs.getStackInSlot(i);
            if (!stack.isEmpty()) {
                tooltip.add(Component.literal(" - Item: ").withStyle(ChatFormatting.GRAY)
                        .append(stack.getHoverName().copy().withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(" x" + stack.getCount()).withStyle(ChatFormatting.GRAY)));
            }
        }
    }
}
