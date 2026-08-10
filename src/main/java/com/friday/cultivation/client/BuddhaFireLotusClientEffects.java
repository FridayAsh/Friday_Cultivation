/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Camera
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.core.particles.DustParticleOptions
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.core.particles.SimpleParticleType
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.client.gui.overlay.IGuiOverlay
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  org.joml.Vector3f
 */
package com.friday.cultivation.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.friday.cultivation.client.renderer.BuddhaFireLotusRenderer;
import com.friday.cultivation.registry.ModParticles;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(modid="friday_cultivation", value={Dist.CLIENT})
public final class BuddhaFireLotusClientEffects {
    public static final IGuiOverlay OVERLAY = (gui, graphics, partialTick, screenWidth, screenHeight) -> BuddhaFireLotusClientEffects.renderOverlay(graphics, screenWidth, screenHeight);
    private static final double TWO_PI = Math.PI * 2;
    private static final DustParticleOptions LOTUS_CYAN_FIRE_DUST = new DustParticleOptions(new Vector3f(0.12f, 1.0f, 0.78f), 1.15f);
    private static final DustParticleOptions LOTUS_WHITE_FIRE_DUST = new DustParticleOptions(new Vector3f(0.95f, 0.98f, 1.0f), 1.05f);
    private static final int MAX_ACTIVE_EXPLOSIONS = 8;
    private static final List<LotusExplosionEffect> ACTIVE = new ArrayList<LotusExplosionEffect>();

    private BuddhaFireLotusClientEffects() {
    }

    public static void onExplosion(double x, double y, double z, double radius, int durationTicks, int chargedQi, int rootFlags) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        Vec3 pos = new Vec3(x, y, z);
        double viewRadius = Math.max(radius + 32.0, radius * 1.82 + 24.0);
        if (player.position().distanceToSqr(pos) > viewRadius * viewRadius) {
            return;
        }
        if (ACTIVE.size() >= 8) {
            ACTIVE.remove(0);
        }
        ACTIVE.add(new LotusExplosionEffect(pos, radius, Math.max(1, durationTicks), chargedQi, rootFlags));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (ACTIVE.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Iterator<LotusExplosionEffect> it = ACTIVE.iterator();
        while (it.hasNext()) {
            LotusExplosionEffect effect = it.next();
            if (effect.remainingTicks <= 0) {
                it.remove();
                continue;
            }
            BuddhaFireLotusClientEffects.spawnLotusQiParticles(mc, effect);
            BuddhaFireLotusClientEffects.spawnLotusFireStreams(mc, effect);
            --effect.remainingTicks;
            if (effect.remainingTicks > 0) continue;
            it.remove();
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ACTIVE.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            return;
        }
        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        float age = (float)(mc.level.getGameTime() & 0xFFFFFFL) + event.getPartialTick();
        boolean rendered = false;
        for (LotusExplosionEffect effect : ACTIVE) {
            if (effect.remainingTicks <= 0 || player.position().distanceToSqr(effect.center) > (effect.radius + 96.0) * (effect.radius + 96.0)) continue;
            int elapsedTicks = Math.max(0, effect.totalTicks - effect.remainingTicks);
            float burstProgress = Math.min(1.0f, (float)elapsedTicks / 138.0f);
            float growProgress = Math.min(1.0f, (float)elapsedTicks / 52.0f);
            float bloom = growProgress * growProgress * (3.0f - 2.0f * growProgress);
            float scale = 1.8f + bloom * (float)Math.min(24.0, effect.radius * 0.42);
            float fade = effect.remainingTicks > 46 ? 1.0f : Math.max(0.0f, (float)effect.remainingTicks / 46.0f);
            float alpha = 0.92f * fade;
            pose.pushPose();
            pose.translate(effect.center.x - camPos.x, effect.center.y - camPos.y + 0.18, effect.center.z - camPos.z);
            float shockProgress = Math.min(1.0f, (float)elapsedTicks / 58.0f);
            if (shockProgress < 1.0f) {
                float shockRadius = 2.0f + shockProgress * (float)Math.min(effect.radius * 1.82, 112.0);
                float shockAlpha = (1.0f - shockProgress) * 0.82f;
                BuddhaFireLotusRenderer.renderCyanShockwave(pose, (MultiBufferSource)buffers, shockRadius, 0.28f + shockRadius * 0.018f, shockAlpha);
            }
            BuddhaFireLotusRenderer.renderLotusLightning(pose, (MultiBufferSource)buffers, (float)effect.radius, burstProgress, age, alpha);
            BuddhaFireLotusRenderer.renderLotus(pose, (MultiBufferSource)buffers, effect.rootFlags, effect.chargedQi, age, scale, alpha, age * 0.55f, bloom);
            pose.popPose();
            rendered = true;
        }
        if (rendered) {
            buffers.endBatch();
        }
    }

