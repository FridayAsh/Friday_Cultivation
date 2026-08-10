/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.friday.cultivation.block.alchemy.AlchemyCoreBlockEntity
 *  com.friday.cultivation.block.formation.FormationCorePlateBlockEntity
 *  com.friday.cultivation.block.formation.FormationFlagBlock
 *  com.friday.cultivation.block.refining.RefiningCoreBlockEntity
 *  com.friday.cultivation.block.spirit.SpiritVeinCoreBlockEntity
 *  com.friday.cultivation.spell.Spell
 *  com.friday.cultivation.technique.TechniqueBonusHelper
 *  com.friday.cultivation.event.CapabilityEvents
 *  com.friday.cultivation.event.SectCombatHandler
 *  com.friday.cultivation.event.SpiritLockHandler$HitScan
 *  com.friday.cultivation.event.SpiritLockHandler$LockTarget
 *  com.friday.cultivation.network.ModNetwork
 *  com.friday.cultivation.network.SpiritLockVisualPacket
 *  com.friday.cultivation.registry.ModBlocks
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.particles.DustParticleOptions
 *  net.minecraft.core.particles.ItemParticleOption
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.ProjectileUtil
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.TickEvent$LevelTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerChangedDimensionEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedInEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$StartTracking
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.network.PacketDistributor
 *  net.minecraftforge.network.PacketDistributor$TargetPoint
 *  org.jetbrains.annotations.Nullable
 *  org.joml.Vector3f
 */
package com.friday.cultivation.event;

