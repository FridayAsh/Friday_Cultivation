package com.friday.cultivation.qi;

import com.friday.cultivation.qi.field.QiFieldRegistry;
import com.friday.cultivation.qi.field.QiModifier;
import com.friday.cultivation.qi.state.ChunkQiCapability;
import com.friday.cultivation.qi.state.ChunkQiPool;
import com.friday.cultivation.qi.state.DegradeFx;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * 灵气生态系统 - 协调方块灵气池的 drain/peek/max 查询与降阶。
 * 完全照搬原 mod: xiaoxiang.cultivation.cultivation.qi.QiEcosystem
 */
public final class QiEcosystem {
    private QiEcosystem() {
    }

    public static int tryDrainBlock(ServerLevel level, BlockPos pos, int amount) {
        BlockState state = level.getBlockState(pos);
        BlockQiSpec rawSpec = BlockQiSpecs.of(state);
        if (rawSpec == null) return 0;
        QiModifier modifier = QiFieldRegistry.of(level).composedModifierAt(pos, rawSpec);
        BlockQiSpec spec = modifier.applyTo(rawSpec);
        long now = level.getGameTime();
        Optional<ChunkQiPool> poolOpt = poolOf(level, pos);
        if (poolOpt.isEmpty()) return 0;
        ChunkQiPool.DrainResult result = poolOpt.get().tryDrain(pos, spec, amount, now, level.random);
        if (result.shouldDegrade()) applyDegrade(level, pos, state, rawSpec);
        return result.drained();
    }

    private static void applyDegrade(ServerLevel level, BlockPos pos, BlockState oldState, BlockQiSpec spec) {
        Block target = spec.degradeRule() != null ? spec.degradeRule().degradeTo() : null;
        BlockState newState = target != null ? target.defaultBlockState() : Blocks.AIR.defaultBlockState();
        level.setBlock(pos, newState, 3);
        DegradeFx.play(level, pos, oldState, newState);
    }

    public static int peekBlock(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        BlockQiSpec rawSpec = BlockQiSpecs.of(state);
        if (rawSpec == null) return 0;
        QiModifier modifier = QiFieldRegistry.of(level).composedModifierAt(pos, rawSpec);
        BlockQiSpec spec = modifier.applyTo(rawSpec);
        long now = level.getGameTime();
        Optional<ChunkQiPool> poolOpt = poolOf(level, pos);
        if (poolOpt.isEmpty()) return 0;
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
