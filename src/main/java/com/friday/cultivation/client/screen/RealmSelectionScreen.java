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
    private static final int CHIP_HOVER_BG = -11385542;
    private static final int POPUP_BG = -14739438;
    private static final int TEXT_LIGHT = -5720;
    private static final int TEXT_MUTED = -726312;
    private static final int TEXT_HINT = -3888992;
    private static final int SHADOW = 0x55000000;
    private static final int HIGHLIGHT = 0x33FFFFFF;

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

    public RealmSelectionScreen() {
        super(Component.translatable("screen.friday_cultivation.realm_selector.title"));
        List<Realm> list = new ArrayList<>();
        for (Realm r : Realm.values()) {
            list.add(r);
        }
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

        // 下拉器尺寸：略高、更舒展
        int chipY = this.panelTop + 64;
        int chipW = 136;
        int chipH = 18;
        this.realmChipX = cx - 140;
        this.realmChipY = chipY;
        this.realmChipW = chipW;
        this.realmChipH = chipH;
        this.subChipX = cx + 4;
        this.subChipY = chipY;
        this.subChipW = chipW;
        this.subChipH = chipH;

        int bottomY = this.panelTop + PANEL_H - 28;
        this.addRenderableWidget(new CinnabarButton(cx - 140, bottomY, 136, 18,
                Component.translatable("screen.friday_cultivation.realm_selector.confirm"),
                b -> this.confirm()));
        this.addRenderableWidget(new CinnabarButton(cx + 4, bottomY, 136, 18,
                Component.translatable("screen.friday_cultivation.realm_selector.cancel"),
                b -> Minecraft.getInstance().setScreen(null)));
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

    private void drawPanelDecorations(GuiGraphics gfx, int cx, int bottomY) {
        // 标题下方装饰线（金线+墨晕）
        int lineY = this.panelTop + 30;
        gfx.fill(this.panelLeft + 40, lineY, this.panelLeft + 280, lineY + 1, GOLD);
        gfx.fill(this.panelLeft + 40, lineY + 1, this.panelLeft + 280, lineY + 2, INK_SOFT);
        // 线端小印
        gfx.fill(this.panelLeft + 36, lineY - 1, this.panelLeft + 39, lineY + 2, GOLD);
        gfx.fill(this.panelLeft + 281, lineY - 1, this.panelLeft + 284, lineY + 2, GOLD);

        // 按钮上方 subtle 分隔
        int botLineY = bottomY - 10;
        gfx.fill(this.panelLeft + 56, botLineY, this.panelLeft + 264, botLineY + 1, INK_SOFT);

        // 四角回纹角标
        int offset = 6;
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

    private void drawDropdownChip(GuiGraphics gfx, int x, int y, int w, int h, Component currentValue, boolean open, int mouseX, int mouseY) {
        boolean hover = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        int bg = open ? VERMILLION : (hover ? CHIP_HOVER_BG : CHIP_BG);

        // 外框
        gfx.fill(x, y, x + w, y + h, INK_BLACK);
        // 底色
        gfx.fill(x + 1, y + 1, x + w - 1, y + h - 1, bg);
        // 顶部高光
        gfx.fill(x + 1, y + 1, x + w - 1, y + 2, HIGHLIGHT);
        // 底部阴影
        gfx.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, SHADOW);
        // 展开时左侧金线点缀
        if (open) {
            gfx.fill(x, y + 2, x + 2, y + h - 2, GOLD);
        }

        String arrow = " \u25be";
        int vw = this.chipTextWidth(currentValue);
        int aw = (int) Math.ceil(this.font.width(arrow) * 0.72f);
        int totalW = vw + aw;
        int textX = x + (w - totalW) / 2;
        int textY = y + (h - 7) / 2;
        int valueColor = open ? GOLD : (hover ? TEXT_LIGHT : TEXT_MUTED);
        this.drawScaled(gfx, currentValue, textX, textY, valueColor, 0.72f);
        this.drawScaled(gfx, Component.literal(arrow), textX + vw, textY, valueColor, 0.72f);
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
        int popupW = Math.max(anchorX2 - anchorX1, maxW + 16);
        int optionH = 13;
        int popupH = options.size() * optionH + 4; // 上下各 2px 内边距
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

        int oy = popupY + 2;
        for (int i = 0; i < options.size(); ++i) {
            Component opt = options.get(i);
            boolean hover = mouseX >= popupX && mouseX < popupX + popupW && mouseY >= oy && mouseY < oy + optionH;
            boolean selected = i == selectedIdx;
            int bg = hover ? VERMILLION : (selected ? CHIP_HOVER_BG : POPUP_BG);
            gfx.fill(popupX, oy, popupX + popupW, oy + optionH, bg);
            if (selected) {
                gfx.fill(popupX + 1, oy, popupX + 3, oy + optionH, GOLD);
            }
            int textColor = hover || selected ? TEXT_LIGHT : TEXT_MUTED;
            int tw = this.chipTextWidth(opt);
            this.drawScaled(gfx, opt, popupX + (popupW - tw) / 2, oy + (optionH - 7) / 2, textColor, 0.72f);

            this.dropdownOptionRects.add(new int[]{popupX, oy, popupX + popupW, oy + optionH, i});

            // 选项间细线分隔
            if (i < options.size() - 1) {
                gfx.fill(popupX + 6, oy + optionH - 1, popupX + popupW - 6, oy + optionH, INK_BLACK);
            }
            oy += optionH;
        }
        gfx.pose().popPose();
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partial) {
        this.renderBackground(gfx);
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

        // 标题区暗化背景
        gfx.fill(this.panelLeft + 8, this.panelTop + 8, this.panelLeft + PANEL_W - 8, this.panelTop + 32, 0x22000000);

        this.drawPanelDecorations(gfx, cx, bottomY);

        // 标题：居中、0.85 缩放、金色
        int titleW = (int) (this.font.width(this.title) * 0.85f);
        this.drawScaled(gfx, this.title, cx - titleW / 2, this.panelTop + 14, GOLD, 0.85f);

        // 提示：0.72 缩放、更清晰
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
        RenderSystem.disableBlend();

        super.render(gfx, mouseX, mouseY, partial);
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
