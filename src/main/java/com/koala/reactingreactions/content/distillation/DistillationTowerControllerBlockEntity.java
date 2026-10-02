package com.koala.reactingreactions.content.distillation;

import com.koala.reactingreactions.content.distillation.recipe.DistillationRecipe;
import com.koala.reactingreactions.content.multiblock.HollowBoxScanner;
import com.koala.reactingreactions.content.multiblock.InputFillOutputDrainWrapper;
import com.koala.reactingreactions.content.multiblock.MachineEffects;
import com.koala.reactingreactions.content.multiblock.MultiblockControllerBlockEntity;
import com.koala.reactingreactions.content.multiblock.MultiblockRenderer;
import com.koala.reactingreactions.registry.CRRBlocks;
import com.koala.reactingreactions.registry.CRRRecipeTypes;
import com.koala.reactingreactions.registry.CRRSounds;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/** A column: the floor row takes the input, the row above it is empty, and each row from the third holds one output. */
public class DistillationTowerControllerBlockEntity extends MultiblockControllerBlockEntity<DistillationRecipe> {
    public static final Spec<DistillationRecipe> SPEC = new Spec<>("Distillation Tower",
            state -> state.is(CRRBlocks.DISTILLATION_TOWER_WALL.get()),
            state -> state.is(CRRBlocks.DISTILLATION_TOWER_CONTROLLER.get()),
            BlockState::isAir, 3, 7, 3, 10, (x, z) -> x.equals(z), 4, 4, true, CRRRecipeTypes.DISTILLATION::get);

    // Not initialised here: the base constructor already fills them.
    private IFluidHandler inputRowCapability;
    private IFluidHandler[] outputRowCapabilities;

    public DistillationTowerControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, SPEC);
    }

    @Override
    protected void refreshCapabilities() {
        super.refreshCapabilities();
        inputRowCapability = new InputFillOutputDrainWrapper(new IFluidHandler[0], inputHandlers(), this::acceptsNewFluid);
        outputRowCapabilities = new IFluidHandler[activeFluidOutputs];
        for (int o = 0; o < activeFluidOutputs; o++) {
            outputRowCapabilities[o] = fluidOutputs[o].getCapability();
        }
    }

    @Override
    @Nullable
    public IFluidHandler getFluidCapabilityAt(BlockPos pos) {
        if (structure == null) {
            return getFluidCapability();
        }
        int row = pos.getY() - structure.min().getY();
        if (row == 0) {
            return inputRowCapability;
        }
        return row >= 2 && row - 2 < outputRowCapabilities.length ? outputRowCapabilities[row - 2] : null;
    }

    /** Each row's tank: 1000 mB per interior column, as one recipe takes at most a bucket. */
    @Override
    protected int formedCapacity(HollowBoxScanner.Result found) {
        return 1000 * (found.sizeX() - 2) * (found.sizeZ() - 2);
    }

    @Override
    protected boolean canRunNow(DistillationRecipe candidate) {
        return hasHeat(candidate.getRequiredHeat());
    }

    /** When several recipes can run, the hottest one wins: superheated variants are the faster ones. */
    @Override
    protected int recipeBonus(DistillationRecipe candidate) {
        return candidate.getRequiredHeat().ordinal();
    }

    @Override
    protected int duration(DistillationRecipe recipe) {
        return Math.max(1, (int) Math.round(super.duration(recipe) / (1 + averageHeat() * 0.5)));
    }

    @Override
    protected SoundEvent workingSound() {
        return CRRSounds.BOILING.get();
    }

    /** The input boils in the floor row, vapour climbs the column, and a little escapes from the roof. */
    @Override
    protected void onClientWorkingTick(RandomSource random) {
        if (structure == null) {
            return;
        }
        RowFluid input = getRowFluid(0);
        if (!input.fluid().isEmpty() && random.nextInt(2) == 0) {
            Vec3 at = randomInside(random, MultiblockRenderer.HULL + input.fill() * (1 - MultiblockRenderer.HULL));
            MachineEffects.bubble(level, random, input.fluid(), at.x, at.y, at.z);
        }
        if (random.nextInt(3) == 0) {
            Vec3 at = randomInside(random, 1 + random.nextDouble() * (structure.sizeY() - 2));
            level.addParticle(ParticleTypes.WHITE_SMOKE, at.x, at.y, at.z, 0, 0.04, 0);
        }
        if (random.nextInt(6) == 0) {
            Vec3 roof = roofTop();
            MachineEffects.puff(level, random, ParticleTypes.CLOUD, roof.x, roof.y, roof.z);
        }
    }

    public record RowFluid(FluidStack fluid, float fill) {
    }

    public RowFluid getRowFluid(int row) {
        SmartFluidTankBehaviour tank = null;
        if (row == 0) {
            for (SmartFluidTankBehaviour input : fluidInputs) {
                if (!input.getPrimaryHandler().getFluid().isEmpty()) {
                    tank = input;
                    break;
                }
            }
        } else if (row >= 2 && row - 2 < activeFluidOutputs) {
            tank = fluidOutputs[row - 2];
        }
        if (tank == null || tank.getPrimaryHandler().getFluid().isEmpty()) {
            return new RowFluid(FluidStack.EMPTY, 0);
        }
        var handler = tank.getPrimaryHandler();
        return new RowFluid(handler.getFluid(), Math.min(1f, handler.getFluid().getAmount() / (float) Math.max(1, handler.getCapacity())));
    }

    @Override
    protected void addStatusTooltip(List<Component> tooltip, BlockPos looked) {
        addHeatTooltip(tooltip);
    }

    @Override
    protected void addTankTooltip(List<Component> tooltip, BlockPos looked) {
        int row = looked.getY() - structure.min().getY();
        if (row == 0) {
            addFluidLines(tooltip, "Input", fluidInputs, fluidInputs.length);
        } else if (row == 1) {
            tooltip.add(Component.literal(" - This row has no connection").withStyle(ChatFormatting.DARK_GRAY));
        } else if (row - 2 < activeFluidOutputs) {
            addFluidLines(tooltip, "Output " + (row - 1), new SmartFluidTankBehaviour[] {fluidOutputs[row - 2]}, 1);
        }
    }
}
