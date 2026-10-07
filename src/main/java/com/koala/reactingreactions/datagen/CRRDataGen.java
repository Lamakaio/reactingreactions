package com.koala.reactingreactions.datagen;

import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.content.render.UpturnedBucketSprites;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/** Entry point of {@code ./gradlew runData}: output goes to {@code src/generated/resources} (see build.gradle). */
@EventBusSubscriber(modid = ReactingReactions.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class CRRDataGen {
    private CRRDataGen() {
    }

    @SubscribeEvent
    public static void gather(GatherDataEvent event) {
        // Made while the atlas loads, not files: models may still use them.
        for (ResourceLocation sprite : new ResourceLocation[] {UpturnedBucketSprites.BUCKET, UpturnedBucketSprites.GAS}) {
            event.getExistingFileHelper().trackGenerated(sprite, PackType.CLIENT_RESOURCES, ".png", "textures");
        }
        event.getGenerator().addProvider(event.includeServer(), new CRRDataProvider(event.getGenerator().getPackOutput()));
        event.getGenerator().addProvider(event.includeClient(), new CRRSoundDefinitions(event.getGenerator().getPackOutput(), event.getExistingFileHelper()));
        event.getGenerator().addProvider(event.includeClient(), new CRRAtlases(event.getGenerator().getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper()));
        event.getGenerator().addProvider(event.includeClient(), new CRRParticleDescriptions(event.getGenerator().getPackOutput(), event.getExistingFileHelper()));
    }
}
