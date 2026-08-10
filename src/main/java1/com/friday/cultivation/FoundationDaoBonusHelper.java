package com.friday.cultivation;

import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.spell.Spell;
import net.minecraft.world.entity.player.Player;

/**
 * 筑基道基加成辅助（严格照搬原模组 com.xiaoxiang.cultivation.cultivation.FoundationDaoBonusHelper）
 */
public final class FoundationDaoBonusHelper {
    private FoundationDaoBonusHelper() {}

    private static FoundationDao daoOf(Player player) {
        if (player == null) return FoundationDao.NONE;
        CultivationData ic = com.friday.cultivation.CultivationCapability.get(player).orElse(null);
        if (ic == null) return FoundationDao.NONE;
        // d.getFoundationDao() 返回 com.friday.cultivation.dao.FoundationDao，
        // 通过 FoundationDao.valueOf 做兼容映射（两个枚举的常量名一致）
        String fdName = ic.getFoundationDao().name();
        return FoundationDao.valueOf(fdName);
    }

    public static double spellDamageMultiplier(Player player, Spell spell) {
        FoundationDao dao = daoOf(player);
        double mult = dao.spellDamageMult();
        if (dao.bloodMastery() && spell != null && spell.isBloodSpell()) {
            mult *= 1.5;
        }
        return mult;
    }

    public static double spellQiCostMultiplier(Player player, Spell spell) {
        FoundationDao dao = daoOf(player);
        double mult = dao.spellQiCostMult();
        if (dao.bloodMastery() && spell != null && spell.isBloodSpell()) {
            mult *= 0.5;
        }
        return mult;
    }

    public static double maxHpMultiplyTotal(Player player) {
        return daoOf(player).hpMult() - 1.0;
    }

    public static int bodyDefenseBonus(Player player) {
        return daoOf(player).bodyDefenseBonus();
    }

    public static int cultivationEfficiencyBonus(Player player) {
        return daoOf(player).cultivationEfficiencyBonus();
    }

    public static int qiRecoveryPerSecondBonus(Player player) {
        return daoOf(player).qiRecoveryPerSecondBonus();
    }

    public static int meleeDamageBonus(Player player) {
        return daoOf(player).meleeDamageBonus();
    }
}