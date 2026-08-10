/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.DustParticleOptions
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
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
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerChangedDimensionEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.network.PacketDistributor
 *  net.minecraftforge.network.PacketDistributor$TargetPoint
 *  org.jetbrains.annotations.Nullable
 *  org.joml.Vector3f
 */
package com.friday.cultivation.event;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.cultivation.technique.TechniqueBonusHelper;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.event.BloodthirstCurseHandler;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.event.QingdiLongevityHandler;
import com.friday.cultivation.event.SectCombatHandler;
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.network.LifeBalanceVisualPacket;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.util.SpellDamageSourceHelper;
import com.friday.cultivation.util.SpellScalingHelper;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class LifeBalanceHandler {
    public static final int MARK_DURATION_TICKS = 600;
    private static final double TARGET_RANGE = 40.0;
    private static final double CHANNEL_MAX_RANGE = 48.0;
    private static final int CHANNEL_ACTIVATE_TICKS = 5;
    private static final long CHANNEL_QI_PER_TICK = 50L;
    private static final float TAP_BLEED_RATIO = 0.1f;
    private static final float TAP_DAMAGE_MULTIPLIER = 1.5f;
    private static final float CHANNEL_DAMAGE = 10.0f;
    private static final DustParticleOptions BLOOD_DUST = new DustParticleOptions(new Vector3f(0.92f, 0.04f, 0.08f), 1.18f);
    private static final Map<ResourceKey<Level>, Map<UUID, MarkState>> MARKS = new ConcurrentHashMap<ResourceKey<Level>, Map<UUID, MarkState>>();
    private static final Map<UUID, ChannelState> CHANNELS = new ConcurrentHashMap<UUID, ChannelState>();

    private LifeBalanceHandler() {
    }

    public static boolean hasTapTarget(ServerPlayer player) {
        return LifeBalanceHandler.raycastLivingTarget(player, 40.0) != null;
    }

    public static void castTap(ServerPlayer player) {
        LivingEntity target = LifeBalanceHandler.raycastLivingTarget(player, 40.0);
        if (target == null) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.taishang_life_balance.no_target"), true);
            return;
        }
        LifeBalanceHandler.castMark((LivingEntity)player, target, true);
    }

    public static boolean castMark(LivingEntity caster, LivingEntity target) {
        return LifeBalanceHandler.castMark(caster, target, false);
    }

    private static boolean castMark(LivingEntity caster, LivingEntity target, boolean showCasterMessage) {
        if (caster == null || target == null || !caster.isAlive() || !target.isAlive()) {
            return false;
        }
        if (!SoulStateHandler.canOrdinaryAffect((Entity)caster, (Entity)target)) {
            return false;
        }
        if (!SectCombatHandler.canApplyOffensiveEffect(caster, target)) {
            return false;
        }
        Level level = target.level();
        if (!(level instanceof ServerLevel)) {
            return false;
        }
        ServerLevel level2 = (ServerLevel)level;
        if (!caster.level().dimension().equals((Object)level2.dimension())) {
            return false;
        }
        long endTick = level2.getGameTime() + 600L;
        MARKS.computeIfAbsent((ResourceKey<Level>)level2.dimension(), ignored -> new ConcurrentHashMap()).put(target.getUUID(), new MarkState((ResourceKey<Level>)level2.dimension(), caster.getUUID(), target.getUUID(), endTick, level2.getGameTime() + 20L));
        LifeBalanceHandler.syncMarkedEntity(target, 600, true);
        LifeBalanceHandler.makeWanderingCultivatorRetaliate(target, caster);
        LifeBalanceHandler.spawnMarkTrail(level2, caster, target);
        LifeBalanceHandler.spawnBloodBurst(level2, target);
        level2.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.CHAIN_PLACE, SoundSource.HOSTILE, 0.8f, 0.75f);
        if (showCasterMessage && caster instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)caster;
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.taishang_life_balance.target_marked", (Object[])new Object[]{target.getDisplayName()}), true);
        }
        return true;
    }

    public static boolean beginChannel(ServerPlayer player, CultivationData data) {
        LivingEntity target = LifeBalanceHandler.raycastLivingTarget(player, 40.0);
        if (target == null) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.taishang_life_balance.no_target"), true);
            return false;
        }
        long tapCost = TechniqueBonusHelper.applySpellQiCostMultiplier((Player)player, Spell.TAISHANG_LIFE_BALANCE, Spell.TAISHANG_LIFE_BALANCE.qiCost());
        if (data.getCurrentQi() < tapCost) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.cast.no_qi", (Object[])new Object[]{Spell.TAISHANG_LIFE_BALANCE.displayName()}), true);
            return false;
        }
        ChannelState state = new ChannelState((ResourceKey<Level>)player.serverLevel().dimension(), player.getUUID(), target.getUUID(), player.position(), target.position(), player.serverLevel().getGameTime());
        CHANNELS.put(player.getUUID(), state);
        data.setChargingSpellId(Spell.TAISHANG_LIFE_BALANCE.id());
        data.setChargedQi(0L);
        data.setChargingEntityId(target.getId());
        CapabilityEvents.syncToClient(player);
        return true;
    }

    public static void tickChannel(ServerPlayer player, CultivationData data) {
        ChannelState state = CHANNELS.get(player.getUUID());
        if (state == null) {
            data.clearCharging();
            CapabilityEvents.syncToClient(player);
            return;
        }
        ServerLevel level = player.serverLevel();
        LivingEntity target = LifeBalanceHandler.findLiving(level, state.targetId);
        if (!LifeBalanceHandler.isValidChannelTarget(player, target, state)) {
            LifeBalanceHandler.stopChannel(player, data, state, target, true);
            return;
        }
        data.incrementChargingTicks();
        if (!state.active && data.getChargingTicks() >= 5) {
            LifeBalanceHandler.activateChannel(player, target, state);
        }
        if (!state.active) {
            CapabilityEvents.syncToClient(player);
            return;
        }
        long actualDrain = TechniqueBonusHelper.applySpellQiCostMultiplier((Player)player, Spell.TAISHANG_LIFE_BALANCE, 50L);
        if (actualDrain > 0L && data.getCurrentQi() < actualDrain) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.taishang_life_balance.channel_no_qi"), true);
            LifeBalanceHandler.stopChannel(player, data, state, target, true);
            return;
        }
        data.setCurrentQi(Math.max(0L, data.getCurrentQi() - Math.max(0L, actualDrain)));
        data.addChargedQi(50L);
        LifeBalanceHandler.makeWanderingCultivatorRetaliate(target, (LivingEntity)player);
        LifeBalanceHandler.freezeAt((LivingEntity)player, state.casterAnchor);
        LifeBalanceHandler.freezeAt(target, state.targetAnchor);
        long now = level.getGameTime();
        if (now - state.lastDrainTick >= 20L) {
            state.lastDrainTick = now;
            LifeBalanceHandler.drainChannelPair(player, target);
        }
        if (now - state.lastVisualTick >= 10L) {
            state.lastVisualTick = now;
            LifeBalanceHandler.syncChannel((LivingEntity)player, target, 24, true);
        }
        CapabilityEvents.syncToClient(player);
    }

    public static void finishChannel(ServerPlayer player, CultivationData data) {
        ChannelState state = CHANNELS.remove(player.getUUID());
        if (state == null) {
            data.clearCharging();
            CapabilityEvents.syncToClient(player);
            return;
        }
        LivingEntity target = LifeBalanceHandler.findLiving(player.serverLevel(), state.targetId);
        if (!state.active) {
            LifeBalanceHandler.finishAsTap(player, data, target);
        } else {
            LifeBalanceHandler.restoreTargetAi(state, target);
            if (target != null) {
                LifeBalanceHandler.syncChannel((LivingEntity)player, target, 0, false);
                LifeBalanceHandler.spawnBreakFx(player.serverLevel(), LifeBalanceHandler.midpoint((LivingEntity)player, target));
            }
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.taishang_life_balance.channel_released"), true);
        }
        data.clearCharging();
        CapabilityEvents.syncToClient(player);
    }

    private static void finishAsTap(ServerPlayer player, CultivationData data, @Nullable LivingEntity target) {
        if (target == null || !target.isAlive()) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.taishang_life_balance.no_target"), true);
            return;
        }
        if (!SoulStateHandler.canOrdinaryAffect((Entity)player, (Entity)target)) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.taishang_life_balance.no_target"), true);
            return;
        }
        long actualCost = TechniqueBonusHelper.applySpellQiCostMultiplier((Player)player, Spell.TAISHANG_LIFE_BALANCE, Spell.TAISHANG_LIFE_BALANCE.qiCost());
        if (data.getCurrentQi() < actualCost) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.cast.no_qi", (Object[])new Object[]{Spell.TAISHANG_LIFE_BALANCE.displayName()}), true);
            return;
        }
        data.setCurrentQi(data.getCurrentQi() - actualCost);
        LifeBalanceHandler.castMark((LivingEntity)player, target, true);
    }

    private static void activateChannel(ServerPlayer player, LivingEntity target, ChannelState state) {
        state.active = true;
        state.lastVisualTick = state.lastDrainTick = player.serverLevel().getGameTime();
        LifeBalanceHandler.makeWanderingCultivatorRetaliate(target, (LivingEntity)player);
        if (target instanceof Mob) {
            Mob mob = (Mob)target;
            if (!(target instanceof WanderingCultivatorEntity)) {
                state.targetWasNoAi = mob.isNoAi();
                state.targetNoAiApplied = true;
                mob.setNoAi(true);
                mob.getNavigation().stop();
            }
        }
        LifeBalanceHandler.syncChannel((LivingEntity)player, target, 24, true);
        LifeBalanceHandler.spawnBloodBurst(player.serverLevel(), (LivingEntity)player);
        LifeBalanceHandler.spawnBloodBurst(player.serverLevel(), target);
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 0.9f, 0.65f);
        player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.taishang_life_balance.channel_started", (Object[])new Object[]{target.getDisplayName()}), true);
    }

    private static void drainChannelPair(ServerPlayer caster, LivingEntity target) {
        float casterLoss = LifeBalanceHandler.drainCasterLife((LivingEntity)caster, 10.0f, 1.0f);
        if (casterLoss <= 0.0f) {
            return;
        }
        LifeBalanceHandler.applyTrueDamage(target, LifeBalanceHandler.scaleOffensiveDamage((LivingEntity)caster, casterLoss), (LivingEntity)caster);
        ServerLevel level = caster.serverLevel();
        LifeBalanceHandler.spawnBloodBurst(level, (LivingEntity)caster);
        LifeBalanceHandler.spawnBloodBurst(level, target);
        level.playSound(null, LifeBalanceHandler.midpoint((LivingEntity)caster, (LivingEntity)target).x, LifeBalanceHandler.midpoint((LivingEntity)caster, (LivingEntity)target).y, LifeBalanceHandler.midpoint((LivingEntity)caster, (LivingEntity)target).z, SoundEvents.WITHER_HURT, SoundSource.HOSTILE, 0.65f, 0.7f);
    }

    private static void stopChannel(ServerPlayer player, CultivationData data, ChannelState state, @Nullable LivingEntity target, boolean sync) {
        CHANNELS.remove(player.getUUID());
        LifeBalanceHandler.restoreTargetAi(state, target);
        if (target != null) {
            LifeBalanceHandler.syncChannel((LivingEntity)player, target, 0, false);
            LifeBalanceHandler.spawnBreakFx(player.serverLevel(), LifeBalanceHandler.midpoint((LivingEntity)player, target));
        }
        data.clearCharging();
        if (sync) {
            CapabilityEvents.syncToClient(player);
        }
    }

    private static void restoreTargetAi(ChannelState state, @Nullable LivingEntity target) {
        if (state.targetNoAiApplied && target instanceof Mob) {
            Mob mob = (Mob)target;
            mob.setNoAi(state.targetWasNoAi);
        }
    }

    private static void makeWanderingCultivatorRetaliate(LivingEntity target, LivingEntity attacker) {
        if (!(target instanceof WanderingCultivatorEntity)) {
            return;
        }
        WanderingCultivatorEntity npc = (WanderingCultivatorEntity)target;
        if (attacker == null || attacker == target || !attacker.isAlive()) {
            return;
        }
        if (!target.level().dimension().equals((Object)attacker.level().dimension())) {
            return;
        }
        if (!SoulStateHandler.canOrdinaryAffect((Entity)target, (Entity)attacker)) {
            return;
        }
        npc.setTarget(attacker);
        npc.setLastHurtByMob(attacker);
        if (attacker instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)attacker;
            npc.setLastHurtByPlayer((Player)player);
        }
        npc.getNavigation().stop();
    }

    private static boolean isValidChannelTarget(ServerPlayer player, @Nullable LivingEntity target, ChannelState state) {
        if (target == null || !target.isAlive() || !player.isAlive()) {
            return false;
        }
        if (!SoulStateHandler.canOrdinaryAffect((Entity)player, (Entity)target)) {
            return false;
        }
        if (!SectCombatHandler.canApplyOffensiveEffect((LivingEntity)player, target)) {
            return false;
        }
        if (!player.serverLevel().dimension().equals(state.dimension)) {
            return false;
        }
        if (!target.level().dimension().equals(state.dimension)) {
            return false;
        }
        return target.distanceToSqr((Entity)player) <= 2304.0;
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
        Map<UUID, MarkState> marks = MARKS.get(level2.dimension());
        if (marks == null || marks.isEmpty()) {
            return;
        }
        long now = level2.getGameTime();
        Iterator<Map.Entry<UUID, MarkState>> it = marks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, MarkState> entry = it.next();
            MarkState mark = entry.getValue();
            LivingEntity target = LifeBalanceHandler.findLiving(level2, mark.targetId);
            LivingEntity caster = LifeBalanceHandler.findLiving(level2, mark.casterId);
            if (!(target != null && caster != null && target.isAlive() && caster.isAlive() && now < mark.endTick && SoulStateHandler.canOrdinaryAffect((Entity)caster, (Entity)target) && SectCombatHandler.canApplyOffensiveEffect(caster, target))) {
                if (target != null) {
                    LifeBalanceHandler.syncMarkedEntity(target, 0, false);
                }
                it.remove();
                continue;
            }
            if (now < mark.nextBleedTick) continue;
            mark.nextBleedTick = now + 20L;
            float lost = LifeBalanceHandler.bleedCaster(caster, Math.max(1.0f, caster.getMaxHealth() * 0.1f));
            if (lost > 0.0f) {
                LifeBalanceHandler.applyTrueDamage(target, LifeBalanceHandler.scaleOffensiveDamage(caster, lost * 1.5f), caster);
                LifeBalanceHandler.spawnBloodBurst(level2, caster);
                LifeBalanceHandler.spawnBloodBurst(level2, target);
            }
            LifeBalanceHandler.syncMarkedEntity(target, LifeBalanceHandler.remainingTicks(mark.endTick, now), true);
        }
        if (marks.isEmpty()) {
            MARKS.remove(level2.dimension());
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer)player;
            CultivationData data = CultivationCapability.get((Player)player2).orElse(null);
            ChannelState state = CHANNELS.get(player2.getUUID());
            if (state != null && data != null) {
                LifeBalanceHandler.stopChannel(player2, data, state, LifeBalanceHandler.findLiving(player2.serverLevel(), state.targetId), false);
            } else {
                CHANNELS.remove(player2.getUUID());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer)player;
            CultivationData data = CultivationCapability.get((Player)player2).orElse(null);
            ChannelState state = CHANNELS.get(player2.getUUID());
            if (state != null && data != null) {
                LifeBalanceHandler.stopChannel(player2, data, state, null, true);
            } else {
                CHANNELS.remove(player2.getUUID());
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        Level level = dead.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        Map<UUID, MarkState> marks = MARKS.get(level2.dimension());
        if (marks != null) {
            marks.values().removeIf(mark -> mark.casterId.equals(dead.getUUID()) || mark.targetId.equals(dead.getUUID()));
        }
        if (dead instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)dead;
            CultivationData data = CultivationCapability.get((Player)player).orElse(null);
            ChannelState state = CHANNELS.get(player.getUUID());
            if (state != null && data != null) {
                LifeBalanceHandler.stopChannel(player, data, state, LifeBalanceHandler.findLiving(level2, state.targetId), true);
            }
        }
        for (ChannelState state : CHANNELS.values()) {
            ServerPlayer caster;
            CultivationData data;
            Entity entity;
            if (!state.targetId.equals(dead.getUUID()) || !((entity = level2.getEntity(state.casterId)) instanceof ServerPlayer) || (data = (CultivationData)CultivationCapability.get((Player)(caster = (ServerPlayer)entity)).orElse(null)) == null) continue;
            LifeBalanceHandler.stopChannel(caster, data, state, dead, true);
        }
    }

    private static float bleedCaster(LivingEntity caster, float amount) {
        float drained = LifeBalanceHandler.drainCasterLife(caster, amount, 0.0f);
        if (caster.isAlive() && caster.getHealth() <= 0.0f) {
            caster.die(caster.damageSources().magic());
        }
        return drained;
    }

    private static float drainCasterLife(LivingEntity caster, float amount, float minimumHealth) {
        float drained;
        if (amount <= 0.0f || !caster.isAlive()) {
            return 0.0f;
        }
        float remaining = amount;
        if ((remaining -= (drained = QingdiLongevityHandler.consumeAbsorptionShield(caster, remaining))) <= 0.0f) {
            return drained;
        }
        float healthLoss = Math.min(remaining, Math.max(0.0f, caster.getHealth() - minimumHealth));
        if (healthLoss <= 0.0f) {
            return drained;
        }
        caster.invulnerableTime = 0;
        caster.setHealth(Math.max(minimumHealth, caster.getHealth() - healthLoss));
        return drained + healthLoss;
    }

    private static float applyTrueDamage(LivingEntity target, float amount, LivingEntity caster) {
        if (amount <= 0.0f || target == null || !target.isAlive()) {
            return 0.0f;
        }
        float before = target.getHealth();
        if (before > amount) {
            target.setHealth(Math.max(0.0f, before - amount));
        } else {
            target.invulnerableTime = 0;
            target.setHealth(Math.max(1.0f, before));
            BloodthirstCurseHandler.hurtWithoutEventReward(target, SpellDamageSourceHelper.directSpell(caster), Math.max(1000.0f, amount + before));
        }
        float actualLost = Math.max(0.0f, before - Math.max(0.0f, target.getHealth()));
        BloodthirstCurseHandler.rewardFromActualDamage(caster, target, actualLost);
        return actualLost;
    }

    private static float scaleOffensiveDamage(LivingEntity caster, float baseDamage) {
        return SpellScalingHelper.scaledDamageFloat(caster, Spell.TAISHANG_LIFE_BALANCE, baseDamage);
    }

    private static void freezeAt(LivingEntity entity, Vec3 anchor) {
        entity.setDeltaMovement(Vec3.ZERO);
        entity.fallDistance = 0.0f;
        if (entity instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)entity;
            player.teleportTo(anchor.x, anchor.y, anchor.z);
        } else {
            entity.moveTo(anchor.x, anchor.y, anchor.z, entity.getYRot(), entity.getXRot());
            if (entity instanceof Mob) {
                Mob mob = (Mob)entity;
                mob.getNavigation().stop();
            }
        }
    }

    @Nullable
    private static LivingEntity raycastLivingTarget(ServerPlayer player, double maxDist) {
        ServerLevel level = player.serverLevel();
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(maxDist));
        double blockDist = LifeBalanceHandler.blockDistance(player, eye, end, maxDist);
        AABB scan = new AABB(eye, end).inflate(1.0);
        LivingEntity best = null;
        double bestDist = blockDist;
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, scan, e -> e != player && e.isAlive() && SoulStateHandler.canOrdinaryAffect((Entity)player, (Entity)e) && SectCombatHandler.canTargetOffensiveEffect((LivingEntity)player, e))) {
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

    @Nullable
    private static LivingEntity findLiving(ServerLevel level, UUID id) {
        LivingEntity living;
        Entity entity = level.getEntity(id);
        return entity instanceof LivingEntity ? (living = (LivingEntity)entity) : null;
    }

    private static void syncMarkedEntity(LivingEntity target, int durationTicks, boolean active) {
        ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> target), (Object)LifeBalanceVisualPacket.mark(target.getId(), durationTicks, active));
    }

    private static void syncChannel(LivingEntity caster, LivingEntity target, int durationTicks, boolean active) {
        Level level = caster.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        ModNetwork.CHANNEL.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(caster.getX(), caster.getY(), caster.getZ(), 128.0, level2.dimension())), (Object)LifeBalanceVisualPacket.link(caster.getId(), target.getId(), durationTicks, active));
    }

    private static void spawnMarkTrail(ServerLevel level, LivingEntity caster, LivingEntity target) {
        Vec3 start = caster.getEyePosition();
        Vec3 end = target.position().add(0.0, (double)target.getBbHeight() * 0.55, 0.0);
        Vec3 delta = end.multiply(start);
        for (int i = 1; i <= 18; ++i) {
            Vec3 p = start.add(delta.scale((double)i / 18.0));
            level.sendParticles((ParticleOptions)BLOOD_DUST, p.x, p.y, p.z, 2, 0.035, 0.035, 0.035, 0.0);
        }
    }

    private static void spawnBloodBurst(ServerLevel level, LivingEntity entity) {
        level.sendParticles((ParticleOptions)BLOOD_DUST, entity.getX(), entity.getY() + (double)entity.getBbHeight() * 0.52, entity.getZ(), 28, Math.max(0.25, (double)entity.getBbWidth() * 0.45), (double)entity.getBbHeight() * 0.3, Math.max(0.25, (double)entity.getBbWidth() * 0.45), 0.05);
    }

    private static void spawnBreakFx(ServerLevel level, Vec3 center) {
        level.sendParticles((ParticleOptions)BLOOD_DUST, center.x, center.y, center.z, 34, 0.45, 0.45, 0.45, 0.08);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.CHAIN_BREAK, SoundSource.PLAYERS, 0.9f, 0.72f);
    }

    private static Vec3 midpoint(LivingEntity a, LivingEntity b) {
        return a.position().add(0.0, (double)a.getBbHeight() * 0.5, 0.0).add(b.position().add(0.0, (double)b.getBbHeight() * 0.5, 0.0)).scale(0.5);
    }

    private static int remainingTicks(long endTick, long now) {
        return (int)Math.min(Integer.MAX_VALUE, Math.max(0L, endTick - now));
    }

    private static final class MarkState {
        private final ResourceKey<Level> dimension;
        private final UUID casterId;
        private final UUID targetId;
        private final long endTick;
        private long nextBleedTick;

        private MarkState(ResourceKey<Level> dimension, UUID casterId, UUID targetId, long endTick, long nextBleedTick) {
            this.dimension = dimension;
            this.casterId = casterId;
            this.targetId = targetId;
            this.endTick = endTick;
            this.nextBleedTick = nextBleedTick;
        }
    }

    private static final class ChannelState {
        private final ResourceKey<Level> dimension;
        private final UUID casterId;
        private final UUID targetId;
        private final Vec3 casterAnchor;
        private final Vec3 targetAnchor;
        private boolean active;
        private boolean targetNoAiApplied;
        private boolean targetWasNoAi;
        private long lastDrainTick;
        private long lastVisualTick;

        private ChannelState(ResourceKey<Level> dimension, UUID casterId, UUID targetId, Vec3 casterAnchor, Vec3 targetAnchor, long startTick) {
            this.dimension = dimension;
            this.casterId = casterId;
            this.targetId = targetId;
            this.casterAnchor = casterAnchor;
            this.targetAnchor = targetAnchor;
            this.lastDrainTick = startTick;
            this.lastVisualTick = startTick;
        }
    }
}

