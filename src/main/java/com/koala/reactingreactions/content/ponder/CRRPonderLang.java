package com.koala.reactingreactions.content.ponder;

import com.koala.reactingreactions.ReactingReactions;

import net.createmod.ponder.foundation.PonderIndex;

import java.util.function.BiConsumer;

/** Datagen only: Ponder shows scene titles and text from lang keys, so every scene's English is written into en_us.json. */
public final class CRRPonderLang {
    private CRRPonderLang() {
    }

    public static void provide(BiConsumer<String, String> consumer) {
        PonderIndex.addPlugin(new CRRPonderPlugin());
        PonderIndex.getLangAccess().provideLang(ReactingReactions.MODID, consumer);
    }
}
