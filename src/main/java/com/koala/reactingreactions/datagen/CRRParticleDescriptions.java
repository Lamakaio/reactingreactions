package com.koala.reactingreactions.datagen;

import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.registry.CRRParticles;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.ParticleDescriptionProvider;

/** Generates {@code particles/*.json}: which sprites each particle type animates through. */
final class CRRParticleDescriptions extends ParticleDescriptionProvider {
    CRRParticleDescriptions(PackOutput output, ExistingFileHelper helper) {
        super(output, helper);
    }

    @Override
    protected void addDescriptions() {
        spriteSet(CRRParticles.TOXIC_HAZE.get(), ReactingReactions.asResource("toxic_haze"), 4, false);
    }
}
