package com.friday.cultivation.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.friday.cultivation.config.ModClientConfig;
import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.PhysiqueBonusHelper;
import com.friday.cultivation.cultivation.QiElement;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.cultivation.realm.RealmTopology;
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
    private static final HudBarAnimator BAR_ANIMATOR = new HudBarAnimator();

    public static final IGuiOverlay OVERLAY = (gui, graphics, partialTick, screenWidth, screenHeight) -> {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        BAR_ANIMATOR.beginFrame(player == null ? null : player.getUUID());
        long nowMillis = System.currentTimeMillis();
        CultivationHud.renderExperienceBar(graphics, screenWidth, screenHeight, nowMillis);
        CultivationHud.render(graphics, screenWidth, nowMillis);
    };

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
    /** 贴图左端固定圆角像素数（clip 段，参考电池护盾 CLIP_WIDTH=3） */
    private static final int CLIP_PX = 3;

    // 各条宽度（递减）
    private static final int HEALTH_WIDTH = 100;
    private static final int CULT_WIDTH = 90;
    private static final int QI_WIDTH = 80;
    private static final int WUDAO_WIDTH = 70;
    /** C 版经验 HUD 的完整横向宽度，保持原版经验条宽度。 */
    private static final int EXPERIENCE_GROUP_WIDTH = 182;
    /** 经验条内容宽度；绘制函数会在左右各加 1 像素边框。 */
    private static final int EXPERIENCE_BAR_WIDTH = EXPERIENCE_GROUP_WIDTH - 2;
    /** 外框总高度为 6：内容 4 像素，加上下各 1 像素边框。 */
    private static final int EXPERIENCE_BAR_HEIGHT = 4;
    private static final int EXPERIENCE_META_HEIGHT = 8;
    private static final int EXPERIENCE_META_GAP = 1;
    private static final float EXPERIENCE_LEVEL_TEXT_SCALE = 0.62f;
    private static final float EXPERIENCE_VALUE_TEXT_SCALE = 0.6f;
    private static final float EXPERIENCE_PROGRESS_TEXT_SCALE = 0.45f;
    /** 经验等级文字改为白色，与快捷栏文字保持一致。 */
    private static final int EXPERIENCE_LEVEL_TEXT_COLOR = 0xFFFFFF;

    // 项目设定色：顶/底渐变
    private static final int HEALTH_TOP = -1944235;
    private static final int HEALTH_BOTTOM = -5758944;
    private static final int CULT_TOP = -928374;
    private static final int CULT_BOTTOM = -3631046;
    private static final int QI_TOP = -9583434;
    private static final int QI_BOTTOM = -13729678;
    private static final int WUDAO_TOP = -8355712;
    private static final int WUDAO_BOTTOM = -10592674;
    /**
     * 原版 1.20.1 assets/minecraft/textures/gui/icons.png 经验条填充区域
     * (x=120, y=70..72) 的内部像素采样：顶部/底部各取对应渐变色。
     */
    private static final int EXPERIENCE_TOP = 0xFF5F8B3E;
    private static final int EXPERIENCE_BOTTOM = 0xFF436924;

    // 原版属性行（位于生命条右侧，与生命条同 y）
    private static final int ATTR_ICON_SIZE = 8;
    private static final float ATTR_TEXT_SCALE = 0.6f;
    private static final int ATTR_GROUP_GAP = 4;
    private static final int ATTR_TO_HEALTH_GAP = 6;
    /** 状态行文本区相对于 HUD 左侧 x 的偏移；0 表示与 HUD 左边缘对齐（头像在 x+4，状态行在其下方，不重叠） */
    private static final int STATUS_TEXT_LEFT = 0;
    /** 状态行文本近似高度（缩放后），用于判断是否会与头像下方饱食/氧气横排重叠 */
    private static final int STATUS_LINE_HEIGHT = 7;
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
        if (event.getOverlay() == VanillaGuiOverlay.EXPERIENCE_BAR.type()) {
            // 原版经验条与等级数字都由本项目 HUD 统一重绘，避免重复显示。
            event.setCanceled(true);
            return;
        }
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

    /** C 版布局：上方显示等级/总经验，下方显示本级经验进度。 */
    private static void renderExperienceBar(GuiGraphics graphics, int screenWidth, int screenHeight, long nowMillis) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui || player.isSpectator()
                || player.getXpNeededForNextLevel() <= 0) {
            return;
        }
        int groupX = screenWidth / 2 - EXPERIENCE_GROUP_WIDTH / 2;
        int nextLevelExperience = Math.max(1, player.getXpNeededForNextLevel());
        int currentLevelExperience = Math.max(0, Math.min(nextLevelExperience,
                Math.round(player.experienceProgress * (float)nextLevelExperience)));
        HudBarAnimator.Visual experienceVisual = BAR_ANIMATOR.sample(
                HudBarAnimator.BarId.EXPERIENCE,
                currentLevelExperience,
                nextLevelExperience,
                Math.max(0, player.experienceLevel),
                nowMillis);
        int displayLevel = (int)Math.max(0L, experienceVisual.displayCycleKey());
        int displayMaxExperience = Math.max(1, (int)Math.round(experienceVisual.textMax()));
        int displayCurrentExperience = Math.max(0, Math.min(displayMaxExperience,
                (int)Math.round(experienceVisual.textCurrent())));
        Component level = Component.literal("等级:" + displayLevel);
        int rawLevelWidth = mc.font.width((FormattedText)level);
        int levelWidth = Math.max(1,
                (int)Math.ceil((double)rawLevelWidth * (double)EXPERIENCE_LEVEL_TEXT_SCALE));
        Component totalExperience = Component.literal("总经验 " + Math.max(0, player.totalExperience));
        // 快捷栏顶部约为 screenHeight - 22；将经验条下移 2 像素，压缩两者之间的空隙。
        int barY = screenHeight - 27;
        int metaY = barY - EXPERIENCE_META_HEIGHT - EXPERIENCE_META_GAP;
        Component progressText = Component.literal(displayCurrentExperience + " / " + displayMaxExperience);
        drawLeftScaledInRect(graphics, mc, level, groupX, metaY,
                levelWidth, EXPERIENCE_META_HEIGHT, EXPERIENCE_LEVEL_TEXT_SCALE, EXPERIENCE_LEVEL_TEXT_COLOR, false);
        drawRightScaledInRect(graphics, mc, totalExperience, groupX, metaY,
                EXPERIENCE_GROUP_WIDTH, EXPERIENCE_META_HEIGHT, EXPERIENCE_VALUE_TEXT_SCALE, -1, false);
        // renderCultivationPanelBar 的 x/y 是内容区域坐标，边框会向外扩 1 像素。
        // 因此内容从整体左边界 +1 开始，宽度减 2，确保外框严格落在 groupX..groupX+182 内。
        renderCultivationPanelBar(graphics, mc, groupX + 1, barY, EXPERIENCE_BAR_WIDTH, EXPERIENCE_BAR_HEIGHT,
                experienceVisual, EXPERIENCE_TOP, EXPERIENCE_BOTTOM, progressText);
    }

    private static void render(GuiGraphics graphics, int screenWidth, long nowMillis) {
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
        int statusAlignWidth = HEALTH_WIDTH; // 状态行默认与生命条区域对齐

        if (!player.isCreative()) {
            // 生命条（条内显示 当前/最大 HP）
            Component healthText = Component.literal(String.format("%.0f/%.0f", player.getHealth(), player.getMaxHealth()));
            HudBarAnimator.Visual healthVisual = BAR_ANIMATOR.sample(
                    HudBarAnimator.BarId.HEALTH, player.getHealth(), player.getMaxHealth(), 0L, nowMillis);
            renderHealthBar(graphics, textBaseX, barY, HEALTH_WIDTH, BAR_HEIGHT,
                    healthVisual, healthText, -1);

            // 右侧属性行：盔甲 → 韧性，位于生命条右侧同 y
            int attrX = textBaseX + HEALTH_WIDTH + ATTR_TO_HEALTH_GAP;
            renderAttributeRow(graphics, player, attrX, barY - 1);

            statusAlignWidth = HEALTH_WIDTH;
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
                HudBarAnimator.Visual cultivationVisual = BAR_ANIMATOR.sample(
                        HudBarAnimator.BarId.CULTIVATION, curCult, maxCult, cultivationCycleKey(data), nowMillis);
                renderValueBar(graphics, mc, textBaseX, barY, CULT_WIDTH, BAR_HEIGHT,
                        cultivationVisual, CULT_TOP, CULT_BOTTOM, CULT_TOP, cultText);
                statusAlignWidth = CULT_WIDTH;
                barY += 8;
            }

            // 灵气条（凡人阶段无灵气属性，仅非凡人显示）
            if (data.getRealm() != Realm.MORTAL) {
                long curQi = data.getCurrentQi();
                long maxQi = data.getMaxQi();
                Component qiText = Component.translatable("hud.friday_cultivation.qi", curQi, maxQi);
                HudBarAnimator.Visual qiVisual = BAR_ANIMATOR.sample(
                        HudBarAnimator.BarId.QI, curQi, maxQi, 0L, nowMillis);
                renderValueBar(graphics, mc, textBaseX, barY, QI_WIDTH, BAR_HEIGHT,
                        qiVisual, QI_TOP, QI_BOTTOM, QI_TOP, qiText);
                statusAlignWidth = QI_WIDTH;
                barY += 8;
            }

            // 悟道条（仅 getWuDaoMax() > 0；第九帝界后隐藏）
            long maxWudao = data.getWuDaoMax();
            if (maxWudao > 0L && !peakGreatEmperor) {
                long curWudao = data.getWuDaoProgress();
                Component wudaoText = Component.translatable("hud.friday_cultivation.wudao", curWudao, maxWudao);
                HudBarAnimator.Visual wudaoVisual = BAR_ANIMATOR.sample(
                        HudBarAnimator.BarId.WUDAO, curWudao, maxWudao, 0L, nowMillis);
                renderValueBar(graphics, mc, textBaseX, barY, WUDAO_WIDTH, BAR_HEIGHT,
                        wudaoVisual, WUDAO_TOP, WUDAO_BOTTOM, WUDAO_TOP, wudaoText);
                statusAlignWidth = WUDAO_WIDTH;
                barY += 8;
            }
        }

        // 4. 状态行：水平基准与最后一条可见属性条区域对齐，垂直紧贴其下方
        int statusY = barY + 2;
        int statusX = textBaseX + STATUS_TEXT_LEFT;
        int statusW = statusAlignWidth;
        if (data.canBreakthrough()) {
            MutableComponent bt = Component.translatable("hud.friday_cultivation.breakthrough_ready").withStyle(ChatFormatting.GOLD);
            drawPlainStatus(graphics, mc, bt, statusX, statusY, statusW, -11930);
            statusY += 9;
        }
        if (data.isMeditating()) {
            MutableComponent med = Component.translatable("hud.friday_cultivation.meditating").withStyle(ChatFormatting.GREEN);
            drawPlainStatus(graphics, mc, med, statusX, statusY, statusW, -7471203, false);
            statusY += 9;
        }
        long gameTime;
        if (data.hasActiveInverseFiveElementMark(gameTime = player.level().getGameTime())) {
            drawInverseFiveElementStatus(graphics, mc, data, gameTime, statusX, statusY, statusW);
            statusY += 10;
        }
        if (data.isInTribulation()) {
            MutableComponent trib = Component.translatable("hud.friday_cultivation.tribulation", Realm.formatTribulationCount(data.getTribulationStrikesRemaining(), data.getTribulationBoltsPerWave())).withStyle(ChatFormatting.RED);
            drawPlainStatus(graphics, mc, trib, statusX, statusY, statusW, -35483, false);
            statusY += 9;
        }
        if (soul) {
            renderSoulStatus(graphics, mc, player, data, statusX, statusY, statusW);
        }

        RenderSystem.disableBlend();
    }

    private static int hudX(int screenWidth) {
        if (ModClientConfig.hudPosition().isRightAligned()) {
            return Math.max(HUD_X, screenWidth - HUD_WIDTH - HUD_X);
        }
        return HUD_X;
    }

    private static long cultivationCycleKey(CultivationData data) {
        return Math.max(0L, RealmTopology.progressionIndex(data.getRealm(), data.getSubStage()));
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
    private static void renderTextureBar(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height,
                                         HudBarAnimator.Visual visual, ResourceLocation emptyTex, ResourceLocation fillTex,
                                         int texW, int texH, int topColor, int bottomColor, Component text, int textColor) {
        // 底条：整张贴图（texW x texH）等比缩放到目标宽高
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        graphics.blit(emptyTex, x, y, width, height, 0.0f, 0.0f, texW, texH, texW, texH);
        // 内部深黑灰底槽：复用填充贴图染深黑灰铺满整个条（贴图自带圆角，不会像矩形凸出）
        setBarColor(graphics, BAR_INNER_BG);
        graphics.blit(fillTex, x, y, width, height, 0.0f, 0.0f, texW, texH, texW, texH);
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);

        double primaryRatio = visual.primaryRatio();
        double trailingRatio = Math.max(primaryRatio, visual.trailingRatio());
        if (trailingRatio > primaryRatio + 0.0001D) {
            drawTextureFill(graphics, x, y, width, height, trailingRatio, emptyTex, fillTex, texW, texH,
                    scaleColor(topColor, 0.58D), scaleColor(bottomColor, 0.58D), false);
        }
        drawTextureFill(graphics, x, y, width, height, primaryRatio, emptyTex, fillTex, texW, texH,
                topColor, bottomColor, true);

        // 左上角纹理属性条不绘制白色闪光；经验条的白色闪光仍由下方程序化经验条保留。
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);

        if (text != null) {
            drawCenteredScaledInRect(graphics, mc, text, x, y, width, height, 0.5f, textColor, true);
        }
    }

    private static void drawTextureFill(GuiGraphics graphics, int x, int y, int width, int height, double ratio,
                                        ResourceLocation emptyTex, ResourceLocation fillTex, int texW, int texH,
                                        int topColor, int bottomColor, boolean highlight) {
        int targetW = (int)((double)width * Math.max(0.0D, Math.min(1.0D, ratio)));
        if (targetW > 0) {
            int halfH = Math.max(1, height / 2);
            int topH = halfH;
            int botH = height - halfH;
            // 左端圆角段屏幕宽：按贴图 CLIP_PX 像素 × 全宽缩放比例（底槽圆角同比例，随条宽变化）
            int clipScreen = Math.max(1, (int)((double)width * (double)CLIP_PX / (double)texW));
            if (targetW >= width) {
                // 满/接近满：全宽整图等比缩放（与底槽一致，圆角完整）
                setBarColor(graphics, topColor);
                graphics.blit(fillTex, x, y, width, topH, 0.0f, 0.0f, texW, halfH, texW, texH);
                setBarColor(graphics, bottomColor);
                graphics.blit(fillTex, x, y + topH, width, botH, 0.0f, (float)halfH, texW, texH - halfH, texW, texH);
                graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            } else if (targetW <= clipScreen) {
                // 很小（刚开始填充）：只采样贴图左端像素（不包含右端圆角），
                // 避免整图缩放把右端圆角压到左侧造成像素溢出条外
                int leftSrc = Math.max(1, (int)Math.round((double)targetW * (double)texW / (double)width));
                setBarColor(graphics, topColor);
                graphics.blit(fillTex, x, y, targetW, topH, 0.0f, 0.0f, leftSrc, halfH, texW, texH);
                setBarColor(graphics, bottomColor);
                graphics.blit(fillTex, x, y + topH, targetW, botH, 0.0f, (float)halfH, leftSrc, texH - halfH, texW, texH);
                graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            } else {
                // 电池护盾 clip：左端固定圆角段（与底槽缩放一致）+ 右侧从贴图尾部滑入
                int rightScreen = targetW - clipScreen;
                // 左端：采样贴图左端 CLIP_PX 像素，等比缩放到 clipScreen
                setBarColor(graphics, topColor);
                graphics.blit(fillTex, x, y, clipScreen, topH, 0.0f, 0.0f, CLIP_PX, halfH, texW, texH);
                setBarColor(graphics, bottomColor);
                graphics.blit(fillTex, x, y + topH, clipScreen, botH, 0.0f, (float)halfH, CLIP_PX, texH - halfH, texW, texH);
                // 右侧：从贴图尾部取 rightScreen/width 比例像素，等比缩放到 rightScreen
                int rightSrc = Math.max(1, (int)Math.round((double)rightScreen * (double)texW / (double)width));
                setBarColor(graphics, topColor);
                graphics.blit(fillTex, x + clipScreen, y, rightScreen, topH, (float)(texW - rightSrc), 0.0f, rightSrc, halfH, texW, texH);
                setBarColor(graphics, bottomColor);
                graphics.blit(fillTex, x + clipScreen, y + topH, rightScreen, botH, (float)(texW - rightSrc), (float)halfH, rightSrc, texH - halfH, texW, texH);
                graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            }
            if (highlight) {
                graphics.fill(x, y, x + targetW, y + 1, 0x40FFFFFF);
                graphics.fill(x, y + height - 1, x + targetW, y + height, 0x33000000);
            }
        }
    }

    /**
     * 修仙面板属性条的统一视觉：边框、暗槽、顶端高光、上下渐变与底部阴影。
     * 经验条只替换属性条的填充颜色为原版经验绿色，避免再次引入另一套贴图样式。
     */
    private static void renderCultivationPanelBar(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height,
                                                  HudBarAnimator.Visual visual, int topColor, int bottomColor, Component text) {
        int barW = Math.max(1, width);
        int barH = Math.max(1, height);
        int border = -15067628;
        int background = -14739179;
        int topHighlight = -16119802;

        graphics.fill(x - 1, y - 1, x + barW + 1, y, border);
        graphics.fill(x - 1, y + barH, x + barW + 1, y + barH + 1, border);
        graphics.fill(x - 1, y, x, y + barH, border);
        graphics.fill(x + barW, y, x + barW + 1, y + barH, border);
        graphics.fill(x, y, x + barW, y + barH, background);
        graphics.fill(x, y, x + barW, y + 1, topHighlight);

        // 空槽只保留四分之一刻度，避免原先每 6 像素一条竖纹造成视觉噪声。
        for (int quarter = 1; quarter < 4; quarter++) {
            int tickX = x + Math.round((float)barW * (float)quarter / 4.0f);
            graphics.fill(tickX, y + 1, tickX + 1, y + barH - 1, 0x24000000);
        }

        double primaryRatio = visual.primaryRatio();
        double trailingRatio = Math.max(primaryRatio, visual.trailingRatio());
        if (trailingRatio > primaryRatio + 0.0001D) {
            drawProceduralFill(graphics, x, y, barW, barH, trailingRatio,
                    scaleColor(topColor, 0.58D), scaleColor(bottomColor, 0.58D), false);
        }
        drawProceduralFill(graphics, x, y, barW, barH, primaryRatio, topColor, bottomColor, true);

        int filledW = (int)((double)barW * primaryRatio);
        if (visual.pulseStrength() > 0.0F && filledW > 0) {
            int alpha = Math.max(0, Math.min(96, (int)(visual.pulseStrength() * 96.0F)));
            graphics.fill(x, y, x + filledW, y + barH, (alpha << 24) | 0x00FFFFFF);
        }

        if (text != null) {
            drawCenteredScaledInRect(graphics, mc, text, x, y, barW, barH,
                    EXPERIENCE_PROGRESS_TEXT_SCALE, -1, true);
        }
    }

    private static void drawProceduralFill(GuiGraphics graphics, int x, int y, int width, int height, double ratio,
                                           int topColor, int bottomColor, boolean highlight) {
        int filledW = (int)((double)width * Math.max(0.0D, Math.min(1.0D, ratio)));
        if (filledW <= 0) {
            return;
        }
        int half = Math.max(1, height / 2);
        graphics.fill(x, y, x + filledW, y + half, topColor);
        graphics.fill(x, y + half, x + filledW, y + height, bottomColor);
        if (highlight) {
            graphics.fill(x, y, x + filledW, y + 1, 0x40FFFFFF);
            graphics.fill(x, y + height - 1, x + filledW, y + height, 0x33000000);
        }
    }

    private static int scaleColor(int color, double factor) {
        int red = (int)(((color >> 16) & 0xFF) * factor);
        int green = (int)(((color >> 8) & 0xFF) * factor);
        int blue = (int)((color & 0xFF) * factor);
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    private static void renderHealthBar(GuiGraphics graphics, int x, int y, int width, int height,
                                        HudBarAnimator.Visual visual, Component text, int textColor) {
        renderTextureBar(graphics, Minecraft.getInstance(), x, y, width, height, visual,
                BLOOD_EMPTY, BLOOD_FILL, 96, 6, HEALTH_TOP, HEALTH_BOTTOM, text, textColor);
    }

    private static void renderValueBar(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height,
                                       HudBarAnimator.Visual visual, int topColor, int bottomColor,
                                       int textColor, Component text) {
        renderTextureBar(graphics, mc, x, y, width, height, visual,
                BLOOD_EMPTY, BLOOD_FILL, 96, 6, topColor, bottomColor, text, textColor);
    }

    private static void renderSoulStatus(GuiGraphics graphics, Minecraft mc, LocalPlayer player, CultivationData data, int x, int statusY, int width) {
        MutableComponent title = Component.translatable("hud.friday_cultivation.soul.title");
        drawPlainStatus(graphics, mc, title, x + STATUS_TEXT_LEFT, statusY, width, -4724737, true);
        statusY += 9;
        boolean inDifu = player.level().dimension() == ModDimensions.DIFU;
        if (!inDifu) {
            MutableComponent hint = Component.translatable("hud.friday_cultivation.soul.go_difu_hint");
            drawPlainStatus(graphics, mc, hint, x + STATUS_TEXT_LEFT, statusY, width, -4151578, false);
        } else if (data.isReincarnationReady()) {
            MutableComponent ready = Component.translatable("hud.friday_cultivation.soul.can_reincarnate");
            drawPlainStatus(graphics, mc, ready, x + STATUS_TEXT_LEFT, statusY, width, -11930, false);
        } else {
            int remainTicks = Math.max(0, 1200 - data.getDifuTicks());
            int totalSec = (remainTicks + 19) / 20;
            String timeStr = String.format("%d:%02d", totalSec / 60, totalSec % 60);
            MutableComponent cd = Component.translatable("hud.friday_cultivation.soul.countdown", timeStr);
            drawPlainStatus(graphics, mc, cd, x + STATUS_TEXT_LEFT, statusY, width, -1456016, false);
        }
    }

    private static void drawInverseFiveElementStatus(GuiGraphics graphics, Minecraft mc, CultivationData data, long gameTime, int x, int y, int width) {
        QiElement next = PhysiqueBonusHelper.nextInverseElement(data.getInverseFiveElementMark());
        int stacks = data.getActiveInverseFiveElementStacks(gameTime);
        MutableComponent text = Component.translatable("hud.friday_cultivation.inverse_five_elements_next", next.displayName(), stacks);
        // 图标+文本作为整体在状态行区域内居中
        float scale = 0.58f;
        int iconSize = 8;
        float groupW = iconSize + 1 + mc.font.width((FormattedText) text) * scale;
        int offset = Math.max(0, (int) ((width - groupW) / 2.0f));
        graphics.blit(spiritRootIcon(next), x + offset, y, iconSize, iconSize, 0.0f, 0.0f, 16, 16, 16, 16);
        drawLeftScaled(graphics, mc, text, x + offset + iconSize + 1, y + 1, width - offset - iconSize - 1, scale, -8064799, true);
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
        // 长文本自动缩放到不溢出，确保在区域内真正居中
        float actualScale = textWidth <= 0 ? scale : Math.min(scale, (float)width / (float)textWidth);
        int scaledW = (int)((float)textWidth * actualScale);
        int drawX = x + (width - scaledW) / 2;
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

    private static void drawLeftScaledInRect(GuiGraphics graphics, Minecraft mc, Component text, int x, int y, int width, int height, float scale, int color, boolean shadow) {
        int textWidth = mc.font.width((FormattedText)text);
        float actualScale = textWidth <= 0 ? scale : Math.min(scale, Math.max(0.18f, (float)width / (float)textWidth));
        int scaledH = (int)(9.0f * actualScale);
        int drawY = y + Math.max(0, (height - scaledH) / 2);
        drawScaled(graphics, mc, text, x, drawY, actualScale, color, shadow);
    }

    private static void drawRightScaledInRect(GuiGraphics graphics, Minecraft mc, Component text, int x, int y, int width, int height, float scale, int color, boolean shadow) {
        int textWidth = mc.font.width((FormattedText)text);
        float actualScale = textWidth <= 0 ? scale : Math.min(scale, Math.max(0.18f, (float)width / (float)textWidth));
        int scaledW = (int)((float)textWidth * actualScale);
        int scaledH = (int)(9.0f * actualScale);
        int drawX = x + width - scaledW;
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

        // 生命条右侧 2 项：盔甲 → 韧性（均为 0 时不显示）
        int armor = player.getArmorValue();
        double toughness = player.getAttribute(Attributes.ARMOR_TOUGHNESS).getValue();
        boolean showArmor = armor > 0;
        boolean showToughness = toughness > 0.0;
        if (!showArmor && !showToughness) {
            return;
        }

        Component armorText = Component.literal(String.valueOf(armor));
        Component toughText = Component.literal(String.format("%.0f", toughness));

        int armorTextW = mc.font.width((FormattedText)armorText);
        int toughTextW = mc.font.width((FormattedText)toughText);

        float armorW = iconSize + 1 + armorTextW * textScale;

        int gx = x;
        if (showArmor) {
            drawAttributeIcon(graphics, VANILLA_ICONS, gx, y, ARMOR_ICON_U, ARMOR_ICON_V, iconSize, ARMOR_COLOR, armorText, textScale);
            gx += Math.round(armorW + ATTR_GROUP_GAP);
        }
        if (showToughness) {
            drawAttributeIcon(graphics, OVERFLOWING_ICONS, gx, y, TOUGH_ICON_U, TOUGH_ICON_V, iconSize, TOUGH_COLOR, toughText, textScale);
        }
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
