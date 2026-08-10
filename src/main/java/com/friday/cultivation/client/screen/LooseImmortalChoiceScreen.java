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
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.util.FormattedCharSequence
 */
package com.friday.cultivation.client.screen;

import com.friday.cultivation.network.LooseImmortalChoicePacket;
import com.friday.cultivation.network.ModNetwork;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

public class LooseImmortalChoiceScreen
extends Screen {
    private static final int PANEL_W = 380;
    private static final int PANEL_H = 172;

    public LooseImmortalChoiceScreen() {
        super((Component)Component.translatable((String)"screen.friday_cultivation.loose_immortal_choice.title"));
    }

    protected void init() {
        int cx = this.width / 2;
        int top = this.height / 2 - 86;
        int buttonY = top + 172 - 32;
        this.addRenderableWidget(Button.builder((Component)Component.translatable((String)"screen.friday_cultivation.loose_immortal_choice.death"), button -> this.choose(false)).bounds(cx - 174, buttonY, 164, 20).build());
        this.addRenderableWidget(Button.builder((Component)Component.translatable((String)"screen.friday_cultivation.loose_immortal_choice.sanxian"), button -> this.choose(true)).bounds(cx + 10, buttonY, 164, 20).build());
    }

    private void choose(boolean becomeLooseImmortal) {
        ModNetwork.CHANNEL.sendToServer((Object)new LooseImmortalChoicePacket(becomeLooseImmortal));
        Minecraft.getInstance().setScreen(null);
    }

    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gfx);
        int cx = this.width / 2;
        int left = cx - 190;
        int top = this.height / 2 - 86;
        gfx.fill(left - 2, top - 2, left + 380 + 2, top + 172 + 2, -10676446);
        gfx.fill(left, top, left + 380, top + 172, -267713272);
        gfx.fill(left, top, left + 380, top + 1, -2928038);
        gfx.fill(left, top + 172 - 1, left + 380, top + 172, -12711150);
        gfx.fill(left, top, left + 1, top + 172, -2928038);
        gfx.fill(left + 380 - 1, top, left + 380, top + 172, -12711150);
        gfx.drawCenteredString(this.font, this.title, cx, top + 14, -11410);
        this.drawWrapped(gfx, (Component)Component.translatable((String)"screen.friday_cultivation.loose_immortal_choice.body"), left + 24, top + 42, 332, -1649726);
        this.drawWrapped(gfx, (Component)Component.translatable((String)"screen.friday_cultivation.loose_immortal_choice.warning"), left + 24, top + 88, 332, -33411);
        super.render(gfx, mouseX, mouseY, partialTick);
    }

    private void drawWrapped(GuiGraphics gfx, Component text, int x, int y, int width, int color) {
        List<FormattedCharSequence> lines = this.font.split((FormattedText)text, width);
        int dy = 0;
        for (FormattedCharSequence line : lines) {
            gfx.drawString(this.font, line, x, y + dy, color, false);
            dy += 11;
        }
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    public boolean isPauseScreen() {
        return false;
    }
}

