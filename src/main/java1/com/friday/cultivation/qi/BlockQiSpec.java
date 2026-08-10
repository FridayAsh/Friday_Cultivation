package com.friday.cultivation.qi;

import com.friday.cultivation.QiElement;
import org.jetbrains.annotations.Nullable;

/**
 * 方块灵气规格 - 描述方块最大灵气/恢复速率/发出速率/降阶/升阶规则。
 * 完全照搬原 mod: xiaoxiang.cultivation.cultivation.qi.BlockQiSpec
 */
public record BlockQiSpec(QiElement element, int baseMaxQi, double baseRegenPerSec, double baseEmitRate,
                          @Nullable BlockDegradeRule degradeRule, @Nullable BlockUpgradeRule upgradeRule) {

    public static BlockQiSpec of(QiElement element, int maxQi, double regenPerSec, double emitRate) {
        return new BlockQiSpec(element, maxQi, regenPerSec, emitRate, null, null);
    }

    public static BlockQiSpec ofWithDegrade(QiElement element, int maxQi, double regenPerSec, double emitRate, BlockDegradeRule degradeRule) {
        return new BlockQiSpec(element, maxQi, regenPerSec, emitRate, degradeRule, null);
    }

    public static BlockQiSpec ofWithUpgrade(QiElement element, int maxQi, double regenPerSec, double emitRate, BlockUpgradeRule upgradeRule) {
        return new BlockQiSpec(element, maxQi, regenPerSec, emitRate, null, upgradeRule);
    }

    public static BlockQiSpec ofFull(QiElement element, int maxQi, double regenPerSec, double emitRate, BlockDegradeRule degradeRule, BlockUpgradeRule upgradeRule) {
        return new BlockQiSpec(element, maxQi, regenPerSec, emitRate, degradeRule, upgradeRule);
    }
}
