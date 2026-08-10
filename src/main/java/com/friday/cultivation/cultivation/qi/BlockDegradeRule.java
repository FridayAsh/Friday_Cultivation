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

public record BlockDegradeRule(int drainThreshold, @Nullable Block degradeTo, double chancePerCheck) {
    public static BlockDegradeRule likely(int threshold, Block target) {
        return new BlockDegradeRule(threshold, target, 0.8);
    }

    public static BlockDegradeRule of(int threshold, Block target, double chance) {
        return new BlockDegradeRule(threshold, target, chance);
    }
}

