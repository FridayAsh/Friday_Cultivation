/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants$Key
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.core.Holder
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.Style
 *  net.minecraft.network.chat.TextColor
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.util.FormattedCharSequence
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraftforge.registries.RegistryObject
 *  org.jetbrains.annotations.NotNull
 *  org.lwjgl.glfw.GLFW
 */
package com.friday.cultivation.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.friday.cultivation.client.ClientKeybindings;
import com.friday.cultivation.client.screen.EditNameScreen;
import com.friday.cultivation.client.screen.SpellIconRenderHelper;
import com.friday.cultivation.client.screen.TimeAccelerationChoiceScreen;
import com.friday.cultivation.client.screen.widget.BambooTabButton;
import com.friday.cultivation.client.screen.widget.CinnabarButton;
import com.friday.cultivation.client.screen.widget.CloseIconButton;
import com.friday.cultivation.client.screen.widget.DrawCardWidget;
import com.friday.cultivation.client.screen.widget.LabeledToggleSwitchButton;
import com.friday.cultivation.client.screen.widget.MiniCinnabarButton;
import com.friday.cultivation.cultivation.BodyDefenseHelper;
import com.friday.cultivation.cultivation.CultivationBonusCategory;
import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.FoundationDao;
import com.friday.cultivation.cultivation.FoundationDaoBonusHelper;
import com.friday.cultivation.cultivation.GoldenCoreDao;
import com.friday.cultivation.cultivation.GoldenCoreDaoBonusHelper;
import com.friday.cultivation.cultivation.Identity;
import com.friday.cultivation.cultivation.ItemTier;
import com.friday.cultivation.cultivation.LifespanHelper;
import com.friday.cultivation.cultivation.LooseImmortalBonusHelper;
import com.friday.cultivation.cultivation.Physique;
import com.friday.cultivation.cultivation.QiElement;
import com.friday.cultivation.cultivation.SpiritRoot;
import com.friday.cultivation.cultivation.SpiritRootBonusHelper;
import com.friday.cultivation.cultivation.ZhenyuanBonusHelper;
import com.friday.cultivation.cultivation.alchemy.AlchemyRank;
import com.friday.cultivation.cultivation.qi.PlayerQiAbsorptionHelper;
import com.friday.cultivation.cultivation.qi.consumer.PlayerQiConsumer;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.cultivation.realm.SubStage;
import com.friday.cultivation.cultivation.refining.RefiningRank;
import com.friday.cultivation.cultivation.sect.SectRole;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.cultivation.spell.SpellElement;
import com.friday.cultivation.cultivation.spell.SpellType;
import com.friday.cultivation.cultivation.spell.SpellWheelLayout;
import com.friday.cultivation.cultivation.technique.Technique;
import com.friday.cultivation.cultivation.technique.TechniqueBonusHelper;
import com.friday.cultivation.entity.SeatEntity;
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.network.CreateImperialArtPacket;
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
import com.friday.cultivation.registry.ModDimensions;
import com.friday.cultivation.registry.ModItems;
import com.friday.cultivation.util.CompactNumberFormat;
import com.friday.cultivation.util.SpellScalingHelper;
import com.friday.cultivation.util.TooltipUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public class CultivationScreen
extends Screen {
    private static final ResourceLocation BG_TEXTURE = CultivationScreen.guiTexture("textures/gui/cultivation_bg.png");
    private static final ResourceLocation ICON_HP = CultivationScreen.guiTexture("textures/gui/icon_hp.png");
    private static final ResourceLocation ICON_CULTIVATION = CultivationScreen.guiTexture("textures/gui/icon_cultivation.png");
    private static final ResourceLocation ICON_QI = CultivationScreen.guiTexture("textures/gui/icon_qi.png");
    public static final ResourceLocation TAIJI_TEXTURE = CultivationScreen.guiTexture("textures/gui/taiji.png");
    private static final int PANEL_WIDTH = 320;
    private static final int PANEL_HEIGHT = 200;
    private static final int BG_TEX_W = 320;
    private static final int BG_TEX_H = 200;
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
    private static final Style JADE_REQUIREMENT_STYLE = Style.EMPTY.withColor(TextColor.fromRgb((int)1941347));
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
    private Tab currentTab = Tab.ATTRIBUTES;
    private static final FoundationDao[] FOUNDATION_BREAKTHROUGH_OPTIONS = new FoundationDao[]{FoundationDao.HUMAN, FoundationDao.BLOOD, FoundationDao.EARTH, FoundationDao.HEAVEN};
    private static final GoldenCoreDao[] GOLDEN_CORE_BREAKTHROUGH_OPTIONS = new GoldenCoreDao[]{GoldenCoreDao.HUMAN, GoldenCoreDao.BLOOD, GoldenCoreDao.EARTH, GoldenCoreDao.HEAVEN};
    private SpellTypeFilter spellTypeFilter = SpellTypeFilter.ALL;
    private ElementFilter spellElementFilter = ElementFilter.ALL;
    private TierFilter spellTierFilter = TierFilter.ALL;
    private ElementFilter techElementFilter = ElementFilter.ALL;
    private TierFilter techTierFilter = TierFilter.ALL;
    private final List<int[]> filterButtonRects = new ArrayList<int[]>();
    private int openDropdown = 0;
    private int[] openDropdownAnchor = null;
    private final List<int[]> dropdownOptionRects = new ArrayList<int[]>();
    private Button breakthroughBtn;
    private Button reincarnationBtn;
    private Button goDifuBtn;
    private BambooTabButton tabAttrBtn;
    private BambooTabButton tabTechBtn;
    private BambooTabButton tabSpellBtn;
    private BambooTabButton tabBreakthroughBtn;
    private Button toggleSpellBtn;
    private LabeledToggleSwitchButton spellTerrainDestructionBtn;
    private FoundationDao selectedFoundationDao = FoundationDao.HUMAN;
    private GoldenCoreDao selectedGoldenCoreDao = GoldenCoreDao.HUMAN;
    private final List<int[]> breakthroughOptionRects = new ArrayList<int[]>();
    private int hoveredBreakthroughOptionKind = 0;
    private int hoveredBreakthroughOptionOrdinal = -1;
    private int[] breakthroughHistoryButtonRect = null;
    private int[] createImperialArtButtonRect = null;
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
    private final List<BonusToggleRowRect> bonusToggleRowRects = new ArrayList<BonusToggleRowRect>();
    private boolean bonusSettingsPopupOpen = false;
    private CultivationBonusCategory hoveredBonusCategory = null;
    private final List<int[]> learnedCellRects = new ArrayList<int[]>();
    private final List<int[]> wheelCellRects = new ArrayList<int[]>();
    private final List<int[]> techniqueLearnedCellRects = new ArrayList<int[]>();
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

    private static ResourceLocation guiTexture(String path) {
        return new ResourceLocation("friday_cultivation", path);
    }

    private static float effectiveTextScale() {
        try {
            String lang = Minecraft.getInstance().getLanguageManager().getSelected();
            if (lang != null && lang.startsWith("zh")) {
                return 0.85f;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return 0.7f;
    }

    public CultivationScreen() {
        super((Component)Component.translatable((String)"screen.friday_cultivation.cultivation.title"));
    }

    protected void init() {
        super.init();
        int leftX = (this.width - 320) / 2;
        int topY = (this.height - 200) / 2;
        int leftHalfX = leftX;
        int rightHalfX = leftX + 160;
        this.breakthroughBtn = new CinnabarButton(leftHalfX + 12, topY + 200 - 26, 136, 18, (Component)Component.translatable((String)"screen.friday_cultivation.breakthrough"), btn -> this.submitBreakthroughRequest());
        this.addRenderableWidget(this.breakthroughBtn);
        this.reincarnationBtn = new CinnabarButton(leftHalfX + 12, topY + 200 - 26, 136, 18, (Component)Component.translatable((String)"screen.friday_cultivation.reincarnation.button"), btn -> ModNetwork.CHANNEL.sendToServer((Object)new RequestReincarnationScreenPacket()));
        this.reincarnationBtn.visible = false;
        this.addRenderableWidget(this.reincarnationBtn);
        this.goDifuBtn = new CinnabarButton(leftHalfX + 12, topY + 200 - 26, 136, 18, (Component)Component.translatable((String)"screen.friday_cultivation.reincarnation.go_difu"), btn -> {
            Minecraft.getInstance().setScreen(null);
            ModNetwork.CHANNEL.sendToServer((Object)new RequestGoDifuPacket());
        });
        this.goDifuBtn.visible = false;
        this.addRenderableWidget(this.goDifuBtn);
        int tabReserveRight = 16;
        int tabAreaW = 153 - tabReserveRight;
        int tabW = tabAreaW / 4 - 1;
        int tabY = topY + 10;
        Supplier<Float> sharedScaleSup = () -> Float.valueOf(this.sharedTabScale);
        int tabH = 14;
        int tabGap = 1;
        int tabStartX = rightHalfX + 4;
        this.tabAttrBtn = new BambooTabButton(tabStartX, tabY, tabW, tabH, (Component)Component.translatable((String)"screen.friday_cultivation.tab.attributes"), btn -> {
            this.currentTab = Tab.ATTRIBUTES;
            this.selectedSpellId = null;
        }).setActiveSupplier(() -> this.currentTab == Tab.ATTRIBUTES).setForcedScaleSupplier(sharedScaleSup);
        this.tabTechBtn = new BambooTabButton(tabStartX + (tabW + tabGap), tabY, tabW, tabH, (Component)Component.translatable((String)"screen.friday_cultivation.tab.techniques"), btn -> {
            this.currentTab = Tab.TECHNIQUES;
            this.selectedSpellId = null;
        }).setActiveSupplier(() -> this.currentTab == Tab.TECHNIQUES).setForcedScaleSupplier(sharedScaleSup);
        this.tabSpellBtn = new BambooTabButton(tabStartX + (tabW + tabGap) * 2, tabY, tabW, tabH, (Component)Component.translatable((String)"screen.friday_cultivation.tab.spells"), btn -> {
            this.currentTab = Tab.SPELLS;
        }).setActiveSupplier(() -> this.currentTab == Tab.SPELLS).setForcedScaleSupplier(sharedScaleSup);
        this.tabBreakthroughBtn = new BambooTabButton(tabStartX + (tabW + tabGap) * 3, tabY, tabW, tabH, (Component)Component.translatable((String)"screen.friday_cultivation.tab.breakthrough"), btn -> {
            this.currentTab = Tab.BREAKTHROUGH;
            this.selectedSpellId = null;
        }).setActiveSupplier(() -> this.currentTab == Tab.BREAKTHROUGH).setForcedScaleSupplier(sharedScaleSup);
        this.addRenderableWidget(this.tabAttrBtn);
        this.addRenderableWidget(this.tabTechBtn);
        this.addRenderableWidget(this.tabSpellBtn);
        this.addRenderableWidget(this.tabBreakthroughBtn);
        CloseIconButton closeBtn = new CloseIconButton(leftX + 320 - 16, topY + 5, 12, btn -> this.onClose());
        this.addRenderableWidget(closeBtn);
        int spellBottomRowY = topY + 200 - 22;
        this.toggleSpellBtn = new MiniCinnabarButton(rightHalfX + 160 - 12 - 52, spellBottomRowY, 52, 14, (Component)Component.translatable((String)"screen.friday_cultivation.spell.toggle.disable"), btn -> {
            if (this.selectedSpellId == null) {
                return;
            }
            Spell sel = Spell.byId(this.selectedSpellId);
            if (sel == null) {
                return;
            }
            LocalPlayer p = Minecraft.getInstance().player;
            if (p == null) {
                return;
            }
            boolean currentlyEnabled = CultivationCapability.get((Player)p).map(d -> d.isSpellEnabled(sel)).orElse(false);
            ModNetwork.CHANNEL.sendToServer((Object)new ToggleSpellPacket(sel.id(), !currentlyEnabled));
        });
        this.toggleSpellBtn.visible = false;
        this.addRenderableWidget(this.toggleSpellBtn);
        int spellContentLeft = rightHalfX + 12;
        this.spellTerrainDestructionBtn = new LabeledToggleSwitchButton(spellContentLeft, spellBottomRowY, 56, 14, (Component)Component.translatable((String)"screen.friday_cultivation.spell_terrain.label"), btn -> {
            LocalPlayer p = Minecraft.getInstance().player;
            if (p == null) {
                return;
            }
            CultivationCapability.get((Player)p).ifPresent(data -> {
                if (data.isSpellTerrainDestructionForcedOffByServer()) {
                    return;
                }
                ModNetwork.CHANNEL.sendToServer((Object)new SetSpellTerrainDestructionPacket(!data.isSpellTerrainDestructionEnabled()));
            });
        });
        this.spellTerrainDestructionBtn.visible = false;
        this.addRenderableWidget(this.spellTerrainDestructionBtn);
        this.nameEditBox = new EditBox(this.font, leftHalfX + 12, topY + 74, 136, 12, (Component)Component.translatable((String)"screen.friday_cultivation.attr.id.name"));
        this.nameEditBox.setMaxLength(16);
        this.nameEditBox.setVisible(false);
        this.nameEditBox.setBordered(true);
        this.addRenderableWidget(this.nameEditBox);
        this.editingName = false;
    }

    public void render(@NotNull GuiGraphics gfx, int mouseX, int mouseY, float partial) {
        Technique ht;
        Spell hovered;
        ArrayList<Component> lines;
        ArrayList<Component> lines2;
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        this.renderBackground(gfx);
        int leftX = (this.width - 320) / 2;
        int topY = (this.height - 200) / 2;
        if (this.tabAttrBtn != null && this.tabTechBtn != null && this.tabSpellBtn != null && this.tabBreakthroughBtn != null) {
            float s = Math.min(this.tabAttrBtn.computeAutoScale(), Math.min(this.tabTechBtn.computeAutoScale(), Math.min(this.tabSpellBtn.computeAutoScale(), this.tabBreakthroughBtn.computeAutoScale())));
            this.sharedTabScale = Math.min(0.86f, s);
        }
        this.drawHardShadow(gfx, leftX, topY, 320, 200, 6);
        RenderSystem.enableBlend();
        gfx.blit(BG_TEXTURE, leftX, topY, 0.0f, 0.0f, 320, 200, 320, 200);
        RenderSystem.disableBlend();
        this.drawPanelFrame(gfx, leftX, topY, 320, 200);
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
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        if (data == null) {
            super.render(gfx, mouseX, mouseY, partial);
            return;
        }
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
        this.bonusSettingsButtonRect[1] = 0;
        this.bonusSettingsButtonRect[0] = 0;
        this.bonusSettingsButtonRect[3] = 0;
        this.bonusSettingsButtonRect[2] = 0;
        this.bonusSettingsPopupRect = null;
        this.bonusSettingsPopupCloseRect = null;
        this.bonusToggleRowRects.clear();
        this.hoveredBonusCategory = null;
        this.normalizeBreakthroughSelection(data, player);
        if (this.currentTab != Tab.BREAKTHROUGH || data.getRealm().ordinal() < Realm.GOLDEN_CORE.ordinal()) {
            this.breakthroughHistoryPopupOpen = false;
        }
        if (this.currentTab != Tab.ATTRIBUTES) {
            this.bonusSettingsPopupOpen = false;
        }
        this.mouseOverSpellGrid = false;
        this.updateBottomActionButtons(topY, splitX, player, data);
        this.renderLeftPanel(gfx, leftX, topY, player, data, mouseX, mouseY);
        this.renderRightPanel(gfx, splitX, topY, player, data, mouseX, mouseY);
        this.highlightActiveTab(gfx);
        this.updateBottomActionButtons(topY, splitX, player, data);
        this.updateToggleSpellBtn(data);
        this.updateSpellTerrainDestructionBtn(data);
        super.render(gfx, mouseX, mouseY, partial);
        if (this.spellTerrainDestructionBtn != null && this.spellTerrainDestructionBtn.visible && this.spellTerrainDestructionBtn.isMouseOver(mouseX, mouseY)) {
            lines2 = new ArrayList<Component>();
            lines2.add((Component)Component.translatable((String)"tooltip.friday_cultivation.spell_terrain.title").copy().withStyle(ChatFormatting.GOLD));
            if (data.isSpellTerrainDestructionForcedOffByServer()) {
                lines2.add((Component)Component.translatable((String)"tooltip.friday_cultivation.spell_terrain.locked").copy().withStyle(ChatFormatting.RED));
            } else {
                lines2.add((Component)Component.translatable((String)(data.isSpellTerrainDestructionEnabled() ? "tooltip.friday_cultivation.spell_terrain.enabled" : "tooltip.friday_cultivation.spell_terrain.disabled")).copy().withStyle(ChatFormatting.GRAY));
            }
            gfx.renderTooltip(this.font, lines2, Optional.empty(), mouseX, mouseY);
        }
        if (this.sectEntryArrowRect != null && mouseX >= this.sectEntryArrowRect[0] && mouseX < this.sectEntryArrowRect[2] && mouseY >= this.sectEntryArrowRect[1] && mouseY < this.sectEntryArrowRect[3]) {
            lines2 = new ArrayList();
            lines2.add((Component)Component.translatable((String)"screen.friday_cultivation.cultivation.open_sect").copy().withStyle(ChatFormatting.GOLD));
            lines2.add((Component)Component.translatable((String)"tooltip.friday_cultivation.identity.open_sect").copy().withStyle(ChatFormatting.GRAY));
            this.renderOriginInfoTooltip(gfx, lines2, mouseX, mouseY);
        } else if (this.identityRowRect != null && (data.hasChosenIdentity() || data.isSoulReaperIdentity()) && mouseX >= this.identityRowRect[0] && mouseX < this.identityRowRect[2] && mouseY >= this.identityRowRect[1] && mouseY < this.identityRowRect[3]) {
            lines2 = new ArrayList();
            lines2.add((Component)this.formatIdentityValue(data).copy().withStyle(ChatFormatting.GOLD));
            if (data.isSoulReaperIdentity()) {
                lines2.add((Component)Component.translatable((String)"tooltip.friday_cultivation.soul_reaper.identity_desc").copy().withStyle(ChatFormatting.GRAY));
            } else {
                Identity identity = Identity.byId(data.getIdentityId());
                lines2.add((Component)Component.translatable((String)identity.descriptionKey()).copy().withStyle(ChatFormatting.GRAY));
            }
            this.renderOriginInfoTooltip(gfx, lines2, mouseX, mouseY);
        }
        if (this.spiritRootRowRect != null && mouseX >= this.spiritRootRowRect[0] && mouseX < this.spiritRootRowRect[2] && mouseY >= this.spiritRootRowRect[1] && mouseY < this.spiritRootRowRect[3]) {
            SpiritRoot root = data.getSpiritRoot();
            lines = new ArrayList<Component>();
            int color = CultivationScreen.spiritRootRowColor(root);
            ChatFormatting cf = CultivationScreen.nearestChatFormatting(color);
            lines.add((Component)Component.translatable((String)root.translationKey()).copy().withStyle(cf));
            lines.add((Component)Component.literal((String)"[").append((Component)Component.translatable((String)root.rarity().translationKey())).append((Component)Component.literal((String)"]")).withStyle(cf));
            lines.add((Component)Component.translatable((String)root.tooltipKey()).copy().withStyle(ChatFormatting.GRAY));
            this.renderOriginInfoTooltip(gfx, lines, mouseX, mouseY);
        }
        if (this.physiqueRowRect != null && mouseX >= this.physiqueRowRect[0] && mouseX < this.physiqueRowRect[2] && mouseY >= this.physiqueRowRect[1] && mouseY < this.physiqueRowRect[3]) {
            Physique physique = data.getPhysique();
            lines = new ArrayList();
            ChatFormatting cf = CultivationScreen.physiqueChatFormatting(physique.rarity());
            lines.add((Component)Component.translatable((String)physique.translationKey()).copy().withStyle(cf));
            lines.add((Component)Component.literal((String)"[").append((Component)Component.translatable((String)physique.rarity().translationKey())).append((Component)Component.literal((String)"]")).withStyle(cf));
            lines.add((Component)Component.translatable((String)physique.tooltipKey()).copy().withStyle(ChatFormatting.GRAY));
            this.renderOriginInfoTooltip(gfx, lines, mouseX, mouseY);
        }
        if (this.genderCellRect != null && mouseX >= this.genderCellRect[0] && mouseX < this.genderCellRect[2] && mouseY >= this.genderCellRect[1] && mouseY < this.genderCellRect[3]) {
            lines2 = new ArrayList();
            lines2.add((Component)Component.translatable((String)"screen.friday_cultivation.attr.id.gender").copy().withStyle(ChatFormatting.GOLD));
            lines2.add((Component)Component.translatable((String)"screen.friday_cultivation.gender.edit_hint", (Object[])new Object[]{data.getGenderEditsLeft()}).copy().withStyle(ChatFormatting.GRAY));
            this.renderOriginInfoTooltip(gfx, lines2, mouseX, mouseY);
        }
        if (this.foundationCellRect != null && mouseX >= this.foundationCellRect[0] && mouseX < this.foundationCellRect[2] && mouseY >= this.foundationCellRect[1] && mouseY < this.foundationCellRect[3]) {
            lines2 = new ArrayList();
            lines2.add((Component)Component.translatable((String)"tooltip.friday_cultivation.foundation.title").copy().withStyle(ChatFormatting.GOLD));
            lines2.add((Component)Component.translatable((String)"tooltip.friday_cultivation.foundation.human").copy().withStyle(ChatFormatting.GRAY));
            lines2.add((Component)Component.translatable((String)"tooltip.friday_cultivation.foundation.blood").copy().withStyle(ChatFormatting.GRAY));
            lines2.add((Component)Component.translatable((String)"tooltip.friday_cultivation.foundation.earth").copy().withStyle(ChatFormatting.GRAY));
            lines2.add((Component)Component.translatable((String)"tooltip.friday_cultivation.foundation.heaven").copy().withStyle(ChatFormatting.GRAY));
            this.renderOriginInfoTooltip(gfx, lines2, mouseX, mouseY);
        }
        if (this.currentTab == Tab.ATTRIBUTES) {
            for (int i = 0; i < 5; ++i) {
                int[] r = this.zhenyuanLabelRects[i];
                if (r[2] == 0 || mouseX < r[0] || mouseX >= r[2] || mouseY < r[1] || mouseY >= r[3]) continue;
                this.renderZhenyuanTooltip(gfx, mouseX, mouseY, data, i);
                break;
            }
        }
        if (this.currentTab == Tab.ATTRIBUTES) {
            int[] br = this.bonusSettingsButtonRect;
            if (!this.bonusSettingsPopupOpen && br[2] != 0 && mouseX >= br[0] && mouseX < br[2] && mouseY >= br[1] && mouseY < br[3]) {
                lines = new ArrayList();
                lines.add((Component)Component.translatable((String)"screen.friday_cultivation.attr.bonus_settings.button").copy().withStyle(ChatFormatting.GOLD));
                lines.add((Component)Component.translatable((String)"screen.friday_cultivation.attr.bonus_settings.button_tooltip").copy().withStyle(ChatFormatting.GRAY));
                gfx.renderComponentTooltip(this.font, new java.util.ArrayList<net.minecraft.network.chat.Component>(lines), mouseX, mouseY);
            }
        }
        if (this.currentTab == Tab.SPELLS && this.hoveredSpellId != null && (hovered = Spell.byId(this.hoveredSpellId)) != null) {
            boolean enabled = data.isSpellEnabled(hovered);
            gfx.renderTooltip(this.font, hovered.tooltipLines(enabled), Optional.empty(), mouseX, mouseY);
        }
        if (this.currentTab == Tab.TECHNIQUES && this.hoveredTechniqueId != null && (ht = Technique.byId(this.hoveredTechniqueId)) != null) {
            ArrayList<Component> lines3 = new ArrayList<Component>();
            lines3.add((Component)TooltipUtils.tieredName(ht.displayName(), ht.tier()));
            lines3.add((Component)TooltipUtils.tierElementLine(ht.tier(), ht.primaryElement()));
            lines3.add((Component)TooltipUtils.statsLine((Component)Component.translatable((String)"tooltip.friday_cultivation.technique.path", (Object[])new Object[]{Component.translatable((String)ht.daoPathTranslationKey())})));
            TooltipUtils.addBlank(lines3);
            TooltipUtils.addSection(lines3, "tooltip.friday_cultivation.section.effect");
            lines3.add((Component)TooltipUtils.descriptionLine(ht.description()));
            boolean isEq = ht.id().equals(data.getEquippedTechniqueId());
            boolean compatible = CultivationScreen.canEquipTechniqueForScreenState(data, ht);
            TooltipUtils.addBlank(lines3);
            String hintKey = isEq ? "screen.friday_cultivation.tech.tooltip.click_unequip" : (compatible ? "screen.friday_cultivation.tech.tooltip.click_equip" : (ht.isGhostDao() ? "screen.friday_cultivation.tech.tooltip.requires_soul" : "screen.friday_cultivation.tech.tooltip.requires_living"));
            lines3.add((Component)TooltipUtils.hintLine((Component)Component.translatable((String)hintKey)));
            gfx.renderTooltip(this.font, lines3, Optional.empty(), mouseX, mouseY);
        }
        if (this.bonusSettingsPopupOpen) {
            this.renderBonusSettingsPopup(gfx, data, mouseX, mouseY);
            return;
        }
        if (this.breakthroughHistoryPopupOpen) {
            this.renderBreakthroughHistoryPopup(gfx, data, mouseX, mouseY);
        } else {
            this.renderBreakthroughTooltip(gfx, mouseX, mouseY);
        }
        this.renderOpenDropdown(gfx, mouseX, mouseY);
    }

    private void drawPanelFrame(GuiGraphics gfx, int x, int y, int w, int h) {
        gfx.fill(x - 2, y - 2, x + w + 2, y, -10859978);
        gfx.fill(x - 2, y + h, x + w + 2, y + h + 2, -10859978);
        gfx.fill(x - 2, y, x, y + h, -10859978);
        gfx.fill(x + w, y, x + w + 2, y + h, -10859978);
        gfx.fill(x, y, x + w, y + 2, -15067628);
        gfx.fill(x, y + h - 2, x + w, y + h, -15067628);
        gfx.fill(x, y, x + 2, y + h, -15067628);
        gfx.fill(x + w - 2, y, x + w, y + h, -15067628);
        gfx.fill(x + 2, y + 2, x + w - 2, y + 3, -2504802);
        gfx.fill(x + 2, y + h - 3, x + w - 2, y + h - 2, -2504802);
        gfx.fill(x + 2, y + 2, x + 3, y + h - 2, -2504802);
        gfx.fill(x + w - 3, y + 2, x + w - 2, y + h - 2, -2504802);
    }

    private void drawHardShadow(GuiGraphics gfx, int x, int y, int w, int h, int offset) {
        gfx.fill(x + offset, y + h, x + w + offset, y + h + offset, -1946157056);
        gfx.fill(x + w, y + offset, x + w + offset, y + h, -1946157056);
    }

    private void drawSectionLabel(GuiGraphics gfx, Component label, int x, int y, int rightX) {
        int dotStart;
        gfx.fill(x, y, x + 2, y + 9, -4703686);
        this.drawSmall(gfx, label, x + 5, y + 1, -9807288);
        int textW = (int)((float)this.font.width((FormattedText)label) * CultivationScreen.effectiveTextScale());
        for (int dx = dotStart = x + 7 + textW; dx < rightX - 1; dx += 6) {
            gfx.fill(dx, y + 4, dx + 2, y + 5, -1721148856);
        }
    }

    private void drawCinnabarLeftLine(GuiGraphics gfx, int x, int yTop, int yBot) {
        gfx.fill(x, yTop, x + 2, yBot, -4703686);
    }

    private void drawDottedHLine(GuiGraphics gfx, int xLeft, int y, int xRight, int color) {
        for (int dx = xLeft; dx < xRight - 3; dx += 8) {
            gfx.fill(dx, y, dx + 4, y + 1, color);
        }
    }

    private void updateBottomActionButtons(int topY, int splitX, LocalPlayer player, CultivationData data) {
        boolean soul = data.isSoulState();
        boolean inDifu = player.level().dimension() == ModDimensions.DIFU;
        boolean canReincarnate = soul && inDifu && data.isReincarnationReady();
        boolean canGoDifu = soul && !inDifu;
        this.reincarnationBtn.visible = canReincarnate;
        this.reincarnationBtn.active = canReincarnate;
        this.goDifuBtn.visible = canGoDifu;
        this.goDifuBtn.active = canGoDifu;
        int breakthroughW = 112;
        this.breakthroughBtn.setX(splitX + 24);
        this.breakthroughBtn.setY(topY + 200 - 26);
        this.breakthroughBtn.setWidth(breakthroughW);
        this.breakthroughBtn.visible = this.currentTab == Tab.BREAKTHROUGH && data.getRealm() != Realm.LOOSE_IMMORTAL;
        this.breakthroughBtn.active = data.canBreakthrough() && !data.isInTribulation() && this.isSelectedBreakthroughReady(data, player);
    }

    private void renderLeftPanel(GuiGraphics gfx, int x, int y, LocalPlayer player, CultivationData data, int mouseX, int mouseY) {
        Component reaperCountdown;
        int half = 160;
        int contentX = x + 12;
        int contentRight = x + half - 8;
        gfx.drawCenteredString(this.font, (Component)Component.translatable((String)"screen.friday_cultivation.cultivation.title"), x + half / 2, y + 8, -15067628);
        int modelX = x + half / 2;
        int modelY = y + 60;
        InventoryScreen.renderEntityInInventoryFollowsMouse((GuiGraphics)gfx, (int)modelX, (int)modelY, (int)22, (float)(modelX - mouseX), (float)(modelY - 30 - mouseY), (LivingEntity)player);
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
        MutableComponent genderVal = Component.translatable((String)(data.getGender() == 2 ? "screen.friday_cultivation.gender.female" : "screen.friday_cultivation.gender.male"));
        MutableComponent realmValue = data.isLooseImmortal() ? Component.translatable((String)("realm.friday_cultivation.loose_immortal.level." + data.getLooseImmortalTribulations())) : (realm == Realm.MORTAL ? realm.displayName().copy() : Component.translatable((String)"screen.friday_cultivation.attr.id.realm_combined", (Object[])new Object[]{realm.displayName(), sub.displayName()}));
        MutableComponent foundationVal = fdao == FoundationDao.NONE ? Component.literal((String)"\u2014") : Component.translatable((String)fdao.translationKey());
        String yearUnit = Component.translatable((String)"screen.friday_cultivation.attr.year_unit").getString();
        boolean nearImmortal = LifespanHelper.isNearImmortal(data);
        MutableComponent boneAgeVal = Component.literal((String)(LifespanHelper.displayBoneAge(data) + yearUnit));
        MutableComponent lifespanVal = Component.literal((String)(nearImmortal ? "\u221e" : LifespanHelper.lifespanCap(data) + yearUnit));
        if (!this.editingName) {
            this.drawInfoCell(gfx, col1X, col1Right, infoY, (Component)Component.translatable((String)"screen.friday_cultivation.attr.id.name"), (Component)Component.literal((String)displayName), -12950192);
        }
        this.nameCellRect = new int[]{col1X, infoY - 1, col1Right, infoY + rowH - 1};
        this.drawInfoCell(gfx, col2X, col2Right, infoY, (Component)Component.translatable((String)"screen.friday_cultivation.attr.id.gender"), (Component)genderVal, -9810000);
        this.genderCellRect = new int[]{col2X, infoY - 1, col2Right, infoY + rowH - 1};
        this.drawInfoCell(gfx, col1X, col1Right, infoY += rowH, (Component)Component.translatable((String)"screen.friday_cultivation.attr.id.race"), this.formatRaceValue(data), data.isSoulState() ? -7704640 : -15067628);
        this.drawInfoCell(gfx, col2X, col2Right, infoY, (Component)Component.translatable((String)"screen.friday_cultivation.attr.id.realm"), (Component)realmValue, -4703686);
        boolean hasSectEntry = data.hasSectDisplay();
        int sectArrowSpace = hasSectEntry ? 13 : 0;
        this.drawInfoCell(gfx, col1X, col1Right - sectArrowSpace, infoY += rowH, (Component)Component.translatable((String)"screen.friday_cultivation.attr.id.identity"), this.formatIdentityValue(data), -15067628);
        this.identityRowRect = new int[]{col1X, infoY, col1Right - sectArrowSpace, infoY + rowH};
        if (hasSectEntry) {
            this.sectEntryArrowRect = new int[]{col1Right - 11, infoY, col1Right, infoY + rowH};
            this.drawSectEntryArrow(gfx, this.sectEntryArrowRect, CultivationScreen.inRect(this.sectEntryArrowRect, mouseX, mouseY));
        } else {
            this.sectEntryArrowRect = null;
        }
        this.drawInfoCell(gfx, col2X, col2Right, infoY, (Component)Component.translatable((String)"screen.friday_cultivation.attr.id.spirit_root"), (Component)Component.translatable((String)root.translationKey()), CultivationScreen.spiritRootRowColor(root));
        this.spiritRootRowRect = new int[]{col2X, infoY, col2Right, infoY + rowH};
        this.drawInfoCell(gfx, col1X, col1Right, infoY += rowH, (Component)Component.translatable((String)"screen.friday_cultivation.attr.id.physique_kind"), (Component)Component.translatable((String)physique.translationKey()), CultivationScreen.physiqueRowColor(physique));
        this.physiqueRowRect = new int[]{col1X, infoY, col1Right, infoY + rowH};
        this.drawInfoCell(gfx, col2X, col2Right, infoY, (Component)Component.translatable((String)"screen.friday_cultivation.attr.id.foundation"), (Component)foundationVal, -7706064);
        this.foundationCellRect = new int[]{col2X, infoY, col2Right, infoY + rowH};
        this.drawInfoCell(gfx, col1X, col1Right, infoY += rowH, (Component)Component.translatable((String)"screen.friday_cultivation.attr.bone_age_label"), (Component)boneAgeVal, -15067628);
        this.drawInfoCell(gfx, col2X, col2Right, infoY, (Component)Component.translatable((String)"screen.friday_cultivation.attr.lifespan_label"), (Component)lifespanVal, -15067628);
        float hp = player.getHealth();
        float maxHp = player.getMaxHealth();
        int statusBarW = half - 24;
        this.drawLeftStatusBar(gfx, ICON_HP, contentX, infoY += rowH + 3, statusBarW, maxHp <= 0.0f ? 0.0f : hp / maxHp, (Component)Component.translatable((String)"screen.friday_cultivation.hp_short"), String.format("%.0f / %.0f", Float.valueOf(hp), Float.valueOf(maxHp)), -1944235, -5758944);
        long curCult = data.getCultivationProgress();
        long maxCult = data.getMaxCultivation();
        this.drawLeftStatusBar(gfx, ICON_CULTIVATION, contentX, infoY += 11, statusBarW, maxCult == 0L ? 0.0f : (float)curCult / (float)maxCult, (Component)Component.translatable((String)"screen.friday_cultivation.cult_short"), curCult + " / " + maxCult, -928374, -3631046);
        long curQi = data.getCurrentQi();
        long maxQi = data.getMaxQi();
        this.drawLeftStatusBar(gfx, ICON_QI, contentX, infoY += 11, statusBarW, maxQi == 0L ? 0.0f : (float)curQi / (float)maxQi, (Component)Component.translatable((String)"screen.friday_cultivation.qi_short"), curQi + " / " + maxQi, -9583434, -13729678);
        infoY += 14;
        if (data.isInTribulation()) {
            int noteX = contentX;
            int noteY = infoY;
            int noteW = half - 24;
            int noteH = 12;
            gfx.fill(noteX, noteY, noteX + noteW, noteY + noteH, 347617850);
            this.drawCinnabarLeftLine(gfx, noteX, noteY, noteY + noteH);
            this.drawSmall(gfx, (Component)Component.translatable((String)"screen.friday_cultivation.tribulation_active", (Object[])new Object[]{Realm.formatTribulationCount(data.getTribulationStrikesRemaining(), data.getTribulationBoltsPerWave())}), noteX + 4, noteY + 2, -7723482);
        }
        if ((reaperCountdown = this.soulReaperCountdownText(data, player)) != null && this.goDifuBtn != null && this.goDifuBtn.visible) {
            int bottomButtonY = y + 200 - 26;
            int countdownY = Math.max(infoY + 1, bottomButtonY - 12);
            countdownY = Math.min(countdownY, bottomButtonY - 9);
            this.drawTinyMultilineCentered(gfx, reaperCountdown, x + half / 2, countdownY, half - 28, -7723482);
        }
        int btnY = this.breakthroughBtn.getY();
        if (this.breakthroughBtn.visible && this.currentTab != Tab.BREAKTHROUGH) {
            MutableComponent hint;
            if (realm == Realm.MORTAL) {
                hint = Component.translatable((String)"screen.friday_cultivation.breakthrough.hint_mortal");
            } else {
                int strikes = realm.tribulationCount(sub);
                int boltsPerWave = realm.tribulationBoltsPerWave(sub);
                int damage = realm.tribulationStrikeDamage();
                hint = Component.translatable((String)"screen.friday_cultivation.breakthrough.hint_tribulation", (Object[])new Object[]{Realm.formatTribulationCount(strikes, boltsPerWave), damage});
            }
            int hintMaxW = half - 24;
            this.drawTinyMultilineCentered(gfx, (Component)hint, x + half / 2, btnY - 16, hintMaxW, -9807288);
        }
    }

    private void drawTinyMultilineCentered(GuiGraphics gfx, Component text, int cx, int yTop, int maxW, int color) {
        float scale = 0.7f;
        int rawMaxW = (int)((float)maxW / scale);
        List lines = this.font.split((FormattedText)text, rawMaxW);
        Objects.requireNonNull(this.font);
        int lineH = (int)(9.0f * scale) + 1;
        for (int i = 0; i < lines.size(); ++i) {
            FormattedCharSequence line = (FormattedCharSequence)lines.get(i);
            int lineW = (int)((float)this.font.width(line) * scale);
            int lineX = cx - lineW / 2;
            int lineY = yTop + i * lineH;
            gfx.pose().pushPose();
            gfx.pose().translate((float)lineX, (float)lineY, 0.0f);
            gfx.pose().scale(scale, scale, 1.0f);
            gfx.drawString(this.font, line, 0, 0, color, false);
            gfx.pose().popPose();
        }
    }

    private void renderTimeAccelerationStatus(GuiGraphics gfx, int x, int y, int half, CultivationData data) {
        if (!data.isTimeAccelerationActive()) {
            return;
        }
        int statusX = x + 16;
        int statusY = y + 64;
        int statusRight = x + half - 40;
        gfx.fill(statusX - 2, statusY - 2, statusRight, statusY + 10, 1712985620);
        this.drawCinnabarLeftLine(gfx, statusX - 2, statusY - 2, statusY + 10);
        MutableComponent status = Component.translatable((String)"screen.friday_cultivation.time_acceleration.active_status", (Object[])new Object[]{data.getTimeAccelerationMultiplier(), CultivationScreen.formatTimeAccelerationElapsed(data.getTimeAccelerationElapsedTicks()), CultivationScreen.formatTimeAccelerationYears(data.getTimeAccelerationElapsedTicks())});
        int availableW = Math.max(1, statusRight - statusX - 6);
        float scale = Math.min(0.68f, (float)availableW / (float)Math.max(1, this.font.width((FormattedText)status)));
        scale = Math.max(0.5f, scale);
        this.drawScaled(gfx, (Component)status, statusX + 3, statusY + 1, -3562934, scale);
    }

    private static String formatTimeAccelerationElapsed(long ticks) {
        long seconds = Math.max(0L, ticks / 20L);
        long hours = seconds / 3600L;
        long minutes = seconds % 3600L / 60L;
        long secs = seconds % 60L;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    private static String formatTimeAccelerationYears(long ticks) {
        double years = (double)Math.max(0L, ticks) * 1.0 / 24000.0;
        if (years >= 100.0) {
            return String.format(Locale.ROOT, "%.0f", years);
        }
        if (years >= 10.0) {
            return String.format(Locale.ROOT, "%.1f", years);
        }
        return String.format(Locale.ROOT, "%.2f", years);
    }

    private int drawBreakthroughParagraphCentered(GuiGraphics gfx, Component text, int cx, int yTop, int maxW, int color) {
        return this.drawBreakthroughParagraphCentered(gfx, text, cx, yTop, maxW, color, 0.7f, 3);
    }

    private int drawBreakthroughParagraphCentered(GuiGraphics gfx, Component text, int cx, int yTop, int maxW, int color, float scale, int lineGap) {
        int rawMaxW = Math.max(1, (int)((float)maxW / scale));
        List lines = this.font.split((FormattedText)text, rawMaxW);
        Objects.requireNonNull(this.font);
        int lineH = (int)Math.ceil(9.0f * scale) + lineGap;
        for (int i = 0; i < lines.size(); ++i) {
            FormattedCharSequence line = (FormattedCharSequence)lines.get(i);
            int lineW = (int)((float)this.font.width(line) * scale);
            int lineX = cx - lineW / 2;
            int lineY = yTop + i * lineH;
            gfx.pose().pushPose();
            gfx.pose().translate((float)lineX, (float)lineY, 0.0f);
            gfx.pose().scale(scale, scale, 1.0f);
            gfx.drawString(this.font, line, 0, 0, color, false);
            gfx.pose().popPose();
        }
        return lines.size() * lineH;
    }

    private void drawIdRow(GuiGraphics gfx, int xLeft, int xRight, int y, Component label, Component value, int valueColor) {
        this.drawSmall(gfx, label, xLeft, y + 1, -9807288);
        int valueW = (int)((float)this.font.width((FormattedText)value) * CultivationScreen.effectiveTextScale());
        this.drawSmall(gfx, value, xRight - valueW, y + 1, valueColor);
    }

    private void drawInfoCell(GuiGraphics gfx, int cellX, int cellRight, int y, Component label, Component value, int valueColor) {
        float s = CultivationScreen.effectiveTextScale() * 0.78f;
        this.drawCellAt(gfx, label, cellX, y + 1, -9807288, s);
        float labW = (float)this.font.width((FormattedText)label) * s;
        float avail = (float)(cellRight - cellX) - labW - 3.0f;
        float valW = (float)this.font.width((FormattedText)value) * s;
        float vs = valW > avail && valW > 0.0f ? s * (avail / valW) : s;
        int vsw = (int)((float)this.font.width((FormattedText)value) * vs);
        this.drawCellAt(gfx, value, cellRight - vsw, y + 1, valueColor, vs);
    }

    private void drawSectEntryArrow(GuiGraphics gfx, int[] rect, boolean hovered) {
        if (rect == null || rect.length < 4) {
            return;
        }
        if (hovered) {
            gfx.fill(rect[0] - 1, rect[1], rect[2], rect[3], 414726714);
        }
        int color = hovered ? -7723482 : -12950192;
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
        gfx.pose().translate((float)x, (float)y, 0.0f);
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.drawString(this.font, text, 0, 0, color, false);
        gfx.pose().popPose();
    }

    private static boolean inRect(int[] r, double mx, double my) {
        return r != null && mx >= (double)r[0] && mx < (double)r[2] && my >= (double)r[1] && my < (double)r[3];
    }

    private void playUiClick() {
        Minecraft.getInstance().getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((Holder)SoundEvents.UI_BUTTON_CLICK, (float)1.0f));
    }

    private void startNameEdit() {
        LocalPlayer lp = Minecraft.getInstance().player;
        String cur = lp == null ? "" : CultivationCapability.get((Player)lp).map(d -> d.getCustomName()).orElse("");
        Minecraft.getInstance().setScreen((Screen)new EditNameScreen(this, cur));
    }

    private void submitName() {
        if (!this.editingName) {
            return;
        }
        ModNetwork.CHANNEL.sendToServer((Object)new SetCultivationNamePacket(this.nameEditBox.getValue()));
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

    public void removed() {
        if (this.editingName) {
            this.submitName();
        }
        this.stopZhenyuanHold();
        super.removed();
    }

    public void tick() {
        super.tick();
        ++this.screenTickCounter;
        this.tickZhenyuanHold();
    }

    private void startZhenyuanHold(int attrIndex) {
        if (this.spendZhenyuanPoints(attrIndex, 1, true)) {
            this.zhenyuanHoldAttrIndex = attrIndex;
            this.zhenyuanHoldStartTick = this.screenTickCounter;
            this.zhenyuanHoldNextSpendTick = this.screenTickCounter + 8L;
        } else {
            this.stopZhenyuanHold();
        }
    }

    private void tickZhenyuanHold() {
        if (this.zhenyuanHoldAttrIndex < 0) {
            return;
        }
        if (this.currentTab != Tab.ATTRIBUTES || !this.isPrimaryMouseButtonDown() || !this.isMouseInsideZhenyuanPlus(this.zhenyuanHoldAttrIndex, this.lastMouseX, this.lastMouseY)) {
            this.stopZhenyuanHold();
            return;
        }
        CultivationData data = this.currentClientCultivationData();
        if (data == null || data.getUnallocatedZhenyuan() <= 0) {
            this.stopZhenyuanHold();
            return;
        }
        if (this.screenTickCounter < this.zhenyuanHoldNextSpendTick) {
            return;
        }
        long heldTicks = Math.max(0L, this.screenTickCounter - this.zhenyuanHoldStartTick);
        int amount = Math.min(CultivationScreen.zhenyuanHoldBatchSize(heldTicks), data.getUnallocatedZhenyuan());
        if (!this.spendZhenyuanPoints(this.zhenyuanHoldAttrIndex, amount, false)) {
            this.stopZhenyuanHold();
            return;
        }
        this.zhenyuanHoldNextSpendTick = this.screenTickCounter + (long)CultivationScreen.zhenyuanHoldIntervalTicks(heldTicks);
    }

    private boolean spendZhenyuanPoints(int attrIndex, int amount, boolean playSound) {
        CultivationData data = this.currentClientCultivationData();
        CultivationData.ZhenyuanAttr[] attrs = CultivationData.ZhenyuanAttr.values();
        if (data == null || attrIndex < 0 || attrIndex >= attrs.length) {
            return false;
        }
        int clampedAmount = Math.min(Math.max(1, amount), Math.max(0, data.getUnallocatedZhenyuan()));
        if (clampedAmount <= 0) {
            return false;
        }
        ModNetwork.CHANNEL.sendToServer((Object)new SpendZhenyuanPacket(attrs[attrIndex], clampedAmount));
        if (playSound) {
            this.playUiClick();
        }
        this.zhenyuanPlusFlashUntil[attrIndex] = System.currentTimeMillis() + 140L;
        return true;
    }

    private CultivationData currentClientCultivationData() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null ? null : (CultivationData)CultivationCapability.get((Player)player).orElse(null);
    }

    private boolean isMouseInsideZhenyuanPlus(int attrIndex, double mouseX, double mouseY) {
        if (attrIndex < 0 || attrIndex >= this.zhenyuanPlusRects.length) {
            return false;
        }
        int[] r = this.zhenyuanPlusRects[attrIndex];
        return r[2] != 0 && mouseX >= (double)r[0] && mouseX < (double)r[2] && mouseY >= (double)r[1] && mouseY < (double)r[3];
    }

    private boolean isPrimaryMouseButtonDown() {
        long window = Minecraft.getInstance().getWindow().getWindow();
        return GLFW.glfwGetMouseButton((long)window, (int)0) == 1;
    }

    private void stopZhenyuanHold() {
        this.zhenyuanHoldAttrIndex = -1;
        this.zhenyuanHoldStartTick = 0L;
        this.zhenyuanHoldNextSpendTick = 0L;
    }

    private static int zhenyuanHoldIntervalTicks(long heldTicks) {
        if (heldTicks >= 100L) {
            return 1;
        }
        if (heldTicks >= 50L) {
            return 2;
        }
        if (heldTicks >= 20L) {
            return 3;
        }
        return 4;
    }

    private static int zhenyuanHoldBatchSize(long heldTicks) {
        if (heldTicks >= 160L) {
            return 16;
        }
        if (heldTicks >= 100L) {
            return 8;
        }
        if (heldTicks >= 50L) {
            return 4;
        }
        if (heldTicks >= 20L) {
            return 2;
        }
        return 1;
    }

    private Component formatIdentityValue(CultivationData data) {
        if (data.isSoulReaperIdentity()) {
            return Component.translatable((String)"entity.friday_cultivation.soul_reaper");
        }
        SectRole sectRole = SectRole.byId(data.getSectRoleId());
        if (data.hasSectDisplay() && sectRole != SectRole.NONE) {
            return sectRole.identity(data.getSectName());
        }
        if (!data.hasChosenIdentity()) {
            return Component.literal((String)"\u2014");
        }
        Identity ident = Identity.byId(data.getIdentityId());
        MutableComponent identName = Component.translatable((String)ident.translationKey());
        if (ident.isSolo()) {
            return identName;
        }
        MutableComponent sect = Component.translatable((String)("sect.friday_cultivation." + ident.defaultSectId()));
        return Component.translatable((String)"screen.friday_cultivation.attr.id.identity_with_sect", (Object[])new Object[]{identName, sect});
    }

    private Component soulReaperCountdownText(CultivationData data, LocalPlayer player) {
        if (data == null || player == null || !data.isSoulState()) {
            return null;
        }
        if (player.level().dimension() == ModDimensions.DIFU) {
            return null;
        }
        int arrivalTick = SoulStateHandler.nextScheduledReaperTick(data);
        if (arrivalTick < 0) {
            return null;
        }
        int remainingTicks = arrivalTick - data.getSoulTicks();
        if (remainingTicks <= 0) {
            return null;
        }
        return Component.translatable((String)"screen.friday_cultivation.soul_reaper.countdown", (Object[])new Object[]{this.formatSoulReaperCountdownTime(remainingTicks), SoulStateHandler.upcomingReaperCount(data), SoulStateHandler.upcomingReaperRealm(data).displayName()});
    }

    private Component formatSoulReaperCountdownTime(int ticks) {
        int seconds = Math.max(1, (ticks + 19) / 20);
        int minutes = seconds / 60;
        int remainSeconds = seconds % 60;
        if (minutes > 0) {
            return Component.translatable((String)"screen.friday_cultivation.soul_reaper.time.minutes_seconds", (Object[])new Object[]{minutes, remainSeconds});
        }
        return Component.translatable((String)"screen.friday_cultivation.soul_reaper.time.seconds", (Object[])new Object[]{remainSeconds});
    }

    private Component formatRaceValue(CultivationData data) {
        Realm realm = data.getRealm();
        boolean soul = data.isSoulState();
        MutableComponent race = Component.translatable((String)(soul ? "race.friday_cultivation.ghost" : "race.friday_cultivation.human"));
        String subKey = realm == Realm.MORTAL ? (soul ? "race_sub.friday_cultivation.ghost_mortal" : "race_sub.friday_cultivation.mortal") : (realm == Realm.GREAT_EMPEROR ? (soul ? "race_sub.friday_cultivation.ghost_great_emperor" : "race_sub.friday_cultivation.great_emperor") : (realm.ordinal() >= Realm.TRUE_IMMORTAL.ordinal() ? (soul ? "race_sub.friday_cultivation.ghost_immortal" : "race_sub.friday_cultivation.immortal") : (soul ? "race_sub.friday_cultivation.ghost_cultivator" : "race_sub.friday_cultivation.cultivator")));
        return Component.translatable((String)"screen.friday_cultivation.attr.id.race_value", (Object[])new Object[]{race, Component.translatable((String)subKey)});
    }

    private void renderOriginInfoTooltip(GuiGraphics gfx, List<Component> lines, int mouseX, int mouseY) {
        int maxWidth = Math.max(80, Math.min(180, this.width - 32));
        ArrayList wrapped = new ArrayList();
        for (Component line : lines) {
            wrapped.addAll(this.font.split((FormattedText)line, maxWidth));
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
            case LOW -> -7829368;
            case MID -> -10577298;
            case HIGH -> -5207510;
            case SUPREME -> -3109824;
            case IMMORTAL -> -2080704;
            case SPECIAL -> -2080704;
        };
    }

    private static ChatFormatting physiqueChatFormatting(Physique.Rarity rarity) {
        return switch (rarity) {
            default -> throw new IncompatibleClassChangeError();
            case LOW -> ChatFormatting.GRAY;
            case MID -> ChatFormatting.GREEN;
            case HIGH -> ChatFormatting.GOLD;
            case SUPREME -> ChatFormatting.GOLD;
            case IMMORTAL -> ChatFormatting.RED;
            case SPECIAL -> ChatFormatting.RED;
        };
    }

    private void renderRightPanel(GuiGraphics gfx, int splitX, int y, LocalPlayer player, CultivationData data, int mouseX, int mouseY) {
        int contentX = splitX + 12;
        int contentY = y + 36;
        switch (this.currentTab) {
            case ATTRIBUTES: {
                this.renderAttributesTab(gfx, contentX, contentY, data, mouseX, mouseY);
                break;
            }
            case TECHNIQUES: {
                this.renderTechniquesTab(gfx, splitX, contentY, data, mouseX, mouseY);
                break;
            }
            case SPELLS: {
                this.renderSpellsTab(gfx, splitX, contentY, data, mouseX, mouseY);
                break;
            }
            case BREAKTHROUGH: {
                this.renderBreakthroughTab(gfx, contentX, contentY, player, data, mouseX, mouseY);
            }
        }
    }

    private void renderBreakthroughTab(GuiGraphics gfx, int x, int y, LocalPlayer player, CultivationData data, int mouseX, int mouseY) {
        int rightX = x + 160 - 24;
        int width = rightX - x;
        int cx = x + width / 2;
        Realm realm = data.getRealm();
        SubStage sub = data.getSubStage();
        MutableComponent realmValue = data.isLooseImmortal() ? Component.translatable((String)("realm.friday_cultivation.loose_immortal.level." + data.getLooseImmortalTribulations())) : (realm == Realm.MORTAL ? realm.displayName().copy() : Component.translatable((String)"screen.friday_cultivation.attr.id.realm_combined", (Object[])new Object[]{realm.displayName(), sub.displayName()}));
        this.drawSmallCentered(gfx, (Component)realmValue, cx, y, -4703686);
        y += 12;
        if (data.isLooseImmortal()) {
            this.renderLooseImmortalBreakthroughTab(gfx, x, rightX, y, player, data);
            return;
        }
        long curCult = data.getCultivationProgress();
        long maxCult = data.getMaxCultivation();
        this.drawThinBar(gfx, x + 4, y, width - 8, maxCult == 0L ? 0.0f : (float)curCult / (float)maxCult, Component.translatable((String)"screen.friday_cultivation.cult_short").getString(), curCult + " / " + maxCult, -928374, -3631046);
        y += 13;
        int boneAge = LifespanHelper.displayBoneAge(data);
        boolean hasBloodTalisman = this.hasBloodTransformationTalisman(player);
        if (realm == Realm.BODY_TEMPERING) {
            y += this.drawBreakthroughParagraphCentered(gfx, (Component)Component.translatable((String)"screen.friday_cultivation.breakthrough.body_tempering_hint"), cx, y, width - 8, -9807288) + 4;
        } else if (this.canChooseFoundationRoute(data)) {
            y = this.renderFoundationBreakthroughOptions(gfx, x, rightX, y, data, boneAge, mouseX, mouseY);
        } else if (realm == Realm.QI_REFINING) {
            y += this.drawBreakthroughParagraphCentered(gfx, (Component)Component.translatable((String)"screen.friday_cultivation.breakthrough.route_locked_stage"), cx, y, width - 8, -9807288) + 4;
        } else if (realm == Realm.FOUNDATION_BUILDING) {
            MutableComponent current = Component.translatable((String)"screen.friday_cultivation.breakthrough.current_foundation", (Object[])new Object[]{Component.translatable((String)data.getFoundationDao().translationKey())});
            this.drawBreakthroughCentered(gfx, (Component)current, cx, y, width - 8, -12950192, false);
            y += 13;
            y = this.canChooseGoldenCoreRoute(data) ? this.renderGoldenCoreBreakthroughOptions(gfx, x, rightX, y, data, boneAge, hasBloodTalisman, mouseX, mouseY) : (y += this.drawBreakthroughParagraphCentered(gfx, (Component)Component.translatable((String)"screen.friday_cultivation.breakthrough.route_locked_stage"), cx, y, width - 8, -9807288) + 4);
        } else if (realm == Realm.TRUE_IMMORTAL && sub.isPeakFor(Realm.TRUE_IMMORTAL)) {
            boolean killedEmperor = data.hasKilledGreatEmperor();
            boolean hasArt = data.hasCreatedImperialArt();
            boolean artEquipped = Technique.IMPERIAL_ART.id().equals(data.getEquippedTechniqueId());
            MutableComponent killLine = killedEmperor ? Component.translatable((String)"screen.friday_cultivation.breakthrough.great_emperor_requirement_met").withStyle(ChatFormatting.GREEN) : Component.translatable((String)"screen.friday_cultivation.breakthrough.great_emperor_requirement").withStyle(ChatFormatting.RED);
            y += this.drawBreakthroughParagraphCentered(gfx, (Component)killLine, cx, y, width - 8, killedEmperor ? 5635925 : -9807288) + 4;
            MutableComponent artLine;
            if (hasArt && artEquipped) {
                artLine = Component.translatable((String)"screen.friday_cultivation.breakthrough.imperial_art_met", (Object[])new Object[]{Component.literal(data.getImperialArtName())}).withStyle(ChatFormatting.GREEN);
            } else if (hasArt) {
                artLine = Component.translatable((String)"screen.friday_cultivation.breakthrough.imperial_art_not_equipped", (Object[])new Object[]{Component.literal(data.getImperialArtName())}).withStyle(ChatFormatting.RED);
            } else {
                artLine = Component.translatable((String)"screen.friday_cultivation.breakthrough.imperial_art_requirement").withStyle(ChatFormatting.RED);
            }
            y += this.drawBreakthroughParagraphCentered(gfx, (Component)artLine, cx, y, width - 8, (hasArt && artEquipped) ? 5635925 : -9807288) + 4;
            if (killedEmperor && !hasArt) {
                y = this.renderCreateImperialArtButton(gfx, x, rightX, y, mouseX, mouseY);
            }
            if (!killedEmperor || !hasArt || !artEquipped) {
                y += this.drawBreakthroughParagraphCentered(gfx, (Component)Component.translatable((String)"screen.friday_cultivation.breakthrough.route_locked_stage"), cx, y, width - 8, -9807288) + 4;
            }
        } else if (realm.ordinal() >= Realm.GOLDEN_CORE.ordinal()) {
            y = this.renderBreakthroughHistoryButton(gfx, x, rightX, y, data, mouseX, mouseY);
            y += this.drawBreakthroughParagraphCentered(gfx, (Component)Component.translatable((String)"screen.friday_cultivation.breakthrough.route_waiting"), cx, y, width - 8, -9807288) + 4;
        }
        Component hint = this.breakthroughHint(data, boneAge, hasBloodTalisman);
        this.drawBreakthroughParagraphCentered(gfx, hint, cx, y + 4, width - 8, -9807288);
    }

    private void renderLooseImmortalBreakthroughTab(GuiGraphics gfx, int x, int rightX, int y, LocalPlayer player, CultivationData data) {
        int width = rightX - x;
        int cx = x + width / 2;
        int level = Math.max(1, LooseImmortalBonusHelper.clampLevel(data.getLooseImmortalTribulations()));
        long now = player == null || player.level() == null ? 0L : player.level().getGameTime();
        long remaining = data.getLooseImmortalTribulationRemainingTicks(now);
        y = this.renderLooseImmortalStatusCard(gfx, x, rightX, y, level, remaining);
        y += this.drawBreakthroughParagraphCentered(gfx, (Component)Component.translatable((String)"screen.friday_cultivation.breakthrough.loose_immortal.desc"), cx, y, width - 18, -9807288, 0.62f, 2) + 4;
        if (level < 9) {
            int waves = LooseImmortalBonusHelper.wavesForCurrentLevel(level);
            int bolts = LooseImmortalBonusHelper.boltsPerWaveForCurrentLevel(level);
            int damage = LooseImmortalBonusHelper.strikeDamageForCurrentLevel(level);
            y = this.renderLooseImmortalInfoLine(gfx, x, rightX, y, this.looseImmortalTokenStack(level + 1), (Component)Component.translatable((String)"screen.friday_cultivation.breakthrough.loose_immortal.next_tribulation", (Object[])new Object[]{level + 1, Realm.formatTribulationCount(waves, bolts), damage}), -7723482);
            y = this.renderLooseImmortalInfoLine(gfx, x, rightX, y, this.looseImmortalTokenStack(level + 1), this.looseImmortalBonusText(level + 1), -12950192);
        }
    }

    private int renderLooseImmortalStatusCard(GuiGraphics gfx, int x, int rightX, int y, int level, long remaining) {
        int rowX = x + 6;
        int rowRight = rightX - 6;
        int rowH = 36;
        gfx.fill(rowX, y, rowRight, y + rowH, -15067628);
        gfx.fill(rowX + 1, y + 1, rowRight - 1, y + rowH - 1, -1846616);
        gfx.fill(rowX + 2, y + 2, rowRight - 2, y + 3, -3562934);
        gfx.fill(rowX + 2, y + rowH - 3, rowRight - 2, y + rowH - 2, -10859978);
        int iconX = rowX + 8;
        int iconY = y + 7;
        this.drawLooseImmortalTokenIcon(gfx, this.looseImmortalTokenStack(level), iconX, iconY);
        MutableComponent countdown = level >= 9 ? Component.translatable((String)"screen.friday_cultivation.breakthrough.loose_immortal.cap") : Component.translatable((String)"screen.friday_cultivation.breakthrough.loose_immortal.countdown", (Object[])new Object[]{this.formatLooseImmortalDuration(remaining)});
        int textX = iconX + 28;
        int textRight = rowRight - 8;
        this.drawScaledFitting(gfx, (Component)countdown, textX, y + 7, textRight - textX, level >= 9 ? -12950192 : -7707624, CultivationScreen.effectiveTextScale() * 0.72f);
        this.drawLooseImmortalTribulationPips(gfx, textX, y + 23, textRight, level);
        return y + rowH + 6;
    }

    private int renderLooseImmortalInfoLine(GuiGraphics gfx, int x, int rightX, int y, ItemStack icon, Component text, int color) {
        int rowX = x + 6;
        int rowRight = rightX - 6;
        int iconX = rowX;
        int textX = iconX + 20;
        float scale = CultivationScreen.effectiveTextScale() * 0.64f;
        int rawTextW = Math.max(24, (int)((float)(rowRight - textX) / scale));
        List lines = this.font.split((FormattedText)text, rawTextW);
        Objects.requireNonNull(this.font);
        int lineH = (int)Math.ceil(9.0f * scale) + 1;
        int rowH = Math.max(18, lines.size() * lineH + 3);
        gfx.renderItem(icon, iconX, y + Math.max(0, (rowH - 16) / 2));
        gfx.pose().pushPose();
        gfx.pose().translate((float)textX, (float)(y + 1), 0.0f);
        gfx.pose().scale(scale, scale, 1.0f);
        for (int i = 0; i < lines.size(); ++i) {
            FormattedCharSequence formattedCharSequence = (FormattedCharSequence)lines.get(i);
            Objects.requireNonNull(this.font);
            gfx.drawString(this.font, formattedCharSequence, 0, i * 9, color, false);
        }
        gfx.pose().popPose();
        return y + rowH + 3;
    }

    private void drawLooseImmortalTokenIcon(GuiGraphics gfx, ItemStack icon, int x, int y) {
        gfx.fill(x, y, x + 22, y + 22, -15067628);
        gfx.fill(x + 1, y + 1, x + 21, y + 21, -1517128);
        gfx.fill(x + 2, y + 2, x + 20, y + 3, -3562934);
        gfx.renderItem(icon, x + 3, y + 3);
    }

    private void drawLooseImmortalTribulationPips(GuiGraphics gfx, int x, int y, int rightX, int level) {
        int count = 9;
        int pip = 8;
        int gap = 2;
        int total = count * pip + (count - 1) * gap;
        if (x + total > rightX) {
            pip = 7;
            gap = 1;
            total = count * pip + (count - 1) * gap;
        }
        int startX = x + Math.max(0, (rightX - x - total) / 2);
        for (int i = 1; i <= count; ++i) {
            int px = startX + (i - 1) * (pip + gap);
            boolean reached = i <= level;
            boolean current = i == level;
            gfx.fill(px, y, px + pip, y + pip, current ? -7723482 : -10859978);
            gfx.fill(px + 1, y + 1, px + pip - 1, y + pip - 1, reached ? -7707624 : -1648976);
            MutableComponent digit = Component.literal((String)Integer.toString(i));
            float scale = pip <= 7 ? 0.5f : 0.54f;
            int digitW = (int)((float)this.font.width((FormattedText)digit) * scale);
            int digitX = px + (pip - digitW) / 2;
            int digitY = y + (pip <= 7 ? 1 : 2);
            this.drawScaled(gfx, (Component)digit, digitX, digitY, reached ? -1 : -9807288, scale);
        }
    }

    private ItemStack looseImmortalTokenStack(int level) {
        int clamped = LooseImmortalBonusHelper.clampLevel(level);
        RegistryObject<Item> token = ModItems.LOOSE_IMMORTAL_REALM_TOKENS.get(clamped);
        return token == null ? ItemStack.EMPTY : new ItemStack((ItemLike)token.get());
    }

    private Component looseImmortalBonusText(int level) {
        int clamped = LooseImmortalBonusHelper.clampLevel(level);
        return Component.translatable((String)"screen.friday_cultivation.breakthrough.loose_immortal.next_bonus", (Object[])new Object[]{clamped, CompactNumberFormat.format(LooseImmortalBonusHelper.maxQiBonusForLevel(clamped)), LooseImmortalBonusHelper.bodyDefenseBonusForLevel(clamped), LooseImmortalBonusHelper.cultivationEfficiencyBonusForLevel(clamped), LooseImmortalBonusHelper.qiRecoveryPerSecondBonusForLevel(clamped), LooseImmortalBonusHelper.meleeDamageBonusForLevel(clamped), LooseImmortalBonusHelper.spellDamageBonusPercentForLevel(clamped), LooseImmortalBonusHelper.spellQiCostReductionPercentForLevel(clamped), LooseImmortalBonusHelper.freeZhenyuanRewardBetween(clamped - 1, clamped), LooseImmortalBonusHelper.automaticZhenyuanAttributesRewardBetween(clamped - 1, clamped)});
    }

    private Component formatLooseImmortalDuration(long ticks) {
        long remainingTicks = Math.max(0L, ticks);
        if (remainingTicks >= 24000L) {
            long years = Math.max(1L, (remainingTicks + 24000L - 1L) / 24000L);
            return Component.translatable((String)"screen.friday_cultivation.duration.years", (Object[])new Object[]{years});
        }
        long totalSeconds = Math.max(0L, (remainingTicks + 19L) / 20L);
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        return Component.translatable((String)"screen.friday_cultivation.duration.minutes_seconds", (Object[])new Object[]{minutes, seconds});
    }

    private int renderFoundationBreakthroughOptions(GuiGraphics gfx, int x, int rightX, int y, CultivationData data, int boneAge, int mouseX, int mouseY) {
        for (FoundationDao dao : FOUNDATION_BREAKTHROUGH_OPTIONS) {
            boolean ready = data.isEligibleFoundationDao(dao, boneAge);
            BreakthroughRouteRow row = this.buildFoundationRequirementRow(dao, data, boneAge);
            y = this.renderBreakthroughOptionRow(gfx, x, rightX, y, row, ready, this.selectedFoundationDao == dao, false, true, this.foundationRouteIcon(dao), 1, dao.ordinal(), mouseX, mouseY);
        }
        return y + 2;
    }

    private int renderGoldenCoreBreakthroughOptions(GuiGraphics gfx, int x, int rightX, int y, CultivationData data, int boneAge, boolean hasBloodTalisman, int mouseX, int mouseY) {
        for (GoldenCoreDao dao : GOLDEN_CORE_BREAKTHROUGH_OPTIONS) {
            boolean ready = data.isEligibleGoldenCoreDao(dao, boneAge, hasBloodTalisman);
            boolean selectable = data.isFoundationAllowedForGoldenCore(dao, hasBloodTalisman);
            BreakthroughRouteRow row = this.buildGoldenCoreRequirementRow(dao, data, boneAge);
            y = this.renderBreakthroughOptionRow(gfx, x, rightX, y, row, ready, selectable && this.selectedGoldenCoreDao == dao, !selectable, selectable, this.goldenCoreRouteIcon(dao), 2, dao.ordinal(), mouseX, mouseY);
        }
        return y + 2;
    }

    private int renderBreakthroughHistoryButton(GuiGraphics gfx, int x, int rightX, int y, CultivationData data, int mouseX, int mouseY) {
        int rowX = x + 4;
        int rowRight = rightX - 4;
        int rowH = 18;
        boolean hover = mouseX >= rowX && mouseX < rowRight && mouseY >= y && mouseY < y + rowH;
        this.breakthroughHistoryButtonRect = new int[]{rowX, y, rowRight, y + rowH};
        gfx.fill(rowX, y, rowRight, y + rowH, -15067628);
        gfx.fill(rowX + 1, y + 1, rowRight - 1, y + rowH - 1, hover ? -1517128 : -2571110);
        gfx.fill(rowX + 2, y + 2, rowRight - 2, y + 3, hover ? -10496 : -2504802);
        this.drawBreakthroughHistoryButtonText(gfx, this.breakthroughHistorySummary(data), (rowX + rowRight) / 2, y + 5, rowRight - rowX - 8, hover ? -4703686 : -12950192);
        return y + rowH + 5;
    }

    private int renderCreateImperialArtButton(GuiGraphics gfx, int x, int rightX, int y, int mouseX, int mouseY) {
        int rowX = x + 4;
        int rowRight = rightX - 4;
        int rowH = 18;
        boolean hover = mouseX >= rowX && mouseX < rowRight && mouseY >= y && mouseY < y + rowH;
        this.createImperialArtButtonRect = new int[]{rowX, y, rowRight, y + rowH};
        gfx.fill(rowX, y, rowRight, y + rowH, -15067628);
        gfx.fill(rowX + 1, y + 1, rowRight - 1, y + rowH - 1, hover ? -1517128 : -2571110);
        gfx.fill(rowX + 2, y + 2, rowRight - 2, y + 3, hover ? -10496 : -2504802);
        this.drawBreakthroughHistoryButtonText(gfx, (Component)Component.translatable((String)"screen.friday_cultivation.breakthrough.create_imperial_art"), (rowX + rowRight) / 2, y + 5, rowRight - rowX - 8, hover ? -4703686 : -12950192);
        return y + rowH + 5;
    }

    private BreakthroughRouteRow buildFoundationRequirementRow(FoundationDao dao, CultivationData data, int boneAge) {
        ArrayList<RequirementPart> parts = new ArrayList<RequirementPart>();
        switch (dao) {
            case HUMAN: {
                parts.add(this.requirementPart("foundation.zhuji_dan_1", data.getZhujiDanEaten() >= 1));
                break;
            }
            case BLOOD: {
                parts.add(this.requirementPart("foundation.blood_spirit_pill_3", data.getBloodPillEaten() >= 3));
                break;
            }
            case EARTH: {
                parts.add(this.requirementPart("foundation.zhuji_dan_6", data.getZhujiDanEaten() >= 6));
                parts.add(this.requirementPart("foundation.foundation_secret", data.isZhujiSecretUsed()));
                break;
            }
            case HEAVEN: {
                parts.add(this.requirementPart("foundation.zhuji_dan_9", data.getZhujiDanEaten() >= 9));
                parts.add(this.requirementPart("foundation.dao_foundation_fruit_1", data.getDaoFruitEaten() >= 1));
                parts.add(this.requirementPart("foundation.foundation_secret", data.isZhujiSecretUsed()));
                parts.add(this.requirementPart("foundation.bone_age_under_21", boneAge < 21));
                break;
            }
        }
        return new BreakthroughRouteRow((Component)Component.translatable((String)dao.translationKey()), parts);
    }

    private BreakthroughRouteRow buildGoldenCoreRequirementRow(GoldenCoreDao dao, CultivationData data, int boneAge) {
        ArrayList<RequirementPart> parts = new ArrayList<RequirementPart>();
        switch (dao) {
            case HUMAN: {
                parts.add(this.requirementPart("golden_core.jiedan_pill_1", data.getJiedanPillUsed() >= 1));
                break;
            }
            case BLOOD: {
                parts.add(this.requirementPart("golden_core.blood_jiedan_pill_3", data.getBloodJiedanPillUsed() >= 3));
                parts.add(this.requirementPart("golden_core.all_creatures_true_blood_1", data.getTrueBloodUsed() >= 1));
                break;
            }
            case EARTH: {
                parts.add(this.requirementPart("golden_core.jiedan_pill_6", data.getJiedanPillUsed() >= 6));
                parts.add(this.requirementPart("golden_core.earth_evil_qi_1", data.getEarthEvilQiUsed() >= 1));
                break;
            }
            case HEAVEN: {
                parts.add(this.requirementPart("golden_core.jiedan_pill_9", data.getJiedanPillUsed() >= 9));
                parts.add(this.requirementPart("golden_core.heaven_clear_qi_1", data.getHeavenClearQiUsed() >= 1));
                parts.add(this.requirementPart("golden_core.ningzhen_creation_fruit_1", data.getCreationFruitEaten() >= 1));
                parts.add(this.requirementPart("golden_core.bone_age_under_60", boneAge < 60));
                break;
            }
        }
        return new BreakthroughRouteRow((Component)Component.translatable((String)dao.translationKey()), parts);
    }

    private RequirementPart requirementPart(String suffix, boolean met) {
        MutableComponent part = Component.translatable((String)("screen.friday_cultivation.breakthrough.requirement." + suffix));
        return new RequirementPart((Component)(met ? part.withStyle(JADE_REQUIREMENT_STYLE) : part));
    }

    private int renderBreakthroughOptionRow(GuiGraphics gfx, int x, int rightX, int y, BreakthroughRouteRow row, boolean ready, boolean selected, boolean strike, boolean selectable, ItemStack icon, int kind, int ordinal, int mouseX, int mouseY) {
        boolean hover;
        int rowX = x + 3;
        int rowRight = rightX - 3;
        int textMaxW = Math.max(24, rowRight - rowX - 12 - 3 - 4);
        List<BreakthroughVisualLine> lines = this.wrapBreakthroughRow(row, ready, textMaxW);
        int lineH = this.breakthroughRowLineHeight();
        int rowH = Math.max(15, lines.size() * lineH + 4);
        boolean bl = hover = mouseX >= rowX && mouseX < rowRight && mouseY >= y && mouseY < y + rowH;
        if (hover) {
            this.hoveredBreakthroughOptionKind = kind;
            this.hoveredBreakthroughOptionOrdinal = ordinal;
        }
        if (selected || hover && selectable) {
            int bg = selected ? 617194058 : 0x18FFFFFF;
            gfx.fill(rowX, y, rowRight, y + rowH, bg);
            gfx.fill(rowX, y, rowRight, y + 1, selected ? -3562934 : -2504802);
            gfx.fill(rowX, y + rowH - 1, rowRight, y + rowH, selected ? -3562934 : -2504802);
        } else if (hover) {
            gfx.fill(rowX, y, rowRight, y + rowH, 0x10FFFFFF);
        }
        int maxLineW = 0;
        for (BreakthroughVisualLine line : lines) {
            maxLineW = Math.max(maxLineW, this.scaledBreakthroughWidth(line.rawWidth()));
        }
        int groupW = 15 + maxLineW;
        int groupX = (rowX + rowRight - groupW) / 2;
        int iconY = y + (rowH - 12) / 2;
        this.drawBreakthroughIcon(gfx, icon, groupX, iconY);
        if (!selectable) {
            gfx.fill(groupX, iconY, groupX + 12, iconY + 12, 1894308280);
        }
        int textX = groupX + 12 + 3;
        int textY = y + (rowH - lines.size() * lineH) / 2;
        int color = ready ? -14718150 : (strike ? -7570576 : -12766422);
        for (int i = 0; i < lines.size(); ++i) {
            BreakthroughVisualLine line = lines.get(i);
            int lineY = textY + i * lineH;
            this.drawBreakthroughSegments(gfx, line.segments(), textX, lineY, color);
            if (!strike) continue;
            int lineW = this.scaledBreakthroughWidth(line.rawWidth());
            int strikeY = lineY + this.breakthroughStrikeOffset();
            gfx.fill(textX, strikeY, textX + lineW, strikeY + 1, color);
        }
        this.breakthroughOptionRects.add(new int[]{rowX, y, rowRight, y + rowH, kind, ordinal, selectable ? 1 : 0});
        return y + rowH;
    }

    private List<BreakthroughVisualLine> wrapBreakthroughRow(BreakthroughRouteRow row, boolean ready, int maxTextW) {
        int rawMaxW = Math.max(1, (int)((float)maxTextW / 0.7f));
        MutableComponent separator = Component.translatable((String)"screen.friday_cultivation.breakthrough.requirement.separator");
        int separatorW = this.font.width((FormattedText)separator);
        ArrayList<BreakthroughVisualLine> lines = new ArrayList<BreakthroughVisualLine>();
        ArrayList<Component> current = new ArrayList<Component>();
        int currentW = 0;
        boolean hasRequirementOnLine = false;
        MutableComponent prefix = Component.translatable((String)"screen.friday_cultivation.breakthrough.requirement.row", (Object[])new Object[]{row.routeName(), Component.empty()});
        current.add(prefix);
        currentW += this.font.width((FormattedText)prefix);
        for (RequirementPart requirement : row.requirements()) {
            Component text = requirement.text();
            int textW = this.font.width((FormattedText)text);
            boolean needsSeparator = hasRequirementOnLine;
            int addW = (needsSeparator ? separatorW : 0) + textW;
            if (!current.isEmpty() && currentW + addW > rawMaxW) {
                lines.add(new BreakthroughVisualLine(new ArrayList<Component>(current), currentW));
                current.clear();
                currentW = 0;
                hasRequirementOnLine = false;
                needsSeparator = false;
            }
            if (needsSeparator) {
                current.add(separator);
                currentW += separatorW;
            }
            current.add(text);
            currentW += textW;
            hasRequirementOnLine = true;
        }
        if (ready) {
            MutableComponent marker = Component.empty().append((Component)Component.literal((String)" ")).append((Component)Component.translatable((String)"screen.friday_cultivation.breakthrough.met"));
            int markerW = this.font.width((FormattedText)marker);
            if (!current.isEmpty() && currentW + markerW > rawMaxW) {
                lines.add(new BreakthroughVisualLine(new ArrayList<Component>(current), currentW));
                current.clear();
                currentW = 0;
            }
            current.add(marker);
            currentW += markerW;
        }
        if (!current.isEmpty()) {
            lines.add(new BreakthroughVisualLine(new ArrayList<Component>(current), currentW));
        }
        return lines.isEmpty() ? List.of(new BreakthroughVisualLine(List.of(row.routeName()), this.font.width((FormattedText)row.routeName()))) : lines;
    }

    private int breakthroughRowLineHeight() {
        Objects.requireNonNull(this.font);
        return (int)Math.ceil(9.0f * 0.7f) + 1;
    }

    private int breakthroughStrikeOffset() {
        Objects.requireNonNull(this.font);
        return Math.max(3, Math.round((float)(9 - 1) * 0.7f * 0.5f));
    }

    private int scaledBreakthroughWidth(int rawWidth) {
        return (int)Math.ceil((float)rawWidth * 0.7f);
    }

    private void drawBreakthroughSegments(GuiGraphics gfx, List<Component> segments, int x, int y, int color) {
        gfx.pose().pushPose();
        gfx.pose().translate((float)x, (float)y, 0.0f);
        gfx.pose().scale(0.7f, 0.7f, 1.0f);
        int cursor = 0;
        for (Component segment : segments) {
            gfx.drawString(this.font, segment, cursor, 0, color, false);
            cursor += this.font.width((FormattedText)segment);
        }
        gfx.pose().popPose();
    }

    private void drawBreakthroughIcon(GuiGraphics gfx, ItemStack icon, int x, int y) {
        float scale = 0.75f;
        gfx.pose().pushPose();
        gfx.pose().translate((float)x, (float)y, 120.0f);
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.renderItem(icon, 0, 0);
        gfx.pose().popPose();
    }

    private ItemStack foundationRouteIcon(FoundationDao dao) {
        return switch (dao) {
            case BLOOD -> new ItemStack((ItemLike)ModItems.BLOOD_SPIRIT_PILL.get());
            case EARTH -> new ItemStack((ItemLike)ModItems.FOUNDATION_SECRET.get());
            case HEAVEN -> new ItemStack((ItemLike)ModItems.DAO_FOUNDATION_FRUIT.get());
            default -> new ItemStack((ItemLike)ModItems.ZHUJI_DAN.get());
        };
    }

    private ItemStack goldenCoreRouteIcon(GoldenCoreDao dao) {
        return switch (dao) {
            case BLOOD -> new ItemStack((ItemLike)ModItems.BLOOD_JIEDAN_PILL.get());
            case EARTH -> new ItemStack((ItemLike)ModItems.EARTH_EVIL_QI.get());
            case HEAVEN -> new ItemStack((ItemLike)ModItems.NINGZHEN_CREATION_FRUIT.get());
            default -> new ItemStack((ItemLike)ModItems.JIEDAN_PILL.get());
        };
    }

    private Component breakthroughHistorySummary(CultivationData data) {
        List<Component> names = this.breakthroughHistoryRouteNames(data);
        Component value = names.isEmpty() ? Component.translatable("screen.friday_cultivation.breakthrough.history_missing") : this.joinBreakthroughHistoryNames(names);
        return Component.translatable((String)"screen.friday_cultivation.breakthrough.history_button", (Object[])new Object[]{value});
    }

    private List<Component> breakthroughHistoryRouteNames(CultivationData data) {
        GoldenCoreDao goldenCoreDao;
        ArrayList<Component> names = new ArrayList<Component>();
        FoundationDao foundationDao = data.getFoundationDao();
        if (foundationDao != null && foundationDao != FoundationDao.NONE) {
            names.add((Component)Component.translatable((String)foundationDao.translationKey()));
        }
        if ((goldenCoreDao = data.getGoldenCoreDao()) != null && goldenCoreDao != GoldenCoreDao.NONE) {
            names.add((Component)Component.translatable((String)goldenCoreDao.translationKey()));
        }
        return names;
    }

    private Component joinBreakthroughHistoryNames(List<Component> names) {
        MutableComponent result = Component.empty();
        MutableComponent separator = Component.translatable((String)"screen.friday_cultivation.breakthrough.requirement.separator");
        for (int i = 0; i < names.size(); ++i) {
            if (i > 0) {
                result.append((Component)separator);
            }
            result.append(names.get(i));
        }
        return result;
    }

    private void renderBreakthroughHistoryPopup(GuiGraphics gfx, CultivationData data, int mouseX, int mouseY) {
        List<Component> routeNames = this.breakthroughHistoryRouteNames(data);
        int popupW = Math.max(180, Math.min(292, this.width - 44));
        int contentW = popupW - 28;
        float scale = CultivationScreen.effectiveTextScale() * 0.82f;
        ArrayList<HistoryPopupLine> lines = new ArrayList<HistoryPopupLine>();
        if (routeNames.isEmpty()) {
            lines.add(new HistoryPopupLine((Component)Component.translatable((String)"screen.friday_cultivation.breakthrough.history_missing"), -9807288, scale));
        } else {
            Object goldenCoreDao;
            FoundationDao foundationDao = data.getFoundationDao();
            if (foundationDao != null && foundationDao != FoundationDao.NONE) {
                lines.add(new HistoryPopupLine((Component)Component.translatable((String)"screen.friday_cultivation.breakthrough.history_foundation", (Object[])new Object[]{Component.translatable((String)foundationDao.translationKey())}), -12950192, scale));
                lines.add(new HistoryPopupLine((Component)Component.translatable((String)("tooltip.friday_cultivation.foundation." + foundationDao.id())), -9807288, scale));
            }
            if ((goldenCoreDao = data.getGoldenCoreDao()) != null && goldenCoreDao != GoldenCoreDao.NONE) {
                lines.add(new HistoryPopupLine((Component)Component.translatable((String)"screen.friday_cultivation.breakthrough.history_golden_core", (Object[])new Object[]{Component.translatable((String)((GoldenCoreDao)((Object)goldenCoreDao)).translationKey())}), -12950192, scale));
                lines.add(new HistoryPopupLine((Component)Component.translatable((String)((GoldenCoreDao)((Object)goldenCoreDao)).tooltipKey()), -9807288, scale));
            }
        }
        int bodyH = 0;
        for (HistoryPopupLine line : lines) {
            bodyH += this.wrappedHistoryLineHeight(line.text(), contentW, line.scale()) + 4;
        }
        int popupH = Math.min(this.height - 24, Math.max(82, bodyH + 36));
        int popupX = (this.width - popupW) / 2;
        int popupY = (this.height - popupH) / 2;
        this.breakthroughHistoryPopupRect = new int[]{popupX, popupY, popupX + popupW, popupY + popupH};
        this.breakthroughHistoryPopupCloseRect = new int[]{popupX + popupW - 18, popupY + 6, popupX + popupW - 7, popupY + 17};
        gfx.pose().pushPose();
        gfx.pose().translate(0.0f, 0.0f, 360.0f);
        gfx.fill(0, 0, this.width, this.height, 0x7A000000);
        this.drawHardShadow(gfx, popupX, popupY, popupW, popupH, 4);
        gfx.fill(popupX - 2, popupY - 2, popupX + popupW + 2, popupY + popupH + 2, -10859978);
        gfx.fill(popupX, popupY, popupX + popupW, popupY + popupH, -923956);
        this.drawPanelFrame(gfx, popupX, popupY, popupW, popupH);
        this.drawSmallCentered(gfx, (Component)Component.translatable((String)"screen.friday_cultivation.breakthrough.history_title"), popupX + popupW / 2, popupY + 10, -4703686);
        this.drawPopupCloseIcon(gfx, this.breakthroughHistoryPopupCloseRect, mouseX, mouseY);
        int lineY = popupY + 28;
        int bottom = popupY + popupH - 10;
        for (HistoryPopupLine line : lines) {
            if (lineY >= bottom) break;
            int used = this.drawHistoryWrappedLine(gfx, line.text(), popupX + 14, lineY, contentW, line.color(), line.scale(), bottom);
            lineY += used + 4;
        }
        gfx.pose().popPose();
    }

    private int wrappedHistoryLineHeight(Component text, int maxW, float scale) {
        int rawMaxW = Math.max(1, (int)((float)maxW / scale));
        List<FormattedCharSequence> wrapped = this.font.split((FormattedText)text, rawMaxW);
        Objects.requireNonNull(this.font);
        int lineH = (int)Math.ceil(9.0f * scale) + 2;
        return Math.max(lineH, wrapped.size() * lineH);
    }

    private int drawHistoryWrappedLine(GuiGraphics gfx, Component text, int x, int y, int maxW, int color, float scale, int bottom) {
        int rawMaxW = Math.max(1, (int)((float)maxW / scale));
        List<FormattedCharSequence> wrapped = this.font.split((FormattedText)text, rawMaxW);
        Objects.requireNonNull(this.font);
        int lineH = (int)Math.ceil(9.0f * scale) + 2;
        int drawn = 0;
        for (FormattedCharSequence line : wrapped) {
            if (y + drawn + lineH > bottom) break;
            gfx.pose().pushPose();
            gfx.pose().translate((float)x, (float)(y + drawn), 0.0f);
            gfx.pose().scale(scale, scale, 1.0f);
            gfx.drawString(this.font, line, 0, 0, color, false);
            gfx.pose().popPose();
            drawn += lineH;
        }
        return Math.max(lineH, drawn);
    }

    private void drawPopupCloseIcon(GuiGraphics gfx, int[] rect, int mouseX, int mouseY) {
        int color;
        boolean hover = mouseX >= rect[0] && mouseX < rect[2] && mouseY >= rect[1] && mouseY < rect[3];
        int n = color = hover ? -4703686 : -15067628;
        if (hover) {
            gfx.fill(rect[0] - 1, rect[1] - 1, rect[2] + 1, rect[3] + 1, 548944442);
        }
        int x = rect[0] + 2;
        int y = rect[1] + 2;
        int size = Math.min(rect[2] - rect[0], rect[3] - rect[1]) - 4;
        for (int i = 0; i < size; ++i) {
            gfx.fill(x + i, y + i, x + i + 2, y + i + 2, color);
            gfx.fill(x + size - 1 - i, y + i, x + size + 1 - i, y + i + 2, color);
        }
    }

    private Component breakthroughHint(CultivationData data, int boneAge, boolean hasBloodTalisman) {
        Realm realm = data.getRealm();
        SubStage sub = data.getSubStage();
        if (realm == Realm.MORTAL) {
            return Component.translatable((String)"screen.friday_cultivation.breakthrough.hint_mortal");
        }
        if (realm == Realm.QI_REFINING && sub.isPeakFor(realm)) {
            int waves = this.foundationTribulationWaves(this.selectedFoundationDao);
            int damage = waves > 0 ? Realm.QI_REFINING.tribulationStrikeDamage() : 0;
            return Component.translatable((String)"screen.friday_cultivation.breakthrough.route_hint_foundation", (Object[])new Object[]{Component.translatable((String)this.selectedFoundationDao.translationKey()), Realm.formatTribulationCount(waves, 1), damage});
        }
        if (realm == Realm.FOUNDATION_BUILDING && sub.isPeakFor(realm)) {
            return Component.translatable((String)"screen.friday_cultivation.breakthrough.route_hint_golden_core", (Object[])new Object[]{Component.translatable((String)this.selectedGoldenCoreDao.translationKey()), Realm.formatTribulationCount(this.selectedGoldenCoreDao.tribulationStrikes(), 1), this.selectedGoldenCoreDao.tribulationDamage()});
        }
        int strikes = realm.tribulationCount(sub);
        int boltsPerWave = realm.tribulationBoltsPerWave(sub);
        int damage = realm.tribulationStrikeDamage();
        return Component.translatable((String)"screen.friday_cultivation.breakthrough.hint_tribulation", (Object[])new Object[]{Realm.formatTribulationCount(strikes, boltsPerWave), damage});
    }

    private void drawBreakthroughCentered(GuiGraphics gfx, Component text, int cx, int y, int maxW, int color, boolean strike) {
        float scale = CultivationScreen.effectiveTextScale() * 0.8f;
        int rawW = this.font.width((FormattedText)text);
        if ((float)rawW * scale > (float)maxW && rawW > 0) {
            scale = Math.max(0.5f, (float)maxW / (float)rawW);
        }
        int drawW = (int)((float)rawW * scale);
        int drawX = cx - drawW / 2;
        gfx.pose().pushPose();
        gfx.pose().translate((float)drawX, (float)y, 0.0f);
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.drawString(this.font, text, 0, 0, color, false);
        gfx.pose().popPose();
        if (strike) {
            Objects.requireNonNull(this.font);
            int lineY = y + Math.max(4, (int)(9.0f * scale / 2.0f));
            gfx.fill(drawX, lineY, drawX + drawW, lineY + 1, color);
        }
    }

    private void drawBreakthroughHistoryButtonText(GuiGraphics gfx, Component text, int cx, int y, int maxW, int color) {
        float scale = CultivationScreen.effectiveTextScale() * 0.78f;
        int rawW = this.font.width((FormattedText)text);
        if ((float)rawW * scale > (float)maxW && rawW > 0) {
            scale = Math.max(0.38f, (float)maxW / (float)rawW);
        }
        int drawW = (int)((float)rawW * scale);
        int drawX = cx - drawW / 2;
        gfx.pose().pushPose();
        gfx.pose().translate((float)drawX, (float)y, 0.0f);
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.drawString(this.font, text, 0, 0, color, false);
        gfx.pose().popPose();
    }

    private void normalizeBreakthroughSelection(CultivationData data, LocalPlayer player) {
        if (this.selectedFoundationDao == null || this.selectedFoundationDao == FoundationDao.NONE) {
            this.selectedFoundationDao = FoundationDao.HUMAN;
        }
        if (this.selectedGoldenCoreDao == null || this.selectedGoldenCoreDao == GoldenCoreDao.NONE) {
            this.selectedGoldenCoreDao = GoldenCoreDao.HUMAN;
        }
        if (!this.canChooseFoundationRoute(data)) {
            this.selectedFoundationDao = FoundationDao.HUMAN;
        }
        if (!this.canChooseGoldenCoreRoute(data)) {
            this.selectedGoldenCoreDao = GoldenCoreDao.HUMAN;
        } else {
            boolean hasBloodTalisman = this.hasBloodTransformationTalisman(player);
            if (!data.isFoundationAllowedForGoldenCore(this.selectedGoldenCoreDao, hasBloodTalisman)) {
                this.selectedGoldenCoreDao = this.firstSelectableGoldenCoreDao(data, hasBloodTalisman);
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
        if (data.getRealm() == Realm.QI_REFINING && data.getSubStage().isPeakFor(Realm.QI_REFINING)) {
            return true;
        }
        if (data.getRealm() == Realm.FOUNDATION_BUILDING && data.getSubStage().isPeakFor(Realm.FOUNDATION_BUILDING)) {
            return data.isEligibleGoldenCoreDao(this.selectedGoldenCoreDao, LifespanHelper.displayBoneAge(data), this.hasBloodTransformationTalisman(player));
        }
        if (data.getRealm() == Realm.TRUE_IMMORTAL && data.getSubStage().isPeakFor(Realm.TRUE_IMMORTAL)) {
            return data.canBreakthroughToGreatEmperor();
        }
        return true;
    }

    private boolean hasBloodTransformationTalisman(LocalPlayer player) {
        if (player == null) {
            return false;
        }
        return player.getInventory().contains(new ItemStack((ItemLike)ModItems.BLOOD_TRANSFORMATION_TALISMAN.get()));
    }

    private boolean handleBreakthroughOptionClick(double mouseX, double mouseY, int button) {
        if (button != 0 || this.currentTab != Tab.BREAKTHROUGH) {
            return false;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        CultivationData data = player == null ? null : (CultivationData)CultivationCapability.get((Player)player).orElse(null);
        for (int[] rect : this.breakthroughOptionRects) {
            if (mouseX < (double)rect[0] || mouseX >= (double)rect[2] || mouseY < (double)rect[1] || mouseY >= (double)rect[3]) continue;
            if (rect.length > 6 && rect[6] == 0) {
                return true;
            }
            if (rect[4] == 1) {
                if (!this.canChooseFoundationRoute(data)) {
                    return true;
                }
                this.selectedFoundationDao = FoundationDao.values()[rect[5]];
            } else if (rect[4] == 2) {
                if (!this.canChooseGoldenCoreRoute(data)) {
                    return true;
                }
                this.selectedGoldenCoreDao = GoldenCoreDao.values()[rect[5]];
            }
            this.playUiClick();
            return true;
        }
        return false;
    }

    private boolean handleBreakthroughHistoryClick(double mouseX, double mouseY, int button) {
        if (this.breakthroughHistoryPopupOpen) {
            if (button == 0 && (this.isInsideRect(this.breakthroughHistoryPopupCloseRect, mouseX, mouseY) || !this.isInsideRect(this.breakthroughHistoryPopupRect, mouseX, mouseY))) {
                this.breakthroughHistoryPopupOpen = false;
                this.playUiClick();
            }
            return true;
        }
        if (button != 0) {
            return false;
        }
        if (this.currentTab == Tab.BREAKTHROUGH && this.isInsideRect(this.breakthroughHistoryButtonRect, mouseX, mouseY)) {
            this.breakthroughHistoryPopupOpen = true;
            this.playUiClick();
            return true;
        }
        return false;
    }

    private boolean isInsideRect(int[] rect, double mouseX, double mouseY) {
        return rect != null && mouseX >= (double)rect[0] && mouseX < (double)rect[2] && mouseY >= (double)rect[1] && mouseY < (double)rect[3];
    }

    private void submitBreakthroughRequest() {
        LocalPlayer player = Minecraft.getInstance().player;
        CultivationData data = player == null ? null : (CultivationData)CultivationCapability.get((Player)player).orElse(null);
        FoundationDao foundationDao = this.canChooseFoundationRoute(data) ? this.selectedFoundationDao : FoundationDao.NONE;
        GoldenCoreDao goldenCoreDao = this.canChooseGoldenCoreRoute(data) ? this.selectedGoldenCoreDao : GoldenCoreDao.NONE;
        ModNetwork.CHANNEL.sendToServer((Object)new RequestBreakthroughPacket(foundationDao, goldenCoreDao));
    }

    private boolean canChooseFoundationRoute(CultivationData data) {
        return data != null && data.getRealm() == Realm.QI_REFINING && data.getSubStage().isPeakFor(Realm.QI_REFINING);
    }

    private boolean canChooseGoldenCoreRoute(CultivationData data) {
        return data != null && data.getRealm() == Realm.FOUNDATION_BUILDING && data.getSubStage().isPeakFor(Realm.FOUNDATION_BUILDING);
    }

    private void renderBreakthroughTooltip(GuiGraphics gfx, int mouseX, int mouseY) {
        if (this.currentTab != Tab.BREAKTHROUGH || this.hoveredBreakthroughOptionOrdinal < 0) {
            return;
        }
        ArrayList<MutableComponent> lines = new ArrayList<MutableComponent>();
        if (this.hoveredBreakthroughOptionKind == 1) {
            FoundationDao dao = FoundationDao.values()[this.hoveredBreakthroughOptionOrdinal];
            lines.add(Component.translatable((String)dao.translationKey()).copy().withStyle(ChatFormatting.GOLD));
            lines.add(Component.translatable((String)("tooltip.friday_cultivation.foundation." + dao.id())).copy().withStyle(ChatFormatting.GRAY));
        } else if (this.hoveredBreakthroughOptionKind == 2) {
            GoldenCoreDao dao = GoldenCoreDao.values()[this.hoveredBreakthroughOptionOrdinal];
            lines.add(Component.translatable((String)dao.translationKey()).copy().withStyle(ChatFormatting.GOLD));
            lines.add(Component.translatable((String)dao.tooltipKey()).copy().withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable((String)"tooltip.friday_cultivation.golden_core.tribulation", (Object[])new Object[]{Realm.formatTribulationCount(dao.tribulationStrikes(), 1), dao.tribulationDamage()}).copy().withStyle(ChatFormatting.DARK_GRAY));
        }
        if (!lines.isEmpty()) {
            gfx.renderComponentTooltip(this.font, new java.util.ArrayList<net.minecraft.network.chat.Component>(lines), mouseX, mouseY);
        }
    }

    private int foundationTribulationWaves(FoundationDao dao) {
        return switch (dao) {
            case EARTH -> 1;
            case HEAVEN -> 3;
            default -> 0;
        };
    }

    private void renderAttributesTab(GuiGraphics gfx, int x, int y, CultivationData data, int mouseX, int mouseY) {
        double rootMult;
        int contentTop = y;
        Technique eq = Technique.byId(data.getEquippedTechniqueId());
        Technique.Bonus tb = eq == null ? Technique.Bonus.NONE : eq.bonus();
        int rightX = x + 160 - 24;
        LocalPlayer pp = Minecraft.getInstance().player;
        boolean meleeBonusEnabled = data.isBonusCategoryEnabled(CultivationBonusCategory.MELEE_DAMAGE);
        int zyAtk = !meleeBonusEnabled || pp == null ? 0 : ZhenyuanBonusHelper.physiqueAttackBonus((Player)pp);
        int baseAttackDisplay = meleeBonusEnabled ? data.getAttack() : 0;
        int totalAtkBonus = meleeBonusEnabled ? tb.attack + zyAtk : 0;
        y = this.drawStatRow(gfx, x, rightX, y, "attack", CultivationScreen.formatBonus(baseAttackDisplay, totalAtkBonus), totalAtkBonus > 0);
        y = this.drawBodyDefenseRow(gfx, x, rightX, y, data, pp);
        boolean immortalIncantation = pp != null && TechniqueBonusHelper.isImmortalIncantationActive((Player)pp);
        double realmBase = PlayerQiAbsorptionHelper.baseAbsorbMultiplier(data);
        double d = rootMult = pp == null ? 1.0 : SpiritRootBonusHelper.qiAbsorptionMultiplier((Player)pp);
        double multFromTechniqueAndSpell = pp == null ? tb.qiAbsorbMult * (immortalIncantation ? 10.0 : 1.0) : TechniqueBonusHelper.qiAbsorbMultiplier((Player)pp);
        boolean ghostDaoInDifu = pp != null && data.isSoulState() && eq != null && eq.isGhostDao() && pp.level().dimension() == ModDimensions.DIFU;
        int efficiency = ghostDaoInDifu ? 10 : PlayerQiConsumer.cultivationEfficiencyPerParticle((Player)pp, data, QiElement.PURE);
        int qiRecovery = PlayerQiConsumer.nominalQiRecoveryPerSecond((Player)pp, data, QiElement.PURE);
        if (ghostDaoInDifu) {
            qiRecovery += 10;
        }
        boolean cultivationEfficiencyEnabled = data.isBonusCategoryEnabled(CultivationBonusCategory.CULTIVATION_EFFICIENCY);
        int efficiencyRowY = y;
        y = this.drawStatRow(gfx, x, rightX, y, "cultivation_efficiency", CultivationScreen.formatCultivationEfficiency(efficiency), cultivationEfficiencyEnabled && (multFromTechniqueAndSpell > 1.0 || rootMult != 1.0 || data.isMeditating()));
        y = this.drawStatRow(gfx, x, rightX, y, "qi_recovery", CultivationScreen.formatQiRecoveryPerSecond(qiRecovery), qiRecovery > 0);
        y = this.drawStatRow(gfx, x, rightX, y, "refining", CultivationScreen.formatRefiningRank(data), false);
        y = this.drawStatRow(gfx, x, rightX, y, "alchemy", CultivationScreen.formatAlchemyRank(data), false);
        this.renderZhenyuanRadar(gfx, x, rightX, y += 2, data, mouseX, mouseY);
        this.renderBonusSettingsButton(gfx, x, contentTop + 200 - 58, mouseX, mouseY);
        if (mouseX >= x && mouseX < rightX && mouseY >= efficiencyRowY && mouseY < efficiencyRowY + 10 && cultivationEfficiencyEnabled && (multFromTechniqueAndSpell > 1.0 || rootMult != 1.0 || data.isMeditating())) {
            this.renderCultivationEfficiencyTooltip(gfx, mouseX, mouseY, data, realmBase, rootMult, multFromTechniqueAndSpell, data.isMeditating(), efficiency, ghostDaoInDifu);
        }
    }

    private void renderCultivationEfficiencyTooltip(GuiGraphics gfx, int mouseX, int mouseY, CultivationData data, double realmBase, double rootMult, double multTechniqueAndSpell, boolean meditating, int efficiency, boolean ghostDaoInDifu) {
        ArrayList<MutableComponent> lines = new ArrayList<MutableComponent>();
        lines.add(Component.translatable((String)"screen.friday_cultivation.attr.cultivation_efficiency_label").copy().withStyle(ChatFormatting.GOLD));
        if (ghostDaoInDifu) {
            lines.add(Component.translatable((String)"screen.friday_cultivation.attr.cultivation_efficiency_tooltip.ghost_dao", (Object[])new Object[]{efficiency}).copy().withStyle(ChatFormatting.GRAY));
            gfx.renderComponentTooltip(this.font, new java.util.ArrayList<net.minecraft.network.chat.Component>(lines), mouseX, mouseY);
            return;
        }
        Component realmName = data.getRealm().displayName();
        lines.add(Component.translatable((String)"screen.friday_cultivation.attr.cultivation_efficiency_tooltip.realm", (Object[])new Object[]{realmName, String.format("%.0f", realmBase)}).copy().withStyle(ChatFormatting.GRAY));
        if (rootMult != 1.0) {
            lines.add(Component.translatable((String)"screen.friday_cultivation.attr.cultivation_efficiency_tooltip.root", (Object[])new Object[]{String.format("%.1f", rootMult)}).copy().withStyle(ChatFormatting.GRAY));
        }
        if (multTechniqueAndSpell > 1.0) {
            lines.add(Component.translatable((String)"screen.friday_cultivation.attr.cultivation_efficiency_tooltip.tech", (Object[])new Object[]{String.format("%.1f", multTechniqueAndSpell)}).copy().withStyle(ChatFormatting.GRAY));
        }
        if (meditating) {
            lines.add(Component.translatable((String)"screen.friday_cultivation.attr.cultivation_efficiency_tooltip.meditation", (Object[])new Object[]{String.format("%.0f", 10.0)}).copy().withStyle(ChatFormatting.GRAY));
        }
        lines.add(Component.translatable((String)"screen.friday_cultivation.attr.cultivation_efficiency_tooltip.final", (Object[])new Object[]{efficiency}).copy().withStyle(ChatFormatting.WHITE));
        gfx.renderComponentTooltip(this.font, new java.util.ArrayList<net.minecraft.network.chat.Component>(lines), mouseX, mouseY);
    }

    private int drawStatRow(GuiGraphics gfx, int xLeft, int xRight, int y, String key, String value, boolean hasBonus) {
        int rowH = 10;
        MutableComponent label = Component.translatable((String)("screen.friday_cultivation.attr." + key + "_label"));
        this.drawAttrSmall(gfx, (Component)label, xLeft, y + 1, -12766422);
        MutableComponent val = Component.literal((String)value);
        int valW = (int)((float)this.font.width((FormattedText)val) * this.attrTabScale());
        this.drawAttrSmall(gfx, (Component)val, xRight - valW, y + 1, hasBonus ? -12950192 : -15067628);
        this.drawDottedHLine(gfx, xLeft, y + rowH - 1, xRight, -2006295992);
        return y + rowH;
    }

    private int drawBodyDefenseRow(GuiGraphics gfx, int xLeft, int xRight, int y, CultivationData data, LocalPlayer pp) {
        int rowH = 10;
        boolean enabled = data.isBodyDefenseEnabled();
        int raw = pp == null ? 0 : BodyDefenseHelper.playerRawBodyDefense((Player)pp);
        MutableComponent label = Component.translatable((String)"screen.friday_cultivation.attr.defense_label");
        this.drawAttrSmall(gfx, (Component)label, xLeft, y + 1, -12766422);
        Object valueStr = enabled ? "+" + raw : "0";
        MutableComponent val = Component.literal((String)valueStr);
        int valW = (int)((float)this.font.width((FormattedText)val) * this.attrTabScale());
        this.drawAttrSmall(gfx, (Component)val, xRight - valW, y + 1, enabled ? -12950192 : -5222320);
        this.drawDottedHLine(gfx, xLeft, y + rowH - 1, xRight, -2006295992);
        return y + rowH;
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

    private void renderBonusSettingsPopup(GuiGraphics gfx, CultivationData data, int mouseX, int mouseY) {
        boolean closeHover;
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
        MutableComponent title = Component.translatable((String)"screen.friday_cultivation.attr.bonus_settings.title");
        this.drawSmallCentered(gfx, (Component)title, px + popupW / 2, py + 9, -7707624);
        int closeX = px + popupW - 17;
        int closeY = py + 7;
        this.bonusSettingsPopupCloseRect = new int[]{closeX, closeY, closeX + 10, closeY + 10};
        boolean bl = closeHover = mouseX >= closeX && mouseX < closeX + 10 && mouseY >= closeY && mouseY < closeY + 10;
        if (closeHover) {
            gfx.fill(closeX - 1, closeY - 1, closeX + 11, closeY + 11, -5720);
        }
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
            ArrayList<MutableComponent> lines = new ArrayList<MutableComponent>();
            lines.add(Component.translatable((String)this.hoveredBonusCategory.labelKey()).copy().withStyle(ChatFormatting.GOLD));
            lines.add(Component.translatable((String)this.hoveredBonusCategory.descriptionKey()).copy().withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable((String)(enabled ? "screen.friday_cultivation.attr.bonus_settings.enabled" : "screen.friday_cultivation.attr.bonus_settings.disabled")).copy().withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED));
            lines.add(Component.translatable((String)"screen.friday_cultivation.attr.bonus_settings.toggle_hint").copy().withStyle(ChatFormatting.DARK_GRAY));
            gfx.renderComponentTooltip(this.font, new java.util.ArrayList<net.minecraft.network.chat.Component>(lines), mouseX, mouseY);
        }
        gfx.pose().popPose();
    }

    private void renderBonusToggleRow(GuiGraphics gfx, CultivationData data, CultivationBonusCategory category, int x, int y, int w, int h, int mouseX, int mouseY) {
        boolean enabled = data.isBonusCategoryEnabled(category);
        boolean hover = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        this.bonusToggleRowRects.add(new BonusToggleRowRect(x, y, x + w, y + h, category));
        if (hover) {
            this.hoveredBonusCategory = category;
            gfx.fill(x - 2, y, x + w + 2, y + h, 868852298);
        }
        this.drawBonusCategoryIcon(gfx, x + 2, y + 3, category, enabled);
        MutableComponent label = Component.translatable((String)category.labelKey());
        MutableComponent value = Component.literal((String)this.bonusCategoryCurrentValue(data, category));
        int switchX = x + w - 28;
        int valueRight = switchX - 5;
        int valueMaxW = 54;
        int valueColor = this.bonusCategoryValueColor(data, category, enabled);
        this.drawSmallRightFitting(gfx, (Component)value, valueRight, y + 3, valueMaxW, valueColor);
        int labelMaxW = Math.max(24, valueRight - valueMaxW - (x + 14) - 4);
        this.drawScaledFitting(gfx, (Component)label, x + 14, y + 3, labelMaxW, enabled ? -12766422 : -7702176, CultivationScreen.effectiveTextScale());
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
            case BODY_DEFENSE -> CultivationScreen.formatSignedInt(BodyDefenseHelper.playerRawBodyDefense((Player)player));
            case MOVEMENT_SPEED -> CultivationScreen.formatSignedPercent((TechniqueBonusHelper.moveSpeedBonus((Player)player) + ZhenyuanBonusHelper.agilityMoveSpeedMult((Player)player)) * 100.0);
            case JUMP_HEIGHT -> CultivationScreen.formatSignedPercent(ZhenyuanBonusHelper.agilityJumpHeightMult((Player)player) * 100.0);
            case MELEE_DAMAGE -> CultivationScreen.formatSignedInt(CultivationScreen.meleeDamageBonusValue(data, player));
            case MINING_SPEED -> CultivationScreen.formatSignedPercent(ZhenyuanBonusHelper.physiqueMiningSpeedPct((Player)player));
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
        return data.getAttack() + TechniqueBonusHelper.attackBonus((Player)player) + ZhenyuanBonusHelper.physiqueAttackBonus((Player)player) + FoundationDaoBonusHelper.meleeDamageBonus((Player)player) + GoldenCoreDaoBonusHelper.meleeDamageBonus((Player)player) + LooseImmortalBonusHelper.meleeDamageBonus((Player)player);
    }

    private static String spellDamageBonusRange(LocalPlayer player) {
        int min = 0;
        int max = 0;
        boolean found = false;
        for (Spell spell : Spell.values()) {
            if (spell == null || spell.damage() <= 0) continue;
            int value = SpellScalingHelper.powerBonusPercent((LivingEntity)player, spell);
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
            base = Math.max(1L, Math.round((double)base * 1.5));
        }
        return data.getMaxQi() - base;
    }

    private static int qiRecoveryBonusValue(LocalPlayer player, CultivationData data) {
        if (player == null || data == null) {
            return 0;
        }
        return PlayerQiConsumer.nominalQiRecoveryPerSecond((Player)player, data, QiElement.PURE);
    }

    private static int cultivationEfficiencyBonusValue(LocalPlayer player, CultivationData data) {
        if (player == null || data == null) {
            return 0;
        }
        int total = PlayerQiConsumer.cultivationEfficiencyPerParticle((Player)player, data, QiElement.PURE);
        int base = (int)Math.round(PlayerQiAbsorptionHelper.baseAbsorbMultiplier(data));
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
        return Component.translatable((String)"screen.friday_cultivation.attr.qi_recovery_value", (Object[])new Object[]{CultivationScreen.formatSignedInt(Math.max(0, amount))}).getString();
    }

    private static String formatCultivationEfficiencyBonus(int amount) {
        return Component.translatable((String)"screen.friday_cultivation.attr.cultivation_efficiency_value", (Object[])new Object[]{CultivationScreen.formatSignedInt(Math.max(0, amount))}).getString();
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

    private void renderSmallSwitch(GuiGraphics gfx, int x, int y, boolean enabled, boolean hover) {
        int w = 22;
        int h = 10;
        if (hover) {
            gfx.fill(x - 1, y - 1, x + w + 1, y + h + 1, -5720);
        }
        gfx.fill(x, y, x + w, y + h, -15067628);
        gfx.fill(x + 1, y + 1, x + w - 1, y + h - 1, enabled ? -4703686 : -9804193);
        int knobX = enabled ? x + w - 9 : x + 2;
        gfx.fill(knobX, y + 2, knobX + 7, y + h - 2, -726312);
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

    private static String formatBonus(int base, int bonus) {
        if (bonus <= 0) {
            return "+" + base;
        }
        return "+" + (base + bonus) + " (+" + bonus + ")";
    }

    private static String formatAlchemyRank(CultivationData data) {
        AlchemyRank rank = data.getAlchemyRank();
        String rankName = rank.displayName().getString();
        if (rank.isMax()) {
            return rankName + " (MAX)";
        }
        return rankName + " (" + data.getAlchemyXp() + "/" + rank.xpToNext() + ")";
    }

    private static String formatRefiningRank(CultivationData data) {
        RefiningRank rank = data.getRefiningRank();
        String rankName = rank.displayName().getString();
        if (rank.isMax()) {
            return rankName + " (MAX)";
        }
        return rankName + " (" + data.getRefiningXp() + "/" + rank.xpToNext() + ")";
    }

    private static String formatCultivationEfficiency(int amount) {
        return Component.translatable((String)"screen.friday_cultivation.attr.cultivation_efficiency_value", (Object[])new Object[]{Math.max(0, amount)}).getString();
    }

    private static String formatQiRecoveryPerSecond(int amount) {
        return Component.translatable((String)"screen.friday_cultivation.attr.qi_recovery_value", (Object[])new Object[]{Math.max(0, amount)}).getString();
    }

    private void renderZhenyuanRadar(GuiGraphics gfx, int x, int rightX, int y, CultivationData data, int mouseX, int mouseY) {
        int i;
        int i2;
        int i3;
        int i4;
        MutableComponent header = Component.translatable((String)"zhenyuan.friday_cultivation.title");
        MutableComponent tag = Component.translatable((String)"zhenyuan.friday_cultivation.unallocated_tag", (Object[])new Object[]{data.getUnallocatedZhenyuan()});
        this.drawConfigSectionLabel(gfx, (Component)header, (Component)tag, x, y += 4, rightX, data.getUnallocatedZhenyuan() > 0);
        y += 12;
        int[] vals = new int[]{data.getAttrConstitution(), data.getAttrPhysique(), data.getAttrAgility(), data.getAttrSpellPower(), data.getAttrQiSea()};
        String[] labelKeys = new String[]{"zhenyuan.friday_cultivation.attr.constitution", "zhenyuan.friday_cultivation.attr.physique", "zhenyuan.friday_cultivation.attr.agility", "zhenyuan.friday_cultivation.attr.spell_power", "zhenyuan.friday_cultivation.attr.qi_sea"};
        int maxVal = 10;
        for (int v : vals) {
            maxVal = Math.max(maxVal, v);
        }
        if (maxVal % 5 != 0) {
            maxVal = (maxVal / 5 + 1) * 5;
        }
        int cx = (x + rightX) / 2;
        int radarTop = y;
        int radius = 28;
        int cy = radarTop + radius + 12;
        double[] cosA = new double[5];
        double[] sinA = new double[5];
        for (int i5 = 0; i5 < 5; ++i5) {
            double a = Math.toRadians(-90 + 72 * i5);
            cosA[i5] = Math.cos(a);
            sinA[i5] = Math.sin(a);
        }
        int outerRingColor = -4478832;
        int axisColor = -3161944;
        int innerColor = -2076624;
        int dotColor = -5758960;
        int innerFillColor = -1998557120;
        int[] outerX = new int[5];
        int[] outerY = new int[5];
        for (i4 = 0; i4 < 5; ++i4) {
            outerX[i4] = cx + (int)Math.round(cosA[i4] * (double)radius);
            outerY[i4] = cy + (int)Math.round(sinA[i4] * (double)radius);
        }
        for (i4 = 0; i4 < 5; ++i4) {
            this.drawPixelLine(gfx, outerX[i4], outerY[i4], outerX[(i4 + 1) % 5], outerY[(i4 + 1) % 5], outerRingColor);
        }
        for (i4 = 0; i4 < 5; ++i4) {
            this.drawPixelLine(gfx, cx, cy, outerX[i4], outerY[i4], axisColor);
        }
        int halfR = radius / 2;
        int[] halfX = new int[5];
        int[] halfY = new int[5];
        for (i3 = 0; i3 < 5; ++i3) {
            halfX[i3] = cx + (int)Math.round(cosA[i3] * (double)halfR);
            halfY[i3] = cy + (int)Math.round(sinA[i3] * (double)halfR);
        }
        for (i3 = 0; i3 < 5; ++i3) {
            this.drawPixelLine(gfx, halfX[i3], halfY[i3], halfX[(i3 + 1) % 5], halfY[(i3 + 1) % 5], 1723574416);
        }
        int[] curX = new int[5];
        int[] curY = new int[5];
        for (i2 = 0; i2 < 5; ++i2) {
            double r = (double)vals[i2] / (double)maxVal * (double)radius;
            curX[i2] = cx + (int)Math.round(cosA[i2] * r);
            curY[i2] = cy + (int)Math.round(sinA[i2] * r);
        }
        for (i2 = 0; i2 < 5; ++i2) {
            int j = (i2 + 1) % 5;
            CultivationScreen.fillTriangle(gfx, cx, cy, curX[i2], curY[i2], curX[j], curY[j], innerFillColor);
        }
        for (i2 = 0; i2 < 5; ++i2) {
            this.drawPixelLine(gfx, curX[i2], curY[i2], curX[(i2 + 1) % 5], curY[(i2 + 1) % 5], innerColor);
        }
        for (i2 = 0; i2 < 5; ++i2) {
            gfx.fill(curX[i2] - 1, curY[i2] - 1, curX[i2] + 2, curY[i2] + 2, dotColor);
        }
        int labelOffset = 5;
        boolean canSpend = data.getUnallocatedZhenyuan() > 0;
        long now = System.currentTimeMillis();
        int BTN_SIZE = 6;
        boolean BTN_GAP = true;
        int LINE_H = 7;
        for (i = 0; i < 5; ++i) {
            this.zhenyuanPlusRects[i][0] = 0;
            this.zhenyuanPlusRects[i][1] = 0;
            this.zhenyuanPlusRects[i][2] = 0;
            this.zhenyuanPlusRects[i][3] = 0;
            this.zhenyuanLabelRects[i][0] = 0;
            this.zhenyuanLabelRects[i][1] = 0;
            this.zhenyuanLabelRects[i][2] = 0;
            this.zhenyuanLabelRects[i][3] = 0;
        }
        for (i = 0; i < 5; ++i) {
            int btnFg;
            int btnBg;
            boolean btnFlash;
            int boxX;
            MutableComponent label = Component.translatable((String)labelKeys[i]);
            MutableComponent labelWithValue = Component.translatable((String)"zhenyuan.friday_cultivation.attr.label_with_value", (Object[])new Object[]{label, vals[i]});
            int lx = cx + (int)Math.round(cosA[i] * (double)(radius + labelOffset));
            int ly = cy + (int)Math.round(sinA[i] * (double)(radius + labelOffset));
            int boxY = switch (i) {
                case 0 -> {
                    boxX = lx - 18;
                    yield ly - 6;
                }
                case 1 -> {
                    boxX = lx + 2;
                    yield ly - 8;
                }
                case 2 -> {
                    boxX = lx + 2;
                    yield ly - 4;
                }
                case 3 -> {
                    boxX = lx - 30;
                    yield ly - 4;
                }
                default -> {
                    boxX = lx - 30;
                    yield ly - 8;
                }
            };
            int btnX = boxX;
            int btnY = boxY;
            int textX = btnX + 6 + 1;
            int line1Y = boxY;
            this.drawTinyAt(gfx, (Component)labelWithValue, textX, line1Y, -15067628);
            boolean btnHover = canSpend && mouseX >= btnX && mouseX < btnX + 6 && mouseY >= btnY && mouseY < btnY + 6;
            boolean bl = btnFlash = now < this.zhenyuanPlusFlashUntil[i];
            if (!canSpend) {
                btnBg = -7829368;
                btnFg = -3355444;
            } else if (btnFlash) {
                btnBg = -4388;
                btnFg = -4703686;
            } else if (btnHover) {
                btnBg = -2074022;
                btnFg = -1;
            } else {
                btnBg = -4703686;
                btnFg = -4388;
            }
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
            this.zhenyuanLabelRects[i][1] = line1Y - 1;
            this.zhenyuanLabelRects[i][2] = textX + 60;
            this.zhenyuanLabelRects[i][3] = line1Y + 7 + 1;
        }
    }

    private static void fillTriangle(GuiGraphics gfx, int x1, int y1, int x2, int y2, int x3, int y3, int color) {
        int minX = Math.min(x1, Math.min(x2, x3));
        int maxX = Math.max(x1, Math.max(x2, x3));
        int minY = Math.min(y1, Math.min(y2, y3));
        int maxY = Math.max(y1, Math.max(y2, y3));
        for (int px = minX; px <= maxX; ++px) {
            for (int py = minY; py <= maxY; ++py) {
                boolean hasPos;
                int d1 = CultivationScreen.edgeSign(px, py, x1, y1, x2, y2);
                int d2 = CultivationScreen.edgeSign(px, py, x2, y2, x3, y3);
                int d3 = CultivationScreen.edgeSign(px, py, x3, y3, x1, y1);
                boolean hasNeg = d1 < 0 || d2 < 0 || d3 < 0;
                boolean bl = hasPos = d1 > 0 || d2 > 0 || d3 > 0;
                if (hasNeg && hasPos) continue;
                gfx.fill(px, py, px + 1, py + 1, color);
            }
        }
    }

    private static int edgeSign(int px, int py, int x1, int y1, int x2, int y2) {
        return (px - x2) * (y1 - y2) - (x1 - x2) * (py - y2);
    }

    private void drawConfigSectionLabel(GuiGraphics gfx, Component label, Component tagText, int x, int y, int rightX, boolean tagActive) {
        gfx.fill(x, y, x + 2, y + 9, -4703686);
        this.drawSmall(gfx, label, x + 5, y + 1, -9807288);
        int textW = (int)((float)this.font.width((FormattedText)label) * CultivationScreen.effectiveTextScale());
        float tagScale = 0.7f;
        int tagW = (int)((float)this.font.width((FormattedText)tagText) * tagScale);
        int tagPadH = 2;
        int tagPadV = 2;
        int tagBoxW = tagW + tagPadV * 2;
        Objects.requireNonNull(this.font);
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
        gfx.pose().translate((float)(tagBoxX + tagPadV), (float)(tagBoxY + tagPadH), 0.0f);
        gfx.pose().scale(tagScale, tagScale, 1.0f);
        gfx.drawString(this.font, tagText, 0, 0, -4388, false);
        gfx.pose().popPose();
    }

    private void renderZhenyuanTooltip(GuiGraphics gfx, int mouseX, int mouseY, CultivationData data, int attrIdx) {
        MutableComponent currentEffect;
        MutableComponent perPointDesc;
        String[] nameKeys = new String[]{"zhenyuan.friday_cultivation.attr.constitution", "zhenyuan.friday_cultivation.attr.physique", "zhenyuan.friday_cultivation.attr.agility", "zhenyuan.friday_cultivation.attr.spell_power", "zhenyuan.friday_cultivation.attr.qi_sea"};
        int[] points = new int[]{data.getAttrConstitution(), data.getAttrPhysique(), data.getAttrAgility(), data.getAttrSpellPower(), data.getAttrQiSea()};
        switch (attrIdx) {
            case 0: {
                perPointDesc = Component.translatable((String)"zhenyuan.friday_cultivation.tooltip.per_point.constitution");
                currentEffect = Component.translatable((String)"zhenyuan.friday_cultivation.tooltip.current.constitution", (Object[])new Object[]{points[0]});
                break;
            }
            case 1: {
                perPointDesc = Component.translatable((String)"zhenyuan.friday_cultivation.tooltip.per_point.physique");
                currentEffect = Component.translatable((String)"zhenyuan.friday_cultivation.tooltip.current.physique", (Object[])new Object[]{points[1], CultivationScreen.formatZhenyuanPercent((double)points[1] * 1.0)});
                break;
            }
            case 2: {
                perPointDesc = Component.translatable((String)"zhenyuan.friday_cultivation.tooltip.per_point.agility");
                currentEffect = Component.translatable((String)"zhenyuan.friday_cultivation.tooltip.current.agility", (Object[])new Object[]{CultivationScreen.formatZhenyuanPercent((double)points[2] * 1.0), CultivationScreen.formatZhenyuanPercent((double)points[2] * 0.2)});
                break;
            }
            case 3: {
                perPointDesc = Component.translatable((String)"zhenyuan.friday_cultivation.tooltip.per_point.spell_power");
                currentEffect = Component.translatable((String)"zhenyuan.friday_cultivation.tooltip.current.spell_power", (Object[])new Object[]{points[3] * 5});
                break;
            }
            case 4: {
                perPointDesc = Component.translatable((String)"zhenyuan.friday_cultivation.tooltip.per_point.qi_sea");
                currentEffect = Component.translatable((String)"zhenyuan.friday_cultivation.tooltip.current.qi_sea", (Object[])new Object[]{(long)points[4] * 100L, (long)points[4] * 1L});
                break;
            }
            default: {
                return;
            }
        }
        ArrayList<MutableComponent> lines = new ArrayList<MutableComponent>();
        lines.add(Component.translatable((String)nameKeys[attrIdx]).copy().withStyle(ChatFormatting.AQUA));
        lines.add(Component.translatable((String)"zhenyuan.friday_cultivation.tooltip.points", (Object[])new Object[]{points[attrIdx]}).copy().withStyle(ChatFormatting.GRAY));
        lines.add(perPointDesc.copy().withStyle(ChatFormatting.GRAY));
        lines.add(currentEffect.copy().withStyle(ChatFormatting.WHITE));
        if (data.getUnallocatedZhenyuan() > 0) {
            lines.add(Component.translatable((String)"zhenyuan.friday_cultivation.tooltip.click_to_spend").copy().withStyle(ChatFormatting.YELLOW));
        }
        gfx.renderComponentTooltip(this.font, new java.util.ArrayList<net.minecraft.network.chat.Component>(lines), mouseX, mouseY);
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
            if (e2 > -dy) {
                err -= dy;
                cx += sx;
            }
            if (e2 >= dx) continue;
            err += dx;
            cy += sy;
        }
    }

    private void drawTinyAt(GuiGraphics gfx, Component text, int x, int y, int color) {
        float scale = 0.6f;
        gfx.pose().pushPose();
        gfx.pose().translate((float)x, (float)y, 0.0f);
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.drawString(this.font, text, 0, 0, color, false);
        gfx.pose().popPose();
    }

    private static String formatZhenyuanPercent(double value) {
        if (Math.abs(value - Math.rint(value)) < 1.0E-4) {
            return Integer.toString((int)Math.rint(value));
        }
        return String.format(Locale.ROOT, "%.1f", value);
    }

    @Deprecated
    private void renderElementPercents(GuiGraphics gfx, int x, int rightX, int y, CultivationData data) {
        QiElement dominant = data.getDominantElement();
        MutableComponent header = Component.translatable((String)"screen.friday_cultivation.attr.element_section", (Object[])new Object[]{Component.translatable((String)("element.friday_cultivation." + dominant.id()))});
        this.drawSectionLabel(gfx, (Component)header, x, y += 4, rightX);
        y += 11;
        for (QiElement el : QiElement.values()) {
            long count = data.getElementCount(el);
            double pct = data.getElementPercent(el);
            int bonus = data.getElementDamageBonus(el);
            MutableComponent name = Component.translatable((String)("element.friday_cultivation." + el.id()));
            String countStr = CompactNumberFormat.format(count);
            MutableComponent mainPart = Component.translatable((String)"screen.friday_cultivation.attr.element_row_main", (Object[])new Object[]{name, countStr});
            MutableComponent suffixPart = Component.translatable((String)"screen.friday_cultivation.attr.element_row_suffix", (Object[])new Object[]{String.format("%.1f", pct), bonus});
            int mainColor = el == dominant ? -3562934 : -12766422;
            int suffixColor = -7702176;
            this.drawAttrSmall(gfx, (Component)mainPart, x, y, mainColor);
            int mainW = (int)((float)this.font.width((FormattedText)mainPart) * this.attrTabScale());
            this.drawAttrTinyInline(gfx, (Component)suffixPart, x + mainW + 4, y + 1, suffixColor);
            y += 9;
        }
    }

    private void drawAttrTinyInline(GuiGraphics gfx, Component text, int x, int y, int color) {
        float scale = 0.63f;
        gfx.pose().pushPose();
        gfx.pose().translate((float)x, (float)y, 0.0f);
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.drawString(this.font, text, 0, 0, color, false);
        gfx.pose().popPose();
    }

    private void drawTinyInline(GuiGraphics gfx, Component text, int x, int y, int color) {
        float scale = 0.7f;
        gfx.pose().pushPose();
        gfx.pose().translate((float)x, (float)y, 0.0f);
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.drawString(this.font, text, 0, 0, color, false);
        gfx.pose().popPose();
    }

    private void renderTechniquesTab(GuiGraphics gfx, int splitX, int y, CultivationData data, int mouseX, int mouseY) {
        int x = splitX + 12;
        int rightX = splitX + 160 - 12;
        this.techniqueLearnedCellRects.clear();
        this.techniqueEquippedRect = null;
        this.drawSectionLabel(gfx, (Component)Component.translatable((String)"screen.friday_cultivation.tech.equipped_label"), x, y, rightX);
        Technique equipped = Technique.byId(data.getEquippedTechniqueId());
        int blockW = rightX - x;
        int blockH = 36;
        this.drawEquippedTechniqueBlock(gfx, x, y += 11, blockW, blockH, equipped);
        this.techniqueEquippedRect = new int[]{x, y};
        if (equipped != null && mouseX >= x && mouseX < x + blockW && mouseY >= y && mouseY < y + blockH) {
            this.hoveredTechniqueId = equipped.id();
        }
        y += blockH + 3;
        this.drawSectionLabel(gfx, (Component)Component.translatable((String)"screen.friday_cultivation.tech.learned_label"), x, y += 4, rightX);
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
        boolean bl = this.mouseOverTechGrid = mouseX >= gridLeft && mouseX < gridLeft + gridW + 8 && mouseY >= gridTop && mouseY < gridTop + gridH;
        if (this.techMaxScroll > 0) {
            this.drawScrollbar(gfx, gridLeft + gridW + 2, gridTop, gridTop + gridH, rows, totalRows, this.techScrollOffset);
        }
        int curStart = this.techScrollOffset * cols + 1;
        int curEnd = Math.min(tids.size(), (this.techScrollOffset + rows) * cols);
        MutableComponent pageLabel = tids.isEmpty() ? Component.translatable((String)"screen.friday_cultivation.tech.none") : Component.translatable((String)"screen.friday_cultivation.tech.range", (Object[])new Object[]{curStart, curEnd, tids.size()});
        this.drawSmallCentered(gfx, (Component)pageLabel, splitX + 80, y += gridH + 4, -9807288);
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
            this.drawSmall(gfx, (Component)Component.translatable((String)"screen.friday_cultivation.tech.none_equipped_warn"), x + 6, y + 4, -7723482);
            this.drawSmall(gfx, (Component)Component.translatable((String)"screen.friday_cultivation.tech.none_equipped"), x + 6, y + 14, -9807288);
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
        this.drawSmall(gfx, (Component)Component.translatable((String)"screen.friday_cultivation.tech.click_to_unequip"), textX, y + 14, -9807288);
        MutableComponent equippedLabel = Component.translatable((String)"screen.friday_cultivation.tech.equipped_label_short");
        int labelW = (int)((float)this.font.width((FormattedText)equippedLabel) * CultivationScreen.effectiveTextScale()) + 4;
        int labelX = x + w - labelW - 4;
        int labelY = y - 3;
        gfx.fill(labelX - 1, labelY - 1, labelX + labelW + 1, labelY + 8, -15067628);
        gfx.fill(labelX, labelY, labelX + labelW, labelY + 7, -3562934);
        this.drawSmall(gfx, (Component)equippedLabel, labelX + 2, labelY, -15068144);
        this.drawTierElementBadges(gfx, iconX, y + h - 11, t.tier(), t.primaryElement());
    }

    private void drawTierElementBadges(GuiGraphics gfx, int x, int y, ItemTier tier, QiElement element) {
        Component tierText = tier.displayName();
        int tierW = (int)((float)this.font.width((FormattedText)tierText) * CultivationScreen.effectiveTextScale()) + 4;
        gfx.fill(x, y, x + tierW, y + 9, -1290136560);
        this.drawSmall(gfx, tierText, x + 2, y + 1, CultivationScreen.tierRgb(tier));
        int elX = x + tierW + 3;
        MutableComponent elText = Component.translatable((String)("element.friday_cultivation." + element.id()));
        int elW = (int)((float)this.font.width((FormattedText)elText) * CultivationScreen.effectiveTextScale()) + 4;
        gfx.fill(elX, y, elX + elW, y + 9, -1290136560);
        this.drawSmall(gfx, (Component)elText, elX + 2, y + 1, 0xFF000000 | element.rgb() & 0xFFFFFF);
    }

    private static int tierRgb(ItemTier tier) {
        return switch (tier) {
            default -> throw new IncompatibleClassChangeError();
            case LOW -> -1;
            case MID -> -8128;
            case HIGH -> -12525344;
            case SUPREME -> -2064129;
            case IMMORTAL -> -2068440;
        };
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

    private void renderSpellsTab(GuiGraphics gfx, int splitX, int y, CultivationData data, int mouseX, int mouseY) {
        Spell sp;
        this.learnedCellRects.clear();
        this.wheelCellRects.clear();
        int gridW = 56;
        int gridLeft = splitX + (160 - gridW) / 2;
        int gridTop = y;
        String[] equipped = data.getEquippedSpells();
        for (int i = 0; i < 8; ++i) {
            int cx = gridLeft + OCT_X[i] * 19;
            int cy = gridTop + OCT_Y[i] * 19;
            String sid = equipped[i];
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
        this.drawSectionLabel(gfx, (Component)Component.translatable((String)"screen.friday_cultivation.spell.learned_label"), splitX + 12, learnedY - 4, splitX + 160 - 12);
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
        boolean bl = this.mouseOverSpellGrid = mouseX >= learnedLeft && mouseX < learnedLeft + learnedGridW + 8 && mouseY >= learnedY && mouseY < learnedY + learnedGridH;
        if (this.spellMaxScroll > 0) {
            this.drawScrollbar(gfx, learnedLeft + learnedGridW + 2, learnedY, learnedY + learnedGridH, rows, totalRows, this.spellScrollOffset);
        }
        if (this.isDragging && this.draggingSpellId != null && (sp = Spell.byId(this.draggingSpellId)) != null) {
            RenderSystem.enableBlend();
            this.blitSpellIcon(gfx, sp, mouseX - 7, mouseY - 7, 14);
            RenderSystem.disableBlend();
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
        boolean disabledPassive = CultivationScreen.isDisabledPassive(sp, enabled);
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
        boolean disabledPassive = CultivationScreen.isDisabledPassive(sp, enabled);
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

    private void drawGradientBar(GuiGraphics gfx, ResourceLocation iconTex, int x, int y, int totalWidth, float ratio, String text, int colorTop, int colorBot) {
        int iconSize = 9;
        RenderSystem.enableBlend();
        gfx.blit(iconTex, x, y, 0.0f, 0.0f, iconSize, iconSize, iconSize, iconSize);
        RenderSystem.disableBlend();
        int barX = x + iconSize + 3;
        int barY = y + 1;
        int barW = totalWidth - iconSize - 3;
        int barH = 7;
        gfx.fill(barX - 1, barY - 1, barX + barW + 1, barY, -15067628);
        gfx.fill(barX - 1, barY + barH, barX + barW + 1, barY + barH + 1, -15067628);
        gfx.fill(barX - 1, barY, barX, barY + barH, -15067628);
        gfx.fill(barX + barW, barY, barX + barW + 1, barY + barH, -15067628);
        gfx.fill(barX, barY, barX + barW, barY + barH, -14739179);
        gfx.fill(barX, barY, barX + barW, barY + 1, -16119802);
        ratio = Math.max(0.0f, Math.min(1.0f, ratio));
        int filledW = (int)((float)barW * ratio);
        if (filledW > 0) {
            int half = barH / 2;
            gfx.fill(barX, barY, barX + filledW, barY + half, colorTop);
            gfx.fill(barX, barY + half, barX + filledW, barY + barH, colorBot);
            gfx.fill(barX, barY, barX + filledW, barY + 1, 0x40FFFFFF);
            gfx.fill(barX, barY + barH - 1, barX + filledW, barY + barH, 0x33000000);
            for (int sx = barX + 6; sx < barX + filledW; sx += 6) {
                gfx.fill(sx, barY, sx + 1, barY + barH, 0x1F000000);
            }
        }
        int textW = (int)((float)this.font.width(text) * CultivationScreen.effectiveTextScale());
        int textX = barX + (barW - textW) / 2;
        int textY = barY - 1;
        this.drawSmall(gfx, (Component)Component.literal((String)text), textX, textY, -1);
    }

    private void drawLeftStatusBar(GuiGraphics gfx, ResourceLocation iconTex, int x, int y, int totalWidth, float ratio, Component label, String value, int colorTop, int colorBot) {
        int maxTextW;
        int iconSize = 9;
        RenderSystem.enableBlend();
        gfx.blit(iconTex, x, y, 0.0f, 0.0f, iconSize, iconSize, iconSize, iconSize);
        RenderSystem.disableBlend();
        int labelX = x + iconSize + 3;
        this.drawScaledFitting(gfx, label, labelX, y + 2, 14, -3562934, 0.62f);
        int barX = labelX + 14 + 1;
        int barY = y + 1;
        int barW = Math.max(1, totalWidth - (barX - x));
        int barH = 7;
        gfx.fill(barX - 1, barY - 1, barX + barW + 1, barY, -15067628);
        gfx.fill(barX - 1, barY + barH, barX + barW + 1, barY + barH + 1, -15067628);
        gfx.fill(barX - 1, barY, barX, barY + barH, -15067628);
        gfx.fill(barX + barW, barY, barX + barW + 1, barY + barH, -15067628);
        gfx.fill(barX, barY, barX + barW, barY + barH, -14739179);
        gfx.fill(barX, barY, barX + barW, barY + 1, -16119802);
        ratio = Math.max(0.0f, Math.min(1.0f, ratio));
        int filledW = (int)((float)barW * ratio);
        if (filledW > 0) {
            int half = barH / 2;
            gfx.fill(barX, barY, barX + filledW, barY + half, colorTop);
            gfx.fill(barX, barY + half, barX + filledW, barY + barH, colorBot);
            gfx.fill(barX, barY, barX + filledW, barY + 1, 0x40FFFFFF);
            gfx.fill(barX, barY + barH - 1, barX + filledW, barY + barH, 0x33000000);
            for (int sx = barX + 6; sx < barX + filledW; sx += 6) {
                gfx.fill(sx, barY, sx + 1, barY + barH, 0x1F000000);
            }
        }
        MutableComponent valueText = Component.literal((String)value);
        float textScale = CultivationScreen.effectiveTextScale();
        int rawTextW = this.font.width((FormattedText)valueText);
        if ((float)rawTextW * textScale > (float)(maxTextW = Math.max(1, barW - 4))) {
            textScale = Math.max(0.42f, (float)maxTextW / (float)rawTextW);
        }
        int textW = (int)((float)rawTextW * textScale);
        this.drawScaled(gfx, (Component)valueText, barX + (barW - textW) / 2, barY - 1, -1, textScale);
    }

    private void drawThinBar(GuiGraphics gfx, int x, int y, int totalWidth, float ratio, String label, String value, int colorTop, int colorBot) {
        this.drawSmall(gfx, (Component)Component.literal((String)label), x, y - 1, -3562934);
        int labelW = Math.max(18, (int)((float)this.font.width(label) * CultivationScreen.effectiveTextScale()) + 4);
        int barX = x + labelW;
        int barY = y;
        int barW = Math.max(1, totalWidth - labelW);
        int barH = 6;
        gfx.fill(barX - 1, barY - 1, barX + barW + 1, barY, -15067628);
        gfx.fill(barX - 1, barY + barH, barX + barW + 1, barY + barH + 1, -15067628);
        gfx.fill(barX - 1, barY, barX, barY + barH, -15067628);
        gfx.fill(barX + barW, barY, barX + barW + 1, barY + barH, -15067628);
        gfx.fill(barX, barY, barX + barW, barY + barH, -14739179);
        ratio = Math.max(0.0f, Math.min(1.0f, ratio));
        int filledW = (int)((float)barW * ratio);
        if (filledW > 0) {
            int half = barH / 2;
            gfx.fill(barX, barY, barX + filledW, barY + half, colorTop);
            gfx.fill(barX, barY + half, barX + filledW, barY + barH, colorBot);
            gfx.fill(barX, barY, barX + filledW, barY + 1, 0x40FFFFFF);
        }
        int textW = (int)((float)this.font.width(value) * CultivationScreen.effectiveTextScale());
        this.drawSmall(gfx, (Component)Component.literal((String)value), barX + (barW - textW) / 2, barY - 1, -1);
    }

    private void drawScrollbar(GuiGraphics gfx, int x, int yTop, int yBot, int visibleRows, int totalRows, int currentRow) {
        int trackW = 4;
        gfx.fill(x, yTop, x + trackW, yBot, -15067628);
        gfx.fill(x + 1, yTop + 1, x + trackW - 1, yBot - 1, -7439524);
        int trackH = yBot - yTop - 2;
        int thumbH = Math.max(6, (int)((double)trackH * (double)visibleRows / (double)totalRows));
        int maxOffset = totalRows - visibleRows;
        double progress = maxOffset > 0 ? (double)currentRow / (double)maxOffset : 0.0;
        int thumbY = yTop + 1 + (int)((double)(trackH - thumbH) * progress);
        gfx.fill(x + 1, thumbY, x + trackW - 1, thumbY + thumbH, -3562934);
        if (thumbH > 4) {
            int hx = x + 1;
            int midY = thumbY + thumbH / 2;
            gfx.fill(hx, midY - 1, hx + trackW - 2, midY + 1, -4703686);
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int newOffset;
        if (this.breakthroughHistoryPopupOpen) {
            return true;
        }
        if (this.currentTab == Tab.TECHNIQUES && this.mouseOverTechGrid && this.techMaxScroll > 0 && (newOffset = (int)Math.max(0.0, Math.min((double)this.techMaxScroll, (double)this.techScrollOffset - delta))) != this.techScrollOffset) {
            this.techScrollOffset = newOffset;
            return true;
        }
        if (this.currentTab == Tab.SPELLS && this.mouseOverSpellGrid && this.spellMaxScroll > 0 && (newOffset = (int)Math.max(0.0, Math.min((double)this.spellMaxScroll, (double)this.spellScrollOffset - delta))) != this.spellScrollOffset) {
            this.spellScrollOffset = newOffset;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private void updateToggleSpellBtn(CultivationData data) {
        if (this.currentTab != Tab.SPELLS || this.selectedSpellId == null) {
            this.toggleSpellBtn.visible = false;
            return;
        }
        Spell sel = Spell.byId(this.selectedSpellId);
        if (sel == null || sel.type() != SpellType.PASSIVE) {
            this.toggleSpellBtn.visible = false;
            return;
        }
        this.toggleSpellBtn.visible = true;
        boolean enabled = data.isSpellEnabled(sel);
    }

    private void updateSpellTerrainDestructionBtn(CultivationData data) {
        if (this.spellTerrainDestructionBtn == null) {
            return;
        }
        if (this.currentTab != Tab.SPELLS) {
            this.spellTerrainDestructionBtn.visible = false;
            return;
        }
        this.spellTerrainDestructionBtn.visible = true;
        boolean forcedOff = data.isSpellTerrainDestructionForcedOffByServer();
        this.spellTerrainDestructionBtn.active = !forcedOff;
        this.spellTerrainDestructionBtn.setState(data.isSpellTerrainDestructionEnabled(), forcedOff);
    }

    private boolean handleBonusSettingsClick(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }
        if (this.bonusSettingsPopupOpen) {
            CultivationData data;
            if (CultivationScreen.inRect(this.bonusSettingsPopupCloseRect, mouseX, mouseY) || !CultivationScreen.inRect(this.bonusSettingsPopupRect, mouseX, mouseY)) {
                this.bonusSettingsPopupOpen = false;
                this.playUiClick();
                return true;
            }
            CultivationData cultivationData = data = Minecraft.getInstance().player == null ? null : (CultivationData)CultivationCapability.get((Player)Minecraft.getInstance().player).orElse(null);
            if (data != null) {
                for (BonusToggleRowRect row : this.bonusToggleRowRects) {
                    if (!(mouseX >= (double)row.x1()) || !(mouseX < (double)row.x2()) || !(mouseY >= (double)row.y1()) || !(mouseY < (double)row.y2())) continue;
                    boolean enabled = data.isBonusCategoryEnabled(row.category());
                    ModNetwork.CHANNEL.sendToServer((Object)new ToggleBonusCategoryPacket(row.category().id(), !enabled));
                    this.playUiClick();
                    return true;
                }
            }
            return true;
        }
        if (this.currentTab == Tab.ATTRIBUTES && CultivationScreen.inRect(this.bonusSettingsButtonRect, mouseX, mouseY)) {
            this.bonusSettingsPopupOpen = true;
            this.playUiClick();
            return true;
        }
        return false;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int idx;
        int cy;
        int cx;
        CultivationData data;
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        if (this.handleBonusSettingsClick(mouseX, mouseY, button)) {
            return true;
        }
        if (this.handleBreakthroughHistoryClick(mouseX, mouseY, button)) {
            return true;
        }
        if (button == 0 && this.createImperialArtButtonRect != null && this.isInsideRect(this.createImperialArtButtonRect, mouseX, mouseY)) {
            ModNetwork.CHANNEL.sendToServer((Object)new CreateImperialArtPacket());
            this.createImperialArtButtonRect = null;
            this.playUiClick();
            return true;
        }
        if (this.editingName) {
            if (this.nameEditBox.isMouseOver(mouseX, mouseY)) {
                return this.nameEditBox.mouseClicked(mouseX, mouseY, button);
            }
            this.submitName();
        }
        if (button == 0 && CultivationScreen.inRect(this.sectEntryArrowRect, mouseX, mouseY)) {
            CultivationData cultivationData = data = Minecraft.getInstance().player == null ? null : (CultivationData)CultivationCapability.get((Player)Minecraft.getInstance().player).orElse(null);
            if (data != null && data.hasSectDisplay()) {
                ModNetwork.CHANNEL.sendToServer((Object)new RequestSectScreenPacket(-1));
                this.playUiClick();
                return true;
            }
        }
        if (button == 0 && !this.editingName && CultivationScreen.inRect(this.nameCellRect, mouseX, mouseY)) {
            this.startNameEdit();
            this.playUiClick();
            return true;
        }
        if (button == 0 && CultivationScreen.inRect(this.genderCellRect, mouseX, mouseY)) {
            ModNetwork.CHANNEL.sendToServer((Object)new CycleGenderPacket());
            this.playUiClick();
            return true;
        }
        if ((this.currentTab == Tab.SPELLS || this.currentTab == Tab.TECHNIQUES) && this.handleFilterClick(mouseX, mouseY, button)) {
            Minecraft.getInstance().getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((Holder)SoundEvents.UI_BUTTON_CLICK, (float)1.0f));
            return true;
        }
        if (this.handleBreakthroughOptionClick(mouseX, mouseY, button)) {
            return true;
        }
        if (this.currentTab == Tab.ATTRIBUTES && button == 0) {
            for (int i = 0; i < 5; ++i) {
                int[] r = this.zhenyuanPlusRects[i];
                if (r[2] == 0 || !(mouseX >= (double)r[0]) || !(mouseX < (double)r[2]) || !(mouseY >= (double)r[1]) || !(mouseY < (double)r[3])) continue;
                this.startZhenyuanHold(i);
                return true;
            }
        }
        if (this.currentTab == Tab.TECHNIQUES && button == 0) {
            CultivationData cultivationData = data = Minecraft.getInstance().player == null ? null : (CultivationData)CultivationCapability.get((Player)Minecraft.getInstance().player).orElse(null);
            if (data != null) {
                List<String> tids = this.filteredLearnedTechniques(data);
                if (this.techniqueEquippedRect != null) {
                    int ex = this.techniqueEquippedRect[0] + 4;
                    int ey = this.techniqueEquippedRect[1] + 4;
                    if (mouseX >= (double)ex && mouseX < (double)(ex + 16) && mouseY >= (double)ey && mouseY < (double)(ey + 16)) {
                        if (!data.getEquippedTechniqueId().isEmpty()) {
                            ModNetwork.CHANNEL.sendToServer((Object)new EquipTechniquePacket(""));
                        }
                        return true;
                    }
                }
                for (int[] rect : this.techniqueLearnedCellRects) {
                    cx = rect[0];
                    cy = rect[1];
                    idx = rect[2];
                    if (!(mouseX >= (double)cx) || !(mouseX < (double)(cx + 18)) || !(mouseY >= (double)cy) || !(mouseY < (double)(cy + 18))) continue;
                    if (idx >= 0 && idx < tids.size()) {
                        String tid = tids.get(idx);
                        if (tid.equals(data.getEquippedTechniqueId())) {
                            ModNetwork.CHANNEL.sendToServer((Object)new EquipTechniquePacket(""));
                        } else {
                            Technique target = Technique.byId(tid);
                            if (CultivationScreen.canEquipTechniqueForScreenState(data, target)) {
                                ModNetwork.CHANNEL.sendToServer((Object)new EquipTechniquePacket(tid));
                            }
                        }
                    }
                    return true;
                }
            }
        }
        if (this.currentTab == Tab.SPELLS && button == 0) {
            CultivationData cultivationData = data = Minecraft.getInstance().player == null ? null : (CultivationData)CultivationCapability.get((Player)Minecraft.getInstance().player).orElse(null);
            if (data != null) {
                List<String> sids = this.filteredLearnedSpells(data);
                for (int[] rect : this.learnedCellRects) {
                    cx = rect[0];
                    cy = rect[1];
                    idx = rect[2];
                    if (!(mouseX >= (double)cx) || !(mouseX < (double)(cx + 18)) || !(mouseY >= (double)cy) || !(mouseY < (double)(cy + 18)) || idx < 0 || idx >= sids.size()) continue;
                    this.draggingSpellId = sids.get(idx);
                    this.draggingFromSlot = -1;
                    this.dragStartX = mouseX;
                    this.dragStartY = mouseY;
                    this.isDragging = false;
                    this.selectedSpellId = this.draggingSpellId;
                    return true;
                }
                for (int[] rect : this.wheelCellRects) {
                    cx = rect[0];
                    cy = rect[1];
                    int slot = rect[2];
                    if (!(mouseX >= (double)cx) || !(mouseX < (double)(cx + 18)) || !(mouseY >= (double)cy) || !(mouseY < (double)(cy + 18))) continue;
                    String wheelSid = data.getEquippedSpellAt(slot);
                    if (!wheelSid.isEmpty()) {
                        this.draggingSpellId = wheelSid;
                        this.draggingFromSlot = slot;
                        this.dragStartX = mouseX;
                        this.dragStartY = mouseY;
                        this.isDragging = false;
                        this.selectedSpellId = wheelSid;
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        double dy;
        double dx;
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        if (this.draggingSpellId != null && !this.isDragging && (dx = mouseX - this.dragStartX) * dx + (dy = mouseY - this.dragStartY) * dy > 16.0) {
            this.isDragging = true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        if (button == 0) {
            this.stopZhenyuanHold();
        }
        if (this.currentTab == Tab.SPELLS && button == 0 && this.draggingSpellId != null) {
            String sid = this.draggingSpellId;
            int fromSlot = this.draggingFromSlot;
            boolean wasDragging = this.isDragging;
            this.draggingSpellId = null;
            this.draggingFromSlot = -1;
            this.isDragging = false;
            if (!wasDragging) {
                return true;
            }
            int targetSlot = -1;
            for (int[] rect : this.wheelCellRects) {
                int cx = rect[0];
                int cy = rect[1];
                int slot = rect[2];
                if (!(mouseX >= (double)cx) || !(mouseX < (double)(cx + 18)) || !(mouseY >= (double)cy) || !(mouseY < (double)(cy + 18))) continue;
                targetSlot = slot;
                break;
            }
            if (targetSlot >= 0) {
                ModNetwork.CHANNEL.sendToServer((Object)new EquipSpellPacket(targetSlot, sid));
                if (fromSlot >= 0 && fromSlot != targetSlot) {
                    ModNetwork.CHANNEL.sendToServer((Object)new EquipSpellPacket(fromSlot, ""));
                }
            } else if (fromSlot >= 0) {
                ModNetwork.CHANNEL.sendToServer((Object)new EquipSpellPacket(fromSlot, ""));
            }
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public void mouseMoved(double mouseX, double mouseY) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        super.mouseMoved(mouseX, mouseY);
    }

    private void highlightActiveTab(GuiGraphics gfx) {
    }

    private void drawSmall(GuiGraphics gfx, Component text, int x, int y, int color) {
        float scale = CultivationScreen.effectiveTextScale();
        gfx.pose().pushPose();
        gfx.pose().translate((float)x, (float)y, 0.0f);
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.drawString(this.font, text, 0, 0, color, false);
        gfx.pose().popPose();
    }

    private void drawAttrSmall(GuiGraphics gfx, Component text, int x, int y, int color) {
        float scale = CultivationScreen.effectiveTextScale() * 0.9f;
        gfx.pose().pushPose();
        gfx.pose().translate((float)x, (float)y, 0.0f);
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.drawString(this.font, text, 0, 0, color, false);
        gfx.pose().popPose();
    }

    private float attrTabScale() {
        return CultivationScreen.effectiveTextScale() * 0.9f;
    }

    private void drawSmallCentered(GuiGraphics gfx, Component text, int cx, int y, int color) {
        int width = this.font.width((FormattedText)text);
        int actualW = (int)((float)width * CultivationScreen.effectiveTextScale());
        this.drawSmall(gfx, text, cx - actualW / 2, y, color);
    }

    private List<String> filteredLearnedSpells(CultivationData data) {
        ArrayList<String> result = new ArrayList<String>();
        for (String sid : data.getLearnedSpells()) {
            Spell sp = Spell.byId(sid);
            if (sp == null || this.spellTypeFilter == SpellTypeFilter.PASSIVE && sp.type() != SpellType.PASSIVE || this.spellTypeFilter == SpellTypeFilter.ACTIVE && sp.type() != SpellType.ACTIVE || !this.spellElementFilter.matchesSpell(sp) || !this.spellTierFilter.matchesTier(sp.tier())) continue;
            result.add(sid);
        }
        return result;
    }

    private List<String> filteredLearnedTechniques(CultivationData data) {
        ArrayList<String> result = new ArrayList<String>();
        for (String tid : data.getLearnedTechniques()) {
            Technique t = Technique.byId(tid);
            if (t == null || !this.techElementFilter.matchesTechnique(t) || !this.techTierFilter.matchesTier(t.tier())) continue;
            result.add(tid);
        }
        return result;
    }

    private static boolean canEquipTechniqueForScreenState(CultivationData data, Technique technique) {
        if (data == null || technique == null) {
            return false;
        }
        return data.isSoulState() ? technique.isGhostDao() : technique.isHumanDao();
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

    private int renderTechFilterRow(GuiGraphics gfx, int leftX, int rightX, int y, int mouseX, int mouseY) {
        this.renderRightDropdowns(gfx, rightX, y, this.techElementFilter, this.techTierFilter, 3, 4, mouseX, mouseY);
        return 11;
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
        this.drawDropdownChip(gfx, x += elemChipW + gapBetween, y, tierChipW, 11, tierValue, this.openDropdown == tierKind, mouseX, mouseY);
        this.filterButtonRects.add(new int[]{x, y, x + tierChipW, y + 11, tierKind, 0});
    }

    private Component elementDropdownDisplay(ElementFilter value) {
        return value == ElementFilter.ALL ? Component.translatable((String)"screen.friday_cultivation.filter.element.label") : value.display();
    }

    private Component tierDropdownDisplay(TierFilter value) {
        return value == TierFilter.ALL ? Component.translatable((String)"screen.friday_cultivation.filter.tier.label") : value.display();
    }

    private int dropdownChipWidth(Component value) {
        int aw = this.chipTextWidth((Component)Component.literal((String)" \u25be"));
        return this.chipTextWidth(value) + aw + 4;
    }

    private int chipTextWidth(String text) {
        return (int)Math.ceil((float)this.font.width(text) * 0.65f);
    }

    private int chipTextWidth(Component c) {
        return (int)Math.ceil((float)this.font.width((FormattedText)c) * 0.65f);
    }

    private void drawFilterChip(GuiGraphics gfx, int x, int y, int w, int h, Component label, boolean active, int mouseX, int mouseY) {
        boolean hover;
        boolean bl = hover = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        int bg = active ? -4703686 : (hover ? -11385542 : -12635095);
        gfx.fill(x, y, x + w, y + h, -15067628);
        gfx.fill(x + 1, y + 1, x + w - 1, y + h - 1, bg);
        int color = active ? -5720 : -726312;
        int tw = this.chipTextWidth(label);
        this.drawScaled(gfx, label, x + (w - tw) / 2, y + (h - 6) / 2, color, 0.65f);
    }

    private void drawDropdownChip(GuiGraphics gfx, int x, int y, int w, int h, Component currentValue, boolean open, int mouseX, int mouseY) {
        boolean hover;
        boolean bl = hover = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
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
        this.drawScaled(gfx, (Component)Component.literal((String)arrow), textX + vw, textY, valueColor, 0.65f);
    }

    private void renderOpenDropdown(GuiGraphics gfx, int mouseX, int mouseY) {
        int selectedIdx;
        List<Component> options;
        if (this.openDropdown == 0 || this.openDropdownAnchor == null) {
            return;
        }
        this.dropdownOptionRects.clear();
        switch (this.openDropdown) {
            case 1: {
                options = Arrays.stream(ElementFilter.values()).map(ElementFilter::display).toList();
                selectedIdx = this.spellElementFilter.ordinal();
                break;
            }
            case 2: {
                options = Arrays.stream(TierFilter.values()).map(TierFilter::display).toList();
                selectedIdx = this.spellTierFilter.ordinal();
                break;
            }
            case 3: {
                options = Arrays.stream(ElementFilter.values()).map(ElementFilter::display).toList();
                selectedIdx = this.techElementFilter.ordinal();
                break;
            }
            case 4: {
                options = Arrays.stream(TierFilter.values()).map(TierFilter::display).toList();
                selectedIdx = this.techTierFilter.ordinal();
                break;
            }
            default: {
                return;
            }
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
        int i = 0;
        while (i < options.size()) {
            boolean selected;
            Component opt = options.get(i);
            boolean hover = mouseX >= popupX && mouseX < popupX + popupW && mouseY >= oy && mouseY < oy + optionH;
            boolean bl = selected = i == selectedIdx;
            int bg = hover ? -4703686 : (selected ? -11385542 : -14739438);
            gfx.fill(popupX, oy, popupX + popupW, oy + optionH, bg);
            int textColor = hover || selected ? -5720 : -726312;
            int tw = this.chipTextWidth(opt);
            this.drawScaled(gfx, opt, popupX + (popupW - tw) / 2, oy + (optionH - 6) / 2, textColor, 0.65f);
            this.dropdownOptionRects.add(new int[]{popupX, oy, popupX + popupW, oy + optionH, i++});
            oy += optionH;
        }
        gfx.pose().popPose();
    }

    private void drawScaled(GuiGraphics gfx, Component text, int x, int y, int color, float scale) {
        gfx.pose().pushPose();
        gfx.pose().translate((float)x, (float)y, 0.0f);
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.drawString(this.font, text, 0, 0, color, false);
        gfx.pose().popPose();
    }

    private void drawScaledFitting(GuiGraphics gfx, Component text, int x, int y, int maxWidth, int color, float scale) {
        float fittingScale = scale;
        int rawWidth = this.font.width((FormattedText)text);
        if (rawWidth > 0 && (float)rawWidth * fittingScale > (float)maxWidth) {
            fittingScale = Math.max(0.42f, (float)maxWidth / (float)rawWidth);
        }
        this.drawScaled(gfx, text, x, y, color, fittingScale);
    }

    private void drawSmallRightFitting(GuiGraphics gfx, Component text, int rightX, int y, int maxWidth, int color) {
        float scale = CultivationScreen.effectiveTextScale();
        int rawWidth = this.font.width((FormattedText)text);
        if (rawWidth > 0 && (float)rawWidth * scale > (float)maxWidth) {
            scale = Math.max(0.42f, (float)maxWidth / (float)rawWidth);
        }
        int actualW = (int)((float)rawWidth * scale);
        this.drawScaled(gfx, text, rightX - actualW, y, color, scale);
    }

    private boolean handleFilterClick(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }
        if (this.openDropdown != 0) {
            for (int[] r : this.dropdownOptionRects) {
                if (!(mouseX >= (double)r[0]) || !(mouseX < (double)r[2]) || !(mouseY >= (double)r[1]) || !(mouseY < (double)r[3])) continue;
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
            if (mouseX < (double)rect[0] || mouseX >= (double)rect[2] || mouseY < (double)rect[1] || mouseY >= (double)rect[3]) continue;
            int kind = rect[4];
            int valueIdx = rect[5];
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
            case 1: {
                this.spellElementFilter = ElementFilter.values()[idx];
                break;
            }
            case 2: {
                this.spellTierFilter = TierFilter.values()[idx];
                break;
            }
            case 3: {
                this.techElementFilter = ElementFilter.values()[idx];
                break;
            }
            case 4: {
                this.techTierFilter = TierFilter.values()[idx];
            }
        }
    }

    public boolean isPauseScreen() {
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.breakthroughHistoryPopupOpen && keyCode == 256) {
            this.breakthroughHistoryPopupOpen = false;
            return true;
        }
        if (this.editingName) {
            if (keyCode == 257 || keyCode == 335) {
                this.submitName();
                return true;
            }
            if (keyCode == 256) {
                this.endNameEdit();
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (keyCode == 256 && this.openDropdown != 0) {
            this.openDropdown = 0;
            this.openDropdownAnchor = null;
            return true;
        }
        InputConstants.Key cultivationKey = ClientKeybindings.OPEN_CULTIVATION_SCREEN.getKey();
        if (cultivationKey.getType() == InputConstants.Type.KEYSYM && cultivationKey.getValue() == keyCode) {
            this.onClose();
            return true;
        }
        InputConstants.Key inventoryKey = Minecraft.getInstance().options.keyInventory.getKey();
        if (inventoryKey.getType() == InputConstants.Type.KEYSYM && inventoryKey.getValue() == keyCode) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private static enum Tab {
        ATTRIBUTES,
        TECHNIQUES,
        SPELLS,
        BREAKTHROUGH;

    }

    private static enum SpellTypeFilter {
        ALL("screen.friday_cultivation.filter.type.all"),
        PASSIVE("screen.friday_cultivation.filter.type.passive"),
        ACTIVE("screen.friday_cultivation.filter.type.active");

        final String key;

        private SpellTypeFilter(String key) {
            this.key = key;
        }

        Component display() {
            return Component.translatable((String)this.key);
        }
    }

    private static enum ElementFilter {
        ALL("screen.friday_cultivation.filter.element.all"),
        METAL("element.friday_cultivation.metal"),
        WOOD("element.friday_cultivation.wood"),
        WATER("element.friday_cultivation.water"),
        FIRE("element.friday_cultivation.fire"),
        EARTH("element.friday_cultivation.earth"),
        ICE("element.friday_cultivation.ice"),
        LIGHTNING("element.friday_cultivation.lightning"),
        NONE("screen.friday_cultivation.filter.element.none"),
        SPACE("screen.friday_cultivation.filter.element.space");

        final String key;

        private ElementFilter(String key) {
            this.key = key;
        }

        Component display() {
            return Component.translatable((String)this.key);
        }

        boolean matchesSpell(Spell sp) {
            boolean isSpaceSpell;
            if (this == ALL) {
                return true;
            }
            String id = sp.id();
            boolean bl = isSpaceSpell = id.equals("shadow_step") || id.equals("nascent_soul_out_of_body") || id.equals("void_step") || id.equals("void_escape") || id.equals("divine_sense");
            if (this == SPACE) {
                return isSpaceSpell;
            }
            SpellElement spe = sp.element();
            if (this == ICE) {
                return spe == SpellElement.ICE;
            }
            if (this == NONE) {
                return spe == SpellElement.NONE && !isSpaceSpell;
            }
            if (this == WATER) {
                return spe == SpellElement.WATER;
            }
            if (this == FIRE) {
                return spe == SpellElement.FIRE || spe == SpellElement.WOOD_FIRE;
            }
            if (this == WOOD) {
                return spe == SpellElement.WOOD;
            }
            if (this == METAL) {
                return spe == SpellElement.METAL;
            }
            if (this == EARTH) {
                return spe == SpellElement.EARTH;
            }
            if (this == LIGHTNING) {
                return spe == SpellElement.LIGHTNING;
            }
            return false;
        }

        boolean matchesTechnique(Technique t) {
            if (this == ALL) {
                return true;
            }
            if (this == SPACE) {
                return false;
            }
            QiElement qe = t.primaryElement();
            return switch (this) {
                case METAL -> {
                    if (qe == QiElement.METAL) {
                        yield true;
                    }
                    yield false;
                }
                case WOOD -> {
                    if (qe == QiElement.WOOD) {
                        yield true;
                    }
                    yield false;
                }
                case WATER -> {
                    if (qe == QiElement.WATER) {
                        yield true;
                    }
                    yield false;
                }
                case FIRE -> {
                    if (qe == QiElement.FIRE) {
                        yield true;
                    }
                    yield false;
                }
                case EARTH -> {
                    if (qe == QiElement.EARTH) {
                        yield true;
                    }
                    yield false;
                }
                case ICE -> {
                    if (qe == QiElement.ICE) {
                        yield true;
                    }
                    yield false;
                }
                case LIGHTNING -> {
                    if (qe == QiElement.LIGHTNING) {
                        yield true;
                    }
                    yield false;
                }
                case NONE -> {
                    if (qe == QiElement.PURE) {
                        yield true;
                    }
                    yield false;
                }
                default -> false;
            };
        }
    }

    private static enum TierFilter {
        ALL("screen.friday_cultivation.filter.tier.all"),
        LOW("item_tier.friday_cultivation.low"),
        MID("item_tier.friday_cultivation.mid"),
        HIGH("item_tier.friday_cultivation.high"),
        SUPREME("item_tier.friday_cultivation.supreme"),
        IMMORTAL("item_tier.friday_cultivation.immortal");

        final String key;

        private TierFilter(String key) {
            this.key = key;
        }

        Component display() {
            return Component.translatable((String)this.key);
        }

        boolean matchesTier(ItemTier tier) {
            if (this == ALL) {
                return true;
            }
            return switch (this) {
                case LOW -> {
                    if (tier == ItemTier.LOW) {
                        yield true;
                    }
                    yield false;
                }
                case MID -> {
                    if (tier == ItemTier.MID) {
                        yield true;
                    }
                    yield false;
                }
                case HIGH -> {
                    if (tier == ItemTier.HIGH) {
                        yield true;
                    }
                    yield false;
                }
                case SUPREME -> {
                    if (tier == ItemTier.SUPREME) {
                        yield true;
                    }
                    yield false;
                }
                case IMMORTAL -> {
                    if (tier == ItemTier.IMMORTAL) {
                        yield true;
                    }
                    yield false;
                }
                default -> true;
            };
        }
    }

    private record BreakthroughRouteRow(Component routeName, List<RequirementPart> requirements) {
    }

    private record RequirementPart(Component text) {
    }

    private record BreakthroughVisualLine(List<Component> segments, int rawWidth) {
    }

    private record HistoryPopupLine(Component text, int color, float scale) {
    }

    private record BonusToggleRowRect(int x1, int y1, int x2, int y2, CultivationBonusCategory category) {
    }
}

