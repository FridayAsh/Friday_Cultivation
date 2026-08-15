package com.friday.cultivation.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.friday.cultivation.config.ModClientConfig;
import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.PhysiqueBonusHelper;
import com.friday.cultivation.cultivation.QiElement;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.registry.ModDimensions;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public final class CultivationHud {
    public static final IGuiOverlay OVERLAY = (gui, graphics, partialTick, screenWidth, screenHeight) -> CultivationHud.render(graphics, screenWidth);

    private static final ResourceLocation BLOOD_EMPTY = new ResourceLocation("friday_cultivation", "textures/gui/blood_empty.png");
    private static final ResourceLocation BLOOD_FILL = new ResourceLocation("friday_cultivation", "textures/gui/blood_fill.png");
    private static final ResourceLocation BATTERY_EMPTY = new ResourceLocation("friday_cultivation", "textures/gui/battery_empty.png");
    private static final ResourceLocation BATTERY_FILL_W = new ResourceLocation("friday_cultivation", "textures/gui/battery_fill_w.png");
    private static final String SPIRIT_ROOT_ICON_PREFIX = "textures/gui/spirit_root/";

    private static final int HUD_X = 6;
    private static final int HUD_Y = 6;
    private static final int HUD_WIDTH = 140;
    private static final int HUD_HEIGHT = 96;
    private static final int BAR_WIDTH = 96;
    private static final int BAR_HEIGHT = 6;
    private static final int AVATAR_SIZE = 24; // 8 * 3
    private static final int GOLD_TEXT = -1456016;

    private CultivationHud() {
    }

    private static void render(GuiGraphics graphics, int screenWidth) {
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
        int y = HUD_Y;

        RenderSystem.enableBlend();

        // 统一暗色底版（像素风，保证文字/贴图可读）
        graphics.fill(x, y, x + HUD_WIDTH, y + HUD_HEIGHT, 0x88000000);

        // 1. 2D 头像（电池护盾风格：3x 缩放 8x8 脸 + 帽层）
        drawHead(graphics, player, x + 4, y + 4);

        // 2. 玩家名字（0.8 缩放，白色）
        Component name = player.getDisplayName();
        drawScaled(graphics, mc, name, x + 32, y + 5, 0.8f, -1, true);

        // 3. 生命值条（blood_empty + blood_fill 贴图）
        renderHealthBar(graphics, x + 32, y + 15, BAR_WIDTH, BAR_HEIGHT, player.getHealth(), player.getMaxHealth());

        // 4. 境界行（保留）
        Realm realm = data.getRealm();
        MutableComponent realmLine = data.isLooseImmortal()
                ? Component.translatable("hud.friday_cultivation.realm", Component.translatable("realm.friday_cultivation.loose_immortal.level." + data.getLooseImmortalTribulations()))
                : (realm == Realm.MORTAL
                        ? Component.translatable("hud.friday_cultivation.realm", realm.displayName())
                        : Component.translatable("hud.friday_cultivation.realm_full", realm.displayName(), data.getSubStage().displayName()));
        drawRealmText(graphics, mc, realmLine, x + 32, y + 24, 100);

        // 5. 修为条（battery_empty 底 + 金色填充）
        long curCult = data.getCultivationProgress();
        long maxCult = data.getMaxCultivation();
        Component cultText = Component.translatable("hud.friday_cultivation.cultivation", curCult, maxCult);
        renderTextureBar(graphics, mc, x + 32, y + 32, BAR_WIDTH, BAR_HEIGHT, curCult, maxCult, 1.0f, 0.65f, 0.15f, cultText);

        // 6. 灵气条（battery_empty 底 + 蓝色填充）
        long curQi = data.getCurrentQi();
        long maxQi = data.getMaxQi();
        Component qiText = Component.translatable("hud.friday_cultivation.qi", curQi, maxQi);
        renderTextureBar(graphics, mc, x + 32, y + 40, BAR_WIDTH, BAR_HEIGHT, curQi, maxQi, 0.25f, 0.65f, 1.0f, qiText);

        // 7. 状态行（全部保留）
        int statusY = y + 50;
        if (data.canBreakthrough()) {
            MutableComponent bt = Component.translatable("hud.friday_cultivation.breakthrough_ready").withStyle(ChatFormatting.GOLD);
            drawPlainStatus(graphics, mc, bt, x + 8, statusY, 124, -11930);
            statusY += 9;
        }
        if (data.isMeditating()) {
            MutableComponent med = Component.translatable("hud.friday_cultivation.meditating").withStyle(ChatFormatting.GREEN);
            drawPlainStatus(graphics, mc, med, x + 8, statusY, 124, -7471203, false);
            statusY += 9;
        }
        long gameTime;
        if (data.hasActiveInverseFiveElementMark(gameTime = player.level().getGameTime())) {
            drawInverseFiveElementStatus(graphics, mc, data, gameTime, x + 8, statusY, 124);
            statusY += 10;
        }
        if (data.isInTribulation()) {
            MutableComponent trib = Component.translatable("hud.friday_cultivation.tribulation", Realm.formatTribulationCount(data.getTribulationStrikesRemaining(), data.getTribulationBoltsPerWave())).withStyle(ChatFormatting.RED);
            drawPlainStatus(graphics, mc, trib, x + 8, statusY, 124, -35483, false);
            statusY += 9;
        }
        if (soul) {
            renderSoulStatus(graphics, mc, player, data, x, statusY);
        }

        RenderSystem.disableBlend();
    }

    private static int hudX(int screenWidth) {
        if (ModClientConfig.hudPosition().isRightAligned()) {
            return Math.max(HUD_X, screenWidth - HUD_WIDTH - HUD_X);
        }
        return HUD_X;
    }

    private static void drawHead(GuiGraphics graphics, LocalPlayer player, int x, int y) {
        ResourceLocation skin = player.getSkinTextureLocation();
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0f);
        graphics.pose().scale(3.0f, 3.0f, 1.0f);
        graphics.blit(skin, 0, 0, 8, 8, 8.0f, 8.0f, 8, 8, 64, 64);
        graphics.blit(skin, 0, 0, 8, 8, 40.0f, 8.0f, 8, 8, 64, 64);
        graphics.pose().popPose();
    }

    private static void renderHealthBar(GuiGraphics graphics, int x, int y, int width, int height, float value, float max) {
        if (max <= 0.0f) max = 1.0f;
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        graphics.blit(BLOOD_EMPTY, x, y, 0.0f, 0.0f, width, height, 96, 6);

        int target = (int)((double)width * (double)value / (double)max);
        if (target > 0) {
            int clip = (int)(1.0 * (double)width / 96.0 * 3.0);
            if (target >= 96) {
                graphics.blit(BLOOD_FILL, x, y, 0.0f, 0.0f, 96, height, 96, 6);
            } else if (target <= clip) {
                graphics.blit(BLOOD_FILL, x, y, 0.0f, 0.0f, target, height, 96, 6);
            } else {
                graphics.blit(BLOOD_FILL, x, y, 0.0f, 0.0f, clip, height, 96, 6);
                graphics.blit(BLOOD_FILL, x + clip, y, (float)(96 - target), 0.0f, target, height, 96, 6);
            }
        }
    }

    private static void renderTextureBar(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height, long current, long max, float r, float g, float b, Component text) {
        if (max <= 0L) max = 1L;
        int cells = 5;
        int cellW = width / cells;
        long cellMax = max / cells;
        if (cellMax <= 0L) cellMax = 1L;

        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        for (int i = 0; i < cells; ++i) {
            long val = current - i * cellMax;
            if (val < 0L) val = 0L;
            if (val > cellMax) val = cellMax;
            int cx = x + i * cellW;
            // 底
            renderBarCell(graphics, cx, y, cellW, height, BATTERY_EMPTY, 21, 6, cellMax, cellMax);
            // 填充（带颜色）
            if (val > 0L) {
                graphics.setColor(r, g, b, 1.0f);
                renderBarCell(graphics, cx, y, cellW, height, BATTERY_FILL_W, 21, 6, val, cellMax);
                graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            }
        }
        drawCenteredScaledInRect(graphics, mc, text, x, y, width, height, 0.5f, -1, true);
    }

    private static void renderBarCell(GuiGraphics graphics, int x, int y, int w, int h, ResourceLocation tex, int texW, int texH, long value, long max) {
        if (value <= 0L || max <= 0L) return;
        int target = (int)((double)w * (double)value / (double)max);
        if (target <= 0) return;
        graphics.blit(tex, x, y, 0.0f, 0.0f, target, h, texW, texH);
    }

    private static void renderSoulStatus(GuiGraphics graphics, Minecraft mc, LocalPlayer player, CultivationData data, int x, int statusY) {
        MutableComponent title = Component.translatable("hud.friday_cultivation.soul.title");
        drawPlainStatus(graphics, mc, title, x + 8, statusY, 124, -4724737, true);
        statusY += 9;
        boolean inDifu = player.level().dimension() == ModDimensions.DIFU;
        if (!inDifu) {
            MutableComponent hint = Component.translatable("hud.friday_cultivation.soul.go_difu_hint");
            drawPlainStatus(graphics, mc, hint, x + 8, statusY, 124, -4151578, false);
        } else if (data.isReincarnationReady()) {
            MutableComponent ready = Component.translatable("hud.friday_cultivation.soul.can_reincarnate");
            drawPlainStatus(graphics, mc, ready, x + 8, statusY, 124, -11930, false);
        } else {
            int remainTicks = Math.max(0, 1200 - data.getDifuTicks());
            int totalSec = (remainTicks + 19) / 20;
            String timeStr = String.format("%d:%02d", totalSec / 60, totalSec % 60);
            MutableComponent cd = Component.translatable("hud.friday_cultivation.soul.countdown", timeStr);
            drawPlainStatus(graphics, mc, cd, x + 8, statusY, 124, -1456016, false);
        }
    }

    private static void drawRealmText(GuiGraphics graphics, Minecraft mc, Component text, int x, int y, int width) {
        graphics.fill(x, y + 7, x + Math.min(width, 58), y + 8, 673454866);
        drawLeftScaled(graphics, mc, text, x, y, width, 0.64f, GOLD_TEXT, true);
    }

    private static void drawInverseFiveElementStatus(GuiGraphics graphics, Minecraft mc, CultivationData data, long gameTime, int x, int y, int width) {
        QiElement next = PhysiqueBonusHelper.nextInverseElement(data.getInverseFiveElementMark());
        graphics.blit(spiritRootIcon(next), x, y, 8, 8, 0.0f, 0.0f, 16, 16, 16, 16);
        int stacks = data.getActiveInverseFiveElementStacks(gameTime);
        MutableComponent text = Component.translatable("hud.friday_cultivation.inverse_five_elements_next", next.displayName(), stacks);
        drawLeftScaled(graphics, mc, text, x + 10, y + 1, width - 10, 0.58f, -8064799, true);
    }

    private static ResourceLocation spiritRootIcon(QiElement element) {
        String id = element == QiElement.PURE ? "none" : element.id();
        return new ResourceLocation("friday_cultivation", SPIRIT_ROOT_ICON_PREFIX + id + ".png");
    }

    private static void drawPlainStatus(GuiGraphics graphics, Minecraft mc, Component text, int x, int y, int width, int color) {
        drawPlainStatus(graphics, mc, text, x, y, width, color, true);
    }

    private static void drawPlainStatus(GuiGraphics graphics, Minecraft mc, Component text, int x, int y, int width, int color, boolean shadow) {
        drawCenteredScaled(graphics, mc, text, x, y, width, 0.62f, color, shadow);
    }

    private static void drawCenteredScaled(GuiGraphics graphics, Minecraft mc, Component text, int x, int y, int width, float scale, int color, boolean shadow) {
        int textWidth = mc.font.width((FormattedText)text);
        float actualScale = textWidth <= 0 ? scale : Math.min(scale, Math.max(0.62f, (float)width / (float)textWidth));
        int drawX = x + (width - (int)((float)textWidth * actualScale)) / 2;
        drawScaled(graphics, mc, text, drawX, y, actualScale, color, shadow);
    }

    private static void drawCenteredScaledInRect(GuiGraphics graphics, Minecraft mc, Component text, int x, int y, int width, int height, float scale, int color, boolean shadow) {
        int textWidth = mc.font.width((FormattedText)text);
        float actualScale = textWidth <= 0 ? scale : Math.min(scale, Math.max(0.18f, (float)width / (float)textWidth));
        int scaledW = (int)((float)textWidth * actualScale);
        int scaledH = (int)(9.0f * actualScale);
        int drawX = x + (width - scaledW) / 2;
        int drawY = y + Math.max(0, (height - scaledH) / 2);
        drawScaled(graphics, mc, text, drawX, drawY, actualScale, color, shadow);
    }

    private static void drawLeftScaled(GuiGraphics graphics, Minecraft mc, Component text, int x, int y, int width, float scale, int color, boolean shadow) {
        int textWidth = mc.font.width((FormattedText)text);
        float actualScale = textWidth <= 0 ? scale : Math.min(scale, Math.max(0.58f, (float)width / (float)textWidth));
        drawScaled(graphics, mc, text, x, y, actualScale, color, shadow);
    }

    private static void drawScaled(GuiGraphics graphics, Minecraft mc, Component text, int x, int y, float scale, int color, boolean shadow) {
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.drawString(mc.font, text, Math.round((float)x / scale), Math.round((float)y / scale), color, shadow);
        graphics.pose().popPose();
    }

    public static ResourceLocation overlayId() {
        return new ResourceLocation("friday_cultivation", "cultivation_hud");
    }
}
