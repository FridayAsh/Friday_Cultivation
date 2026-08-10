package com.friday.cultivation.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

/**
 * 勾魂进度条 HUD（严格照搬原模组 com.xiaoxiang.cultivation.client.SoulHookProgressHud）。
 * <p>由服务端 SyncSoulHookProgressPacket 在 active=true 时填满进度条；锁定时显示剩余秒数。</p>
 */
public final class SoulHookProgressHud {
    public static IGuiOverlay OVERLAY = (gui, graphics, partialTick, screenWidth, screenHeight) -> render(graphics, screenWidth, screenHeight);
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

    public static void register(net.minecraftforge.eventbus.api.Event event) {
    }

    public static void onSync(boolean packetActive, int remainingTicks, int packetTotalTicks, boolean packetLocked) {
        active = packetActive;
        locked = packetLocked;
        totalTicks = Math.max(1, packetTotalTicks);
        lastSyncMs = System.currentTimeMillis();
        endMs = lastSyncMs + (long) Math.max(0, remainingTicks) * 50L;
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
        float remainingTicks = (float) remainingMs / 50.0f;
        float progress = 1.0f - Mth.clamp(remainingTicks / (float) totalTicks, 0.0f, 1.0f);
        int seconds = locked ? Math.max(0, (int) Math.ceil((double) remainingMs / 1000.0))
                : Math.max(1, (int) Math.ceil((double) remainingMs / 1000.0));
        boolean lockedCountdown = locked && remainingMs > 0L;
        int barX = (screenWidth - 132) / 2;
        int barY = screenHeight - BAR_BOTTOM_OFFSET;
        gfx.fill(barX - 2, barY - 16, barX + 132 + 2, barY + 9 + 12, -2013265920);
        gfx.fill(barX - 1, barY - 1, barX + 132 + 1, barY + 9 + 1, INK_BLACK);
        gfx.fill(barX, barY, barX + 132, barY + 9, BG_TRACK);
        int filled = (int) (132.0f * progress);
        if (filled > 0) {
            gfx.fill(barX, barY, barX + filled, barY + 9, locked ? VORTEX_DARK : BLOOD);
            gfx.fill(barX, barY, barX + filled, barY + 1, TEXT_RED);
            gfx.fill(barX, barY + 9 - 1, barX + filled, barY + 9, BLOOD_DARK);
        }
        MutableComponent label = lockedCountdown
                ? Component.literal("hud.friday_cultivation.soul_hook.difu_countdown")
                : (locked
                        ? Component.literal("hud.friday_cultivation.soul_hook.locked")
                        : Component.literal("hud.friday_cultivation.soul_hook.progress"));
        gfx.drawString(mc.font, (Component) label, barX, barY - 12, locked ? TEXT_RED : TEXT_LIGHT, true);
        MutableComponent remaining = Component.translatable("hud.friday_cultivation.soul_hook.remaining", seconds);
        String pct = (int) (progress * 100.0f) + "%";
        int rightTextWidth = mc.font.width(pct);
        gfx.drawString(mc.font, (Component) remaining, barX, barY + 9 + 3, TEXT_LIGHT, true);
        gfx.drawString(mc.font, pct, barX + 132 - rightTextWidth, barY - 12, locked ? TEXT_RED : TEXT_LIGHT, true);
    }
}
