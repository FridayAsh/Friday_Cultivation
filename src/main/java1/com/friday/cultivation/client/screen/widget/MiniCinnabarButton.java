package com.friday.cultivation.client.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

/**
 * 迷你朱砂按钮（复刻原模组 MiniCinnabarButton）
 * 单像素内嵌边框 + 朱红背景，文字自动缩放。
 */
public class MiniCinnabarButton extends Button {
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

    @Override
    protected void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        int x = this.getX();
        int y = this.getY();
        int w = this.width;
        int h = this.height;
        boolean enabled = this.active;
        boolean hovered = this.isHovered() && enabled;
        boolean pressed = hovered && this.isMouseDown();
        int bg = !enabled ? BG_DISABLED : (hovered ? BG_HOVER : BG);
        gfx.fill(x, y, x + w, y + h, BORDER);
        gfx.fill(x + 1, y + 1, x + w - 1, y + h - 1, bg);
        int light = !enabled ? LIGHT_DISABLED : (pressed ? DEEP : LIGHT);
        int deep = !enabled ? DEEP_DISABLED : (pressed ? LIGHT : DEEP);
        gfx.fill(x + 1, y + 1, x + w - 1, y + 2, light);
        gfx.fill(x + 1, y + 1, x + 2, y + h - 1, light);
        gfx.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, deep);
        gfx.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, deep);
        Font font = Minecraft.getInstance().font;
        Component msg = this.getMessage();
        int textW = font.width(msg);
        int availW = Math.max(1, w - 4);
        float scale = Math.min(MAX_TEXT_SCALE, (float) availW / (float) Math.max(1, textW));
        scale = Math.max(MIN_TEXT_SCALE, scale);
        int scaledW = (int) Math.ceil((float) textW * scale);
        Objects.requireNonNull(font);
        int scaledH = (int) Math.ceil(9.0f * scale);
        int textX = x + (w - scaledW) / 2;
        int textY = y + (h - scaledH) / 2 + 1;
        gfx.pose().pushPose();
        gfx.pose().translate((float) textX, (float) textY, 0.0f);
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.drawString(font, msg, 0, 0, enabled ? TEXT : TEXT_DISABLED, false);
        gfx.pose().popPose();
    }

    private boolean isMouseDown() {
        long window = Minecraft.getInstance().getWindow().getWindow();
        return GLFW.glfwGetMouseButton(window, 0) == 1;
    }
}