import com.friday.cultivation.block.alchemy.AlchemyCoreBlockEntity;
import com.friday.cultivation.block.formation.FormationCorePlateBlockEntity;
import com.friday.cultivation.block.formation.FormationFlagBlock;
import com.friday.cultivation.block.refining.RefiningCoreBlockEntity;
import com.friday.cultivation.block.spirit.SpiritVeinCoreBlockEntity;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.spell.Spell;
import com.friday.cultivation.technique.TechniqueBonusHelper;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.event.SectCombatHandler;
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.event.SpiritLockHandler;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.SpiritLockVisualPacket;
import com.friday.cultivation.registry.ModBlocks;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class SpiritLockHandler {
    public static final int DURATION_TICKS = 600;
    private static final double TARGET_RANGE = 32.0;
    private static final DustParticleOptions GOLD_DUST = new DustParticleOptions(new Vector3f(1.0f, 0.78f, 0.18f), 1.05f);
    private static final ItemParticleOption GOLD_SHARD = new ItemParticleOption(ParticleTypes.ITEM, new ItemStack((ItemLike)Items.GOLD_NUGGET));
    private static final Map<ResourceKey<Level>, Map<UUID, Long>> ENTITY_LOCKS = new ConcurrentHashMap<ResourceKey<Level>, Map<UUID, Long>>();
    private static final Map<ResourceKey<Level>, Map<BlockPos, Long>> BLOCK_LOCKS = new ConcurrentHashMap<ResourceKey<Level>, Map<BlockPos, Long>>();

    private SpiritLockHandler() {
    }

    public static boolean isEntityLocked(@Nullable Entity entity) {
        if (entity == null || entity.level().isClientSide) {
            return false;
        }
        Map<UUID, Long> locks = ENTITY_LOCKS.get(entity.level().dimension());
        if (locks == null) {
            return false;
        }
        Long until = locks.get(entity.getUUID());
        if (until == null) {
            return false;
        }
        if (until <= entity.level().getGameTime()) {
            locks.remove(entity.getUUID());
            return false;
        }
        return true;
    }

    public static boolean isBlockLocked(@Nullable Level level, @Nullable BlockPos pos) {
        if (level == null || pos == null || level.isClientSide) {
            return false;
        }
        Map<BlockPos, Long> locks = BLOCK_LOCKS.get(level.dimension());
        if (locks == null) {
            return false;
        }
        BlockPos key = pos.immutable();
        Long until = locks.get(key);
        if (until == null) {
            return false;
        }
        if (until <= level.getGameTime()) {
            locks.remove(key);
            return false;
        }
        return true;
    }

    public static boolean isLockableBlock(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof AlchemyCoreBlockEntity || be instanceof RefiningCoreBlockEntity || be instanceof FormationCorePlateBlockEntity || be instanceof SpiritVeinCoreBlockEntity) {
            return true;
        }
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof FormationFlagBlock || state.is((Block)ModBlocks.SPIRIT_VEIN_SPRING.get());
    }

    public static boolean hasLockTarget(ServerPlayer player) {
        return SpiritLockHandler.selectTarget(player, false) != null;
    }

    public static boolean hasUnlockTarget(ServerPlayer player) {
        return SpiritLockHandler.isEntityLocked((Entity)player) || SpiritLockHandler.selectTarget(player, true) != null;
    }

    public static boolean lockEntity(@Nullable LivingEntity target) {
        if (target == null || target.level().isClientSide || !target.isAlive()) {
            return false;
        }
        if (!SoulStateHandler.canOrdinaryAffect(null, (Entity)target)) {
            return false;
        }
        Level level = target.level();
        if (!(level instanceof ServerLevel)) {
            return false;
        }
        ServerLevel level2 = (ServerLevel)level;
        long until = level2.getGameTime() + 600L;
        ENTITY_LOCKS.computeIfAbsent((ResourceKey<Level>)level2.dimension(), ignored -> new ConcurrentHashMap()).put(target.getUUID(), until);
        SpiritLockHandler.syncEntityLock((Entity)target, 600, true);
        SpiritLockHandler.spawnLockBurst(level2, target.position().add(0.0, (double)target.getBbHeight() * 0.5, 0.0));
        level2.playSound(null, target.blockPosition(), SoundEvents.CHAIN_HIT, SoundSource.HOSTILE, 0.8f, 1.15f);
        return true;
    }

    public static boolean unlockEntity(@Nullable LivingEntity target) {
        if (target == null || target.level().isClientSide) {
            return false;
        }
        if (!SpiritLockHandler.isEntityLocked((Entity)target)) {
            return false;
        }
        SpiritLockHandler.removeEntityLock((Entity)target);
        SpiritLockHandler.syncEntityLock((Entity)target, 0, false);
        Level level = target.level();
        if (level instanceof ServerLevel) {
            ServerLevel level2 = (ServerLevel)level;
            SpiritLockHandler.spawnBreakFx(level2, target.position().add(0.0, (double)target.getBbHeight() * 0.5, 0.0));
        }
        return true;
    }

    public static void clearEntityLock(@Nullable Entity entity) {
        if (entity == null || entity.level().isClientSide) {
            return;
        }
        if (SpiritLockHandler.removeEntityLock(entity)) {
            SpiritLockHandler.syncEntityLock(entity, 0, false);
        }
    }

    public static boolean tryCastSelfUnlock(ServerPlayer player, CultivationData data) {
        if (player == null || data == null || !SpiritLockHandler.isEntityLocked((Entity)player)) {
            return false;
        }
        if (!data.hasSpell(Spell.SPIRIT_UNLOCK)) {
            return false;
        }
        long actualCost = TechniqueBonusHelper.applySpellQiCostMultiplier((Player)player, (Spell)Spell.SPIRIT_UNLOCK, (long)Spell.SPIRIT_UNLOCK.qiCost());
        if (data.getCurrentQi() < actualCost) {
            player.sendSystemMessage((Component)Component.translatable((String)"message.friday_cultivation.cast.no_qi", (Object[])new Object[]{Spell.SPIRIT_UNLOCK.displayName()}));
            return true;
        }
        data.setCurrentQi(data.getCurrentQi() - actualCost);
        SpiritLockHandler.removeEntityLock((Entity)player);
        SpiritLockHandler.syncEntityLock((Entity)player, 0, false);
        SpiritLockHandler.spawnBreakFx(player.serverLevel(), player.position().add(0.0, (double)player.getBbHeight() * 0.5, 0.0));
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.CHAIN_BREAK, SoundSource.PLAYERS, 0.85f, 1.35f);
        player.sendSystemMessage((Component)Component.translatable((String)"message.friday_cultivation.spirit_unlock.self_unlocked"));
        CapabilityEvents.syncToClient((ServerPlayer)player);
        return true;
    }

    public static void castLock(ServerPlayer player) {
        LockTarget target = SpiritLockHandler.selectTarget(player, false);
        if (target == null) {
            player.sendSystemMessage((Component)Component.translatable((String)"message.friday_cultivation.spirit_lock.no_target"));
            return;
        }
        ServerLevel level = player.serverLevel();
        long until = level.getGameTime() + 600L;
        if (target.entity != null) {
            if (!SectCombatHandler.canApplyOffensiveEffect((LivingEntity)player, (LivingEntity)target.entity)) {
                return;
            }
            ENTITY_LOCKS.computeIfAbsent((ResourceKey<Level>)level.dimension(), ignored -> new ConcurrentHashMap()).put(target.entity.getUUID(), until);
            SpiritLockHandler.syncEntityLock((Entity)target.entity, 600, true);
            SpiritLockHandler.spawnLockBurst(level, target.entity.position().add(0.0, (double)target.entity.getBbHeight() * 0.5, 0.0));
            player.sendSystemMessage((Component)Component.translatable((String)"message.friday_cultivation.spirit_lock.target_entity_locked", (Object[])new Object[]{target.entity.getDisplayName()}));
        } else if (target.blockPos != null) {
            BlockPos pos = target.blockPos.immutable();
            BLOCK_LOCKS.computeIfAbsent((ResourceKey<Level>)level.dimension(), ignored -> new ConcurrentHashMap()).put(pos, until);
            SpiritLockHandler.syncBlockLock(level, pos, 600, true);
            SpiritLockHandler.spawnLockBurst(level, Vec3.atCenterOf((Vec3i)pos));
            player.sendSystemMessage((Component)Component.translatable((String)"message.friday_cultivation.spirit_lock.target_block_locked", (Object[])new Object[]{SpiritLockHandler.blockName((Level)level, pos)}));
        }
        level.playSound(null, player.blockPosition(), SoundEvents.CHAIN_HIT, SoundSource.PLAYERS, 0.8f, 1.15f);
    }

    public static void castUnlock(ServerPlayer player) {
        if (SpiritLockHandler.isEntityLocked((Entity)player)) {
            SpiritLockHandler.removeEntityLock((Entity)player);
            SpiritLockHandler.syncEntityLock((Entity)player, 0, false);
            SpiritLockHandler.spawnBreakFx(player.serverLevel(), player.position().add(0.0, (double)player.getBbHeight() * 0.5, 0.0));
            player.sendSystemMessage((Component)Component.translatable((String)"message.friday_cultivation.spirit_unlock.self_unlocked"));
            return;
        }
        LockTarget target = SpiritLockHandler.selectTarget(player, true);
        if (target == null) {
            player.sendSystemMessage((Component)Component.translatable((String)"message.friday_cultivation.spirit_unlock.no_locked_target"));
            return;
        }
        ServerLevel level = player.serverLevel();
        if (target.entity != null) {
            SpiritLockHandler.removeEntityLock((Entity)target.entity);
            SpiritLockHandler.syncEntityLock((Entity)target.entity, 0, false);
            SpiritLockHandler.spawnBreakFx(level, target.entity.position().add(0.0, (double)target.entity.getBbHeight() * 0.5, 0.0));
            player.sendSystemMessage((Component)Component.translatable((String)"message.friday_cultivation.spirit_unlock.target_entity_unlocked", (Object[])new Object[]{target.entity.getDisplayName()}));
        } else if (target.blockPos != null) {
            BlockPos pos = target.blockPos.immutable();
            SpiritLockHandler.removeBlockLock((Level)level, pos);
            SpiritLockHandler.syncBlockLock(level, pos, 0, false);
            SpiritLockHandler.spawnBreakFx(level, Vec3.atCenterOf((Vec3i)pos));
            player.sendSystemMessage((Component)Component.translatable((String)"message.friday_cultivation.spirit_unlock.target_block_unlocked", (Object[])new Object[]{SpiritLockHandler.blockName((Level)level, pos)}));
        }
    }

    private static boolean removeEntityLock(Entity entity) {
        Map<UUID, Long> locks = ENTITY_LOCKS.get(entity.level().dimension());
        return locks != null && locks.remove(entity.getUUID()) != null;
    }

    private static void removeBlockLock(Level level, BlockPos pos) {
        Map<BlockPos, Long> locks = BLOCK_LOCKS.get(level.dimension());
        if (locks != null) {
            locks.remove(pos.immutable());
        }
    }

    @Nullable
    private static LockTarget selectTarget(ServerPlayer player, boolean requireLocked) {
        HitScan scan = SpiritLockHandler.scanHits(player);
        LockTarget best = null;
        if (scan.entity != null && (!requireLocked || SpiritLockHandler.isEntityLocked((Entity)scan.entity)) && (requireLocked || SoulStateHandler.canOrdinaryAffect((Entity)player, (Entity)scan.entity) && SectCombatHandler.canTargetOffensiveEffect((LivingEntity)player, (LivingEntity)scan.entity))) {
            best = new LockTarget(scan.entity, null, scan.entityDistanceSqr);
        }
        if (scan.blockPos != null && SpiritLockHandler.isLockableBlock((Level)player.serverLevel(), scan.blockPos) && (!requireLocked || SpiritLockHandler.isBlockLocked((Level)player.serverLevel(), scan.blockPos))) {
            LockTarget blockTarget = new LockTarget(null, scan.blockPos.immutable(), scan.blockDistanceSqr);
            if (best == null || blockTarget.distanceSqr < best.distanceSqr) {
                best = blockTarget;
            }
        }
        return best;
    }

    private static HitScan scanHits(ServerPlayer player) {
        LivingEntity living;
        Entity entity;
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 maxEnd = eye.add(look.scale(32.0));
        BlockHitResult blockHit = player.serverLevel().clip(new ClipContext(eye, maxEnd, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, (Entity)player));
        Vec3 entityEnd = blockHit.getType() == HitResult.Type.MISS ? maxEnd : blockHit.getLocation();
        AABB sweep = new AABB(eye, entityEnd).inflate(1.0);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult((Level)player.serverLevel(), (Entity)player, (Vec3)eye, (Vec3)entityEnd, (AABB)sweep, e -> {
            return e != player && e instanceof LivingEntity && ((LivingEntity)e).isAlive();
        }, (float)0.35f);
        LivingEntity entity2 = entityHit != null && (entity = entityHit.getEntity()) instanceof LivingEntity ? (living = (LivingEntity)entity) : null;
        double entityDistanceSqr = entityHit == null ? Double.MAX_VALUE : eye.distanceToSqr(entityHit.getLocation());
        BlockPos blockPos = blockHit.getType() == HitResult.Type.BLOCK ? blockHit.getBlockPos() : null;
        double blockDistanceSqr = blockHit.getType() == HitResult.Type.BLOCK ? eye.distanceToSqr(blockHit.getLocation()) : Double.MAX_VALUE;
        return new HitScan(entity2, entityDistanceSqr, blockPos, blockDistanceSqr);
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        Map<BlockPos, Long> blockLocks;
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Level level = event.level;
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        long now = level2.getGameTime();
        Map<UUID, Long> entityLocks = ENTITY_LOCKS.get(level2.dimension());
        if (entityLocks != null) {
            Iterator<Map.Entry<UUID, Long>> it = entityLocks.entrySet().iterator();
            while (it.hasNext()) {
                LivingEntity living;
                Entity entity;
                Map.Entry<UUID, Long> entry = it.next();
                if (entry.getValue() <= now) {
                    entity = level2.getEntity(entry.getKey());
                    if (entity != null) {
                        SpiritLockHandler.syncEntityLock(entity, 0, false);
                    }
                    it.remove();
                    continue;
                }
                if (now % 20L != 0L || !((entity = level2.getEntity(entry.getKey())) instanceof LivingEntity) || !(living = (LivingEntity)entity).isAlive()) continue;
                SpiritLockHandler.syncEntityLock((Entity)living, SpiritLockHandler.remainingTicks(entry.getValue(), now), true);
            }
        }
        if ((blockLocks = BLOCK_LOCKS.get(level2.dimension())) != null) {
            Iterator<Map.Entry<BlockPos, Long>> it = blockLocks.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<BlockPos, Long> entry = it.next();
                BlockPos pos = entry.getKey();
                if (entry.getValue() <= now) {
                    SpiritLockHandler.syncBlockLock(level2, pos, 0, false);
                    it.remove();
                    continue;
                }
                if (!level2.isLoaded(pos)) continue;
                if (!SpiritLockHandler.isLockableBlock((Level)level2, pos)) {
                    SpiritLockHandler.syncBlockLock(level2, pos, 0, false);
                    it.remove();
                    continue;
                }
                if (now % 20L != 0L) continue;
                SpiritLockHandler.syncBlockLock(level2, pos, SpiritLockHandler.remainingTicks(entry.getValue(), now), true);
            }
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        Entity target = event.getTarget();
        Level level = target.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        Map<UUID, Long> locks = ENTITY_LOCKS.get(level2.dimension());
        if (locks == null) {
            return;
        }
        Long until = locks.get(target.getUUID());
        if (until == null) {
            return;
        }
        int remaining = SpiritLockHandler.remainingTicks(until, level2.getGameTime());
        if (remaining <= 0) {
            locks.remove(target.getUUID());
            return;
        }
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player2), (Object)SpiritLockVisualPacket.entity((int)target.getId(), (int)remaining, (boolean)true));
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer)player;
            SpiritLockHandler.syncActiveLocksToPlayer(player2);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer)player;
            SpiritLockHandler.syncActiveLocksToPlayer(player2);
        }
    }

    private static void syncActiveLocksToPlayer(ServerPlayer player) {
        Map<BlockPos, Long> blockLocks;
        ServerLevel level = player.serverLevel();
        long now = level.getGameTime();
        Map<UUID, Long> entityLocks = ENTITY_LOCKS.get(level.dimension());
        if (entityLocks != null) {
            for (Map.Entry<UUID, Long> entry : entityLocks.entrySet()) {
                Entity entity;
                int remaining = SpiritLockHandler.remainingTicks(entry.getValue(), now);
                if (remaining <= 0 || (entity = level.getEntity(entry.getKey())) == null) continue;
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)SpiritLockVisualPacket.entity((int)entity.getId(), (int)remaining, (boolean)true));
            }
        }
        if ((blockLocks = BLOCK_LOCKS.get(level.dimension())) != null) {
            for (Map.Entry<BlockPos, Long> entry : blockLocks.entrySet()) {
                int remaining = SpiritLockHandler.remainingTicks(entry.getValue(), now);
                if (remaining <= 0) continue;
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)SpiritLockVisualPacket.block((BlockPos)entry.getKey(), (int)remaining, (boolean)true));
            }
        }
    }

    private static void syncEntityLock(Entity entity, int durationTicks, boolean locked) {
        if (!(entity.level() instanceof ServerLevel)) {
            return;
        }
        ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), (Object)SpiritLockVisualPacket.entity((int)entity.getId(), (int)durationTicks, (boolean)locked));
    }

    private static void syncBlockLock(ServerLevel level, BlockPos pos, int durationTicks, boolean locked) {
        ModNetwork.CHANNEL.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, 96.0, level.dimension())), (Object)SpiritLockVisualPacket.block((BlockPos)pos, (int)durationTicks, (boolean)locked));
    }

    private static int remainingTicks(long until, long now) {
        return (int)Math.min(Integer.MAX_VALUE, Math.max(0L, until - now));
    }

    private static Component blockName(Level level, BlockPos pos) {
        return level.getBlockState(pos).getBlock().getName();
    }

    private static void spawnLockBurst(ServerLevel level, Vec3 center) {
        level.sendParticles((ParticleOptions)GOLD_DUST, center.x, center.y, center.z, 36, 0.45, 0.45, 0.45, 0.04);
        level.sendParticles((ParticleOptions)GOLD_SHARD, center.x, center.y, center.z, 10, 0.35, 0.35, 0.35, 0.06);
    }

    private static void spawnBreakFx(ServerLevel level, Vec3 center) {
        level.sendParticles((ParticleOptions)GOLD_SHARD, center.x, center.y, center.z, 24, 0.52, 0.52, 0.52, 0.1);
        level.sendParticles((ParticleOptions)GOLD_DUST, center.x, center.y, center.z, 28, 0.55, 0.55, 0.55, 0.06);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.CHAIN_BREAK, SoundSource.PLAYERS, 0.85f, 1.25f);
    }

    private static void spawnEntityChains(ServerLevel level, LivingEntity target) {
        double height = Math.max(1.0, (double)target.getBbHeight());
        double radius = Math.max(0.36, (double)target.getBbWidth() * 0.72);
        double phase = (double)target.tickCount * 0.27;
        for (int strand = 0; strand < 3; ++strand) {
            double strandOffset = (double)strand * Math.PI * 2.0 / 3.0;
            for (int i = 0; i <= 10; ++i) {
                double t = (double)i / 10.0;
                double angle = phase + strandOffset + t * Math.PI * 2.2;
                double x = target.getX() + Math.cos(angle) * radius;
                double y = target.getY() + 0.18 + height * t;
                double z = target.getZ() + Math.sin(angle) * radius;
                level.sendParticles((ParticleOptions)GOLD_DUST, x, y, z, 1, 0.015, 0.015, 0.015, 0.0);
                if (i % 4 != 0) continue;
                level.sendParticles((ParticleOptions)GOLD_SHARD, x, y, z, 1, 0.02, 0.02, 0.02, 0.0);
            }
        }
    }

    private static void spawnBlockChains(ServerLevel level, BlockPos pos) {
        Vec3 center = Vec3.atCenterOf((Vec3i)pos);
        double phase = (double)level.getGameTime() * 0.18;
        for (int strand = 0; strand < 4; ++strand) {
            double offset = (double)strand * Math.PI * 0.5;
            for (int i = 0; i <= 12; ++i) {
                double t = (double)i / 12.0;
                double angle = phase + offset + t * Math.PI * 1.8;
                double x = center.x + Math.cos(angle) * 0.62;
                double y = (double)pos.getY() + 0.08 + t * 0.84;
                double z = center.z + Math.sin(angle) * 0.62;
                level.sendParticles((ParticleOptions)GOLD_DUST, x, y, z, 1, 0.012, 0.012, 0.012, 0.0);
                if (i % 5 != 0) continue;
                level.sendParticles((ParticleOptions)GOLD_SHARD, x, y, z, 1, 0.015, 0.015, 0.015, 0.0);
            }
        }
    }


    /** 锁定目标（照搬原模组） */
    private record LockTarget(LivingEntity entity, BlockPos blockPos, double distanceSqr) {
    }

    /** 命中扫描结果（照搬原模组） */
    private record HitScan(LivingEntity entity, double entityDistanceSqr, BlockPos blockPos, double blockDistanceSqr) {
    }
}
