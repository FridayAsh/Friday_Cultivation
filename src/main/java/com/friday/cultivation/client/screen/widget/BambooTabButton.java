/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
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

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import org.jetbrains.annotations.NotNull;

public class BambooTabButton
extends Button {
    private static final int WOOD_LIGHT = -3559288;
    private static final int WOOD_LIGHT_HOVER = -2966635;
    private static final int WOOD_DARK = -8757442;
    private static final int BAMBOO_HIGHLIGHT = -1650016;
    private static final int BG_PAGE = -923956;
    private static final int INK_BLACK = -15067628;
    private static final int INK_SOFT = -12766422;
    private static final int CINNABAR = -4703686;
    private static final int BORDER_LIGHT = -2504802;
    private static final int BORDER_DARK = -10859978;
    private static final int GOLD_BRIGHT = -10496;
    private static final int INACTIVE_DROP = 3;
    private Supplier<Boolean> activeSupplier = () -> false;
    private Supplier<Float> forcedScaleSupplier = () -> null;

    public BambooTabButton(int x, int y, int width, int height, Component msg, Button.OnPress onPress) {
        super(x, y, width, height, msg, onPress, DEFAULT_NARRATION);
    }

    public BambooTabButton setActiveSupplier(Supplier<Boolean> sup) {
        this.activeSupplier = sup;
        return this;
    }

    public BambooTabButton setForcedScaleSupplier(Supplier<Float> sup) {
        this.forcedScaleSupplier = sup;
        return this;
    }

    public float computeAutoScale() {
        int availW;
        Font font = Minecraft.getInstance().font;
        int textW = font.width((FormattedText)this.getMessage());
        return textW > (availW = this.width - 2) ? Math.max(0.45f, (float)availW / (float)textW) : 1.0f;
    }

    protected void getCategory(@NotNull GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        boolean active = this.activeSupplier.get();
        boolean hovered = this.isHoveredOrFocused();
        int x = this.getX();
        int y = this.getY();
        int w = this.width;
        int h = this.height;
        int drawY = active ? y : (hovered ? y + 1 : y + 3);
        int bg = active ? -923956 : (hovered ? -2966635 : -3559288);
        gfx.fill(x, drawY, x + w, drawY + h, bg);
        gfx.fill(x, drawY, x + w, drawY + 1, -15067628);
        gfx.fill(x, drawY + h - 1, x + w, drawY + h, -15067628);
        gfx.fill(x, drawY, x + 1, drawY + h, -15067628);
        gfx.fill(x + w - 1, drawY, x + w, drawY + h, -15067628);
        int insetLight = active ? -2504802 : -1650016;
        int insetDark = active ? -10859978 : -8757442;
        gfx.fill(x + 1, drawY + 1, x + w - 1, drawY + 2, insetLight);
        gfx.fill(x + 1, drawY + 1, x + 2, drawY + h - 1, insetLight);
        gfx.fill(x + 1, drawY + h - 2, x + w - 1, drawY + h - 1, insetDark);
        gfx.fill(x + w - 2, drawY + 1, x + w - 1, drawY + h - 1, insetDark);
        if (active) {
            gfx.fill(x + 2, drawY + 2, x + w - 2, drawY + 3, -10496);
        }
        Font font = Minecraft.getInstance().font;
        int textColor = active ? -4703686 : -12766422;
        Component msg = this.getMessage();
        int textW = font.width((FormattedText)msg);
        int availW = w - 2;
        Float forced = this.forcedScaleSupplier.get();
        float scale = forced != null && forced.floatValue() > 0.0f ? forced.floatValue() : (textW > availW ? Math.max(0.45f, (float)availW / (float)textW) : 1.0f);
        int scaledW = (int)((float)textW * scale);
        Objects.requireNonNull(font);
        int scaledLineH = (int)(9.0f * scale);
        int textX = x + (w - scaledW) / 2;
        int textY = drawY + (h - scaledLineH) / 2 + 1;
        PoseStack pose = gfx.pose();
        pose.pushPose();
        if (scale < 1.0f) {
            pose.translate((float)textX, (float)textY, 0.0f);
            pose.scale(scale, scale, 1.0f);
            gfx.drawString(font, msg, 0, 0, textColor, false);
        } else {
            gfx.drawString(font, msg, textX, textY, textColor, false);
        }
        pose.popPose();
    }
}

