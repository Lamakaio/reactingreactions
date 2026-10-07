package com.koala.reactingreactions.content.reaction;

import com.koala.reactingreactions.content.multiblock.MachineEffects;
import com.koala.reactingreactions.content.multiblock.MachineTiers;
import com.koala.reactingreactions.content.multiblock.ProcessingMachineBlockEntity;
import com.koala.reactingreactions.content.multiblock.attachment.OutletValveBlockEntity;
import com.koala.reactingreactions.content.reaction.recipe.ReactionRecipe;
import com.koala.reactingreactions.content.toxic.TankSealing;
import com.koala.reactingreactions.registry.CRRBlocks;
import com.koala.reactingreactions.registry.CRRRecipeTypes;
import com.koala.reactingreactions.registry.CRRSounds;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.List;
import java.util.Locale;

/**
 * A one-block Reaction Chamber, two tall: stirred by a shaft into its top, heated by a Blaze Burner under it, at half the big
 * chamber's speed. It runs only reactions of at most two fluids and two items in, one fluid and one item out. Its only
 * attachment is a Gasket (TankSealing), which shows on its model.
 */
public class SmallReactionChamberBlockEntity extends ProcessingMachineBlockEntity<ReactionRecipe> {
    private static final int ITEM_INPUTS = 2;
    private static final int ITEM_OUTPUTS = 1;
    private static final int FLUID_INPUTS = 2;
    private static final int FLUID_OUTPUTS = 1;
    /** The hollow inside the model (tools/machine_models.py), in blocks above the lower half's bottom, and its square inset. */
    public static final float FLUID_BOTTOM = 4.5F / 16;
    public static final float FLUID_HEIGHT = 20F / 16;
    public static final float FLUID_INSET = 4.5F / 16;

    // The running recipe's stirring band, synced for the goggles.
    private float syncedMinRpm;
    private float syncedMaxRpm;

