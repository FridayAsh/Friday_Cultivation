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
import com.friday.cultivation.client.renderer.FireballRenderHelper;
import com.friday.cultivation.entity.GreatFireballEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class GreatFireballRenderer
extends EntityRenderer<GreatFireballEntity> {
    public GreatFireballRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    public void render(@NotNull GreatFireballEntity entity, float yaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buf, int light) {
        float diameter = entity.getRenderScale();
        float age = (float)entity.tickCount + partialTick;
        FireballRenderHelper.render(poseStack, buf, diameter, age);
        super.render(entity, yaw, partialTick, poseStack, buf, light);
    }

    @NotNull
    public ResourceLocation getTextureLocation(@NotNull GreatFireballEntity entity) {
        return FireballRenderHelper.TEXTURE;
    }
}

