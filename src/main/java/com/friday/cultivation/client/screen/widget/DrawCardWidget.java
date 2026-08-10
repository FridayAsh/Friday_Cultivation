/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.AbstractButton
 *  net.minecraft.client.gui.narration.NarrationElementOutput
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.FormattedCharSequence
 *  net.minecraft.world.item.ItemStack
 *  org.jetbrains.annotations.NotNull
 */
package com.friday.cultivation.client.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.friday.cultivation.cultivation.Identity;
import com.friday.cultivation.cultivation.SpiritRoot;
import java.util.List;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DrawCardWidget
extends AbstractButton {
    private static final int BG_PARCHMENT = -923956;
    private static final int BG_SELECTED = -8022;
    private static final int BG_PLACEHOLDER = -2569816;
    private static final int BORDER_DARK = -10859978;
    private static final int BORDER_GOLD = -2056128;
    private static final int BORDER_HOVER = -3626888;
    private static final int INK_BLACK = -15067628;
    private static final int INK_MUTE = -9807288;
    private Identity identity;
    private SpiritRoot spiritRoot;
    private final Runnable onClick;
    private boolean selected = false;
    private final int[][] itemRects = new int[8][4];
    private int itemRectCount = 0;

    public DrawCardWidget(int x, int y, int width, int height, Runnable onClick) {
        super(x, y, width, height, (Component)Component.empty());
        this.onClick = onClick;
    }

    public void setContent(Identity identity, SpiritRoot spiritRoot) {
        this.identity = identity;
        this.spiritRoot = spiritRoot;
    }

    public boolean isRevealed() {
        return this.identity != null && this.spiritRoot != null;
    }

    public void setSelected(boolean v) {
        this.selected = v;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public void createLabelForValue() {
        if (this.onClick != null && this.isRevealed()) {
            this.onClick.run();
        }
    }

    public ItemStack getHoveredItemStack(int mouseX, int mouseY) {
        if (!this.isRevealed()) {
            return null;
        }
        for (int i = 0; i < this.itemRectCount; ++i) {
            List<ItemStack> items;
            int[] r = this.itemRects[i];
            if (mouseX < r[0] || mouseX >= r[2] || mouseY < r[1] || mouseY >= r[3] || i >= (items = this.identity.starterItems()).size()) continue;
            return items.get(i);
        }
        return null;
    }

    protected void getCategory(@NotNull GuiGraphics gfx, int mouseX, int mouseY, float partial) {
        boolean hover;
        boolean bl = hover = this.isHoveredOrFocused() && !this.selected && this.isRevealed();
        if (hover) {
            float scale = 1.03f;
            int cx = this.getX() + this.width / 2;
            int cy = this.getY() + this.height / 2;
            gfx.pose().pushPose();
            gfx.pose().translate((float)cx, (float)cy, 0.0f);
            gfx.pose().scale(scale, scale, 1.0f);
            gfx.pose().translate((float)(-cx), (float)(-cy), 0.0f);
        }
        int x = this.getX();
        int y = this.getY();
        int w = this.width;
        int h = this.height;
        int bg = !this.isRevealed() ? -2569816 : (this.selected ? -8022 : -923956);
        gfx.fill(x, y, x + w, y + h, bg);
        Font font = Minecraft.getInstance().font;
        this.itemRectCount = 0;
        if (!this.isRevealed()) {
            DrawCardWidget.drawBorder(gfx, x, y, w, h, -10859978);
            String q = "?";
            int qScale = 3;
            int qW = font.width(q) * qScale;
            Objects.requireNonNull(font);
            int qH = 9 * qScale;
            gfx.pose().pushPose();
            gfx.pose().translate((double)x + (double)(w - qW) / 2.0, (double)y + (double)(h - qH) / 2.0, 0.0);
            gfx.pose().scale((float)qScale, (float)qScale, 1.0f);
            gfx.drawString(font, q, 0, 0, -9807288, false);
            gfx.pose().popPose();
        } else {
            int rarityRgb = DrawCardWidget.rarityRgbColor(this.spiritRoot.rarity());
            gfx.fill(x + 1, y + 1, x + w - 1, y + 4, rarityRgb);
            int borderColor = this.selected ? -2056128 : (this.isHoveredOrFocused() ? -3626888 : rarityRgb);
            DrawCardWidget.drawBorder(gfx, x, y, w, h, borderColor);
            ResourceLocation portraitTex = this.identity.portraitTexture();
            int portraitSize = 32;
            int portraitX = x + (w - portraitSize) / 2;
            int portraitY = y + 6;
            RenderSystem.enableBlend();
            gfx.blit(portraitTex, portraitX, portraitY, 0.0f, 0.0f, portraitSize, portraitSize, portraitSize, portraitSize);
            RenderSystem.disableBlend();
            int textLeft = x + 4;
            int textRight = x + w - 4;
            int innerW = textRight - textLeft;
            MutableComponent identityLine = Component.translatable((String)"screen.friday_cultivation.identity_draw.card.identity_line", (Object[])new Object[]{Component.translatable((String)this.identity.translationKey())});
            DrawCardWidget.drawTextScale(gfx, font, (Component)identityLine, textLeft, y + 41, innerW, -15067628, 0.7f);
            MutableComponent identityDesc = Component.translatable((String)this.identity.descriptionKey());
            DrawCardWidget.drawWrappedText(gfx, font, (Component)identityDesc, textLeft, y + 49, innerW, -9807288, 0.55f, 2);
            MutableComponent rootLine = Component.translatable((String)"screen.friday_cultivation.identity_draw.card.spirit_root_line", (Object[])new Object[]{Component.translatable((String)this.spiritRoot.translationKey())});
            DrawCardWidget.drawTextScale(gfx, font, (Component)rootLine, textLeft, y + 68, innerW, rarityRgb, 0.7f);
            MutableComponent rootDesc = Component.translatable((String)this.spiritRoot.tooltipKey());
            DrawCardWidget.drawWrappedText(gfx, font, (Component)rootDesc, textLeft, y + 76, innerW, -9807288, 0.55f, 4);
            List<ItemStack> items = this.identity.starterItems();
            MutableComponent itemsLabel = Component.translatable((String)"screen.friday_cultivation.identity_draw.card.items_label_count", (Object[])new Object[]{items.size()});
            DrawCardWidget.drawTextScale(gfx, font, (Component)itemsLabel, textLeft, y + 110, innerW, -15067628, 0.65f);
            if (!items.isEmpty()) {
                int iconSize = 12;
                int gap = 1;
                int maxShow = Math.min(items.size(), 7);
                int totalW = maxShow * iconSize + (maxShow - 1) * gap;
                int startX = x + (w - totalW) / 2;
                int iconY = y + 122;
                for (int i = 0; i < maxShow; ++i) {
                    int ix = startX + i * (iconSize + gap);
                    DrawCardWidget.drawItemScaled(gfx, items.get(i), ix, iconY, (float)iconSize / 16.0f);
                    if (i >= this.itemRects.length) continue;
                    this.itemRects[i][0] = ix;
                    this.itemRects[i][1] = iconY;
                    this.itemRects[i][2] = ix + iconSize;
                    this.itemRects[i][3] = iconY + iconSize;
                    this.itemRectCount = i + 1;
                }
                if (items.size() > maxShow) {
                    String more = "+" + (items.size() - maxShow);
                    int moreX = startX + totalW + 1;
                    gfx.pose().pushPose();
                    gfx.pose().translate((float)moreX, (float)(iconY + 3), 0.0f);
                    gfx.pose().scale(0.7f, 0.7f, 1.0f);
                    gfx.drawString(font, more, 0, 0, -9807288, false);
                    gfx.pose().popPose();
                }
            }
            if (this.selected) {
                gfx.drawString(font, "\u2726", x + 3, y + 3, -2056128, false);
            }
        }
        if (hover) {
            gfx.pose().popPose();
        }
    }

    private static void drawBorder(GuiGraphics gfx, int x, int y, int w, int h, int color) {
        gfx.fill(x, y, x + w, y + 1, color);
        gfx.fill(x, y + h - 1, x + w, y + h, color);
        gfx.fill(x, y, x + 1, y + h, color);
        gfx.fill(x + w - 1, y, x + w, y + h, color);
    }

    private static void drawTextScale(GuiGraphics gfx, Font font, Component text, int x, int y, int maxW, int color, float scale) {
        int rawMaxW = (int)((float)maxW / scale);
        Object s = text.getString();
        int sw = font.width((String)s);
        if (sw > rawMaxW) {
            while (sw > rawMaxW - font.width("\u2026") && ((String)s).length() > 1) {
                s = ((String)s).substring(0, ((String)s).length() - 1);
                sw = font.width((String)s + "\u2026");
            }
            s = (String)s + "\u2026";
        }
        gfx.pose().pushPose();
        gfx.pose().translate((float)x, (float)y, 0.0f);
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.drawString(font, (String)s, 0, 0, color, false);
        gfx.pose().popPose();
    }

    private static void drawWrappedText(GuiGraphics gfx, Font font, Component text, int x, int y, int maxW, int color, float scale, int maxLines) {
        int rawMaxW = (int)((float)maxW / scale);
        List lines = font.split((FormattedText)text, rawMaxW);
        Objects.requireNonNull(font);
        int lineH = (int)(9.0f * scale) + 1;
        int show = Math.min(lines.size(), maxLines);
        for (int i = 0; i < show; ++i) {
            FormattedCharSequence line = (FormattedCharSequence)lines.get(i);
            gfx.pose().pushPose();
            gfx.pose().translate((float)x, (float)(y + i * lineH), 0.0f);
            gfx.pose().scale(scale, scale, 1.0f);
            gfx.drawString(font, line, 0, 0, color, false);
            gfx.pose().popPose();
        }
        if (lines.size() > maxLines && show > 0) {
            int lastY = y + (show - 1) * lineH;
            gfx.pose().pushPose();
            gfx.pose().translate((float)(x + maxW - (int)((float)font.width("\u2026") * scale) - 1), (float)lastY, 0.0f);
            gfx.pose().scale(scale, scale, 1.0f);
            gfx.drawString(font, "\u2026", 0, 0, color, false);
            gfx.pose().popPose();
        }
    }

    private static void drawItemScaled(GuiGraphics gfx, ItemStack stack, int x, int y, float scale) {
        gfx.pose().pushPose();
        gfx.pose().translate((float)x, (float)y, 0.0f);
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.renderItem(stack, 0, 0);
        gfx.pose().popPose();
    }

    public static int rarityRgbColor(SpiritRoot.Rarity r) {
        return switch (r) {
            default -> throw new IncompatibleClassChangeError();
            case NORMAL -> -7829368;
            case R -> -12549889;
            case SR -> -2047936;
            case SSR -> -5213953;
            case SPECIAL -> -2080704;
        };
    }

    public static int rarityRgb(SpiritRoot.Rarity r) {
        return DrawCardWidget.rarityRgbColor(r);
    }

    public static ChatFormatting rarityChatColor(SpiritRoot.Rarity r) {
        return switch (r) {
            default -> throw new IncompatibleClassChangeError();
            case NORMAL -> ChatFormatting.GRAY;
            case R -> ChatFormatting.BLUE;
            case SR -> ChatFormatting.YELLOW;
            case SSR -> ChatFormatting.LIGHT_PURPLE;
            case SPECIAL -> ChatFormatting.RED;
        };
    }

    public Identity identity() {
        return this.identity;
    }

    public SpiritRoot spiritRoot() {
        return this.spiritRoot;
    }

    protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }

    @Override
    public void onPress() {
    }
}
