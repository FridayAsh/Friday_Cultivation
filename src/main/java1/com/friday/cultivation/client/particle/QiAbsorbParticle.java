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
 * 灵气吸收粒子 - 玩家从脚下吸取灵气时的白色上升光点。
 * 完全照搬原 mod: xiaoxiang.cultivation.client.particle.QiAbsorbParticle
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class QiAbsorbParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final double targetY;
    private final float startY;
    private int tickCounter = 0;

    protected QiAbsorbParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z, vx, vy, vz);
        this.sprites = sprites;
        this.lifetime = 40;
        this.startY = (float) y;
        this.targetY = y + 1.5;
        this.scale(1.0f);
        this.setColor(0.6f, 0.85f, 1.0f);
        this.setAlpha(0.85f);
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
        int frame = Math.min(3, this.tickCounter / 10);
        this.setSprite(this.sprites.get(frame, 0));
        this.setAlpha(0.85f * (1.0f - f));
        this.y = this.startY + (this.targetY - this.startY) * f;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
            return new QiAbsorbParticle(level, x, y, z, vx, vy, vz, sprites);
        }
    }

    @SubscribeEvent
    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(com.friday.cultivation.registry.ModParticles.QI_ABSORB.get(), Provider::new);
    }
}
