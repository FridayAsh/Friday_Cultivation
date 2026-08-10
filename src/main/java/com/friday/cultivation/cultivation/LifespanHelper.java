/*
 * Decompiled with CFR 0.152.
 */
package com.friday.cultivation.cultivation;

import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.realm.Realm;

public final class LifespanHelper {
    public static final int START_BONE_AGE_MIN = 14;
    public static final int START_BONE_AGE_MAX = 18;
    public static final double AGE_PER_DAY = 1.0;
    public static final double AGE_PER_DAY_MEDITATING = 1.0;
    public static final long TICKS_PER_DAY = 24000L;
    public static final int NEAR_IMMORTAL_THRESHOLD = 1000000;

    private LifespanHelper() {
    }

    public static int lifespanCap(CultivationData d) {
        if (d == null) {
            return 0;
        }
        Realm realm = d.getRealm();
        if (realm == Realm.MORTAL) {
            return Math.max(1, d.getMortalLifespan());
        }
        int base = realm.baseLifespan();
        if (realm.ordinal() >= Realm.FOUNDATION_BUILDING.ordinal()) {
            base += d.getFoundationDao().lifespanBonus();
        }
        if (realm.ordinal() >= Realm.GOLDEN_CORE.ordinal()) {
            base += d.getGoldenCoreDao().lifespanBonus();
        }
        return base;
    }

    public static int displayBoneAge(CultivationData d) {
        return d == null ? 0 : (int)Math.floor(d.getBoneAge());
    }

    public static boolean isNearImmortal(CultivationData d) {
        return LifespanHelper.lifespanCap(d) >= 1000000;
    }

    public static boolean isExhausted(CultivationData d) {
        if (d == null || !d.hasChosenIdentity()) {
            return false;
        }
        if (LifespanHelper.isNearImmortal(d)) {
            return false;
        }
        return LifespanHelper.displayBoneAge(d) >= LifespanHelper.lifespanCap(d);
    }
}

