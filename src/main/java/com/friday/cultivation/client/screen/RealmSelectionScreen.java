package com.friday.cultivation.client.screen;

import com.friday.cultivation.client.screen.widget.CinnabarButton;
import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.cultivation.realm.SubStage;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.RealmSelectionPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;

public class RealmSelectionScreen extends Screen {
    private static final int PANEL_W = 320;
    private static final int PANEL_H = 200;
    private static final ResourceLocation BG_TEXTURE = new ResourceLocation("friday_cultivation", "textures/gui/cultivation_bg.png");

    // 水墨基调
    private static final int INK_BLACK = -15067628;
    private static final int INK_SOFT = -13819625;
    private static final int INK_MUTE = -11979486;
    private static final int BORDER_LIGHT = -1516872;
    private static final int GOLD = -2047936;
    private static final int COFFEE = -9420002;
    private static final int JADE = -11093851;
    private static final int VERMILLION = -4703686;

    // 新增：统一用色
    private static final int CHIP_BG = -12635095;
    private static final int CHIP_HOVER_BG = JADE;
    private static final int POPUP_BG = -14739438;
    private static final int TEXT_LIGHT = -5720;
    private static final int TEXT_MUTED = -726312;
    private static final int TEXT_HINT = INK_BLACK; // 问题1：深墨色，对比度足够
    private static final int SHADOW = 0x55000000;
    private static final int HIGHLIGHT = 0x33FFFFFF;
    private static final int TITLE_HIGHLIGHT = 0xFFD65A5A;

    // 问题2b：下拉器金色边框（已按用户反馈减细 60%，2px 金边 + 1px 内衬）
    private static final int CHIP_BORDER_GOLD = 0xFFD4AF37;
    private static final int CHIP_BORDER_INNER = 0xFF4A3A2A;
    private static final int CHIP_BORDER = 2;
    private static final int CHIP_INSET = CHIP_BORDER + 1; // 3

    private final List<Realm> realms;
    private Realm selectedRealm;
    private int selectedSubStageLevel;
    private boolean realmDropdownOpen = false;
    private boolean subStageDropdownOpen = false;

    // 以下字段被 mouseClicked / renderOpenDropdown 依赖，必须保留并同步更新
    private int panelLeft;
    private int panelTop;
    private int realmChipX;
    private int realmChipY;
    private int realmChipW;
    private int realmChipH;
    private int subChipX;
    private int subChipY;
    private int subChipW;
    private int subChipH;
    private final List<int[]> dropdownOptionRects = new ArrayList<>();

    // 按钮引用，用于主按钮描边高亮
    private Button confirmButton;
    private Button cancelButton;

    // 当前渲染动画时间
    private float animTime;

    public RealmSelectionScreen() {
        super(Component.translatable("screen.friday_cultivation.realm_selector.title"));
        List<Realm> list = new ArrayList<>(Realm.logicalOrder());
        this.realms = list;

        Realm curRealm = Realm.MORTAL;
        int curSubLevel = 0;
        net.minecraft.client.player.LocalPlayer lp = Minecraft.getInstance().player;
        if (lp != null) {
            CultivationData data = (CultivationData) CultivationCapability.get((net.minecraft.world.entity.player.Player) lp).orElse(null);
            if (data != null) {
                curRealm = data.getRealm();
                if (curRealm == Realm.LOOSE_IMMORTAL) {
                    curSubLevel = data.getLooseImmortalTribulations();
                } else {
                    curSubLevel = data.getSubStage().level();
                }
            }
        }
        if (!list.contains(curRealm)) {
            curRealm = list.get(0);
        }
        this.selectedRealm = curRealm;
        List<Integer> levels = this.currentSubStageLevels(curRealm);
        this.selectedSubStageLevel = levels.contains(curSubLevel) ? curSubLevel : this.firstSubStageLevel(curRealm);
    }

    private boolean currentRealmHasSubStages() {
        return !this.currentSubStageLevels(this.selectedRealm).isEmpty();
    }

    private int firstSubStageLevel(Realm realm) {
        List<Integer> levels = this.currentSubStageLevels(realm);
        return levels.isEmpty() ? 0 : levels.get(0);
    }

    private List<Integer> currentSubStageLevels(Realm realm) {
        List<Integer> list = new ArrayList<>();
        if (realm == Realm.LOOSE_IMMORTAL) {
            for (int i = 1; i <= 9; ++i) {
                list.add(i);
            }
            return list;
        }
        int count = realm.subStageCount();
        if (count <= 1) {
            return list;
        }
        for (int i = 0; i < count; ++i) {
            SubStage s = realm.usesNumericLevels() ? realm.subStageAt(i + 1) : realm.subStageAt(i);
            if (s == null) continue;
            list.add(s.level());
        }
        return list;
    }

