/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.cultivation.qi;

import com.friday.cultivation.cultivation.QiElement;
import com.friday.cultivation.cultivation.qi.BlockDegradeRule;
import com.friday.cultivation.cultivation.qi.BlockUpgradeRule;
import org.jetbrains.annotations.Nullable;

public record BlockQiSpec(QiElement element, int baseMaxQi, double baseRegenPerSec, double baseEmitRate, @Nullable BlockDegradeRule degradeRule, @Nullable BlockUpgradeRule upgradeRule) {
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

