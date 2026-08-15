/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$ServerTickEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package com.friday.cultivation.event;

import com.friday.cultivation.cultivation.QiElement;
import com.friday.cultivation.cultivation.qi.BlockQiSpec;
import com.friday.cultivation.cultivation.qi.BlockQiSpecs;
import com.friday.cultivation.cultivation.qi.QiEcosystem;
import com.friday.cultivation.cultivation.qi.field.QiFieldRegistry;
import com.friday.cultivation.cultivation.qi.field.QiModifier;
import com.friday.cultivation.entity.QiOrbEntity;
import com.friday.cultivation.event.SpiritLockHandler;
import com.friday.cultivation.registry.ModDimensions;
import java.util.List;
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

@Mod.EventBusSubscriber(modid="friday_cultivation")
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
        // 环境灵气：不依赖任何方块，任何位置（空中/水中/洞穴）都持续生成，保证随时随地可修炼
        if (level.random.nextDouble() < 0.08) {
            double ax = (double)player.getX() + (level.random.nextDouble() - 0.5) * 8.0;
            double ay = (double)player.getY() + (level.random.nextDouble() - 0.5) * 4.0;
            double az = (double)player.getZ() + (level.random.nextDouble() - 0.5) * 8.0;
            QiOrbEntity ambientOrb = new QiOrbEntity((Level)level, ax, ay, az, QiElement.PURE);
            level.addFreshEntity((Entity)ambientOrb);
            if (++nearbyOrbs >= 48) {
                return;
            }
        }
        BlockPos.MutableBlockPos cur = new BlockPos.MutableBlockPos();
        for (int i = 0; i < Math.max(0, samples); ++i) {
            int drained;
            BlockPos firmPos;
            int dx = level.random.nextInt(15) - 7;
            int dy = level.random.nextInt(9) - 4;
            int dz = level.random.nextInt(15) - 7;
            cur.set(player.getBlockX() + dx, player.getBlockY() + dy, player.getBlockZ() + dz);
            BlockState state = level.getBlockState((BlockPos)cur);
            BlockQiSpec rawSpec = BlockQiSpecs.of(state);
            if (rawSpec == null) continue;
            QiModifier modifier = QiFieldRegistry.of(level).composedModifierAt((BlockPos)cur, rawSpec);
            BlockQiSpec spec = modifier.applyTo(rawSpec);
            double perTickChance = spec.baseEmitRate() * 2.0 / 20.0;
            if (level.random.nextDouble() >= perTickChance || SpiritLockHandler.isBlockLocked((Level)level, firmPos = cur.east()) || (drained = QiEcosystem.tryDrainBlock(level, firmPos, 1)) <= 0) continue;
            NaturalQiSpawner.spawnOrbFromBlock(level, firmPos, state, spec);
            if (++nearbyOrbs < 48) continue;
            return;
        }
    }

    private static void spawnOrbFromBlock(ServerLevel level, BlockPos blockPos, BlockState state, BlockQiSpec spec) {
        double sz;
        double sy;
        double sx;
        BlockPos above = blockPos.above();
        BlockState aboveState = level.getBlockState(above);
        if (aboveState.getCollisionShape(level, above).isEmpty()) {
            sx = (double)blockPos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.4;
            sy = (double)blockPos.getY() + 1.0 + level.random.nextDouble() * 0.4;
            sz = (double)blockPos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.4;
        } else if (state.getCollisionShape(level, blockPos).isEmpty()) {
            sx = (double)blockPos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.4;
            sy = (double)blockPos.getY() + 0.4 + level.random.nextDouble() * 0.4;
            sz = (double)blockPos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.4;
        } else {
            return;
        }
        QiOrbEntity orb = new QiOrbEntity((Level)level, sx, sy, sz, spec.element());
        level.addFreshEntity((Entity)orb);
    }

    private static int countNearbyOrbs(ServerLevel level, ServerPlayer player) {
        List nearby = level.getEntitiesOfClass(QiOrbEntity.class, player.getBoundingBox().inflate(24.0));
        return nearby.size();
    }
}

