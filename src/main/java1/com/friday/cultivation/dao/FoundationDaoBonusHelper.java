package com.friday.cultivation.dao;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.spell.Spell;
import net.minecraft.world.entity.player.Player;

/**
 * 筑基 Dao 加成辅助 — 完整复刻原模组 FoundationDaoBonusHelper
 * 包含：法术伤害倍率（血道筑基对血法+50%）、法术灵气消耗倍率（血道-50%）、HP/体防/修炼效率/灵气恢复/近战加成
 */
public final class FoundationDaoBonusHelper {
    private FoundationDaoBonusHelper() {}

    private static FoundationDao daoOf(Player player) {
        if (player == null) return FoundationDao.NONE;
        CultivationData ic = CultivationCapability.get(player).orElse(null);
        return ic != null ? ic.getFoundationDao() : FoundationDao.NONE;
    }

    /** 法术伤害倍率（血道筑基对血法术额外×1.5） */
    public static double spellDamageMultiplier(Player player, Spell spell) {
        FoundationDao dao = daoOf(player);
        double mult = dao.spellDamageMult();
        if (dao.bloodMastery() && spell != null && spell.isBloodSpell()) {
            mult *= 1.5;
        }
        return mult;
    }

    /** 法术灵气消耗倍率（血道筑基对血法术额外×0.5） */
    public static double spellQiCostMultiplier(Player player, Spell spell) {
        FoundationDao dao = daoOf(player);
        double mult = dao.spellQiCostMult();
        if (dao.bloodMastery() && spell != null && spell.isBloodSpell()) {
            mult *= 0.5;
        }
        return mult;
    }

    /** HP倍率增量（dao.hpMult - 1.0） */
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
