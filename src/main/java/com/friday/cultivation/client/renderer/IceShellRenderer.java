/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  org.jetbrains.annotations.NotNull
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 */
package com.friday.cultivation.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.friday.cultivation.entity.IceShellEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class IceShellRenderer
extends EntityRenderer<IceShellEntity> {
    private static final ResourceLocation ICE_TEXTURE = new ResourceLocation("textures/block/ice.png");

    public IceShellRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.0f;
    }

    public void render(@NotNull IceShellEntity entity, float yaw, float partialTick, @NotNull PoseStack pose, @NotNull MultiBufferSource buf, int light) {
        pose.pushPose();
        float halfW = 0.7f;
        float halfH = 1.05f;
        float yMin = -0.1f;
        float yMax = halfH * 2.0f + yMin;
        RenderSystem.enableBlend();
        VertexConsumer vc = buf.getBuffer(RenderType.entityTranslucent((ResourceLocation)ICE_TEXTURE));
        Matrix4f m = pose.last().pose();
        Matrix3f n = pose.last().normal();
        int alpha = 160;
        float pulse = (float)Math.sin((double)entity.tickCount * 0.3) * 20.0f;
        alpha = Math.max(120, Math.min(220, (int)((float)alpha + pulse)));
        IceShellRenderer.addQuad(vc, m, n, -halfW, yMax, halfW, 0.0f, 1.0f, halfW, yMax, halfW, 1.0f, 1.0f, halfW, yMax, -halfW, 1.0f, 0.0f, -halfW, yMax, -halfW, 0.0f, 0.0f, alpha, 0.0f, 1.0f, 0.0f);
        IceShellRenderer.addQuad(vc, m, n, -halfW, yMin, -halfW, 0.0f, 0.0f, halfW, yMin, -halfW, 1.0f, 0.0f, halfW, yMin, halfW, 1.0f, 1.0f, -halfW, yMin, halfW, 0.0f, 1.0f, alpha, 0.0f, -1.0f, 0.0f);
        IceShellRenderer.addQuad(vc, m, n, halfW, yMin, -halfW, 0.0f, 1.0f, halfW, yMax, -halfW, 0.0f, 0.0f, halfW, yMax, halfW, 1.0f, 0.0f, halfW, yMin, halfW, 1.0f, 1.0f, alpha, 1.0f, 0.0f, 0.0f);
        IceShellRenderer.addQuad(vc, m, n, -halfW, yMin, halfW, 0.0f, 1.0f, -halfW, yMax, halfW, 0.0f, 0.0f, -halfW, yMax, -halfW, 1.0f, 0.0f, -halfW, yMin, -halfW, 1.0f, 1.0f, alpha, -1.0f, 0.0f, 0.0f);
        IceShellRenderer.addQuad(vc, m, n, halfW, yMin, halfW, 0.0f, 1.0f, halfW, yMax, halfW, 0.0f, 0.0f, -halfW, yMax, halfW, 1.0f, 0.0f, -halfW, yMin, halfW, 1.0f, 1.0f, alpha, 0.0f, 0.0f, 1.0f);
        IceShellRenderer.addQuad(vc, m, n, -halfW, yMin, -halfW, 0.0f, 1.0f, -halfW, yMax, -halfW, 0.0f, 0.0f, halfW, yMax, -halfW, 1.0f, 0.0f, halfW, yMin, -halfW, 1.0f, 1.0f, alpha, 0.0f, 0.0f, -1.0f);
        RenderSystem.disableBlend();
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buf, light);
    }

    private static void addQuad(VertexConsumer vc, Matrix4f m, Matrix3f n, float x1, float y1, float z1, float u1, float v1, float x2, float y2, float z2, float u2, float v2, float x3, float y3, float z3, float u3, float v3, float x4, float y4, float z4, float u4, float v4, int alpha, float nx, float ny, float nz) {
        IceShellRenderer.addV(vc, m, n, x1, y1, z1, u1, v1, alpha, nx, ny, nz);
        IceShellRenderer.addV(vc, m, n, x2, y2, z2, u2, v2, alpha, nx, ny, nz);
        IceShellRenderer.addV(vc, m, n, x3, y3, z3, u3, v3, alpha, nx, ny, nz);
        IceShellRenderer.addV(vc, m, n, x4, y4, z4, u4, v4, alpha, nx, ny, nz);
    }

    private static void addV(VertexConsumer vc, Matrix4f m, Matrix3f n, float x, float y, float z, float u, float v, int alpha, float nx, float ny, float nz) {
        vc.vertex(m, x, y, z).color(220, 240, 255, alpha).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(Minecraft.getInstance().level == null ? 0xF000F0 : 0xF000F0).normal(n, nx, ny, nz).endVertex();
    }

    @NotNull
    public ResourceLocation getTextureLocation(@NotNull IceShellEntity entity) {
        return ICE_TEXTURE;
    }
}

