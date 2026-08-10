package com.friday.cultivation;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import net.minecraft.world.entity.player.Player;

/**
 * 真元加成计算（严格照搬原模组 com.xiaoxiang.cultivation.cultivation.ZhenyuanBonusHelper）
 */
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
    @Deprecated(forRemoval = false)
    public static final int QI_SEA_ABSORB_RANGE_PER_POINT = 0;
    @Deprecated(forRemoval = false)
    public static final double QI_SEA_ABSORB_MULT_PER_POINT = 0.0;

    private ZhenyuanBonusHelper() {}

    private static CultivationData dataOf(Player player) {
        if (player == null) return null;
        CultivationData ic = CultivationCapability.get(player).orElse(null);
        return ic != null ? ic : null;
    }

    public static double constitutionHpBonus(Player player) {
        CultivationData d = dataOf(player);
        if (d == null) return 0.0;
        return (double) d.getAttrConstitution() * HP_PER_POINT;
    }

    public static int physiqueAttackBonus(Player player) {
        CultivationData d = dataOf(player);
        if (d == null || !d.isBonusCategoryEnabled(CultivationBonusCategory.MELEE_DAMAGE)) return 0;
        return d.getAttrPhysique() * PHYSIQUE_ATK_PER_POINT;
    }

    public static double physiqueMiningSpeedPct(Player player) {
        CultivationData d = dataOf(player);
        if (d == null || !d.isBonusCategoryEnabled(CultivationBonusCategory.MINING_SPEED)) return 0.0;
        return (double) d.getAttrPhysique() * PHYSIQUE_MINING_SPEED_PCT_PER_POINT;
    }

    public static double physiqueMiningSpeedBonus(Player player) {
        return Math.min(MAX_PHYSIQUE_MINING_SPEED_BONUS, physiqueMiningSpeedPct(player) / 100.0);
    }

    public static double agilityMoveSpeedMult(Player player) {
        CultivationData d = dataOf(player);
        if (d == null || !d.isBonusCategoryEnabled(CultivationBonusCategory.MOVEMENT_SPEED)) return 0.0;
        return (double) d.getAttrAgility() * AGILITY_MOVE_PCT_PER_POINT / 100.0;
    }

    public static double agilityJumpHeightMult(Player player) {
        CultivationData d = dataOf(player);
        if (d == null || !d.isBonusCategoryEnabled(CultivationBonusCategory.JUMP_HEIGHT)) return 0.0;
        return (double) d.getAttrAgility() * AGILITY_JUMP_PCT_PER_POINT / 100.0;
    }

    public static double agilityJumpVelocityMult(Player player) {
        double heightBonus = agilityJumpHeightMult(player);
        if (heightBonus <= 0.0) return 0.0;
        return Math.sqrt(1.0 + heightBonus) - 1.0;
    }

    public static double spellPowerMultiplier(Player player) {
        CultivationData d = dataOf(player);
        if (d == null || !d.isBonusCategoryEnabled(CultivationBonusCategory.SPELL_DAMAGE)) return 1.0;
        return 1.0 + (double) d.getAttrSpellPower() * SPELL_DAMAGE_PCT_PER_POINT / 100.0;
    }

    public static long qiSeaFlatBonus(CultivationData d) {
        if (d == null || !d.isBonusCategoryEnabled(CultivationBonusCategory.MAX_QI)) return 0L;
        return (long) d.getAttrQiSea() * QI_SEA_FLAT_PER_POINT;
    }

    public static long qiSeaFlatBonus(Player player) {
        return qiSeaFlatBonus(dataOf(player));
    }

    public static long qiSeaRecoveryPerSecond(CultivationData d) {
        if (d == null || !d.isBonusCategoryEnabled(CultivationBonusCategory.QI_RECOVERY)) return 0L;
        return (long) d.getAttrQiSea() * QI_SEA_QI_RECOVERY_PER_POINT;
    }

    public static long qiSeaRecoveryPerSecond(Player player) {
        return qiSeaRecoveryPerSecond(dataOf(player));
    }

    @Deprecated(forRemoval = false)
    public static int qiSeaAbsorbRangeBonus(CultivationData d) { return 0; }

    @Deprecated(forRemoval = false)
    public static int qiSeaAbsorbRangeBonus(Player player) { return 0; }

    @Deprecated(forRemoval = false)
    public static double qiSeaAbsorbMultiplierBonus(CultivationData d) { return 0.0; }

    @Deprecated(forRemoval = false)
    public static double qiSeaAbsorbMultiplierBonus(Player player) { return 0.0; }
}
