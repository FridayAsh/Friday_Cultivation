/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.client.gui.overlay.IGuiOverlay
 */
package com.friday.cultivation.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.friday.cultivation.config.ModClientConfig;
import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.PhysiqueBonusHelper;
import com.friday.cultivation.cultivation.QiElement;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.registry.ModDimensions;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public final class CultivationHud {
    public static final IGuiOverlay OVERLAY = (gui, graphics, partialTick, screenWidth, screenHeight) -> CultivationHud.render(graphics, screenWidth);
    private static final ResourceLocation HUD_INK_BACKDROP = new ResourceLocation((String)"friday_cultivation", (String)"textures/gui/hud_ink_backdrop.png");
    private static final String SPIRIT_ROOT_ICON_PREFIX = "textures/gui/spirit_root/";
    private static AbstractTexture cachedBackdropTexture;
    private static final int HUD_X = 6;
    private static final int HUD_Y = 6;
    private static final int HUD_WIDTH = 120;
    private static final int HUD_MAIN_HEIGHT = 30;
    private static final int HUD_TEXTURE_SCALE = 2;
    private static final int HUD_TEXTURE_WIDTH = 240;
    private static final int HUD_TEXTURE_HEIGHT = 60;
    private static final int BACKDROP_X_OFFSET = -9;
    private static final int BACKDROP_Y_OFFSET = 1;
    private static final int BACKDROP_DISPLAY_WIDTH = 123;
    private static final int BACKDROP_DISPLAY_HEIGHT = 31;
    private static final int PORTRAIT_SIZE = 23;
    private static final int PORTRAIT_X = 5;
    private static final int PORTRAIT_Y = 4;
    private static final int TEXT_X = 32;
    private static final int REALM_Y = 7;
    private static final int CULT_Y = 15;
    private static final int QI_Y = 22;
    private static final int BAR_H = 6;
    private static final int GOLD_TEXT = -1456016;
    private static final int INK_BLACK = -16448509;
    private static final int QI_TOP = -9583434;
    private static final int QI_BOTTOM = -13729678;
    private static final int CULT_TOP = -928374;
    private static final int CULT_BOTTOM = -3631046;

    private CultivationHud() {
    }

    private static void render(GuiGraphics graphics, int screenWidth) {
        long gameTime;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui) {
            return;
        }
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        if (data == null) {
            return;
        }
        boolean soul = data.isSoulState();
        if (!soul && !data.hasEquippedTechnique()) {
            return;
        }
        int x = CultivationHud.hudX(screenWidth);
        int y = 6;
        Realm realm = data.getRealm();
        MutableComponent realmLine = data.isLooseImmortal() ? Component.translatable((String)"hud.friday_cultivation.realm", (Object[])new Object[]{Component.translatable((String)("realm.friday_cultivation.loose_immortal.level." + data.getLooseImmortalTribulations()))}) : (realm == Realm.MORTAL || realm == Realm.TRUE_IMMORTAL ? Component.translatable((String)"hud.friday_cultivation.realm", (Object[])new Object[]{realm.displayName()}) : Component.translatable((String)"hud.friday_cultivation.realm_full", (Object[])new Object[]{realm.displayName(), data.getSubStage().displayName()}));
        long curQi = data.getCurrentQi();
        long maxQi = data.getMaxQi();
        long curCult = data.getCultivationProgress();
        long maxCult = data.getMaxCultivation();
        MutableComponent cultText = Component.translatable((String)"hud.friday_cultivation.cultivation", (Object[])new Object[]{curCult, maxCult});
        MutableComponent qiText = Component.translatable((String)"hud.friday_cultivation.qi", (Object[])new Object[]{curQi, maxQi});
        int contentW = 82;
        CultivationHud.drawInkBackdrop(graphics, x, y);
        CultivationHud.drawPortrait(graphics, player, x + 5, y + 4);
        CultivationHud.drawRealmText(graphics, mc, (Component)realmLine, x + 32, y + 7, contentW);
        CultivationHud.drawBar(graphics, mc, x + 32, y + 15, contentW, curCult, maxCult, (Component)cultText, -928374, -3631046);
        CultivationHud.drawBar(graphics, mc, x + 32, y + 22, contentW, curQi, maxQi, (Component)qiText, -9583434, -13729678);
        int statusY = y + 30 + 1;
        if (data.canBreakthrough()) {
            MutableComponent bt = Component.translatable((String)"hud.friday_cultivation.breakthrough_ready").withStyle(ChatFormatting.GOLD);
            CultivationHud.drawPlainStatus(graphics, mc, (Component)bt, x + 8, statusY, 104, -11930);
            statusY += 9;
        }
        if (data.isMeditating()) {
            MutableComponent med = Component.translatable((String)"hud.friday_cultivation.meditating").withStyle(ChatFormatting.GREEN);
            CultivationHud.drawPlainStatus(graphics, mc, (Component)med, x + 8, statusY, 104, -7471203, false);
            statusY += 9;
        }
        if (data.hasActiveInverseFiveElementMark(gameTime = player.level().getGameTime())) {
            CultivationHud.drawInverseFiveElementStatus(graphics, mc, data, gameTime, x + 8, statusY, 104);
            statusY += 10;
        }
        if (data.isInTribulation()) {
            MutableComponent trib = Component.translatable((String)"hud.friday_cultivation.tribulation", (Object[])new Object[]{Realm.formatTribulationCount(data.getTribulationStrikesRemaining(), data.getTribulationBoltsPerWave())}).withStyle(ChatFormatting.RED);
            CultivationHud.drawPlainStatus(graphics, mc, (Component)trib, x + 8, statusY, 104, -35483, false);
            statusY += 9;
        }
        if (soul) {
            CultivationHud.renderSoulStatus(graphics, mc, player, data, x, statusY);
        }
    }

    private static int hudX(int screenWidth) {
        if (ModClientConfig.hudPosition().isRightAligned()) {
            return Math.max(6, screenWidth - 120 - 6);
        }
        return 6;
    }

    private static void renderSoulStatus(GuiGraphics graphics, Minecraft mc, LocalPlayer player, CultivationData data, int x, int statusY) {
        boolean inDifu;
        MutableComponent title = Component.translatable((String)"hud.friday_cultivation.soul.title");
        CultivationHud.drawPlainStatus(graphics, mc, (Component)title, x + 8, statusY, 104, -4724737, true);
        statusY += 9;
        boolean bl = inDifu = player.level().dimension() == ModDimensions.DIFU;
        if (!inDifu) {
            MutableComponent hint = Component.translatable((String)"hud.friday_cultivation.soul.go_difu_hint");
            CultivationHud.drawPlainStatus(graphics, mc, (Component)hint, x + 8, statusY, 104, -4151578, false);
        } else if (data.isReincarnationReady()) {
            MutableComponent ready = Component.translatable((String)"hud.friday_cultivation.soul.can_reincarnate");
            CultivationHud.drawPlainStatus(graphics, mc, (Component)ready, x + 8, statusY, 104, -11930, false);
        } else {
            int remainTicks = Math.max(0, 1200 - data.getDifuTicks());
            int totalSec = (remainTicks + 19) / 20;
            String timeStr = String.format("%d:%02d", totalSec / 60, totalSec % 60);
            MutableComponent cd = Component.translatable((String)"hud.friday_cultivation.soul.countdown", (Object[])new Object[]{timeStr});
            CultivationHud.drawPlainStatus(graphics, mc, (Component)cd, x + 8, statusY, 104, -1456016, false);
        }
    }

    private static void drawInkBackdrop(GuiGraphics graphics, int x, int y) {
        RenderSystem.enableBlend();
        try {
            CultivationHud.configureBackdropFilter();
            graphics.blit(HUD_INK_BACKDROP, x + -9, y + 1, 123, 31, 0.0f, 0.0f, 240, 60, 240, 60);
        }
        finally {
            RenderSystem.disableBlend();
        }
    }

    private static void configureBackdropFilter() {
        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(HUD_INK_BACKDROP);
        if (texture != cachedBackdropTexture) {
            texture.setBlurMipmap(true, false);
            cachedBackdropTexture = texture;
        }
    }

    private static void drawPortrait(GuiGraphics graphics, LocalPlayer player, int x, int y) {
        CultivationHud.drawOval(graphics, x - 1, y - 1, 25, 25, -1526529278);
        CultivationHud.drawOval(graphics, x, y, 23, 23, -118296088);
        ResourceLocation skin = player.getSkinTextureLocation();
        int face = 16;
        int faceX = x + 3;
        int faceY = y + 3;
        RenderSystem.enableBlend();
        graphics.blit(skin, faceX, faceY, face, face, 8.0f, 8.0f, 8, 8, 64, 64);
        graphics.blit(skin, faceX, faceY, face, face, 40.0f, 8.0f, 8, 8, 64, 64);
        RenderSystem.disableBlend();
        CultivationHud.drawOvalOutline(graphics, x - 1, y - 1, 25, 25, -15591659);
    }

    private static void drawRealmText(GuiGraphics graphics, Minecraft mc, Component text, int x, int y, int width) {
        graphics.fill(x, y + 7, x + Math.min(width, 58), y + 8, 673454866);
        CultivationHud.drawLeftScaled(graphics, mc, text, x, y, width, 0.64f, -1456016, true);
    }

    private static void drawBar(GuiGraphics graphics, Minecraft mc, int x, int y, int width, long current, long max, Component valueText, int topColor, int bottomColor) {
        int barX = x;
        int barY = y;
        int barW = Math.min(width, 72);
        int barH = 6;
        CultivationHud.fillPillRect(graphics, barX, barY, barW, barH, -670759933);
        int innerX = barX + 1;
        int innerY = barY + 1;
        int innerW = Math.max(1, barW - 2);
        int innerH = Math.max(1, barH - 2);
        CultivationHud.fillPillRect(graphics, innerX, innerY, innerW, innerH, -14739179);
        float ratio = max <= 0L ? 0.0f : (float)Math.min(1.0, Math.max(0.0, (double)current / (double)max));
        int filledW = (int)((float)innerW * ratio);
        if (filledW > 0) {
            int half = innerH / 2;
            CultivationHud.fillPillSegment(graphics, innerX, innerY, innerW, innerH, filledW, 0, half, topColor);
            CultivationHud.fillPillSegment(graphics, innerX, innerY, innerW, innerH, filledW, half, innerH, bottomColor);
            CultivationHud.fillPillSegment(graphics, innerX, innerY, innerW, innerH, filledW, 0, 1, 0x40FFFFFF);
            CultivationHud.fillPillSegment(graphics, innerX, innerY, innerW, innerH, filledW, innerH - 1, innerH, 0x33000000);
            for (int sx = innerX + 8; sx < innerX + filledW - 1; sx += 8) {
                graphics.fill(sx, innerY, sx + 1, innerY + innerH, 0x1F000000);
            }
        }
        CultivationHud.drawCenteredScaledInRect(graphics, mc, valueText, barX, barY, barW, barH, 0.46f, -1, true);
    }

    private static void drawInverseFiveElementStatus(GuiGraphics graphics, Minecraft mc, CultivationData data, long gameTime, int x, int y, int width) {
        QiElement next = PhysiqueBonusHelper.nextInverseElement(data.getInverseFiveElementMark());
        RenderSystem.enableBlend();
        graphics.blit(CultivationHud.spiritRootIcon(next), x, y, 8, 8, 0.0f, 0.0f, 16, 16, 16, 16);
        RenderSystem.disableBlend();
        int stacks = data.getActiveInverseFiveElementStacks(gameTime);
        MutableComponent text = Component.translatable((String)"hud.friday_cultivation.inverse_five_elements_next", (Object[])new Object[]{next.displayName(), stacks});
        CultivationHud.drawLeftScaled(graphics, mc, (Component)text, x + 10, y + 1, width - 10, 0.58f, -8064799, true);
    }

    private static ResourceLocation spiritRootIcon(QiElement element) {
        String id = element == QiElement.PURE ? "none" : element.id();
        return new ResourceLocation((String)"friday_cultivation", (String)(SPIRIT_ROOT_ICON_PREFIX + id + ".png"));
    }

    private static void drawPlainStatus(GuiGraphics graphics, Minecraft mc, Component text, int x, int y, int width, int color) {
        CultivationHud.drawPlainStatus(graphics, mc, text, x, y, width, color, true);
    }

    private static void drawPlainStatus(GuiGraphics graphics, Minecraft mc, Component text, int x, int y, int width, int color, boolean shadow) {
        CultivationHud.drawCenteredScaled(graphics, mc, text, x, y, width, 0.62f, color, shadow);
    }

    private static void drawStatus(GuiGraphics graphics, Minecraft mc, Component text, int x, int y, int width, int color) {
        graphics.fill(x + 1, y + 1, x + width + 1, y + 9, 0x30000000);
        graphics.fill(x, y, x + width, y + 8, -1978788332);
        graphics.fill(x + 1, y + 1, x + width - 1, y + 2, 607114953);
        graphics.fill(x, y + 1, x + 2, y + 7, color);
        CultivationHud.drawCenteredScaled(graphics, mc, text, x + 5, y + 1, width - 10, 0.62f, color, true);
    }

    private static void drawOval(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        double rx = (double)width / 2.0;
        double ry = (double)height / 2.0;
        double cx = (double)x + rx - 0.5;
        double cy = (double)y + ry - 0.5;
        for (int localY = 0; localY < height; ++localY) {
            int spanStart = -1;
            int yy = y + localY;
            for (int localX = 0; localX < width; ++localX) {
                int xx = x + localX;
                double dx = ((double)xx - cx) / rx;
                double dy = ((double)yy - cy) / ry;
                if (dx * dx + dy * dy <= 1.0) {
                    if (spanStart >= 0) continue;
                    spanStart = localX;
                    continue;
                }
                if (spanStart < 0) continue;
                graphics.fill(x + spanStart, yy, xx, yy + 1, color);
                spanStart = -1;
            }
            if (spanStart < 0) continue;
            graphics.fill(x + spanStart, yy, x + width, yy + 1, color);
        }
    }

    private static void drawOvalOutline(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        double rx = (double)width / 2.0;
        double ry = (double)height / 2.0;
        double cx = (double)x + rx - 0.5;
        double cy = (double)y + ry - 0.5;
        for (int localY = 0; localY < height; ++localY) {
            int spanStart = -1;
            int yy = y + localY;
            for (int localX = 0; localX < width; ++localX) {
                int xx = x + localX;
                double dx = ((double)xx - cx) / rx;
                double dy = ((double)yy - cy) / ry;
                double d = dx * dx + dy * dy;
                if (d <= 1.0 && d >= 0.86) {
                    if (spanStart >= 0) continue;
                    spanStart = localX;
                    continue;
                }
                if (spanStart < 0) continue;
                graphics.fill(x + spanStart, yy, xx, yy + 1, color);
                spanStart = -1;
            }
            if (spanStart < 0) continue;
            graphics.fill(x + spanStart, yy, x + width, yy + 1, color);
        }
    }

    private static void drawCenteredScaled(GuiGraphics graphics, Minecraft mc, Component text, int x, int y, int width, float scale, int color, boolean shadow) {
        int textWidth = mc.font.width((FormattedText)text);
        float actualScale = textWidth <= 0 ? scale : Math.min(scale, Math.max(0.62f, (float)width / (float)textWidth));
        int drawX = x + (width - (int)((float)textWidth * actualScale)) / 2;
        CultivationHud.drawScaled(graphics, mc, text, drawX, y, actualScale, color, shadow);
    }

    private static void drawCenteredScaledInRect(GuiGraphics graphics, Minecraft mc, Component text, int x, int y, int width, int height, float scale, int color, boolean shadow) {
        int textWidth = mc.font.width((FormattedText)text);
        float actualScale = textWidth <= 0 ? scale : Math.min(scale, Math.max(0.18f, (float)width / (float)textWidth));
        int scaledW = (int)((float)textWidth * actualScale);
        Objects.requireNonNull(mc.font);
        int scaledH = (int)(9.0f * actualScale);
        int drawX = x + (width - scaledW) / 2;
        int drawY = y + Math.max(0, (height - scaledH) / 2);
        CultivationHud.drawScaled(graphics, mc, text, drawX, drawY, actualScale, color, shadow);
    }

    private static void drawLeftScaled(GuiGraphics graphics, Minecraft mc, Component text, int x, int y, int width, float scale, int color, boolean shadow) {
        int textWidth = mc.font.width((FormattedText)text);
        float actualScale = textWidth <= 0 ? scale : Math.min(scale, Math.max(0.58f, (float)width / (float)textWidth));
        CultivationHud.drawScaled(graphics, mc, text, x, y, actualScale, color, shadow);
    }

    private static void fillPillRect(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        CultivationHud.fillPillSegment(graphics, x, y, width, height, width, 0, height, color);
    }

    private static void fillPillSegment(GuiGraphics graphics, int x, int y, int totalWidth, int height, int filledWidth, int yStart, int yEnd, int color) {
        int rightLimit = Math.min(filledWidth, totalWidth);
        if (rightLimit <= 0 || height <= 0 || totalWidth <= 0) {
            return;
        }
        for (int yy = Math.max(0, yStart); yy < Math.min(height, yEnd); ++yy) {
            int spanStart = -1;
            for (int xx = 0; xx < rightLimit; ++xx) {
                if (CultivationHud.insidePill(xx, yy, totalWidth, height)) {
                    if (spanStart >= 0) continue;
                    spanStart = xx;
                    continue;
                }
                if (spanStart < 0) continue;
                graphics.fill(x + spanStart, y + yy, x + xx, y + yy + 1, color);
                spanStart = -1;
            }
            if (spanStart < 0) continue;
            graphics.fill(x + spanStart, y + yy, x + rightLimit, y + yy + 1, color);
        }
    }

    private static boolean insidePill(int localX, int localY, int width, int height) {
        double radius = (double)height / 2.0;
        double centerY = (double)(height - 1) / 2.0;
        if ((double)localX >= radius && (double)localX < (double)width - radius) {
            return true;
        }
        double centerX = (double)localX < radius ? radius - 0.5 : (double)width - radius - 0.5;
        double dx = (double)localX - centerX;
        double dy = (double)localY - centerY;
        return dx * dx + dy * dy <= radius * radius;
    }

    private static void drawScaled(GuiGraphics graphics, Minecraft mc, Component text, int x, int y, float scale, int color, boolean shadow) {
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.drawString(mc.font, text, Math.round((float)x / scale), Math.round((float)y / scale), color, shadow);
        graphics.pose().popPose();
    }

    public static ResourceLocation overlayId() {
        return new ResourceLocation((String)"friday_cultivation", (String)"cultivation_hud");
    }
}

