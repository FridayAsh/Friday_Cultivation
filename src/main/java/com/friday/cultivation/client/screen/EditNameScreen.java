/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  org.jetbrains.annotations.NotNull
 */
package com.friday.cultivation.client.screen;

import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.SetCultivationNamePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class EditNameScreen
extends Screen {
    private static final int PANEL_W = 220;
    private static final int PANEL_H = 116;
    private final Screen parent;
    private final String initial;
    private EditBox input;

    public EditNameScreen(Screen parent, String initial) {
        super((Component)Component.translatable((String)"screen.friday_cultivation.name_edit.title"));
        this.parent = parent;
        this.initial = initial == null ? "" : initial;
    }

    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;
        this.input = new EditBox(this.font, cx - 90, cy - 6, 180, 18, (Component)Component.translatable((String)"screen.friday_cultivation.name_edit.title"));
        this.input.setMaxLength(16);
        this.input.setValue(this.initial);
        this.addRenderableWidget(this.input);
        this.setInitialFocus((GuiEventListener)this.input);
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> this.confirm()).bounds(cx - 92, cy + 30, 86, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> this.onClose()).bounds(cx + 6, cy + 30, 86, 20).build());
    }

    private void confirm() {
        ModNetwork.CHANNEL.sendToServer((Object)new SetCultivationNamePacket(this.input.getValue()));
        this.onClose();
    }

    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 || keyCode == 335) {
            this.confirm();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partial) {
        this.renderBackground(g);
        int cx = this.width / 2;
        int cy = this.height / 2;
        int left = cx - 110;
        int top = cy - 58;
        g.fill(left, top, left + 220, top + 116, -267121400);
        g.fill(left, top, left + 220, top + 1, -9807288);
        g.fill(left, top + 116 - 1, left + 220, top + 116, -9807288);
        g.fill(left, top, left + 1, top + 116, -9807288);
        g.fill(left + 220 - 1, top, left + 220, top + 116, -9807288);
        g.drawCenteredString(this.font, this.title, cx, top + 10, -1456016);
        g.drawCenteredString(this.font, (Component)Component.translatable((String)"screen.friday_cultivation.name_edit.desc1"), cx, top + 26, -2570072);
        g.drawCenteredString(this.font, (Component)Component.translatable((String)"screen.friday_cultivation.name_edit.desc2"), cx, top + 38, -5201784);
        super.render(g, mouseX, mouseY, partial);
    }

    public boolean isPauseScreen() {
        return false;
    }
}

