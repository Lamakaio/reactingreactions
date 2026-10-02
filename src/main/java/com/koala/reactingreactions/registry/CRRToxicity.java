package com.koala.reactingreactions.registry;

import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.content.toxic.ToxicFluid;
import com.koala.reactingreactions.content.toxic.ToxicItem;
import com.mojang.serialization.Codec;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

/** Registrations for toxic compounds: the two data maps, the player's gauge and the damage type. */
public class CRRToxicity {
    public static final DataMapType<Fluid, ToxicFluid> TOXIC_FLUID = DataMapType
            .builder(ReactingReactions.asResource("toxicity"), Registries.FLUID, ToxicFluid.CODEC).synced(ToxicFluid.CODEC, false).build();
    public static final DataMapType<Item, ToxicItem> TOXIC_ITEM = DataMapType
            .builder(ReactingReactions.asResource("toxic_item"), Registries.ITEM, ToxicItem.CODEC).synced(ToxicItem.CODEC, false).build();

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ReactingReactions.MODID);

    /** The player's personal toxicity gauge, 0-100. Not copied on death, so dying empties it. Synced so the client can draw it. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Float>> GAUGE = ATTACHMENTS.register("toxicity",
            () -> AttachmentType.builder(() -> 0.0F).serialize(Codec.FLOAT).sync(ByteBufCodecs.FLOAT).build());

    public static final ResourceKey<DamageType> TOXICITY_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE,
            ReactingReactions.asResource("toxicity"));

    public static void register(IEventBus modEventBus) {
        ATTACHMENTS.register(modEventBus);
        modEventBus.addListener(CRRToxicity::registerDataMaps);
    }

    private static void registerDataMaps(RegisterDataMapTypesEvent event) {
        event.register(TOXIC_FLUID);
        event.register(TOXIC_ITEM);
    }
}
