package com.friday.cultivation;

import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.FoundationDaoBonusHelper;
import com.friday.cultivation.dao.GoldenCoreDaoBonusHelper;
import com.friday.cultivation.LooseImmortalBonusHelper;
import com.friday.cultivation.technique.TechniqueBonusHelper;
import net.minecraft.world.entity.player.Player;

/**
 * 体术防御助手 - 玩家的体术/护甲防御乘数。
 * 完全照搬原 mod: xiaoxiang.cultivation.cultivation.BodyDefenseHelper
 */
public final class BodyDefenseHelper {
    public static final int BASE_BODY_DEFENSE = 2;

    private BodyDefenseHelper() {
    }

    private static CultivationData dataOf(Player player) {
        if (player == null) {
            return null;
        }
        return CultivationCapability.get(player).orElse(null);
    }

    public static int playerRawBodyDefense(Player player) {
        if (player == null) {
            return 0;
        }
        CultivationData d = BodyDefenseHelper.dataOf(player);
        int self = d == null ? 0 : d.getDefense();
        return 2 + self + TechniqueBonusHelper.defenseBonus(player) + FoundationDaoBonusHelper.bodyDefenseBonus(player) + GoldenCoreDaoBonusHelper.bodyDefenseBonus(player) + LooseImmortalBonusHelper.bodyDefenseBonus(player);
    }

    public static int playerEffectiveBodyDefense(Player player) {
        if (player == null) {
            return 0;
        }
        CultivationData d = BodyDefenseHelper.dataOf(player);
        if (d != null && !d.isBodyDefenseEnabled()) {
            return 0;
        }
        return BodyDefenseHelper.playerRawBodyDefense(player);
    }

    public static int npcBodyDefense(int techniqueDefenseBonus) {
        return 2 + Math.max(0, techniqueDefenseBonus);
    }
}
