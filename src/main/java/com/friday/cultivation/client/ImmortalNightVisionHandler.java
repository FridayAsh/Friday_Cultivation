/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.OptionInstance
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.ClientPlayerNetworkEvent$LoggingOut
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package com.friday.cultivation.client;

import com.friday.cultivation.cultivation.technique.TechniqueBonusHelper;
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

@Mod.EventBusSubscriber(modid="friday_cultivation", value={Dist.CLIENT})
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
            ImmortalNightVisionHandler.restoreGamma();
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            ImmortalNightVisionHandler.restoreGamma();
            return;
        }
        boolean localFallback = TechniqueBonusHelper.hasImmortalCombo((Player)player);
        boolean wantDarkVision = serverSyncReceived ? serverDarkVision : localFallback;
        OptionInstance gammaOpt = mc.options.gamma();
        double currentGamma = (Double)gammaOpt.get();
        if (wantDarkVision) {
            if (savedGamma == null) {
                savedGamma = currentGamma;
            }
            if (Math.abs(currentGamma - 16.0) > 0.001) {
                ImmortalNightVisionHandler.setGamma(mc, (OptionInstance<Double>)gammaOpt, 16.0);
            } else {
                ImmortalNightVisionHandler.markLightTextureDirty(mc);
            }
        } else {
            ImmortalNightVisionHandler.restoreGamma();
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        serverSyncReceived = false;
        serverDarkVision = false;
        ImmortalNightVisionHandler.restoreGamma();
    }

    private static void restoreGamma() {
        if (savedGamma == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ImmortalNightVisionHandler.setGamma(mc, (OptionInstance<Double>)mc.options.gamma(), savedGamma);
        savedGamma = null;
    }

    private static void setGamma(Minecraft mc, OptionInstance<Double> gammaOpt, double value) {
        if (!ImmortalNightVisionHandler.forceSetGammaValue(gammaOpt, value)) {
            gammaOpt.set(Math.min(1.0, Math.max(0.0, value)));
        }
        ImmortalNightVisionHandler.markLightTextureDirty(mc);
    }

    private static boolean forceSetGammaValue(OptionInstance<Double> gammaOpt, double value) {
        if (gammaValueField != null && ImmortalNightVisionHandler.trySetGammaValue(gammaOpt, gammaValueField, value)) {
            return true;
        }
        if (!reflectionScanned) {
            reflectionScanned = true;
            Field named = ImmortalNightVisionHandler.findDeclaredField("value");
            if (named != null && ImmortalNightVisionHandler.trySetGammaValue(gammaOpt, named, value)) {
                gammaValueField = named;
                return true;
            }
            for (Field field : OptionInstance.class.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || !ImmortalNightVisionHandler.trySetGammaValue(gammaOpt, field, value)) continue;
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
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    private static boolean trySetGammaValue(OptionInstance<Double> gammaOpt, Field field, double value) {
        try {
            field.setAccessible(true);
            Object oldValue = field.get(gammaOpt);
            if (!(oldValue instanceof Double)) {
                return false;
            }
            field.set(gammaOpt, value);
            if (Math.abs((Double)gammaOpt.get() - value) <= 0.001) {
                return true;
            }
            field.set(gammaOpt, oldValue);
        }
        finally {
            return false;
        }
    }

    private static void markLightTextureDirty(Minecraft mc) {
        try {
            if (mc != null && mc.gameRenderer != null) {
                mc.gameRenderer.lightTexture().tick();
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }
}

