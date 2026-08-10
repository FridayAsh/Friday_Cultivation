/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.chunk.LevelChunk
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.cultivation.qi;

import com.friday.cultivation.cultivation.qi.BlockQiSpec;
import com.friday.cultivation.cultivation.qi.BlockQiSpecs;
import com.friday.cultivation.cultivation.qi.field.QiFieldRegistry;
import com.friday.cultivation.cultivation.qi.field.QiModifier;
import com.friday.cultivation.cultivation.qi.state.ChunkQiCapability;
import com.friday.cultivation.cultivation.qi.state.ChunkQiPool;
import com.friday.cultivation.cultivation.qi.state.DegradeFx;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

public final class QiEcosystem {
    private QiEcosystem() {
    }

    public static int tryDrainBlock(ServerLevel level, BlockPos pos, int amount) {
        BlockState state = level.getBlockState(pos);
        BlockQiSpec rawSpec = BlockQiSpecs.of(state);
        if (rawSpec == null) {
            return 0;
        }
        QiModifier modifier = QiFieldRegistry.of(level).composedModifierAt(pos, rawSpec);
        BlockQiSpec spec = modifier.applyTo(rawSpec);
        long now = level.getGameTime();
        Optional<ChunkQiPool> poolOpt = QiEcosystem.poolOf(level, pos);
        if (poolOpt.isEmpty()) {
            return 0;
        }
        ChunkQiPool.DrainResult result = poolOpt.get().tryDrain(pos, spec, amount, now, level.random);
        if (result.shouldDegrade()) {
            QiEcosystem.applyDegrade(level, pos, state, rawSpec);
        }
        return result.drained();
    }

    private static void applyDegrade(ServerLevel level, BlockPos pos, BlockState oldState, BlockQiSpec spec) {
        Block target = spec.degradeRule().degradeTo();
        BlockState newState = target != null ? target.defaultBlockState() : Blocks.AIR.defaultBlockState();
        level.setBlockAndUpdate(pos, newState);
        DegradeFx.play(level, pos, oldState, newState);
    }

    public static int peekBlock(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        BlockQiSpec rawSpec = BlockQiSpecs.of(state);
        if (rawSpec == null) {
            return 0;
        }
        QiModifier modifier = QiFieldRegistry.of(level).composedModifierAt(pos, rawSpec);
        BlockQiSpec spec = modifier.applyTo(rawSpec);
        long now = level.getGameTime();
        Optional<ChunkQiPool> poolOpt = QiEcosystem.poolOf(level, pos);
        if (poolOpt.isEmpty()) {
            return 0;
        }
        return poolOpt.get().peek(pos, spec, now);
    }

    public static int getMaxBlock(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        BlockQiSpec spec = BlockQiSpecs.of(state);
        return spec != null ? spec.baseMaxQi() : 0;
    }

    @Nullable
    public static BlockQiSpec specOf(ServerLevel level, BlockPos pos) {
        return BlockQiSpecs.of(level.getBlockState(pos));
    }

    public static Optional<ChunkQiPool> poolOf(ServerLevel level, BlockPos pos) {
        LevelChunk chunk = level.getChunkAt(pos);
        return ChunkQiCapability.get(chunk);
    }
}

