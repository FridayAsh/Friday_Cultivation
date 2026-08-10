package com.friday.cultivation.client.model;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * 掌雷（掌心雷）渲染模型 — 严格复刻自原模组 com.xiaoxiang.cultivation.client.model.PalmThunderModel
 * <p>
 * 3 个主要 API：
 * <ul>
 *   <li>{@link #renderOrb} 球状掌心雷（电光外壳）</li>
 *   <li>{@link #renderBurst} 爆发（向外辐射闪电链）</li>
 *   <li>{@link #renderWrappedEntity} 缠绕生物（5 链 × 9 段螺旋）</li>
 * </ul>
 * <p>
 * 私有：emitCore（球壳层）/ renderRing（光环）/ renderOrbLightning（球面闪电）/
 * renderBurstBolts（辐射闪电）/ emitSurfaceBolt / emitRadialBolt /
 * emitLightningSegment（3 层叠色）/ ringPoint / spherePoint / directionFromSeed /
 * perpendicular / pointXZ / emitSegment / triangle / quad / vertex /
 * alphaToByte / positiveModulo / hash01 / signedHash
 */
public final class PalmThunderModel {
    private static final ResourceLocation WHITE_TEXTURE = new ResourceLocation("textures/misc/white.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(WHITE_TEXTURE);
    private static final int WHITE = 0xF8FFFF;
    private static final int PALE = 15063039;
    private static final int VIOLET = 10314751;
    private static final int DEEP = 4202874;

    private PalmThunderModel() {
    }

    public static RenderType renderType() {
        return RENDER_TYPE;
    }

    public static void renderOrb(PoseStack pose, MultiBufferSource buffers, float ageTicks, float radius, float alpha) {
        RenderSystem.enableBlend();
        VertexConsumer vc = buffers.getBuffer(RENDER_TYPE);
        Matrix4f matrix = pose.last().pose();
        Matrix3f normal = pose.last().normal();
        int a = alphaToByte(alpha);
        if (a <= 0) {
            return;
        }
        float pulse = 0.92f + Mth.sin((float) (ageTicks * 0.18f)) * 0.08f;
        float r = radius * pulse;
        emitCore(vc, matrix, normal, r * 0.74f, Math.min(255, a + 10));
        emitCore(vc, matrix, normal, r * 0.96f, (int) ((float) a * 0.24f));
        renderOrbLightning(vc, matrix, normal, r * 1.12f, ageTicks, a);
    }

    public static void renderBurst(PoseStack pose, MultiBufferSource buffers, float ageTicks, float radius, float progress, float alpha) {
        RenderSystem.enableBlend();
        VertexConsumer vc = buffers.getBuffer(RENDER_TYPE);
        Matrix4f matrix = pose.last().pose();
        Matrix3f normal = pose.last().normal();
        float p = Mth.clamp(progress, 0.0f, 1.0f);
        int a = alphaToByte(alpha * (1.0f - p * 0.55f));
        if (a <= 0) {
            return;
        }
        emitCore(vc, matrix, normal, radius * (0.16f + (1.0f - p) * 0.18f), (int) ((float) a * (1.0f - p) * 0.34f));
        renderBurstBolts(vc, matrix, normal, radius, p, ageTicks, a);
    }

    public static void renderWrappedEntity(PoseStack pose, MultiBufferSource buffers, float width, float height, float ageTicks, float alpha) {
        RenderSystem.enableBlend();
        VertexConsumer vc = buffers.getBuffer(RENDER_TYPE);
        Matrix4f matrix = pose.last().pose();
        Matrix3f normal = pose.last().normal();
        int a = alphaToByte(alpha);
        if (a <= 0) {
            return;
        }
        float radius = Math.max(0.34f, width * 0.78f);
        int strands = 5;
        for (int strand = 0; strand < strands; ++strand) {
            float phase = (float) strand * ((float) Math.PI * 2) / (float) strands + ageTicks * (0.055f + (float) strand * 0.006f);
            int color = strand % 3 == 0 ? WHITE : (strand % 3 == 1 ? PALE : VIOLET);
            Vec3 previous = null;
            int segments = 9;
            for (int i = 0; i <= segments; ++i) {
                float t = (float) i / (float) segments;
                float angle = phase + t * ((float) Math.PI * 2) * 1.18f;
                float y = 0.12f + t * (height - 0.12f) + Mth.sin((float) (ageTicks * 0.11f + (float) strand + (float) i)) * 0.045f;
                float rr = radius * (0.88f + 0.14f * Mth.sin((float) (ageTicks * 0.17f + (float) i * 1.7f)));
                Vec3 next = new Vec3((double) (Mth.cos((float) angle) * rr), (double) y, (double) (Mth.sin((float) angle) * rr));
                if (previous != null && (i + strand & 1) == 1) {
                    emitSegment(vc, matrix, normal, previous, next, 0.028f, color, (int) ((float) a * 0.82f));
                    emitSegment(vc, matrix, normal, previous, next, 0.07f, DEEP, (int) ((float) a * 0.2f));
                }
                previous = next;
            }
        }
    }

    private static void emitCore(VertexConsumer vc, Matrix4f matrix, Matrix3f normal, float radius, int alpha) {
        int a = Mth.clamp(alpha, 0, 255);
        int latBands = 10;
        int lonBands = 18;
        for (int lat = 0; lat < latBands; ++lat) {
            float t0 = (float) lat / (float) latBands;
            float t1 = (float) (lat + 1) / (float) latBands;
            float theta0 = -1.5707964f + t0 * (float) Math.PI;
            float theta1 = -1.5707964f + t1 * (float) Math.PI;
            float y0 = Mth.sin(theta0) * radius;
            float y1 = Mth.sin(theta1) * radius;
            float r0 = Mth.cos(theta0) * radius;
            float r1 = Mth.cos(theta1) * radius;
            float band = 1.0f - Math.abs((t0 + t1) * 0.5f - 0.5f) * 1.35f;
            int bandAlpha = (int) ((float) a * Mth.clamp(0.42f + band * 0.36f, 0.32f, 0.78f));
            int color = lat < latBands / 3 ? VIOLET : (lat > latBands * 2 / 3 ? WHITE : PALE);
            for (int lon = 0; lon < lonBands; ++lon) {
                float p0 = (float) lon * ((float) Math.PI * 2) / (float) lonBands;
                float p1 = (float) (lon + 1) * ((float) Math.PI * 2) / (float) lonBands;
                Vec3 a0 = pointXZ(p0, r0, y0);
                Vec3 a1 = pointXZ(p1, r0, y0);
                Vec3 b1 = pointXZ(p1, r1, y1);
                Vec3 b0 = pointXZ(p0, r1, y1);
                quad(vc, matrix, normal, a0, a1, b1, b0, color, bandAlpha);
            }
        }
    }

    private static void renderRing(VertexConsumer vc, Matrix4f matrix, Matrix3f normal, float radius, int plane, float phase, int color, int alpha) {
        if (alpha <= 0) {
            return;
        }
        int segments = 40;
        for (int i = 0; i < segments; ++i) {
            if ((i + plane) % 7 == 0) continue;
            float a0 = phase + (float) i * ((float) Math.PI * 2) / (float) segments;
            float a1 = phase + (float) (i + 1) * ((float) Math.PI * 2) / (float) segments;
            Vec3 p0 = ringPoint(a0, radius, plane);
            Vec3 p1 = ringPoint(a1, radius, plane);
            emitSegment(vc, matrix, normal, p0, p1, Math.max(0.012f, radius * 0.03f), color, alpha);
        }
    }

    private static void renderOrbLightning(VertexConsumer vc, Matrix4f matrix, Matrix3f normal, float radius, float ageTicks, int alpha) {
        int bolts = 10;
        for (int i = 0; i < bolts; ++i) {
            float shiftedAge = ageTicks + (float) i * 2.13f;
            float period = 7.0f;
            float tick = positiveModulo(shiftedAge, period);
            if (tick > 2.6f) continue;
            float visible = 1.0f - tick / 2.6f;
            int seed = 34127 + i * 7919 + (int) Math.floor(shiftedAge / period) * 19349663;
            Vec3 start = directionFromSeed(seed);
            Vec3 end = directionFromSeed(seed + 97);
            if (start.distanceToSqr(end) > 0.72) {
                end = directionFromSeed(seed + 251);
            }
            emitSurfaceBolt(vc, matrix, normal, start, end, radius,
                    radius * (0.07f + hash01(seed + 5) * 0.045f),
                    radius * 0.014f, seed,
                    (int) ((float) alpha * visible * 0.95f),
                    (i & 1) == 0 ? WHITE : PALE);
        }
    }

    private static void renderBurstBolts(VertexConsumer vc, Matrix4f matrix, Matrix3f normal, float radius, float progress, float ageTicks, int alpha) {
        int bolts = 18;
        int frame = (int) Math.floor(ageTicks / 3.0f);
        for (int i = 0; i < bolts; ++i) {
            int seed = 91873 + i * 197 + frame * 65537;
            Vec3 direction = directionFromSeed(seed);
            float lengthFactor = 0.72f + hash01(seed + 11) * 0.45f;
            float start = radius * (0.04f + progress * 0.12f);
            float end = radius * (0.36f + progress * 1.1f) * lengthFactor;
            float jitter = radius * (0.075f + hash01(seed + 17) * 0.08f);
            int color = i % 3 == 0 ? WHITE : (i % 3 == 1 ? PALE : VIOLET);
            int boltAlpha = (int) ((float) alpha * (0.78f + hash01(seed + 31) * 0.22f));
            emitRadialBolt(vc, matrix, normal, direction, start, end, jitter,
                    radius * 0.018f, seed, boltAlpha, color);
        }
    }

    private static void emitSurfaceBolt(VertexConsumer vc, Matrix4f matrix, Matrix3f normal, Vec3 startDir, Vec3 endDir, float radius, float jitter, float thickness, int seed, int alpha, int color) {
        int segments = 6;
        Vec3 previous = startDir.normalize().scale(radius);
        for (int s = 1; s <= segments; ++s) {
            float t = (float) s / (float) segments;
            Vec3 dir = startDir.scale(1.0 - (double) t).add(endDir.scale(t)).normalize();
            Vec3 sideA = perpendicular(dir);
            Vec3 sideB = dir.cross(sideA).normalize();
            double jA = signedHash(seed + s * 37) * jitter;
            double jB = (double) (signedHash(seed + s * 41) * jitter) * 0.72;
            Vec3 next = dir.scale((double) (radius * (0.94f + hash01(seed + s * 43) * 0.15f)))
                    .add(sideA.scale(jA))
                    .add(sideB.scale(jB));
            float taper = 1.0f - t * 0.38f;
            emitLightningSegment(vc, matrix, normal, previous, next, thickness * taper, color, alpha);
            if ((s == 2 || s == 4) && hash01(seed + s * 83) > 0.28f) {
                Vec3 branchDir = dir.add(sideA.scale((double) signedHash(seed + s * 97) * 0.42))
                        .add(sideB.scale((double) signedHash(seed + s * 101) * 0.42))
                        .normalize();
                Vec3 branchEnd = next.add(branchDir.scale((double) (radius * (0.16f + hash01(seed + s * 107) * 0.16f))));
                emitLightningSegment(vc, matrix, normal, next, branchEnd, thickness * 0.62f, WHITE, (int) ((float) alpha * 0.58f));
            }
            previous = next;
        }
    }

    private static void emitRadialBolt(VertexConsumer vc, Matrix4f matrix, Matrix3f normal, Vec3 direction, float startDistance, float endDistance, float jitter, float thickness, int seed, int alpha, int color) {
        Vec3 dir = direction.normalize();
        Vec3 sideA = perpendicular(dir);
        Vec3 sideB = dir.cross(sideA).normalize();
        int segments = 7;
        Vec3 previous = dir.scale(startDistance);
        for (int s = 1; s <= segments; ++s) {
            float t = (float) s / (float) segments;
            float distance = Mth.lerp(t, startDistance, endDistance);
            double jA = (double) (signedHash(seed + s * 53) * jitter) * (0.35 + (double) t);
            double jB = (double) (signedHash(seed + s * 59) * jitter) * (0.28 + (double) t * 0.72);
            Vec3 next = dir.scale(distance)
                    .add(sideA.scale(jA))
                    .add(sideB.scale(jB));
            float taper = 1.0f - t * 0.55f;
            emitLightningSegment(vc, matrix, normal, previous, next, thickness * taper, color, alpha);
            if ((s == 3 || s == 5) && hash01(seed + s * 109) > 0.18f) {
                Vec3 branchDir = dir.add(sideA.scale((double) signedHash(seed + s * 113) * 0.55))
                        .add(sideB.scale((double) signedHash(seed + s * 127) * 0.55))
                        .normalize();
                Vec3 branchEnd = next.add(branchDir.scale((double) ((endDistance - startDistance) * (0.12f + hash01(seed + s * 131) * 0.11f))));
                emitLightningSegment(vc, matrix, normal, next, branchEnd, thickness * 0.58f, WHITE, (int) ((float) alpha * 0.52f));
            }
            previous = next;
        }
    }

    private static void emitLightningSegment(VertexConsumer vc, Matrix4f matrix, Matrix3f normal, Vec3 start, Vec3 end, float thickness, int color, int alpha) {
        emitSegment(vc, matrix, normal, start, end, thickness * 3.0f, DEEP, (int) ((float) alpha * 0.22f));
        emitSegment(vc, matrix, normal, start, end, thickness * 1.65f, VIOLET, (int) ((float) alpha * 0.34f));
        emitSegment(vc, matrix, normal, start, end, thickness, color, alpha);
    }

    private static Vec3 ringPoint(float angle, float radius, int plane) {
        float x = Mth.cos(angle) * radius;
        float z = Mth.sin(angle) * radius;
        return switch (plane) {
            case 1 -> new Vec3((double) x, (double) z, 0.0);
            case 2 -> new Vec3(0.0, (double) x, (double) z);
            default -> new Vec3((double) x, 0.0, (double) z);
        };
    }

    private static Vec3 spherePoint(float angle, float radius, float yNorm) {
        float y = Mth.clamp(yNorm, -0.82f, 0.82f) * radius;
        float horizontal = (float) Math.sqrt(Math.max(0.0f, radius * radius - y * y));
        return pointXZ(angle, horizontal, y);
    }

    private static Vec3 directionFromSeed(int seed) {
        float y = signedHash(seed + 13) * 0.88f;
        float angle = hash01(seed + 17) * ((float) Math.PI * 2);
        float horizontal = (float) Math.sqrt(Math.max(0.0f, 1.0f - y * y));
        return new Vec3((double) (Mth.cos(angle) * horizontal), (double) y, (double) (Mth.sin(angle) * horizontal)).normalize();
    }

    private static Vec3 perpendicular(Vec3 direction) {
        Vec3 axis = Math.abs(direction.y) < 0.86 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(1.0, 0.0, 0.0);
        Vec3 side = direction.cross(axis);
        if (side.lengthSqr() < 1.0E-6) {
            side = new Vec3(1.0, 0.0, 0.0);
        }
        return side.normalize();
    }

    private static Vec3 pointXZ(float angle, float radius, float y) {
        return new Vec3((double) (Mth.cos(angle) * radius), (double) y, (double) (Mth.sin(angle) * radius));
    }

    private static void emitSegment(VertexConsumer vc, Matrix4f matrix, Matrix3f normal, Vec3 start, Vec3 end, float thickness, int color, int alpha) {
        if (alpha <= 0) {
            return;
        }
        Vec3 dir = end.subtract(start);
        if (dir.lengthSqr() < 1.0E-6) {
            return;
        }
        Vec3 side = dir.normalize().cross(new Vec3(0.0, 1.0, 0.0));
        if (side.lengthSqr() < 1.0E-6) {
            side = new Vec3(1.0, 0.0, 0.0);
        }
        side = side.normalize().scale(thickness);
        quad(vc, matrix, normal,
                start.add(side), end.add(side), end.subtract(side), start.subtract(side),
                color, Mth.clamp(alpha, 0, 255));
    }

    private static void triangle(VertexConsumer vc, Matrix4f matrix, Matrix3f normal, Vec3 a, Vec3 b, Vec3 c, int color, int alpha) {
        vertex(vc, matrix, normal, a, color, alpha);
        vertex(vc, matrix, normal, b, color, alpha);
        vertex(vc, matrix, normal, c, color, alpha);
    }

    private static void quad(VertexConsumer vc, Matrix4f matrix, Matrix3f normal, Vec3 a, Vec3 b, Vec3 c, Vec3 d, int color, int alpha) {
        vertex(vc, matrix, normal, a, color, alpha);
        vertex(vc, matrix, normal, b, color, alpha);
        vertex(vc, matrix, normal, c, color, alpha);
        vertex(vc, matrix, normal, d, color, alpha);
    }

    private static void vertex(VertexConsumer vc, Matrix4f matrix, Matrix3f normal, Vec3 pos, int color, int alpha) {
        vc.vertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                .color(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, Mth.clamp(alpha, 0, 255))
                .uv(0.0f, 0.0f)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(0xF000F0)
                .normal(normal, 0.0f, 1.0f, 0.0f)
                .endVertex();
    }

    private static int alphaToByte(float alpha) {
        return Mth.clamp((int) (alpha * 255.0f), 0, 255);
    }

    private static float positiveModulo(float value, float mod) {
        float result = value % mod;
        return result < 0.0f ? result + mod : result;
    }

    private static float hash01(int value) {
        int x = value;
        x ^= x >>> 16;
        x *= 2146121005;
        x ^= x >>> 15;
        x *= -2073254261;
        x ^= x >>> 16;
        return (float) (x & 0xFFFF) / 65535.0f;
    }

    private static float signedHash(int value) {
        return hash01(value) * 2.0f - 1.0f;
    }
}
