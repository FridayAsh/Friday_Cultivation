package com.friday.cultivation.dao;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.realm.Realm;
import com.friday.cultivation.spell.Spell;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * 金丹 Dao 加成辅助 — 完整复刻原模组 GoldenCoreDaoBonusHelper
 * 包含：法术伤害/消耗倍率（血法额外乘bloodSpell倍率）、HP/体防/修炼效率/灵气恢复/近战加成、
 * 天道金丹阳光下灵气恢复翻倍
 */
public final class GoldenCoreDaoBonusHelper {
    private GoldenCoreDaoBonusHelper() {}

    public static GoldenCoreDao daoOf(Player player) {
        if (player == null) return GoldenCoreDao.NONE;
        CultivationData ic = CultivationCapability.get(player).orElse(null);
        if (ic == null) return GoldenCoreDao.NONE;
        if (ic.getRealm().ordinal() < Realm.GOLDEN_CORE.ordinal()) return GoldenCoreDao.NONE;
        return ic.getGoldenCoreDao();
    }

    /** 法术伤害倍率（血法术额外乘 bloodSpellDamageMult） */
    public static double spellDamageMultiplier(Player player, Spell spell) {
        GoldenCoreDao dao = daoOf(player);
        double mult = dao.spellDamageMult();
        if (spell != null && spell.isBloodSpell()) {
            mult *= dao.bloodSpellDamageMult();
        }
        return sanitize(mult);
    }

    /** 法术灵气消耗倍率（血法术额外乘 bloodSpellQiCostMult） */
    public static double spellQiCostMultiplier(Player player, Spell spell) {
        GoldenCoreDao dao = daoOf(player);
        double mult = dao.spellQiCostMult();
        if (spell != null && spell.isBloodSpell()) {
            mult *= dao.bloodSpellQiCostMult();
        }
        return sanitize(mult);
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

    /** 天道金丹：站在户外阳光下时灵气恢复翻倍 */
    public static long applyQiRecoveryMultiplier(ServerPlayer player, CultivationData data, long baseRecovery) {
        if (player == null || data == null || baseRecovery <= 0L) return baseRecovery;
        if (data.getRealm().ordinal() < Realm.GOLDEN_CORE.ordinal()) return baseRecovery;
        if (data.getGoldenCoreDao() != GoldenCoreDao.HEAVEN) return baseRecovery;
        BlockPos head = player.blockPosition().above();
        if (player.level().dimensionType().hasSkyLight()
                && player.level().canSeeSky(head)
                && !player.level().isRainingAt(head)) {
            return Math.max(1L, baseRecovery * 2L);
        }
        return baseRecovery;
    }

    private static double sanitize(double multiplier) {
        if (!Double.isFinite(multiplier)) return 1.0;
        return Math.max(0.0, multiplier);
    }
}
