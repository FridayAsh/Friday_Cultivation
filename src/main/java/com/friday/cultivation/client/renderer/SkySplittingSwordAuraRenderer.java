/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.Vec3
 *  org.jetbrains.annotations.NotNull
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 *  org.joml.Quaternionf
 */
package com.friday.cultivation.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.friday.cultivation.entity.SkySplittingSwordAuraEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class SkySplittingSwordAuraRenderer
extends EntityRenderer<SkySplittingSwordAuraEntity> {
    private static final ResourceLocation WHITE = new ResourceLocation("textures/misc/white.png");
    private static final float OUTER_R = 1.0f;
    private static final float INNER_R = 1.0f;
    private static final float OFFSET = 0.6f;
    private static final int SEGMENTS = 24;
    private static final int CROSS_N = 6;
    private static final float MAX_THICK = 0.12f;
    private static final int ALPHA = 235;
    private static final float HALO_SCALE = 1.18f;
    private static final int HALO_ALPHA = 60;
    private static final float MEGA_OUTER_HALO_SCALE = 1.45f;
    private static final int MEGA_OUTER_HALO_ALPHA = 30;
    private static final float[][][] RING_POS = new float[25][6][3];

    public SkySplittingSwordAuraRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.0f;
    }

    private static void precomputeMesh() {
        float zIntersect = -0.3f;
        float xIntersect = (float)Math.sqrt(1.0f - zIntersect * zIntersect);
        double tip1Outer = Math.atan2(zIntersect, xIntersect);
        double tip1Inner = Math.atan2(zIntersect + 0.6f, xIntersect);
        double outerSweep = Math.PI - 2.0 * tip1Outer;
        double innerSweep = Math.PI - 2.0 * tip1Inner;
        for (int i = 0; i <= 24; ++i) {
            float radUz;
            float radUx;
            float t = (float)i / 24.0f;
            double thetaO = tip1Outer + (double)t * outerSweep;
            float ox = (float)(1.0 * Math.cos(thetaO));
            float oz = (float)(1.0 * Math.sin(thetaO));
            double thetaI = tip1Inner + (double)t * innerSweep;
            float ix = (float)(1.0 * Math.cos(thetaI));
            float iz = (float)(1.0 * Math.sin(thetaI) - (double)0.6f);
            float cx = (ox + ix) * 0.5f;
            float cz = (oz + iz) * 0.5f;
            float radX = ox - ix;
            float radZ = oz - iz;
            float radLen = (float)Math.sqrt(radX * radX + radZ * radZ);
            float halfWidth = radLen * 0.5f;
            float halfHeight = 0.06f * (float)Math.sin((double)t * Math.PI);
            if (radLen > 1.0E-5f) {
                radUx = radX / radLen;
                radUz = radZ / radLen;
            } else {
                radUx = 1.0f;
                radUz = 0.0f;
            }
            for (int j = 0; j < 6; ++j) {
                double phi = (double)j / 6.0 * 2.0 * Math.PI;
                float radialOffset = halfWidth * (float)Math.cos(phi);
                float yOffset = halfHeight * (float)Math.sin(phi);
                SkySplittingSwordAuraRenderer.RING_POS[i][j][0] = cx + radUx * radialOffset;
                SkySplittingSwordAuraRenderer.RING_POS[i][j][1] = yOffset;
                SkySplittingSwordAuraRenderer.RING_POS[i][j][2] = cz + radUz * radialOffset;
            }
        }
    }

    public void render(@NotNull SkySplittingSwordAuraEntity entity, float yaw, float partialTick, @NotNull PoseStack pose, @NotNull MultiBufferSource buf, int light) {
        Vec3 forward = entity.renderForward();
        if (forward.lengthSqr() < 1.0E-4) {
            return;
        }
        float scale = entity.scale();
        float alphaMul = entity.getRenderAlpha(partialTick);
        if (alphaMul <= 0.001f) {
            return;
        }
        int bodyAlpha = (int)(235.0f * alphaMul);
        int innerHaloAlpha = (int)(60.0f * alphaMul);
        int outerHaloAlpha = (int)(30.0f * alphaMul);
        pose.pushPose();
        float yawRad = (float)Math.atan2(forward.x, forward.z);
        double horizLen = Math.sqrt(forward.x * forward.x + forward.z * forward.z);
        float pitchRad = (float)Math.atan2(-forward.y, horizLen);
        pose.mulPose(new Quaternionf().rotationY(yawRad));
        pose.mulPose(new Quaternionf().rotationX(pitchRad));
        pose.mulPose(new Quaternionf().rotationZ(entity.rollRad()));
        RenderSystem.enableBlend();
        VertexConsumer vc = buf.getBuffer(RenderType.entityTranslucentEmissive((ResourceLocation)WHITE));
        pose.pushPose();
        pose.scale(scale, scale, scale);
        SkySplittingSwordAuraRenderer.emitMesh(vc, pose.last().pose(), pose.last().normal(), 255, 255, 255, bodyAlpha);
        pose.popPose();
        pose.pushPose();
        float haloScale = scale * 1.18f;
        pose.scale(haloScale, haloScale, haloScale);
        SkySplittingSwordAuraRenderer.emitMesh(vc, pose.last().pose(), pose.last().normal(), 255, 255, 255, innerHaloAlpha);
        pose.popPose();
        if (entity.isMega()) {
            pose.pushPose();
            float outerHaloScale = scale * 1.45f;
            pose.scale(outerHaloScale, outerHaloScale, outerHaloScale);
            SkySplittingSwordAuraRenderer.emitMesh(vc, pose.last().pose(), pose.last().normal(), 220, 230, 255, outerHaloAlpha);
            pose.popPose();
        }
        RenderSystem.disableBlend();
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buf, light);
    }

    private static void emitMesh(VertexConsumer vc, Matrix4f m, Matrix3f n, int r, int g, int b, int a) {
        for (int i = 0; i < 24; ++i) {
            float u1 = (float)i / 24.0f;
            float u2 = (float)(i + 1) / 24.0f;
            for (int j = 0; j < 6; ++j) {
                int jn = (j + 1) % 6;
                float v1 = (float)j / 6.0f;
                float v2 = (float)(j + 1) / 6.0f;
                float[] p00 = RING_POS[i][j];
                float[] p10 = RING_POS[i + 1][j];
                float[] p11 = RING_POS[i + 1][jn];
                float[] p01 = RING_POS[i][jn];
                SkySplittingSwordAuraRenderer.addV(vc, m, n, p00, u1, v1, r, g, b, a);
                SkySplittingSwordAuraRenderer.addV(vc, m, n, p10, u2, v1, r, g, b, a);
                SkySplittingSwordAuraRenderer.addV(vc, m, n, p11, u2, v2, r, g, b, a);
                SkySplittingSwordAuraRenderer.addV(vc, m, n, p01, u1, v2, r, g, b, a);
            }
        }
    }

    private static void addV(VertexConsumer vc, Matrix4f m, Matrix3f n, float[] pos, float u, float v, int r, int g, int b, int a) {
        vc.vertex(m, pos[0], pos[1], pos[2]).color(r, g, b, a).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0, 0xF000F0).normal(n, 0.0f, 1.0f, 0.0f).endVertex();
    }

    @NotNull
    public ResourceLocation getTextureLocation(@NotNull SkySplittingSwordAuraEntity entity) {
        return WHITE;
    }

    static {
        SkySplittingSwordAuraRenderer.precomputeMesh();
    }
}

