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
    public static final double HP_PER_POINT = 1.0;
    public static final int PHYSIQUE_ATK_PER_POINT = 1;
    public static final double PHYSIQUE_MINING_SPEED_PCT_PER_POINT = 1.0;
    public static final double MAX_PHYSIQUE_MINING_SPEED_BONUS = 1.0;
    public static final double AGILITY_MOVE_PCT_PER_POINT = 1.0;
    public static final double AGILITY_JUMP_PCT_PER_POINT = 0.2;
    public static final double SPELL_DAMAGE_PCT_PER_POINT = 5.0;
    public static final long QI_SEA_FLAT_PER_POINT = 100L;
    public static final long QI_SEA_QI_RECOVERY_PER_POINT = 1L;
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

    public static double constitutionHpBonus(Player player) {
        CultivationData d = ZhenyuanBonusHelper.dataOf(player);
        if (d == null) {
            return 0.0;
        }
        // 每点真元体质 +10 生命值
        return (double)d.getAttrConstitution() * 10.0;
    }

    /** 每点真元体质 +8 盔甲值 */
    public static double constitutionArmorBonus(Player player) {
        CultivationData d = ZhenyuanBonusHelper.dataOf(player);
        if (d == null) {
            return 0.0;
        }
        return (double)d.getAttrConstitution() * 8.0;
    }

    /** 每点真元体质 +3 韧性 */
    public static double constitutionToughnessBonus(Player player) {
        CultivationData d = ZhenyuanBonusHelper.dataOf(player);
        if (d == null) {
            return 0.0;
        }
        return (double)d.getAttrConstitution() * 3.0;
    }

    public static int physiqueAttackBonus(Player player) {
        CultivationData d = ZhenyuanBonusHelper.dataOf(player);
        if (d == null || !d.isBonusCategoryEnabled(CultivationBonusCategory.MELEE_DAMAGE)) {
            return 0;
        }
        return d.getAttrPhysique() * 1;
    }

    public static double physiqueMiningSpeedPct(Player player) {
        CultivationData d = ZhenyuanBonusHelper.dataOf(player);
        if (d == null || !d.isBonusCategoryEnabled(CultivationBonusCategory.MINING_SPEED)) {
            return 0.0;
        }
        return (double)d.getAttrPhysique() * 1.0;
    }

    public static double physiqueMiningSpeedBonus(Player player) {
        return Math.min(1.0, ZhenyuanBonusHelper.physiqueMiningSpeedPct(player) / 100.0);
    }

    public static double agilityMoveSpeedMult(Player player) {
        CultivationData d = ZhenyuanBonusHelper.dataOf(player);
        if (d == null || !d.isBonusCategoryEnabled(CultivationBonusCategory.MOVEMENT_SPEED)) {
            return 0.0;
        }
        return (double)d.getAttrAgility() * 1.0 / 100.0;
    }

    public static double agilityJumpHeightMult(Player player) {
        CultivationData d = ZhenyuanBonusHelper.dataOf(player);
        if (d == null || !d.isBonusCategoryEnabled(CultivationBonusCategory.JUMP_HEIGHT)) {
            return 0.0;
        }
        return (double)d.getAttrAgility() * 0.2 / 100.0;
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
        return 1.0 + (double)d.getAttrSpellPower() * 5.0 / 100.0;
    }

    public static long qiSeaFlatBonus(CultivationData d) {
        if (d == null || !d.isBonusCategoryEnabled(CultivationBonusCategory.MAX_QI)) {
            return 0L;
        }
        return (long)d.getAttrQiSea() * 100L;
    }

    public static long qiSeaFlatBonus(Player player) {
        return ZhenyuanBonusHelper.qiSeaFlatBonus(ZhenyuanBonusHelper.dataOf(player));
    }

    public static long qiSeaRecoveryPerSecond(CultivationData d) {
        if (d == null || !d.isBonusCategoryEnabled(CultivationBonusCategory.QI_RECOVERY)) {
            return 0L;
        }
        return (long)d.getAttrQiSea() * 1L;
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

