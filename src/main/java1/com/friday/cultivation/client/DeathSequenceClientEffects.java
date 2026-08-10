package com.friday.cultivation.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "friday_cultivation", value = Dist.CLIENT)
public final class DeathSequenceClientEffects {
    private static final int TITLE_COLOR = -4048338;
    private static final int DESC_COLOR = -1648452;
    private static final int COUNT_LABEL_COLOR = -1648452;
    private static final int COUNT_NUM_COLOR = -15797;
    private static final int BAND_COLOR = -1778384896;
    private static int titleTicks = 0;
    private static int countdownTicks = 0;
    private static int elapsed = -1;
    public static final IGuiOverlay OVERLAY = (gui, graphics, partialTick, sw, sh) -> DeathSequenceClientEffects.render(graphics, sw, sh);

    private DeathSequenceClientEffects() {
    }

    public static void registerOverlay(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("death_sequence", OVERLAY);
    }

    public static void start(int title, int countdown) {
        titleTicks = Math.max(1, title);
        countdownTicks = Math.max(0, countdown);
        elapsed = 0;
    }

    public static void clear() {
        elapsed = -1;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || elapsed < 0) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            elapsed = -1;
            return;
        }
        if (mc.isPaused()) {
            return;
        }
        if (++elapsed >= titleTicks + countdownTicks) {
            elapsed = -1;
        }
    }

    private static void render(GuiGraphics g, int sw, int sh) {
        if (elapsed < 0) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) {
            return;
        }
        Font font = mc.font;
        int cx = sw / 2;
        if (elapsed < titleTicks) {
            int titleY = (int)((float)sh * 0.32f);
            g.fill(0, titleY - 14, sw, titleY + 66, -1778384896);
            DeathSequenceClientEffects.drawScaledCentered(g, font, Component.translatable("screen.friday_cultivation.death.title"), cx, titleY, 3.4f, -4048338);
            DeathSequenceClientEffects.drawCentered(g, font, Component.translatable("screen.friday_cultivation.death.desc1"), cx, titleY + 40, -1648452);
            DeathSequenceClientEffects.drawCentered(g, font, Component.translatable("screen.friday_cultivation.death.desc2"), cx, titleY + 52, -1648452);
        } else {
            int remainTicks = titleTicks + countdownTicks - elapsed;
            int sec = (remainTicks + 19) / 20;
            int y = (int)((float)sh * 0.36f);
            g.fill(0, y - 10, sw, y + 26, -1778384896);
            DeathSequenceClientEffects.drawCentered(g, font, Component.translatable("screen.friday_cultivation.death.countdown"), cx, y, -1648452);
            float pulse = sec <= 3 ? 2.4f : 1.9f;
            int numColor = sec <= 3 ? -4048338 : -15797;
            DeathSequenceClientEffects.drawScaledCentered(g, font, Component.literal(String.valueOf(sec)), cx, y + 13, pulse, numColor);
        }
    }

    private static void drawCentered(GuiGraphics g, Font font, Component text, int cx, int y, int color) {
        g.drawString(font, text, cx - font.width(text) / 2, y, color, true);
    }

    private static void drawScaledCentered(GuiGraphics g, Font font, Component text, int cx, int y, float scale, int color) {
        int w = font.width(text);
        g.pose().pushPose();
        g.pose().translate((float)cx, (float)y, 0.0f);
        g.pose().scale(scale, scale, 1.0f);
        g.drawString(font, text, -w / 2, 0, color, true);
        g.pose().popPose();
    }
}
