/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Holder
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundSoundPacket
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.TickEvent$LevelTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.entity.living.LivingDeathEvent
 *  net.minecraftforge.event.entity.player.AttackEntityEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerChangedDimensionEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$EntityInteract
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$EntityInteractSpecific
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$LeftClickBlock
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$RightClickBlock
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$RightClickItem
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.network.PacketDistributor
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.event;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.entity.npc.SoulReaperEntity;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.event.SectCombatHandler;
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.SoulHookProgressPacket;
import com.friday.cultivation.network.SoulHookVisualPacket;
import com.friday.cultivation.registry.ModDimensions;
import com.friday.cultivation.worldgen.NaiheBridgeBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class SoulHookHandler {
    public static final int BIND_TICKS = 200;
    public static final int VORTEX_PULL_TICKS = 60;
    public static final int FINISH_TICKS = 60;
    private static final double TARGET_RANGE = 32.0;
    private static final double BREAK_RANGE = 72.0;
    private static final double VORTEX_HEIGHT = 2.55;
    private static final double VORTEX_TOUCH_RADIUS = 1.62;
    private static final double VORTEX_TOUCH_Y_TOLERANCE = 0.62;
    private static final double VORTEX_PULL_SPEED_MIN = 0.08;
    private static final double VORTEX_PULL_SPEED_MAX = 0.24;
    private static final int MAX_ACTIVE_TICKS = 340;
    private static final int VISUAL_REFRESH_TICKS = 5;
    private static final int PROGRESS_REFRESH_TICKS = 5;
    private static final int COMPLETION_VISUAL_LINGER_TICKS = 160;
    private static final Map<UUID, HookState> ACTIVE_BY_TARGET = new ConcurrentHashMap<UUID, HookState>();
    private static final Map<UUID, UUID> TARGET_BY_CASTER = new ConcurrentHashMap<UUID, UUID>();

    private SoulHookHandler() {
    }

    public static boolean hasTarget(ServerPlayer player) {
        return SoulHookHandler.raycastLivingTarget(player, 32.0) != null;
    }

    public static boolean cast(ServerPlayer player) {
        LivingEntity target = SoulHookHandler.raycastLivingTarget(player, 32.0);
        if (target == null) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.soul_hook_spell.no_target"), true);
            return false;
        }
        return SoulHookHandler.start((LivingEntity)player, target, false);
    }

    public static boolean start(LivingEntity caster, LivingEntity target) {
        return SoulHookHandler.start(caster, target, false);
    }

    public static boolean startEscort(LivingEntity caster, LivingEntity soul) {
        return SoulHookHandler.start(caster, soul, true);
    }

    public static boolean hasActive(LivingEntity caster, LivingEntity target) {
        if (caster == null || target == null) {
            return false;
        }
        HookState state = ACTIVE_BY_TARGET.get(target.getUUID());
        return state != null && state.casterId.equals(caster.getUUID());
    }

    public static boolean hasActiveTarget(@Nullable Entity target) {
        return target != null && ACTIVE_BY_TARGET.containsKey(target.getUUID());
    }

    public static boolean isActionLocked(@Nullable Entity entity) {
        if (entity == null || entity.level().isClientSide) {
            return false;
        }
        HookState state = ACTIVE_BY_TARGET.get(entity.getUUID());
        if (state == null) {
            return false;
        }
        Level level = entity.level();
        if (!(level instanceof ServerLevel)) {
            return false;
        }
        ServerLevel level2 = (ServerLevel)level;
        return level2.getGameTime() - state.startTick >= 200L;
    }

    public static boolean start(LivingEntity caster, LivingEntity target, boolean discardCasterOnDifu) {
        float f;
        if (caster == null || target == null || caster == target) {
            return false;
        }
        if (!caster.isAlive() || !target.isAlive()) {
            return false;
        }
        if (!SoulStateHandler.canSoulHookTarget((Entity)target)) {
            return false;
        }
        if (!SectCombatHandler.canApplyOffensiveEffect(caster, target)) {
            return false;
        }
        Level level = caster.level();
        if (!(level instanceof ServerLevel)) {
            return false;
        }
        ServerLevel level2 = (ServerLevel)level;
        if (!caster.level().dimension().equals((Object)target.level().dimension())) {
            return false;
        }
        SoulHookHandler.cancelExistingForCaster(level2, caster.getUUID());
        HookState oldTargetState = ACTIVE_BY_TARGET.get(target.getUUID());
        if (oldTargetState != null) {
            SoulHookHandler.cancelState(level2, oldTargetState, true);
        }
        long now = level2.getGameTime();
        if (target instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)target;
            f = player.getAbilities().getFlyingSpeed();
        } else {
            f = 0.0f;
        }
        float targetFlyingSpeed = f;
        HookState state = new HookState((ResourceKey<Level>)level2.dimension(), caster.getUUID(), target.getUUID(), now, now - 5L, now - 5L, targetFlyingSpeed, discardCasterOnDifu);
        ACTIVE_BY_TARGET.put(target.getUUID(), state);
        TARGET_BY_CASTER.put(caster.getUUID(), target.getUUID());
        SoulHookHandler.applyHookRestraint(target, state, 0.0, false);
        SoulHookHandler.syncVisual(level2, caster, target, state, 23, true, 0);
        SoulHookHandler.syncProgress(target, 200, false, true);
        SoulHookHandler.playSoulHookSound(level2, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 0.95f, 0.62f);
        return true;
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Level level = event.level;
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        long now = level2.getGameTime();
        for (HookState state : ACTIVE_BY_TARGET.values()) {
            ServerPlayer eligTarget;
            boolean playerDifuTarget;
            ServerPlayer preTarget;
            LivingEntity target;
            if (!state.dimension.equals((Object)level2.dimension())) continue;
            LivingEntity caster = SoulHookHandler.findLiving(level2, state.casterId);
            if (!SoulHookHandler.isValid(caster, target = SoulHookHandler.findLiving(level2, state.targetId), state)) {
                SoulHookHandler.cancelState(level2, state, true);
                continue;
            }
            long elapsed = now - state.startTick;
            if (elapsed > 340L || caster.distanceToSqr((Entity)target) > 5184.0) {
                SoulHookHandler.cancelState(level2, state, true);
                continue;
            }
            boolean vortexPhase = elapsed >= 200L;
            boolean justEnteredVortexPhase = vortexPhase && !state.vortexPhase;
            int finishTicks = Math.max(0, (int)(elapsed - 200L));
            state.chainCount = 1;
            state.vortexPhase = vortexPhase;
            if (vortexPhase) {
                SoulHookHandler.ensureVortexAnchor(target, state);
            }
            double bindProgress = Mth.clamp((double)((double)elapsed / 200.0), (double)0.0, (double)1.0);
            SoulHookHandler.applyHookRestraint(target, state, bindProgress, vortexPhase);
            if (vortexPhase) {
                SoulHookHandler.pullTargetTowardVortex(target, state, finishTicks);
            }
            SoulHookHandler.playStagedHookSound(level2, target, state, elapsed, vortexPhase);
            if (vortexPhase && !state.difuArrivalAttempted && target instanceof ServerPlayer && SoulHookHandler.isSoulEligibleForDifu(preTarget = (ServerPlayer)target)) {
                state.difuArrivalAttempted = true;
                state.difuArrivalPreparation = SoulStateHandler.beginDifuArrivalPreparation(preTarget);
                if (state.difuArrivalPreparation == null) {
                    state.difuFallbackArrival = SoulStateHandler.prepareEmergencyDifuArrival(preTarget);
                }
                SoulHookHandler.syncVisual(level2, caster, target, state, 200, true, (int)Math.min(Integer.MAX_VALUE, elapsed));
            }
            if (vortexPhase && state.difuArrivalPreparation != null && state.difuArrival == null && state.difuArrivalPreparation.tick()) {
                state.difuArrival = state.difuArrivalPreparation.arrival();
                state.difuArrivalPreparation = null;
            }
            int difuCountdownRemaining = Math.max(0, 60 - finishTicks);
            if (vortexPhase) {
                SoulHookHandler.sendDifuCountdown(target, state, difuCountdownRemaining);
            }
            boolean bl = playerDifuTarget = target instanceof ServerPlayer && SoulHookHandler.isSoulEligibleForDifu(eligTarget = (ServerPlayer)target);
            boolean readyToComplete = SoulHookHandler.hasTouchedVortex(target, state) || finishTicks >= 60;
            boolean bl2 = playerDifuTarget ? finishTicks >= 60 : readyToComplete;
            if (vortexPhase && readyToComplete) {
                SoulHookHandler.completeHook(level2, caster, target, state);
                continue;
            }
            if (justEnteredVortexPhase) {
                state.lastVisualTick = now;
                SoulHookHandler.syncVisual(level2, caster, target, state, 83, true, (int)Math.min(Integer.MAX_VALUE, elapsed));
                SoulHookHandler.syncProgress(target, 60, 60, true, true);
            } else if (now - state.lastVisualTick >= 5L) {
                state.lastVisualTick = now;
                SoulHookHandler.syncVisual(level2, caster, target, state, 23, true, (int)Math.min(Integer.MAX_VALUE, elapsed));
            }
            if (now - state.lastProgressTick < 5L) continue;
            state.lastProgressTick = now;
            if (vortexPhase) {
                SoulHookHandler.syncProgress(target, difuCountdownRemaining, 60, true, true);
                continue;
            }
            int remaining = Math.max(0, 200 - (int)Math.min(200L, elapsed));
            SoulHookHandler.syncProgress(target, remaining, 200, false, true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onAttackEntity(AttackEntityEvent event) {
        if (SoulHookHandler.isActionLocked((Entity)event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        SoulHookHandler.cancelPlayerActionIfHooked((PlayerInteractEvent)event);
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        SoulHookHandler.cancelPlayerActionIfHooked((PlayerInteractEvent)event);
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        SoulHookHandler.cancelPlayerActionIfHooked((PlayerInteractEvent)event);
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        SoulHookHandler.cancelPlayerActionIfHooked((PlayerInteractEvent)event);
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        SoulHookHandler.cancelPlayerActionIfHooked((PlayerInteractEvent)event);
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer)player;
            SoulHookHandler.restoreSpeedIfTarget(player2);
            SoulHookHandler.cancelForEntity(player2.serverLevel(), player2.getUUID(), true);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer)player;
            SoulHookHandler.restoreSpeedIfTarget(player2);
            SoulHookHandler.cancelForEntity(player2.serverLevel(), player2.getUUID(), true);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();
        if (level instanceof ServerLevel) {
            ServerLevel level2 = (ServerLevel)level;
            SoulHookHandler.cancelForEntity(level2, entity.getUUID(), true);
        }
    }

    private static void cancelPlayerActionIfHooked(PlayerInteractEvent event) {
        if (!SoulHookHandler.isActionLocked((Entity)event.getEntity())) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.FAIL);
    }

    @Nullable
    private static LivingEntity raycastLivingTarget(ServerPlayer player, double maxDist) {
        ServerLevel level = player.serverLevel();
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(maxDist));
        double blockDist = SoulHookHandler.blockDistance(player, eye, end, maxDist);
        AABB scan = new AABB(eye, end).inflate(1.0);
        LivingEntity best = null;
        double bestDist = blockDist;
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, scan, e -> e != player && e.isAlive() && SoulStateHandler.canSoulHookTarget((Entity)e) && SectCombatHandler.canTargetOffensiveEffect((LivingEntity)player, e))) {
            double dist;
            Optional hit = entity.getBoundingBox().inflate(0.35).clip(eye, end);
            if (hit.isEmpty() || !((dist = eye.distanceTo((Vec3)hit.get())) < bestDist)) continue;
            bestDist = dist;
            best = entity;
        }
        return best;
    }

    private static double blockDistance(ServerPlayer player, Vec3 eye, Vec3 end, double maxDist) {
        BlockHitResult hit = player.serverLevel().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)player));
        return hit.getType() == HitResult.Type.MISS ? maxDist : eye.distanceTo(hit.getLocation());
    }

    private static void applyHookRestraint(LivingEntity target, HookState state, double bindProgress, boolean actionLocked) {
        double speedFactor = actionLocked ? 0.0 : Mth.clamp((double)(0.5 * (1.0 - bindProgress)), (double)0.0, (double)0.5);
        int slowAmplifier = actionLocked ? 9 : Mth.clamp((int)(2 + (int)Math.floor(bindProgress * 7.0)), (int)2, (int)8);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 28, slowAmplifier, false, false, false));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 28, 1, false, false, false));
        if (actionLocked) {
            target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 28, 4, false, false, false));
        }
        Vec3 motion = target.getDeltaMovement();
        target.setDeltaMovement(actionLocked ? Vec3.ZERO : motion.scale(speedFactor));
        target.hurtMarked = true;
        target.fallDistance = 0.0f;
        if (target instanceof ServerPlayer) {
            float flightSpeed;
            ServerPlayer player = (ServerPlayer)target;
            float f = flightSpeed = actionLocked ? 0.0f : (float)((double)state.targetFlyingSpeed * speedFactor);
            if (Math.abs(player.getAbilities().getFlyingSpeed() - flightSpeed) > 1.0E-4f) {
                player.getAbilities().setFlyingSpeed(flightSpeed);
                player.onUpdateAbilities();
            }
            if (actionLocked) {
                player.setSprinting(false);
                player.stopUsingItem();
            }
        }
        if (target instanceof Mob) {
            Mob mob = (Mob)target;
            mob.getNavigation().stop();
            mob.setTarget(null);
            mob.setLastHurtByMob(null);
            mob.setAggressive(false);
        }
    }

    private static void restoreTargetMobility(@Nullable LivingEntity target, HookState state) {
        if (!(target instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer)target;
        if (Math.abs(player.getAbilities().getFlyingSpeed() - state.targetFlyingSpeed) > 1.0E-4f) {
            player.getAbilities().setFlyingSpeed(state.targetFlyingSpeed);
            player.onUpdateAbilities();
        }
    }

    private static void restoreSpeedIfTarget(ServerPlayer player) {
        HookState state = ACTIVE_BY_TARGET.get(player.getUUID());
        if (state != null) {
            SoulHookHandler.restoreTargetMobility((LivingEntity)player, state);
        }
    }

    private static void pullTargetTowardVortex(LivingEntity target, HookState state, int finishTicks) {
        if (!state.vortexAnchorSet) {
            return;
        }
        Vec3 center = new Vec3(state.vortexX, state.vortexY, state.vortexZ);
        Vec3 head = target.position().add(0.0, (double)target.getBbHeight() * 0.92, 0.0);
        Vec3 delta = center.multiply(head);
        double distance = delta.length();
        if (distance < 0.08) {
            return;
        }
        double progress = Mth.clamp((double)((double)finishTicks / 60.0), (double)0.0, (double)1.0);
        double speed = Mth.lerp((double)progress, (double)0.08, (double)0.24);
        Vec3 pull = delta.normalize().scale(Math.min(speed, distance * 0.42));
        target.setDeltaMovement(pull);
        target.hurtMarked = true;
        target.fallDistance = 0.0f;
    }

    private static boolean hasTouchedVortex(LivingEntity target, HookState state) {
        if (!state.vortexAnchorSet) {
            return false;
        }
        Vec3 head = target.position().add(0.0, (double)target.getBbHeight() * 0.92, 0.0);
        double dx = head.x - state.vortexX;
        double dz = head.z - state.vortexZ;
        boolean insideDisk = dx * dx + dz * dz <= 2.6244000000000005;
        boolean highEnough = head.y >= state.vortexY - 0.62;
        return insideDisk && highEnough;
    }

    private static void ensureVortexAnchor(LivingEntity target, HookState state) {
        if (state.vortexAnchorSet) {
            return;
        }
        state.vortexX = target.getX();
        state.vortexY = target.getY() + (double)target.getBbHeight() * 0.52 + 2.55;
        state.vortexZ = target.getZ();
        state.vortexAnchorSet = true;
    }

    private static void playStagedHookSound(ServerLevel level, LivingEntity target, HookState state, long elapsed, boolean vortexPhase) {
        int soundSecond = (int)(elapsed / 20L);
        if (soundSecond == state.lastSoundSecond) {
            return;
        }
        state.lastSoundSecond = soundSecond;
        if (!vortexPhase) {
            SoulHookHandler.playSoulHookSound(level, target.getX(), target.getY(), target.getZ(), SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 0.62f, 0.68f);
        } else {
            SoulHookHandler.playSoulHookSound(level, target.getX(), target.getY(), target.getZ(), SoundEvents.PORTAL_AMBIENT, SoundSource.PLAYERS, 0.72f, 0.56f);
        }
    }

    private static void completeHook(ServerLevel level, LivingEntity caster, LivingEntity target, HookState state) {
        WanderingCultivatorEntity npc;
        ACTIVE_BY_TARGET.remove(state.targetId, state);
        TARGET_BY_CASTER.remove(state.casterId, state.targetId);
        SoulHookHandler.syncVisual(level, caster, target, state, 160, true, (int)Math.min(Integer.MAX_VALUE, level.getGameTime() - state.startTick));
        SoulHookHandler.syncProgress(target, 0, true, false);
        SoulHookHandler.restoreTargetMobility(target, state);
        SoulHookHandler.playSoulHookSound(level, target.getX(), target.getY(), target.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 0.48f);
        if (target instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)target;
            if (SoulHookHandler.isSoulEligibleForDifu(player)) {
                boolean sent;
                BlockPos arrival = state.difuArrival;
                if (arrival == null && state.difuArrivalPreparation != null && (arrival = SoulStateHandler.completeDifuArrivalPreparation(state.difuArrivalPreparation)) != null) {
                    state.difuArrival = arrival;
                    state.difuArrivalPreparation = null;
                }
                if (arrival == null) {
                    arrival = state.difuFallbackArrival;
                }
                if (arrival == null) {
                    arrival = SoulStateHandler.prepareEmergencyDifuArrival(player);
                }
                boolean bl = sent = arrival != null && SoulStateHandler.sendToDifuAt(player, arrival);
                if (sent) {
                    if (state.discardCasterOnDifu && caster instanceof SoulReaperEntity) {
                        SoulReaperEntity reaper = (SoulReaperEntity)caster;
                        reaper.discard();
                    }
                } else {
                    SoulHookHandler.syncVisual(level, caster, target, state, 1, false, (int)Math.min(Integer.MAX_VALUE, level.getGameTime() - state.startTick));
                }
            }
            return;
        }
        if (target instanceof WanderingCultivatorEntity && SoulHookHandler.isNpcSoulEligibleForDifu(npc = (WanderingCultivatorEntity)target)) {
            boolean sent = SoulStateHandler.sendNpcSoulToDifu(npc);
            if (sent) {
                if (state.discardCasterOnDifu && caster instanceof SoulReaperEntity) {
                    SoulReaperEntity reaper = (SoulReaperEntity)caster;
                    reaper.discard();
                }
            } else {
                SoulHookHandler.syncVisual(level, caster, target, state, 1, false, (int)Math.min(Integer.MAX_VALUE, level.getGameTime() - state.startTick));
            }
        }
    }

    private static boolean isSoulEligibleForDifu(ServerPlayer player) {
        if (player.level().dimension() == ModDimensions.DIFU) {
            return false;
        }
        return CultivationCapability.get((Player)player).map(data -> data.isSoulState()).orElse(false);
    }

    private static boolean isNpcSoulEligibleForDifu(WanderingCultivatorEntity npc) {
        return npc.level().dimension() != ModDimensions.DIFU && npc.isNpcSoulState();
    }

    @Nullable
    private static LivingEntity findLiving(ServerLevel level, UUID id) {
        LivingEntity living;
        Entity entity = level.getEntity(id);
        return entity instanceof LivingEntity ? (living = (LivingEntity)entity) : null;
    }

    private static boolean isValid(@Nullable LivingEntity caster, @Nullable LivingEntity target, HookState state) {
        if (caster == null || target == null || !caster.isAlive() || !target.isAlive()) {
            return false;
        }
        if (!caster.level().dimension().equals(state.dimension)) {
            return false;
        }
        if (!SoulStateHandler.canSoulHookTarget((Entity)target)) {
            return false;
        }
        return target.level().dimension().equals(state.dimension);
    }

    private static void syncVisual(ServerLevel level, LivingEntity caster, LivingEntity target, HookState state, int durationTicks, boolean active, int elapsedTicks) {
        double x = (caster.getX() + target.getX()) * 0.5;
        double y = (caster.getY() + target.getY()) * 0.5;
        double z = (caster.getZ() + target.getZ()) * 0.5;
        double radius = Math.max(96.0, (double)caster.distanceTo((Entity)target) + 48.0);
        double radiusSq = radius * radius;
        SoulHookVisualPacket packet = SoulHookHandler.visualPacket(caster, target, state, durationTicks, active, elapsedTicks);
        boolean sentToTargetPlayer = false;
        for (ServerPlayer viewer : level.players()) {
            if (viewer.distanceToSqr(x, y, z) > radiusSq || active && !SoulStateHandler.canPerceiveSoulSystem(viewer)) continue;
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> viewer), (Object)packet);
            if (viewer != target) continue;
            sentToTargetPlayer = true;
        }
        if (active && state.vortexPhase && target instanceof ServerPlayer) {
            ServerPlayer targetPlayer = (ServerPlayer)target;
            if (!sentToTargetPlayer) {
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> targetPlayer), (Object)packet);
            }
        }
    }

    private static SoulHookVisualPacket visualPacket(LivingEntity caster, LivingEntity target, HookState state, int durationTicks, boolean active, int elapsedTicks) {
        return new SoulHookVisualPacket(caster.getId(), target.getId(), Math.max(1, durationTicks), state.chainCount, state.vortexPhase, active, elapsedTicks, state.vortexAnchorSet, state.vortexX, state.vortexY, state.vortexZ);
    }

    private static void sendDifuCountdown(LivingEntity target, HookState state, int remainingTicks) {
        if (!(target instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer)target;
        int seconds = (Math.max(0, remainingTicks) + 19) / 20;
        if (seconds == state.lastDifuCountdownSecond) {
            return;
        }
        state.lastDifuCountdownSecond = seconds;
        if (seconds <= 0) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.go_difu.teleporting"), true);
        } else {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.go_difu.countdown", (Object[])new Object[]{seconds}), true);
        }
    }

    private static void syncProgress(LivingEntity target, int remainingTicks, boolean locked, boolean active) {
        SoulHookHandler.syncProgress(target, remainingTicks, locked ? 60 : 200, locked, active);
    }

    private static void syncProgress(LivingEntity target, int remainingTicks, int totalTicks, boolean locked, boolean active) {
        if (!(target instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer)target;
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)new SoulHookProgressPacket(active, Math.max(0, remainingTicks), Math.max(1, totalTicks), locked));
    }

    private static void cancelExistingForCaster(ServerLevel level, UUID casterId) {
        UUID oldTargetId = TARGET_BY_CASTER.get(casterId);
        if (oldTargetId == null) {
            return;
        }
        HookState state = ACTIVE_BY_TARGET.get(oldTargetId);
        if (state != null) {
            SoulHookHandler.cancelState(level, state, true);
        }
    }

    private static void cancelForEntity(ServerLevel level, UUID id, boolean sync) {
        HookState targetState;
        HookState casterState;
        UUID targetId = TARGET_BY_CASTER.get(id);
        if (targetId != null && (casterState = ACTIVE_BY_TARGET.get(targetId)) != null) {
            SoulHookHandler.cancelState(level, casterState, sync);
        }
        if ((targetState = ACTIVE_BY_TARGET.get(id)) != null) {
            SoulHookHandler.cancelState(level, targetState, sync);
        }
    }

    private static void cancelState(ServerLevel level, HookState state, boolean sync) {
        ACTIVE_BY_TARGET.remove(state.targetId, state);
        TARGET_BY_CASTER.remove(state.casterId, state.targetId);
        LivingEntity caster = SoulHookHandler.findLiving(level, state.casterId);
        LivingEntity target = SoulHookHandler.findLiving(level, state.targetId);
        SoulHookHandler.restoreTargetMobility(target, state);
        if (target != null) {
            SoulHookHandler.syncProgress(target, 0, false, false);
        }
        if (!sync) {
            return;
        }
        if (caster != null && target != null) {
            state.chainCount = 0;
            state.vortexPhase = false;
            state.vortexAnchorSet = false;
            SoulHookHandler.syncVisual(level, caster, target, state, 1, false, 0);
            SoulHookHandler.playSoulHookSound(level, target.getX(), target.getY(), target.getZ(), SoundEvents.CHAIN_BREAK, SoundSource.PLAYERS, 0.65f, 0.72f);
        }
    }

    private static void playSoulHookSound(ServerLevel level, double x, double y, double z, SoundEvent sound, SoundSource source, float volume, float pitch) {
        double range = Math.max(16.0, volume > 1.0f ? (double)volume * 16.0 : 16.0);
        double rangeSq = range * range;
        ClientboundSoundPacket packet = new ClientboundSoundPacket(Holder.direct(sound), source, x, y, z, volume, pitch, level.random.nextLong());
        for (ServerPlayer viewer : level.players()) {
            if (!SoulStateHandler.canPerceiveSoulSystem(viewer) || viewer.distanceToSqr(x, y, z) > rangeSq) continue;
            viewer.connection.send((Packet)packet);
        }
    }

    private static final class HookState {
        private final ResourceKey<Level> dimension;
        private final UUID casterId;
        private final UUID targetId;
        private final long startTick;
        private final float targetFlyingSpeed;
        private final boolean discardCasterOnDifu;
        private long lastVisualTick;
        private long lastProgressTick;
        private int lastSoundSecond = -1;
        private int chainCount = 1;
        private boolean vortexPhase = false;
        private boolean vortexAnchorSet = false;
        private double vortexX;
        private double vortexY;
        private double vortexZ;
        private boolean difuArrivalAttempted = false;
        private NaiheBridgeBuilder.ArrivalPreparation difuArrivalPreparation = null;
        private BlockPos difuArrival = null;
        private BlockPos difuFallbackArrival = null;
        private int lastDifuCountdownSecond = Integer.MIN_VALUE;

        private HookState(ResourceKey<Level> dimension, UUID casterId, UUID targetId, long startTick, long lastVisualTick, long lastProgressTick, float targetFlyingSpeed, boolean discardCasterOnDifu) {
            this.dimension = dimension;
            this.casterId = casterId;
            this.targetId = targetId;
            this.startTick = startTick;
            this.lastVisualTick = lastVisualTick;
            this.lastProgressTick = lastProgressTick;
            this.targetFlyingSpeed = targetFlyingSpeed;
            this.discardCasterOnDifu = discardCasterOnDifu;
        }
    }
}

