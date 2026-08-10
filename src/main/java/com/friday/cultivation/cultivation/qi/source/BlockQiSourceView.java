/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerLevel
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.cultivation.qi.source;

import com.friday.cultivation.cultivation.QiElement;
import com.friday.cultivation.cultivation.qi.BlockQiSpec;
import com.friday.cultivation.cultivation.qi.BlockQiSpecs;
import com.friday.cultivation.cultivation.qi.IQiSource;
import com.friday.cultivation.cultivation.qi.QiEcosystem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

public final class BlockQiSourceView
implements IQiSource {
    private final ServerLevel level;
    private final BlockPos pos;
    private final QiElement element;

    private BlockQiSourceView(ServerLevel level, BlockPos pos, QiElement element) {
        this.level = level;
        this.pos = pos;
        this.element = element;
    }

    @Nullable
    public static BlockQiSourceView create(ServerLevel level, BlockPos pos) {
        BlockQiSpec spec = BlockQiSpecs.of(level.getBlockState(pos));
        if (spec == null) {
            return null;
        }
        return new BlockQiSourceView(level, pos.east(), spec.element());
    }

    @Override
    public QiElement element() {
        return this.element;
    }

    @Override
    public int peek() {
        return QiEcosystem.peekBlock(this.level, this.pos);
    }

    @Override
    public int extract(int amount) {
        return QiEcosystem.tryDrainBlock(this.level, this.pos, amount);
    }

    public BlockPos pos() {
        return this.pos;
    }
}

