package com.friday.cultivation.client.renderer;

import com.friday.cultivation.entity.npc.CorpseEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
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
import org.jetbrains.annotations.NotNull;

/**
 * 尸骸渲染器 - 严格 1:1 复刻原模组 com.xiaoxiang.cultivation.client.renderer.CorpseRenderer
 */
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

    @Override
    public void render(@NotNull CorpseEntity corpse, float entityYaw, float partial, @NotNull PoseStack pose, @NotNull MultiBufferSource buffers, int light) {
        ResourceLocation skin = this.resolveSkin(corpse);
        this.configureModel();
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(180.0f - corpse.getYRot()));
        pose.mulPose(Axis.XP.rotationDegrees(90.0f));
        pose.translate(0.0, (double) CORPSE_BODY_CENTER, (double) CORPSE_GROUND_LIFT);
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
        this.model.head.setPos(0.0f, 0.0f, 0.0f);
        this.model.body.setPos(0.0f, 0.0f, 0.0f);
        this.model.rightArm.setPos(0.0f, 0.0f, 0.05f);
        this.model.leftArm.setPos(0.0f, 0.0f, -0.05f);
        this.model.rightLeg.setPos(0.0f, 0.0f, 0.0f);
        this.model.leftLeg.setPos(0.0f, 0.0f, 0.0f);
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
        return DefaultPlayerSkin.getDefaultSkin(id != null ? id : Util.NIL_UUID);
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull CorpseEntity corpse) {
        return this.resolveSkin(corpse);
    }
}
