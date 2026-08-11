/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.math.Axis
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.core.Holder
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.util.FormattedCharSequence
 *  net.minecraft.world.SimpleContainer
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ArmorItem
 *  net.minecraft.world.item.BowItem
 *  net.minecraft.world.item.CrossbowItem
 *  net.minecraft.world.item.DiggerItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.ShieldItem
 *  net.minecraft.world.item.SwordItem
 *  net.minecraft.world.item.TridentItem
 *  net.minecraft.world.item.trading.MerchantOffer
 *  net.minecraft.world.item.trading.MerchantOffers
 *  net.minecraft.world.level.ItemLike
 *  org.jetbrains.annotations.NotNull
 */
package com.friday.cultivation.client.screen;

import com.mojang.math.Axis;
import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.QiElement;
import com.friday.cultivation.cultivation.alchemy.AlchemyRank;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.cultivation.realm.SubStage;
import com.friday.cultivation.cultivation.refining.RefiningRank;
import com.friday.cultivation.cultivation.sect.SectRole;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.cultivation.technique.Technique;
import com.friday.cultivation.entity.npc.SundryPricing;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.inventory.WanderingCultivatorMenu;
import com.friday.cultivation.item.SpellBookItem;
import com.friday.cultivation.item.SpiritStoneItem;
import com.friday.cultivation.item.TechniqueBookItem;
import com.friday.cultivation.network.ExecuteCultivatorTradePacket;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.RequestSectJoinDialoguePacket;
import com.friday.cultivation.network.RequestSectScreenPacket;
import com.friday.cultivation.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

