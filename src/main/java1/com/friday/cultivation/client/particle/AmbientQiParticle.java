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

/**
 * 环境灵气粒子（严格照搬原模组 com.xiaoxiang.cultivation.client.particle.AmbientQiParticle）。
 * <p>由 AmbientQiHandler 在服务端推送时客户端实例化。</p>
 */
public class AmbientQiParticle extends TextureSheetParticle {
    private final boolean burstMode;

    protected AmbientQiParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz,
                                float r, float g, float b, SpriteSet sprites) {
        super(level, x, y, z, vx, vy, vz);
        this.setSize(0.02f, 0.02f);
        this.burstMode = Math.abs(vx) > 0.15 || Math.abs(vz) > 0.15;
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
        this.pickSprite(sprites);
        this.hasPhysics = false;
    }

    @NotNull
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        float t = (float) this.age / (float) this.lifetime;
        this.alpha = (float) Math.sin(t * Math.PI) * (this.burstMode ? 0.85f : 0.6f);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
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
        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
            return new AmbientQiParticle(level, x, y, z, vx, vy, vz, this.r, this.g, this.b, this.sprites);
        }
    }
}
