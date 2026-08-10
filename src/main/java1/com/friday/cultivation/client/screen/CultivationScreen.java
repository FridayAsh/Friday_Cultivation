package com.friday.cultivation.client.screen;

import com.friday.cultivation.client.screen.widget.BambooTabButton;
import com.friday.cultivation.client.screen.widget.CinnabarButton;
import com.friday.cultivation.client.screen.widget.CloseIconButton;
import com.friday.cultivation.client.screen.widget.DrawCardWidget;
import com.friday.cultivation.client.screen.widget.LabeledToggleSwitchButton;
import com.friday.cultivation.client.screen.widget.MiniCinnabarButton;
import com.friday.cultivation.BodyDefenseHelper;
import com.friday.cultivation.CultivationBonusCategory;
import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.dao.FoundationDao;
import com.friday.cultivation.dao.FoundationDaoBonusHelper;
import com.friday.cultivation.dao.GoldenCoreDao;
import com.friday.cultivation.dao.GoldenCoreDaoBonusHelper;
import com.friday.cultivation.dao.LooseImmortalBonusHelper;
import com.friday.cultivation.ItemTier;
import com.friday.cultivation.LifespanHelper;
import com.friday.cultivation.sect.SectRole;
import com.friday.cultivation.spell.Spell;
import com.friday.cultivation.spell.SpellType;
import com.friday.cultivation.spell.SpellWheelLayout;
import com.friday.cultivation.technique.Technique;
import com.friday.cultivation.technique.TechniqueBonusHelper;
import com.friday.cultivation.entity.SeatEntity;
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.identity.Identity;
import com.friday.cultivation.network.CycleGenderPacket;
import com.friday.cultivation.network.EquipSpellPacket;
import com.friday.cultivation.network.EquipTechniquePacket;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.RequestBreakthroughPacket;
import com.friday.cultivation.network.RequestGoDifuPacket;
import com.friday.cultivation.network.RequestReincarnationScreenPacket;
import com.friday.cultivation.network.RequestSectScreenPacket;
import com.friday.cultivation.network.SetCultivationNamePacket;
import com.friday.cultivation.network.SetSpellTerrainDestructionPacket;
import com.friday.cultivation.network.SetTimeAccelerationPacket;
import com.friday.cultivation.network.SpendZhenyuanPacket;
import com.friday.cultivation.network.ToggleBonusCategoryPacket;
import com.friday.cultivation.network.ToggleSpellPacket;
import com.friday.cultivation.physique.Physique;
import com.friday.cultivation.qi.PlayerQiAbsorptionHelper;
import com.friday.cultivation.qi.consumer.PlayerQiConsumer;
import com.friday.cultivation.QiElement;
import com.friday.cultivation.spirit.SpiritRoot;
import com.friday.cultivation.spirit.SpiritRootBonusHelper;
import com.friday.cultivation.ZhenyuanBonusHelper;
import com.friday.cultivation.alchemy.AlchemyRank;
import com.friday.cultivation.refining.RefiningRank;
import com.friday.cultivation.registry.ModDimensions;
import com.friday.cultivation.item.ModItems;
import com.friday.cultivation.realm.Realm;
import com.friday.cultivation.realm.SubStage;
import com.friday.cultivation.util.CompactNumberFormat;
import com.friday.cultivation.util.SpellScalingHelper;
import com.friday.cultivation.util.TooltipUtils;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 修仙主面板 — 一比一复刻原模组 CultivationScreen
 * 320×200 水墨风面板，左侧信息+玩家模型，右侧4标签页（属性/功法/法术/突破）。
 * 照搬原模组所有字段、方法、内部类，仅做命名空间(xiaoxiang_cultivation→friday_cultivation)和
 * API映射（Mojang混淆名→1.20.1映射）转换。
 */
public class CultivationScreen extends Screen {
    private static final ResourceLocation BG_TEXTURE = guiTexture("textures/gui/cultivation_bg.png");
    private static final ResourceLocation ICON_HP = guiTexture("textures/gui/icon_hp.png");
    private static final ResourceLocation ICON_CULTIVATION = guiTexture("textures/gui/icon_cultivation.png");
    private static final ResourceLocation ICON_QI = guiTexture("textures/gui/icon_qi.png");
    public static final ResourceLocation TAIJI_TEXTURE = guiTexture("textures/gui/taiji.png");

    // ── 布局常量 ──
    private static final int PANEL_WIDTH = 320;
    private static final int PANEL_HEIGHT = 200;
    private static final int BG_TEX_W = 320;
    private static final int BG_TEX_H = 200;

    // ── 颜色常量（照搬原模组配色，十六进制补码） ──
    private static final int BG_PAGE = -923956;
    private static final int BG_PANEL = -1517128;
    private static final int INK_BLACK = -15067628;
    private static final int INK_SOFT = -12766422;
    private static final int INK_MUTE = -9807288;
    private static final int VERMILLION = -4703686;
    private static final int VERMILLION_DEEP = -7723482;
    private static final int GOLD_BORDER = -3562934;
    private static final int GOLD_BRIGHT = -10496;
    private static final int GOLD_TEXT_DARK = -7707624;
    private static final int GREEN_INK = -12950192;
    private static final int JADE_REQUIREMENT_MET = -14835869;
    private static final Style JADE_REQUIREMENT_STYLE = Style.EMPTY.withColor(TextColor.fromRgb(1941347));
    private static final int BREAKTHROUGH_READY_TEXT = -14718150;
    private static final float BREAKTHROUGH_ROW_SCALE = 0.7f;
    private static final int BREAKTHROUGH_ROW_ICON_SIZE = 12;
    private static final int BREAKTHROUGH_ROW_ICON_GAP = 3;
    private static final int BREAKTHROUGH_ROW_V_PAD = 2;
    private static final int BORDER_LIGHT = -2504802;
    private static final int BORDER_DARK = -10859978;
    private static final int SHADOW_HARD = -1946157056;
    private static final int TEXT_DARK = -12766422;
    private static final int TEXT_REALM = -7644652;
    private static final int TEXT_GREY = -9807288;
    private static final int CELL_BG_LIGHT = -992352;
    private static final int CELL_BG_DARK = -7439524;
    private static final int CELL_BG_EMPTY = -4677516;
    private static final int ORIGIN_INFO_TOOLTIP_MAX_WIDTH = 180;
    private static final int ORIGIN_INFO_TOOLTIP_MARGIN = 32;
    private static final int LEFT_STATUS_BAR_ICON_SIZE = 9;
    private static final int LEFT_STATUS_BAR_LABEL_SLOT = 14;
    private static final int LEFT_STATUS_BAR_ICON_LABEL_GAP = 3;
    private static final int LEFT_STATUS_BAR_LABEL_BAR_GAP = 1;
    private static final int LEFT_STATUS_BAR_ROW_HEIGHT = 10;
    private static final int LEFT_STATUS_BAR_GAP = 1;
    private static final int LEFT_STATUS_AFTER_BARS_GAP = 4;
    private static final float LEFT_STATUS_BAR_LABEL_SCALE = 0.62f;
    private static final int SPELL_CONTENT_INSET = 12;
    private static final int SPELL_BOTTOM_ROW_Y_FROM_BOTTOM = 22;
    private static final int SPELL_BOTTOM_CONTROL_HEIGHT = 14;
    private static final int SPELL_TERRAIN_TOGGLE_WIDTH = 56;
    private static final int SPELL_PASSIVE_TOGGLE_WIDTH = 52;
    private static final float TEXT_SCALE = 0.85f;
    private static final float TEXT_SCALE_EN = 0.7f;
    private static final float ATTR_TAB_SCALE_MULT = 0.9f;
    private static final int SPELL_CELL = 18;
    private static final int SPELL_GAP = 1;
    private static final int SPELL_ICON_SIZE = 14;
    private static final int LEARNED_SPELL_COLUMNS = 7;
    private static final int LEARNED_SPELL_ROWS = 3;
    private static final int TECH_CELL = 18;
    private static final int TECH_GAP = 1;
    private static final int[] OCT_X = SpellWheelLayout.OCT_X;
    private static final int[] OCT_Y = SpellWheelLayout.OCT_Y;

    // ── 状态字段 ──
    private Tab currentTab = Tab.ATTRIBUTES;
    private static final FoundationDao[] FOUNDATION_BREAKTHROUGH_OPTIONS = new FoundationDao[]{
            FoundationDao.HUMAN, FoundationDao.BLOOD, FoundationDao.EARTH, FoundationDao.HEAVEN};
    private static final GoldenCoreDao[] GOLDEN_CORE_BREAKTHROUGH_OPTIONS = new GoldenCoreDao[]{
            GoldenCoreDao.HUMAN, GoldenCoreDao.BLOOD, GoldenCoreDao.EARTH, GoldenCoreDao.HEAVEN};
    private SpellTypeFilter spellTypeFilter = SpellTypeFilter.ALL;
    private ElementFilter spellElementFilter = ElementFilter.ALL;
    private TierFilter spellTierFilter = TierFilter.ALL;
    private ElementFilter techElementFilter = ElementFilter.ALL;
    private TierFilter techTierFilter = TierFilter.ALL;
    private final List<int[]> filterButtonRects = new ArrayList<>();
    private int openDropdown = 0;
    private int[] openDropdownAnchor = null;
    private final List<int[]> dropdownOptionRects = new ArrayList<>();
    private Button breakthroughBtn;
    private Button reincarnationBtn;
    private Button goDifuBtn;
    private Button timeAccelerationStartBtn;
    private Button timeAccelerationStopBtn;
    private BambooTabButton tabAttrBtn;
    private BambooTabButton tabTechBtn;
    private BambooTabButton tabSpellBtn;
    private BambooTabButton tabBreakthroughBtn;
    private Button toggleSpellBtn;
    private LabeledToggleSwitchButton spellTerrainDestructionBtn;
    private FoundationDao selectedFoundationDao = FoundationDao.HUMAN;
    private GoldenCoreDao selectedGoldenCoreDao = GoldenCoreDao.HUMAN;
    private final List<int[]> breakthroughOptionRects = new ArrayList<>();
    private int hoveredBreakthroughOptionKind = 0;
    private int hoveredBreakthroughOptionOrdinal = -1;
    private int[] breakthroughHistoryButtonRect = null;
    private int[] breakthroughHistoryPopupRect = null;
    private int[] breakthroughHistoryPopupCloseRect = null;
    private boolean breakthroughHistoryPopupOpen = false;
    private EditBox nameEditBox;
    private boolean editingName = false;
    private int[] nameCellRect = null;
    private int[] genderCellRect = null;
    private int[] foundationCellRect = null;
    private String selectedSpellId = null;
    private String hoveredSpellId = null;
    private int[] identityRowRect = null;
    private int[] sectEntryArrowRect = null;
    private int[] spiritRootRowRect = null;
    private int[] physiqueRowRect = null;
    private final int[][] zhenyuanPlusRects = new int[5][4];
    private final int[][] zhenyuanLabelRects = new int[5][4];
    private int hoveredZhenyuanAttr = -1;
    private final long[] zhenyuanPlusFlashUntil = new long[5];
    private static final int ZHENYUAN_HOLD_INITIAL_DELAY_TICKS = 8;
    private static final int ZHENYUAN_HOLD_FAST_TICKS = 20;
    private static final int ZHENYUAN_HOLD_FASTER_TICKS = 50;
    private static final int ZHENYUAN_HOLD_RUSH_TICKS = 100;
    private static final int ZHENYUAN_HOLD_MAX_TICKS = 160;
    private static final int ZHENYUAN_HOLD_FLASH_MS = 140;
    private long screenTickCounter = 0L;
    private int zhenyuanHoldAttrIndex = -1;
    private long zhenyuanHoldStartTick = 0L;
    private long zhenyuanHoldNextSpendTick = 0L;
    private double lastMouseX = 0.0;
    private double lastMouseY = 0.0;
    private final int[] bonusSettingsButtonRect = new int[4];
    private int[] bonusSettingsPopupRect = null;
    private int[] bonusSettingsPopupCloseRect = null;
    private final List<BonusToggleRowRect> bonusToggleRowRects = new ArrayList<>();
    private boolean bonusSettingsPopupOpen = false;
    private CultivationBonusCategory hoveredBonusCategory = null;
    private final List<int[]> learnedCellRects = new ArrayList<>();
    private final List<int[]> wheelCellRects = new ArrayList<>();
    private final List<int[]> techniqueLearnedCellRects = new ArrayList<>();
    private int[] techniqueEquippedRect = null;
    private String hoveredTechniqueId = null;
    private float sharedTabScale = 1.0f;
    private int techScrollOffset = 0;
    private int spellScrollOffset = 0;
    private boolean mouseOverTechGrid = false;
    private boolean mouseOverSpellGrid = false;
    private int techMaxScroll = 0;
    private int spellMaxScroll = 0;
    private String draggingSpellId = null;
    private int draggingFromSlot = -1;
    private double dragStartX;
    private double dragStartY;
    private boolean isDragging = false;
    private static final int FILTER_ROW_H = 11;
    private static final int FILTER_CHIP_H = 11;
    private static final float FILTER_TEXT_SCALE = 0.65f;
    private static final int FILTER_INK = -15067628;
    private static final int FILTER_BG = -12635095;
    private static final int FILTER_BG_ACTIVE = -4703686;
    private static final int FILTER_BG_HOVER = -11385542;
    private static final int FILTER_TEXT = -726312;
    private static final int FILTER_TEXT_ACTIVE = -5720;

    // ═══════════════════════════════════════════
    // 构造与初始化
    // ═══════════════════════════════════════════

    private static ResourceLocation guiTexture(String path) {
        return new ResourceLocation("friday_cultivation", path);
    }

    private static float effectiveTextScale() {
        try {
            String lang = Minecraft.getInstance().getLanguageManager().getSelected();
            if (lang != null && lang.startsWith("zh")) {
                return 0.85f;
            }
        } catch (Throwable throwable) {
            // 忽略
        }
        return 0.7f;
    }

    public CultivationScreen() {
        super(Component.translatable("screen.friday_cultivation.cultivation.title"));
    }

    @Override
    protected void init() {
        super.init();
        int leftX = (this.width - PANEL_WIDTH) / 2;
        int topY = (this.height - PANEL_HEIGHT) / 2;
        int leftHalfX = leftX;
        int rightHalfX = leftX + 160;

        this.breakthroughBtn = new CinnabarButton(leftHalfX + 12, topY + PANEL_HEIGHT - 26, 136, 18,
                Component.translatable("screen.friday_cultivation.breakthrough"), btn -> submitBreakthroughRequest());
        this.addRenderableWidget(this.breakthroughBtn);

        this.reincarnationBtn = new CinnabarButton(leftHalfX + 12, topY + PANEL_HEIGHT - 26, 136, 18,
                Component.translatable("screen.friday_cultivation.reincarnation.button"),
                btn -> ModNetwork.CHANNEL.sendToServer(new RequestReincarnationScreenPacket()));
        this.reincarnationBtn.active = false;
        this.addRenderableWidget(this.reincarnationBtn);

        this.goDifuBtn = new CinnabarButton(leftHalfX + 12, topY + PANEL_HEIGHT - 26, 136, 18,
                Component.translatable("screen.friday_cultivation.reincarnation.go_difu"), btn -> {
            Minecraft.getInstance().setScreen(null);
            ModNetwork.CHANNEL.sendToServer(new RequestGoDifuPacket());
        });
        this.goDifuBtn.active = false;
        this.addRenderableWidget(this.goDifuBtn);

        this.timeAccelerationStartBtn = new MiniCinnabarButton(leftHalfX + 58, topY + 62, 44, 10,
                Component.translatable("screen.friday_cultivation.time_acceleration.button"),
                btn -> Minecraft.getInstance().setScreen(new TimeAccelerationChoiceScreen()));
        this.timeAccelerationStartBtn.active = false;
        this.addRenderableWidget(this.timeAccelerationStartBtn);

        this.timeAccelerationStopBtn = new MiniCinnabarButton(leftHalfX + 124, topY + 62, 30, 10,
                Component.translatable("screen.friday_cultivation.time_acceleration.stop"),
                btn -> ModNetwork.CHANNEL.sendToServer(new SetTimeAccelerationPacket(0)));
        this.timeAccelerationStopBtn.active = false;
        this.addRenderableWidget(this.timeAccelerationStopBtn);

        int tabReserveRight = 16;
        int tabAreaW = 153 - tabReserveRight;
        int tabW = tabAreaW / 4 - 1;
        int tabY = topY + 10;
        Supplier<Float> sharedScaleSup = () -> this.sharedTabScale;
        int tabH = 14;
        int tabGap = 1;
        int tabStartX = rightHalfX + 4;

        this.tabAttrBtn = new BambooTabButton(tabStartX, tabY, tabW, tabH,
                Component.translatable("screen.friday_cultivation.tab.attributes"), btn -> {
            this.currentTab = Tab.ATTRIBUTES;
            this.selectedSpellId = null;
        });
        this.tabAttrBtn.setActiveSupplier(() -> this.currentTab == Tab.ATTRIBUTES);
        this.tabAttrBtn.setForcedScaleSupplier(sharedScaleSup);

        this.tabTechBtn = new BambooTabButton(tabStartX + (tabW + tabGap), tabY, tabW, tabH,
                Component.translatable("screen.friday_cultivation.tab.techniques"), btn -> {
            this.currentTab = Tab.TECHNIQUES;
            this.selectedSpellId = null;
        });
        this.tabTechBtn.setActiveSupplier(() -> this.currentTab == Tab.TECHNIQUES);
        this.tabTechBtn.setForcedScaleSupplier(sharedScaleSup);

        this.tabSpellBtn = new BambooTabButton(tabStartX + (tabW + tabGap) * 2, tabY, tabW, tabH,
                Component.translatable("screen.friday_cultivation.tab.spells"), btn -> {
            this.currentTab = Tab.SPELLS;
        });
        this.tabSpellBtn.setActiveSupplier(() -> this.currentTab == Tab.SPELLS);
        this.tabSpellBtn.setForcedScaleSupplier(sharedScaleSup);

        this.tabBreakthroughBtn = new BambooTabButton(tabStartX + (tabW + tabGap) * 3, tabY, tabW, tabH,
                Component.translatable("screen.friday_cultivation.tab.breakthrough"), btn -> {
            this.currentTab = Tab.BREAKTHROUGH;
            this.selectedSpellId = null;
        });
        this.tabBreakthroughBtn.setActiveSupplier(() -> this.currentTab == Tab.BREAKTHROUGH);
        this.tabBreakthroughBtn.setForcedScaleSupplier(sharedScaleSup);

        this.addRenderableWidget(this.tabAttrBtn);
        this.addRenderableWidget(this.tabTechBtn);
        this.addRenderableWidget(this.tabSpellBtn);
        this.addRenderableWidget(this.tabBreakthroughBtn);

        CloseIconButton closeBtn = new CloseIconButton(leftX + PANEL_WIDTH - 16, topY + 5, 12, btn -> this.onClose());
        this.addRenderableWidget(closeBtn);

        int spellBottomRowY = topY + PANEL_HEIGHT - SPELL_BOTTOM_ROW_Y_FROM_BOTTOM;
        this.toggleSpellBtn = new MiniCinnabarButton(rightHalfX + 160 - 12 - SPELL_PASSIVE_TOGGLE_WIDTH,
                spellBottomRowY, SPELL_PASSIVE_TOGGLE_WIDTH, SPELL_BOTTOM_CONTROL_HEIGHT,
                Component.translatable("screen.friday_cultivation.spell.toggle.disable"), btn -> {
            if (this.selectedSpellId == null) return;
            Spell sel = Spell.byId(this.selectedSpellId);
            if (sel == null) return;
            LocalPlayer p = Minecraft.getInstance().player;
            if (p == null) return;
            CultivationData ic = CultivationCapability.get(p).orElse(null);
            if (ic == null) return;
            boolean currentlyEnabled = ic.isSpellEnabled(sel);
            ModNetwork.CHANNEL.sendToServer(new ToggleSpellPacket(sel.id(), !currentlyEnabled));
        });
        this.toggleSpellBtn.active = false;
        this.addRenderableWidget(this.toggleSpellBtn);

        int spellContentLeft = rightHalfX + SPELL_CONTENT_INSET;
        this.spellTerrainDestructionBtn = new LabeledToggleSwitchButton(spellContentLeft, spellBottomRowY,
                SPELL_TERRAIN_TOGGLE_WIDTH, SPELL_BOTTOM_CONTROL_HEIGHT,
                Component.translatable("screen.friday_cultivation.spell_terrain.label"), btn -> {
            LocalPlayer p = Minecraft.getInstance().player;
            if (p == null) return;
            CultivationData ic = CultivationCapability.get(p).orElse(null);
            if (ic == null) return;
            if (ic.isSpellTerrainDestructionForcedOffByServer()) return;
            ModNetwork.CHANNEL.sendToServer(
                    new SetSpellTerrainDestructionPacket(!ic.isSpellTerrainDestructionEnabled()));
        });
        this.spellTerrainDestructionBtn.active = false;
        this.addRenderableWidget(this.spellTerrainDestructionBtn);

        this.nameEditBox = new EditBox(this.font, leftHalfX + 12, topY + 74, 136, 12,
                Component.translatable("screen.friday_cultivation.attr.id.name"));
        this.nameEditBox.setMaxLength(16);
        this.nameEditBox.setVisible(false);
        this.nameEditBox.setFocused(false);
        this.addRenderableWidget(this.nameEditBox);
        this.editingName = false;
    }

    // ═══════════════════════════════════════════
    // 主渲染入口
    // ═══════════════════════════════════════════

    @Override
    public void render(@NotNull GuiGraphics gfx, int mouseX, int mouseY, float partial) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        this.renderBackground(gfx);

        int leftX = (this.width - PANEL_WIDTH) / 2;
        int topY = (this.height - PANEL_HEIGHT) / 2;

        if (this.tabAttrBtn != null && this.tabTechBtn != null && this.tabSpellBtn != null && this.tabBreakthroughBtn != null) {
            float s = Math.min(this.tabAttrBtn.computeAutoScale(),
                    Math.min(this.tabTechBtn.computeAutoScale(),
                            Math.min(this.tabSpellBtn.computeAutoScale(), this.tabBreakthroughBtn.computeAutoScale())));
            this.sharedTabScale = Math.min(0.86f, s);
        }

        this.drawHardShadow(gfx, leftX, topY, PANEL_WIDTH, PANEL_HEIGHT, 6);
        RenderSystem.enableBlend();
        gfx.blit(BG_TEXTURE, leftX, topY, 0.0f, 0.0f, PANEL_WIDTH, PANEL_HEIGHT, BG_TEX_W, BG_TEX_H);
        RenderSystem.disableBlend();
        this.drawPanelFrame(gfx, leftX, topY, PANEL_WIDTH, PANEL_HEIGHT);

        // 中缝虚线
        int splitX = leftX + 160;
        for (int dy = 8; dy < 192; dy += 4) {
            gfx.fill(splitX, topY + dy, splitX + 1, topY + dy + 2, -2140579256);
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            super.render(gfx, mouseX, mouseY, partial);
            return;
        }
        CultivationData ic = CultivationCapability.get(player).orElse(null);
        if (ic == null) {
            super.render(gfx, mouseX, mouseY, partial);
            return;
        }

        // 每帧重置悬停状态
        this.hoveredSpellId = null;
        this.hoveredTechniqueId = null;
        this.filterButtonRects.clear();
        this.mouseOverTechGrid = false;
        this.breakthroughOptionRects.clear();
        this.breakthroughHistoryButtonRect = null;
        this.breakthroughHistoryPopupRect = null;
        this.breakthroughHistoryPopupCloseRect = null;
        this.hoveredBreakthroughOptionKind = 0;
        this.hoveredBreakthroughOptionOrdinal = -1;
        this.bonusSettingsButtonRect[0] = 0; this.bonusSettingsButtonRect[1] = 0;
        this.bonusSettingsButtonRect[2] = 0; this.bonusSettingsButtonRect[3] = 0;
        this.bonusSettingsPopupRect = null;
        this.bonusSettingsPopupCloseRect = null;
        this.bonusToggleRowRects.clear();
        this.hoveredBonusCategory = null;
        this.mouseOverSpellGrid = false;

        this.normalizeBreakthroughSelection(ic, player);
        if (this.currentTab != Tab.BREAKTHROUGH || ic.getRealm().ordinal() < Realm.GOLDEN_CORE.ordinal()) {
            this.breakthroughHistoryPopupOpen = false;
        }
        if (this.currentTab != Tab.ATTRIBUTES) {
            this.bonusSettingsPopupOpen = false;
        }

        this.updateBottomActionButtons(topY, splitX, player, ic);
        this.renderLeftPanel(gfx, leftX, topY, player, ic, mouseX, mouseY);
        this.renderRightPanel(gfx, splitX, topY, player, ic, mouseX, mouseY);
        this.highlightActiveTab(gfx);
        this.updateBottomActionButtons(topY, splitX, player, ic);
        this.updateToggleSpellBtn(ic);
        this.updateSpellTerrainDestructionBtn(ic);

