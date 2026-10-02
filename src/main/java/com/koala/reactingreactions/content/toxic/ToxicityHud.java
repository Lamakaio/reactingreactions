package com.koala.reactingreactions.content.toxic;

import com.koala.reactingreactions.Config;
import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.registry.CRRToxicity;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

/** The toxicity gauge above the food bar, and the green vignette of toxic air and poisoning. */
public final class ToxicityHud {
    private static final ResourceLocation VIGNETTE = ReactingReactions.asResource("textures/misc/toxic_vignette.png");
    private static final int WIDTH = 81;
    // A 5x5 skull, drawn left of the gauge.
    private static final String[] SKULL = {".###.", "#####", "#.#.#", "#####", ".#.#."};

    private static float shownGauge;
    private static float lastSyncedGauge;
    private static boolean rising;

    private ToxicityHud() {
    }

    private static boolean gaugeOn() {
        return Config.toxicityEnabled() && Config.bool(Config.GAUGE_ENABLED, true);
    }

    /** Green creeping in from the edges with bad air or a high gauge, pulsing once the gauge is past 75. */
    public static void renderVignette(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui || !Config.toxicityEnabled()) {
            return;
        }
        float air = ClientContamination.levelAtPlayer();
        float gauge = gaugeOn() ? minecraft.player.getData(CRRToxicity.GAUGE) : 0;
        float strength = Math.max(air > 0.2F ? Math.min(air, 4.0F) / 4.0F : 0, Math.min(gauge, 100.0F) / 100.0F * 0.7F);
        if (strength <= 0.01F) {
            return;
        }
        float time = minecraft.player.tickCount + deltaTracker.getGameTimeDeltaPartialTick(false);
        if (gauge >= 75) {
            strength *= 0.8F + 0.2F * Mth.sin(time * 0.15F);
        }
        RenderSystem.enableBlend();
        graphics.setColor(1, 1, 1, Math.min(1, strength));
        graphics.blit(VIGNETTE, 0, 0, graphics.guiWidth(), graphics.guiHeight(), 0, 0, 256, 256, 256, 256);
        graphics.setColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
        if (air > 0.2F) {
            Component text = Component.translatable("reactingreactions.hud.toxic_air");
            int alpha = Math.round(160 + 95 * (0.5F + 0.5F * Mth.sin(time * 0.2F)));
            graphics.drawString(minecraft.font, text, (graphics.guiWidth() - minecraft.font.width(text)) / 2, graphics.guiHeight() / 2 + 22,
                    (alpha << 24) | 0xB0D040, true);
        }
    }

    /** The bar eases to new values, coloured from green to red along its length, notched at the 25/50/75 effect thresholds. */
    public static void renderGauge(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui || !gaugeOn()) {
            return;
        }
        float gauge = minecraft.player.getData(CRRToxicity.GAUGE);
        if (gauge != lastSyncedGauge) {
            rising = gauge > lastSyncedGauge;
            lastSyncedGauge = gauge;
        }
        shownGauge += (gauge - shownGauge) * 0.1F;
        if (gauge <= 0 && shownGauge < 0.5F) {
            shownGauge = 0;
            return;
        }
        float time = minecraft.player.tickCount + deltaTracker.getGameTimeDeltaPartialTick(false);
        int x = graphics.guiWidth() / 2 + 10;
        int y = graphics.guiHeight() - 49 - 12;
        int filled = Math.round(WIDTH * Math.min(shownGauge, 100.0F) / 100.0F);
        graphics.fill(x - 1, y - 1, x + WIDTH + 1, y + 5, 0xFF101010);
        graphics.fill(x, y, x + WIDTH, y + 4, 0xFF2A2A2A);
        for (int i = 0; i < filled; i++) {
            graphics.fill(x + i, y, x + i + 1, y + 4, colourAt(i / (float) (WIDTH - 1)));
        }
        graphics.fill(x, y, x + filled, y + 1, 0x50FFFFFF);
        // A light band running along the filled part while it climbs.
        if (rising && filled > 3) {
            int band = x + Math.floorMod(Math.round(time * 1.5F), filled + 6) - 3;
            graphics.fill(Math.max(x, band), y, Math.min(x + filled, band + 3), y + 4, 0x60FFFFFF);
        }
        for (int notch = 1; notch < 4; notch++) {
            int nx = x + WIDTH * notch / 4;
            graphics.fill(nx, y, nx + 1, y + 4, 0x90101010);
        }
        if (shownGauge >= 75) {
            int alpha = Math.round(70 * (0.5F + 0.5F * Mth.sin(time * 0.3F)));
            graphics.fill(x - 1, y - 1, x + WIDTH + 1, y + 5, (alpha << 24) | 0xFF2020);
        }
        drawSkull(graphics, x - 7, y - 1, shownGauge >= 75 ? 0xFFE04030 : 0xFFB0D040);
        // A small arrow right of the bar: up while it climbs, down while it clears.
        int ax = x + WIDTH + 3;
        int arrow = rising ? 0xFFE06030 : 0xFF60C060;
        for (int row = 0; row < 3; row++) {
            int ry = rising ? y + row : y + 3 - row;
            graphics.fill(ax + 2 - row, ry, ax + 3 + row, ry + 1, arrow);
        }
    }

    private static int colourAt(float position) {
        int green = 0xFF40C030;
        int yellow = 0xFFE0D030;
        int red = 0xFFE03020;
        return position < 0.5F ? FastColor.ARGB32.lerp(position * 2, green, yellow) : FastColor.ARGB32.lerp((position - 0.5F) * 2, yellow, red);
    }

    private static void drawSkull(GuiGraphics graphics, int x, int y, int colour) {
        for (int row = 0; row < SKULL.length; row++) {
            for (int col = 0; col < SKULL[row].length(); col++) {
                if (SKULL[row].charAt(col) == '#') {
                    graphics.fill(x + col, y + row, x + col + 1, y + row + 1, colour);
                }
            }
        }
    }
}
