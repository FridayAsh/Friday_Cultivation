package com.friday.cultivation.dao;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.realm.Realm;
import com.friday.cultivation.spell.Spell;
import net.minecraft.world.entity.player.Player;

/**
 * 散仙天劫加成辅助 — 完整复刻原模组 LooseImmortalBonusHelper
 * 包含：散仙等级判断、9波天劫参数、体防/修炼效率/灵气恢复/近战/法术伤害/法术消耗/最大灵气/真元奖励等按等级数组计算
 */
public final class LooseImmortalBonusHelper {
    public static final int MAX_TRIBULATIONS = 9;
    public static final int INTERVAL_YEARS = 500;
    public static final long INTERVAL_TICKS = 12000000L;
    public static final int WARNING_TICKS = 200;
    public static final int WAVES_PER_TRIBULATION = 9;
    public static final int BOLTS_PER_WAVE = 9;
    public static final int STRIKE_DAMAGE = 1000;

    private static final int[] BODY_DEFENSE = {0, 0, 8, 14, 22, 32, 45, 60, 78, 100};
    private static final int[] CULTIVATION_EFFICIENCY = {0, 0, 2, 4, 7, 10, 14, 18, 23, 30};
    private static final int[] QI_RECOVERY = {0, 0, 2, 4, 7, 10, 14, 18, 23, 30};
    private static final int[] MELEE_DAMAGE = {0, 0, 8, 14, 22, 32, 45, 60, 78, 100};
    private static final double[] SPELL_DAMAGE = {1.0, 1.0, 1.1, 1.2, 1.35, 1.55, 1.8, 2.1, 2.45, 3.0};
    private static final double[] SPELL_COST = {1.0, 1.0, 0.95, 0.9, 0.85, 0.78, 0.7, 0.62, 0.55, 0.45};
    private static final long[] MAX_QI = {0L, 0L, 10000L, 25000L, 45000L, 70000L, 100000L, 135000L, 175000L, 220000L};
    private static final int[] FREE_ZHENYUAN = {0, 0, 10, 25, 45, 70, 100, 135, 175, 220};
    private static final int[] AUTO_ZHENYUAN_ATTR = {0, 0, 5, 10, 18, 28, 40, 55, 73, 95};

    private LooseImmortalBonusHelper() {}

    /** 是否散仙（境界=散仙 且 已渡劫次数>0） */
    public static boolean isLooseImmortal(CultivationData data) {
        return data != null && data.getRealm() == Realm.LOOSE_IMMORTAL && data.getLooseImmortalTribulations() > 0;
    }

    /** 散仙等级（=已渡劫次数，clamp 0~9） */
    public static int level(CultivationData data) {
        return data == null ? 0 : clampLevel(data.getLooseImmortalTribulations());
    }

    /** 等级钳制 0~9 */
    public static int clampLevel(int level) {
        return Math.max(0, Math.min(MAX_TRIBULATIONS, level));
    }

    /** 当前等级天劫波数（1~8级=9波，其他0） */
    public static int wavesForCurrentLevel(int level) {
        int clamped = clampLevel(level);
        return (clamped >= 1 && clamped < 9) ? 9 : 0;
    }

    /** 当前等级每波雷数（1~8级=9，其他1） */
    public static int boltsPerWaveForCurrentLevel(int level) {
        int clamped = clampLevel(level);
        return (clamped >= 1 && clamped < 9) ? 9 : 1;
    }

    /** 当前等级雷击伤害（1~8级=1000，其他0） */
    public static int strikeDamageForCurrentLevel(int level) {
        int clamped = clampLevel(level);
        return (clamped <= 0 || clamped >= 9) ? 0 : STRIKE_DAMAGE;
    }

    public static int bodyDefenseBonus(Player player) {
        return bodyDefenseBonus(dataOf(player));
    }

    public static int bodyDefenseBonus(CultivationData data) {
        return BODY_DEFENSE[level(data)];
    }

