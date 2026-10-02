package com.koala.reactingreactions.datagen;

import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.registry.CRRSounds;

import net.minecraft.data.PackOutput;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Generates sounds.json: each sound event, its file and its subtitle. */
final class CRRSoundDefinitions extends SoundDefinitionsProvider {
    CRRSoundDefinitions(PackOutput output, ExistingFileHelper helper) {
        super(output, ReactingReactions.MODID, helper);
    }

    @Override
    public void registerSounds() {
        define(CRRSounds.DRILL_RUMBLE, "drill_rumble", "subtitles.reactingreactions.drill_rumble");
        define(CRRSounds.SCRUBBER_HUM, "scrubber_hum", "subtitles.reactingreactions.scrubber_hum");
        define(CRRSounds.GAS_HISS, "gas_hiss", "subtitles.reactingreactions.gas_hiss");
        define(CRRSounds.IGNITE_WHOOSH, "ignite_whoosh", "subtitles.reactingreactions.ignite_whoosh");
        define(CRRSounds.POOL_SPLASH, "pool_splash", "subtitles.reactingreactions.pool_splash");
        define(CRRSounds.LEAK_DRIP, "leak_drip", "subtitles.reactingreactions.leak_drip");
        define(CRRSounds.VAT_BUZZ, "vat_buzz", "subtitles.reactingreactions.vat_buzz");
        define(CRRSounds.BOILING, "boiling", "subtitles.reactingreactions.boiling");
        define(CRRSounds.OVEN_ROAR, "oven_roar", "subtitles.reactingreactions.oven_roar");
    }

    private void define(DeferredHolder<SoundEvent, SoundEvent> event, String file, String subtitle) {
        add(event, definition().subtitle(subtitle).with(sound(ReactingReactions.asResource(file))));
    }
}
