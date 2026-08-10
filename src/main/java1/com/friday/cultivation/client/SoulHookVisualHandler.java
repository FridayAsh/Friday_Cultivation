package com.friday.cultivation.client;

import com.friday.cultivation.client.model.SpiritLockChainModel;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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

/**
 * 勾魂视觉效果处理器 — 完整复刻原模组 SoulHookVisualHandler。
 * 接收 SoulHookVisualPacket 同步的勾魂状态，在客户端渲染：
 * - 绑定阶段：金色锁链连接施法者与目标
 * - 漩涡阶段：血色漩涡（7环64段渐变四边形+边缘扭曲带）
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation", value = Dist.CLIENT)
public final class SoulHookVisualHandler {
    private static final int BLOOD = 14689582;
    private static final int BLOOD_DARK = 6031120;
    private static final int BLOOD_BLACK = 1312011;
    private static final int BLOOD_LIGHT = 16742274;
    private static final float BIND_CHAIN_SCALE = 1.28f;
    private static final int BIND_TICKS = 200;
    private static final Map<Long, Link> LINKS = new ConcurrentHashMap<>();
    private static ResourceKey<Level> currentDimension;

    private SoulHookVisualHandler() {
    }

    public static void onSync(int casterId, int targetId, int durationTicks, int chainCount, boolean vortexPhase, boolean active, int elapsedTicks, boolean hasVortexAnchor, double vortexX, double vortexY, double vortexZ) {
        long key = linkKey(casterId, targetId);
        if (!active) {
            LINKS.remove(key);
            return;
        }
        long now = System.currentTimeMillis();
        LINKS.put(key, new Link(casterId, targetId, now + (long) Math.max(1, durationTicks) * 50L, vortexPhase, Math.max(0, elapsedTicks), now, hasVortexAnchor, vortexX, vortexY, vortexZ));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            clear();
            return;
        }
        ResourceKey<Level> dimension = mc.level.dimension();
        if (currentDimension == null) {
            currentDimension = dimension;
        } else if (!currentDimension.equals(dimension)) {
            clear();
            currentDimension = dimension;
            return;
        }
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        long now = System.currentTimeMillis();
        LINKS.entrySet().removeIf(e -> e.getValue().endMs <= now);
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
        float age = (float) mc.level.getGameTime() + event.getPartialTick();
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
            Entity target = resolveEntity(mc, link.targetId);
            LivingEntity targetLiving = target instanceof LivingEntity ? (living = (LivingEntity) target) : null;
            Vec3 targetCenter = targetLiving == null ? null : targetLiving.getPosition(partial).add(0.0, (double) targetLiving.getBbHeight() * 0.52, 0.0);
            Vec3 visualCenter = link.vortexPhase && link.hasVortexAnchor ? new Vec3(link.vortexX, link.vortexY, link.vortexZ) : targetCenter;
            if (visualCenter == null || mc.player.position().distanceToSqr(visualCenter) > 30976.0) continue;
            int elapsedTicks = elapsedTicks(link);
            float alpha = alpha(link.endMs);
            if (link.vortexPhase) {
                renderVortexPhase(pose, buffers, targetCenter == null ? visualCenter : targetCenter, age, alpha, link, Math.max(0, elapsedTicks - 200));
                continue;
            }
            if (targetLiving == null || !((caster = resolveEntity(mc, link.casterId)) instanceof LivingEntity)) continue;
            LivingEntity cl = (LivingEntity) caster;
            renderBindChain(pose, buffers, cl, targetLiving, targetCenter, partial, age, alpha);
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
        Vec3 end = targetCenter.add(0.0, Math.sin((double) ((float) target.tickCount + partial) * 0.11) * 0.035, 0.0);
        SpiritLockChainModel.renderChainBetween(pose, buffers, start, end, age, alpha, BLOOD, BLOOD_DARK, BLOOD_LIGHT, BIND_CHAIN_SCALE);
    }

    private static void renderVortexPhase(PoseStack pose, MultiBufferSource buffers, Vec3 targetCenter, float age, float alpha, Link link, int finishTicks) {
        Vec3 center = link.hasVortexAnchor ? new Vec3(link.vortexX, link.vortexY, link.vortexZ) : targetCenter.add(0.0, 3.15, 0.0);
        double pulse = Math.sin((double) (age + (float) finishTicks) * 0.13) * 0.06;
        renderGradientVortex(pose, buffers, center, 1.88 + pulse, age, alpha, finishTicks);
    }

    private static void renderGradientVortex(PoseStack pose, MultiBufferSource buffers, Vec3 center, double radius, float age, float alpha, int finishTicks) {
        RenderSystem.enableBlend();
        VertexConsumer vc = buffers.getBuffer(SpiritLockChainModel.renderType());
        Matrix4f matrix = pose.last().pose();
        Matrix3f normal = pose.last().normal();
        int rings = 7;
        int segments = 64;
        double spin = (double) age * 0.072 + (double) finishTicks * 0.018;
        for (int ring = 0; ring < rings; ++ring) {
            double innerT = (double) ring / (double) rings;
            double outerT = (double) (ring + 1) / (double) rings;
            double innerRadius = radius * innerT;
            double outerRadius = radius * outerT;
            for (int segment = 0; segment < segments; ++segment) {
                double base0 = (float) segment * ((float) Math.PI * 2) / (float) segments;
                double base1 = (float) (segment + 1) * ((float) Math.PI * 2) / (float) segments;
                double angle00 = base0 + swirlOffset(innerT, base0, spin, age);
                double angle01 = base1 + swirlOffset(innerT, base1, spin, age);
                double angle10 = base0 + swirlOffset(outerT, base0, spin, age);
                double angle11 = base1 + swirlOffset(outerT, base1, spin, age);
                Vec3 p00 = vortexPoint(center, innerRadius, angle00);
                Vec3 p01 = vortexPoint(center, innerRadius, angle01);
                Vec3 p10 = vortexPoint(center, outerRadius, angle10);
                Vec3 p11 = vortexPoint(center, outerRadius, angle11);
                int c00 = vortexColor(innerT, base0, age);
                int c01 = vortexColor(innerT, base1, age);
                int c10 = vortexColor(outerT, base0, age);
                int c11 = vortexColor(outerT, base1, age);
                int a00 = vortexAlpha(alpha, innerT, base0, age);
                int a01 = vortexAlpha(alpha, innerT, base1, age);
                int a10 = vortexAlpha(alpha, outerT, base0, age);
                int a11 = vortexAlpha(alpha, outerT, base1, age);
                vortexQuad(vc, matrix, normal, p00, p10, p11, p01, c00, c10, c11, c01, a00, a10, a11, a01, 0.0f, 1.0f, 0.0f);
                vortexQuad(vc, matrix, normal, p01, p11, p10, p00, c01, c11, c10, c00, a01, a11, a10, a00, 0.0f, -1.0f, 0.0f);
            }
        }
        renderVortexEdge(pose, buffers, center, radius, age, alpha);
    }

    private static void renderVortexEdge(PoseStack pose, MultiBufferSource buffers, Vec3 center, double radius, float age, float alpha) {
        VertexConsumer vc = buffers.getBuffer(SpiritLockChainModel.renderType());
        Matrix4f matrix = pose.last().pose();
        Matrix3f normal = pose.last().normal();
        int segments = 64;
        double spin = (double) age * 0.088;
        double inner = radius * 0.88;
        double outer = radius * 1.04;
        double lower = 0.32 + Math.sin((double) age * 0.11) * 0.05;
        for (int segment = 0; segment < segments; ++segment) {
            double a0 = (double) ((float) segment * ((float) Math.PI * 2) / (float) segments) + spin;
            double a1 = (double) ((float) (segment + 1) * ((float) Math.PI * 2) / (float) segments) + spin;
            double twist0 = Math.sin((double) segment * 0.7 + (double) age * 0.06) * 0.055;
            double twist1 = Math.sin((double) (segment + 1) * 0.7 + (double) age * 0.06) * 0.055;
            Vec3 top0 = vortexPoint(center, outer, a0);
            Vec3 top1 = vortexPoint(center, outer, a1);
            Vec3 bottom1 = vortexPoint(center.add(0.0, -lower, 0.0), inner, a1 + twist1);
            Vec3 bottom0 = vortexPoint(center.add(0.0, -lower, 0.0), inner, a0 + twist0);
            double wave = (Math.sin((double) segment * 0.9 - (double) age * 0.14) + 1.0) * 0.5;
            int edgeColor = lerpColor(BLOOD_DARK, BLOOD, 0.42 + wave * 0.36);
            int edgeAlpha = Mth.clamp((int) (255.0 * (double) alpha * (0.42 + wave * 0.24)), 58, 210);
            vortexQuad(vc, matrix, normal, top0, top1, bottom1, bottom0, edgeColor, edgeColor, BLOOD_BLACK, BLOOD_BLACK, edgeAlpha, edgeAlpha, (int) ((float) edgeAlpha * 0.46f), (int) ((float) edgeAlpha * 0.46f), 0.0f, 0.0f, 1.0f);
        }
    }

    private static double swirlOffset(double radial, double baseAngle, double spin, float age) {
        return spin + (1.0 - radial) * 1.85 + Math.sin(baseAngle * 3.0 + (double) age * 0.045) * 0.12;
    }

    private static Vec3 vortexPoint(Vec3 center, double radius, double angle) {
        return center.add(Math.cos(angle) * radius, 0.0, Math.sin(angle) * radius);
    }

    private static int vortexColor(double radial, double angle, float age) {
        double wave = (Math.sin(angle * 4.0 - (double) age * 0.13) + 1.0) * 0.5;
        if (radial < 0.16) {
            return lerpColor(BLOOD_BLACK, BLOOD_DARK, 0.35 + wave * 0.25);
        }
        if (radial < 0.62) {
            double t = Mth.clamp((radial - 0.16) / 0.46 * 0.78 + wave * 0.22, 0.0, 1.0);
            return lerpColor(BLOOD_DARK, BLOOD, t);
        }
        double edge = Mth.clamp((radial - 0.62) / 0.38, 0.0, 1.0);
        int redToBlack = lerpColor(BLOOD, BLOOD_BLACK, edge);
        return lerpColor(redToBlack, BLOOD_LIGHT, wave * 0.12 * (1.0 - edge));
    }

    private static int vortexAlpha(float alpha, double radial, double angle, float age) {
        double edgeFade = Mth.lerp(Mth.clamp((radial - 0.84) / 0.16, 0.0, 1.0), 1.0, 0.28);
        double pulse = 0.86 + 0.14 * Math.sin(angle * 5.0 + (double) age * 0.09);
        return Mth.clamp((int) (255.0 * (double) alpha * edgeFade * pulse), 0, 225);
    }

    private static int lerpColor(int from, int to, double t) {
        double clamped = Mth.clamp(t, 0.0, 1.0);
        int r = (int) Mth.lerp(clamped, from >> 16 & 0xFF, to >> 16 & 0xFF);
        int g = (int) Mth.lerp(clamped, from >> 8 & 0xFF, to >> 8 & 0xFF);
        int b = (int) Mth.lerp(clamped, from & 0xFF, to & 0xFF);
        return r << 16 | g << 8 | b;
    }

    private static void vortexQuad(VertexConsumer vc, Matrix4f matrix, Matrix3f normal, Vec3 p1, Vec3 p2, Vec3 p3, Vec3 p4, int c1, int c2, int c3, int c4, int a1, int a2, int a3, int a4, float nx, float ny, float nz) {
        vortexVertex(vc, matrix, normal, p1, c1, a1, 0.0f, 1.0f, nx, ny, nz);
        vortexVertex(vc, matrix, normal, p2, c2, a2, 1.0f, 1.0f, nx, ny, nz);
        vortexVertex(vc, matrix, normal, p3, c3, a3, 1.0f, 0.0f, nx, ny, nz);
        vortexVertex(vc, matrix, normal, p4, c4, a4, 0.0f, 0.0f, nx, ny, nz);
    }

    private static void vortexVertex(VertexConsumer vc, Matrix4f matrix, Matrix3f normal, Vec3 p, int rgb, int alpha, float u, float v, float nx, float ny, float nz) {
        vc.vertex(matrix, (float) p.x, (float) p.y, (float) p.z).color(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF, alpha).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(normal, nx, ny, nz).endVertex();
    }

    private static int elapsedTicks(Link link) {
        long ms = Math.max(0L, System.currentTimeMillis() - link.syncMs);
        return link.elapsedTicksAtSync + (int) (ms / 50L);
    }

    private static float alpha(long endMs) {
        long remaining = endMs - System.currentTimeMillis();
        if (remaining <= 0L) {
            return 0.0f;
        }
        if (remaining < 280L) {
            return Math.max(0.0f, (float) remaining / 280.0f) * 0.94f;
        }
        return 0.94f;
    }

    private static long linkKey(int casterId, int targetId) {
        return (long) casterId << 32 ^ (long) targetId & 0xFFFFFFFFL;
    }

    private static void clear() {
        LINKS.clear();
        currentDimension = null;
    }

    private record Link(int casterId, int targetId, long endMs, boolean vortexPhase, int elapsedTicksAtSync, long syncMs, boolean hasVortexAnchor, double vortexX, double vortexY, double vortexZ) {
    }
}
