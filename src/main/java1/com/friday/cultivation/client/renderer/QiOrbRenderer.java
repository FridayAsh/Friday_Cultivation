package com.friday.cultivation.client.renderer;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.QiElement;
import com.friday.cultivation.entity.spell.QiOrbEntity;
import com.friday.cultivation.spell.Spell;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * 灵气球渲染器（严格照搬原模组 com.xiaoxiang.cultivation.client.renderer.QiOrbRenderer）
 * 仅修炼者（拥有灵气视野法术）可见，按元素颜色渲染，出生后 500 tick 开始淡出。
 */
public class QiOrbRenderer extends EntityRenderer<QiOrbEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("friday_cultivation", "textures/particle/qi.png");

    public QiOrbRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.0f;
    }

    @Override
    public boolean shouldRender(QiOrbEntity entity, Frustum cam, double camX, double camY, double camZ) {
        if (!QiOrbRenderer.isViewerCultivator()) {
            return false;
        }
        return super.shouldRender(entity, cam, camX, camY, camZ);
    }

    @Override
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
        if (tick > 500 && (a = 0.9f * (1.0f - (fadeProgress = Math.min(1.0f, (float) (tick - 500) / 100.0f)))) <= 0.01f) {
            return;
        }
        pose.pushPose();
        pose.mulPose(this.entityRenderDispatcher.cameraOrientation());
        float size = 0.18f;
        pose.scale(size, size, size);
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucent(TEXTURE));
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
    @Override
    public ResourceLocation getTextureLocation(@NotNull QiOrbEntity entity) {
        return TEXTURE;
    }

    private static boolean isViewerCultivator() {
        LocalPlayer viewer = Minecraft.getInstance().player;
        if (viewer == null) {
            return false;
        }
        return CultivationCapability.get(viewer).map(d -> d.isSpellEnabled(Spell.SPIRIT_VISION)).orElse(false);
    }
}
