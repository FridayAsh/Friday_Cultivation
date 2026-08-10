/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 */
package com.friday.cultivation.cultivation;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.FoundationDao;
import com.friday.cultivation.cultivation.spell.Spell;
import net.minecraft.world.entity.player.Player;

public final class FoundationDaoBonusHelper {
    private FoundationDaoBonusHelper() {
    }

    private static FoundationDao daoOf(Player player) {
        if (player == null) {
            return FoundationDao.NONE;
        }
        CultivationData d = CultivationCapability.get(player).orElse(null);
        return d == null ? FoundationDao.NONE : d.getFoundationDao();
    }

    public static double spellDamageMultiplier(Player player, Spell spell) {
        FoundationDao dao = FoundationDaoBonusHelper.daoOf(player);
        double mult = dao.spellDamageMult();
        if (dao.bloodMastery() && spell != null && spell.isBloodSpell()) {
            mult *= 1.5;
        }
        return mult;
    }

    public static double spellQiCostMultiplier(Player player, Spell spell) {
        FoundationDao dao = FoundationDaoBonusHelper.daoOf(player);
        double mult = dao.spellQiCostMult();
        if (dao.bloodMastery() && spell != null && spell.isBloodSpell()) {
            mult *= 0.5;
        }
        return mult;
    }

    public static double maxHpMultiplyTotal(Player player) {
        return FoundationDaoBonusHelper.daoOf(player).hpMult() - 1.0;
    }

    public static int bodyDefenseBonus(Player player) {
        return FoundationDaoBonusHelper.daoOf(player).bodyDefenseBonus();
    }

    public static int cultivationEfficiencyBonus(Player player) {
        return FoundationDaoBonusHelper.daoOf(player).cultivationEfficiencyBonus();
    }

    public static int qiRecoveryPerSecondBonus(Player player) {
        return FoundationDaoBonusHelper.daoOf(player).qiRecoveryPerSecondBonus();
    }

    public static int meleeDamageBonus(Player player) {
        return FoundationDaoBonusHelper.daoOf(player).meleeDamageBonus();
    }
}

