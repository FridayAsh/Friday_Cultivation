/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.culling.Frustum
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  org.jetbrains.annotations.NotNull
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 */
package com.friday.cultivation.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.QiElement;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.entity.QiOrbEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class QiOrbRenderer
extends EntityRenderer<QiOrbEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("friday_cultivation", "textures/particle/qi.png");

    public QiOrbRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.0f;
    }

    public boolean shouldRender(QiOrbEntity entity, Frustum cam, double camX, double camY, double camZ) {
        if (!QiOrbRenderer.isViewerCultivator()) {
            return false;
        }
        return super.shouldRender(entity, cam, camX, camY, camZ);
    }

    public void render(@NotNull QiOrbEntity entity, float yaw, float partialTick, @NotNull PoseStack pose, @NotNull MultiBufferSource buffers, int light) {
        float fadeProgress;
        if (!QiOrbRenderer.isViewerCultivator()) {
            return;
        }
        QiElement element = entity.getElement();
        float r = element.r();
        float g = element.g();
        float b = element.b();
        float a = 0.9f;
        int FADE_DURATION = 100;
        int FADE_START = 500;
        int tick = entity.tickCount;
        if (tick > 500 && (a = 0.9f * (1.0f - (fadeProgress = Math.min(1.0f, (float)(tick - 500) / 100.0f)))) <= 0.01f) {
            return;
        }
        pose.pushPose();
        pose.mulPose(this.entityRenderDispatcher.cameraOrientation());
        float size = 0.18f;
        pose.scale(size, size, size);
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucentEmissive((ResourceLocation)TEXTURE));
        Matrix4f matrix = pose.last().pose();
        Matrix3f normal = pose.last().normal();
        int packedLight = 0xF000F0;
        consumer.vertex(matrix, -1.0f, -1.0f, 0.0f).color(r, g, b, a).uv(0.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal, 0.0f, 0.0f, 1.0f).endVertex();
        consumer.vertex(matrix, 1.0f, -1.0f, 0.0f).color(r, g, b, a).uv(1.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal, 0.0f, 0.0f, 1.0f).endVertex();
        consumer.vertex(matrix, 1.0f, 1.0f, 0.0f).color(r, g, b, a).uv(1.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal, 0.0f, 0.0f, 1.0f).endVertex();
        consumer.vertex(matrix, -1.0f, 1.0f, 0.0f).color(r, g, b, a).uv(0.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal, 0.0f, 0.0f, 1.0f).endVertex();
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buffers, light);
    }

    @NotNull
    public ResourceLocation getTextureLocation(@NotNull QiOrbEntity entity) {
        return TEXTURE;
    }

    private static boolean isViewerCultivator() {
        LocalPlayer viewer = Minecraft.getInstance().player;
        if (viewer == null) {
            return false;
        }
        return CultivationCapability.get((Player)viewer).map(d -> d.isSpellEnabled(Spell.SPIRIT_VISION)).orElse(false);
    }
}

