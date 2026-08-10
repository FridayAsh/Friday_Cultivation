/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.friday.cultivation.spell.Spell
 *  com.friday.cultivation.entity.spell.GreatFireballEntity
 *  com.friday.cultivation.entity.spell.HeavenPiercingConeEntity
 *  com.friday.cultivation.entity.spell.MeteorEntity
 *  com.friday.cultivation.entity.spell.MushroomCloudEntity
 *  com.friday.cultivation.entity.spell.QiOrbEntity
 *  com.friday.cultivation.entity.spell.ShockwaveEntity
 *  com.friday.cultivation.entity.spell.SkySplittingSwordAuraEntity
 *  com.friday.cultivation.entity.SkyTrailEntity
 *  com.friday.cultivation.entity.spell.SwordAuraEntity
 *  com.friday.cultivation.entity.spell.SwordProjectileEntity
 *  com.friday.cultivation.entity.spell.XiaoxiangFireballEntity
 *  com.friday.cultivation.entity.npc.NpcSpellCaster
 *  com.friday.cultivation.event.SectCombatHandler
 *  com.friday.cultivation.event.TimeStasisHandler$Domain
 *  com.friday.cultivation.event.TimeStasisHandler$FrozenBlockSnapshot
 *  com.friday.cultivation.event.TimeStasisHandler$FrozenSnapshot
 *  com.friday.cultivation.event.TimeStasisHandler$SingleStasis
 *  com.friday.cultivation.network.ClientOnlyGlowPacket
 *  com.friday.cultivation.network.ModNetwork
 *  com.friday.cultivation.network.TimeStasisDomainPacket
 *  com.friday.cultivation.network.TimeStasisTargetPacket
 *  com.friday.cultivation.registry.ModEffects
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.TickEvent$LevelTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.entity.EntityJoinLevelEvent
 *  net.minecraftforge.event.entity.ProjectileImpactEvent
 *  net.minecraftforge.event.entity.living.LivingAttackEvent
 *  net.minecraftforge.event.entity.living.LivingEvent$LivingTickEvent
 *  net.minecraftforge.event.entity.player.AttackEntityEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$StartTracking
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$LeftClickBlock
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$RightClickBlock
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$RightClickItem
 *  net.minecraftforge.event.level.BlockEvent$BlockToolModificationEvent
 *  net.minecraftforge.event.level.BlockEvent$BreakEvent
 *  net.minecraftforge.event.level.BlockEvent$CropGrowEvent$Pre
 *  net.minecraftforge.event.level.BlockEvent$EntityPlaceEvent
 *  net.minecraftforge.event.level.BlockEvent$FarmlandTrampleEvent
 *  net.minecraftforge.event.level.BlockEvent$FluidPlaceBlockEvent
 *  net.minecraftforge.event.level.BlockEvent$NeighborNotifyEvent
 *  net.minecraftforge.eventbus.api.Event$Result
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.network.PacketDistributor
 */
package com.friday.cultivation.event;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.spell.Spell;
import com.friday.cultivation.entity.spell.GreatFireballEntity;
import com.friday.cultivation.entity.spell.HeavenPiercingConeEntity;
import com.friday.cultivation.entity.spell.MeteorEntity;
import com.friday.cultivation.entity.spell.MushroomCloudEntity;
import com.friday.cultivation.entity.spell.QiOrbEntity;
import com.friday.cultivation.entity.spell.ShockwaveEntity;
import com.friday.cultivation.entity.spell.SkySplittingSwordAuraEntity;
import com.friday.cultivation.entity.SkyTrailEntity;
import com.friday.cultivation.entity.spell.SwordAuraEntity;
import com.friday.cultivation.entity.spell.SwordProjectileEntity;
import com.friday.cultivation.entity.spell.XiaoxiangFireballEntity;
import com.friday.cultivation.entity.npc.NpcSpellCaster;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.event.SectCombatHandler;
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.event.TimeStasisHandler;
import com.friday.cultivation.event.TribulationHandler;
import com.friday.cultivation.network.ClientOnlyGlowPacket;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.TimeStasisDomainPacket;
import com.friday.cultivation.network.TimeStasisTargetPacket;
import com.friday.cultivation.registry.ModEffects;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

/*
 * Exception performing whole class analysis ignored.
 */
