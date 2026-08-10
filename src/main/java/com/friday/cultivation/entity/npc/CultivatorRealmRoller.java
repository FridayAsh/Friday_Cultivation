/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.RandomSource
 */
package com.friday.cultivation.entity.npc;

import com.friday.cultivation.cultivation.realm.Realm;
import net.minecraft.util.RandomSource;

public final class CultivatorRealmRoller {
    private static final int[] WEIGHTS = new int[]{1000, 500, 250, 125, 60, 30, 15, 8, 4, 2, 1};
    private static final int TOTAL_WEIGHT;

    private CultivatorRealmRoller() {
    }

    public static Realm roll(RandomSource random) {
        int roll = random.nextInt(TOTAL_WEIGHT);
        int acc = 0;
        Realm[] vals = Realm.values();
        for (int i = 0; i < WEIGHTS.length && i < vals.length; ++i) {
            if (roll >= (acc += WEIGHTS[i])) continue;
            return vals[i];
        }
        return Realm.MORTAL;
    }

    static {
        int sum = 0;
        for (int w : WEIGHTS) {
            sum += w;
        }
        TOTAL_WEIGHT = sum;
    }
}