public class WanderingCultivatorScreen
extends AbstractContainerScreen<WanderingCultivatorMenu> {
    private static final int BG_PAGE = -923956;
    private static final int BG_PANEL = -1517128;
    private static final int INK_BLACK = -15067628;
    private static final int INK_SOFT = -12766422;
    private static final int INK_MUTE = -9807288;
    private static final int VERMILLION = -4703686;
    private static final int VERMILLION_DEEP = -7723482;
    private static final int GOLD_BORDER = -3562934;
    private static final int GOLD_BRIGHT = -10496;
    private static final int BRUSH_DARK = -12365222;
    private static final int JADE_DARK = -14722744;
    private static final int AMBER = -4818904;
    private static final int GREEN_INK = -12950192;
    private static final int BORDER_LIGHT = -2504802;
    private static final int BORDER_DARK = -10859978;
    private static final int ICON_INK_DARK = -9556443;
    private static final int DIAMOND_RED = -6534610;
    private static final int RULE_LINE = -3886189;
    private static final int OLIVE_GOLD = -7705298;
    private static final int BROWN_ORANGE = -4885446;
    private static final int STAT_RED = -5750484;
    private static final int STAT_JADE = -13668780;
    private static final int STAT_TEAL = -13668470;
    private static final int RADAR_RING = -4478832;
    private static final int RADAR_FILL = -2143449177;
    private static final int RADAR_LINE = -13668470;
    private static final int PAGE_INK = -14081252;
    private static final int PAGE_INK_SOFT = -11911632;
    private static final int OFFER_AVAILABLE = -1517128;
    private static final int OFFER_HOVER = -1056582;
    private static final int OFFER_SELECTED = -6528;
    private static final int OFFER_LOCKED = -7638944;
    private static final int RED_BAR_TOP = -4181968;
    private static final int RED_BAR_BOT = -10481648;
    private static final int TEAL_BAR_TOP = -10440528;
    private static final int TEAL_BAR_BOT = -14659504;
    private static final int CENTER_X = 180;
    private static final int LEFT_X = 6;
    private static final int LEFT_END = 176;
    private static final int RIGHT_X = 176;
    private static final int RIGHT_END = 356;
    private static final int CONTENT_Y_TOP = 18;
    private static final int LEFT_DIVIDER_Y = 152;
    private static final int RIGHT_CONTENT_BOTTOM = 232;
    private static final int LEGACY_WIDTH = 360;
    private static final int LEGACY_HEIGHT = 240;
    private static final int DETAIL_DESIGN_WIDTH = 640;
    private static final int DETAIL_DESIGN_HEIGHT = 416;
    private static final int DETAIL_SPINE_WIDTH = 40;
    private static final int DETAIL_SAFE_MARGIN = 10;
    private static final int DETAIL_TOP_TAB_Y = 12;
    private static final int DETAIL_CONTENT_TOP = 44;
    private static final int DETAIL_LOADOUT_SLOT_SIZE = 26;
    private static final int DETAIL_LOADOUT_SLOT_STEP = 32;
    private static final int TRADE_SLOT_SIZE = 24;
    private static final int VIRTUAL_INVENTORY_SLOT_SIZE = 24;
    private static final int NPC_INVENTORY_SLOT_SIZE = 24;
    private static final int LEGACY_TOP_TAB_Y = 5;
    private static final int LEGACY_CONTENT_TOP = 24;
    private static final Tab[] TOP_TABS = new Tab[]{Tab.DETAILS, Tab.TRADE};
    private Tab currentTab = Tab.DETAILS;
    private final int[][] tabRects = new int[TOP_TABS.length][4];
    private final List<int[]> offerHitboxes = new ArrayList<int[]>();
    private int selectedOfferIdx = -1;
    private int[] buyButtonRect = new int[4];
    private final List<int[]> tradeItemHoverRects = new ArrayList<int[]>();
    private final List<int[]> npcInvHoverRects = new ArrayList<int[]>();
    private final List<VirtualSlotRect> virtualSlotRects = new ArrayList<VirtualSlotRect>();
    private final List<int[]> spellHoverRects = new ArrayList<int[]>();
    private int[] techBlockRect = new int[4];
    private int[] sundryButtonRect = new int[4];
    private int[] sectButtonRect = new int[4];
    private int[] talkButtonRect = new int[4];
    private int[] detailTradeButtonRect = new int[4];
    private int[] closeButtonRect = new int[4];
    private boolean sectButtonEnabled = true;
    private boolean sectButtonHasSect = false;
    private boolean talkButtonEnabled = false;
    private final List<DetailStackHoverRect> detailStackHoverRects = new ArrayList<DetailStackHoverRect>();
    private final List<DetailTextHoverRect> detailTextHoverRects = new ArrayList<DetailTextHoverRect>();
    private final int[][] npcZhenyuanLabelRects = new int[5][4];
    private int tradeScrollOffset = 0;
    private int spellScrollOffset = 0;
    private DetailViewport activeDetailViewport = null;

    public WanderingCultivatorScreen(WanderingCultivatorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 360;
        this.imageHeight = 240;
    }

    protected void init() {
        this.imageWidth = this.legacyImageWidth();
        this.imageHeight = this.legacyImageHeight();
        super.init();
        this.titleLabelX = 8;
        this.titleLabelY = 4;
        this.inventoryLabelY = 250;
        this.inventoryLabelX = 8;
        this.updateSlotVisibility();
    }

    private void updateSlotVisibility() {
        ((WanderingCultivatorMenu)this.menu).setPlayerSlotsVisible(false);
        ((WanderingCultivatorMenu)this.menu).setSellSlotVisible(false);
    }

    private int legacyImageWidth() {
        return Math.min(360, Math.max(300, this.width - 56));
    }

    private int legacyImageHeight() {
        return Math.min(240, Math.max(220, this.height - 40));
    }

    private DetailViewport detailViewport() {
        int availableW = Math.max(120, this.width - 20);
        int availableH = Math.max(120, this.height - 20);
        float scale = Math.min(1.0f, Math.min((float)availableW / 680.0f, (float)availableH / 416.0f));
        if (this.width <= 700 || this.height <= 390) {
            scale = Math.min(scale, 0.72f);
        }
        scale = Math.max(0.48f, scale);
        int drawnW = Math.round(640.0f * scale);
        int totalW = Math.round(680.0f * scale);
        int left = Math.round((float)(this.width - totalW) / 2.0f + 40.0f * scale);
        left = Math.max(Math.round(40.0f * scale) + 3, left);
        int top = Math.round(((float)this.height - 416.0f * scale) / 2.0f);
        top = Math.max(10, Math.min(top, this.height - Math.round(416.0f * scale) - 4));
        if (left + drawnW > this.width - 4) {
            left = Math.max(Math.round(40.0f * scale) + 3, this.width - drawnW - 4);
        }
        return new DetailViewport(left, top, scale);
    }

    private void moveDetailRectsToScreen(DetailViewport viewport) {
        int i;
        this.closeButtonRect = WanderingCultivatorScreen.toScreenRect(this.closeButtonRect, viewport);
        this.sectButtonRect = WanderingCultivatorScreen.toScreenRect(this.sectButtonRect, viewport);
        this.talkButtonRect = WanderingCultivatorScreen.toScreenRect(this.talkButtonRect, viewport);
        this.detailTradeButtonRect = WanderingCultivatorScreen.toScreenRect(this.detailTradeButtonRect, viewport);
        this.buyButtonRect = WanderingCultivatorScreen.toScreenRect(this.buyButtonRect, viewport);
        this.sundryButtonRect = WanderingCultivatorScreen.toScreenRect(this.sundryButtonRect, viewport);
        this.techBlockRect = WanderingCultivatorScreen.toScreenRect(this.techBlockRect, viewport);
        for (i = 0; i < this.tabRects.length; ++i) {
            this.tabRects[i] = WanderingCultivatorScreen.toScreenRect(this.tabRects[i], viewport);
        }
        WanderingCultivatorScreen.scaleRectList(this.offerHitboxes, viewport, 0, 1, 2, 3);
        WanderingCultivatorScreen.scaleRectList(this.tradeItemHoverRects, viewport, 1, 2, 3, 4);
        WanderingCultivatorScreen.scaleRectList(this.npcInvHoverRects, viewport, 1, 2, 3, 4);
        WanderingCultivatorScreen.scaleRectList(this.spellHoverRects, viewport, 1, 2, 3, 4);
        for (i = 0; i < this.virtualSlotRects.size(); ++i) {
            this.virtualSlotRects.set(i, this.virtualSlotRects.get(i).toScreen(viewport));
        }
        for (i = 0; i < this.detailStackHoverRects.size(); ++i) {
            this.detailStackHoverRects.set(i, this.detailStackHoverRects.get(i).toScreen(viewport));
        }
        for (i = 0; i < this.detailTextHoverRects.size(); ++i) {
            this.detailTextHoverRects.set(i, this.detailTextHoverRects.get(i).toScreen(viewport));
        }
        for (i = 0; i < this.npcZhenyuanLabelRects.length; ++i) {
            int[] rect = this.npcZhenyuanLabelRects[i];
            if (rect[2] <= rect[0] || rect[3] <= rect[1]) continue;
            int x = Math.round((float)viewport.left + (float)rect[0] * viewport.scale);
            int y = Math.round((float)viewport.top + (float)rect[1] * viewport.scale);
            int right = Math.round((float)viewport.left + (float)rect[2] * viewport.scale);
            int bottom = Math.round((float)viewport.top + (float)rect[3] * viewport.scale);
            this.npcZhenyuanLabelRects[i] = new int[]{x, y, Math.max(x + 1, right), Math.max(y + 1, bottom)};
        }
    }

    private static void scaleRectList(List<int[]> rects, DetailViewport viewport, int xIndex, int yIndex, int wIndex, int hIndex) {
        for (int[] rect : rects) {
            if (rect.length <= hIndex || rect[wIndex] <= 0 || rect[hIndex] <= 0) continue;
            int[] scaled = WanderingCultivatorScreen.toScreenRect(new int[]{rect[xIndex], rect[yIndex], rect[wIndex], rect[hIndex]}, viewport);
            rect[xIndex] = scaled[0];
            rect[yIndex] = scaled[1];
            rect[wIndex] = scaled[2];
            rect[hIndex] = scaled[3];
        }
    }

    private void clearNpcZhenyuanLabelRects() {
        for (int i = 0; i < this.npcZhenyuanLabelRects.length; ++i) {
            this.npcZhenyuanLabelRects[i][0] = 0;
            this.npcZhenyuanLabelRects[i][1] = 0;
            this.npcZhenyuanLabelRects[i][2] = 0;
            this.npcZhenyuanLabelRects[i][3] = 0;
        }
    }

    private static int[] toScreenRect(int[] rect, DetailViewport viewport) {
        if (rect == null || rect.length < 4 || rect[2] <= 0 || rect[3] <= 0) {
            return new int[4];
        }
        int x = Math.round((float)viewport.left + (float)rect[0] * viewport.scale);
        int y = Math.round((float)viewport.top + (float)rect[1] * viewport.scale);
        int w = Math.max(1, Math.round((float)rect[2] * viewport.scale));
        int h = Math.max(1, Math.round((float)rect[3] * viewport.scale));
        return new int[]{x, y, w, h};
    }

    /*
     * WARNING - void declaration
     */
    public void render(@NotNull GuiGraphics gfx, int mouseX, int mouseY, float partial) {
        WanderingCultivatorEntity npc;
        int rh;
        int rw;
        int ry;
        int rx;
        super.render(gfx, mouseX, mouseY, partial);
        if (this.renderSectActionTooltips(gfx, mouseX, mouseY)) {
            return;
        }
        if (this.currentTab == Tab.DETAILS || this.currentTab == Tab.TRADE || this.currentTab == Tab.ITEMS) {
            for (DetailStackHoverRect detailStackHoverRect : this.detailStackHoverRects) {
                if (!detailStackHoverRect.contains(mouseX, mouseY) || detailStackHoverRect.stack.isEmpty()) continue;
                gfx.renderTooltip(this.font, detailStackHoverRect.stack, mouseX, mouseY);
                return;
            }
            for (DetailTextHoverRect detailTextHoverRect : this.detailTextHoverRects) {
                if (!detailTextHoverRect.contains(mouseX, mouseY)) continue;
                gfx.renderComponentTooltip(this.font, detailTextHoverRect.lines, mouseX, mouseY);
                return;
            }
        }
        if (this.currentTab == Tab.TRADE) {
            for (VirtualSlotRect virtualSlotRect : this.virtualSlotRects) {
                if (!virtualSlotRect.contains(mouseX, mouseY) || virtualSlotRect.slot.getItem().isEmpty()) continue;
                gfx.renderTooltip(this.font, virtualSlotRect.slot.getItem(), mouseX, mouseY);
                return;
            }
        }
        if (this.currentTab == Tab.TRADE) {
            MerchantOffers offers = ((WanderingCultivatorMenu)this.menu).getOffers();
            for (int[] r : this.tradeItemHoverRects) {
                ItemStack target;
                int offerIdx = r[0];
                rx = r[1];
                ry = r[2];
                rw = r[3];
                rh = r[4];
                int type = r[5];
                if (mouseX < rx || mouseX >= rx + rw || mouseY < ry || mouseY >= ry + rh) continue;
                if (type == 98) {
                    int favIdx = -100 - offerIdx;
                    WanderingCultivatorEntity npc2 = ((WanderingCultivatorMenu)this.menu).getCultivator();
                    if (npc2 == null) break;
                    List<Item> favs = npc2.getFavoriteItems();
                    if (favIdx < 0 || favIdx >= favs.size()) break;
                    ItemStack stack = new ItemStack((ItemLike)favs.get(favIdx));
                    gfx.renderTooltip(this.font, stack, mouseX, mouseY);
                    break;
                }
                if (type == 99) {
                    ItemStack sellStack = ((WanderingCultivatorMenu)this.menu).getSellContainer().getItem(0);
                    ItemStack price = SundryPricing.priceFor(sellStack);
                    if (price.isEmpty()) break;
                    gfx.renderTooltip(this.font, price, mouseX, mouseY);
                    break;
                }
                if (offers == null || offerIdx < 0 || offerIdx >= offers.size()) continue;
                MerchantOffer offer = (MerchantOffer)offers.get(offerIdx);
                switch (type) {
                    case 0: {
                        target = offer.getCostA();
                        break;
                    }
                    case 1: {
                        target = offer.getCostB();
                        break;
                    }
                    default: {
                        target = offer.getResult();
                    }
                }
                if (target.isEmpty()) break;
                gfx.renderTooltip(this.font, target, mouseX, mouseY);
                break;
            }
        }
        if (this.currentTab == Tab.DETAILS && (npc = ((WanderingCultivatorMenu)this.menu).getCultivator()) != null) {
            int var6_14 = 0;
            boolean bl = false;
            while (var6_14 < 5) {
                int[] r;
                r = this.npcZhenyuanLabelRects[var6_14];
                if ((r[2] != 0 || r[3] != 0) && mouseX >= r[0] && mouseX < r[2] && mouseY >= r[1] && mouseY < r[3]) {
                    this.renderNpcZhenyuanTooltip(gfx, mouseX, mouseY, npc, var6_14);
                    break;
                }
                ++var6_14;
            }
        }
        if (this.currentTab == Tab.ITEMS) {
            WanderingCultivatorEntity wanderingCultivatorEntity;
            List<ItemStack> displayInv = ((WanderingCultivatorMenu)this.menu).getDisplayInventory();
            for (int[] r : this.npcInvHoverRects) {
                int idx = r[0];
                rx = r[1];
                ry = r[2];
                rw = r[3];
                rh = r[4];
                if (mouseX < rx || mouseX >= rx + rw || mouseY < ry || mouseY >= ry + rh || idx < 0 || idx >= displayInv.size()) continue;
                ItemStack stack = displayInv.get(idx);
                if (stack.isEmpty()) break;
                gfx.renderTooltip(this.font, stack, mouseX, mouseY);
                break;
            }
            if ((wanderingCultivatorEntity = ((WanderingCultivatorMenu)this.menu).getCultivator()) != null) {
                Technique t;
                String techId;
                List<String> spellIds = wanderingCultivatorEntity.getSpellIds();
                for (int[] r : this.spellHoverRects) {
                    int idx = r[0];
                    int rx2 = r[1];
                    int ry2 = r[2];
                    int rw2 = r[3];
                    int rh2 = r[4];
                    if (mouseX < rx2 || mouseX >= rx2 + rw2 || mouseY < ry2 || mouseY >= ry2 + rh2 || idx < 0 || idx >= spellIds.size()) continue;
                    Spell sp = Spell.byId(spellIds.get(idx));
                    if (sp == null) break;
                    gfx.renderComponentTooltip(this.font, sp.tooltipLines(false), mouseX, mouseY);
                    break;
                }
                if (this.techBlockRect[2] > 0 && mouseX >= this.techBlockRect[0] && mouseX < this.techBlockRect[0] + this.techBlockRect[2] && mouseY >= this.techBlockRect[1] && mouseY < this.techBlockRect[1] + this.techBlockRect[3] && !(techId = wanderingCultivatorEntity.getTechniqueId()).isEmpty() && (t = Technique.byId(techId)) != null) {
                    gfx.renderComponentTooltip(this.font, WanderingCultivatorScreen.buildTechniqueTooltip(t), mouseX, mouseY);
                }
            }
        }
    }

    private static List<Component> buildTechniqueTooltip(Technique t) {
        ArrayList<Component> lines = new ArrayList<Component>();
        lines.add((Component)t.displayName().copy().withStyle(ChatFormatting.GOLD));
        QiElement el = WanderingCultivatorScreen.inferTechniqueMainElement(t);
        MutableComponent tierComp = t.tier().displayName().copy().withStyle(s -> s.withColor(t.tier().rgb()));
        MutableComponent elComp = el != null ? Component.translatable((String)("element.friday_cultivation." + el.id())).copy().withStyle(s -> s.withColor(el.rgb() | 0xFF000000)) : Component.translatable((String)"element.friday_cultivation.pure").copy().withStyle(ChatFormatting.WHITE);
        lines.add((Component)Component.literal((String)"\u3010").append((Component)tierComp).append("\u3011 ").append((Component)elComp));
        MutableComponent desc = Component.translatable((String)("technique.friday_cultivation." + t.id() + ".desc")).withStyle(ChatFormatting.GRAY);
        lines.add((Component)desc);
        return lines;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void renderBg(@NotNull GuiGraphics gfx, float partial, int mouseX, int mouseY) {
        WanderingCultivatorEntity npc = ((WanderingCultivatorMenu)this.menu).getCultivator();
        DetailViewport viewport = this.detailViewport();
        int localMouseX = viewport.toLocalX(mouseX);
        int localMouseY = viewport.toLocalY(mouseY);
        gfx.pose().pushPose();
        gfx.pose().translate((float)viewport.left, (float)viewport.top, 0.0f);
        gfx.pose().scale(viewport.scale, viewport.scale, 1.0f);
        this.activeDetailViewport = viewport;
        try {
            this.renderNpcDetailPage(gfx, 0, 0, npc, localMouseX, localMouseY);
        }
        finally {
            this.activeDetailViewport = null;
        }
        gfx.pose().popPose();
        this.moveDetailRectsToScreen(viewport);
    }

    protected void renderLabels(@NotNull GuiGraphics gfx, int mouseX, int mouseY) {
    }

    private void renderNpcDetailPage(GuiGraphics gfx, int x, int y, WanderingCultivatorEntity npc, int mouseX, int mouseY) {
        int detailW = 640;
        int detailH = 416;
        this.offerHitboxes.clear();
        this.tradeItemHoverRects.clear();
        this.npcInvHoverRects.clear();
        this.spellHoverRects.clear();
        this.virtualSlotRects.clear();
        this.detailStackHoverRects.clear();
        this.detailTextHoverRects.clear();
        this.clearNpcZhenyuanLabelRects();
        this.buyButtonRect = new int[4];
        this.sundryButtonRect = new int[4];
        this.detailTradeButtonRect = new int[4];
        this.closeButtonRect = new int[4];
        this.renderCultivatorBookSpine(gfx, x, y, detailH);
        this.drawWidePaperFrame(gfx, x, y, detailW, detailH);
        this.renderRibbonBookmark(gfx, x, y, detailH);
        this.closeButtonRect = this.drawCloseButton(gfx, x + detailW - 42, y + 14, mouseX, mouseY);
        this.renderTopTabs(gfx, x + detailW / 2, y + 12, detailW - 160, mouseX, mouseY);
        if (npc == null) {
            this.drawCenteredClipped(gfx, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.detail_missing"), x + detailW / 2, y + detailH / 2, 240, -9807288);
            return;
        }
        int cardTop = y + 44;
        int cardH = detailH - 44 - 14;
        int margin = 22;
        int midGap = 18;
        int pageW = (detailW - margin * 2 - midGap) / 2;
        int leftX = x + margin;
        int rightX = x + margin + pageW + midGap;
        gfx.fill(leftX + pageW + midGap / 2 - 1, cardTop - 2, leftX + pageW + midGap / 2 + 1, cardTop + cardH, 410538032);
        this.renderDetailLeftPage(gfx, leftX, cardTop, pageW, cardH, npc, mouseX, mouseY);
        switch (this.currentTab) {
            case TRADE: {
                this.renderWideTradeCard(gfx, rightX, cardTop, pageW, cardH, npc, mouseX, mouseY);
                break;
            }
            default: {
                this.renderDetailCategoriesPage(gfx, rightX, cardTop, pageW, cardH, npc);
            }
        }
    }

    private void renderRibbonBookmark(GuiGraphics gfx, int x, int y, int detailH) {
        int rx = x + 40;
        int top = y - 2;
        gfx.fill(rx, top, rx + 12, top + 30, -7725024);
        gfx.fill(rx + 1, top, rx + 11, top + 30, -4706256);
        gfx.fill(rx + 4, top, rx + 8, top + 30, -3127738);
        gfx.fill(rx, top + 30, rx + 5, top + 35, -4706256);
        gfx.fill(rx + 7, top + 30, rx + 12, top + 35, -4706256);
        gfx.fill(rx + 5, top + 30, rx + 7, top + 33, -7725024);
    }

    private void drawWidePaperFrame(GuiGraphics gfx, int x, int y, int width, int height) {
        this.drawTiltedBackPaperFrame(gfx, x, y, width, height);
        this.drawSingleParchmentFrame(gfx, x, y, width, height, true);
    }

    private void drawTiltedBackPaperFrame(GuiGraphics gfx, int x, int y, int width, int height) {
        float pivotX = (float)x + (float)width / 2.0f;
        float pivotY = (float)y + (float)height / 2.0f;
        gfx.pose().pushPose();
        gfx.pose().translate(pivotX, pivotY, 0.0f);
        gfx.pose().mulPose(Axis.ZP.rotationDegrees(1.4f));
        gfx.pose().translate(-pivotX + 8.0f, -pivotY + 9.0f, 0.0f);
        this.drawSingleParchmentFrame(gfx, x, y, width, height, false);
        gfx.pose().popPose();
    }

    private void drawSingleParchmentFrame(GuiGraphics gfx, int x, int y, int width, int height, boolean topSheet) {
        int shadowAlpha = topSheet ? 0x66000000 : -2013265920;
        gfx.fill(x - 5, y - 4, x + width + 5, y + height + 5, shadowAlpha);
        gfx.fill(x - 3, y - 3, x + width + 3, y + height + 3, -15658735);
        gfx.fill(x - 1, y - 1, x + width + 1, y + height + 1, -2504802);
        gfx.fill(x, y, x + width, y + height, topSheet ? -790821 : -1186608);
        gfx.fill(x + 7, y + 7, x + width - 7, y + 8, 1146767926);
        gfx.fill(x + 7, y + height - 8, x + width - 7, y + height - 7, 1146767926);
        gfx.fill(x + 7, y + 7, x + 8, y + height - 7, 1146767926);
        gfx.fill(x + width - 8, y + 7, x + width - 7, y + height - 7, 1146767926);
        gfx.fill(x + 10, y + 10, x + width - 10, y + 11, 578841422);
        gfx.fill(x + 10, y + height - 11, x + width - 10, y + height - 10, 578841422);
        if (!topSheet) {
            return;
        }
        for (int i = 0; i < 58; ++i) {
            int sx = x + 24 + i * 37 % Math.max(48, width - 48);
            int sy = y + 16 + i * 23 % Math.max(48, height - 42);
            gfx.fill(sx, sy, sx + 8, sy + 1, 312773986);
        }
        gfx.fill(x + 36, y - 3, x + 72, y - 2, -15658735);
        gfx.fill(x + width / 2 - 18, y - 3, x + width / 2 + 26, y - 2, -15658735);
        gfx.fill(x + width - 104, y - 3, x + width - 58, y - 2, -15658735);
        gfx.fill(x + 70, y + height + 2, x + 116, y + height + 3, -15658735);
        gfx.fill(x + width / 2 + 24, y + height + 2, x + width / 2 + 72, y + height + 3, -15658735);
    }

    private int[] drawCloseButton(GuiGraphics gfx, int x, int y, int mouseX, int mouseY) {
        boolean hover = mouseX >= x && mouseX < x + 24 && mouseY >= y && mouseY < y + 24;
        int cx = x + 12;
        int cy = y + 12;
        this.fillCircle(gfx, cx, cy, 13, -14016488);
        this.fillCircle(gfx, cx, cy, 12, hover ? -2800315 : -4703686);
        int ink = -397604;
        for (int i = 0; i < 9; ++i) {
            gfx.fill(cx - 4 + i, cy - 4 + i, cx - 2 + i, cy - 2 + i, ink);
            gfx.fill(cx + 4 - i, cy - 4 + i, cx + 6 - i, cy - 2 + i, ink);
        }
        return new int[]{x, y, 24, 24};
    }

    private void renderDetailLeftPage(GuiGraphics gfx, int x, int y, int width, int height, WanderingCultivatorEntity npc, int mouseX, int mouseY) {
        int pad = 14;
        int ix = x + pad;
        int iw = width - pad * 2;
        int avSize = 50;
        int avR = avSize / 2;
        int avCx = ix + avR;
        int avCy = y + 6 + avR;
        this.drawCircularAvatar(gfx, avCx, avCy, avR, npc, mouseX, mouseY);
        int txtX = ix + avSize + 12;
        MutableComponent realm = Component.empty().append(npc.getRealm().displayName()).append(" ").append((Component)npc.getSubStage().displayName()).copy().withStyle(ChatFormatting.BOLD);
        int realmW = this.font.width((FormattedText)realm);
        this.drawScaledComponent(gfx, (Component)npc.getCultivatorName().copy().withStyle(ChatFormatting.BOLD), txtX, y + 12, 1.3f, -14081252, iw - avSize - 12 - realmW - 6);
        gfx.drawString(this.font, (Component)realm, ix + iw - realmW, y + 14, -7705298, false);
        this.drawLabeledValue(gfx, txtX, y + 34, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.detail.cultivation"), (Component)Component.literal((String)String.valueOf(npc.getCurrentQi())), -4885446);
        MutableComponent genderValue = Component.translatable((String)(npc.getGender() == 2 ? "screen.friday_cultivation.gender.female" : "screen.friday_cultivation.gender.male"));
        this.drawRightLabeledValue(gfx, ix + iw, y + 34, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.detail.label.gender"), (Component)genderValue, -4885446);
        int cy = y + 60;
        this.drawDiamondHeader(gfx, ix, cy, iw, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.detail.profile"));
        this.drawKvRow(gfx, ix, cy += 16, iw, (Component)Component.translatable((String)"screen.friday_cultivation.sect.title"), (Component)(npc.hasSectMembership() ? Component.literal((String)npc.getSectName()) : Component.translatable((String)"sect.friday_cultivation.none")), (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.detail.label.identity"), npc.hasSectMembership() ? npc.getSectRole().displayName() : npc.getSectIdentityComponent());
        this.drawKvRow(gfx, ix, cy += 14, iw, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.detail.label.spirit_root"), (Component)Component.translatable((String)npc.getSpiritRoot().translationKey()), (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.detail.label.physique"), (Component)Component.translatable((String)npc.getPhysique().translationKey()));
        MutableComponent desc = Component.translatable((String)(npc.hasSectMembership() ? "screen.friday_cultivation.cultivator.detail.desc.sect" : "screen.friday_cultivation.cultivator.detail.desc.lone"), (Object[])new Object[]{npc.getSectIdentityComponent()});
        this.drawWrappedLimited(gfx, (Component)desc, ix, cy += 16, iw, -11911632, 2);
        this.drawDiamondHeader(gfx, ix, cy += 26, iw, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.detail.attributes"));
        RefiningRank rRank = npc.getRefiningRank();
        AlchemyRank aRank = npc.getAlchemyRank();
        this.drawStatGridRow(gfx, ix, cy += 17, iw, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.detail.hp"), (Component)Component.literal((String)((int)npc.getHealth() + "/" + (int)npc.getMaxHealth())), -5750484, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.detail.qi"), (Component)Component.literal((String)(npc.getCurrentQi() + "/" + npc.getMaxQi())), -13668470);
        this.drawStatGridRow(gfx, ix, cy += 13, iw, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.detail.attack"), (Component)Component.literal((String)String.valueOf((int)Math.round(npc.getAttributeValue(Attributes.ATTACK_DAMAGE)))), -13668780, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.detail.body_attack"), (Component)Component.literal((String)String.valueOf(WanderingCultivatorScreen.detailBodyAttack(npc))), -5750484);
        this.drawStatGridRow(gfx, ix, cy += 13, iw, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.detail.speed"), (Component)Component.literal((String)String.valueOf(WanderingCultivatorScreen.detailSpeed(npc))), -13668470, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.detail.defense"), (Component)Component.literal((String)String.valueOf(npc.getBodyDefense())), -13668780);
        this.drawStatGridRow(gfx, ix, cy += 13, iw, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.detail.qi_recovery"), (Component)Component.literal((String)String.valueOf(npc.getNaturalQiRecoveryPerSecond())), -4818904, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.detail.cultivation_eff"), (Component)Component.translatable((String)"screen.friday_cultivation.attr.cultivation_efficiency_value", (Object[])new Object[]{WanderingCultivatorScreen.detailCultivationEfficiency(npc)}), -13668780);
        this.drawStatGridRow(gfx, ix, cy += 13, iw, (Component)Component.translatable((String)"screen.friday_cultivation.attr.refining_label"), rRank.displayName(), -11911632, (Component)Component.translatable((String)"screen.friday_cultivation.attr.alchemy_label"), aRank.displayName(), -11911632);
        this.drawDiamondHeader(gfx, ix, cy += 16, iw, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.detail.zhenyuan"));
        this.renderLeftRadar(gfx, ix, cy + 12, iw, npc);
        int actionY = y + height - 20;
        int buttonGap = 12;
        int buttonW = (iw - buttonGap) / 2;
        this.sectButtonEnabled = true;
        this.sectButtonHasSect = npc.hasSectMembership();
        this.sectButtonRect = this.drawDetailActionButton(gfx, ix, actionY, buttonW, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.sect_button"), true, -14722744, mouseX, mouseY);
        this.talkButtonEnabled = npc.hasSectMembership();
        this.talkButtonRect = this.drawDetailActionButton(gfx, ix + buttonW + buttonGap, actionY, buttonW, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.talk_button"), this.talkButtonEnabled, -4818904, mouseX, mouseY);
        this.detailTradeButtonRect = new int[4];
    }

    private void drawDiamondHeader(GuiGraphics gfx, int x, int y, int w, Component label) {
        int cx = x + w / 2;
        int tw = this.font.width((FormattedText)label);
        int midY = y + 4;
        int half = tw / 2;
        int dGap = 6;
        int dHalf = 3;
        int rGap = 6;
        int leftDx = cx - half - dGap - dHalf;
        int rightDx = cx + half + dGap + dHalf;
        int leftRuleEnd = leftDx - dHalf - rGap;
        int rightRuleStart = rightDx + dHalf + rGap;
        if (leftRuleEnd > x) {
            gfx.fill(x, midY, leftRuleEnd, midY + 1, -3886189);
        }
        if (rightRuleStart < x + w) {
            gfx.fill(rightRuleStart, midY, x + w, midY + 1, -3886189);
        }
        this.drawDiamond(gfx, leftDx, midY, dHalf, -6534610);
        this.drawDiamond(gfx, rightDx, midY, dHalf, -6534610);
        gfx.drawString(this.font, (Component)label.copy().withStyle(ChatFormatting.BOLD), cx - half, y, -14081252, false);
    }

    private void drawDiamondHeaderTag(GuiGraphics gfx, int x, int y, int w, Component label, Component tag, boolean tagOn) {
        int cx = x + w / 2;
        int tw = this.font.width((FormattedText)label);
        int midY = y + 4;
        int half = tw / 2;
        int dGap = 6;
        int dHalf = 3;
        int rGap = 6;
        int leftDx = cx - half - dGap - dHalf;
        int rightDx = cx + half + dGap + dHalf;
        int leftRuleEnd = leftDx - dHalf - rGap;
        if (leftRuleEnd > x) {
            gfx.fill(x, midY, leftRuleEnd, midY + 1, -3886189);
        }
        this.drawDiamond(gfx, leftDx, midY, dHalf, -6534610);
        this.drawDiamond(gfx, rightDx, midY, dHalf, -6534610);
        gfx.drawString(this.font, (Component)label.copy().withStyle(ChatFormatting.BOLD), cx - half, y, -14081252, false);
        float s = 0.8f;
        int tagW = (int)((float)this.font.width((FormattedText)tag) * s) + 6;
        Objects.requireNonNull(this.font);
        int tagH = (int)(9.0f * s) + 3;
        int tagX = x + w - tagW;
        int tagY = y - 1;
        gfx.fill(tagX, tagY, tagX + tagW, tagY + tagH, tagOn ? -13668780 : -7438745);
        gfx.pose().pushPose();
        gfx.pose().translate((float)(tagX + 3), (float)(tagY + 2), 0.0f);
        gfx.pose().scale(s, s, 1.0f);
        gfx.drawString(this.font, tag, 0, 0, -528679, false);
        gfx.pose().popPose();
    }

    private void drawDiamond(GuiGraphics gfx, int cx, int cy, int half, int color) {
        for (int dy = -half; dy <= half; ++dy) {
            int dx = half - Math.abs(dy);
            gfx.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
        }
    }

    private void drawKvRow(GuiGraphics gfx, int x, int y, int w, Component k1, Component v1, Component k2, Component v2) {
        int half = w / 2;
        this.drawKvCell(gfx, x, y, half - 6, k1, v1);
        this.drawKvCell(gfx, x + half, y, half - 6, k2, v2);
    }

    private void drawKvCell(GuiGraphics gfx, int x, int y, int w, Component key, Component value) {
        MutableComponent k = Component.empty().append(key).append("\uff1a");
        gfx.drawString(this.font, (Component)k, x, y, -9807288, false);
        int kw = this.font.width((FormattedText)k);
        this.drawClippedComponent(gfx, value, x + kw, y, Math.max(10, w - kw), 10, -11911632);
    }

    private void drawStatGridRow(GuiGraphics gfx, int x, int y, int w, Component k1, Component v1, int c1, Component k2, Component v2, int c2) {
        int half = w / 2;
        this.drawStatCell(gfx, x, y, half - 6, k1, v1, c1);
        this.drawStatCell(gfx, x + half, y, half - 6, k2, v2, c2);
    }

    private void drawStatCell(GuiGraphics gfx, int x, int y, int w, Component label, Component value, int valueColor) {
        gfx.drawString(this.font, label, x, y, -9807288, false);
        this.drawClippedComponent(gfx, (Component)value.copy().withStyle(ChatFormatting.BOLD), x + 38, y, Math.max(10, w - 38), 10, valueColor);
    }

    private void drawLabeledValue(GuiGraphics gfx, int x, int y, Component label, Component value, int valueColor) {
        gfx.drawString(this.font, label, x, y, -9807288, false);
        int lw = this.font.width((FormattedText)label);
        gfx.drawString(this.font, (Component)value.copy().withStyle(ChatFormatting.BOLD), x + lw + 6, y, valueColor, false);
    }

    private void drawRightLabeledValue(GuiGraphics gfx, int rightX, int y, Component label, Component value, int valueColor) {
        MutableComponent v = value.copy().withStyle(ChatFormatting.BOLD);
        int vw = this.font.width((FormattedText)v);
        gfx.drawString(this.font, (Component)v, rightX - vw, y, valueColor, false);
        int lw = this.font.width((FormattedText)label);
        gfx.drawString(this.font, label, rightX - vw - 6 - lw, y, -9807288, false);
    }

    private void drawScaledComponent(GuiGraphics gfx, Component c, int x, int y, float scale, int color, int maxWidth) {
        Component draw = c;
        if ((float)this.font.width((FormattedText)c) * scale > (float)maxWidth) {
            draw = this.trimToWidth(c, Math.max(0, (int)((float)maxWidth / scale)));
        }
        gfx.pose().pushPose();
        gfx.pose().translate((float)x, (float)y, 0.0f);
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.drawString(this.font, draw, 0, 0, color, false);
        gfx.pose().popPose();
    }

    private void drawCircularAvatar(GuiGraphics gfx, int cx, int cy, int r, WanderingCultivatorEntity npc, int mouseX, int mouseY) {
        this.fillCircle(gfx, cx, cy, r, -528679);
        try {
            InventoryScreen.renderEntityInInventoryFollowsMouse((GuiGraphics)gfx, (int)cx, (int)(cy + r - 3), (int)(r - 2), (float)(cx - mouseX), (float)(cy - 2 - mouseY), (LivingEntity)npc);
        }
        catch (Exception ignored) {
            gfx.fill(cx - 8, cy - 10, cx + 8, cy + 12, -10722417);
        }
        this.maskCircleCorners(gfx, cx, cy, r, 6, -790821);
        this.drawCircleRing(gfx, cx, cy, r + 2, r, -14665650);
        this.drawCircleRing(gfx, cx, cy, r, r - 1, -3562934);
    }

    private void fillCircle(GuiGraphics gfx, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; ++dy) {
            int dx = (int)Math.round(Math.sqrt((double)r * (double)r - (double)(dy * dy)));
            gfx.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
        }
    }

    private void maskCircleCorners(GuiGraphics gfx, int cx, int cy, int r, int margin, int color) {
        for (int dy = -r - margin; dy <= r + margin; ++dy) {
            if (Math.abs(dy) <= r) {
                int dx = (int)Math.round(Math.sqrt((double)r * (double)r - (double)(dy * dy)));
                gfx.fill(cx - r - margin, cy + dy, cx - dx, cy + dy + 1, color);
                gfx.fill(cx + dx + 1, cy + dy, cx + r + margin + 1, cy + dy + 1, color);
                continue;
            }
            gfx.fill(cx - r - margin, cy + dy, cx + r + margin + 1, cy + dy + 1, color);
        }
    }

    private void drawCircleRing(GuiGraphics gfx, int cx, int cy, int rOuter, int rInner, int color) {
        for (int dy = -rOuter; dy <= rOuter; ++dy) {
            int outer = (int)Math.round(Math.sqrt((double)rOuter * (double)rOuter - (double)(dy * dy)));
            double innSq = (double)rInner * (double)rInner - (double)(dy * dy);
            if (innSq <= 0.0) {
                gfx.fill(cx - outer, cy + dy, cx + outer + 1, cy + dy + 1, color);
                continue;
            }
            int inner = (int)Math.round(Math.sqrt(innSq));
            gfx.fill(cx - outer, cy + dy, cx - inner, cy + dy + 1, color);
            gfx.fill(cx + inner + 1, cy + dy, cx + outer + 1, cy + dy + 1, color);
        }
    }

    private void renderLeftRadar(GuiGraphics gfx, int x, int y, int width, WanderingCultivatorEntity npc) {
        int i;
        int[] vals = npc.getZhenyuanAttrs();
        int maxVal = 10;
        for (int v : vals) {
            maxVal = Math.max(maxVal, v);
        }
        if (maxVal % 5 != 0) {
            maxVal = (maxVal / 5 + 1) * 5;
        }
        int cx = x + width / 2;
        int r = 36;
        int cyR = y + r + 20;
        double[] cs = new double[5];
        double[] sn = new double[5];
        int[] ox = new int[5];
        int[] oy = new int[5];
        int[] hx = new int[5];
        int[] hy = new int[5];
        int[] vx = new int[5];
        int[] vy = new int[5];
        for (i = 0; i < 5; ++i) {
            double a = Math.toRadians(-90 + i * 72);
            cs[i] = Math.cos(a);
            sn[i] = Math.sin(a);
            ox[i] = cx + (int)Math.round(cs[i] * (double)r);
            oy[i] = cyR + (int)Math.round(sn[i] * (double)r);
            hx[i] = cx + (int)Math.round(cs[i] * (double)r / 2.0);
            hy[i] = cyR + (int)Math.round(sn[i] * (double)r / 2.0);
            double vr = (double)Math.max(0, vals[i]) / (double)maxVal * (double)r;
            vx[i] = cx + (int)Math.round(cs[i] * vr);
            vy[i] = cyR + (int)Math.round(sn[i] * vr);
        }
        for (i = 0; i < 5; ++i) {
            int n = (i + 1) % 5;
            this.drawPixelLine(gfx, ox[i], oy[i], ox[n], oy[n], -4478832);
            this.drawPixelLine(gfx, cx, cyR, ox[i], oy[i], 1723574416);
            this.drawPixelLine(gfx, hx[i], hy[i], hx[n], hy[n], 1438361744);
        }
        for (i = 0; i < 5; ++i) {
            int n = (i + 1) % 5;
            WanderingCultivatorScreen.fillTriangle(gfx, cx, cyR, vx[i], vy[i], vx[n], vy[n], -2143449177);
        }
        for (i = 0; i < 5; ++i) {
            int n = (i + 1) % 5;
            this.drawPixelLine(gfx, vx[i], vy[i], vx[n], vy[n], -13668470);
            gfx.fill(vx[i] - 1, vy[i] - 1, vx[i] + 2, vy[i] + 2, -13668470);
        }
        String[] keys = new String[]{"zhenyuan.friday_cultivation.attr.constitution", "zhenyuan.friday_cultivation.attr.physique", "zhenyuan.friday_cultivation.attr.agility", "zhenyuan.friday_cultivation.attr.spell_power", "zhenyuan.friday_cultivation.attr.qi_sea"};
        float lblScale = 0.82f;
        Objects.requireNonNull(this.font);
        int lblH = (int)(9.0f * lblScale);
        for (int i2 = 0; i2 < 5; ++i2) {
            int boxX;
            MutableComponent label = Component.translatable((String)"zhenyuan.friday_cultivation.attr.label_with_value", (Object[])new Object[]{Component.translatable((String)keys[i2]), vals[i2]});
            int lblW = (int)((float)this.font.width((FormattedText)label) * lblScale);
            int lx = cx + (int)Math.round(cs[i2] * (double)(r + 9));
            int ly = cyR + (int)Math.round(sn[i2] * (double)(r + 8));
            int boxY = switch (i2) {
                case 0 -> {
                    boxX = lx - lblW / 2;
                    yield ly - lblH - 1;
                }
                case 1 -> {
                    boxX = lx + 2;
                    yield ly - lblH / 2;
                }
                case 2 -> {
                    boxX = lx + 2;
                    yield ly - 1;
                }
                case 3 -> {
                    boxX = lx - lblW - 2;
                    yield ly - 1;
                }
                default -> {
                    boxX = lx - lblW - 2;
                    yield ly - lblH / 2;
                }
            };
            this.drawScaledComponent(gfx, (Component)label, boxX, boxY, lblScale, WanderingCultivatorScreen.roleColorForRadar(i2), lblW + 4);
            this.npcZhenyuanLabelRects[i2][0] = boxX - 1;
            this.npcZhenyuanLabelRects[i2][1] = boxY - 1;
            this.npcZhenyuanLabelRects[i2][2] = boxX + lblW + 2;
            this.npcZhenyuanLabelRects[i2][3] = boxY + lblH + 1;
        }
    }

    private void renderDetailStatsCard(GuiGraphics gfx, int x, int y, int width, int height, WanderingCultivatorEntity npc) {
        int rowY = y + 50;
        rowY = this.drawDetailStatRow(gfx, x + 22, x + width - 22, rowY, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.detail.hp"), (Component)Component.literal((String)((int)npc.getHealth() + "/" + (int)npc.getMaxHealth())), -7723482);
        rowY = this.drawDetailStatRow(gfx, x + 22, x + width - 22, rowY, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.detail.qi"), (Component)Component.literal((String)(npc.getCurrentQi() + "/" + npc.getMaxQi())), -14722744);
        rowY = this.drawDetailStatRow(gfx, x + 22, x + width - 22, rowY, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.detail.attack"), (Component)Component.literal((String)String.valueOf((int)Math.round(npc.getAttributeValue(Attributes.ATTACK_DAMAGE)))), -13664921);
        rowY = this.drawDetailStatRow(gfx, x + 22, x + width - 22, rowY, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.detail.body_attack"), (Component)Component.literal((String)String.valueOf(WanderingCultivatorScreen.detailBodyAttack(npc))), -7723482);
        rowY = this.drawDetailStatRow(gfx, x + 22, x + width - 22, rowY, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.detail.speed"), (Component)Component.literal((String)String.valueOf(WanderingCultivatorScreen.detailSpeed(npc))), -13275765);
        rowY = this.drawDetailStatRow(gfx, x + 22, x + width - 22, rowY, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.detail.defense"), (Component)Component.literal((String)String.valueOf(npc.getBodyDefense())), -12950192);
        rowY = this.drawDetailStatRow(gfx, x + 22, x + width - 22, rowY, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.detail.qi_recovery"), (Component)Component.literal((String)String.valueOf(npc.getNaturalQiRecoveryPerSecond())), -4818904);
        this.drawDivider(gfx, x + 16, rowY + 8, width - 32);
        this.drawCenteredClipped(gfx, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.detail.token"), x + width / 2, rowY + 36, width - 24, -15067628);
        ItemStack token = this.findSectToken(npc);
        int slotX = x + width / 2 - 16;
        int slotY = rowY + 56;
        this.drawDetailSlot(gfx, slotX, slotY, 32, DetailCell.stack(token));
        this.drawCenteredClipped(gfx, (Component)(token.isEmpty() ? Component.translatable((String)"screen.friday_cultivation.cultivator.detail.token_missing") : Component.translatable((String)"screen.friday_cultivation.cultivator.detail.token_bound")), x + width / 2, slotY + 38, width - 18, token.isEmpty() ? -9807288 : -12766422);
    }

    private void renderDetailSmallRadar(GuiGraphics gfx, int x, int y, int width, WanderingCultivatorEntity npc) {
        int i;
        int[] vals = npc.getZhenyuanAttrs();
        int maxVal = 8;
        for (int v : vals) {
            maxVal = Math.max(maxVal, v);
        }
        int cx = x + width / 2;
        int cy = y + 36;
        int radius = 32;
        int[] outerX = new int[5];
        int[] outerY = new int[5];
        int[] curX = new int[5];
        int[] curY = new int[5];
        for (i = 0; i < 5; ++i) {
            double angle = Math.toRadians(-90 + i * 72);
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            outerX[i] = cx + (int)Math.round(cos * (double)radius);
            outerY[i] = cy + (int)Math.round(sin * (double)radius);
            double valueRadius = (double)Math.max(0, vals[i]) / (double)maxVal * (double)radius;
            curX[i] = cx + (int)Math.round(cos * valueRadius);
            curY[i] = cy + (int)Math.round(sin * valueRadius);
        }
        for (i = 0; i < 5; ++i) {
            int next = (i + 1) % 5;
            this.drawPixelLine(gfx, outerX[i], outerY[i], outerX[next], outerY[next], -4478832);
            this.drawPixelLine(gfx, cx, cy, outerX[i], outerY[i], -2000967536);
            WanderingCultivatorScreen.fillTriangle(gfx, cx, cy, curX[i], curY[i], curX[next], curY[next], 2000523175);
        }
        for (i = 0; i < 5; ++i) {
            int next = (i + 1) % 5;
            this.drawPixelLine(gfx, curX[i], curY[i], curX[next], curY[next], -13668470);
            gfx.fill(curX[i] - 1, curY[i] - 1, curX[i] + 2, curY[i] + 2, -13668470);
        }
        gfx.fill(cx - 38, cy - 8, cx + 38, cy + 9, -868003238);
        gfx.drawCenteredString(this.font, (Component)Component.translatable((String)"zhenyuan.friday_cultivation.title").copy().withStyle(ChatFormatting.BOLD), cx, cy - 4, -528679);
        String[] labelKeys = new String[]{"zhenyuan.friday_cultivation.attr.constitution", "zhenyuan.friday_cultivation.attr.physique", "zhenyuan.friday_cultivation.attr.agility", "zhenyuan.friday_cultivation.attr.spell_power", "zhenyuan.friday_cultivation.attr.qi_sea"};
        for (int i2 = 0; i2 < 5; ++i2) {
            double angle = Math.toRadians(-90 + i2 * 72);
            int labelX = cx + (int)Math.round(Math.cos(angle) * (double)(radius + 11));
            int labelY = cy + (int)Math.round(Math.sin(angle) * (double)(radius + 5));
            MutableComponent label = Component.translatable((String)"zhenyuan.friday_cultivation.attr.label_with_value", (Object[])new Object[]{Component.translatable((String)labelKeys[i2]), vals[i2]});
            int boxX = i2 == 0 ? labelX - 18 : (i2 < 3 ? labelX + 1 : labelX - 34);
            int boxY = i2 == 0 ? labelY + 1 : labelY - 2;
            this.drawTinyAt(gfx, (Component)label, boxX, boxY, WanderingCultivatorScreen.roleColorForRadar(i2));
            this.npcZhenyuanLabelRects[i2][0] = boxX - 1;
            this.npcZhenyuanLabelRects[i2][1] = boxY - 1;
            this.npcZhenyuanLabelRects[i2][2] = boxX + 50;
            this.npcZhenyuanLabelRects[i2][3] = boxY + 8;
        }
    }

    private static int roleColorForRadar(int index) {
        return switch (index) {
            case 0 -> -13275229;
            case 1 -> -7723482;
            case 2 -> -13664921;
            case 3 -> -6660586;
            default -> -9807288;
        };
    }

    private void renderDetailCategoriesPage(GuiGraphics gfx, int x, int y, int width, int height, WanderingCultivatorEntity npc) {
        int pad = 12;
        int ix = x + pad;
        int iw = width - pad * 2;
        int slotSize = 26;
        int singleH = 16 + slotSize;
        int tripleH = 16 + slotSize * 3 + 8;
        int totalContent = singleH * 4 + tripleH;
        int gap = Math.max(4, (height - 12 - totalContent) / 5);
        int cy = y + 6;
        cy = this.renderCategoryRow(gfx, ix, cy, iw, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.detail.category.technique"), this.detailTechniqueCells(npc), slotSize, 1) + gap;
        cy = this.renderCategoryRow(gfx, ix, cy, iw, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.detail.category.spell"), this.detailSpellCells(npc), slotSize, 1) + gap;
        cy = this.renderCategoryRow(gfx, ix, cy, iw, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.detail.category.armor"), this.detailInventoryCells(this::isArmorStack, 9), slotSize, 1) + gap;
        cy = this.renderCategoryRow(gfx, ix, cy, iw, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.detail.category.treasure"), this.detailInventoryCells(this::isTreasureStack, 9), slotSize, 1) + gap;
        this.renderCategoryRow(gfx, ix, cy, iw, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.detail.category.item"), this.detailInventoryCells(stack -> !this.isArmorStack((ItemStack)stack) && !this.isTreasureStack((ItemStack)stack), 27), slotSize, 3);
    }

    private int renderCategoryRow(GuiGraphics gfx, int x, int y, int width, Component label, List<DetailCell> cells, int slotSize, int rows) {
        this.drawDiamondHeader(gfx, x, y, width, label);
        int slots = 9;
        int slotY = y + 16;
        int step = (width - slotSize) / (slots - 1);
        int idx = 0;
        for (int row = 0; row < rows; ++row) {
            for (int i = 0; i < slots; ++i) {
                int sx = x + i * step;
                int sy = slotY + row * (slotSize + 4);
                DetailCell cell = idx < cells.size() ? cells.get(idx) : DetailCell.empty();
                this.drawDetailSlot(gfx, sx, sy, slotSize, cell);
                ++idx;
            }
        }
        return slotY + rows * slotSize + (rows - 1) * 4;
    }

    private void renderDetailCultivationSummary(GuiGraphics gfx, int x, int y, int width, WanderingCultivatorEntity npc) {
        RefiningRank rRank = npc.getRefiningRank();
        AlchemyRank aRank = npc.getAlchemyRank();
        this.drawCompactDetailStatPair(gfx, x, y, width, (Component)Component.translatable((String)"screen.friday_cultivation.attr.cultivation_efficiency_label"), (Component)Component.translatable((String)"screen.friday_cultivation.attr.cultivation_efficiency_value", (Object[])new Object[]{WanderingCultivatorScreen.detailCultivationEfficiency(npc)}), -13664921, (Component)Component.translatable((String)"screen.friday_cultivation.attr.refining_label"), rRank.displayName(), WanderingCultivatorScreen.rankColor(rRank.color(), -4818904));
        this.drawCompactDetailStatPair(gfx, x, y + 17, width, (Component)Component.translatable((String)"screen.friday_cultivation.attr.alchemy_label"), aRank.displayName(), WanderingCultivatorScreen.rankColor(aRank.color(), -7723482), (Component)Component.translatable((String)"screen.friday_cultivation.attr.id.spirit_root"), (Component)Component.translatable((String)npc.getSpiritRoot().translationKey()), -14722744);
        this.drawCompactDetailStatPair(gfx, x, y + 34, width, (Component)Component.translatable((String)"screen.friday_cultivation.attr.id.physique_kind"), (Component)Component.translatable((String)npc.getPhysique().translationKey()), -7723482, (Component)Component.translatable((String)"zhenyuan.friday_cultivation.title"), (Component)Component.literal((String)String.valueOf(WanderingCultivatorScreen.sumZhenyuanAttrs(npc))), -4818904);
        this.renderDetailSmallRadar(gfx, x, y + 52, width, npc);
    }

    private int renderDetailCategory(GuiGraphics gfx, int x, int y, int width, Component label, List<DetailCell> cells, int accent) {
        int centerX = x + width / 2;
        int labelW = Math.min(82, Math.max(42, this.font.width((FormattedText)label) + 10));
        int labelLeft = centerX - labelW / 2;
        int labelRight = centerX + labelW / 2;
        gfx.fill(x, y + 6, Math.max(x, labelLeft - 8), y + 8, 1151634786);
        gfx.fill(Math.min(x + width, labelRight + 8), y + 6, x + width, y + 8, 1151634786);
        gfx.fill(labelLeft - 4, y + 3, labelLeft, y + 11, accent);
        this.drawCenteredClipped(gfx, (Component)label.copy().withStyle(ChatFormatting.BOLD), centerX, y + 2, labelW, -15067628);
        int slotSize = 26;
        int slotY = y + 16;
        int totalW = 6 * slotSize + 5 * (32 - slotSize);
        int startX = x + Math.max(0, width - totalW) / 2;
        for (int slot = 0; slot < 6; ++slot) {
            int sx = startX + slot * 32;
            DetailCell cell = slot < cells.size() ? cells.get(slot) : DetailCell.empty();
            this.drawDetailSlot(gfx, sx, slotY, slotSize, cell);
        }
        return y + 46;
    }

    private void drawDetailSlot(GuiGraphics gfx, int x, int y, int size, DetailCell cell) {
        gfx.fill(x - 1, y - 1, x + size + 1, y + size + 1, -10724784);
        gfx.fill(x, y, x + size, y + size, -1448230);
        gfx.fill(x + 2, y + 2, x + size - 2, y + size - 2, -2762800);
        gfx.fill(x + 5, y + 3, x + size - 5, y + 5, -1711803935);
        gfx.fill(x + 5, y + size - 5, x + size - 5, y + size - 3, 1432115792);
        if (!cell.stack.isEmpty()) {
            int iconSize = Math.min(size - 4, Math.max(16, size - 2));
            int itemX = x + (size - iconSize) / 2;
            int itemY = y + (size - iconSize) / 2;
            this.renderItemAtSize(gfx, cell.stack, itemX, itemY, iconSize);
            this.detailStackHoverRects.add(new DetailStackHoverRect(itemX, itemY, iconSize, iconSize, cell.stack.copy()));
            return;
        }
        if (cell.texture != null) {
            try {
                int iconSize = Math.min(size - 4, Math.max(16, size - 2));
                gfx.blit(cell.texture, x + (size - iconSize) / 2, y + (size - iconSize) / 2, iconSize, iconSize, 0.0f, 0.0f, cell.textureSize, cell.textureSize, cell.textureSize, cell.textureSize);
                this.detailTextHoverRects.add(new DetailTextHoverRect(x, y, size, size, cell.tooltip));
                return;
            }
            catch (Exception iconSize) {
                // empty catch block
            }
        }
        if (!cell.tooltip.isEmpty()) {
            this.detailTextHoverRects.add(new DetailTextHoverRect(x, y, size, size, cell.tooltip));
        }
        int cx = x + size / 2;
        int cy = y + size / 2;
        gfx.fill(cx - 5, cy - 6, cx + 6, cy + 6, -6578022);
        gfx.fill(cx - 6, cy - 5, cx + 7, cy + 5, -6578022);
        gfx.fill(cx - 4, cy - 4, cx + 5, cy + 5, -2762800);
        gfx.fill(cx - 1, cy - 1, cx + 2, cy + 2, -4604488);
    }

    private void renderItemAtSize(GuiGraphics gfx, ItemStack stack, int x, int y, int iconSize) {
        if (iconSize == 16) {
            gfx.renderItem(stack, x, y);
            gfx.renderItemDecorations(this.font, stack, x, y);
            return;
        }
        float scale = (float)iconSize / 16.0f;
        gfx.pose().pushPose();
        gfx.pose().translate((float)x, (float)y, 0.0f);
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.renderItem(stack, 0, 0);
        gfx.renderItemDecorations(this.font, stack, 0, 0);
        gfx.pose().popPose();
    }

    private List<DetailCell> detailTechniqueCells(WanderingCultivatorEntity npc) {
        String techId = npc.getTechniqueId();
        if (techId == null || techId.isBlank()) {
            return List.of();
        }
        Technique technique = Technique.byId(techId);
        if (technique == null) {
            return List.of();
        }
        return List.of(DetailCell.texture(technique.iconTexture(), 32, WanderingCultivatorScreen.buildTechniqueTooltip(technique)));
    }

    private List<DetailCell> detailSpellCells(WanderingCultivatorEntity npc) {
        ArrayList<DetailCell> cells = new ArrayList<DetailCell>();
        for (String id : npc.getSpellIds()) {
            Spell spell = Spell.byId(id);
            if (spell != null) {
                cells.add(DetailCell.texture(spell.iconTexture(), spell.iconTextureSize(), spell.tooltipLines(false)));
            }
            if (cells.size() < 9) continue;
            break;
        }
        return cells;
    }

    private List<DetailCell> detailInventoryCells(Predicate<ItemStack> predicate, int limit) {
        ArrayList<DetailCell> cells = new ArrayList<DetailCell>();
        for (ItemStack stack : ((WanderingCultivatorMenu)this.menu).getDisplayInventory()) {
            if (stack.isEmpty() || !predicate.test(stack)) continue;
            cells.add(DetailCell.stack(stack.copy()));
            if (cells.size() < limit) continue;
            break;
        }
        return cells;
    }

    private boolean isToolStack(ItemStack stack) {
        return stack.getItem() instanceof SwordItem || stack.getItem() instanceof DiggerItem || stack.getItem() instanceof TridentItem || stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem;
    }

    private boolean isArmorStack(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem || stack.getItem() instanceof ShieldItem;
    }

    private boolean isTreasureStack(ItemStack stack) {
        if (stack.is((Item)ModItems.SECT_TOKEN.get())) {
            return true;
        }
        String className = stack.getItem().getClass().getSimpleName().toLowerCase(Locale.ROOT);
        return className.contains("artifact") || className.contains("talisman") || className.contains("formation") || className.contains("array");
    }

    private int countNpcSpiritStones() {
        int count = 0;
        for (ItemStack stack : ((WanderingCultivatorMenu)this.menu).getDisplayInventory()) {
            if (!(stack.getItem() instanceof SpiritStoneItem)) continue;
            count += stack.getCount();
        }
        return count;
    }

    private ItemStack findSectToken(WanderingCultivatorEntity npc) {
        if (npc == null) {
            return ItemStack.EMPTY;
        }
        for (ItemStack stack : ((WanderingCultivatorMenu)this.menu).getDisplayInventory()) {
            if (!stack.is((Item)ModItems.SECT_TOKEN.get())) continue;
            return stack.copy();
        }
        return ItemStack.EMPTY;
    }

    private boolean hasAnyOffers() {
        MerchantOffers offers = ((WanderingCultivatorMenu)this.menu).getOffers();
        return offers != null && !offers.isEmpty();
    }

    private static int detailBodyAttack(WanderingCultivatorEntity npc) {
        int[] attrs = npc.getZhenyuanAttrs();
        return attrs.length > 3 ? Math.max(0, attrs[3] * 5) : 0;
    }

    private static int detailSpeed(WanderingCultivatorEntity npc) {
        return Math.max(0, (int)Math.round(npc.getAttributeValue(Attributes.MOVEMENT_SPEED) * 100.0));
    }

    private static int detailCultivationEfficiency(WanderingCultivatorEntity npc) {
        boolean immortalTechniqueOrLegacySpell = Technique.IMMORTAL_INCANTATION.id().equals(npc.getTechniqueId()) || npc.getSpellIds().contains(Spell.IMMORTAL_INCANTATION.id());
        double absorbMult = (immortalTechniqueOrLegacySpell ? 5.0 : 0.5) * npc.getPhysique().bonus().qiAbsorbMult();
        return Math.max(1, (int)Math.ceil(absorbMult));
    }

    private static int sumZhenyuanAttrs(WanderingCultivatorEntity npc) {
        int total = 0;
        for (int value : npc.getZhenyuanAttrs()) {
            total += Math.max(0, value);
        }
        return total;
    }

    private static int rankColor(ChatFormatting formatting, int fallback) {
        Integer rgb = formatting.getColor();
        return rgb == null ? fallback : 0xFF000000 | rgb;
    }

    private void drawDetailKV(GuiGraphics gfx, int x, int y, Component label, Component value) {
        gfx.drawString(this.font, (Component)label.copy().withStyle(ChatFormatting.BOLD), x, y, -12766422, false);
        this.drawClippedComponent(gfx, value, x + 50, y, 150, 10, -9807288);
    }

    private int drawDetailStatRow(GuiGraphics gfx, int x, int right, int y, Component label, Component value, int valueColor) {
        gfx.drawString(this.font, label, x, y, -9807288, false);
        int valueW = this.font.width((FormattedText)value);
        gfx.drawString(this.font, (Component)value.copy().withStyle(ChatFormatting.BOLD), right - valueW, y, valueColor, false);
        return y + 20;
    }

    private void drawCompactDetailStatPair(GuiGraphics gfx, int x, int y, int width, Component leftLabel, Component leftValue, int leftColor, Component rightLabel, Component rightValue, int rightColor) {
        int gap = 10;
        int columnW = Math.max(48, (width - gap) / 2);
        this.drawCompactDetailStat(gfx, x, y, columnW, leftLabel, leftValue, leftColor);
        this.drawCompactDetailStat(gfx, x + columnW + gap, y, columnW, rightLabel, rightValue, rightColor);
    }

    private void drawCompactDetailStat(GuiGraphics gfx, int x, int y, int width, Component label, Component value, int valueColor) {
        int valueW = Math.min(this.font.width((FormattedText)value), Math.max(18, width / 2));
        this.drawClippedComponent(gfx, label, x, y, Math.max(20, width - valueW - 4), 10, -9807288);
        this.drawClippedComponent(gfx, (Component)value.copy().withStyle(ChatFormatting.BOLD), x + width - valueW, y, valueW, 10, valueColor);
    }

    private void drawDivider(GuiGraphics gfx, int x, int y, int width) {
        gfx.fill(x, y, x + width, y + 1, 1722060130);
        gfx.fill(x + width / 2 - 2, y - 1, x + width / 2 + 3, y + 2, -2002481822);
    }

    private int[] drawDetailActionButton(GuiGraphics gfx, int x, int y, int width, Component label, boolean enabled, int accent, int mouseX, int mouseY) {
        int border;
        boolean hovered = enabled && mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + 20;
        int n = border = enabled ? accent : -7438745;
        int fill = !enabled ? -1910849 : (hovered ? -3640 : -527903);
        gfx.fill(x, y, x + width, y + 20, border);
        gfx.fill(x + 2, y + 2, x + width - 2, y + 18, fill);
        this.drawCenteredClipped(gfx, (Component)label.copy().withStyle(ChatFormatting.BOLD), x + width / 2, y + 6, width - 8, enabled ? border : -9807288);
        return new int[]{x, y, width, 20};
    }

    private Component trimToWidth(Component text, int maxWidth) {
        if (this.font.width((FormattedText)text) <= maxWidth) {
            return text;
        }
        String raw = text.getString();
        String trimmed = this.font.plainSubstrByWidth(raw, Math.max(0, maxWidth));
        return Component.literal((String)trimmed).withStyle(text.getStyle());
    }

    private void drawCenteredClipped(GuiGraphics gfx, Component text, int centerX, int y, int maxWidth, int color) {
        int textW = this.font.width((FormattedText)text);
        if (textW <= maxWidth) {
            gfx.drawCenteredString(this.font, text, centerX, y, color);
            return;
        }
        if (this.activeDetailViewport != null) {
            gfx.drawCenteredString(this.font, this.trimToWidth(text, maxWidth), centerX, y, color);
            return;
        }
        int n = centerX - maxWidth / 2;
        int n2 = centerX + maxWidth / 2;
        Objects.requireNonNull(this.font);
        gfx.enableScissor(n, y, n2, y + 9 + 2);
        gfx.drawCenteredString(this.font, text, centerX, y, color);
        gfx.disableScissor();
    }

    private int drawWrappedLimited(GuiGraphics gfx, Component text, int x, int y, int maxWidth, int color, int maxLines) {
        List lines = this.font.split((FormattedText)text, maxWidth);
        int drawn = Math.min(maxLines, lines.size());
        for (int i = 0; i < drawn; ++i) {
            FormattedCharSequence formattedCharSequence = (FormattedCharSequence)lines.get(i);
            Objects.requireNonNull(this.font);
            gfx.drawString(this.font, formattedCharSequence, x, y + i * (9 + 2), color, false);
        }
        Objects.requireNonNull(this.font);
        return y + drawn * (9 + 2);
    }

    private void renderCultivatorBookSpine(GuiGraphics gfx, int x, int y) {
        this.renderCultivatorBookSpine(gfx, x, y, this.imageHeight);
    }

    private void renderCultivatorBookSpine(GuiGraphics gfx, int x, int y, int frameHeight) {
        int spineX = x - 40;
        gfx.fill(spineX - 2, y + 8, x - 3, y + frameHeight - 8, -1441984236);
        gfx.fill(spineX, y + 10, x - 6, y + frameHeight - 10, -14665650);
        gfx.fill(spineX + 4, y + 14, x - 10, y + frameHeight - 14, -13676952);
        int labelW = 28;
        int labelH = 96;
        int labelX = spineX + 5;
        int labelY = y + 34;
        MutableComponent label = Component.translatable((String)"screen.friday_cultivation.cultivator.detail_side").copy().withStyle(ChatFormatting.BOLD);
        String text = label.getString();
        gfx.fill(labelX - 2, labelY - 4, labelX + labelW + 2, labelY + labelH + 4, -1441984236);
        gfx.fill(labelX, labelY, labelX + labelW, labelY + labelH, -527903);
        gfx.fill(labelX + 2, labelY + 2, labelX + labelW - 2, labelY + labelH - 2, -1186608);
        int centerX = labelX + labelW / 2;
        float s = 1.5f;
        int charH = (int)(14.0f * s);
        int startY = labelY + (labelH - text.length() * charH) / 2 + 4;
        for (int i = 0; i < text.length(); ++i) {
            MutableComponent part = Component.literal((String)String.valueOf(text.charAt(i))).withStyle(ChatFormatting.BOLD);
            gfx.pose().pushPose();
            gfx.pose().translate((float)centerX, (float)(startY + i * charH), 0.0f);
            gfx.pose().scale(s, s, 1.0f);
            gfx.drawCenteredString(this.font, (Component)part, 0, 0, -15067628);
            gfx.pose().popPose();
        }
    }

    private void drawSectionHeader(GuiGraphics gfx, int x, int y, int rightEnd, Component label) {
        int dotStart;
        gfx.fill(x, y, x + 2, y + 9, -4703686);
        gfx.drawString(this.font, label, x + 5, y + 1, -9807288, false);
        int textW = this.font.width((FormattedText)label);
        for (int dx = dotStart = x + 7 + textW; dx < rightEnd - 1; dx += 6) {
            gfx.fill(dx, y + 4, dx + 2, y + 5, -1721148856);
        }
    }

    private void renderPlayerInventoryGrid(GuiGraphics gfx) {
        SimpleContainer sellC = ((WanderingCultivatorMenu)this.menu).getSellContainer();
        for (Slot s : ((WanderingCultivatorMenu)this.menu).slots) {
            if (s.container == sellC) continue;
            int cellX = this.leftPos + s.x - 1;
            int cellY = this.topPos + s.y - 1;
            gfx.fill(cellX, cellY, cellX + 18, cellY + 18, -3562934);
            gfx.fill(cellX + 1, cellY + 1, cellX + 17, cellY + 17, -13226976);
        }
    }

    private void renderLeftPanel(GuiGraphics gfx, int x, int y, WanderingCultivatorEntity npc, int mouseX, int mouseY) {
        MutableComponent name = npc.getCultivatorName().withStyle(ChatFormatting.BOLD).withStyle(s -> s.withColor(-4703686));
        gfx.drawString(this.font, (Component)name, x, y, -15067628, false);
        Realm realm = npc.getRealm();
        SubStage sub = npc.getSubStage();
        MutableComponent realmText = Component.literal((String)"").append(realm.displayName()).append(" ").append((Component)sub.displayName()).withStyle(s -> s.withColor(-12950192));
        gfx.drawString(this.font, (Component)realmText, x, y + 12, -12766422, false);
        try {
            InventoryScreen.renderEntityInInventoryFollowsMouse((GuiGraphics)gfx, (int)(x + 80), (int)(y + 102), (int)30, (float)(x + 80 - mouseX), (float)(y + 60 - mouseY), (LivingEntity)npc);
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.renderSectIdentityCard(gfx, x, y + 24, npc);
        this.drawMiniInfoLine(gfx, x, y + 49, (Component)Component.translatable((String)"screen.friday_cultivation.attr.id.spirit_root"), (Component)Component.translatable((String)npc.getSpiritRoot().translationKey()));
        this.drawMiniInfoLine(gfx, x, y + 59, (Component)Component.translatable((String)"screen.friday_cultivation.attr.id.physique_kind"), (Component)Component.translatable((String)npc.getPhysique().translationKey()));
        this.renderSectButton(gfx, x, y + 73, mouseX, mouseY, npc);
        this.renderTalkButton(gfx, x + 64, y + 73, mouseX, mouseY, npc);
        int barW = 158;
        int hpY = y + 108;
        float hpRatio = npc.getMaxHealth() > 0.0f ? npc.getHealth() / npc.getMaxHealth() : 0.0f;
        this.drawGradientBar(gfx, x, hpY, barW, hpRatio, -4181968, -10481648, String.format("HP %d/%d", (int)npc.getHealth(), (int)npc.getMaxHealth()));
        long curQi = npc.getCurrentQi();
        long maxQi = npc.getMaxQi();
        int qiY = hpY + 14;
        float qiRatio = maxQi > 0L ? Math.min(1.0f, (float)curQi / (float)maxQi) : 0.0f;
        this.drawGradientBar(gfx, x, qiY, barW, qiRatio, -10440528, -14659504, String.format("Qi %d/%d", curQi, maxQi));
    }

    private void renderSectIdentityCard(GuiGraphics gfx, int x, int y, WanderingCultivatorEntity npc) {
        int w = 158;
        int h = 18;
        boolean hasSect = npc.hasSectMembership();
        int accent = hasSect ? WanderingCultivatorScreen.sectRoleColor(npc.getSectRole()) : -9807288;
        gfx.fill(x - 1, y - 1, x + w + 1, y + h + 1, hasSect ? accent : -10859978);
        gfx.fill(x, y, x + w, y + h, -1056320);
        gfx.fill(x + 1, y + 1, x + 5, y + h - 1, accent);
        MutableComponent label = Component.translatable((String)"screen.friday_cultivation.attr.id.identity").copy().withStyle(ChatFormatting.DARK_GRAY);
        gfx.drawString(this.font, (Component)label, x + 9, y + 5, -9807288, false);
        MutableComponent value = npc.getSectIdentityComponent().copy().withStyle(s -> s.withColor(accent));
        this.drawClippedComponent(gfx, (Component)value, x + 48, y + 5, w - 54, 10, hasSect ? accent : -12766422);
    }

    private void renderSectButton(GuiGraphics gfx, int x, int y, int mouseX, int mouseY, WanderingCultivatorEntity npc) {
        this.sectButtonEnabled = true;
        this.sectButtonHasSect = npc != null && npc.hasSectMembership();
        int accent = this.sectButtonHasSect ? WanderingCultivatorScreen.sectRoleColor(npc.getSectRole()) : -9807288;
        this.sectButtonRect = this.renderCultivatorActionButton(gfx, x, y, 58, mouseX, mouseY, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.sect_button"), this.sectButtonEnabled, true, accent);
    }

    private void renderTalkButton(GuiGraphics gfx, int x, int y, int mouseX, int mouseY, WanderingCultivatorEntity npc) {
        this.talkButtonEnabled = npc != null && npc.hasSectMembership();
        int accent = this.talkButtonEnabled ? WanderingCultivatorScreen.sectRoleColor(npc.getSectRole()) : -9807288;
        this.talkButtonRect = this.renderCultivatorActionButton(gfx, x, y, 58, mouseX, mouseY, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.talk_button"), this.talkButtonEnabled, false, accent);
    }

    private int[] renderCultivatorActionButton(GuiGraphics gfx, int x, int y, int w, int mouseX, int mouseY, Component label, boolean enabled, boolean sectIcon, int accent) {
        boolean hovered;
        int h = 16;
        boolean bl = hovered = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        int border = enabled ? (hovered ? accent : -3562934) : -7438745;
        int inner = !enabled ? -3030620 : (hovered ? -1056582 : -1517128);
        gfx.fill(x - 1, y - 1, x + w + 1, y + h + 1, border);
        gfx.fill(x, y, x + w, y + h, inner);
        int iconX = x + 3;
        int iconY = y + 3;
        gfx.fill(iconX - 1, iconY - 1, iconX + 12, iconY + 12, enabled ? -12832733 : -8687521);
        gfx.fill(iconX, iconY, iconX + 11, iconY + 11, enabled ? accent : -9807288);
        this.drawCultivatorActionIcon(gfx, iconX, iconY, sectIcon, enabled);
        this.drawClippedComponent(gfx, label, x + 19, y + 4, w - 22, 9, enabled ? (hovered ? -7723482 : -12766422) : -9148329);
        return new int[]{x, y, w, h};
    }

    private void drawCultivatorActionIcon(GuiGraphics gfx, int x, int y, boolean sectIcon, boolean enabled) {
        int ink;
        int n = ink = enabled ? -3642 : -4674414;
        if (sectIcon) {
            gfx.fill(x + 2, y + 3, x + 9, y + 4, ink);
            gfx.fill(x + 1, y + 5, x + 10, y + 6, ink);
            gfx.fill(x + 3, y + 7, x + 4, y + 10, ink);
            gfx.fill(x + 7, y + 7, x + 8, y + 10, ink);
        } else {
            gfx.fill(x + 2, y + 2, x + 9, y + 8, ink);
            gfx.fill(x + 3, y + 8, x + 6, y + 10, ink);
            gfx.fill(x + 3, y + 4, x + 8, y + 5, enabled ? -9556443 : -7437716);
            gfx.fill(x + 3, y + 6, x + 7, y + 7, enabled ? -9556443 : -7437716);
        }
    }

    private boolean renderSectActionTooltips(GuiGraphics gfx, int mouseX, int mouseY) {
        if (this.isInsideRect(mouseX, mouseY, this.sectButtonRect)) {
            MutableComponent body = Component.translatable((String)(this.sectButtonHasSect ? "screen.friday_cultivation.cultivator.sect_button.tooltip" : "screen.friday_cultivation.cultivator.sect_button.tooltip.no_sect"));
            gfx.renderComponentTooltip(this.font, List.of(Component.translatable((String)"screen.friday_cultivation.cultivator.sect_button").copy().withStyle(ChatFormatting.GOLD), body.copy().withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
            return true;
        }
        if (this.isInsideRect(mouseX, mouseY, this.talkButtonRect)) {
            MutableComponent body = Component.translatable((String)(this.talkButtonEnabled ? "screen.friday_cultivation.cultivator.talk_button.tooltip" : "screen.friday_cultivation.cultivator.talk_button.tooltip.no_sect"));
            gfx.renderComponentTooltip(this.font, List.of(Component.translatable((String)"screen.friday_cultivation.cultivator.talk_button").copy().withStyle(this.talkButtonEnabled ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY), body.copy().withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
            return true;
        }
        if (this.isInsideRect(mouseX, mouseY, this.detailTradeButtonRect)) {
            MutableComponent body = Component.translatable((String)(this.hasAnyOffers() ? "screen.friday_cultivation.cultivator.detail.trade.tooltip" : "screen.friday_cultivation.cultivator.detail.trade.tooltip.empty"));
            gfx.renderComponentTooltip(this.font, List.of(Component.translatable((String)"screen.friday_cultivation.cultivator.detail.trade").copy().withStyle(this.hasAnyOffers() ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY), body.copy().withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
            return true;
        }
        return false;
    }

    private boolean isInsideRect(int mouseX, int mouseY, int[] rect) {
        return rect != null && rect.length >= 4 && rect[2] > 0 && rect[3] > 0 && mouseX >= rect[0] && mouseX < rect[0] + rect[2] && mouseY >= rect[1] && mouseY < rect[1] + rect[3];
    }

    private void drawClippedComponent(GuiGraphics gfx, Component text, int x, int y, int maxWidth, int height, int color) {
        if (this.activeDetailViewport != null) {
            gfx.drawString(this.font, this.trimToWidth(text, maxWidth), x, y, color, false);
            return;
        }
        gfx.enableScissor(x, y, x + maxWidth, y + height);
        gfx.drawString(this.font, text, x, y, color, false);
        gfx.disableScissor();
    }

    private static int sectRoleColor(SectRole role) {
        if (role == null) {
            return -9807288;
        }
        return switch (role) {
            default -> throw new IncompatibleClassChangeError();
            case ANCESTOR -> -4628524;
            case MASTER -> -2975186;
            case ELDER -> -4699067;
            case INNER_DISCIPLE -> -13664933;
            case OUTER_DISCIPLE -> -13275765;
            case GUARD_DISCIPLE -> -10722417;
            case SERVANT -> -8754603;
            case NONE -> -9807288;
        };
    }

    private void drawMiniInfoLine(GuiGraphics gfx, int x, int y, Component label, Component value) {
        gfx.drawString(this.font, (Component)label.copy().withStyle(ChatFormatting.DARK_GRAY), x, y, -12766422, false);
        gfx.drawString(this.font, (Component)value.copy().withStyle(ChatFormatting.GRAY), x + 48, y, -12766422, false);
    }

    private void drawGradientBar(GuiGraphics gfx, int x, int y, int w, float ratio, int colTop, int colBot, String label) {
        int h = 12;
        gfx.fill(x - 1, y - 1, x + w + 1, y + h + 1, -3562934);
        gfx.fill(x, y, x + w, y + h, -15067628);
        int fill = (int)((float)w * Math.max(0.0f, Math.min(1.0f, ratio)));
        if (fill > 0) {
            gfx.fillGradient(x, y, x + fill, y + h, colTop, colBot);
        }
        for (int i = 4; i < w; i += 4) {
            gfx.fill(x + i, y, x + i + 1, y + h, 0x40000000);
        }
        int tw = this.font.width(label);
        gfx.drawString(this.font, label, x + (w - tw) / 2, y + 2, -1, false);
    }

    private void renderTopTabs(GuiGraphics gfx, int centerX, int y, int availableWidth, int mouseX, int mouseY) {
        Component[] labels = new Component[]{Component.translatable((String)"screen.friday_cultivation.cultivator.detail_title"), Component.translatable((String)"screen.friday_cultivation.cultivator.tab.trade")};
        int tabH = 18;
        int gap = 6;
        int tabW = Math.max(52, Math.min(82, (availableWidth - gap * (TOP_TABS.length - 1)) / TOP_TABS.length));
        int totalW = tabW * TOP_TABS.length + gap * (TOP_TABS.length - 1);
        int startX = centerX - totalW / 2;
        for (int i = 0; i < TOP_TABS.length; ++i) {
            boolean hover;
            int tx = startX + i * (tabW + gap);
            int ty = y;
            this.tabRects[i] = new int[]{tx, ty, tabW, tabH};
            boolean active = this.currentTab == TOP_TABS[i];
            boolean bl = hover = mouseX >= tx && mouseX < tx + tabW && mouseY >= ty && mouseY < ty + tabH;
            int bg = active ? -12365222 : (hover ? -1056582 : -527903);
            int fg = active ? -528679 : -15067628;
            int border = active ? -3562934 : -10859978;
            gfx.fill(tx - 1, ty - 1, tx + tabW + 1, ty + tabH + 1, border);
            gfx.fill(tx, ty, tx + tabW, ty + tabH, bg);
            gfx.fill(tx + 4, ty + 3, tx + 7, ty + tabH - 3, active ? -10496 : border);
            if (active) {
                gfx.fill(tx + 12, ty + 3, tx + tabW - 12, ty + 5, 0x33FFFFFF);
            }
            int tw = this.font.width((FormattedText)labels[i]);
            gfx.drawString(this.font, labels[i], tx + (tabW - tw) / 2, ty + 5, fg, false);
        }
    }

    private void renderWideTradeCard(GuiGraphics gfx, int x, int y, int width, int height, WanderingCultivatorEntity npc, int mouseX, int mouseY) {
        this.offerHitboxes.clear();
        this.tradeItemHoverRects.clear();
        this.virtualSlotRects.clear();
        this.buyButtonRect = new int[4];
        this.sundryButtonRect = new int[4];
        int contentX = x + 16;
        int contentW = width - 32;
        this.drawDiamondHeader(gfx, contentX, y + 6, contentW, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.trade_list_title"));
        int bpSlot = 22;
        int bpStep = (contentW - bpSlot) / 8;
        int bpGridH = 4 * bpSlot + 3;
        int bpHeaderY = y + height - bpGridH - 14;
        this.drawDiamondHeader(gfx, contentX, bpHeaderY, contentW, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.player_inventory_section"));
        this.renderVirtualPlayerInventoryWide(gfx, contentX, bpHeaderY + 14, bpSlot, bpStep);
        int listTop = y + 24;
        int buyButtonY = bpHeaderY - 34;
        int listBottom = buyButtonY - 4;
        MerchantOffers offers = ((WanderingCultivatorMenu)this.menu).getOffers();
        ArrayList<Integer> visible = new ArrayList<Integer>();
        if (offers != null) {
            for (int i = 0; i < offers.size(); ++i) {
                if (((MerchantOffer)offers.get(i)).isOutOfStock()) continue;
                visible.add(i);
            }
        }
        if (!visible.contains(this.selectedOfferIdx)) {
            this.selectedOfferIdx = -1;
        }
        if (visible.isEmpty()) {
            this.drawCenteredClipped(gfx, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.no_trades"), contentX + contentW / 2, listTop + 28, contentW - 20, -9807288);
            return;
        }
        int rowH = 24;
        int rowGap = 2;
        int rowW = contentW - 6;
        int visibleRows = Math.max(1, (listBottom - listTop) / (rowH + rowGap));
        int total = visible.size();
        int maxOffset = Math.max(0, total - visibleRows);
        if (this.tradeScrollOffset > maxOffset) {
            this.tradeScrollOffset = maxOffset;
        }
        if (this.tradeScrollOffset < 0) {
            this.tradeScrollOffset = 0;
        }
        int displayCount = Math.min(visibleRows, total - this.tradeScrollOffset);
        for (int i = 0; i < displayCount; ++i) {
            int realIdx = (Integer)visible.get(this.tradeScrollOffset + i);
            MerchantOffer offer = (MerchantOffer)offers.get(realIdx);
            int ry = listTop + i * (rowH + rowGap);
            boolean hover = mouseX >= contentX && mouseX < contentX + rowW && mouseY >= ry && mouseY < ry + rowH;
            boolean affordable = this.playerCanAfford(offer);
            boolean selected = realIdx == this.selectedOfferIdx;
            this.offerHitboxes.add(new int[]{contentX, ry, rowW, rowH, realIdx, affordable ? 1 : 0});
            gfx.fill(contentX - 1, ry - 1, contentX + rowW + 1, ry + rowH + 1, selected ? -10496 : (hover ? -3562934 : -10859978));
            gfx.fill(contentX, ry, contentX + rowW, ry + rowH, selected ? -6528 : (hover ? -1056582 : -1517128));
            if (selected) {
                gfx.fill(contentX, ry, contentX + rowW, ry + 1, -10496);
            }
            int slot = 20;
            int cellX = contentX + 5;
            int cellY = ry + 2;
            this.drawItemCell(gfx, cellX, cellY, offer.getCostA(), affordable, slot);
            this.tradeItemHoverRects.add(new int[]{realIdx, cellX, cellY, slot, slot, 0});
            if (!offer.getCostB().isEmpty()) {
                this.drawItemCell(gfx, cellX + slot + 4, cellY, offer.getCostB(), affordable, slot);
                this.tradeItemHoverRects.add(new int[]{realIdx, cellX + slot + 4, cellY, slot, slot, 1});
            }
            int arrowX = cellX + (slot + 4) * 2 + 2;
            gfx.drawString(this.font, ">", arrowX, cellY + 6, -15067628, false);
            int resultX = arrowX + 12;
            this.drawItemCell(gfx, resultX, cellY, offer.getResult(), true, slot);
            this.tradeItemHoverRects.add(new int[]{realIdx, resultX, cellY, slot, slot, 2});
            String learnMark = this.checkLearnStatus(offer.getResult());
            if (!learnMark.isEmpty()) {
                int learnColor = "\u5df2\u5b78".equals(learnMark) ? -15048653 : -7704480;
                this.drawClippedComponent(gfx, (Component)Component.literal((String)learnMark), resultX + slot + 6, cellY + 6, rowW - (resultX - contentX) - slot - 34, 10, learnColor);
            }
            int iconX = contentX + rowW - 16;
            int iconY = ry + (rowH - 10) / 2;
            if (affordable) {
                this.drawCheckIcon(gfx, iconX, iconY, -15037894);
                continue;
            }
            this.drawCrossIcon(gfx, iconX, iconY, -5230544);
        }
        if (total > visibleRows) {
            int barX = contentX + rowW + 2;
            int barTop = listTop;
            int barBot = listTop + visibleRows * (rowH + rowGap);
            gfx.fill(barX, barTop, barX + 4, barBot, -10859978);
            int thumbH = Math.max(8, (barBot - barTop) * visibleRows / total);
            int thumbY = barTop + (barBot - barTop - thumbH) * this.tradeScrollOffset / Math.max(1, maxOffset);
            gfx.fill(barX + 1, thumbY, barX + 4, thumbY + thumbH, -3562934);
        }
        if (this.selectedOfferIdx >= 0 && this.selectedOfferIdx < offers.size()) {
            this.renderBuyButton(gfx, contentX + (contentW - 120) / 2, buyButtonY, (MerchantOffer)offers.get(this.selectedOfferIdx), mouseX, mouseY);
        }
    }

    private void renderVirtualPlayerInventoryWide(GuiGraphics gfx, int x, int y, int slotSize, int step) {
        List<Slot> slots = ((WanderingCultivatorMenu)this.menu).getPlayerInventorySlots();
        for (int i = 0; i < Math.min(36, slots.size()); ++i) {
            int sx = x + i % 9 * step;
            int sy = y + i / 9 * (slotSize + 1);
            this.renderVirtualSlot(gfx, slots.get(i), sx, sy, slotSize);
        }
    }

    private void drawCheckIcon(GuiGraphics gfx, int x, int y, int color) {
        int i;
        for (i = 0; i <= 3; ++i) {
            gfx.fill(x + 1 + i, y + 5 + i, x + 3 + i, y + 7 + i, color);
        }
        for (i = 0; i <= 5; ++i) {
            gfx.fill(x + 4 + i, y + 8 - i, x + 6 + i, y + 10 - i, color);
        }
    }

    private void drawCrossIcon(GuiGraphics gfx, int x, int y, int color) {
        for (int i = 0; i <= 7; ++i) {
            gfx.fill(x + 1 + i, y + 1 + i, x + 3 + i, y + 3 + i, color);
            gfx.fill(x + 8 - i, y + 1 + i, x + 10 - i, y + 3 + i, color);
        }
    }

    private void renderWideSundrySellArea(GuiGraphics gfx, int x, int y, int width, WanderingCultivatorEntity npc, int mouseX, int mouseY) {
        boolean hover;
        List<Item> favs = npc.getFavoriteItems();
        int favX = x + 4;
        int favY = y;
        gfx.drawString(this.font, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.favorites_section"), favX, favY + 5, -12766422, false);
        int iconX = favX + 46;
        for (int i = 0; i < Math.min(8, favs.size()); ++i) {
            ItemStack stack = new ItemStack((ItemLike)favs.get(i));
            this.drawDetailSlot(gfx, iconX, favY, 24, DetailCell.stack(stack));
            this.tradeItemHoverRects.add(new int[]{-100 - i, iconX, favY, 24, 24, 98});
            iconX += 26;
        }
        Slot sellSlot = ((WanderingCultivatorMenu)this.menu).getSellSlot();
        int slotX = x + 4;
        int slotY = y + 26;
        this.renderVirtualSlot(gfx, sellSlot, slotX, slotY, 24);
        ItemStack sellStack = ((WanderingCultivatorMenu)this.menu).getSellContainer().getItem(0);
        ItemStack price = SundryPricing.priceFor(sellStack);
        gfx.drawString(this.font, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.price_label"), slotX + 34, slotY + 8, -15067628, false);
        int priceX = slotX + 72;
        this.drawDetailSlot(gfx, priceX, slotY, 24, DetailCell.stack(price));
        if (!price.isEmpty()) {
            this.tradeItemHoverRects.add(new int[]{-1, priceX, slotY, 24, 24, 99});
        }
        boolean enabled = !price.isEmpty();
        int btnW = 76;
        int btnH = 18;
        int btnX = x + width - btnW - 8;
        int btnY = slotY + 3;
        this.sundryButtonRect = new int[]{btnX, btnY, btnW, btnH};
        boolean bl = hover = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
        int btnBg = enabled ? (hover ? -4703686 : -7723482) : -7638944;
        int btnBorder = enabled ? -3562934 : -10859978;
        gfx.fill(btnX - 1, btnY - 1, btnX + btnW + 1, btnY + btnH + 1, btnBorder);
        gfx.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnBg);
        if (hover && enabled) {
            gfx.fill(btnX, btnY, btnX + btnW, btnY + 1, -10496);
        }
        MutableComponent label = Component.translatable((String)"screen.friday_cultivation.cultivator.sell_btn");
        int tw = this.font.width((FormattedText)label);
        gfx.drawString(this.font, (Component)label, btnX + (btnW - tw) / 2, btnY + 5, -1, false);
    }

    private void renderVirtualPlayerInventory(GuiGraphics gfx, int x, int y) {
        List<Slot> slots = ((WanderingCultivatorMenu)this.menu).getPlayerInventorySlots();
        int slotSize = 24;
        int gap = 0;
        for (int i = 0; i < Math.min(36, slots.size()); ++i) {
            int sx = x + i % 9 * (slotSize + gap);
            int sy = y + i / 9 * (slotSize + gap);
            this.renderVirtualSlot(gfx, slots.get(i), sx, sy, slotSize);
        }
    }

    private void renderVirtualSlot(GuiGraphics gfx, Slot slot, int x, int y, int size) {
        ItemStack stack = slot == null ? ItemStack.EMPTY : slot.getItem();
        this.drawDetailSlot(gfx, x, y, size, DetailCell.stack(stack));
        if (slot != null) {
            this.virtualSlotRects.add(new VirtualSlotRect(slot, x, y, size, size));
        }
    }

    private void renderWideItemsCard(GuiGraphics gfx, int x, int y, int width, int height, WanderingCultivatorEntity npc) {
        this.npcInvHoverRects.clear();
        this.spellHoverRects.clear();
        int rowY = y + 8;
        rowY = this.renderDetailCategory(gfx, x + 18, rowY, width - 36, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.detail.category.technique"), this.detailTechniqueCells(npc), -13664921);
        rowY = this.renderDetailCategory(gfx, x + 18, rowY, width - 36, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.detail.category.spell"), this.detailSpellCells(npc), -9547375);
        gfx.drawString(this.font, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.inventory_section").copy().withStyle(ChatFormatting.BOLD), x + 22, ++rowY, -15067628, false);
        this.drawDivider(gfx, x + 18, rowY + 11, width - 36);
        rowY += 16;
        List<ItemStack> displayInv = ((WanderingCultivatorMenu)this.menu).getDisplayInventory();
        int slotSize = 24;
        int gap = 4;
        int cols = 9;
        int gridW = cols * slotSize + (cols - 1) * gap;
        int startX = x + (width - gridW) / 2;
        int rows = Math.min(5, Math.max(3, (height - (rowY - y) - 12) / (slotSize + gap)));
        int totalCells = cols * rows;
        for (int i = 0; i < totalCells; ++i) {
            int sx = startX + i % cols * (slotSize + gap);
            int sy = rowY + i / cols * (slotSize + gap);
            ItemStack stack = i < displayInv.size() ? displayInv.get(i) : ItemStack.EMPTY;
            this.drawDetailSlot(gfx, sx, sy, slotSize, DetailCell.stack(stack));
            if (stack.isEmpty()) continue;
            this.npcInvHoverRects.add(new int[]{i, sx, sy, slotSize, slotSize});
        }
    }

    private void renderTradeTab(GuiGraphics gfx, int x, int y, WanderingCultivatorEntity npc, int mouseX, int mouseY) {
        this.offerHitboxes.clear();
        this.tradeItemHoverRects.clear();
        this.buyButtonRect = new int[]{0, 0, 0, 0};
        this.sundryButtonRect = new int[4];
        int rightEnd = this.leftPos + 356;
        this.drawSectionHeader(gfx, x, y, rightEnd, (Component)Component.literal((String)"\u53ef\u4ea4\u6613\u7269\u54c1"));
        int listY = y + 12;
        int listEndY = this.topPos + 232 - 70;
        int rowH = 18;
        int rowGap = 1;
        int visibleRows = Math.max(1, (listEndY - listY) / (rowH + rowGap));
        MerchantOffers offers = ((WanderingCultivatorMenu)this.menu).getOffers();
        if (offers == null || offers.isEmpty()) {
            gfx.drawString(this.font, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.no_trades"), x, listY + 4, -9807288, false);
            return;
        }
        int totalOffers = offers.size();
        int maxOffset = Math.max(0, totalOffers - visibleRows);
        if (this.tradeScrollOffset > maxOffset) {
            this.tradeScrollOffset = maxOffset;
        }
        if (this.tradeScrollOffset < 0) {
            this.tradeScrollOffset = 0;
        }
        int rowW = Math.max(162, rightEnd - x - 8);
        int displayCount = Math.min(visibleRows, totalOffers - this.tradeScrollOffset);
        for (int i = 0; i < displayCount; ++i) {
            int statusColor;
            String statusIcon;
            int offerIdx = this.tradeScrollOffset + i;
            MerchantOffer offer = (MerchantOffer)offers.get(offerIdx);
            int ry = listY + i * (rowH + rowGap);
            this.offerHitboxes.add(new int[]{x, ry, rowW, rowH});
            boolean hover = mouseX >= x && mouseX < x + rowW && mouseY >= ry && mouseY < ry + rowH;
            boolean selected = offerIdx == this.selectedOfferIdx;
            boolean affordable = this.playerCanAfford(offer);
            boolean locked = offer.isOutOfStock();
            gfx.fill(x - 1, ry - 1, x + rowW + 1, ry + rowH + 1, selected ? -3562934 : -10859978);
            int rowBg = locked ? -7638944 : (selected ? -6528 : (hover ? -1056582 : -1517128));
            gfx.fill(x, ry, x + rowW, ry + rowH, rowBg);
            if (selected) {
                gfx.fill(x, ry, x + rowW, ry + 1, -10496);
            }
            int cellX = x + 3;
            int cellY = ry + 1;
            this.drawItemCell(gfx, cellX, cellY, offer.getCostA(), affordable);
            this.tradeItemHoverRects.add(new int[]{offerIdx, cellX, cellY, 16, 16, 0});
            if (!offer.getCostB().isEmpty()) {
                this.drawItemCell(gfx, cellX + 18, cellY, offer.getCostB(), affordable);
                this.tradeItemHoverRects.add(new int[]{offerIdx, cellX + 18, cellY, 16, 16, 1});
            }
            gfx.drawString(this.font, "\u2192", cellX + 38, cellY + 4, locked ? -9807288 : -15067628, false);
            this.drawItemCell(gfx, cellX + 50, cellY, offer.getResult(), true);
            this.tradeItemHoverRects.add(new int[]{offerIdx, cellX + 50, cellY, 16, 16, 2});
            String learnMark = this.checkLearnStatus(offer.getResult());
            if (!learnMark.isEmpty()) {
                int learnColor = "\u5df2\u5b78".equals(learnMark) ? -15048653 : -7704480;
                gfx.drawString(this.font, learnMark, cellX + 70, cellY + 4, learnColor, false);
            }
            int statusX = x + rowW - 14;
            if (locked) {
                statusIcon = "\u2717";
                statusColor = -8781824;
            } else if (affordable) {
                statusIcon = "\u2713";
                statusColor = -15048653;
            } else {
                statusIcon = "?";
                statusColor = -7723482;
            }
            gfx.drawString(this.font, statusIcon, statusX, cellY + 4, statusColor, false);
        }
        if (totalOffers > visibleRows) {
            int barX = x + rowW + 2;
            int barTop = listY;
            int barBot = listY + visibleRows * (rowH + rowGap);
            gfx.fill(barX, barTop, barX + 4, barBot, -10859978);
            int thumbH = Math.max(8, (barBot - barTop) * visibleRows / totalOffers);
            int thumbY = barTop + (barBot - barTop - thumbH) * this.tradeScrollOffset / Math.max(1, maxOffset);
            gfx.fill(barX + 1, thumbY, barX + 4, thumbY + thumbH, -3562934);
        }
        if (this.selectedOfferIdx >= 0 && this.selectedOfferIdx < offers.size()) {
            this.renderBuyButton(gfx, x + 40, listEndY + 2, (MerchantOffer)offers.get(this.selectedOfferIdx), mouseX, mouseY);
        }
        this.renderSundrySellArea(gfx, x, this.topPos + 232 - 56, rightEnd, npc, mouseX, mouseY);
    }

    private void renderSundrySellArea(GuiGraphics gfx, int x, int y, int rightEnd, WanderingCultivatorEntity npc, int mouseX, int mouseY) {
        boolean hover;
        this.drawSectionHeader(gfx, x, y, rightEnd, (Component)Component.literal((String)"\u96dc\u8ca8\u8ce3\u51fa"));
        List<Item> favs = npc.getFavoriteItems();
        int favY = y + 12;
        gfx.drawString(this.font, "\u6536\u8cfc\uff1a", x, favY + 4, -12766422, false);
        int iconX = x + 32;
        for (int i = 0; i < Math.min(8, favs.size()); ++i) {
            Item it = favs.get(i);
            ItemStack stack = new ItemStack((ItemLike)it);
            gfx.fill(iconX - 1, favY - 1, iconX + 17, favY + 17, -10859978);
            gfx.fill(iconX, favY, iconX + 16, favY + 16, -1517128);
            gfx.renderItem(stack, iconX, favY);
            this.tradeItemHoverRects.add(new int[]{-100 - i, iconX, favY, 16, 16, 98});
            iconX += 17;
        }
        int rowY = y + 32;
        int slotScreenX = this.leftPos + 192;
        int slotScreenY = this.topPos + 222;
        gfx.fill(slotScreenX - 1, slotScreenY - 1, slotScreenX + 17, slotScreenY + 17, -3562934);
        gfx.fill(slotScreenX, slotScreenY, slotScreenX + 16, slotScreenY + 16, -13226976);
        ItemStack sellStack = ((WanderingCultivatorMenu)this.menu).getSellContainer().getItem(0);
        ItemStack price = SundryPricing.priceFor(sellStack);
        gfx.drawString(this.font, "\u2192", slotScreenX + 22, slotScreenY + 4, -15067628, false);
        int priceX = slotScreenX + 36;
        if (!price.isEmpty()) {
            this.drawItemCell(gfx, priceX, slotScreenY, price, true);
            this.tradeItemHoverRects.add(new int[]{-1, priceX, slotScreenY, 16, 16, 99});
        } else if (!sellStack.isEmpty()) {
            gfx.fill(priceX - 1, slotScreenY - 1, priceX + 17, slotScreenY + 17, -10859978);
            gfx.fill(priceX, slotScreenY, priceX + 16, slotScreenY + 16, -1517128);
            gfx.drawString(this.font, "\u2717", priceX + 4, slotScreenY + 4, -8781824, false);
        } else {
            gfx.fill(priceX - 1, slotScreenY - 1, priceX + 17, slotScreenY + 17, -10859978);
            gfx.fill(priceX, slotScreenY, priceX + 16, slotScreenY + 16, -1517128);
        }
        boolean enabled = !price.isEmpty();
        int btnX = slotScreenX + 60;
        int btnW = 80;
        int btnH = 14;
        int btnY = slotScreenY + 1;
        this.sundryButtonRect = new int[]{btnX, btnY, btnW, btnH};
        boolean bl = hover = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
        int btnBg = enabled ? (hover ? -4703686 : -7723482) : -7638944;
        int btnBorder = enabled ? -3562934 : -10859978;
        gfx.fill(btnX - 1, btnY - 1, btnX + btnW + 1, btnY + btnH + 1, btnBorder);
        gfx.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnBg);
        if (hover && enabled) {
            gfx.fill(btnX, btnY, btnX + btnW, btnY + 1, -10496);
        }
        MutableComponent label = Component.literal((String)"\u8ce3\u51fa");
        int tw = this.font.width((FormattedText)label);
        gfx.drawString(this.font, (Component)label, btnX + (btnW - tw) / 2, btnY + 3, -1, false);
    }

    private void drawItemCell(GuiGraphics gfx, int x, int y, ItemStack stack, boolean affordable) {
        this.drawItemCell(gfx, x, y, stack, affordable, 16);
    }

    private void drawItemCell(GuiGraphics gfx, int x, int y, ItemStack stack, boolean affordable, int size) {
        gfx.fill(x - 1, y - 1, x + size + 1, y + size + 1, -3562934);
        gfx.fill(x, y, x + size, y + size, -1517128);
        int iconSize = Math.min(size - 2, Math.max(16, size - 4));
        int iconX = x + (size - iconSize) / 2;
        int iconY = y + (size - iconSize) / 2;
        this.renderItemAtSize(gfx, stack, iconX, iconY, iconSize);
        if (!affordable) {
            gfx.fill(x, y, x + size, y + size, 0x70202020);
        }
    }

    private void renderBuyButton(GuiGraphics gfx, int x, int y, MerchantOffer offer, int mouseX, int mouseY) {
        boolean hover;
        boolean affordable = this.playerCanAfford(offer);
        boolean locked = offer.isOutOfStock();
        boolean enabled = affordable && !locked;
        int btnW = 120;
        int btnH = 16;
        this.buyButtonRect = new int[]{x, y, btnW, btnH};
        boolean bl = hover = mouseX >= x && mouseX < x + btnW && mouseY >= y && mouseY < y + btnH;
        int btnBg = enabled ? (hover ? -4703686 : -7723482) : -7638944;
        int btnBorder = enabled ? -3562934 : -10859978;
        gfx.fill(x - 1, y - 1, x + btnW + 1, y + btnH + 1, btnBorder);
        gfx.fill(x, y, x + btnW, y + btnH, btnBg);
        if (hover && enabled) {
            gfx.fill(x, y, x + btnW, y + 1, -10496);
        }
        MutableComponent label = Component.translatable((String)"screen.friday_cultivation.cultivator.trade_btn");
        int tw = this.font.width((FormattedText)label);
        gfx.drawString(this.font, (Component)label, x + (btnW - tw) / 2, y + 4, -1, false);
    }

    private String checkLearnStatus(ItemStack result) {
        if (result.isEmpty()) {
            return "";
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return "";
        }
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        if (data == null) {
            return "";
        }
        Item item = result.getItem();
        if (item instanceof TechniqueBookItem) {
            TechniqueBookItem tb = (TechniqueBookItem)item;
            String techId = tb.technique().id();
            return data.getLearnedTechniques().contains(techId) ? "\u5df2\u5b78" : "\u672a\u5b78";
        }
        item = result.getItem();
        if (item instanceof SpellBookItem) {
            SpellBookItem sb = (SpellBookItem)item;
            return data.hasSpell(sb.spell()) ? "\u5df2\u5b78" : "\u672a\u5b78";
        }
        return "";
    }

    private boolean playerCanAfford(MerchantOffer offer) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }
        Inventory pinv = player.getInventory();
        return this.hasItem(pinv, offer.getCostA()) && (offer.getCostB().isEmpty() || this.hasItem(pinv, offer.getCostB()));
    }

    private boolean hasItem(Inventory inv, ItemStack required) {
        if (required.isEmpty()) {
            return true;
        }
        int needed = required.getCount();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack slot = inv.getItem(i);
            if (!ItemStack.isSameItemSameTags((ItemStack)slot, (ItemStack)required) || (needed -= slot.getCount()) > 0) continue;
            return true;
        }
        return false;
    }

    private void renderAttributesTab(GuiGraphics gfx, int x, int y, WanderingCultivatorEntity npc) {
        Technique.Bonus tb = WanderingCultivatorScreen.getTechniqueBonus(npc);
        int rightEnd = this.leftPos + 356;
        int attack = (int)Math.round(npc.getAttributeValue(Attributes.ATTACK_DAMAGE));
        int defense = npc.getBodyDefense();
        boolean immortalTechniqueOrLegacySpell = Technique.IMMORTAL_INCANTATION.id().equals(npc.getTechniqueId()) || npc.getSpellIds().contains(Spell.IMMORTAL_INCANTATION.id());
        double absorbMult = (immortalTechniqueOrLegacySpell ? 5.0 : 0.5) * npc.getPhysique().bonus().qiAbsorbMult();
        int cultivationEfficiency = Math.max(1, (int)Math.ceil(absorbMult));
        long qiRecovery = npc.getNaturalQiRecoveryPerSecond();
        MutableComponent meleeLabel = Component.translatable((String)"screen.friday_cultivation.attr.attack_label").copy().append("\uff1a");
        MutableComponent defenseLabel = Component.translatable((String)"screen.friday_cultivation.attr.defense_label").copy().append("\uff1a");
        MutableComponent cultivationEfficiencyLabel = Component.translatable((String)"screen.friday_cultivation.attr.cultivation_efficiency_label").copy().append("\uff1a");
        MutableComponent qiRecoveryLabel = Component.translatable((String)"screen.friday_cultivation.attr.qi_recovery_label").copy().append("\uff1a");
        MutableComponent refiningLabel = Component.translatable((String)"screen.friday_cultivation.attr.refining_label").copy().append("\uff1a");
        MutableComponent alchemyLabel = Component.translatable((String)"screen.friday_cultivation.attr.alchemy_label").copy().append("\uff1a");
        y = this.drawAttrRowComponent(gfx, x, rightEnd, y, (Component)meleeLabel, (Component)Component.literal((String)("+" + attack)), -15067628);
        y = this.drawAttrRowComponent(gfx, x, rightEnd, y + 10, (Component)defenseLabel, (Component)Component.literal((String)("+" + defense)), -15067628);
        y = this.drawAttrRowComponent(gfx, x, rightEnd, y + 10, (Component)cultivationEfficiencyLabel, (Component)Component.translatable((String)"screen.friday_cultivation.attr.cultivation_efficiency_value", (Object[])new Object[]{cultivationEfficiency}), -15067628);
        y = this.drawAttrRowComponent(gfx, x, rightEnd, y + 10, (Component)qiRecoveryLabel, (Component)Component.translatable((String)"screen.friday_cultivation.attr.qi_recovery_value", (Object[])new Object[]{qiRecovery}), -15067628);
        RefiningRank rRank = npc.getRefiningRank();
        AlchemyRank aRank = npc.getAlchemyRank();
        MutableComponent rRankName = rRank.displayName().copy().withStyle(rRank.color());
        MutableComponent aRankName = aRank.displayName().copy().withStyle(aRank.color());
        y = this.drawAttrRowComponent(gfx, x, rightEnd, y + 10, (Component)refiningLabel, (Component)rRankName, -15067628);
        y = this.drawAttrRowComponent(gfx, x, rightEnd, y + 10, (Component)alchemyLabel, (Component)aRankName, -15067628);
        this.renderNpcZhenyuanRadar(gfx, x, rightEnd, y += 14, npc);
    }

    private int drawAttrRowComponent(GuiGraphics gfx, int x, int rightEnd, int y, Component label, Component value, int labelColor) {
        gfx.drawString(this.font, label, x, y, labelColor, false);
        int valueW = this.font.width((FormattedText)value);
        gfx.drawString(this.font, value, rightEnd - valueW, y, -15067628, false);
        return y;
    }

    private void renderNpcZhenyuanRadar(GuiGraphics gfx, int x, int rightEnd, int y, WanderingCultivatorEntity npc) {
        int i;
        int i2;
        int i3;
        int i4;
        int[] vals = npc.getZhenyuanAttrs();
        int total = 0;
        for (int v : vals) {
            total += v;
        }
        MutableComponent header = Component.translatable((String)"zhenyuan.friday_cultivation.title");
        MutableComponent tag = Component.translatable((String)"zhenyuan.friday_cultivation.unallocated_tag", (Object[])new Object[]{total});
        this.drawNpcZhenyuanSectionLabel(gfx, (Component)header, (Component)tag, x, y, rightEnd, total > 0);
        y += 12;
        String[] labelKeys = new String[]{"zhenyuan.friday_cultivation.attr.constitution", "zhenyuan.friday_cultivation.attr.physique", "zhenyuan.friday_cultivation.attr.agility", "zhenyuan.friday_cultivation.attr.spell_power", "zhenyuan.friday_cultivation.attr.qi_sea"};
        int maxVal = 10;
        for (int v : vals) {
            maxVal = Math.max(maxVal, v);
        }
        if (maxVal % 5 != 0) {
            maxVal = (maxVal / 5 + 1) * 5;
        }
        int cx = (x + rightEnd) / 2;
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
            WanderingCultivatorScreen.fillTriangle(gfx, cx, cy, curX[i2], curY[i2], curX[j], curY[j], innerFillColor);
        }
        for (i2 = 0; i2 < 5; ++i2) {
            this.drawPixelLine(gfx, curX[i2], curY[i2], curX[(i2 + 1) % 5], curY[(i2 + 1) % 5], innerColor);
        }
        for (i2 = 0; i2 < 5; ++i2) {
            gfx.fill(curX[i2] - 1, curY[i2] - 1, curX[i2] + 2, curY[i2] + 2, dotColor);
        }
        int labelOffset = 5;
        for (i = 0; i < 5; ++i) {
            this.npcZhenyuanLabelRects[i][0] = 0;
            this.npcZhenyuanLabelRects[i][1] = 0;
            this.npcZhenyuanLabelRects[i][2] = 0;
            this.npcZhenyuanLabelRects[i][3] = 0;
        }
        for (i = 0; i < 5; ++i) {
            int boxX;
            MutableComponent label = Component.translatable((String)labelKeys[i]);
            MutableComponent labelWithValue = Component.translatable((String)"zhenyuan.friday_cultivation.attr.label_with_value", (Object[])new Object[]{label, vals[i]});
            int lx = cx + (int)Math.round(cosA[i] * (double)(radius + labelOffset));
            int ly = cy + (int)Math.round(sinA[i] * (double)(radius + labelOffset));
            int line1Y = switch (i) {
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
            this.drawTinyAt(gfx, (Component)labelWithValue, boxX, line1Y, -15067628);
            this.npcZhenyuanLabelRects[i][0] = boxX - 1;
            this.npcZhenyuanLabelRects[i][1] = line1Y - 1;
            this.npcZhenyuanLabelRects[i][2] = boxX + 50;
            this.npcZhenyuanLabelRects[i][3] = line1Y + 8;
        }
    }

    private void drawNpcZhenyuanSectionLabel(GuiGraphics gfx, Component label, Component tagText, int x, int y, int rightX, boolean tagActive) {
        gfx.fill(x, y, x + 2, y + 9, -4703686);
        gfx.drawString(this.font, label, x + 5, y + 1, -9807288, false);
        int textW = this.font.width((FormattedText)label);
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

    private void renderNpcZhenyuanTooltip(GuiGraphics gfx, int mouseX, int mouseY, WanderingCultivatorEntity npc, int attrIdx) {
        MutableComponent currentEffect;
        MutableComponent perPointDesc;
        String[] nameKeys = new String[]{"zhenyuan.friday_cultivation.attr.constitution", "zhenyuan.friday_cultivation.attr.physique", "zhenyuan.friday_cultivation.attr.agility", "zhenyuan.friday_cultivation.attr.spell_power", "zhenyuan.friday_cultivation.attr.qi_sea"};
        int[] points = npc.getZhenyuanAttrs();
        switch (attrIdx) {
            case 0: {
                perPointDesc = Component.translatable((String)"zhenyuan.friday_cultivation.tooltip.per_point.constitution");
                currentEffect = Component.translatable((String)"zhenyuan.friday_cultivation.tooltip.current.constitution", (Object[])new Object[]{points[0]});
                break;
            }
            case 1: {
                perPointDesc = Component.translatable((String)"zhenyuan.friday_cultivation.tooltip.per_point.physique");
                currentEffect = Component.translatable((String)"zhenyuan.friday_cultivation.tooltip.current.physique", (Object[])new Object[]{points[1], WanderingCultivatorScreen.formatZhenyuanPercent((double)points[1] * 1.0)});
                break;
            }
            case 2: {
                perPointDesc = Component.translatable((String)"zhenyuan.friday_cultivation.tooltip.per_point.agility");
                currentEffect = Component.translatable((String)"zhenyuan.friday_cultivation.tooltip.current.agility", (Object[])new Object[]{WanderingCultivatorScreen.formatZhenyuanPercent((double)points[2] * 1.0), WanderingCultivatorScreen.formatZhenyuanPercent((double)points[2] * 0.2)});
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
        gfx.renderComponentTooltip(this.font, new java.util.ArrayList<Component>(lines), mouseX, mouseY);
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

    private static void fillTriangle(GuiGraphics gfx, int x1, int y1, int x2, int y2, int x3, int y3, int color) {
        int minX = Math.min(x1, Math.min(x2, x3));
        int maxX = Math.max(x1, Math.max(x2, x3));
        int minY = Math.min(y1, Math.min(y2, y3));
        int maxY = Math.max(y1, Math.max(y2, y3));
        for (int px = minX; px <= maxX; ++px) {
            for (int py = minY; py <= maxY; ++py) {
                boolean hasPos;
                int d1 = WanderingCultivatorScreen.edgeSign(px, py, x1, y1, x2, y2);
                int d2 = WanderingCultivatorScreen.edgeSign(px, py, x2, y2, x3, y3);
                int d3 = WanderingCultivatorScreen.edgeSign(px, py, x3, y3, x1, y1);
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

    private static Technique.Bonus getTechniqueBonus(WanderingCultivatorEntity npc) {
        String techId = npc.getTechniqueId();
        if (techId == null || techId.isEmpty()) {
            return Technique.Bonus.NONE;
        }
        Technique t = Technique.byId(techId);
        return t == null ? Technique.Bonus.NONE : t.bonus();
    }

    private void renderItemsTab(GuiGraphics gfx, int x, int y, WanderingCultivatorEntity npc, int mouseX, int mouseY) {
        this.npcInvHoverRects.clear();
        this.spellHoverRects.clear();
        int rightEnd = this.leftPos + 356;
        this.drawSectionHeader(gfx, x, y, rightEnd, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.tech_section"));
        this.renderEquippedTechniqueBlock(gfx, x, y += 12, npc, rightEnd);
        List<String> spellIds = npc.getSpellIds();
        this.drawSectionHeader(gfx, x, y += 38, rightEnd, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.spell_section", (Object[])new Object[]{spellIds.size()}));
        y += 12;
        if (spellIds.isEmpty()) {
            gfx.drawString(this.font, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.no_spells").withStyle(ChatFormatting.GRAY), x + 4, y, -9807288, false);
            y += 12;
        } else {
            int spellRowH = 14;
            int maxVisible = 5;
            int total = spellIds.size();
            int spellMaxOffset = Math.max(0, total - maxVisible);
            if (this.spellScrollOffset > spellMaxOffset) {
                this.spellScrollOffset = spellMaxOffset;
            }
            if (this.spellScrollOffset < 0) {
                this.spellScrollOffset = 0;
            }
            int displayN = Math.min(maxVisible, total - this.spellScrollOffset);
            for (int i = 0; i < displayN; ++i) {
                int spellIdx = this.spellScrollOffset + i;
                Spell sp = Spell.byId(spellIds.get(spellIdx));
                this.renderSpellRow(gfx, x, y + i * spellRowH, sp, spellIdx, rightEnd - x - 8);
            }
            if (total > maxVisible) {
                int barX = rightEnd - 4;
                int barTop = y;
                int barBot = y + maxVisible * spellRowH;
                gfx.fill(barX, barTop, barX + 4, barBot, -10859978);
                int thumbH = Math.max(8, (barBot - barTop) * maxVisible / total);
                int thumbY = barTop + (barBot - barTop - thumbH) * this.spellScrollOffset / Math.max(1, spellMaxOffset);
                gfx.fill(barX + 1, thumbY, barX + 4, thumbY + thumbH, -3562934);
            }
            y += displayN * spellRowH;
        }
        this.drawSectionHeader(gfx, x, y += 4, rightEnd, (Component)Component.translatable((String)"screen.friday_cultivation.cultivator.inventory_section"));
        y += 12;
        List<ItemStack> displayInv = ((WanderingCultivatorMenu)this.menu).getDisplayInventory();
        int slotSize = 16;
        int gap = 1;
        int cols = Math.max(9, Math.min(10, (rightEnd - x - 4) / (slotSize + gap)));
        int rows = 3;
        int totalCells = cols * rows;
        for (int i = 0; i < totalCells; ++i) {
            ItemStack stack;
            int sx = x + i % cols * (slotSize + gap);
            int sy = y + i / cols * (slotSize + gap);
            gfx.fill(sx - 1, sy - 1, sx + slotSize + 1, sy + slotSize + 1, -3562934);
            gfx.fill(sx, sy, sx + slotSize, sy + slotSize, -1517128);
            if (i >= displayInv.size() || (stack = displayInv.get(i)).isEmpty()) continue;
            gfx.renderItem(stack, sx, sy);
            gfx.renderItemDecorations(this.font, stack, sx, sy);
            this.npcInvHoverRects.add(new int[]{i, sx, sy, slotSize, slotSize});
        }
    }

    private void renderEquippedTechniqueBlock(GuiGraphics gfx, int x, int y, WanderingCultivatorEntity npc, int rightEnd) {
        int elFg;
        MutableComponent elLabel;
        String techId = npc.getTechniqueId();
        int blockW = Math.max(166, rightEnd - x - 4);
        int blockH = 36;
        this.techBlockRect = new int[]{x, y, blockW, blockH};
        gfx.fill(x - 1, y - 1, x + blockW + 1, y + blockH + 1, -3562934);
        gfx.fill(x, y, x + blockW, y + blockH, -15067628);
        gfx.fill(x + 1, y + 1, x + blockW - 1, y + blockH - 1, -923956);
        if (techId.isEmpty()) {
            this.techBlockRect = new int[4];
            MutableComponent noTech = Component.translatable((String)"screen.friday_cultivation.cultivator.no_technique").withStyle(ChatFormatting.GRAY);
            gfx.drawString(this.font, (Component)noTech, x + 6, y + 14, -9807288, false);
            return;
        }
        Technique t = Technique.byId(techId);
        if (t == null) {
            return;
        }
        try {
            gfx.blit(t.iconTexture(), x + 4, y + 4, 16, 16, 0.0f, 0.0f, 32, 32, 32, 32);
        }
        catch (Exception ignored) {
            gfx.fill(x + 4, y + 4, x + 20, y + 20, -10859978);
        }
        MutableComponent name = t.displayName().copy().withStyle(ChatFormatting.BOLD).withStyle(s -> s.withColor(-4703686));
        gfx.drawString(this.font, (Component)name, x + 24, y + 6, -15067628, false);
        int badgeY = y + 22;
        MutableComponent tierLabel = Component.literal((String)"\u3010").append(t.tier().displayName()).append("\u3011");
        int tierFg = t.tier().rgb();
        int tierW = this.font.width((FormattedText)tierLabel) + 4;
        gfx.fill(x + 4, badgeY, x + 4 + tierW, badgeY + 10, -15067628);
        gfx.drawString(this.font, (Component)tierLabel, x + 6, badgeY + 1, tierFg, false);
        QiElement mainEl = WanderingCultivatorScreen.inferTechniqueMainElement(t);
        if (mainEl != null) {
            elLabel = Component.translatable((String)("element.friday_cultivation." + mainEl.id()));
            elFg = mainEl.rgb() | 0xFF000000;
        } else {
            elLabel = Component.translatable((String)"element.friday_cultivation.pure");
            elFg = -1;
        }
        int elW = this.font.width((FormattedText)elLabel) + 6;
        int elX = x + 4 + tierW + 4;
        gfx.fill(elX, badgeY, elX + elW, badgeY + 10, -15067628);
        gfx.drawString(this.font, (Component)elLabel, elX + 3, badgeY + 1, elFg, false);
    }

    private static QiElement inferTechniqueMainElement(Technique t) {
        Technique.Bonus b = t.bonus();
        QiElement best = null;
        double max = 1.0;
        for (QiElement el : QiElement.values()) {
            double m = b.spellMultFor(el);
            if (!(m > max)) continue;
            max = m;
            best = el;
        }
        return best;
    }

    private void renderSpellRow(GuiGraphics gfx, int x, int y, Spell sp, int spellIdx, int rowW) {
        if (sp == null) {
            return;
        }
        gfx.fill(x, y, x + rowW, y + 14, 0x40000000);
        try {
            int textureSize = sp.iconTextureSize();
            gfx.blit(sp.iconTexture(), x + 1, y - 1, 0.0f, 0.0f, 16, 16, textureSize, textureSize);
        }
        catch (Exception ignored) {
            gfx.fill(x + 1, y - 1, x + 17, y + 15, -10859978);
        }
        int color = sp.tier().rgb();
        MutableComponent name = sp.displayName().copy().withStyle(s -> s.withColor(color));
        if (sp.tier().ordinal() >= 3) {
            name = name.withStyle(ChatFormatting.BOLD);
        }
        gfx.drawString(this.font, (Component)name, x + 20, y + 2, -15067628, false);
        this.spellHoverRects.add(new int[]{spellIdx, x, y, rowW, 14});
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (this.isInsideRect((int)mouseX, (int)mouseY, this.closeButtonRect)) {
                this.onClose();
                this.playClickSound();
                return true;
            }
            if (this.sectButtonEnabled && this.sectButtonRect[2] > 0 && mouseX >= (double)this.sectButtonRect[0] && mouseX < (double)(this.sectButtonRect[0] + this.sectButtonRect[2]) && mouseY >= (double)this.sectButtonRect[1] && mouseY < (double)(this.sectButtonRect[1] + this.sectButtonRect[3])) {
                ModNetwork.CHANNEL.sendToServer((Object)new RequestSectScreenPacket(((WanderingCultivatorMenu)this.menu).getEntityId()));
                this.playClickSound();
                return true;
            }
            if (this.talkButtonEnabled && this.talkButtonRect[2] > 0 && mouseX >= (double)this.talkButtonRect[0] && mouseX < (double)(this.talkButtonRect[0] + this.talkButtonRect[2]) && mouseY >= (double)this.talkButtonRect[1] && mouseY < (double)(this.talkButtonRect[1] + this.talkButtonRect[3])) {
                ModNetwork.CHANNEL.sendToServer((Object)new RequestSectJoinDialoguePacket(((WanderingCultivatorMenu)this.menu).getEntityId()));
                if (this.minecraft != null && this.minecraft.player != null) {
                    this.minecraft.player.closeContainer();
                }
                this.playClickSound();
                return true;
            }
            Tab[] legacyTabs = TOP_TABS;
            for (int i = 0; i < legacyTabs.length; ++i) {
                int[] r = this.tabRects[i];
                if (r[2] == 0 || !(mouseX >= (double)r[0]) || !(mouseX < (double)(r[0] + r[2])) || !(mouseY >= (double)r[1]) || !(mouseY < (double)(r[1] + r[3]))) continue;
                if (this.currentTab != legacyTabs[i]) {
                    this.currentTab = legacyTabs[i];
                    this.updateSlotVisibility();
                    this.playClickSound();
                }
                return true;
            }
            if (this.currentTab == Tab.TRADE) {
                if (this.buyButtonRect[2] > 0 && mouseX >= (double)this.buyButtonRect[0] && mouseX < (double)(this.buyButtonRect[0] + this.buyButtonRect[2]) && mouseY >= (double)this.buyButtonRect[1] && mouseY < (double)(this.buyButtonRect[1] + this.buyButtonRect[3])) {
                    MerchantOffer sel;
                    MerchantOffers offers = ((WanderingCultivatorMenu)this.menu).getOffers();
                    if (this.selectedOfferIdx >= 0 && offers != null && this.selectedOfferIdx < offers.size() && this.playerCanAfford(sel = (MerchantOffer)offers.get(this.selectedOfferIdx)) && !sel.isOutOfStock()) {
                        ModNetwork.CHANNEL.sendToServer((Object)new ExecuteCultivatorTradePacket(((WanderingCultivatorMenu)this.menu).getEntityId(), this.selectedOfferIdx));
                        if (this.minecraft != null) {
                            this.minecraft.getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)SoundEvents.VILLAGER_TRADE, (float)1.0f));
                        }
                        return true;
                    }
                    this.playClickSound();
                    return true;
                }
                for (int[] r : this.offerHitboxes) {
                    if (!(mouseX >= (double)r[0]) || !(mouseX < (double)(r[0] + r[2])) || !(mouseY >= (double)r[1]) || !(mouseY < (double)(r[1] + r[3]))) continue;
                    this.selectedOfferIdx = r[4];
                    this.playClickSound();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.currentTab == Tab.TRADE) {
            this.tradeScrollOffset = Math.max(0, this.tradeScrollOffset - (int)Math.signum(delta));
            return true;
        }
        if (this.currentTab == Tab.ITEMS) {
            this.spellScrollOffset = Math.max(0, this.spellScrollOffset - (int)Math.signum(delta));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private void playClickSound() {
        if (this.minecraft != null) {
            this.minecraft.getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((Holder)SoundEvents.UI_BUTTON_CLICK, (float)1.0f));
        }
    }

    public boolean isPauseScreen() {
        return false;
    }

    private static enum Tab {
        DETAILS,
        TRADE,
        ITEMS;

    }

    private record DetailViewport(int left, int top, float scale) {
        int toLocalX(int screenX) {
            return Math.round((float)(screenX - this.left) / this.scale);
        }

        int toLocalY(int screenY) {
            return Math.round((float)(screenY - this.top) / this.scale);
        }
    }

    private record VirtualSlotRect(Slot slot, int x, int y, int w, int h) {
        boolean contains(double mouseX, double mouseY) {
            return mouseX >= (double)this.x && mouseX < (double)(this.x + this.w) && mouseY >= (double)this.y && mouseY < (double)(this.y + this.h);
        }

        VirtualSlotRect toScreen(DetailViewport viewport) {
            int[] rect = WanderingCultivatorScreen.toScreenRect(new int[]{this.x, this.y, this.w, this.h}, viewport);
            return new VirtualSlotRect(this.slot, rect[0], rect[1], rect[2], rect[3]);
        }
    }

    private record DetailStackHoverRect(int x, int y, int w, int h, ItemStack stack) {
        boolean contains(int mouseX, int mouseY) {
            return mouseX >= this.x && mouseX < this.x + this.w && mouseY >= this.y && mouseY < this.y + this.h;
        }

        DetailStackHoverRect toScreen(DetailViewport viewport) {
            int[] rect = WanderingCultivatorScreen.toScreenRect(new int[]{this.x, this.y, this.w, this.h}, viewport);
            return new DetailStackHoverRect(rect[0], rect[1], rect[2], rect[3], this.stack);
        }
    }

    private record DetailTextHoverRect(int x, int y, int w, int h, List<Component> lines) {
        boolean contains(int mouseX, int mouseY) {
            return mouseX >= this.x && mouseX < this.x + this.w && mouseY >= this.y && mouseY < this.y + this.h;
        }

        DetailTextHoverRect toScreen(DetailViewport viewport) {
            int[] rect = WanderingCultivatorScreen.toScreenRect(new int[]{this.x, this.y, this.w, this.h}, viewport);
            return new DetailTextHoverRect(rect[0], rect[1], rect[2], rect[3], this.lines);
        }
    }

    private record DetailCell(ItemStack stack, ResourceLocation texture, int textureSize, List<Component> tooltip) {
        static DetailCell empty() {
            return new DetailCell(ItemStack.EMPTY, null, 16, List.of());
        }

        static DetailCell stack(ItemStack stack) {
            return new DetailCell(stack == null ? ItemStack.EMPTY : stack.copy(), null, 16, List.of());
        }

        static DetailCell texture(ResourceLocation texture, int textureSize, List<Component> tooltip) {
            return new DetailCell(ItemStack.EMPTY, texture, textureSize, tooltip == null ? List.of() : List.copyOf(tooltip));
        }
    }
}

