package com.friday.cultivation.entity.npc;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.RandomSource;

/**
 * 修仙者姓名生成器（严格照搬原模组 com.xiaoxiang.cultivation.entity.npc.CultivatorNames）
 * 20个姓 + 20个名，组成400种修仙者名字
 */
public final class CultivatorNames {

    public static final int SURNAME_COUNT = 20;
    public static final int GIVEN_COUNT = 20;

    private CultivatorNames() {}

    public static int randomSurnameIdx(RandomSource random) {
        return random.nextInt(20);
    }

    public static int randomGivenIdx(RandomSource random) {
        return random.nextInt(20);
    }

    public static MutableComponent display(int surnameIdx, int givenIdx) {
        int s = Math.floorMod(surnameIdx, 20);
        int g = Math.floorMod(givenIdx, 20);
        return Component.translatable("cultivator.friday_cultivation.name.format",
                Component.translatable("cultivator.friday_cultivation.surname." + s),
                Component.translatable("cultivator.friday_cultivation.given." + g));
    }
}