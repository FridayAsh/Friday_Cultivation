/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 */
package com.friday.cultivation.cultivation;

import com.friday.cultivation.cultivation.CultivationBonusCategory;
import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import net.minecraft.world.entity.player.Player;

public final class ZhenyuanBonusHelper {
    // ===== 每点真元属性增益常量（改数值只改这里）=====
    /** 体质：每点 +10 生命 */
    public static final double HP_PER_POINT = 10.0;
    /** 体质：每点 +8 盔甲 */
    public static final double ARMOR_PER_POINT = 8.0;
    /** 体质：每点 +3 韧性 */
    public static final double TOUGHNESS_PER_POINT = 3.0;
    /** 筋骨：每点 +4 近战伤害 */
    public static final int PHYSIQUE_ATK_PER_POINT = 4;
    /** 筋骨：每点 +1% 挖掘速度 */
    public static final double PHYSIQUE_MINING_SPEED_PCT_PER_POINT = 1.0;
    public static final double MAX_PHYSIQUE_MINING_SPEED_BONUS = 1.0;
    /** 身法：每点 +1% 移动速度 */
    public static final double AGILITY_MOVE_PCT_PER_POINT = 1.0;
    /** 身法：每点 +0.2% 跳跃高度 */
    public static final double AGILITY_JUMP_PCT_PER_POINT = 0.2;
    /** 法伤：每点 +5% 法术伤害 */
    public static final double SPELL_DAMAGE_PCT_PER_POINT = 5.0;
    /** 气海：每点 +200 灵气上限 */
    public static final long QI_SEA_FLAT_PER_POINT = 200L;
    /** 气海：每点 +3/秒 灵气回复 */
    public static final long QI_SEA_QI_RECOVERY_PER_POINT = 3L;
    @Deprecated(forRemoval=false)
    public static final int QI_SEA_ABSORB_RANGE_PER_POINT = 0;
    @Deprecated(forRemoval=false)
    public static final double QI_SEA_ABSORB_MULT_PER_POINT = 0.0;

    private ZhenyuanBonusHelper() {
    }

    private static CultivationData dataOf(Player player) {
        if (player == null) {
            return null;
        }
        return CultivationCapability.get(player).orElse(null);
    }

    /** 渡劫隐藏加成乘数（连乘复利；身法不受加成） */
    private static double hiddenMult(CultivationData d) {
        return d == null ? 1.0 : d.activeTribulationMultiplier();
    }

    /** 体质总点数（含渡劫隐藏奖励，隐藏不显示在雷达图） */
    private static int constitutionPoints(CultivationData d) {
        if (d == null) {
            return 0;
        }
        return (int) Math.round(d.getAttrConstitution() * ZhenyuanBonusHelper.hiddenMult(d));
    }

    /** 筋骨总点数（含渡劫隐藏奖励） */
    private static int physiquePoints(CultivationData d) {
        if (d == null) {
            return 0;
        }
        return (int) Math.round(d.getAttrPhysique() * ZhenyuanBonusHelper.hiddenMult(d));
    }

    /** 身法总点数（含渡劫隐藏奖励） */
    private static int agilityPoints(CultivationData d) {
        return d == null ? 0 : d.getAttrAgility();
    }

    /** 法伤总点数（含渡劫隐藏奖励） */
    private static int spellPowerPoints(CultivationData d) {
        if (d == null) {
            return 0;
        }
        return (int) Math.round(d.getAttrSpellPower() * ZhenyuanBonusHelper.hiddenMult(d));
    }

    /** 气海总点数（含渡劫隐藏奖励） */
    private static int qiSeaPoints(CultivationData d) {
        if (d == null) {
            return 0;
        }
        return (int) Math.round(d.getAttrQiSea() * ZhenyuanBonusHelper.hiddenMult(d));
    }

    public static double constitutionHpBonus(Player player) {
        CultivationData d = ZhenyuanBonusHelper.dataOf(player);
        if (d == null) {
            return 0.0;
        }
        // 每点真元体质 +HP_PER_POINT 生命值
        return (double)ZhenyuanBonusHelper.constitutionPoints(d) * HP_PER_POINT;
    }

    /** 每点真元体质 +ARMOR_PER_POINT 盔甲值 */
    public static double constitutionArmorBonus(Player player) {
        CultivationData d = ZhenyuanBonusHelper.dataOf(player);
        if (d == null) {
            return 0.0;
        }
        return (double)ZhenyuanBonusHelper.constitutionPoints(d) * ARMOR_PER_POINT;
    }

