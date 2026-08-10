/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.state.BlockState
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.cultivation;

import com.friday.cultivation.cultivation.QiElement;
import com.friday.cultivation.cultivation.qi.BlockQiSpecs;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@Deprecated(forRemoval=false)
public final class BlockQiMapping {
    private BlockQiMapping() {
    }

    @Nullable
    public static QiElement of(BlockState state) {
        return BlockQiSpecs.elementOf(state);
    }
}

