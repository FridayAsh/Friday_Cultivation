/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.Vec3
 *  org.jetbrains.annotations.NotNull
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 */
package com.friday.cultivation.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.friday.cultivation.entity.HeavenPiercingConeEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class HeavenPiercingConeRenderer
extends EntityRenderer<HeavenPiercingConeEntity> {
    private static final ResourceLocation WHITE = ResourceLocation.tryParse((String)"minecraft:textures/misc/white.png");
    private static final int SEGMENTS = 12;
    private static final float[] RING_Z = new float[]{-0.56f, -0.4f, -0.16f, 0.12f, 0.36f, 0.58f};
    private static final float[] RING_RADIUS = new float[]{1.12f, 0.96f, 0.68f, 0.41f, 0.17f, 0.0f};

    public HeavenPiercingConeRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.0f;
    }

    public void render(@NotNull HeavenPiercingConeEntity entity, float yaw, float partialTick, @NotNull PoseStack pose, @NotNull MultiBufferSource buffers, int packedLight) {
        Vec3 direction = entity.getDeltaMovement();
        if (direction.lengthSqr() < 1.0E-6) {
            direction = entity.getLookAngle();
        }
        HeavenPiercingConeRenderer.renderCone(pose, buffers, direction, entity.visualStage(), entity.chargeTicks(), (float)entity.tickCount + partialTick, packedLight, 1.0f);
        super.render(entity, yaw, partialTick, pose, buffers, packedLight);
    }

    public static void renderCone(PoseStack pose, MultiBufferSource buffers, Vec3 direction, int stage, int chargeTicks, float ageTicks, float alphaMultiplier) {
        HeavenPiercingConeRenderer.renderCone(pose, buffers, direction, stage, chargeTicks, ageTicks, 0xF000F0, alphaMultiplier);
    }

    public static void renderCone(PoseStack pose, MultiBufferSource buffers, Vec3 direction, int stage, int chargeTicks, float ageTicks, int packedLight, float alphaMultiplier) {
        Vec3 dir = direction.lengthSqr() < 1.0E-6 ? new Vec3(0.0, 0.0, 1.0) : direction.normalize();
        int safeStage = Mth.clamp((int)stage, (int)1, (int)4);
        float alpha = Mth.clamp((float)alphaMultiplier, (float)0.0f, (float)1.0f);
        pose.pushPose();
        HeavenPiercingConeRenderer.orientAlongDirection(pose, dir);
        pose.mulPose(Axis.ZP.rotationDegrees(ageTicks * (18.0f + (float)safeStage * 16.0f + (float)chargeTicks * 0.08f)));
        float length = switch (safeStage) {
            case 1 -> 1.55f;
            case 2 -> 1.9f;
            case 3 -> 2.25f;
            default -> 2.65f;
        };
        float radius = switch (safeStage) {
            case 1 -> 0.22f;
            case 2 -> 0.28f;
            case 3 -> 0.35f;
            default -> 0.43f;
        };
        ConePalette palette = HeavenPiercingConeRenderer.paletteFor(safeStage);
        int alphaByte = (int)(255.0f * alpha);
        RenderSystem.enableBlend();
        VertexConsumer body = buffers.getBuffer(RenderType.entityTranslucent((ResourceLocation)WHITE));
        HeavenPiercingConeRenderer.emitStoneModel(pose, body, length, radius, palette, alphaByte, packedLight);
        VertexConsumer glow = buffers.getBuffer(RenderType.entityTranslucentEmissive((ResourceLocation)WHITE));
        HeavenPiercingConeRenderer.emitGlowModel(pose, glow, length, radius, palette, safeStage, chargeTicks, alphaByte);
        pose.popPose();
    }

    private static void orientAlongDirection(PoseStack pose, Vec3 dir) {
        float yaw = (float)(Mth.atan2((double)dir.x, (double)dir.z) * 57.2957763671875);
        double horizontal = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
        float pitch = (float)(Mth.atan2((double)(-dir.y), (double)horizontal) * 57.2957763671875);
        pose.mulPose(Axis.YP.rotationDegrees(yaw));
        pose.mulPose(Axis.XP.rotationDegrees(pitch));
    }

    private static ConePalette paletteFor(int stage) {
        return switch (stage) {
            case 1 -> new ConePalette(-13551558, -13551558, -11643047, -9537671, -7747608, -12761527, -13946315);
            case 2 -> new ConePalette(-13025986, -13025986, -10460832, -8027781, -2558721, -11315627, -13486538);
            case 3 -> new ConePalette(-10931928, -10931928, -8766164, -3718621, -24491, -10341590, -12967908);
            default -> new ConePalette(-3721950, -3721950, -38347, -12166, -3914, -6606561, -10609900);
        };
    }

    private static void emitStoneModel(PoseStack pose, VertexConsumer vc, float length, float radius, ConePalette palette, int alpha, int packedLight) {
        PoseStack.Pose last = pose.last();
        Matrix4f matrix = last.pose();
        Matrix3f normal = last.normal();
        for (int ring = 0; ring < RING_Z.length - 1; ++ring) {
            float z0 = RING_Z[ring] * length;
            float z1 = RING_Z[ring + 1] * length;
            float r0 = RING_RADIUS[ring] * radius;
            float r1 = RING_RADIUS[ring + 1] * radius;
            float progress = (float)ring / (float)(RING_Z.length - 2);
            int base = HeavenPiercingConeRenderer.mix(palette.body(), palette.tip(), progress * 0.75f);
            for (int i = 0; i < 12; ++i) {
                float a0 = HeavenPiercingConeRenderer.angle(i);
                float a1 = HeavenPiercingConeRenderer.angle(i + 1);
                float facet0 = HeavenPiercingConeRenderer.facetScale(i, ring);
                float facet1 = HeavenPiercingConeRenderer.facetScale(i + 1, ring);
                float[] p00 = HeavenPiercingConeRenderer.point(a0, r0 * facet0, z0);
                float[] p01 = HeavenPiercingConeRenderer.point(a1, r0 * facet1, z0);
                float[] p10 = HeavenPiercingConeRenderer.point(a0, r1 * HeavenPiercingConeRenderer.facetScale(i, ring + 1), z1);
                float[] p11 = HeavenPiercingConeRenderer.point(a1, r1 * HeavenPiercingConeRenderer.facetScale(i + 1, ring + 1), z1);
                float shade = 0.78f + ((i & 1) == 0 ? 0.12f : 0.0f) + (float)ring * 0.025f;
                int color = HeavenPiercingConeRenderer.shade(base, shade);
                float normalAngle = (a0 + a1) * 0.5f;
                float nx = (float)Math.cos(normalAngle) * 0.78f;
                float ny = (float)Math.sin(normalAngle) * 0.78f;
                float nz = 0.28f;
                if (r1 <= 0.001f) {
                    HeavenPiercingConeRenderer.triangle(vc, matrix, normal, p00, p01, p10, color, alpha, packedLight, nx, ny, nz);
                    continue;
                }
                HeavenPiercingConeRenderer.quad(vc, matrix, normal, p00, p01, p11, p10, color, alpha, packedLight, nx, ny, nz);
            }
        }
        HeavenPiercingConeRenderer.emitRearCap(vc, matrix, normal, length, radius, palette, alpha, packedLight);
        HeavenPiercingConeRenderer.emitRaisedBand(vc, matrix, normal, length, radius, -0.41f, 1.16f, HeavenPiercingConeRenderer.rearBodyColor(palette), alpha, packedLight);
        HeavenPiercingConeRenderer.emitRaisedBand(vc, matrix, normal, length, radius, -0.11f, 0.76f, palette.edge(), alpha, packedLight);
        HeavenPiercingConeRenderer.emitRaisedBand(vc, matrix, normal, length, radius, 0.26f, 0.32f, palette.edge(), alpha, packedLight);
        HeavenPiercingConeRenderer.emitTailFins(vc, matrix, normal, length, radius, palette, alpha, packedLight);
    }

    private static void emitRearCap(VertexConsumer vc, Matrix4f matrix, Matrix3f normal, float length, float radius, ConePalette palette, int alpha, int packedLight) {
        float z = RING_Z[0] * length;
        float r = RING_RADIUS[0] * radius;
        float[] center = new float[]{0.0f, 0.0f, z};
        for (int i = 0; i < 12; ++i) {
            float a0 = HeavenPiercingConeRenderer.angle(i);
            float a1 = HeavenPiercingConeRenderer.angle(i + 1);
            float[] p0 = HeavenPiercingConeRenderer.point(a0, r * HeavenPiercingConeRenderer.facetScale(i, 0), z);
            float[] p1 = HeavenPiercingConeRenderer.point(a1, r * HeavenPiercingConeRenderer.facetScale(i + 1, 0), z);
            HeavenPiercingConeRenderer.triangle(vc, matrix, normal, center, p1, p0, HeavenPiercingConeRenderer.rearBodyColor(palette), alpha, packedLight, 0.0f, 0.0f, -1.0f);
        }
    }

    private static void emitRaisedBand(VertexConsumer vc, Matrix4f matrix, Matrix3f normal, float length, float radius, float zNorm, float radiusFactor, int color, int alpha, int packedLight) {
        float z0 = zNorm * length - 0.018f * length;
        float z1 = zNorm * length + 0.018f * length;
        float r = radius * radiusFactor;
        for (int i = 0; i < 12; ++i) {
            float a0 = HeavenPiercingConeRenderer.angle(i);
            float a1 = HeavenPiercingConeRenderer.angle(i + 1);
            float[] p00 = HeavenPiercingConeRenderer.point(a0, r * 1.03f, z0);
            float[] p01 = HeavenPiercingConeRenderer.point(a1, r * 1.03f, z0);
            float[] p11 = HeavenPiercingConeRenderer.point(a1, r * 0.97f, z1);
            float[] p10 = HeavenPiercingConeRenderer.point(a0, r * 0.97f, z1);
            float normalAngle = (a0 + a1) * 0.5f;
            HeavenPiercingConeRenderer.quad(vc, matrix, normal, p00, p01, p11, p10, HeavenPiercingConeRenderer.shade(color, 0.86f + (float)(i % 3) * 0.08f), alpha, packedLight, (float)Math.cos(normalAngle), (float)Math.sin(normalAngle), 0.12f);
        }
    }

    private static void emitTailFins(VertexConsumer vc, Matrix4f matrix, Matrix3f normal, float length, float radius, ConePalette palette, int alpha, int packedLight) {
        float rear = -0.57f * length;
        float front = -0.3f * length;
        for (int i = 0; i < 4; ++i) {
            float a = HeavenPiercingConeRenderer.angle(i * 3);
            float side = 0.12f;
            float[] p0 = HeavenPiercingConeRenderer.point(a - side, radius * 0.82f, rear);
            float[] p1 = HeavenPiercingConeRenderer.point(a + side, radius * 0.82f, rear);
            float[] p2 = HeavenPiercingConeRenderer.point(a, radius * 1.42f, front);
            HeavenPiercingConeRenderer.triangle(vc, matrix, normal, p0, p1, p2, HeavenPiercingConeRenderer.shade(palette.rear(), 0.72f), alpha, packedLight, (float)Math.cos(a), (float)Math.sin(a), -0.15f);
        }
    }

    private static void emitGlowModel(PoseStack pose, VertexConsumer vc, float length, float radius, ConePalette palette, int stage, int chargeTicks, int alpha) {
        PoseStack.Pose last = pose.last();
        Matrix4f matrix = last.pose();
        Matrix3f normal = last.normal();
        int glowAlpha = Math.min(255, Math.max(80, alpha + 15));
        float pulse = 0.82f + Mth.sin((float)((float)chargeTicks * 0.22f)) * 0.18f;
        int glow = HeavenPiercingConeRenderer.shade(palette.glow(), pulse);
        int veinCount = stage >= 3 ? 6 : 4;
        for (int i = 0; i < veinCount; ++i) {
            float a = HeavenPiercingConeRenderer.angle(i * 12 / veinCount);
            HeavenPiercingConeRenderer.emitGlowVein(vc, matrix, normal, length, radius, a, glow, glowAlpha);
        }
        if (stage >= 3) {
            float z0 = 0.26f * length;
            float z1 = 0.58f * length;
            float r0 = radius * (stage >= 4 ? 0.28f : 0.2f);
            for (int i = 0; i < 12; ++i) {
                float a0 = HeavenPiercingConeRenderer.angle(i);
                float a1 = HeavenPiercingConeRenderer.angle(i + 1);
                float[] p0 = HeavenPiercingConeRenderer.point(a0, r0, z0);
                float[] p1 = HeavenPiercingConeRenderer.point(a1, r0, z0);
                float[] tip = new float[]{0.0f, 0.0f, z1};
                HeavenPiercingConeRenderer.triangle(vc, matrix, normal, p0, p1, tip, glow, glowAlpha, 0xF000F0, 0.0f, 0.0f, 1.0f);
            }
        }
    }

    private static void emitGlowVein(VertexConsumer vc, Matrix4f matrix, Matrix3f normal, float length, float radius, float centerAngle, int color, int alpha) {
        float halfWidth = 0.035f;
        float z0 = -0.3f * length;
        float z1 = 0.34f * length;
        float r0 = radius * 0.93f;
        float r1 = radius * 0.24f;
        float[] p00 = HeavenPiercingConeRenderer.point(centerAngle - halfWidth, r0, z0);
        float[] p01 = HeavenPiercingConeRenderer.point(centerAngle + halfWidth, r0, z0);
        float[] p11 = HeavenPiercingConeRenderer.point(centerAngle + halfWidth, r1, z1);
        float[] p10 = HeavenPiercingConeRenderer.point(centerAngle - halfWidth, r1, z1);
        HeavenPiercingConeRenderer.quad(vc, matrix, normal, p00, p01, p11, p10, color, alpha, 0xF000F0, (float)Math.cos(centerAngle), (float)Math.sin(centerAngle), 0.22f);
    }

    private static float angle(int segment) {
        return (float)((double)segment * Math.PI * 2.0 / 12.0);
    }

    private static float facetScale(int segment, int ring) {
        float uneven = (segment + ring) % 3 == 0 ? 0.055f : -0.015f;
        return 1.0f + uneven + Mth.sin((float)((float)segment * 1.37f + (float)ring * 0.71f)) * 0.035f;
    }

    private static float[] point(float angle, float radius, float z) {
        return new float[]{(float)Math.cos(angle) * radius, (float)Math.sin(angle) * radius, z};
    }

    private static int mix(int first, int second, float t) {
        float clamped = Mth.clamp((float)t, (float)0.0f, (float)1.0f);
        int r = (int)((float)HeavenPiercingConeRenderer.red(first) + (float)(HeavenPiercingConeRenderer.red(second) - HeavenPiercingConeRenderer.red(first)) * clamped);
        int g = (int)((float)HeavenPiercingConeRenderer.green(first) + (float)(HeavenPiercingConeRenderer.green(second) - HeavenPiercingConeRenderer.green(first)) * clamped);
        int b = (int)((float)HeavenPiercingConeRenderer.blue(first) + (float)(HeavenPiercingConeRenderer.blue(second) - HeavenPiercingConeRenderer.blue(first)) * clamped);
        return r << 16 | g << 8 | b;
    }

    private static int shade(int color, float factor) {
        float clamped = Mth.clamp((float)factor, (float)0.0f, (float)1.6f);
        int r = Mth.clamp((int)((int)((float)HeavenPiercingConeRenderer.red(color) * clamped)), (int)0, (int)255);
        int g = Mth.clamp((int)((int)((float)HeavenPiercingConeRenderer.green(color) * clamped)), (int)0, (int)255);
        int b = Mth.clamp((int)((int)((float)HeavenPiercingConeRenderer.blue(color) * clamped)), (int)0, (int)255);
        return r << 16 | g << 8 | b;
    }

    private static int rearBodyColor(ConePalette palette) {
        return HeavenPiercingConeRenderer.shade(palette.body(), 0.82f);
    }

    private static int red(int color) {
        return color >> 16 & 0xFF;
    }

    private static int green(int color) {
        return color >> 8 & 0xFF;
    }

    private static int blue(int color) {
        return color & 0xFF;
    }

    private static void triangle(VertexConsumer vc, Matrix4f matrix, Matrix3f normal, float[] a, float[] b, float[] c, int color, int alpha, int packedLight, float nx, float ny, float nz) {
        HeavenPiercingConeRenderer.vertex(vc, matrix, normal, a[0], a[1], a[2], color, alpha, packedLight, nx, ny, nz);
        HeavenPiercingConeRenderer.vertex(vc, matrix, normal, b[0], b[1], b[2], color, alpha, packedLight, nx, ny, nz);
        HeavenPiercingConeRenderer.vertex(vc, matrix, normal, c[0], c[1], c[2], color, alpha, packedLight, nx, ny, nz);
        HeavenPiercingConeRenderer.vertex(vc, matrix, normal, c[0], c[1], c[2], color, alpha, packedLight, nx, ny, nz);
    }

    private static void quad(VertexConsumer vc, Matrix4f matrix, Matrix3f normal, float[] a, float[] b, float[] c, float[] d, int color, int alpha, int packedLight, float nx, float ny, float nz) {
        HeavenPiercingConeRenderer.vertex(vc, matrix, normal, a[0], a[1], a[2], color, alpha, packedLight, nx, ny, nz);
        HeavenPiercingConeRenderer.vertex(vc, matrix, normal, b[0], b[1], b[2], color, alpha, packedLight, nx, ny, nz);
        HeavenPiercingConeRenderer.vertex(vc, matrix, normal, c[0], c[1], c[2], color, alpha, packedLight, nx, ny, nz);
        HeavenPiercingConeRenderer.vertex(vc, matrix, normal, d[0], d[1], d[2], color, alpha, packedLight, nx, ny, nz);
    }

    private static void vertex(VertexConsumer vc, Matrix4f matrix, Matrix3f normal, float x, float y, float z, int color, int alpha, int packedLight, float nx, float ny, float nz) {
        vc.vertex(matrix, x, y, z).color(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, alpha).uv(0.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal, nx, ny, nz).endVertex();
    }

    @NotNull
    public ResourceLocation getTextureLocation(@NotNull HeavenPiercingConeEntity entity) {
        return WHITE;
    }

    private record ConePalette(int rear, int body, int edge, int tip, int glow, int band, int dark) {
    }
}

