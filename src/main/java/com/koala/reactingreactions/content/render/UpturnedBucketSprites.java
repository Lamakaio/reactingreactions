package com.koala.reactingreactions.content.render;

import com.koala.reactingreactions.ReactingReactions;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;

import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;

/**
 * The gas bucket's sprites, made while the atlas loads from the vanilla water bucket turned upside down (so no copy of a
 * Mojang texture ships with the mod): the bucket without its water, and the water alone in grey, which the item colour tints.
 */
public record UpturnedBucketSprites() implements SpriteSource {
    public static final MapCodec<UpturnedBucketSprites> CODEC = MapCodec.unit(new UpturnedBucketSprites());
    public static final SpriteSourceType TYPE = new SpriteSourceType(CODEC);
    public static final ResourceLocation ID = ReactingReactions.asResource("upturned_bucket");
    public static final ResourceLocation BUCKET = ReactingReactions.asResource("item/gas_bucket");
    public static final ResourceLocation GAS = ReactingReactions.asResource("item/gas_bucket_gas");
    private static final ResourceLocation WATER_BUCKET = ResourceLocation.withDefaultNamespace("textures/item/water_bucket.png");

    @Override
    public void run(ResourceManager resourceManager, Output output) {
        resourceManager.getResource(WATER_BUCKET).ifPresent(resource -> {
            output.add(BUCKET, loader -> make(resource, BUCKET, false));
            output.add(GAS, loader -> make(resource, GAS, true));
        });
    }

    @Override
    public SpriteSourceType type() {
        return TYPE;
    }

    @Nullable
    private static SpriteContents make(Resource resource, ResourceLocation id, boolean gas) {
        try (InputStream in = resource.open(); NativeImage water = NativeImage.read(in)) {
            int w = water.getWidth(), h = water.getHeight();
            int brightest = 1;
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int c = water.getPixelRGBA(x, y);
                    if (isWater(c)) {
                        brightest = Math.max(brightest, luminance(c));
                    }
                }
            }
            NativeImage image = new NativeImage(w, h, true);
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int c = water.getPixelRGBA(x, h - 1 - y);
                    int out = 0;
                    if (isWater(c) == gas) {
                        // The water's own shading, in grey with its brightest pixel white, for the tint to colour.
                        int v = gas ? Math.min(255, luminance(c) * 255 / brightest) : 0;
                        out = gas ? (c & 0xFF000000) | v << 16 | v << 8 | v : c;
                    }
                    image.setPixelRGBA(x, y, out);
                }
            }
            return new SpriteContents(id, new FrameSize(w, h), image, ResourceMetadata.EMPTY);
        } catch (IOException e) {
            LogUtils.getLogger().error("Could not make {} from the water bucket", id, e);
            return null;
        }
    }

    // NativeImage pixels are ABGR.
    private static boolean isWater(int c) {
        int r = c & 0xFF, g = c >> 8 & 0xFF, b = c >> 16 & 0xFF;
        return c >>> 24 != 0 && b > r + 40 && b > g + 10;
    }

    private static int luminance(int c) {
        return ((c & 0xFF) * 30 + (c >> 8 & 0xFF) * 59 + (c >> 16 & 0xFF) * 11) / 100;
    }
}
