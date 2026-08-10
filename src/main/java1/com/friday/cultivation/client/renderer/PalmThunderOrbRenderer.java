package com.friday.cultivation.client.renderer;

import com.friday.cultivation.client.model.PalmThunderModel;
import com.friday.cultivation.entity.spell.PalmThunderOrbEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * 掌心雷球渲染器 - 严格 1:1 复刻原模组 com.xiaoxiang.cultivation.client.renderer.PalmThunderOrbRenderer
 */
public class PalmThunderOrbRenderer
extends EntityRenderer<PalmThunderOrbEntity> {
    private static final ResourceLocation WHITE_TEXTURE = new ResourceLocation("textures/misc/white.png");

    public PalmThunderOrbRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.0f;
    }

    @Override
    public void render(@NotNull PalmThunderOrbEntity entity, float yaw, float partialTick, @NotNull PoseStack pose, @NotNull MultiBufferSource buffers, int packedLight) {
        pose.pushPose();
        PalmThunderModel.renderOrb(pose, buffers, (float) entity.tickCount + partialTick, 0.42f, 0.96f);
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buffers, packedLight);
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull PalmThunderOrbEntity entity) {
        return WHITE_TEXTURE;
    }
}
