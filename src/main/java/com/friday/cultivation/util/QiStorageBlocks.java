/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.util;

import com.friday.cultivation.block.alchemy.AlchemyCoreBlockEntity;
import com.friday.cultivation.block.formation.FormationCorePlateBlockEntity;
import com.friday.cultivation.block.refining.RefiningCoreBlockEntity;
import com.friday.cultivation.block.spirit.SpiritVeinCoreBlockEntity;
import com.friday.cultivation.event.SpiritLockHandler;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public final class QiStorageBlocks {
    private QiStorageBlocks() {
    }

    public static boolean isUnlockedSpiritVeinCore(@Nullable BlockEntity be) {
        if (!(be instanceof SpiritVeinCoreBlockEntity)) {
            return false;
        }
        SpiritVeinCoreBlockEntity core = (SpiritVeinCoreBlockEntity)be;
        Level level = core.getLevel();
        return level != null && !SpiritLockHandler.isBlockLocked(level, core.getBlockPos());
    }

    public static boolean isUnlockedStorageTarget(@Nullable BlockEntity be) {
        if (be instanceof AlchemyCoreBlockEntity) {
            AlchemyCoreBlockEntity core = (AlchemyCoreBlockEntity)be;
            Level level = core.getLevel();
            return level != null && !SpiritLockHandler.isBlockLocked(level, core.getBlockPos());
        }
        if (be instanceof RefiningCoreBlockEntity) {
            RefiningCoreBlockEntity core = (RefiningCoreBlockEntity)be;
            Level level = core.getLevel();
            return level != null && !SpiritLockHandler.isBlockLocked(level, core.getBlockPos());
        }
        if (be instanceof FormationCorePlateBlockEntity) {
            FormationCorePlateBlockEntity core = (FormationCorePlateBlockEntity)be;
            Level level = core.getLevel();
            return level != null && !SpiritLockHandler.isBlockLocked(level, core.getBlockPos());
        }
        return false;
    }
}

