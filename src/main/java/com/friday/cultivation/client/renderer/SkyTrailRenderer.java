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
import com.friday.cultivation.entity.SkyTrailEntity;
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

public class SkyTrailRenderer
extends EntityRenderer<SkyTrailEntity> {
    private static final ResourceLocation WHITE = new ResourceLocation("textures/misc/white.png");
    private static final int CROSS_N = 8;
    private static final float RADIUS = 0.6f;
    private static final int BASE_ALPHA = 200;

    public SkyTrailRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.0f;
    }

    public void render(@NotNull SkyTrailEntity entity, float yaw, float partialTick, @NotNull PoseStack pose, @NotNull MultiBufferSource buf, int light) {
        Vec3 start;
        Vec3 end = entity.endPos();
        Vec3 dir = end.multiply(start = entity.position());
        double length = dir.length();
        if (length < 0.1) {
            return;
        }
        float alpha = 200.0f * entity.alphaMultiplier();
        if (alpha < 1.0f) {
            return;
        }
        int alphaByte = (int)alpha;
        pose.pushPose();
        Vec3 unit = dir.scale(1.0 / length);
        float yawRad = (float)Math.atan2(unit.x, unit.z);
        double horizLen = Math.sqrt(unit.x * unit.x + unit.z * unit.z);
        float pitchRad = (float)Math.atan2(-unit.y, horizLen);
        pose.mulPose(new Quaternionf().rotationY(yawRad));
        pose.mulPose(new Quaternionf().rotationX(pitchRad));
        RenderSystem.enableBlend();
        VertexConsumer vc = buf.getBuffer(RenderType.entityTranslucentEmissive((ResourceLocation)WHITE));
        SkyTrailRenderer.emitCylinder(vc, pose, length, 0.6f, 255, 255, 255, alphaByte);
        SkyTrailRenderer.emitCylinder(vc, pose, length, 0.96000004f, 255, 255, 255, (int)((double)alphaByte * 0.4));
        RenderSystem.disableBlend();
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buf, light);
    }

    private static void emitCylinder(VertexConsumer vc, PoseStack pose, double length, float r, int red, int green, int blue, int alpha) {
        int j;
        Matrix4f m = pose.last().pose();
        Matrix3f n = pose.last().normal();
        float[][] startRing = new float[8][3];
        float[][] endRing = new float[8][3];
        for (j = 0; j < 8; ++j) {
            double phi = (double)j / 8.0 * 2.0 * Math.PI;
            startRing[j][0] = r * (float)Math.cos(phi);
            startRing[j][1] = r * (float)Math.sin(phi);
            startRing[j][2] = 0.0f;
            endRing[j][0] = startRing[j][0];
            endRing[j][1] = startRing[j][1];
            endRing[j][2] = (float)length;
        }
        for (j = 0; j < 8; ++j) {
            int jn = (j + 1) % 8;
            float[] p00 = startRing[j];
            float[] p10 = endRing[j];
            float[] p11 = endRing[jn];
            float[] p01 = startRing[jn];
            float u1 = (float)j / 8.0f;
            float u2 = (float)(j + 1) / 8.0f;
            SkyTrailRenderer.addV(vc, m, n, p00, u1, 0.0f, red, green, blue, alpha);
            SkyTrailRenderer.addV(vc, m, n, p10, u1, 1.0f, red, green, blue, alpha);
            SkyTrailRenderer.addV(vc, m, n, p11, u2, 1.0f, red, green, blue, alpha);
            SkyTrailRenderer.addV(vc, m, n, p01, u2, 0.0f, red, green, blue, alpha);
        }
    }

    private static void addV(VertexConsumer vc, Matrix4f m, Matrix3f n, float[] pos, float u, float v, int r, int g, int b, int a) {
        vc.vertex(m, pos[0], pos[1], pos[2]).color(r, g, b, a).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0, 0xF000F0).normal(n, 0.0f, 1.0f, 0.0f).endVertex();
    }

    @NotNull
    public ResourceLocation getTextureLocation(@NotNull SkyTrailEntity entity) {
        return WHITE;
    }
}

