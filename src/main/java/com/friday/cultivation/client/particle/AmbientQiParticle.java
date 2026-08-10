/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.particle.Particle
 *  net.minecraft.client.particle.ParticleProvider
 *  net.minecraft.client.particle.ParticleRenderType
 *  net.minecraft.client.particle.SpriteSet
 *  net.minecraft.client.particle.TextureSheetParticle
 *  net.minecraft.core.particles.SimpleParticleType
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AmbientQiParticle
extends TextureSheetParticle {
    private final boolean burstMode;

    protected AmbientQiParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, float r, float g, float b, SpriteSet sprites) {
        super(level, x, y, z, vx, vy, vz);
        this.setSize(0.02f, 0.02f);
        boolean bl = this.burstMode = Math.abs(vx) > 0.15 || Math.abs(vz) > 0.15;
        if (this.burstMode) {
            this.lifetime = 8 + this.random.nextInt(6);
            this.gravity = 0.0f;
            this.friction = 0.7f;
        } else {
            this.lifetime = 60 + this.random.nextInt(40);
            this.gravity = -0.005f;
            this.friction = 0.96f;
        }
        this.quadSize = 0.04f + this.random.nextFloat() * 0.03f;
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
        this.alpha = 0.0f;
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.setSpriteFromAge(sprites);
        this.hasPhysics = false;
    }

    @NotNull
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public void tick() {
        super.tick();
        float t = (float)this.age / (float)this.lifetime;
        this.alpha = (float)Math.sin((double)t * Math.PI) * (this.burstMode ? 0.85f : 0.6f);
    }

    public static class Provider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final float r;
        private final float g;
        private final float b;

        public Provider(SpriteSet sprites, float r, float g, float b) {
            this.sprites = sprites;
            this.r = r;
            this.g = g;
            this.b = b;
        }

        @Nullable
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
            return new AmbientQiParticle(level, x, y, z, vx, vy, vz, this.r, this.g, this.b, this.sprites);
        }
    }
}

