package com.friday.cultivation.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.spell.Spell;
import com.friday.cultivation.entity.npc.SoulReaperEntity;
import com.friday.cultivation.registry.ModDimensions;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/**
 * 灵魂视觉处理器 - 灵魂态玩家/散修的灵魂投影渲染。
 * 完全照搬原 mod: xiaoxiang.cultivation.client.SoulVisualHandler
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SoulVisualHandler {
    private static final double MAX_RENDER_DISTANCE_SQR = 36864.0;
    private static final float LOWER_FADE_START_Y = 0.72f;
    private static final float LOWER_FADE_END_Y = 1.52f;
    private static final float LOWER_FADE_MIN_MULTIPLIER = 0.08f;
    private static volatile boolean guiRender = false;
    private static boolean localSoulPhaseApplied = false;
    private static boolean localPrevNoGravity = false;
    private static boolean localPrevNoPhysics = false;

    private SoulVisualHandler() {
    }

    @SubscribeEvent
    public static void onScreenRenderPre(ScreenEvent.Render.Pre event) {
        guiRender = true;
    }

    @SubscribeEvent
    public static void onScreenRenderPost(ScreenEvent.Render.Post event) {
        guiRender = false;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        SoulVisualHandler.tickLocalSoulPhase();
    }

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        if (guiRender && Minecraft.getInstance().screen != null) {
            return;
        }
        if (ClientSoulRegistry.isSoul(event.getEntity().getUUID())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        if (event.getEntity() instanceof SoulReaperEntity && !SoulVisibilityClient.localCanSeeSouls()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        if (!SoulVisibilityClient.localCanSeeSouls()) {
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
        for (UUID id : ClientSoulRegistry.all()) {
            AbstractClientPlayer player;
            Player pl = mc.level.getPlayerByUUID(id);
            if (!(pl instanceof AbstractClientPlayer) || (player = (AbstractClientPlayer) pl) == mc.player && mc.options.getCameraType().isFirstPerson()) continue;
            SoulVisualHandler.renderSoulProjection(mc, pose, buffers, player, camera, partial);
        }
        buffers.endBatch();
    }

    private static void renderSoulProjection(Minecraft mc, PoseStack pose, MultiBufferSource buffers, AbstractClientPlayer player, Vec3 camera, float partial) {
        Vec3 pos = new Vec3(Mth.lerp((double) partial, (double) player.xo, (double) player.getX()), Mth.lerp((double) partial, (double) player.yo, (double) player.getY()), Mth.lerp((double) partial, (double) player.zo, (double) player.getZ()));
        if (pos.distanceToSqr(camera) > 36864.0) {
            return;
        }
        float yRot = Mth.rotLerp((float) partial, (float) player.yBodyRotO, (float) player.getYRot());
        float xRot = Mth.lerp((float) partial, (float) player.xRotO, (float) player.getXRot());
        EntityRenderer renderer = mc.getEntityRenderDispatcher().getRenderer((Entity) player);
        if (!(renderer instanceof PlayerRenderer)) {
            return;
        }
        PlayerRenderer playerRenderer = (PlayerRenderer) renderer;
        PlayerModel model = (PlayerModel) playerRenderer.getModel();
        SoulVisualHandler.configureModel((PlayerModel<AbstractClientPlayer>) model, player);
        pose.pushPose();
        pose.translate(pos.x - camera.x, pos.y - camera.y, pos.z - camera.z);
        pose.mulPose(Axis.YP.rotationDegrees(180.0f - yRot));
        pose.scale(-1.0f, -1.0f, 1.0f);
        pose.scale(0.9375f, 0.9375f, 0.9375f);
        pose.translate(0.0f, -1.501f, 0.0f);
        float age = (float) player.tickCount + partial;
        model.prepareMobModel((LivingEntity) player, 0.0f, 0.0f, partial);
        model.setupAnim((LivingEntity) player, 0.0f, 0.0f, age, 0.0f, xRot);
        SoulFadeVertexConsumer buffer = new SoulFadeVertexConsumer(buffers.getBuffer(RenderType.entityTranslucent((ResourceLocation) player.getSkinTextureLocation())));
        int light = LevelRenderer.getLightColor((BlockAndTintGetter) mc.level, BlockPos.containing((double) pos.x, (double) (pos.y + 1.0), (double) pos.z));
        model.renderToBuffer(pose, buffer, light, OverlayTexture.NO_OVERLAY, 0.62f, 0.8f, 1.0f, 0.42f);
        pose.popPose();
    }

    private static void tickLocalSoulPhase() {
        boolean inDifu;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            SoulVisualHandler.resetLocalSoulPhaseSnapshot();
            return;
        }
        CultivationData data = CultivationCapability.get((Player) player).orElse(null);
        boolean soul = data != null && data.isSoulState();
        boolean ghostFlightPhase = soul && data.isSpellEnabled(Spell.GHOST_FLIGHT) && player.getAbilities().flying;
        boolean bl = inDifu = player.level().dimension() == ModDimensions.DIFU;
        if (ghostFlightPhase && !inDifu) {
            SoulVisualHandler.applyLocalSoulPhase(player);
            return;
        }
        SoulVisualHandler.restoreLocalSoulPhase(player);
        if (soul && inDifu && !player.isSpectator() && !player.isCreative()) {
            player.noPhysics = false;
            player.setNoGravity(false);
            player.fallDistance = 0.0f;
        }
    }

    private static void applyLocalSoulPhase(LocalPlayer player) {
        if (player.isSpectator() || player.isCreative()) {
            SoulVisualHandler.restoreLocalSoulPhase(player);
            return;
        }
        if (!localSoulPhaseApplied) {
            localSoulPhaseApplied = true;
            localPrevNoGravity = player.isNoGravity();
            localPrevNoPhysics = player.noPhysics;
        }
        player.noPhysics = true;
        player.setNoGravity(true);
        player.fallDistance = 0.0f;
    }

    private static void restoreLocalSoulPhase(LocalPlayer player) {
        if (!localSoulPhaseApplied) {
            return;
        }
        localSoulPhaseApplied = false;
        player.fallDistance = 0.0f;
        if (player.isCreative()) {
            player.noPhysics = true;
            return;
        }
        player.noPhysics = localPrevNoPhysics;
        player.setNoGravity(localPrevNoGravity);
    }

    private static void resetLocalSoulPhaseSnapshot() {
        localSoulPhaseApplied = false;
        localPrevNoGravity = false;
        localPrevNoPhysics = false;
    }

    private static float lowerBodyAlphaMultiplier(float modelY) {
        if (modelY <= 0.72f) {
            return 1.0f;
        }
        if (modelY >= 1.52f) {
            return 0.08f;
        }
        float t = (modelY - 0.72f) / 0.79999995f;
        return Mth.lerp((float) t, (float) 1.0f, (float) 0.08f);
    }

    private static void configureModel(PlayerModel<AbstractClientPlayer> model, AbstractClientPlayer player) {
        model.setAllVisible(true);
        model.hat.visible = player.isModelPartShown(PlayerModelPart.HAT);
        model.jacket.visible = player.isModelPartShown(PlayerModelPart.JACKET);
        model.leftPants.visible = player.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
        model.rightPants.visible = player.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
        model.leftSleeve.visible = player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
        model.rightSleeve.visible = player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
        model.crouching = player.isCrouching();
        model.riding = false;
        model.young = false;
        model.attackTime = 0.0f;
        model.rightArmPose = HumanoidModel.ArmPose.EMPTY;
        model.leftArmPose = HumanoidModel.ArmPose.EMPTY;
    }

    private static final class SoulFadeVertexConsumer
    implements VertexConsumer {
        private final VertexConsumer parent;
        private float modelY = 0.0f;

        private SoulFadeVertexConsumer(VertexConsumer parent) {
            this.parent = parent;
        }

        public VertexConsumer vertex(double x, double y, double z) {
            this.modelY = (float) y;
            this.parent.vertex(x, y, z);
            return this;
        }

        public VertexConsumer vertex(Matrix4f matrix, float x, float y, float z) {
            this.modelY = y;
            this.parent.vertex(matrix, x, y, z);
            return this;
        }

        public VertexConsumer color(int r, int g, int b, int a) {
            this.parent.color(r, g, b, Math.round((float) a * SoulVisualHandler.lowerBodyAlphaMultiplier(this.modelY)));
            return this;
        }

        public VertexConsumer uv(float u, float v) {
            this.parent.uv(u, v);
            return this;
        }

        public VertexConsumer overlayCoords(int u, int v) {
            this.parent.overlayCoords(u, v);
            return this;
        }

        public VertexConsumer uv2(int u, int v) {
            this.parent.uv2(u, v);
            return this;
        }

        public VertexConsumer normal(float x, float y, float z) {
            this.parent.normal(x, y, z);
            return this;
        }

        public void endVertex() {
            this.parent.endVertex();
        }

        public void defaultColor(int r, int g, int b, int a) {
            this.parent.defaultColor(r, g, b, a);
        }

        public void unsetDefaultColor() {
            this.parent.unsetDefaultColor();
        }
    }
}
