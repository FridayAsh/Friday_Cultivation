/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 */
package com.friday.cultivation.cultivation.qi.field;

import com.friday.cultivation.cultivation.qi.BlockQiSpec;
import com.friday.cultivation.cultivation.qi.field.QiModifier;
import net.minecraft.core.BlockPos;

public interface IQiFieldEffect {
    public BlockPos origin();

    public int radius();

    public boolean isActive();

    public QiModifier modifyAt(BlockPos var1, BlockQiSpec var2);
}