    private static void spawnLotusQiParticles(Minecraft mc, LotusExplosionEffect effect) {
        ClientLevel level = mc.level;
        LocalPlayer player = mc.player;
        if (level == null || player == null) {
            return;
        }
        if (player.position().distanceToSqr(effect.center) > (effect.radius + 96.0) * (effect.radius + 96.0)) {
            return;
        }
        if ((effect.particleTicker++ & 1) != 0) {
            return;
        }
        float progress = 1.0f - (float)effect.remainingTicks / (float)Math.max(1, effect.totalTicks);
        double orbit = Math.min(36.0, effect.radius * 0.58);
        double height = Math.min(20.0, Math.max(8.0, effect.radius * 0.42));
        int count = progress < 0.1f ? 5 : 10;
        for (int i = 0; i < count; ++i) {
            double angle = level.random.nextDouble() * Math.PI * 2.0;
            double r = Math.sqrt(level.random.nextDouble()) * orbit;
            double x = effect.center.x + Math.cos(angle) * r;
            double z = effect.center.z + Math.sin(angle) * r;
            double y = effect.center.y + 1.4 + level.random.nextDouble() * height;
            double swirl = 0.012 + level.random.nextDouble() * 0.024;
            double lift = 0.01 + level.random.nextDouble() * 0.03;
            SimpleParticleType particle = BuddhaFireLotusClientEffects.lotusQiParticle(i + effect.particleTicker);
            level.addParticle((ParticleOptions)particle, x, y, z, -Math.sin(angle) * swirl, lift, Math.cos(angle) * swirl);
        }
    }

    private static SimpleParticleType lotusQiParticle(int index) {
        return switch (Math.floorMod(index, 3)) {
            case 0 -> (SimpleParticleType)ModParticles.AMBIENT_QI_LOTUS.get();
            case 1 -> (SimpleParticleType)ModParticles.AMBIENT_QI_LIGHTNING.get();
            default -> (SimpleParticleType)ModParticles.AMBIENT_QI_WATER.get();
        };
    }

