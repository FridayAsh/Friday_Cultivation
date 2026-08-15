package com.friday.cultivation.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.friday.cultivation.config.ModClientConfig;
import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.PhysiqueBonusHelper;
import com.friday.cultivation.cultivation.QiElement;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.registry.ModDimensions;
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
    private static final String SPIRIT_ROOT_ICON_PREFIX = "textures/gui/spirit_root/";

    private static final int HUD_X = 6;
    private static final int HUD_Y = 6;
    private static final int HUD_WIDTH = 140;
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

        // 1. 2D 头像（电池护盾风格）
        drawHead(graphics, player, x + 4, y + 4);

        // 2. 顶行：玩家名字 + 境界（同一行）
        int textBaseX = x + 32;
        int topY = y + 5;
        Component name = player.getDisplayName();
        float nameScale = 0.8f;
        int nameScaledW = (int)(mc.font.width((FormattedText)name) * nameScale);
        drawScaled(graphics, mc, name, textBaseX, topY, nameScale, -1, true);

        Realm realm = data.getRealm();
        MutableComponent realmLine = data.isLooseImmortal()
                ? Component.translatable("hud.friday_cultivation.realm", Component.translatable("realm.friday_cultivation.loose_immortal.level." + data.getLooseImmortalTribulations()))
                : (realm == Realm.MORTAL
                        ? Component.translatable("hud.friday_cultivation.realm", realm.displayName())
                        : Component.translatable("hud.friday_cultivation.realm_full", realm.displayName(), data.getSubStage().displayName()));
        int realmX = textBaseX + nameScaledW + 4;
        int realmAvailW = Math.max(20, x + HUD_WIDTH - realmX - 2);
        drawLeftScaled(graphics, mc, realmLine, realmX, topY + 1, realmAvailW, 0.6f, GOLD_TEXT, true);

        // 3. 生命值条
        renderHealthBar(graphics, textBaseX, y + 16, BAR_WIDTH, BAR_HEIGHT, player.getHealth(), player.getMaxHealth());

        // 4. 修为条（blood 贴图）
        long curCult = data.getCultivationProgress();
        long maxCult = data.getMaxCultivation();
        Component cultText = Component.translatable("hud.friday_cultivation.cultivation", curCult, maxCult);
        renderValueBar(graphics, mc, textBaseX, y + 24, BAR_WIDTH, BAR_HEIGHT, curCult, maxCult, cultText);

        // 5. 灵气条（blood 贴图）
        long curQi = data.getCurrentQi();
        long maxQi = data.getMaxQi();
        Component qiText = Component.translatable("hud.friday_cultivation.qi", curQi, maxQi);
        renderValueBar(graphics, mc, textBaseX, y + 32, BAR_WIDTH, BAR_HEIGHT, curQi, maxQi, qiText);

        // 6. 悟道条（blood 贴图，仅当 getWuDaoMax() > 0 时显示）
        int statusY = y + 42;
        long maxWudao = data.getWuDaoMax();
        if (maxWudao > 0L) {
            long curWudao = data.getWuDaoProgress();
            Component wudaoText = Component.translatable("hud.friday_cultivation.wudao", curWudao, maxWudao);
            renderValueBar(graphics, mc, textBaseX, y + 40, BAR_WIDTH, BAR_HEIGHT, curWudao, maxWudao, wudaoText);
            statusY = y + 50;
        }

        // 7. 状态行（全部保留）
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
            renderBloodFill(graphics, x, y, width, height, target);
        }
    }

    private static void renderValueBar(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height, long current, long max, Component text) {
        if (max <= 0L) max = 1L;
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        graphics.blit(BLOOD_EMPTY, x, y, 0.0f, 0.0f, width, height, 96, 6);
        int target = (int)((double)width * (double)current / (double)max);
        if (target > 0) {
            renderBloodFill(graphics, x, y, width, height, target);
        }
        drawCenteredScaledInRect(graphics, mc, text, x, y, width, height, 0.5f, -1, true);
    }

    private static void renderBloodFill(GuiGraphics graphics, int x, int y, int width, int height, int target) {
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
