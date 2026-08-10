/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.FormattedCharSequence
 */
package com.friday.cultivation.client.screen;

import com.friday.cultivation.client.screen.widget.MiniCinnabarButton;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.SetTimeAccelerationPacket;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

public class TimeAccelerationChoiceScreen
extends Screen {
    private static final ResourceLocation MEDITATION_ICON = new ResourceLocation((String)"friday_cultivation", (String)"textures/gui/icon_meditation.png");
    private static final int PANEL_W = 150;
    private static final int PANEL_H = 164;
    private static final int BUTTON_W = 48;
    private static final int BUTTON_H = 14;
    private static final int BUTTON_GAP_X = 6;
    private static final int BUTTON_GAP_Y = 4;
    private static final int BUTTON_COLUMNS = 2;
    private static final float BODY_TEXT_SCALE = 0.72f;
    private static final int[] MULTIPLIERS = CultivationData.allowedTimeAccelerationMultipliers();
    private final Screen parent;

    public TimeAccelerationChoiceScreen(Screen parent) {
        super((Component)Component.translatable((String)"screen.friday_cultivation.time_acceleration.title"));
        this.parent = parent;
    }

    protected void init() {
        int left = this.width / 2 - 75;
        int top = this.height / 2 - 82;
        int buttonRows = (MULTIPLIERS.length + 2 - 1) / 2;
        int buttonsW = 102;
        int buttonsH = buttonRows * 14 + (buttonRows - 1) * 4;
        int startX = left + (150 - buttonsW) / 2;
        int startY = top + 164 - buttonsH - 8;
        for (int i = 0; i < MULTIPLIERS.length; ++i) {
            int multiplier = MULTIPLIERS[i];
            int col = i % 2;
            int row = i / 2;
            this.addRenderableWidget(new MiniCinnabarButton(startX + col * 54, startY + row * 18, 48, 14, (Component)Component.translatable((String)"screen.friday_cultivation.time_acceleration.option", (Object[])new Object[]{multiplier}), button -> this.choose(multiplier)));
        }
    }

    private void choose(int multiplier) {
        ModNetwork.CHANNEL.sendToServer((Object)new SetTimeAccelerationPacket(multiplier));
        Minecraft.getInstance().setScreen(this.parent);
    }

    public void addEntry(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gfx);
        int left = this.width / 2 - 75;
        int top = this.height / 2 - 82;
        gfx.fill(left - 2, top - 2, left + 150 + 2, top + 164 + 2, -10859978);
        gfx.fill(left, top, left + 150, top + 164, -923956);
        gfx.fill(left, top, left + 150, top + 2, -2504802);
        gfx.fill(left, top + 164 - 2, left + 150, top + 164, -10859978);
        gfx.fill(left, top, left + 2, top + 164, -2504802);
        gfx.fill(left + 150 - 2, top, left + 150, top + 164, -10859978);
        this.renderTitle(gfx, top + 13);
        this.drawWrappedScaled(gfx, (Component)Component.translatable((String)"screen.friday_cultivation.time_acceleration.body"), left + 12, top + 35, 126, -12766422, 4);
        this.drawWrappedScaled(gfx, (Component)Component.translatable((String)"screen.friday_cultivation.time_acceleration.scope"), left + 12, top + 68, 126, -9807288, 4);
        super.render(gfx, mouseX, mouseY, partialTick);
    }

    private void renderTitle(GuiGraphics gfx, int y) {
        int iconSize = 14;
        int gap = 4;
        int titleW = this.font.width((FormattedText)this.title);
        int groupW = iconSize + gap + titleW;
        int groupX = this.width / 2 - groupW / 2;
        gfx.blit(MEDITATION_ICON, groupX, y - 3, 0.0f, 0.0f, iconSize, iconSize, 16, 16);
        gfx.drawString(this.font, this.title, groupX + iconSize + gap, y, -15067628, false);
    }

    private void drawWrappedScaled(GuiGraphics gfx, Component text, int x, int y, int width, int color, int maxLines) {
        int logicalWidth = Math.max(1, (int)((float)width / 0.72f));
        List lines = this.font.split((FormattedText)text, logicalWidth);
        Objects.requireNonNull(this.font);
        int lineH = Math.max(7, (int)Math.ceil(9.0f * 0.72f) + 1);
        int count = Math.min(maxLines, lines.size());
        for (int i = 0; i < count; ++i) {
            gfx.pose().pushPose();
            gfx.pose().translate((float)x, (float)(y + i * lineH), 0.0f);
            gfx.pose().scale(0.72f, 0.72f, 1.0f);
            gfx.drawString(this.font, (FormattedCharSequence)lines.get(i), 0, 0, color, false);
            gfx.pose().popPose();
        }
    }

    public void onClose() {
        Minecraft.getInstance().setScreen(this.parent);
    }

    public boolean isPauseScreen() {
        return false;
    }
}

