/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Player
 *  org.jetbrains.annotations.NotNull
 */
package com.friday.cultivation.client.screen;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.alchemy.AlchemyRank;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.cultivation.realm.SubStage;
import com.friday.cultivation.cultivation.refining.RefiningRank;
import com.friday.cultivation.network.EditPlayerStatsPacket;
import com.friday.cultivation.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class StatEditorScreen
extends Screen {
    private static final int PANEL_W = 268;
    private static final int PANEL_H = 234;
    private static final int ROW0_DY = 24;
    private static final int ROW_H = 16;
    private static final int MAX_BONE_AGE_YEARS = 1000000;
    private static final int LABEL_COLOR = -1516872;
    private static final int VALUE_COLOR = -1;
    private static final int TITLE_COLOR = -1456016;
    private static final String[] INT_LABEL_KEYS = new String[]{"screen.friday_cultivation.stat_editor.attr_constitution", "screen.friday_cultivation.stat_editor.attr_physique", "screen.friday_cultivation.stat_editor.attr_agility", "screen.friday_cultivation.stat_editor.attr_spell_power", "screen.friday_cultivation.stat_editor.attr_qi_sea", "screen.friday_cultivation.stat_editor.body_defense"};
    private static final String[] ENUM_LABEL_KEYS = new String[]{"screen.friday_cultivation.stat_editor.realm", "screen.friday_cultivation.stat_editor.substage", "screen.friday_cultivation.stat_editor.refining", "screen.friday_cultivation.stat_editor.alchemy"};
    private final Screen parent;
    private final int[] intVals = new int[6];
    private final int[] enumIdx = new int[4];
    private int boneAgeYears;
    private EditBox boneAgeBox;
    private int left;
    private int top;

    public StatEditorScreen(Screen parent) {
        super((Component)Component.translatable((String)"screen.friday_cultivation.stat_editor.title"));
        this.parent = parent;
    }

    public StatEditorScreen() {
        this(null);
    }

    private static int enumMax(int j) {
        return switch (j) {
            case 0 -> Realm.values().length - 1;
            case 1 -> SubStage.values().length - 1;
            case 2 -> RefiningRank.values().length - 1;
            default -> AlchemyRank.values().length - 1;
        };
    }

    private Component enumValue(int j) {
        return switch (j) {
            case 0 -> Realm.values()[this.enumIdx[0]].displayName();
            case 1 -> SubStage.values()[this.enumIdx[1]].displayName();
            case 2 -> RefiningRank.values()[this.enumIdx[2]].displayName();
            default -> AlchemyRank.values()[this.enumIdx[3]].displayName();
        };
    }

    protected void init() {
        int idx;
        int ry;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            CultivationCapability.get((Player)mc.player).ifPresent(d -> {
                this.intVals[0] = d.getAttrConstitution();
                this.intVals[1] = d.getAttrPhysique();
                this.intVals[2] = d.getAttrAgility();
                this.intVals[3] = d.getAttrSpellPower();
                this.intVals[4] = d.getAttrQiSea();
                this.intVals[5] = d.getDefense();
                this.enumIdx[0] = StatEditorScreen.clamp(d.getRealm().ordinal(), 0, StatEditorScreen.enumMax(0));
                this.enumIdx[1] = StatEditorScreen.clamp(d.getSubStage().ordinal(), 0, StatEditorScreen.enumMax(1));
                this.enumIdx[2] = StatEditorScreen.clamp(d.getRefining(), 0, StatEditorScreen.enumMax(2));
                this.enumIdx[3] = StatEditorScreen.clamp(d.getAlchemy(), 0, StatEditorScreen.enumMax(3));
                this.boneAgeYears = StatEditorScreen.clamp((int)Math.floor(d.getBoneAge()), 0, 1000000);
            });
        }
        this.left = (this.width - 268) / 2;
        this.top = (this.height - 234) / 2;
        int cx = this.left + 118;
        int bw = 18;
        int bh = 14;
        int i = 0;
        while (i < 6) {
            ry = this.top + 24 + i * 16;
            idx = i++;
            final int idxInt = idx;
            this.addRenderableWidget(Button.builder(Component.literal("-10"), b -> this.addInt(idxInt, -10)).bounds(cx, ry, bw, bh).build());
            this.addRenderableWidget(Button.builder(Component.literal("-1"), b -> this.addInt(idxInt, -1)).bounds(cx + 20, ry, bw, bh).build());
            this.addRenderableWidget(Button.builder(Component.literal("+1"), b -> this.addInt(idxInt, 1)).bounds(cx + 76, ry, bw, bh).build());
            this.addRenderableWidget(Button.builder(Component.literal("+10"), b -> this.addInt(idxInt, 10)).bounds(cx + 96, ry, bw, bh).build());
        }
        int j = 0;
        while (j < 4) {
            ry = this.top + 24 + (6 + j) * 16;
            idx = j++;
            final int idxEnum = idx;
            this.addRenderableWidget(Button.builder(Component.literal("<"), b -> this.addEnum(idxEnum, -1)).bounds(cx, ry, bw, bh).build());
            this.addRenderableWidget(Button.builder(Component.literal(">"), b -> this.addEnum(idxEnum, 1)).bounds(cx + 96, ry, bw, bh).build());
        }
        int boneY = this.top + 24 + 160;
        this.boneAgeBox = new EditBox(this.font, cx, boneY, 114, bh, (Component)Component.translatable((String)"screen.friday_cultivation.stat_editor.bone_age"));
        this.boneAgeBox.setMaxLength(7);
        this.boneAgeBox.setFilter(value -> value.isEmpty() || value.matches("\\d{0,7}"));
        this.boneAgeBox.setValue(String.valueOf(this.boneAgeYears));
        this.addRenderableWidget(this.boneAgeBox);
        int by = this.top + 234 - 22;
        int halfW = 120;
        this.addRenderableWidget(Button.builder(Component.translatable("screen.friday_cultivation.stat_editor.apply"), b -> this.apply()).bounds(this.left + 10, by, halfW, 18).build());
        this.addRenderableWidget(Button.builder(Component.translatable("screen.friday_cultivation.stat_editor.close"), b -> this.onClose()).bounds(this.left + 18 + halfW, by, halfW, 18).build());
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private void addInt(int idx, int delta) {
        this.intVals[idx] = Math.max(0, this.intVals[idx] + delta);
    }

    private void addEnum(int idx, int delta) {
        this.enumIdx[idx] = StatEditorScreen.clamp(this.enumIdx[idx] + delta, 0, StatEditorScreen.enumMax(idx));
    }

    private void apply() {
        this.boneAgeYears = this.parseBoneAge();
        ModNetwork.CHANNEL.sendToServer((Object)new EditPlayerStatsPacket(this.intVals[0], this.intVals[1], this.intVals[2], this.intVals[3], this.intVals[4], this.intVals[5], this.enumIdx[0], this.enumIdx[1], this.enumIdx[2], this.enumIdx[3], this.boneAgeYears));
        this.onClose();
    }

    private int parseBoneAge() {
        if (this.boneAgeBox == null) {
            return this.boneAgeYears;
        }
        String text = this.boneAgeBox.getValue().trim();
        if (text.isEmpty()) {
            return 0;
        }
        try {
            return StatEditorScreen.clamp(Integer.parseInt(text), 0, 1000000);
        }
        catch (NumberFormatException ignored) {
            return this.boneAgeYears;
        }
    }

    public void onClose() {
        if (this.parent != null) {
            Minecraft.getInstance().setScreen(this.parent);
        } else {
            super.onClose();
        }
    }

    public void addEntry(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int ry;
        this.renderBackground(g);
        g.fill(this.left - 2, this.top - 2, this.left + 268 + 2, this.top + 234 + 2, -3562934);
        g.fill(this.left, this.top, this.left + 268, this.top + 234, -233171954);
        g.drawCenteredString(this.font, this.title, this.left + 134, this.top + 8, -1456016);
        int cx = this.left + 118;
        int valCenter = cx + 57;
        for (int i = 0; i < 6; ++i) {
            ry = this.top + 24 + i * 16;
            g.drawString(this.font, (Component)Component.translatable((String)INT_LABEL_KEYS[i]), this.left + 12, ry + 3, -1516872, false);
            g.drawCenteredString(this.font, String.valueOf(this.intVals[i]), valCenter, ry + 3, -1);
        }
        for (int j = 0; j < 4; ++j) {
            ry = this.top + 24 + (6 + j) * 16;
            g.drawString(this.font, (Component)Component.translatable((String)ENUM_LABEL_KEYS[j]), this.left + 12, ry + 3, -1516872, false);
            g.drawCenteredString(this.font, this.enumValue(j), valCenter, ry + 3, -1);
        }
        int boneY = this.top + 24 + 160;
        g.drawString(this.font, (Component)Component.translatable((String)"screen.friday_cultivation.stat_editor.bone_age"), this.left + 12, boneY + 3, -1516872, false);
        super.render(g, mouseX, mouseY, partialTick);
    }

    public boolean isPauseScreen() {
        return false;
    }
}