    /** 每点真元体质 +TOUGHNESS_PER_POINT 韧性 */
    public static double constitutionToughnessBonus(Player player) {
        CultivationData d = ZhenyuanBonusHelper.dataOf(player);
        if (d == null) {
            return 0.0;
        }
        return (double)ZhenyuanBonusHelper.constitutionPoints(d) * TOUGHNESS_PER_POINT;
    }

    public static int physiqueAttackBonus(Player player) {
        CultivationData d = ZhenyuanBonusHelper.dataOf(player);
        if (d == null || !d.isBonusCategoryEnabled(CultivationBonusCategory.MELEE_DAMAGE)) {
            return 0;
        }
        return ZhenyuanBonusHelper.physiquePoints(d) * PHYSIQUE_ATK_PER_POINT;
    }

    public static double physiqueMiningSpeedPct(Player player) {
        CultivationData d = ZhenyuanBonusHelper.dataOf(player);
        if (d == null || !d.isBonusCategoryEnabled(CultivationBonusCategory.MINING_SPEED)) {
            return 0.0;
        }
        return (double)ZhenyuanBonusHelper.physiquePoints(d) * PHYSIQUE_MINING_SPEED_PCT_PER_POINT;
    }

    public static double physiqueMiningSpeedBonus(Player player) {
        return Math.min(1.0, ZhenyuanBonusHelper.physiqueMiningSpeedPct(player) / 100.0);
    }

    public static double agilityMoveSpeedMult(Player player) {
        CultivationData d = ZhenyuanBonusHelper.dataOf(player);
        if (d == null || !d.isBonusCategoryEnabled(CultivationBonusCategory.MOVEMENT_SPEED)) {
            return 0.0;
        }
        return (double)ZhenyuanBonusHelper.agilityPoints(d) * AGILITY_MOVE_PCT_PER_POINT / 100.0;
    }

    public static double agilityJumpHeightMult(Player player) {
        CultivationData d = ZhenyuanBonusHelper.dataOf(player);
        if (d == null || !d.isBonusCategoryEnabled(CultivationBonusCategory.JUMP_HEIGHT)) {
            return 0.0;
        }
        return (double)ZhenyuanBonusHelper.agilityPoints(d) * AGILITY_JUMP_PCT_PER_POINT / 100.0;
    }

    public static double agilityJumpVelocityMult(Player player) {
        double heightBonus = ZhenyuanBonusHelper.agilityJumpHeightMult(player);
        if (heightBonus <= 0.0) {
            return 0.0;
        }
        return Math.sqrt(1.0 + heightBonus) - 1.0;
    }

    public static double spellPowerMultiplier(Player player) {
        CultivationData d = ZhenyuanBonusHelper.dataOf(player);
        if (d == null || !d.isBonusCategoryEnabled(CultivationBonusCategory.SPELL_DAMAGE)) {
            return 1.0;
        }
        return 1.0 + (double)ZhenyuanBonusHelper.spellPowerPoints(d) * SPELL_DAMAGE_PCT_PER_POINT / 100.0;
    }

    public static long qiSeaFlatBonus(CultivationData d) {
        if (d == null || !d.isBonusCategoryEnabled(CultivationBonusCategory.MAX_QI)) {
            return 0L;
        }
        return (long)ZhenyuanBonusHelper.qiSeaPoints(d) * QI_SEA_FLAT_PER_POINT;
    }

    public static long qiSeaFlatBonus(Player player) {
        return ZhenyuanBonusHelper.qiSeaFlatBonus(ZhenyuanBonusHelper.dataOf(player));
    }

    public static long qiSeaRecoveryPerSecond(CultivationData d) {
        if (d == null || !d.isBonusCategoryEnabled(CultivationBonusCategory.QI_RECOVERY)) {
            return 0L;
        }
        return (long)ZhenyuanBonusHelper.qiSeaPoints(d) * QI_SEA_QI_RECOVERY_PER_POINT;
    }

    public static long qiSeaRecoveryPerSecond(Player player) {
        return ZhenyuanBonusHelper.qiSeaRecoveryPerSecond(ZhenyuanBonusHelper.dataOf(player));
    }

    @Deprecated(forRemoval=false)
    public static int qiSeaAbsorbRangeBonus(CultivationData d) {
        return 0;
    }

    @Deprecated(forRemoval=false)
    public static int qiSeaAbsorbRangeBonus(Player player) {
        return 0;
    }

    @Deprecated(forRemoval=false)
    public static double qiSeaAbsorbMultiplierBonus(CultivationData d) {
        return 0.0;
    }

    @Deprecated(forRemoval=false)
    public static double qiSeaAbsorbMultiplierBonus(Player player) {
        return 0.0;
    }
}

