package com.koala.reactingreactions.content.airlessoven;

import com.koala.reactingreactions.content.airlessoven.recipe.AirlessOvenRecipe;
import com.koala.reactingreactions.content.multiblock.MachineEffects;
import com.koala.reactingreactions.content.multiblock.MachineTiers;
import com.koala.reactingreactions.content.multiblock.MultiblockControllerBlockEntity;
import com.koala.reactingreactions.registry.CRRBlocks;
import com.koala.reactingreactions.registry.CRRRecipeTypes;
import com.koala.reactingreactions.registry.CRRSounds;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class AirlessOvenControllerBlockEntity extends MultiblockControllerBlockEntity<AirlessOvenRecipe> {
    public static final Spec<AirlessOvenRecipe> SPEC = new Spec<>("Airless Oven",
            state -> state.is(CRRBlocks.AIRLESS_OVEN_WALL.get()), state -> state.is(CRRBlocks.AIRLESS_OVEN_CONTROLLER.get()),
            BlockState::isAir, 3, 3, 3, 3, (x, z) -> x.equals(z), 4, 4, false, CRRRecipeTypes.AIRLESS_OVEN::get);

    public AirlessOvenControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, SPEC);
    }

    @Override
    protected List<MachineTiers.Tier> tiers() {
        return MachineTiers.AIRLESS_OVEN;
    }

    @Override
    protected SoundEvent workingSound() {
        return CRRSounds.OVEN_ROAR.get();
    }

    /** Smoke rises from the roof while it bakes; the renderer lights the firebox. */
    @Override
    protected void onClientWorkingTick(RandomSource random) {
        if (structure != null && random.nextInt(4) == 0) {
            // Formed, it smokes from the chimney in the back corner (tools/machine_models.py).
            MachineTiers.Fit fit = getBlockState().getValue(MachineTiers.PART) > 0 ? fit(structure) : null;
            Vec3 roof = fit != null ? MachineTiers.toWorld(structure, fit, 39, 62, 39) : roofTop();
            MachineEffects.puff(level, random, ParticleTypes.LARGE_SMOKE, roof.x, roof.y, roof.z);
        }
    }
}
