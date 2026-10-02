package com.koala.reactingreactions.registry;

import com.koala.reactingreactions.ReactingReactions;

import net.minecraft.server.level.ServerPlayer;

/** Grants this mod's advancements that have no vanilla trigger (their criterion is "impossible" and only code can complete it). */
public final class CRRAdvancements {
    private CRRAdvancements() {
    }

    public static void grant(ServerPlayer player, String path) {
        var holder = player.server.getAdvancements().get(ReactingReactions.asResource(path));
        if (holder != null) {
            player.getAdvancements().award(holder, "impossible");
        }
    }
}
