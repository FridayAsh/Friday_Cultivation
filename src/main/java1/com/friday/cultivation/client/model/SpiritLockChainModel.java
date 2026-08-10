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
import org.joml.Quaternionf;

/**
 * 锁链渲染模型 — 完整复刻原模组 SpiritLockChainModel。
 * 用于勾魂术锁链、锁灵术锁链等视觉效果。提供 renderEntity/renderBlock/renderChainBetween 三种渲染模式。
 */
public final class SpiritLockChainModel {
    private static final ResourceLocation WHITE_TEXTURE = new ResourceLocation("textures/misc/white.png");
    private static final RenderType CHAIN_RENDER_TYPE = RenderType.entityTranslucent(WHITE_TEXTURE);
    private static final int GOLD = 16766042;
    private static final int GOLD_DARK = 11103766;
    private static final int GOLD_LIGHT = 0xFFF0A0;

    private SpiritLockChainModel() {
    }

    public static RenderType renderType() {
        return CHAIN_RENDER_TYPE;
    }

    public static void renderEntity(PoseStack pose, MultiBufferSource buffers, float entityWidth, float entityHeight, float age, float alpha) {
        renderEntity(pose, buffers, entityWidth, entityHeight, age, alpha, GOLD, GOLD_DARK, GOLD_LIGHT);
    }

    public static void renderEntity(PoseStack pose, MultiBufferSource buffers, float entityWidth, float entityHeight, float age, float alpha, int mainColor, int darkColor, int lightColor) {
        float height = Math.max(0.8f, entityHeight);
        float radius = Math.max(0.34f, entityWidth * 0.68f);
        float linkWidth = Mth.clamp(entityWidth * 0.23f, 0.15f, 0.3f);
        float linkHeight = linkWidth * 1.48f;
        float linkDepth = Math.max(0.035f, linkWidth * 0.2f);
        float thickness = Math.max(0.026f, linkWidth * 0.18f);
        RenderSystem.enableBlend();
        VertexConsumer vc = buffers.getBuffer(renderType());
        int a = alphaToByte(alpha);
        for (int strand = 0; strand < 3; ++strand) {
            float phase = (float) strand * ((float) Math.PI * 2) / 3.0f;
            for (int i = 0; i < 12; ++i) {
                float t = (float) i / 11.0f;
                float angle = age * 0.045f + phase + t * ((float) Math.PI * 2) * 1.28f;
                float y = 0.14f + t * (height - 0.2f);
                float x = Mth.cos(angle) * radius;
                float z = Mth.sin(angle) * radius;
                float yaw = (float) (1.5707963267948966 - (double) angle);
                float roll = (i + strand & 1) == 0 ? 0.0f : 1.5707964f;
                renderLinkAt(pose, vc, x, y, z, yaw, 0.38f, roll, linkWidth, linkHeight, linkDepth, thickness, a, mainColor, darkColor, lightColor);
            }
        }
        renderEntityBelt(pose, vc, radius * 1.05f, height * 0.42f, age * 0.02f, linkWidth * 0.92f, linkHeight * 0.92f, linkDepth, thickness, a, mainColor, darkColor, lightColor);
        renderEntityBelt(pose, vc, radius * 0.98f, height * 0.68f, -age * 0.018f + 0.7f, linkWidth * 0.86f, linkHeight * 0.86f, linkDepth, thickness, a, mainColor, darkColor, lightColor);
    }

    public static void renderBlock(PoseStack pose, MultiBufferSource buffers, float age, float alpha) {
        renderBlock(pose, buffers, age, alpha, GOLD, GOLD_DARK, GOLD_LIGHT);
    }

