package com.koala.reactingreactions.content.equipment.worn;

import com.mojang.blaze3d.vertex.PoseStack;

import io.wispforest.accessories.api.client.AccessoriesRendererRegistry;
import io.wispforest.accessories.api.client.AccessoryRenderer;
import io.wispforest.accessories.api.slot.SlotReference;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** {@link WornModels} for Accessories' slots; only loaded with Accessories. */
public final class AccessoriesWornRenderer implements AccessoryRenderer {
    public static void register() {
        for (var entry : WornModels.items()) {
            AccessoriesRendererRegistry.registerRenderer(entry.get(), AccessoriesWornRenderer::new);
        }
    }

    @Override
    public <M extends LivingEntity> void render(ItemStack stack, SlotReference reference, PoseStack ms, EntityModel<M> model, MultiBufferSource buffer,
                                                int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                                                float netHeadYaw, float headPitch) {
        if (model instanceof HumanoidModel<?> body) {
            WornModels.render(stack, body, ms, buffer, light);
        }
    }
}