    private static void spawnLotusFireStreams(Minecraft mc, LotusExplosionEffect effect) {
        float fade;
        ClientLevel level = mc.level;
        LocalPlayer player = mc.player;
        if (level == null || player == null) {
            return;
        }
        if (player.position().distanceToSqr(effect.center) > (effect.radius + 96.0) * (effect.radius + 96.0)) {
            return;
        }
        if ((effect.flameTicker++ & 1) != 0) {
            return;
        }
        int elapsedTicks = Math.max(0, effect.totalTicks - effect.remainingTicks);
        if (elapsedTicks < 8) {
            return;
        }
        float f = fade = effect.remainingTicks > 46 ? 1.0f : Math.max(0.0f, (float)effect.remainingTicks / 46.0f);
        if (fade <= 0.05f) {
            return;
        }
        double lotusScale = 1.8 + Math.min(24.0, effect.radius * 0.42);
        double orbit = Math.max(3.2, lotusScale * 0.82);
        double height = Math.max(3.0, Math.min(20.0, effect.radius * 0.36));
        int count = (int)Math.min(18.0, Math.max(8.0, effect.radius / 5.5));
        if (effect.remainingTicks < 46) {
            count = Math.max(3, count / 2);
        }
        double time = ((double)level.getGameTime() + (double)effect.flameTicker * 0.5) * 0.15;
        for (int i = 0; i < count; ++i) {
            boolean cyan = (i + effect.flameTicker & 1) == 0;
            double direction = cyan ? 1.0 : -1.15;
            double phase = (double)i * (Math.PI * 2) / (double)count + time * direction;
            double localOrbit = orbit * (0.72 + level.random.nextDouble() * 0.55);
            double x = effect.center.x + Math.cos(phase) * localOrbit;
            double z = effect.center.z + Math.sin(phase) * localOrbit;
            double y = effect.center.y + 0.8 + level.random.nextDouble() * height + Math.sin(time * 1.7 + (double)i * 0.91) * height * 0.18;
            Vec3 tangent = new Vec3(-Math.sin(phase), 0.0, Math.cos(phase)).scale(direction);
            Vec3 outward = new Vec3(Math.cos(phase), 0.0, Math.sin(phase));
            double swirl = 0.06 + level.random.nextDouble() * 0.052;
            double spread = 0.02 + level.random.nextDouble() * 0.05;
            Vec3 velocity = tangent.scale(swirl).subtract(outward.scale(spread)).add(0.0, -0.015 + level.random.nextDouble() * 0.095, 0.0);
            level.addParticle((ParticleOptions)(cyan ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.CLOUD), x, y, z, velocity.x, velocity.y, velocity.z);
            level.addParticle((ParticleOptions)(cyan ? LOTUS_CYAN_FIRE_DUST : LOTUS_WHITE_FIRE_DUST), x, y + 0.04, z, velocity.x * 0.62, velocity.y * 0.72, velocity.z * 0.62);
            if (cyan || (i & 3) != 0) continue;
            level.addParticle((ParticleOptions)ParticleTypes.END_ROD, x, y + 0.03, z, velocity.x * 0.34, velocity.y * 0.52, velocity.z * 0.34);
        }
    }

    private static void renderOverlay(GuiGraphics graphics, int screenWidth, int screenHeight) {
        if (ACTIVE.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui) {
            return;
        }
        int grayAlpha = 0;
        int flashAlpha = 0;
        for (LotusExplosionEffect effect : ACTIVE) {
            double dist;
            double influence;
            if (effect.remainingTicks <= 0 || (influence = 1.0 - Math.min(1.0, (dist = player.position().distanceTo(effect.center)) / Math.max(1.0, effect.radius + 24.0))) <= 0.0) continue;
            float progress = 1.0f - (float)effect.remainingTicks / (float)Math.max(1, effect.totalTicks);
            grayAlpha = Math.max(grayAlpha, (int)(150.0 * influence * (double)Math.max(0.0f, 1.0f - progress * 1.8f)));
            float flashCurve = progress < 0.28f ? progress / 0.28f : Math.max(0.0f, 1.0f - (progress - 0.28f) / 0.72f);
            flashAlpha = Math.max(flashAlpha, (int)(220.0 * influence * (double)flashCurve));
        }
        if (grayAlpha > 0) {
            graphics.fill(0, 0, screenWidth, screenHeight, Math.min(180, grayAlpha) << 24 | 0xBFC4CC);
        }
        if (flashAlpha > 0) {
            graphics.fill(0, 0, screenWidth, screenHeight, Math.min(210, flashAlpha) << 24 | 0xFF8A28);
        }
    }

    private static final class LotusExplosionEffect {
        final Vec3 center;
        final double radius;
        final int totalTicks;
        final int chargedQi;
        final int rootFlags;
        int remainingTicks;
        int particleTicker;
        int flameTicker;

        LotusExplosionEffect(Vec3 center, double radius, int durationTicks, int chargedQi, int rootFlags) {
            this.center = center;
            this.radius = radius;
            this.totalTicks = durationTicks;
            this.remainingTicks = durationTicks;
            this.chargedQi = chargedQi;
            this.rootFlags = rootFlags;
        }
    }
}

