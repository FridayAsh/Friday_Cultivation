/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 */
package com.friday.cultivation.cultivation.qi;

import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.PhysiqueBonusHelper;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.cultivation.technique.TechniqueLoadoutHelper;
import com.friday.cultivation.registry.ModDimensions;
import net.minecraft.server.level.ServerPlayer;

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

