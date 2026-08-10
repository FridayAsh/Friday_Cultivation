/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 */
package com.friday.cultivation.client.screen;

import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.ReincarnationChoicePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ReincarnationScreen
extends Screen {
    private static final int PANEL_W = 360;
    private static final int PANEL_H = 184;

    public ReincarnationScreen() {
        super((Component)Component.translatable((String)"screen.friday_cultivation.reincarnation.title"));
    }

    protected void init() {
        int cx = this.width / 2;
        int panelTop = this.height / 2 - 92;
        int btnY = panelTop + 184 - 30;
        this.addRenderableWidget(Button.builder(Component.translatable("screen.friday_cultivation.reincarnation.reincarnate"), b -> this.choose(true)).bounds(cx - 165, btnY, 160, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("screen.friday_cultivation.reincarnation.return_intact"), b -> this.choose(false)).bounds(cx + 5, btnY, 160, 20).build());
    }

    private void choose(boolean reincarnate) {
        ModNetwork.CHANNEL.sendToServer((Object)new ReincarnationChoicePacket(reincarnate));
        Minecraft.getInstance().setScreen(null);
    }

    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partial) {
        this.renderBackground(gfx);
        int cx = this.width / 2;
        int panelLeft = cx - 180;
        int panelTop = this.height / 2 - 92;
        gfx.fill(panelLeft, panelTop, panelLeft + 360, panelTop + 184, -301331950);
        gfx.fill(panelLeft, panelTop, panelLeft + 360, panelTop + 1, -8773069);
        gfx.fill(panelLeft, panelTop + 184 - 1, panelLeft + 360, panelTop + 184, -8773069);
        gfx.fill(panelLeft, panelTop, panelLeft + 1, panelTop + 184, -8773069);
        gfx.fill(panelLeft + 360 - 1, panelTop, panelLeft + 360, panelTop + 184, -8773069);
        gfx.drawCenteredString(this.font, this.title, cx, panelTop + 14, -2047936);
        int ty = panelTop + 40;
        for (int i = 1; i <= 4; ++i) {
            gfx.drawCenteredString(this.font, (Component)Component.translatable((String)("screen.friday_cultivation.reincarnation.desc" + i)), cx, ty, -3160390);
            ty += 13;
        }
        super.render(gfx, mouseX, mouseY, partial);
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    public boolean isPauseScreen() {
        return false;
    }
}

