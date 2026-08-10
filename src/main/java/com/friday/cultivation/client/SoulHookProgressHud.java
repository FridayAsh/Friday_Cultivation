/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.util.Mth
 *  net.minecraftforge.client.gui.overlay.IGuiOverlay
 */
package com.friday.cultivation.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public final class SoulHookProgressHud {
    public static final IGuiOverlay OVERLAY = (gui, graphics, partialTick, screenWidth, screenHeight) -> SoulHookProgressHud.render(graphics, screenWidth, screenHeight);
    private static final int BAR_W = 132;
    private static final int BAR_H = 9;
    private static final int BAR_BOTTOM_OFFSET = 82;
    private static final int INK_BLACK = -15463665;
    private static final int BG_TRACK = -870313960;
    private static final int BLOOD = -2087634;
    private static final int BLOOD_DARK = -10746096;
    private static final int VORTEX_DARK = -16121082;
    private static final int TEXT_LIGHT = -726312;
    private static final int TEXT_RED = -34942;
    private static boolean active;
    private static boolean locked;
    private static int totalTicks;
    private static long endMs;
    private static long lastSyncMs;

    private SoulHookProgressHud() {
    }

    public static void onSync(boolean packetActive, int remainingTicks, int packetTotalTicks, boolean packetLocked) {
        active = packetActive;
        locked = packetLocked;
        totalTicks = Math.max(1, packetTotalTicks);
        lastSyncMs = System.currentTimeMillis();
        endMs = lastSyncMs + (long)Math.max(0, remainingTicks) * 50L;
        if (!packetActive) {
            active = false;
            locked = false;
            endMs = 0L;
        }
    }

    private static void render(GuiGraphics gfx, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (!active || mc.player == null || mc.options.hideGui) {
            return;
        }
        long now = System.currentTimeMillis();
        long remainingMs = Math.max(0L, endMs - now);
        if (!locked && remainingMs <= 0L && now - lastSyncMs > 900L) {
            active = false;
            return;
        }
        if (locked && remainingMs <= 0L && now - lastSyncMs > 1200L) {
            active = false;
            return;
        }
        float remainingTicks = (float)remainingMs / 50.0f;
        float progress = 1.0f - Mth.clamp((float)(remainingTicks / (float)totalTicks), (float)0.0f, (float)1.0f);
        int seconds = locked ? Math.max(0, (int)Math.ceil((double)remainingMs / 1000.0)) : Math.max(1, (int)Math.ceil((double)remainingMs / 1000.0));
        boolean lockedCountdown = locked && remainingMs > 0L;
        int barX = (screenWidth - 132) / 2;
        int barY = screenHeight - 82;
        gfx.fill(barX - 2, barY - 16, barX + 132 + 2, barY + 9 + 12, -2013265920);
        gfx.fill(barX - 1, barY - 1, barX + 132 + 1, barY + 9 + 1, -15463665);
        gfx.fill(barX, barY, barX + 132, barY + 9, -870313960);
        int filled = (int)(132.0f * progress);
        if (filled > 0) {
            gfx.fill(barX, barY, barX + filled, barY + 9, locked ? -16121082 : -2087634);
            gfx.fill(barX, barY, barX + filled, barY + 1, -34942);
            gfx.fill(barX, barY + 9 - 1, barX + filled, barY + 9, -10746096);
        }
        MutableComponent label = lockedCountdown ? Component.translatable((String)"hud.friday_cultivation.soul_hook.difu_countdown") : (locked ? Component.translatable((String)"hud.friday_cultivation.soul_hook.locked") : Component.translatable((String)"hud.friday_cultivation.soul_hook.progress"));
        gfx.drawString(mc.font, (Component)label, barX, barY - 12, locked ? -34942 : -726312, true);
        MutableComponent remaining = Component.translatable((String)"hud.friday_cultivation.soul_hook.remaining", (Object[])new Object[]{seconds});
        String pct = (int)(progress * 100.0f) + "%";
        int rightTextWidth = mc.font.width(pct);
        gfx.drawString(mc.font, (Component)remaining, barX, barY + 9 + 3, -726312, true);
        gfx.drawString(mc.font, pct, barX + 132 - rightTextWidth, barY - 12, locked ? -34942 : -726312, true);
    }
}

