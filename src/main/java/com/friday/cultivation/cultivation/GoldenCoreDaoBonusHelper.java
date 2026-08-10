/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 */
package com.friday.cultivation.cultivation;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.GoldenCoreDao;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.cultivation.spell.Spell;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class GoldenCoreDaoBonusHelper {
    private GoldenCoreDaoBonusHelper() {
    }

    public static GoldenCoreDao daoOf(Player player) {
        if (player == null) {
            return GoldenCoreDao.NONE;
        }
        CultivationData data = CultivationCapability.get(player).orElse(null);
        if (data == null || data.getRealm().ordinal() < Realm.GOLDEN_CORE.ordinal()) {
            return GoldenCoreDao.NONE;
        }
        return data.getGoldenCoreDao();
    }

    public static double spellDamageMultiplier(Player player, Spell spell) {
        GoldenCoreDao dao = GoldenCoreDaoBonusHelper.daoOf(player);
        double mult = dao.spellDamageMult();
        if (spell != null && spell.isBloodSpell()) {
            mult *= dao.bloodSpellDamageMult();
        }
        return GoldenCoreDaoBonusHelper.sanitize(mult);
    }

    public static double spellQiCostMultiplier(Player player, Spell spell) {
        GoldenCoreDao dao = GoldenCoreDaoBonusHelper.daoOf(player);
        double mult = dao.spellQiCostMult();
        if (spell != null && spell.isBloodSpell()) {
            mult *= dao.bloodSpellQiCostMult();
        }
        return GoldenCoreDaoBonusHelper.sanitize(mult);
    }

    public static double maxHpMultiplyTotal(Player player) {
        return GoldenCoreDaoBonusHelper.daoOf(player).hpMult() - 1.0;
    }

    public static int bodyDefenseBonus(Player player) {
        return GoldenCoreDaoBonusHelper.daoOf(player).bodyDefenseBonus();
    }

    public static int cultivationEfficiencyBonus(Player player) {
        return GoldenCoreDaoBonusHelper.daoOf(player).cultivationEfficiencyBonus();
    }

    public static int qiRecoveryPerSecondBonus(Player player) {
        return GoldenCoreDaoBonusHelper.daoOf(player).qiRecoveryPerSecondBonus();
    }

    public static int meleeDamageBonus(Player player) {
        return GoldenCoreDaoBonusHelper.daoOf(player).meleeDamageBonus();
    }

    public static long applyQiRecoveryMultiplier(ServerPlayer player, CultivationData data, long baseRecovery) {
        if (player == null || data == null || baseRecovery <= 0L) {
            return baseRecovery;
        }
        if (data.getRealm().ordinal() < Realm.GOLDEN_CORE.ordinal() || data.getGoldenCoreDao() != GoldenCoreDao.HEAVEN) {
            return baseRecovery;
        }
        BlockPos head = player.blockPosition().above();
        if (player.level().dimensionType().hasSkyLight() && player.level().isDay() && player.level().canSeeSky(head) && !player.level().isRainingAt(head)) {
            return Math.max(1L, baseRecovery * 2L);
        }
        return baseRecovery;
    }

    private static double sanitize(double multiplier) {
        if (!Double.isFinite(multiplier)) {
            return 1.0;
        }
        return Math.max(0.0, multiplier);
    }
}

