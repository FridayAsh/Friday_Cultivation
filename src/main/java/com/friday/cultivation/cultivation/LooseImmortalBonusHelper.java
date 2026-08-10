/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 */
package com.friday.cultivation.cultivation;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.cultivation.spell.Spell;
import net.minecraft.world.entity.player.Player;

public final class LooseImmortalBonusHelper {
    public static final int MAX_TRIBULATIONS = 9;
    public static final int INTERVAL_YEARS = 500;
    public static final long INTERVAL_TICKS = 12000000L;
    public static final int WARNING_TICKS = 200;
    public static final int WAVES_PER_TRIBULATION = 9;
    public static final int BOLTS_PER_WAVE = 9;
    public static final int STRIKE_DAMAGE = 1000;
    private static final int[] BODY_DEFENSE = new int[]{0, 0, 8, 14, 22, 32, 45, 60, 78, 100};
    private static final int[] CULTIVATION_EFFICIENCY = new int[]{0, 0, 2, 4, 7, 10, 14, 18, 23, 30};
    private static final int[] QI_RECOVERY = new int[]{0, 0, 2, 4, 7, 10, 14, 18, 23, 30};
    private static final int[] MELEE_DAMAGE = new int[]{0, 0, 8, 14, 22, 32, 45, 60, 78, 100};
    private static final double[] SPELL_DAMAGE = new double[]{1.0, 1.0, 1.1, 1.2, 1.35, 1.55, 1.8, 2.1, 2.45, 3.0};
    private static final double[] SPELL_COST = new double[]{1.0, 1.0, 0.95, 0.9, 0.85, 0.78, 0.7, 0.62, 0.55, 0.45};
    private static final long[] MAX_QI = new long[]{0L, 0L, 10000L, 25000L, 45000L, 70000L, 100000L, 135000L, 175000L, 220000L};
    private static final int[] FREE_ZHENYUAN = new int[]{0, 0, 10, 25, 45, 70, 100, 135, 175, 220};
    private static final int[] AUTO_ZHENYUAN_ATTR = new int[]{0, 0, 5, 10, 18, 28, 40, 55, 73, 95};

    private LooseImmortalBonusHelper() {
    }

    public static boolean isLooseImmortal(CultivationData data) {
        return data != null && data.getRealm() == Realm.LOOSE_IMMORTAL && data.getLooseImmortalTribulations() > 0;
    }

    public static int level(CultivationData data) {
        return data == null ? 0 : LooseImmortalBonusHelper.clampLevel(data.getLooseImmortalTribulations());
    }

    public static int clampLevel(int level) {
        return Math.max(0, Math.min(9, level));
    }

    public static int wavesForCurrentLevel(int level) {
        int clamped = LooseImmortalBonusHelper.clampLevel(level);
        return clamped >= 1 && clamped < 9 ? 9 : 0;
    }

    public static int boltsPerWaveForCurrentLevel(int level) {
        int clamped = LooseImmortalBonusHelper.clampLevel(level);
        return clamped >= 1 && clamped < 9 ? 9 : 1;
    }

    public static int strikeDamageForCurrentLevel(int level) {
        int clamped = LooseImmortalBonusHelper.clampLevel(level);
        if (clamped <= 0 || clamped >= 9) {
            return 0;
        }
        return 1000;
    }

    public static int bodyDefenseBonus(Player player) {
        return LooseImmortalBonusHelper.bodyDefenseBonus(LooseImmortalBonusHelper.dataOf(player));
    }

    public static int bodyDefenseBonus(CultivationData data) {
        return BODY_DEFENSE[LooseImmortalBonusHelper.level(data)];
    }

    public static int bodyDefenseBonusForLevel(int level) {
        return BODY_DEFENSE[LooseImmortalBonusHelper.clampLevel(level)];
    }

    public static int cultivationEfficiencyBonus(Player player) {
        return CULTIVATION_EFFICIENCY[LooseImmortalBonusHelper.level(LooseImmortalBonusHelper.dataOf(player))];
    }

    public static int cultivationEfficiencyBonusForLevel(int level) {
        return CULTIVATION_EFFICIENCY[LooseImmortalBonusHelper.clampLevel(level)];
    }

