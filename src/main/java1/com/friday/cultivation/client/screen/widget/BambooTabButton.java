package com.friday.cultivation.client.screen.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * 竹简风格标签页按钮（复刻原模组 BambooTabButton）
 * 木质/竹色背景，激活时高亮金色顶部，未激活时下移3像素。
 */
public class BambooTabButton extends Button {
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
        Font font = Minecraft.getInstance().font;
        int textW = font.width(this.getMessage());
        int availW = this.width - 2;
        return textW > availW ? Math.max(0.45f, (float) availW / (float) textW) : 1.0f;
    }

    @Override
    protected void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        boolean active = this.activeSupplier.get();
        boolean hovered = this.isHovered();
        int x = this.getX();
        int y = this.getY();
        int w = this.width;
        int h = this.height;
        int drawY = active ? y : (hovered ? y + 1 : y + 3);
        int bg = active ? BG_PAGE : (hovered ? WOOD_LIGHT_HOVER : WOOD_LIGHT);
        gfx.fill(x, drawY, x + w, drawY + h, bg);
        gfx.fill(x, drawY, x + w, drawY + 1, INK_BLACK);
        gfx.fill(x, drawY + h - 1, x + w, drawY + h, INK_BLACK);
        gfx.fill(x, drawY, x + 1, drawY + h, INK_BLACK);
        gfx.fill(x + w - 1, drawY, x + w, drawY + h, INK_BLACK);
        int insetLight = active ? BORDER_LIGHT : BAMBOO_HIGHLIGHT;
        int insetDark = active ? BORDER_DARK : WOOD_DARK;
        gfx.fill(x + 1, drawY + 1, x + w - 1, drawY + 2, insetLight);
        gfx.fill(x + 1, drawY + 1, x + 2, drawY + h - 1, insetLight);
        gfx.fill(x + 1, drawY + h - 2, x + w - 1, drawY + h - 1, insetDark);
        gfx.fill(x + w - 2, drawY + 1, x + w - 1, drawY + h - 1, insetDark);
        if (active) {
            gfx.fill(x + 2, drawY + 2, x + w - 2, drawY + 3, GOLD_BRIGHT);
        }
        Font font = Minecraft.getInstance().font;
        int textColor = active ? CINNABAR : INK_SOFT;
        Component msg = this.getMessage();
        int textW = font.width(msg);
        int availW = w - 2;
        Float forced = this.forcedScaleSupplier.get();
        float scale = forced != null && forced.floatValue() > 0.0f ? forced.floatValue() : (textW > availW ? Math.max(0.45f, (float) availW / (float) textW) : 1.0f);
        int scaledW = (int) ((float) textW * scale);
        Objects.requireNonNull(font);
        int scaledLineH = (int) (9.0f * scale);
        int textX = x + (w - scaledW) / 2;
        int textY = drawY + (h - scaledLineH) / 2 + 1;
        PoseStack pose = gfx.pose();
        pose.pushPose();
        if (scale < 1.0f) {
            pose.translate((float) textX, (float) textY, 0.0f);
            pose.scale(scale, scale, 1.0f);
            gfx.drawString(font, msg, 0, 0, textColor, false);
        } else {
            gfx.drawString(font, msg, textX, textY, textColor, false);
        }
        pose.popPose();
    }
}
