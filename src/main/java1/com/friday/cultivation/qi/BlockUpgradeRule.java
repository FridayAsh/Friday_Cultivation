package com.friday.cultivation.qi;

import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

/**
 * 方块升阶规则 - 灵气池蓄满后闲置若干 tick 按概率升阶。
 * 完全照搬原 mod: xiaoxiang.cultivation.cultivation.qi.BlockUpgradeRule
 */
public record BlockUpgradeRule(int idleTicksRequired, @Nullable Block upgradeTo, double chancePerCheck) {
    public static BlockUpgradeRule of(int idleTicks, Block target, double chance) {
        return new BlockUpgradeRule(idleTicks, target, chance);
    }

    public static BlockUpgradeRule to(int idle, Block target, double chance) {
        return new BlockUpgradeRule(idle, target, chance);
    }
}
