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

@Mod.EventBusSubscriber(modid = "friday_cultivation", value = Dist.CLIENT)
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

    public static void onBodySync(boolean active, int entityId, double x, double y, double z, float yaw, float pitch, int durationTicks) {
        DharmaBodyClientEffects.onSync(active, entityId, durationTicks);
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
            if (now < entry.getValue().endMs() && mc.level.getEntity(entry.getKey()) != null) continue;
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
            Entity entity = mc.level.getEntity(entry.getKey());
            if (now >= entry.getValue().endMs() || !(entity instanceof AbstractClientPlayer)) continue;
            AbstractClientPlayer player = (AbstractClientPlayer) entity;
            if (player == mc.player && mc.options.getCameraType().isFirstPerson()) continue;
            Vec3 pos = DharmaBodyClientEffects.interpolatedPosition(player, partial);
            if (pos.distanceToSqr(camera) > 36864.0) continue;
            DharmaBodyClientEffects.renderManifestation(mc, pose, buffers, player, pos, camera, partial, now, entry.getValue().endMs());
        }
        buffers.endBatch();
    }

    private static void renderManifestation(Minecraft mc, PoseStack pose, MultiBufferSource buffers, AbstractClientPlayer player, Vec3 playerPos, Vec3 camera, float partial, long now, long endMs) {
        EntityRenderer renderer = mc.getEntityRenderDispatcher().getRenderer(player);
        if (!(renderer instanceof PlayerRenderer)) {
            return;
        }
        PlayerRenderer playerRenderer = (PlayerRenderer) renderer;
        float yRot = Mth.rotLerp(partial, player.yHeadRotO, player.yHeadRot);
        float xRot = Mth.lerp(partial, player.xRotO, player.getXRot());
        double yawRad = Math.toRadians(yRot);
        Vec3 behind = new Vec3(Math.sin(yawRad), 0.0, -Math.cos(yawRad)).scale(0.76);
        Vec3 pos = playerPos.add(behind).add(0.0, 0.04, 0.0);
        float remaining = Mth.clamp((float)(endMs - now) / 30000.0f, 0.0f, 1.0f);
        float pulse = 0.9f + 0.1f * Mth.sin(((float)player.tickCount + partial) * 0.18f);
        float alpha = (0.22f + 0.12f * pulse) * Mth.clamp(remaining * 2.0f, 0.18f, 1.0f);
        PlayerModel model = (PlayerModel) playerRenderer.getModel();
        DharmaBodyClientEffects.configureModel((PlayerModel<AbstractClientPlayer>) model, player);
        pose.pushPose();
        pose.translate(pos.x - camera.x, pos.y - camera.y, pos.z - camera.z);
        pose.mulPose(Axis.YP.rotationDegrees(180.0f - yRot));
        pose.scale(-1.875f, -1.875f, 1.875f);
        pose.translate(0.0f, -1.501f, 0.0f);
        float age = (float)player.tickCount + partial;
        model.prepareMobModel(player, 0.0f, 0.0f, partial);
        model.setupAnim(player, 0.0f, 0.0f, age, 0.0f, xRot);
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityTranslucent(player.getSkinTextureLocation()));
        model.renderToBuffer(pose, buffer, 0xF000F0, OverlayTexture.NO_OVERLAY, 0.78f, 0.92f, 1.0f, alpha);
        pose.popPose();
    }

    private static Vec3 interpolatedPosition(AbstractClientPlayer player, float partial) {
        double x = Mth.lerp((double)partial, player.xo, player.getX());
        double y = Mth.lerp((double)partial, player.yo, player.getY());
        double z = Mth.lerp((double)partial, player.zo, player.getZ());
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
        model.swimAmount = 0.0f;
        model.leftArmPose = HumanoidModel.ArmPose.EMPTY;
        model.rightArmPose = HumanoidModel.ArmPose.EMPTY;
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
        MutableComponent text = Component.translatable("hud.friday_cultivation.dharma_body_manifestation.remaining", remainSec).withStyle(ChatFormatting.GOLD);
        Font font = mc.font;
        int x = (screenWidth - font.width(text)) / 2;
        int y = screenHeight - 78;
        graphics.drawString(font, text, x, y, -11654, true);
    }

    private record ManifestationVisual(long endMs) {
    }
}
