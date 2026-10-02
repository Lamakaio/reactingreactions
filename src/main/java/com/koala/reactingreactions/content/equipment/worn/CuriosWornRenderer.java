package com.koala.reactingreactions.content.equipment.worn;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.client.ICurioRenderer;

/** {@link WornModels} for Curios' slots; only loaded with Curios. */
public final class CuriosWornRenderer implements ICurioRenderer {
    public static void register() {
        for (var entry : WornModels.items()) {
            CuriosRendererRegistry.register(entry.get(), CuriosWornRenderer::new);
        }
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack ms,
                                                                          RenderLayerParent<T, M> parent, MultiBufferSource buffer, int light,
                                                                          float limbSwing, float limbSwingAmount, float partialTicks,
                                                                          float ageInTicks, float netHeadYaw, float headPitch) {
        if (parent.getModel() instanceof HumanoidModel<?> body) {
            WornModels.render(stack, body, ms, buffer, light);
        }
    }
}