    public SmallReactionChamberBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, ITEM_INPUTS, ITEM_OUTPUTS);
    }

    /** Both halves reach the lower one's handlers. */
    public static void registerCapabilities(RegisterCapabilitiesEvent event, Block block) {
        event.registerBlock(Capabilities.FluidHandler.BLOCK, (level, pos, state, be, side) -> {
            // An Outlet Valve on either half pulls through its own filtered outlet.
            IFluidHandler valve = OutletValveBlockEntity.sourceFor(level, pos, side);
            return valve != null ? valve : level.getBlockEntity(SmallReactionChamberBlock.lowerPos(state, pos)) instanceof SmallReactionChamberBlockEntity chamber
                    ? chamber.getFluidCapability() : null;
        }, block);
        event.registerBlock(Capabilities.ItemHandler.BLOCK, (level, pos, state, be, side) ->
                level.getBlockEntity(SmallReactionChamberBlock.lowerPos(state, pos)) instanceof SmallReactionChamberBlockEntity chamber
                        ? chamber.getItemCapability() : null, block);
    }

    /** Both halves take attachments on their sides. */
    @Override
    protected List<BlockPos> bodyBlocks() {
        return List.of(worldPosition, worldPosition.above());
    }

    public static boolean canRun(ReactionRecipe candidate) {
        return candidate.getFluidIngredients().size() <= FLUID_INPUTS
                && candidate.getFluidResults().size() <= FLUID_OUTPUTS
                && candidate.getIngredients().size() <= ITEM_INPUTS
                && candidate.getRollableResults().size() <= ITEM_OUTPUTS;
    }

    /** The shaft coming down into the top: any kinetic block on a vertical axis right above the upper half. */
    public float stirSpeed() {
        if (level == null) {
            return 0;
        }
        BlockPos above = worldPosition.above(2);
        BlockState state = level.getBlockState(above);
        if (!state.hasProperty(BlockStateProperties.AXIS) || state.getValue(BlockStateProperties.AXIS) != Direction.Axis.Y) {
            return 0;
        }
        return level.getBlockEntity(above) instanceof KineticBlockEntity kinetic ? Math.abs(kinetic.getSpeed()) : 0;
    }

    public BlazeBurnerBlock.HeatLevel heatLevel() {
        BlockState below = level == null ? null : level.getBlockState(worldPosition.below());
        return below != null && below.hasProperty(BlazeBurnerBlock.HEAT_LEVEL) ? below.getValue(BlazeBurnerBlock.HEAT_LEVEL) : BlazeBurnerBlock.HeatLevel.NONE;
    }

    @Override
    protected boolean matchesExtra(ReactionRecipe candidate) {
        return canRun(candidate);
    }

    @Override
    protected boolean canRunNow(ReactionRecipe candidate) {
        HeatCondition heat = candidate.getRequiredHeat();
        return candidate.acceptsSpeed(stirSpeed()) && (heat == HeatCondition.NONE || heat.testBlazeBurner(heatLevel()));
    }

    @Override
    protected String whyNotNow(ReactionRecipe recipe) {
        return !recipe.acceptsSpeed(stirSpeed()) ? recipe.stirringShortfall(stirSpeed()) : ReactionRecipe.heatShortfall(recipe.getRequiredHeat());
    }

    @Override
    protected String whyNotExtra(ReactionRecipe recipe) {
        return "Too many ingredients or products for a Small Reaction Chamber";
    }

    @Override
    protected int duration(ReactionRecipe recipe) {
        return super.duration(recipe) * 2;
    }

    /** The Gasket's flag (TankSealing) shows on the model of both halves. */
    @Override
    protected void onServerTick() {
        boolean sealed = TankSealing.isSealed(this);
        if (getBlockState().getValue(MachineTiers.SEALED) != sealed) {
            for (BlockPos pos : new BlockPos[] {worldPosition, worldPosition.above()}) {
                BlockState state = level.getBlockState(pos);
                if (state.is(CRRBlocks.SMALL_REACTION_CHAMBER.get())) {
                    level.setBlock(pos, state.setValue(MachineTiers.SEALED, sealed), Block.UPDATE_CLIENTS);
                }
            }
        }
    }

    /** The tank shown through the window: the first non-empty one, outputs last. */
    public SmartFluidTankBehaviour renderedTank() {
        for (SmartFluidTankBehaviour[] tanks : new SmartFluidTankBehaviour[][] {fluidInputs, fluidOutputs}) {
            for (SmartFluidTankBehaviour tank : tanks) {
                if (!tank.getPrimaryHandler().getFluid().isEmpty()) {
                    return tank;
                }
            }
        }
        return null;
    }

    @Override
    protected void onClientWorkingTick(RandomSource random) {
        SmartFluidTankBehaviour tank = renderedTank();
        if (tank == null || random.nextInt(3) != 0) {
            return;
        }
        FluidStack fluid = tank.getPrimaryHandler().getFluid();
        double fill = fluid.getAmount() / (double) Math.max(1, tank.getPrimaryHandler().getCapacity());
        MachineEffects.bubble(level, random, fluid, worldPosition.getX() + 0.4 + random.nextDouble() * 0.2,
                worldPosition.getY() + FLUID_BOTTOM + fill * FLUID_HEIGHT,
                worldPosition.getZ() + 0.4 + random.nextDouble() * 0.2);
    }

    @Override
    protected SoundEvent workingSound() {
        return CRRSounds.BOILING.get();
    }

    @Override
    protected void addStatusTooltip(List<Component> tooltip, BlockPos looked) {
        float stir = stirSpeed();
        boolean needsStirring = syncedMaxRpm > 0;
        boolean ok = !needsStirring || (stir >= syncedMinRpm && stir <= syncedMaxRpm);
        tooltip.add(Component.literal(needsStirring
                        ? String.format(" - Stirring: %.0f RPM (needs %.0f-%.0f)", stir, syncedMinRpm, syncedMaxRpm)
                        : String.format(" - Stirring: %.0f RPM", stir))
                .withStyle(ok ? ChatFormatting.GRAY : ChatFormatting.RED));
        BlazeBurnerBlock.HeatLevel heat = heatLevel();
        tooltip.add(Component.literal(" - Heat: " + heat.name().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.GRAY));
        if (TankSealing.isSealed(this)) {
            tooltip.add(Component.literal(" - Sealed: no leaks").withStyle(ChatFormatting.GRAY));
        }
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
        return 2000;
    }

    @Override
    protected String name() {
        return "Small Reaction Chamber";
    }

    @Override
    protected RecipeType<ReactionRecipe> recipeType() {
        return CRRRecipeTypes.REACTION.get();
    }

    @Override
    protected void read(CompoundTag compound, Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        if (clientPacket) {
            syncedMinRpm = compound.getFloat("RpmMin");
            syncedMaxRpm = compound.getFloat("RpmMax");
        }
    }

    @Override
    public void write(CompoundTag compound, Provider registries, boolean clientPacket) {
        if (clientPacket) {
            compound.putFloat("RpmMin", recipe == null ? 0 : recipe.minRpm);
            compound.putFloat("RpmMax", recipe == null ? 0 : recipe.maxRpm);
        }
        super.write(compound, registries, clientPacket);
    }
}
