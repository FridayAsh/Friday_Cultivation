/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.server.level.ServerBossEvent
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.util.Mth
 *  net.minecraft.world.BossEvent$BossBarColor
 *  net.minecraft.world.BossEvent$BossBarOverlay
 *  net.minecraft.world.damagesource.DamageTypes
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.MobCategory
 *  net.minecraft.world.entity.MobSpawnType
 *  net.minecraft.world.entity.monster.Enemy
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.AbstractArrow
 *  net.minecraft.world.entity.projectile.Fireball
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.TickEvent$LevelTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.entity.ProjectileImpactEvent
 *  net.minecraftforge.event.entity.living.LivingAttackEvent
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 *  net.minecraftforge.event.entity.living.MobSpawnEvent$FinalizeSpawn
 *  net.minecraftforge.event.entity.living.MobSpawnEvent$PositionCheck
 *  net.minecraftforge.event.entity.living.MobSpawnEvent$SpawnPlacementCheck
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerChangedDimensionEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedInEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$RightClickBlock
 *  net.minecraftforge.event.level.BlockEvent$BreakEvent
 *  net.minecraftforge.event.level.ExplosionEvent$Detonate
 *  net.minecraftforge.eventbus.api.Event$Result
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.network.PacketDistributor
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.event;

import com.friday.cultivation.block.formation.FormationCorePlateBlockEntity;
import com.friday.cultivation.block.formation.SectProtectionBarrierBlock;
import com.friday.cultivation.cultivation.ItemTier;
import com.friday.cultivation.cultivation.qi.formation.FormationType;
import com.friday.cultivation.cultivation.sect.SectSavedData;
import com.friday.cultivation.entity.BuddhaFireLotusEntity;
import com.friday.cultivation.entity.GreatFireballEntity;
import com.friday.cultivation.entity.HeavenPiercingConeEntity;
import com.friday.cultivation.entity.MeteorEntity;
import com.friday.cultivation.entity.SkySplittingSwordAuraEntity;
import com.friday.cultivation.entity.StoneBulletEntity;
import com.friday.cultivation.entity.SwordAuraEntity;
import com.friday.cultivation.entity.SwordProjectileEntity;
import com.friday.cultivation.entity.XiaoxiangFireballEntity;
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.item.SectTokenItem;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.SectShieldRipplePacket;
import com.friday.cultivation.network.SyncDomePacket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class SectProtectionDomeHandler {
    private static final Map<ResourceKey<Level>, Set<FormationCorePlateBlockEntity>> ACTIVE_DOMES = new ConcurrentHashMap<ResourceKey<Level>, Set<FormationCorePlateBlockEntity>>();
    private static final int DOME_BOSS_BAR_VISIBLE_TICKS = 160;
    private static final double DOME_PROJECTILE_SCAN_PLAYER_MARGIN = 96.0;
    private static final double DOME_PASS_THROUGH_SHELL_SCAN_PLAYER_MARGIN = 16.0;
    private static final int MAX_AREA_RIPPLES_PER_DOME = 48;
    private static final double BARRIER_EXPLOSION_MARK_RADIUS_SQ = 256.0;
    private static final int SECT_ENTRY_HINT_COOLDOWN_TICKS = 80;
    private static final Map<DomeKey, BossBarState> ACTIVE_BOSS_BARS = new ConcurrentHashMap<DomeKey, BossBarState>();
    private static final Map<ResourceKey<Level>, Set<BarrierExplosionMark>> PENDING_BARRIER_EXPLOSIONS = new ConcurrentHashMap<ResourceKey<Level>, Set<BarrierExplosionMark>>();
    private static final Map<UUID, Long> SECT_ENTRY_HINT_COOLDOWNS = new ConcurrentHashMap<UUID, Long>();

    private SectProtectionDomeHandler() {
    }

    private static boolean hasSectProtection(FormationCorePlateBlockEntity dome) {
        return dome.hasActiveFormation(FormationType.SECT_PROTECTION);
    }

    private static int sectRadius(FormationCorePlateBlockEntity dome) {
        return Math.max(1, dome.getActiveRadius());
    }

    private static ItemTier sectTier(FormationCorePlateBlockEntity dome) {
        ItemTier tier = dome.getActiveFlagTier(FormationType.SECT_PROTECTION);
        return tier == null ? ItemTier.LOW : tier;
    }

    public static void registerDome(FormationCorePlateBlockEntity core) {
        if (!SectProtectionDomeHandler.hasSectProtection(core)) {
            return;
        }
        Level level = core.getLevel();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        ACTIVE_DOMES.computeIfAbsent((ResourceKey<Level>)sl.dimension(), k -> ConcurrentHashMap.newKeySet()).add(core);
        ModNetwork.CHANNEL.send(PacketDistributor.DIMENSION.with(() -> ((ServerLevel)sl).dimension()), (Object)new SyncDomePacket(core.getBlockPos(), core.getSectProtectionSpheres()));
    }

    public static void unregisterDome(FormationCorePlateBlockEntity core) {
        Level level = core.getLevel();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        SectProtectionDomeHandler.removeDomeBossBar(sl, core.getBlockPos());
        Set<FormationCorePlateBlockEntity> set = ACTIVE_DOMES.get(sl.dimension());
        if (set != null) {
            set.remove(core);
        }
        ModNetwork.CHANNEL.send(PacketDistributor.DIMENSION.with(() -> ((ServerLevel)sl).dimension()), (Object)new SyncDomePacket(core.getBlockPos(), 0));
    }

    public static boolean isDomeRegistered(FormationCorePlateBlockEntity core) {
        Level level = core.getLevel();
        if (!(level instanceof ServerLevel)) {
            return false;
        }
        ServerLevel sl = (ServerLevel)level;
        Set<FormationCorePlateBlockEntity> set = ACTIVE_DOMES.get(sl.dimension());
        return set != null && set.contains(core);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer sp = (ServerPlayer)player;
        ResourceKey dim = sp.serverLevel().dimension();
        Set<FormationCorePlateBlockEntity> set = ACTIVE_DOMES.get(dim);
        if (set == null || set.isEmpty()) {
            return;
        }
        for (FormationCorePlateBlockEntity dome : set) {
            if (!dome.isActive() || !SectProtectionDomeHandler.hasSectProtection(dome)) continue;
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), (Object)new SyncDomePacket(dome.getBlockPos(), dome.getSectProtectionSpheres()));
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer sp = (ServerPlayer)player;
        SectProtectionDomeHandler.removePlayerFromAllBossBars(sp);
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), (Object)new SyncDomePacket(BlockPos.ZERO, -1));
        Set<FormationCorePlateBlockEntity> set = ACTIVE_DOMES.get(event.getTo());
        if (set == null || set.isEmpty()) {
            return;
        }
        for (FormationCorePlateBlockEntity dome : set) {
            if (!dome.isActive() || !SectProtectionDomeHandler.hasSectProtection(dome)) continue;
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), (Object)new SyncDomePacket(dome.getBlockPos(), dome.getSectProtectionSpheres()));
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer sp = (ServerPlayer)player;
            SectProtectionDomeHandler.removePlayerFromAllBossBars(sp);
        }
    }

    private static void showDomeBossBar(ServerLevel sl, FormationCorePlateBlockEntity dome) {
        DomeKey key = new DomeKey((ResourceKey<Level>)sl.dimension(), dome.getBlockPos());
        BossBarState state = ACTIVE_BOSS_BARS.computeIfAbsent(key, k -> new BossBarState(new ServerBossEvent(SectProtectionDomeHandler.domeBossBarName(dome), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.PROGRESS), sl.getGameTime() + 160L));
        state.hideAtTick = sl.getGameTime() + 160L;
        SectProtectionDomeHandler.updateDomeBossBar(sl, dome, state);
    }

    private static void tickDomeBossBars(ServerLevel sl) {
        long now = sl.getGameTime();
        Iterator<Map.Entry<DomeKey, BossBarState>> it = ACTIVE_BOSS_BARS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<DomeKey, BossBarState> entry = it.next();
            DomeKey key = entry.getKey();
            if (!key.dimension().equals((Object)sl.dimension())) continue;
            FormationCorePlateBlockEntity dome = SectProtectionDomeHandler.findActiveDome(sl, key.corePos());
            if (dome == null || now >= entry.getValue().hideAtTick) {
                entry.getValue().event.removeAllPlayers();
                it.remove();
                continue;
            }
            SectProtectionDomeHandler.updateDomeBossBar(sl, dome, entry.getValue());
        }
    }

    private static void updateDomeBossBar(ServerLevel sl, FormationCorePlateBlockEntity dome, BossBarState state) {
        state.event.setName(SectProtectionDomeHandler.domeBossBarName(dome));
        long max = dome.getMaxQi();
        float progress = (float)Mth.clamp((double)((double)dome.getCurrentQi() / (double)Math.max(1L, max)), (double)0.0, (double)1.0);
        state.event.setProgress(progress);
        for (ServerPlayer player : sl.players()) {
            state.event.addPlayer(player);
        }
    }

    private static Component domeBossBarName(FormationCorePlateBlockEntity dome) {
        String customName = dome.getCustomName();
        MutableComponent displayName = customName == null || customName.isBlank() ? Component.translatable((String)"bossbar.friday_cultivation.sect_protection_dome.unnamed") : Component.literal((String)customName);
        return Component.translatable((String)"bossbar.friday_cultivation.sect_protection_dome", (Object[])new Object[]{displayName});
    }

    private static void removeDomeBossBar(ServerLevel sl, BlockPos corePos) {
        BossBarState state = ACTIVE_BOSS_BARS.remove(new DomeKey((ResourceKey<Level>)sl.dimension(), corePos));
        if (state != null) {
            state.event.removeAllPlayers();
        }
    }

    private static void removePlayerFromAllBossBars(ServerPlayer player) {
        for (BossBarState state : ACTIVE_BOSS_BARS.values()) {
            state.event.removePlayer(player);
        }
    }

    private static FormationCorePlateBlockEntity findActiveDome(ServerLevel sl, BlockPos corePos) {
        Set<FormationCorePlateBlockEntity> set = ACTIVE_DOMES.get(sl.dimension());
        if (set == null) {
            return null;
        }
        for (FormationCorePlateBlockEntity dome : set) {
            if (!dome.isActive() || !SectProtectionDomeHandler.hasSectProtection(dome) || !dome.getBlockPos().equals((Object)corePos)) continue;
            return dome;
        }
        return null;
    }

    public static FormationCorePlateBlockEntity domeContaining(Level level, double x, double y, double z) {
        if (!(level instanceof ServerLevel)) {
            return null;
        }
        ServerLevel sl = (ServerLevel)level;
        Set<FormationCorePlateBlockEntity> set = ACTIVE_DOMES.get(sl.dimension());
        if (set == null || set.isEmpty()) {
            return null;
        }
        for (FormationCorePlateBlockEntity dome : set) {
            if (!dome.isActive() || !SectProtectionDomeHandler.hasSectProtection(dome) || !dome.containsActiveFormation(FormationType.SECT_PROTECTION, x, y, z)) continue;
            return dome;
        }
        return null;
    }

    public static boolean isInsideAnySectProtectionDome(Level level, double x, double y, double z) {
        return SectProtectionDomeHandler.domeContaining(level, x, y, z) != null;
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onHostileSpawnPlacement(MobSpawnEvent.SpawnPlacementCheck event) {
        if (event.getEntityType().getCategory() != MobCategory.MONSTER) {
            return;
        }
        if (!SectProtectionDomeHandler.shouldBlockHostileSpawn(event.getSpawnType(), event.getLevel().getLevel(), (double)event.getPos().getX() + 0.5, (double)event.getPos().getY() + 0.5, (double)event.getPos().getZ() + 0.5)) {
            return;
        }
        event.setResult(Event.Result.DENY);
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onHostileSpawnPosition(MobSpawnEvent.PositionCheck event) {
        Mob mob = event.getEntity();
        if (!SectProtectionDomeHandler.isHostileSpawnMob(mob)) {
            return;
        }
        if (!SectProtectionDomeHandler.shouldBlockHostileSpawn(event.getSpawnType(), event.getLevel().getLevel(), event.getX(), event.getY(), event.getZ())) {
            return;
        }
        event.setResult(Event.Result.DENY);
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onHostileFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        Mob mob = event.getEntity();
        if (!SectProtectionDomeHandler.isHostileSpawnMob(mob)) {
            return;
        }
        if (!SectProtectionDomeHandler.shouldBlockHostileSpawn(event.getSpawnType(), event.getLevel().getLevel(), event.getX(), event.getY(), event.getZ())) {
            return;
        }
        event.setSpawnCancelled(true);
        event.setResult(Event.Result.DENY);
    }

    private static boolean shouldBlockHostileSpawn(MobSpawnType spawnType, ServerLevel level, double x, double y, double z) {
        return SectProtectionDomeHandler.isDomeBlockedHostileSpawnType(spawnType) && SectProtectionDomeHandler.isInsideAnySectProtectionDome((Level)level, x, y, z);
    }

    private static boolean isHostileSpawnMob(Mob mob) {
        if (mob == null) {
            return false;
        }
        return mob instanceof Enemy || mob.getType().getCategory() == MobCategory.MONSTER;
    }

    private static boolean isDomeBlockedHostileSpawnType(MobSpawnType spawnType) {
        return switch (spawnType) {
            default -> throw new IncompatibleClassChangeError();
            case NATURAL, CHUNK_GENERATION, SPAWNER, STRUCTURE, JOCKEY, EVENT, REINFORCEMENT, TRIGGERED, PATROL -> true;
            case BREEDING, MOB_SUMMONED, CONVERSION, BUCKET, SPAWN_EGG, COMMAND, DISPENSER -> false;
        };
    }

    public static boolean isEntityProtectedByOwnDome(Entity entity) {
        FormationCorePlateBlockEntity dome = SectProtectionDomeHandler.domeContaining(entity.level(), entity.getX(), entity.getY(), entity.getZ());
        if (dome == null) {
            return false;
        }
        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)entity;
            return dome.isProtectedByDome(living);
        }
        return true;
    }

    public static boolean isProtectedByAnySectProtectionDome(Level level, BlockPos pos) {
        if (SectProtectionDomeHandler.domeOwningProtectedShell(level, pos) != null) {
            return true;
        }
        if (level.getBlockState(pos).getBlock() instanceof SectProtectionBarrierBlock) {
            return true;
        }
        return SectProtectionDomeHandler.isInsideAnySectProtectionDome(level, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
    }

    public static boolean canPlayerCarveFormationRuneAt(Player player, Level level, BlockPos pos) {
        if (player == null) {
            return false;
        }
        if (!(level instanceof ServerLevel)) {
            return false;
        }
        ServerLevel sl = (ServerLevel)level;
        if (SectProtectionDomeHandler.domeOwningProtectedShell(level, pos) != null) {
            return false;
        }
        if (level.getBlockState(pos).getBlock() instanceof SectProtectionBarrierBlock) {
            return false;
        }
        Set<FormationCorePlateBlockEntity> set = ACTIVE_DOMES.get(sl.dimension());
        if (set == null || set.isEmpty()) {
            return false;
        }
        boolean protectedByDome = false;
        for (FormationCorePlateBlockEntity dome : set) {
            if (!dome.isActive() || !SectProtectionDomeHandler.hasSectProtection(dome) || !SectProtectionDomeHandler.containsBlockCenter(dome, pos)) continue;
            protectedByDome = true;
            if (SectTokenItem.playerHasTokenForCore(player, level, dome.getBlockPos())) continue;
            return false;
        }
        return protectedByDome;
    }

    private static boolean containsBlockCenter(FormationCorePlateBlockEntity dome, BlockPos pos) {
        return dome.containsActiveFormation(FormationType.SECT_PROTECTION, pos);
    }

    public static boolean isProtectedFromExternal(Level level, BlockPos pos, double attackerX, double attackerY, double attackerZ) {
        if (SectProtectionDomeHandler.domeOwningProtectedShell(level, pos) != null) {
            return true;
        }
        if (level.getBlockState(pos).getBlock() instanceof SectProtectionBarrierBlock) {
            return true;
        }
        FormationCorePlateBlockEntity blockDome = SectProtectionDomeHandler.domeContaining(level, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
        if (blockDome == null) {
            return false;
        }
        FormationCorePlateBlockEntity attackerDome = SectProtectionDomeHandler.domeContaining(level, attackerX, attackerY, attackerZ);
        return attackerDome != blockDome;
    }

    public static FormationCorePlateBlockEntity domeOwningBarrier(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel)) {
            return null;
        }
        ServerLevel sl = (ServerLevel)level;
        Set<FormationCorePlateBlockEntity> set = ACTIVE_DOMES.get(sl.dimension());
        if (set == null) {
            return null;
        }
        long packed = pos.asLong();
        for (FormationCorePlateBlockEntity dome : set) {
            if (!dome.isOwnedBarrier(packed)) continue;
            return dome;
        }
        return null;
    }

    public static FormationCorePlateBlockEntity domeOwningProtectedShell(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel)) {
            return null;
        }
        ServerLevel sl = (ServerLevel)level;
        Set<FormationCorePlateBlockEntity> set = ACTIVE_DOMES.get(sl.dimension());
        if (set == null) {
            return null;
        }
        long packed = pos.asLong();
        for (FormationCorePlateBlockEntity dome : set) {
            if (!dome.isActive() || !SectProtectionDomeHandler.hasSectProtection(dome) || !dome.isProtectedShellBlock(packed)) continue;
            return dome;
        }
        return null;
    }

    public static boolean stopProjectileTouchingBarrier(ServerLevel sl, Entity projectile, Vec3 oldPos, Vec3 newPos, LivingEntity owner, float damageEquivalent, double reachRadius) {
        BarrierHit hit = SectProtectionDomeHandler.touchProjectileBarrier(sl, projectile, oldPos, newPos, owner, damageEquivalent, reachRadius);
        if (hit == null) {
            return false;
        }
        projectile.discard();
        return true;
    }

    public static BarrierHit touchProjectileBarrier(ServerLevel sl, Entity projectile, Vec3 oldPos, Vec3 newPos, LivingEntity owner, float damageEquivalent, double reachRadius) {
        Set<FormationCorePlateBlockEntity> set = ACTIVE_DOMES.get(sl.dimension());
        if (set == null || set.isEmpty()) {
            return null;
        }
        for (FormationCorePlateBlockEntity dome : set) {
            if (!dome.isActive() || !SectProtectionDomeHandler.hasSectProtection(dome) || !SectProtectionDomeHandler.projectileTouchesDomeBoundary(dome, oldPos, newPos, reachRadius) || SectProtectionDomeHandler.projectileFiredOutwardByToken((Entity)owner, dome, oldPos.x, oldPos.y, oldPos.z)) continue;
            Vec3 hitPos = SectProtectionDomeHandler.boundaryHitPos(dome, oldPos, newPos);
            SectProtectionDomeHandler.consumeBarrierHit(sl, dome, hitPos, damageEquivalent);
            return new BarrierHit(dome, hitPos);
        }
        return null;
    }

    public static void onBarrierAreaTouched(ServerLevel sl, Iterable<BlockPos> positions, float damageEquivalent) {
        SectProtectionDomeHandler.onBarrierAreaTouched(sl, positions, damageEquivalent, null);
    }

    public static void onBarrierAreaTouched(ServerLevel sl, Iterable<BlockPos> positions, float damageEquivalent, @Nullable Entity owner) {
        IdentityHashMap<FormationCorePlateBlockEntity, List> hitsByDome = new IdentityHashMap<FormationCorePlateBlockEntity, List>();
        for (BlockPos blockPos : positions) {
            FormationCorePlateBlockEntity dome = SectProtectionDomeHandler.domeOwningProtectedShell((Level)sl, blockPos);
            if (dome == null || SectProtectionDomeHandler.ownerMayFireOutward(owner, dome)) continue;
            hitsByDome.computeIfAbsent(dome, ignored -> new ArrayList()).add(blockPos.east());
        }
        for (Map.Entry entry : hitsByDome.entrySet()) {
            List<BlockPos> sampled = SectProtectionDomeHandler.sampleRipplePositions((List)entry.getValue(), 48);
            if (sampled.isEmpty()) continue;
            SectProtectionDomeHandler.consumeBarrierHit(sl, (FormationCorePlateBlockEntity)entry.getKey(), Vec3.atCenterOf((Vec3i)((Vec3i)sampled.get(0))), damageEquivalent);
            for (int i = 1; i < sampled.size(); ++i) {
                SectProtectionDomeHandler.spawnBarrierRipple(sl, Vec3.atCenterOf((Vec3i)((Vec3i)sampled.get(i))), false);
            }
        }
    }

    public static boolean onSpellAreaTouchedBarrier(ServerLevel sl, Vec3 center, double radius, Entity attacker, float damageEquivalent) {
        if (sl == null || center == null) {
            return false;
        }
        if (radius <= 0.0 || damageEquivalent <= 0.0f) {
            return false;
        }
        Set<FormationCorePlateBlockEntity> set = ACTIVE_DOMES.get(sl.dimension());
        if (set == null || set.isEmpty()) {
            return false;
        }
        boolean touched = false;
        double safeRadius = Math.max(0.25, radius);
        for (FormationCorePlateBlockEntity dome : set) {
            if (!dome.isActive() || !SectProtectionDomeHandler.hasSectProtection(dome) || !SectProtectionDomeHandler.spellAreaTouchesDomeBoundary(dome, center, safeRadius) || SectProtectionDomeHandler.spellMayPassOutward(attacker, dome, center) || SectProtectionDomeHandler.hasRecentBarrierExplosionMark(sl, dome, center)) continue;
            SectProtectionDomeHandler.consumeBarrierHit(sl, dome, SectProtectionDomeHandler.closestBoundaryPoint(dome, center), damageEquivalent);
            touched = true;
        }
        return touched;
    }

    public static boolean onSpellShellTouchedBarrier(ServerLevel sl, Vec3 center, double innerRadius, double outerRadius, Entity attacker, float damageEquivalent, Set<BlockPos> alreadyTouchedCores) {
        if (sl == null || center == null) {
            return false;
        }
        if (outerRadius <= 0.0 || damageEquivalent <= 0.0f) {
            return false;
        }
        Set<FormationCorePlateBlockEntity> set = ACTIVE_DOMES.get(sl.dimension());
        if (set == null || set.isEmpty()) {
            return false;
        }
        boolean touched = false;
        double safeInner = Math.max(0.0, innerRadius);
        double safeOuter = Math.max(safeInner, outerRadius);
        for (FormationCorePlateBlockEntity dome : set) {
            double distToBoundary;
            if (!dome.isActive() || !SectProtectionDomeHandler.hasSectProtection(dome)) continue;
            BlockPos core = dome.getBlockPos().east();
            if (alreadyTouchedCores != null && alreadyTouchedCores.contains(core) || (distToBoundary = SectProtectionDomeHandler.distanceToDomeBoundary(dome, center)) < safeInner || distToBoundary > safeOuter || SectProtectionDomeHandler.spellMayPassOutward(attacker, dome, center) || SectProtectionDomeHandler.hasRecentBarrierExplosionMark(sl, dome, center)) continue;
            SectProtectionDomeHandler.consumeBarrierHit(sl, dome, SectProtectionDomeHandler.closestBoundaryPoint(dome, center), damageEquivalent);
            if (alreadyTouchedCores != null) {
                alreadyTouchedCores.add(core);
            }
            touched = true;
        }
        return touched;
    }

    private static boolean spellAreaTouchesDomeBoundary(FormationCorePlateBlockEntity dome, Vec3 center, double radius) {
        return SectProtectionDomeHandler.distanceToDomeBoundary(dome, center) <= radius;
    }

    private static double distanceToDomeBoundary(FormationCorePlateBlockEntity dome, Vec3 center) {
        double best = Double.MAX_VALUE;
        for (FormationCorePlateBlockEntity.FormationSphere sphere : dome.getSectProtectionSpheres()) {
            double dist = center.distanceTo(sphere.centerVec());
            best = Math.min(best, Math.abs(dist - (double)sphere.radius()));
        }
        return best == Double.MAX_VALUE ? Double.MAX_VALUE : best;
    }

    private static Vec3 closestBoundaryPoint(FormationCorePlateBlockEntity dome, Vec3 center) {
        Vec3 bestPoint = center;
        double bestDistance = Double.MAX_VALUE;
        for (FormationCorePlateBlockEntity.FormationSphere sphere : dome.getSectProtectionSpheres()) {
            Vec3 domeCenter = sphere.centerVec();
            Vec3 radial = center.multiply(domeCenter);
            Vec3 vec3 = radial.lengthSqr() < 1.0E-6 ? domeCenter.add(0.0, (double)sphere.radius(), 0.0) : domeCenter.add(radial.normalize().scale((double)sphere.radius()));
            Vec3 point = vec3;
            double distance = point.distanceToSqr(center);
            if (!(distance < bestDistance)) continue;
            bestDistance = distance;
            bestPoint = point;
        }
        return bestPoint;
    }

    private static boolean holdsTokenForDome(Entity owner, FormationCorePlateBlockEntity dome) {
        return owner != null && dome != null && SectTokenItem.entityHasTokenForCore(owner, owner.level(), dome.getBlockPos());
    }

    private static boolean ownerMayFireOutward(Entity owner, FormationCorePlateBlockEntity dome) {
        return SectProtectionDomeHandler.holdsTokenForDome(owner, dome) && SectProtectionDomeHandler.domeContaining(owner.level(), owner.getX(), owner.getY(), owner.getZ()) == dome;
    }

    private static boolean projectileFiredOutwardByToken(Entity owner, FormationCorePlateBlockEntity dome, double ox, double oy, double oz) {
        return SectProtectionDomeHandler.holdsTokenForDome(owner, dome) && dome.containsActiveFormation(FormationType.SECT_PROTECTION, ox, oy, oz);
    }

    private static boolean spellMayPassOutward(Entity attacker, FormationCorePlateBlockEntity dome, Vec3 center) {
        if (!SectProtectionDomeHandler.holdsTokenForDome(attacker, dome)) {
            return false;
        }
        if (SectProtectionDomeHandler.domeContaining(attacker.level(), attacker.getX(), attacker.getY(), attacker.getZ()) == dome) {
            return true;
        }
        return center != null && !dome.containsActiveFormation(FormationType.SECT_PROTECTION, center.x, center.y, center.z);
    }

    public static boolean projectileMayPassBarrierOutward(Level level, BlockPos barrierPos, Entity projectile) {
        Entity entity;
        if (!(level instanceof ServerLevel)) {
            return false;
        }
        FormationCorePlateBlockEntity dome = SectProtectionDomeHandler.domeOwningBarrier(level, barrierPos);
        if (dome == null) {
            dome = SectProtectionDomeHandler.domeOwningProtectedShell(level, barrierPos);
        }
        if (dome == null) {
            return false;
        }
        if (projectile instanceof Projectile) {
            Projectile pj = (Projectile)projectile;
            entity = pj.getOwner();
        } else {
            entity = null;
        }
        Entity owner = entity;
        return SectProtectionDomeHandler.projectileFiredOutwardByToken(owner, dome, projectile.xOld, projectile.yOld, projectile.zOld);
    }

    private static boolean hasRecentBarrierExplosionMark(ServerLevel sl, FormationCorePlateBlockEntity dome, Vec3 origin) {
        Set<BarrierExplosionMark> marks = PENDING_BARRIER_EXPLOSIONS.get(sl.dimension());
        if (marks == null || marks.isEmpty()) {
            return false;
        }
        long now = sl.getGameTime();
        for (BarrierExplosionMark mark : marks) {
            if (now > mark.expiresAtTick()) {
                marks.remove(mark);
                continue;
            }
            if (!mark.corePos().equals((Object)dome.getBlockPos()) || !(mark.hitPos().distanceToSqr(origin) <= 256.0)) continue;
            return true;
        }
        return false;
    }

    private static List<BlockPos> sampleRipplePositions(List<BlockPos> positions, int maxCount) {
        if (positions.size() <= maxCount) {
            return positions;
        }
        ArrayList<BlockPos> sampled = new ArrayList<BlockPos>(maxCount);
        double step = (double)(positions.size() - 1) / (double)(maxCount - 1);
        for (int i = 0; i < maxCount; ++i) {
            int idx = (int)Math.round((double)i * step);
            sampled.add(positions.get(Math.min(idx, positions.size() - 1)));
        }
        return sampled;
    }

    public static void onBarrierTouched(Level level, BlockPos barrierPos, Vec3 hitPos, float damageEquivalent) {
        SectProtectionDomeHandler.onBarrierTouched(level, barrierPos, hitPos, damageEquivalent, null);
    }

    public static void onBarrierTouched(Level level, BlockPos barrierPos, Vec3 hitPos, float damageEquivalent, @Nullable Player player) {
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        FormationCorePlateBlockEntity dome = SectProtectionDomeHandler.domeOwningProtectedShell(level, barrierPos);
        if (dome == null) {
            dome = SectProtectionDomeHandler.domeOwningBarrier(level, barrierPos);
        }
        if (dome == null) {
            return;
        }
        SectProtectionDomeHandler.maybeShowSectEntryHint(sl, player, dome);
        SectProtectionDomeHandler.consumeBarrierHit(sl, dome, hitPos, damageEquivalent);
    }

    private static void maybeShowSectEntryHint(ServerLevel sl, @Nullable Player player, FormationCorePlateBlockEntity dome) {
        if (player == null || dome == null) {
            return;
        }
        if (SectTokenItem.playerHasTokenForCore(player, (Level)sl, dome.getBlockPos())) {
            return;
        }
        SectSavedData.SectRecord sect = SectSavedData.get(sl).findGeneratedSectByCore(dome.getBlockPos());
        if (sect == null) {
            return;
        }
        long now = sl.getGameTime();
        UUID playerId = player.getUUID();
        Long nextAllowed = SECT_ENTRY_HINT_COOLDOWNS.get(playerId);
        if (nextAllowed != null && now < nextAllowed) {
            return;
        }
        SECT_ENTRY_HINT_COOLDOWNS.put(playerId, now + 80L);
        player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.sect.entry_hint", (Object[])new Object[]{sect.name}), true);
    }

    public static void spawnBarrierRipple(ServerLevel sl, Vec3 pos) {
        SectProtectionDomeHandler.spawnBarrierRipple(sl, pos, true);
    }

    private static void spawnBarrierRipple(ServerLevel sl, Vec3 pos, boolean playSound) {
        BlockPos blockPos = BlockPos.containing((Position)pos);
        ModNetwork.CHANNEL.send(PacketDistributor.DIMENSION.with(() -> ((ServerLevel)sl).dimension()), (Object)new SectShieldRipplePacket(blockPos));
        if (playSound) {
            sl.playSound(null, pos.x, pos.y, pos.z, SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 0.35f, 1.9f);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        FormationCorePlateBlockEntity attackerDome;
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide) {
            return;
        }
        FormationCorePlateBlockEntity victimDome = SectProtectionDomeHandler.domeContaining(victim.level(), victim.getX(), victim.getY(), victim.getZ());
        if (victimDome == null) {
            return;
        }
        Entity attacker = event.getSource().getEntity();
        if (attacker != null && (attackerDome = SectProtectionDomeHandler.domeContaining(attacker.level(), attacker.getX(), attacker.getY(), attacker.getZ())) == victimDome) {
            return;
        }
        if (event.getSource().is(DamageTypes.FALL) || event.getSource().is(DamageTypes.DROWN) || event.getSource().is(DamageTypes.IN_WALL) || event.getSource().is(DamageTypes.STARVE) || event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD) || event.getSource().is(DamageTypes.GENERIC_KILL)) {
            return;
        }
        if (!victimDome.isProtectedByDome(victim)) {
            return;
        }
        ItemTier tier = SectProtectionDomeHandler.sectTier(victimDome);
        if (tier == null) {
            return;
        }
        long qiPerDamage = FormationType.SECT_PROTECTION.sectProtectionQiPerDamage(tier);
        float incoming = event.getAmount();
        if (incoming <= 0.0f) {
            return;
        }
        long qiNeeded = (long)Math.ceil(incoming * (float)qiPerDamage);
        long pool = victimDome.getCurrentQi();
        if (pool >= qiNeeded) {
            victimDome.consumeQi(qiNeeded);
            SectProtectionDomeHandler.showDomeBossBar((ServerLevel)victim.level(), victimDome);
            event.setResult(Event.Result.DENY);
            SectProtectionDomeHandler.spawnRippleFx(victim, incoming);
            SectProtectionDomeHandler.applyHitPassives(victimDome, tier, attacker, victim, incoming);
        } else if (pool > 0L) {
            victimDome.consumeQi(pool);
            SectProtectionDomeHandler.showDomeBossBar((ServerLevel)victim.level(), victimDome);
            SectProtectionDomeHandler.spawnBreakFx(victimDome);
            SectProtectionDomeHandler.applyHitPassives(victimDome, tier, attacker, victim, (float)((double)pool / (double)qiPerDamage));
        }
    }

    public static float absorbTribulationDamage(ServerPlayer player, float incoming) {
        if (player == null || incoming <= 0.0f) {
            return incoming;
        }
        Level level = player.level();
        if (!(level instanceof ServerLevel)) {
            return incoming;
        }
        ServerLevel level2 = (ServerLevel)level;
        FormationCorePlateBlockEntity dome = SectProtectionDomeHandler.domeContaining((Level)level2, player.getX(), player.getY(), player.getZ());
        if (dome == null || !SectProtectionDomeHandler.hasSectProtection(dome)) {
            return incoming;
        }
        if (!SectTokenItem.playerHasTokenForCore((Player)player, (Level)level2, dome.getBlockPos())) {
            return incoming;
        }
        ItemTier tier = SectProtectionDomeHandler.sectTier(dome);
        if (tier == null) {
            return incoming;
        }
        long qiPerDamage = FormationType.SECT_PROTECTION.sectProtectionQiPerDamage(tier);
        if (qiPerDamage <= 0L) {
            return incoming;
        }
        long qiNeeded = (long)Math.ceil(incoming * (float)qiPerDamage);
        long pool = dome.getCurrentQi();
        if (pool <= 0L) {
            return incoming;
        }
        long consumed = dome.consumeQi(Math.min(pool, qiNeeded));
        if (consumed <= 0L) {
            return incoming;
        }
        float blocked = Math.min(incoming, (float)consumed / (float)qiPerDamage);
        SectProtectionDomeHandler.showDomeBossBar(level2, dome);
        SectProtectionDomeHandler.spawnRippleFx((LivingEntity)player, blocked);
        if (consumed < qiNeeded && dome.getCurrentQi() <= 0L) {
            SectProtectionDomeHandler.spawnBreakFx(dome);
        }
        return Math.max(0.0f, incoming - blocked);
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        BlockPos pos = event.getPos();
        LevelAccessor levelAccessor = event.getLevel();
        if (!(levelAccessor instanceof Level)) {
            return;
        }
        Level lvl = (Level)levelAccessor;
        FormationCorePlateBlockEntity shellDome = SectProtectionDomeHandler.domeOwningProtectedShell(lvl, pos);
        if (shellDome != null) {
            event.setResult(Event.Result.DENY);
            if (lvl instanceof ServerLevel) {
                ServerLevel sl = (ServerLevel)lvl;
                Vec3 hit = new Vec3((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
                SectProtectionDomeHandler.maybeShowSectEntryHint(sl, event.getPlayer(), shellDome);
                SectProtectionDomeHandler.consumeBarrierHit(sl, shellDome, hit, 1.0f);
            }
            return;
        }
        FormationCorePlateBlockEntity blockDome = SectProtectionDomeHandler.domeContaining(lvl, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
        if (blockDome == null) {
            return;
        }
        Player breaker = event.getPlayer();
        if (breaker != null) {
            FormationCorePlateBlockEntity breakerDome = SectProtectionDomeHandler.domeContaining(lvl, breaker.getX(), breaker.getY(), breaker.getZ());
            if (breakerDome == blockDome) {
                return;
            }
            if (SectTokenItem.playerHasTokenForCore(breaker, lvl, blockDome.getBlockPos())) {
                return;
            }
        }
        event.setResult(Event.Result.DENY);
        if (lvl instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)lvl;
            SectProtectionDomeHandler.maybeShowSectEntryHint(sl, breaker, blockDome);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide) {
            return;
        }
        BlockPos clickedPos = event.getPos();
        BlockPos pos = clickedPos.relative(event.getFace());
        if (SectProtectionDomeHandler.domeOwningProtectedShell(event.getLevel(), clickedPos) != null) {
            SectProtectionDomeHandler.denyRightClickOnProtectedShell(event, clickedPos);
            return;
        }
        if (SectProtectionDomeHandler.domeOwningProtectedShell(event.getLevel(), pos) != null) {
            SectProtectionDomeHandler.denyRightClickOnProtectedShell(event, pos);
            return;
        }
        FormationCorePlateBlockEntity placeDome = SectProtectionDomeHandler.domeContaining(event.getLevel(), (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
        if (placeDome == null) {
            return;
        }
        FormationCorePlateBlockEntity playerDome = SectProtectionDomeHandler.domeContaining(event.getLevel(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ());
        if (playerDome == placeDome) {
            return;
        }
        if (SectTokenItem.playerHasTokenForCore(event.getEntity(), event.getLevel(), placeDome.getBlockPos())) {
            return;
        }
        event.setUseItem(Event.Result.DENY);
        Level level = event.getLevel();
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            SectProtectionDomeHandler.maybeShowSectEntryHint(sl, event.getEntity(), placeDome);
        }
    }

    private static void denyRightClickOnProtectedShell(PlayerInteractEvent.RightClickBlock event, BlockPos pos) {
        event.setUseBlock(Event.Result.DENY);
        event.setUseItem(Event.Result.DENY);
        Vec3 hit = new Vec3((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
        Level level = event.getLevel();
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            FormationCorePlateBlockEntity dome = SectProtectionDomeHandler.domeOwningProtectedShell(event.getLevel(), pos);
            if (dome != null) {
                SectProtectionDomeHandler.maybeShowSectEntryHint(sl, event.getEntity(), dome);
            }
            SectProtectionDomeHandler.spawnBarrierRipple(sl, hit);
        }
    }

    public static void recordExternalBarrierExplosion(ServerLevel sl, FormationCorePlateBlockEntity dome, Vec3 hitPos) {
        if (dome == null || !dome.isActive()) {
            return;
        }
        Set<BarrierExplosionMark> marks = PENDING_BARRIER_EXPLOSIONS.computeIfAbsent((ResourceKey<Level>)sl.dimension(), ignored -> ConcurrentHashMap.newKeySet());
        marks.removeIf(existing -> existing.corePos().equals((Object)dome.getBlockPos()) && existing.hitPos().distanceToSqr(hitPos) <= 4.0);
        marks.add(new BarrierExplosionMark(dome.getBlockPos().east(), hitPos, sl.getGameTime() + 5L));
    }

    private static BarrierExplosionMark consumeRecentBarrierExplosionMark(ServerLevel sl, Vec3 origin) {
        Set<BarrierExplosionMark> marks = PENDING_BARRIER_EXPLOSIONS.get(sl.dimension());
        if (marks == null || marks.isEmpty()) {
            return null;
        }
        long now = sl.getGameTime();
        BarrierExplosionMark best = null;
        double bestDistSq = 256.0;
        for (BarrierExplosionMark mark : marks) {
            if (now > mark.expiresAtTick()) {
                marks.remove(mark);
                continue;
            }
            double distSq = mark.hitPos().distanceToSqr(origin);
            if (!(distSq <= bestDistSq)) continue;
            bestDistSq = distSq;
            best = mark;
        }
        if (best != null) {
            marks.remove(best);
        }
        return best;
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        FormationCorePlateBlockEntity markedDome;
        ServerLevel sl;
        BarrierExplosionMark mark;
        Level lvl = event.getLevel();
        if (lvl.isClientSide) {
            return;
        }
        Vec3 origin = event.getExplosion().getPosition();
        if (lvl instanceof ServerLevel && (mark = SectProtectionDomeHandler.consumeRecentBarrierExplosionMark(sl = (ServerLevel)lvl, origin)) != null && (markedDome = SectProtectionDomeHandler.findActiveDome(sl, mark.corePos())) != null) {
            event.getAffectedBlocks().removeIf(p -> SectProtectionDomeHandler.isProtectedByAnySectProtectionDome(lvl, p));
            event.getAffectedEntities().removeIf(SectProtectionDomeHandler::isEntityProtectedByOwnDome);
            return;
        }
        event.getAffectedBlocks().removeIf(p -> SectProtectionDomeHandler.isProtectedByAnySectProtectionDome(lvl, p));
        event.getAffectedEntities().removeIf(SectProtectionDomeHandler::isEntityProtectedByOwnDome);
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (event.level.isClientSide) {
            return;
        }
        Level level = event.level;
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        SectProtectionDomeHandler.tickDomeBossBars(sl);
        Set<FormationCorePlateBlockEntity> set = ACTIVE_DOMES.get(sl.dimension());
        if (set == null || set.isEmpty()) {
            return;
        }
        for (FormationCorePlateBlockEntity dome : set) {
            if (!dome.isActive()) continue;
            SectProtectionDomeHandler.scanCrossingProjectiles(dome, sl);
            SectProtectionDomeHandler.enforcePassThroughShellCollision(dome, sl);
        }
    }

    private static void enforcePassThroughShellCollision(FormationCorePlateBlockEntity dome, ServerLevel sl) {
        int r;
        double cz;
        double cy;
        if (!dome.hasPassThroughProtectedShellBlocks()) {
            return;
        }
        BlockPos o = dome.getBlockPos();
        double cx = (double)o.getX() + 0.5;
        if (!sl.hasNearbyAlivePlayer(cx, cy = (double)o.getY() + 0.5, cz = (double)o.getZ() + 0.5, (double)(r = SectProtectionDomeHandler.sectRadius(dome)) + 16.0)) {
            return;
        }
        HashSet<LivingEntity> handled = new HashSet<LivingEntity>();
        for (FormationCorePlateBlockEntity.FormationSphere sphere : dome.getSectProtectionSpheres()) {
            AABB scan = sphere.bounds(1.0);
            for (LivingEntity entity2 : sl.getEntitiesOfClass(LivingEntity.class, scan, entity -> entity.isAlive() && dome.touchesPassThroughProtectedShellBlock((LivingEntity)entity))) {
                if (!handled.add(entity2) || SectTokenItem.entityHasTokenForCore((Entity)entity2, (Level)sl, dome.getBlockPos())) continue;
                SectProtectionDomeHandler.repelFromPassThroughShell(sl, dome, entity2);
            }
        }
    }

    private static void repelFromPassThroughShell(ServerLevel sl, FormationCorePlateBlockEntity dome, LivingEntity entity) {
        Vec3 current = entity.position();
        Vec3 target = new Vec3(entity.xOld, entity.yOld, entity.zOld);
        if (!SectProtectionDomeHandler.isUsableReboundTarget(target) || target.distanceToSqr(current) < 1.0E-4) {
            target = SectProtectionDomeHandler.nearestPassThroughShellReboundPoint(dome, entity);
        }
        if (entity instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)entity;
            player.connection.teleport(target.x, target.y, target.z, player.getYRot(), player.getXRot());
            SectProtectionDomeHandler.maybeShowSectEntryHint(sl, (Player)player, dome);
        } else {
            entity.teleportTo(target.x, target.y, target.z);
        }
        entity.setDeltaMovement(Vec3.ZERO);
        entity.hurtMarked = true;
        SectProtectionDomeHandler.spawnBarrierRipple(sl, SectProtectionDomeHandler.closestBoundaryPoint(dome, current), false);
    }

    private static boolean isUsableReboundTarget(Vec3 target) {
        return Double.isFinite(target.x) && Double.isFinite(target.y) && Double.isFinite(target.z);
    }

    private static Vec3 nearestPassThroughShellReboundPoint(FormationCorePlateBlockEntity dome, LivingEntity entity) {
        Vec3 current = entity.position();
        FormationCorePlateBlockEntity.FormationSphere best = null;
        double bestDelta = Double.MAX_VALUE;
        for (FormationCorePlateBlockEntity.FormationSphere sphere : dome.getSectProtectionSpheres()) {
            double delta = Math.abs(current.distanceTo(sphere.centerVec()) - (double)sphere.radius());
            if (!(delta < bestDelta)) continue;
            bestDelta = delta;
            best = sphere;
        }
        if (best == null) {
            return current;
        }
        Vec3 center = best.centerVec();
        Vec3 radial = current.multiply(center);
        if (radial.lengthSqr() < 1.0E-6) {
            radial = new Vec3(1.0, 0.0, 0.0);
        }
        boolean inside = dome.containsActiveFormation(FormationType.SECT_PROTECTION, current.x, current.y, current.z);
        double safeRadius = Math.max(0.5, (double)best.radius() + (inside ? -1.25 : 1.25));
        return center.add(radial.normalize().scale(safeRadius));
    }

    private static void scanCrossingProjectiles(FormationCorePlateBlockEntity dome, ServerLevel sl) {
        int r;
        double cz;
        double cy;
        BlockPos o = dome.getBlockPos();
        double cx = (double)o.getX() + 0.5;
        if (!sl.hasNearbyAlivePlayer(cx, cy = (double)o.getY() + 0.5, cz = (double)o.getZ() + 0.5, (double)(r = SectProtectionDomeHandler.sectRadius(dome)) + 96.0)) {
            return;
        }
        AABB scan = new AABB(cx - (double)r - 5.0, cy - (double)r - 5.0, cz - (double)r - 5.0, cx + (double)r + 5.0, cy + (double)r + 5.0, cz + (double)r + 5.0);
        long rsq = (long)r * (long)r;
        for (Entity e : sl.getEntities((Entity)null, scan, SectProtectionDomeHandler::shouldStopAtBarrier)) {
            boolean oldInside;
            double dx = e.getX() - cx;
            double dy = e.getY() - cy;
            double dz = e.getZ() - cz;
            double dsq = dx * dx + dy * dy + dz * dz;
            double dxOld = e.xOld - cx;
            double dyOld = e.yOld - cy;
            double dzOld = e.zOld - cz;
            double dsqOld = dxOld * dxOld + dyOld * dyOld + dzOld * dzOld;
            boolean nowInside = dome.containsActiveFormation(FormationType.SECT_PROTECTION, e.getX(), e.getY(), e.getZ());
            if (nowInside == (oldInside = dome.containsActiveFormation(FormationType.SECT_PROTECTION, e.xOld, e.yOld, e.zOld)) || oldInside && !nowInside && SectProtectionDomeHandler.holdsTokenForDome(SectProtectionDomeHandler.resolveProjectileOwner(e, sl), dome)) continue;
            float damageEquiv = SectProtectionDomeHandler.estimateProjectileDamage(e);
            Vec3 hitPos = new Vec3(e.getX(), e.getY(), e.getZ());
            SectProtectionDomeHandler.consumeBarrierHit(sl, dome, hitPos, damageEquiv);
            if (e instanceof MeteorEntity) {
                MeteorEntity meteor = (MeteorEntity)e;
                meteor.impactOnBarrier(hitPos);
                continue;
            }
            if (e instanceof SwordProjectileEntity) {
                SwordProjectileEntity sword = (SwordProjectileEntity)e;
                sword.impactOnBarrier(hitPos);
                continue;
            }
            if (e instanceof SwordAuraEntity) {
                SwordAuraEntity aura = (SwordAuraEntity)e;
                aura.impactOnBarrier(hitPos);
                continue;
            }
            if (e instanceof HeavenPiercingConeEntity) {
                HeavenPiercingConeEntity cone = (HeavenPiercingConeEntity)e;
                cone.impactOnBarrier(hitPos);
                continue;
            }
            if (e instanceof BuddhaFireLotusEntity) {
                BuddhaFireLotusEntity lotus = (BuddhaFireLotusEntity)e;
                lotus.impactOnBarrier(hitPos);
                continue;
            }
            e.discard();
        }
    }

    private static boolean shouldStopAtBarrier(Entity e) {
        if (e == null || !e.isAlive()) {
            return false;
        }
        if (e instanceof LivingEntity) {
            return false;
        }
        if (e instanceof Projectile) {
            return true;
        }
        if (e instanceof SwordProjectileEntity) {
            return true;
        }
        if (e instanceof MeteorEntity) {
            return true;
        }
        if (e instanceof SwordAuraEntity) {
            return true;
        }
        if (e instanceof HeavenPiercingConeEntity) {
            return true;
        }
        return e instanceof BuddhaFireLotusEntity;
    }

    private static float estimateProjectileDamage(Entity e) {
        if (e instanceof AbstractArrow) {
            AbstractArrow arrow = (AbstractArrow)e;
            return (float)arrow.getBaseDamage();
        }
        if (e instanceof SkySplittingSwordAuraEntity) {
            SkySplittingSwordAuraEntity sky = (SkySplittingSwordAuraEntity)e;
            return sky.getBarrierDamageEquivalent();
        }
        if (e instanceof HeavenPiercingConeEntity) {
            HeavenPiercingConeEntity cone = (HeavenPiercingConeEntity)e;
            return cone.getDamage();
        }
        if (e instanceof StoneBulletEntity) {
            StoneBulletEntity stone = (StoneBulletEntity)e;
            return stone.getDamage();
        }
        if (e instanceof SwordProjectileEntity) {
            SwordProjectileEntity sword = (SwordProjectileEntity)e;
            return sword.getBarrierDamageEquivalent();
        }
        if (e instanceof MeteorEntity) {
            MeteorEntity meteor = (MeteorEntity)e;
            return meteor.getBarrierDamageEquivalent();
        }
        if (e instanceof SwordAuraEntity) {
            SwordAuraEntity aura = (SwordAuraEntity)e;
            return aura.getBarrierDamageEquivalent();
        }
        if (e instanceof XiaoxiangFireballEntity) {
            XiaoxiangFireballEntity fireball = (XiaoxiangFireballEntity)e;
            return Math.max(1.0f, (float)fireball.getExtraDamage());
        }
        if (e instanceof GreatFireballEntity) {
            GreatFireballEntity greatFireball = (GreatFireballEntity)e;
            return Math.max(1.0f, (float)greatFireball.computedExtraDamage());
        }
        if (e instanceof BuddhaFireLotusEntity) {
            BuddhaFireLotusEntity lotus = (BuddhaFireLotusEntity)e;
            return lotus.damage();
        }
        return 1.0f;
    }

    private static Entity resolveProjectileOwner(Entity e, ServerLevel sl) {
        SwordProjectileEntity sword;
        if (e instanceof Projectile) {
            Projectile pj = (Projectile)e;
            return pj.getOwner();
        }
        if (e instanceof SkySplittingSwordAuraEntity) {
            SkySplittingSwordAuraEntity sky = (SkySplittingSwordAuraEntity)e;
            return sky.getOwnerEntity(sl);
        }
        if (e instanceof SwordAuraEntity) {
            SwordAuraEntity aura = (SwordAuraEntity)e;
            return aura.getOwnerEntity(sl);
        }
        if (e instanceof SwordProjectileEntity && (sword = (SwordProjectileEntity)e).getOwnerUuid() != null) {
            return sl.getEntity(sword.getOwnerUuid());
        }
        if (e instanceof MeteorEntity) {
            MeteorEntity meteor = (MeteorEntity)e;
            return meteor.getOwnerEntity(sl);
        }
        if (e instanceof HeavenPiercingConeEntity) {
            HeavenPiercingConeEntity cone = (HeavenPiercingConeEntity)e;
            return cone.getOwnerEntity(sl);
        }
        if (e instanceof BuddhaFireLotusEntity) {
            BuddhaFireLotusEntity lotus = (BuddhaFireLotusEntity)e;
            return lotus.getOwnerEntity(sl);
        }
        return null;
    }

    private static void consumeBarrierHit(ServerLevel sl, FormationCorePlateBlockEntity dome, Vec3 hitPos, float damageEquivalent) {
        ItemTier tier = SectProtectionDomeHandler.sectTier(dome);
        if (tier == null) {
            return;
        }
        long qiPerDmg = FormationType.SECT_PROTECTION.sectProtectionQiPerDamage(tier);
        long qiCost = (long)Math.ceil(Math.max(1.0f, damageEquivalent) * (float)qiPerDmg);
        long actualConsumed = dome.consumeQi(qiCost);
        SectProtectionDomeHandler.showDomeBossBar(sl, dome);
        SectProtectionDomeHandler.spawnBarrierRipple(sl, hitPos);
        if (actualConsumed > 0L && dome.getCurrentQi() <= 0L) {
            dome.deactivate();
        }
    }

    private static boolean projectileTouchesDomeBoundary(FormationCorePlateBlockEntity dome, Vec3 oldPos, Vec3 newPos, double reachRadius) {
        for (FormationCorePlateBlockEntity.FormationSphere sphere : dome.getSectProtectionSpheres()) {
            if (!SectProtectionDomeHandler.projectileTouchesSphereBoundary(sphere, oldPos, newPos, reachRadius)) continue;
            return true;
        }
        return false;
    }

    private static Vec3 boundaryHitPos(FormationCorePlateBlockEntity dome, Vec3 oldPos, Vec3 newPos) {
        Vec3 best = SectProtectionDomeHandler.closestBoundaryPoint(dome, newPos);
        double bestDistance = Double.MAX_VALUE;
        for (FormationCorePlateBlockEntity.FormationSphere sphere : dome.getSectProtectionSpheres()) {
            Vec3 hit = SectProtectionDomeHandler.boundaryHitPos(sphere, oldPos, newPos);
            double distance = hit.distanceToSqr(newPos);
            if (!(distance < bestDistance)) continue;
            bestDistance = distance;
            best = hit;
        }
        return best;
    }

    private static boolean projectileTouchesSphereBoundary(FormationCorePlateBlockEntity.FormationSphere sphere, Vec3 oldPos, Vec3 newPos, double reachRadius) {
        double newDist;
        Vec3 center = sphere.centerVec();
        double r = sphere.radius();
        double oldDist = oldPos.distanceTo(center);
        if (oldDist <= r != (newDist = newPos.distanceTo(center)) <= r) {
            return true;
        }
        double reach = Math.max(0.0, reachRadius);
        if (reach <= 0.0) {
            return false;
        }
        Vec3 seg = newPos.multiply(oldPos);
        double lenSq = seg.lengthSqr();
        double t = lenSq <= 1.0E-6 ? 0.0 : center.multiply(oldPos).dot(seg) / lenSq;
        t = Math.max(0.0, Math.min(1.0, t));
        double closestDist = oldPos.add(seg.scale(t)).distanceTo(center);
        return Math.abs(closestDist - r) <= reach || Math.abs(oldDist - r) <= reach || Math.abs(newDist - r) <= reach;
    }

    private static Vec3 boundaryHitPos(FormationCorePlateBlockEntity.FormationSphere sphere, Vec3 oldPos, Vec3 newPos) {
        Vec3 radial;
        Vec3 center = sphere.centerVec();
        double r = sphere.radius();
        Vec3 d = newPos.multiply(oldPos);
        Vec3 m = oldPos.multiply(center);
        double a = d.lengthSqr();
        double b = 2.0 * m.dot(d);
        double c = m.lengthSqr() - r * r;
        double disc = b * b - 4.0 * a * c;
        if (a > 1.0E-6 && disc >= 0.0) {
            double t;
            double root = Math.sqrt(disc);
            double t1 = (-b - root) / (2.0 * a);
            double t2 = (-b + root) / (2.0 * a);
            double d2 = t = t1 >= 0.0 && t1 <= 1.0 ? t1 : t2;
            if (t >= 0.0 && t <= 1.0) {
                return oldPos.add(d.scale(t));
            }
        }
        if ((radial = newPos.multiply(center)).lengthSqr() < 1.0E-6) {
            return newPos;
        }
        return center.add(radial.normalize().scale(r));
    }

    private static void applyHitPassives(FormationCorePlateBlockEntity dome, ItemTier tier, Entity attacker, LivingEntity victim, float blockedDmg) {
        float reflectAmt;
        if (!(attacker instanceof LivingEntity)) {
            return;
        }
        LivingEntity livingAtk = (LivingEntity)attacker;
        if (!SoulStateHandler.canOrdinaryAffect((Entity)livingAtk, (Entity)victim) || !SoulStateHandler.canOrdinaryAffect((Entity)victim, (Entity)livingAtk)) {
            return;
        }
        double reflectPct = switch (tier) {
            case LOW -> 0.0;
            case MID -> 0.1;
            case HIGH -> 0.3;
            case SUPREME -> 0.5;
            case IMMORTAL -> 1.0;
            case GREAT_EMPEROR -> 1.0;
        };
        if (reflectPct > 0.0 && (reflectAmt = (float)((double)blockedDmg * reflectPct)) > 0.0f) {
            livingAtk.hurt(victim.damageSources().magic(), reflectAmt);
        }
    }

    private static void spawnRippleFx(LivingEntity victim, float intensity) {
        Level level = victim.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        int count = Math.min(40, 10 + (int)intensity);
        sl.sendParticles((ParticleOptions)ParticleTypes.END_ROD, victim.getX(), victim.getY() + (double)victim.getBbHeight() * 0.5, victim.getZ(), count, 0.4, 0.4, 0.4, 0.05);
        sl.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 0.4f, 1.8f);
    }

    private static void spawnBreakFx(FormationCorePlateBlockEntity dome) {
        Level level = dome.getLevel();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        BlockPos o = dome.getBlockPos();
        int r = SectProtectionDomeHandler.sectRadius(dome);
        for (int i = 0; i < 80; ++i) {
            double theta = sl.random.nextDouble() * Math.PI * 2.0;
            double phi = sl.random.nextDouble() * Math.PI;
            double sx = (double)o.getX() + 0.5 + (double)r * Math.sin(phi) * Math.cos(theta);
            double sy = (double)o.getY() + 0.5 + (double)r * Math.cos(phi);
            double sz = (double)o.getZ() + 0.5 + (double)r * Math.sin(phi) * Math.sin(theta);
            sl.sendParticles((ParticleOptions)ParticleTypes.END_ROD, sx, sy, sz, 2, 0.1, 0.1, 0.1, 0.02);
        }
        sl.playSound(null, (double)o.getX(), (double)o.getY(), (double)o.getZ(), SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 2.0f, 0.8f);
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        FormationCorePlateBlockEntity ownerDome;
        Projectile projectile = event.getProjectile();
        if (projectile.level().isClientSide) {
            return;
        }
        Entity owner = projectile.getOwner();
        HitResult hitResult = event.getRayTraceResult();
        Vec3 hitPos = hitResult.getLocation();
        FormationCorePlateBlockEntity hitDome = null;
        if (hitResult instanceof BlockHitResult) {
            BlockHitResult blockHit = (BlockHitResult)hitResult;
            hitDome = SectProtectionDomeHandler.domeOwningProtectedShell(projectile.level(), blockHit.getBlockPos());
            if (hitDome == null) {
                hitDome = SectProtectionDomeHandler.domeOwningBarrier(projectile.level(), blockHit.getBlockPos());
            }
        }
        if (hitDome == null) {
            hitDome = SectProtectionDomeHandler.domeContaining(projectile.level(), hitPos.x, hitPos.y, hitPos.z);
        }
        if (hitDome == null) {
            return;
        }
        if (owner != null && (ownerDome = SectProtectionDomeHandler.domeContaining(owner.level(), owner.getX(), owner.getY(), owner.getZ())) == hitDome) {
            return;
        }
        Level level = projectile.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        SectProtectionDomeHandler.consumeBarrierHit(sl, hitDome, hitPos, SectProtectionDomeHandler.estimateProjectileDamage((Entity)projectile));
        if (projectile instanceof Fireball) {
            SectProtectionDomeHandler.recordExternalBarrierExplosion(sl, hitDome, hitPos);
            return;
        }
        event.setResult(Event.Result.DENY);
        projectile.discard();
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        FormationCorePlateBlockEntity attackerDome;
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide) {
            return;
        }
        FormationCorePlateBlockEntity victimDome = SectProtectionDomeHandler.domeContaining(victim.level(), victim.getX(), victim.getY(), victim.getZ());
        if (victimDome == null) {
            return;
        }
        Entity attacker = event.getSource().getEntity();
        if (attacker != null && (attackerDome = SectProtectionDomeHandler.domeContaining(attacker.level(), attacker.getX(), attacker.getY(), attacker.getZ())) == victimDome) {
            return;
        }
        if (event.getSource().is(DamageTypes.FALL) || event.getSource().is(DamageTypes.DROWN) || event.getSource().is(DamageTypes.IN_WALL) || event.getSource().is(DamageTypes.STARVE) || event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD) || event.getSource().is(DamageTypes.GENERIC_KILL)) {
            return;
        }
        if (!victimDome.isProtectedByDome(victim)) {
            return;
        }
        ItemTier tier = SectProtectionDomeHandler.sectTier(victimDome);
        if (tier == null) {
            return;
        }
        long qiPerDamage = FormationType.SECT_PROTECTION.sectProtectionQiPerDamage(tier);
        float incoming = event.getAmount();
        if (incoming <= 0.0f) {
            return;
        }
        long qiNeeded = (long)Math.ceil(incoming * (float)qiPerDamage);
        long pool = victimDome.getCurrentQi();
        if (pool >= qiNeeded) {
            victimDome.consumeQi(qiNeeded);
            SectProtectionDomeHandler.showDomeBossBar((ServerLevel)victim.level(), victimDome);
            event.setResult(Event.Result.DENY);
            SectProtectionDomeHandler.spawnRippleFx(victim, incoming);
            SectProtectionDomeHandler.applyHitPassives(victimDome, tier, attacker, victim, incoming);
        } else if (pool > 0L) {
            victimDome.consumeQi(pool);
            SectProtectionDomeHandler.showDomeBossBar((ServerLevel)victim.level(), victimDome);
            float absorbed = (float)((double)pool / (double)qiPerDamage);
            float remaining = Math.max(0.0f, incoming - absorbed);
            event.setAmount(remaining);
            SectProtectionDomeHandler.spawnBreakFx(victimDome);
            SectProtectionDomeHandler.applyHitPassives(victimDome, tier, attacker, victim, absorbed);
        }
    }

    private record DomeKey(ResourceKey<Level> dimension, BlockPos corePos) {
    }

    private static final class BossBarState {
        private final ServerBossEvent event;
        private long hideAtTick;

        private BossBarState(ServerBossEvent event, long hideAtTick) {
            this.event = event;
            this.hideAtTick = hideAtTick;
        }
    }

    public record BarrierHit(FormationCorePlateBlockEntity dome, Vec3 hitPos) {
    }

    private record BarrierExplosionMark(BlockPos corePos, Vec3 hitPos, long expiresAtTick) {
    }
}

