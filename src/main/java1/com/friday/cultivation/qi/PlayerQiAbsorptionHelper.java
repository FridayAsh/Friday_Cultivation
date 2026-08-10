package com.friday.cultivation.qi;

import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.physique.PhysiqueBonusHelper;
import com.friday.cultivation.realm.Realm;
import com.friday.cultivation.registry.ModDimensions;
import com.friday.cultivation.technique.TechniqueLoadoutHelper;
import net.minecraft.server.level.ServerPlayer;

/**
 * 玩家灵气吸收助手 - 判断玩家能否自动吸收灵气并计算基础吸收倍率。
 * 严格 1:1 复刻原 mod: xiaoxiang.cultivation.cultivation.qi.PlayerQiAbsorptionHelper
 */
public final class PlayerQiAbsorptionHelper {
    public static final double MORTAL_EQUIPPED_TECHNIQUE_BASE_MULT = 1.0;

    private PlayerQiAbsorptionHelper() {
    }

    public static boolean canAutoAbsorb(CultivationData data) {
        if (data == null) {
            return false;
        }
        if (!data.hasEquippedTechnique()) {
            return false;
        }
        if (PlayerQiAbsorptionHelper.cannotCultivate(data)) {
            return false;
        }
        return PlayerQiAbsorptionHelper.baseAbsorbMultiplier(data) > 0.0;
    }

    public static boolean canAutoAbsorb(ServerPlayer player, CultivationData data) {
        if (player == null || data == null) {
            return false;
        }
        if (!PlayerQiAbsorptionHelper.canAutoAbsorb(data)) {
            return false;
        }
        if (player.level().dimension() == ModDimensions.DIFU) {
            return false;
        }
        if (data.isSoulState()) {
            return false;
        }
        return !TechniqueLoadoutHelper.equippedTechniqueIsGhostDao(data);
    }

    public static double baseAbsorbMultiplier(CultivationData data) {
        if (data == null) {
            return 0.0;
        }
        if (!data.hasEquippedTechnique()) {
            return 0.0;
        }
        if (PlayerQiAbsorptionHelper.cannotCultivate(data)) {
            return 0.0;
        }
        int realmBase = data.getRealm().baseAbsorbMult();
        if (realmBase > 0) {
            return realmBase;
        }
        if (data.getRealm() == Realm.MORTAL) {
            return 1.0;
        }
        return 0.0;
    }

    private static boolean cannotCultivate(CultivationData data) {
        return !PhysiqueBonusHelper.canCultivate(data.getPhysique());
    }
}