    public static void renderBlock(PoseStack pose, MultiBufferSource buffers, float age, float alpha, int mainColor, int darkColor, int lightColor) {
        RenderSystem.enableBlend();
        VertexConsumer vc = buffers.getBuffer(renderType());
        int a = alphaToByte(alpha);
        float linkWidth = 0.18f;
        float linkHeight = 0.28f;
        float linkDepth = 0.045f;
        float thickness = 0.035f;
        float radius = 0.69f;
        for (int side = 0; side < 4; ++side) {
            float phase = (float) side * 1.5707964f;
            for (int i = 0; i < 7; ++i) {
                float t = (float) i / 6.0f;
                float angle = phase + age * 0.02f + t * 0.42f;
                float x = Mth.cos(angle) * radius;
                float z = Mth.sin(angle) * radius;
                float y = -0.34f + t * 0.68f;
                float yaw = (float) (1.5707963267948966 - (double) angle);
                float roll = (i & 1) == 0 ? 0.0f : 1.5707964f;
                renderLinkAt(pose, vc, x, y, z, yaw, 0.18f, roll, linkWidth, linkHeight, linkDepth, thickness, a, mainColor, darkColor, lightColor);
            }
        }
        renderBlockBelt(pose, vc, radius, -0.2f, age * 0.018f, linkWidth, linkHeight, linkDepth, thickness, a, mainColor, darkColor, lightColor);
        renderBlockBelt(pose, vc, radius, 0.23f, -age * 0.018f + 0.35f, linkWidth, linkHeight, linkDepth, thickness, a, mainColor, darkColor, lightColor);
    }

    public static void renderChainBetween(PoseStack pose, MultiBufferSource buffers, Vec3 start, Vec3 end, float age, float alpha, int mainColor, int darkColor, int lightColor) {
        renderChainBetween(pose, buffers, start, end, age, alpha, mainColor, darkColor, lightColor, 1.0f);
    }

    public static void renderChainBetween(PoseStack pose, MultiBufferSource buffers, Vec3 start, Vec3 end, float age, float alpha, int mainColor, int darkColor, int lightColor, float scale) {
        Vec3 delta = end.subtract(start);
        double length = delta.length();
        if (length < 0.05) {
            return;
        }
        RenderSystem.enableBlend();
        VertexConsumer vc = buffers.getBuffer(renderType());
        int a = alphaToByte(alpha);
        Vec3 dir = delta.scale(1.0 / length);
        Vec3 side = new Vec3(-dir.z, 0.0, dir.x);
        side = side.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : side.normalize();
        Vec3 up = dir.cross(side).normalize();
        float yaw = (float) Math.atan2(dir.x, dir.z);
        float pitch = (float) (-Math.asin(Mth.clamp((float) dir.y, -1.0f, 1.0f)));
        int links = Mth.clamp((int) Math.ceil(length * 3.0 / (double) Math.max(0.6f, scale)), 4, 96);
        float linkWidth = 0.16f * scale;
        float linkHeight = 0.26f * scale;
        float linkDepth = 0.042f * scale;
        float thickness = 0.032f * scale;
        double waveAmp = 0.045 * (double) scale;
        for (int i = 0; i <= links; ++i) {
            double t = (double) i / (double) links;
            double wave = Math.sin(t * Math.PI * 6.0 + (double) (age * 0.11f)) * waveAmp;
            Vec3 p = start.add(delta.scale(t)).add(side.scale(wave)).add(up.scale(wave * 0.35));
            float roll = (i & 1) == 0 ? 0.0f : 1.5707964f;
            renderLinkAt(pose, vc, (float) p.x, (float) p.y, (float) p.z, yaw, pitch, roll, linkWidth, linkHeight, linkDepth, thickness, a, mainColor, darkColor, lightColor);
        }
    }

    private static void renderEntityBelt(PoseStack pose, VertexConsumer vc, float radius, float y, float phase, float linkWidth, float linkHeight, float linkDepth, float thickness, int alpha, int mainColor, int darkColor, int lightColor) {
        for (int i = 0; i < 14; ++i) {
            float angle = phase + (float) i * ((float) Math.PI * 2) / 14.0f;
            float x = Mth.cos(angle) * radius;
            float z = Mth.sin(angle) * radius;
            float yaw = (float) (1.5707963267948966 - (double) angle);
            float roll = (i & 1) == 0 ? 1.5707964f : 0.0f;
            renderLinkAt(pose, vc, x, y, z, yaw, 0.0f, roll, linkWidth, linkHeight, linkDepth, thickness, alpha, mainColor, darkColor, lightColor);
        }
    }

