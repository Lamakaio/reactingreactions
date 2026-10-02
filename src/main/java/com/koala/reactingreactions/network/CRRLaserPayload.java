package com.koala.reactingreactions.network;

import com.koala.reactingreactions.ReactingReactions;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

/** Where a laser pointer's holder is aiming, sent to nearby clients. */
public record CRRLaserPayload(UUID shooter, double x, double y, double z) implements CustomPacketPayload {
    public static final Type<CRRLaserPayload> TYPE = new Type<>(ReactingReactions.asResource("laser"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CRRLaserPayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, CRRLaserPayload::shooter,
            ByteBufCodecs.DOUBLE, CRRLaserPayload::x,
            ByteBufCodecs.DOUBLE, CRRLaserPayload::y,
            ByteBufCodecs.DOUBLE, CRRLaserPayload::z,
            CRRLaserPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