    public static int qiRecoveryPerSecondBonus(Player player) {
        return QI_RECOVERY[LooseImmortalBonusHelper.level(LooseImmortalBonusHelper.dataOf(player))];
    }

    public static int qiRecoveryPerSecondBonusForLevel(int level) {
        return QI_RECOVERY[LooseImmortalBonusHelper.clampLevel(level)];
    }

    public static int meleeDamageBonus(Player player) {
        return MELEE_DAMAGE[LooseImmortalBonusHelper.level(LooseImmortalBonusHelper.dataOf(player))];
    }

    public static int meleeDamageBonusForLevel(int level) {
        return MELEE_DAMAGE[LooseImmortalBonusHelper.clampLevel(level)];
    }

    public static double spellDamageMultiplier(Player player, Spell spell) {
        return SPELL_DAMAGE[LooseImmortalBonusHelper.level(LooseImmortalBonusHelper.dataOf(player))];
    }

    public static int spellDamageBonusPercentForLevel(int level) {
        return (int)Math.round((SPELL_DAMAGE[LooseImmortalBonusHelper.clampLevel(level)] - 1.0) * 100.0);
    }

    public static double spellQiCostMultiplier(Player player, Spell spell) {
        return SPELL_COST[LooseImmortalBonusHelper.level(LooseImmortalBonusHelper.dataOf(player))];
    }

    public static int spellQiCostReductionPercentForLevel(int level) {
        return (int)Math.round((1.0 - SPELL_COST[LooseImmortalBonusHelper.clampLevel(level)]) * 100.0);
    }

    public static long maxQiBonus(Player player) {
        return LooseImmortalBonusHelper.maxQiBonus(LooseImmortalBonusHelper.dataOf(player));
    }

    public static long maxQiBonus(CultivationData data) {
        return MAX_QI[LooseImmortalBonusHelper.level(data)];
    }

    public static long maxQiBonusForLevel(int level) {
        return MAX_QI[LooseImmortalBonusHelper.clampLevel(level)];
    }

    public static int freeZhenyuanTotalForLevel(int level) {
        return FREE_ZHENYUAN[LooseImmortalBonusHelper.clampLevel(level)];
    }

    public static int automaticZhenyuanAttributesTotalForLevel(int level) {
        return AUTO_ZHENYUAN_ATTR[LooseImmortalBonusHelper.clampLevel(level)];
    }

    public static long maxQiBonusRewardBetween(int fromLevel, int toLevel) {
        return Math.max(0L, LooseImmortalBonusHelper.maxQiBonusForLevel(toLevel) - LooseImmortalBonusHelper.maxQiBonusForLevel(fromLevel));
    }

    public static int freeZhenyuanRewardBetween(int fromLevel, int toLevel) {
        return Math.max(0, LooseImmortalBonusHelper.freeZhenyuanTotalForLevel(toLevel) - LooseImmortalBonusHelper.freeZhenyuanTotalForLevel(fromLevel));
    }

    public static int automaticZhenyuanAttributesRewardBetween(int fromLevel, int toLevel) {
        return Math.max(0, LooseImmortalBonusHelper.automaticZhenyuanAttributesTotalForLevel(toLevel) - LooseImmortalBonusHelper.automaticZhenyuanAttributesTotalForLevel(fromLevel));
    }

    public static double maxHpMultiplyTotal(Player player) {
        CultivationData data = LooseImmortalBonusHelper.dataOf(player);
        return LooseImmortalBonusHelper.isLooseImmortal(data) && data.getLooseImmortalTribulations() == 1 ? -0.5 : 0.0;
    }

    public static void applyFirstTribulationPenalty(CultivationData data) {
        if (data == null) {
            return;
        }
        data.setAttrConstitution(data.getAttrConstitution() - 20);
        data.setAttrPhysique(data.getAttrPhysique() - 20);
        data.setAttrAgility(data.getAttrAgility() - 20);
        data.setAttrSpellPower(data.getAttrSpellPower() - 20);
        data.setAttrQiSea(data.getAttrQiSea() - 20);
    }

    private static CultivationData dataOf(Player player) {
        return player == null ? null : (CultivationData)CultivationCapability.get(player).orElse(null);
    }
}

