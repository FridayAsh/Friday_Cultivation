/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.chunk.LevelChunk
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$ServerTickEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package com.friday.cultivation.cultivation.qi.state;

import com.friday.cultivation.cultivation.qi.BlockQiSpec;
import com.friday.cultivation.cultivation.qi.BlockQiSpecs;
import com.friday.cultivation.cultivation.qi.BlockUpgradeRule;
import com.friday.cultivation.cultivation.qi.state.BlockQiState;
import com.friday.cultivation.cultivation.qi.state.ChunkQiCapability;
import com.friday.cultivation.cultivation.qi.state.ChunkQiPool;
import com.friday.cultivation.cultivation.qi.state.UpgradeFx;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class QiUpgradeTickHandler {
    private static final int CHECK_INTERVAL_TICKS = 20;
    private static final int PLAYER_CHUNK_RADIUS = 4;
    private static int tickCounter = 0;

    private QiUpgradeTickHandler() {
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (++tickCounter < 20) {
            return;
        }
        tickCounter = 0;
        for (ServerLevel level : event.getServer().getAllLevels()) {
            HashSet<Long> processed = new HashSet<Long>();
            for (ServerPlayer player : level.players()) {
                QiUpgradeTickHandler.processChunksAroundPlayer(level, player, processed);
            }
        }
    }

    private static void processChunksAroundPlayer(ServerLevel level, ServerPlayer player, Set<Long> processed) {
        int pcx = player.getBlockX() >> 4;
        int pcz = player.getBlockZ() >> 4;
        int r = 4;
        for (int cx = pcx - r; cx <= pcx + r; ++cx) {
            for (int cz = pcz - r; cz <= pcz + r; ++cz) {
                LevelChunk chunk;
                long key = (long)cx << 32 | (long)cz & 0xFFFFFFFFL;
                if (!processed.add(key) || (chunk = level.getChunkSource().getChunkNow(cx, cz)) == null) continue;
                ChunkQiCapability.get(chunk).ifPresent(pool -> QiUpgradeTickHandler.processPool(level, pool));
            }
        }
    }

    private static void processPool(ServerLevel level, ChunkQiPool pool) {
        long now = level.getGameTime();
        ArrayList<UpgradeJob> jobs = new ArrayList<UpgradeJob>();
        Iterator<Map.Entry<Long, BlockQiState>> it = pool.iterator();
        while (it.hasNext()) {
            BlockUpgradeRule rule;
            Map.Entry<Long, BlockQiState> entry = it.next();
            BlockQiState st = entry.getValue();
            BlockPos pos = BlockPos.of((long)entry.getKey());
            BlockState state = level.getBlockState(pos);
            BlockQiSpec spec = BlockQiSpecs.of(state);
            if (spec == null || (rule = spec.upgradeRule()) == null || rule.upgradeTo() == null || st.currentQi < spec.baseMaxQi() || now - st.lastTouchTime < (long)rule.idleTicksRequired() || level.random.nextDouble() >= rule.chancePerCheck()) continue;
            jobs.add(new UpgradeJob(pos, rule.upgradeTo()));
        }
        for (UpgradeJob job : jobs) {
            QiUpgradeTickHandler.applyUpgrade(level, pool, job);
        }
    }

    private static void applyUpgrade(ServerLevel level, ChunkQiPool pool, UpgradeJob job) {
        BlockState newState = job.target.defaultBlockState();
        BlockState oldState = level.getBlockState(job.pos);
        level.setBlockAndUpdate(job.pos, newState);
        pool.removeEntry(job.pos);
        UpgradeFx.play(level, job.pos, oldState, newState);
    }

    private record UpgradeJob(BlockPos pos, Block target) {
    }
}

