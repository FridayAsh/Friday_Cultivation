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
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.entity.player.Player
 *  org.jetbrains.annotations.NotNull
 *  org.lwjgl.glfw.GLFW
 */
package com.friday.cultivation.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.friday.cultivation.client.ClientKeybindings;
import com.friday.cultivation.client.screen.CultivationScreen;
import com.friday.cultivation.client.screen.SpellIconRenderHelper;
import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.cultivation.spell.SpellType;
import com.friday.cultivation.cultivation.spell.SpellWheelLayout;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.SelectSpellSlotPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public class SpellWheelScreen
extends Screen {
    private static final int SLOT_SIZE = 18;
    private static final int CELL_SPACING = 30;
    private static final int ICON_SIZE = 14;
    private static final double DEAD_ZONE = 10.0;
    private final int[][] slotCenters = new int[8][2];
    private int hoveredSlot = -1;
    private boolean committed = false;

    public SpellWheelScreen() {
        super((Component)Component.translatable((String)"screen.friday_cultivation.spell_wheel.title"));
    }

    protected void init() {
        super.init();
        int cx = this.width / 2;
        int cy = this.height / 2;
        for (int i = 0; i < 8; ++i) {
            this.slotCenters[i][0] = cx + SpellWheelLayout.offsetX(i) * 30;
            this.slotCenters[i][1] = cy + SpellWheelLayout.offsetY(i) * 30;
        }
        Minecraft mc = Minecraft.getInstance();
        double physicalCx = (double)mc.getWindow().getScreenWidth() / 2.0;
        double physicalCy = (double)mc.getWindow().getScreenHeight() / 2.0;
        GLFW.glfwSetCursorPos((long)mc.getWindow().getWindow(), (double)physicalCx, (double)physicalCy);
    }

    public void tick() {
        super.tick();
        if (!SpellWheelScreen.isWheelKeyHeld()) {
            this.commitAndClose();
        }
    }

    private static boolean isWheelKeyHeld() {
        InputConstants.Key key = ClientKeybindings.OPEN_SPELL_WHEEL.getKey();
        long window = Minecraft.getInstance().getWindow().getWindow();
        int code = key.getValue();
        if (code < 0) {
            return false;
        }
        return switch (key.getType()) {
            case KEYSYM -> {
                if (GLFW.glfwGetKey((long)window, (int)code) == 1) {
                    yield true;
                }
                yield false;
            }
            case MOUSE -> {
                if (GLFW.glfwGetMouseButton((long)window, (int)code) == 1) {
                    yield true;
                }
                yield false;
            }
            default -> false;
        };
    }

    private void commitAndClose() {
        if (!this.committed) {
            this.committed = true;
            if (this.hoveredSlot >= 0 && this.hoveredSlot < 8) {
                ModNetwork.CHANNEL.sendToServer((Object)new SelectSpellSlotPacket(this.hoveredSlot));
            }
        }
        this.onClose();
    }

    public void renderBackground(@NotNull GuiGraphics gfx) {
        gfx.fill(0, 0, this.width, this.height, 0x50000000);
    }

    public void addEntry(@NotNull GuiGraphics gfx, int mouseX, int mouseY, float partial) {
        this.renderBackground(gfx);
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        if (data == null) {
            return;
        }
        int cx = this.width / 2;
        int cy = this.height / 2;
        double dx = mouseX - cx;
        double dy = mouseY - cy;
        this.hoveredSlot = SpellWheelLayout.closestSlot(dx, dy, 10.0);
        String[] equipped = data.getEquippedSpells();
        this.drawCenterOrb(gfx, cx, cy);
        if (this.hoveredSlot >= 0) {
            this.drawPointerLine(gfx, cx, cy, this.slotCenters[this.hoveredSlot][0], this.slotCenters[this.hoveredSlot][1]);
        }
        RenderSystem.enableBlend();
        for (int i = 0; i < 8; ++i) {
            int sx = this.slotCenters[i][0] - 9;
            int sy = this.slotCenters[i][1] - 9;
            String sid = equipped[i];
            Spell sp = sid == null || sid.isEmpty() ? null : Spell.byId(sid);
            boolean hovered = i == this.hoveredSlot;
            boolean primed = i == data.getSelectedSpellSlot();
            this.drawSlot(gfx, sx, sy, sp, data, hovered, primed);
        }
        RenderSystem.disableBlend();
        if (this.hoveredSlot >= 0) {
            String sid = equipped[this.hoveredSlot];
            Spell sp = sid == null || sid.isEmpty() ? null : Spell.byId(sid);
            MutableComponent label = sp != null ? sp.displayNameForRealm(data.getRealm()).copy().withStyle(new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD}) : Component.translatable((String)"screen.friday_cultivation.spell_wheel.empty_slot").withStyle(ChatFormatting.GRAY);
            int w = this.font.width((FormattedText)label);
            gfx.drawString(this.font, (Component)label, cx - w / 2, cy + 45 + 12, -1, true);
        }
        MutableComponent hint = Component.translatable((String)"screen.friday_cultivation.spell_wheel.hint");
        gfx.drawCenteredString(this.font, (Component)hint, cx, this.height - 28, -4144960);
        super.render(gfx, mouseX, mouseY, partial);
    }

    private void drawCenterOrb(GuiGraphics gfx, int cx, int cy) {
        gfx.fill(cx - 6, cy - 6, cx + 6, cy + 6, -792090518);
        gfx.fill(cx - 5, cy - 5, cx + 5, cy + 5, -520820008);
        RenderSystem.enableBlend();
        gfx.blit(CultivationScreen.TAIJI_TEXTURE, cx - 5, cy - 5, 10, 10, 0.0f, 0.0f, 32, 32, 32, 32);
        RenderSystem.disableBlend();
    }

    private void drawPointerLine(GuiGraphics gfx, int cx, int cy, int tx, int ty) {
        double fdx = tx - cx;
        double fdy = ty - cy;
        double len = Math.sqrt(fdx * fdx + fdy * fdy);
        if (len < 3.0) {
            return;
        }
        int steps = (int)Math.min(len, 50.0);
        for (int i = 5; i < steps; ++i) {
            double t = (double)i / (double)steps;
            int px = (int)((double)cx + fdx * t);
            int py = (int)((double)cy + fdy * t);
            gfx.fill(px, py, px + 1, py + 1, -4703686);
        }
        int ax = (int)((double)cx + fdx * 0.8);
        int ay = (int)((double)cy + fdy * 0.8);
        gfx.fill(ax - 1, ay - 1, ax + 2, ay + 2, -10496);
    }

    private void drawSlot(GuiGraphics gfx, int sx, int sy, Spell sp, CultivationData data, boolean hovered, boolean primed) {
        int border = hovered ? -10496 : (primed ? -4703686 : -535161324);
        gfx.fill(sx - 2, sy - 2, sx + 18 + 2, sy + 18 + 2, border);
        int bg = hovered ? -252384552 : -789255464;
        gfx.fill(sx, sy, sx + 18, sy + 18, bg);
        gfx.fill(sx, sy, sx + 18, sy + 2, -2130716928);
        if (sp != null) {
            int iconX = sx + 2;
            int iconY = sy + 2;
            RenderSystem.enableBlend();
            boolean disabledPassive = sp.type() == SpellType.PASSIVE && !data.isSpellEnabled(sp);
            SpellIconRenderHelper.blitSpellIcon(gfx, sp, iconX, iconY, 14, disabledPassive);
            RenderSystem.disableBlend();
        }
    }

    public boolean calculateIngredientsPositions(double mouseX, double mouseY, int button) {
        if (button == 0 && this.hoveredSlot >= 0) {
            this.commitAndClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.committed = true;
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean isPauseScreen() {
        return false;
    }
}

