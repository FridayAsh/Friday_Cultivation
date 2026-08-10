package com.friday.cultivation.client.screen;

import com.friday.cultivation.identity.Identity;
import com.friday.cultivation.identity.draw.IdentityDrawDeck;
import com.friday.cultivation.physique.Physique;
import com.friday.cultivation.spirit.SpiritRoot;
import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.network.ChooseOriginPacket;
import com.friday.cultivation.network.ModNetwork;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * 身份抽取界面 — 选择身份/灵根/体质
 * 复刻自原模组 com.xiaoxiang.cultivation.client.screen.IdentityDrawScreen
 */
public class IdentityDrawScreen extends Screen {

    private static final int PANEL_WIDTH = 318;
    private static final int PANEL_HEIGHT = 226;
    private static final int PORTRAIT_SIZE = 32;
    private static final int ROOT_ICON_SIZE = 16;
    private static final int ROOT_BUTTON_SIZE = 18;
    private static final int ROOT_ICON_GAP = 5;
    private static final int SELECTOR_BTN_SIZE = 14;

    private static final ResourceLocation BG_TEXTURE =
            new ResourceLocation("friday_cultivation", "textures/gui/cultivation_bg.png");

    private static final int INK_BLACK = 0xFF1A1A1A;
    private static final int INK_SOFT = 0xFF2C2C2C;
    private static final int INK_MUTE = 0xFF494949;
    private static final int BORDER_DARK = 0xFF5A5A5A;
    private static final int BORDER_LIGHT = 0xFFE8E8E8;
    private static final int GOLD = 0xFFE0C040;
    private static final int GOLD_DARK = 0xFF8F7018;
    private static final int COFFEE = 0xFF706050;
    private static final int JADE = 0xFF56A5A5;
    private static final int CINNABAR = 0xFFC63B3B;
    private static final int CINNABAR_DEEP = 0xFF8A2A2A;
    private static final int CINNABAR_LIGHT = 0xFFD66B6B;
    private static final int CINNABAR_SOFT = 0xFFB44A4A;
    private static final int ROOT_SELECTED_EDGE = 0xFF548AA8;
    private static final int CARD_BG = 0xDD222222;
    private static final int CARD_INNER = 0x66555555;
    private static final int SHADOW_HARD = 0x8C000000;

    private IdentityDrawDeck deck;
    private boolean reconfigureMode;
    private final List<Identity> identities = Identity.selectableOrigins();
    private final List<Physique> physiques = Physique.selectableValues();
    private final EnumSet<RootPick> selectedRootPicks = EnumSet.of(RootPick.METAL);
    private int identityIndex = 0;
    private int physiqueIndex = 0;
    private List<ItemStack> starterPreview = List.of();
    private ItemStack hoveredStarter = ItemStack.EMPTY;
    private RootPick hoveredRootPick;
    private Physique hoveredPhysique;
    private FooterHelp hoveredFooterHelp;

    // ═══════════════════════════════════════════
    // 内部枚举
    // ═══════════════════════════════════════════

    enum RootPick {
        METAL("screen.friday_cultivation.identity_draw.root_pick.metal",
              "screen.friday_cultivation.identity_draw.root_pick.metal_short",
              0xFFFFDD55, false, SpiritRoot.HEAVENLY_METAL,
              "textures/gui/spirit_root/metal.png"),
        WOOD("screen.friday_cultivation.identity_draw.root_pick.wood",
             "screen.friday_cultivation.identity_draw.root_pick.wood_short",
             0xFF55CC55, false, SpiritRoot.HEAVENLY_WOOD,
              "textures/gui/spirit_root/wood.png"),
        WATER("screen.friday_cultivation.identity_draw.root_pick.water",
              "screen.friday_cultivation.identity_draw.root_pick.water_short",
              0xFF5588FF, false, SpiritRoot.HEAVENLY_WATER,
              "textures/gui/spirit_root/water.png"),
        FIRE("screen.friday_cultivation.identity_draw.root_pick.fire",
             "screen.friday_cultivation.identity_draw.root_pick.fire_short",
             0xFFFF5544, false, SpiritRoot.HEAVENLY_FIRE,
              "textures/gui/spirit_root/fire.png"),
        EARTH("screen.friday_cultivation.identity_draw.root_pick.earth",
              "screen.friday_cultivation.identity_draw.root_pick.earth_short",
              0xFFCCAA44, false, SpiritRoot.HEAVENLY_EARTH,
              "textures/gui/spirit_root/earth.png"),
        ICE("screen.friday_cultivation.identity_draw.root_pick.ice",
            "screen.friday_cultivation.identity_draw.root_pick.ice_short",
            0xFF88CCFF, true, null,
            "textures/gui/spirit_root/ice.png"),
        LIGHTNING("screen.friday_cultivation.identity_draw.root_pick.lightning",
                  "screen.friday_cultivation.identity_draw.root_pick.lightning_short",
                  0xFFCC88FF, true, null,
                  "textures/gui/spirit_root/lightning.png"),
        NONE("screen.friday_cultivation.identity_draw.root_pick.none",
             "screen.friday_cultivation.identity_draw.root_pick.none_short",
             0xFF888888, true, null,
             "textures/gui/spirit_root/none.png");

        final String labelKey;
        final String shortLabelKey;
        final int labelColor;
        final boolean special;
        final SpiritRoot singleRoot;
        final ResourceLocation texture;

