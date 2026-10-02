package com.koala.reactingreactions.content.toxic;

import com.koala.reactingreactions.ReactingReactions;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/** Draws the stains contaminated air leaves on the ground ({@link ClientContamination}) as decals on the blocks' top faces. */
public final class ToxicStainRenderer {
    private static final ResourceLocation STAIN = ReactingReactions.asResource("textures/misc/toxic_stain.png");
    private static final double MAX_DISTANCE_SQR = 48 * 48;
    private static final float[][] CORNERS = {{0, 0}, {0, 1}, {1, 1}, {1, 0}};

    private ToxicStainRenderer() {
    }

    public static void render(RenderLevelStageEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        // Touches the shared buffers only when there is something to draw.
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS || minecraft.level == null || !ClientContamination.hasStains()) {
            return;
        }
        Vec3 camera = event.getCamera().getPosition();
        PoseStack ms = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucent(STAIN));
        ms.pushPose();
        ms.translate(-camera.x, -camera.y, -camera.z);
        BlockPos.MutableBlockPos ground = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos above = new BlockPos.MutableBlockPos();
        ClientContamination.forEachStain((key, darkness) -> {
            ground.set(key);
            if (ground.distToCenterSqr(camera) > MAX_DISTANCE_SQR) {
                return;
            }
            int light = LevelRenderer.getLightColor(minecraft.level, above.setWithOffset(ground, 0, 1, 0));
            decal(ms, consumer, ground, Math.round(darkness * 220), light);
        });
        ms.popPose();
        buffers.endBatch(RenderType.entityTranslucent(STAIN));
    }

    /** One quad just above the block's top, the texture turned by a quarter per position so neighbours do not repeat. */
    private static void decal(PoseStack ms, VertexConsumer consumer, BlockPos ground, int alpha, int light) {
        float y = ground.getY() + 1.003F;
        int turn = Math.floorMod(ground.getX() * 31 + ground.getZ() * 17, 4);
        var pose = ms.last();
        for (int i = 0; i < 4; i++) {
            float[] uv = CORNERS[(i + turn) % 4];
            consumer.addVertex(pose, ground.getX() + CORNERS[i][0], y, ground.getZ() + CORNERS[i][1]).setColor(255, 255, 255, alpha)
                    .setUv(uv[0], uv[1]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        }
    }
}