    private Component subStageDisplayName(Realm realm, int level) {
        if (realm == Realm.LOOSE_IMMORTAL) {
            return Component.translatable("realm.friday_cultivation.loose_immortal.level." + level);
        }
        SubStage s = realm.subStageAt(level);
        return s != null ? s.displayName() : Component.literal(Integer.toString(level));
    }

    @Override
    protected void init() {
        this.panelLeft = (this.width - PANEL_W) / 2;
        this.panelTop = (this.height - PANEL_H) / 2;
        int cx = this.width / 2;

        // 下拉器尺寸：给 4px 金框+1px 内衬留空间
        int chipY = this.panelTop + 64;
        int chipW = 136;
        int chipH = 20;
        this.realmChipX = cx - 140;
        this.realmChipY = chipY;
        this.realmChipW = chipW;
        this.realmChipH = chipH;
        this.subChipX = cx + 4;
        this.subChipY = chipY;
        this.subChipW = chipW;
        this.subChipH = chipH;

        int bottomY = this.panelTop + PANEL_H - 28;
        this.confirmButton = new CinnabarButton(cx - 140, bottomY, 136, 18,
                Component.translatable("screen.friday_cultivation.realm_selector.confirm"),
                b -> this.confirm());
        this.cancelButton = new CinnabarButton(cx + 4, bottomY, 136, 18,
                Component.translatable("screen.friday_cultivation.realm_selector.cancel"),
                b -> Minecraft.getInstance().setScreen(null));
        this.addRenderableWidget(this.confirmButton);
        this.addRenderableWidget(this.cancelButton);
    }

    private int chipTextWidth(Component c) {
        return (int) Math.ceil(this.font.width((FormattedText) c) * 0.72f);
    }

    private void drawScaled(GuiGraphics gfx, Component text, int x, int y, int color, float scale) {
        gfx.pose().pushPose();
        gfx.pose().translate(x, y, 0.0f);
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.drawString(this.font, text, 0, 0, color, false);
        gfx.pose().popPose();
    }

    private void drawScaledShadowed(GuiGraphics gfx, Component text, int x, int y, int color, float scale) {
        gfx.pose().pushPose();
        gfx.pose().translate(x, y, 0.0f);
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.drawString(this.font, text, 0, 0, color, true);
        gfx.pose().popPose();
    }

    /**
     * 呼吸 alpha：把 rgb 的 alpha 通道按正弦波动。
     */
    private int pulseColor(int rgb, float speed, float minA, float maxA) {
        float t = (float) Math.sin(this.animTime * speed);
        int a = (int) (minA + (maxA - minA) * (t * 0.5f + 0.5f));
        if (a < 0) a = 0;
        if (a > 255) a = 255;
        return (a << 24) | (rgb & 0xFFFFFF);
    }

    private void drawTitleHeader(GuiGraphics gfx) {
        int x1 = this.panelLeft + 8;
        int y1 = this.panelTop + 8;
        int x2 = this.panelLeft + PANEL_W - 8;
        int y2 = this.panelTop + 32;

        // 外框
        gfx.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, INK_BLACK);
        // 朱砂底
        gfx.fill(x1, y1, x2, y2, VERMILLION);
        // 顶部呼吸高光
        gfx.fill(x1, y1, x2, y1 + 2, pulseColor(TITLE_HIGHLIGHT, 0.12f, 100, 200));
        // 底部暗化阴影带
        gfx.fill(x1, y2 - 4, x2, y2, 0x44000000);