        RootPick(String labelKey, String shortLabelKey, int labelColor, boolean special,
                 SpiritRoot singleRoot, String texturePath) {
            this.labelKey = labelKey;
            this.shortLabelKey = shortLabelKey;
            this.labelColor = labelColor;
            this.special = special;
            this.singleRoot = singleRoot;
            this.texture = new ResourceLocation("friday_cultivation", texturePath);
        }
    }

    enum FooterHelp {
        RANDOM("screen.friday_cultivation.identity_draw.footer_help.random"),
        CUSTOM("screen.friday_cultivation.identity_draw.footer_help.custom");

        final String tooltipKey;

        FooterHelp(String tooltipKey) {
            this.tooltipKey = tooltipKey;
        }

        String tooltipKey(boolean reconfigure) {
            return reconfigure ? tooltipKey + "_reconfigure" : tooltipKey;
        }
    }

    record Layout(int panelX, int panelY, int panelW, int panelH,
                  int contentX, int contentY, int contentW,
                  int identityCardX, int identityCardY, int identityCardW, int identityCardH,
                  int rootCardX, int rootCardY, int rootCardW, int rootCardH,
                  int physiqueCardX, int physiqueCardY, int physiqueCardW, int physiqueCardH,
                  int buttonY) {
    }

    // ═══════════════════════════════════════════
    // 构造函数
    // ═══════════════════════════════════════════

    public IdentityDrawScreen(IdentityDrawDeck deck) {
        this(deck, false);
    }

    public IdentityDrawScreen(IdentityDrawDeck deck, boolean reconfigureMode) {
        super(Component.translatable(reconfigureMode
                ? "screen.friday_cultivation.identity_draw.title_reconfigure"
                : "screen.friday_cultivation.identity_draw.title"));
        this.deck = deck;
        this.reconfigureMode = reconfigureMode;
        if (reconfigureMode) {
            applyCurrentOriginSelection();
        }
        refreshStarterPreview();
    }

    public void updateDeck(IdentityDrawDeck newDeck) {
        updateDeck(newDeck, false);
    }

    public void updateDeck(IdentityDrawDeck newDeck, boolean reconfigureMode) {
        this.deck = newDeck;
        this.reconfigureMode = reconfigureMode;
        if (reconfigureMode) {
            applyCurrentOriginSelection();
            refreshStarterPreview();
        }
    }

    // ═══════════════════════════════════════════
    // 初始化
    // ═══════════════════════════════════════════

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();
        Layout layout = layout();

        // 关闭按钮
        addRenderableWidget(new CloseButton(layout.panelX + layout.panelW - 18, layout.panelY + 6, 12, () -> onClose()));

        int bottomButtonW = (layout.contentW - 8) / 2;

        // 随机按钮
        addRenderableWidget(new CinnabarButton(layout.contentX, layout.buttonY, bottomButtonW, 18,
                Component.translatable(randomButtonKey()), () -> {
            ModNetwork.CHANNEL.sendToServer(new ChooseOriginPacket(true, "", "", "", reconfigureMode));
            onClose();
        }));

        // 身份翻页按钮
        addSelectorButtons(layout.identityCardX + 6, layout.identityCardX + layout.identityCardW - 6,
                layout.identityCardY + 42, 0);

        // 体质翻页按钮
        addSelectorButtons(layout.physiqueCardX + 6, layout.physiqueCardX + layout.physiqueCardW - 6,
                layout.physiqueCardY + 18, 2);

