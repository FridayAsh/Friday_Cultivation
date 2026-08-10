package com.friday.cultivation.client.screen.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * 关闭图标按钮（复刻原模组 CloseIconButton）
 * 绘制斜向 X 形关闭符号，悬停时变朱砂色。
 */
public class CloseIconButton extends Button {
    private static final int INK_BLACK = -15067628;
    private static final int CINNABAR = -4703686;

    public CloseIconButton(int x, int y, int size, Button.OnPress onPress) {
        super(x, y, size, size, Component.literal("X"), onPress, DEFAULT_NARRATION);
    }

    @Override
    protected void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        boolean hovered = this.isHovered();
        int color = hovered ? CINNABAR : INK_BLACK;
        int x = this.getX();
        int y = this.getY();
        int w = this.width;
        int h = this.height;
        int padding = 2;
        int x1 = x + padding;
        int y1 = y + padding;
        int x2 = x + w - padding - 1;
        int y2 = y + h - padding - 1;
        this.drawDiagonalLine(gfx, x1, y1, x2, y2, color);
        this.drawDiagonalLine(gfx, x1, y2, x2, y1, color);
    }

    private void drawDiagonalLine(GuiGraphics gfx, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;
        int cx = x1;
        int cy = y1;
        while (true) {
            gfx.fill(cx, cy, cx + 2, cy + 2, color);
            if (cx == x2 && cy == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                cx += sx;
            }
            if (e2 >= dx) continue;
            err += dx;
            cy += sy;
        }
    }
}
