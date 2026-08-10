/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.Block
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.cultivation.qi;

import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public record BlockUpgradeRule(int idleTicksRequired, @Nullable Block upgradeTo, double chancePerCheck) {
    public static BlockUpgradeRule of(int idleTicks, Block target, double chance) {
        return new BlockUpgradeRule(idleTicks, target, chance);
    }
}

