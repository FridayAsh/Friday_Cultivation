package com.friday.cultivation.client.screen;

import com.friday.cultivation.network.SoulReaperTargetEntry;
import com.friday.cultivation.network.SoulReaperTeleportPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import com.friday.cultivation.network.ModNetwork;

import java.util.List;

/**
 * 勾魂目标屏（严格照搬原模组 com.xiaoxiang.cultivation.client.screen.SoulReaperTargetScreen）。
 * <p>由 ClientSoulReaperTargetHooks.open() 在目标更新时打开/更新。</p>
 */
public class SoulReaperTargetScreen extends Screen {
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
        super(Component.literal("screen.xiaoxiang_cultivation.soul_reaper_targets.title"));
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

    @Override
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
            int y = layout.rowTop() + i * ROW_H + 12;
            this.addRenderableWidget(Button.builder(
                    Component.literal("screen.xiaoxiang_cultivation.soul_reaper_targets.teleport"),
                    button -> this.teleport(entry))
                    .pos(layout.rowRight() - 58, y).size(58, 18)
                    .build());
        }
        this.addRenderableWidget(Button.builder(
                Component.literal("screen.xiaoxiang_cultivation.close"),
                button -> this.onClose())
                .pos(layout.left() + layout.panelW() / 2 - 40, layout.closeY()).size(80, 20)
                .build());
    }

    private void teleport(SoulReaperTargetEntry entry) {
        ModNetwork.CHANNEL.sendToServer(new SoulReaperTeleportPacket(entry.targetId()));
        this.onClose();
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
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
            gfx.drawCenteredString(this.font, Component.literal("screen.xiaoxiang_cultivation.soul_reaper_targets.empty").withStyle(net.minecraft.ChatFormatting.GRAY),
                    layout.left() + layout.panelW() / 2, emptyY, -3160390);
        } else {
            this.renderRows(gfx, layout);
            this.renderScrollbar(gfx, layout);
        }
        super.render(gfx, mouseX, mouseY, partialTick);
    }

    private void renderRows(GuiGraphics gfx, ScreenLayout layout) {
        int textW = Math.max(80, layout.rowRight() - layout.rowLeft() - 58 - 16);
        int visible = this.visibleRows(layout);
        for (int i = 0; i < visible; ++i) {
            SoulReaperTargetEntry entry = this.targets.get(this.scrollOffset + i);
            int y = layout.rowTop() + i * ROW_H;
            gfx.fill(layout.rowLeft(), y, layout.rowRight(), y + ROW_H - 4, -14675429);
            gfx.fill(layout.rowLeft(), y, layout.rowLeft() + 2, y + ROW_H - 4, entry.playerTarget() ? -9972481 : -5022721);
            String typeKey = entry.playerTarget() ? "screen.xiaoxiang_cultivation.soul_reaper_targets.type.player" : "screen.xiaoxiang_cultivation.soul_reaper_targets.type.npc";
            MutableComponent titleLine = Component.literal("screen.xiaoxiang_cultivation.soul_reaper_targets.line.title")
                    .append(": ").append(entry.name())
                    .append(" (").append(Component.literal(typeKey)).append(")")
                    .withStyle(net.minecraft.ChatFormatting.GOLD);
            MutableComponent profile = Component.literal("screen.xiaoxiang_cultivation.soul_reaper_targets.line.profile")
                    .append(entry.gender()).append(", ")
                    .append(entry.identity()).append(", ")
                    .append(entry.realm())
                    .withStyle(net.minecraft.ChatFormatting.GRAY);
            MutableComponent location = Component.literal("screen.xiaoxiang_cultivation.soul_reaper_targets.line.location")
                    .append(entry.location())
                    .withStyle(net.minecraft.ChatFormatting.DARK_GRAY);
            this.drawTrimmed(gfx, titleLine, layout.rowLeft() + 8, y + 5, textW, -10870);
            this.drawTrimmed(gfx, profile, layout.rowLeft() + 8, y + 17, textW, -3160390);
            this.drawTrimmed(gfx, location, layout.rowLeft() + 8, y + 29, textW, -7502960);
        }
    }

    private void renderScrollbar(GuiGraphics gfx, ScreenLayout layout) {
        int visibleCapacity = layout.visibleRowCapacity();
        if (visibleCapacity <= 0 || this.targets.size() <= visibleCapacity) {
            return;
        }
        int barX = layout.left() + layout.panelW() - 8;
        int barTop = layout.rowTop();
        int barBottom = barTop + visibleCapacity * ROW_H - 4;
        gfx.fill(barX, barTop, barX + 3, barBottom, -13032918);
        int maxOffset = this.maxScrollOffset(visibleCapacity);
        int thumbH = Math.max(16, (barBottom - barTop) * visibleCapacity / this.targets.size());
        int thumbY = barTop + (barBottom - barTop - thumbH) * this.scrollOffset / Math.max(1, maxOffset);
        gfx.fill(barX, thumbY, barX + 3, thumbY + thumbH, -4703686);
    }

    private void drawTrimmed(GuiGraphics gfx, Component text, int x, int y, int maxWidth, int color) {
        String raw = text.getString();
        // 1.20.1 中 Font.substrByWidth 返回 FormattedText，导致 drawString 类型不匹配。
        // 退化为简单的字符截断。
        String trimmed = raw;
        while (this.font.width(trimmed) > maxWidth && trimmed.length() > 1) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        gfx.drawString(this.font, trimmed, x, y, color, false);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        ScreenLayout layout = this.layout();
        int maxOffset = this.maxScrollOffset(layout.visibleRowCapacity());
        if (maxOffset <= 0) {
            return super.mouseScrolled(mouseX, mouseY, delta);
        }
        int old = this.scrollOffset;
        this.scrollOffset = Math.max(0, Math.min(maxOffset, this.scrollOffset - (int) Math.signum(delta)));
        if (old != this.scrollOffset) {
            this.rebuildButtons();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private ScreenLayout layout() {
        int availableW = Math.max(1, this.width - 24);
        int panelW = Math.min(MAX_PANEL_W, availableW);
        if (panelW < MIN_PANEL_W) {
            panelW = availableW;
        }
        int desiredRows = this.targets.isEmpty() ? 0 : Math.min(MAX_VISIBLE_ROWS, this.targets.size());
        int desiredH = this.targets.isEmpty() ? 156 : HEADER_H + desiredRows * ROW_H + FOOTER_H;
        int availableH = Math.max(1, this.height - 24);
        int panelH = Math.min(MAX_PANEL_H, Math.min(desiredH, availableH));
        if (availableH >= MIN_PANEL_H) {
            panelH = Math.max(MIN_PANEL_H, panelH);
        }
        int left = (this.width - panelW) / 2;
        int top = Math.max(SCREEN_MARGIN, (this.height - panelH) / 2);
        int closeY = top + panelH - CLOSE_BUTTON_H - 8;
        int rowTop = top + HEADER_H;
        int visibleCapacity = this.targets.isEmpty() ? 0 : Math.max(0, Math.min(MAX_VISIBLE_ROWS, (closeY - 2 - rowTop + 4) / ROW_H));
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

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /**
     * 勾魂目标屏布局信息（严格照搬原 mod 内部类 ScreenLayout）。
     */
    public record ScreenLayout(int panelW, int panelH, int left, int top, int rowTop, int closeY, int visibleRowCapacity) {
        public int right() {
            return left + panelW;
        }

        public int bottom() {
            return top + panelH;
        }

        public int rowLeft() {
            return left + 6;
        }

        public int rowRight() {
            return right() - 16;
        }
    }
}
