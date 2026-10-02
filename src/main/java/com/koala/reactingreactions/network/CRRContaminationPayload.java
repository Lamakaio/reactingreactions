package com.koala.reactingreactions.network;

import com.koala.reactingreactions.ReactingReactions;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

/** Server to client: the contaminated cells near a player (packed block positions and their levels), so the client can draw particles and warn. */
public record CRRContaminationPayload(List<Long> positions, List<Float> levels) implements CustomPacketPayload {
    public static final Type<CRRContaminationPayload> TYPE = new Type<>(ReactingReactions.asResource("contamination"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CRRContaminationPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG.apply(ByteBufCodecs.list()), CRRContaminationPayload::positions,
            ByteBufCodecs.FLOAT.apply(ByteBufCodecs.list()), CRRContaminationPayload::levels,
            CRRContaminationPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
