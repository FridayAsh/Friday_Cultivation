/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.RandomSource
 */
package com.friday.cultivation.entity.npc;

import com.friday.cultivation.cultivation.realm.Realm;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.util.RandomSource;

public final class CultivatorRealmRoller {
    private static final Map<Realm, Integer> WEIGHTS = new LinkedHashMap<Realm, Integer>();
    private static final int TOTAL_WEIGHT;

    private CultivatorRealmRoller() {
    }

    public static Realm roll(RandomSource random) {
        int roll = random.nextInt(TOTAL_WEIGHT);
        int acc = 0;
        for (Map.Entry<Realm, Integer> e : WEIGHTS.entrySet()) {
            if (roll >= (acc += e.getValue().intValue())) continue;
            return e.getKey();
        }
        return Realm.MORTAL;
    }

    static {
        int sum = 0;
        WEIGHTS.put(Realm.MORTAL, 1000);
        WEIGHTS.put(Realm.BODY_TEMPERING, 750);
        WEIGHTS.put(Realm.QI_REFINING, 500);
        WEIGHTS.put(Realm.FOUNDATION_BUILDING, 250);
        WEIGHTS.put(Realm.GOLDEN_CORE, 125);
        WEIGHTS.put(Realm.NASCENT_SOUL, 60);
        WEIGHTS.put(Realm.SOUL_FORMATION, 30);
        WEIGHTS.put(Realm.VOID_REFINING, 15);
        WEIGHTS.put(Realm.BODY_INTEGRATION, 8);
        WEIGHTS.put(Realm.MAHAYANA, 4);
        WEIGHTS.put(Realm.TRIBULATION_TRANSCENDENCE, 2);
        WEIGHTS.put(Realm.TRUE_IMMORTAL, 1);
        // 大帝：极稀有（权重仅 1），且受全存档上限 10 约束（finalizeSpawn 中拦截降级）
        WEIGHTS.put(Realm.GREAT_EMPEROR, 1);
        for (int w : WEIGHTS.values()) {
            sum += w;
        }
        TOTAL_WEIGHT = sum;
    }
}
