/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.Button$OnPress
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.TextColor
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.FormattedCharSequence
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  org.jetbrains.annotations.NotNull
 */
package com.friday.cultivation.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.friday.cultivation.client.screen.StatEditorScreen;
import com.friday.cultivation.client.screen.widget.CinnabarButton;
import com.friday.cultivation.client.screen.widget.CloseIconButton;
import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.Identity;
import com.friday.cultivation.cultivation.Physique;
import com.friday.cultivation.cultivation.SpiritRoot;
import com.friday.cultivation.cultivation.draw.IdentityDrawDeck;
import com.friday.cultivation.network.ChooseOriginPacket;
import com.friday.cultivation.network.ModNetwork;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class IdentityDrawScreen
extends Screen {
    private static final int PANEL_WIDTH = 318;
    private static final int PANEL_HEIGHT = 226;
    private static final int BTN_H = 18;
    private static final int PORTRAIT_SIZE = 32;
    private static final int ROOT_ICON_SIZE = 16;
    private static final int ROOT_BUTTON_SIZE = 18;
    private static final int ROOT_ICON_GAP = 5;
    private static final int SELECTOR_BTN_SIZE = 14;
    private static final int HELP_ICON_SIZE = 9;
    private static final float SMALL_TEXT_SCALE = 0.68f;
    private static final float TINY_TEXT_SCALE = 0.56f;
    private static final ResourceLocation BG_TEXTURE = new ResourceLocation((String)"friday_cultivation", (String)"textures/gui/cultivation_bg.png");
    private static final int BG_TEX_W = 320;
    private static final int BG_TEX_H = 200;
    private static final int INK_BLACK = -15067628;
    private static final int INK_SOFT = -13819625;
    private static final int INK_MUTE = -11979486;
    private static final int BORDER_DARK = -10859978;
    private static final int BORDER_LIGHT = -1516872;
    private static final int GOLD = -2047936;
    private static final int GOLD_DARK = -7378920;
    private static final int COFFEE = -9420002;
    private static final int JADE = -11093851;
    private static final int CINNABAR = -3777989;
    private static final int CINNABAR_DEEP = -7723482;
    private static final int CINNABAR_LIGHT = -2725013;
    private static final int CINNABAR_SOFT = -4966087;
    private static final int ROOT_SELECTED_EDGE = -11227478;
    private static final int CARD_BG = -571152697;
    private static final int CARD_INNER = 1728050388;
    private static final int SHADOW_HARD = -1946157056;
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

    public IdentityDrawScreen(IdentityDrawDeck deck) {
        this(deck, false);
    }

    public IdentityDrawScreen(IdentityDrawDeck deck, boolean reconfigureMode) {
        super((Component)Component.translatable((String)(reconfigureMode ? "screen.friday_cultivation.identity_draw.title_reconfigure" : "screen.friday_cultivation.identity_draw.title")));
        this.deck = deck;
        this.reconfigureMode = reconfigureMode;
        if (reconfigureMode) {
            this.applyCurrentOriginSelection();
        }
        this.refreshStarterPreview();
    }

    public void updateDeck(IdentityDrawDeck newDeck) {
        this.updateDeck(newDeck, false);
    }

    public void updateDeck(IdentityDrawDeck newDeck, boolean reconfigureMode) {
        this.deck = newDeck;
        this.reconfigureMode = reconfigureMode;
        if (reconfigureMode) {
            this.applyCurrentOriginSelection();
            this.refreshStarterPreview();
        }
    }

    protected void init() {
        super.init();
        this.clearWidgets();
        Layout layout = this.layout();
        this.addRenderableWidget(new CloseIconButton(layout.panelX + layout.panelW - 18, layout.panelY + 6, 12, b -> this.onClose()));
        int bottomButtonW = (layout.contentW - 8) / 2;
        this.addRenderableWidget(new CinnabarButton(layout.contentX, layout.buttonY, bottomButtonW, 18, (Component)Component.translatable((String)this.randomButtonKey()), b -> {
            ModNetwork.CHANNEL.sendToServer((Object)new ChooseOriginPacket(true, "", "", "", this.reconfigureMode));
            this.onClose();
        }));
        this.addSelectorButtons(layout.identityCardX + 6, layout.identityCardX + layout.identityCardW - 6, layout.identityCardY + 42, 0);
        this.addSelectorButtons(layout.physiqueCardX + 6, layout.physiqueCardX + layout.physiqueCardW - 6, layout.physiqueCardY + 18, 2);
        this.addRenderableWidget(new CinnabarButton(layout.contentX + bottomButtonW + 8, layout.buttonY, bottomButtonW, 18, (Component)Component.translatable((String)this.confirmButtonKey()), b -> {
            Identity identity = this.selectedIdentity();
            SpiritRoot root = this.selectedRoot();
            Physique physique = this.selectedPhysique();
            ModNetwork.CHANNEL.sendToServer((Object)new ChooseOriginPacket(false, identity.id(), root.id(), physique.id(), this.reconfigureMode));
            this.onClose();
        }));
        if (this.reconfigureMode) {
            int panelLeft = (this.width - 318) / 2;
            int panelTop = (this.height - 226) / 2;
            this.addRenderableWidget(new CinnabarButton(panelLeft + 6, panelTop + 6, 66, 14, (Component)Component.translatable((String)"screen.friday_cultivation.stat_editor.open"), b -> Minecraft.getInstance().setScreen((Screen)new StatEditorScreen(this))));
        }
    }

    private String randomButtonKey() {
        return this.reconfigureMode ? "screen.friday_cultivation.identity_draw.random_reconfigure" : "screen.friday_cultivation.identity_draw.random_start";
    }

    private String confirmButtonKey() {
        return this.reconfigureMode ? "screen.friday_cultivation.identity_draw.confirm_reconfigure" : "screen.friday_cultivation.identity_draw.confirm_custom";
    }

    private void addSelectorButtons(int leftX, int rightX, int y, int type) {
        this.addRenderableWidget(new EarthSelectorButton(leftX, y, 14, 14, (Component)Component.translatable((String)"screen.friday_cultivation.identity_draw.prev"), b -> this.shiftSelection(type, -1)));
        this.addRenderableWidget(new EarthSelectorButton(rightX - 14, y, 14, 14, (Component)Component.translatable((String)"screen.friday_cultivation.identity_draw.next"), b -> this.shiftSelection(type, 1)));
    }

    private void shiftSelection(int type, int delta) {
        if (type == 0) {
            this.identityIndex = IdentityDrawScreen.wrap(this.identityIndex + delta, this.identities.size());
            this.refreshStarterPreview();
        } else {
            this.physiqueIndex = IdentityDrawScreen.wrap(this.physiqueIndex + delta, this.physiques.size());
        }
    }

    private static int wrap(int value, int size) {
        if (size <= 0) {
            return 0;
        }
        int out = value % size;
        return out < 0 ? out + size : out;
    }

    private Identity selectedIdentity() {
        return this.identities.isEmpty() ? Identity.LONE_CULTIVATOR : this.identities.get(IdentityDrawScreen.wrap(this.identityIndex, this.identities.size()));
    }

    private SpiritRoot selectedRoot() {
        if (this.selectedRootPicks.contains((Object)RootPick.NONE)) {
            return SpiritRoot.HEAVENLY_HIDDEN;
        }
        if (this.selectedRootPicks.contains((Object)RootPick.ICE)) {
            return SpiritRoot.MUTANT_ICE;
        }
        if (this.selectedRootPicks.contains((Object)RootPick.LIGHTNING)) {
            return SpiritRoot.MUTANT_LIGHTNING;
        }
        EnumSet<RootPick> basics = this.selectedBasicRoots();
        return switch (basics.size()) {
            case 1 -> ((RootPick)((Object)basics.iterator().next())).singleRoot;
            case 2 -> IdentityDrawScreen.dualRoot(basics);
            case 3 -> SpiritRoot.TRIPLE;
            case 4 -> SpiritRoot.QUADRUPLE;
            case 5 -> SpiritRoot.FIVE_ROOT;
            default -> SpiritRoot.HEAVENLY_METAL;
        };
    }

    private Physique selectedPhysique() {
        return this.physiques.isEmpty() ? Physique.MORTAL_BODY : this.physiques.get(IdentityDrawScreen.wrap(this.physiqueIndex, this.physiques.size()));
    }

    private void refreshStarterPreview() {
        this.starterPreview = new ArrayList<ItemStack>(this.selectedIdentity().starterItems());
    }

    private void applyCurrentOriginSelection() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        CultivationCapability.get((Player)mc.player).ifPresent(data -> {
            int foundPhysique;
            Identity currentIdentity = Identity.byId(data.getIdentityId());
            int foundIdentity = this.identities.indexOf((Object)currentIdentity);
            if (foundIdentity >= 0) {
                this.identityIndex = foundIdentity;
            }
            if ((foundPhysique = this.physiques.indexOf((Object)data.getPhysique())) >= 0) {
                this.physiqueIndex = foundPhysique;
            }
            this.setRootPicksFromSpiritRoot(data.getSpiritRoot());
        });
    }

    public void addEntry(@NotNull GuiGraphics g, int mx, int my, float partial) {
        this.renderBackground(g);
        this.hoveredStarter = ItemStack.EMPTY;
        this.hoveredRootPick = null;
        this.hoveredPhysique = null;
        this.hoveredFooterHelp = null;
        Layout layout = this.layout();
        this.drawScreenShell(g, layout);
        this.renderIdentityCard(g, layout, mx, my);
        this.renderRootCard(g, layout, mx, my);
        this.renderPhysiqueCard(g, layout, mx, my);
        super.render(g, mx, my, partial);
        this.renderFooterHelpIcons(g, layout, mx, my);
        if (this.hoveredFooterHelp != null) {
            g.renderTooltip(this.font, (Component)Component.translatable((String)this.hoveredFooterHelp.tooltipKey(this.reconfigureMode)), mx, my);
        }
        if (this.hoveredRootPick != null) {
            g.renderTooltip(this.font, (Component)Component.translatable((String)this.hoveredRootPick.labelKey), mx, my);
        }
        if (this.hoveredPhysique != null) {
            g.renderComponentTooltip(this.font, this.buildPhysiqueTooltip(this.hoveredPhysique), mx, my);
        }
        if (!this.hoveredStarter.isEmpty()) {
            g.renderTooltip(this.font, this.hoveredStarter, mx, my);
        }
    }

    private void drawScreenShell(GuiGraphics g, Layout layout) {
        IdentityDrawScreen.drawHardShadow(g, layout.panelX, layout.panelY, layout.panelW, layout.panelH, 5);
        RenderSystem.enableBlend();
        g.blit(BG_TEXTURE, layout.panelX, layout.panelY, 0.0f, 0.0f, layout.panelW, 200, 320, 200);
        if (layout.panelH > 200) {
            int extra = layout.panelH - 200;
            g.blit(BG_TEXTURE, layout.panelX, layout.panelY + 200, 0.0f, (float)(200 - extra), layout.panelW, extra, 320, 200);
        }
        RenderSystem.disableBlend();
        g.fill(layout.panelX + 4, layout.panelY + 4, layout.panelX + layout.panelW - 4, layout.panelY + layout.panelH - 4, 587199439);
        IdentityDrawScreen.drawPanelFrame(g, layout.panelX, layout.panelY, layout.panelW, layout.panelH);
        int titleX = layout.panelX + layout.panelW / 2;
        g.drawCenteredString(this.font, this.title, titleX, layout.panelY + 8, -15067628);
    }

    private void renderIdentityCard(GuiGraphics g, Layout layout, int mx, int my) {
        Identity identity = this.selectedIdentity();
        this.drawCard(g, layout.identityCardX, layout.identityCardY, layout.identityCardW, layout.identityCardH, -3777989);
        this.drawSmall(g, (Component)Component.translatable((String)"screen.friday_cultivation.identity_draw.identity_label"), layout.identityCardX + 8, layout.identityCardY + 6, -11979486);
        int portraitX = layout.identityCardX + (layout.identityCardW - 32) / 2;
        int portraitY = layout.identityCardY + 21;
        g.fill(portraitX - 2, portraitY - 2, portraitX + 32 + 2, portraitY + 32 + 2, -1722136010);
        g.fill(portraitX - 1, portraitY - 1, portraitX + 32 + 1, portraitY + 32 + 1, -1516872);
        RenderSystem.enableBlend();
        g.blit(identity.portraitTexture(), portraitX, portraitY, 32, 32, 0.0f, 0.0f, 32, 32, 32, 32);
        RenderSystem.disableBlend();
        this.drawSmallCentered(g, (Component)Component.translatable((String)identity.translationKey()), layout.identityCardX + layout.identityCardW / 2, layout.identityCardY + 58, -9420002);
        this.drawWrappedScaled(g, (Component)Component.translatable((String)identity.descriptionKey()), layout.identityCardX + 9, layout.identityCardY + 70, layout.identityCardW - 18, 4, 0.58f, -13819625);
        int itemLabelY = layout.identityCardY + layout.identityCardH - 42;
        this.drawSmall(g, (Component)Component.translatable((String)(this.reconfigureMode ? "screen.friday_cultivation.identity_draw.starter_items_preview_only" : "screen.friday_cultivation.identity_draw.starter_items")), layout.identityCardX + 8, itemLabelY, -11979486);
        this.renderStarterItems(g, layout.identityCardX + 8, itemLabelY + 12, mx, my);
    }

    private void renderRootCard(GuiGraphics g, Layout layout, int mx, int my) {
        SpiritRoot root = this.selectedRoot();
        int accent = IdentityDrawScreen.readableRootColor(root.rarity());
        this.drawCard(g, layout.rootCardX, layout.rootCardY, layout.rootCardW, layout.rootCardH, -11093851);
        this.drawSmall(g, (Component)Component.translatable((String)"screen.friday_cultivation.identity_draw.spirit_root_label"), layout.rootCardX + 8, layout.rootCardY + 6, -11979486);
        int buttonX = IdentityDrawScreen.rootIconStartX(layout);
        int buttonY = IdentityDrawScreen.rootIconY(layout);
        for (RootPick pick : RootPick.values()) {
            boolean selected = this.selectedRootPicks.contains((Object)pick);
            boolean hovered = IdentityDrawScreen.isInsideRootPickButton(mx, my, buttonX, buttonY);
            this.drawRootPickButton(g, pick, buttonX, buttonY, selected, hovered);
            this.drawTinyCentered(g, (Component)Component.translatable((String)pick.shortLabelKey), buttonX + 9, buttonY + 18 + 4, selected ? pick.labelColor : -11979486);
            if (hovered) {
                this.hoveredRootPick = pick;
            }
            buttonX += 23;
        }
        int infoX = layout.rootCardX + 10;
        int infoY = layout.rootCardY + 50;
        int infoW = layout.rootCardW - 20;
        g.fill(infoX, infoY, infoX + infoW, infoY + 54, 1728050388);
        this.drawSmall(g, (Component)Component.translatable((String)"screen.friday_cultivation.identity_draw.root_config", (Object[])new Object[]{Component.translatable((String)IdentityDrawScreen.rootCategoryKey(root))}), infoX + 5, infoY + 4, accent);
        this.drawSmall(g, (Component)Component.translatable((String)root.translationKey()), infoX + 5, infoY + 17, accent);
        this.drawWrappedScaled(g, (Component)Component.translatable((String)root.tooltipKey()), infoX + 5, infoY + 30, infoW - 10, 2, 0.58f, -13819625);
    }

    private void renderPhysiqueCard(GuiGraphics g, Layout layout, int mx, int my) {
        Physique physique = this.selectedPhysique();
        int accent = IdentityDrawScreen.physiqueColor(physique.rarity());
        this.drawCard(g, layout.physiqueCardX, layout.physiqueCardY, layout.physiqueCardW, layout.physiqueCardH, accent);
        this.drawSmall(g, (Component)Component.translatable((String)"screen.friday_cultivation.identity_draw.physique_label"), layout.physiqueCardX + 8, layout.physiqueCardY + 6, -11979486);
        this.drawSmallCentered(g, (Component)Component.translatable((String)physique.translationKey()), layout.physiqueCardX + layout.physiqueCardW / 2, layout.physiqueCardY + 18, accent);
        this.drawWrappedScaledCentered(g, (Component)Component.translatable((String)physique.introKey()), layout.physiqueCardX + 28, layout.physiqueCardY + 29, layout.physiqueCardW - 56, 3, 0.58f, -13819625);
        if (IdentityDrawScreen.isInsidePhysiqueTextArea(mx, my, layout)) {
            this.hoveredPhysique = physique;
        }
    }

    private List<Component> buildPhysiqueTooltip(Physique physique) {
        int color = IdentityDrawScreen.physiqueColor(physique.rarity());
        ArrayList<Component> lines = new ArrayList<Component>();
        lines.add((Component)Component.translatable((String)physique.translationKey()).copy().withStyle(style -> style.withColor(TextColor.fromRgb((int)(color & 0xFFFFFF)))));
        lines.add((Component)Component.translatable((String)"screen.friday_cultivation.identity_draw.physique_tier", (Object[])new Object[]{Component.translatable((String)physique.rarity().translationKey())}).copy().withStyle(ChatFormatting.GOLD));
        lines.add((Component)Component.empty());
        lines.add((Component)Component.translatable((String)"screen.friday_cultivation.identity_draw.physique_intro").copy().withStyle(ChatFormatting.YELLOW));
        lines.add((Component)Component.translatable((String)physique.introKey()).copy().withStyle(ChatFormatting.GRAY));
        lines.add((Component)Component.empty());
        lines.add((Component)Component.translatable((String)"tooltip.friday_cultivation.section.effect").copy().withStyle(ChatFormatting.YELLOW));
        lines.add((Component)Component.translatable((String)physique.effectsKey()).copy().withStyle(ChatFormatting.GRAY));
        return lines;
    }

    private void renderFooterHelpIcons(GuiGraphics g, Layout layout, int mx, int my) {
        int bottomButtonW = (layout.contentW - 8) / 2;
        this.renderFooterHelpIcon(g, (Component)Component.translatable((String)this.randomButtonKey()), layout.contentX, layout.buttonY, bottomButtonW, FooterHelp.RANDOM, mx, my);
        this.renderFooterHelpIcon(g, (Component)Component.translatable((String)this.confirmButtonKey()), layout.contentX + bottomButtonW + 8, layout.buttonY, bottomButtonW, FooterHelp.CUSTOM, mx, my);
    }

    private void drawRootPickButton(GuiGraphics g, RootPick pick, int x, int y, boolean selected, boolean hovered) {
        int fill;
        int cx = x + 9;
        int cy = y + 9;
        if (!selected && hovered) {
            IdentityDrawScreen.drawCircle(g, cx, cy, 10, 862607944);
        }
        int n = fill = !selected && hovered ? -2008401361 : 1715023141;
        int edge = selected ? -11227478 : (hovered ? -1516872 : -1150657976);
        IdentityDrawScreen.drawCircle(g, cx, cy, 9, fill);
        IdentityDrawScreen.drawCircleOutline(g, cx, cy, 9, edge);
        RenderSystem.enableBlend();
        g.blit(pick.texture, x + 1, y + 1, 16, 16, 0.0f, 0.0f, 16, 16, 16, 16);
        RenderSystem.disableBlend();
    }

    private void renderFooterHelpIcon(GuiGraphics g, Component label, int buttonX, int buttonY, int buttonW, FooterHelp help, int mx, int my) {
        int textW = this.font.width((FormattedText)label);
        int iconX = Math.min(buttonX + buttonW - 9 - 6, buttonX + (buttonW + textW) / 2 + 5);
        int iconY = buttonY + 4;
        boolean hovered = mx >= iconX && mx < iconX + 9 && my >= iconY && my < iconY + 9;
        this.drawHelpIcon(g, iconX, iconY, hovered);
        if (hovered) {
            this.hoveredFooterHelp = help;
        }
    }

    private void drawHelpIcon(GuiGraphics g, int x, int y, boolean hovered) {
        int text = hovered ? -1 : -5924;
        int edge = hovered ? -7723482 : text;
        int fill = hovered ? 2060872555 : 1152661817;
        IdentityDrawScreen.drawCircle(g, x + 4, y + 4, 4, fill);
        IdentityDrawScreen.drawCircleOutline(g, x + 4, y + 4, 4, edge);
        this.drawScaled(g, (Component)Component.translatable((String)"screen.friday_cultivation.identity_draw.help_icon"), x + 3, y + 2, 0.58f, text);
    }

    private void renderStarterItems(GuiGraphics g, int x, int y, int mx, int my) {
        int max = Math.min(this.starterPreview.size(), 4);
        for (int i = 0; i < max; ++i) {
            ItemStack stack = this.starterPreview.get(i);
            int ix = x + i * 19;
            this.drawItemSlot(g, ix - 2, y - 2);
            g.renderItem(stack, ix, y);
            g.renderItemDecorations(this.font, stack, ix, y);
            if (mx < ix || mx >= ix + 16 || my < y || my >= y + 16) continue;
            this.hoveredStarter = stack;
        }
    }

    private void drawSection(GuiGraphics g, Component label, int x, int y, int rightX) {
        g.fill(x, y + 8, rightX, y + 9, -2006295992);
        g.fill(x, y + 3, x + 2, y + 13, -2047936);
        g.drawString(this.font, label, x + 7, y, -15067628, false);
    }

    private void drawCard(GuiGraphics g, int x, int y, int w, int h, int accent) {
        g.fill(x + 2, y + 3, x + w + 2, y + h + 3, 0x33000000);
        g.fill(x, y, x + w, y + h, -571152697);
        g.fill(x, y, x + w, y + 1, -1516872);
        g.fill(x, y + h - 1, x + w, y + h, -10859978);
        g.fill(x, y, x + 1, y + h, -1516872);
        g.fill(x + w - 1, y, x + w, y + h, -10859978);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, 0x44FFFFFF);
        g.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, 861555254);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, accent & 0xFFFFFF | 0x66000000);
    }

    private void drawItemSlot(GuiGraphics g, int x, int y) {
        g.fill(x, y, x + 20, y + 20, -1722136010);
        g.fill(x + 1, y + 1, x + 19, y + 19, -1426066220);
        g.fill(x + 2, y + 2, x + 18, y + 18, 1714168860);
    }

    private void drawWrappedScaled(GuiGraphics g, Component text, int x, int y, int maxW, int maxLines, float scale, int color) {
        int scaledWidth = Math.max(1, (int)((float)maxW / scale));
        List lines = this.font.split((FormattedText)text, scaledWidth);
        int count = Math.min(maxLines, lines.size());
        g.pose().pushPose();
        g.pose().translate((float)x, (float)y, 0.0f);
        g.pose().scale(scale, scale, 1.0f);
        for (int i = 0; i < count; ++i) {
            g.drawString(this.font, (FormattedCharSequence)lines.get(i), 0, i * 9, color, false);
        }
        g.pose().popPose();
    }

    private void drawWrappedScaledCentered(GuiGraphics g, Component text, int x, int y, int maxW, int maxLines, float scale, int color) {
        int scaledWidth = Math.max(1, (int)((float)maxW / scale));
        List lines = this.font.split((FormattedText)text, scaledWidth);
        int count = Math.min(maxLines, lines.size());
        g.pose().pushPose();
        g.pose().translate((float)x, (float)y, 0.0f);
        g.pose().scale(scale, scale, 1.0f);
        for (int i = 0; i < count; ++i) {
            FormattedCharSequence line = (FormattedCharSequence)lines.get(i);
            int lineX = Math.max(0, (scaledWidth - this.font.width(line)) / 2);
            g.drawString(this.font, line, lineX, i * 9, color, false);
        }
        g.pose().popPose();
    }

    private void drawSmall(GuiGraphics g, Component text, int x, int y, int color) {
        this.drawScaled(g, text, x, y, 0.68f, color);
    }

    private void drawTiny(GuiGraphics g, Component text, int x, int y, int color) {
        this.drawScaled(g, text, x, y, 0.56f, color);
    }

    private void drawSmallCentered(GuiGraphics g, Component text, int centerX, int y, int color) {
        int width = (int)((float)this.font.width((FormattedText)text) * 0.68f);
        this.drawSmall(g, text, centerX - width / 2, y, color);
    }

    private void drawTinyCentered(GuiGraphics g, Component text, int centerX, int y, int color) {
        int width = (int)((float)this.font.width((FormattedText)text) * 0.56f);
        this.drawTiny(g, text, centerX - width / 2, y, color);
    }

    private void drawScaled(GuiGraphics g, Component text, int x, int y, float scale, int color) {
        g.pose().pushPose();
        g.pose().translate((float)x, (float)y, 0.0f);
        g.pose().scale(scale, scale, 1.0f);
        g.drawString(this.font, text, 0, 0, color, false);
        g.pose().popPose();
    }

    public boolean calculateIngredientsPositions(double mx, double my, int button) {
        RootPick picked = this.rootPickAt((int)mx, (int)my);
        if (picked != null) {
            this.toggleRootPick(picked);
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    private RootPick rootPickAt(int mx, int my) {
        Layout layout = this.layout();
        int buttonX = IdentityDrawScreen.rootIconStartX(layout);
        int buttonY = IdentityDrawScreen.rootIconY(layout);
        for (RootPick pick : RootPick.values()) {
            if (IdentityDrawScreen.isInsideRootPickButton(mx, my, buttonX, buttonY)) {
                return pick;
            }
            buttonX += 23;
        }
        return null;
    }

    private static boolean isInsideRootPickButton(int mx, int my, int x, int y) {
        int radius = 9;
        int dx = mx - (x + radius);
        int dy = my - (y + radius);
        return dx * dx + dy * dy <= radius * radius;
    }

    private static boolean isInside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static boolean isInsidePhysiqueTextArea(int mx, int my, Layout layout) {
        int x = layout.physiqueCardX + 28;
        int y = layout.physiqueCardY + 16;
        int w = layout.physiqueCardW - 56;
        int h = 32;
        return IdentityDrawScreen.isInside(mx, my, x, y, w, h);
    }

    private static int rootIconStartX(Layout layout) {
        int rowW = RootPick.values().length * 18 + (RootPick.values().length - 1) * 5;
        return layout.rootCardX + (layout.rootCardW - rowW) / 2;
    }

    private static int rootIconY(Layout layout) {
        return layout.rootCardY + 23;
    }

    private void toggleRootPick(RootPick pick) {
        if (pick.special) {
            this.selectedRootPicks.clear();
            this.selectedRootPicks.add(pick);
            return;
        }
        this.selectedRootPicks.remove((Object)RootPick.NONE);
        this.selectedRootPicks.remove((Object)RootPick.ICE);
        this.selectedRootPicks.remove((Object)RootPick.LIGHTNING);
        if (this.selectedRootPicks.contains((Object)pick)) {
            if (this.selectedRootPicks.size() > 1) {
                this.selectedRootPicks.remove((Object)pick);
            }
        } else {
            this.selectedRootPicks.add(pick);
        }
    }

    private void setRootPicksFromSpiritRoot(SpiritRoot root) {
        this.selectedRootPicks.clear();
        switch (root) {
            case HEAVENLY_WOOD: {
                this.selectedRootPicks.add(RootPick.WOOD);
                break;
            }
            case HEAVENLY_WATER: {
                this.selectedRootPicks.add(RootPick.WATER);
                break;
            }
            case HEAVENLY_FIRE: {
                this.selectedRootPicks.add(RootPick.FIRE);
                break;
            }
            case HEAVENLY_EARTH: {
                this.selectedRootPicks.add(RootPick.EARTH);
                break;
            }
            case MUTANT_ICE: {
                this.selectedRootPicks.add(RootPick.ICE);
                break;
            }
            case MUTANT_LIGHTNING: {
                this.selectedRootPicks.add(RootPick.LIGHTNING);
                break;
            }
            case HEAVENLY_HIDDEN: {
                this.selectedRootPicks.add(RootPick.NONE);
                break;
            }
            case DUAL_METAL_WOOD: {
                this.addRootPicks(RootPick.METAL, RootPick.WOOD);
                break;
            }
            case DUAL_METAL_WATER: {
                this.addRootPicks(RootPick.METAL, RootPick.WATER);
                break;
            }
            case DUAL_METAL_FIRE: {
                this.addRootPicks(RootPick.METAL, RootPick.FIRE);
                break;
            }
            case DUAL_METAL_EARTH: {
                this.addRootPicks(RootPick.METAL, RootPick.EARTH);
                break;
            }
            case DUAL_WOOD_WATER: {
                this.addRootPicks(RootPick.WOOD, RootPick.WATER);
                break;
            }
            case DUAL_WOOD_FIRE: {
                this.addRootPicks(RootPick.WOOD, RootPick.FIRE);
                break;
            }
            case DUAL_WOOD_EARTH: {
                this.addRootPicks(RootPick.WOOD, RootPick.EARTH);
                break;
            }
            case DUAL_WATER_FIRE: {
                this.addRootPicks(RootPick.WATER, RootPick.FIRE);
                break;
            }
            case DUAL_WATER_EARTH: {
                this.addRootPicks(RootPick.WATER, RootPick.EARTH);
                break;
            }
            case DUAL_FIRE_EARTH: {
                this.addRootPicks(RootPick.FIRE, RootPick.EARTH);
                break;
            }
            case TRIPLE: {
                this.addRootPicks(RootPick.METAL, RootPick.WOOD, RootPick.WATER);
                break;
            }
            case QUADRUPLE: {
                this.addRootPicks(RootPick.METAL, RootPick.WOOD, RootPick.WATER, RootPick.FIRE);
                break;
            }
            case FIVE_ROOT: {
                this.addRootPicks(RootPick.METAL, RootPick.WOOD, RootPick.WATER, RootPick.FIRE, RootPick.EARTH);
                break;
            }
            default: {
                this.selectedRootPicks.add(RootPick.METAL);
            }
        }
    }

    private void addRootPicks(RootPick ... picks) {
        this.selectedRootPicks.clear();
        for (RootPick pick : picks) {
            this.selectedRootPicks.add(pick);
        }
    }

    private EnumSet<RootPick> selectedBasicRoots() {
        EnumSet<RootPick> basics = EnumSet.noneOf(RootPick.class);
        for (RootPick pick : this.selectedRootPicks) {
            if (pick.special) continue;
            basics.add(pick);
        }
        return basics;
    }

    private static SpiritRoot dualRoot(EnumSet<RootPick> picks) {
        if (IdentityDrawScreen.has(picks, RootPick.METAL, RootPick.WOOD)) {
            return SpiritRoot.DUAL_METAL_WOOD;
        }
        if (IdentityDrawScreen.has(picks, RootPick.METAL, RootPick.WATER)) {
            return SpiritRoot.DUAL_METAL_WATER;
        }
        if (IdentityDrawScreen.has(picks, RootPick.METAL, RootPick.FIRE)) {
            return SpiritRoot.DUAL_METAL_FIRE;
        }
        if (IdentityDrawScreen.has(picks, RootPick.METAL, RootPick.EARTH)) {
            return SpiritRoot.DUAL_METAL_EARTH;
        }
        if (IdentityDrawScreen.has(picks, RootPick.WOOD, RootPick.WATER)) {
            return SpiritRoot.DUAL_WOOD_WATER;
        }
        if (IdentityDrawScreen.has(picks, RootPick.WOOD, RootPick.FIRE)) {
            return SpiritRoot.DUAL_WOOD_FIRE;
        }
        if (IdentityDrawScreen.has(picks, RootPick.WOOD, RootPick.EARTH)) {
            return SpiritRoot.DUAL_WOOD_EARTH;
        }
        if (IdentityDrawScreen.has(picks, RootPick.WATER, RootPick.FIRE)) {
            return SpiritRoot.DUAL_WATER_FIRE;
        }
        if (IdentityDrawScreen.has(picks, RootPick.WATER, RootPick.EARTH)) {
            return SpiritRoot.DUAL_WATER_EARTH;
        }
        return SpiritRoot.DUAL_FIRE_EARTH;
    }

    private static boolean has(EnumSet<RootPick> picks, RootPick a, RootPick b) {
        return picks.contains((Object)a) && picks.contains((Object)b);
    }

    private static String rootCategoryKey(SpiritRoot root) {
        if (root == SpiritRoot.HEAVENLY_HIDDEN) {
            return "screen.friday_cultivation.identity_draw.root_category.hidden";
        }
        if (root == SpiritRoot.MUTANT_ICE || root == SpiritRoot.MUTANT_LIGHTNING) {
            return "screen.friday_cultivation.identity_draw.root_category.mutant";
        }
        if (root == SpiritRoot.TRIPLE) {
            return "screen.friday_cultivation.identity_draw.root_category.triple";
        }
        if (root == SpiritRoot.QUADRUPLE) {
            return "screen.friday_cultivation.identity_draw.root_category.quadruple";
        }
        if (root == SpiritRoot.FIVE_ROOT) {
            return "screen.friday_cultivation.identity_draw.root_category.five";
        }
        return root.name().startsWith("DUAL_") ? "screen.friday_cultivation.identity_draw.root_category.dual" : "screen.friday_cultivation.identity_draw.root_category.heavenly";
    }

    private Layout layout() {
        int panelX = (this.width - 318) / 2;
        int panelY = (this.height - 226) / 2;
        int contentX = panelX + 9;
        int contentY = panelY + 24;
        int contentW = 300;
        int leftW = 96;
        int gap = 7;
        int rightX = contentX + leftW + gap;
        int rightW = contentW - leftW - gap;
        return new Layout(panelX, panelY, 318, 226, contentX, contentY, contentW, contentX, contentY, leftW, 168, rightX, contentY, rightW, 110, rightX, contentY + 116, rightW, 52, panelY + 226 - 24);
    }

    private static void drawCircle(GuiGraphics g, int cx, int cy, int radius, int color) {
        int radiusSq = radius * radius;
        for (int y = cy - radius; y <= cy + radius; ++y) {
            for (int x = cx - radius; x <= cx + radius; ++x) {
                int dx = x - cx;
                int dy = y - cy;
                if (dx * dx + dy * dy > radiusSq) continue;
                g.fill(x, y, x + 1, y + 1, color);
            }
        }
    }

    private static void drawCircleOutline(GuiGraphics g, int cx, int cy, int radius, int color) {
        int outer = radius * radius;
        int inner = (radius - 1) * (radius - 1);
        for (int y = cy - radius; y <= cy + radius; ++y) {
            for (int x = cx - radius; x <= cx + radius; ++x) {
                int dx = x - cx;
                int dy = y - cy;
                int d = dx * dx + dy * dy;
                if (d > outer || d < inner) continue;
                g.fill(x, y, x + 1, y + 1, color);
            }
        }
    }

    private static int readableRootColor(SpiritRoot.Rarity rarity) {
        return switch (rarity) {
            default -> throw new IncompatibleClassChangeError();
            case NORMAL -> -11910085;
            case R -> -14131576;
            case SR -> -7378920;
            case SSR -> -9550964;
            case SPECIAL -> -7259601;
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
        g.fill(x - 2, y - 2, x + w + 2, y, -10859978);
        g.fill(x - 2, y + h, x + w + 2, y + h + 2, -10859978);
        g.fill(x - 2, y, x, y + h, -10859978);
        g.fill(x + w, y, x + w + 2, y + h, -10859978);
        g.fill(x, y, x + w, y + 2, -15067628);
        g.fill(x, y + h - 2, x + w, y + h, -15067628);
        g.fill(x, y, x + 2, y + h, -15067628);
        g.fill(x + w - 2, y, x + w, y + h, -15067628);
        g.fill(x + 2, y + 2, x + w - 2, y + 3, -1516872);
        g.fill(x + 2, y + h - 3, x + w - 2, y + h - 2, -1516872);
        g.fill(x + 2, y + 2, x + 3, y + h - 2, -1516872);
        g.fill(x + w - 3, y + 2, x + w - 2, y + h - 2, -1516872);
    }

    private static void drawHardShadow(GuiGraphics g, int x, int y, int w, int h, int offset) {
        g.fill(x + offset, y + h, x + w + offset, y + h + offset, -1946157056);
        g.fill(x + w, y + offset, x + w + offset, y + h, -1946157056);
    }

    public boolean shouldCloseOnEsc() {
        return true;
    }

    public boolean isPauseScreen() {
        Minecraft mc = Minecraft.getInstance();
        return !this.reconfigureMode && mc.isLocalServer();
    }

    private static enum RootPick {
        METAL("metal", SpiritRoot.HEAVENLY_METAL, false, -7378920),
        WOOD("wood", SpiritRoot.HEAVENLY_WOOD, false, -13469122),
        WATER("water", SpiritRoot.HEAVENLY_WATER, false, -13865058),
        FIRE("fire", SpiritRoot.HEAVENLY_FIRE, false, -4828110),
        EARTH("earth", SpiritRoot.HEAVENLY_EARTH, false, -7709406),
        ICE("ice", SpiritRoot.MUTANT_ICE, true, -12747375),
        LIGHTNING("lightning", SpiritRoot.MUTANT_LIGHTNING, true, -8695146),
        NONE("none", SpiritRoot.HEAVENLY_HIDDEN, true, -11910085);

        private final String labelKey;
        private final String shortLabelKey;
        private final ResourceLocation texture;
        private final SpiritRoot singleRoot;
        private final boolean special;
        private final int labelColor;

        private RootPick(String id, SpiritRoot singleRoot, boolean special, int labelColor) {
            this.labelKey = "screen.friday_cultivation.identity_draw.root_pick." + id;
            this.shortLabelKey = "screen.friday_cultivation.identity_draw.root_pick_short." + id;
            this.texture = new ResourceLocation((String)"friday_cultivation", (String)("textures/gui/spirit_root/" + id + ".png"));
            this.singleRoot = singleRoot;
            this.special = special;
            this.labelColor = labelColor;
        }
    }

    private record Layout(int panelX, int panelY, int panelW, int panelH, int contentX, int contentY, int contentW, int identityCardX, int identityCardY, int identityCardW, int identityCardH, int rootCardX, int rootCardY, int rootCardW, int rootCardH, int physiqueCardX, int physiqueCardY, int physiqueCardW, int physiqueCardH, int buttonY) {
    }

    private final class EarthSelectorButton
    extends Button {
        private EarthSelectorButton(int x, int y, int width, int height, Component msg, Button.OnPress onPress) {
            super(x, y, width, height, msg, onPress, DEFAULT_NARRATION);
        }

        protected void getCategory(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            int x = this.getX();
            int y = this.getY();
            int w = this.width;
            int h = this.height;
            boolean hovered = this.isHoveredOrFocused() && this.active;
            int bg = hovered ? -3564958 : -5932734;
            int top = hovered ? -1916790 : -3431570;
            int bottom = hovered ? -9616605 : -11127521;
            g.fill(x, y, x + w, y + h, 0x55000000);
            g.fill(x + 1, y + 1, x + w - 1, y + h - 1, bg);
            g.fill(x + 1, y + 1, x + w - 1, y + 2, top);
            g.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, bottom);
            g.fill(x + 1, y + 1, x + 2, y + h - 1, top);
            g.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, bottom);
            int iconColor = hovered ? -3896 : -12901356;
            int textW = IdentityDrawScreen.this.font.width((FormattedText)this.getMessage());
            g.drawString(IdentityDrawScreen.this.font, this.getMessage(), x + (w - textW) / 2, y + 3, iconColor, false);
        }
    }

    private static enum FooterHelp {
        RANDOM("screen.friday_cultivation.identity_draw.random_start.tooltip", "screen.friday_cultivation.identity_draw.random_reconfigure.tooltip"),
        CUSTOM("screen.friday_cultivation.identity_draw.confirm_custom.tooltip", "screen.friday_cultivation.identity_draw.confirm_reconfigure.tooltip");

        private final String normalTooltipKey;
        private final String reconfigureTooltipKey;

        private FooterHelp(String normalTooltipKey, String reconfigureTooltipKey) {
            this.normalTooltipKey = normalTooltipKey;
            this.reconfigureTooltipKey = reconfigureTooltipKey;
        }

        private String tooltipKey(boolean reconfigureMode) {
            return reconfigureMode ? this.reconfigureTooltipKey : this.normalTooltipKey;
        }
    }
}

