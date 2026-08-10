/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  org.jetbrains.annotations.NotNull
 */
package com.friday.cultivation.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.friday.cultivation.client.model.PalmThunderModel;
import com.friday.cultivation.entity.PalmThunderOrbEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class PalmThunderOrbRenderer
extends EntityRenderer<PalmThunderOrbEntity> {
    private static final ResourceLocation WHITE_TEXTURE = new ResourceLocation("textures/misc/white.png");

    public PalmThunderOrbRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.0f;
    }

    public void render(@NotNull PalmThunderOrbEntity entity, float yaw, float partialTick, @NotNull PoseStack pose, @NotNull MultiBufferSource buffers, int packedLight) {
        pose.pushPose();
        PalmThunderModel.renderOrb(pose, buffers, (float)entity.tickCount + partialTick, 0.42f, 0.96f);
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buffers, packedLight);
    }

    @NotNull
    public ResourceLocation getTextureLocation(@NotNull PalmThunderOrbEntity entity) {
        return WHITE_TEXTURE;
    }
}

