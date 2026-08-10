package com.friday.cultivation.client;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod.EventBusSubscriber(modid = "friday_cultivation", value = Dist.CLIENT)
public final class ClientOnlyGlowHandler {
    private static final Logger LOG = LoggerFactory.getLogger("friday_cultivation");
    private static final int MAX_GLOW_TICKS = 1200;
    private static final Map<Integer, Long> LOCAL_GLOW_UNTIL_MS = new ConcurrentHashMap<Integer, Long>();
    private static final int GLOWING_SHARED_FLAG = 6;
    private static final Method SET_SHARED_FLAG = ClientOnlyGlowHandler.resolveSetSharedFlag();

    private static Method resolveSetSharedFlag() {
        try {
            return ObfuscationReflectionHelper.findMethod(Entity.class, (String)"m_20115_", (Class[])new Class[]{Integer.TYPE, Boolean.TYPE});
        }
        catch (Throwable t) {
            LOG.error("[ClientOnlyGlowHandler] 无法反射 Entity.setSharedFlag(m_20115_)，client 端发光轮廓将失效", t);
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
                LOG.error("[ClientOnlyGlowHandler] setSharedFlag 反射调用失败", e);
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
