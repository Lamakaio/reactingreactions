package com.koala.reactingreactions.content.render;

import com.koala.reactingreactions.ReactingReactions;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;

/** The moving pieces of the formed machines, drawn and turned by their renderers (models from {@code tools/machine_models.py}). */
public final class CRRPartialModels {
    public static final PartialModel STIRRER_FAN = moving("stirrer_fan");
    public static final PartialModel GAUGE_NEEDLE = moving("gauge_needle");

    private CRRPartialModels() {
    }

    private static PartialModel moving(String name) {
        return PartialModel.of(ReactingReactions.asResource("block/moving/" + name));
    }

    /** Loads the class during client setup, so the models are registered before baking. */
    public static void init() {
    }
}
