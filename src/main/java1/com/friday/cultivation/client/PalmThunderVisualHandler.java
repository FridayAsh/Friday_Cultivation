package com.friday.cultivation.client;

import com.friday.cultivation.client.model.PalmThunderModel;
import com.friday.cultivation.registry.ModEffects;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 掌雷（掌心雷）客户端视觉处理器（服务端→客户端接收）— 严格复刻自原模组
 * com.xiaoxiang.cultivation.client.PalmThunderVisualHandler
 * <p>
 * 2 个 mode 同步：
 * <ul>
 *   <li>CHANNEL (0) — 蓄力球 (on entity, 用 hasEffect(PALM_THUNDER_STUN) 同步消除)</li>
 *   <li>BURST (1) — 爆发点</li>
 * </ul>
 * <p>
 * 事件订阅：ClientTickEvent (清理) + RenderLevelStageEvent (球+爆发) +
 * RenderLivingEvent.Post (缠绕目标)
 * <p>
 * 跨维度清理：currentDimension ResourceKey 比较，跨维度时 clear()
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation", value = Dist.CLIENT)
public final class PalmThunderVisualHandler {

    private static final Map<Integer, ChannelVisual> CHANNELS = new ConcurrentHashMap<>();
    private static final Map<Integer, Burst> BURSTS = new ConcurrentHashMap<>();
    private static final AtomicInteger NEXT_BURST_ID = new AtomicInteger();
    private static ResourceKey<Level> currentDimension;

    private PalmThunderVisualHandler() {
    }

    public static void onSync(int mode, int entityId, int durationTicks, boolean active, double x, double y, double z, float radius, float progress, boolean armed) {
        long endMs = System.currentTimeMillis() + (long) Math.max(1, durationTicks) * 50L;
        if (mode == 0) {
            if (active) {
                CHANNELS.put(entityId, new ChannelVisual(endMs, Mth.clamp(progress, 0.0f, 1.0f), armed));
            } else {
                CHANNELS.remove(entityId);
            }
            return;
        }
        if (mode == 1) {
            int id = NEXT_BURST_ID.incrementAndGet();
            BURSTS.put(id, new Burst(new Vec3(x, y, z), Math.max(0.5f, radius), System.currentTimeMillis(), endMs));
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            clear();
            return;
        }
        ResourceKey<Level> dimension = mc.level.dimension();
        if (currentDimension == null) {
            currentDimension = dimension;
        } else if (!currentDimension.equals(dimension)) {
            clear();
            currentDimension = dimension;
            return;
        }
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        long now = System.currentTimeMillis();
        CHANNELS.entrySet().removeIf(entry -> entry.getValue().endMs <= now);
        BURSTS.entrySet().removeIf(entry -> entry.getValue().endMs <= now);
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        if (CHANNELS.isEmpty() && BURSTS.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        float age = (float) mc.level.getGameTime() + event.getPartialTick();
        long now = System.currentTimeMillis();
        pose.pushPose();
        pose.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        Iterator<Map.Entry<Integer, ChannelVisual>> channelIt = CHANNELS.entrySet().iterator();
        while (channelIt.hasNext()) {
            Map.Entry<Integer, ChannelVisual> entry = channelIt.next();
            ChannelVisual visual = entry.getValue();
            if (visual.endMs <= now) {
                channelIt.remove();
                continue;
            }
            Entity entity = mc.level.getEntity(entry.getKey().intValue());
            if (!(entity instanceof LivingEntity living)) {
                channelIt.remove();
                continue;
            }
            if (mc.player.distanceToSqr(living) > 16384.0) continue;
            Vec3 center = handPosition(mc, camera, living, event.getPartialTick());
            float growth = smooth(visual.progress);
            float radius = visual.armed ? 0.34f : Mth.lerp(growth, 0.055f, 0.34f);
            float alpha = alpha(visual.endMs) * (visual.armed ? 1.0f : Mth.lerp(growth, 0.42f, 1.0f));
            pose.pushPose();
            pose.translate(center.x, center.y, center.z);
            PalmThunderModel.renderOrb(pose, buffers, age, radius, alpha);
            pose.popPose();
        }
        Iterator<Map.Entry<Integer, Burst>> burstIt = BURSTS.entrySet().iterator();
        while (burstIt.hasNext()) {
            Burst burst = burstIt.next().getValue();
            if (burst.endMs <= now) {
                burstIt.remove();
                continue;
            }
            if (mc.player.position().distanceToSqr(burst.center) > 25600.0) continue;
            float progress = (float) ((double) (now - burst.startMs) / Math.max(1.0, (double) (burst.endMs - burst.startMs)));
            pose.pushPose();
            pose.translate(burst.center.x, burst.center.y, burst.center.z);
            PalmThunderModel.renderBurst(pose, buffers, age, burst.radius, progress, 0.92f * (1.0f - Math.max(0.0f, progress - 0.72f) / 0.28f));
            pose.popPose();
        }
        pose.popPose();
        buffers.endBatch(PalmThunderModel.renderType());
    }

    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Post<?, ?> event) {
        LivingEntity entity = event.getEntity();
        if (!entity.hasEffect((MobEffect) ModEffects.PALM_THUNDER_STUN.get())) {
            return;
        }
        float age = (float) entity.tickCount + event.getPartialTick();
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        PalmThunderModel.renderWrappedEntity(pose, event.getMultiBufferSource(), (float) entity.getBbWidth(), (float) entity.getBbHeight(), age, 0.92f);
        pose.popPose();
    }

