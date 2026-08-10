/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  org.jetbrains.annotations.NotNull
 */
package com.friday.cultivation.client.screen;

import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.SoulReaperTargetEntry;
import com.friday.cultivation.network.SoulReaperTeleportPacket;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

public class SoulReaperTargetScreen
extends Screen {
    private static final int ROW_H = 42;
    private static final int MAX_VISIBLE_ROWS = 5;
    private static final int MAX_PANEL_W = 430;
    private static final int MIN_PANEL_W = 300;
    private static final int MAX_PANEL_H = 284;
    private static final int MIN_PANEL_H = 120;
    private static final int SCREEN_MARGIN = 12;
    private static final int HEADER_H = 48;
    private static final int FOOTER_H = 30;
    private static final int ROW_BUTTON_GAP = 2;
    private static final int BUTTON_W = 58;
    private static final int BUTTON_H = 18;
    private static final int CLOSE_BUTTON_W = 80;
    private static final int CLOSE_BUTTON_H = 20;
    private List<SoulReaperTargetEntry> targets;
    private int scrollOffset = 0;

    public SoulReaperTargetScreen(List<SoulReaperTargetEntry> targets) {
        super((Component)Component.translatable((String)"screen.friday_cultivation.soul_reaper_targets.title"));
        this.targets = List.copyOf(targets);
    }

    public void updateTargets(List<SoulReaperTargetEntry> nextTargets) {
        this.targets = List.copyOf(nextTargets);
        if (this.minecraft != null) {
            this.rebuildButtons();
        } else {
            this.scrollOffset = 0;
        }
    }

    protected void init() {
        this.rebuildButtons();
    }

    private void rebuildButtons() {
        this.clearWidgets();
        ScreenLayout layout = this.layout();
        this.clampScroll(layout.visibleRowCapacity());
        int visible = this.visibleRows(layout);
        for (int i = 0; i < visible; ++i) {
            int targetIndex = this.scrollOffset + i;
            SoulReaperTargetEntry entry = this.targets.get(targetIndex);
            int y = layout.rowTop() + i * 42 + 12;
            this.addRenderableWidget(Button.builder(Component.translatable("screen.friday_cultivation.soul_reaper_targets.teleport"), button -> this.teleport(entry)).bounds(layout.rowRight() - 58, y, 58, 18).build());
        }
        this.addRenderableWidget(Button.builder(Component.translatable("screen.friday_cultivation.close"), button -> this.onClose()).bounds(layout.left() + layout.panelW() / 2 - 40, layout.closeY(), 80, 20).build());
    }

    private void teleport(SoulReaperTargetEntry entry) {
        ModNetwork.CHANNEL.sendToServer((Object)new SoulReaperTeleportPacket(entry.targetId()));
        this.onClose();
    }

    public void addEntry(@NotNull GuiGraphics gfx, int mouseX, int mouseY, float partial) {
        this.renderBackground(gfx);
        ScreenLayout layout = this.layout();
        this.clampScroll(layout.visibleRowCapacity());
        gfx.fill(layout.left(), layout.top(), layout.right(), layout.bottom(), -267712752);
        gfx.fill(layout.left(), layout.top(), layout.right(), layout.top() + 1, -6675664);
        gfx.fill(layout.left(), layout.bottom() - 1, layout.right(), layout.bottom(), -6675664);
        gfx.fill(layout.left(), layout.top(), layout.left() + 1, layout.bottom(), -6675664);
        gfx.fill(layout.right() - 1, layout.top(), layout.right(), layout.bottom(), -6675664);
        gfx.drawCenteredString(this.font, this.title, layout.left() + layout.panelW() / 2, layout.top() + 16, -10870);
        if (this.targets.isEmpty()) {
            int emptyY = Math.max(layout.top() + 42, Math.min(layout.top() + 86, layout.closeY() - 18));
            gfx.drawCenteredString(this.font, (Component)Component.translatable((String)"screen.friday_cultivation.soul_reaper_targets.empty").withStyle(ChatFormatting.GRAY), layout.left() + layout.panelW() / 2, emptyY, -3160390);
        } else {
            this.renderRows(gfx, layout);
            this.renderScrollbar(gfx, layout);
        }
        super.render(gfx, mouseX, mouseY, partial);
    }

    private void renderRows(GuiGraphics gfx, ScreenLayout layout) {
        int textW = Math.max(80, layout.rowRight() - layout.rowLeft() - 58 - 16);
        int visible = this.visibleRows(layout);
        for (int i = 0; i < visible; ++i) {
            SoulReaperTargetEntry entry = this.targets.get(this.scrollOffset + i);
            int y = layout.rowTop() + i * 42;
            gfx.fill(layout.rowLeft(), y, layout.rowRight(), y + 42 - 4, -14675429);
            gfx.fill(layout.rowLeft(), y, layout.rowLeft() + 2, y + 42 - 4, entry.playerTarget() ? -9972481 : -5022721);
            MutableComponent type = Component.translatable((String)(entry.playerTarget() ? "screen.friday_cultivation.soul_reaper_targets.type.player" : "screen.friday_cultivation.soul_reaper_targets.type.npc"));
            MutableComponent titleLine = Component.translatable((String)"screen.friday_cultivation.soul_reaper_targets.line.title", (Object[])new Object[]{entry.name(), type}).withStyle(ChatFormatting.GOLD);
            MutableComponent profile = Component.translatable((String)"screen.friday_cultivation.soul_reaper_targets.line.profile", (Object[])new Object[]{entry.gender(), entry.identity(), entry.realm()}).withStyle(ChatFormatting.GRAY);
            MutableComponent location = Component.translatable((String)"screen.friday_cultivation.soul_reaper_targets.line.location", (Object[])new Object[]{entry.location()}).withStyle(ChatFormatting.DARK_GRAY);
            this.drawTrimmed(gfx, (Component)titleLine, layout.rowLeft() + 8, y + 5, textW, -10870);
            this.drawTrimmed(gfx, (Component)profile, layout.rowLeft() + 8, y + 17, textW, -3160390);
            this.drawTrimmed(gfx, (Component)location, layout.rowLeft() + 8, y + 29, textW, -7502960);
        }
    }

    private void renderScrollbar(GuiGraphics gfx, ScreenLayout layout) {
        int visibleCapacity = layout.visibleRowCapacity();
        if (visibleCapacity <= 0 || this.targets.size() <= visibleCapacity) {
            return;
        }
        int barX = layout.left() + layout.panelW() - 8;
        int barTop = layout.rowTop();
        int barBottom = barTop + visibleCapacity * 42 - 4;
        gfx.fill(barX, barTop, barX + 3, barBottom, -13032918);
        int maxOffset = this.maxScrollOffset(visibleCapacity);
        int thumbH = Math.max(16, (barBottom - barTop) * visibleCapacity / this.targets.size());
        int thumbY = barTop + (barBottom - barTop - thumbH) * this.scrollOffset / Math.max(1, maxOffset);
        gfx.fill(barX, thumbY, barX + 3, thumbY + thumbH, -4703686);
    }

    private void drawTrimmed(GuiGraphics gfx, Component text, int x, int y, int maxWidth, int color) {
        String raw = text.getString();
        String trimmed = this.font.plainSubstrByWidth(raw, maxWidth);
        gfx.drawString(this.font, trimmed, x, y, color, false);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        ScreenLayout layout = this.layout();
        int maxOffset = this.maxScrollOffset(layout.visibleRowCapacity());
        if (maxOffset <= 0) {
            return super.mouseScrolled(mouseX, mouseY, delta);
        }
        int old = this.scrollOffset;
        this.scrollOffset = Math.max(0, Math.min(maxOffset, this.scrollOffset - (int)Math.signum(delta)));
        if (old != this.scrollOffset) {
            this.rebuildButtons();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private ScreenLayout layout() {
        int availableW = Math.max(1, this.width - 24);
        int panelW = Math.min(430, availableW);
        if (panelW < 300) {
            panelW = availableW;
        }
        int desiredRows = this.targets.isEmpty() ? 0 : Math.min(5, this.targets.size());
        int desiredH = this.targets.isEmpty() ? 156 : 48 + desiredRows * 42 + 30;
        int availableH = Math.max(1, this.height - 24);
        int panelH = Math.min(284, Math.min(desiredH, availableH));
        if (availableH >= 120) {
            panelH = Math.max(120, panelH);
        }
        int left = (this.width - panelW) / 2;
        int top = Math.max(12, (this.height - panelH) / 2);
        int closeY = top + panelH - 20 - 8;
        int rowTop = top + 48;
        int visibleCapacity = this.targets.isEmpty() ? 0 : Math.max(0, Math.min(5, (closeY - 2 - rowTop + 4) / 42));
        return new ScreenLayout(panelW, panelH, left, top, rowTop, closeY, visibleCapacity);
    }

    private int visibleRows(ScreenLayout layout) {
        return Math.min(layout.visibleRowCapacity(), Math.max(0, this.targets.size() - this.scrollOffset));
    }

    private int maxScrollOffset(int visibleCapacity) {
        if (visibleCapacity <= 0) {
            return 0;
        }
        return Math.max(0, this.targets.size() - visibleCapacity);
    }

    private void clampScroll(int visibleCapacity) {
        this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, this.maxScrollOffset(visibleCapacity)));
    }

    public boolean isPauseScreen() {
        return false;
    }

    private record ScreenLayout(int panelW, int panelH, int left, int top, int rowTop, int closeY, int visibleRowCapacity) {
        int right() {
            return this.left + this.panelW;
        }

        int bottom() {
            return this.top + this.panelH;
        }

        int rowLeft() {
            return this.left + 14;
        }

        int rowRight() {
            return this.left + this.panelW - 14;
        }
    }
}

