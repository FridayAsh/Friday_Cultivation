/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.Button$OnPress
 *  net.minecraft.network.chat.Component
 *  org.jetbrains.annotations.NotNull
 *  org.lwjgl.glfw.GLFW
 */
package com.friday.cultivation.client.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public class ParchmentCloseButton
extends Button {
    private static final int BG_PANEL = -1517128;
    private static final int BG_PANEL_HOVER = -1056059;
    private static final int INK_BLACK = -15067628;
    private static final int BORDER_LIGHT = -2504802;
    private static final int BORDER_DARK = -10859978;
    private static final int CINNABAR = -4703686;

    public ParchmentCloseButton(int x, int y, int size, Button.OnPress onPress) {
        super(x, y, size, size, (Component)Component.literal((String)"X"), onPress, DEFAULT_NARRATION);
    }

    protected void getCategory(@NotNull GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        int x = this.getX();
        int y = this.getY();
        int w = this.width;
        int h = this.height;
        boolean hovered = this.isHoveredOrFocused();
        boolean pressed = hovered && this.isMouseDown();
        int bg = hovered ? -1056059 : -1517128;
        gfx.fill(x, y, x + w, y + h, bg);
        gfx.fill(x, y, x + w, y + 1, -15067628);
        gfx.fill(x, y + h - 1, x + w, y + h, -15067628);
        gfx.fill(x, y, x + 1, y + h, -15067628);
        gfx.fill(x + w - 1, y, x + w, y + h, -15067628);
        int insetLight = pressed ? -10859978 : -2504802;
        int insetDark = pressed ? -2504802 : -10859978;
        gfx.fill(x + 1, y + 1, x + w - 1, y + 2, insetLight);
        gfx.fill(x + 1, y + 1, x + 2, y + h - 1, insetLight);
        gfx.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, insetDark);
        gfx.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, insetDark);
        int xColor = hovered ? -4703686 : -15067628;
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
        return GLFW.glfwGetMouseButton((long)window, (int)0) == 1;
    }
}