        super.render(gfx, mouseX, mouseY, partial);

        // ── 悬停tooltip区域 ──
        this.renderTooltips(gfx, ic, player, mouseX, mouseY);

        if (this.bonusSettingsPopupOpen) {
            this.renderBonusSettingsPopup(gfx, ic, mouseX, mouseY);
            return;
        }
        if (this.breakthroughHistoryPopupOpen) {
            this.renderBreakthroughHistoryPopup(gfx, ic, mouseX, mouseY);
        } else {
            this.renderBreakthroughTooltip(gfx, mouseX, mouseY);
        }
        this.renderOpenDropdown(gfx, mouseX, mouseY);
    }

    /** 集中处理各类悬停tooltip（身份/灵根/体质/性别/道基/法术/功法/真元/增益设置） */
    private void renderTooltips(GuiGraphics gfx, CultivationData ic, LocalPlayer player, int mouseX, int mouseY) {
        // 时间加速按钮tooltip
        if (this.timeAccelerationStartBtn != null && this.timeAccelerationStartBtn.active
                && this.timeAccelerationStartBtn.isMouseOver(mouseX, mouseY)) {
            MutableComponent tooltip = this.timeAccelerationStartBtn.visible
                    ? Component.translatable("tooltip.friday_cultivation.time_acceleration.start")
                    : Component.translatable("tooltip.friday_cultivation.time_acceleration.requires_cushion");
            gfx.renderTooltip(this.font, tooltip, mouseX, mouseY);
        }
        // 法术地形破坏开关tooltip
        if (this.spellTerrainDestructionBtn != null && this.spellTerrainDestructionBtn.active
                && this.spellTerrainDestructionBtn.isMouseOver(mouseX, mouseY)) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.translatable("tooltip.friday_cultivation.spell_terrain.title").withStyle(ChatFormatting.GOLD));
            if (ic.isSpellTerrainDestructionForcedOffByServer()) {
                lines.add(Component.translatable("tooltip.friday_cultivation.spell_terrain.locked").withStyle(ChatFormatting.RED));
            } else {
                lines.add(Component.translatable(ic.isSpellTerrainDestructionEnabled()
                        ? "tooltip.friday_cultivation.spell_terrain.enabled"
                        : "tooltip.friday_cultivation.spell_terrain.disabled").withStyle(ChatFormatting.GRAY));
            }
            gfx.renderTooltip(this.font, lines, Optional.empty(), mouseX, mouseY);
        }
        // 宗门入口箭头
        if (this.sectEntryArrowRect != null && inRect(this.sectEntryArrowRect, mouseX, mouseY)) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.translatable("screen.friday_cultivation.cultivation.open_sect").withStyle(ChatFormatting.GOLD));
            lines.add(Component.translatable("tooltip.friday_cultivation.identity.open_sect").withStyle(ChatFormatting.GRAY));
            this.renderOriginInfoTooltip(gfx, lines, mouseX, mouseY);
        } else if (this.identityRowRect != null && (ic.hasChosenIdentity() || ic.isSoulReaperIdentity())
                && inRect(this.identityRowRect, mouseX, mouseY)) {
            List<Component> lines = new ArrayList<>();
            lines.add(this.formatIdentityValue(ic).copy().withStyle(ChatFormatting.GOLD));
            if (ic.isSoulReaperIdentity()) {
                lines.add(Component.translatable("tooltip.friday_cultivation.soul_reaper.identity_desc").withStyle(ChatFormatting.GRAY));
            } else {
                Identity identity = Identity.byId(ic.getIdentityId());
                lines.add(Component.translatable(identity.descriptionKey()).withStyle(ChatFormatting.GRAY));
            }
            this.renderOriginInfoTooltip(gfx, lines, mouseX, mouseY);
        }
        // 灵根行
        if (this.spiritRootRowRect != null && inRect(this.spiritRootRowRect, mouseX, mouseY)) {
            SpiritRoot root = ic.getSpiritRoot();
            List<Component> lines = new ArrayList<>();
            int color = DrawCardWidget.rarityRgbColor(root.rarity());
            ChatFormatting cf = nearestChatFormatting(color);
            lines.add(Component.translatable(root.translationKey()).withStyle(cf));
            lines.add(Component.literal("[").append(Component.translatable(root.rarity().translationKey())).append(Component.literal("]")).withStyle(cf));
            lines.add(Component.translatable(root.tooltipKey()).withStyle(ChatFormatting.GRAY));
            this.renderOriginInfoTooltip(gfx, lines, mouseX, mouseY);
        }
        // 体质行
        if (this.physiqueRowRect != null && inRect(this.physiqueRowRect, mouseX, mouseY)) {
            Physique physique = ic.getPhysique();
            List<Component> lines = new ArrayList<>();
            ChatFormatting cf = physiqueChatFormatting(physique.rarity());
            lines.add(Component.translatable(physique.translationKey()).withStyle(cf));
            lines.add(Component.literal("[").append(Component.translatable(physique.rarity().translationKey())).append(Component.literal("]")).withStyle(cf));
            lines.add(Component.translatable(physique.tooltipKey()).withStyle(ChatFormatting.GRAY));
            this.renderOriginInfoTooltip(gfx, lines, mouseX, mouseY);
        }
        // 性别行
        if (this.genderCellRect != null && inRect(this.genderCellRect, mouseX, mouseY)) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.translatable("screen.friday_cultivation.attr.id.gender").withStyle(ChatFormatting.GOLD));
            lines.add(Component.translatable("screen.friday_cultivation.gender.edit_hint", ic.getGenderEditsLeft()).withStyle(ChatFormatting.GRAY));
            this.renderOriginInfoTooltip(gfx, lines, mouseX, mouseY);
        }
        // 道基行
        if (this.foundationCellRect != null && inRect(this.foundationCellRect, mouseX, mouseY)) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.translatable("tooltip.friday_cultivation.foundation.title").withStyle(ChatFormatting.GOLD));
            lines.add(Component.translatable("tooltip.friday_cultivation.foundation.human").withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable("tooltip.friday_cultivation.foundation.blood").withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable("tooltip.friday_cultivation.foundation.earth").withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable("tooltip.friday_cultivation.foundation.heaven").withStyle(ChatFormatting.GRAY));
            this.renderOriginInfoTooltip(gfx, lines, mouseX, mouseY);
        }
        // 真元属性标签tooltip
        if (this.currentTab == Tab.ATTRIBUTES) {
            for (int i = 0; i < 5; ++i) {
                int[] r = this.zhenyuanLabelRects[i];
                if (r[2] == 0 || !inRect(r, mouseX, mouseY)) continue;
                this.renderZhenyuanTooltip(gfx, mouseX, mouseY, ic, i);
                break;
            }
        }
        // 增益设置按钮tooltip
        if (this.currentTab == Tab.ATTRIBUTES) {
            int[] br = this.bonusSettingsButtonRect;
            if (!this.bonusSettingsPopupOpen && br[2] != 0 && inRect(br, mouseX, mouseY)) {
                List<Component> lines = new ArrayList<>();
                lines.add(Component.translatable("screen.friday_cultivation.attr.bonus_settings.button").withStyle(ChatFormatting.GOLD));
                lines.add(Component.translatable("screen.friday_cultivation.attr.bonus_settings.button_tooltip").withStyle(ChatFormatting.GRAY));
                gfx.renderTooltip(this.font, lines, Optional.empty(), mouseX, mouseY);
            }
        }
        // 法术标签页悬停法术tooltip
        if (this.currentTab == Tab.SPELLS && this.hoveredSpellId != null) {
            Spell hovered = Spell.byId(this.hoveredSpellId);
            if (hovered != null) {
                boolean enabled = ic.isSpellEnabled(hovered);
                List<Component> spellLines = hovered.tooltipLines(enabled);
                gfx.renderTooltip(this.font, spellLines, Optional.empty(), mouseX, mouseY);
            }
        }
        // 功法标签页悬停功法tooltip（照搬原模组 TooltipUtils 格式）
        if (this.currentTab == Tab.TECHNIQUES && this.hoveredTechniqueId != null) {
            Technique ht = Technique.byId(this.hoveredTechniqueId);
            if (ht != null) {
                List<Component> techLines = new ArrayList<>();
                ItemTier techTier = ItemTier.valueOf(ht.tier().name());
                techLines.add(TooltipUtils.tieredName(ht.displayName(), techTier));
                techLines.add(TooltipUtils.tierElementLine(techTier, ht.primaryElement()));
                techLines.add(TooltipUtils.statsLine(Component.translatable("tooltip.friday_cultivation.technique.path",
                        Component.translatable("dao_path.friday_cultivation." + ht.daoPath().id()))));
                TooltipUtils.addBlank(techLines);
                TooltipUtils.addSection(techLines, "tooltip.friday_cultivation.section.effect");
                techLines.add(TooltipUtils.descriptionLine(ht.description()));
                boolean isEq = ht.id().equals(ic.getEquippedTechniqueId());
                boolean compatible = canEquipTechniqueForScreenState(ic, ht);
                TooltipUtils.addBlank(techLines);
                String hintKey = isEq ? "screen.friday_cultivation.tech.tooltip.click_unequip"
                        : (compatible ? "screen.friday_cultivation.tech.tooltip.click_equip"
                        : (ht.isGhostDao() ? "screen.friday_cultivation.tech.tooltip.requires_soul"
                        : "screen.friday_cultivation.tech.tooltip.requires_living"));
                techLines.add(TooltipUtils.hintLine(Component.translatable(hintKey)));
                gfx.renderTooltip(this.font, techLines, Optional.empty(), mouseX, mouseY);
            }
        }
    }

    // ═══════════════════════════════════════════
    // 面板框架绘制
    // ═══════════════════════════════════════════

    private void drawPanelFrame(GuiGraphics gfx, int x, int y, int w, int h) {
        gfx.fill(x - 2, y - 2, x + w + 2, y, BORDER_DARK);
        gfx.fill(x - 2, y + h, x + w + 2, y + h + 2, BORDER_DARK);
        gfx.fill(x - 2, y, x, y + h, BORDER_DARK);
        gfx.fill(x + w, y, x + w + 2, y + h, BORDER_DARK);
        gfx.fill(x, y, x + w, y + 2, INK_BLACK);
        gfx.fill(x, y + h - 2, x + w, y + h, INK_BLACK);
        gfx.fill(x, y, x + 2, y + h, INK_BLACK);
        gfx.fill(x + w - 2, y, x + w, y + h, INK_BLACK);
        gfx.fill(x + 2, y + 2, x + w - 2, y + 3, BORDER_LIGHT);
        gfx.fill(x + 2, y + h - 3, x + w - 2, y + h - 2, BORDER_LIGHT);
        gfx.fill(x + 2, y + 2, x + 3, y + h - 2, BORDER_LIGHT);
        gfx.fill(x + w - 3, y + 2, x + w - 2, y + h - 2, BORDER_LIGHT);
    }

    private void drawHardShadow(GuiGraphics gfx, int x, int y, int w, int h, int offset) {
        gfx.fill(x + offset, y + h, x + w + offset, y + h + offset, SHADOW_HARD);
        gfx.fill(x + w, y + offset, x + w + offset, y + h, SHADOW_HARD);
    }

    private void drawSectionLabel(GuiGraphics gfx, Component label, int x, int y, int rightX) {
        gfx.fill(x, y, x + 2, y + 9, VERMILLION);
        this.drawSmall(gfx, label, x + 5, y + 1, TEXT_GREY);
        int textW = (int)(this.font.width(label) * effectiveTextScale());
        int dotStart = x + 7 + textW;
        for (int dx = dotStart; dx < rightX - 1; dx += 6) {
            gfx.fill(dx, y + 4, dx + 2, y + 5, -1721148856);
        }
    }

    private void drawCinnabarLeftLine(GuiGraphics gfx, int x, int yTop, int yBot) {
        gfx.fill(x, yTop, x + 2, yBot, VERMILLION);
    }

    private void drawDottedHLine(GuiGraphics gfx, int xLeft, int y, int xRight, int color) {
        for (int dx = xLeft; dx < xRight - 3; dx += 8) {
            gfx.fill(dx, y, dx + 4, y + 1, color);
        }
    }

    private void highlightActiveTab(GuiGraphics gfx) {
        BambooTabButton active = switch (this.currentTab) {
            case ATTRIBUTES -> this.tabAttrBtn;
            case TECHNIQUES -> this.tabTechBtn;
            case SPELLS -> this.tabSpellBtn;
            case BREAKTHROUGH -> this.tabBreakthroughBtn;
        };
        if (active != null) {
            int x = active.getX();
            int y = active.getY();
            int w = active.getWidth();
            int h = active.getHeight();
            gfx.fill(x - 1, y - 1, x + w + 1, y, GOLD_BRIGHT);
            gfx.fill(x - 1, y, x, y + h, GOLD_BRIGHT);
            gfx.fill(x + w, y, x + w + 1, y + h, GOLD_BRIGHT);
        }
    }

    // ═══════════════════════════════════════════
    // 底部按钮状态更新
    // ═══════════════════════════════════════════

    private void updateBottomActionButtons(int topY, int splitX, LocalPlayer player, CultivationData data) {
        boolean soul = data.isSoulState();
        boolean inDifu = player.level().dimension() == ModDimensions.DIFU;
        boolean canReincarnate = soul && inDifu && data.isReincarnationReady();
        boolean canGoDifu = soul && !inDifu;

        this.reincarnationBtn.active = canReincarnate;
        this.reincarnationBtn.visible = canReincarnate;
        this.goDifuBtn.active = canGoDifu;
        this.goDifuBtn.visible = canGoDifu;

        boolean canUseTimeAcceleration = data.canUseTimeAcceleration();
        boolean timeAccelerationActive = data.isTimeAccelerationActive();
        boolean sittingOnCushion = player.getVehicle() instanceof SeatEntity;
        this.timeAccelerationStartBtn.active = canUseTimeAcceleration && !timeAccelerationActive;
        this.timeAccelerationStartBtn.visible = this.timeAccelerationStartBtn.active && sittingOnCushion;
        this.timeAccelerationStopBtn.active = this.timeAccelerationStopBtn.visible =
                canUseTimeAcceleration && timeAccelerationActive;

        int breakthroughW = 112;
        this.breakthroughBtn.setX(splitX + 24);
        this.breakthroughBtn.setY(topY + PANEL_HEIGHT - 26);
        this.breakthroughBtn.setWidth(breakthroughW);
        // 原模组：visible=当前突破标签页且非真仙；active=可突破+非渡劫中+选中路线就绪
        this.breakthroughBtn.visible = this.currentTab == Tab.BREAKTHROUGH
                && data.getRealm() != Realm.TRUE_IMMORTAL;
        this.breakthroughBtn.active = data.canBreakthrough() && !data.isInTribulation()
                && this.isSelectedBreakthroughReady(data, player);
    }

    private void updateToggleSpellBtn(CultivationData data) {
        boolean canToggle = this.currentTab == Tab.SPELLS && this.selectedSpellId != null;
        this.toggleSpellBtn.visible = this.currentTab == Tab.SPELLS;
        if (canToggle) {
            Spell sel = Spell.byId(this.selectedSpellId);
            if (sel != null) {
                boolean enabled = data.isSpellEnabled(sel);
                this.toggleSpellBtn.active = true;
                this.toggleSpellBtn.setMessage(Component.translatable(
                        enabled ? "screen.friday_cultivation.spell.toggle.disable"
                                : "screen.friday_cultivation.spell.toggle.enable"));
            } else {
                this.toggleSpellBtn.active = false;
            }
        } else {
            this.toggleSpellBtn.active = false;
        }
    }

    private void updateSpellTerrainDestructionBtn(CultivationData data) {
        this.spellTerrainDestructionBtn.visible = this.currentTab == Tab.SPELLS;
        this.spellTerrainDestructionBtn.active = this.currentTab == Tab.SPELLS
                && !data.isSpellTerrainDestructionForcedOffByServer();
        this.spellTerrainDestructionBtn.setState(data.isSpellTerrainDestructionEnabled(), data.isSpellTerrainDestructionForcedOffByServer());
    }

    // ═══════════════════════════════════════════
    // 左侧面板渲染
    // ═══════════════════════════════════════════

    /**
     * 渲染玩家模型（照搬原模组：InventoryScreen.renderEntityInInventory float版本）
     * 1.20.1 Forge编译时找不到float重载（SRG映射问题），用反射调用运行时存在的方法
     */
    private void renderPlayerModel(GuiGraphics gfx, int x, int y, int scale, float mouseX, float mouseY, LivingEntity entity) {
        if (entity == null) return;
        try {
            // 反射调用 InventoryScreen.renderEntityInInventory(GuiGraphics, int, int, int, float, float, LivingEntity)
            java.lang.reflect.Method m = net.minecraft.client.gui.screens.inventory.InventoryScreen.class.getMethod(
                    "m_274545_", GuiGraphics.class, int.class, int.class, int.class, float.class, float.class, LivingEntity.class);
            m.invoke(null, gfx, x, y, scale, mouseX, mouseY, entity);
        } catch (NoSuchMethodException e1) {
            // SRG名找不到，尝试Mojang名
            try {
                java.lang.reflect.Method m = net.minecraft.client.gui.screens.inventory.InventoryScreen.class.getDeclaredMethod(
                        "renderEntityInInventory", GuiGraphics.class, int.class, int.class, int.class, float.class, float.class, LivingEntity.class);
                m.setAccessible(true);
                m.invoke(null, gfx, x, y, scale, mouseX, mouseY, entity);
            } catch (Exception e2) {
                // 都找不到，用EntityRenderDispatcher手动渲染
                renderPlayerModelFallback(gfx, x, y, scale, mouseX, mouseY, entity);
            }
        } catch (Exception e) {
            // 反射调用失败，用fallback
            renderPlayerModelFallback(gfx, x, y, scale, mouseX, mouseY, entity);
        }
    }

    /** 手动渲染fallback（参考vanilla InventoryScreen.renderEntityInInventory源码） */
    private void renderPlayerModelFallback(GuiGraphics gfx, int x, int y, int scale, float mouseX, float mouseY, LivingEntity entity) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        com.mojang.blaze3d.vertex.PoseStack pose = gfx.pose();
        pose.pushPose();
        pose.translate(x, y, 50.0);
        // vanilla做法：Y轴scale取负值修正头部朝下
        pose.scale(scale, -scale, scale);
        // 参考vanilla: rotationYXZ让模型正面朝向，鼠标控制旋转角度
        org.joml.Quaternionf rotation = new org.joml.Quaternionf().rotationYXZ(
                (float)Math.toRadians(180), 0.0f, (float)Math.toRadians(180));
        org.joml.Quaternionf mouseRot = new org.joml.Quaternionf().rotationYXZ(
                mouseY * 0.02f, mouseX * 0.02f, 0.0f);
        rotation.mul(mouseRot);
        pose.mulPose(rotation);
        net.minecraft.client.renderer.MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        net.minecraft.client.renderer.entity.EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        dispatcher.setRenderShadow(false);
        dispatcher.setRenderHitBoxes(false);
        try {
            dispatcher.render(entity, 0.0, 0.0, 0.0, 0.0f, 1.0f, pose, buffer, 0xF000F0);
        } catch (Exception ignored) {}
        buffer.endBatch();
        dispatcher.setRenderShadow(true);
        pose.popPose();
    }

    private void renderLeftPanel(GuiGraphics gfx, int x, int y, LocalPlayer player, CultivationData data, int mouseX, int mouseY) {
        int half = 160;
        int contentX = x + 12;
        int contentRight = x + half - 8;

        gfx.drawCenteredString(this.font, Component.translatable("screen.friday_cultivation.cultivation.title"),
                x + half / 2, y + 8, INK_BLACK);

        // 玩家模型（手动调用EntityRenderDispatcher渲染，避免InventoryScreen方法签名兼容问题）
        int modelX = x + half / 2;
        int modelY = y + 60;
        renderPlayerModel(gfx, modelX, modelY, 22, (float)(modelX - mouseX), (float)(modelY - 30 - mouseY), player);

        this.renderTimeAccelerationStatus(gfx, x, y, half, data);

        Realm realm = data.getRealm();
        SubStage sub = data.getSubStage();
        int colGap = 6;
        int colW = (half - 24 - colGap) / 2;
        int col1X = contentX;
        int col1Right = col1X + colW;
        int col2X = col1Right + colGap;
        int col2Right = col2X + colW;
        int infoY = y + 76;
        int rowH = 10;

        SpiritRoot root = data.getSpiritRoot();
        Physique physique = data.getPhysique();
        FoundationDao fdao = data.getFoundationDao();
        String displayName = data.getCustomName().isEmpty() ? player.getName().getString() : data.getCustomName();
        MutableComponent genderVal = Component.translatable(
                data.getGender() == 2 ? "screen.friday_cultivation.gender.female" : "screen.friday_cultivation.gender.male");
        MutableComponent realmValue = data.isLooseImmortal()
                ? Component.translatable("realm.friday_cultivation.loose_immortal.level." + data.getLooseImmortalTribulations())
                : (realm == Realm.MORTAL || realm == Realm.TRUE_IMMORTAL
                    ? realm.displayName().copy()
                    : Component.translatable("screen.friday_cultivation.attr.id.realm_combined", realm.displayName(), sub.displayName()));
        MutableComponent foundationVal = fdao == FoundationDao.NONE
                ? Component.literal("\u2014") : Component.translatable(fdao.translationKey());
        String yearUnit = Component.translatable("screen.friday_cultivation.attr.year_unit").getString();
        boolean nearImmortal = LifespanHelper.isNearImmortal(data);
        MutableComponent boneAgeVal = Component.literal(LifespanHelper.displayBoneAge(data) + yearUnit);
        MutableComponent lifespanVal = Component.literal(nearImmortal ? "\u221e" : LifespanHelper.lifespanCap(data) + yearUnit);

        if (!this.editingName) {
            this.drawInfoCell(gfx, col1X, col1Right, infoY,
                    Component.translatable("screen.friday_cultivation.attr.id.name"),
                    Component.literal(displayName), GREEN_INK);
        }
        this.nameCellRect = new int[]{col1X, infoY - 1, col1Right, infoY + rowH - 1};
        this.drawInfoCell(gfx, col2X, col2Right, infoY,
                Component.translatable("screen.friday_cultivation.attr.id.gender"), genderVal, -9810000);
        this.genderCellRect = new int[]{col2X, infoY - 1, col2Right, infoY + rowH - 1};

        infoY += rowH;
        this.drawInfoCell(gfx, col1X, col1Right, infoY,
                Component.translatable("screen.friday_cultivation.attr.id.race"),
                this.formatRaceValue(data), data.isSoulState() ? -7704640 : INK_BLACK);
        this.drawInfoCell(gfx, col2X, col2Right, infoY,
                Component.translatable("screen.friday_cultivation.attr.id.realm"), realmValue, VERMILLION);

        boolean hasSectEntry = data.hasSectDisplay();
        int sectArrowSpace = hasSectEntry ? 13 : 0;
        infoY += rowH;
        this.drawInfoCell(gfx, col1X, col1Right - sectArrowSpace, infoY,
                Component.translatable("screen.friday_cultivation.attr.id.identity"),
                this.formatIdentityValue(data), INK_BLACK);
        this.identityRowRect = new int[]{col1X, infoY, col1Right - sectArrowSpace, infoY + rowH};
        if (hasSectEntry) {
            this.sectEntryArrowRect = new int[]{col1Right - 11, infoY, col1Right, infoY + rowH};
            this.drawSectEntryArrow(gfx, this.sectEntryArrowRect, inRect(this.sectEntryArrowRect, mouseX, mouseY));
        } else {
            this.sectEntryArrowRect = null;
        }
        this.drawInfoCell(gfx, col2X, col2Right, infoY,
                Component.translatable("screen.friday_cultivation.attr.id.spirit_root"),
                Component.translatable(root.translationKey()), DrawCardWidget.rarityRgbColor(root.rarity()));
        this.spiritRootRowRect = new int[]{col2X, infoY, col2Right, infoY + rowH};

        infoY += rowH;
        this.drawInfoCell(gfx, col1X, col1Right, infoY,
                Component.translatable("screen.friday_cultivation.attr.id.physique_kind"),
                Component.translatable(physique.translationKey()), physiqueRowColor(physique));
        this.physiqueRowRect = new int[]{col1X, infoY, col1Right, infoY + rowH};
        this.drawInfoCell(gfx, col2X, col2Right, infoY,
                Component.translatable("screen.friday_cultivation.attr.id.foundation"), foundationVal, -7706064);
        this.foundationCellRect = new int[]{col2X, infoY, col2Right, infoY + rowH};

        infoY += rowH;
        this.drawInfoCell(gfx, col1X, col1Right, infoY,
                Component.translatable("screen.friday_cultivation.attr.bone_age_label"), boneAgeVal, INK_BLACK);
        this.drawInfoCell(gfx, col2X, col2Right, infoY,
                Component.translatable("screen.friday_cultivation.attr.lifespan_label"), lifespanVal, INK_BLACK);

        // 状态栏：HP / 修炼 / 灵气
        float hp = player.getHealth();
        float maxHp = player.getMaxHealth();
        int statusBarW = half - 24;
        infoY += rowH + 3;
        this.drawLeftStatusBar(gfx, ICON_HP, contentX, infoY, statusBarW,
                maxHp <= 0.0f ? 0.0f : hp / maxHp,
                Component.translatable("screen.friday_cultivation.hp_short"),
                String.format("%.0f / %.0f", hp, maxHp), -1944235, -5758944);
        infoY += 11;
        long curCult = data.getCultivationProgress();
        long maxCult = data.getMaxCultivation();
        this.drawLeftStatusBar(gfx, ICON_CULTIVATION, contentX, infoY, statusBarW,
                maxCult == 0L ? 0.0f : (float) curCult / (float) maxCult,
                Component.translatable("screen.friday_cultivation.cult_short"),
                curCult + " / " + maxCult, -928374, -3631046);
        infoY += 11;
        long curQi = data.getCurrentQi();
        long maxQi = data.getMaxQi();
        this.drawLeftStatusBar(gfx, ICON_QI, contentX, infoY, statusBarW,
                maxQi == 0L ? 0.0f : (float) curQi / (float) maxQi,
                Component.translatable("screen.friday_cultivation.qi_short"),
                curQi + " / " + maxQi, -9583434, -13729678);
        infoY += 14;

        // 渡劫状态提示
        if (data.isInTribulation()) {
            int noteX = contentX;
            int noteY = infoY;
            int noteW = half - 24;
            int noteH = 12;
            gfx.fill(noteX, noteY, noteX + noteW, noteY + noteH, 347617850);
            this.drawCinnabarLeftLine(gfx, noteX, noteY, noteY + noteH);
            this.drawSmall(gfx, Component.translatable("screen.friday_cultivation.tribulation_active",
                    Realm.formatTribulationCount(data.getTribulationStrikesRemaining(), data.getTribulationBoltsPerWave())),
                    noteX + 4, noteY + 2, VERMILLION_DEEP);
        }

        // 牛头马面倒计时
        Component reaperCountdown = this.soulReaperCountdownText(data, player);
        if (reaperCountdown != null && this.goDifuBtn != null && this.goDifuBtn.active) {
            int bottomButtonY = y + PANEL_HEIGHT - 26;
            int countdownY = Math.max(infoY + 1, bottomButtonY - 12);
            countdownY = Math.min(countdownY, bottomButtonY - 9);
            this.drawTinyMultilineCentered(gfx, reaperCountdown, x + half / 2, countdownY, half - 28, VERMILLION_DEEP);
        }

        // 突破提示（用户要求删除：不在左面板显示突破提示文本）
        // int btnY = this.breakthroughBtn.getY();
        // if (this.breakthroughBtn.active && this.currentTab != Tab.BREAKTHROUGH) {
        //     ... 已删除 ...
        // }
    }

    /** 左侧状态栏（图标+标签+进度条+数值） */
    private void drawLeftStatusBar(GuiGraphics gfx, ResourceLocation icon, int x, int y, int w, float pct,
                                   Component label, String valueText, int barColor, int valueColor) {
        int iconSize = LEFT_STATUS_BAR_ICON_SIZE;
        gfx.blit(icon, x, y + 1, 0, 0, iconSize, iconSize, iconSize, iconSize);
        int labelX = x + iconSize + LEFT_STATUS_BAR_ICON_LABEL_GAP;
        int labelSlot = LEFT_STATUS_BAR_LABEL_SLOT;
        // 标签（缩放）
        gfx.pose().pushPose();
        gfx.pose().translate(labelX, y + 2, 0.0f);
        gfx.pose().scale(LEFT_STATUS_BAR_LABEL_SCALE, LEFT_STATUS_BAR_LABEL_SCALE, 1.0f);
        gfx.drawString(this.font, label, 0, 0, TEXT_GREY, false);
        gfx.pose().popPose();
        int barX = labelX + labelSlot + LEFT_STATUS_BAR_LABEL_BAR_GAP;
        int barW = w - (barX - x) - 1;
        int barY = y + 3;
        int barH = 4;
        gfx.fill(barX, barY, barX + barW, barY + barH, CELL_BG_DARK);
        gfx.fill(barX, barY, barX + (int)(barW * Math.max(0.0f, Math.min(1.0f, pct))), barY + barH, barColor);
        // 数值（缩放）
        int valueW = (int)(this.font.width(valueText) * LEFT_STATUS_BAR_LABEL_SCALE);
        gfx.pose().pushPose();
        gfx.pose().translate(barX + barW - valueW, y + 7, 0.0f);
        gfx.pose().scale(LEFT_STATUS_BAR_LABEL_SCALE, LEFT_STATUS_BAR_LABEL_SCALE, 1.0f);
        gfx.drawString(this.font, valueText, 0, 0, valueColor, false);
        gfx.pose().popPose();
    }

    private void renderTimeAccelerationStatus(GuiGraphics gfx, int x, int y, int half, CultivationData data) {
        if (!data.isTimeAccelerationActive()) return;
        int statusX = x + 16;
        int statusY = y + 64;
        int statusRight = x + half - 40;
        gfx.fill(statusX - 2, statusY - 2, statusRight, statusY + 10, 1712985620);
        this.drawCinnabarLeftLine(gfx, statusX - 2, statusY - 2, statusY + 10);
        MutableComponent status = Component.translatable("screen.friday_cultivation.time_acceleration.active_status",
                data.getTimeAccelerationMultiplier(),
                formatTimeAccelerationElapsed(data.getTimeAccelerationElapsedTicks()),
                formatTimeAccelerationYears(data.getTimeAccelerationElapsedTicks()));
        int availableW = Math.max(1, statusRight - statusX - 6);
        float scale = Math.min(0.68f, (float) availableW / Math.max(1, this.font.width(status)));
        scale = Math.max(0.5f, scale);
        this.drawScaled(gfx, status, statusX + 3, statusY + 1, GOLD_BORDER, scale);
    }

    private static String formatTimeAccelerationElapsed(long ticks) {
        long seconds = Math.max(0L, ticks / 20L);
        long hours = seconds / 3600L;
        long minutes = seconds % 3600L / 60L;
        long secs = seconds % 60L;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    private static String formatTimeAccelerationYears(long ticks) {
        double years = (double) Math.max(0L, ticks) / 24000.0;
        if (years >= 100.0) return String.format(Locale.ROOT, "%.0f", years);
        if (years >= 10.0) return String.format(Locale.ROOT, "%.1f", years);
        return String.format(Locale.ROOT, "%.2f", years);
    }

    // ═══════════════════════════════════════════
    // 右侧面板分发
    // ═══════════════════════════════════════════

    private void renderRightPanel(GuiGraphics gfx, int splitX, int y, LocalPlayer player, CultivationData data, int mouseX, int mouseY) {
        int contentX = splitX + 12;
        int contentY = y + 36;
        switch (this.currentTab) {
            case ATTRIBUTES -> this.renderAttributesTab(gfx, contentX, contentY, data, mouseX, mouseY);
            case TECHNIQUES -> this.renderTechniquesTab(gfx, splitX, contentY, data, mouseX, mouseY);
            case SPELLS -> this.renderSpellsTab(gfx, splitX, contentY, data, mouseX, mouseY);
            case BREAKTHROUGH -> this.renderBreakthroughTab(gfx, contentX, contentY, player, data, mouseX, mouseY);
        }
    }

    // ═══════════════════════════════════════════
    // 标签页：属性 / 功法 / 法术 / 突破
    // （照搬原模组，各标签页方法后续逐个补齐）
    // ═══════════════════════════════════════════

    private void renderAttributesTab(GuiGraphics gfx, int x, int y, CultivationData data, int mouseX, int mouseY) {
        // 照搬原模组 renderAttributesTab：属性行 + 真元雷达图 + 增益设置按钮
        int contentTop = y;
        com.friday.cultivation.technique.Technique eq = com.friday.cultivation.technique.Technique.byId(data.getEquippedTechniqueId());
        com.friday.cultivation.technique.Technique.Bonus tb = eq == null ? com.friday.cultivation.technique.Technique.Bonus.NONE : eq.bonus();
        int rightX = x + 160 - 24;
        LocalPlayer pp = Minecraft.getInstance().player;
        boolean meleeBonusEnabled = data.isBonusCategoryEnabled(CultivationBonusCategory.MELEE_DAMAGE);
        int zyAtk = !meleeBonusEnabled || pp == null ? 0 : ZhenyuanBonusHelper.physiqueAttackBonus(pp);
        int baseAttackDisplay = meleeBonusEnabled ? data.getAttack() : 0;
        int totalAtkBonus = meleeBonusEnabled ? tb.attack + zyAtk : 0;
        y = this.drawStatRow(gfx, x, rightX, y, "attack", formatBonus(baseAttackDisplay, totalAtkBonus), totalAtkBonus > 0);
        y = this.drawBodyDefenseRow(gfx, x, rightX, y, data, pp);
        boolean immortalIncantation = pp != null && TechniqueBonusHelper.isImmortalIncantationActive(pp);
        double realmBase = PlayerQiAbsorptionHelper.baseAbsorbMultiplier(data);
        double rootMult = pp == null ? 1.0 : SpiritRootBonusHelper.qiAbsorptionMultiplier(pp);
        double multFromTechniqueAndSpell = pp == null ? tb.qiAbsorbMult * (immortalIncantation ? 10.0 : 1.0) : TechniqueBonusHelper.qiAbsorbMultiplier(pp);
        boolean ghostDaoInDifu = pp != null && data.isSoulState() && eq != null && eq.isGhostDao() && pp.level().dimension() == ModDimensions.DIFU;
        int efficiency = ghostDaoInDifu ? 10 : PlayerQiConsumer.cultivationEfficiencyPerParticle(pp, data, QiElement.PURE);
        int qiRecovery = PlayerQiConsumer.nominalQiRecoveryPerSecond(pp, data, QiElement.PURE);
        if (ghostDaoInDifu) {
            qiRecovery += 10;
        }
        boolean cultivationEfficiencyEnabled = data.isBonusCategoryEnabled(CultivationBonusCategory.CULTIVATION_EFFICIENCY);
        int efficiencyRowY = y;
        y = this.drawStatRow(gfx, x, rightX, y, "cultivation_efficiency", formatCultivationEfficiency(efficiency), cultivationEfficiencyEnabled && (multFromTechniqueAndSpell > 1.0 || rootMult != 1.0 || data.isMeditating()));
        y = this.drawStatRow(gfx, x, rightX, y, "qi_recovery", formatQiRecoveryPerSecond(qiRecovery), qiRecovery > 0);
        y = this.drawStatRow(gfx, x, rightX, y, "refining", formatRefiningRank(data), false);
        y = this.drawStatRow(gfx, x, rightX, y, "alchemy", formatAlchemyRank(data), false);
        y += 2;
        this.renderZhenyuanRadar(gfx, x, rightX, y, data, mouseX, mouseY);
        this.renderBonusSettingsButton(gfx, x, contentTop + 200 - 58, mouseX, mouseY);
        if (mouseX >= x && mouseX < rightX && mouseY >= efficiencyRowY && mouseY < efficiencyRowY + 10 && cultivationEfficiencyEnabled && (multFromTechniqueAndSpell > 1.0 || rootMult != 1.0 || data.isMeditating())) {
            this.renderCultivationEfficiencyTooltip(gfx, mouseX, mouseY, data, realmBase, rootMult, multFromTechniqueAndSpell, data.isMeditating(), efficiency, ghostDaoInDifu);
        }
    }

    private void renderCultivationEfficiencyTooltip(GuiGraphics gfx, int mouseX, int mouseY, CultivationData data, double realmBase, double rootMult, double multTechniqueAndSpell, boolean meditating, int efficiency, boolean ghostDaoInDifu) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("screen.friday_cultivation.attr.cultivation_efficiency_label").withStyle(ChatFormatting.GOLD));
        if (ghostDaoInDifu) {
            lines.add(Component.translatable("screen.friday_cultivation.attr.cultivation_efficiency_tooltip.ghost_dao", efficiency).withStyle(ChatFormatting.GRAY));
            gfx.renderTooltip(this.font, lines, Optional.empty(), mouseX, mouseY);
            return;
        }
        Component realmName = data.getRealm().displayName();
        lines.add(Component.translatable("screen.friday_cultivation.attr.cultivation_efficiency_tooltip.realm", realmName, String.format("%.0f", realmBase)).withStyle(ChatFormatting.GRAY));
        if (rootMult != 1.0) {
            lines.add(Component.translatable("screen.friday_cultivation.attr.cultivation_efficiency_tooltip.root", String.format("%.1f", rootMult)).withStyle(ChatFormatting.GRAY));
        }
        if (multTechniqueAndSpell > 1.0) {
            lines.add(Component.translatable("screen.friday_cultivation.attr.cultivation_efficiency_tooltip.tech", String.format("%.1f", multTechniqueAndSpell)).withStyle(ChatFormatting.GRAY));
        }
        if (meditating) {
            lines.add(Component.translatable("screen.friday_cultivation.attr.cultivation_efficiency_tooltip.meditation", String.format("%.0f", 10.0)).withStyle(ChatFormatting.GRAY));
        }
        lines.add(Component.translatable("screen.friday_cultivation.attr.cultivation_efficiency_tooltip.final", efficiency).withStyle(ChatFormatting.WHITE));
        gfx.renderTooltip(this.font, lines, Optional.empty(), mouseX, mouseY);
    }

    private int drawStatRow(GuiGraphics gfx, int xLeft, int xRight, int y, String key, String value, boolean hasBonus) {
        int rowH = 10;
        MutableComponent label = Component.translatable("screen.friday_cultivation.attr." + key + "_label");
        this.drawAttrSmall(gfx, label, xLeft, y + 1, -12766422);
        MutableComponent val = Component.literal(value);
        int valW = (int)((float)this.font.width(val) * this.attrTabScale());
        this.drawAttrSmall(gfx, val, xRight - valW, y + 1, hasBonus ? -12950192 : -15067628);
        this.drawDottedHLine(gfx, xLeft, y + rowH - 1, xRight, -2006295992);
        return y + rowH;
    }

    private int drawBodyDefenseRow(GuiGraphics gfx, int xLeft, int xRight, int y, CultivationData data, LocalPlayer pp) {
        int rowH = 10;
        boolean enabled = data.isBodyDefenseEnabled();
        int raw = pp == null ? 0 : BodyDefenseHelper.playerRawBodyDefense(pp);
        MutableComponent label = Component.translatable("screen.friday_cultivation.attr.defense_label");
        this.drawAttrSmall(gfx, label, xLeft, y + 1, -12766422);
        String valueStr = enabled ? "+" + raw : "0";
        MutableComponent val = Component.literal(valueStr);
        int valW = (int)((float)this.font.width(val) * this.attrTabScale());
        this.drawAttrSmall(gfx, val, xRight - valW, y + 1, enabled ? -12950192 : -5222320);
        this.drawDottedHLine(gfx, xLeft, y + rowH - 1, xRight, -2006295992);
        return y + rowH;
    }

    private void drawAttrSmall(GuiGraphics gfx, Component text, int x, int y, int color) {
        float scale = effectiveTextScale() * 0.9f;
        gfx.pose().pushPose();
        gfx.pose().translate(x, y, 0.0f);
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.drawString(this.font, text, 0, 0, color, false);
        gfx.pose().popPose();
    }

    private float attrTabScale() {
        return effectiveTextScale() * 0.9f;
    }

    private static String formatBonus(int base, int bonus) {
        if (bonus <= 0) return "+" + base;
        return "+" + (base + bonus) + " (+" + bonus + ")";
    }

    private static String formatAlchemyRank(CultivationData data) {
        com.friday.cultivation.alchemy.AlchemyRank rank = data.getAlchemyRank();
        String rankName = rank.displayName().getString();
        if (rank.isMax()) return rankName + " (MAX)";
        return rankName + " (" + data.getAlchemyXp() + "/" + rank.xpToNext() + ")";
    }

    private static String formatRefiningRank(CultivationData data) {
        com.friday.cultivation.refining.RefiningRank rank = data.getRefiningRank();
        String rankName = rank.displayName().getString();
        if (rank.isMax()) return rankName + " (MAX)";
        return rankName + " (" + data.getRefiningXp() + "/" + rank.xpToNext() + ")";
    }

    private static String formatCultivationEfficiency(int amount) {
        return Component.translatable("screen.friday_cultivation.attr.cultivation_efficiency_value", Math.max(0, amount)).getString();
    }

    private static String formatQiRecoveryPerSecond(int amount) {
        return Component.translatable("screen.friday_cultivation.attr.qi_recovery_value", Math.max(0, amount)).getString();
    }

    private void renderBonusSettingsButton(GuiGraphics gfx, int x, int y, int mouseX, int mouseY) {
        int size = 12;
        boolean hover = mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size;
        this.bonusSettingsButtonRect[0] = x;
        this.bonusSettingsButtonRect[1] = y;
        this.bonusSettingsButtonRect[2] = x + size;
        this.bonusSettingsButtonRect[3] = y + size;
        if (hover || this.bonusSettingsPopupOpen) {
            gfx.fill(x - 1, y - 1, x + size + 1, y + size + 1, -5720);
        }
        gfx.fill(x, y, x + size, y + size, -15067628);
        gfx.fill(x + 1, y + 1, x + size - 1, y + size - 1, this.bonusSettingsPopupOpen ? -1981574 : -1517128);
        gfx.fill(x + 3, y + 3, x + 9, y + 4, -7707624);
        gfx.fill(x + 3, y + 6, x + 9, y + 7, -7707624);
        gfx.fill(x + 3, y + 9, x + 9, y + 10, -7707624);
        gfx.fill(x + 5, y + 2, x + 7, y + 5, -4703686);
        gfx.fill(x + 7, y + 5, x + 9, y + 8, -12950192);
        gfx.fill(x + 4, y + 8, x + 6, y + 11, -12747575);
    }

    private void renderZhenyuanRadar(GuiGraphics gfx, int x, int rightX, int y, CultivationData data, int mouseX, int mouseY) {
        // 照搬原模组 renderZhenyuanRadar：五边形雷达图 + 加点按钮
        MutableComponent header = Component.translatable("zhenyuan.friday_cultivation.title");
        MutableComponent tag = Component.translatable("zhenyuan.friday_cultivation.unallocated_tag", data.getUnallocatedZhenyuan());
        this.drawConfigSectionLabel(gfx, header, tag, x, y + 4, rightX, data.getUnallocatedZhenyuan() > 0);
        y += 12;
        int[] vals = new int[]{data.getAttrConstitution(), data.getAttrPhysique(), data.getAttrAgility(), data.getAttrSpellPower(), data.getAttrQiSea()};
        String[] labelKeys = new String[]{
            "zhenyuan.friday_cultivation.attr.constitution",
            "zhenyuan.friday_cultivation.attr.physique",
            "zhenyuan.friday_cultivation.attr.agility",
            "zhenyuan.friday_cultivation.attr.spell_power",
            "zhenyuan.friday_cultivation.attr.qi_sea"
        };
        int maxVal = 10;
        for (int v : vals) maxVal = Math.max(maxVal, v);
        if (maxVal % 5 != 0) maxVal = (maxVal / 5 + 1) * 5;
        int cx = (x + rightX) / 2;
        int radarTop = y;
        int radius = 28;
        int cy = radarTop + radius + 12;
        double[] cosA = new double[5];
        double[] sinA = new double[5];
        for (int i = 0; i < 5; ++i) {
            double a = Math.toRadians(-90 + 72 * i);
            cosA[i] = Math.cos(a);
            sinA[i] = Math.sin(a);
        }
        int[] outerX = new int[5];
        int[] outerY = new int[5];
        for (int i = 0; i < 5; ++i) {
            outerX[i] = cx + (int)Math.round(cosA[i] * radius);
            outerY[i] = cy + (int)Math.round(sinA[i] * radius);
        }
        for (int i = 0; i < 5; ++i) {
            this.drawPixelLine(gfx, outerX[i], outerY[i], outerX[(i + 1) % 5], outerY[(i + 1) % 5], -4478832);
        }
        for (int i = 0; i < 5; ++i) {
            this.drawPixelLine(gfx, cx, cy, outerX[i], outerY[i], -3161944);
        }
        int halfR = radius / 2;
        int[] halfX = new int[5];
        int[] halfY = new int[5];
        for (int i = 0; i < 5; ++i) {
            halfX[i] = cx + (int)Math.round(cosA[i] * halfR);
            halfY[i] = cy + (int)Math.round(sinA[i] * halfR);
        }
        for (int i = 0; i < 5; ++i) {
            this.drawPixelLine(gfx, halfX[i], halfY[i], halfX[(i + 1) % 5], halfY[(i + 1) % 5], 1723574416);
        }
        int[] curX = new int[5];
        int[] curY = new int[5];
        for (int i = 0; i < 5; ++i) {
            double r = (double)vals[i] / (double)maxVal * radius;
            curX[i] = cx + (int)Math.round(cosA[i] * r);
            curY[i] = cy + (int)Math.round(sinA[i] * r);
        }
        for (int i = 0; i < 5; ++i) {
            int j = (i + 1) % 5;
            fillTriangle(gfx, cx, cy, curX[i], curY[i], curX[j], curY[j], -1998557120);
        }
        for (int i = 0; i < 5; ++i) {
            this.drawPixelLine(gfx, curX[i], curY[i], curX[(i + 1) % 5], curY[(i + 1) % 5], -2076624);
        }
        for (int i = 0; i < 5; ++i) {
            gfx.fill(curX[i] - 1, curY[i] - 1, curX[i] + 2, curY[i] + 2, -5758960);
        }
        int labelOffset = 5;
        boolean canSpend = data.getUnallocatedZhenyuan() > 0;
        long now = System.currentTimeMillis();
        for (int i = 0; i < 5; ++i) {
            this.zhenyuanPlusRects[i][0] = 0; this.zhenyuanPlusRects[i][1] = 0;
            this.zhenyuanPlusRects[i][2] = 0; this.zhenyuanPlusRects[i][3] = 0;
            this.zhenyuanLabelRects[i][0] = 0; this.zhenyuanLabelRects[i][1] = 0;
            this.zhenyuanLabelRects[i][2] = 0; this.zhenyuanLabelRects[i][3] = 0;
        }
        for (int i = 0; i < 5; ++i) {
            MutableComponent label = Component.translatable(labelKeys[i]);
            MutableComponent labelWithValue = Component.translatable("zhenyuan.friday_cultivation.attr.label_with_value", label, vals[i]);
            int lx = cx + (int)Math.round(cosA[i] * (radius + labelOffset));
            int ly = cy + (int)Math.round(sinA[i] * (radius + labelOffset));
            int boxX;
            int boxY = switch (i) {
                case 0 -> { boxX = lx - 18; yield ly - 6; }
                case 1 -> { boxX = lx + 2; yield ly - 8; }
                case 2 -> { boxX = lx + 2; yield ly - 4; }
                case 3 -> { boxX = lx - 30; yield ly - 4; }
                default -> { boxX = lx - 30; yield ly - 8; }
            };
            int btnX = boxX;
            int btnY = boxY;
            int textX = btnX + 6 + 1;
            this.drawTinyAt(gfx, labelWithValue, textX, boxY, -15067628);
            boolean btnHover = canSpend && mouseX >= btnX && mouseX < btnX + 6 && mouseY >= btnY && mouseY < btnY + 6;
            boolean btnFlash = now < this.zhenyuanPlusFlashUntil[i];
            int btnBg, btnFg;
            if (!canSpend) { btnBg = -7829368; btnFg = -3355444; }
            else if (btnFlash) { btnBg = -4388; btnFg = -4703686; }
            else if (btnHover) { btnBg = -2074022; btnFg = -1; }
            else { btnBg = -4703686; btnFg = -4388; }
            if (btnHover && !btnFlash) {
                gfx.fill(btnX - 1, btnY - 1, btnX + 6 + 1, btnY + 6 + 1, -4388);
            }
            gfx.fill(btnX, btnY, btnX + 6, btnY + 6, btnBg);
            gfx.fill(btnX + 1, btnY + 2, btnX + 6 - 1, btnY + 4, btnFg);
            gfx.fill(btnX + 2, btnY + 1, btnX + 4, btnY + 6 - 1, btnFg);
            if (canSpend) {
                this.zhenyuanPlusRects[i][0] = btnX;
                this.zhenyuanPlusRects[i][1] = btnY;
                this.zhenyuanPlusRects[i][2] = btnX + 6;
                this.zhenyuanPlusRects[i][3] = btnY + 6;
            }
            this.zhenyuanLabelRects[i][0] = textX - 1;
            this.zhenyuanLabelRects[i][1] = boxY - 1;
            this.zhenyuanLabelRects[i][2] = textX + 60;
            this.zhenyuanLabelRects[i][3] = boxY + 7 + 1;
        }
    }

    private static void fillTriangle(GuiGraphics gfx, int x1, int y1, int x2, int y2, int x3, int y3, int color) {
        int minX = Math.min(x1, Math.min(x2, x3));
        int maxX = Math.max(x1, Math.max(x2, x3));
        int minY = Math.min(y1, Math.min(y2, y3));
        int maxY = Math.max(y1, Math.max(y2, y3));
        for (int px = minX; px <= maxX; ++px) {
            for (int py = minY; py <= maxY; ++py) {
                int d1 = edgeSign(px, py, x1, y1, x2, y2);
                int d2 = edgeSign(px, py, x2, y2, x3, y3);
                int d3 = edgeSign(px, py, x3, y3, x1, y1);
                boolean hasNeg = d1 < 0 || d2 < 0 || d3 < 0;
                boolean hasPos = d1 > 0 || d2 > 0 || d3 > 0;
                if (hasNeg && hasPos) continue;
                gfx.fill(px, py, px + 1, py + 1, color);
            }
        }
    }

    private static int edgeSign(int px, int py, int x1, int y1, int x2, int y2) {
        return (px - x2) * (y1 - y2) - (x1 - x2) * (py - y2);
    }

    private void drawPixelLine(GuiGraphics gfx, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;
        int cx = x1;
        int cy = y1;
        while (true) {
            gfx.fill(cx, cy, cx + 1, cy + 1, color);
            if (cx == x2 && cy == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; cx += sx; }
            if (e2 >= dx) continue;
            err += dx; cy += sy;
        }
    }

    private void drawTinyAt(GuiGraphics gfx, Component text, int x, int y, int color) {
        float scale = 0.6f;
        gfx.pose().pushPose();
        gfx.pose().translate(x, y, 0.0f);
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.drawString(this.font, text, 0, 0, color, false);
        gfx.pose().popPose();
    }

    private void drawConfigSectionLabel(GuiGraphics gfx, Component label, Component tagText, int x, int y, int rightX, boolean tagActive) {
        gfx.fill(x, y, x + 2, y + 9, -4703686);
        this.drawSmall(gfx, label, x + 5, y + 1, -9807288);
        int textW = (int)((float)this.font.width(label) * effectiveTextScale());
        float tagScale = 0.7f;
        int tagW = (int)((float)this.font.width(tagText) * tagScale);
        int tagPadH = 2;
        int tagPadV = 2;
        int tagBoxW = tagW + tagPadV * 2;
        int tagBoxH = (int)(9.0f * tagScale) + tagPadH * 2;
        int tagBoxX = rightX - tagBoxW;
        int tagBoxY = y - 1;
        int dotStart = x + 7 + textW;
        int dotEnd = tagBoxX - 4;
        for (int dx = dotStart; dx < dotEnd - 1; dx += 6) {
            gfx.fill(dx, y + 4, dx + 2, y + 5, -1721148856);
        }
        int bgColor = tagActive ? -4703686 : -8750470;
        gfx.fill(tagBoxX + 1, tagBoxY, tagBoxX + tagBoxW - 1, tagBoxY + tagBoxH, bgColor);
        gfx.fill(tagBoxX, tagBoxY + 1, tagBoxX + tagBoxW, tagBoxY + tagBoxH - 1, bgColor);
        gfx.pose().pushPose();
        gfx.pose().translate(tagBoxX + tagPadV, tagBoxY + tagPadH, 0.0f);
        gfx.pose().scale(tagScale, tagScale, 1.0f);
        gfx.drawString(this.font, tagText, 0, 0, -4388, false);
        gfx.pose().popPose();
    }

    private void renderZhenyuanTooltip(GuiGraphics gfx, int mouseX, int mouseY, CultivationData data, int attrIdx) {
        String[] nameKeys = new String[]{
            "zhenyuan.friday_cultivation.attr.constitution",
            "zhenyuan.friday_cultivation.attr.physique",
            "zhenyuan.friday_cultivation.attr.agility",
            "zhenyuan.friday_cultivation.attr.spell_power",
            "zhenyuan.friday_cultivation.attr.qi_sea"
        };
        int[] points = new int[]{data.getAttrConstitution(), data.getAttrPhysique(), data.getAttrAgility(), data.getAttrSpellPower(), data.getAttrQiSea()};
        MutableComponent perPointDesc;
        MutableComponent currentEffect;
        switch (attrIdx) {
            case 0:
                perPointDesc = Component.translatable("zhenyuan.friday_cultivation.tooltip.per_point.constitution");
                currentEffect = Component.translatable("zhenyuan.friday_cultivation.tooltip.current.constitution", points[0]);
                break;
            case 1:
                perPointDesc = Component.translatable("zhenyuan.friday_cultivation.tooltip.per_point.physique");
                currentEffect = Component.translatable("zhenyuan.friday_cultivation.tooltip.current.physique", points[1], formatZhenyuanPercent(points[1] * 1.0));
                break;
            case 2:
                perPointDesc = Component.translatable("zhenyuan.friday_cultivation.tooltip.per_point.agility");
                currentEffect = Component.translatable("zhenyuan.friday_cultivation.tooltip.current.agility", formatZhenyuanPercent(points[2] * 1.0), formatZhenyuanPercent(points[2] * 0.2));
                break;
            case 3:
                perPointDesc = Component.translatable("zhenyuan.friday_cultivation.tooltip.per_point.spell_power");
                currentEffect = Component.translatable("zhenyuan.friday_cultivation.tooltip.current.spell_power", points[3] * 5);
                break;
            case 4:
                perPointDesc = Component.translatable("zhenyuan.friday_cultivation.tooltip.per_point.qi_sea");
                currentEffect = Component.translatable("zhenyuan.friday_cultivation.tooltip.current.qi_sea", (long)points[4] * 100L, (long)points[4] * 1L);
                break;
            default: return;
        }
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable(nameKeys[attrIdx]).withStyle(ChatFormatting.AQUA));
        lines.add(Component.translatable("zhenyuan.friday_cultivation.tooltip.points", points[attrIdx]).withStyle(ChatFormatting.GRAY));
        lines.add(perPointDesc.withStyle(ChatFormatting.GRAY));
        lines.add(currentEffect.withStyle(ChatFormatting.WHITE));
        if (data.getUnallocatedZhenyuan() > 0) {
            lines.add(Component.translatable("zhenyuan.friday_cultivation.tooltip.click_to_spend").withStyle(ChatFormatting.YELLOW));
        }
        gfx.renderTooltip(this.font, lines, Optional.empty(), mouseX, mouseY);
    }

    private static String formatZhenyuanPercent(double value) {
        if (Math.abs(value - Math.rint(value)) < 1.0E-4) {
            return Integer.toString((int)Math.rint(value));
        }
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private void renderBonusSettingsPopup(GuiGraphics gfx, CultivationData data, int mouseX, int mouseY) {
        // 照搬原模组 renderBonusSettingsPopup：增益分类开关弹窗
        int popupW = 258;
        int rowH = 13;
        int popupH = 28 + CultivationBonusCategory.values().length * rowH + 12;
        int px = Math.max(6, Math.min(this.width - popupW - 6, (this.width - popupW) / 2));
        int py = Math.max(6, Math.min(this.height - popupH - 6, (this.height - popupH) / 2));
        gfx.pose().pushPose();
        gfx.pose().translate(0.0f, 0.0f, 420.0f);
        gfx.fill(0, 0, this.width, this.height, 0x77000000);
        this.drawHardShadow(gfx, px, py, popupW, popupH, 4);
        gfx.fill(px, py, px + popupW, py + popupH, -15067628);
        gfx.fill(px + 2, py + 2, px + popupW - 2, py + popupH - 2, -923956);
        gfx.fill(px + 4, py + 4, px + popupW - 4, py + 5, -3562934);
        gfx.fill(px + 4, py + popupH - 5, px + popupW - 4, py + popupH - 4, -2504802);
        this.bonusSettingsPopupRect = new int[]{px, py, px + popupW, py + popupH};
        MutableComponent title = Component.translatable("screen.friday_cultivation.attr.bonus_settings.title");
        this.drawSmallCentered(gfx, title, px + popupW / 2, py + 9, -7707624);
        int closeX = px + popupW - 17;
        int closeY = py + 7;
        this.bonusSettingsPopupCloseRect = new int[]{closeX, closeY, closeX + 10, closeY + 10};
        boolean closeHover = mouseX >= closeX && mouseX < closeX + 10 && mouseY >= closeY && mouseY < closeY + 10;
        if (closeHover) gfx.fill(closeX - 1, closeY - 1, closeX + 11, closeY + 11, -5720);
        gfx.fill(closeX, closeY, closeX + 10, closeY + 10, -15067628);
        gfx.fill(closeX + 2, closeY + 2, closeX + 4, closeY + 4, -923956);
        gfx.fill(closeX + 6, closeY + 2, closeX + 8, closeY + 4, -923956);
        gfx.fill(closeX + 4, closeY + 4, closeX + 6, closeY + 6, -923956);
        gfx.fill(closeX + 2, closeY + 6, closeX + 4, closeY + 8, -923956);
        gfx.fill(closeX + 6, closeY + 6, closeX + 8, closeY + 8, -923956);
        int rowX = px + 12;
        int rowY = py + 24;
        int rowW = popupW - 24;
        for (CultivationBonusCategory category : CultivationBonusCategory.values()) {
            this.renderBonusToggleRow(gfx, data, category, rowX, rowY, rowW, rowH, mouseX, mouseY);
            rowY += rowH;
        }
        if (this.hoveredBonusCategory != null) {
            boolean enabled = data.isBonusCategoryEnabled(this.hoveredBonusCategory);
            List<Component> lines = new ArrayList<>();
            lines.add(Component.translatable(this.hoveredBonusCategory.labelKey()).withStyle(ChatFormatting.GOLD));
            lines.add(Component.translatable(this.hoveredBonusCategory.descriptionKey()).withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable(enabled ? "screen.friday_cultivation.attr.bonus_settings.enabled" : "screen.friday_cultivation.attr.bonus_settings.disabled").withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED));
            lines.add(Component.translatable("screen.friday_cultivation.attr.bonus_settings.toggle_hint").withStyle(ChatFormatting.DARK_GRAY));
            gfx.renderTooltip(this.font, lines, Optional.empty(), mouseX, mouseY);
        }
        gfx.pose().popPose();
    }

    private void renderBonusToggleRow(GuiGraphics gfx, CultivationData data, CultivationBonusCategory category, int x, int y, int w, int h, int mouseX, int mouseY) {
        boolean enabled = data.isBonusCategoryEnabled(category);
        boolean hover = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        this.bonusToggleRowRects.add(new BonusToggleRowRect(new int[]{x, y, x + w, y + h}, category));
        if (hover) {
            this.hoveredBonusCategory = category;
            gfx.fill(x - 2, y, x + w + 2, y + h, 868852298);
        }
        this.drawBonusCategoryIcon(gfx, x + 2, y + 3, category, enabled);
        MutableComponent label = Component.translatable(category.labelKey());
        MutableComponent value = Component.literal(this.bonusCategoryCurrentValue(data, category));
        int switchX = x + w - 28;
        int valueRight = switchX - 5;
        int valueMaxW = 54;
        int valueColor = this.bonusCategoryValueColor(data, category, enabled);
        this.drawSmallRightFitting(gfx, value, valueRight, y + 3, valueMaxW, valueColor);
        int labelMaxW = Math.max(24, valueRight - valueMaxW - (x + 14) - 4);
        this.drawScaledFitting(gfx, label, x + 14, y + 3, labelMaxW, enabled ? -12766422 : -7702176, effectiveTextScale());
        this.renderSmallSwitch(gfx, switchX, y + 2, enabled, hover);
        this.drawDottedHLine(gfx, x, y + h - 1, x + w, 1437110872);
    }

    private String bonusCategoryCurrentValue(CultivationData data, CultivationBonusCategory category) {
        boolean enabled;
        LocalPlayer player = Minecraft.getInstance().player;
        boolean bl = enabled = data != null && data.isBonusCategoryEnabled(category);
        if (!enabled || player == null) {
            return CultivationScreen.zeroBonusValue(category);
        }
        return switch (category) {
            default -> throw new IncompatibleClassChangeError();
            case BODY_DEFENSE -> CultivationScreen.formatSignedInt(BodyDefenseHelper.playerRawBodyDefense(player));
            case MOVEMENT_SPEED -> CultivationScreen.formatSignedPercent((TechniqueBonusHelper.moveSpeedBonus(player) + ZhenyuanBonusHelper.agilityMoveSpeedMult(player)) * 100.0);
            case JUMP_HEIGHT -> CultivationScreen.formatSignedPercent(ZhenyuanBonusHelper.agilityJumpHeightMult(player) * 100.0);
            case MELEE_DAMAGE -> CultivationScreen.formatSignedInt(CultivationScreen.meleeDamageBonusValue(data, player));
            case MINING_SPEED -> CultivationScreen.formatSignedPercent(ZhenyuanBonusHelper.physiqueMiningSpeedPct(player));
            case SPELL_DAMAGE -> CultivationScreen.spellDamageBonusRange(player);
            case MAX_QI -> CultivationScreen.formatSignedCompact(CultivationScreen.maxQiBonusValue(data));
            case QI_RECOVERY -> CultivationScreen.formatQiRecoveryBonusPerSecond(CultivationScreen.qiRecoveryBonusValue(player, data));
            case CULTIVATION_EFFICIENCY -> CultivationScreen.formatCultivationEfficiencyBonus(CultivationScreen.cultivationEfficiencyBonusValue(player, data));
        };
    }

    private int bonusCategoryValueColor(CultivationData data, CultivationBonusCategory category, boolean enabled) {
        if (!enabled) {
            return -7702176;
        }
        String value = this.bonusCategoryCurrentValue(data, category);
        if (CultivationScreen.isZeroBonusValue(value)) {
            return -9807288;
        }
        return value.stripLeading().startsWith("-") ? -5222320 : -12950192;
    }

    private static boolean isZeroBonusValue(String value) {
        if (value == null || value.isBlank()) {
            return true;
        }
        String stripped = value.replace(" ", "").replace("/\u79d2", "").replace("/\u9897", "").replace("/\u9846", "").replace("/sec", "").replace("/particle", "").replace("%", "");
        return stripped.equals("0") || stripped.equals("+0") || stripped.equals("0~0");
    }

    private static String zeroBonusValue(CultivationBonusCategory category) {
        return switch (category) {
            case MOVEMENT_SPEED, JUMP_HEIGHT, MINING_SPEED, SPELL_DAMAGE -> "0%";
            case QI_RECOVERY -> CultivationScreen.formatQiRecoveryBonusPerSecond(0);
            case CULTIVATION_EFFICIENCY -> CultivationScreen.formatCultivationEfficiencyBonus(0);
            default -> "0";
        };
    }

    private static int meleeDamageBonusValue(CultivationData data, LocalPlayer player) {
        if (data == null || player == null) {
            return 0;
        }
        return data.getAttack() + TechniqueBonusHelper.attackBonus(player) + ZhenyuanBonusHelper.physiqueAttackBonus(player) + FoundationDaoBonusHelper.meleeDamageBonus(player) + GoldenCoreDaoBonusHelper.meleeDamageBonus(player) + LooseImmortalBonusHelper.meleeDamageBonus(player);
    }

    private static String spellDamageBonusRange(LocalPlayer player) {
        int min = 0;
        int max = 0;
        boolean found = false;
        for (Spell spell : Spell.values()) {
            if (spell == null || spell.damage() <= 0) continue;
            int value = SpellScalingHelper.powerBonusPercent(player, spell);
            if (!found) {
                min = value;
                max = value;
                found = true;
                continue;
            }
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        if (!found || min == 0 && max == 0) {
            return "0%";
        }
        if (min == max) {
            return CultivationScreen.formatSignedPercent(min);
        }
        return CultivationScreen.formatSignedPercent(min) + "~" + CultivationScreen.formatSignedPercent(max);
    }

    private static long maxQiBonusValue(CultivationData data) {
        if (data == null) {
            return 0L;
        }
        long base = data.getRealm().maxQi(data.getSubStage());
        if (data.getSpiritRoot() == SpiritRoot.HEAVENLY_HIDDEN) {
            base = Math.max(1L, Math.round((double) base * 1.5));
        }
        return data.getMaxQi() - base;
    }

    private static int qiRecoveryBonusValue(LocalPlayer player, CultivationData data) {
        if (player == null || data == null) {
            return 0;
        }
        return PlayerQiConsumer.nominalQiRecoveryPerSecond(player, data, QiElement.PURE);
    }

    private static int cultivationEfficiencyBonusValue(LocalPlayer player, CultivationData data) {
        if (player == null || data == null) {
            return 0;
        }
        int total = PlayerQiConsumer.cultivationEfficiencyPerParticle(player, data, QiElement.PURE);
        int base = (int) Math.round(PlayerQiAbsorptionHelper.baseAbsorbMultiplier(data));
        return Math.max(0, total - base);
    }

    private static String formatSignedInt(int value) {
        return value > 0 ? "+" + value : Integer.toString(value);
    }

    private static String formatSignedCompact(long value) {
        if (value == 0L) {
            return "0";
        }
        long abs = Math.abs(value);
        String formatted = CompactNumberFormat.format(abs);
        return value > 0L ? "+" + formatted : "-" + formatted;
    }

    private static String formatSignedPercent(double value) {
        if (Math.abs(value) < 0.05) {
            return "0%";
        }
        String amount = CultivationScreen.formatZhenyuanPercent(Math.abs(value));
        return (value > 0.0 ? "+" : "-") + amount + "%";
    }

    private static String formatQiRecoveryBonusPerSecond(int amount) {
        return Component.translatable("screen.friday_cultivation.attr.qi_recovery_value", CultivationScreen.formatSignedInt(Math.max(0, amount))).getString();
    }

    private static String formatCultivationEfficiencyBonus(int amount) {
        return Component.translatable("screen.friday_cultivation.attr.cultivation_efficiency_value", CultivationScreen.formatSignedInt(Math.max(0, amount))).getString();
    }

    private void drawBonusCategoryIcon(GuiGraphics gfx, int x, int y, CultivationBonusCategory category, boolean enabled) {
        int color = enabled ? this.bonusCategoryColor(category) : -7702176;
        gfx.fill(x, y, x + 7, y + 7, -15067628);
        gfx.fill(x + 1, y + 1, x + 6, y + 6, color);
        switch (category) {
            case BODY_DEFENSE: {
                gfx.fill(x + 3, y + 2, x + 4, y + 6, -726312);
                break;
            }
            case MOVEMENT_SPEED: {
                gfx.fill(x + 2, y + 2, x + 6, y + 3, -726312);
                gfx.fill(x + 1, y + 4, x + 5, y + 5, -726312);
                break;
            }
            case JUMP_HEIGHT: {
                gfx.fill(x + 3, y + 1, x + 4, y + 6, -726312);
                gfx.fill(x + 2, y + 2, x + 5, y + 3, -726312);
                break;
            }
            case MELEE_DAMAGE: {
                gfx.fill(x + 2, y + 2, x + 6, y + 6, -726312);
                break;
            }
            case MINING_SPEED: {
                gfx.fill(x + 1, y + 5, x + 6, y + 6, -726312);
                break;
            }
            case SPELL_DAMAGE: {
                gfx.fill(x + 2, y + 2, x + 5, y + 5, -726312);
                break;
            }
            case MAX_QI: {
                gfx.fill(x + 2, y + 1, x + 5, y + 6, -726312);
                break;
            }
            case QI_RECOVERY: {
                gfx.fill(x + 1, y + 3, x + 6, y + 4, -726312);
                gfx.fill(x + 3, y + 1, x + 4, y + 6, -726312);
                break;
            }
            case CULTIVATION_EFFICIENCY: {
                gfx.fill(x + 2, y + 2, x + 5, y + 3, -726312);
                gfx.fill(x + 3, y + 3, x + 4, y + 6, -726312);
            }
        }
    }

    private int bonusCategoryColor(CultivationBonusCategory category) {
        return switch (category) {
            default -> throw new IncompatibleClassChangeError();
            case BODY_DEFENSE -> -3630291;
            case MOVEMENT_SPEED -> -11555224;
            case JUMP_HEIGHT -> -9783082;
            case MELEE_DAMAGE -> -4703686;
            case MINING_SPEED -> -6653371;
            case SPELL_DAMAGE -> -8497720;
            case MAX_QI -> -13660970;
            case QI_RECOVERY -> -11157593;
            case CULTIVATION_EFFICIENCY -> -3562934;
        };
    }

    private boolean isInsideRect(int[] rect, double mouseX, double mouseY) {
        return rect != null && mouseX >= (double) rect[0] && mouseX < (double) rect[2] && mouseY >= (double) rect[1] && mouseY < (double) rect[3];
    }

    private void renderElementPercents(GuiGraphics gfx, int x, int rightX, int y, CultivationData data) {
        com.friday.cultivation.spirit.QiElement dominant = data.getDominantElement();
        MutableComponent header = Component.translatable("screen.friday_cultivation.attr.element_section", Component.translatable("element.friday_cultivation." + dominant.id()));
        this.drawSectionLabel(gfx, header, x, y += 4, rightX);
        y += 11;
        for (com.friday.cultivation.spirit.QiElement el : com.friday.cultivation.spirit.QiElement.values()) {
            long count = data.getElementCount(el);
            double pct = data.getElementPercent(el);
            int bonus = data.getElementDamageBonus(el);
            MutableComponent name = Component.translatable("element.friday_cultivation." + el.id());
            String countStr = CompactNumberFormat.format(count);
            MutableComponent mainPart = Component.translatable("screen.friday_cultivation.attr.element_row_main", name, countStr);
            MutableComponent suffixPart = Component.translatable("screen.friday_cultivation.attr.element_row_suffix", String.format("%.1f", pct), bonus);
            int mainColor = el == dominant ? -3562934 : -12766422;
            int suffixColor = -7702176;
            this.drawAttrSmall(gfx, mainPart, x, y, mainColor);
            int mainW = (int) ((float) this.font.width(mainPart) * this.attrTabScale());
            this.drawAttrTinyInline(gfx, suffixPart, x + mainW + 4, y + 1, suffixColor);
            y += 9;
        }
    }

    private void drawAttrTinyInline(GuiGraphics gfx, Component text, int x, int y, int color) {
        float scale = 0.63f;
        gfx.pose().pushPose();
        gfx.pose().translate((float) x, (float) y, 0.0f);
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.drawString(this.font, text, 0, 0, color, false);
        gfx.pose().popPose();
    }

    private void drawTinyInline(GuiGraphics gfx, Component text, int x, int y, int color) {
        float scale = 0.7f;
        gfx.pose().pushPose();
        gfx.pose().translate((float) x, (float) y, 0.0f);
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.drawString(this.font, text, 0, 0, color, false);
        gfx.pose().popPose();
    }

    private void drawTechniqueCell(GuiGraphics gfx, int x, int y, Technique t, boolean equippedSlot) {
        if (t == null) {
            this.drawEmptyCell(gfx, x, y, false, 18);
            return;
        }
        int border = equippedSlot ? -4703686 : -15067628;
        gfx.fill(x, y, x + 18, y + 18, border);
        gfx.fill(x + 1, y + 1, x + 18 - 1, y + 18 - 1, -992352);
        gfx.fill(x + 1, y + 1, x + 18 - 1, y + 2, -4680376);
        int iconX = x + 1;
        int iconY = y + 1;
        RenderSystem.enableBlend();
        gfx.blit(t.iconTexture(), iconX, iconY, 16, 16, 0.0f, 0.0f, 32, 32, 32, 32);
        RenderSystem.disableBlend();
    }

    private void renderSmallSwitch(GuiGraphics gfx, int x, int y, boolean enabled, boolean hover) {
        int w = 22;
        int h = 10;
        if (hover) gfx.fill(x - 1, y - 1, x + w + 1, y + h + 1, -5720);
        gfx.fill(x, y, x + w, y + h, -15067628);
        gfx.fill(x + 1, y + 1, x + w - 1, y + h - 1, enabled ? -4703686 : -9804193);
        int knobX = enabled ? x + w - 9 : x + 2;
        gfx.fill(knobX, y + 2, knobX + 7, y + h - 2, -726312);
    }

    private void drawSmallRightFitting(GuiGraphics gfx, Component text, int rightX, int y, int maxWidth, int color) {
        float scale = effectiveTextScale();
        int rawWidth = this.font.width(text);
        if (rawWidth > 0 && (float)rawWidth * scale > (float)maxWidth) {
            scale = Math.max(0.42f, (float)maxWidth / (float)rawWidth);
        }
        int drawW = (int)((float)rawWidth * scale);
        this.drawScaled(gfx, text, rightX - drawW, y, color, scale);
    }

    private void renderTechniquesTab(GuiGraphics gfx, int splitX, int y, CultivationData data, int mouseX, int mouseY) {
        // 照搬原模组 renderTechniquesTab：功法装备格 + 已学网格 + 筛选器
        int x = splitX + 12;
        int rightX = splitX + 160 - 12;
        this.techniqueLearnedCellRects.clear();
        this.techniqueEquippedRect = null;
        this.drawSectionLabel(gfx, Component.translatable("screen.friday_cultivation.tech.equipped_label"), x, y, rightX);
        Technique equipped = Technique.byId(data.getEquippedTechniqueId());
        int blockW = rightX - x;
        int blockH = 36;
        y += 11;
        this.drawEquippedTechniqueBlock(gfx, x, y, blockW, blockH, equipped);
        this.techniqueEquippedRect = new int[]{x, y};
        if (equipped != null && mouseX >= x && mouseX < x + blockW && mouseY >= y && mouseY < y + blockH) {
            this.hoveredTechniqueId = equipped.id();
        }
        y += blockH + 3;
        y += 4;
        this.drawSectionLabel(gfx, Component.translatable("screen.friday_cultivation.tech.learned_label"), x, y, rightX);
        y += 11;
        y += this.renderTechFilterRow(gfx, x, rightX, y, mouseX, mouseY) + 2;
        int cols = 7;
        int rows = 3;
        List<String> tids = this.filteredLearnedTechniques(data);
        int totalRows = (tids.size() + cols - 1) / cols;
        this.techMaxScroll = Math.max(0, totalRows - rows);
        if (this.techScrollOffset > this.techMaxScroll) {
            this.techScrollOffset = this.techMaxScroll;
        }
        if (this.techScrollOffset < 0) {
            this.techScrollOffset = 0;
        }
        int gridLeft = x;
        int gridTop = y;
        int gridW = cols * 18 + (cols - 1) * 1;
        int gridH = rows * 18 + (rows - 1) * 1;
        for (int row = 0; row < rows; ++row) {
            for (int col = 0; col < cols; ++col) {
                int actualIdx = (this.techScrollOffset + row) * cols + col;
                int cx = gridLeft + col * 19;
                int cy = gridTop + row * 19;
                String tid = actualIdx < tids.size() ? tids.get(actualIdx) : null;
                Technique t = tid == null ? null : Technique.byId(tid);
                boolean isEquipped = t != null && t.id().equals(data.getEquippedTechniqueId());
                this.drawTechniqueLearnedCell(gfx, cx, cy, t, isEquipped);
                if (t == null) continue;
                this.techniqueLearnedCellRects.add(new int[]{cx, cy, actualIdx});
                if (mouseX < cx || mouseX >= cx + 18 || mouseY < cy || mouseY >= cy + 18) continue;
                this.hoveredTechniqueId = t.id();
            }
        }
        this.mouseOverTechGrid = mouseX >= gridLeft && mouseX < gridLeft + gridW + 8 && mouseY >= gridTop && mouseY < gridTop + gridH;
        if (this.techMaxScroll > 0) {
            this.drawScrollbar(gfx, gridLeft + gridW + 2, gridTop, gridTop + gridH, rows, totalRows, this.techScrollOffset);
        }
        int curStart = this.techScrollOffset * cols + 1;
        int curEnd = Math.min(tids.size(), (this.techScrollOffset + rows) * cols);
        Component pageLabel = tids.isEmpty()
                ? Component.translatable("screen.friday_cultivation.tech.none")
                : Component.translatable("screen.friday_cultivation.tech.range", curStart, curEnd, tids.size());
        y += gridH + 4;
        this.drawSmallCentered(gfx, pageLabel, splitX + 80, y, -9807288);
    }

    private void drawEquippedTechniqueBlock(GuiGraphics gfx, int x, int y, int w, int h, Technique t) {
        gfx.fill(x, y, x + w, y + h, 533307978);
        gfx.fill(x, y, x + w, y + 2, -3562934);
        gfx.fill(x, y + h - 2, x + w, y + h, -3562934);
        gfx.fill(x, y, x + 2, y + h, -3562934);
        gfx.fill(x + w - 2, y, x + w, y + h, -3562934);
        gfx.fill(x + 2, y + 2, x + w - 2, y + 3, 0x4DFFFFFF);
        gfx.fill(x + 2, y + 2, x + 3, y + h - 2, 0x4DFFFFFF);
        if (t == null) {
            this.drawSmall(gfx, Component.translatable("screen.friday_cultivation.tech.none_equipped_warn"), x + 6, y + 4, -7723482);
            this.drawSmall(gfx, Component.translatable("screen.friday_cultivation.tech.none_equipped"), x + 6, y + 14, -9807288);
            return;
        }
        int iconSize = 16;
        int iconX = x + 4;
        int iconY = y + 4;
        gfx.fill(iconX - 1, iconY - 1, iconX + iconSize + 1, iconY + iconSize + 1, -15067628);
        RenderSystem.enableBlend();
        gfx.blit(t.iconTexture(), iconX, iconY, 16, 16, 0.0f, 0.0f, 32, 32, 32, 32);
        RenderSystem.disableBlend();
        int textX = iconX + iconSize + 4;
        this.drawSmall(gfx, t.displayName(), textX, y + 4, -4703686);
        this.drawSmall(gfx, Component.translatable("screen.friday_cultivation.tech.click_to_unequip"), textX, y + 14, -9807288);
        MutableComponent equippedLabel = Component.translatable("screen.friday_cultivation.tech.equipped_label_short");
        int labelW = (int)((float)this.font.width(equippedLabel) * effectiveTextScale()) + 4;
        int labelX = x + w - labelW - 4;
        int labelY = y - 3;
        gfx.fill(labelX - 1, labelY - 1, labelX + labelW + 1, labelY + 8, -15067628);
        gfx.fill(labelX, labelY, labelX + labelW, labelY + 7, -3562934);
        this.drawSmall(gfx, equippedLabel, labelX + 2, labelY, -15068144);
        this.drawTierElementBadges(gfx, iconX, y + h - 11, t.tier(), com.friday.cultivation.QiElement.PURE);
    }

    private void drawTierElementBadges(GuiGraphics gfx, int x, int y, Technique.Tier tier, QiElement element) {
        Component tierText = Component.translatable("tier.friday_cultivation." + tier.name().toLowerCase());
        int tierW = (int)((float)this.font.width(tierText) * effectiveTextScale()) + 4;
        gfx.fill(x, y, x + tierW, y + 9, -1290136560);
        this.drawSmall(gfx, tierText, x + 2, y + 1, tierRgb(tier));
        int elX = x + tierW + 3;
        MutableComponent elText = Component.translatable("element.friday_cultivation." + element.name().toLowerCase());
        int elW = (int)((float)this.font.width(elText) * effectiveTextScale()) + 4;
        gfx.fill(elX, y, elX + elW, y + 9, -1290136560);
        this.drawSmall(gfx, elText, elX + 2, y + 1, 0xFF000000 | (element.rgb() & 0xFFFFFF));
    }

    private static int tierRgb(Technique.Tier tier) {
        return switch (tier) {
            case LOW      -> -1;
            case MID      -> -8128;
            case HIGH     -> -12525344;
            case SUPREME  -> -2064129;
            case IMMORTAL -> -2068440;
        };
    }

    private void drawTechniqueLearnedCell(GuiGraphics gfx, int x, int y, Technique t, boolean equipped) {
        if (t == null) {
            this.drawEmptyCell(gfx, x, y, true, 18);
            return;
        }
        int border = equipped ? -4703686 : -15067628;
        gfx.fill(x, y, x + 18, y + 18, border);
        gfx.fill(x + 1, y + 1, x + 18 - 1, y + 18 - 1, -992352);
        gfx.fill(x + 1, y + 1, x + 18 - 1, y + 2, -4680376);
        int iconX = x + 1;
        int iconY = y + 1;
        RenderSystem.enableBlend();
        gfx.blit(t.iconTexture(), iconX, iconY, 16, 16, 0.0f, 0.0f, 32, 32, 32, 32);
        RenderSystem.disableBlend();
        if (equipped) {
            int tx = x + 18 - 6;
            int ty = y + 1;
            gfx.fill(tx, ty, tx + 5, ty + 5, -4703686);
            gfx.fill(tx + 1, ty + 2, tx + 2, ty + 3, -1);
            gfx.fill(tx + 2, ty + 3, tx + 3, ty + 4, -1);
            gfx.fill(tx + 3, ty + 1, tx + 4, ty + 4, -1);
        }
    }

    private List<String> filteredLearnedTechniques(CultivationData data) {
        List<String> result = new ArrayList<>();
        for (String tid : data.getLearnedTechniques()) {
            Technique t = Technique.byId(tid);
            if (t == null) continue;
            if (!this.techElementFilter.matchesTechnique(t)) continue;
            if (!this.techTierFilter.matchesTier(t.tier())) continue;
            result.add(tid);
        }
        return result;
    }

    private int renderTechFilterRow(GuiGraphics gfx, int leftX, int rightX, int y, int mouseX, int mouseY) {
        this.renderRightDropdowns(gfx, rightX, y, this.techElementFilter, this.techTierFilter, 3, 4, mouseX, mouseY);
        return 11;
    }

    private void renderSpellsTab(GuiGraphics gfx, int splitX, int y, CultivationData data, int mouseX, int mouseY) {
        // 照搬原模组 renderSpellsTab：法术轮盘8槽位 + 已学网格 + 筛选器 + 拖拽
        this.learnedCellRects.clear();
        this.wheelCellRects.clear();
        int gridW = 56;
        int gridLeft = splitX + (160 - gridW) / 2;
        int gridTop = y;
        List<String> equipped = data.getEquippedSpells();
        for (int i = 0; i < 8; ++i) {
            int cx = gridLeft + OCT_X[i] * 19;
            int cy = gridTop + OCT_Y[i] * 19;
            String sid = (i < equipped.size()) ? equipped.get(i) : null;
            Spell sp2 = sid == null || sid.isEmpty() ? null : Spell.byId(sid);
            boolean enabled = sp2 != null && data.isSpellEnabled(sp2);
            boolean selected = sid != null && sid.equals(this.selectedSpellId);
            boolean primed = i == data.getSelectedSpellSlot();
            this.drawSpellCell(gfx, cx, cy, sp2, enabled, selected, primed);
            this.wheelCellRects.add(new int[]{cx, cy, i});
            if (sp2 == null || mouseX < cx || mouseX >= cx + 18 || mouseY < cy || mouseY >= cy + 18 || this.isDragging) continue;
            this.hoveredSpellId = sid;
        }
        int centerCx = gridLeft + 19;
        int centerCy = gridTop + 19;
        this.drawTaiji(gfx, centerCx, centerCy);
        int learnedY = gridTop + 57 + 4;
        this.drawSectionLabel(gfx, Component.translatable("screen.friday_cultivation.spell.learned_label"), splitX + 12, learnedY - 4, splitX + 160 - 12);
        learnedY += 7;
        learnedY += this.renderSpellFilterRow(gfx, splitX + 12, splitX + 160 - 12, learnedY, mouseX, mouseY) + 1;
        List<String> sids = this.filteredLearnedSpells(data);
        int cols = 7;
        int rows = 3;
        int totalRows = (sids.size() + cols - 1) / cols;
        this.spellMaxScroll = Math.max(0, totalRows - rows);
        if (this.spellScrollOffset > this.spellMaxScroll) {
            this.spellScrollOffset = this.spellMaxScroll;
        }
        if (this.spellScrollOffset < 0) {
            this.spellScrollOffset = 0;
        }
        int learnedLeft = splitX + 12;
        int learnedGridW = cols * 18 + (cols - 1) * 1;
        int learnedGridH = rows * 18 + (rows - 1) * 1;
        for (int row = 0; row < rows; ++row) {
            for (int col = 0; col < cols; ++col) {
                int actualIdx = (this.spellScrollOffset + row) * cols + col;
                int cx = learnedLeft + col * 19;
                int cy = learnedY + row * 19;
                String sid = actualIdx < sids.size() ? sids.get(actualIdx) : null;
                Spell sp3 = sid == null ? null : Spell.byId(sid);
                boolean enabled = sp3 != null && data.isSpellEnabled(sp3);
                boolean equipFlag = sp3 != null && data.isSpellEquipped(sid);
                boolean selected = sid != null && sid.equals(this.selectedSpellId);
                this.drawLearnedCell(gfx, cx, cy, sp3, enabled, selected, equipFlag);
                if (sp3 == null) continue;
                this.learnedCellRects.add(new int[]{cx, cy, actualIdx});
                if (mouseX < cx || mouseX >= cx + 18 || mouseY < cy || mouseY >= cy + 18 || this.isDragging) continue;
                this.hoveredSpellId = sid;
            }
        }
        this.mouseOverSpellGrid = mouseX >= learnedLeft && mouseX < learnedLeft + learnedGridW + 8 && mouseY >= learnedY && mouseY < learnedY + learnedGridH;
        if (this.spellMaxScroll > 0) {
            this.drawScrollbar(gfx, learnedLeft + learnedGridW + 2, learnedY, learnedY + learnedGridH, rows, totalRows, this.spellScrollOffset);
        }
        if (this.isDragging && this.draggingSpellId != null) {
            Spell sp = Spell.byId(this.draggingSpellId);
            if (sp != null) {
                RenderSystem.enableBlend();
                this.blitSpellIcon(gfx, sp, mouseX - 7, mouseY - 7, 14);
                RenderSystem.disableBlend();
            }
        }
    }

    private void drawTaiji(GuiGraphics gfx, int x, int y) {
        gfx.fill(x, y, x + 18, y + 18, -3562934);
        gfx.fill(x + 1, y + 1, x + 18 - 1, y + 18 - 1, -726312);
        int s = 16;
        RenderSystem.enableBlend();
        gfx.blit(TAIJI_TEXTURE, x + 1, y + 1, s, s, 0.0f, 0.0f, 32, 32, 32, 32);
        RenderSystem.disableBlend();
    }

    private void drawEmptyCell(GuiGraphics gfx, int x, int y, boolean dim) {
        this.drawEmptyCell(gfx, x, y, dim, 18);
    }

    private void drawEmptyCell(GuiGraphics gfx, int x, int y, boolean dim, int cellSize) {
        int border = -15067628;
        int bg = dim ? -4677516 : -7439524;
        gfx.fill(x, y, x + cellSize, y + cellSize, border);
        gfx.fill(x + 1, y + 1, x + cellSize - 1, y + cellSize - 1, bg);
        gfx.fill(x + 1, y + 1, x + cellSize - 1, y + 2, -9544640);
    }

    private void drawSpellCell(GuiGraphics gfx, int x, int y, Spell sp, boolean enabled, boolean selected, boolean primed) {
        if (sp == null) {
            int border = primed ? -4703686 : -8758764;
            gfx.fill(x, y, x + 18, y + 18, border);
            gfx.fill(x + 1, y + 1, x + 18 - 1, y + 18 - 1, -4677516);
            return;
        }
        int border = primed ? -4703686 : (selected ? -10496 : -15067628);
        gfx.fill(x, y, x + 18, y + 18, border);
        int innerBg = enabled ? -992352 : -7439524;
        gfx.fill(x + 1, y + 1, x + 18 - 1, y + 18 - 1, innerBg);
        gfx.fill(x + 1, y + 1, x + 18 - 1, y + 2, -4680376);
        int iconX = x + 2;
        int iconY = y + 2;
        RenderSystem.enableBlend();
        boolean disabledPassive = isDisabledPassive(sp, enabled);
        this.blitSpellIcon(gfx, sp, iconX, iconY, 14, disabledPassive);
        if (!enabled && !disabledPassive) {
            gfx.fill(iconX, iconY, iconX + 14, iconY + 14, Integer.MIN_VALUE);
        }
        RenderSystem.disableBlend();
    }

    private void drawLearnedCell(GuiGraphics gfx, int x, int y, Spell sp, boolean enabled, boolean selected, boolean equipped) {
        if (sp == null) {
            this.drawEmptyCell(gfx, x, y, true);
            return;
        }
        int border = equipped ? -4703686 : (selected ? -10496 : -15067628);
        gfx.fill(x, y, x + 18, y + 18, border);
        int innerBg = enabled ? -992352 : -7439524;
        gfx.fill(x + 1, y + 1, x + 18 - 1, y + 18 - 1, innerBg);
        gfx.fill(x + 1, y + 1, x + 18 - 1, y + 2, -4680376);
        int iconX = x + 2;
        int iconY = y + 2;
        RenderSystem.enableBlend();
        boolean disabledPassive = isDisabledPassive(sp, enabled);
        this.blitSpellIcon(gfx, sp, iconX, iconY, 14, disabledPassive);
        if (!enabled && !disabledPassive) {
            gfx.fill(iconX, iconY, iconX + 14, iconY + 14, Integer.MIN_VALUE);
        }
        RenderSystem.disableBlend();
        if (equipped) {
            int tx = x + 18 - 6;
            int ty = y + 1;
            gfx.fill(tx, ty, tx + 5, ty + 5, -4703686);
            gfx.fill(tx + 1, ty + 2, tx + 2, ty + 3, -1);
            gfx.fill(tx + 2, ty + 3, tx + 3, ty + 4, -1);
            gfx.fill(tx + 3, ty + 1, tx + 4, ty + 4, -1);
        }
    }

    private static boolean isDisabledPassive(Spell sp, boolean enabled) {
        return !enabled && sp.type() == SpellType.PASSIVE;
    }

    private void blitSpellIcon(GuiGraphics gfx, Spell sp, int x, int y, int size) {
        this.blitSpellIcon(gfx, sp, x, y, size, false);
    }

    private void blitSpellIcon(GuiGraphics gfx, Spell sp, int x, int y, int size, boolean disabledPassive) {
        SpellIconRenderHelper.blitSpellIcon(gfx, sp, x, y, size, disabledPassive);
    }

    private List<String> filteredLearnedSpells(CultivationData data) {
        List<String> result = new ArrayList<>();
        for (String sid : data.getLearnedSpells()) {
            Spell sp = Spell.byId(sid);
            if (sp == null) continue;
            if (this.spellTypeFilter == SpellTypeFilter.PASSIVE && sp.type() != SpellType.PASSIVE) continue;
            if (this.spellTypeFilter == SpellTypeFilter.ACTIVE && sp.type() != SpellType.ACTIVE) continue;
            if (!this.spellElementFilter.matchesSpell(sp)) continue;
            if (!this.spellTierFilter.matchesTier(sp.tier())) continue;
            result.add(sid);
        }
        return result;
    }

    private int renderSpellFilterRow(GuiGraphics gfx, int leftX, int rightX, int y, int mouseX, int mouseY) {
        int chipGap = 2;
        int textW = 0;
        for (SpellTypeFilter f : SpellTypeFilter.values()) {
            textW = Math.max(textW, this.chipTextWidth(f.display()));
        }
        int toggleW = Math.max(18, textW + 6);
        int x = leftX;
        for (SpellTypeFilter f : SpellTypeFilter.values()) {
            boolean active = f == this.spellTypeFilter;
            this.drawFilterChip(gfx, x, y, toggleW, 11, f.display(), active, mouseX, mouseY);
            this.filterButtonRects.add(new int[]{x, y, x + toggleW, y + 11, 0, f.ordinal()});
            x += toggleW + chipGap;
        }
        this.renderRightDropdowns(gfx, rightX, y, this.spellElementFilter, this.spellTierFilter, 1, 2, mouseX, mouseY);
        return 11;
    }

    private void drawFilterChip(GuiGraphics gfx, int x, int y, int w, int h, Component label, boolean active, int mouseX, int mouseY) {
        boolean hover = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        int bg = active ? -4703686 : (hover ? -11385542 : -12635095);
        gfx.fill(x, y, x + w, y + h, -15067628);
        gfx.fill(x + 1, y + 1, x + w - 1, y + h - 1, bg);
        int color = active ? -5720 : -726312;
        int tw = this.chipTextWidth(label);
        this.drawScaled(gfx, label, x + (w - tw) / 2, y + (h - 6) / 2, color, 0.65f);
    }

    private void drawDropdownChip(GuiGraphics gfx, int x, int y, int w, int h, Component currentValue, boolean open, int mouseX, int mouseY) {
        boolean hover = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        int bg = open ? -4703686 : (hover ? -11385542 : -12635095);
        gfx.fill(x, y, x + w, y + h, -15067628);
        gfx.fill(x + 1, y + 1, x + w - 1, y + h - 1, bg);
        String arrow = " \u25be";
        int vw = this.chipTextWidth(currentValue);
        int aw = (int)Math.ceil((float)this.font.width(arrow) * 0.65f);
        int totalW = vw + aw;
        int textX = x + (w - totalW) / 2;
        int textY = y + (h - 6) / 2;
        int valueColor = open ? -5720 : -726312;
        this.drawScaled(gfx, currentValue, textX, textY, valueColor, 0.65f);
        this.drawScaled(gfx, Component.literal(arrow), textX + vw, textY, valueColor, 0.65f);
    }

    private void renderRightDropdowns(GuiGraphics gfx, int rightX, int y, ElementFilter currentElem, TierFilter currentTier, int elemKind, int tierKind, int mouseX, int mouseY) {
        Component elemValue = this.elementDropdownDisplay(currentElem);
        Component tierValue = this.tierDropdownDisplay(currentTier);
        int elemChipW = this.dropdownChipWidth(elemValue);
        int tierChipW = this.dropdownChipWidth(tierValue);
        int gapBetween = 2;
        int totalW = elemChipW + gapBetween + tierChipW;
        int x = rightX - totalW;
        this.drawDropdownChip(gfx, x, y, elemChipW, 11, elemValue, this.openDropdown == elemKind, mouseX, mouseY);
        this.filterButtonRects.add(new int[]{x, y, x + elemChipW, y + 11, elemKind, 0});
        x += elemChipW + gapBetween;
        this.drawDropdownChip(gfx, x, y, tierChipW, 11, tierValue, this.openDropdown == tierKind, mouseX, mouseY);
        this.filterButtonRects.add(new int[]{x, y, x + tierChipW, y + 11, tierKind, 0});
    }

    private Component elementDropdownDisplay(ElementFilter value) {
        return value == ElementFilter.ALL ? Component.translatable("screen.friday_cultivation.filter.element.label") : value.display();
    }

    private Component tierDropdownDisplay(TierFilter value) {
        return value == TierFilter.ALL ? Component.translatable("screen.friday_cultivation.filter.tier.label") : value.display();
    }

    private int dropdownChipWidth(Component value) {
        int aw = this.chipTextWidth(Component.literal(" \u25be"));
        return this.chipTextWidth(value) + aw + 4;
    }

    private int chipTextWidth(String text) {
        return (int)Math.ceil((float)this.font.width(text) * 0.65f);
    }

    private int chipTextWidth(Component c) {
        return (int)Math.ceil((float)this.font.width(c) * 0.65f);
    }

    private void drawScaledFitting(GuiGraphics gfx, Component text, int x, int y, int maxWidth, int color, float scale) {
        float fittingScale = scale;
        int rawWidth = this.font.width(text);
        if (rawWidth > 0 && (float)rawWidth * fittingScale > (float)maxWidth) {
            fittingScale = Math.max(0.42f, (float)maxWidth / (float)rawWidth);
        }
        this.drawScaled(gfx, text, x, y, color, fittingScale);
    }

    private void renderBreakthroughTab(GuiGraphics gfx, int x, int y, LocalPlayer player, CultivationData data, int mouseX, int mouseY) {
        int rightX = x + 160 - 24;
        int width = rightX - x;
        int cx = x + width / 2;
        Realm realm = data.getRealm();
        SubStage sub = data.getSubStage();
        MutableComponent realmValue = data.isLooseImmortal()
                ? Component.translatable("realm.friday_cultivation.loose_immortal.level." + data.getLooseImmortalTribulations())
                : (realm == Realm.MORTAL || realm == Realm.TRUE_IMMORTAL
                    ? realm.displayName().copy()
                    : Component.translatable("screen.friday_cultivation.attr.id.realm_combined", realm.displayName(), sub.displayName()));
        this.drawSmallCentered(gfx, realmValue, cx, y, VERMILLION);
        y += 12;
        if (data.isLooseImmortal()) {
            this.renderLooseImmortalBreakthroughTab(gfx, x, rightX, y, player, data);
            return;
        }
        long curCult = data.getCultivationProgress();
        long maxCult = data.getMaxCultivation();
        this.drawThinBar(gfx, x + 4, y, width - 8,
                maxCult == 0L ? 0.0f : (float) curCult / (float) maxCult,
                Component.translatable("screen.friday_cultivation.cult_short").getString(),
                curCult + " / " + maxCult, -928374, -3631046);
        y += 13;
        int boneAge = LifespanHelper.displayBoneAge(data);
        boolean hasBloodTalisman = this.hasBloodTransformationTalisman(player);
        if (this.canChooseFoundationRoute(data)) {
            y = this.renderFoundationBreakthroughOptions(gfx, x, rightX, y, data, boneAge, mouseX, mouseY);
        } else if (realm == Realm.QI_REFINING) {
            y += this.drawBreakthroughParagraphCentered(gfx, Component.translatable("screen.friday_cultivation.breakthrough.route_locked_stage"), cx, y, width - 8, TEXT_GREY) + 4;
        } else if (realm == Realm.FOUNDATION_BUILDING) {
            MutableComponent current = Component.translatable("screen.friday_cultivation.breakthrough.current_foundation",
                    Component.translatable(data.getFoundationDao().translationKey()));
            this.drawBreakthroughCentered(gfx, current, cx, y, width - 8, GREEN_INK, false);
            y += 13;
            if (data.canChooseGoldenCoreRoute()) {
                y = this.renderGoldenCoreBreakthroughOptions(gfx, x, rightX, y, data, boneAge, hasBloodTalisman, mouseX, mouseY);
            } else {
                y += this.drawBreakthroughParagraphCentered(gfx, Component.translatable("screen.friday_cultivation.breakthrough.route_locked_stage"), cx, y, width - 8, TEXT_GREY) + 4;
            }
        } else if (realm.ordinal() >= Realm.GOLDEN_CORE.ordinal()) {
            y = this.renderBreakthroughHistoryButton(gfx, x, rightX, y, data, mouseX, mouseY);
            y += this.drawBreakthroughParagraphCentered(gfx, Component.translatable("screen.friday_cultivation.breakthrough.route_waiting"), cx, y, width - 8, TEXT_GREY) + 4;
        }
        Component hint = this.breakthroughHint(data, boneAge, hasBloodTalisman);
        this.drawBreakthroughParagraphCentered(gfx, hint, cx, y + 4, width - 8, TEXT_GREY);
    }

    private void renderLooseImmortalBreakthroughTab(GuiGraphics gfx, int x, int rightX, int y, LocalPlayer player, CultivationData data) {
        int width = rightX - x;
        int cx = x + width / 2;
        int level = Math.max(1, LooseImmortalBonusHelper.clampLevel(data.getLooseImmortalTribulations()));
        long now = player == null || player.level() == null ? 0L : player.level().getGameTime();
        long remaining = data.getLooseImmortalTribulationRemainingTicks(now);
        y = this.renderLooseImmortalStatusCard(gfx, x, rightX, y, level, remaining);
        y += this.drawBreakthroughParagraphCentered(gfx, Component.translatable("screen.friday_cultivation.breakthrough.loose_immortal.desc"), cx, y, width - 18, TEXT_GREY, 0.62f, 2) + 4;
        if (level < 9) {
            int waves = LooseImmortalBonusHelper.wavesForCurrentLevel(level);
            int bolts = LooseImmortalBonusHelper.boltsPerWaveForCurrentLevel(level);
            int damage = LooseImmortalBonusHelper.strikeDamageForCurrentLevel(level);
            y = this.renderLooseImmortalInfoLine(gfx, x, rightX, y, this.looseImmortalTokenStack(level + 1),
                    Component.translatable("screen.friday_cultivation.breakthrough.loose_immortal.next_tribulation", level + 1, Realm.formatTribulationCount(waves, bolts), damage), VERMILLION_DEEP);
            y = this.renderLooseImmortalInfoLine(gfx, x, rightX, y, this.looseImmortalTokenStack(level + 1),
                    this.looseImmortalBonusText(level + 1), GREEN_INK);
        }
    }

    private int renderLooseImmortalStatusCard(GuiGraphics gfx, int x, int rightX, int y, int level, long remaining) {
        int rowX = x + 6, rowRight = rightX - 6, rowH = 36;
        gfx.fill(rowX, y, rowRight, y + rowH, INK_BLACK);
        gfx.fill(rowX + 1, y + 1, rowRight - 1, y + rowH - 1, BG_PANEL);
        gfx.fill(rowX + 2, y + 2, rowRight - 2, y + 3, GOLD_BORDER);
        gfx.fill(rowX + 2, y + rowH - 3, rowRight - 2, y + rowH - 2, BORDER_DARK);
        int iconX = rowX + 8, iconY = y + 7;
        this.drawLooseImmortalTokenIcon(gfx, this.looseImmortalTokenStack(level), iconX, iconY);
        MutableComponent countdown = level >= 9
                ? Component.translatable("screen.friday_cultivation.breakthrough.loose_immortal.cap")
                : Component.translatable("screen.friday_cultivation.breakthrough.loose_immortal.countdown", this.formatLooseImmortalDuration(remaining));
        int textX = iconX + 28, textRight = rowRight - 8;
        this.drawScaledFitting(gfx, countdown, textX, y + 7, textRight - textX, level >= 9 ? GREEN_INK : GOLD_TEXT_DARK, effectiveTextScale() * 0.72f);
        this.drawLooseImmortalTribulationPips(gfx, textX, y + 23, textRight, level);
        return y + rowH + 6;
    }

    private int renderLooseImmortalInfoLine(GuiGraphics gfx, int x, int rightX, int y, ItemStack icon, Component text, int color) {
        int rowX = x + 6, rowRight = rightX - 6, iconX = rowX, textX = iconX + 20;
        float scale = effectiveTextScale() * 0.64f;
        int rawTextW = Math.max(24, (int)((rowRight - textX) / scale));
        List<FormattedCharSequence> lines = this.font.split(text, rawTextW);
        int lineH = (int)Math.ceil(9.0f * scale) + 1;
        int rowH = Math.max(18, lines.size() * lineH + 3);
        gfx.renderItem(icon, iconX, y + Math.max(0, (rowH - 16) / 2));
        gfx.pose().pushPose();
        gfx.pose().translate(textX, y + 1, 0.0f);
        gfx.pose().scale(scale, scale, 1.0f);
        for (int i = 0; i < lines.size(); ++i) gfx.drawString(this.font, lines.get(i), 0, i * 9, color, false);
        gfx.pose().popPose();
        return y + rowH + 3;
    }

    private void drawLooseImmortalTokenIcon(GuiGraphics gfx, ItemStack icon, int x, int y) {
        gfx.fill(x, y, x + 22, y + 22, INK_BLACK);
        gfx.fill(x + 1, y + 1, x + 21, y + 21, BG_PANEL);
        gfx.fill(x + 2, y + 2, x + 20, y + 3, GOLD_BORDER);
        gfx.renderItem(icon, x + 3, y + 3);
    }

    private void drawLooseImmortalTribulationPips(GuiGraphics gfx, int x, int y, int rightX, int level) {
        int count = 9, pip = 8, gap = 2;
        int total = count * pip + (count - 1) * gap;
        if (x + total > rightX) { pip = 7; gap = 1; total = count * pip + (count - 1) * gap; }
        int startX = x + Math.max(0, (rightX - x - total) / 2);
        for (int i = 1; i <= count; ++i) {
            int px = startX + (i - 1) * (pip + gap);
            boolean reached = i <= level;
            boolean current = i == level;
            gfx.fill(px, y, px + pip, y + pip, current ? VERMILLION_DEEP : BORDER_DARK);
            gfx.fill(px + 1, y + 1, px + pip - 1, y + pip - 1, reached ? GOLD_TEXT_DARK : CELL_BG_EMPTY);
            MutableComponent digit = Component.literal(Integer.toString(i));
            float scale = pip <= 7 ? 0.5f : 0.54f;
            int digitW = (int)(this.font.width(digit) * scale);
            this.drawScaled(gfx, digit, px + (pip - digitW) / 2, y + (pip <= 7 ? 1 : 2), reached ? 0xFFFFFF : TEXT_GREY, scale);
        }
    }

    private ItemStack looseImmortalTokenStack(int level) { return ItemStack.EMPTY; }

    private Component looseImmortalBonusText(int level) {
        int clamped = LooseImmortalBonusHelper.clampLevel(level);
        return Component.translatable("screen.friday_cultivation.breakthrough.loose_immortal.next_bonus",
                clamped, CompactNumberFormat.format(LooseImmortalBonusHelper.maxQiBonusForLevel(clamped)),
                LooseImmortalBonusHelper.bodyDefenseBonusForLevel(clamped),
                LooseImmortalBonusHelper.cultivationEfficiencyBonusForLevel(clamped),
                LooseImmortalBonusHelper.qiRecoveryPerSecondBonusForLevel(clamped),
                LooseImmortalBonusHelper.meleeDamageBonusForLevel(clamped),
                LooseImmortalBonusHelper.spellDamageBonusPercentForLevel(clamped),
                LooseImmortalBonusHelper.spellQiCostReductionPercentForLevel(clamped),
                LooseImmortalBonusHelper.freeZhenyuanRewardBetween(clamped - 1, clamped),
                LooseImmortalBonusHelper.automaticZhenyuanAttributesRewardBetween(clamped - 1, clamped));
    }

    private Component formatLooseImmortalDuration(long ticks) {
        long remainingTicks = Math.max(0L, ticks);
        if (remainingTicks >= 24000L) {
            long years = Math.max(1L, (remainingTicks + 24000L - 1L) / 24000L);
            return Component.translatable("screen.friday_cultivation.duration.years", years);
        }
        long totalSeconds = Math.max(0L, (remainingTicks + 19L) / 20L);
        return Component.translatable("screen.friday_cultivation.duration.minutes_seconds", totalSeconds / 60L, totalSeconds % 60L);
    }

    private int renderFoundationBreakthroughOptions(GuiGraphics gfx, int x, int rightX, int y, CultivationData data, int boneAge, int mouseX, int mouseY) {
        // 用数组索引而非 dao.ordinal()（枚举含 NONE 占位，ordinal 与数组下标不一致）
        for (int i = 0; i < FOUNDATION_BREAKTHROUGH_OPTIONS.length; i++) {
            FoundationDao dao = FOUNDATION_BREAKTHROUGH_OPTIONS[i];
            boolean ready = data.isEligibleFoundationDao(dao, boneAge);
            BreakthroughRouteRow row = this.buildFoundationRequirementRow(dao, data, boneAge);
            y = this.renderBreakthroughOptionRow(gfx, x, rightX, y, row, ready, this.selectedFoundationDao == dao, false, true, this.foundationRouteIcon(dao), 1, i, mouseX, mouseY);
        }
        return y + 2;
    }

    private int renderGoldenCoreBreakthroughOptions(GuiGraphics gfx, int x, int rightX, int y, CultivationData data, int boneAge, boolean hasBloodTalisman, int mouseX, int mouseY) {
        // 用数组索引而非 dao.ordinal()（枚举含 NONE 占位，ordinal 与数组下标不一致）
        for (int i = 0; i < GOLDEN_CORE_BREAKTHROUGH_OPTIONS.length; i++) {
            GoldenCoreDao dao = GOLDEN_CORE_BREAKTHROUGH_OPTIONS[i];
            boolean ready = data.isEligibleGoldenCoreDao(dao, boneAge, hasBloodTalisman);
            boolean selectable = data.isFoundationAllowedForGoldenCore(dao, hasBloodTalisman);
            BreakthroughRouteRow row = this.buildGoldenCoreRequirementRow(dao, data, boneAge);
            y = this.renderBreakthroughOptionRow(gfx, x, rightX, y, row, ready, selectable && this.selectedGoldenCoreDao == dao, !selectable, selectable, this.goldenCoreRouteIcon(dao), 2, i, mouseX, mouseY);
        }
        return y + 2;
    }

    private int renderBreakthroughHistoryButton(GuiGraphics gfx, int x, int rightX, int y, CultivationData data, int mouseX, int mouseY) {
        int rowX = x + 4, rowRight = rightX - 4, rowH = 18;
        boolean hover = mouseX >= rowX && mouseX < rowRight && mouseY >= y && mouseY < y + rowH;
        this.breakthroughHistoryButtonRect = new int[]{rowX, y, rowRight, y + rowH};
        gfx.fill(rowX, y, rowRight, y + rowH, INK_BLACK);
        gfx.fill(rowX + 1, y + 1, rowRight - 1, y + rowH - 1, hover ? BG_PANEL : 0xFFD9C7A6);
        gfx.fill(rowX + 2, y + 2, rowRight - 2, y + 3, hover ? GOLD_BRIGHT : BORDER_LIGHT);
        this.drawBreakthroughHistoryButtonText(gfx, this.breakthroughHistorySummary(data), (rowX + rowRight) / 2, y + 5, rowRight - rowX - 8, hover ? VERMILLION : GREEN_INK);
        return y + rowH + 5;
    }

    private BreakthroughRouteRow buildFoundationRequirementRow(FoundationDao dao, CultivationData data, int boneAge) {
        List<RequirementPart> parts = new ArrayList<>();
        switch (dao) {
            case HUMAN: parts.add(this.requirementPart("foundation.zhuji_dan_1", data.getZhujiDanEaten() >= 1)); break;
            case BLOOD: parts.add(this.requirementPart("foundation.blood_spirit_pill_3", data.getBloodPillEaten() >= 3)); break;
            case EARTH:
                parts.add(this.requirementPart("foundation.zhuji_dan_6", data.getZhujiDanEaten() >= 6));
                parts.add(this.requirementPart("foundation.foundation_secret", data.isZhujiSecretUsed()));
                break;
            case HEAVEN:
                parts.add(this.requirementPart("foundation.zhuji_dan_9", data.getZhujiDanEaten() >= 9));
                parts.add(this.requirementPart("foundation.dao_foundation_fruit_1", data.getDaoFruitEaten() >= 1));
                parts.add(this.requirementPart("foundation.foundation_secret", data.isZhujiSecretUsed()));
                parts.add(this.requirementPart("foundation.bone_age_under_21", boneAge < 21));
                break;
            default: break;
        }
        return new BreakthroughRouteRow(Component.translatable(dao.translationKey()), parts);
    }

    private BreakthroughRouteRow buildGoldenCoreRequirementRow(GoldenCoreDao dao, CultivationData data, int boneAge) {
        List<RequirementPart> parts = new ArrayList<>();
        switch (dao) {
            case HUMAN: parts.add(this.requirementPart("golden_core.jiedan_pill_1", data.getJiedanPillUsed() >= 1)); break;
            case BLOOD:
                parts.add(this.requirementPart("golden_core.blood_jiedan_pill_3", data.getBloodJiedanPillUsed() >= 3));
                parts.add(this.requirementPart("golden_core.all_creatures_true_blood_1", data.getTrueBloodUsed() >= 1));
                break;
            case EARTH:
                parts.add(this.requirementPart("golden_core.jiedan_pill_6", data.getJiedanPillUsed() >= 6));
                parts.add(this.requirementPart("golden_core.earth_evil_qi_1", data.getEarthEvilQiUsed() >= 1));
                break;
            case HEAVEN:
                parts.add(this.requirementPart("golden_core.jiedan_pill_9", data.getJiedanPillUsed() >= 9));
                parts.add(this.requirementPart("golden_core.heaven_clear_qi_1", data.getHeavenClearQiUsed() >= 1));
                parts.add(this.requirementPart("golden_core.ningzhen_creation_fruit_1", data.getCreationFruitEaten() >= 1));
                parts.add(this.requirementPart("golden_core.bone_age_under_60", boneAge < 60));
                break;
            default: break;
        }
        return new BreakthroughRouteRow(Component.translatable(dao.translationKey()), parts);
    }

    private RequirementPart requirementPart(String suffix, boolean met) {
        MutableComponent part = Component.translatable("screen.friday_cultivation.breakthrough.requirement." + suffix);
        return new RequirementPart(met ? part.withStyle(JADE_REQUIREMENT_STYLE) : part, met);
    }

    private int renderBreakthroughOptionRow(GuiGraphics gfx, int x, int rightX, int y, BreakthroughRouteRow row, boolean ready, boolean selected, boolean strike, boolean selectable, ItemStack icon, int kind, int ordinal, int mouseX, int mouseY) {
        int rowX = x + 3, rowRight = rightX - 3;
        int textMaxW = Math.max(24, rowRight - rowX - 12 - 3 - 4);
        List<BreakthroughVisualLine> lines = this.wrapBreakthroughRow(row, ready, textMaxW);
        int lineH = this.breakthroughRowLineHeight();
        int rowH = Math.max(15, lines.size() * lineH + 4);
        boolean hover = mouseX >= rowX && mouseX < rowRight && mouseY >= y && mouseY < y + rowH;
        if (hover) { this.hoveredBreakthroughOptionKind = kind; this.hoveredBreakthroughOptionOrdinal = ordinal; }
        if (selected || hover && selectable) {
            gfx.fill(rowX, y, rowRight, y + rowH, selected ? 617194058 : 0x18FFFFFF);
            gfx.fill(rowX, y, rowRight, y + 1, selected ? GOLD_BORDER : BORDER_LIGHT);
            gfx.fill(rowX, y + rowH - 1, rowRight, y + rowH, selected ? GOLD_BORDER : BORDER_LIGHT);
        } else if (hover) {
            gfx.fill(rowX, y, rowRight, y + rowH, 0x10FFFFFF);
        }
        int maxLineW = 0;
        for (BreakthroughVisualLine line : lines) maxLineW = Math.max(maxLineW, this.scaledBreakthroughWidth(line.rawWidth()));
        int groupX = (rowX + rowRight - (15 + maxLineW)) / 2;
        int iconY = y + (rowH - 12) / 2;
        this.drawBreakthroughIcon(gfx, icon, groupX, iconY);
        if (!selectable) gfx.fill(groupX, iconY, groupX + 12, iconY + 12, 1894308280);
        int textX = groupX + 12 + 3;
        int textY = y + (rowH - lines.size() * lineH) / 2;
        int color = ready ? BREAKTHROUGH_READY_TEXT : (strike ? -7570576 : TEXT_DARK);
        for (int i = 0; i < lines.size(); ++i) {
            BreakthroughVisualLine line = lines.get(i);
            int lineY = textY + i * lineH;
            this.drawBreakthroughSegments(gfx, line.segments(), textX, lineY, color);
            if (!strike) continue;
            int lineW = this.scaledBreakthroughWidth(line.rawWidth());
            gfx.fill(textX, lineY + this.breakthroughStrikeOffset(), textX + lineW, lineY + this.breakthroughStrikeOffset() + 1, color);
        }
        this.breakthroughOptionRects.add(new int[]{rowX, y, rowRight, y + rowH, kind, ordinal, selectable ? 1 : 0});
        return y + rowH;
    }

    private List<BreakthroughVisualLine> wrapBreakthroughRow(BreakthroughRouteRow row, boolean ready, int maxTextW) {
        int rawMaxW = Math.max(1, (int)(maxTextW / 0.7f));
        MutableComponent separator = Component.translatable("screen.friday_cultivation.breakthrough.requirement.separator");
        int separatorW = this.font.width(separator);
        List<BreakthroughVisualLine> lines = new ArrayList<>();
        List<Component> current = new ArrayList<>();
        int currentW = 0;
        boolean hasRequirementOnLine = false;
        MutableComponent prefix = Component.translatable("screen.friday_cultivation.breakthrough.requirement.row", row.routeName(), Component.empty());
        current.add(prefix);
        currentW += this.font.width(prefix);
        for (RequirementPart requirement : row.requirements()) {
            Component text = requirement.text();
            int textW = this.font.width(text);
            boolean needsSeparator = hasRequirementOnLine;
            int addW = (needsSeparator ? separatorW : 0) + textW;
            if (!current.isEmpty() && currentW + addW > rawMaxW) {
                lines.add(new BreakthroughVisualLine(new ArrayList<>(current), currentW));
                current.clear(); currentW = 0; hasRequirementOnLine = false; needsSeparator = false;
            }
            if (needsSeparator) { current.add(separator); currentW += separatorW; }
            current.add(text); currentW += textW; hasRequirementOnLine = true;
        }
        if (ready) {
            MutableComponent marker = Component.empty().append(Component.literal(" ")).append(Component.translatable("screen.friday_cultivation.breakthrough.met"));
            int markerW = this.font.width(marker);
            if (!current.isEmpty() && currentW + markerW > rawMaxW) {
                lines.add(new BreakthroughVisualLine(new ArrayList<>(current), currentW));
                current.clear(); currentW = 0;
            }
            current.add(marker); currentW += markerW;
        }
        if (!current.isEmpty()) lines.add(new BreakthroughVisualLine(new ArrayList<>(current), currentW));
        return lines.isEmpty() ? List.of(new BreakthroughVisualLine(List.of(row.routeName()), this.font.width(row.routeName()))) : lines;
    }

    private int breakthroughRowLineHeight() { return (int)Math.ceil(9.0f * 0.7f) + 1; }
    private int breakthroughStrikeOffset() { return Math.max(3, Math.round((9 - 1) * 0.7f * 0.5f)); }
    private int scaledBreakthroughWidth(int rawWidth) { return (int)Math.ceil(rawWidth * 0.7f); }

    private void drawBreakthroughSegments(GuiGraphics gfx, List<Component> segments, int x, int y, int color) {
        gfx.pose().pushPose();
        gfx.pose().translate(x, y, 0.0f);
        gfx.pose().scale(0.7f, 0.7f, 1.0f);
        int cursor = 0;
        for (Component segment : segments) { gfx.drawString(this.font, segment, cursor, 0, color, false); cursor += this.font.width(segment); }
        gfx.pose().popPose();
    }

    private void drawBreakthroughIcon(GuiGraphics gfx, ItemStack icon, int x, int y) {
        gfx.pose().pushPose();
        gfx.pose().translate(x, y, 120.0f);
        gfx.pose().scale(0.75f, 0.75f, 1.0f);
        gfx.renderItem(icon, 0, 0);
        gfx.pose().popPose();
    }

    private ItemStack foundationRouteIcon(FoundationDao dao) { return ItemStack.EMPTY; }
    private ItemStack goldenCoreRouteIcon(GoldenCoreDao dao) { return ItemStack.EMPTY; }

    private Component breakthroughHistorySummary(CultivationData data) {
        List<Component> names = this.breakthroughHistoryRouteNames(data);
        Component value = names.isEmpty() ? Component.translatable("screen.friday_cultivation.breakthrough.history_missing") : this.joinBreakthroughHistoryNames(names);
        return Component.translatable("screen.friday_cultivation.breakthrough.history_button", value);
    }

    private List<Component> breakthroughHistoryRouteNames(CultivationData data) {
        List<Component> names = new ArrayList<>();
        FoundationDao fdao = data.getFoundationDao();
        if (fdao != null && fdao != FoundationDao.NONE) names.add(Component.translatable(fdao.translationKey()));
        GoldenCoreDao gdao = data.getGoldenCoreDao();
        if (gdao != null && gdao != GoldenCoreDao.NONE) names.add(Component.translatable(gdao.translationKey()));
        return names;
    }

    private Component joinBreakthroughHistoryNames(List<Component> names) {
        MutableComponent result = Component.empty();
        MutableComponent separator = Component.translatable("screen.friday_cultivation.breakthrough.requirement.separator");
        for (int i = 0; i < names.size(); ++i) { if (i > 0) result.append(separator); result.append(names.get(i)); }
        return result;
    }

    private void renderBreakthroughHistoryPopup(GuiGraphics gfx, CultivationData data, int mouseX, int mouseY) {
        List<Component> routeNames = this.breakthroughHistoryRouteNames(data);
        int popupW = Math.max(180, Math.min(292, this.width - 44));
        int contentW = popupW - 28;
        float scale = effectiveTextScale() * 0.82f;
        List<HistoryPopupLine> lines = new ArrayList<>();
        if (routeNames.isEmpty()) {
            lines.add(new HistoryPopupLine(Component.translatable("screen.friday_cultivation.breakthrough.history_missing"), TEXT_GREY, scale));
        } else {
            FoundationDao fdao = data.getFoundationDao();
            if (fdao != null && fdao != FoundationDao.NONE) {
                lines.add(new HistoryPopupLine(Component.translatable("screen.friday_cultivation.breakthrough.history_foundation", Component.translatable(fdao.translationKey())), GREEN_INK, scale));
                lines.add(new HistoryPopupLine(Component.translatable("tooltip.friday_cultivation.foundation." + fdao.id()), TEXT_GREY, scale));
            }
            GoldenCoreDao gdao = data.getGoldenCoreDao();
            if (gdao != null && gdao != GoldenCoreDao.NONE) {
                lines.add(new HistoryPopupLine(Component.translatable("screen.friday_cultivation.breakthrough.history_golden_core", Component.translatable(gdao.translationKey())), GREEN_INK, scale));
                lines.add(new HistoryPopupLine(Component.translatable(gdao.tooltipKey()), TEXT_GREY, scale));
            }
        }
        int bodyH = 0;
        for (HistoryPopupLine line : lines) bodyH += this.wrappedHistoryLineHeight(line.text(), contentW, line.scale()) + 4;
        int popupH = Math.min(this.height - 24, Math.max(82, bodyH + 36));
        int popupX = (this.width - popupW) / 2;
        int popupY = (this.height - popupH) / 2;
        this.breakthroughHistoryPopupRect = new int[]{popupX, popupY, popupX + popupW, popupY + popupH};
        this.breakthroughHistoryPopupCloseRect = new int[]{popupX + popupW - 18, popupY + 6, popupX + popupW - 7, popupY + 17};
        gfx.pose().pushPose();
        gfx.pose().translate(0.0f, 0.0f, 360.0f);
        gfx.fill(0, 0, this.width, this.height, 0x7A000000);
        this.drawHardShadow(gfx, popupX, popupY, popupW, popupH, 4);
        gfx.fill(popupX - 2, popupY - 2, popupX + popupW + 2, popupY + popupH + 2, BORDER_DARK);
        gfx.fill(popupX, popupY, popupX + popupW, popupY + popupH, BG_PAGE);
        this.drawPanelFrame(gfx, popupX, popupY, popupW, popupH);
        this.drawSmallCentered(gfx, Component.translatable("screen.friday_cultivation.breakthrough.history_title"), popupX + popupW / 2, popupY + 10, VERMILLION);
        this.drawPopupCloseIcon(gfx, this.breakthroughHistoryPopupCloseRect, mouseX, mouseY);
        int lineY = popupY + 28;
        int bottom = popupY + popupH - 10;
        for (HistoryPopupLine line : lines) {
            if (lineY >= bottom) break;
            lineY += this.drawHistoryWrappedLine(gfx, line.text(), popupX + 14, lineY, contentW, line.color(), line.scale(), bottom) + 4;
        }
        gfx.pose().popPose();
    }

    private int wrappedHistoryLineHeight(Component text, int maxW, float scale) {
        List<FormattedCharSequence> wrapped = this.font.split(text, Math.max(1, (int)(maxW / scale)));
        int lineH = (int)Math.ceil(9.0f * scale) + 2;
        return Math.max(lineH, wrapped.size() * lineH);
    }

    private int drawHistoryWrappedLine(GuiGraphics gfx, Component text, int x, int y, int maxW, int color, float scale, int bottom) {
        List<FormattedCharSequence> wrapped = this.font.split(text, Math.max(1, (int)(maxW / scale)));
        int lineH = (int)Math.ceil(9.0f * scale) + 2;
        int drawn = 0;
        for (FormattedCharSequence line : wrapped) {
            if (y + drawn + lineH > bottom) break;
            gfx.pose().pushPose();
            gfx.pose().translate(x, y + drawn, 0.0f);
            gfx.pose().scale(scale, scale, 1.0f);
            gfx.drawString(this.font, line, 0, 0, color, false);
            gfx.pose().popPose();
            drawn += lineH;
        }
        return Math.max(lineH, drawn);
    }

    private void drawPopupCloseIcon(GuiGraphics gfx, int[] rect, int mouseX, int mouseY) {
        boolean hover = mouseX >= rect[0] && mouseX < rect[2] && mouseY >= rect[1] && mouseY < rect[3];
        int color = hover ? VERMILLION : INK_BLACK;
        if (hover) gfx.fill(rect[0] - 1, rect[1] - 1, rect[2] + 1, rect[3] + 1, 548944442);
        int x = rect[0] + 2, y = rect[1] + 2;
        int size = Math.min(rect[2] - rect[0], rect[3] - rect[1]) - 4;
        for (int i = 0; i < size; ++i) {
            gfx.fill(x + i, y + i, x + i + 2, y + i + 2, color);
            gfx.fill(x + size - 1 - i, y + i, x + size + 1 - i, y + i + 2, color);
        }
    }

    private Component breakthroughHint(CultivationData data, int boneAge, boolean hasBloodTalisman) {
        Realm realm = data.getRealm();
        SubStage sub = data.getSubStage();
        if (realm == Realm.MORTAL) return Component.translatable("screen.friday_cultivation.breakthrough.hint_mortal");
        if (realm == Realm.QI_REFINING && sub == SubStage.PEAK) {
            int waves = this.foundationTribulationWaves(this.selectedFoundationDao);
            int damage = waves > 0 ? Realm.QI_REFINING.tribulationStrikeDamage() : 0;
            return Component.translatable("screen.friday_cultivation.breakthrough.route_hint_foundation",
                    Component.translatable(this.selectedFoundationDao.translationKey()), Realm.formatTribulationCount(waves, 1), damage);
        }
        if (realm == Realm.FOUNDATION_BUILDING && sub == SubStage.PEAK) {
            return Component.translatable("screen.friday_cultivation.breakthrough.route_hint_golden_core",
                    Component.translatable(this.selectedGoldenCoreDao.translationKey()),
                    Realm.formatTribulationCount(this.selectedGoldenCoreDao.tribulationStrikes(), 1),
                    this.selectedGoldenCoreDao.tribulationDamage());
        }
        int strikes = realm.tribulationCount(sub);
        int boltsPerWave = realm.tribulationBoltsPerWave(sub);
        int damage = realm.tribulationStrikeDamage();
        return Component.translatable("screen.friday_cultivation.breakthrough.hint_tribulation",
                Realm.formatTribulationCount(strikes, boltsPerWave), damage);
    }

    private int foundationTribulationWaves(FoundationDao dao) { return dao == FoundationDao.NONE ? 0 : 1; }

    private void drawBreakthroughCentered(GuiGraphics gfx, Component text, int cx, int y, int maxW, int color, boolean strike) {
        float scale = effectiveTextScale() * 0.8f;
        int rawW = this.font.width(text);
        if (rawW * scale > maxW && rawW > 0) scale = Math.max(0.5f, (float)maxW / rawW);
        int drawW = (int)(rawW * scale);
        int drawX = cx - drawW / 2;
        gfx.pose().pushPose();
        gfx.pose().translate(drawX, y, 0.0f);
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.drawString(this.font, text, 0, 0, color, false);
        gfx.pose().popPose();
        if (strike) { int lineY = y + Math.max(4, (int)(9.0f * scale / 2.0f)); gfx.fill(drawX, lineY, drawX + drawW, lineY + 1, color); }
    }

    private void drawBreakthroughHistoryButtonText(GuiGraphics gfx, Component text, int cx, int y, int maxW, int color) {
        float scale = effectiveTextScale() * 0.78f;
        int rawW = this.font.width(text);
        if (rawW * scale > maxW && rawW > 0) scale = Math.max(0.38f, (float)maxW / rawW);
        int drawW = (int)(rawW * scale);
        gfx.pose().pushPose();
        gfx.pose().translate(cx - drawW / 2, y, 0.0f);
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.drawString(this.font, text, 0, 0, color, false);
        gfx.pose().popPose();
    }

    private void renderBreakthroughTooltip(GuiGraphics gfx, int mouseX, int mouseY) {
        if (this.hoveredBreakthroughOptionKind == 0 || this.hoveredBreakthroughOptionOrdinal < 0) return;
        // 灵魂状态下境界数据异常，跳过突破tooltip防止数组越界
        CultivationData data = currentClientCultivationData();
        if (data == null || data.isSoulState()) return;
        LocalPlayer player = Minecraft.getInstance().player;
        int boneAge = LifespanHelper.displayBoneAge(data);
        boolean hasBloodTalisman = this.hasBloodTransformationTalisman(player);
        BreakthroughRouteRow row;
        boolean ready;
        boolean selectable;
        if (this.hoveredBreakthroughOptionKind == 1) {
            if (this.hoveredBreakthroughOptionOrdinal >= FOUNDATION_BREAKTHROUGH_OPTIONS.length) return;
            FoundationDao dao = FOUNDATION_BREAKTHROUGH_OPTIONS[this.hoveredBreakthroughOptionOrdinal];
            row = this.buildFoundationRequirementRow(dao, data, boneAge);
            ready = data.isEligibleFoundationDao(dao, boneAge);
            selectable = true;
        } else if (this.hoveredBreakthroughOptionKind == 2) {
            if (this.hoveredBreakthroughOptionOrdinal >= GOLDEN_CORE_BREAKTHROUGH_OPTIONS.length) return;
            GoldenCoreDao dao = GOLDEN_CORE_BREAKTHROUGH_OPTIONS[this.hoveredBreakthroughOptionOrdinal];
            row = this.buildGoldenCoreRequirementRow(dao, data, boneAge);
            ready = data.isEligibleGoldenCoreDao(dao, boneAge, hasBloodTalisman);
            selectable = data.isFoundationAllowedForGoldenCore(dao, hasBloodTalisman);
        } else return;
        List<Component> lines = new ArrayList<>();
        lines.add(row.routeName().copy().withStyle(ChatFormatting.GOLD));
        for (RequirementPart part : row.requirements()) lines.add(part.text());
        if (ready) lines.add(Component.translatable("screen.friday_cultivation.breakthrough.met").withStyle(ChatFormatting.GREEN));
        if (!selectable) lines.add(Component.translatable("screen.friday_cultivation.breakthrough.not_selectable").withStyle(ChatFormatting.RED));
        gfx.renderTooltip(this.font, lines, Optional.empty(), mouseX, mouseY);
    }

    private void renderOpenDropdown(GuiGraphics gfx, int mouseX, int mouseY) {
        // 照搬原模组 renderOpenDropdown：下拉筛选器弹窗
        if (this.openDropdown == 0 || this.openDropdownAnchor == null) return;
        this.dropdownOptionRects.clear();
        List<Component> options;
        int selectedIdx;
        switch (this.openDropdown) {
            case 1:
                options = new ArrayList<>();
                for (ElementFilter f : ElementFilter.values()) options.add(f.display());
                selectedIdx = this.spellElementFilter.ordinal();
                break;
            case 2:
                options = new ArrayList<>();
                for (TierFilter f : TierFilter.values()) options.add(f.display());
                selectedIdx = this.spellTierFilter.ordinal();
                break;
            case 3:
                options = new ArrayList<>();
                for (ElementFilter f : ElementFilter.values()) options.add(f.display());
                selectedIdx = this.techElementFilter.ordinal();
                break;
            case 4:
                options = new ArrayList<>();
                for (TierFilter f : TierFilter.values()) options.add(f.display());
                selectedIdx = this.techTierFilter.ordinal();
                break;
            default: return;
        }
        int anchorX1 = this.openDropdownAnchor[0];
        int anchorY1 = this.openDropdownAnchor[1];
        int anchorX2 = this.openDropdownAnchor[2];
        int anchorY2 = this.openDropdownAnchor[3];
        int maxW = 0;
        for (Component opt : options) {
            maxW = Math.max(maxW, this.chipTextWidth(opt));
        }
        int popupW = Math.max(anchorX2 - anchorX1, maxW + 10);
        int optionH = 10;
        int popupH = options.size() * optionH + 2;
        int popupX = anchorX1;
        int popupY = anchorY2 + 1;
        if (popupY + popupH > this.height - 4) {
            popupY = anchorY1 - 1 - popupH;
        }
        gfx.pose().pushPose();
        gfx.pose().translate(0.0f, 0.0f, 300.0f);
        gfx.fill(popupX - 1, popupY - 1, popupX + popupW + 1, popupY + popupH + 1, -15067628);
        gfx.fill(popupX, popupY, popupX + popupW, popupY + popupH, -14739438);
        int oy = popupY + 1;
        for (int i = 0; i < options.size(); ++i) {
            Component opt = options.get(i);
            boolean hover = mouseX >= popupX && mouseX < popupX + popupW && mouseY >= oy && mouseY < oy + optionH;
            boolean selected = i == selectedIdx;
            int bg = hover ? -4703686 : (selected ? -11385542 : -14739438);
            gfx.fill(popupX, oy, popupX + popupW, oy + optionH, bg);
            int textColor = hover || selected ? -5720 : -726312;
            int tw = this.chipTextWidth(opt);
            this.drawScaled(gfx, opt, popupX + (popupW - tw) / 2, oy + (optionH - 6) / 2, textColor, 0.65f);
            this.dropdownOptionRects.add(new int[]{popupX, oy, popupX + popupW, oy + optionH, i});
            oy += optionH;
        }
        gfx.pose().popPose();
    }

    // ═══════════════════════════════════════════
    // 辅助绘制方法
    // ═══════════════════════════════════════════

    private void drawTinyMultilineCentered(GuiGraphics gfx, Component text, int cx, int yTop, int maxW, int color) {
        float scale = 0.7f;
        int rawMaxW = (int)(maxW / scale);
        List<FormattedCharSequence> lines = this.font.split(text, rawMaxW);
        int lineH = (int)(9.0f * scale) + 1;
        for (int i = 0; i < lines.size(); ++i) {
            FormattedCharSequence line = lines.get(i);
            int lineW = (int)(this.font.width(line) * scale);
            int lineX = cx - lineW / 2;
            int lineY = yTop + i * lineH;
            gfx.pose().pushPose();
            gfx.pose().translate(lineX, lineY, 0.0f);
            gfx.pose().scale(scale, scale, 1.0f);
            gfx.drawString(this.font, line, 0, 0, color, false);
            gfx.pose().popPose();
        }
    }

    private int drawBreakthroughParagraphCentered(GuiGraphics gfx, Component text, int cx, int yTop, int maxW, int color) {
        return this.drawBreakthroughParagraphCentered(gfx, text, cx, yTop, maxW, color, 0.7f, 3);
    }

    private int drawBreakthroughParagraphCentered(GuiGraphics gfx, Component text, int cx, int yTop, int maxW, int color, float scale, int lineGap) {
        int rawMaxW = Math.max(1, (int)(maxW / scale));
        List<FormattedCharSequence> lines = this.font.split(text, rawMaxW);
        int lineH = (int)Math.ceil(9.0f * scale) + lineGap;
        for (int i = 0; i < lines.size(); ++i) {
            FormattedCharSequence line = lines.get(i);
            int lineW = (int)(this.font.width(line) * scale);
            int lineX = cx - lineW / 2;
            int lineY = yTop + i * lineH;
            gfx.pose().pushPose();
            gfx.pose().translate(lineX, lineY, 0.0f);
            gfx.pose().scale(scale, scale, 1.0f);
            gfx.drawString(this.font, line, 0, 0, color, false);
            gfx.pose().popPose();
        }
        return lines.size() * lineH;
    }

    private void drawIdRow(GuiGraphics gfx, int xLeft, int xRight, int y, Component label, Component value, int valueColor) {
        this.drawSmall(gfx, label, xLeft, y + 1, TEXT_GREY);
        int valueW = (int)(this.font.width(value) * effectiveTextScale());
        this.drawSmall(gfx, value, xRight - valueW, y + 1, valueColor);
    }

    private void drawInfoCell(GuiGraphics gfx, int cellX, int cellRight, int y, Component label, Component value, int valueColor) {
        float s = effectiveTextScale() * 0.78f;
        this.drawCellAt(gfx, label, cellX, y + 1, TEXT_GREY, s);
        float labW = this.font.width(label) * s;
        float avail = (cellRight - cellX) - labW - 3.0f;
        float valW = this.font.width(value) * s;
        float vs = valW > avail && valW > 0.0f ? s * (avail / valW) : s;
        int vsw = (int)(this.font.width(value) * vs);
        this.drawCellAt(gfx, value, cellRight - vsw, y + 1, valueColor, vs);
    }

    private void drawSectEntryArrow(GuiGraphics gfx, int[] rect, boolean hovered) {
        if (rect == null || rect.length < 4) return;
        if (hovered) {
            gfx.fill(rect[0] - 1, rect[1], rect[2], rect[3], 414726714);
        }
        int color = hovered ? VERMILLION_DEEP : GREEN_INK;
        int cx = (rect[0] + rect[2]) / 2;
        int y = rect[1] + 2;
        gfx.fill(cx, y, cx + 1, y + 1, color);
        gfx.fill(cx - 1, y + 1, cx, y + 2, color);
        gfx.fill(cx + 1, y + 1, cx + 2, y + 2, color);
        gfx.fill(cx - 2, y + 2, cx - 1, y + 3, color);
        gfx.fill(cx + 2, y + 2, cx + 3, y + 3, color);
        gfx.fill(cx - 3, y + 3, cx - 2, y + 4, color);
        gfx.fill(cx + 3, y + 3, cx + 4, y + 4, color);
        gfx.fill(cx, y + 4, cx + 1, rect[3] - 2, color);
    }

    private void drawCellAt(GuiGraphics gfx, Component text, int x, int y, int color, float scale) {
        gfx.pose().pushPose();
        gfx.pose().translate(x, y, 0.0f);
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.drawString(this.font, text, 0, 0, color, false);
        gfx.pose().popPose();
    }

    private void drawSmall(GuiGraphics gfx, Component text, int x, int y, int color) {
        float s = effectiveTextScale();
        gfx.pose().pushPose();
        gfx.pose().translate(x, y, 0.0f);
        gfx.pose().scale(s, s, 1.0f);
        gfx.drawString(this.font, text, 0, 0, color, false);
        gfx.pose().popPose();
    }

    private void drawSmallCentered(GuiGraphics gfx, Component text, int cx, int y, int color) {
        float s = effectiveTextScale();
        int w = (int)(this.font.width(text) * s);
        this.drawSmall(gfx, text, cx - w / 2, y, color);
    }

    private void drawScaled(GuiGraphics gfx, Component text, int x, int y, int color, float scale) {
        gfx.pose().pushPose();
        gfx.pose().translate(x, y, 0.0f);
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.drawString(this.font, text, 0, 0, color, false);
        gfx.pose().popPose();
    }

    private void drawThinBar(GuiGraphics gfx, int x, int y, int w, float pct, String label, String valueText, int barColor, int valueColor) {
        gfx.fill(x, y, x + w, y + 4, CELL_BG_DARK);
        gfx.fill(x, y, x + (int)(w * Math.max(0.0f, Math.min(1.0f, pct))), y + 4, barColor);
        this.drawSmall(gfx, Component.literal(label), x, y + 5, TEXT_GREY);
        int valueW = (int)(this.font.width(valueText) * effectiveTextScale());
        this.drawSmall(gfx, Component.literal(valueText), x + w - valueW, y + 5, valueColor);
    }

    private void drawGradientBar(GuiGraphics gfx, int x, int y, int w, int h, float pct, int colorLow, int colorHigh) {
        gfx.fill(x, y, x + w, y + h, CELL_BG_DARK);
        int fillW = (int)(w * Math.max(0.0f, Math.min(1.0f, pct)));
        gfx.fill(x, y, x + fillW, y + h, colorHigh);
    }

    private void drawScrollbar(GuiGraphics gfx, int x, int y, int h, int offset, int max, int total) {
        if (max <= 0) return;
        gfx.fill(x, y, x + 2, y + h, CELL_BG_DARK);
        int knobH = Math.max(8, h * Math.max(1, total - max) / Math.max(1, total));
        int knobY = y + (h - knobH) * offset / Math.max(1, max);
        gfx.fill(x, knobY, x + 2, knobY + knobH, BORDER_LIGHT);
    }

    // ═══════════════════════════════════════════
    // 工具方法
    // ═══════════════════════════════════════════

    private static boolean inRect(int[] r, double mx, double my) {
        return r != null && mx >= r[0] && mx < r[2] && my >= r[1] && my < r[3];
    }

    private void playUiClick() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }

    private void startNameEdit() {
        LocalPlayer lp = Minecraft.getInstance().player;
        CultivationData ic = lp == null ? null : CultivationCapability.get(lp).orElse(null);
        String cur = ic != null ? ic.getCustomName() : "";
        Minecraft.getInstance().setScreen(new EditNameScreen(this, cur));
    }

    private void submitName() {
        if (!this.editingName) return;
        ModNetwork.CHANNEL.sendToServer(new SetCultivationNamePacket(this.nameEditBox.getValue()));
        this.endNameEdit();
    }

    private void endNameEdit() {
        this.editingName = false;
        this.nameEditBox.setVisible(false);
        this.nameEditBox.setFocused(false);
        if (this.getFocused() == this.nameEditBox) {
            this.setFocused(null);
        }
    }

    private void submitBreakthroughRequest() {
        // 照搬原模组：发送突破请求包，携带选中的筑基/金丹Dao（本项目用AdvanceRealmPacket等价替代）
        LocalPlayer player = Minecraft.getInstance().player;
        CultivationData data = null;
        if (player != null) {
            CultivationData ic = CultivationCapability.get(player).orElse(null);
            if (ic != null) data = ic;
        }
        FoundationDao foundationDao = canChooseFoundationRoute(data) ? this.selectedFoundationDao : FoundationDao.NONE;
        GoldenCoreDao goldenCoreDao = canChooseGoldenCoreRoute(data) ? this.selectedGoldenCoreDao : GoldenCoreDao.NONE;
        ModNetwork.CHANNEL.sendToServer(new RequestBreakthroughPacket(foundationDao, goldenCoreDao));
    }

    private void normalizeBreakthroughSelection(CultivationData data, LocalPlayer player) {
        // 照搬原模组完整逻辑
        if (this.selectedFoundationDao == null || this.selectedFoundationDao == FoundationDao.NONE) {
            this.selectedFoundationDao = FoundationDao.HUMAN;
        }
        if (this.selectedGoldenCoreDao == null || this.selectedGoldenCoreDao == GoldenCoreDao.NONE) {
            this.selectedGoldenCoreDao = GoldenCoreDao.HUMAN;
        }
        if (!canChooseFoundationRoute(data)) {
            this.selectedFoundationDao = FoundationDao.HUMAN;
        }
        if (!canChooseGoldenCoreRoute(data)) {
            this.selectedGoldenCoreDao = GoldenCoreDao.HUMAN;
        } else {
            boolean hasBloodTalisman = hasBloodTransformationTalisman(player);
            if (!data.isFoundationAllowedForGoldenCore(this.selectedGoldenCoreDao, hasBloodTalisman)) {
                this.selectedGoldenCoreDao = firstSelectableGoldenCoreDao(data, hasBloodTalisman);
            }
        }
    }

    private GoldenCoreDao firstSelectableGoldenCoreDao(CultivationData data, boolean hasBloodTalisman) {
        for (GoldenCoreDao dao : GOLDEN_CORE_BREAKTHROUGH_OPTIONS) {
            if (!data.isFoundationAllowedForGoldenCore(dao, hasBloodTalisman)) continue;
            return dao;
        }
        return GoldenCoreDao.HUMAN;
    }

    private boolean isSelectedBreakthroughReady(CultivationData data, LocalPlayer player) {
        // 照搬原模组：练气大圆满检查筑基Dao就绪，筑基大圆满检查金丹Dao就绪
        if (data.getRealm() == Realm.QI_REFINING && data.getSubStage().isPeak()) {
            return data.isEligibleFoundationDao(this.selectedFoundationDao, com.friday.cultivation.LifespanHelper.displayBoneAge(data));
        }
        if (data.getRealm() == Realm.FOUNDATION_BUILDING && data.getSubStage().isPeak()) {
            return data.isEligibleGoldenCoreDao(this.selectedGoldenCoreDao, com.friday.cultivation.LifespanHelper.displayBoneAge(data), hasBloodTransformationTalisman(player));
        }
        return true;
    }

    private boolean canChooseFoundationRoute(CultivationData data) {
        // 照搬原模组：练气大圆满才能选筑基路线
        return data != null && data.getRealm() == Realm.QI_REFINING && data.getSubStage().isPeak();
    }

    private boolean canChooseGoldenCoreRoute(CultivationData data) {
        // 照搬原模组：筑基大圆满才能选金丹路线
        return data != null && data.getRealm() == Realm.FOUNDATION_BUILDING && data.getSubStage().isPeak();
    }

    private boolean hasBloodTransformationTalisman(LocalPlayer player) {
        // 照搬原模组：检查玩家背包是否有血气转化符（加isPresent保护避免注册未就绪崩溃）
        if (player == null) return false;
        if (!ModItems.BLOOD_TRANSFORMATION_TALISMAN.isPresent()) return false;
        return player.getInventory().contains(new ItemStack(ModItems.BLOOD_TRANSFORMATION_TALISMAN.get()));
    }

    private Component formatIdentityValue(CultivationData data) {
        if (data.isSoulReaperIdentity()) {
            return Component.translatable("entity.friday_cultivation.soul_reaper");
        }
        SectRole sectRole = SectRole.byId(data.getSectRoleId());
        if (data.hasSectDisplay() && sectRole != SectRole.NONE) {
            return sectRole.identity(data.getSectName());
        }
        if (!data.hasChosenIdentity()) {
            return Component.literal("\u2014");
        }
        Identity ident = Identity.byId(data.getIdentityId());
        MutableComponent identName = Component.translatable(ident.translationKey());
        if (ident.isSolo()) {
            return identName;
        }
        MutableComponent sect = Component.translatable("sect.friday_cultivation." + ident.defaultSectId());
        return Component.translatable("screen.friday_cultivation.attr.id.identity_with_sect", identName, sect);
    }

    private Component soulReaperCountdownText(CultivationData data, LocalPlayer player) {
        if (data == null || player == null || !data.isSoulState()) return null;
        if (player.level().dimension() == ModDimensions.DIFU) return null;
        int arrivalTick = SoulStateHandler.nextScheduledReaperTick(data);
        if (arrivalTick < 0) return null;
        int remainingTicks = arrivalTick - data.getSoulTicks();
        if (remainingTicks <= 0) return null;
        return Component.translatable("screen.friday_cultivation.soul_reaper.countdown",
                this.formatSoulReaperCountdownTime(remainingTicks),
                SoulStateHandler.upcomingReaperCount(data),
                SoulStateHandler.upcomingReaperRealm(data).displayName());
    }

    private Component formatSoulReaperCountdownTime(int ticks) {
        int seconds = Math.max(1, (ticks + 19) / 20);
        int minutes = seconds / 60;
        int remainSeconds = seconds % 60;
        if (minutes > 0) {
            return Component.translatable("screen.friday_cultivation.soul_reaper.time.minutes_seconds", minutes, remainSeconds);
        }
        return Component.translatable("screen.friday_cultivation.soul_reaper.time.seconds", remainSeconds);
    }

    private Component formatRaceValue(CultivationData data) {
        Realm realm = data.getRealm();
        boolean soul = data.isSoulState();
        MutableComponent race = Component.translatable(
                soul ? "race.friday_cultivation.ghost" : "race.friday_cultivation.human");
        String subKey = realm == Realm.MORTAL
                ? (soul ? "race_sub.friday_cultivation.ghost_mortal" : "race_sub.friday_cultivation.mortal")
                : (realm.ordinal() >= Realm.TRUE_IMMORTAL.ordinal()
                    ? (soul ? "race_sub.friday_cultivation.ghost_immortal" : "race_sub.friday_cultivation.immortal")
                    : (soul ? "race_sub.friday_cultivation.ghost_cultivator" : "race_sub.friday_cultivation.cultivator"));
        return Component.translatable("screen.friday_cultivation.attr.id.race_value", race, Component.translatable(subKey));
    }

    private void renderOriginInfoTooltip(GuiGraphics gfx, List<Component> lines, int mouseX, int mouseY) {
        int maxWidth = Math.max(80, Math.min(ORIGIN_INFO_TOOLTIP_MAX_WIDTH, this.width - ORIGIN_INFO_TOOLTIP_MARGIN));
        List<FormattedCharSequence> wrapped = new ArrayList<>();
        for (Component line : lines) {
            wrapped.addAll(this.font.split(line, maxWidth));
        }
        gfx.renderTooltip(this.font, wrapped, mouseX, mouseY);
    }

    private static int spiritRootRowColor(SpiritRoot root) {
        return DrawCardWidget.rarityRgbColor(root.rarity());
    }

    private static ChatFormatting nearestChatFormatting(int rgb) {
        return switch (rgb) {
            case -5213953 -> ChatFormatting.LIGHT_PURPLE;
            case -2047936 -> ChatFormatting.YELLOW;
            case -12549889 -> ChatFormatting.BLUE;
            case -2080704 -> ChatFormatting.RED;
            case -7829368 -> ChatFormatting.GRAY;
            default -> ChatFormatting.GRAY;
        };
    }

    private static int physiqueRowColor(Physique physique) {
        return switch (physique.rarity()) {
            default -> throw new IncompatibleClassChangeError();
            case LOW      -> -7829368;
            case MID      -> -10577298;
            case HIGH     -> -5207510;
            case SUPREME  -> -3109824;
            case IMMORTAL -> -2080704;
            case SPECIAL  -> -2080704;
        };
    }

    private static ChatFormatting physiqueChatFormatting(Physique.Rarity rarity) {
        return switch (rarity) {
            default -> throw new IncompatibleClassChangeError();
            case LOW      -> ChatFormatting.GRAY;
            case MID      -> ChatFormatting.GREEN;
            case HIGH     -> ChatFormatting.GOLD;
            case SUPREME  -> ChatFormatting.GOLD;
            case IMMORTAL -> ChatFormatting.RED;
            case SPECIAL  -> ChatFormatting.RED;
        };
    }

    private static boolean canEquipTechniqueForScreenState(CultivationData data, Technique t) {
        if (t.isGhostDao() && !data.isSoulState()) return false;
        if (!t.isGhostDao() && data.isSoulState()) return false;
        return true;
    }

    private CultivationData currentClientCultivationData() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return null;
        CultivationData ic = CultivationCapability.get(player).orElse(null);
        return ic != null ? ic : null;
    }

    // 真元加点相关（照搬原模组长按逻辑）
    private void startZhenyuanHold(int attrIndex) {
        if (this.spendZhenyuanPoints(attrIndex, 1, true)) {
            this.zhenyuanHoldAttrIndex = attrIndex;
            this.zhenyuanHoldStartTick = this.screenTickCounter;
            this.zhenyuanHoldNextSpendTick = this.screenTickCounter + ZHENYUAN_HOLD_INITIAL_DELAY_TICKS;
        } else {
            this.stopZhenyuanHold();
        }
    }

    private void tickZhenyuanHold() {
        if (this.zhenyuanHoldAttrIndex < 0) return;
        if (this.currentTab != Tab.ATTRIBUTES || !this.isPrimaryMouseButtonDown()
                || !this.isMouseInsideZhenyuanPlus(this.zhenyuanHoldAttrIndex, this.lastMouseX, this.lastMouseY)) {
            this.stopZhenyuanHold();
            return;
        }
        CultivationData data = this.currentClientCultivationData();
        if (data == null || data.getUnallocatedZhenyuan() <= 0) {
            this.stopZhenyuanHold();
            return;
        }
        if (this.screenTickCounter < this.zhenyuanHoldNextSpendTick) return;
        long heldTicks = Math.max(0L, this.screenTickCounter - this.zhenyuanHoldStartTick);
        int amount = Math.min(zhenyuanHoldBatchSize(heldTicks), data.getUnallocatedZhenyuan());
        if (!this.spendZhenyuanPoints(this.zhenyuanHoldAttrIndex, amount, false)) {
            this.stopZhenyuanHold();
            return;
        }
        this.zhenyuanHoldNextSpendTick = this.screenTickCounter + zhenyuanHoldIntervalTicks(heldTicks);
    }

    private boolean spendZhenyuanPoints(int attrIndex, int amount, boolean playSound) {
        CultivationData data = this.currentClientCultivationData();
        CultivationData.ZhenyuanAttr[] attrs = CultivationData.ZhenyuanAttr.values();
        if (data == null || attrIndex < 0 || attrIndex >= attrs.length) return false;
        int clampedAmount = Math.min(Math.max(1, amount), Math.max(0, data.getUnallocatedZhenyuan()));
        if (clampedAmount <= 0) return false;
        ModNetwork.CHANNEL.sendToServer(new SpendZhenyuanPacket(attrs[attrIndex], clampedAmount));
        if (playSound) this.playUiClick();
        this.zhenyuanPlusFlashUntil[attrIndex] = System.currentTimeMillis() + ZHENYUAN_HOLD_FLASH_MS;
        return true;
    }

    private boolean isMouseInsideZhenyuanPlus(int attrIndex, double mouseX, double mouseY) {
        if (attrIndex < 0 || attrIndex >= this.zhenyuanPlusRects.length) return false;
        int[] r = this.zhenyuanPlusRects[attrIndex];
        return r[2] != 0 && inRect(r, mouseX, mouseY);
    }

    private boolean isPrimaryMouseButtonDown() {
        long window = Minecraft.getInstance().getWindow().getWindow();
        return GLFW.glfwGetMouseButton(window, 0) == 1;
    }

    private void stopZhenyuanHold() {
        this.zhenyuanHoldAttrIndex = -1;
        this.zhenyuanHoldStartTick = 0L;
        this.zhenyuanHoldNextSpendTick = 0L;
    }

    private static int zhenyuanHoldIntervalTicks(long heldTicks) {
        if (heldTicks >= ZHENYUAN_HOLD_RUSH_TICKS) return 1;
        if (heldTicks >= ZHENYUAN_HOLD_FASTER_TICKS) return 2;
        if (heldTicks >= ZHENYUAN_HOLD_FAST_TICKS) return 3;
        return 4;
    }

    private static int zhenyuanHoldBatchSize(long heldTicks) {
        if (heldTicks >= ZHENYUAN_HOLD_MAX_TICKS) return 16;
        if (heldTicks >= ZHENYUAN_HOLD_RUSH_TICKS) return 8;
        if (heldTicks >= ZHENYUAN_HOLD_FASTER_TICKS) return 4;
        if (heldTicks >= ZHENYUAN_HOLD_FAST_TICKS) return 2;
        return 1;
    }

    // ═══════════════════════════════════════════
    // 鼠标交互（照搬原模组 mouseClicked/mouseScrolled/keyPressed）
    // ═══════════════════════════════════════════

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        if (this.handleBonusSettingsClick(mouseX, mouseY, button)) return true;
        if (this.handleBreakthroughHistoryClick(mouseX, mouseY, button)) return true;
        if (this.editingName) {
            if (this.nameEditBox.isMouseOver(mouseX, mouseY)) {
                return this.nameEditBox.mouseClicked(mouseX, mouseY, button);
            }
            this.submitName();
        }
        if (button == 0 && inRect(this.sectEntryArrowRect, mouseX, mouseY)) {
            CultivationData data = currentClientCultivationData();
            if (data != null && data.hasSectDisplay()) {
                ModNetwork.CHANNEL.sendToServer(new RequestSectScreenPacket(-1));
                this.playUiClick();
                return true;
            }
        }
        if (button == 0 && !this.editingName && inRect(this.nameCellRect, mouseX, mouseY)) {
            this.startNameEdit();
            this.playUiClick();
            return true;
        }
        if (button == 0 && inRect(this.genderCellRect, mouseX, mouseY)) {
            ModNetwork.CHANNEL.sendToServer(new CycleGenderPacket());
            this.playUiClick();
            return true;
        }
        if ((this.currentTab == Tab.SPELLS || this.currentTab == Tab.TECHNIQUES) && this.handleFilterClick(mouseX, mouseY, button)) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            return true;
        }
        if (this.handleBreakthroughOptionClick(mouseX, mouseY, button)) return true;
        if (this.currentTab == Tab.ATTRIBUTES && button == 0) {
            for (int i = 0; i < 5; ++i) {
                int[] r = this.zhenyuanPlusRects[i];
                if (r[2] != 0 && inRect(r, mouseX, mouseY)) {
                    this.startZhenyuanHold(i);
                    return true;
                }
            }
        }
        if (this.currentTab == Tab.TECHNIQUES && button == 0) {
            CultivationData data = currentClientCultivationData();
            if (data != null) {
                List<String> tids = this.filteredLearnedTechniques(data);
                if (this.techniqueEquippedRect != null) {
                    int ex = this.techniqueEquippedRect[0] + 4;
                    int ey = this.techniqueEquippedRect[1] + 4;
                    if (mouseX >= ex && mouseX < ex + 16 && mouseY >= ey && mouseY < ey + 16) {
                        if (!data.getEquippedTechniqueId().isEmpty()) {
                            ModNetwork.CHANNEL.sendToServer(new EquipTechniquePacket(""));
                        }
                        return true;
                    }
                }
                for (int[] rect : this.techniqueLearnedCellRects) {
                    int cx = rect[0], cy = rect[1], idx = rect[2];
                    if (!inRect(new int[]{cx, cy, cx + 18, cy + 18}, mouseX, mouseY)) continue;
                    if (idx >= 0 && idx < tids.size()) {
                        String tid = tids.get(idx);
                        if (tid.equals(data.getEquippedTechniqueId())) {
                            ModNetwork.CHANNEL.sendToServer(new EquipTechniquePacket(""));
                        } else {
                            Technique target = Technique.byId(tid);
                            if (canEquipTechniqueForScreenState(data, target)) {
                                ModNetwork.CHANNEL.sendToServer(new EquipTechniquePacket(tid));
                            }
                        }
                    }
                    return true;
                }
            }
        }
        if (this.currentTab == Tab.SPELLS && button == 0) {
            CultivationData data = currentClientCultivationData();
            if (data != null) {
                List<String> sids = this.filteredLearnedSpells(data);
                for (int[] rect : this.learnedCellRects) {
                    int cx = rect[0], cy = rect[1], idx = rect[2];
                    if (!inRect(new int[]{cx, cy, cx + 18, cy + 18}, mouseX, mouseY) || idx < 0 || idx >= sids.size()) continue;
                    this.draggingSpellId = sids.get(idx);
                    this.draggingFromSlot = -1;
                    this.dragStartX = mouseX;
                    this.dragStartY = mouseY;
                    this.isDragging = false;
                    this.selectedSpellId = this.draggingSpellId;
                    return true;
                }
                for (int[] rect : this.wheelCellRects) {
                    int cx = rect[0], cy = rect[1], slot = rect[2];
                    if (!inRect(new int[]{cx, cy, cx + 18, cy + 18}, mouseX, mouseY)) continue;
                    String wheelSid = data.getEquippedSpellAt(slot);
                    if (!wheelSid.isEmpty()) {
                        this.draggingSpellId = wheelSid;
                        this.draggingFromSlot = slot;
                        this.dragStartX = mouseX;
                        this.dragStartY = mouseY;
                        this.isDragging = false;
                        this.selectedSpellId = this.draggingSpellId;
                    } else if (this.selectedSpellId != null) {
                        Spell sel = Spell.byId(this.selectedSpellId);
                        if (sel != null && sel.type() == SpellType.ACTIVE) {
                            ModNetwork.CHANNEL.sendToServer(new EquipSpellPacket(slot, this.selectedSpellId));
                        }
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && this.draggingSpellId != null) {
            if (this.isDragging) {
                CultivationData data = currentClientCultivationData();
                if (data != null) {
                    boolean droppedOnWheel = false;
                    for (int[] rect : this.wheelCellRects) {
                        int cx = rect[0], cy = rect[1], slot = rect[2];
                        if (!inRect(new int[]{cx, cy, cx + 18, cy + 18}, mouseX, mouseY)) continue;
                        Spell sp = Spell.byId(this.draggingSpellId);
                        if (sp != null && sp.type() == SpellType.ACTIVE) {
                            ModNetwork.CHANNEL.sendToServer(new EquipSpellPacket(slot, this.draggingSpellId));
                        }
                        droppedOnWheel = true;
                        break;
                    }
                    if (!droppedOnWheel && this.draggingFromSlot >= 0) {
                        // 拖出轮盘 = 卸下
                        ModNetwork.CHANNEL.sendToServer(new EquipSpellPacket(this.draggingFromSlot, ""));
                    }
                }
            }
            this.draggingSpellId = null;
            this.draggingFromSlot = -1;
            this.isDragging = false;
            return true;
        }
        if (button == 0) {
            this.stopZhenyuanHold();
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        if (this.draggingSpellId != null && !this.isDragging) {
            double dx = mouseX - this.dragStartX;
            double dy = mouseY - this.dragStartY;
            if (dx * dx + dy * dy > 16.0) {
                this.isDragging = true;
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.breakthroughHistoryPopupOpen) return true;
        if (this.currentTab == Tab.TECHNIQUES && this.mouseOverTechGrid && this.techMaxScroll > 0) {
            int newOffset = (int)Math.max(0, Math.min(this.techMaxScroll, this.techScrollOffset - delta));
            if (newOffset != this.techScrollOffset) { this.techScrollOffset = newOffset; return true; }
        }
        if (this.currentTab == Tab.SPELLS && this.mouseOverSpellGrid && this.spellMaxScroll > 0) {
            int newOffset = (int)Math.max(0, Math.min(this.spellMaxScroll, this.spellScrollOffset - delta));
            if (newOffset != this.spellScrollOffset) { this.spellScrollOffset = newOffset; return true; }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.breakthroughHistoryPopupOpen && keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.breakthroughHistoryPopupOpen = false;
            return true;
        }
        if (this.editingName) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                this.submitName();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.endNameEdit();
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && this.openDropdown != 0) {
            this.openDropdown = 0;
            this.openDropdownAnchor = null;
            return true;
        }
        // K键或E键关闭
        try {
            InputConstants.Key cultivationKey = com.friday.cultivation.client.ClientKeybindings.OPEN_CULTIVATION_SCREEN.getKey();
            if (cultivationKey.getType() == InputConstants.Type.KEYSYM && cultivationKey.getValue() == keyCode) {
                this.onClose();
                return true;
            }
            InputConstants.Key inventoryKey = Minecraft.getInstance().options.keyInventory.getKey();
            if (inventoryKey.getType() == InputConstants.Type.KEYSYM && inventoryKey.getValue() == keyCode) {
                this.onClose();
                return true;
            }
        } catch (Throwable ignored) {}
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private boolean handleFilterClick(double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        if (this.openDropdown != 0) {
            for (int[] r : this.dropdownOptionRects) {
                if (!inRect(r, mouseX, mouseY)) continue;
                int idx = r[4];
                this.applyDropdownSelection(this.openDropdown, idx);
                this.openDropdown = 0;
                this.openDropdownAnchor = null;
                return true;
            }
            this.openDropdown = 0;
            this.openDropdownAnchor = null;
        }
        for (int[] rect : this.filterButtonRects) {
            if (!inRect(rect, mouseX, mouseY)) continue;
            int kind = rect[4], valueIdx = rect[5];
            if (kind == 0) {
                this.spellTypeFilter = SpellTypeFilter.values()[valueIdx];
                return true;
            }
            if (this.openDropdown == kind) {
                this.openDropdown = 0;
                this.openDropdownAnchor = null;
            } else {
                this.openDropdown = kind;
                this.openDropdownAnchor = new int[]{rect[0], rect[1], rect[2], rect[3]};
            }
            return true;
        }
        return false;
    }

    private void applyDropdownSelection(int kind, int idx) {
        switch (kind) {
            case 1: this.spellElementFilter = ElementFilter.values()[idx]; break;
            case 2: this.spellTierFilter = TierFilter.values()[idx]; break;
            case 3: this.techElementFilter = ElementFilter.values()[idx]; break;
            case 4: this.techTierFilter = TierFilter.values()[idx]; break;
        }
    }

    private boolean handleBonusSettingsClick(double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        if (this.bonusSettingsPopupOpen) {
            if (inRect(this.bonusSettingsPopupCloseRect, mouseX, mouseY) || !inRect(this.bonusSettingsPopupRect, mouseX, mouseY)) {
                this.bonusSettingsPopupOpen = false;
                this.playUiClick();
                return true;
            }
            CultivationData data = currentClientCultivationData();
            if (data != null) {
                for (BonusToggleRowRect row : this.bonusToggleRowRects) {
                    if (!inRect(row.rect, mouseX, mouseY)) continue;
                    boolean enabled = data.isBonusCategoryEnabled(row.category);
                    // 客户端立即更新本地状态，让UI动态变化（服务端包处理后会再次同步确认）
                    data.setBonusCategoryEnabled(row.category, !enabled);
                    ModNetwork.CHANNEL.sendToServer(new ToggleBonusCategoryPacket(row.category.id(), !enabled));
                    this.playUiClick();
                    return true;
                }
            }
            return true;
        }
        if (this.currentTab == Tab.ATTRIBUTES && inRect(this.bonusSettingsButtonRect, mouseX, mouseY)) {
            this.bonusSettingsPopupOpen = true;
            this.playUiClick();
            return true;
        }
        return false;
    }

    private boolean handleBreakthroughHistoryClick(double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        if (this.breakthroughHistoryPopupOpen) {
            if (inRect(this.breakthroughHistoryPopupCloseRect, mouseX, mouseY) || !inRect(this.breakthroughHistoryPopupRect, mouseX, mouseY)) {
                this.breakthroughHistoryPopupOpen = false;
                this.playUiClick();
                return true;
            }
            return true;
        }
        if (this.currentTab == Tab.BREAKTHROUGH && inRect(this.breakthroughHistoryButtonRect, mouseX, mouseY)) {
            this.breakthroughHistoryPopupOpen = true;
            this.playUiClick();
            return true;
        }
        return false;
    }

    private boolean handleBreakthroughOptionClick(double mouseX, double mouseY, int button) {
        if (button != 0 || this.currentTab != Tab.BREAKTHROUGH) return false;
        for (int[] r : this.breakthroughOptionRects) {
            if (!inRect(r, mouseX, mouseY)) continue;
            int kind = r[4], ordinal = r[5];
            if (kind == 1) {
                if (ordinal < 0 || ordinal >= FOUNDATION_BREAKTHROUGH_OPTIONS.length) return false;
                this.selectedFoundationDao = FOUNDATION_BREAKTHROUGH_OPTIONS[ordinal];
            } else if (kind == 2) {
                if (ordinal < 0 || ordinal >= GOLDEN_CORE_BREAKTHROUGH_OPTIONS.length) return false;
                this.selectedGoldenCoreDao = GOLDEN_CORE_BREAKTHROUGH_OPTIONS[ordinal];
            }
            this.playUiClick();
            return true;
        }
        return false;
    }

    // ═══════════════════════════════════════════
    // 生命周期
    // ═══════════════════════════════════════════

    @Override
    public void removed() {
        if (this.editingName) this.submitName();
        this.stopZhenyuanHold();
        super.removed();
    }

    @Override
    public void tick() {
        super.tick();
        ++this.screenTickCounter;
        this.tickZhenyuanHold();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ═══════════════════════════════════════════
    // 内部类（照搬原模组10个内部类）
    // ═══════════════════════════════════════════

    /** 标签页枚举 */
    private enum Tab {
        ATTRIBUTES, TECHNIQUES, SPELLS, BREAKTHROUGH
    }

    /** 法术类型筛选器 */
    private enum SpellTypeFilter {
        ALL, ACTIVE, PASSIVE;

        public Component display() {
            return Component.translatable("screen.friday_cultivation.filter.spell_type." + name().toLowerCase());
        }
    }

    /** 元素筛选器 */
    private enum ElementFilter {
        ALL, METAL, WOOD, WATER, FIRE, EARTH, ICE, LIGHTNING, PURE;

        public Component display() {
            return Component.translatable("screen.friday_cultivation.filter.element." + name().toLowerCase());
        }

        public boolean matchesSpell(Spell sp) {
            if (this == ALL) return true;
            return sp.element().name().equals(this.name());
        }

        public boolean matchesTechnique(Technique t) {
            if (this == ALL) return true;
            return t.primaryElement().name().equals(this.name());
        }
    }

    /** 品阶筛选器 */
    private enum TierFilter {
        ALL, COMMON, UNCOMMON, RARE, EPIC, LEGENDARY;

        public Component display() {
            return Component.translatable("screen.friday_cultivation.filter.tier." + name().toLowerCase());
        }

        public boolean matchesTier(Technique.Tier tier) {
            if (this == ALL) return true;
            // 原模组TierFilter用COMMON/UNCOMMON/RARE/EPIC/LEGENDARY，映射到 LOW/MID/HIGH/SUPREME/IMMORTAL
            return switch (this) {
                case COMMON    -> tier == Technique.Tier.LOW;
                case UNCOMMON  -> tier == Technique.Tier.MID;
                case RARE      -> tier == Technique.Tier.HIGH;
                case EPIC      -> tier == Technique.Tier.SUPREME;
                case LEGENDARY -> tier == Technique.Tier.IMMORTAL;
                default -> false;
            };
        }

        public boolean matchesTier(ItemTier tier) {
            if (this == ALL) return true;
            return switch (this) {
                case COMMON    -> tier == ItemTier.LOW;
                case UNCOMMON  -> tier == ItemTier.MID;
                case RARE      -> tier == ItemTier.HIGH;
                case EPIC      -> tier == ItemTier.SUPREME;
                case LEGENDARY -> tier == ItemTier.IMMORTAL;
                default -> false;
            };
        }
    }

    /** 增益开关行矩形 */
    private static class BonusToggleRowRect {
        final int[] rect;
        final CultivationBonusCategory category;
        BonusToggleRowRect(int[] rect, CultivationBonusCategory category) {
            this.rect = rect;
            this.category = category;
        }
    }

    /** 突破路线行 */
    private static class BreakthroughRouteRow {
        final Component routeName;
        final List<RequirementPart> requirements;
        BreakthroughRouteRow(Component routeName, List<RequirementPart> requirements) {
            this.routeName = routeName;
            this.requirements = requirements;
        }
        Component routeName() { return routeName; }
        List<RequirementPart> requirements() { return requirements; }
    }

    /** 突破可视行 */
    private static class BreakthroughVisualLine {
        final List<Component> segments;
        final int rawWidth;
        BreakthroughVisualLine(List<Component> segments, int rawWidth) {
            this.segments = segments;
            this.rawWidth = rawWidth;
        }
        List<Component> segments() { return segments; }
        int rawWidth() { return rawWidth; }
    }

    /** 突破历史弹窗行 */
    private static class HistoryPopupLine {
        final Component text;
        final int color;
        final float scale;
        HistoryPopupLine(Component text, int color, float scale) {
            this.text = text;
            this.color = color;
            this.scale = scale;
        }
        Component text() { return text; }
        int color() { return color; }
        float scale() { return scale; }
    }

    /** 突破需求部件 */
    private static class RequirementPart {
        final Component text;
        final boolean met;
        RequirementPart(Component text, boolean met) {
            this.text = text;
            this.met = met;
        }
        Component text() { return text; }
        boolean met() { return met; }
    }
}
