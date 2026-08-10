package com.friday.cultivation.client.screen;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.alchemy.AlchemyRank;
import com.friday.cultivation.network.EditPlayerStatsPacket;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.realm.Realm;
import com.friday.cultivation.realm.SubStage;
import com.friday.cultivation.refining.RefiningRank;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * 数值编辑器界面 — 完全照搬原模组 com.xiaoxiang.cultivation.client.screen.StatEditorScreen。
 * 编辑 6 项属性 + 境界 + 副阶 + 炼器 + 炼丹 + 骨龄，通过 EditPlayerStatsPacket 发送给服务端。
 */
public class StatEditorScreen extends Screen {
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
        super(Component.translatable("screen.friday_cultivation.stat_editor.title"));
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

    @Override
    protected void init() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            CultivationCapability.get(mc.player).ifPresent(d -> {
                this.intVals[0] = d.getAttrConstitution();
                this.intVals[1] = d.getAttrPhysique();
                this.intVals[2] = d.getAttrAgility();
                this.intVals[3] = d.getAttrSpellPower();
                this.intVals[4] = d.getAttrQiSea();
                this.intVals[5] = d.getDefense();
                this.enumIdx[0] = clamp(d.getRealm().ordinal(), 0, enumMax(0));
                this.enumIdx[1] = clamp(d.getSubStage().ordinal(), 0, enumMax(1));
                this.enumIdx[2] = clamp(d.getRefining(), 0, enumMax(2));
                this.enumIdx[3] = clamp(d.getAlchemy(), 0, enumMax(3));
                this.boneAgeYears = clamp((int) Math.floor(d.getBoneAge()), 0, MAX_BONE_AGE_YEARS);
            });
        }
        this.left = (this.width - PANEL_W) / 2;
        this.top = (this.height - PANEL_H) / 2;
        int cx = this.left + 118;
        int bw = 18;
        int bh = 14;
        for (int i = 0; i < 6; ++i) {
            int ry = this.top + ROW0_DY + i * ROW_H;
            final int idx = i;
            this.addRenderableWidget(Button.builder(Component.literal("-10"), b -> this.addInt(idx, -10)).bounds(cx, ry, bw, bh).build());
            this.addRenderableWidget(Button.builder(Component.literal("-1"), b -> this.addInt(idx, -1)).bounds(cx + 20, ry, bw, bh).build());
            this.addRenderableWidget(Button.builder(Component.literal("+1"), b -> this.addInt(idx, 1)).bounds(cx + 76, ry, bw, bh).build());
            this.addRenderableWidget(Button.builder(Component.literal("+10"), b -> this.addInt(idx, 10)).bounds(cx + 96, ry, bw, bh).build());
        }
        for (int j = 0; j < 4; ++j) {
            int ry = this.top + ROW0_DY + (6 + j) * ROW_H;
            final int idx = j;
            this.addRenderableWidget(Button.builder(Component.literal("<"), b -> this.addEnum(idx, -1)).bounds(cx, ry, bw, bh).build());
            this.addRenderableWidget(Button.builder(Component.literal(">"), b -> this.addEnum(idx, 1)).bounds(cx + 96, ry, bw, bh).build());
        }
        int boneY = this.top + ROW0_DY + 160;
        this.boneAgeBox = new EditBox(this.font, cx, boneY, 114, bh, Component.translatable("screen.friday_cultivation.stat_editor.bone_age"));
        this.boneAgeBox.setMaxLength(7);
        this.boneAgeBox.setFilter(value -> value.isEmpty() || value.matches("\\d{0,7}"));
        this.boneAgeBox.setValue(String.valueOf(this.boneAgeYears));
        this.addRenderableWidget(this.boneAgeBox);
        int by = this.top + PANEL_H - 22;
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
        this.enumIdx[idx] = clamp(this.enumIdx[idx] + delta, 0, enumMax(idx));
    }

    private void apply() {
        this.boneAgeYears = this.parseBoneAge();
        ModNetwork.CHANNEL.sendToServer(new EditPlayerStatsPacket(this.intVals[0], this.intVals[1], this.intVals[2], this.intVals[3], this.intVals[4], this.intVals[5], this.enumIdx[0], this.enumIdx[1], this.enumIdx[2], this.enumIdx[3], this.boneAgeYears));
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
            return clamp(Integer.parseInt(text), 0, MAX_BONE_AGE_YEARS);
        }
        catch (NumberFormatException ignored) {
            return this.boneAgeYears;
        }
    }

    @Override
    public void onClose() {
        if (this.parent != null) {
            Minecraft.getInstance().setScreen(this.parent);
        } else {
            super.onClose();
        }
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);
        g.fill(this.left - 2, this.top - 2, this.left + PANEL_W + 2, this.top + PANEL_H + 2, -3562934);
        g.fill(this.left, this.top, this.left + PANEL_W, this.top + PANEL_H, -233171954);
        g.drawCenteredString(this.font, this.title, this.left + 134, this.top + 8, TITLE_COLOR);
        int cx = this.left + 118;
        int valCenter = cx + 57;
        for (int i = 0; i < 6; ++i) {
            int ry = this.top + ROW0_DY + i * ROW_H;
            g.drawString(this.font, Component.translatable(INT_LABEL_KEYS[i]), this.left + 12, ry + 3, LABEL_COLOR, false);
            g.drawCenteredString(this.font, String.valueOf(this.intVals[i]), valCenter, ry + 3, VALUE_COLOR);
        }
        for (int j = 0; j < 4; ++j) {
            int ry = this.top + ROW0_DY + (6 + j) * ROW_H;
            g.drawString(this.font, Component.translatable(ENUM_LABEL_KEYS[j]), this.left + 12, ry + 3, LABEL_COLOR, false);
            g.drawCenteredString(this.font, this.enumValue(j), valCenter, ry + 3, VALUE_COLOR);
        }
        int boneY = this.top + ROW0_DY + 160;
        g.drawString(this.font, Component.translatable("screen.friday_cultivation.stat_editor.bone_age"), this.left + 12, boneY + 3, LABEL_COLOR, false);
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
