package com.koala.reactingreactions.datagen;

import com.koala.reactingreactions.ReactingReactions;

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
        event.getGenerator().addProvider(event.includeServer(), new CRRDataProvider(event.getGenerator().getPackOutput()));
        event.getGenerator().addProvider(event.includeClient(), new CRRSoundDefinitions(event.getGenerator().getPackOutput(), event.getExistingFileHelper()));
        event.getGenerator().addProvider(event.includeClient(), new CRRParticleDescriptions(event.getGenerator().getPackOutput(), event.getExistingFileHelper()));
    }
}
