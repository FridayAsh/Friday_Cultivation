package com.friday.cultivation.event;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.QiElement;
import com.friday.cultivation.block.alchemy.AlchemyCoreBlockEntity;
import com.friday.cultivation.block.refining.RefiningCoreBlockEntity;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.dao.GoldenCoreDaoBonusHelper;
import com.friday.cultivation.entity.SeatEntity;
import com.friday.cultivation.entity.spell.QiOrbEntity;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.SyncCultivationDataPacket;
import com.friday.cultivation.qi.consumer.PlayerQiConsumer;
import com.friday.cultivation.registry.ModDimensions;
import com.friday.cultivation.technique.TechniqueBonusHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

/**
 * 时间加速事件 handler — 完整复刻原模组 com.xiaoxiang.cultivation.event.TimeAccelerationHandler。
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation")
public final class TimeAccelerationHandler {
    public static final int RADIUS = 10;
    private static final int SINGLEPLAYER_RANDOM_TICK_RADIUS = 64;
    private static final int RANDOM_TICK_SAMPLES_PER_EXTRA_TICK = 3;
    private static final int CROP_RANDOM_TICK_HORIZONTAL_RADIUS_CAP = 32;
    private static final int CROP_RANDOM_TICK_VERTICAL_SCAN = 6;
    private static final int CROP_RANDOM_TICK_XZ_SAMPLES_PER_EXTRA_TICK = 8;
    private static final int CROP_RANDOM_TICK_MIN_XZ_SAMPLES = 16;
    private static final int CROP_RANDOM_TICK_MAX_XZ_SAMPLES = 1536;
    private static final int MAX_SIMULATION_EXTRA_TICKS = 99;

    private TimeAccelerationHandler() {
    }

    public static boolean isAllowedMultiplier(int multiplier) {
        return CultivationData.isAllowedTimeAccelerationMultiplier(multiplier);
    }

    public static void start(ServerPlayer player, CultivationData data, int multiplier) {
        if (player == null || data == null) {
            return;
        }
        if (!data.canUseTimeAcceleration()) {
            player.sendSystemMessage(Component.translatable("message.friday_cultivation.time_acceleration.requires_foundation"), true);
            return;
        }
        if (!TimeAccelerationHandler.isSittingOnCushion(player)) {
            player.sendSystemMessage(Component.translatable("message.friday_cultivation.time_acceleration.requires_cushion"), true);
            return;
        }
        if (!TimeAccelerationHandler.isAllowedMultiplier(multiplier)) {
            player.sendSystemMessage(Component.translatable("message.friday_cultivation.time_acceleration.invalid"), true);
            return;
        }
        data.startTimeAcceleration(multiplier);
        TimeAccelerationHandler.sync(player, data);
        player.sendSystemMessage(Component.translatable("message.friday_cultivation.time_acceleration.started", multiplier), true);
    }

    public static void stop(ServerPlayer player, CultivationData data, boolean notify) {
        if (player == null || data == null) {
            return;
        }
        if (!data.isTimeAccelerationActive()) {
            return;
        }
        data.stopTimeAcceleration();
        TimeAccelerationHandler.sync(player, data);
        if (notify) {
            player.sendSystemMessage(Component.translatable("message.friday_cultivation.time_acceleration.stopped"), true);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        MinecraftServer server = event.getServer();
        if (server == null) {
            return;
        }
        ArrayList<ActiveTimeAcceleration> active = new ArrayList<ActiveTimeAcceleration>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            CultivationData data = CultivationCapability.get(player).orElse(null);
            if (data == null || !data.isTimeAccelerationActive()) continue;
            if (!data.canUseTimeAcceleration() || !TimeAccelerationHandler.isSittingOnCushion(player) || player.isRemoved() || player.isSpectator()) {
                TimeAccelerationHandler.stop(player, data, true);
                continue;
            }
            int multiplier = data.getTimeAccelerationMultiplier();
            if (!TimeAccelerationHandler.isAllowedMultiplier(multiplier)) {
                TimeAccelerationHandler.stop(player, data, false);
                continue;
            }
            active.add(new ActiveTimeAcceleration(player, data, multiplier));
        }
        if (active.isEmpty()) {
            return;
        }
        active.sort(Comparator.comparingInt(ActiveTimeAcceleration::multiplier).reversed());
        boolean globalSingleplayer = server.isSingleplayer() && server.getPlayerList().getPlayerCount() <= 1 && active.size() == 1;
        Set<Entity> tickedEntities = Collections.newSetFromMap(new IdentityHashMap<>());
        Set<BlockEntity> tickedBlockEntities = Collections.newSetFromMap(new IdentityHashMap<>());
        if (globalSingleplayer) {
            ActiveTimeAcceleration state = active.get(0);
            int playerExtraTicks = TimeAccelerationHandler.playerTimerExtraTicks(state.multiplier());
            int simulationExtraTicks = TimeAccelerationHandler.simulationExtraTicks(state.multiplier());
            TimeAccelerationHandler.applyPlayerTime(state, playerExtraTicks);
            TimeAccelerationHandler.accelerateNaturalQi(state.player().serverLevel(), state.player(), simulationExtraTicks);
            for (ServerLevel level : server.getAllLevels()) {
                if (level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
                    level.setDayTime(level.getDayTime() + (long)playerExtraTicks);
                }
                TimeAccelerationHandler.accelerateEntities(level, state.player(), simulationExtraTicks, true, tickedEntities);
                TimeAccelerationHandler.accelerateCraftingCores(level, state.player().blockPosition(), simulationExtraTicks, true, tickedBlockEntities);
            }
            TimeAccelerationHandler.accelerateNearbyQiOrbs(state.player().serverLevel(), state.player(), simulationExtraTicks, tickedEntities);
            TimeAccelerationHandler.accelerateRandomTicks(state.player().serverLevel(), state.player().blockPosition(), 64, simulationExtraTicks);
        } else {
            for (ActiveTimeAcceleration state : active) {
                int playerExtraTicks = TimeAccelerationHandler.playerTimerExtraTicks(state.multiplier());
                int simulationExtraTicks = TimeAccelerationHandler.simulationExtraTicks(state.multiplier());
                TimeAccelerationHandler.applyPlayerTime(state, playerExtraTicks);
                ServerLevel level = state.player().serverLevel();
                BlockPos center = state.player().blockPosition();
                TimeAccelerationHandler.accelerateNaturalQi(level, state.player(), simulationExtraTicks);
                TimeAccelerationHandler.accelerateEntities(level, state.player(), simulationExtraTicks, false, tickedEntities);
                TimeAccelerationHandler.accelerateNearbyQiOrbs(level, state.player(), simulationExtraTicks, tickedEntities);
                TimeAccelerationHandler.accelerateCraftingCores(level, center, simulationExtraTicks, false, tickedBlockEntities);
                TimeAccelerationHandler.accelerateRandomTicks(level, center, 10, simulationExtraTicks);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer player2) {
            CultivationCapability.get(player2).ifPresent(CultivationData::stopTimeAcceleration);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer player2) {
            CultivationCapability.get(player2).ifPresent(CultivationData::stopTimeAcceleration);
        }
    }

    private static int simulationExtraTicks(int multiplier) {
        return Math.min(99, Math.max(0, multiplier - 1));
    }

    private static int playerTimerExtraTicks(int multiplier) {
        return Math.min(9999, Math.max(0, multiplier - 1));
    }

    private static void applyPlayerTime(ActiveTimeAcceleration state, int playerExtraTicks) {
        ServerPlayer player = state.player();
        CultivationData data = state.data();
        data.tickTimeAccelerationElapsed(state.multiplier());
        boolean playerTimeChanged = TimeAccelerationHandler.acceleratePlayerCultivationTimers(player, data, playerExtraTicks);
        if (player.tickCount % 20 == 0) {
            TimeAccelerationHandler.accelerateQiRecovery(player, data, playerExtraTicks);
        }
        if (playerTimeChanged || player.tickCount % 20 == 0) {
            TimeAccelerationHandler.sync(player, data);
        }
    }

    private static boolean acceleratePlayerCultivationTimers(ServerPlayer player, CultivationData data, int extraTicks) {
        if (extraTicks <= 0) {
            return false;
        }
        boolean visibleAgeChanged = LifespanHandler.advanceBoneAge(player, data, (long)extraTicks);
        data.advanceLooseImmortalTribulationCountdown(player.serverLevel().getGameTime(), extraTicks);
        if (data.isSoulState()) {
            data.addSoulTicks(extraTicks);
            if (player.level().dimension() == ModDimensions.DIFU) {
                data.addDifuTicks(extraTicks);
            }
        }
        return visibleAgeChanged;
    }

    private static void accelerateQiRecovery(ServerPlayer player, CultivationData data, int extraTicks) {
        if (extraTicks <= 0) {
            return;
        }
        long recovery = PlayerQiConsumer.nominalQiRecoveryPerSecond(player, data, QiElement.PURE);
        recovery = GoldenCoreDaoBonusHelper.applyQiRecoveryMultiplier(player, data, recovery);
        if (recovery <= 0L) {
            return;
        }
        long before = data.getCurrentQi();
        if (before >= data.getMaxQi()) {
            return;
        }
        data.setCurrentQi(TimeAccelerationHandler.saturatedAdd(before, TimeAccelerationHandler.saturatedMultiply(recovery, extraTicks)));
    }

    private static long saturatedMultiply(long value, int multiplier) {
        if (value <= 0L || multiplier <= 0) {
            return 0L;
        }
        return value > Long.MAX_VALUE / (long)multiplier ? Long.MAX_VALUE : value * (long)multiplier;
    }

    private static long saturatedAdd(long left, long right) {
        if (right <= 0L) {
            return left;
        }
        return left > Long.MAX_VALUE - right ? Long.MAX_VALUE : left + right;
    }

    private static void accelerateNaturalQi(ServerLevel level, ServerPlayer player, int extraTicks) {
        if (extraTicks <= 0) {
            return;
        }
        NaturalQiSpawner.tickPlayerExtraTicks(level, player, extraTicks);
    }

    private static void accelerateEntities(ServerLevel level, ServerPlayer source, int extraTicks, boolean global, Set<Entity> tickedEntities) {
        if (extraTicks <= 0) {
            return;
        }
        ArrayList<Entity> entities = new ArrayList<Entity>();
        if (global) {
            for (Entity entity2 : level.getAllEntities()) {
                entities.add(entity2);
            }
        } else {
            AABB box = new AABB(source.blockPosition()).inflate(10.0);
            entities.addAll(level.getEntities((Entity)null, box, entity -> !(entity instanceof Player)));
        }
        double radiusSqr = 100.0;
        for (Entity entity3 : entities) {
            if (entity3 == null || entity3 instanceof Player || entity3.isRemoved() || entity3.level() != level || !global && entity3.distanceToSqr(source) > radiusSqr || !tickedEntities.add(entity3)) continue;
            for (int i = 0; i < extraTicks && !entity3.isRemoved(); ++i) {
                entity3.tick();
            }
        }
    }

    private static void accelerateCraftingCores(ServerLevel level, BlockPos center, int extraTicks, boolean global, Set<BlockEntity> tickedBlockEntities) {
        if (extraTicks <= 0) {
            return;
        }
        double radiusSqr = 100.0;
        AlchemyCoreBlockEntity.forLoaded(level, core -> {
            if (!global && core.getBlockPos().distSqr((Vec3i)center) > radiusSqr) {
                return;
            }
            if (!tickedBlockEntities.add((BlockEntity)core)) {
                return;
            }
            for (int i = 0; i < extraTicks; ++i) {
                core.serverTick();
            }
        });
        RefiningCoreBlockEntity.forLoaded(level, core -> {
            if (!global && core.getBlockPos().distSqr((Vec3i)center) > radiusSqr) {
                return;
            }
            if (!tickedBlockEntities.add((BlockEntity)core)) {
                return;
            }
            for (int i = 0; i < extraTicks; ++i) {
                core.serverTick();
            }
        });
    }

    private static void accelerateNearbyQiOrbs(ServerLevel level, ServerPlayer source, int extraTicks, Set<Entity> tickedEntities) {
        if (extraTicks <= 0) {
            return;
        }
        double radius = Math.max(10.0, 24.0 + (double)TechniqueBonusHelper.qiAbsorbRangeBonus(source));
        double radiusSqr = radius * radius;
        AABB box = source.getBoundingBox().inflate(radius);
        for (QiOrbEntity orb : level.getEntitiesOfClass(QiOrbEntity.class, box)) {
            if (orb == null || orb.isRemoved() || orb.level() != level || orb.distanceToSqr(source) > radiusSqr || !tickedEntities.add(orb)) continue;
            for (int i = 0; i < extraTicks && !orb.isRemoved(); ++i) {
                orb.tick();
            }
        }
    }

    private static void accelerateRandomTicks(ServerLevel level, BlockPos center, int radius, int extraTicks) {
        if (extraTicks <= 0) {
            return;
        }
        int samples = extraTicks * 3;
        int diameter = radius * 2 + 1;
        int minY = level.getMinBuildHeight();
        int maxY = level.getHeight() - 1;
        HashSet<BlockPos> sampled = new HashSet<BlockPos>();
        TimeAccelerationHandler.accelerateCropRandomTicks(level, center, radius, extraTicks, sampled);
        for (int i = 0; i < samples; ++i) {
            int x = center.getX() + level.random.nextInt(diameter) - radius;
            int y = Math.max(minY, Math.min(maxY, center.getY() + level.random.nextInt(diameter) - radius));
            int z = center.getZ() + level.random.nextInt(diameter) - radius;
            BlockPos pos = new BlockPos(x, y, z);
            if (!sampled.add(pos) || !level.hasChunkAt(pos)) continue;
            BlockState state = level.getBlockState(pos);
            if (!state.isRandomlyTicking()) continue;
            state.randomTick(level, pos, level.random);
        }
    }

    private static void accelerateCropRandomTicks(ServerLevel level, BlockPos center, int radius, int extraTicks, Set<BlockPos> sampled) {
        int horizontalRadius = Math.max(1, Math.min(radius, 32));
        int diameter = horizontalRadius * 2 + 1;
        long radiusSqr = (long)horizontalRadius * (long)horizontalRadius;
        int minY = Math.max(level.getMinBuildHeight(), center.getY() - 6);
        int maxY = Math.min(level.getHeight() - 1, center.getY() + 6);
        int xzSamples = Math.min(1536, Math.max(16, extraTicks * 8));
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        block0: for (int i = 0; i < xzSamples; ++i) {
            int dx = level.random.nextInt(diameter) - horizontalRadius;
            int dz = level.random.nextInt(diameter) - horizontalRadius;
            if ((long)dx * (long)dx + (long)dz * (long)dz > radiusSqr) continue;
            int x = center.getX() + dx;
            int z = center.getZ() + dz;
            for (int y = minY; y <= maxY; ++y) {
                BlockState state;
                cursor.set(x, y, z);
                if (!level.hasChunkAt((BlockPos)cursor) || !(state = level.getBlockState((BlockPos)cursor)).isRandomlyTicking() || !TimeAccelerationHandler.isTimeAcceleratedCrop(state)) continue;
                BlockPos pos = cursor.immutable();
                if (!sampled.add(pos)) continue block0;
                state.randomTick(level, pos, level.random);
                continue block0;
            }
        }
    }

    private static boolean isTimeAcceleratedCrop(BlockState state) {
        if (state.is(BlockTags.CROPS)) {
            return true;
        }
        return state.getBlock() instanceof CropBlock || state.getBlock() instanceof StemBlock || state.getBlock() instanceof NetherWartBlock || state.getBlock() instanceof SugarCaneBlock || state.getBlock() instanceof CactusBlock || state.getBlock() instanceof BonemealableBlock;
    }

    private static void sync(ServerPlayer player, CultivationData data) {
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncCultivationDataPacket(data));
    }

    private static boolean isSittingOnCushion(ServerPlayer player) {
        return player != null && player.getVehicle() instanceof SeatEntity;
    }

    private record ActiveTimeAcceleration(ServerPlayer player, CultivationData data, int multiplier) {
    }
}
