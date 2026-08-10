package com.friday.cultivation.event;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.identity.Identity;
import com.friday.cultivation.network.ClientOnlyGlowPacket;
import com.friday.cultivation.network.DivineSenseScanPacket;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.util.QiStorageBlocks;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = "friday_cultivation")
public final class DivineSenseHandler {
    private static final int GLOW_DURATION = 600;
    private static final int EXPANSION_TICKS = 60;
    private static final double ACTIVE_RANGE = 72.0;
    private static final int MAX_ENTITY_TARGETS = 256;
    private static final int MAX_BLOCK_TARGETS = 512;
    private static final int ENTITY_SWEEP_GRACE_TICKS = 4;
    private static final double ENTITY_SWEEP_PADDING = 0.35;
    private static final Map<UUID, ActiveScan> ACTIVE_SCANS = new HashMap<UUID, ActiveScan>();

    private DivineSenseHandler() {
    }

    public static void singleScan(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 center = DivineSenseHandler.scanCenter(player);
        List<BlockPos> blockTargets = DivineSenseHandler.collectStorageBlocks(level, center, 72.0);
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new DivineSenseScanPacket(center.x, center.y, center.z, 72.0, 60, 600, List.of(), blockTargets));
        long startTick = level.getGameTime();
        ACTIVE_SCANS.put(player.getUUID(), new ActiveScan(level.dimension().location().toString(), center, startTick, startTick + 60L + 4L, startTick + 600L));
        player.removeEffect(MobEffects.GLOWING);
        LivingEntity inspected = DivineSenseHandler.raycastLivingTarget(player, 72.0);
        player.displayClientMessage(inspected == null ? Component.translatable("message.friday_cultivation.divine_sense.scan") : DivineSenseHandler.inspectMessage(inspected), true);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (event.getServer() == null) {
            return;
        }
        if (ACTIVE_SCANS.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<UUID, ActiveScan>> it = ACTIVE_SCANS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, ActiveScan> entry = it.next();
            ActiveScan scan = entry.getValue();
            ServerLevel level = DivineSenseHandler.resolveLevel(event, scan.levelKey);
            if (level == null) {
                it.remove();
                continue;
            }
            long now = level.getGameTime();
            ServerPlayer scanPlayer = event.getServer().getPlayerList().getPlayer(entry.getKey());
            if (scanPlayer == null || scanPlayer.serverLevel() != level) {
                it.remove();
                continue;
            }
            DivineSenseHandler.revealReachedEntities(level, scanPlayer, scan, now);
            DivineSenseHandler.updateInspectionMessage(scanPlayer, now);
            if (now <= scan.endTick) continue;
            it.remove();
        }
    }

    private static void revealReachedEntities(ServerLevel level, ServerPlayer scanPlayer, ActiveScan scan, long now) {
        if (now > scan.sweepEndTick || scan.revealedEntityIds.size() >= 256) {
            return;
        }
        double radius = scan.currentRadius(now);
        List<LivingEntity> readyEntities = DivineSenseHandler.collectReachedLivingEntities(level, scanPlayer, scan.center, radius, scan.revealedEntityIds);
        if (readyEntities.isEmpty()) {
            return;
        }
        ArrayList<Integer> readyEntityIds = new ArrayList<Integer>(readyEntities.size());
        for (LivingEntity living : readyEntities) {
            if (!scan.revealedEntityIds.add(living.getId())) continue;
            readyEntityIds.add(living.getId());
        }
        if (!readyEntityIds.isEmpty()) {
            ClientOnlyGlowPacket.send(scanPlayer, readyEntityIds, 600);
        }
    }

    private static void updateInspectionMessage(ServerPlayer scanPlayer, long now) {
        if ((now & 3L) != 0L) {
            return;
        }
        LivingEntity inspected = DivineSenseHandler.raycastLivingTarget(scanPlayer, 72.0);
        if (inspected != null) {
            scanPlayer.displayClientMessage(DivineSenseHandler.inspectMessage(inspected), true);
        }
    }

    private static ServerLevel resolveLevel(TickEvent.ServerTickEvent event, String levelKeyStr) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (!level.dimension().location().toString().equals(levelKeyStr)) continue;
            return level;
        }
        return null;
    }

    private static Vec3 scanCenter(ServerPlayer player) {
        return player.position().add(0.0, (double) player.getBbHeight() * 0.55, 0.0);
    }

    static List<LivingEntity> collectReachedLivingEntities(ServerLevel level, ServerPlayer player, Vec3 center, double currentRadius, Set<Integer> alreadyRevealed) {
        double reachRadius = Math.min(72.0, Math.max(0.0, currentRadius)) + 0.35;
        AABB box = new AABB(center, center).inflate(reachRadius);
        ArrayList<LivingEntity> targets = new ArrayList<LivingEntity>();
        for (LivingEntity entity2 : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            double contactDistance;
            if (entity2 == player || alreadyRevealed.contains(entity2.getId()) || !SoulStateHandler.canOrdinaryAffect(player, entity2) || (contactDistance = DivineSenseHandler.shellContactDistance(center, entity2)) > reachRadius) continue;
            targets.add(entity2);
        }
        targets.sort(Comparator.comparingDouble(entity -> DivineSenseHandler.shellContactDistance(center, entity)));
        int remaining = 256 - alreadyRevealed.size();
        if (remaining <= 0) {
            return List.of();
        }
        if (targets.size() > remaining) {
            return new ArrayList<LivingEntity>(targets.subList(0, remaining));
        }
        return targets;
    }

    private static double shellContactDistance(Vec3 center, LivingEntity entity) {
        return DivineSenseHandler.distanceToAabb(center, entity.getBoundingBox().inflate(0.1));
    }

    private static double distanceToAabb(Vec3 point, AABB box) {
        double dx = DivineSenseHandler.axisDistance(point.x, box.minX, box.maxX);
        double dy = DivineSenseHandler.axisDistance(point.y, box.minY, box.maxY);
        double dz = DivineSenseHandler.axisDistance(point.z, box.minZ, box.maxZ);
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static double axisDistance(double value, double min, double max) {
        if (value < min) {
            return min - value;
        }
        if (value > max) {
            return value - max;
        }
        return 0.0;
    }

    private static List<BlockPos> collectStorageBlocks(ServerLevel level, Vec3 center, double radius) {
        int minChunkX = (int) Math.floor((center.x - radius) / 16.0);
        int maxChunkX = (int) Math.floor((center.x + radius) / 16.0);
        int minChunkZ = (int) Math.floor((center.z - radius) / 16.0);
        int maxChunkZ = (int) Math.floor((center.z + radius) / 16.0);
        double radiusSq = radius * radius;
        ArrayList<BlockPos> blocks = new ArrayList<BlockPos>();
        for (int cx = minChunkX; cx <= maxChunkX; ++cx) {
            for (int cz = minChunkZ; cz <= maxChunkZ; ++cz) {
                ChunkAccess chunk = level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false);
                if (!(chunk instanceof LevelChunk)) continue;
                LevelChunk levelChunk = (LevelChunk) chunk;
                for (BlockEntity be : levelChunk.getBlockEntities().values()) {
                    BlockPos pos;
                    if (!QiStorageBlocks.isUnlockedStorageTarget(be) && !QiStorageBlocks.isUnlockedSpiritVeinCore(be) || Vec3.atBottomCenterOf(pos = be.getBlockPos()).distanceToSqr(center) > radiusSq) continue;
                    blocks.add(pos.immutable());
                    if (blocks.size() < 512) continue;
                    return blocks;
                }
            }
        }
        return blocks;
    }

    private static Component inspectMessage(LivingEntity target) {
        ServerPlayer player;
        CultivationData data;
        if (target instanceof ServerPlayer && (data = CultivationCapability.get(player = (ServerPlayer) target).orElse(null)) != null) {
            return Component.translatable("message.friday_cultivation.divine_sense.inspect.player", player.getName(), data.getRealm().displayName(), DivineSenseHandler.health(target), data.getCurrentQi(), data.getMaxQi(), Component.translatable(Identity.byId(data.getIdentityId()).translationKey()), Component.translatable(data.getSpiritRoot().translationKey()), Component.translatable(data.getPhysique().translationKey()));
        }
        if (target instanceof WanderingCultivatorEntity) {
            WanderingCultivatorEntity npc = (WanderingCultivatorEntity) target;
            return Component.translatable("message.friday_cultivation.divine_sense.inspect.npc", npc.getName(), npc.getRealm().displayName(), DivineSenseHandler.health(target), npc.getCurrentQi(), npc.getMaxQi(), Component.translatable(npc.getSpiritRoot().translationKey()), Component.translatable(npc.getPhysique().translationKey()));
        }
        return Component.translatable("message.friday_cultivation.divine_sense.inspect.entity", target.getName(), DivineSenseHandler.health(target));
    }

    private static String health(LivingEntity target) {
        return String.format(Locale.ROOT, "%.0f/%.0f", Float.valueOf(target.getHealth()), Float.valueOf(target.getMaxHealth()));
    }

    private static LivingEntity raycastLivingTarget(ServerPlayer player, double maxDist) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        Vec3 end = eye.add(look.scale(maxDist));
        AABB scan = new AABB(eye, end).inflate(1.75);
        LivingEntity best = null;
        double bestDist = maxDist;
        for (LivingEntity entity : player.serverLevel().getEntitiesOfClass(LivingEntity.class, scan, e -> e != player && e.isAlive() && e.isPickable() && SoulStateHandler.canOrdinaryAffect(player, e))) {
            double dist;
            Optional<Vec3> hit = entity.getBoundingBox().inflate(0.65).clip(eye, end);
            if (hit.isEmpty() || !((dist = eye.distanceTo(hit.get())) < bestDist)) continue;
            bestDist = dist;
            best = entity;
        }
        return best;
    }

    private static final class ActiveScan {
        final String levelKey;
        final Vec3 center;
        final long startTick;
        final long sweepEndTick;
        final long endTick;
        final Set<Integer> revealedEntityIds = new HashSet<Integer>();

        ActiveScan(String levelKey, Vec3 center, long startTick, long sweepEndTick, long endTick) {
            this.levelKey = levelKey;
            this.center = center;
            this.startTick = startTick;
            this.sweepEndTick = sweepEndTick;
            this.endTick = endTick;
        }

        double currentRadius(long now) {
            double progress = (double) (now - this.startTick) / (double) Math.max(1, 60);
            return 72.0 * Math.max(0.0, Math.min(1.0, progress));
        }
    }
}