        // 标题下金线（带呼吸）
        int lineY = this.panelTop + 31;
        int goldPulse = pulseColor(GOLD, 0.10f, 120, 230);
        gfx.fill(this.panelLeft + 40, lineY, this.panelLeft + 280, lineY + 1, goldPulse);
        gfx.fill(this.panelLeft + 40, lineY + 1, this.panelLeft + 280, lineY + 2, INK_SOFT);
        // 线端小印
        gfx.fill(this.panelLeft + 36, lineY - 1, this.panelLeft + 39, lineY + 2, goldPulse);
        gfx.fill(this.panelLeft + 281, lineY - 1, this.panelLeft + 284, lineY + 2, goldPulse);
    }

    private void drawPanelDecorations(GuiGraphics gfx, int cx, int bottomY) {
        // 按钮上方 subtle 分隔
        int botLineY = bottomY - 10;
        gfx.fill(this.panelLeft + 56, botLineY, this.panelLeft + 264, botLineY + 1, INK_SOFT);

        // 四角回纹角标
        int len = 8;
        // 左上
        gfx.fill(this.panelLeft + 4, this.panelTop + 4, this.panelLeft + 4 + len, this.panelTop + 5, BORDER_LIGHT);
        gfx.fill(this.panelLeft + 4, this.panelTop + 4, this.panelLeft + 5, this.panelTop + 4 + len, BORDER_LIGHT);
        // 右上
        gfx.fill(this.panelLeft + 316 - len, this.panelTop + 4, this.panelLeft + 316, this.panelTop + 5, BORDER_LIGHT);
        gfx.fill(this.panelLeft + 315, this.panelTop + 4, this.panelLeft + 316, this.panelTop + 4 + len, BORDER_LIGHT);
        // 左下
        gfx.fill(this.panelLeft + 4, this.panelTop + 195, this.panelLeft + 4 + len, this.panelTop + 196, BORDER_LIGHT);
        gfx.fill(this.panelLeft + 4, this.panelTop + 196 - len, this.panelLeft + 5, this.panelTop + 196, BORDER_LIGHT);
        // 右下
        gfx.fill(this.panelLeft + 316 - len, this.panelTop + 195, this.panelLeft + 316, this.panelTop + 196, BORDER_LIGHT);
        gfx.fill(this.panelLeft + 315, this.panelTop + 196 - len, this.panelLeft + 316, this.panelTop + 196, BORDER_LIGHT);
    }

    private void drawDropdownArrow(GuiGraphics gfx, int x, int y, int color) {
        gfx.fill(x, y, x + 5, y + 1, color);
        gfx.fill(x + 1, y + 1, x + 4, y + 2, color);
        gfx.fill(x + 2, y + 2, x + 3, y + 3, color);
    }

    private void drawCornerStud(GuiGraphics gfx, int x, int y, int gold, int dark) {
        gfx.fill(x, y, x + 3, y + 3, gold);
        gfx.fill(x + 1, y + 1, x + 2, y + 2, dark);
    }

    private void drawDropdownChip(GuiGraphics gfx, int x, int y, int w, int h, Component currentValue, boolean open, int mouseX, int mouseY) {
        boolean hover = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        int bg = open ? VERMILLION : (hover ? CHIP_HOVER_BG : CHIP_BG);
        int edgeColor = open ? 0x66FFD700 : (hover ? 0x55FFFFFF : 0x33000000);

        // 内容区内嵌区域（金框 4px + 棕线 1px）
        int ix1 = x + CHIP_INSET;
        int iy1 = y + CHIP_INSET;
        int ix2 = x + w - CHIP_INSET;
        int iy2 = y + h - CHIP_INSET;
        int iw = ix2 - ix1;
        int ih = iy2 - iy1;

        // 1. 金色主边框（4px，扁平硬边）
        gfx.fill(x, y, x + w, y + CHIP_BORDER, CHIP_BORDER_GOLD);
        gfx.fill(x, y + h - CHIP_BORDER, x + w, y + h, CHIP_BORDER_GOLD);
        gfx.fill(x, y + CHIP_BORDER, x + CHIP_BORDER, y + h - CHIP_BORDER, CHIP_BORDER_GOLD);
        gfx.fill(x + w - CHIP_BORDER, y + CHIP_BORDER, x + w, y + h - CHIP_BORDER, CHIP_BORDER_GOLD);

        // 2. 深棕内衬线（1px）
        gfx.fill(x + CHIP_BORDER, y + CHIP_BORDER, x + w - CHIP_BORDER, y + CHIP_BORDER + 1, CHIP_BORDER_INNER);
        gfx.fill(x + CHIP_BORDER, y + h - CHIP_BORDER - 1, x + w - CHIP_BORDER, y + h - CHIP_BORDER, CHIP_BORDER_INNER);
        gfx.fill(x + CHIP_BORDER, y + CHIP_BORDER, x + CHIP_BORDER + 1, y + h - CHIP_BORDER, CHIP_BORDER_INNER);
        gfx.fill(x + w - CHIP_BORDER - 1, y + CHIP_BORDER, x + w - CHIP_BORDER, y + h - CHIP_BORDER, CHIP_BORDER_INNER);

        // 3. 四角铆钉/铜包角
        drawCornerStud(gfx, x, y, CHIP_BORDER_GOLD, CHIP_BORDER_INNER);
        drawCornerStud(gfx, x + w - 3, y, CHIP_BORDER_GOLD, CHIP_BORDER_INNER);
        drawCornerStud(gfx, x, y + h - 3, CHIP_BORDER_GOLD, CHIP_BORDER_INNER);
        drawCornerStud(gfx, x + w - 3, y + h - 3, CHIP_BORDER_GOLD, CHIP_BORDER_INNER);

        // 4. 内容区背景（玉简质感）
        gfx.fill(ix1, iy1, ix2, iy2, bg);
        // 左右卷边
        gfx.fill(ix1 + 1, iy1 + 1, ix1 + 3, iy2 - 1, edgeColor);
        gfx.fill(ix2 - 3, iy1 + 1, ix2 - 1, iy2 - 1, edgeColor);
        // 中央微亮带
        gfx.fill(ix1 + 4, iy1 + 2, ix2 - 4, iy2 - 2, open ? 0x55FFFFFF : 0x11FFFFFF);
        // 顶部高光
        gfx.fill(ix1, iy1, ix2, iy1 + 1, HIGHLIGHT);
        // 底部阴影
        gfx.fill(ix1, iy2 - 1, ix2, iy2, SHADOW);
        // 展开时左侧金色呼吸标线
        if (open) {
            gfx.fill(ix1, iy1 + 1, ix1 + 2, iy2 - 1, pulseColor(GOLD, 0.15f, 160, 255));
        }

        // 5. 文本 + 箭头整体视觉居中（问题2a）
        int arrowW = 5;
        int arrowGap = 3;
        int vw = this.chipTextWidth(currentValue);
        int totalW = vw + arrowW + arrowGap;
        int textX = ix1 + (iw - totalW) / 2;
        int textY = iy1 + (ih - 7) / 2;
        if (textX < ix1 + 2) textX = ix1 + 2;
        int textColor = open ? GOLD : (hover ? TEXT_LIGHT : TEXT_MUTED);
        this.drawScaled(gfx, currentValue, textX, textY, textColor, 0.72f);

        int arrowColor = open ? pulseColor(GOLD, 0.15f, 180, 255) : (hover ? TEXT_LIGHT : TEXT_MUTED);
        int arrowX = textX + vw + arrowGap;
        int arrowY = iy1 + (ih - 3) / 2;
        this.drawDropdownArrow(gfx, arrowX, arrowY, arrowColor);
    }

    private void renderOpenDropdown(GuiGraphics gfx, int mouseX, int mouseY) {
        List<Component> options;
        int selectedIdx;
        boolean isRealm = this.realmDropdownOpen;
        if (!isRealm && !this.subStageDropdownOpen) {
            return;
        }
        this.dropdownOptionRects.clear();

        if (isRealm) {
            options = new ArrayList<>();
            for (Realm r : this.realms) {
                options.add(r.displayName());
            }
            selectedIdx = this.realms.indexOf(this.selectedRealm);
        } else {
            List<Integer> levels = this.currentSubStageLevels(this.selectedRealm);
            options = new ArrayList<>();
            for (int level : levels) {
                options.add(this.subStageDisplayName(this.selectedRealm, level));
            }
            selectedIdx = levels.indexOf(this.selectedSubStageLevel);
        }

        int anchorX1 = isRealm ? this.realmChipX : this.subChipX;
        int anchorY1 = isRealm ? this.realmChipY : this.subChipY;
        int anchorX2 = anchorX1 + (isRealm ? this.realmChipW : this.subChipW);
        int anchorY2 = anchorY1 + (isRealm ? this.realmChipH : this.subChipH);

        int maxW = 0;
        for (Component opt : options) {
            maxW = Math.max(maxW, this.chipTextWidth(opt));
        }
        // 大境界 21 项分两列，避免下拉超高截断
        int columns = isRealm ? 2 : 1;
        int rows = (options.size() + columns - 1) / columns;
        int optionH = 13;
        int colW = Math.max(anchorX2 - anchorX1, maxW + 16) / columns + 4;
        int popupW = colW * columns + 4;
        int popupH = rows * optionH + 4;
        int popupX = anchorX1;
        int popupY = anchorY2 + 3;
        if (popupY + popupH > this.height - 4) {
            popupY = Math.max(this.panelTop + 4, anchorY1 - 3 - popupH);
        }

        gfx.pose().pushPose();
        gfx.pose().translate(0.0f, 0.0f, 300.0f);

        // 投影
        gfx.fill(popupX + 2, popupY + 2, popupX + popupW + 2, popupY + popupH + 2, SHADOW);
        // 边框与背景
        gfx.fill(popupX - 1, popupY - 1, popupX + popupW + 1, popupY + popupH + 1, INK_BLACK);
        gfx.fill(popupX, popupY, popupX + popupW, popupY + popupH, POPUP_BG);

        for (int i = 0; i < options.size(); ++i) {
            Component opt = options.get(i);
            int col = i / rows;
            int row = i % rows;
            int ox = popupX + 2 + col * colW;
            int oy = popupY + 2 + row * optionH;
            boolean hover = mouseX >= ox && mouseX < ox + colW && mouseY >= oy && mouseY < oy + optionH;
            boolean selected = i == selectedIdx;
            int bg = hover ? VERMILLION : (selected ? CHIP_HOVER_BG : POPUP_BG);
            gfx.fill(ox, oy, ox + colW, oy + optionH, bg);
            if (selected) {
                // 金色呼吸左标
                gfx.fill(ox + 1, oy, ox + 3, oy + optionH, pulseColor(GOLD, 0.18f, 150, 255));
            }
            int textColor = hover || selected ? TEXT_LIGHT : TEXT_MUTED;
            int tw = this.chipTextWidth(opt);
            this.drawScaled(gfx, opt, ox + (colW - tw) / 2, oy + (optionH - 7) / 2, textColor, 0.72f);

            this.dropdownOptionRects.add(new int[]{ox, oy, ox + colW, oy + optionH, i});

            // 选项间细线分隔
            if (i < options.size() - 1) {
                gfx.fill(ox + 6, oy + optionH - 1, ox + colW - 6, oy + optionH, INK_BLACK);
            }
        }
        gfx.pose().popPose();
    }

    /**
     * 普通按钮金色细描边（用于取消）。
     */
    private void drawButtonOutline(GuiGraphics gfx, Button btn, int pad, int color) {
        if (btn == null) return;
        int x = btn.getX() - pad;
        int y = btn.getY() - pad;
        int x2 = btn.getX() + btn.getWidth() + pad;
        int y2 = btn.getY() + btn.getHeight() + pad;
        gfx.fill(x, y, x2, y + 1, color);
        gfx.fill(x, y2 - 1, x2, y2, color);
        gfx.fill(x, y, x + 1, y2, color);
        gfx.fill(x2 - 1, y, x2, y2, color);
    }

    /**
     * 主按钮（确认）金色呼吸外框+四角铆钉，注意不再绘制实心覆盖。
     */
    private void drawPrimaryButtonFrame(GuiGraphics gfx, Button btn) {
        if (btn == null) return;
        int x = btn.getX();
        int y = btn.getY();
        int w = btn.getWidth();
        int h = btn.getHeight();
        int pad = 2;
        int gold = pulseColor(GOLD, 0.12f, 140, 240);

        // 金色框线（空心，不覆盖按钮本身）
        gfx.fill(x - pad, y - pad, x + w + pad, y - pad + 1, gold);
        gfx.fill(x - pad, y + h + pad - 1, x + w + pad, y + h + pad, gold);
        gfx.fill(x - pad, y - pad, x - pad + 1, y + h + pad, gold);
        gfx.fill(x + w + pad - 1, y - pad, x + w + pad, y + h + pad, gold);

        // 四角铆钉
        drawCornerStud(gfx, x - pad - 1, y - pad - 1, gold, CHIP_BORDER_INNER);
        drawCornerStud(gfx, x + w + pad - 2, y - pad - 1, gold, CHIP_BORDER_INNER);
        drawCornerStud(gfx, x - pad - 1, y + h + pad - 2, gold, CHIP_BORDER_INNER);
        drawCornerStud(gfx, x + w + pad - 2, y + h + pad - 2, gold, CHIP_BORDER_INNER);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partial) {
        this.renderBackground(gfx);
        this.animTime = (Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0) + partial;
        int cx = this.width / 2;
        int bottomY = this.panelTop + PANEL_H - 28;

        RenderSystem.enableBlend();
        gfx.blit(BG_TEXTURE, this.panelLeft, this.panelTop, 0.0f, 0.0f, PANEL_W, PANEL_H, PANEL_W, PANEL_H);

        // 面板底色蒙版
        gfx.fill(this.panelLeft + 4, this.panelTop + 4, this.panelLeft + PANEL_W - 4, this.panelTop + PANEL_H - 4, 0x3308080F);
        // 外框
        gfx.fill(this.panelLeft, this.panelTop, this.panelLeft + PANEL_W, this.panelTop + 1, BORDER_LIGHT);
        gfx.fill(this.panelLeft, this.panelTop + PANEL_H - 1, this.panelLeft + PANEL_W, this.panelTop + PANEL_H, BORDER_LIGHT);
        gfx.fill(this.panelLeft, this.panelTop, this.panelLeft + 1, this.panelTop + PANEL_H, BORDER_LIGHT);
        gfx.fill(this.panelLeft + PANEL_W - 1, this.panelTop, this.panelLeft + PANEL_W, this.panelTop + PANEL_H, BORDER_LIGHT);

        this.drawTitleHeader(gfx);
        this.drawPanelDecorations(gfx, cx, bottomY);

        // 标题：带投影的金色大字
        int titleW = (int) (this.font.width(this.title) * 0.85f);
        this.drawScaledShadowed(gfx, this.title, cx - titleW / 2, this.panelTop + 13, GOLD, 0.85f);

        // 提示（深墨色，问题1）
        Component hint = Component.translatable("screen.friday_cultivation.realm_selector.hint");
        int hintW = this.chipTextWidth(hint);
        this.drawScaled(gfx, hint, cx - hintW / 2, this.panelTop + 36, TEXT_HINT, 0.72f);

        Component realmLabel = Component.translatable("screen.friday_cultivation.realm_selector.realm", this.selectedRealm.displayName());
        this.drawDropdownChip(gfx, this.realmChipX, this.realmChipY, this.realmChipW, this.realmChipH, realmLabel, this.realmDropdownOpen, mouseX, mouseY);

        if (this.currentRealmHasSubStages()) {
            Component subLabel = Component.translatable("screen.friday_cultivation.realm_selector.substage", this.subStageDisplayName(this.selectedRealm, this.selectedSubStageLevel));
            this.drawDropdownChip(gfx, this.subChipX, this.subChipY, this.subChipW, this.subChipH, subLabel, this.subStageDropdownOpen, mouseX, mouseY);
        }

        this.renderOpenDropdown(gfx, mouseX, mouseY);

        // 在按钮绘制前先画外框（空心，不会遮住按钮）
        this.drawButtonOutline(gfx, this.cancelButton, 1, GOLD);
        this.drawPrimaryButtonFrame(gfx, this.confirmButton);

        super.render(gfx, mouseX, mouseY, partial);

        RenderSystem.disableBlend();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (this.realmDropdownOpen || this.subStageDropdownOpen) {
                for (int[] r : this.dropdownOptionRects) {
                    if (!(mouseX >= (double) r[0]) || !(mouseX < (double) r[2]) || !(mouseY >= (double) r[1]) || !(mouseY < (double) r[3])) {
                        continue;
                    }
                    int idx = r[4];
                    if (this.realmDropdownOpen) {
                        this.selectedRealm = this.realms.get(idx);
                        this.selectedSubStageLevel = this.firstSubStageLevel(this.selectedRealm);
                    } else {
                        List<Integer> levels = this.currentSubStageLevels(this.selectedRealm);
                        this.selectedSubStageLevel = levels.get(idx);
                    }
                    this.realmDropdownOpen = false;
                    this.subStageDropdownOpen = false;
                    return true;
                }
                this.realmDropdownOpen = false;
                this.subStageDropdownOpen = false;
                return true;
            }
            if (mouseX >= (double) this.realmChipX && mouseX < (double) (this.realmChipX + this.realmChipW)
                    && mouseY >= (double) this.realmChipY && mouseY < (double) (this.realmChipY + this.realmChipH)) {
                this.realmDropdownOpen = !this.realmDropdownOpen;
                this.subStageDropdownOpen = false;
                return true;
            }
            if (this.currentRealmHasSubStages()
                    && mouseX >= (double) this.subChipX && mouseX < (double) (this.subChipX + this.subChipW)
                    && mouseY >= (double) this.subChipY && mouseY < (double) (this.subChipY + this.subChipH)) {
                this.subStageDropdownOpen = !this.subStageDropdownOpen;
                this.realmDropdownOpen = false;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void confirm() {
        ModNetwork.CHANNEL.sendToServer(new RealmSelectionPacket(this.selectedRealm.id(), this.selectedSubStageLevel));
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
