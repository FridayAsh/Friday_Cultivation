/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.Button$OnPress
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  org.jetbrains.annotations.NotNull
 *  org.lwjgl.glfw.GLFW
 */
package com.friday.cultivation.client.screen.widget;

import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public class MiniCinnabarButton
extends Button {
    private static final int BORDER = -15067628;
    private static final int BG = -4966087;
    private static final int BG_HOVER = -3649456;
    private static final int BG_DISABLED = -9813952;
    private static final int LIGHT = -2725013;
    private static final int DEEP = -7723482;
    private static final int LIGHT_DISABLED = -7708576;
    private static final int DEEP_DISABLED = -12966362;
    private static final int TEXT = -726312;
    private static final int TEXT_DISABLED = -3888992;
    private static final float MAX_TEXT_SCALE = 0.72f;
    private static final float MIN_TEXT_SCALE = 0.52f;

    public MiniCinnabarButton(int x, int y, int width, int height, Component msg, Button.OnPress onPress) {
        super(x, y, width, height, msg, onPress, DEFAULT_NARRATION);
    }

    protected void getCategory(@NotNull GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        boolean pressed;
        int x = this.getX();
        int y = this.getY();
        int w = this.width;
        int h = this.height;
        boolean enabled = this.active;
        boolean hovered = this.isHoveredOrFocused() && enabled;
        boolean bl = pressed = hovered && this.isMouseDown();
        int bg = !enabled ? -9813952 : (hovered ? -3649456 : -4966087);
        gfx.fill(x, y, x + w, y + h, -15067628);
        gfx.fill(x + 1, y + 1, x + w - 1, y + h - 1, bg);
        int light = !enabled ? -7708576 : (pressed ? -7723482 : -2725013);
        int deep = !enabled ? -12966362 : (pressed ? -2725013 : -7723482);
        gfx.fill(x + 1, y + 1, x + w - 1, y + 2, light);
        gfx.fill(x + 1, y + 1, x + 2, y + h - 1, light);
        gfx.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, deep);
        gfx.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, deep);
        Font font = Minecraft.getInstance().font;
        Component msg = this.getMessage();
        int textW = font.width((FormattedText)msg);
        int availW = Math.max(1, w - 4);
        float scale = Math.min(0.72f, (float)availW / (float)Math.max(1, textW));
        scale = Math.max(0.52f, scale);
        int scaledW = (int)Math.ceil((float)textW * scale);
        Objects.requireNonNull(font);
        int scaledH = (int)Math.ceil(9.0f * scale);
        int textX = x + (w - scaledW) / 2;
        int textY = y + (h - scaledH) / 2 + 1;
        gfx.pose().pushPose();
        gfx.pose().translate((float)textX, (float)textY, 0.0f);
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.drawString(font, msg, 0, 0, enabled ? -726312 : -3888992, false);
        gfx.pose().popPose();
    }

    private boolean isMouseDown() {
        long window = Minecraft.getInstance().getWindow().getWindow();
        return GLFW.glfwGetMouseButton((long)window, (int)0) == 1;
    }
}

