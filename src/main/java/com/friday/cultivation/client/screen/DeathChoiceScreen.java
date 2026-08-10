/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
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

import com.friday.cultivation.client.DeathSequenceClientEffects;
import com.friday.cultivation.network.DeathChoicePacket;
import com.friday.cultivation.network.ModNetwork;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

public class DeathChoiceScreen
extends Screen {
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 24;
    private static final int TITLE_COLOR = 0xFFFFFF;
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int HINT_COLOR = 0xA0A0A0;
    private static final int SAFETY_DELAY_TICKS = 20;
    @Nullable
    private final Component causeOfDeath;
    private final List<Button> choiceButtons = new ArrayList<Button>();
    private int delayTicker;
    private boolean choiceSent;

    public DeathChoiceScreen(@Nullable Component causeOfDeath) {
        super((Component)Component.translatable((String)"deathScreen.title"));
        this.causeOfDeath = causeOfDeath;
    }

    protected void init() {
        DeathSequenceClientEffects.clear();
        this.choiceButtons.clear();
        int buttonWidth = Math.min(200, Math.max(120, this.width - 40));
        int x = (this.width - buttonWidth) / 2;
        int y = Math.min(this.height - 88, this.height / 4 + 72);
        this.addChoiceButton(DeathChoicePacket.Choice.VANILLA_DEATH, (Component)Component.translatable((String)"screen.friday_cultivation.death_choice.vanilla.title"), x, y, buttonWidth);
        this.addChoiceButton(DeathChoicePacket.Choice.GO_DIFU, (Component)Component.translatable((String)"screen.friday_cultivation.death_choice.difu.title"), x, y + 24, buttonWidth);
        this.addChoiceButton(DeathChoicePacket.Choice.WANDERING_SOUL, (Component)Component.translatable((String)"screen.friday_cultivation.death_choice.wander.title"), x, y + 48, buttonWidth);
        this.updateButtonActivity();
    }

    private void addChoiceButton(DeathChoicePacket.Choice choice, Component label, int x, int y, int width) {
        Button button = Button.builder(label, b -> this.choose(choice)).bounds(x, y, width, 20).build();
        this.choiceButtons.add(button);
        this.addRenderableWidget(button);
    }

    public void tick() {
        super.tick();
        if (this.delayTicker < 20) {
            ++this.delayTicker;
            this.updateButtonActivity();
        }
    }

    private void updateButtonActivity() {
        boolean active = !this.choiceSent && this.delayTicker >= 20;
        for (Button button : this.choiceButtons) {
            button.active = active;
        }
    }

    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        Component hoveredHint;
        gfx.fillGradient(0, 0, this.width, this.height, 0x60500000, -1600126976);
        gfx.pose().pushPose();
        gfx.pose().scale(2.0f, 2.0f, 2.0f);
        gfx.drawCenteredString(this.font, this.title, this.width / 2 / 2, 30, 0xFFFFFF);
        gfx.pose().popPose();
        int textY = 85;
        if (this.causeOfDeath != null) {
            gfx.drawCenteredString(this.font, this.causeOfDeath, this.width / 2, textY, 0xFFFFFF);
            textY += 18;
        }
        if ((hoveredHint = this.hoveredHint(mouseX, mouseY)) != null) {
            this.drawWrappedCentered(gfx, hoveredHint, this.width / 2, Math.max(textY, this.height / 4 + 152), Math.min(300, this.width - 28), 0xA0A0A0);
        }
        super.render(gfx, mouseX, mouseY, partialTick);
    }

    @Nullable
    private Component hoveredHint(int mouseX, int mouseY) {
        for (int i = 0; i < this.choiceButtons.size(); ++i) {
            Button button = this.choiceButtons.get(i);
            if (!button.isMouseOver((double)mouseX, (double)mouseY)) continue;
            return switch (i) {
                case 0 -> Component.translatable((String)"screen.friday_cultivation.death_choice.vanilla.desc");
                case 1 -> Component.translatable((String)"screen.friday_cultivation.death_choice.difu.desc");
                default -> Component.translatable((String)"screen.friday_cultivation.death_choice.wander.desc");
            };
        }
        return null;
    }

    private void drawWrappedCentered(GuiGraphics gfx, Component text, int cx, int y, int width, int color) {
        List lines = this.font.split((FormattedText)text, width);
        int maxLines = Math.min(2, lines.size());
        for (int i = 0; i < maxLines; ++i) {
            FormattedCharSequence line = (FormattedCharSequence)lines.get(i);
            gfx.drawString(this.font, line, cx - this.font.width(line) / 2, y + i * 11, color, false);
        }
    }

    private void choose(DeathChoicePacket.Choice choice) {
        if (this.choiceSent) {
            return;
        }
        this.choiceSent = true;
        this.updateButtonActivity();
        DeathSequenceClientEffects.clear();
        ModNetwork.CHANNEL.sendToServer((Object)new DeathChoicePacket(choice));
        Minecraft.getInstance().setScreen(null);
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    public boolean isPauseScreen() {
        return false;
    }
}

