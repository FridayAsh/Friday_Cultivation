package com.friday.cultivation.client.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.Objects;

/**
 * 带标签的开关切换按钮（复刻原模组 LabeledToggleSwitchButton）
 * 左侧标签 + 右侧拨杆开关，支持锁定态。
 */
public class LabeledToggleSwitchButton extends Button {
    private static final int LABEL = -3562934;
    private static final int LABEL_DISABLED = -7440800;
    private static final int BORDER = -15067628;
    private static final int OFF_BG = -8949398;
    private static final int OFF_BG_HOVER = -7699591;
    private static final int ON_BG = -4966087;
    private static final int ON_BG_HOVER = -3651005;
    private static final int LOCKED_BG = -10923444;
    private static final int KNOB = -528676;
    private static final int KNOB_SHADOW = -2768222;
    private static final int SWITCH_LIGHT = 0x66FFFFFF;
    private static final int SWITCH_SHADOW = 0x66000000;
    private static final float LABEL_SCALE = 0.76f;
    private static final float MIN_LABEL_SCALE = 0.46f;
    private static final int SWITCH_W = 22;
    private static final int SWITCH_H = 11;
    private static final int KNOB_SIZE = 7;
    private static final int LABEL_SWITCH_GAP = 2;
    private final Component label;
    private boolean toggled;
    private boolean locked;

    public LabeledToggleSwitchButton(int x, int y, int width, int height, Component label, Button.OnPress onPress) {
        super(x, y, width, height, label, onPress, DEFAULT_NARRATION);
        this.label = label;
    }

    public void setState(boolean toggled, boolean locked) {
        this.toggled = toggled;
        this.locked = locked;
    }

    @Override
    protected void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        int x = this.getX();
        int y = this.getY();
        boolean enabled = this.active && !this.locked;
        boolean hovered = this.isHovered() && enabled;
        int switchX = x + this.width - SWITCH_W;
        int switchY = y + (this.height - SWITCH_H) / 2;
        this.renderLabel(gfx, x, y, switchX);
        this.renderSwitch(gfx, switchX, switchY, enabled, hovered);
    }

    private void renderLabel(GuiGraphics gfx, int x, int y, int switchX) {
        Font font = Minecraft.getInstance().font;
        int maxW = Math.max(1, switchX - x - LABEL_SWITCH_GAP);
        int rawW = font.width(this.label);
        float scale = LABEL_SCALE;
        if (rawW > 0 && (float) rawW * scale > (float) maxW) {
            scale = Math.max(MIN_LABEL_SCALE, (float) maxW / (float) rawW);
        }
        int textX = x;
        Objects.requireNonNull(font);
        int textY = y + (this.height - (int) Math.ceil(9.0f * scale)) / 2 + 1;
        gfx.pose().pushPose();
        gfx.pose().translate((float) textX, (float) textY, 0.0f);
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.drawString(font, this.label, 0, 0, this.active ? LABEL : LABEL_DISABLED, false);
        gfx.pose().popPose();
    }

    private void renderSwitch(GuiGraphics gfx, int x, int y, boolean enabled, boolean hovered) {
        int bg = this.locked ? LOCKED_BG : (this.toggled ? (hovered ? ON_BG_HOVER : ON_BG) : (hovered ? OFF_BG_HOVER : OFF_BG));
        gfx.fill(x, y, x + SWITCH_W, y + SWITCH_H, BORDER);
        gfx.fill(x + 1, y + 1, x + SWITCH_W - 1, y + SWITCH_H - 1, bg);
        gfx.fill(x + 1, y + 1, x + SWITCH_W - 1, y + 2, SWITCH_LIGHT);
        gfx.fill(x + 1, y + SWITCH_H - 2, x + SWITCH_W - 1, y + SWITCH_H - 1, SWITCH_SHADOW);
        int knobX = this.toggled && !this.locked ? x + SWITCH_W - KNOB_SIZE - 2 : x + 2;
        int knobY = y + 2;
        gfx.fill(knobX, knobY, knobX + KNOB_SIZE, knobY + KNOB_SIZE, enabled ? KNOB : -2043458);
        gfx.fill(knobX, knobY + KNOB_SIZE - 1, knobX + KNOB_SIZE, knobY + KNOB_SIZE, KNOB_SHADOW);
        gfx.fill(knobX + KNOB_SIZE - 1, knobY, knobX + KNOB_SIZE, knobY + KNOB_SIZE, 0x22000000);
    }
}
