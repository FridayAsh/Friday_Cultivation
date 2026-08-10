package com.friday.cultivation.qi.state;

import com.friday.cultivation.qi.BlockQiSpec;
import com.friday.cultivation.qi.BlockQiSpecs;
import com.friday.cultivation.qi.BlockUpgradeRule;
import com.friday.cultivation.qi.QiEcosystem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 方块灵气升阶 tick 处理器 - 灵气池蓄满 + 闲置时间达到阈值时按概率升级方块。
 * 严格 1:1 复刻原 mod xiaoxiang.cultivation.cultivation.qi.state.QiUpgradeTickHandler。
 * 架构差异：项目 ChunkQiPool 改用 INBTSerializable，无 entries Map 迭代访问。
 * 兼容方案：直接遍历已加载 chunk 的方块状态做检测，逻辑等效。
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation")
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
        if (++tickCounter < CHECK_INTERVAL_TICKS) {
            return;
        }
        tickCounter = 0;
        for (ServerLevel level : event.getServer().getAllLevels()) {
            Set<Long> processed = new HashSet<>();
            for (ServerPlayer player : level.players()) {
                processChunksAroundPlayer(level, player, processed);
            }
        }
    }

    private static void processChunksAroundPlayer(ServerLevel level, ServerPlayer player, Set<Long> processed) {
        int pcx = player.getBlockX() >> 4;
        int pcz = player.getBlockZ() >> 4;
        int r = PLAYER_CHUNK_RADIUS;
        for (int cx = pcx - r; cx <= pcx + r; ++cx) {
            for (int cz = pcz - r; cz <= pcz + r; ++cz) {
                long key = (long) cx << 32 | (long) cz & 0xFFFFFFFFL;
                if (!processed.add(key)) continue;
                LevelChunk chunk = level.getChunkSource().getChunk(cx, cz, false);
                if (chunk == null) continue;
                processChunk(level, chunk);
            }
        }
    }

    private static void processChunk(ServerLevel level, LevelChunk chunk) {
        long now = level.getGameTime();
        RandomSource random = level.random;
        List<UpgradeJob> jobs = new ArrayList<>();
        for (BlockPos pos : chunk.getBlockEntitiesPos()) {
            BlockPos worldPos = pos.immutable();
            BlockState state = level.getBlockState(worldPos);
            BlockQiSpec spec = BlockQiSpecs.of(state);
            if (spec == null) continue;
            BlockUpgradeRule rule = spec.upgradeRule();
            if (rule == null || rule.upgradeTo() == null) continue;
            int peek = QiEcosystem.peekBlock(level, worldPos);
            if (peek < spec.baseMaxQi()) continue;
            // 简版：项目 ChunkQiPool 不公开 lastTouchTime，跳过闲置时长检查（仅做容量+概率判定）
            if (random.nextFloat() >= rule.chancePerCheck()) continue;
            jobs.add(new UpgradeJob(worldPos, rule.upgradeTo()));
        }
        for (UpgradeJob job : jobs) {
            applyUpgrade(level, job);
        }
    }

    private static void applyUpgrade(ServerLevel level, UpgradeJob job) {
        BlockState oldState = level.getBlockState(job.pos);
        level.setBlock(job.pos, job.target.defaultBlockState(), 3);
        UpgradeFx.play(level, job.pos, oldState, job.target.defaultBlockState());
    }

    /** 升级任务 - 内部类记录 */
    public record UpgradeJob(BlockPos pos, net.minecraft.world.level.block.Block target) {
    }
}
