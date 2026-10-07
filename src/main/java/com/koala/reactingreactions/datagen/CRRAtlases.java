package com.koala.reactingreactions.datagen;

import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.content.render.UpturnedBucketSprites;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SpriteSourceProvider;

import java.util.concurrent.CompletableFuture;

/** Generates {@code atlases/blocks.json}: the sprites made while the atlas loads. */
final class CRRAtlases extends SpriteSourceProvider {
    CRRAtlases(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper helper) {
        super(output, lookup, ReactingReactions.MODID, helper);
    }

    @Override
    protected void gather() {
        atlas(BLOCKS_ATLAS).addSource(new UpturnedBucketSprites());
    }
}
