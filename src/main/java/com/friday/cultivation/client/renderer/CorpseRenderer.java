/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.Util
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.model.HumanoidModel$ArmPose
 *  net.minecraft.client.model.PlayerModel
 *  net.minecraft.client.model.geom.ModelLayers
 *  net.minecraft.client.multiplayer.PlayerInfo
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.client.resources.DefaultPlayerSkin
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  org.jetbrains.annotations.NotNull
 */
package com.friday.cultivation.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.friday.cultivation.entity.npc.CorpseEntity;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class CorpseRenderer
extends EntityRenderer<CorpseEntity> {
    private static final ResourceLocation DIFU_SKIN = new ResourceLocation("friday_cultivation", "textures/entity/soul_reaper.png");
    private static final float CORPSE_BODY_CENTER = -0.85f;
    private static final float CORPSE_GROUND_LIFT = -0.15f;
    private final PlayerModel<AbstractClientPlayer> model;

    public CorpseRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.model = new PlayerModel(ctx.bakeLayer(ModelLayers.PLAYER), false);
    }

    public void render(@NotNull CorpseEntity corpse, float entityYaw, float partial, @NotNull PoseStack pose, @NotNull MultiBufferSource buffers, int light) {
        ResourceLocation skin = this.resolveSkin(corpse);
        this.configureModel();
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(180.0f - corpse.getYRot()));
        pose.mulPose(Axis.XP.rotationDegrees(90.0f));
        pose.translate(0.0, (double)-0.85f, (double)-0.15f);
        pose.scale(-1.0f, -1.0f, 1.0f);
        pose.translate(0.0, -1.501, 0.0);
        pose.scale(0.94f, 0.94f, 0.94f);
        VertexConsumer vc = buffers.getBuffer(this.model.renderType(skin));
        this.model.renderToBuffer(pose, vc, light, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
        pose.popPose();
        super.render(corpse, entityYaw, partial, pose, buffers, light);
    }

    private void configureModel() {
        this.model.setAllVisible(true);
        this.model.crouching = false;
        this.model.young = false;
        this.model.riding = false;
        this.model.attackTime = 0.0f;
        this.model.rightArmPose = HumanoidModel.ArmPose.EMPTY;
        this.model.leftArmPose = HumanoidModel.ArmPose.EMPTY;
        this.model.head.setRotation(0.0f, 0.0f, 0.0f);
        this.model.body.setRotation(0.0f, 0.0f, 0.0f);
        this.model.rightArm.setRotation(0.0f, 0.0f, 0.05f);
        this.model.leftArm.setRotation(0.0f, 0.0f, -0.05f);
        this.model.rightLeg.setRotation(0.0f, 0.0f, 0.0f);
        this.model.leftLeg.setRotation(0.0f, 0.0f, 0.0f);
        this.model.hat.copyFrom(this.model.head);
    }

    private ResourceLocation resolveSkin(CorpseEntity corpse) {
        PlayerInfo info;
        if (corpse.isNpcCorpse()) {
            if (corpse.isNpcDifuReaperCorpse()) {
                return DIFU_SKIN;
            }
            int variant = Math.floorMod(corpse.getNpcSkinVariant(), 48);
            return new ResourceLocation("friday_cultivation", "textures/entity/wandering_cultivator_" + variant + ".png");
        }
        UUID id = corpse.getOwnerUuid();
        if (id != null && Minecraft.getInstance().getConnection() != null && (info = Minecraft.getInstance().getConnection().getPlayerInfo(id)) != null) {
            return info.getSkinLocation();
        }
        return DefaultPlayerSkin.getDefaultSkin((UUID)(id != null ? id : Util.NIL_UUID));
    }

    @NotNull
    public ResourceLocation getTextureLocation(@NotNull CorpseEntity corpse) {
        return this.resolveSkin(corpse);
    }
}

