package com.friday.cultivation.client.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

/**
 * 羊皮纸关闭按钮（严格照搬原模组 com.xiaoxiang.cultivation.client.screen.widget.ParchmentCloseButton）
 * 自绘面板 + 对角叉号，悬停变红，按下内缩。
 */
public class ParchmentCloseButton extends Button {
    private static final int BG_PANEL = -1517128;
    private static final int BG_PANEL_HOVER = -1056059;
    private static final int INK_BLACK = -15067628;
    private static final int BORDER_LIGHT = -2504802;
    private static final int BORDER_DARK = -10859978;
    private static final int CINNABAR = -4703686;

    public ParchmentCloseButton(int x, int y, int size, Button.OnPress onPress) {
        super(x, y, size, size, Component.literal("X"), onPress, DEFAULT_NARRATION);
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        int x = this.getX();
        int y = this.getY();
        int w = this.width;
        int h = this.height;
        boolean hovered = this.isHoveredOrFocused();
        boolean pressed = hovered && this.isMouseDown();
        int bg = hovered ? BG_PANEL_HOVER : BG_PANEL;
        gfx.fill(x, y, x + w, y + h, bg);
        gfx.fill(x, y, x + w, y + 1, INK_BLACK);
        gfx.fill(x, y + h - 1, x + w, y + h, INK_BLACK);
        gfx.fill(x, y, x + 1, y + h, INK_BLACK);
        gfx.fill(x + w - 1, y, x + w, y + h, INK_BLACK);
        int insetLight = pressed ? BORDER_DARK : BORDER_LIGHT;
        int insetDark = pressed ? BORDER_LIGHT : BORDER_DARK;
        gfx.fill(x + 1, y + 1, x + w - 1, y + 2, insetLight);
        gfx.fill(x + 1, y + 1, x + 2, y + h - 1, insetLight);
        gfx.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, insetDark);
        gfx.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, insetDark);
        int xColor = hovered ? CINNABAR : INK_BLACK;
        int padding = 4;
        int cx1 = x + padding;
        int cy1 = y + padding;
        int cx2 = x + w - padding;
        int cy2 = y + h - padding;
        this.drawDiagonalLine(gfx, cx1, cy1, cx2, cy2, xColor);
        this.drawDiagonalLine(gfx, cx1, cy2, cx2, cy1, xColor);
        if (pressed) {
            // empty if block
        }
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

    private boolean isMouseDown() {
        long window = Minecraft.getInstance().getWindow().getWindow();
        return GLFW.glfwGetMouseButton(window, 0) == 1;
    }
}
