package com.friday.cultivation.client;

import com.friday.cultivation.technique.TechniqueBonusHelper;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 不朽暗视处理器 — 完整复刻原模组 ImmortalNightVisionHandler。
 * 不朽经+不朽体质（hasImmortalCombo）时，通过提高 gamma 提供暗视效果。
 * 服务端 SyncImmortalDarkVisionPacket 同步状态，客户端 tick 强制 gamma=16（带反射fallback）。
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation", value = Dist.CLIENT)
public final class ImmortalNightVisionHandler {
    private static final double DARKVISION_GAMMA = 16.0;
    private static final double GAMMA_EPSILON = 0.001;
    private static Double savedGamma = null;
    private static Field gammaValueField = null;
    private static boolean reflectionScanned = false;
    private static boolean serverSyncReceived = false;
    private static boolean serverDarkVision = false;

    private ImmortalNightVisionHandler() {
    }

    public static void setServerDarkVision(boolean enabled) {
        serverSyncReceived = true;
        serverDarkVision = enabled;
        if (!enabled) {
            restoreGamma();
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            restoreGamma();
            return;
        }
        boolean localFallback = TechniqueBonusHelper.hasImmortalCombo(player);
        boolean wantDarkVision = serverSyncReceived ? serverDarkVision : localFallback;
        OptionInstance<Double> gammaOpt = mc.options.gamma();
        double currentGamma = gammaOpt.get();
        if (wantDarkVision) {
            if (savedGamma == null) {
                savedGamma = currentGamma;
            }
            if (Math.abs(currentGamma - DARKVISION_GAMMA) > GAMMA_EPSILON) {
                setGamma(mc, gammaOpt, DARKVISION_GAMMA);
            } else {
                markLightTextureDirty(mc);
            }
        } else {
            restoreGamma();
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        serverSyncReceived = false;
        serverDarkVision = false;
        restoreGamma();
    }

    private static void restoreGamma() {
        if (savedGamma == null) return;
        Minecraft mc = Minecraft.getInstance();
        setGamma(mc, mc.options.gamma(), savedGamma);
        savedGamma = null;
    }

    private static void setGamma(Minecraft mc, OptionInstance<Double> gammaOpt, double value) {
        if (!forceSetGammaValue(gammaOpt, value)) {
            gammaOpt.set(Math.min(1.0, Math.max(0.0, value)));
        }
        markLightTextureDirty(mc);
    }

    private static boolean forceSetGammaValue(OptionInstance<Double> gammaOpt, double value) {
        if (gammaValueField != null && trySetGammaValue(gammaOpt, gammaValueField, value)) {
            return true;
        }
        if (!reflectionScanned) {
            reflectionScanned = true;
            Field named = findDeclaredField("value");
            if (named != null && trySetGammaValue(gammaOpt, named, value)) {
                gammaValueField = named;
                return true;
            }
            for (Field field : OptionInstance.class.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || !trySetGammaValue(gammaOpt, field, value)) continue;
                gammaValueField = field;
                return true;
            }
        }
        return false;
    }

    private static Field findDeclaredField(String name) {
        try {
            Field field = OptionInstance.class.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static boolean trySetGammaValue(OptionInstance<Double> gammaOpt, Field field, double value) {
        try {
            field.setAccessible(true);
            Object oldValue = field.get(gammaOpt);
            if (!(oldValue instanceof Double)) return false;
            field.set(gammaOpt, value);
            if (Math.abs(gammaOpt.get() - value) <= GAMMA_EPSILON) return true;
            field.set(gammaOpt, oldValue);
        } finally {
            return false;
        }
    }

    private static void markLightTextureDirty(Minecraft mc) {
        try {
            if (mc != null && mc.gameRenderer != null) {
                mc.gameRenderer.lightTexture().turnOffLightLayer();
            }
        } catch (Throwable ignored) {
        }
    }
}