    private static Vec3 handPosition(Minecraft mc, Camera camera, LivingEntity entity, float partialTick) {
        if (entity == mc.player && mc.options.getCameraType().isFirstPerson()) {
            Vec3 forward = toVec3(camera.getLookVector()).normalize();
            Vec3 up = toVec3(camera.getUpVector()).normalize();
            Vec3 left = toVec3(camera.getLeftVector()).normalize();
            return camera.getPosition().add(forward.scale(0.68)).add(left.scale(-0.32)).subtract(up.scale(0.24));
        }
        Vec3 look = entity.getViewVector(1.0f);
        if (look.lengthSqr() < 1.0E-6) {
            look = new Vec3(0.0, 0.0, 1.0);
        }
        look = look.normalize();
        Vec3 right = new Vec3(-look.z, 0.0, look.x);
        if (right.lengthSqr() < 1.0E-6) {
            right = new Vec3(1.0, 0.0, 0.0);
        }
        right = right.normalize();
        return entity.getEyePosition(partialTick).add(look.scale(0.74)).add(right.scale(0.27)).add(0.0, -0.22, 0.0);
    }

    private static Vec3 toVec3(Vector3f vector) {
        return new Vec3(vector.x(), vector.y(), vector.z());
    }

    private static float alpha(long endMs) {
        long remaining = endMs - System.currentTimeMillis();
        if (remaining <= 0L) {
            return 0.0f;
        }
        if (remaining < 240L) {
            return Math.max(0.0f, (float) remaining / 240.0f) * 0.92f;
        }
        return 0.92f;
    }

    private static float smooth(float value) {
        float clamped = Mth.clamp(value, 0.0f, 1.0f);
        return clamped * clamped * (3.0f - 2.0f * clamped);
    }

    private static void clear() {
        CHANNELS.clear();
        BURSTS.clear();
        currentDimension = null;
    }

    /** 蓄力球 visual (endMs, progress, armed) */
    public static final class ChannelVisual {
        public final long endMs;
        public final float progress;
        public final boolean armed;

        public ChannelVisual(long endMs, float progress, boolean armed) {
            this.endMs = endMs;
            this.progress = progress;
            this.armed = armed;
        }
    }

    /** 爆发点 visual (center, radius, startMs, endMs) */
    public static final class Burst {
        public final Vec3 center;
        public final float radius;
        public final long startMs;
        public final long endMs;

        public Burst(Vec3 center, float radius, long startMs, long endMs) {
            this.center = center;
            this.radius = radius;
            this.startMs = startMs;
            this.endMs = endMs;
        }
    }
}
