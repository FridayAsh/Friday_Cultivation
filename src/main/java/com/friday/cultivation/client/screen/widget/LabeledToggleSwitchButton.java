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

public class LabeledToggleSwitchButton
extends Button {
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

    protected void getCategory(@NotNull GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        int x = this.getX();
        int y = this.getY();
        boolean enabled = this.active && !this.locked;
        boolean hovered = this.isHoveredOrFocused() && enabled;
        int switchX = x + this.width - 22;
        int switchY = y + (this.height - 11) / 2;
        this.renderLabel(gfx, x, y, switchX);
        this.renderSwitch(gfx, switchX, switchY, enabled, hovered);
    }

    private void renderLabel(GuiGraphics gfx, int x, int y, int switchX) {
        Font font = Minecraft.getInstance().font;
        int maxW = Math.max(1, switchX - x - 2);
        int rawW = font.width((FormattedText)this.label);
        float scale = 0.76f;
        if (rawW > 0 && (float)rawW * scale > (float)maxW) {
            scale = Math.max(0.46f, (float)maxW / (float)rawW);
        }
        int textX = x;
        Objects.requireNonNull(font);
        int textY = y + (this.height - (int)Math.ceil(9.0f * scale)) / 2 + 1;
        gfx.pose().pushPose();
        gfx.pose().translate((float)textX, (float)textY, 0.0f);
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.drawString(font, this.label, 0, 0, this.active ? -3562934 : -7440800, false);
        gfx.pose().popPose();
    }

    private void renderSwitch(GuiGraphics gfx, int x, int y, boolean enabled, boolean hovered) {
        int bg = this.locked ? -10923444 : (this.toggled ? (hovered ? -3651005 : -4966087) : (hovered ? -7699591 : -8949398));
        gfx.fill(x, y, x + 22, y + 11, -15067628);
        gfx.fill(x + 1, y + 1, x + 22 - 1, y + 11 - 1, bg);
        gfx.fill(x + 1, y + 1, x + 22 - 1, y + 2, 0x66FFFFFF);
        gfx.fill(x + 1, y + 11 - 2, x + 22 - 1, y + 11 - 1, 0x66000000);
        int knobX = this.toggled && !this.locked ? x + 22 - 7 - 2 : x + 2;
        int knobY = y + 2;
        gfx.fill(knobX, knobY, knobX + 7, knobY + 7, enabled ? -528676 : -2043458);
        gfx.fill(knobX, knobY + 7 - 1, knobX + 7, knobY + 7, -2768222);
        gfx.fill(knobX + 7 - 1, knobY, knobX + 7, knobY + 7, 0x22000000);
    }
}

