package com.friday.cultivation.qi;

import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

/**
 * 方块降阶规则 - 灵气池耗尽总抽取量达到阈值后，方块按概率降阶。
 * 完全照搬原 mod: xiaoxiang.cultivation.cultivation.qi.BlockDegradeRule
 */
public record BlockDegradeRule(int drainThreshold, @Nullable Block degradeTo, double chancePerCheck) {
    public static BlockDegradeRule likely(int threshold, Block target) {
        return new BlockDegradeRule(threshold, target, 0.8);
    }

    public static BlockDegradeRule of(int threshold, Block target, double chance) {
        return new BlockDegradeRule(threshold, target, chance);
    }

    public static BlockDegradeRule to(int threshold, Block target, double chance) {
        return new BlockDegradeRule(threshold, target, chance);
    }
}
