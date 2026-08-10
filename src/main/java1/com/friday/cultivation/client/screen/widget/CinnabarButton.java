package com.friday.cultivation.client.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

/**
 * 朱砂风格按钮（复刻原模组 CinnabarButton）
 * 朱红背景 + 墨色描边 + 内嵌明暗边框，文字超宽自动缩放。
 */
public class CinnabarButton extends Button {
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

    @Override
    protected void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        int x = this.getX();
        int y = this.getY();
        int w = this.width;
        int h = this.height;
        boolean enabled = this.active;
        boolean hovered = this.isHovered() && enabled;
        boolean pressed = hovered && this.isMouseDown();
        int bg = !enabled ? CINNABAR_DISABLED : (hovered ? CINNABAR_HOVER : CINNABAR);
        gfx.fill(x, y, x + w, y + h, bg);
        gfx.fill(x, y, x + w, y + 2, INK_BLACK);
        gfx.fill(x, y + h - 2, x + w, y + h, INK_BLACK);
        gfx.fill(x, y, x + 2, y + h, INK_BLACK);
        gfx.fill(x + w - 2, y, x + w, y + h, INK_BLACK);
        int insetLight = !enabled ? DISABLED_LIGHT : (pressed ? CINNABAR_DEEP : CINNABAR_LIGHT);
        int insetDark = !enabled ? DISABLED_DEEP : (pressed ? CINNABAR_LIGHT : CINNABAR_DEEP);
        gfx.fill(x + 2, y + 2, x + w - 2, y + 3, insetLight);
        gfx.fill(x + 2, y + 2, x + 3, y + h - 2, insetLight);
        gfx.fill(x + 2, y + h - 3, x + w - 2, y + h - 2, insetDark);
        gfx.fill(x + w - 3, y + 2, x + w - 2, y + h - 2, insetDark);
        Font font = Minecraft.getInstance().font;
        int textColor = enabled ? TEXT_LIGHT : TEXT_DISABLED;
        Component msg = this.getMessage();
        int textW = font.width(msg);
        int availW = w - 6;
        float scale = textW > availW ? Math.max(0.6f, (float) availW / (float) textW) : 1.0f;
        int scaledW = (int) ((float) textW * scale);
        Objects.requireNonNull(font);
        int scaledLineH = (int) (9.0f * scale);
        int textX = x + (w - scaledW) / 2;
        int textY = y + (h - scaledLineH) / 2 + 1;
        if (scale < 1.0f) {
            gfx.pose().pushPose();
            gfx.pose().translate((float) textX, (float) textY, 0.0f);
            gfx.pose().scale(scale, scale, 1.0f);
            gfx.drawString(font, msg, 0, 0, textColor, false);
            gfx.pose().popPose();
        } else {
            gfx.drawString(font, msg, textX, textY, textColor, false);
        }
    }

    private boolean isMouseDown() {
        long window = Minecraft.getInstance().getWindow().getWindow();
        return GLFW.glfwGetMouseButton(window, 0) == 1;
    }
}