    public static int bodyDefenseBonusForLevel(int level) {
        return BODY_DEFENSE[clampLevel(level)];
    }

    public static int cultivationEfficiencyBonus(Player player) {
        return CULTIVATION_EFFICIENCY[level(dataOf(player))];
    }

    public static int cultivationEfficiencyBonusForLevel(int level) {
        return CULTIVATION_EFFICIENCY[clampLevel(level)];
    }

    public static double cultivationEfficiencyBonusForLevelDouble(int level) {
        return CULTIVATION_EFFICIENCY[clampLevel(level)];
    }

    public static int qiRecoveryPerSecondBonus(Player player) {
        return QI_RECOVERY[level(dataOf(player))];
    }

    public static int qiRecoveryPerSecondBonusForLevel(int level) {
        return QI_RECOVERY[clampLevel(level)];
    }

    public static int meleeDamageBonus(Player player) {
        return MELEE_DAMAGE[level(dataOf(player))];
    }

    public static int meleeDamageBonusForLevel(int level) {
        return MELEE_DAMAGE[clampLevel(level)];
    }

    public static double spellDamageMultiplier(Player player, Spell spell) {
        return SPELL_DAMAGE[level(dataOf(player))];
    }

    public static int spellDamageBonusPercentForLevel(int level) {
        return (int) Math.round((SPELL_DAMAGE[clampLevel(level)] - 1.0) * 100.0);
    }

    public static double spellQiCostMultiplier(Player player, Spell spell) {
        return SPELL_COST[level(dataOf(player))];
    }

    public static int spellQiCostReductionPercentForLevel(int level) {
        return (int) Math.round((1.0 - SPELL_COST[clampLevel(level)]) * 100.0);
    }

    public static long maxQiBonus(Player player) {
        return maxQiBonus(dataOf(player));
    }

    public static long maxQiBonus(CultivationData data) {
        return MAX_QI[level(data)];
    }

    public static long maxQiBonusForLevel(int level) {
        return MAX_QI[clampLevel(level)];
    }

    public static int freeZhenyuanTotalForLevel(int level) {
        return FREE_ZHENYUAN[clampLevel(level)];
    }

    public static int automaticZhenyuanAttributesTotalForLevel(int level) {
        return AUTO_ZHENYUAN_ATTR[clampLevel(level)];
    }

    public static long maxQiBonusRewardBetween(int fromLevel, int toLevel) {
        return Math.max(0L, maxQiBonusForLevel(toLevel) - maxQiBonusForLevel(fromLevel));
    }

    public static int freeZhenyuanRewardBetween(int fromLevel, int toLevel) {
        return Math.max(0, freeZhenyuanTotalForLevel(toLevel) - freeZhenyuanTotalForLevel(fromLevel));
    }

    public static int automaticZhenyuanAttributesRewardBetween(int fromLevel, int toLevel) {
        return Math.max(0, automaticZhenyuanAttributesTotalForLevel(toLevel) - automaticZhenyuanAttributesTotalForLevel(fromLevel));
    }

    /** HP倍率增量（首次渡劫-50%，其他0） */
    public static double maxHpMultiplyTotal(Player player) {
        CultivationData data = dataOf(player);
        return (isLooseImmortal(data) && data.getLooseImmortalTribulations() == 1) ? -0.5 : 0.0;
    }

    /** 首次渡劫惩罚（5项属性各-20） */
    public static void applyFirstTribulationPenalty(CultivationData data) {
        if (data == null) return;
        data.setAttrConstitution(data.getAttrConstitution() - 20);
        data.setAttrPhysique(data.getAttrPhysique() - 20);
        data.setAttrAgility(data.getAttrAgility() - 20);
        data.setAttrSpellPower(data.getAttrSpellPower() - 20);
        data.setAttrQiSea(data.getAttrQiSea() - 20);
    }

    private static CultivationData dataOf(Player player) {
        if (player == null) return null;
        CultivationData ic = CultivationCapability.get(player).orElse(null);
        return ic != null ? ic : null;
    }
}
