package com.koala.reactingreactions.network;

import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.item.ExoSettings;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Client to server: one setting of the chase item in an inventory slot changed on the Exo Settings screen. */
public record CRRExoSettingPayload(int slot, String key, int value) implements CustomPacketPayload {
    public static final Type<CRRExoSettingPayload> TYPE = new Type<>(ReactingReactions.asResource("exo_setting"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CRRExoSettingPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CRRExoSettingPayload::slot, ByteBufCodecs.STRING_UTF8, CRRExoSettingPayload::key,
            ByteBufCodecs.VAR_INT, CRRExoSettingPayload::value, CRRExoSettingPayload::new);

    public void apply(Player player) {
        if (slot < 0 || slot >= player.getInventory().getContainerSize()) {
            return;
        }
        ItemStack stack = player.getInventory().getItem(slot);
        if (stack.getItem() instanceof ExoSettings.Configurable configurable) {
            configurable.set(stack, key, value);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