    private static void renderBlockBelt(PoseStack pose, VertexConsumer vc, float radius, float y, float phase, float linkWidth, float linkHeight, float linkDepth, float thickness, int alpha, int mainColor, int darkColor, int lightColor) {
        for (int i = 0; i < 16; ++i) {
            float angle = phase + (float) i * ((float) Math.PI * 2) / 16.0f;
            float x = Mth.cos(angle) * radius;
            float z = Mth.sin(angle) * radius;
            float yaw = (float) (1.5707963267948966 - (double) angle);
            float roll = (i & 1) == 0 ? 1.5707964f : 0.0f;
            renderLinkAt(pose, vc, x, y, z, yaw, 0.0f, roll, linkWidth, linkHeight, linkDepth, thickness, alpha, mainColor, darkColor, lightColor);
        }
    }

    private static void renderLinkAt(PoseStack pose, VertexConsumer vc, float x, float y, float z, float yaw, float pitch, float roll, float width, float height, float depth, float thickness, int alpha, int mainColor, int darkColor, int lightColor) {
        pose.pushPose();
        pose.translate(x, y, z);
        pose.mulPose(new Quaternionf().rotationY(yaw));
        pose.mulPose(new Quaternionf().rotationX(pitch));
        pose.mulPose(new Quaternionf().rotationZ(roll));
        renderChainLink(pose, vc, width, height, depth, thickness, alpha, mainColor, darkColor, lightColor);
        pose.popPose();
    }

    private static void renderChainLink(PoseStack pose, VertexConsumer vc, float width, float height, float depth, float thickness, int alpha, int mainColor, int darkColor, int lightColor) {
        float hw = width * 0.5f;
        float hh = height * 0.5f;
        float hd = depth * 0.5f;
        float sideThickness = thickness * 0.86f;
        box(pose, vc, -hw, hh - thickness, -hd, hw, hh, hd, lightColor, alpha);
        box(pose, vc, -hw, -hh, -hd, hw, -hh + thickness, hd, darkColor, alpha);
        box(pose, vc, -hw, -hh, -hd, -hw + sideThickness, hh, hd, mainColor, alpha);
        box(pose, vc, hw - sideThickness, -hh, -hd, hw, hh, hd, mainColor, alpha);
    }

    private static void box(PoseStack pose, VertexConsumer vc, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int rgb, int alpha) {
        Matrix4f m = pose.last().pose();
        Matrix3f n = pose.last().normal();
        int r = rgb >> 16 & 0xFF;
        int g = rgb >> 8 & 0xFF;
        int b = rgb & 0xFF;
        face(vc, m, n, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, 0.0f, 0.0f, 1.0f, r, g, b, alpha);
        face(vc, m, n, maxX, minY, minZ, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, 0.0f, 0.0f, -1.0f, r, g, b, alpha);
        face(vc, m, n, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, minX, maxY, minZ, 0.0f, 1.0f, 0.0f, r, g, b, alpha);
        face(vc, m, n, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, 0.0f, -1.0f, 0.0f, r, g, b, alpha);
        face(vc, m, n, maxX, minY, maxZ, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, 1.0f, 0.0f, 0.0f, r, g, b, alpha);
        face(vc, m, n, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, -1.0f, 0.0f, 0.0f, r, g, b, alpha);
    }

    private static void face(VertexConsumer vc, Matrix4f m, Matrix3f n, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float nx, float ny, float nz, int r, int g, int b, int a) {
        vertex(vc, m, n, x1, y1, z1, 0.0f, 1.0f, nx, ny, nz, r, g, b, a);
        vertex(vc, m, n, x2, y2, z2, 1.0f, 1.0f, nx, ny, nz, r, g, b, a);
        vertex(vc, m, n, x3, y3, z3, 1.0f, 0.0f, nx, ny, nz, r, g, b, a);
        vertex(vc, m, n, x4, y4, z4, 0.0f, 0.0f, nx, ny, nz, r, g, b, a);
    }

    private static void vertex(VertexConsumer vc, Matrix4f m, Matrix3f n, float x, float y, float z, float u, float v, float nx, float ny, float nz, int r, int g, int b, int a) {
        vc.vertex(m, x, y, z).color(r, g, b, a).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(n, nx, ny, nz).endVertex();
    }

    private static int alphaToByte(float alpha) {
        return Mth.clamp((int) (alpha * 255.0f), 0, 255);
    }
}
