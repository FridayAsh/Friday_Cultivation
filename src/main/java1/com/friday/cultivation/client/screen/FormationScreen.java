package com.friday.cultivation.client.screen;

import com.friday.cultivation.block.formation.FormationCorePlateBlockEntity;
import com.friday.cultivation.client.ClientFormationRangePreview;
import com.friday.cultivation.inventory.FormationMenu;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.SetFormationFlagPreviewPacket;
import com.friday.cultivation.network.SetFormationFlagRadiusPacket;
import com.friday.cultivation.network.SetFormationNamePacket;
import com.friday.cultivation.network.SyncFormationFlagsPacket;
import com.friday.cultivation.network.ToggleFormationPacket;
import com.friday.cultivation.qi.formation.FormationType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 阵法控制台界面 — 严格 1:1 复刻原 mod com.xiaoxiang.cultivation.client.screen.FormationScreen。
 */
public class FormationScreen
        extends AbstractContainerScreen<FormationMenu> {
    private static final ResourceLocation TEXTURE = FormationScreen.guiTexture("textures/gui/formation_screen.png");
    private static final int TEX_W = 512;
    private static final int TEX_H = 512;
    private static final int BG_U = 0;
    private static final int BG_V = 0;
    private static final int QI_TROUGH_U = 0;
    private static final int QI_TROUGH_V = 240;
    private static final int QI_TROUGH_W = 242;
    private static final int QI_TROUGH_H = 10;
    private static final int QI_FILL_U = 0;
    private static final int QI_FILL_V = 252;
    private static final int QI_FILL_H = 8;
    private static final int ROW_U = 0;
    private static final int ROW_V = 304;
    private static final int ROW_HOVER_V = 322;
    private static final int BADGE_U = 0;
    private static final int BADGE_V = 340;
    private static final int SMALL_BTN_U = 48;
    private static final int SMALL_BTN_HOVER_U = 62;
    private static final int SMALL_BTN_V = 340;
    private static final int PREVIEW_BTN_U = 80;
    private static final int PREVIEW_BTN_HOVER_U = 116;
    private static final int PREVIEW_BTN_V = 340;
    private static final int NAME_BTN_U = 0;
    private static final int NAME_BTN_HOVER_U = 54;
    private static final int NAME_BTN_DIRTY_U = 108;
    private static final int NAME_BTN_SAVED_U = 162;
    private static final int NAME_BTN_V = 356;
    private static final int MAIN_BTN_U = 0;
    private static final int MAIN_BTN_HOVER_U = 82;
    private static final int MAIN_BTN_DISABLED_U = 164;
    private static final int MAIN_BTN_V = 372;
    private static final int ICON_V = 396;
    private static final int ICON_SIZE = 12;
    private static final int ICON_NAME = 0;
    private static final int ICON_STATUS = 1;
    private static final int ICON_FLAG = 2;
    private static final int ICON_DRAIN = 3;
    private static final int ICON_QI = 4;
    private static final int ICON_RADIUS = 5;
    private static final int INK_BLACK = -15067628;
    private static final int INK_SOFT = -12766422;
    private static final int INK_FAINT = -9807288;
    private static final int VERMILLION = -4703686;
    private static final int GOLD_BORDER = -3562934;
    private static final int GOLD_BRIGHT = -10496;
    private static final int GOLD_TEXT_DARK = -7707624;
    private static final int GREEN_OK = -14835869;
    private static final int GREEN_INK = -12950192;
    private static final int PARCHMENT_SHADOW = 1430074154;
    private static final int SCROLL_TRACK = -2569816;
    private static final int BTN_X = 95;
    private static final int BTN_Y = 205;
    private static final int BTN_W = 80;
    private static final int BTN_H = 18;
    private static final int NAME_LABEL_X = 30;
    private static final int NAME_X = 78;
    private static final int NAME_Y = 27;
    private static final int NAME_W = 120;
    private static final int NAME_H = 14;
    private static final int NAME_CONFIRM_X = 204;
    private static final int NAME_CONFIRM_Y = 27;
    private static final int NAME_CONFIRM_W = 52;
    private static final int NAME_CONFIRM_H = 14;
    private static final int STATUS_Y = 54;
    private static final int STATUS_H = 30;
    private static final int QI_LABEL_Y = 90;
    private static final int QI_BAR_Y = 101;
    private static final int FLAG_PANEL_X = 12;
    private static final int FLAG_PANEL_Y = 116;
    private static final int FLAG_PANEL_W = 246;
    private static final int FLAG_PANEL_H = 84;
    private static final int FLAG_HEADER_H = 25;
    private static final int FLAG_ROW_H = 16;
    private static final int FLAG_VISIBLE_ROWS = 4;
    private static final int FLAG_SMALL_BTN = 12;
    private static final int FLAG_LINK_X = 86;
    private static final int FLAG_LINK_W = 42;
    private static final int FLAG_MINUS_X = 144;
    private static final int FLAG_RADIUS_FIELD_X = 158;
    private static final int FLAG_RADIUS_FIELD_W = 30;
    private static final int FLAG_PLUS_X = 190;
    private static final int FLAG_PREVIEW_X = 207;
    private static final int FLAG_PREVIEW_W = 34;
    private static final int FLAG_SCROLLBAR_X = 242;
    private static final int FLAG_SCROLLBAR_W = 4;
    @Nullable
    private EditBox nameField;
    @Nullable
    private EditBox radiusField;
    @Nullable
    private BlockPos editingRadiusPos;
    private boolean nameDirty = false;
    private long nameSavedFlashUntil = 0L;
    private boolean draggingFlagScrollbar = false;
    private final List<SyncFormationFlagsPacket.Entry> flagEntries = new ArrayList<SyncFormationFlagsPacket.Entry>();
    private int flagScroll = 0;

    public FormationScreen(FormationMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 270;
        this.imageHeight = 238;
        this.inventoryLabelY = 999;
        this.leftPos = 8;
        this.topPos = 8;
    }

    private static ResourceLocation guiTexture(String path) {
        return new ResourceLocation("friday_cultivation", path);
    }

    @Override
    protected void init() {
        super.init();
        this.nameField = new EditBox(this.font, this.leftPos + 78, this.topPos + 27, 120, 14, Component.translatable("screen.friday_cultivation.formation.name_label"));
        this.nameField.setMaxLength(32);
        this.nameField.setBordered(false);
        this.nameField.setValue(this.menu.getCustomName());
        this.nameField.setTextColor(1709588);
        this.nameField.setTextColorUneditable(6969928);
        this.nameField.setResponder(s -> {
            this.nameDirty = !Objects.equals(s == null ? "" : s, this.menu.getCustomName());
        });
        this.addRenderableWidget(this.nameField);
        this.radiusField = new EditBox(this.font, this.leftPos, this.topPos, 26, 12, Component.translatable("screen.friday_cultivation.formation.flag_column_radius"));
        this.radiusField.setMaxLength(3);
        this.radiusField.setBordered(false);
        this.radiusField.setTextColor(1709588);
        this.radiusField.setTextColorUneditable(6969928);
        this.radiusField.setFilter(s -> s.isEmpty() || s.chars().allMatch(Character::isDigit));
        this.radiusField.setVisible(false);
        this.addRenderableWidget(this.radiusField);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        this.blitPart(gfx, x, y, 0, 0, this.imageWidth, this.imageHeight);
        this.blitIcon(gfx, x + 15, y + 27 + 1, 0);
        gfx.drawString(this.font, Component.translatable("screen.friday_cultivation.formation.name_label"), x + 30, y + 27 + 3, -12766422, false);
        this.renderNameConfirmButton(gfx, x, y, mouseX, mouseY);
        this.renderStatusInfo(gfx, x, y);
        this.renderQiBar(gfx, x, y);
        this.renderFlagPanel(gfx, x, y, mouseX, mouseY);
        this.renderToggleButton(gfx, x, y, mouseX, mouseY);
        if (this.menu.getCurrentQi() <= 0L) {
            MutableComponent hint = Component.translatable("screen.friday_cultivation.formation.usage_hint").withStyle(ChatFormatting.ITALIC);
            int hintW = this.font.width(hint);
            gfx.drawString(this.font, hint, x + (this.imageWidth - hintW) / 2, y + 224, -9807288, false);
        }
    }

    private void blitPart(GuiGraphics gfx, int x, int y, int u, int v, int w, int h) {
        if (w <= 0 || h <= 0) {
            return;
        }
        gfx.blit(TEXTURE, x, y, (float) u, (float) v, w, h, 512, 512);
    }

    private void blitIcon(GuiGraphics gfx, int x, int y, int icon) {
        this.blitPart(gfx, x, y, icon * 12, 396, 12, 12);
    }

    private void confirmName() {
        if (this.nameField == null) {
            return;
        }
        String name = this.nameField.getValue();
        if (name == null) {
            name = "";
        }
        this.menu.setClientCustomName(name);
        ModNetwork.CHANNEL.sendToServer(new SetFormationNamePacket(name));
        this.nameDirty = false;
        this.nameSavedFlashUntil = System.currentTimeMillis() + 1500L;
        this.playClick();
    }

    private boolean isInNameConfirmButton(int mx, int my) {
        return mx >= this.leftPos + 204 && mx < this.leftPos + 204 + 52 && my >= this.topPos + 27 && my < this.topPos + 27 + 14;
    }

    private void renderNameConfirmButton(GuiGraphics gfx, int x, int y, int mouseX, int mouseY) {
        MutableComponent label;
        int buttonU;
        long now = System.currentTimeMillis();
        boolean justSaved = now < this.nameSavedFlashUntil;
        boolean hover = this.isInNameConfirmButton(mouseX, mouseY);
        int bx = x + 204;
        int by = y + 27;
        int textColor = -15067628;
        if (justSaved) {
            buttonU = 162;
            label = Component.translatable("screen.friday_cultivation.formation.name_saved");
            textColor = -14835869;
        } else if (this.nameDirty) {
            buttonU = 108;
            label = Component.translatable("screen.friday_cultivation.formation.name_confirm");
            textColor = -2342;
        } else {
            buttonU = hover ? 54 : 0;
            label = Component.translatable("screen.friday_cultivation.formation.name_confirm");
        }
        this.blitPart(gfx, bx, by, buttonU, 356, 52, 14);
        int tw = this.font.width(label);
        gfx.drawString(this.font, label, bx + (52 - tw) / 2, by + 3 + 1, textColor, false);
    }

    private void renderStatusInfo(GuiGraphics gfx, int x, int y) {
        MutableComponent statusValue;
        boolean activated = this.menu.isActivated();
        FormationType type = this.menu.getFormationType();
        MutableComponent mutableComponent = statusValue = activated ? Component.translatable("screen.friday_cultivation.formation.status_active") : Component.translatable("screen.friday_cultivation.formation.status_inactive");
        MutableComponent typeLine = activated && type != null ? (this.menu.getActiveFormationCount() > 1 ? Component.translatable("formation.friday_cultivation.multiple") : Component.translatable(type.translationKey())) : Component.translatable("screen.friday_cultivation.formation.no_active");
        this.drawInfoCard(gfx, x + 12, y + 54, 66, 30, 1, Component.translatable("screen.friday_cultivation.formation.card_status"), statusValue, activated ? -14835869 : -9807288, typeLine);
        this.drawInfoCard(gfx, x + 84, y + 54, 72, 30, 2, Component.translatable("screen.friday_cultivation.formation.card_flags"), Component.literal(String.valueOf(this.menu.getDetectedFlagsCount())), this.menu.getDetectedFlagsCount() > 0 ? -15067628 : -4703686, Component.translatable("screen.friday_cultivation.formation.core_center_hint"));
        int sources = this.menu.getSourcesInRange();
        MutableComponent drainValue = activated ? Component.translatable("screen.friday_cultivation.formation.drain_value", String.format("%.2f", this.menu.getDrainPerSec())) : Component.translatable("screen.friday_cultivation.formation.value_empty");
        MutableComponent sourceLine = activated && sources >= 0 ? Component.translatable("screen.friday_cultivation.formation.sources_short", sources) : Component.translatable("screen.friday_cultivation.formation.no_active");
        this.drawInfoCard(gfx, x + 162, y + 54, 96, 30, 3, Component.translatable("screen.friday_cultivation.formation.card_drain"), drainValue, activated && this.menu.getDrainPerSec() > 0.0 ? -4703686 : -9807288, sourceLine);
    }

    private void drawInfoCard(GuiGraphics gfx, int x, int y, int w, int h, int icon, Component title, Component value, int valueColor, Component subtitle) {
        this.blitIcon(gfx, x + 4, y + 4, icon);
        gfx.drawString(this.font, title, x + 19, y + 4, -9807288, false);
        String valueText = this.font.plainSubstrByWidth(value.getString(), w - 23);
        gfx.drawString(this.font, valueText, x + 19, y + 17, valueColor, false);
    }

    private void renderQiBar(GuiGraphics gfx, int x, int y) {
        Component qiLabel = Component.translatable("screen.friday_cultivation.formation.qi_pool");
        long cur = this.menu.getCurrentQi();
        long max = Math.max(1L, this.menu.getMaxQi());
        String text = cur + " / " + max;
        this.blitIcon(gfx, x + 16, y + 90 - 2, 4);
        gfx.drawString(this.font, qiLabel, x + 31, y + 90, -15067628, false);
        gfx.drawString(this.font, text, x + this.imageWidth - 14 - this.font.width(text), y + 90, -12766422, false);
        int barX = x + 14;
        int barY = y + 101;
        int barW = 242;
        double fillRatio = Mth.clamp((double) cur / (double) max, 0.0, 1.0);
        int fillW = (int) ((double) (barW - 2) * fillRatio);
        this.blitPart(gfx, barX, barY, 0, 240, barW, 10);
        if (fillW > 0) {
            this.blitPart(gfx, barX + 1, barY + 1, 0, 252, fillW, 8);
        }
    }

    private void renderFlagPanel(GuiGraphics gfx, int x, int y, int mouseX, int mouseY) {
        int index;
        int px = x + 12;
        int py = y + 116;
        this.blitIcon(gfx, px + 5, py + 4, 2);
        gfx.drawString(this.font, Component.translatable("screen.friday_cultivation.formation.flag_ranges"), px + 21, py + 5, -7707624, false);
        MutableComponent centerHint = Component.translatable("screen.friday_cultivation.formation.core_center_hint");
        gfx.drawString(this.font, centerHint, px + 246 - 4 - this.font.width(centerHint), py + 5, -9807288, false);
        if (this.flagEntries.isEmpty()) {
            this.cancelRadiusEdit();
            MutableComponent empty = Component.translatable("screen.friday_cultivation.formation.flag_empty").withStyle(ChatFormatting.GRAY);
            int emptyW = this.font.width(empty);
            gfx.drawString(this.font, empty, px + (246 - emptyW) / 2, py + 46, -12766422, false);
            return;
        }
        gfx.drawString(this.font, Component.translatable("screen.friday_cultivation.formation.flag_column_flag"), px + 8, py + 16, -9807288, false);
        gfx.drawString(this.font, Component.translatable("screen.friday_cultivation.formation.flag_column_link"), px + 88, py + 16, -9807288, false);
        gfx.drawString(this.font, Component.translatable("screen.friday_cultivation.formation.flag_column_radius"), px + 149, py + 16, -9807288, false);
        gfx.drawString(this.font, Component.translatable("screen.friday_cultivation.formation.flag_column_preview"), px + 205, py + 16, -9807288, false);
        int maxScroll = this.flagMaxScroll();
        this.flagScroll = Mth.clamp(this.flagScroll, 0, maxScroll);
        boolean hasScrollbar = this.hasFlagScrollbar();
        int firstRowY = py + 25;
        boolean editorPlaced = false;
        for (int row = 0; row < 4 && (index = this.flagScroll + row) < this.flagEntries.size(); ++row) {
            SyncFormationFlagsPacket.Entry entry = this.flagEntries.get(index);
            int rowY = firstRowY + row * 16;
            int rowRight = px + (hasScrollbar ? 241 : 244);
            boolean hover = mouseX >= px + 2 && mouseX < rowRight && mouseY >= rowY && mouseY < rowY + 16;
            int accent = entry.type().visualColor() & 0xFFFFFF | 0x55000000;
            this.blitPart(gfx, px, rowY, 0, hover ? 322 : 304, 246, 16);
            gfx.fill(px + 4, rowY + 3, px + 6, rowY + 16 - 3, accent);
            this.blitIcon(gfx, px + 8, rowY + 2, 5);
            MutableComponent label = Component.translatable(entry.type().translationKey());
            String labelText = this.font.plainSubstrByWidth(label.getString(), 58);
            gfx.drawString(this.font, labelText, px + 23, rowY + 4, -15067628, false);
            this.drawBadge(gfx, this.connectionLabel(entry), px + 86, rowY + 2, 42, 12);
            this.drawSmallButton(gfx, px + 144, rowY + 2, 12, 12, Component.literal("-"), this.isInFlagMinus(mouseX, mouseY, rowY, px));
            editorPlaced |= this.drawRadiusValue(gfx, entry, rowY, px);
            this.drawSmallButton(gfx, px + 190, rowY + 2, 12, 12, Component.literal("+"), this.isInFlagPlus(mouseX, mouseY, rowY, px));
            boolean visible = ClientFormationRangePreview.isVisible(this.menu.getCorePos(), entry.pos());
            MutableComponent previewLabel = Component.translatable(visible ? "screen.friday_cultivation.formation.flag_hide" : "screen.friday_cultivation.formation.flag_show");
            this.drawSmallButton(gfx, px + 207, rowY + 2, 34, 12, previewLabel, this.isInFlagPreview(mouseX, mouseY, rowY, px));
        }
        if (!editorPlaced && this.editingRadiusPos != null) {
            this.cancelRadiusEdit();
        }
        if (hasScrollbar) {
            this.renderFlagScrollbar(gfx, px, py);
        }
    }

    private boolean drawRadiusValue(GuiGraphics gfx, SyncFormationFlagsPacket.Entry entry, int rowY, int panelX) {
        int fieldX = panelX + 158;
        int fieldY = rowY + 2;
        this.blitPart(gfx, fieldX, fieldY, 0, 340, 30, 12);
        boolean editing = this.isEditingRadius(entry.pos());
        if (editing) {
            gfx.fill(fieldX + 1, fieldY + 1, fieldX + 30 - 1, fieldY + 2, -10496);
            this.positionRadiusField(rowY, panelX);
            return true;
        }
        String radiusText = String.valueOf(entry.radius());
        gfx.drawString(this.font, radiusText, fieldX + (30 - this.font.width(radiusText)) / 2, rowY + 4, -15067628, false);
        return false;
    }

    private void drawBadge(GuiGraphics gfx, Component label, int x, int y, int w, int h) {
        this.blitPart(gfx, x, y, 0, 340, w, h);
        String text = this.font.plainSubstrByWidth(label.getString(), w - 4);
        gfx.drawString(this.font, text, x + (w - this.font.width(text)) / 2, y + 2, -12766422, false);
    }

    private MutableComponent connectionLabel(SyncFormationFlagsPacket.Entry entry) {
        if (entry.runeLinked()) {
            return Component.translatable("screen.friday_cultivation.formation.flag_connection_rune");
        }
        if (entry.directLinked()) {
            return Component.translatable("screen.friday_cultivation.formation.flag_connection_direct");
        }
        return Component.translatable("screen.friday_cultivation.formation.flag_connection_manual");
    }

    private void drawSmallButton(GuiGraphics gfx, int x, int y, int w, int h, Component label, boolean hover) {
        if (w == 34) {
            this.blitPart(gfx, x, y, hover ? 116 : 80, 340, w, h);
        } else {
            this.blitPart(gfx, x, y, hover ? 62 : 48, 340, w, h);
        }
        int labelW = this.font.width(label);
        gfx.drawString(this.font, label, x + (w - labelW) / 2, y + 2, -15067628, false);
    }

    private void renderFlagScrollbar(GuiGraphics gfx, int panelX, int panelY) {
        int trackX = panelX + 242;
        int trackY = panelY + 25;
        int trackH = 64;
        int thumbH = this.flagScrollbarThumbHeight();
        int thumbY = this.flagScrollbarThumbY();
        gfx.fill(trackX, trackY, trackX + 4, trackY + trackH, -15067628);
        gfx.fill(trackX + 1, trackY + 1, trackX + 4 - 1, trackY + trackH - 1, -2569816);
        gfx.fill(trackX + 1, thumbY, trackX + 4 - 1, thumbY + thumbH, -3562934);
        if (thumbH > 12) {
            int midY = thumbY + thumbH / 2;
            gfx.fill(trackX + 1, midY - 1, trackX + 4 - 1, midY + 1, -4703686);
        }
    }

    private void renderToggleButton(GuiGraphics gfx, int x, int y, int mouseX, int mouseY) {
        MutableComponent btnLabel;
        boolean enabled;
        boolean activated = this.menu.isActivated();
        if (activated) {
            enabled = true;
            btnLabel = Component.translatable("screen.friday_cultivation.formation.btn_deactivate").withStyle(ChatFormatting.GOLD);
        } else {
            enabled = this.menu.getDetectedFlagsCount() >= 1 && this.menu.getCurrentQi() > 0L;
            btnLabel = Component.translatable("screen.friday_cultivation.formation.btn_activate").withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY);
        }
        boolean hover = this.isInButton(mouseX, mouseY, x, y);
        int buttonU = !enabled ? 164 : (hover ? 82 : 0);
        int bx = x + 95;
        int by = y + 205;
        this.blitPart(gfx, bx, by, buttonU, 372, 80, 18);
        int tw = this.font.width(btnLabel);
        gfx.drawString(this.font, btnLabel, bx + (80 - tw) / 2, by + 5 + 1, enabled ? -15067628 : -9807288, false);
        if (!activated && !enabled && hover) {
            List<Component> tooltip = new ArrayList<Component>();
            if (this.menu.getDetectedFlagsCount() < 1) {
                tooltip.add(Component.translatable("screen.friday_cultivation.formation.disabled.no_flags").withStyle(ChatFormatting.RED));
            }
            if (this.menu.getCurrentQi() <= 0L) {
                tooltip.add(Component.translatable("screen.friday_cultivation.formation.disabled.no_qi").withStyle(ChatFormatting.RED));
            }
            if (!tooltip.isEmpty()) {
                gfx.renderTooltip(this.font, tooltip, Optional.empty(), mouseX, mouseY);
            }
        }
    }

    private boolean isInButton(int mouseX, int mouseY, int x, int y) {
        return mouseX >= x + 95 && mouseX < x + 95 + 80 && mouseY >= y + 205 && mouseY < y + 205 + 18;
    }

    private boolean isInFlagPanel(double mouseX, double mouseY) {
        int px = this.leftPos + 12;
        int py = this.topPos + 116;
        return mouseX >= (double) px && mouseX < (double) (px + 246) && mouseY >= (double) py && mouseY < (double) (py + 84);
    }

    private int hoveredFlagIndex(double mouseX, double mouseY) {
        int py = this.topPos + 116 + 25;
        int row = ((int) mouseY - py) / 16;
        if (row < 0 || row >= 4) {
            return -1;
        }
        int index = this.flagScroll + row;
        return index >= 0 && index < this.flagEntries.size() ? index : -1;
    }

    private boolean isInFlagMinus(int mouseX, int mouseY, int rowY, int panelX) {
        return mouseX >= panelX + 144 && mouseX < panelX + 144 + 12 && mouseY >= rowY + 2 && mouseY < rowY + 2 + 12;
    }

    private boolean isInFlagPlus(int mouseX, int mouseY, int rowY, int panelX) {
        return mouseX >= panelX + 190 && mouseX < panelX + 190 + 12 && mouseY >= rowY + 2 && mouseY < rowY + 2 + 12;
    }

    private boolean isInFlagRadiusField(int mouseX, int mouseY, int rowY, int panelX) {
        return mouseX >= panelX + 158 && mouseX < panelX + 158 + 30 && mouseY >= rowY + 2 && mouseY < rowY + 2 + 12;
    }

    private boolean isInFlagPreview(int mouseX, int mouseY, int rowY, int panelX) {
        return mouseX >= panelX + 207 && mouseX < panelX + 207 + 34 && mouseY >= rowY + 2 && mouseY < rowY + 2 + 12;
    }

    private boolean hasFlagScrollbar() {
        return this.flagMaxScroll() > 0;
    }

    private int flagMaxScroll() {
        return Math.max(0, this.flagEntries.size() - 4);
    }

    private int flagScrollbarTrackTop() {
        return this.topPos + 116 + 25;
    }

    private int flagScrollbarTrackHeight() {
        return 64;
    }

    private int flagScrollbarThumbHeight() {
        int trackH = this.flagScrollbarTrackHeight() - 2;
        return Math.max(10, (int) ((double) trackH * 4.0 / (double) Math.max(4, this.flagEntries.size())));
    }

    private int flagScrollbarThumbY() {
        int trackY = this.flagScrollbarTrackTop();
        int trackH = this.flagScrollbarTrackHeight();
        int thumbH = this.flagScrollbarThumbHeight();
        int range = Math.max(1, trackH - 2 - thumbH);
        double progress = this.flagMaxScroll() > 0 ? (double) this.flagScroll / (double) this.flagMaxScroll() : 0.0;
        return trackY + 1 + (int) Math.round((double) range * progress);
    }

    private boolean isInFlagScrollbar(double mouseX, double mouseY) {
        if (!this.hasFlagScrollbar()) {
            return false;
        }
        int trackX = this.leftPos + 12 + 242;
        int trackY = this.flagScrollbarTrackTop();
        int trackH = this.flagScrollbarTrackHeight();
        return mouseX >= (double) trackX && mouseX < (double) (trackX + 4) && mouseY >= (double) trackY && mouseY < (double) (trackY + trackH);
    }

    private void setFlagScrollFromMouse(double mouseY) {
        int maxScroll = this.flagMaxScroll();
        if (maxScroll <= 0) {
            this.flagScroll = 0;
            return;
        }
        int trackY = this.flagScrollbarTrackTop();
        int trackH = this.flagScrollbarTrackHeight();
        int thumbH = this.flagScrollbarThumbHeight();
        int range = Math.max(1, trackH - 2 - thumbH);
        double offset = mouseY - (double) trackY - 1.0 - (double) thumbH / 2.0;
        int next = (int) Math.round(offset / (double) range * (double) maxScroll);
        this.flagScroll = Mth.clamp(next, 0, maxScroll);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.radiusField != null && this.radiusField.isFocused()) {
            if (keyCode == 257 || keyCode == 335) {
                this.commitRadiusEdit();
                return true;
            }
            if (keyCode == 256) {
                this.cancelRadiusEdit();
                return true;
            }
        }
        if ((keyCode == 257 || keyCode == 335) && this.nameField != null && this.nameField.isFocused() && this.nameDirty) {
            this.confirmName();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.isInNameConfirmButton((int) mouseX, (int) mouseY)) {
            this.commitRadiusEdit();
            this.confirmName();
            return true;
        }
        if (button == 0 && this.isInFlagPanel(mouseX, mouseY)) {
            if (this.isInFlagScrollbar(mouseX, mouseY)) {
                this.commitRadiusEdit();
                this.draggingFlagScrollbar = true;
                this.setFlagScrollFromMouse(mouseY);
                this.playClick();
                return true;
            }
            int index = this.hoveredFlagIndex(mouseX, mouseY);
            if (index >= 0) {
                SyncFormationFlagsPacket.Entry entry = this.flagEntries.get(index);
                int rowY = this.topPos + 116 + 25 + (index - this.flagScroll) * 16;
                int panelX = this.leftPos + 12;
                if (this.isInFlagRadiusField((int) mouseX, (int) mouseY, rowY, panelX)) {
                    this.startRadiusEdit(entry, rowY, panelX);
                    return true;
                }
                this.commitRadiusEdit();
                if (this.isInFlagMinus((int) mouseX, (int) mouseY, rowY, panelX)) {
                    this.sendRadius(entry, entry.radius() - 1);
                    this.playClick();
                    return true;
                }
                if (this.isInFlagPlus((int) mouseX, (int) mouseY, rowY, panelX)) {
                    this.sendRadius(entry, entry.radius() + 1);
                    this.playClick();
                    return true;
                }
                if (this.isInFlagPreview((int) mouseX, (int) mouseY, rowY, panelX)) {
                    boolean visible = ClientFormationRangePreview.isVisible(this.menu.getCorePos(), entry.pos());
                    ModNetwork.CHANNEL.sendToServer(new SetFormationFlagPreviewPacket(entry.pos(), !visible));
                    this.playClick();
                    return true;
                }
            }
        }
        if (button == 0) {
            this.commitRadiusEdit();
        }
        if (button == 0 && this.isInButton((int) mouseX, (int) mouseY, this.leftPos, this.topPos)) {
            boolean activated = this.menu.isActivated();
            boolean canClick = activated || this.menu.getDetectedFlagsCount() >= 1 && this.menu.getCurrentQi() > 0L;
            if (canClick) {
                ModNetwork.CHANNEL.sendToServer(new ToggleFormationPacket(!activated));
                this.playClick();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && this.draggingFlagScrollbar) {
            this.setFlagScrollFromMouse(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && this.draggingFlagScrollbar) {
            this.draggingFlagScrollbar = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.isInFlagPanel(mouseX, mouseY) && this.hasFlagScrollbar()) {
            this.commitRadiusEdit();
            this.flagScroll = Mth.clamp(this.flagScroll + (delta < 0.0 ? 1 : -1), 0, this.flagMaxScroll());
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private void startRadiusEdit(SyncFormationFlagsPacket.Entry entry, int rowY, int panelX) {
        if (this.radiusField == null) {
            return;
        }
        this.editingRadiusPos = entry.pos();
        String value = String.valueOf(FormationCorePlateBlockEntity.clampFlagEffectRadius(entry.radius()));
        this.radiusField.setValue(value);
        this.radiusField.setVisible(true);
        this.positionRadiusField(rowY, panelX);
        this.radiusField.setFocused(true);
        this.radiusField.setCursorPosition(value.length());
        this.radiusField.setHighlightPos(0);
        this.setFocused(this.radiusField);
        this.playClick();
    }

    private void positionRadiusField(int rowY, int panelX) {
        if (this.radiusField == null) {
            return;
        }
        this.radiusField.setX(panelX + 158 + 2);
        this.radiusField.setY(rowY + 3);
        this.radiusField.setVisible(true);
    }

    private boolean isEditingRadius(BlockPos pos) {
        return this.editingRadiusPos != null && this.editingRadiusPos.equals(pos);
    }

    private void commitRadiusEdit() {
        if (this.radiusField == null || this.editingRadiusPos == null) {
            return;
        }
        String raw = this.radiusField.getValue();
        if (raw != null && !raw.isBlank()) {
            int parsed;
            try {
                parsed = Integer.parseInt(raw);
            } catch (NumberFormatException ignored) {
                parsed = 8;
            }
            int clamped = FormationCorePlateBlockEntity.clampFlagEffectRadius(parsed);
            SyncFormationFlagsPacket.Entry current = this.findEntry(this.editingRadiusPos);
            if (current == null || current.radius() != clamped) {
                ModNetwork.CHANNEL.sendToServer(new SetFormationFlagRadiusPacket(this.editingRadiusPos, clamped));
                this.playClick();
            }
        }
        this.cancelRadiusEdit();
    }

    private void cancelRadiusEdit() {
        this.editingRadiusPos = null;
        if (this.radiusField != null) {
            this.radiusField.setValue("");
            this.radiusField.setVisible(false);
            this.radiusField.setFocused(false);
            if (this.getFocused() == this.radiusField) {
                this.setFocused(null);
            }
        }
    }

    private void sendRadius(SyncFormationFlagsPacket.Entry entry, int requestedRadius) {
        int clamped = FormationCorePlateBlockEntity.clampFlagEffectRadius(requestedRadius);
        if (clamped != entry.radius()) {
            ModNetwork.CHANNEL.sendToServer(new SetFormationFlagRadiusPacket(entry.pos(), clamped));
        }
    }

    @Nullable
    private SyncFormationFlagsPacket.Entry findEntry(BlockPos pos) {
        for (SyncFormationFlagsPacket.Entry entry : this.flagEntries) {
            if (!entry.pos().equals(pos)) continue;
            return entry;
        }
        return null;
    }

    private void playClick() {
        if (this.minecraft != null) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        }
    }

    public void setFlagEntries(BlockPos corePos, List<SyncFormationFlagsPacket.Entry> entries) {
        if (!this.menu.getCorePos().equals(corePos)) {
            return;
        }
        this.flagEntries.clear();
        this.flagEntries.addAll(entries);
        ClientFormationRangePreview.retainForCore(this.menu.getCorePos(), this.flagEntries.stream().map(SyncFormationFlagsPacket.Entry::pos).toList());
        this.flagScroll = Mth.clamp(this.flagScroll, 0, this.flagMaxScroll());
        if (this.editingRadiusPos != null && this.findEntry(this.editingRadiusPos) == null) {
            this.cancelRadiusEdit();
        }
        for (SyncFormationFlagsPacket.Entry entry : this.flagEntries) {
            ClientFormationRangePreview.updateIfVisible(this.menu.getCorePos(), entry.pos(), entry.radius(), entry.typeOrdinal());
        }
    }

    private void renderFlagTooltip(GuiGraphics gfx, int mouseX, int mouseY) {
        if (!this.isInFlagPanel(mouseX, mouseY)) {
            return;
        }
        if (this.isInFlagScrollbar(mouseX, mouseY)) {
            return;
        }
        if (this.radiusField != null && this.radiusField.visible && this.radiusField.isMouseOver((double) mouseX, (double) mouseY)) {
            return;
        }
        int index = this.hoveredFlagIndex(mouseX, mouseY);
        if (index < 0 || index >= this.flagEntries.size()) {
            return;
        }
        SyncFormationFlagsPacket.Entry entry = this.flagEntries.get(index);
        List<Component> tooltip = new ArrayList<Component>();
        tooltip.add(Component.translatable(entry.type().translationKey()).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("screen.friday_cultivation.formation.flag_tooltip_pos", entry.pos().getX(), entry.pos().getY(), entry.pos().getZ()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("screen.friday_cultivation.formation.flag_tooltip_connection", this.connectionLabel(entry)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("screen.friday_cultivation.formation.flag_tooltip_radius", entry.radius()).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("screen.friday_cultivation.formation.flag_tooltip_center").withStyle(ChatFormatting.DARK_AQUA));
        gfx.renderTooltip(this.font, tooltip, Optional.empty(), mouseX, mouseY);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics gfx, int mouseX, int mouseY) {
        String customName = this.menu.getCustomName();
        Component header = customName == null || customName.isEmpty() ? this.title : Component.translatable("screen.friday_cultivation.formation.title_named", customName, this.title).withStyle(ChatFormatting.GOLD);
        String headerText = this.font.plainSubstrByWidth(header.getString(), this.imageWidth - 16);
        gfx.drawString(this.font, headerText, this.leftPos, this.topPos, -15067628, false);
    }

    @Override
    public void render(@NotNull GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gfx);
        super.render(gfx, mouseX, mouseY, partialTick);
        this.renderTooltip(gfx, mouseX, mouseY);
        this.renderFlagTooltip(gfx, mouseX, mouseY);
    }

    @Override
    public void onClose() {
        this.commitRadiusEdit();
        super.onClose();
    }
}
