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
 *  net.minecraft.client.renderer.entity.ItemRenderer
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
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
import com.friday.cultivation.entity.SwordProjectileEntity;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class SwordProjectileRenderer
extends EntityRenderer<SwordProjectileEntity> {
    private static final ResourceLocation ICON = new ResourceLocation("textures/item/iron_sword.png");
    private static final ResourceLocation WHITE = new ResourceLocation("textures/misc/white.png");
    private static final float STREAK_HALF_WIDTH = 0.1f;
    private static final int STREAK_HEAD_ALPHA = 220;

    public SwordProjectileRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    public void render(@NotNull SwordProjectileEntity entity, float yaw, float partialTick, @NotNull PoseStack pose, @NotNull MultiBufferSource buf, int light) {
        Vec3 vel = entity.getDeltaMovement();
        double len = vel.length();
        if (entity.isNoTerrain()) {
            this.renderStreak(entity, pose, buf, partialTick);
        }
        pose.pushPose();
        if (len > 0.001) {
            float yawRad = (float)Math.atan2(vel.x, vel.z);
            double horizLen = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
            float pitchRad = (float)Math.atan2(-vel.y, horizLen);
            pose.mulPose(new Quaternionf().rotationY(yawRad));
            pose.mulPose(new Quaternionf().rotationX(pitchRad));
        }
        pose.mulPose(new Quaternionf().rotationY(1.5707964f));
        pose.mulPose(new Quaternionf().rotationZ(0.7853982f));
        pose.scale(1.6f, 1.6f, 1.6f);
        ItemStack sword = new ItemStack((ItemLike)Items.IRON_SWORD);
        ItemRenderer ir = Minecraft.getInstance().getItemRenderer();
        ir.renderStatic(sword, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, pose, buf, entity.level(), entity.getId());
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buf, light);
    }

    private void renderStreak(SwordProjectileEntity entity, PoseStack pose, MultiBufferSource buf, float partialTick) {
        ArrayList<Vec3> history = new ArrayList<Vec3>(entity.getTrailHistory());
        if (history.size() < 2) {
            return;
        }
        Vec3 renderPos = new Vec3(Mth.lerp((double)partialTick, (double)entity.xOld, (double)entity.getX()), Mth.lerp((double)partialTick, (double)entity.yOld, (double)entity.getY()), Mth.lerp((double)partialTick, (double)entity.zOld, (double)entity.getZ()));
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        RenderSystem.enableBlend();
        VertexConsumer vc = buf.getBuffer(RenderType.entityTranslucentEmissive((ResourceLocation)WHITE));
        Matrix4f m = pose.last().pose();
        Matrix3f n = pose.last().normal();
        int count = history.size();
        for (int i = 0; i < count - 1; ++i) {
            Vec3 perp;
            Vec3 worldP0 = (Vec3)history.get(i);
            Vec3 worldP1 = (Vec3)history.get(i + 1);
            Vec3 segWorld = worldP1.multiply(worldP0);
            if (segWorld.lengthSqr() < 1.0E-6) continue;
            Vec3 segDir = segWorld.normalize();
            Vec3 midWorld = worldP0.add(worldP1).scale(0.5);
            Vec3 toCamera = cameraPos.multiply(midWorld);
            if (toCamera.lengthSqr() < 1.0E-6 || (perp = segDir.cross(toCamera = toCamera.normalize())).lengthSqr() < 1.0E-6) continue;
            perp = perp.normalize();
            float t0 = (float)i / (float)(count - 1);
            float t1 = (float)(i + 1) / (float)(count - 1);
            float w0 = 0.1f * (1.0f - t0);
            float w1 = 0.1f * (1.0f - t1);
            int a0 = (int)(220.0f * (1.0f - t0));
            int a1 = (int)(220.0f * (1.0f - t1));
            Vec3 lp0 = worldP0.multiply(renderPos);
            Vec3 lp1 = worldP1.multiply(renderPos);
            SwordProjectileRenderer.addStreakVert(vc, m, n, (float)(lp0.x + perp.x * (double)w0), (float)(lp0.y + perp.y * (double)w0), (float)(lp0.z + perp.z * (double)w0), 1.0f, 0.0f, a0);
            SwordProjectileRenderer.addStreakVert(vc, m, n, (float)(lp1.x + perp.x * (double)w1), (float)(lp1.y + perp.y * (double)w1), (float)(lp1.z + perp.z * (double)w1), 1.0f, 1.0f, a1);
            SwordProjectileRenderer.addStreakVert(vc, m, n, (float)(lp1.x - perp.x * (double)w1), (float)(lp1.y - perp.y * (double)w1), (float)(lp1.z - perp.z * (double)w1), 0.0f, 1.0f, a1);
            SwordProjectileRenderer.addStreakVert(vc, m, n, (float)(lp0.x - perp.x * (double)w0), (float)(lp0.y - perp.y * (double)w0), (float)(lp0.z - perp.z * (double)w0), 0.0f, 0.0f, a0);
        }
        RenderSystem.disableBlend();
    }

    private static void addStreakVert(VertexConsumer vc, Matrix4f m, Matrix3f n, float x, float y, float z, float u, float v, int alpha) {
        vc.vertex(m, x, y, z).color(255, 255, 255, alpha).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0, 0xF000F0).normal(n, 0.0f, 1.0f, 0.0f).endVertex();
    }

    @NotNull
    public ResourceLocation getTextureLocation(@NotNull SwordProjectileEntity entity) {
        return ICON;
    }
}

