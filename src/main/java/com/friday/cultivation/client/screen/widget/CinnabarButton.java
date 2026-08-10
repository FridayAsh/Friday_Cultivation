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

public class CinnabarButton
extends Button {
    private static final int CINNABAR = -4966087;
    private static final int CINNABAR_HOVER = -3649456;
    private static final int CINNABAR_DISABLED = -9813952;
    private static final int INK_BLACK = -15067628;
    private static final int CINNABAR_LIGHT = -2725013;
    private static final int CINNABAR_DEEP = -7723482;
    private static final int DISABLED_LIGHT = -7708576;
    private static final int DISABLED_DEEP = -12966362;
    private static final int TEXT_LIGHT = -726312;
    private static final int TEXT_DISABLED = -3888992;

    public CinnabarButton(int x, int y, int width, int height, Component msg, Button.OnPress onPress) {
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
        gfx.fill(x, y, x + w, y + h, bg);
        gfx.fill(x, y, x + w, y + 2, -15067628);
        gfx.fill(x, y + h - 2, x + w, y + h, -15067628);
        gfx.fill(x, y, x + 2, y + h, -15067628);
        gfx.fill(x + w - 2, y, x + w, y + h, -15067628);
        int insetLight = !enabled ? -7708576 : (pressed ? -7723482 : -2725013);
        int insetDark = !enabled ? -12966362 : (pressed ? -2725013 : -7723482);
        gfx.fill(x + 2, y + 2, x + w - 2, y + 3, insetLight);
        gfx.fill(x + 2, y + 2, x + 3, y + h - 2, insetLight);
        gfx.fill(x + 2, y + h - 3, x + w - 2, y + h - 2, insetDark);
        gfx.fill(x + w - 3, y + 2, x + w - 2, y + h - 2, insetDark);
        Font font = Minecraft.getInstance().font;
        int textColor = enabled ? -726312 : -3888992;
        Component msg = this.getMessage();
        int textW = font.width((FormattedText)msg);
        int availW = w - 6;
        float scale = textW > availW ? Math.max(0.6f, (float)availW / (float)textW) : 1.0f;
        int scaledW = (int)((float)textW * scale);
        Objects.requireNonNull(font);
        int scaledLineH = (int)(9.0f * scale);
        int textX = x + (w - scaledW) / 2;
        int textY = y + (h - scaledLineH) / 2 + 1;
        if (scale < 1.0f) {
            gfx.pose().pushPose();
            gfx.pose().translate((float)textX, (float)textY, 0.0f);
            gfx.pose().scale(scale, scale, 1.0f);
            gfx.drawString(font, msg, 0, 0, textColor, false);
            gfx.pose().popPose();
        } else {
            gfx.drawString(font, msg, textX, textY, textColor, false);
        }
    }

    private boolean isMouseDown() {
        long window = Minecraft.getInstance().getWindow().getWindow();
        return GLFW.glfwGetMouseButton((long)window, (int)0) == 1;
    }
}

