/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.client.event.RenderLivingEvent$Pre
 *  net.minecraftforge.client.event.RenderPlayerEvent$Pre
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.util.ObfuscationReflectionHelper
 */
package com.friday.cultivation.client;

import com.friday.cultivation.FridayCultivationMod;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

@Mod.EventBusSubscriber(modid="friday_cultivation", value={Dist.CLIENT})
public final class ClientOnlyGlowHandler {
    private static final int MAX_GLOW_TICKS = 1200;
    private static final Map<Integer, Long> LOCAL_GLOW_UNTIL_MS = new ConcurrentHashMap<Integer, Long>();
    private static final int GLOWING_SHARED_FLAG = 6;
    private static final Method SET_SHARED_FLAG = ClientOnlyGlowHandler.resolveSetSharedFlag();

    private static Method resolveSetSharedFlag() {
        try {
            return ObfuscationReflectionHelper.findMethod(Entity.class, (String)"m_20115_", (Class[])new Class[]{Integer.TYPE, Boolean.TYPE});
        }
        catch (Throwable t) {
            FridayCultivationMod.LOGGER.error("[ClientOnlyGlowHandler] \u7121\u6cd5\u53cd\u5c04 Entity.setSharedFlag(m_20115_)\uff0cclient \u7aef\u767c\u5149\u8f2a\u5ed3\u5c07\u5931\u6548", t);
            return null;
        }
    }

    private ClientOnlyGlowHandler() {
    }

    private static void applyClientGlow(Entity entity, boolean glowing) {
        if (entity == null) {
            return;
        }
        if (SET_SHARED_FLAG != null) {
            try {
                SET_SHARED_FLAG.invoke((Object)entity, 6, glowing);
                return;
            }
            catch (ReflectiveOperationException e) {
                FridayCultivationMod.LOGGER.error("[ClientOnlyGlowHandler] setSharedFlag \u53cd\u5c04\u547c\u53eb\u5931\u6557", (Throwable)e);
            }
        }
        entity.setGlowingTag(glowing);
    }

    public static void show(Collection<Integer> entityIds, int durationTicks) {
        if (entityIds == null || entityIds.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        long durationMs = Math.max(50L, (long)Math.min(1200, Math.max(1, durationTicks)) * 50L);
        long until = now + durationMs;
        Minecraft mc = Minecraft.getInstance();
        for (int entityId : entityIds) {
            LOCAL_GLOW_UNTIL_MS.merge(entityId, until, Math::max);
            ClientOnlyGlowHandler.setLocalGlow(mc, entityId, true);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            LOCAL_GLOW_UNTIL_MS.clear();
            return;
        }
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<Integer, Long>> it = LOCAL_GLOW_UNTIL_MS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Long> entry = it.next();
            if (now >= entry.getValue()) {
                ClientOnlyGlowHandler.setLocalGlow(mc, entry.getKey(), false);
                it.remove();
                continue;
            }
            ClientOnlyGlowHandler.setLocalGlow(mc, entry.getKey(), true);
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) {
            return;
        }
        ClientOnlyGlowHandler.reinforceAllLocalGlow(Minecraft.getInstance());
    }

    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        ClientOnlyGlowHandler.reinforceLocalGlow((Entity)event.getEntity());
    }

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        ClientOnlyGlowHandler.reinforceLocalGlow((Entity)event.getEntity());
    }

    private static void reinforceLocalGlow(Entity entity) {
        if (entity == null) {
            return;
        }
        Long until = LOCAL_GLOW_UNTIL_MS.get(entity.getId());
        if (until == null || System.currentTimeMillis() >= until) {
            return;
        }
        ClientOnlyGlowHandler.applyClientGlow(entity, true);
    }

    private static void reinforceAllLocalGlow(Minecraft mc) {
        if (mc.level == null || LOCAL_GLOW_UNTIL_MS.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        for (Map.Entry<Integer, Long> entry : LOCAL_GLOW_UNTIL_MS.entrySet()) {
            if (now >= entry.getValue()) continue;
            ClientOnlyGlowHandler.setLocalGlow(mc, entry.getKey(), true);
        }
    }

    private static void setLocalGlow(Minecraft mc, int entityId, boolean glowing) {
        if (mc.level == null) {
            return;
        }
        Entity entity = mc.level.getEntity(entityId);
        if (entity == null) {
            return;
        }
        if (!glowing) {
            LivingEntity living;
            if (entity instanceof LivingEntity && (living = (LivingEntity)entity).hasEffect(MobEffects.GLOWING)) {
                return;
            }
            ClientOnlyGlowHandler.applyClientGlow(entity, false);
            return;
        }
        ClientOnlyGlowHandler.applyClientGlow(entity, glowing);
    }
}

