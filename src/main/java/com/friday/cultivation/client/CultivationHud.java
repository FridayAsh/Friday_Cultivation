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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "friday_cultivation", value = Dist.CLIENT)
public class CultivationHud {
    public static final IGuiOverlay OVERLAY = (gui, graphics, partialTick, screenWidth, screenHeight) -> CultivationHud.render(graphics, screenWidth);

    private static final ResourceLocation BLOOD_EMPTY = new ResourceLocation("friday_cultivation", "textures/gui/blood_empty.png");
    private static final ResourceLocation BLOOD_FILL = new ResourceLocation("friday_cultivation", "textures/gui/blood_fill.png");
    private static final String SPIRIT_ROOT_ICON_PREFIX = "textures/gui/spirit_root/";

    private static final int HUD_X = 6;
    private static final int HUD_Y = 6;
    private static final int HUD_WIDTH = 140;
    private static final int BAR_HEIGHT = 6;
    private static final int AVATAR_SIZE = 24; // 8 * 3
    private static final int GOLD_TEXT = -1456016;

    // 各条宽度（递减）
    private static final int HEALTH_WIDTH = 100;
    private static final int CULT_WIDTH = 90;
    private static final int QI_WIDTH = 80;
    private static final int WUDAO_WIDTH = 70;

    // 项目设定色：顶/底渐变
    private static final int HEALTH_TOP = -1944235;
    private static final int HEALTH_BOTTOM = -5758944;
    private static final int CULT_TOP = -928374;
    private static final int CULT_BOTTOM = -3631046;
    private static final int QI_TOP = -9583434;
    private static final int QI_BOTTOM = -13729678;
    private static final int WUDAO_TOP = -8355712;
    private static final int WUDAO_BOTTOM = -10592674;

    private CultivationHud() {
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay() != VanillaGuiOverlay.PLAYER_HEALTH.type()
                && event.getOverlay() != VanillaGuiOverlay.ARMOR_LEVEL.type()
                && event.getOverlay() != VanillaGuiOverlay.FOOD_LEVEL.type()) {
            return;
        }
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
        event.setCanceled(true);
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

        // 1. 2D 头像
        drawHead(graphics, player, x + 4, y + 4);

        // 2. 顶行：玩家名字 + 境界（同一行，0.8f）
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
        drawLeftScaled(graphics, mc, realmLine, realmX, topY + 1, realmAvailW, 0.8f, GOLD_TEXT, true);

        // 3. 条带区域（生命→修为→灵气→悟道）
        int barY = y + 16;

        // 生命条
        renderHealthBar(graphics, textBaseX, barY, HEALTH_WIDTH, BAR_HEIGHT, player.getHealth(), player.getMaxHealth());
        barY += 8;

        // 修为条
        long curCult = data.getCultivationProgress();
        long maxCult = data.getMaxCultivation();
        Component cultText = Component.translatable("hud.friday_cultivation.cultivation", curCult, maxCult);
        renderValueBar(graphics, mc, textBaseX, barY, CULT_WIDTH, BAR_HEIGHT, curCult, maxCult, CULT_TOP, CULT_BOTTOM, CULT_TOP, cultText);
        barY += 8;

        // 灵气条
        long curQi = data.getCurrentQi();
        long maxQi = data.getMaxQi();
        Component qiText = Component.translatable("hud.friday_cultivation.qi", curQi, maxQi);
        renderValueBar(graphics, mc, textBaseX, barY, QI_WIDTH, BAR_HEIGHT, curQi, maxQi, QI_TOP, QI_BOTTOM, QI_TOP, qiText);
        barY += 8;

        // 悟道条（仅 getWuDaoMax() > 0）
        long maxWudao = data.getWuDaoMax();
        if (maxWudao > 0L) {
            long curWudao = data.getWuDaoProgress();
            Component wudaoText = Component.translatable("hud.friday_cultivation.wudao", curWudao, maxWudao);
            renderValueBar(graphics, mc, textBaseX, barY, WUDAO_WIDTH, BAR_HEIGHT, curWudao, maxWudao, WUDAO_TOP, WUDAO_BOTTOM, WUDAO_TOP, wudaoText);
            barY += 8;
        }