@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class TimeStasisHandler {
    private static final int DURATION_TICKS = 600;
    private static final long DOMAIN_CHARGE_QI = 10000L;
    private static final double TARGET_RANGE = 40.0;
    private static final double DOMAIN_RADIUS = 64.0;
    private static final int BLOCK_RESTORE_FLAGS = 50;
    private static final int MAX_DOMAIN_BLOCK_SCAN = 300000;
    private static final int MAX_DOMAIN_BLOCK_SNAPSHOTS = 20000;
    private static final Map<ResourceKey<Level>, List<Domain>> DOMAINS = new ConcurrentHashMap<ResourceKey<Level>, List<Domain>>();
    private static final Map<UUID, SingleStasis> SINGLE_STASIS = new ConcurrentHashMap<UUID, SingleStasis>();
    private static final Map<UUID, FrozenSnapshot> FROZEN = new ConcurrentHashMap<UUID, FrozenSnapshot>();

    private TimeStasisHandler() {
    }

    public static long domainChargeQi() {
        return 10000L;
    }

    public static void onChargeStarted(ServerPlayer player) {
        if (TimeStasisHandler.isEntityStopped((Entity)player) || TimeStasisHandler.isInsideAnyDomain(player.serverLevel(), player.position())) {
            TimeStasisHandler.clearSingleStasis((Entity)player);
            player.removeEffect((MobEffect)ModEffects.TIME_STASIS.get());
            player.addEffect(new MobEffectInstance((MobEffect)ModEffects.TIME_STASIS_FLOW.get(), 600, 0, false, true, true));
        }
        ClientOnlyGlowPacket.send((ServerPlayer)player, java.util.List.of(player.getId()), (int)20);
        TimeStasisHandler.thawEntity((Entity)player);
    }

    public static void release(ServerPlayer player, long chargedQi) {
        if (chargedQi >= 10000L) {
            TimeStasisHandler.castDomain(player);
        } else {
            TimeStasisHandler.castSingleOrRelease(player);
        }
    }

    public static void castSingleOrRelease(ServerPlayer player) {
        if (TimeStasisHandler.releaseStoppedEntity((LivingEntity)player)) {
            player.sendSystemMessage((Component)Component.translatable((String)"message.friday_cultivation.time_stasis.self_released"));
            return;
        }
        LivingEntity target = TimeStasisHandler.raycastLivingTarget(player, 40.0);
        if (target == null) {
            player.sendSystemMessage((Component)Component.translatable((String)"message.friday_cultivation.time_stasis.no_target"));
            return;
        }
        if (TimeStasisHandler.canActInTimeStasis((Entity)target)) {
            player.sendSystemMessage((Component)Component.translatable((String)"message.friday_cultivation.time_stasis.target_resisted", (Object[])new Object[]{target.getDisplayName()}));
            return;
        }
        if (!SectCombatHandler.canApplyOffensiveEffect((LivingEntity)player, (LivingEntity)target)) {
            return;
        }
        TimeStasisHandler.startSingleStasis(target, 600);
        TimeStasisHandler.spawnSingleTargetFx(player.serverLevel(), target);
        player.sendSystemMessage((Component)Component.translatable((String)"message.friday_cultivation.time_stasis.target_stopped", (Object[])new Object[]{target.getDisplayName()}));
        if (target instanceof ServerPlayer) {
            ServerPlayer targetPlayer = (ServerPlayer)target;
            targetPlayer.sendSystemMessage((Component)Component.translatable((String)"message.friday_cultivation.time_stasis.you_are_stopped"));
        }
    }

    public static boolean stopEntity(LivingEntity target, int durationTicks) {
        if (target == null || target.level().isClientSide || !target.isAlive()) {
            return false;
        }
        if (!SoulStateHandler.canOrdinaryAffect(null, (Entity)target)) {
            return false;
        }
        if (TimeStasisHandler.canActInTimeStasis((Entity)target)) {
            return false;
        }
        TimeStasisHandler.startSingleStasis(target, durationTicks);
        Level level = target.level();
        if (level instanceof ServerLevel) {
            ServerLevel level2 = (ServerLevel)level;
            TimeStasisHandler.spawnSingleTargetFx(level2, target);
        }
        return true;
    }

    public static boolean releaseStoppedEntity(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide) {
            return false;
        }
        if (!TimeStasisHandler.isEntityStopped((Entity)entity)) {
            return false;
        }
        TimeStasisHandler.clearSingleStasis((Entity)entity);
        entity.removeEffect((MobEffect)ModEffects.TIME_STASIS.get());
        entity.addEffect(new MobEffectInstance((MobEffect)ModEffects.TIME_STASIS_FLOW.get(), 600, 0, false, true, true));
        TimeStasisHandler.thawEntity((Entity)entity);
        Level level = entity.level();
        if (level instanceof ServerLevel) {
            ServerLevel level2 = (ServerLevel)level;
            TimeStasisHandler.spawnSingleTargetFx(level2, entity);
        }
        return true;
    }

    public static void clearEntityRuntime(Entity entity) {
        if (entity == null || entity.level().isClientSide) {
            return;
        }
        TimeStasisHandler.clearSingleStasis(entity);
        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)entity;
            living.removeEffect((MobEffect)ModEffects.TIME_STASIS.get());
            living.removeEffect((MobEffect)ModEffects.TIME_STASIS_FLOW.get());
        }
        TimeStasisHandler.thawEntity(entity);
        if (entity instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)entity;
            TimeStasisHandler.clearCasterDomains(player);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.level.isClientSide) {
            return;
        }
        Level level = event.level;
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        if (event.phase == TickEvent.Phase.START) {
            TimeStasisHandler.tickDomains(level2, true);
            return;
        }
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        TimeStasisHandler.tickDomains(level2, false);
        if (level2.getGameTime() % 10L == 0L) {
            TimeStasisHandler.cleanupFrozen(level2);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        WanderingCultivatorEntity npc;
        LivingEntity entity = event.getEntity();
        Level level = entity.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        MobEffectInstance legacyStasis = entity.getEffect((MobEffect)ModEffects.TIME_STASIS.get());
        if (legacyStasis != null) {
            entity.removeEffect((MobEffect)ModEffects.TIME_STASIS.get());
            if (!TimeStasisHandler.canActInTimeStasis((Entity)entity)) {
                TimeStasisHandler.startSingleStasis(entity, Math.max(1, legacyStasis.getDuration()));
            }
        }
        boolean singleStasis = TimeStasisHandler.isUnderSingleStasis(level2, (Entity)entity);
        boolean domainStasis = TimeStasisHandler.isInsideFreezingDomain(level2, (Entity)entity);
        if ((singleStasis || domainStasis) && entity instanceof WanderingCultivatorEntity && NpcSpellCaster.trySelfRescue((WanderingCultivatorEntity)(npc = (WanderingCultivatorEntity)entity))) {
            return;
        }
        if ((singleStasis || domainStasis) && !TimeStasisHandler.canActInTimeStasis((Entity)entity)) {
            TimeStasisHandler.freezeEntity((Entity)entity);
            return;
        }
        if (FROZEN.containsKey(entity.getUUID()) && !singleStasis && !domainStasis) {
            TimeStasisHandler.thawEntity((Entity)entity);
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity living;
        if (TribulationHandler.isTribulationDamage(event.getSource())) {
            return;
        }
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof LivingEntity && TimeStasisHandler.isFrozen(living = (LivingEntity)attacker)) {
            event.setCanceled(true);
            return;
        }
        if (TimeStasisHandler.isFrozen(event.getEntity()) && (attacker == null || TimeStasisHandler.isEntityStopped(attacker))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onAttackEntity(AttackEntityEvent event) {
        if (TimeStasisHandler.isEntityStopped((Entity)event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide) {
            return;
        }
        if (TimeStasisHandler.isEntityStopped((Entity)event.getEntity())) {
            event.setUseBlock(Event.Result.DENY);
            event.setUseItem(Event.Result.DENY);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getLevel().isClientSide) {
            return;
        }
        if (TimeStasisHandler.isEntityStopped((Entity)event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getLevel().isClientSide) {
            return;
        }
        if (TimeStasisHandler.isEntityStopped((Entity)event.getEntity())) {
            event.setUseBlock(Event.Result.DENY);
            event.setUseItem(Event.Result.DENY);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (TimeStasisHandler.isEntityStopped((Entity)event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        Entity entity = event.getEntity();
        if (entity != null && TimeStasisHandler.isEntityStopped(entity)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onBlockToolModification(BlockEvent.BlockToolModificationEvent event) {
        Player player = event.getPlayer();
        if (player != null && TimeStasisHandler.isEntityStopped((Entity)player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onFarmlandTrample(BlockEvent.FarmlandTrampleEvent event) {
        if (event.getEntity() != null && TimeStasisHandler.isEntityStopped(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onCropGrow(BlockEvent.CropGrowEvent.Pre event) {
        if (TimeStasisHandler.isBlockInsideStoppedTime(event.getLevel(), event.getPos())) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onFluidPlaceBlock(BlockEvent.FluidPlaceBlockEvent event) {
        if (TimeStasisHandler.isBlockInsideStoppedTime(event.getLevel(), event.getPos())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        if (TimeStasisHandler.isBlockInsideStoppedTime(event.getLevel(), event.getPos())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (TimeStasisHandler.isEntityStopped((Entity)event.getProjectile())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        Level level = event.getLevel();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        Entity entity = event.getEntity();
        if (TimeStasisHandler.shouldFreezeEntityNow(level2, entity)) {
            TimeStasisHandler.freezeEntity(entity);
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
        SingleStasis stasis = SINGLE_STASIS.get(target.getUUID());
        if (stasis == null || !stasis.dimension().equals((Object)level2.dimension())) {
            return;
        }
        long remaining = stasis.endTick() - level2.getGameTime();
        if (remaining <= 0L) {
            TimeStasisHandler.clearSingleStasis(target);
            return;
        }
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player2), (Object)new TimeStasisTargetPacket(target.getId(), (int)Math.min(Integer.MAX_VALUE, remaining), true));
    }

    private static void castDomain(ServerPlayer caster) {
        ServerLevel level = caster.serverLevel();
        long endTick = level.getGameTime() + 600L;
        Domain domain = new Domain(level.dimension(), caster.position(), 64.0, caster.getUUID(), endTick, level.getDayTime(), TimeStasisHandler.captureFrozenBlocks(level, caster.position(), 64.0));
        DOMAINS.computeIfAbsent((ResourceKey<Level>)level.dimension(), ignored -> new ArrayList()).add(domain);
        TimeStasisHandler.clearSingleStasis((Entity)caster);
        caster.removeEffect((MobEffect)ModEffects.TIME_STASIS.get());
        caster.addEffect(new MobEffectInstance((MobEffect)ModEffects.TIME_STASIS_FLOW.get(), 620, 0, false, true, true));
        TimeStasisHandler.thawEntity((Entity)caster);
        TimeStasisHandler.applyDomain(level, domain);
        TimeStasisHandler.spawnDomainFx(level, caster.position());
        ModNetwork.CHANNEL.send(PacketDistributor.DIMENSION.with(() -> ((ServerLevel)level).dimension()), (Object)new TimeStasisDomainPacket(caster.getX(), caster.getY(), caster.getZ(), 64.0, 600, caster.getId()));
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.4f, 0.55f);
        caster.sendSystemMessage((Component)Component.translatable((String)"message.friday_cultivation.time_stasis.domain_released"));
    }

    private static void tickDomains(ServerLevel level, boolean beforeWorldTick) {
        List<Domain> domains = DOMAINS.get(level.dimension());
        if (domains == null || domains.isEmpty()) {
            return;
        }
        long now = level.getGameTime();
        boolean removedAny = false;
        Iterator<Domain> it = domains.iterator();
        while (it.hasNext()) {
            Domain domain = it.next();
            if (now >= domain.endTick()) {
                it.remove();
                removedAny = true;
                continue;
            }
            if (beforeWorldTick) {
                TimeStasisHandler.applyDomain(level, domain);
                continue;
            }
            TimeStasisHandler.applyDomain(level, domain);
            TimeStasisHandler.restoreDomainBlocks(level, domain);
        }
        if (domains.isEmpty()) {
            DOMAINS.remove(level.dimension());
            TimeStasisHandler.cleanupFrozen(level);
        } else if (removedAny) {
            TimeStasisHandler.cleanupFrozen(level);
        }
    }

    private static void clearCasterDomains(ServerPlayer player) {
        Iterator<Map.Entry<ResourceKey<Level>, List<Domain>>> mapIt = DOMAINS.entrySet().iterator();
        while (mapIt.hasNext()) {
            Map.Entry<ResourceKey<Level>, List<Domain>> entry = mapIt.next();
            ServerLevel level = player.server.getLevel(entry.getKey());
            boolean removedAny = false;
            Iterator<Domain> it = entry.getValue().iterator();
            while (it.hasNext()) {
                Domain domain = it.next();
                if (!domain.casterId().equals(player.getUUID())) continue;
                it.remove();
                removedAny = true;
                if (level == null) continue;
                ModNetwork.CHANNEL.send(PacketDistributor.DIMENSION.with(() -> ((ServerLevel)level).dimension()), (Object)new TimeStasisDomainPacket(domain.center().x, domain.center().y, domain.center().z, domain.radius(), 0, player.getId()));
            }
            if (entry.getValue().isEmpty()) {
                mapIt.remove();
            }
            if (!removedAny || level == null) continue;
            TimeStasisHandler.cleanupFrozen(level);
        }
    }

    private static void applyDomain(ServerLevel level, Domain domain) {
        AABB box = new AABB(domain.center().x - domain.radius(), domain.center().y - domain.radius(), domain.center().z - domain.radius(), domain.center().x + domain.radius(), domain.center().y + domain.radius(), domain.center().z + domain.radius());
        double radiusSqr = domain.radius() * domain.radius();
        for (Entity entity : level.getEntities((Entity)null, box, e -> TimeStasisHandler.isFreezableEntity(e, domain, radiusSqr))) {
            TimeStasisHandler.freezeEntity(entity);
        }
    }

    public static boolean canPerformStoppedTimeAction(ServerPlayer player, Spell spell) {
        if (!TimeStasisHandler.isEntityStopped((Entity)player)) {
            return true;
        }
        return spell == Spell.TIME_STASIS;
    }

    public static boolean pauseEntityTickInStoppedTime(Entity entity) {
        boolean stopped;
        if (entity == null || entity.isRemoved() || TimeStasisHandler.canActInTimeStasis(entity)) {
            return false;
        }
        Level level = entity.level();
        if (!(level instanceof ServerLevel)) {
            return false;
        }
        ServerLevel level2 = (ServerLevel)level;
        boolean bl = stopped = TimeStasisHandler.isUnderSingleStasis(level2, entity) || TimeStasisHandler.isInsideFreezingDomain(level2, entity);
        if (stopped) {
            TimeStasisHandler.freezeEntity(entity);
            return true;
        }
        if (FROZEN.containsKey(entity.getUUID())) {
            TimeStasisHandler.thawEntity(entity);
        }
        return false;
    }

    public static boolean isEntityStopped(Entity entity) {
        boolean stopped;
        if (entity == null || entity.isRemoved() || TimeStasisHandler.canActInTimeStasis(entity)) {
            return false;
        }
        Level level = entity.level();
        if (!(level instanceof ServerLevel)) {
            return FROZEN.containsKey(entity.getUUID());
        }
        ServerLevel level2 = (ServerLevel)level;
        boolean bl = stopped = TimeStasisHandler.isUnderSingleStasis(level2, entity) || TimeStasisHandler.isInsideFreezingDomain(level2, entity);
        if (!stopped && FROZEN.containsKey(entity.getUUID())) {
            TimeStasisHandler.thawEntity(entity);
        }
        return stopped;
    }

    private static boolean isFreezableEntity(Entity entity, Domain domain, double radiusSqr) {
        ServerLevel level;
        Level level2;
        if (entity.isRemoved()) {
            return false;
        }
        if (entity.getUUID().equals(domain.casterId())) {
            return false;
        }
        if (!entity.level().dimension().equals((Object)domain.dimension())) {
            return false;
        }
        if (entity.position().distanceToSqr(domain.center()) > radiusSqr) {
            return false;
        }
        if (entity instanceof LivingEntity && (level2 = entity.level()) instanceof ServerLevel && !SoulStateHandler.canOrdinaryAffect((level = (ServerLevel)level2).getEntity(domain.casterId()), entity)) {
            return false;
        }
        return !TimeStasisHandler.canActInTimeStasis(entity);
    }

    private static boolean shouldFreezeEntityNow(ServerLevel level, Entity entity) {
        if (entity.isRemoved() || TimeStasisHandler.canActInTimeStasis(entity)) {
            return false;
        }
        return TimeStasisHandler.isInsideFreezingDomain(level, entity);
    }

    private static LivingEntity raycastLivingTarget(ServerPlayer player, double maxDist) {
        ServerLevel level = player.serverLevel();
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(maxDist));
        double blockDist = TimeStasisHandler.blockDistance(player, eye, end, maxDist);
        AABB scan = new AABB(eye, end).inflate(1.0);
        LivingEntity best = null;
        double bestDist = blockDist;
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, scan, e -> e != player && e.isAlive() && SoulStateHandler.canOrdinaryAffect((Entity)player, (Entity)e) && SectCombatHandler.canTargetOffensiveEffect((LivingEntity)player, (LivingEntity)e))) {
            double dist;
            Optional hit = entity.getBoundingBox().inflate(0.35).clip(eye, end);
            if (hit.isEmpty() || !((dist = eye.distanceToSqr((Vec3)hit.get())) < bestDist)) continue;
            bestDist = dist;
            best = entity;
        }
        return best;
    }

    private static double blockDistance(ServerPlayer player, Vec3 eye, Vec3 end, double maxDist) {
        BlockHitResult hit = player.serverLevel().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)player));
        return hit.getType() == HitResult.Type.MISS ? maxDist : eye.distanceToSqr(hit.getLocation());
    }

    private static boolean canActInTimeStasis(Entity entity) {
        LivingEntity living;
        Player player;
        if (entity instanceof Player && (player = (Player)entity).isPassenger()) {
            return true;
        }
        if (entity instanceof LivingEntity && (living = (LivingEntity)entity).hasEffect((MobEffect)ModEffects.TIME_STASIS_FLOW.get())) {
            return true;
        }
        if (entity instanceof ServerPlayer) {
            player = (ServerPlayer)entity;
            return CultivationCapability.get(player).map(data -> Spell.TIME_STASIS.id().equals(data.getChargingSpellId())).orElse(false);
        }
        return false;
    }

    private static boolean isFrozen(LivingEntity living) {
        if (TimeStasisHandler.canActInTimeStasis((Entity)living)) {
            return false;
        }
        Level level = living.level();
        if (level instanceof ServerLevel) {
            ServerLevel level2 = (ServerLevel)level;
            return TimeStasisHandler.isUnderSingleStasis(level2, (Entity)living) || TimeStasisHandler.isInsideFreezingDomain(level2, (Entity)living);
        }
        return FROZEN.containsKey(living.getUUID());
    }

    private static void startSingleStasis(LivingEntity entity, int durationTicks) {
        Level level = entity.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        int duration = Math.max(1, durationTicks);
        SINGLE_STASIS.put(entity.getUUID(), new SingleStasis(level2.dimension(), level2.getGameTime() + (long)duration));
        TimeStasisHandler.syncTargetStasis((Entity)entity, duration, true);
        TimeStasisHandler.freezeEntity((Entity)entity);
    }

    private static boolean isUnderSingleStasis(ServerLevel level, Entity entity) {
        SingleStasis stasis = SINGLE_STASIS.get(entity.getUUID());
        if (stasis == null) {
            return false;
        }
        if (!stasis.dimension().equals((Object)level.dimension()) || level.getGameTime() >= stasis.endTick()) {
            TimeStasisHandler.clearSingleStasis(entity);
            return false;
        }
        return true;
    }

    private static void clearSingleStasis(Entity entity) {
        SingleStasis removed = SINGLE_STASIS.remove(entity.getUUID());
        if (removed != null) {
            TimeStasisHandler.syncTargetStasis(entity, 0, false);
        }
    }

    private static void syncTargetStasis(Entity entity, int durationTicks, boolean frozen) {
        if (!(entity.level() instanceof ServerLevel)) {
            return;
        }
        ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), (Object)new TimeStasisTargetPacket(entity.getId(), durationTicks, frozen));
    }

    private static void freezeEntity(Entity entity) {
        if (entity.isRemoved() || TimeStasisHandler.canActInTimeStasis(entity)) {
            TimeStasisHandler.thawEntity(entity);
            return;
        }
        FrozenSnapshot snapshot = FROZEN.computeIfAbsent(entity.getUUID(), id -> FrozenSnapshot.capture((Entity)entity));
        if (!snapshot.dimension().equals((Object)entity.level().dimension())) {
            TimeStasisHandler.thawEntity(entity);
            return;
        }
        if (entity instanceof Mob) {
            Mob mob = (Mob)entity;
            mob.setNoAi(true);
        }
        entity.setNoGravity(true);
        entity.setDeltaMovement(TimeStasisHandler.shouldPreserveFrozenVelocity(entity) ? snapshot.deltaMovement() : Vec3.ZERO);
        entity.fallDistance = 0.0f;
        entity.tickCount = snapshot.tickCount();
        entity.setRemainingFireTicks(snapshot.fireTicks());
        entity.setRemainingFireTicks(snapshot.airSupply());
        if (entity instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)entity;
            player.connection.teleport(snapshot.x(), snapshot.y(), snapshot.z(), snapshot.yRot(), snapshot.xRot());
        } else {
            entity.teleportTo(snapshot.x(), snapshot.y(), snapshot.z());
            entity.getX(snapshot.yRot());
            entity.getZ(snapshot.xRot());
        }
        entity.hasImpulse = true;
    }

    private static boolean shouldPreserveFrozenVelocity(Entity entity) {
        return entity instanceof SwordAuraEntity || entity instanceof SwordProjectileEntity || entity instanceof SkySplittingSwordAuraEntity || entity instanceof MeteorEntity || entity instanceof HeavenPiercingConeEntity || entity instanceof GreatFireballEntity || entity instanceof XiaoxiangFireballEntity || entity instanceof ShockwaveEntity || entity instanceof MushroomCloudEntity || entity instanceof SkyTrailEntity || entity instanceof QiOrbEntity;
    }

    private static void thawEntity(Entity entity) {
        FrozenSnapshot snapshot = FROZEN.remove(entity.getUUID());
        if (snapshot == null) {
            return;
        }
        entity.setNoGravity(snapshot.noGravity());
        if (entity instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)entity;
            player.setDeltaMovement(Vec3.ZERO);
            if (!player.isSpectator() && !player.isPassenger() && player.onGround() && player.getAbilities().mayfly) {
                player.getAbilities().mayfly = false;
                player.onUpdateAbilities();
            }
        } else {
            entity.setDeltaMovement(snapshot.deltaMovement());
        }
        if (entity instanceof Mob) {
            Mob mob = (Mob)entity;
            if (snapshot.hasMobNoAi()) {
                mob.setNoAi(snapshot.mobNoAi());
            }
        }
        entity.fallDistance = 0.0f;
        entity.hasImpulse = true;
    }

    private static boolean isInsideFreezingDomain(ServerLevel level, Entity entity) {
        List<Domain> domains = DOMAINS.get(level.dimension());
        if (domains == null || domains.isEmpty()) {
            return false;
        }
        long now = level.getGameTime();
        for (Domain domain : domains) {
            if (now >= domain.endTick() || !TimeStasisHandler.isFreezableEntity(entity, domain, domain.radius() * domain.radius())) continue;
            return true;
        }
        return false;
    }

    private static boolean isInsideAnyDomain(ServerLevel level, Vec3 pos) {
        List<Domain> domains = DOMAINS.get(level.dimension());
        if (domains == null || domains.isEmpty()) {
            return false;
        }
        long now = level.getGameTime();
        for (Domain domain : domains) {
            if (now >= domain.endTick() || !(pos.distanceToSqr(domain.center()) <= domain.radius() * domain.radius())) continue;
            return true;
        }
        return false;
    }

    private static boolean isBlockInsideStoppedTime(LevelAccessor accessor, BlockPos pos) {
        if (!(accessor instanceof ServerLevel)) {
            return false;
        }
        ServerLevel level = (ServerLevel)accessor;
        List<Domain> domains = DOMAINS.get(level.dimension());
        if (domains == null || domains.isEmpty()) {
            return false;
        }
        long now = level.getGameTime();
        Vec3 center = Vec3.atCenterOf((Vec3i)pos);
        for (Domain domain : domains) {
            if (now >= domain.endTick() || !(center.distanceToSqr(domain.center()) <= domain.radius() * domain.radius())) continue;
            return true;
        }
        return false;
    }

    private static List<FrozenBlockSnapshot> captureFrozenBlocks(ServerLevel level, Vec3 center, double radius) {
        int minX = (int)Math.floor(center.x - radius);
        int maxX = (int)Math.ceil(center.x + radius);
        int minY = Math.max(level.getMinBuildHeight(), (int)Math.floor(center.y - radius));
        int maxY = Math.min(level.getMaxBuildHeight() - 1, (int)Math.ceil(center.y + radius));
        int minZ = (int)Math.floor(center.z - radius);
        int maxZ = (int)Math.ceil(center.z + radius);
        double radiusSqr = radius * radius;
        ArrayList<FrozenBlockSnapshot> snapshots = new ArrayList<FrozenBlockSnapshot>();
        int scanned = 0;
        for (BlockPos pos : BlockPos.betweenClosed((int)minX, (int)minY, (int)minZ, (int)maxX, (int)maxY, (int)maxZ)) {
            BlockEntity blockEntity;
            if (Vec3.atCenterOf((Vec3i)pos).distanceToSqr(center) > radiusSqr || !level.isLoaded(pos)) continue;
            if (++scanned > 300000) break;
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || !TimeStasisHandler.shouldSnapshotBlock(state, blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null)) continue;
            CompoundTag tag = blockEntity == null ? null : blockEntity.saveWithoutMetadata();
            snapshots.add(new FrozenBlockSnapshot(pos.immutable(), state, tag));
            if (snapshots.size() < 20000) continue;
            break;
        }
        return snapshots;
    }

    private static boolean shouldSnapshotBlock(BlockState state, BlockEntity blockEntity) {
        if (state.isAir()) {
            return false;
        }
        if (state.isRandomlyTicking()) {
            return true;
        }
        if (!state.getFluidState().isSource()) {
            return true;
        }
        if (blockEntity != null) {
            return true;
        }
        return state.is(Blocks.DIRT) || state.is(Blocks.COBBLESTONE) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.STONE) || state.is(Blocks.WATER) || state.is(Blocks.LAVA) || state.is(Blocks.PACKED_ICE) || state.is(Blocks.ICE) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.BLUE_ICE) || state.is(Blocks.GLASS) || state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.SNOW) || state.is(Blocks.CLAY);
    }

    private static void restoreDomainBlocks(ServerLevel level, Domain domain) {
        level.setDayTime(domain.dayTime());
        for (FrozenBlockSnapshot snapshot : domain.frozenBlocks()) {
            BlockEntity blockEntity;
            BlockPos pos = snapshot.pos();
            if (!level.isLoaded(pos)) continue;
            if (!level.getBlockState(pos).equals(snapshot.state())) {
                level.setBlock(pos, snapshot.state(), 50);
            }
            if (snapshot.blockEntityTag() == null || (blockEntity = level.getBlockEntity(pos)) == null) continue;
            blockEntity.load(snapshot.blockEntityTag());
            blockEntity.setChanged();
        }
    }

    private static void cleanupFrozen(ServerLevel level) {
        Iterator<Map.Entry<UUID, FrozenSnapshot>> it = FROZEN.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, FrozenSnapshot> entry = it.next();
            FrozenSnapshot snapshot = entry.getValue();
            if (!snapshot.dimension().equals((Object)level.dimension())) continue;
            Entity entity = level.getEntity(entry.getKey());
            if (entity == null || entity.isRemoved()) {
                SINGLE_STASIS.remove(entry.getKey());
                it.remove();
                continue;
            }
            boolean singleStasis = TimeStasisHandler.isUnderSingleStasis(level, entity);
            boolean domainStasis = TimeStasisHandler.isInsideFreezingDomain(level, entity);
            if (singleStasis || domainStasis) continue;
            TimeStasisHandler.thawEntity(entity);
        }
    }

    private static void spawnSingleTargetFx(ServerLevel level, LivingEntity target) {
        level.sendParticles((ParticleOptions)ParticleTypes.ASH, target.getX(), target.getY() + (double)target.getBbHeight() * 0.5, target.getZ(), 40, (double)target.getBbWidth() * 0.6, (double)target.getBbHeight() * 0.35, (double)target.getBbWidth() * 0.6, 0.02);
        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8f, 0.8f);
    }

    private static void spawnDomainFx(ServerLevel level, Vec3 center) {
        level.sendParticles((ParticleOptions)ParticleTypes.ASH, center.x, center.y + 1.0, center.z, 160, 28.8, 11.52, 28.8, 0.04);
        level.sendParticles((ParticleOptions)ParticleTypes.REVERSE_PORTAL, center.x, center.y + 1.0, center.z, 70, 22.4, 9.6, 22.4, 0.08);
        level.sendParticles((ParticleOptions)ParticleTypes.END_ROD, center.x, center.y + 1.2, center.z, 20, 0.8, 0.8, 0.8, 0.02);
    }

    /** 时间凝滞领域（照搬原模组） */
    private record Domain(ResourceKey<Level> dimension, Vec3 center, double radius, UUID casterId, long endTick, long dayTime, List<FrozenBlockSnapshot> frozenBlocks) {
    }

    /** 冻结方块快照（照搬原模组） */
    private record FrozenBlockSnapshot(BlockPos pos, BlockState state, CompoundTag blockEntityTag) {
    }

    /** 冻结实体快照（照搬原模组） */
    private record FrozenSnapshot(ResourceKey<Level> dimension, double x, double y, double z, float yRot, float xRot,
                                  boolean noGravity, boolean hasMobNoAi, boolean mobNoAi, int tickCount, int fireTicks, int airSupply, Vec3 deltaMovement) {
        private static FrozenSnapshot capture(Entity entity) {
            boolean isMob = entity instanceof net.minecraft.world.entity.Mob;
            boolean hasNoAi = entity instanceof net.minecraft.world.entity.Mob mob && mob.isNoAi();
            return new FrozenSnapshot(
                    entity.level().dimension(),
                    entity.getX(), entity.getY(), entity.getZ(),
                    entity.getYRot(), entity.getXRot(),
                    entity.isNoGravity(), isMob, hasNoAi,
                    entity.tickCount, entity.getRemainingFireTicks(), entity.getAirSupply(),
                    entity.getDeltaMovement());
        }
    }

    /** 单体凝滞（照搬原模组） */
    private record SingleStasis(ResourceKey<Level> dimension, long endTick) {
    }
}
