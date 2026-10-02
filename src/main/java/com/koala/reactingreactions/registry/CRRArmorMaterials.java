package com.koala.reactingreactions.registry;

import com.koala.reactingreactions.ReactingReactions;

import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;

/**
 * Custom {@code Holder<ArmorMaterial>} entries.
 */
public class CRRArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, ReactingReactions.MODID);

    // Same defense/toughness/enchantability as Diamond but applies Speed/Haste/Attack-Speed buffs in CRREquipmentEvents.
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> TITANIUM = ARMOR_MATERIALS.register(
            "titanium",
            () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.BOOTS, 3);
                        map.put(ArmorItem.Type.LEGGINGS, 6);
                        map.put(ArmorItem.Type.CHESTPLATE, 8);
                        map.put(ArmorItem.Type.HELMET, 3);
                        map.put(ArmorItem.Type.BODY, 11);
                    }),
                    10,
                    SoundEvents.ARMOR_EQUIP_DIAMOND,
                    () -> Ingredient.of(CRRItems.TITANIUM.get()),
                    List.of(new ArmorMaterial.Layer(ReactingReactions.asResource("titanium"))),
                    2.0F,
                    0.0F));

    /** Netherite's protection; the pieces are unbreakable, so there is no repair ingredient. */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> COMPOSITE_EXO = ARMOR_MATERIALS.register(
            "composite_exo",
            () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.BOOTS, 3);
                        map.put(ArmorItem.Type.LEGGINGS, 6);
                        map.put(ArmorItem.Type.CHESTPLATE, 8);
                        map.put(ArmorItem.Type.HELMET, 3);
                        map.put(ArmorItem.Type.BODY, 11);
                    }),
                    15,
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.EMPTY,
                    List.of(new ArmorMaterial.Layer(ReactingReactions.asResource("composite_exo"))),
                    3.0F,
                    0.1F));

    public static void register(IEventBus modEventBus) {
        ARMOR_MATERIALS.register(modEventBus);
    }
}