        // 4. 状态行
        int statusY = barY + 2;
        int statusW = HUD_WIDTH - 16;
        if (data.canBreakthrough()) {
            MutableComponent bt = Component.translatable("hud.friday_cultivation.breakthrough_ready").withStyle(ChatFormatting.GOLD);
            drawPlainStatus(graphics, mc, bt, x + 8, statusY, statusW, -11930);
            statusY += 9;
        }
        if (data.isMeditating()) {
            MutableComponent med = Component.translatable("hud.friday_cultivation.meditating").withStyle(ChatFormatting.GREEN);
            drawPlainStatus(graphics, mc, med, x + 8, statusY, statusW, -7471203, false);
            statusY += 9;
        }
        long gameTime;
        if (data.hasActiveInverseFiveElementMark(gameTime = player.level().getGameTime())) {
            drawInverseFiveElementStatus(graphics, mc, data, gameTime, x + 8, statusY, statusW);
            statusY += 10;
        }
        if (data.isInTribulation()) {
            MutableComponent trib = Component.translatable("hud.friday_cultivation.tribulation", Realm.formatTribulationCount(data.getTribulationStrikesRemaining(), data.getTribulationBoltsPerWave())).withStyle(ChatFormatting.RED);
            drawPlainStatus(graphics, mc, trib, x + 8, statusY, statusW, -35483, false);
            statusY += 9;
        }
        if (soul) {
            renderSoulStatus(graphics, mc, player, data, x, statusY, statusW);
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

    private static void setBarColor(GuiGraphics graphics, int color) {
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        graphics.setColor(r, g, b, 1.0f);
    }

    /**
     * 统一贴图条渲染：整张贴图等比缩放，上半/下半分别 tint。
     */
    private static void renderTextureBar(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height, double ratio, ResourceLocation emptyTex, ResourceLocation fillTex, int texW, int texH, int topColor, int bottomColor, Component text, int textColor) {
        // 底条：整张贴图等比缩放到目标宽高
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        graphics.blit(emptyTex, x, y, 0.0f, 0.0f, width, height, texW, texH);

        int targetW = (int)((double)width * ratio);
        if (targetW > 0) {
            int halfH = height / 2;
            // 上半：colorTop tint
            setBarColor(graphics, topColor);
            graphics.blit(fillTex, x, y, 0.0f, 0.0f, targetW, halfH, texW, texH);
            // 下半：colorBot tint（从贴图下半采样）
            setBarColor(graphics, bottomColor);
            graphics.blit(fillTex, x, y + halfH, 0.0f, halfH, targetW, height - halfH, texW, texH);
            graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        }

        if (text != null) {
            drawCenteredScaledInRect(graphics, mc, text, x, y, width, height, 0.5f, textColor, true);
        }
    }

    private static void renderHealthBar(GuiGraphics graphics, int x, int y, int width, int height, float value, float max) {
        if (max <= 0.0f) max = 1.0f;
        renderTextureBar(graphics, Minecraft.getInstance(), x, y, width, height, (double)value / (double)max, BLOOD_EMPTY, BLOOD_FILL, 96, 6, HEALTH_TOP, HEALTH_BOTTOM, null, -1);
    }

    private static void renderValueBar(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height, long current, long max, int topColor, int bottomColor, int textColor, Component text) {
        if (max <= 0L) max = 1L;
        renderTextureBar(graphics, mc, x, y, width, height, (double)current / (double)max, BLOOD_EMPTY, BLOOD_FILL, 96, 6, topColor, bottomColor, text, textColor);
    }

    private static void renderSoulStatus(GuiGraphics graphics, Minecraft mc, LocalPlayer player, CultivationData data, int x, int statusY, int width) {
        MutableComponent title = Component.translatable("hud.friday_cultivation.soul.title");
        drawPlainStatus(graphics, mc, title, x + 8, statusY, width, -4724737, true);
        statusY += 9;
        boolean inDifu = player.level().dimension() == ModDimensions.DIFU;
        if (!inDifu) {
            MutableComponent hint = Component.translatable("hud.friday_cultivation.soul.go_difu_hint");
            drawPlainStatus(graphics, mc, hint, x + 8, statusY, width, -4151578, false);
        } else if (data.isReincarnationReady()) {
            MutableComponent ready = Component.translatable("hud.friday_cultivation.soul.can_reincarnate");
            drawPlainStatus(graphics, mc, ready, x + 8, statusY, width, -11930, false);
        } else {
            int remainTicks = Math.max(0, 1200 - data.getDifuTicks());
            int totalSec = (remainTicks + 19) / 20;
            String timeStr = String.format("%d:%02d", totalSec / 60, totalSec % 60);
            MutableComponent cd = Component.translatable("hud.friday_cultivation.soul.countdown", timeStr);
            drawPlainStatus(graphics, mc, cd, x + 8, statusY, width, -1456016, false);
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
