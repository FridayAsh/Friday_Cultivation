/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 */
package com.friday.cultivation.client.screen;

import com.friday.cultivation.client.screen.widget.CinnabarButton;
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

public class RealmSelectionScreen
extends Screen {
    private static final int PANEL_W = 320;
    private static final int PANEL_H = 200;
    private static final ResourceLocation BG_TEXTURE = new ResourceLocation((String)"friday_cultivation", (String)"textures/gui/cultivation_bg.png");
    private static final int INK_BLACK = -15067628;
    private static final int INK_SOFT = -13819625;
    private static final int INK_MUTE = -11979486;
    private static final int BORDER_LIGHT = -1516872;
    private static final int GOLD = -2047936;
    private static final int COFFEE = -9420002;
    private static final int JADE = -11093851;
    private final List<Realm> realms;
    private Realm selectedRealm;
    private int selectedSubStageLevel;
    private boolean realmDropdownOpen = false;
    private boolean subStageDropdownOpen = false;
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
    private final List<int[]> dropdownOptionRects = new ArrayList<int[]>();

    public RealmSelectionScreen() {
        super((Component)Component.translatable((String)"screen.friday_cultivation.realm_selector.title"));
        ArrayList<Realm> list = new ArrayList<Realm>();
        for (Realm r : Realm.values()) {
            list.add(r);
        }
        this.realms = list;
        this.selectedRealm = list.get(0);
        this.selectedSubStageLevel = this.firstSubStageLevel(this.selectedRealm);
    }

    private boolean currentRealmHasSubStages() {
        return !this.currentSubStageLevels(this.selectedRealm).isEmpty();
    }

    private int firstSubStageLevel(Realm realm) {
        List<Integer> levels = this.currentSubStageLevels(realm);
        return levels.isEmpty() ? 0 : levels.get(0);
    }

    private List<Integer> currentSubStageLevels(Realm realm) {
        ArrayList<Integer> list = new ArrayList<Integer>();
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
            return Component.translatable((String)("realm.friday_cultivation.loose_immortal.level." + level));
        }
        SubStage s = realm.subStageAt(level);
        return s != null ? s.displayName() : Component.literal((String)Integer.toString(level));
    }

    protected void init() {
        this.panelLeft = (this.width - 320) / 2;
        this.panelTop = (this.height - 200) / 2;
        int cx = this.width / 2;
        int chipY = this.panelTop + 56;
        int chipW = 136;
        int chipH = 16;
        this.realmChipX = cx - 140;
        this.realmChipY = chipY;
        this.realmChipW = chipW;
        this.realmChipH = chipH;
        this.subChipX = cx + 4;
        this.subChipY = chipY;
        this.subChipW = chipW;
        this.subChipH = chipH;
        int bottomY = this.panelTop + 200 - 28;
        this.addRenderableWidget(new CinnabarButton(cx - 140, bottomY, 136, 18, (Component)Component.translatable((String)"screen.friday_cultivation.realm_selector.confirm"), b -> this.confirm()));
        this.addRenderableWidget(new CinnabarButton(cx + 4, bottomY, 136, 18, (Component)Component.translatable((String)"screen.friday_cultivation.realm_selector.cancel"), b -> Minecraft.getInstance().setScreen(null)));
    }

    private int chipTextWidth(Component c) {
        return (int)Math.ceil((float)this.font.width((FormattedText)c) * 0.72f);
    }

    private void drawDropdownChip(GuiGraphics gfx, int x, int y, int w, int h, Component currentValue, boolean open, int mouseX, int mouseY) {
        boolean hover = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        int bg = open ? -4703686 : (hover ? -11385542 : -12635095);
        gfx.fill(x, y, x + w, y + h, -15067628);
        gfx.fill(x + 1, y + 1, x + w - 1, y + h - 1, bg);
        String arrow = " \u25be";
        int vw = this.chipTextWidth(currentValue);
        int aw = (int)Math.ceil((float)this.font.width(arrow) * 0.72f);
        int totalW = vw + aw;
        int textX = x + (w - totalW) / 2;
        int textY = y + (h - 7) / 2;
        int valueColor = open ? -5720 : -726312;
        this.drawScaled(gfx, currentValue, textX, textY, valueColor, 0.72f);
        this.drawScaled(gfx, (Component)Component.literal((String)arrow), textX + vw, textY, valueColor, 0.72f);
    }

    private void drawScaled(GuiGraphics gfx, Component text, int x, int y, int color, float scale) {
        gfx.pose().pushPose();
        gfx.pose().translate((float)x, (float)y, 0.0f);
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.drawString(this.font, text, 0, 0, color, false);
        gfx.pose().popPose();
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
            options = new ArrayList<Component>();
            for (Realm r : this.realms) {
                options.add(r.displayName());
            }
            selectedIdx = this.realms.indexOf(this.selectedRealm);
        } else {
            List<Integer> levels = this.currentSubStageLevels(this.selectedRealm);
            options = new ArrayList<Component>();
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
        int popupW = Math.max(anchorX2 - anchorX1, maxW + 12);
        int optionH = 13;
        int popupH = options.size() * optionH + 2;
        int popupX = anchorX1;
        int popupY = anchorY2 + 2;
        if (popupY + popupH > this.height - 4) {
            popupY = Math.max(this.panelTop + 4, anchorY1 - 2 - popupH);
        }
        gfx.pose().pushPose();
        gfx.pose().translate(0.0f, 0.0f, 300.0f);
        gfx.fill(popupX - 1, popupY - 1, popupX + popupW + 1, popupY + popupH + 1, -15067628);
        gfx.fill(popupX, popupY, popupX + popupW, popupY + popupH, -14739438);
        int oy = popupY + 1;
        int i = 0;
        while (i < options.size()) {
            Component opt = options.get(i);
            boolean hover = mouseX >= popupX && mouseX < popupX + popupW && mouseY >= oy && mouseY < oy + optionH;
            boolean selected = i == selectedIdx;
            int bg = hover ? -4703686 : (selected ? -11385542 : -14739438);
            gfx.fill(popupX, oy, popupX + popupW, oy + optionH, bg);
            int textColor = hover || selected ? -5720 : -726312;
            int tw = this.chipTextWidth(opt);
            this.drawScaled(gfx, opt, popupX + (popupW - tw) / 2, oy + (optionH - 7) / 2, textColor, 0.72f);
            this.dropdownOptionRects.add(new int[]{popupX, oy, popupX + popupW, oy + optionH, i++});
            oy += optionH;
        }
        gfx.pose().popPose();
    }

    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partial) {
        this.renderBackground(gfx);
        int cx = this.width / 2;
        RenderSystem.enableBlend();
        gfx.blit(BG_TEXTURE, this.panelLeft, this.panelTop, 0.0f, 0.0f, 320, 200, 320, 200);
        RenderSystem.disableBlend();
        gfx.fill(this.panelLeft + 4, this.panelTop + 4, this.panelLeft + 316, this.panelTop + 196, 587199439);
        gfx.fill(this.panelLeft, this.panelTop, this.panelLeft + 320, this.panelTop + 1, -1516872);
        gfx.fill(this.panelLeft, this.panelTop + 199, this.panelLeft + 320, this.panelTop + 200, -1516872);
        gfx.fill(this.panelLeft, this.panelTop, this.panelLeft + 1, this.panelTop + 200, -1516872);
        gfx.fill(this.panelLeft + 319, this.panelTop, this.panelLeft + 320, this.panelTop + 200, -1516872);
        gfx.drawCenteredString(this.font, this.title, cx, this.panelTop + 12, -15067628);
        gfx.drawCenteredString(this.font, (Component)Component.translatable((String)"screen.friday_cultivation.realm_selector.hint"), cx, this.panelTop + 30, -11979486);
        this.drawDropdownChip(gfx, this.realmChipX, this.realmChipY, this.realmChipW, this.realmChipH, (Component)Component.translatable((String)"screen.friday_cultivation.realm_selector.realm", (Object[])new Object[]{this.selectedRealm.displayName()}), this.realmDropdownOpen, mouseX, mouseY);
        if (this.currentRealmHasSubStages()) {
            this.drawDropdownChip(gfx, this.subChipX, this.subChipY, this.subChipW, this.subChipH, (Component)Component.translatable((String)"screen.friday_cultivation.realm_selector.substage", (Object[])new Object[]{this.subStageDisplayName(this.selectedRealm, this.selectedSubStageLevel)}), this.subStageDropdownOpen, mouseX, mouseY);
        } else {
            gfx.fill(this.subChipX, this.subChipY, this.subChipX + this.subChipW, this.subChipY + this.subChipH, -15067628);
            gfx.fill(this.subChipX + 1, this.subChipY + 1, this.subChipX + this.subChipW - 1, this.subChipY + this.subChipH - 1, -12635095);
            this.drawScaled(gfx, (Component)Component.translatable((String)"screen.friday_cultivation.realm_selector.no_substage"), this.subChipX + (this.subChipW - this.chipTextWidth((Component)Component.translatable((String)"screen.friday_cultivation.realm_selector.no_substage"))) / 2, this.subChipY + (this.subChipH - 7) / 2, -3888992, 0.72f);
        }
        this.renderOpenDropdown(gfx, mouseX, mouseY);
        super.render(gfx, mouseX, mouseY, partial);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (this.realmDropdownOpen || this.subStageDropdownOpen) {
                for (int[] r : this.dropdownOptionRects) {
                    if (!(mouseX >= (double)r[0]) || !(mouseX < (double)r[2]) || !(mouseY >= (double)r[1]) || !(mouseY < (double)r[3])) continue;
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
            if (mouseX >= (double)this.realmChipX && mouseX < (double)(this.realmChipX + this.realmChipW) && mouseY >= (double)this.realmChipY && mouseY < (double)(this.realmChipY + this.realmChipH)) {
                this.realmDropdownOpen = !this.realmDropdownOpen;
                this.subStageDropdownOpen = false;
                return true;
            }
            if (this.currentRealmHasSubStages() && mouseX >= (double)this.subChipX && mouseX < (double)(this.subChipX + this.subChipW) && mouseY >= (double)this.subChipY && mouseY < (double)(this.subChipY + this.subChipH)) {
                this.subStageDropdownOpen = !this.subStageDropdownOpen;
                this.realmDropdownOpen = false;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void confirm() {
        ModNetwork.CHANNEL.sendToServer((Object)new RealmSelectionPacket(this.selectedRealm.id(), this.selectedSubStageLevel));
        Minecraft.getInstance().setScreen(null);
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    public boolean isPauseScreen() {
        return false;
    }
}