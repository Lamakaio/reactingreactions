package com.koala.reactingreactions.content.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import org.joml.Matrix4f;

/** Flat, double-sided quads of a block-atlas texture, for the parts renderers draw: spinning blades, dials, fire, trays. */
public final class FlatQuads {
    private FlatQuads() {
    }

    public static TextureAtlasSprite sprite(ResourceLocation texture) {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);
    }

    public static VertexConsumer consumer(MultiBufferSource buffer) {
        return buffer.getBuffer(RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS));
    }

    /** A quad in the local xy plane, from (x0, y0) to (x1, y1) at depth z, with the sprite's texture stretched over it. */
    public static void quad(PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite, float x0, float y0, float x1, float y1, float z, int light) {
        quad(poseStack, consumer, sprite, x0, y0, x1, y1, z, light, 0, 0, 1, 1);
    }

    /** As above, showing only the part of the sprite from (su0, sv0) to (su1, sv1), as fractions of it. */
    public static void quad(PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite, float x0, float y0, float x1, float y1, float z, int light,
                            float su0, float sv0, float su1, float sv1) {
        Matrix4f matrix = poseStack.last().pose();
        float u0 = sprite.getU(su0);
        float u1 = sprite.getU(su1);
        float v0 = sprite.getV(sv0);
        float v1 = sprite.getV(sv1);
        vertex(consumer, poseStack, matrix, x0, y0, z, u0, v1, light);
        vertex(consumer, poseStack, matrix, x1, y0, z, u1, v1, light);
        vertex(consumer, poseStack, matrix, x1, y1, z, u1, v0, light);
        vertex(consumer, poseStack, matrix, x0, y1, z, u0, v0, light);
    }

    private static void vertex(VertexConsumer consumer, PoseStack poseStack, Matrix4f matrix, float x, float y, float z, float u, float v, int light) {
        consumer.addVertex(matrix, x, y, z).setColor(255, 255, 255, 255).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(poseStack.last(), 0, 0, 1);
    }

    /** Two crossing vertical blades of the given half width, from {@code yBottom} to {@code yTop}, turned by {@code degrees} around the vertical axis through (0.5, 0.5). */
    public static void rod(PoseStack poseStack, MultiBufferSource buffer, ResourceLocation texture, float halfWidth, float yBottom, float yTop, float degrees, int light) {
        TextureAtlasSprite sprite = sprite(texture);
        VertexConsumer consumer = consumer(buffer);
        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(degrees));
        for (int blade = 0; blade < 2; blade++) {
            for (float y = yBottom; y < yTop; y += 1.0F) {
                quad(poseStack, consumer, sprite, -halfWidth, y, halfWidth, Math.min(y + 1.0F, yTop), 0, light);
            }
            poseStack.mulPose(Axis.YP.rotationDegrees(90));
        }
        poseStack.popPose();
    }

    /** A fan of {@code blades} rectangular blades in the local xy plane at depth {@code z}, turned by {@code degrees}. */
    public static void fan(PoseStack poseStack, MultiBufferSource buffer, ResourceLocation texture, int blades, float length, float width, float z, float degrees, int light) {
        TextureAtlasSprite sprite = sprite(texture);
        VertexConsumer consumer = consumer(buffer);
        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(degrees));
        for (int i = 0; i < blades; i++) {
            quad(poseStack, consumer, sprite, 0.02F, -width / 2, length, width / 2, z, light);
            poseStack.mulPose(Axis.ZP.rotationDegrees(360.0F / blades));
        }
        poseStack.popPose();
    }
}
