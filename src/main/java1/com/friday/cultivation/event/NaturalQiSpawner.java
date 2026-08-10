package com.friday.cultivation.event;

import com.friday.cultivation.entity.spell.QiOrbEntity;
import com.friday.cultivation.qi.BlockQiSpec;
import com.friday.cultivation.qi.BlockQiSpecs;
import com.friday.cultivation.qi.QiEcosystem;
import com.friday.cultivation.qi.field.QiFieldRegistry;
import com.friday.cultivation.qi.field.QiModifier;
import com.friday.cultivation.registry.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * 自然灵气生成器 - 每 tick 在玩家周围采样生成灵气球。
 * 完全照搬原 mod: xiaoxiang.cultivation.event.NaturalQiSpawner
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation")
public final class NaturalQiSpawner {
    private static final int MAX_ORBS_NEAR_PLAYER = 48;
    private static final double NEARBY_RADIUS = 24.0;
    private static final int SAMPLES_PER_TICK = 40;
    private static final int SAMPLE_RADIUS_XZ = 7;
    private static final int SAMPLE_RADIUS_Y = 4;
    private static final double GLOBAL_EMIT_MULTIPLIER = 2.0;

    private NaturalQiSpawner() {
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        for (ServerLevel level : event.getServer().getAllLevels()) {
            for (ServerPlayer player : level.players()) {
                NaturalQiSpawner.tickPlayer(level, player);
            }
        }
    }

    public static void tickPlayer(ServerLevel level, ServerPlayer player) {
        NaturalQiSpawner.tickPlayer(level, player, 40);
    }

    public static void tickPlayerExtraTicks(ServerLevel level, ServerPlayer player, int extraTicks) {
        if (extraTicks <= 0) {
            return;
        }
        NaturalQiSpawner.tickPlayer(level, player, 40 * Math.min(99, extraTicks));
    }

    private static void tickPlayer(ServerLevel level, ServerPlayer player, int samples) {
        if (level.dimension() == ModDimensions.DIFU) {
            return;
        }
        int nearbyOrbs = NaturalQiSpawner.countNearbyOrbs(level, player);
        if (nearbyOrbs >= 48) {
            return;
        }
        BlockPos.MutableBlockPos cur = new BlockPos.MutableBlockPos();
        for (int i = 0; i < Math.max(0, samples); ++i) {
            int dx = level.random.nextInt(15) - 7;
            int dy = level.random.nextInt(9) - 4;
            int dz = level.random.nextInt(15) - 7;
            cur.set(player.getBlockX() + dx, player.getBlockY() + dy, player.getBlockZ() + dz);
            BlockState state = level.getBlockState(cur);
            BlockQiSpec rawSpec = BlockQiSpecs.of(state);
            if (rawSpec == null) continue;
            QiModifier modifier = QiFieldRegistry.of(level).composedModifierAt(cur, rawSpec);
            BlockQiSpec spec = modifier.applyTo(rawSpec);
            double perTickChance = spec.baseEmitRate() * 2.0 / 20.0;
            BlockPos firmPos = cur.immutable();
            if (level.random.nextDouble() >= perTickChance || SpiritLockHandler.isBlockLocked(level, firmPos)) continue;
            int drained = QiEcosystem.tryDrainBlock(level, firmPos, 1);
            if (drained <= 0) continue;
            NaturalQiSpawner.spawnOrbFromBlock(level, firmPos, state, spec);
            if (++nearbyOrbs < 48) continue;
            return;
        }
    }

    private static void spawnOrbFromBlock(ServerLevel level, BlockPos blockPos, BlockState state, BlockQiSpec spec) {
        BlockPos above = blockPos.above();
        BlockState aboveState = level.getBlockState(above);
        double sx;
        double sy;
        double sz;
        if (aboveState.getCollisionShape(level, above).isEmpty()) {
            sx = (double) blockPos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.4;
            sy = (double) blockPos.getY() + 1.0 + level.random.nextDouble() * 0.4;
            sz = (double) blockPos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.4;
        } else if (state.getCollisionShape(level, blockPos).isEmpty()) {
            sx = (double) blockPos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.4;
            sy = (double) blockPos.getY() + 0.4 + level.random.nextDouble() * 0.4;
            sz = (double) blockPos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.4;
        } else {
            return;
        }
        QiOrbEntity orb = new QiOrbEntity(level, sx, sy, sz, spec.element());
        level.addFreshEntity(orb);
    }

    private static int countNearbyOrbs(ServerLevel level, ServerPlayer player) {
        List<QiOrbEntity> nearby = level.getEntitiesOfClass(QiOrbEntity.class, player.getBoundingBox().inflate(24.0));
        return nearby.size();
    }
}
