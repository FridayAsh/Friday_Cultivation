package com.friday.cultivation.client.screen;

import com.friday.cultivation.client.screen.widget.CinnabarButton;
import com.friday.cultivation.config.ModClientConfig;
import com.friday.cultivation.config.ModCommonConfig;
import java.util.Objects;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

public class XiaoxiangConfigScreen
extends Screen {
    private static final int PANEL_WIDTH = 244;
    private static final int PANEL_HEIGHT = 224;
    private static final int INK = -1187649;
    private static final int PAPER = -299228904;
    private static final int BORDER = -6325691;
    private static final int ACCENT = -8064799;
    private static final int HINT = -4413050;
    private final Screen parent;
    private Button hudPositionButton;
    private Button spellTerrainButton;
    private Button spellTerrainForceButton;

    public XiaoxiangConfigScreen(Screen parent) {
        super(Component.translatable("screen.friday_cultivation.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = (this.width - 244) / 2;
        int y = (this.height - 224) / 2;
        int buttonW = 208;
        this.hudPositionButton = new CinnabarButton(x + 18, y + 43, buttonW, 20, XiaoxiangConfigScreen.hudPositionLabel(), button -> {
            ModClientConfig.setHudPosition(ModClientConfig.hudPosition().next());
            button.setMessage(XiaoxiangConfigScreen.hudPositionLabel());
        });
        this.addRenderableWidget(this.hudPositionButton);
        this.spellTerrainButton = new CinnabarButton(x + 18, y + 96, buttonW, 20, XiaoxiangConfigScreen.spellTerrainLabel(), button -> {
            ModCommonConfig.setSpellTerrainDestructionDefaultEnabled(!ModCommonConfig.spellTerrainDestructionDefaultEnabled());
            button.setMessage(XiaoxiangConfigScreen.spellTerrainLabel());
        });
        this.addRenderableWidget(this.spellTerrainButton);
        this.spellTerrainForceButton = new CinnabarButton(x + 18, y + 121, buttonW, 20, XiaoxiangConfigScreen.spellTerrainForceLabel(), button -> {
            ModCommonConfig.setSpellTerrainDestructionForceDisabled(!ModCommonConfig.spellTerrainDestructionForceDisabled());
            button.setMessage(XiaoxiangConfigScreen.spellTerrainForceLabel());
        });
        this.addRenderableWidget(this.spellTerrainForceButton);
        this.addRenderableWidget(new CinnabarButton(x + 62, y + 224 - 29, 120, 20, Component.translatable("gui.done"), button -> this.minecraft.setScreen(this.parent)));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        int x = (this.width - 244) / 2;
        int y = (this.height - 224) / 2;
        graphics.fill(x + 4, y + 5, x + 244 + 4, y + 224 + 5, 0x77000000);
        graphics.fill(x, y, x + 244, y + 224, -299228904);
        graphics.fill(x, y, x + 244, y + 2, -6325691);
        graphics.fill(x, y + 224 - 2, x + 244, y + 224, -6325691);
        graphics.fill(x, y, x + 2, y + 224, -6325691);
        graphics.fill(x + 244 - 2, y, x + 244, y + 224, -6325691);
        graphics.drawCenteredString(this.font, this.title, x + 122, y + 11, -1187649);
        graphics.drawString(this.font, Component.translatable("screen.friday_cultivation.config.hud"), x + 18, y + 31, -8064799, false);
        this.drawWrappedHint(graphics, Component.translatable("screen.friday_cultivation.config.hud.hint"), x + 18, y + 66, 208);
        graphics.drawString(this.font, Component.translatable("screen.friday_cultivation.config.server_rules"), x + 18, y + 83, -8064799, false);
        this.drawWrappedHint(graphics, Component.translatable("screen.friday_cultivation.config.server_rules.hint"), x + 18, y + 170, 208);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawWrappedHint(GuiGraphics graphics, Component text, int x, int y, int width) {
        Font textFont = this.font;
        for (FormattedCharSequence line : textFont.split(text, width)) {
            graphics.drawString(textFont, line, x, y, -4413050, false);
            Objects.requireNonNull(textFont);
            y += 9;
        }
    }

    private static Component hudPositionLabel() {
        return Component.translatable(ModClientConfig.hudPosition().isRightAligned() ? "screen.friday_cultivation.config.hud_position.right" : "screen.friday_cultivation.config.hud_position.left");
    }

    private static Component spellTerrainLabel() {
        return Component.translatable(ModCommonConfig.spellTerrainDestructionDefaultEnabled() ? "screen.friday_cultivation.config.spell_terrain.default_enabled" : "screen.friday_cultivation.config.spell_terrain.default_disabled");
    }

    private static Component spellTerrainForceLabel() {
        return Component.translatable(ModCommonConfig.spellTerrainDestructionForceDisabled() ? "screen.friday_cultivation.config.spell_terrain.force_enabled" : "screen.friday_cultivation.config.spell_terrain.force_disabled");
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}
