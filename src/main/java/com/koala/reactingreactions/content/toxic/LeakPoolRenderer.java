package com.koala.reactingreactions.content.toxic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;


/** Draws a leak pool as a flat, tinted puddle in the fluid's own texture, in the outline of its {@link PoolShape}. */
public class LeakPoolRenderer extends EntityRenderer<LeakPoolEntity> {
    public LeakPoolRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(LeakPoolEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    @Override
    public void render(LeakPoolEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light) {
        Fluid fluid = entity.getFluid();
        if (fluid == Fluids.EMPTY) {
            return;
        }
        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid);
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(extensions.getStillTexture());
        int tint = extensions.getTintColor(new FluidStack(fluid, 1000));
        int red = (tint >> 16) & 0xFF;
        int green = (tint >> 8) & 0xFF;
        int blue = tint & 0xFF;
        // A slow swell, so the surface never sits quite still.
        float breath = 1 + 0.02F * Mth.sin((entity.tickCount + partialTicks) * 0.08F + entity.getId());
        float half = entity.shownWidth(partialTicks) / 2.0F * breath;
        if (half <= 0.01F) {
            return;
        }
        PoolShape shape = entity.shape();
        float fill = entity.fill();
        Surface surface = new Surface(buffer.getBuffer(RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS)), poseStack, sprite,
                red, green, blue, light, half);
        // Overlapping pools each get a slightly different height, so they do not flicker into each other.
        float y = 0.015F + (entity.getId() % 8) * 0.001F;
        float[] xs = new float[SEGMENTS];
        float[] zs = new float[SEGMENTS];
        for (int i = 0; i < SEGMENTS; i++) {
            float angle = i * Mth.TWO_PI / SEGMENTS;
            float r = shape.radius(angle, fill) * half;
            xs[i] = Mth.cos(angle) * r;
            zs[i] = Mth.sin(angle) * r;
        }
        // A solid middle out to 80% of the outline, then a band fading out to the edge, drawn as quads.
        for (int i = 0; i < SEGMENTS; i++) {
            int j = (i + 1) % SEGMENTS;
            surface.quad(0, 0, ALPHA, 0, 0, ALPHA, xs[i] * INNER, zs[i] * INNER, ALPHA, xs[j] * INNER, zs[j] * INNER, ALPHA, y);
            surface.quad(xs[i] * INNER, zs[i] * INNER, ALPHA, xs[i], zs[i], EDGE_ALPHA, xs[j], zs[j], EDGE_ALPHA, xs[j] * INNER, zs[j] * INNER, ALPHA, y);
        }
        for (PoolShape.Droplet drop : shape.droplets(fill)) {
            float cx = drop.x() * half;
            float cz = drop.z() * half;
            float r = drop.radius() * Math.min(1, half);
            for (int i = 0; i < DROP_SEGMENTS; i++) {
                float a0 = i * Mth.TWO_PI / DROP_SEGMENTS;
                float a1 = (i + 1) * Mth.TWO_PI / DROP_SEGMENTS;
                surface.quad(cx, cz, ALPHA, cx, cz, ALPHA, cx + Mth.cos(a0) * r, cz + Mth.sin(a0) * r, EDGE_ALPHA,
                        cx + Mth.cos(a1) * r, cz + Mth.sin(a1) * r, EDGE_ALPHA, y);
            }
        }
    }

    private static final int SEGMENTS = 40;
    private static final int DROP_SEGMENTS = 10;
    private static final float INNER = 0.8F;
    private static final int ALPHA = 200;
    private static final int EDGE_ALPHA = 40;

    /** Quads on the pool's surface, the fluid texture spread over its full width. */
    private record Surface(VertexConsumer consumer, PoseStack poseStack, TextureAtlasSprite sprite, int red, int green, int blue, int light,
            float half) {
        // Counter-clockwise seen from above, so the face points up.
        void quad(float x0, float z0, int a0, float x1, float z1, int a1, float x2, float z2, int a2, float x3, float z3, int a3, float y) {
            vertex(x0, z0, a0, y);
            vertex(x3, z3, a3, y);
            vertex(x2, z2, a2, y);
            vertex(x1, z1, a1, y);
        }

        private void vertex(float x, float z, int alpha, float y) {
            // Droplets can sit past the edge; the texture is clamped to the sprite there.
            float u = sprite.getU(Mth.clamp((x / half + 1) / 2, 0, 1));
            float v = sprite.getV(Mth.clamp((z / half + 1) / 2, 0, 1));
            consumer.addVertex(poseStack.last().pose(), x, y, z).setColor(red, green, blue, alpha).setUv(u, v)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(poseStack.last(), 0, 1, 0);
        }
    }
}
