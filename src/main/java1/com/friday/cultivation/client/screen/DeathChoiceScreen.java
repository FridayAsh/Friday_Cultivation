package com.friday.cultivation.client.screen;

import com.friday.cultivation.client.DeathSequenceClientEffects;
import com.friday.cultivation.network.DeathChoicePacket;
import com.friday.cultivation.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * 死亡选择屏 — 完全照搬原模组 com.xiaoxiang.cultivation.client.screen.DeathChoiceScreen。
 * 由 ClientDeathChoiceHooks.open 在服务端 OpenDeathChoicePacket 触发时调用。
 */
public class DeathChoiceScreen extends Screen {
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
        super(Component.translatable("deathScreen.title"));
        this.causeOfDeath = causeOfDeath;
    }

    @Override
    protected void init() {
        DeathSequenceClientEffects.clear();
        this.choiceButtons.clear();
        int buttonWidth = Math.min(BUTTON_WIDTH, Math.max(120, this.width - 40));
        int x = (this.width - buttonWidth) / 2;
        int y = Math.min(this.height - 88, this.height / 4 + 72);
        this.addChoiceButton(DeathChoicePacket.Choice.VANILLA_DEATH, Component.translatable("screen.friday_cultivation.death_choice.vanilla.title"), x, y, buttonWidth);
        this.addChoiceButton(DeathChoicePacket.Choice.GO_DIFU, Component.translatable("screen.friday_cultivation.death_choice.difu.title"), x, y + BUTTON_GAP, buttonWidth);
        this.addChoiceButton(DeathChoicePacket.Choice.WANDERING_SOUL, Component.translatable("screen.friday_cultivation.death_choice.wander.title"), x, y + BUTTON_GAP * 2, buttonWidth);
        this.updateButtonActivity();
    }

    private void addChoiceButton(DeathChoicePacket.Choice choice, Component label, int x, int y, int width) {
        Button button = Button.builder(label, b -> this.choose(choice)).bounds(x, y, width, BUTTON_HEIGHT).build();
        this.choiceButtons.add(button);
        this.addRenderableWidget(button);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.delayTicker < SAFETY_DELAY_TICKS) {
            ++this.delayTicker;
            this.updateButtonActivity();
        }
    }

    private void updateButtonActivity() {
        boolean active = !this.choiceSent && this.delayTicker >= SAFETY_DELAY_TICKS;
        for (Button button : this.choiceButtons) {
            button.active = active;
        }
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        gfx.fillGradient(0, 0, this.width, this.height, 0x60500000, -1600126976);
        gfx.pose().pushPose();
        gfx.pose().scale(2.0f, 2.0f, 2.0f);
        gfx.drawCenteredString(this.font, this.title, this.width / 2 / 2, 30, TITLE_COLOR);
        gfx.pose().popPose();
        int textY = 85;
        if (this.causeOfDeath != null) {
            gfx.drawCenteredString(this.font, this.causeOfDeath, this.width / 2, textY, TEXT_COLOR);
            textY += 18;
        }
        Component hoveredHint = this.hoveredHint(mouseX, mouseY);
        if (hoveredHint != null) {
            this.drawWrappedCentered(gfx, hoveredHint, this.width / 2, Math.max(textY, this.height / 4 + 152), Math.min(300, this.width - 28), HINT_COLOR);
        }
        super.render(gfx, mouseX, mouseY, partialTick);
    }

    @Nullable
    private Component hoveredHint(int mouseX, int mouseY) {
        for (int i = 0; i < this.choiceButtons.size(); ++i) {
            Button button = this.choiceButtons.get(i);
            if (!button.isMouseOver((double) mouseX, (double) mouseY)) continue;
            return switch (i) {
                case 0 -> Component.translatable("screen.friday_cultivation.death_choice.vanilla.desc");
                case 1 -> Component.translatable("screen.friday_cultivation.death_choice.difu.desc");
                default -> Component.translatable("screen.friday_cultivation.death_choice.wander.desc");
            };
        }
        return null;
    }

    private void drawWrappedCentered(GuiGraphics gfx, Component text, int cx, int y, int width, int color) {
        List<FormattedCharSequence> lines = this.font.split(text, width);
        int maxLines = Math.min(2, lines.size());
        for (int i = 0; i < maxLines; ++i) {
            FormattedCharSequence line = lines.get(i);
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
        ModNetwork.CHANNEL.sendToServer(new DeathChoicePacket(choice));
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
