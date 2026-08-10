package com.friday.cultivation.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 突破粒子 - 玩家境界突破时的金色光圈上升粒子。
 * 完全照搬原 mod: xiaoxiang.cultivation.client.particle.BreakthroughParticle
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class BreakthroughParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final float initialScale;
    private int tickCounter = 0;

    protected BreakthroughParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z, vx, vy, vz);
        this.sprites = sprites;
        this.lifetime = 60;
        this.initialScale = 2.0f;
        this.scale(this.initialScale);
        this.setColor(1.0f, 0.85f, 0.4f);
        this.setAlpha(0.9f);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        this.tickCounter++;
        float f = (float) this.tickCounter / (float) this.lifetime;
        int frame = Math.min(3, this.tickCounter / 5);
        this.setSprite(this.sprites.get(frame, 0));
        float fade = 1.0f - f;
        this.setAlpha(fade * 0.9f);
        this.scale(this.initialScale * (1.0f + f * 1.5f));
        this.y += 0.04;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
            return new BreakthroughParticle(level, x, y, z, vx, vy, vz, sprites);
        }
    }

    @SubscribeEvent
    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(com.friday.cultivation.registry.ModParticles.BREAKTHROUGH.get(), Provider::new);
    }
}
