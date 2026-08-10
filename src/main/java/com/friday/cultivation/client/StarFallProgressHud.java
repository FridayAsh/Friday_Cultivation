/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.client.gui.overlay.IGuiOverlay
 */
package com.friday.cultivation.client;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.spell.Spell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public final class StarFallProgressHud {
    public static final IGuiOverlay OVERLAY = (gui, graphics, partialTick, screenWidth, screenHeight) -> StarFallProgressHud.render(graphics, screenWidth, screenHeight);
    private static final long STAR_FALL_THRESHOLD = 10000L;
    private static final long SKY_SPLITTING_THRESHOLD = 3000L;
    private static final long TIME_STASIS_THRESHOLD = 10000L;
    private static final long BUDDHA_FIRE_LOTUS_THRESHOLD = 10000L;
    private static final long CORE_SELF_DESTRUCT_THRESHOLD = 1000L;
    private static final long PALM_THUNDER_THRESHOLD_TICKS = 40L;
    private static final long HEAVEN_PIERCING_CONE_THRESHOLD_TICKS = 100L;
    private static final long VOID_ESCAPE_THRESHOLD_TICKS = 100L;
    private static final int BAR_W = 120;
    private static final int BAR_H = 9;
    private static final int BAR_BOTTOM_OFFSET = 60;
    private static final int INK_BLACK = -15067628;
    private static final int BG_TRACK = -869653472;
    private static final int CINNABAR = -4703686;
    private static final int CINNABAR_DEEP = -7723482;
    private static final int GOLD_BRIGHT = -10496;
    private static final int TEXT_LIGHT = -726312;

    private StarFallProgressHud() {
    }

    private static void render(GuiGraphics gfx, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui) {
            return;
        }
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        if (data == null) {
            return;
        }
        if (!data.isCharging()) {
            return;
        }
        Spell sp = Spell.byId(data.getChargingSpellId());
        if (sp != Spell.STAR_FALL && sp != Spell.SKY_SPLITTING_SWORD_AURA && sp != Spell.TIME_STASIS && sp != Spell.HEAVEN_PIERCING_CONE && sp != Spell.BUDDHA_FIRE_LOTUS && sp != Spell.CORE_SELF_DESTRUCT && sp != Spell.PALM_THUNDER && sp != Spell.VOID_ESCAPE) {
            return;
        }
        long threshold = switch (sp) {
            case SKY_SPLITTING_SWORD_AURA -> 3000L;
            case TIME_STASIS -> 10000L;
            case HEAVEN_PIERCING_CONE -> 100L;
            case BUDDHA_FIRE_LOTUS -> 10000L;
            case CORE_SELF_DESTRUCT -> 1000L;
            case PALM_THUNDER -> 40L;
            case VOID_ESCAPE -> 100L;
            default -> 10000L;
        };
        long chargedQi = sp == Spell.HEAVEN_PIERCING_CONE || sp == Spell.VOID_ESCAPE ? (long)data.getChargingTicks() : data.getChargedQi();
        float rawRatio = (float)chargedQi / (float)threshold;
        float progress = Math.min(1.0f, rawRatio);
        String labelKey = sp == Spell.SKY_SPLITTING_SWORD_AURA ? "hud.friday_cultivation.sky_splitting_sword_aura.charging" : (sp == Spell.TIME_STASIS ? "hud.friday_cultivation.time_stasis.charging" : (sp == Spell.HEAVEN_PIERCING_CONE ? "hud.friday_cultivation.heaven_piercing_cone.charging" : (sp == Spell.BUDDHA_FIRE_LOTUS ? "hud.friday_cultivation.buddha_fire_lotus.charging" : (sp == Spell.CORE_SELF_DESTRUCT ? "hud.friday_cultivation.core_self_destruct.charging" : (sp == Spell.PALM_THUNDER ? "hud.friday_cultivation.palm_thunder.charging" : (sp == Spell.VOID_ESCAPE ? "hud.friday_cultivation.void_escape.charging" : "hud.friday_cultivation.star_fall.charging"))))));
        int barX = (screenWidth - 120) / 2;
        int barY = screenHeight - 60;
        gfx.fill(barX - 1, barY - 1, barX + 120 + 1, barY + 9 + 1, -15067628);
        gfx.fill(barX, barY, barX + 120, barY + 9, -869653472);
        int filled = (int)(120.0f * progress);
        if (filled > 0) {
            gfx.fill(barX, barY, barX + filled, barY + 9, -4703686);
            gfx.fill(barX, barY, barX + filled, barY + 1, -10496);
            gfx.fill(barX, barY + 9 - 1, barX + filled, barY + 9, -7723482);
            if (progress >= 0.9f) {
                int pulseAlpha = (int)(180.0 + Math.sin((double)player.tickCount * 0.5) * 50.0);
                pulseAlpha = Math.max(80, Math.min(255, pulseAlpha));
                int pulseColor = pulseAlpha << 24 | 0xFFD700;
                gfx.fill(barX, barY, barX + filled, barY + 9, pulseColor);
            }
        }
        MutableComponent label = Component.translatable((String)labelKey);
        gfx.drawString(mc.font, (Component)label, barX, barY - 11, -726312, true);
        boolean uncapped = sp == Spell.SKY_SPLITTING_SWORD_AURA || sp == Spell.HEAVEN_PIERCING_CONE || sp == Spell.BUDDHA_FIRE_LOTUS;
        int displayPct = (int)((uncapped ? rawRatio : progress) * 100.0f);
        int pctColor = displayPct > 100 ? -10496 : -726312;
        String pctText = displayPct + "%";
        int pctW = mc.font.width(pctText);
        gfx.drawString(mc.font, pctText, barX + 120 - pctW, barY - 11, pctColor, true);
    }
}

