package com.koala.reactingreactions.content.laser;

import com.koala.reactingreactions.network.CRRLaserPayload;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.phys.Vec3;

import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** The laser dots on the client: each holder's last aim point, drawn as a red particle while fresh. */
public final class ClientLaserPointers {
    private static final Vector3f DOT_COLOR = new Vector3f(1.0F, 0.05F, 0.05F);
    /** Stale after this many ticks without an update, such as when the pointer is put away. */
    private static final int FRESH_TICKS = 10;

    private record LaserState(Vec3 target, long receivedAt) {
    }

    private static final Map<UUID, LaserState> POINTERS = new HashMap<>();

    private ClientLaserPointers() {
    }

    public static void receive(CRRLaserPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        long now = minecraft.level == null ? 0 : minecraft.level.getGameTime();
        POINTERS.put(payload.shooter(), new LaserState(new Vec3(payload.x(), payload.y(), payload.z()), now));
    }

    /** Draws a small red particle burst at every still-fresh laser dot, every tick for a crisp, responsive point. */
    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.isPaused() || POINTERS.isEmpty()) {
            return;
        }
        long now = minecraft.level.getGameTime();
        var random = minecraft.level.random;
        POINTERS.entrySet().removeIf(entry -> now - entry.getValue().receivedAt() > FRESH_TICKS);
        for (LaserState state : POINTERS.values()) {
            Vec3 pos = state.target();
            minecraft.level.addParticle(new DustParticleOptions(DOT_COLOR, 0.6F), pos.x, pos.y, pos.z, 0, 0, 0);
            if (random.nextFloat() < 0.4F) {
                minecraft.level.addParticle(new DustParticleOptions(DOT_COLOR, 0.35F),
                        pos.x + (random.nextDouble() - 0.5) * 0.1, pos.y + (random.nextDouble() - 0.5) * 0.1, pos.z + (random.nextDouble() - 0.5) * 0.1,
                        0, 0, 0);
            }
        }
    }

    public static void clear() {
        POINTERS.clear();
    }
}
