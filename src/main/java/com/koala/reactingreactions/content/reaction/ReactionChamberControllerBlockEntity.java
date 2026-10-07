package com.koala.reactingreactions.content.reaction;

import com.koala.reactingreactions.content.multiblock.HollowBoxScanner;
import com.koala.reactingreactions.content.multiblock.MachineTiers;
import com.koala.reactingreactions.content.multiblock.MachineEffects;
import com.koala.reactingreactions.content.multiblock.MultiblockControllerBlockEntity;
import com.koala.reactingreactions.content.reaction.recipe.ReactionRecipe;
import com.koala.reactingreactions.content.steel.SteelEncasedShaftBlockEntity;
import com.koala.reactingreactions.registry.CRRBlocks;
import com.koala.reactingreactions.registry.CRRRecipeTypes;
import com.koala.reactingreactions.registry.CRRSounds;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Stirred by a steel-encased shaft through the middle of the roof, heated by burners under the floor. Comes in two fixed
 * sizes ({@link MachineTiers#REACTION_CHAMBER}), with the controller in the middle of a side one block above the floor.
 */
public class ReactionChamberControllerBlockEntity extends MultiblockControllerBlockEntity<ReactionRecipe> {
    public static final Spec<ReactionRecipe> SPEC = new Spec<>("Reaction Chamber",
            state -> state.is(CRRBlocks.REACTION_CHAMBER_WALL.get()) || isVerticalShaft(state),
            state -> state.is(CRRBlocks.REACTION_CHAMBER_CONTROLLER.get()),
            BlockState::isAir, 3, 5, 4, 5, (x, z) -> x.equals(z) && x % 2 == 1, 6, 4, false, CRRRecipeTypes.REACTION::get);

    // The running recipe's stirring band, synced for the shaft's goggle line.
    private float syncedMinRpm;
    private float syncedMaxRpm;

    public ReactionChamberControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, SPEC);
    }

    private static boolean isVerticalShaft(BlockState state) {
        return state.is(CRRBlocks.STEEL_ENCASED_SHAFT.get()) && state.getValue(BlockStateProperties.AXIS) == Direction.Axis.Y;
    }

    private BlockPos shaftPos() {
        return shaftPos(structure);
    }

    private static BlockPos shaftPos(HollowBoxScanner.Result shell) {
        return shell.min().offset(shell.sizeX() / 2, shell.sizeY() - 1, shell.sizeZ() / 2);
    }

    /** Also needs its stirring shaft in the middle of the roof, where the formed model leaves it a hole. */
    @Nullable
    @Override
    protected String shapeRefusal(HollowBoxScanner.Result found) {
        String refusal = super.shapeRefusal(found);
        return refusal != null || isVerticalShaft(level.getBlockState(shaftPos(found))) ? refusal
                : "Needs an upright Steel Encased Shaft in the middle of the roof";
    }

    @Override
    protected List<MachineTiers.Tier> tiers() {
        return MachineTiers.REACTION_CHAMBER;
    }

    @Override
    protected void onScanned(@Nullable HollowBoxScanner.Result found) {
        if (found != null && level.getBlockEntity(shaftPos()) instanceof SteelEncasedShaftBlockEntity shaft) {
            shaft.setControllerPos(worldPosition);
        }
    }

    public float stirSpeed() {
        if (structure == null || level == null || !isVerticalShaft(level.getBlockState(shaftPos()))) {
            return 0;
        }
        return level.getBlockEntity(shaftPos()) instanceof KineticBlockEntity kinetic ? Math.abs(kinetic.getSpeed()) : 0;
    }

    @Override
    protected boolean canRunNow(ReactionRecipe candidate) {
        return candidate.acceptsSpeed(stirSpeed()) && hasHeat(candidate.getRequiredHeat());
    }

    @Override
    protected String whyNotNow(ReactionRecipe recipe) {
        return !recipe.acceptsSpeed(stirSpeed()) ? recipe.stirringShortfall(stirSpeed()) : heatShortfall(recipe.getRequiredHeat());
    }

    @Override
    protected void addStatusTooltip(List<Component> tooltip, BlockPos looked) {
        super.addStatusTooltip(tooltip, looked);
        if (looked.getY() == structure.min().getY()) {
            addHeatTooltip(tooltip);
        }
    }

    public void addStirringTooltip(List<Component> tooltip) {
        float stir = stirSpeed();
        boolean needsStirring = syncedMaxRpm > 0;
        boolean ok = !needsStirring || (stir >= syncedMinRpm && stir <= syncedMaxRpm);
        tooltip.add(Component.literal(needsStirring
                        ? String.format(" - Stirring: %.0f RPM (needs %.0f-%.0f)", stir, syncedMinRpm, syncedMaxRpm)
                        : String.format(" - Stirring: %.0f RPM", stir))
                .withStyle(ok ? ChatFormatting.GRAY : ChatFormatting.RED));
    }

    @Override
    protected SoundEvent workingSound() {
        return CRRSounds.BOILING.get();
    }

    /** The stirred liquid bubbles at its surface. */
    @Override
    protected void onClientWorkingTick(RandomSource random) {
        double surface = liquidSurface();
        if (structure != null && surface > 0 && random.nextInt(2) == 0) {
            Vec3 at = randomInside(random, surface);
            MachineEffects.bubble(level, random, getRenderedFluid(), at.x, at.y, at.z);
        }
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
