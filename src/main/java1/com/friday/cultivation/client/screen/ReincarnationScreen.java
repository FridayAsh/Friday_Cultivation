package com.friday.cultivation.client.screen;

import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.ReincarnationChoicePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * 转世选择界面 — 完整复刻原模组 ReincarnationScreen。
 * 360×184面板，4行描述文字，两个按钮：
 * - "转世重生"（左侧）→ ReincarnationChoicePacket(true) → 重置一切重新投胎
 * - "无损返回"（右侧）→ ReincarnationChoicePacket(false) → 保留修为返回主世界
 */
public class ReincarnationScreen extends Screen {
    private static final int PANEL_W = 360;
    private static final int PANEL_H = 184;

    public ReincarnationScreen() {
        super(Component.translatable("screen.friday_cultivation.reincarnation.title"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int panelTop = this.height / 2 - 92;
        int btnY = panelTop + PANEL_H - 30;
        addRenderableWidget(Button.builder(Component.translatable("screen.friday_cultivation.reincarnation.reincarnate"), b -> choose(true)).bounds(cx - 165, btnY, 160, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.friday_cultivation.reincarnation.return_intact"), b -> choose(false)).bounds(cx + 5, btnY, 160, 20).build());
    }

    private void choose(boolean reincarnate) {
        ModNetwork.CHANNEL.sendToServer(new ReincarnationChoicePacket(reincarnate));
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public void render(@NotNull GuiGraphics gfx, int mouseX, int mouseY, float partial) {
        renderBackground(gfx);
        int cx = this.width / 2;
        int panelLeft = cx - 180;
        int panelTop = this.height / 2 - 92;
        gfx.fill(panelLeft, panelTop, panelLeft + PANEL_W, panelTop + PANEL_H, -301331950);
        gfx.fill(panelLeft, panelTop, panelLeft + PANEL_W, panelTop + 1, -8773069);
        gfx.fill(panelLeft, panelTop + PANEL_H - 1, panelLeft + PANEL_W, panelTop + PANEL_H, -8773069);
        gfx.fill(panelLeft, panelTop, panelLeft + 1, panelTop + PANEL_H, -8773069);
        gfx.fill(panelLeft + PANEL_W - 1, panelTop, panelLeft + PANEL_W, panelTop + PANEL_H, -8773069);
        gfx.drawCenteredString(this.font, this.title, cx, panelTop + 14, -2047936);
        int ty = panelTop + 40;
        for (int i = 1; i <= 4; ++i) {
            gfx.drawCenteredString(this.font, Component.translatable("screen.friday_cultivation.reincarnation.desc" + i), cx, ty, -3160390);
            ty += 13;
        }
        super.render(gfx, mouseX, mouseY, partial);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
