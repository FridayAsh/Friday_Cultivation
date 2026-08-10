/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.model.HumanoidModel$ArmPose
 *  net.minecraft.client.model.PlayerModel
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.player.PlayerRenderer
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.PlayerModelPart
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.client.gui.overlay.IGuiOverlay
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package com.friday.cultivation.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="friday_cultivation", value={Dist.CLIENT})
public final class DharmaBodyClientEffects {
    private static final Map<Integer, ManifestationVisual> ACTIVE = new ConcurrentHashMap<Integer, ManifestationVisual>();
    private static final double MAX_RENDER_DISTANCE_SQR = 36864.0;
    private static long localEndMs = 0L;
    public static final IGuiOverlay OVERLAY = (gui, graphics, partialTick, screenWidth, screenHeight) -> DharmaBodyClientEffects.renderOverlay(graphics, screenWidth, screenHeight);

    private DharmaBodyClientEffects() {
    }

    public static void onSync(boolean active, int entityId, int durationTicks) {
        if (!active || durationTicks <= 0) {
            ACTIVE.remove(entityId);
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.getId() == entityId) {
                localEndMs = 0L;
            }
            return;
        }
        long endMs = System.currentTimeMillis() + (long)durationTicks * 50L;
        ACTIVE.put(entityId, new ManifestationVisual(endMs));
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getId() == entityId) {
            localEndMs = endMs;
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            ACTIVE.clear();
            localEndMs = 0L;
            return;
        }
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<Integer, ManifestationVisual>> it = ACTIVE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, ManifestationVisual> entry = it.next();
            if (now < entry.getValue().endMs() && mc.level.getEntity(entry.getKey().intValue()) != null) continue;
            it.remove();
        }
        if (now >= localEndMs) {
            localEndMs = 0L;
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        if (ACTIVE.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        long now = System.currentTimeMillis();
        Vec3 camera = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        float partial = event.getPartialTick();
        for (Map.Entry<Integer, ManifestationVisual> entry : ACTIVE.entrySet()) {
            Vec3 pos;
            AbstractClientPlayer player;
            Entity entity;
            if (now >= entry.getValue().endMs() || !((entity = mc.level.getEntity(entry.getKey().intValue())) instanceof AbstractClientPlayer) || (player = (AbstractClientPlayer)entity) == mc.player && mc.options.getCameraType().isFirstPerson() || (pos = DharmaBodyClientEffects.interpolatedPosition(player, partial)).distanceToSqr(camera) > 36864.0) continue;
            DharmaBodyClientEffects.renderManifestation(mc, pose, (MultiBufferSource)buffers, player, pos, camera, partial, now, entry.getValue().endMs());
        }
        buffers.endBatch();
    }

    private static void renderManifestation(Minecraft mc, PoseStack pose, MultiBufferSource buffers, AbstractClientPlayer player, Vec3 playerPos, Vec3 camera, float partial, long now, long endMs) {
        EntityRenderer renderer = mc.getEntityRenderDispatcher().getRenderer((Entity)player);
        if (!(renderer instanceof PlayerRenderer)) {
            return;
        }
        PlayerRenderer playerRenderer = (PlayerRenderer)renderer;
        float yRot = Mth.rotLerp((float)partial, (float)player.yBodyRotO, (float)player.yBodyRot);
        float xRot = Mth.lerp((float)partial, (float)player.xRotO, (float)player.getXRot());
        double yawRad = Math.toRadians(yRot);
        Vec3 behind = new Vec3(Math.sin(yawRad), 0.0, -Math.cos(yawRad)).scale(0.76);
        Vec3 pos = playerPos.add(behind).add(0.0, 0.04, 0.0);
        float remaining = Mth.clamp((float)((float)(endMs - now) / 30000.0f), (float)0.0f, (float)1.0f);
        float pulse = 0.9f + 0.1f * Mth.sin((float)(((float)player.tickCount + partial) * 0.18f));
        float alpha = (0.22f + 0.12f * pulse) * Mth.clamp((float)(remaining * 2.0f), (float)0.18f, (float)1.0f);
        PlayerModel model = (PlayerModel)playerRenderer.getModel();
        DharmaBodyClientEffects.configureModel((PlayerModel<AbstractClientPlayer>)model, player);
        pose.pushPose();
        pose.translate(pos.x - camera.x, pos.y - camera.y, pos.z - camera.z);
        pose.mulPose(Axis.YP.rotationDegrees(180.0f - yRot));
        pose.scale(-1.875f, -1.875f, 1.875f);
        pose.translate(0.0f, -1.501f, 0.0f);
        float age = (float)player.tickCount + partial;
        model.prepareMobModel((LivingEntity)player, 0.0f, 0.0f, partial);
        model.setupAnim(player, 0.0f, 0.0f, age, 0.0f, xRot);
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityTranslucent((ResourceLocation)player.getSkinTextureLocation()));
        model.renderToBuffer(pose, buffer, 0xF000F0, OverlayTexture.NO_OVERLAY, 0.78f, 0.92f, 1.0f, alpha);
        pose.popPose();
    }

    private static Vec3 interpolatedPosition(AbstractClientPlayer player, float partial) {
        double x = Mth.lerp((double)partial, (double)player.xOld, (double)player.getX());
        double y = Mth.lerp((double)partial, (double)player.yOld, (double)player.getY());
        double z = Mth.lerp((double)partial, (double)player.zOld, (double)player.getZ());
        return new Vec3(x, y, z);
    }

    private static void configureModel(PlayerModel<AbstractClientPlayer> model, AbstractClientPlayer player) {
        model.setAllVisible(true);
        model.hat.visible = player.isModelPartShown(PlayerModelPart.HAT);
        model.jacket.visible = player.isModelPartShown(PlayerModelPart.JACKET);
        model.leftPants.visible = player.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
        model.rightPants.visible = player.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
        model.leftSleeve.visible = player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
        model.rightSleeve.visible = player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
        model.crouching = false;
        model.riding = false;
        model.young = false;
        model.attackTime = 0.0f;
        model.rightArmPose = HumanoidModel.ArmPose.EMPTY;
        model.leftArmPose = HumanoidModel.ArmPose.EMPTY;
    }

    private static void renderOverlay(GuiGraphics graphics, int screenWidth, int screenHeight) {
        long now = System.currentTimeMillis();
        if (now >= localEndMs) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) {
            return;
        }
        int remainSec = (int)Math.ceil((double)(localEndMs - now) / 1000.0);
        MutableComponent text = Component.translatable((String)"hud.friday_cultivation.dharma_body_manifestation.remaining", (Object[])new Object[]{remainSec}).withStyle(ChatFormatting.GOLD);
        Font font = mc.font;
        int x = (screenWidth - font.width((FormattedText)text)) / 2;
        int y = screenHeight - 78;
        graphics.drawString(font, (Component)text, x, y, -11654, true);
    }

    private record ManifestationVisual(long endMs) {
    }
}

