/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.entity.Entity
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
 */
package com.friday.cultivation.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.friday.cultivation.client.model.SpiritLockChainModel;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceKey;
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

@Mod.EventBusSubscriber(modid="friday_cultivation", value={Dist.CLIENT})
public final class LifeBalanceVisualHandler {
    private static final int BLOOD = 14689582;
    private static final int BLOOD_DARK = 6031120;
    private static final int BLOOD_LIGHT = 16742274;
    private static final Map<Integer, Long> MARKED_ENTITIES = new ConcurrentHashMap<Integer, Long>();
    private static final Map<Integer, Long> LINK_BODY_ENTITIES = new ConcurrentHashMap<Integer, Long>();
    private static final Map<Long, LinkVisual> LINKS = new ConcurrentHashMap<Long, LinkVisual>();
    private static ResourceKey<Level> currentDimension;

    private LifeBalanceVisualHandler() {
    }

    public static void onMarkSync(int entityId, int durationTicks, boolean active) {
        if (active) {
            MARKED_ENTITIES.put(entityId, LifeBalanceVisualHandler.endMs(durationTicks));
        } else {
            MARKED_ENTITIES.remove(entityId);
        }
    }

    public static void onLinkSync(int casterId, int targetId, int durationTicks, boolean active) {
        long key = LifeBalanceVisualHandler.linkKey(casterId, targetId);
        if (active) {
            long end = LifeBalanceVisualHandler.endMs(durationTicks);
            LINKS.put(key, new LinkVisual(casterId, targetId, end));
            LINK_BODY_ENTITIES.put(casterId, end);
            LINK_BODY_ENTITIES.put(targetId, end);
        } else {
            LINKS.remove(key);
            LINK_BODY_ENTITIES.remove(casterId);
            LINK_BODY_ENTITIES.remove(targetId);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            LifeBalanceVisualHandler.clear();
            return;
        }
        ResourceKey dimension = mc.level.dimension();
        if (currentDimension == null) {
            currentDimension = dimension;
        } else if (!currentDimension.equals((Object)dimension)) {
            LifeBalanceVisualHandler.clear();
            currentDimension = dimension;
            return;
        }
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        long now = System.currentTimeMillis();
        MARKED_ENTITIES.entrySet().removeIf(entry -> (Long)entry.getValue() <= now);
        LINK_BODY_ENTITIES.entrySet().removeIf(entry -> (Long)entry.getValue() <= now);
        LINKS.entrySet().removeIf(entry -> ((LinkVisual)entry.getValue()).endMs <= now);
    }

    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Post<?, ?> event) {
        LivingEntity entity = event.getEntity();
        long endMs = Math.max(MARKED_ENTITIES.getOrDefault(entity.getId(), 0L), LINK_BODY_ENTITIES.getOrDefault(entity.getId(), 0L));
        if (endMs <= 0L) {
            return;
        }
        float alpha = LifeBalanceVisualHandler.alpha(endMs);
        if (alpha <= 0.0f) {
            MARKED_ENTITIES.remove(entity.getId());
            LINK_BODY_ENTITIES.remove(entity.getId());
            return;
        }
        float age = (float)entity.tickCount + event.getPartialTick();
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        SpiritLockChainModel.renderEntity(pose, event.getMultiBufferSource(), entity.getBbWidth(), entity.getBbHeight(), age, alpha, 14689582, 6031120, 16742274);
        pose.popPose();
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        if (LINKS.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        Vec3 camera = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        float age = (float)mc.level.getGameTime() + event.getPartialTick();
        long now = System.currentTimeMillis();
        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);
        Iterator<Map.Entry<Long, LinkVisual>> it = LINKS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, LinkVisual> entry = it.next();
            LinkVisual link = entry.getValue();
            if (link.endMs <= now) {
                it.remove();
                continue;
            }
            Entity caster = mc.level.getEntity(link.casterId);
            Entity target = mc.level.getEntity(link.targetId);
            if (!(caster instanceof LivingEntity)) continue;
            LivingEntity casterLiving = (LivingEntity)caster;
            if (!(target instanceof LivingEntity)) continue;
            LivingEntity targetLiving = (LivingEntity)target;
            Vec3 from = casterLiving.getPosition(event.getPartialTick()).add(0.0, (double)casterLiving.getBbHeight() * 0.55, 0.0);
            Vec3 to = targetLiving.getPosition(event.getPartialTick()).add(0.0, (double)targetLiving.getBbHeight() * 0.55, 0.0);
            if (mc.player.position().distanceToSqr(from) > 25600.0 && mc.player.position().distanceToSqr(to) > 25600.0) continue;
            SpiritLockChainModel.renderChainBetween(pose, (MultiBufferSource)buffers, from, to, age, LifeBalanceVisualHandler.alpha(link.endMs), 14689582, 6031120, 16742274);
        }
        pose.popPose();
        buffers.endBatch(SpiritLockChainModel.renderType());
    }

    private static void clear() {
        MARKED_ENTITIES.clear();
        LINK_BODY_ENTITIES.clear();
        LINKS.clear();
        currentDimension = null;
    }

    private static long endMs(int durationTicks) {
        return System.currentTimeMillis() + (long)Math.max(1, durationTicks) * 50L;
    }

    private static long linkKey(int casterId, int targetId) {
        return (long)casterId << 32 ^ (long)targetId & 0xFFFFFFFFL;
    }

    private static float alpha(long endMs) {
        long remaining = endMs - System.currentTimeMillis();
        if (remaining <= 0L) {
            return 0.0f;
        }
        if (remaining < 300L) {
            return Math.max(0.0f, (float)remaining / 300.0f) * 0.88f;
        }
        return 0.88f;
    }

    private record LinkVisual(int casterId, int targetId, long endMs) {
    }
}

