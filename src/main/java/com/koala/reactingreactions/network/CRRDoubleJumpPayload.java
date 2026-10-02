package com.koala.reactingreactions.network;

import com.koala.reactingreactions.ReactingReactions;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * A no-data marker sent client -> server when the player presses jump while
 * airborne, requesting a Aerozine Thrusters boost. I didnt find an 
 * event for "jump pressed mid-air" so this has to be detected client-side
 * and relayed to the server, which
 * validates and applies everything itself (equipped item, fuel, cooldown).
 */
public record CRRDoubleJumpPayload() implements CustomPacketPayload {
    public static final Type<CRRDoubleJumpPayload> TYPE =
            new Type<>(ReactingReactions.asResource("double_jump"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CRRDoubleJumpPayload> CODEC =
            StreamCodec.unit(new CRRDoubleJumpPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
