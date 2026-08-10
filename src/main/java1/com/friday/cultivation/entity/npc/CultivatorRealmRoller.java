package com.friday.cultivation.entity.npc;

import com.friday.cultivation.realm.Realm;
import net.minecraft.util.RandomSource;

/**
 * 修仙者境界权重表（严格照搬原模组 com.xiaoxiang.cultivation.entity.npc.CultivatorRealmRoller）
 * 权重：[1000, 500, 250, 125, 60, 30, 15, 8, 4, 2, 1]
 */
public final class CultivatorRealmRoller {
    private static final int[] WEIGHTS = new int[]{1000, 500, 250, 125, 60, 30, 15, 8, 4, 2, 1};
    private static final int TOTAL_WEIGHT;

    static {
        int sum = 0;
        for (int w : WEIGHTS) {
            sum += w;
        }
        TOTAL_WEIGHT = sum;
    }

    private CultivatorRealmRoller() {}

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
}