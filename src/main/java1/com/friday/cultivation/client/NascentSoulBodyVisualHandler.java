package com.friday.cultivation.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "friday_cultivation", value = Dist.CLIENT)
public final class NascentSoulBodyVisualHandler {
    private static final Map<Integer, BodyVisual> BODIES = new ConcurrentHashMap<Integer, BodyVisual>();
    private static final double MAX_RENDER_DISTANCE_SQR = 36864.0;

    private NascentSoulBodyVisualHandler() {
    }

    public static void onBodySync(boolean active, int playerId, double x, double y, double z, float yRot, float xRot, int durationTicks) {
        if (!active || durationTicks <= 0) {
            BODIES.remove(playerId);
            return;
        }
        long now = System.currentTimeMillis();
        BODIES.put(playerId, new BodyVisual(new Vec3(x, y, z), yRot, xRot, now + (long) durationTicks * 50L));
    }

    public static Optional<BodyAnchor> bodyAnchorFor(int playerId) {
        BodyVisual visual = BODIES.get(playerId);
        if (visual == null || System.currentTimeMillis() >= visual.expiresAtMs()) {
            return Optional.empty();
        }
        return Optional.of(new BodyAnchor(visual.pos(), visual.yRot(), visual.xRot()));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            BODIES.clear();
            return;
        }
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<Integer, BodyVisual>> it = BODIES.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, BodyVisual> entry = it.next();
            if (now < entry.getValue().expiresAtMs() && mc.level.getEntity(entry.getKey()) != null) continue;
            it.remove();
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        if (BODIES.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        Vec3 camera = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        float partial = event.getPartialTick();
        for (Map.Entry<Integer, BodyVisual> entry : BODIES.entrySet()) {
            Entity entity = mc.level.getEntity(entry.getKey());
            if (!(entity instanceof AbstractClientPlayer)) continue;
            AbstractClientPlayer player = (AbstractClientPlayer) entity;
            BodyVisual visual = entry.getValue();
            if (visual.pos().distanceToSqr(camera) <= 36864.0) {
                NascentSoulBodyVisualHandler.renderBodyProjection(mc, pose, buffers, player, visual, camera, partial);
            }
            NascentSoulBodyVisualHandler.renderSoulProjection(mc, pose, buffers, player, camera, partial);
        }
        buffers.endBatch();
    }

    private static void renderBodyProjection(Minecraft mc, PoseStack pose, MultiBufferSource buffers, AbstractClientPlayer player, BodyVisual visual, Vec3 camera, float partial) {
        NascentSoulBodyVisualHandler.renderPlayerProjection(mc, pose, buffers, player, visual.pos(), visual.yRot(), visual.xRot(), camera, partial, 1.0f, 1.0f, 1.0f, 0.95f);
    }

    private static void renderSoulProjection(Minecraft mc, PoseStack pose, MultiBufferSource buffers, AbstractClientPlayer player, Vec3 camera, float partial) {
        if (player == mc.player && mc.options.getCameraType().isFirstPerson()) {
            return;
        }
        Vec3 pos = NascentSoulBodyVisualHandler.interpolatedPosition(player, partial);
        if (pos.distanceToSqr(camera) > 36864.0) {
            return;
        }
        float yRot = Mth.rotLerp(partial, player.yRotO, player.getYRot());
        float xRot = Mth.lerp(partial, player.xRotO, player.getXRot());
        NascentSoulBodyVisualHandler.renderPlayerProjection(mc, pose, buffers, player, pos, yRot, xRot, camera, partial, 0.68f, 0.96f, 1.0f, 0.46f);
    }

    private static Vec3 interpolatedPosition(AbstractClientPlayer player, float partial) {
        double x = Mth.lerp((double) partial, player.xo, player.getX());
        double y = Mth.lerp((double) partial, player.yo, player.getY());
        double z = Mth.lerp((double) partial, player.zo, player.getZ());
        return new Vec3(x, y, z);
    }

    private static void renderPlayerProjection(Minecraft mc, PoseStack pose, MultiBufferSource buffers, AbstractClientPlayer player, Vec3 pos, float yRot, float xRot, Vec3 camera, float partial, float red, float green, float blue, float alpha) {
        EntityRenderer renderer = mc.getEntityRenderDispatcher().getRenderer(player);
        if (!(renderer instanceof PlayerRenderer)) {
            return;
        }
        PlayerRenderer playerRenderer = (PlayerRenderer) renderer;
        PlayerModel model = (PlayerModel) playerRenderer.getModel();
        NascentSoulBodyVisualHandler.configureBodyModel(model, player);
        pose.pushPose();
        pose.translate(pos.x - camera.x, pos.y - camera.y, pos.z - camera.z);
        pose.mulPose(Axis.YP.rotationDegrees(180.0f - yRot));
        pose.scale(-1.0f, -1.0f, 1.0f);
        pose.scale(0.9375f, 0.9375f, 0.9375f);
        pose.translate(0.0f, -1.501f, 0.0f);
        float age = (float) player.tickCount + partial;
        model.prepareMobModel(player, 0.0f, 0.0f, partial);
        model.setupAnim(player, 0.0f, 0.0f, age, 0.0f, xRot);
        VertexConsumer buffer = alpha >= 0.99f ? buffers.getBuffer(model.renderType(player.getSkinTextureLocation())) : buffers.getBuffer(RenderType.entityTranslucent(player.getSkinTextureLocation()));
        int light = LevelRenderer.getLightColor(mc.level, BlockPos.containing(pos.x, pos.y + 1.0, pos.z));
        model.renderToBuffer(pose, buffer, light, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
        pose.popPose();
    }

    private static void configureBodyModel(PlayerModel<AbstractClientPlayer> model, AbstractClientPlayer player) {
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

    private record BodyVisual(Vec3 pos, float yRot, float xRot, long expiresAtMs) {
    }

    public record BodyAnchor(Vec3 pos, float yRot, float xRot) {
    }
}
