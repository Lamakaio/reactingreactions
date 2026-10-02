package com.koala.reactingreactions.content.ponder;

import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.ponder.api.element.PonderOverlayElement;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.element.PonderElementBase;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.client.gui.GuiGraphics;

/**
 * The toxicity gauge as the HUD draws it (see {@code ReactingReactionsClient}), shown below the middle of a Ponder scene. Its
 * level eases toward {@link #setTarget}, 0 to 100.
 */
final class GaugeOverlayElement extends PonderElementBase implements PonderOverlayElement {
    private static final int WIDTH = 81;
    private final LerpedFloat level = LerpedFloat.linear().startWithValue(0);

    void setTarget(float target) {
        level.chase(target, 0.08, LerpedFloat.Chaser.EXP);
    }

    @Override
    public void reset(PonderScene scene) {
        level.startWithValue(0);
    }

    @Override
    public void tick(PonderScene scene) {
        level.tickChaser();
    }

    @Override
    public void render(PonderScene scene, PonderUI screen, GuiGraphics graphics, float partialTicks) {
        float gauge = level.getValue(partialTicks);
        int x = graphics.guiWidth() / 2 - WIDTH / 2;
        int y = graphics.guiHeight() * 3 / 4;
        int filled = Math.round(WIDTH * Math.min(gauge, 100.0F) / 100.0F);
        // Green through yellow to red as it fills, like the HUD.
        float t = Math.min(gauge, 100.0F) / 100.0F;
        int red = Math.min(255, Math.round(510 * t));
        int green = Math.min(255, Math.round(510 * (1 - t)));
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 400);
        graphics.fill(x - 1, y - 1, x + WIDTH + 1, y + 5, 0xFF101010);
        graphics.fill(x, y, x + filled, y + 4, 0xFF000000 | (red << 16) | (green << 8) | 0x20);
        graphics.pose().popPose();
    }
}
