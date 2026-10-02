package com.koala.reactingreactions.content.toxic;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.util.Mth;

/** A puff of toxic air or gas: swells, drifts and turns slowly, fading in and out, its sprite growing wispier with age. */
public class HazeParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final float peakAlpha;
    private final float startSize;
    private final float spin;

    protected HazeParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, ColorParticleOption colour, SpriteSet sprites) {
        super(level, x, y, z, dx, dy, dz);
        this.sprites = sprites;
        xd = dx + (random.nextDouble() - 0.5) * 0.01;
        yd = dy + 0.003;
        zd = dz + (random.nextDouble() - 0.5) * 0.01;
        friction = 0.96F;
        gravity = -0.002F;
        hasPhysics = true;
        lifetime = 50 + random.nextInt(40);
        rCol = colour.getRed();
        gCol = colour.getGreen();
        bCol = colour.getBlue();
        peakAlpha = colour.getAlpha();
        alpha = 0;
        startSize = 0.25F + random.nextFloat() * 0.2F;
        quadSize = startSize;
        spin = (random.nextFloat() - 0.5F) * 0.04F;
        roll = random.nextFloat() * Mth.TWO_PI;
        setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (removed) {
            return;
        }
        float life = age / (float) lifetime;
        // In over the first fifth, out over the last half.
        alpha = peakAlpha * Math.min(1, life * 5) * Math.min(1, (1 - life) * 2);
        quadSize = startSize * (1 + life * 1.5F);
        oRoll = roll;
        roll += spin;
        setSpriteFromAge(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public record Provider(SpriteSet sprites) implements ParticleProvider<ColorParticleOption> {
        @Override
        public Particle createParticle(ColorParticleOption colour, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            return new HazeParticle(level, x, y, z, dx, dy, dz, colour, sprites);
        }
    }
}
