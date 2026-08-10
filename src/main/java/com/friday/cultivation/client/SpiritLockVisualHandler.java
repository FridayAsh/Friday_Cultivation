/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Camera
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.client.event.RenderLivingEvent$Post
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  org.joml.Vector3f
 */
package com.friday.cultivation.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.friday.cultivation.client.model.SpiritLockChainModel;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
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

@Mod.EventBusSubscriber(modid="friday_cultivation", value={Dist.CLIENT})
public final class SpiritLockVisualHandler {
    private static final Map<Integer, Long> ENTITY_LOCKS = new ConcurrentHashMap<Integer, Long>();
    private static final Map<BlockPos, Long> BLOCK_LOCKS = new ConcurrentHashMap<BlockPos, Long>();
    private static final int FIRST_PERSON_GOLD = 16766042;
    private static final int FIRST_PERSON_GOLD_DARK = 11103766;
    private static final int FIRST_PERSON_GOLD_LIGHT = 0xFFF0A0;
    private static final double FIRST_PERSON_CHAIN_DISTANCE = 0.78;
    private static ResourceKey<Level> currentDimension;

    private SpiritLockVisualHandler() {
    }

    public static void onVisualSync(boolean blockTarget, boolean locked, int entityId, BlockPos blockPos, int durationTicks) {
        long endMs = System.currentTimeMillis() + (long)Math.max(1, durationTicks) * 50L;
        if (blockTarget) {
            BlockPos key = blockPos.east();
            if (locked) {
                BLOCK_LOCKS.put(key, endMs);
            } else {
                BLOCK_LOCKS.remove(key);
            }
            return;
        }
        if (locked) {
            ENTITY_LOCKS.put(entityId, endMs);
        } else {
            ENTITY_LOCKS.remove(entityId);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            SpiritLockVisualHandler.clear();
            return;
        }
        ResourceKey dimension = mc.level.dimension();
        if (currentDimension == null) {
            currentDimension = dimension;
        } else if (!currentDimension.equals((Object)dimension)) {
            currentDimension = dimension;
            ENTITY_LOCKS.clear();
            BLOCK_LOCKS.clear();
            return;
        }
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        long now = System.currentTimeMillis();
        ENTITY_LOCKS.entrySet().removeIf(entry -> (Long)entry.getValue() <= now);
        BLOCK_LOCKS.entrySet().removeIf(entry -> (Long)entry.getValue() <= now);
    }

    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Post<?, ?> event) {
        LivingEntity entity = event.getEntity();
        Long endMs = ENTITY_LOCKS.get(entity.getId());
        if (endMs == null) {
            return;
        }
        float alpha = SpiritLockVisualHandler.alpha(endMs);
        if (alpha <= 0.0f) {
            ENTITY_LOCKS.remove(entity.getId());
            return;
        }
        float age = (float)entity.tickCount + event.getPartialTick();
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        SpiritLockChainModel.renderEntity(pose, event.getMultiBufferSource(), entity.getBbWidth(), entity.getBbHeight(), age, alpha);
        pose.popPose();
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        boolean hasBlockLocks;
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        Long firstPersonLockEndMs = SpiritLockVisualHandler.firstPersonLockEndMs(mc);
        boolean bl = hasBlockLocks = !BLOCK_LOCKS.isEmpty();
        if (!hasBlockLocks && firstPersonLockEndMs == null) {
            return;
        }
        Vec3 camera = event.getCamera().getPosition();
        Vec3 playerPos = mc.player.position();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);
        float age = (float)mc.level.getGameTime() + event.getPartialTick();
        long now = System.currentTimeMillis();
        if (hasBlockLocks) {
            Iterator<Map.Entry<BlockPos, Long>> it = BLOCK_LOCKS.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<BlockPos, Long> entry = it.next();
                BlockPos pos = entry.getKey();
                Long endMs = entry.getValue();
                if (endMs <= now) {
                    it.remove();
                    continue;
                }
                if (playerPos.distanceToSqr(Vec3.atCenterOf((Vec3i)pos)) > 16384.0) continue;
                pose.pushPose();
                pose.translate((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
                SpiritLockChainModel.renderBlock(pose, (MultiBufferSource)buffers, age, SpiritLockVisualHandler.alpha(endMs));
                pose.popPose();
            }
        }
        if (firstPersonLockEndMs != null) {
            SpiritLockVisualHandler.renderFirstPersonLock(event.getCamera(), pose, (MultiBufferSource)buffers, age, SpiritLockVisualHandler.alpha(firstPersonLockEndMs));
        }
        pose.popPose();
        buffers.endBatch(SpiritLockChainModel.renderType());
    }

    private static Long firstPersonLockEndMs(Minecraft mc) {
        if (!mc.options.getCameraType().isFirstPerson()) {
            return null;
        }
        Long endMs = ENTITY_LOCKS.get(mc.player.getId());
        if (endMs == null) {
            return null;
        }
        if (SpiritLockVisualHandler.alpha(endMs) <= 0.0f) {
            ENTITY_LOCKS.remove(mc.player.getId());
            return null;
        }
        return endMs;
    }

    private static void renderFirstPersonLock(Camera camera, PoseStack pose, MultiBufferSource buffers, float age, float alpha) {
        if (alpha <= 0.0f) {
            return;
        }
        Vec3 origin = camera.getPosition();
        Vec3 forward = SpiritLockVisualHandler.toVec3(camera.getLookVector()).normalize();
        Vec3 up = SpiritLockVisualHandler.toVec3(camera.getUpVector()).normalize();
        Vec3 left = SpiritLockVisualHandler.toVec3(camera.getLeftVector()).normalize();
        Vec3 right = left.scale(-1.0);
        Vec3 center = origin.add(forward.scale(0.78)).multiply(up.scale(0.03));
        double sway = Math.sin((double)age * 0.09) * 0.045;
        double counterSway = Math.cos((double)age * 0.075) * 0.035;
        float pulse = (float)(0.94 + Math.sin((double)age * 0.13) * 0.06);
        SpiritLockVisualHandler.renderFirstPersonChain(pose, buffers, center, right, up, -1.32, 0.55 + sway, 1.28, 0.49 - sway, age, alpha * pulse);
        SpiritLockVisualHandler.renderFirstPersonChain(pose, buffers, center, right, up, -1.26, -0.5 - counterSway, 1.3, -0.58 + counterSway, age + 8.0f, alpha * 0.92f);
        SpiritLockVisualHandler.renderFirstPersonChain(pose, buffers, center, right, up, -1.17, 0.48, -1.02 + counterSway, -0.66, age + 15.0f, alpha * 0.86f);
        SpiritLockVisualHandler.renderFirstPersonChain(pose, buffers, center, right, up, 1.17, 0.43, 0.98 - counterSway, -0.68, age + 23.0f, alpha * 0.86f);
        SpiritLockVisualHandler.renderFirstPersonChain(pose, buffers, center, right, up, -1.24, 0.28 + sway, 0.88, -0.62 - sway, age + 31.0f, alpha * 0.78f);
        SpiritLockVisualHandler.renderFirstPersonChain(pose, buffers, center, right, up, 1.18, 0.22 - counterSway, -0.86, -0.64 + counterSway, age + 39.0f, alpha * 0.76f);
        SpiritLockVisualHandler.renderFirstPersonChain(pose, buffers, center, right, up, -1.04, -0.13 + sway, 1.05, -0.2 - sway, age + 47.0f, alpha * 0.68f);
        SpiritLockVisualHandler.renderFirstPersonChain(pose, buffers, center, right, up, -0.58, -0.74, 0.58, -0.7, age + 55.0f, alpha * 0.72f);
    }

    private static void renderFirstPersonChain(PoseStack pose, MultiBufferSource buffers, Vec3 center, Vec3 right, Vec3 up, double startX, double startY, double endX, double endY, float age, float alpha) {
        SpiritLockChainModel.renderChainBetween(pose, buffers, center.add(right.scale(startX)).add(up.scale(startY)), center.add(right.scale(endX)).add(up.scale(endY)), age, alpha, 16766042, 11103766, 0xFFF0A0);
    }

    private static Vec3 toVec3(Vector3f vector) {
        return new Vec3((double)vector.x(), (double)vector.y(), (double)vector.z());
    }

    private static void clear() {
        ENTITY_LOCKS.clear();
        BLOCK_LOCKS.clear();
        currentDimension = null;
    }

    private static float alpha(long endMs) {
        long remaining = endMs - System.currentTimeMillis();
        if (remaining <= 0L) {
            return 0.0f;
        }
        if (remaining < 300L) {
            return Math.max(0.0f, (float)remaining / 300.0f) * 0.86f;
        }
        return 0.86f;
    }
}

