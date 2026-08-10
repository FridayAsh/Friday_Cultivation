/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.HumanoidModel
 *  net.minecraft.client.model.geom.ModelLayers
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.entity.HumanoidMobRenderer
 *  net.minecraft.resources.ResourceLocation
 *  org.jetbrains.annotations.NotNull
 */
package com.friday.cultivation.client.renderer;

import com.friday.cultivation.entity.npc.SoulReaperEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class SoulReaperRenderer
extends HumanoidMobRenderer<SoulReaperEntity, HumanoidModel<SoulReaperEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("friday_cultivation", "textures/entity/soul_reaper.png");

    public SoulReaperRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new HumanoidModel(ctx.bakeLayer(ModelLayers.PLAYER)), 0.5f);
    }

    @NotNull
    public ResourceLocation getTextureLocation(@NotNull SoulReaperEntity entity) {
        return TEXTURE;
    }
}

