/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.util.RandomSource
 */
package com.friday.cultivation.entity.npc;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.RandomSource;

public final class CultivatorNames {
    public static final int SURNAME_COUNT = 20;
    public static final int GIVEN_COUNT = 20;

    private CultivatorNames() {
    }

    public static int randomSurnameIdx(RandomSource random) {
        return random.nextInt(20);
    }

    public static int randomGivenIdx(RandomSource random) {
        return random.nextInt(20);
    }

    public static MutableComponent display(int surnameIdx, int givenIdx) {
        int s = Math.floorMod(surnameIdx, 20);
        int g = Math.floorMod(givenIdx, 20);
        return Component.translatable((String)"cultivator.friday_cultivation.name.format", (Object[])new Object[]{Component.translatable((String)("cultivator.friday_cultivation.surname." + s)), Component.translatable((String)("cultivator.friday_cultivation.given." + g))});
    }
}

