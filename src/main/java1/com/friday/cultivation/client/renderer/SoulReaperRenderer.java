package com.friday.cultivation.client.renderer;

import com.friday.cultivation.entity.npc.SoulReaperEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * 灵魂收割者渲染器 - 严格 1:1 复刻原模组 com.xiaoxiang.cultivation.client.renderer.SoulReaperRenderer
 */
public class SoulReaperRenderer
extends HumanoidMobRenderer<SoulReaperEntity, HumanoidModel<SoulReaperEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("friday_cultivation", "textures/entity/soul_reaper.png");

    public SoulReaperRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new HumanoidModel(ctx.bakeLayer(ModelLayers.PLAYER)), 0.5f);
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull SoulReaperEntity entity) {
        return TEXTURE;
    }
}
