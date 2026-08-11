/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
 *  net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.MobSpawnType
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.ServerLevelAccessor
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.levelgen.Heightmap$Types
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.event.TickEvent$ServerTickEvent
 *  net.minecraftforge.event.entity.EntityJoinLevelEvent
 *  net.minecraftforge.event.entity.EntityLeaveLevelEvent
 *  net.minecraftforge.event.entity.living.LivingDeathEvent
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.network.PacketDistributor
 */
package com.friday.cultivation.event;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.Identity;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.cultivation.realm.SubStage;
import com.friday.cultivation.entity.npc.CorpseEntity;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.event.SoulHookHandler;
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.item.SoulReaperTokenItem;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.SoulReaperTargetEntry;
import com.friday.cultivation.network.SoulReaperTargetsPacket;
import com.friday.cultivation.registry.ModDimensions;
import com.friday.cultivation.registry.ModEntities;
import com.friday.cultivation.registry.ModItems;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class SoulReaperOrderHandler {
    private static final double NOTIFY_DROP_CHANCE = 0.28;
    private static final int TOKEN_UPDATE_INTERVAL = 40;
    private static final int TELEPORT_SEARCH_RADIUS = 7;
    private static final int SOLO_NPC_SOUL_INTERVAL = 3600;
    private static final int SOLO_NPC_SOUL_MAX_ACTIVE = 3;
    private static final int SOLO_NPC_SOUL_MIN_RADIUS = 48;
    private static final int SOLO_NPC_SOUL_MAX_RADIUS = 96;
    private static final int SOLO_NPC_SOUL_SPAWN_ATTEMPTS = 18;
    private static final int NPC_SOUL_AUTO_REAPER_SOLO_DELAY = 200;
    private static final int NPC_SOUL_AUTO_REAPER_MULTIPLAYER_DELAY = 6000;
    private static final int NPC_SOUL_AUTO_REAPER_SOLO_SCAN_INTERVAL = 20;
    private static final int NPC_SOUL_AUTO_REAPER_MULTIPLAYER_SCAN_INTERVAL = 100;
    private static final int NPC_SOUL_AUTO_REAPER_MAX_PER_SCAN = 4;
    private static final Map<UUID, Integer> SOLO_NPC_SOUL_TIMERS = new HashMap<UUID, Integer>();
    private static int npcSoulAutoReaperScanTicks = 0;

    private SoulReaperOrderHandler() {
    }

    public static void openTargetScreen(ServerPlayer player) {
        if (!SoulReaperOrderHandler.canOperateAsSoulReaper(player, true)) {
            return;
        }
        List<SoulReaperTargetEntry> targets = SoulReaperOrderHandler.collectTargets(player);
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)new SoulReaperTargetsPacket(targets));
        SoulReaperOrderHandler.setTokenGlow(player, !targets.isEmpty());
    }

    public static void teleportToTarget(ServerPlayer player, UUID targetId) {
        if (!SoulReaperOrderHandler.canOperateAsSoulReaper(player, true)) {
            return;
        }
        TargetRef target = SoulReaperOrderHandler.findTarget(player.getServer(), targetId);
        if (target == null || target.entity() == player) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.soul_reaper_token.target_missing"), true);
            return;
        }
        if (target.level().dimension() == ModDimensions.DIFU) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.soul_reaper_token.target_missing"), true);
            return;
        }
        BlockPos dest = SoulReaperOrderHandler.findSafeTeleportPos(target.level(), target.entity().blockPosition());
        player.teleportTo(target.level(), (double)dest.getX() + 0.5, (double)dest.getY(), (double)dest.getZ() + 0.5, player.getYRot(), player.getXRot());
        target.level().playSound(null, dest, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.9f, 0.62f);
        player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.soul_reaper_token.teleported", (Object[])new Object[]{target.entity().getDisplayName()}).withStyle(ChatFormatting.DARK_RED), true);
    }

    public static void notifyDeath(ServerPlayer deadSoul) {
        SoulReaperOrderHandler.notifyDeath(deadSoul.getServer(), deadSoul.getUUID(), true);
    }

    public static void notifyDeath(MinecraftServer server, UUID excludedTarget) {
        SoulReaperOrderHandler.notifyDeath(server, excludedTarget, true);
    }

    public static void notifyNpcSoulAvailable(MinecraftServer server, UUID excludedTarget) {
        SoulReaperOrderHandler.notifyDeath(server, excludedTarget, false);
    }

    private static void notifyDeath(MinecraftServer server, UUID excludedTarget, boolean showTitle) {
        if (server == null) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.getUUID().equals(excludedTarget) || !SoulReaperOrderHandler.hasToken(player)) continue;
            SoulReaperOrderHandler.setTokenGlow(player, true);
            if (!showTitle) continue;
            player.connection.send((Packet)new ClientboundSetTitlesAnimationPacket(8, 58, 12));
            player.connection.send((Packet)new ClientboundSetTitleTextPacket((Component)Component.translatable((String)"message.friday_cultivation.soul_reaper_token.death_alert")));
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof WanderingCultivatorEntity)) {
            return;
        }
        WanderingCultivatorEntity npc = (WanderingCultivatorEntity)entity;
        Level level = npc.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        if (npc.isNpcSoulState()) {
            event.setCanceled(true);
            npc.setHealth(npc.getMaxHealth());
            return;
        }
        if (npc.isDifuReaper()) {
            if (npc.getRandom().nextDouble() < 0.28) {
                npc.spawnAtLocation(new ItemStack((ItemLike)ModItems.SOUL_REAPER_TOKEN.get()));
            }
            return;
        }
        if (level2.dimension() != Level.OVERWORLD) {
            return;
        }
        event.setCanceled(true);
        SoulReaperOrderHandler.spawnNpcCorpse(level2, npc);
        npc.enterNpcSoulState();
        SoulReaperOrderHandler.liftNpcSoul(npc);
        level2.playSound(null, npc.blockPosition(), SoundEvents.SOUL_ESCAPE, SoundSource.NEUTRAL, 0.9f, 0.52f);
        level2.sendParticles((ParticleOptions)ParticleTypes.SOUL, npc.getX(), npc.getY() + (double)npc.getBbHeight() * 0.5, npc.getZ(), 24, 0.4, 0.55, 0.4, 0.02);
        SoulReaperOrderHandler.notifyNpcSoulAvailable(level2.getServer(), npc.getUUID());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = event.player;
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        if (player2.tickCount % 40 != 0) {
            return;
        }
        SoulReaperOrderHandler.tickSoloNpcSoulSpawn(player2);
        if (!SoulReaperOrderHandler.hasToken(player2)) {
            return;
        }
        SoulReaperOrderHandler.setTokenGlow(player2, SoulReaperOrderHandler.hasAnyTargetFor(player2));
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof WanderingCultivatorEntity) {
            WanderingCultivatorEntity npc = (WanderingCultivatorEntity)entity;
            WanderingCultivatorEntity.trackNpcSoulIfApplicable(npc);
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof WanderingCultivatorEntity) {
            WanderingCultivatorEntity npc = (WanderingCultivatorEntity)entity;
            WanderingCultivatorEntity.untrackNpcSoul(npc);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        WanderingCultivatorEntity.clearNpcSoulRegistry();
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (++npcSoulAutoReaperScanTicks < SoulReaperOrderHandler.npcSoulAutoReaperScanInterval(event.getServer())) {
            return;
        }
        npcSoulAutoReaperScanTicks = 0;
        SoulReaperOrderHandler.tickNpcSoulAutoReapers(event.getServer());
    }

    private static void tickNpcSoulAutoReapers(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return;
        }
        int delayTicks = SoulReaperOrderHandler.npcSoulAutoReaperDelay(server);
        int spawned = 0;
        for (WanderingCultivatorEntity npc : new ArrayList<WanderingCultivatorEntity>(WanderingCultivatorEntity.loadedNpcSouls())) {
            if (npc.level() != overworld || !npc.isAlive() || !npc.isNpcSoulState()) {
                WanderingCultivatorEntity.untrackNpcSoul(npc);
                continue;
            }
            if (npc.getNpcSoulTicks() < delayTicks || SoulHookHandler.hasActiveTarget((Entity)npc) || !SoulStateHandler.spawnReaperForNpcSoul(npc) || ++spawned < 4) continue;
            return;
        }
    }

    private static int npcSoulAutoReaperDelay(MinecraftServer server) {
        return SoulReaperOrderHandler.isSingleplayerRun(server) ? 200 : 6000;
    }

    private static int npcSoulAutoReaperScanInterval(MinecraftServer server) {
        return SoulReaperOrderHandler.isSingleplayerRun(server) ? 20 : 100;
    }

    private static boolean isSingleplayerRun(MinecraftServer server) {
        return server != null && !server.isDedicatedServer() && server.getPlayerList().getPlayerCount() <= 1;
    }

    private static void tickSoloNpcSoulSpawn(ServerPlayer player) {
        if (!SoulReaperOrderHandler.isSoloSoulReaperWithToken(player)) {
            SOLO_NPC_SOUL_TIMERS.remove(player.getUUID());
            return;
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        if (SoulReaperOrderHandler.countOverworldNpcSouls(server) >= 3) {
            SOLO_NPC_SOUL_TIMERS.put(player.getUUID(), 0);
            return;
        }
        int ticks = SOLO_NPC_SOUL_TIMERS.getOrDefault(player.getUUID(), 0) + 40;
        if (ticks < 3600) {
            SOLO_NPC_SOUL_TIMERS.put(player.getUUID(), ticks);
            return;
        }
        if (SoulReaperOrderHandler.spawnSoloNpcSoul(player, server)) {
            SOLO_NPC_SOUL_TIMERS.put(player.getUUID(), 0);
        } else {
            SOLO_NPC_SOUL_TIMERS.put(player.getUUID(), 3000);
        }
    }

    private static boolean isSoloSoulReaperWithToken(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null || server.getPlayerList().getPlayerCount() != 1) {
            return false;
        }
        if (!SoulReaperOrderHandler.hasToken(player)) {
            return false;
        }
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        return data != null && data.isSoulState() && data.isSoulReaperIdentity() && data.getRealm().ordinal() >= Realm.QI_REFINING.ordinal();
    }

    private static int countOverworldNpcSouls(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return 0;
        }
        int count = 0;
        for (WanderingCultivatorEntity npc : WanderingCultivatorEntity.loadedNpcSouls()) {
            if (npc.level() != overworld || !npc.isAlive() || !npc.isNpcSoulState()) continue;
            ++count;
        }
        return count;
    }

    private static boolean spawnSoloNpcSoul(ServerPlayer player, MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return false;
        }
        BlockPos anchor = player.level().dimension() == Level.OVERWORLD ? player.blockPosition() : overworld.getSharedSpawnPos();
        BlockPos spawnPos = SoulReaperOrderHandler.findSoloNpcSoulSpawnPos(overworld, anchor, player.getRandom());
        if (spawnPos == null) {
            return false;
        }
        WanderingCultivatorEntity npc = (WanderingCultivatorEntity)((EntityType)ModEntities.WANDERING_CULTIVATOR.get()).create((Level)overworld);
        if (npc == null) {
            return false;
        }
        npc.moveTo((double)spawnPos.getX() + 0.5, spawnPos.getY(), (double)spawnPos.getZ() + 0.5, player.getRandom().nextFloat() * 360.0f, 0.0f);
        npc.checkSpawnObstruction(overworld);
        npc.enterNpcSoulState();
        SoulReaperOrderHandler.liftNpcSoul(npc);
        if (!overworld.addFreshEntity((Entity)npc)) {
            return false;
        }
        overworld.playSound(null, spawnPos, SoundEvents.SOUL_ESCAPE, SoundSource.NEUTRAL, 0.9f, 0.52f);
        overworld.sendParticles((ParticleOptions)ParticleTypes.SOUL, npc.getX(), npc.getY() + (double)npc.getBbHeight() * 0.5, npc.getZ(), 28, 0.45, 0.6, 0.45, 0.025);
        SoulReaperOrderHandler.notifyNpcSoulAvailable(server, npc.getUUID());
        return true;
    }

    private static void spawnNpcCorpse(ServerLevel level, WanderingCultivatorEntity npc) {
        CorpseEntity corpse = (CorpseEntity)((EntityType)ModEntities.CORPSE.get()).create((Level)level);
        if (corpse == null) {
            return;
        }
        corpse.moveTo(npc.getX(), npc.getY(), npc.getZ(), npc.getYRot(), 0.0f);
        corpse.setupNpcCorpse(npc);
        corpse.settleOnGround();
        if (level.addFreshEntity((Entity)corpse)) {
            npc.moveDeathLootToCorpse(corpse);
        }
    }

    private static void liftNpcSoul(WanderingCultivatorEntity npc) {
        double angle = npc.getRandom().nextDouble() * Math.PI * 2.0;
        double speed = 0.12 + npc.getRandom().nextDouble() * 0.08;
        npc.moveTo(npc.getX(), npc.getY() + 0.85, npc.getZ(), npc.getYRot(), -15.0f);
        npc.setDeltaMovement(Math.cos(angle) * speed, 0.32, Math.sin(angle) * speed);
        npc.hasImpulse = true;
        npc.fallDistance = 0.0f;
    }

    private static BlockPos findSoloNpcSoulSpawnPos(ServerLevel level, BlockPos anchor, RandomSource random) {
        for (int attempt = 0; attempt < 18; ++attempt) {
            int z;
            int y;
            double angle = random.nextDouble() * Math.PI * 2.0;
            int radius = 48 + random.nextInt(49);
            int x = anchor.getX() + (int)Math.round(Math.cos(angle) * (double)radius);
            BlockPos pos = new BlockPos(x, y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z = anchor.getZ() + (int)Math.round(Math.sin(angle) * (double)radius)), z);
            if (!SoulReaperOrderHandler.isSafeStandPos(level, pos)) continue;
            return pos;
        }
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, anchor.getX(), anchor.getZ());
        BlockPos fallback = new BlockPos(anchor.getX(), y, anchor.getZ());
        return SoulReaperOrderHandler.isSafeStandPos(level, fallback) ? fallback : null;
    }

    private static boolean canOperateAsSoulReaper(ServerPlayer player, boolean notify) {
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        if (data == null || !data.isSoulState()) {
            if (notify) {
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.soul_reaper_token.requires_soul"), true);
            }
            return false;
        }
        if (data.getRealm().ordinal() < Realm.QI_REFINING.ordinal()) {
            if (notify) {
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.soul_reaper_token.requires_realm", (Object[])new Object[]{Realm.QI_REFINING.displayName()}), true);
            }
            return false;
        }
        if (!data.isSoulReaperIdentity()) {
            if (notify) {
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.soul_reaper_token.requires_identity"), true);
            }
            return false;
        }
        if (!SoulReaperOrderHandler.hasToken(player)) {
            if (notify) {
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.soul_reaper_token.no_token"), true);
            }
            return false;
        }
        return true;
    }

    private static List<SoulReaperTargetEntry> collectTargets(ServerPlayer viewer) {
        MinecraftServer server = viewer.getServer();
        ArrayList<SoulReaperTargetEntry> entries = new ArrayList<SoulReaperTargetEntry>();
        if (server == null) {
            return entries;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            CultivationData data;
            if (player == viewer || (data = (CultivationData)CultivationCapability.get((Player)player).orElse(null)) == null || !data.isSoulState() || player.level().dimension() != Level.OVERWORLD) continue;
            entries.add(SoulReaperOrderHandler.playerEntry(player, data));
        }
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld != null) {
            for (WanderingCultivatorEntity npc : WanderingCultivatorEntity.loadedNpcSouls()) {
                if (npc.level() != overworld || !npc.isAlive() || !npc.isNpcSoulState()) continue;
                entries.add(SoulReaperOrderHandler.npcEntry(npc));
            }
        }
        entries.sort(Comparator.comparing(e -> e.name().getString()));
        return entries;
    }

    private static boolean hasAnyTargetFor(ServerPlayer viewer) {
        return !SoulReaperOrderHandler.collectTargets(viewer).isEmpty();
    }

    private static SoulReaperTargetEntry playerEntry(ServerPlayer player, CultivationData data) {
        return new SoulReaperTargetEntry(player.getUUID(), true, player.getDisplayName(), SoulReaperOrderHandler.genderComponent(data.getGender()), (Component)(data.isSoulReaperIdentity() ? Component.translatable((String)"entity.friday_cultivation.soul_reaper") : Component.translatable((String)Identity.byId(data.getIdentityId()).translationKey())), SoulReaperOrderHandler.realmComponent(data.getRealm(), data.getSubStage()), SoulReaperOrderHandler.locationComponent((Entity)player));
    }

    private static SoulReaperTargetEntry npcEntry(WanderingCultivatorEntity npc) {
        return new SoulReaperTargetEntry(npc.getUUID(), false, (Component)npc.getCultivatorName(), SoulReaperOrderHandler.genderComponent(npc.getGender()), (Component)(npc.isDifuReaper() ? Component.translatable((String)"entity.friday_cultivation.soul_reaper") : Component.translatable((String)npc.getIdentity().translationKey())), SoulReaperOrderHandler.realmComponent(npc.getRealm(), npc.getSubStage()), SoulReaperOrderHandler.locationComponent((Entity)npc));
    }

    private static Component genderComponent(int gender) {
        return switch (gender) {
            case 2 -> Component.translatable((String)"screen.friday_cultivation.gender.female");
            case 1 -> Component.translatable((String)"screen.friday_cultivation.gender.male");
            default -> Component.translatable((String)"screen.friday_cultivation.gender.secret");
        };
    }

    private static Component realmComponent(Realm realm, SubStage subStage) {
        return Component.empty().append(realm.displayName()).append((Component)Component.literal((String)" ")).append((Component)subStage.displayName());
    }

    private static Component locationComponent(Entity entity) {
        BlockPos pos = entity.blockPosition();
        return Component.translatable((String)"screen.friday_cultivation.soul_reaper_targets.location.coords", (Object[])new Object[]{pos.getX(), pos.getY(), pos.getZ()});
    }

    private static TargetRef findTarget(MinecraftServer server, UUID targetId) {
        WanderingCultivatorEntity npc;
        if (server == null || targetId == null) {
            return null;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Level level;
            CultivationData data;
            if (!player.getUUID().equals(targetId) || (data = (CultivationData)CultivationCapability.get((Player)player).orElse(null)) == null || !data.isSoulState() || !((level = player.level()) instanceof ServerLevel)) continue;
            ServerLevel level2 = (ServerLevel)level;
            return new TargetRef(level2, (LivingEntity)player);
        }
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return null;
        }
        Entity entity = overworld.getEntity(targetId);
        if (entity instanceof WanderingCultivatorEntity && (npc = (WanderingCultivatorEntity)entity).isNpcSoulState()) {
            return new TargetRef(overworld, (LivingEntity)npc);
        }
        return null;
    }

    private static BlockPos findSafeTeleportPos(ServerLevel level, BlockPos center) {
        for (int radius = 2; radius <= 7; ++radius) {
            for (int dx = -radius; dx <= radius; ++dx) {
                for (int dz = -radius; dz <= radius; ++dz) {
                    int z;
                    int y;
                    int x;
                    BlockPos pos;
                    if (Math.abs(dx) != radius && Math.abs(dz) != radius || !SoulReaperOrderHandler.isSafeStandPos(level, pos = new BlockPos(x = center.getX() + dx, y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z = center.getZ() + dz), z))) continue;
                    return pos;
                }
            }
        }
        return center.above();
    }

    private static boolean isSafeStandPos(ServerLevel level, BlockPos pos) {
        BlockState feet = level.getBlockState(pos);
        BlockState head = level.getBlockState(pos.above());
        BlockState below = level.getBlockState(pos.below());
        return feet.getCollisionShape(level, pos).isEmpty() && head.getCollisionShape(level, pos.above()).isEmpty() && !below.getCollisionShape(level, pos.below()).isEmpty() && below.getFluidState().isEmpty();
    }

    private static boolean hasToken(ServerPlayer player) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            if (!inv.getItem(i).is((Item)ModItems.SOUL_REAPER_TOKEN.get())) continue;
            return true;
        }
        return false;
    }

    private static void setTokenGlow(ServerPlayer player, boolean active) {
        Inventory inv = player.getInventory();
        boolean touched = false;
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (!stack.is((Item)ModItems.SOUL_REAPER_TOKEN.get())) continue;
            SoulReaperTokenItem.setSoulCall(stack, active);
            touched = true;
        }
        if (touched) {
            inv.setChanged();
        }
    }

    private record TargetRef(ServerLevel level, LivingEntity entity) {
    }
}

