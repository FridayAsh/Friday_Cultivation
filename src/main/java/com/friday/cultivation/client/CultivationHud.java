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
import net.minecraft.world.entity.ai.attributes.Attributes;
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
    private static final ResourceLocation VANILLA_ICONS = new ResourceLocation("textures/gui/icons.png");
    private static final ResourceLocation OVERFLOWING_ICONS = new ResourceLocation("friday_cultivation", "textures/gui/overflowing_icons.png");
    private static final String SPIRIT_ROOT_ICON_PREFIX = "textures/gui/spirit_root/";

    private static final int HUD_X = 6;
    private static final int HUD_Y = 6;
    private static final int HUD_WIDTH = 180;
    private static final int BAR_HEIGHT = 6;
    /** 贴图左端透明圆角列数（blood_fill 96x6：x=0 全透明、x=1 仅 1 实心点，x=2 起实心），小进度采样时跳过 */
    private static final int LEFT_PAD = 2;
    private static final int AVATAR_SIZE = 24; // 8 * 3
    private static final int GOLD_TEXT = -1456016;
    /** 属性条内部深黑灰底槽色（复用填充贴图染此色铺满，自带圆角） */
    private static final int BAR_INNER_BG = 0xFF1A1A1A;

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

    // 原版属性行（位于生命条右侧，与生命条同 y）
    private static final int ATTR_ICON_SIZE = 8;
    private static final float ATTR_TEXT_SCALE = 0.6f;
    private static final int ATTR_GROUP_GAP = 4;
    private static final int ATTR_TO_HEALTH_GAP = 6;
    private static final int FOOD_ICON_U = 52, FOOD_ICON_V = 27;
    private static final int ARMOR_ICON_U = 34, ARMOR_ICON_V = 9;
    private static final int TOUGH_ICON_U = 18, TOUGH_ICON_V = 0;
    private static final int AIR_ICON_U = 16, AIR_ICON_V = 18;
    private static final int FOOD_COLOR = 0xD8A100;
    private static final int ARMOR_COLOR = 0xAAAAAA;
    private static final int TOUGH_COLOR = 0x40E0D0;
    private static final int AIR_COLOR = 0x3FA6FF;

    private CultivationHud() {
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay() != VanillaGuiOverlay.PLAYER_HEALTH.type()
                && event.getOverlay() != VanillaGuiOverlay.ARMOR_LEVEL.type()
                && event.getOverlay() != VanillaGuiOverlay.FOOD_LEVEL.type()
                && event.getOverlay() != VanillaGuiOverlay.AIR_LEVEL.type()) {
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
        // 第九帝界（大帝巅峰）后没有下一个境界：仅隐藏修为条与悟道条，灵气条保留
        boolean peakGreatEmperor = data.getRealm() == Realm.GREAT_EMPEROR && data.getSubStage().isPeakFor(Realm.GREAT_EMPEROR);
        boolean showCultivationBars = data.hasEquippedTechnique() || data.getRealm() != Realm.MORTAL;
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
        drawLeftScaled(graphics, mc, realmLine, realmX, topY, realmAvailW, 0.8f, GOLD_TEXT, true);

        // 3. 条带区域：生命条 + 右侧盔甲/韧性（非创造）→ 修为/灵气/悟道（条件显示）
        int barY = y + 16;

        if (!player.isCreative()) {
            // 生命条（条内显示 当前/最大 HP）
            Component healthText = Component.literal(String.format("%.0f/%.0f", player.getHealth(), player.getMaxHealth()));
            renderHealthBar(graphics, textBaseX, barY, HEALTH_WIDTH, BAR_HEIGHT, player.getHealth(), player.getMaxHealth(), healthText, -1);

            // 右侧属性行：盔甲 → 韧性，位于生命条右侧同 y
            int attrX = textBaseX + HEALTH_WIDTH + ATTR_TO_HEALTH_GAP;
            renderAttributeRow(graphics, player, attrX, barY - 1);

            barY += 8;
        }

        // 头像下方横排：饱食 → 氧气（秒），创造模式隐藏
        if (!player.isCreative()) {
            renderAvatarAttributes(graphics, player, x, y);
        }

        if (showCultivationBars) {
            // 修为条（第九帝界后无下一境界，隐藏）
            if (!peakGreatEmperor) {
                long curCult = data.getCultivationProgress();
                long maxCult = data.getMaxCultivation();
                Component cultText = Component.translatable("hud.friday_cultivation.cultivation", curCult, maxCult);
                renderValueBar(graphics, mc, textBaseX, barY, CULT_WIDTH, BAR_HEIGHT, curCult, maxCult, CULT_TOP, CULT_BOTTOM, CULT_TOP, cultText);
                barY += 8;
            }

            // 灵气条（始终显示）
            long curQi = data.getCurrentQi();
            long maxQi = data.getMaxQi();
            Component qiText = Component.translatable("hud.friday_cultivation.qi", curQi, maxQi);
            renderValueBar(graphics, mc, textBaseX, barY, QI_WIDTH, BAR_HEIGHT, curQi, maxQi, QI_TOP, QI_BOTTOM, QI_TOP, qiText);
            barY += 8;

            // 悟道条（仅 getWuDaoMax() > 0；第九帝界后隐藏）
            long maxWudao = data.getWuDaoMax();
            if (maxWudao > 0L && !peakGreatEmperor) {
                long curWudao = data.getWuDaoProgress();
                Component wudaoText = Component.translatable("hud.friday_cultivation.wudao", curWudao, maxWudao);
                renderValueBar(graphics, mc, textBaseX, barY, WUDAO_WIDTH, BAR_HEIGHT, curWudao, maxWudao, WUDAO_TOP, WUDAO_BOTTOM, WUDAO_TOP, wudaoText);
                barY += 8;
            }
        }

        // 4. 状态行
        int statusY = barY + 2;
        int statusW = HUD_WIDTH - 16;

        // 避免状态行与头像下方饱食/氧气重叠
        int avatarAttrBottom = y + 32 + ATTR_ICON_SIZE;
        if (!player.isCreative() && statusY < avatarAttrBottom + 2) {
            statusY = avatarAttrBottom + 2;
        }
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
     * 统一贴图条渲染（参考电池护盾 clip 逻辑）：
     * - 大进度（targetW >= 贴图宽）：11 参 blit 整图等比缩放，端角自然
     * - 小进度（targetW < 贴图宽）：9 参 blit 1:1 像素采样贴图左端 targetW 宽，
     *   端角保持贴图原始尺寸不变形（避免整图压进几像素导致左下角突出）
     */
    private static void renderTextureBar(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height, double ratio, ResourceLocation emptyTex, ResourceLocation fillTex, int texW, int texH, int topColor, int bottomColor, Component text, int textColor) {
        // 底条：整张贴图（texW x texH）等比缩放到目标宽高
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        graphics.blit(emptyTex, x, y, width, height, 0.0f, 0.0f, texW, texH, texW, texH);
        // 内部深黑灰底槽：复用填充贴图染深黑灰铺满整个条（贴图自带圆角，不会像矩形凸出）
        setBarColor(graphics, BAR_INNER_BG);
        graphics.blit(fillTex, x, y, width, height, 0.0f, 0.0f, texW, texH, texW, texH);
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);

        int targetW = (int)((double)width * ratio);
        if (targetW > 0) {
            int halfH = Math.max(1, height / 2);
            int topH = halfH;
            int botH = height - halfH;
            // 统一 11 参 blit 整图等比缩放（含两端圆角），与生命条一致——
            // 修为/灵气/悟道条宽度虽小于贴图宽，也必须等比缩放，端角完整
            setBarColor(graphics, topColor);
            graphics.blit(fillTex, x, y, targetW, topH, 0.0f, 0.0f, texW, halfH, texW, texH);
            setBarColor(graphics, bottomColor);
            graphics.blit(fillTex, x, y + topH, targetW, botH, 0.0f, (float)halfH, texW, texH - halfH, texW, texH);
            graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        }

        if (text != null) {
            drawCenteredScaledInRect(graphics, mc, text, x, y, width, height, 0.5f, textColor, true);
        }
    }

    private static void renderHealthBar(GuiGraphics graphics, int x, int y, int width, int height, float value, float max, Component text, int textColor) {
        if (max <= 0.0f) max = 1.0f;
        renderTextureBar(graphics, Minecraft.getInstance(), x, y, width, height, (double)value / (double)max, BLOOD_EMPTY, BLOOD_FILL, 96, 6, HEALTH_TOP, HEALTH_BOTTOM, text, textColor);
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
        // 下限 0.45f：长文本可继续缩小以适配宽度，始终在区域内居中（避免 drawX 为负左移）
        float actualScale = textWidth <= 0 ? scale : Math.min(scale, Math.max(0.45f, (float)width / (float)textWidth));
        int drawX = x + Math.max(0, (width - (int)((float)textWidth * actualScale)) / 2);
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

    private static void renderAttributeRow(GuiGraphics graphics, LocalPlayer player, int x, int y) {
        Minecraft mc = Minecraft.getInstance();
        int iconSize = ATTR_ICON_SIZE;
        float textScale = ATTR_TEXT_SCALE;

        // 生命条右侧 2 项：盔甲 → 韧性
        int armor = player.getArmorValue();
        double toughness = player.getAttribute(Attributes.ARMOR_TOUGHNESS).getValue();

        Component armorText = Component.literal(String.valueOf(armor));
        Component toughText = Component.literal(String.format("%.0f", toughness));

        int armorTextW = mc.font.width((FormattedText)armorText);
        int toughTextW = mc.font.width((FormattedText)toughText);

        float armorW = iconSize + 1 + armorTextW * textScale;
        float toughW = iconSize + 1 + toughTextW * textScale;

        drawAttributeIcon(graphics, VANILLA_ICONS, x, y, ARMOR_ICON_U, ARMOR_ICON_V, iconSize, ARMOR_COLOR, armorText, textScale);
        int toughX = x + Math.round(armorW + ATTR_GROUP_GAP);
        drawAttributeIcon(graphics, OVERFLOWING_ICONS, toughX, y, TOUGH_ICON_U, TOUGH_ICON_V, iconSize, TOUGH_COLOR, toughText, textScale);
    }

    private static void renderAvatarAttributes(GuiGraphics graphics, LocalPlayer player, int x, int y) {
        Minecraft mc = Minecraft.getInstance();
        int iconSize = ATTR_ICON_SIZE;
        float textScale = ATTR_TEXT_SCALE;
        int rowY = y + 32;

        // 饱食度
        int food = player.getFoodData().getFoodLevel();
        Component foodText = Component.literal(String.valueOf(food));
        int foodTextW = mc.font.width((FormattedText)foodText);
        float foodW = iconSize + 1 + foodTextW * textScale;

        // 氧气（秒）
        int airSeconds = (int)Math.ceil(player.getAirSupply() / 20.0);
        Component airText = Component.literal(String.valueOf(airSeconds));
        int airTextW = mc.font.width((FormattedText)airText);
        float airW = iconSize + 1 + airTextW * textScale;

        // 在头像列宽度（x..x+32）内居中横排，组间固定 1px 间隙
        int totalW = (int)(foodW + 1 + airW);
        int startOffset = Math.max(0, (32 - totalW) / 2);
        int startX = x + startOffset;

        drawAttributeIcon(graphics, VANILLA_ICONS, startX, rowY, FOOD_ICON_U, FOOD_ICON_V, iconSize, FOOD_COLOR, foodText, textScale);
        int airX = startX + (int)foodW + 1;
        drawAttributeIcon(graphics, VANILLA_ICONS, airX, rowY, AIR_ICON_U, AIR_ICON_V, iconSize, AIR_COLOR, airText, textScale);
    }

    private static void drawAttributeIcon(GuiGraphics graphics, ResourceLocation texture, int x, int y, int u, int v, int iconSize, int color, Component text, float textScale) {
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        graphics.blit(texture, x, y, iconSize, iconSize, (float)u, (float)v, 9, 9, 256, 256);
        drawScaled(graphics, Minecraft.getInstance(), text, x + iconSize + 1, y + 1, textScale, color, true);
    }

    public static ResourceLocation overlayId() {
        return new ResourceLocation("friday_cultivation", "cultivation_hud");
    }
}