        // 确认按钮
        addRenderableWidget(new CinnabarButton(layout.contentX + bottomButtonW + 8, layout.buttonY,
                bottomButtonW, 18, Component.translatable(confirmButtonKey()), () -> {
            Identity identity = selectedIdentity();
            SpiritRoot root = selectedRoot();
            Physique physique = selectedPhysique();
            ModNetwork.CHANNEL.sendToServer(new ChooseOriginPacket(
                    false, identity.id(), root.id(), physique.id(), reconfigureMode));
            onClose();
        }));
    }

    private String randomButtonKey() {
        return reconfigureMode
                ? "screen.friday_cultivation.identity_draw.random_reconfigure"
                : "screen.friday_cultivation.identity_draw.random_start";
    }

    private String confirmButtonKey() {
        return reconfigureMode
                ? "screen.friday_cultivation.identity_draw.confirm_reconfigure"
                : "screen.friday_cultivation.identity_draw.confirm_custom";
    }

    private void addSelectorButtons(int leftX, int rightX, int y, int type) {
        addRenderableWidget(new ArrowButton(leftX, y, 14, 14,
                Component.translatable("screen.friday_cultivation.identity_draw.prev"),
                () -> shiftSelection(type, -1)));
        addRenderableWidget(new ArrowButton(rightX - 14, y, 14, 14,
                Component.translatable("screen.friday_cultivation.identity_draw.next"),
                () -> shiftSelection(type, 1)));
    }

    private void shiftSelection(int type, int delta) {
        if (type == 0) {
            identityIndex = wrap(identityIndex + delta, identities.size());
            refreshStarterPreview();
        } else {
            physiqueIndex = wrap(physiqueIndex + delta, physiques.size());
        }
    }

    private static int wrap(int value, int size) {
        if (size <= 0) return 0;
        int out = value % size;
        return out < 0 ? out + size : out;
    }

    // ═══════════════════════════════════════════
    // 选择
    // ═══════════════════════════════════════════

    private Identity selectedIdentity() {
        return identities.isEmpty() ? Identity.LONE_CULTIVATOR
                : identities.get(wrap(identityIndex, identities.size()));
    }

    private SpiritRoot selectedRoot() {
        if (selectedRootPicks.contains(RootPick.NONE)) return SpiritRoot.HEAVENLY_HIDDEN;
        if (selectedRootPicks.contains(RootPick.ICE)) return SpiritRoot.MUTANT_ICE;
        if (selectedRootPicks.contains(RootPick.LIGHTNING)) return SpiritRoot.MUTANT_LIGHTNING;

        EnumSet<RootPick> basics = selectedBasicRoots();
        return switch (basics.size()) {
            case 1 -> basics.iterator().next().singleRoot;
            case 2 -> dualRoot(basics);
            case 3 -> SpiritRoot.TRIPLE;
            case 4 -> SpiritRoot.QUADRUPLE;
            case 5 -> SpiritRoot.FIVE_ROOT;
            default -> SpiritRoot.HEAVENLY_METAL;
        };
    }

    private Physique selectedPhysique() {
        return physiques.isEmpty() ? Physique.MORTAL_BODY
                : physiques.get(wrap(physiqueIndex, physiques.size()));
    }

    private void refreshStarterPreview() {
        starterPreview = new ArrayList<>(selectedIdentity().starterItems());
    }

    private void applyCurrentOriginSelection() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        CultivationData ic = CultivationCapability.get(mc.player).orElse(null);
        if (ic != null) {
            Identity currentIdentity = Identity.byId(ic.getIdentityId());
            int found = identities.indexOf(currentIdentity);
            if (found >= 0) identityIndex = found;
            int foundP = physiques.indexOf(ic.getPhysique());
            if (foundP >= 0) physiqueIndex = foundP;
            setRootPicksFromSpiritRoot(ic.getSpiritRoot());
        }
    }

    // ═══════════════════════════════════════════
    // 灵根选择逻辑
    // ═══════════════════════════════════════════

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        RootPick picked = rootPickAt((int) mx, (int) my);
        if (picked != null) {
            toggleRootPick(picked);
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    private RootPick rootPickAt(int mx, int my) {
        Layout layout = layout();
        int buttonX = rootIconStartX(layout);
        int buttonY = rootIconY(layout);
        for (RootPick pick : RootPick.values()) {
            if (isInsideCircle(mx, my, buttonX + 9, buttonY + 9, 9)) return pick;
            buttonX += 23;
        }
        return null;
    }

    private static boolean isInsideCircle(int mx, int my, int cx, int cy, int radius) {
        int dx = mx - cx;
        int dy = my - cy;
        return dx * dx + dy * dy <= radius * radius;
    }

    private void toggleRootPick(RootPick pick) {
        if (pick.special) {
            selectedRootPicks.clear();
            selectedRootPicks.add(pick);
            return;
        }
        selectedRootPicks.remove(RootPick.NONE);
        selectedRootPicks.remove(RootPick.ICE);
        selectedRootPicks.remove(RootPick.LIGHTNING);
        if (selectedRootPicks.contains(pick)) {
            if (selectedRootPicks.size() > 1) selectedRootPicks.remove(pick);
        } else {
            selectedRootPicks.add(pick);
        }
    }

    private void setRootPicksFromSpiritRoot(SpiritRoot root) {
        selectedRootPicks.clear();
        if (root == SpiritRoot.HEAVENLY_METAL || root == SpiritRoot.NONE) selectedRootPicks.add(RootPick.METAL);
        else if (root == SpiritRoot.HEAVENLY_WOOD) selectedRootPicks.add(RootPick.WOOD);
        else if (root == SpiritRoot.HEAVENLY_WATER) selectedRootPicks.add(RootPick.WATER);
        else if (root == SpiritRoot.HEAVENLY_FIRE) selectedRootPicks.add(RootPick.FIRE);
        else if (root == SpiritRoot.HEAVENLY_EARTH) selectedRootPicks.add(RootPick.EARTH);
        else if (root == SpiritRoot.MUTANT_ICE) selectedRootPicks.add(RootPick.ICE);
        else if (root == SpiritRoot.MUTANT_LIGHTNING) selectedRootPicks.add(RootPick.LIGHTNING);
        else if (root == SpiritRoot.HEAVENLY_HIDDEN) selectedRootPicks.add(RootPick.NONE);
        else if (root == SpiritRoot.DUAL_METAL_WOOD) addRootPicks(RootPick.METAL, RootPick.WOOD);
        else if (root == SpiritRoot.DUAL_METAL_WATER) addRootPicks(RootPick.METAL, RootPick.WATER);
        else if (root == SpiritRoot.DUAL_METAL_FIRE) addRootPicks(RootPick.METAL, RootPick.FIRE);
        else if (root == SpiritRoot.DUAL_METAL_EARTH) addRootPicks(RootPick.METAL, RootPick.EARTH);
        else if (root == SpiritRoot.DUAL_WOOD_WATER) addRootPicks(RootPick.WOOD, RootPick.WATER);
        else if (root == SpiritRoot.DUAL_WOOD_FIRE) addRootPicks(RootPick.WOOD, RootPick.FIRE);
        else if (root == SpiritRoot.DUAL_WOOD_EARTH) addRootPicks(RootPick.WOOD, RootPick.EARTH);
        else if (root == SpiritRoot.DUAL_WATER_FIRE) addRootPicks(RootPick.WATER, RootPick.FIRE);
        else if (root == SpiritRoot.DUAL_WATER_EARTH) addRootPicks(RootPick.WATER, RootPick.EARTH);
        else if (root == SpiritRoot.DUAL_FIRE_EARTH) addRootPicks(RootPick.FIRE, RootPick.EARTH);
        else if (root == SpiritRoot.TRIPLE) addRootPicks(RootPick.METAL, RootPick.WOOD, RootPick.WATER);
        else if (root == SpiritRoot.QUADRUPLE) addRootPicks(RootPick.METAL, RootPick.WOOD, RootPick.WATER, RootPick.FIRE);
        else if (root == SpiritRoot.FIVE_ROOT) addRootPicks(RootPick.METAL, RootPick.WOOD, RootPick.WATER, RootPick.FIRE, RootPick.EARTH);
        else selectedRootPicks.add(RootPick.METAL);
    }

    private void addRootPicks(RootPick... picks) {
        selectedRootPicks.clear();
        for (RootPick p : picks) selectedRootPicks.add(p);
    }

    private EnumSet<RootPick> selectedBasicRoots() {
        EnumSet<RootPick> basics = EnumSet.noneOf(RootPick.class);
        for (RootPick pick : selectedRootPicks) {
            if (!pick.special) basics.add(pick);
        }
        return basics;
    }

    private static SpiritRoot dualRoot(EnumSet<RootPick> picks) {
        if (has(picks, RootPick.METAL, RootPick.WOOD)) return SpiritRoot.DUAL_METAL_WOOD;
        if (has(picks, RootPick.METAL, RootPick.WATER)) return SpiritRoot.DUAL_METAL_WATER;
        if (has(picks, RootPick.METAL, RootPick.FIRE)) return SpiritRoot.DUAL_METAL_FIRE;
        if (has(picks, RootPick.METAL, RootPick.EARTH)) return SpiritRoot.DUAL_METAL_EARTH;
        if (has(picks, RootPick.WOOD, RootPick.WATER)) return SpiritRoot.DUAL_WOOD_WATER;
        if (has(picks, RootPick.WOOD, RootPick.FIRE)) return SpiritRoot.DUAL_WOOD_FIRE;
        if (has(picks, RootPick.WOOD, RootPick.EARTH)) return SpiritRoot.DUAL_WOOD_EARTH;
        if (has(picks, RootPick.WATER, RootPick.FIRE)) return SpiritRoot.DUAL_WATER_FIRE;
        if (has(picks, RootPick.WATER, RootPick.EARTH)) return SpiritRoot.DUAL_WATER_EARTH;
        return SpiritRoot.DUAL_FIRE_EARTH;
    }

    private static boolean has(EnumSet<RootPick> picks, RootPick a, RootPick b) {
        return picks.contains(a) && picks.contains(b);
    }

    // ═══════════════════════════════════════════
    // 渲染
    // ═══════════════════════════════════════════

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float partial) {
        renderBackground(g);
        hoveredStarter = ItemStack.EMPTY;
        hoveredRootPick = null;
        hoveredPhysique = null;
        hoveredFooterHelp = null;
        Layout layout = layout();
        drawScreenShell(g, layout);
        renderIdentityCard(g, layout, mx, my);
        renderRootCard(g, layout, mx, my);
        renderPhysiqueCard(g, layout, mx, my);
        super.render(g, mx, my, partial);
        renderFooterHelpIcons(g, layout, mx, my);

        if (hoveredFooterHelp != null)
            g.renderTooltip(font, Component.translatable(hoveredFooterHelp.tooltipKey(reconfigureMode)), mx, my);
        if (hoveredRootPick != null)
            g.renderTooltip(font, Component.translatable(hoveredRootPick.labelKey), mx, my);
        if (hoveredPhysique != null)
            g.renderComponentTooltip(font, buildPhysiqueTooltip(hoveredPhysique), mx, my);
        if (!hoveredStarter.isEmpty())
            g.renderTooltip(font, hoveredStarter, mx, my);
    }

    private void drawScreenShell(GuiGraphics g, Layout layout) {
        drawHardShadow(g, layout.panelX, layout.panelY, layout.panelW, layout.panelH, 5);
        RenderSystem.enableBlend();
        g.blit(BG_TEXTURE, layout.panelX, layout.panelY, 0.0f, 0.0f, layout.panelW, 200, 320, 200);
        if (layout.panelH > 200) {
            int extra = layout.panelH - 200;
            g.blit(BG_TEXTURE, layout.panelX, layout.panelY + 200, 0.0f, (float)(200 - extra), layout.panelW, extra, 320, 200);
        }
        RenderSystem.disableBlend();
        g.fill(layout.panelX + 4, layout.panelY + 4, layout.panelX + layout.panelW - 4, layout.panelY + layout.panelH - 4, 0x2300000F);
        drawPanelFrame(g, layout.panelX, layout.panelY, layout.panelW, layout.panelH);
        int titleX = layout.panelX + layout.panelW / 2;
        g.drawCenteredString(font, title, titleX, layout.panelY + 8, INK_BLACK);
    }

    private void renderIdentityCard(GuiGraphics g, Layout layout, int mx, int my) {
        Identity identity = selectedIdentity();
        drawCard(g, layout.identityCardX, layout.identityCardY, layout.identityCardW, layout.identityCardH, CINNABAR);
        drawSmall(g, Component.translatable("screen.friday_cultivation.identity_draw.identity_label"),
                layout.identityCardX + 8, layout.identityCardY + 6, INK_MUTE);

        int portraitX = layout.identityCardX + (layout.identityCardW - PORTRAIT_SIZE) / 2;
        int portraitY = layout.identityCardY + 21;
        g.fill(portraitX - 2, portraitY - 2, portraitX + PORTRAIT_SIZE + 2, portraitY + PORTRAIT_SIZE + 2, 0x99666666);
        g.fill(portraitX - 1, portraitY - 1, portraitX + PORTRAIT_SIZE + 1, portraitY + PORTRAIT_SIZE + 1, BORDER_LIGHT);
        RenderSystem.enableBlend();
        g.blit(identity.portraitTexture(), portraitX, portraitY, 0, 0, PORTRAIT_SIZE, PORTRAIT_SIZE, PORTRAIT_SIZE, PORTRAIT_SIZE);
        RenderSystem.disableBlend();

        drawSmallCentered(g, Component.translatable(identity.translationKey()),
                layout.identityCardX + layout.identityCardW / 2, layout.identityCardY + 58, COFFEE);

        drawWrappedScaled(g, Component.translatable(identity.descriptionKey()),
                layout.identityCardX + 9, layout.identityCardY + 70, layout.identityCardW - 18, 4, 0.58f, INK_SOFT);

        int itemLabelY = layout.identityCardY + layout.identityCardH - 42;
        drawSmall(g, Component.translatable(reconfigureMode
                ? "screen.friday_cultivation.identity_draw.starter_items_preview_only"
                : "screen.friday_cultivation.identity_draw.starter_items"),
                layout.identityCardX + 8, itemLabelY, INK_MUTE);

        renderStarterItems(g, layout.identityCardX + 8, itemLabelY + 12, mx, my);
    }

    private void renderRootCard(GuiGraphics g, Layout layout, int mx, int my) {
        SpiritRoot root = selectedRoot();
        int accent = readableRootColor(root.rarity());
        drawCard(g, layout.rootCardX, layout.rootCardY, layout.rootCardW, layout.rootCardH, JADE);
        drawSmall(g, Component.translatable("screen.friday_cultivation.identity_draw.spirit_root_label"),
                layout.rootCardX + 8, layout.rootCardY + 6, INK_MUTE);

        int buttonX = rootIconStartX(layout);
        int buttonY = rootIconY(layout);
        for (RootPick pick : RootPick.values()) {
            boolean selected = selectedRootPicks.contains(pick);
            boolean hovered = isInsideCircle(mx, my, buttonX + 9, buttonY + 9, 10);
            drawRootPickButton(g, pick, buttonX, buttonY, selected, hovered);
            drawTinyCentered(g, Component.translatable(pick.shortLabelKey),
                    buttonX + 9, buttonY + 18 + 4, selected ? pick.labelColor : INK_MUTE);
            if (hovered) hoveredRootPick = pick;
            buttonX += 23;
        }

        int infoX = layout.rootCardX + 10;
        int infoY = layout.rootCardY + 50;
        int infoW = layout.rootCardW - 20;
        g.fill(infoX, infoY, infoX + infoW, infoY + 54, CARD_INNER);
        drawSmall(g, Component.translatable("screen.friday_cultivation.identity_draw.root_config",
                        Component.translatable(rootCategoryKey(root))), infoX + 5, infoY + 4, accent);
        drawSmall(g, Component.translatable(root.translationKey()), infoX + 5, infoY + 17, accent);
        drawWrappedScaled(g, Component.translatable(root.tooltipKey()),
                infoX + 5, infoY + 30, infoW - 10, 2, 0.58f, INK_SOFT);
    }

    private void renderPhysiqueCard(GuiGraphics g, Layout layout, int mx, int my) {
        Physique physique = selectedPhysique();
        int accent = physiqueColor(physique.rarity());
        drawCard(g, layout.physiqueCardX, layout.physiqueCardY, layout.physiqueCardW, layout.physiqueCardH, accent);
        drawSmall(g, Component.translatable("screen.friday_cultivation.identity_draw.physique_label"),
                layout.physiqueCardX + 8, layout.physiqueCardY + 6, INK_MUTE);
        drawSmallCentered(g, Component.translatable(physique.translationKey()),
                layout.physiqueCardX + layout.physiqueCardW / 2, layout.physiqueCardY + 18, accent);
        drawWrappedScaledCentered(g, Component.translatable(physique.introKey()),
                layout.physiqueCardX + 28, layout.physiqueCardY + 29, layout.physiqueCardW - 56, 3, 0.58f, INK_SOFT);

        if (isInsidePhysiqueTextArea(mx, my, layout)) hoveredPhysique = physique;
    }

    private List<Component> buildPhysiqueTooltip(Physique physique) {
        int color = physiqueColor(physique.rarity());
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable(physique.translationKey()).copy()
                .withStyle(style -> style.withColor(TextColor.fromRgb(color & 0xFFFFFF))));
        lines.add(Component.translatable("screen.friday_cultivation.identity_draw.physique_tier",
                        Component.translatable("physique.friday_cultivation.rarity." + physique.rarity().name().toLowerCase()))
                .copy().withStyle(ChatFormatting.GOLD));
        lines.add(Component.empty());
        lines.add(Component.translatable("screen.friday_cultivation.identity_draw.physique_intro")
                .copy().withStyle(ChatFormatting.YELLOW));
        lines.add(Component.translatable(physique.introKey()).copy().withStyle(ChatFormatting.GRAY));
        lines.add(Component.empty());
        lines.add(Component.translatable("tooltip.friday_cultivation.section.effect")
                .copy().withStyle(ChatFormatting.YELLOW));
        lines.add(Component.translatable(physique.effectsKey()).copy().withStyle(ChatFormatting.GRAY));
        return lines;
    }

    private void renderFooterHelpIcons(GuiGraphics g, Layout layout, int mx, int my) {
        int bottomButtonW = (layout.contentW - 8) / 2;
        renderFooterHelpIcon(g, layout.contentX, layout.buttonY, bottomButtonW, FooterHelp.RANDOM, mx, my);
        renderFooterHelpIcon(g, layout.contentX + bottomButtonW + 8, layout.buttonY, bottomButtonW, FooterHelp.CUSTOM, mx, my);
    }

    private void renderFooterHelpIcon(GuiGraphics g, int buttonX, int buttonY, int buttonW, FooterHelp help, int mx, int my) {
        int iconX = buttonX + buttonW - 9 - 6;
        int iconY = buttonY + 4;
        boolean hovered = mx >= iconX && mx < iconX + 9 && my >= iconY && my < iconY + 9;
        drawHelpIcon(g, iconX, iconY, hovered);
        if (hovered) hoveredFooterHelp = help;
    }

    private void drawHelpIcon(GuiGraphics g, int x, int y, boolean hovered) {
        int text = hovered ? 0xFFFFFFFF : 0xFFE8E8E8;
        int edge = hovered ? CINNABAR_DEEP : text;
        int fill = hovered ? 0x7ADDDDDD : 0x44BBBBBB;
        drawCircle(g, x + 4, y + 4, 4, fill);
        drawCircleOutline(g, x + 4, y + 4, 4, edge);
        drawScaled(g, Component.literal("?"), x + 3, y + 2, 0.58f, text);
    }

    private void drawRootPickButton(GuiGraphics g, RootPick pick, int x, int y, boolean selected, boolean hovered) {
        int cx = x + 9, cy = y + 9;
        if (!selected && hovered) drawCircle(g, cx, cy, 10, 0x33666666);
        int fill = (!selected && hovered) ? 0x88888888 : 0x66666666;
        int edge = selected ? ROOT_SELECTED_EDGE : (hovered ? BORDER_LIGHT : 0xBBAAAAAA);
        drawCircle(g, cx, cy, 9, fill);
        drawCircleOutline(g, cx, cy, 9, edge);
        RenderSystem.enableBlend();
        g.blit(pick.texture, x + 1, y + 1, 0, 0, ROOT_ICON_SIZE, ROOT_ICON_SIZE, ROOT_ICON_SIZE, ROOT_ICON_SIZE);
        RenderSystem.disableBlend();
    }

    private void renderStarterItems(GuiGraphics g, int x, int y, int mx, int my) {
        int max = Math.min(starterPreview.size(), 4);
        for (int i = 0; i < max; ++i) {
            ItemStack stack = starterPreview.get(i);
            int ix = x + i * 19;
            drawItemSlot(g, ix - 2, y - 2);
            g.renderItem(stack, ix, y);
            g.renderItemDecorations(font, stack, ix, y);
            if (mx >= ix && mx < ix + 16 && my >= y && my < y + 16) hoveredStarter = stack;
        }
    }

    // ═══════════════════════════════════════════
    // 绘制工具方法
    // ═══════════════════════════════════════════

    private void drawCard(GuiGraphics g, int x, int y, int w, int h, int accent) {
        // 阴影
        g.fill(x + 2, y + 3, x + w + 2, y + h + 3, 0x33000000);
        // 卡片背景 - 半透明浅灰，让背景纹理透出
        g.fill(x, y, x + w, y + h, 0x1A333333);
        // 边框
        g.fill(x, y, x + w, y + 1, 0xFFE8E8E8);
        g.fill(x, y + h - 1, x + w, y + h, 0xFF5A5A5A);
        g.fill(x, y, x + 1, y + h, 0xFFE8E8E8);
        g.fill(x + w - 1, y, x + w, y + h, 0xFF5A5A5A);
        // 顶部白色高光
        g.fill(x + 1, y + 1, x + w - 1, y + 2, 0x44FFFFFF);
        // 底部阴影
        g.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, 0x33555555);
        // 顶部 accent 高亮带
        g.fill(x + 1, y + 1, x + w - 1, y + 2, (accent & 0xFFFFFF) | 0x66000000);
    }

    private void drawItemSlot(GuiGraphics g, int x, int y) {
        g.fill(x, y, x + 20, y + 20, 0x99666666);
        g.fill(x + 1, y + 1, x + 19, y + 19, 0xAA888888);
        g.fill(x + 2, y + 2, x + 18, y + 18, 0x66666666);
    }

    private void drawWrappedScaled(GuiGraphics g, Component text, int x, int y, int maxW, int maxLines, float scale, int color) {
        int scaledWidth = Math.max(1, (int) (maxW / scale));
        List<FormattedCharSequence> lines = font.split(text, scaledWidth);
        int count = Math.min(maxLines, lines.size());
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(scale, scale, 1.0f);
        for (int i = 0; i < count; ++i)
            g.drawString(font, lines.get(i), 0, i * 9, color, false);
        g.pose().popPose();
    }

    private void drawWrappedScaledCentered(GuiGraphics g, Component text, int x, int y, int maxW, int maxLines, float scale, int color) {
        int scaledWidth = Math.max(1, (int) (maxW / scale));
        List<FormattedCharSequence> lines = font.split(text, scaledWidth);
        int count = Math.min(maxLines, lines.size());
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(scale, scale, 1.0f);
        for (int i = 0; i < count; ++i) {
            FormattedCharSequence line = lines.get(i);
            int lineX = Math.max(0, (scaledWidth - font.width(line)) / 2);
            g.drawString(font, line, lineX, i * 9, color, false);
        }
        g.pose().popPose();
    }

    private void drawSmall(GuiGraphics g, Component text, int x, int y, int color) {
        drawScaled(g, text, x, y, 0.68f, color);
    }

    private void drawTiny(GuiGraphics g, Component text, int x, int y, int color) {
        drawScaled(g, text, x, y, 0.56f, color);
    }

    private void drawSmallCentered(GuiGraphics g, Component text, int centerX, int y, int color) {
        int width = (int) (font.width(text) * 0.68f);
        drawSmall(g, text, centerX - width / 2, y, color);
    }

    private void drawTinyCentered(GuiGraphics g, Component text, int centerX, int y, int color) {
        int width = (int) (font.width(text) * 0.56f);
        drawTiny(g, text, centerX - width / 2, y, color);
    }

    private void drawScaled(GuiGraphics g, Component text, int x, int y, float scale, int color) {
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(scale, scale, 1.0f);
        g.drawString(font, text, 0, 0, color, false);
        g.pose().popPose();
    }

    private static void drawCircle(GuiGraphics g, int cx, int cy, int radius, int color) {
        int r2 = radius * radius;
        for (int dy = -radius; dy <= radius; ++dy)
            for (int dx = -radius; dx <= radius; ++dx)
                if (dx * dx + dy * dy <= r2)
                    g.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
    }

    private static void drawCircleOutline(GuiGraphics g, int cx, int cy, int radius, int color) {
        int outer = radius * radius;
        int inner = (radius - 1) * (radius - 1);
        for (int dy = -radius; dy <= radius; ++dy)
            for (int dx = -radius; dx <= radius; ++dx) {
                int d = dx * dx + dy * dy;
                if (d <= outer && d >= inner)
                    g.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
            }
    }

    private static int readableRootColor(SpiritRoot.Rarity rarity) {
        return switch (rarity) {
            case SPECIAL -> 0xFF4A90D9;
            case SSR -> 0xFF2848C8;
            case SR -> 0xFF8F7018;
            case R -> 0xFF6E8C6C;
            case NORMAL -> 0xFF919191;
        };
    }

    private static int physiqueColor(Physique.Rarity rarity) {
        return switch (rarity) {
            default -> throw new IncompatibleClassChangeError();
            case LOW -> -11910085;
            case MID -> -12619952;
            case HIGH -> -7378920;
            case SUPREME -> -7583196;
            case IMMORTAL -> -7259601;
            case SPECIAL -> -7259601;
        };
    }

    private static void drawPanelFrame(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x - 2, y - 2, x + w + 2, y, BORDER_DARK);
        g.fill(x - 2, y + h, x + w + 2, y + h + 2, BORDER_DARK);
        g.fill(x - 2, y, x, y + h, BORDER_DARK);
        g.fill(x + w, y, x + w + 2, y + h, BORDER_DARK);
        g.fill(x, y, x + w, y + 2, INK_BLACK);
        g.fill(x, y + h - 2, x + w, y + h, INK_BLACK);
        g.fill(x, y, x + 2, y + h, INK_BLACK);
        g.fill(x + w - 2, y, x + w, y + h, INK_BLACK);
        g.fill(x + 2, y + 2, x + w - 2, y + 3, BORDER_LIGHT);
        g.fill(x + 2, y + h - 3, x + w - 2, y + h - 2, BORDER_LIGHT);
        g.fill(x + 2, y + 2, x + 3, y + h - 2, BORDER_LIGHT);
        g.fill(x + w - 3, y + 2, x + w - 2, y + h - 2, BORDER_LIGHT);
    }

    private static void drawHardShadow(GuiGraphics g, int x, int y, int w, int h, int offset) {
        g.fill(x + offset, y + h, x + w + offset, y + h + offset, SHADOW_HARD);
        g.fill(x + w, y + offset, x + w + offset, y + h, SHADOW_HARD);
    }

    private static String rootCategoryKey(SpiritRoot root) {
        if (root == SpiritRoot.HEAVENLY_HIDDEN) return "screen.friday_cultivation.identity_draw.root_category.hidden";
        if (root == SpiritRoot.MUTANT_ICE || root == SpiritRoot.MUTANT_LIGHTNING)
            return "screen.friday_cultivation.identity_draw.root_category.mutant";
        if (root == SpiritRoot.TRIPLE) return "screen.friday_cultivation.identity_draw.root_category.triple";
        if (root == SpiritRoot.QUADRUPLE) return "screen.friday_cultivation.identity_draw.root_category.quadruple";
        if (root == SpiritRoot.FIVE_ROOT) return "screen.friday_cultivation.identity_draw.root_category.five";
        return root.name().startsWith("DUAL_")
                ? "screen.friday_cultivation.identity_draw.root_category.dual"
                : "screen.friday_cultivation.identity_draw.root_category.heavenly";
    }

    private static boolean isInsidePhysiqueTextArea(int mx, int my, Layout layout) {
        return mx >= layout.physiqueCardX + 28 && mx < layout.physiqueCardX + layout.physiqueCardW - 28
                && my >= layout.physiqueCardY + 16 && my < layout.physiqueCardY + 48;
    }

    private Layout layout() {
        int panelX = (width - PANEL_WIDTH) / 2;
        int panelY = (height - PANEL_HEIGHT) / 2;
        int contentX = panelX + 9;
        int contentY = panelY + 24;
        int contentW = 300;
        int leftW = 96;
        int gap = 7;
        int rightX = contentX + leftW + gap;
        int rightW = contentW - leftW - gap;
        return new Layout(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT,
                contentX, contentY, contentW,
                contentX, contentY, leftW, 168,
                rightX, contentY, rightW, 110,
                rightX, contentY + 116, rightW, 52,
                panelY + PANEL_HEIGHT - 24);
    }

    private static int rootIconStartX(Layout layout) {
        int rowW = RootPick.values().length * ROOT_BUTTON_SIZE + (RootPick.values().length - 1) * ROOT_ICON_GAP;
        return layout.rootCardX + (layout.rootCardW - rowW) / 2;
    }

    private static int rootIconY(Layout layout) {
        return layout.rootCardY + 23;
    }

    // ═══════════════════════════════════════════
    // 覆盖方法
    // ═══════════════════════════════════════════

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        Minecraft mc = Minecraft.getInstance();
        return !reconfigureMode && mc.player != null;
    }

    // ═══════════════════════════════════════════
    // 内部控件类
    // ═══════════════════════════════════════════

    private static class CloseButton extends AbstractButton {
        private final Runnable onPress;

        CloseButton(int x, int y, int size, Runnable onPress) {
            super(x, y, size, size, Component.literal("X"));
            this.onPress = onPress;
        }

        @Override
        public void onPress() {
            onPress.run();
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics g, int mx, int my, float partial) {
            int color = isHovered() ? 0xFFFF5555 : 0xFFAAAAAA;
            g.drawCenteredString(Minecraft.getInstance().font, getMessage(), getX() + width / 2, getY() + 1, color);
        }

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }

    private static class ArrowButton extends AbstractButton {
        private final Runnable onPress;

        ArrowButton(int x, int y, int w, int h, Component msg, Runnable onPress) {
            super(x, y, w, h, msg);
            this.onPress = onPress;
        }

        @Override
        public void onPress() {
            onPress.run();
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics g, int mx, int my, float partial) {
            int color = isHovered() ? 0xFFFFFFFF : 0xFFAAAAAA;
            g.fill(getX(), getY(), getX() + width, getY() + height, 0x44000000);
            g.drawCenteredString(Minecraft.getInstance().font, getMessage(), getX() + width / 2, getY() + 2, color);
        }

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }

    private final class EarthSelectorButton extends AbstractButton {
        private final Runnable onPressAction;

        private EarthSelectorButton(int x, int y, int width, int height, Component msg, Runnable onPressAction) {
            super(x, y, width, height, msg);
            this.onPressAction = onPressAction;
        }

        @Override
        public void onPress() {
            if (this.onPressAction != null) {
                this.onPressAction.run();
            }
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            int x = getX();
            int y = getY();
            int w = width;
            int h = height;
            boolean hovered = isHovered() && active;
            int bg = hovered ? 0xFFC83E72 : 0xFFA53050;
            int top = hovered ? 0xFFE3AFD1 : 0xFFCB78A6;
            int bottom = hovered ? 0xFF6D0C2A : 0xFF561F3E;
            g.fill(x, y, x + w, y + h, 0x55000000);
            g.fill(x + 1, y + 1, x + w - 1, y + h - 1, bg);
            g.fill(x + 1, y + 1, x + w - 1, y + 2, top);
            g.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, bottom);
            g.fill(x + 1, y + 1, x + 2, y + h - 1, top);
            g.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, bottom);
            int iconColor = hovered ? 0xFFFF0F08 : 0xFF3A8C44;
            int textW = Minecraft.getInstance().font.width(getMessage());
            g.drawString(Minecraft.getInstance().font, getMessage(), x + (w - textW) / 2, y + 3, iconColor, false);
        }

        @Override
        public void updateWidgetNarration(@NotNull NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }

    private static class CinnabarButton extends AbstractButton {
        private final Runnable onPress;

        CinnabarButton(int x, int y, int w, int h, Component msg, Runnable onPress) {
            super(x, y, w, h, msg);
            this.onPress = onPress;
        }

        @Override
        public void onPress() {
            onPress.run();
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics g, int mx, int my, float partial) {
            int bg = isHovered() ? 0xFFC63B3B : 0xFF8A2A2A;
            g.fill(getX(), getY(), getX() + width, getY() + height, bg);
            g.fill(getX(), getY(), getX() + width, getY() + 1, 0x44FFFFFF);
            g.fill(getX(), getY() + height - 1, getX() + width, getY() + height, 0x44000000);
            int textColor = isHovered() ? 0xFFFFDDDD : 0xFFE0C0C0;
            g.drawCenteredString(Minecraft.getInstance().font, getMessage(), getX() + width / 2, getY() + (height - 8) / 2, textColor);
        }

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }
}
