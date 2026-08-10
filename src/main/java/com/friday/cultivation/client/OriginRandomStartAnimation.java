/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.Util
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraftforge.client.gui.overlay.IGuiOverlay
 */
package com.friday.cultivation.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.friday.cultivation.cultivation.Identity;
import com.friday.cultivation.cultivation.Physique;
import com.friday.cultivation.cultivation.SpiritRoot;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public final class OriginRandomStartAnimation {
    public static final IGuiOverlay OVERLAY = (gui, graphics, partialTick, screenWidth, screenHeight) -> OriginRandomStartAnimation.render(graphics, screenWidth, screenHeight);
    private static final List<Identity> IDENTITIES = Identity.selectableOrigins();
    private static final List<SpiritRoot> SPIRIT_ROOTS = SpiritRoot.selectableValues();
    private static final List<Physique> PHYSIQUES = Physique.selectableValues();
    private static final long ROW_SPIN_MS = 780L;
    private static final long ROW_GAP_MS = 140L;
    private static final long HOLD_MS = 1100L;
    private static final int PANEL_WIDTH = 188;
    private static final int PANEL_HEIGHT = 86;
    private static final int TITLE_COLOR = -6488;
    private static final int LABEL_COLOR = -4346229;
    private static final int FINAL_COLOR = -8064799;
    private static final int SPIN_COLOR = -11930;
    private static ActiveAnimation active;

    private OriginRandomStartAnimation() {
    }

    public static void start(String identityId, String spiritRootId, String physiqueId, boolean grantStarterItems) {
        Identity identity = Identity.byId(identityId);
        SpiritRoot spiritRoot = SpiritRoot.byId(spiritRootId);
        Physique physique = Physique.byId(physiqueId);
        active = new ActiveAnimation(Util.getMillis(), identity, spiritRoot, physique, grantStarterItems);
    }

    private static void render(GuiGraphics graphics, int screenWidth, int screenHeight) {
        long total;
        ActiveAnimation anim = active;
        if (anim == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        long elapsed = Util.getMillis() - anim.startedAt;
        if (elapsed >= (total = 3860L)) {
            OriginRandomStartAnimation.postResultMessage(mc, anim);
            active = null;
            return;
        }
        int panelX = (screenWidth - 188) / 2;
        int panelY = (screenHeight - 86) / 2 - 12;
        float entrance = Math.min(1.0f, (float)elapsed / 220.0f);
        int alpha = (int)(entrance * 224.0f);
        int bg = alpha << 24 | 0x140F0B;
        int border = (int)(entrance * 210.0f) << 24 | 0xB99755;
        RenderSystem.enableBlend();
        graphics.fill(panelX + 4, panelY + 5, panelX + 188 + 4, panelY + 86 + 5, (int)(entrance * 112.0f) << 24);
        graphics.fill(panelX, panelY, panelX + 188, panelY + 86, bg);
        graphics.fill(panelX, panelY, panelX + 188, panelY + 1, border);
        graphics.fill(panelX, panelY + 86 - 1, panelX + 188, panelY + 86, border);
        graphics.fill(panelX, panelY, panelX + 1, panelY + 86, border);
        graphics.fill(panelX + 188 - 1, panelY, panelX + 188, panelY + 86, border);
        MutableComponent title = Component.translatable((String)"screen.friday_cultivation.identity_draw.random_anim.title");
        graphics.drawCenteredString(mc.font, (Component)title, panelX + 94, panelY + 9, -6488);
        OriginRandomStartAnimation.drawRow(graphics, mc, panelX + 14, panelY + 28, 0, elapsed, (Component)Component.translatable((String)"screen.friday_cultivation.identity_draw.identity_label"), (Component)Component.translatable((String)anim.identity.translationKey()));
        OriginRandomStartAnimation.drawRow(graphics, mc, panelX + 14, panelY + 46, 1, elapsed, (Component)Component.translatable((String)"screen.friday_cultivation.identity_draw.spirit_root_label"), (Component)Component.translatable((String)anim.spiritRoot.translationKey()));
        OriginRandomStartAnimation.drawRow(graphics, mc, panelX + 14, panelY + 64, 2, elapsed, (Component)Component.translatable((String)"screen.friday_cultivation.identity_draw.physique_label"), (Component)Component.translatable((String)anim.physique.translationKey()));
        RenderSystem.disableBlend();
    }

    private static void drawRow(GuiGraphics graphics, Minecraft mc, int x, int y, int row, long elapsed, Component label, Component finalValue) {
        int rowBg;
        boolean revealed;
        long start = (long)row * 920L;
        boolean activeRow = elapsed >= start && elapsed < start + 780L;
        revealed = elapsed >= start + 780L;
        rowBg = activeRow ? 1144548459 : (revealed ? 857942048 : 571608586);
        int edge = activeRow ? -1434128159 : (revealed ? 1717022365 : 1430269470);
        graphics.fill(x, y, x + 188 - 28, y + 13, rowBg);
        graphics.fill(x, y, x + 188 - 28, y + 1, edge);
        graphics.drawString(mc.font, label, x + 5, y + 3, -4346229, false);
        Component value = OriginRandomStartAnimation.rowValue(row, elapsed, finalValue, activeRow, revealed);
        int valueX = x + 52;
        int valueW = 102;
        int color = activeRow ? -11930 : (revealed ? -8064799 : -9807035);
        OriginRandomStartAnimation.drawTrimmedCentered(graphics, mc, value, valueX, y + 3, valueW, color);
    }

    private static Component rowValue(int row, long elapsed, Component finalValue, boolean activeRow, boolean revealed) {
        if (revealed) {
            return finalValue;
        }
        if (!activeRow) {
            return Component.translatable((String)"screen.friday_cultivation.identity_draw.random_anim.waiting");
        }
        int offset = (int)(elapsed / 62L + (long)row * 13L);
        return switch (row) {
            case 0 -> Component.translatable((String)IDENTITIES.get(offset % IDENTITIES.size()).translationKey());
            case 1 -> Component.translatable((String)SPIRIT_ROOTS.get(offset % SPIRIT_ROOTS.size()).translationKey());
            default -> Component.translatable((String)PHYSIQUES.get(offset % PHYSIQUES.size()).translationKey());
        };
    }

    private static void drawTrimmedCentered(GuiGraphics graphics, Minecraft mc, Component text, int x, int y, int width, int color) {
        String display = mc.font.plainSubstrByWidth(text.getString(), width);
        int drawX = x + (width - mc.font.width(display)) / 2;
        graphics.drawString(mc.font, display, drawX, y, color, false);
    }

    private static void postResultMessage(Minecraft mc, ActiveAnimation anim) {
        if (mc.gui == null) {
            return;
        }
        String key = anim.grantStarterItems ? "screen.friday_cultivation.identity_draw.confirmed" : "message.friday_cultivation.origin_reconfiguration_token.applied";
        mc.gui.getChat().addMessage((Component)Component.translatable((String)key, (Object[])new Object[]{Component.translatable((String)anim.identity.translationKey()), Component.translatable((String)anim.spiritRoot.translationKey()), Component.translatable((String)anim.physique.translationKey())}));
    }

    private record ActiveAnimation(long startedAt, Identity identity, SpiritRoot spiritRoot, Physique physique, boolean grantStarterItems) {
    }
}

