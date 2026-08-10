/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 */
package com.friday.cultivation.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.friday.cultivation.client.model.SpiritLockChainModel;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid="friday_cultivation", value={Dist.CLIENT})
public final class SoulHookVisualHandler {
    private static final int BLOOD = 14689582;
    private static final int BLOOD_DARK = 6031120;
    private static final int BLOOD_BLACK = 1312011;
    private static final int BLOOD_LIGHT = 16742274;
    private static final float BIND_CHAIN_SCALE = 1.28f;
    private static final int BIND_TICKS = 200;
    private static final Map<Long, Link> LINKS = new ConcurrentHashMap<Long, Link>();
    private static ResourceKey<Level> currentDimension;

    private SoulHookVisualHandler() {
    }

    public static void onSync(int casterId, int targetId, int durationTicks, int chainCount, boolean vortexPhase, boolean active, int elapsedTicks, boolean hasVortexAnchor, double vortexX, double vortexY, double vortexZ) {
        long key = SoulHookVisualHandler.linkKey(casterId, targetId);
        if (!active) {
            LINKS.remove(key);
            return;
        }
        long now = System.currentTimeMillis();
        LINKS.put(key, new Link(casterId, targetId, now + (long)Math.max(1, durationTicks) * 50L, vortexPhase, Math.max(0, elapsedTicks), now, hasVortexAnchor, vortexX, vortexY, vortexZ));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            SoulHookVisualHandler.clear();
            return;
        }
        ResourceKey dimension = mc.level.dimension();
        if (currentDimension == null) {
            currentDimension = dimension;
        } else if (!currentDimension.equals((Object)dimension)) {
            SoulHookVisualHandler.clear();
            currentDimension = dimension;
            return;
        }
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        long now = System.currentTimeMillis();
        LINKS.entrySet().removeIf(e -> ((Link)e.getValue()).endMs <= now);
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        if (LINKS.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        Vec3 camera = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        float age = (float)mc.level.getGameTime() + event.getPartialTick();
        long now = System.currentTimeMillis();
        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);
        Iterator<Map.Entry<Long, Link>> it = LINKS.entrySet().iterator();
        while (it.hasNext()) {
            Entity caster;
            LivingEntity living;
            Link link = it.next().getValue();
            if (link.endMs <= now) {
                it.remove();
                continue;
            }
            float partial = event.getPartialTick();
            Entity target = SoulHookVisualHandler.resolveEntity(mc, link.targetId);
            LivingEntity targetLiving = target instanceof LivingEntity ? (living = (LivingEntity)target) : null;
            Vec3 targetCenter = targetLiving == null ? null : targetLiving.getPosition(partial).add(0.0, (double)targetLiving.getBbHeight() * 0.52, 0.0);
            Vec3 visualCenter = link.vortexPhase && link.hasVortexAnchor ? new Vec3(link.vortexX, link.vortexY, link.vortexZ) : targetCenter;
            if (visualCenter == null || mc.player.position().distanceToSqr(visualCenter) > 30976.0) continue;
            int elapsedTicks = SoulHookVisualHandler.elapsedTicks(link);
            float alpha = SoulHookVisualHandler.alpha(link.endMs);
            if (link.vortexPhase) {
                SoulHookVisualHandler.renderVortexPhase(pose, (MultiBufferSource)buffers, targetCenter == null ? visualCenter : targetCenter, age, alpha, link, Math.max(0, elapsedTicks - 200));
                continue;
            }
            if (targetLiving == null || !((caster = SoulHookVisualHandler.resolveEntity(mc, link.casterId)) instanceof LivingEntity)) continue;
            LivingEntity cl = (LivingEntity)caster;
            SoulHookVisualHandler.renderBindChain(pose, (MultiBufferSource)buffers, cl, targetLiving, targetCenter, partial, age, alpha);
        }
        pose.popPose();
        buffers.endBatch(SpiritLockChainModel.renderType());
    }

    private static Entity resolveEntity(Minecraft mc, int entityId) {
        if (mc.player != null && mc.player.getId() == entityId) {
            return mc.player;
        }
        return mc.level == null ? null : mc.level.getEntity(entityId);
    }

    private static void renderBindChain(PoseStack pose, MultiBufferSource buffers, LivingEntity caster, LivingEntity target, Vec3 targetCenter, float partial, float age, float alpha) {
        Vec3 start = caster.getEyePosition(partial).add(caster.getLookAngle().scale(0.7)).add(0.0, -0.18, 0.0);
        Vec3 end = targetCenter.add(0.0, Math.sin((double)((float)target.tickCount + partial) * 0.11) * 0.035, 0.0);
        SpiritLockChainModel.renderChainBetween(pose, buffers, start, end, age, alpha, 14689582, 6031120, 16742274, 1.28f);
    }

    private static void renderVortexPhase(PoseStack pose, MultiBufferSource buffers, Vec3 targetCenter, float age, float alpha, Link link, int finishTicks) {
        Vec3 center = link.hasVortexAnchor ? new Vec3(link.vortexX, link.vortexY, link.vortexZ) : targetCenter.add(0.0, 3.15, 0.0);
        double pulse = Math.sin((double)(age + (float)finishTicks) * 0.13) * 0.06;
        SoulHookVisualHandler.renderGradientVortex(pose, buffers, center, 1.88 + pulse, age, alpha, finishTicks);
    }

    private static void renderGradientVortex(PoseStack pose, MultiBufferSource buffers, Vec3 center, double radius, float age, float alpha, int finishTicks) {
        RenderSystem.enableBlend();
        VertexConsumer vc = buffers.getBuffer(SpiritLockChainModel.renderType());
        Matrix4f matrix = pose.last().pose();
        Matrix3f normal = pose.last().normal();
        int rings = 7;
        int segments = 64;
        double spin = (double)age * 0.072 + (double)finishTicks * 0.018;
        for (int ring = 0; ring < rings; ++ring) {
            double innerT = (double)ring / (double)rings;
            double outerT = (double)(ring + 1) / (double)rings;
            double innerRadius = radius * innerT;
            double outerRadius = radius * outerT;
            for (int segment = 0; segment < segments; ++segment) {
                double base0 = (float)segment * ((float)Math.PI * 2) / (float)segments;
                double base1 = (float)(segment + 1) * ((float)Math.PI * 2) / (float)segments;
                double angle00 = base0 + SoulHookVisualHandler.swirlOffset(innerT, base0, spin, age);
                double angle01 = base1 + SoulHookVisualHandler.swirlOffset(innerT, base1, spin, age);
                double angle10 = base0 + SoulHookVisualHandler.swirlOffset(outerT, base0, spin, age);
                double angle11 = base1 + SoulHookVisualHandler.swirlOffset(outerT, base1, spin, age);
                Vec3 p00 = SoulHookVisualHandler.vortexPoint(center, innerRadius, angle00);
                Vec3 p01 = SoulHookVisualHandler.vortexPoint(center, innerRadius, angle01);
                Vec3 p10 = SoulHookVisualHandler.vortexPoint(center, outerRadius, angle10);
                Vec3 p11 = SoulHookVisualHandler.vortexPoint(center, outerRadius, angle11);
                int c00 = SoulHookVisualHandler.vortexColor(innerT, base0, age);
                int c01 = SoulHookVisualHandler.vortexColor(innerT, base1, age);
                int c10 = SoulHookVisualHandler.vortexColor(outerT, base0, age);
                int c11 = SoulHookVisualHandler.vortexColor(outerT, base1, age);
                int a00 = SoulHookVisualHandler.vortexAlpha(alpha, innerT, base0, age);
                int a01 = SoulHookVisualHandler.vortexAlpha(alpha, innerT, base1, age);
                int a10 = SoulHookVisualHandler.vortexAlpha(alpha, outerT, base0, age);
                int a11 = SoulHookVisualHandler.vortexAlpha(alpha, outerT, base1, age);
                SoulHookVisualHandler.vortexQuad(vc, matrix, normal, p00, p10, p11, p01, c00, c10, c11, c01, a00, a10, a11, a01, 0.0f, 1.0f, 0.0f);
                SoulHookVisualHandler.vortexQuad(vc, matrix, normal, p01, p11, p10, p00, c01, c11, c10, c00, a01, a11, a10, a00, 0.0f, -1.0f, 0.0f);
            }
        }
        SoulHookVisualHandler.renderVortexEdge(pose, buffers, center, radius, age, alpha);
    }

    private static void renderVortexEdge(PoseStack pose, MultiBufferSource buffers, Vec3 center, double radius, float age, float alpha) {
        VertexConsumer vc = buffers.getBuffer(SpiritLockChainModel.renderType());
        Matrix4f matrix = pose.last().pose();
        Matrix3f normal = pose.last().normal();
        int segments = 64;
        double spin = (double)age * 0.088;
        double inner = radius * 0.88;
        double outer = radius * 1.04;
        double lower = 0.32 + Math.sin((double)age * 0.11) * 0.05;
        for (int segment = 0; segment < segments; ++segment) {
            double a0 = (double)((float)segment * ((float)Math.PI * 2) / (float)segments) + spin;
            double a1 = (double)((float)(segment + 1) * ((float)Math.PI * 2) / (float)segments) + spin;
            double twist0 = Math.sin((double)segment * 0.7 + (double)age * 0.06) * 0.055;
            double twist1 = Math.sin((double)(segment + 1) * 0.7 + (double)age * 0.06) * 0.055;
            Vec3 top0 = SoulHookVisualHandler.vortexPoint(center, outer, a0);
            Vec3 top1 = SoulHookVisualHandler.vortexPoint(center, outer, a1);
            Vec3 bottom1 = SoulHookVisualHandler.vortexPoint(center.add(0.0, -lower, 0.0), inner, a1 + twist1);
            Vec3 bottom0 = SoulHookVisualHandler.vortexPoint(center.add(0.0, -lower, 0.0), inner, a0 + twist0);
            double wave = (Math.sin((double)segment * 0.9 - (double)age * 0.14) + 1.0) * 0.5;
            int edgeColor = SoulHookVisualHandler.lerpColor(6031120, 14689582, 0.42 + wave * 0.36);
            int edgeAlpha = Mth.clamp((int)((int)(255.0 * (double)alpha * (0.42 + wave * 0.24))), (int)58, (int)210);
            SoulHookVisualHandler.vortexQuad(vc, matrix, normal, top0, top1, bottom1, bottom0, edgeColor, edgeColor, 1312011, 1312011, edgeAlpha, edgeAlpha, (int)((float)edgeAlpha * 0.46f), (int)((float)edgeAlpha * 0.46f), 0.0f, 0.0f, 1.0f);
        }
    }

    private static double swirlOffset(double radial, double baseAngle, double spin, float age) {
        return spin + (1.0 - radial) * 1.85 + Math.sin(baseAngle * 3.0 + (double)age * 0.045) * 0.12;
    }

    private static Vec3 vortexPoint(Vec3 center, double radius, double angle) {
        return center.add(Math.cos(angle) * radius, 0.0, Math.sin(angle) * radius);
    }

    private static int vortexColor(double radial, double angle, float age) {
        double wave = (Math.sin(angle * 4.0 - (double)age * 0.13) + 1.0) * 0.5;
        if (radial < 0.16) {
            return SoulHookVisualHandler.lerpColor(1312011, 6031120, 0.35 + wave * 0.25);
        }
        if (radial < 0.62) {
            double t = Mth.clamp((double)((radial - 0.16) / 0.46 * 0.78 + wave * 0.22), (double)0.0, (double)1.0);
            return SoulHookVisualHandler.lerpColor(6031120, 14689582, t);
        }
        double edge = Mth.clamp((double)((radial - 0.62) / 0.38), (double)0.0, (double)1.0);
        int redToBlack = SoulHookVisualHandler.lerpColor(14689582, 1312011, edge);
        return SoulHookVisualHandler.lerpColor(redToBlack, 16742274, wave * 0.12 * (1.0 - edge));
    }

    private static int vortexAlpha(float alpha, double radial, double angle, float age) {
        double edgeFade = Mth.lerp((double)Mth.clamp((double)((radial - 0.84) / 0.16), (double)0.0, (double)1.0), (double)1.0, (double)0.28);
        double pulse = 0.86 + 0.14 * Math.sin(angle * 5.0 + (double)age * 0.09);
        return Mth.clamp((int)((int)(255.0 * (double)alpha * edgeFade * pulse)), (int)0, (int)225);
    }

    private static int lerpColor(int from, int to, double t) {
        double clamped = Mth.clamp((double)t, (double)0.0, (double)1.0);
        int r = (int)Mth.lerp((double)clamped, (double)(from >> 16 & 0xFF), (double)(to >> 16 & 0xFF));
        int g = (int)Mth.lerp((double)clamped, (double)(from >> 8 & 0xFF), (double)(to >> 8 & 0xFF));
        int b = (int)Mth.lerp((double)clamped, (double)(from & 0xFF), (double)(to & 0xFF));
        return r << 16 | g << 8 | b;
    }

    private static void vortexQuad(VertexConsumer vc, Matrix4f matrix, Matrix3f normal, Vec3 p1, Vec3 p2, Vec3 p3, Vec3 p4, int c1, int c2, int c3, int c4, int a1, int a2, int a3, int a4, float nx, float ny, float nz) {
        SoulHookVisualHandler.vortexVertex(vc, matrix, normal, p1, c1, a1, 0.0f, 1.0f, nx, ny, nz);
        SoulHookVisualHandler.vortexVertex(vc, matrix, normal, p2, c2, a2, 1.0f, 1.0f, nx, ny, nz);
        SoulHookVisualHandler.vortexVertex(vc, matrix, normal, p3, c3, a3, 1.0f, 0.0f, nx, ny, nz);
        SoulHookVisualHandler.vortexVertex(vc, matrix, normal, p4, c4, a4, 0.0f, 0.0f, nx, ny, nz);
    }

    private static void vortexVertex(VertexConsumer vc, Matrix4f matrix, Matrix3f normal, Vec3 p, int rgb, int alpha, float u, float v, float nx, float ny, float nz) {
        vc.vertex(matrix, (float)p.x, (float)p.y, (float)p.z).color(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF, alpha).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0, 0xF000F0).normal(normal, nx, ny, nz).endVertex();
    }

    private static int elapsedTicks(Link link) {
        long ms = Math.max(0L, System.currentTimeMillis() - link.syncMs);
        return link.elapsedTicksAtSync + (int)(ms / 50L);
    }

    private static float alpha(long endMs) {
        long remaining = endMs - System.currentTimeMillis();
        if (remaining <= 0L) {
            return 0.0f;
        }
        if (remaining < 280L) {
            return Math.max(0.0f, (float)remaining / 280.0f) * 0.94f;
        }
        return 0.94f;
    }

    private static long linkKey(int casterId, int targetId) {
        return (long)casterId << 32 ^ (long)targetId & 0xFFFFFFFFL;
    }

    private static void clear() {
        LINKS.clear();
        currentDimension = null;
    }

    private record Link(int casterId, int targetId, long endMs, boolean vortexPhase, int elapsedTicksAtSync, long syncMs, boolean hasVortexAnchor, double vortexX, double vortexY, double vortexZ) {
    }
}

