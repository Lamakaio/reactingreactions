package com.koala.reactingreactions.registry;

import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.content.toxic.ChemicalFlaskEntity;
import com.koala.reactingreactions.content.toxic.LeakPoolEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CRREntities {
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, ReactingReactions.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<LeakPoolEntity>> LEAK_POOL = ENTITIES.register("leak_pool",
            () -> EntityType.Builder.<LeakPoolEntity>of(LeakPoolEntity::new, MobCategory.MISC).sized(1.0F, 0.1F).clientTrackingRange(6)
                    .updateInterval(20).noSummon().build("leak_pool"));

    public static final DeferredHolder<EntityType<?>, EntityType<ChemicalFlaskEntity>> CHEMICAL_FLASK = ENTITIES.register("chemical_flask",
            () -> EntityType.Builder.<ChemicalFlaskEntity>of(ChemicalFlaskEntity::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(4)
                    .updateInterval(10).build("chemical_flask"));

    public static void register(IEventBus modEventBus) {
        ENTITIES.register(modEventBus);
    }
}
