/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 */
package com.friday.cultivation.cultivation;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.FoundationDaoBonusHelper;
import com.friday.cultivation.cultivation.GoldenCoreDaoBonusHelper;
import com.friday.cultivation.cultivation.LooseImmortalBonusHelper;
import com.friday.cultivation.cultivation.technique.TechniqueBonusHelper;
import net.minecraft.world.entity.player.Player;

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

