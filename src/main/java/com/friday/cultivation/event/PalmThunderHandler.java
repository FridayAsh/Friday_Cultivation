/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.event.entity.living.LivingEvent$LivingTickEvent
 *  net.minecraftforge.event.entity.player.AttackEntityEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerChangedDimensionEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerRespawnEvent
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$LeftClickBlock
 *  net.minecraftforge.eventbus.api.Event$Result
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.network.PacketDistributor
 *  net.minecraftforge.network.PacketDistributor$TargetPoint
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.event;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.PhysiqueBonusHelper;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.cultivation.technique.TechniqueBonusHelper;
import com.friday.cultivation.entity.PalmThunderOrbEntity;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.event.SectCombatHandler;
import com.friday.cultivation.event.SectProtectionDomeHandler;
import com.friday.cultivation.event.SoulHookHandler;
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.event.SpiritLockHandler;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.PalmThunderVisualPacket;
import com.friday.cultivation.registry.ModEffects;
import com.friday.cultivation.util.SpellDamageSourceHelper;
import com.friday.cultivation.util.SpellScalingHelper;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class PalmThunderHandler {
    public static final float EXPLOSION_RADIUS = 4.25f;
    private static final int STUN_TICKS = 60;
    private static final int TAP_RELEASE_TICKS = 5;
    private static final int ARMING_TICKS = 40;
    private static final int CHANNEL_DRAIN_INTERVAL_TICKS = 4;
    private static final int CHANNEL_DRAIN_BASE_QI = 10;
    private static final int CHANNEL_VISUAL_REFRESH_TICKS = 6;
    private static final int ARMED_VISUAL_REFRESH_TICKS = 10;
    private static final int CHANNEL_EXPLOSION_COOLDOWN_TICKS = 8;
    private static final int DOT_INTERVAL_TICKS = 10;
    private static final double PROJECTILE_SPEED = 1.45;
    private static final double VISUAL_RANGE = 96.0;
    private static final Map<UUID, StunState> ACTIVE_STUNS = new ConcurrentHashMap<UUID, StunState>();
    private static final Map<UUID, ArmedState> ARMED_CHANNELS = new ConcurrentHashMap<UUID, ArmedState>();
    private static final Map<UUID, Long> CHANNEL_COOLDOWN_UNTIL = new ConcurrentHashMap<UUID, Long>();

    private PalmThunderHandler() {
    }

    public static void beginChannel(ServerPlayer player, CultivationData data) {
        if (PalmThunderHandler.dismissIfArmed(player)) {
            return;
        }
        if (data.getCurrentQi() <= 0L) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.cast.no_qi", (Object[])new Object[]{Spell.PALM_THUNDER.displayNameForRealm(data.getRealm())}), true);
            return;
        }
        data.setChargingSpellId(Spell.PALM_THUNDER.id());
        data.setChargedQi(0L);
        CapabilityEvents.syncToClient(player);
        PalmThunderHandler.syncChannelVisual(player.serverLevel(), (LivingEntity)player, 12, 0.0f, false);
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.35f, 1.75f);
    }

    public static void tickChannel(ServerPlayer player, CultivationData data) {
        boolean shouldDrain;
        if (!PalmThunderHandler.isPalmThunderCharging(data)) {
            return;
        }
        if (!player.isAlive() || player.isRemoved()) {
            PalmThunderHandler.clearPreparing(player, data, false);
            return;
        }
        data.incrementChargingTicks();
        int preparingTicks = PalmThunderHandler.preparingTicks(data);
        boolean bl = shouldDrain = preparingTicks > 0 && preparingTicks <= 40 && (preparingTicks - 1) % 4 == 0;
        if (shouldDrain) {
            long actualDrain = TechniqueBonusHelper.applySpellQiCostMultiplier((Player)player, Spell.PALM_THUNDER, 10L);
            if (data.getCurrentQi() < actualDrain) {
                PalmThunderHandler.clearPreparing(player, data, true);
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.palm_thunder.no_qi_channel"), true);
                return;
            }
            data.setCurrentQi(data.getCurrentQi() - actualDrain);
        }
        data.setChargedQi(Math.min(40, preparingTicks));
        if (preparingTicks > 40) {
            PalmThunderHandler.armChannel(player, data);
            return;
        }
        PalmThunderHandler.syncChannelVisual(player.serverLevel(), (LivingEntity)player, 12, PalmThunderHandler.preparationProgress(data), false);
        CapabilityEvents.syncToClient(player);
        if (player.tickCount % 15 == 0) {
            player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.2f, 1.85f);
        }
    }

    public static void finishChannelOrTap(ServerPlayer player, CultivationData data) {
        int heldTicks = data.getChargingTicks();
        if (heldTicks <= 5) {
            PalmThunderHandler.tryPayTapCostAndSpawn(player, data);
        }
        PalmThunderHandler.clearPreparing(player, data, true);
    }

    private static void tryPayTapCostAndSpawn(ServerPlayer player, CultivationData data) {
        long actualCost = TechniqueBonusHelper.applySpellQiCostMultiplier((Player)player, Spell.PALM_THUNDER, Spell.PALM_THUNDER.qiCost());
        if (data.getCurrentQi() < actualCost) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.cast.no_qi", (Object[])new Object[]{Spell.PALM_THUNDER.displayNameForRealm(data.getRealm())}), true);
            return;
        }
        data.setCurrentQi(data.getCurrentQi() - actualCost);
        PalmThunderHandler.spawnProjectile((LivingEntity)player, player.getLookAngle(), SpellScalingHelper.scaledDamageFloat((LivingEntity)player, Spell.PALM_THUNDER, Spell.PALM_THUNDER.damage()));
        PhysiqueBonusHelper.onSpellCast(player, Spell.PALM_THUNDER);
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 0.75f, 1.42f);
        player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.cast.active_done", (Object[])new Object[]{Spell.PALM_THUNDER.displayNameForRealm(data.getRealm())}), true);
    }

    public static void spawnProjectile(LivingEntity caster, Vec3 direction, float damage) {
        Level level = caster.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        Vec3 look = PalmThunderHandler.safeDirection(direction);
        PalmThunderOrbEntity orb = new PalmThunderOrbEntity((Level)level2, caster, damage);
        Vec3 hand = PalmThunderHandler.handPosition(caster, look);
        orb.setPos(hand.x, hand.y, hand.z);
        orb.setDeltaMovement(look.scale(1.45));
        level2.addFreshEntity((Entity)orb);
    }

    public static void explode(ServerLevel level, @Nullable Entity source, @Nullable LivingEntity owner, Vec3 center, float damage, float radius) {
        PalmThunderHandler.explode(level, source, owner, center, damage, radius, true);
    }

    public static void explode(ServerLevel level, @Nullable Entity source, @Nullable LivingEntity owner, Vec3 center, float damage, float radius, boolean touchBarrierArea) {
        float safeDamage = Math.max(1.0f, damage);
        float safeRadius = Math.max(1.0f, radius);
        PalmThunderHandler.syncBurstVisual(level, center, safeRadius);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.25f, 1.25f);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1.6f, 1.65f);
        if (touchBarrierArea) {
            SectProtectionDomeHandler.onSpellAreaTouchedBarrier(level, center, safeRadius, (Entity)(owner != null ? owner : source), safeDamage);
        }
        AABB box = new AABB(center, center).inflate((double)safeRadius);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box, Entity::isAlive)) {
            Vec3 targetCenter;
            double dist;
            if (target == owner || SectProtectionDomeHandler.isEntityProtectedByOwnDome((Entity)target) || !SoulStateHandler.canOrdinaryAffect((Entity)owner, (Entity)target) || !SectCombatHandler.canApplyOffensiveEffect(owner, target) || (dist = (targetCenter = target.position().add(0.0, (double)target.getBbHeight() * 0.5, 0.0)).distanceTo(center)) > (double)safeRadius) continue;
            float falloff = (float)Math.max(0.45, 1.0 - dist / Math.max(1.0, (double)safeRadius) * 0.55);
            target.hurt(source != null ? SpellDamageSourceHelper.indirectSpell(source, owner) : (owner != null ? SpellDamageSourceHelper.directSpell(owner) : level.damageSources().magic()), safeDamage * falloff);
            PalmThunderHandler.applyStun(level, owner, target, safeDamage * 0.15f * falloff);
        }
    }

    private static void applyStun(ServerLevel level, @Nullable LivingEntity owner, LivingEntity target, float dotDamage) {
        target.addEffect(new MobEffectInstance((MobEffect)ModEffects.PALM_THUNDER_STUN.get(), 60, 0, false, true, true));
        target.addEffect(new MobEffectInstance((MobEffect)ModEffects.ROOTED.get(), 60, 0, false, true, true));
        target.addEffect(new MobEffectInstance((MobEffect)ModEffects.GRAVITY_SUPPRESSION.get(), 60, 0, false, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 4, false, true, true));
        ACTIVE_STUNS.put(target.getUUID(), new StunState(level.dimension().location().toString(), owner == null ? null : owner.getUUID(), level.getGameTime() + 60L, Math.max(1.0f, dotDamage), level.getGameTime()));
        PalmThunderHandler.stopMotionAndFlight(target);
    }

    public static boolean tryChannelExplosion(ServerPlayer player, Vec3 center) {
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        if (data != null && PalmThunderHandler.isPalmThunderCharging(data)) {
            return true;
        }
        if (data == null || !PalmThunderHandler.isArmed(player)) {
            return false;
        }
        if (data.getCurrentQi() <= 0L) {
            PalmThunderHandler.dismissArmed(player, false);
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.palm_thunder.no_qi_channel"), true);
            return true;
        }
        long now = player.serverLevel().getGameTime();
        long blockedUntil = CHANNEL_COOLDOWN_UNTIL.getOrDefault(player.getUUID(), 0L);
        if (blockedUntil > now) {
            return true;
        }
        CHANNEL_COOLDOWN_UNTIL.entrySet().removeIf(entry -> (Long)entry.getValue() + 200L < now);
        CHANNEL_COOLDOWN_UNTIL.put(player.getUUID(), now + 8L);
        float damage = SpellScalingHelper.scaledDamageFloat((LivingEntity)player, Spell.PALM_THUNDER, Spell.PALM_THUNDER.damage());
        PalmThunderHandler.explode(player.serverLevel(), (Entity)player, (LivingEntity)player, center, damage, 4.25f);
        PhysiqueBonusHelper.onSpellCast(player, Spell.PALM_THUNDER);
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 0.65f, 1.35f);
        return true;
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        Entity target = event.getTarget();
        Vec3 center = target.position().add(0.0, (double)target.getBbHeight() * 0.5, 0.0);
        if (PalmThunderHandler.tryChannelExplosion(player2, center)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getLevel().isClientSide) {
            return;
        }
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        Vec3 center = Vec3.atCenterOf((Vec3i)event.getPos());
        if (PalmThunderHandler.tryChannelExplosion(player2, center)) {
            event.setUseBlock(Event.Result.DENY);
            event.setUseItem(Event.Result.DENY);
            event.setCanceled(true);
        }
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
        if (!PalmThunderHandler.isArmed(player2)) {
            return;
        }
        PalmThunderHandler.tickArmed(player2);
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer)player;
            PalmThunderHandler.dismissArmed(player2, false);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer)player;
            PalmThunderHandler.dismissArmed(player2, false);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer)player;
            PalmThunderHandler.dismissArmed(player2, false);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) {
            return;
        }
        if (!entity.hasEffect((MobEffect)ModEffects.PALM_THUNDER_STUN.get())) {
            ACTIVE_STUNS.remove(entity.getUUID());
            return;
        }
        PalmThunderHandler.stopMotionAndFlight(entity);
        Level level = entity.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        StunState state = ACTIVE_STUNS.get(entity.getUUID());
        if (state == null || !state.dimension.equals(level2.dimension().location().toString()) || level2.getGameTime() >= state.endTick) {
            ACTIVE_STUNS.remove(entity.getUUID());
            return;
        }
        if (level2.getGameTime() - state.lastDamageTick >= 10L) {
            LivingEntity living;
            Entity entity2;
            LivingEntity owner = state.ownerId == null ? null : (entity2 = level2.getEntity(state.ownerId)) instanceof LivingEntity ? (living = (LivingEntity)entity2) : null;
            LivingEntity livingEntity = state.ownerId == null ? null : (entity2 = level2.getEntity(state.ownerId)) instanceof LivingEntity ? (living = (LivingEntity)entity2) : null;
            if (SoulStateHandler.canOrdinaryAffect(owner, (Entity)entity) && SectCombatHandler.canApplyOffensiveEffect(owner, entity)) {
                entity.hurt(owner != null ? SpellDamageSourceHelper.directSpell(owner) : level2.damageSources().magic(), state.damagePerTick);
            } else {
                ACTIVE_STUNS.remove(entity.getUUID());
            }
            state.lastDamageTick = level2.getGameTime();
        }
    }

    private static boolean isPalmThunderCharging(CultivationData data) {
        return data != null && data.isCharging() && Spell.PALM_THUNDER.id().equals(data.getChargingSpellId());
    }

    private static void clearPreparing(ServerPlayer player, CultivationData data, boolean stopVisual) {
        data.clearCharging();
        CapabilityEvents.syncToClient(player);
        if (stopVisual) {
            PalmThunderHandler.syncChannelStop(player.serverLevel(), (LivingEntity)player);
        }
    }

    private static int preparingTicks(CultivationData data) {
        return Math.max(0, data.getChargingTicks() - 5);
    }

    private static float preparationProgress(CultivationData data) {
        return Math.min(1.0f, (float)PalmThunderHandler.preparingTicks(data) / 40.0f);
    }

    private static void armChannel(ServerPlayer player, CultivationData data) {
        data.clearCharging();
        ARMED_CHANNELS.put(player.getUUID(), new ArmedState(player.level().dimension().location().toString()));
        CapabilityEvents.syncToClient(player);
        PalmThunderHandler.syncChannelVisual(player.serverLevel(), (LivingEntity)player, 18, 1.0f, true);
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.45f, 1.65f);
        player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.palm_thunder.armed"), true);
    }

    public static boolean dismissIfArmed(ServerPlayer player) {
        if (!PalmThunderHandler.isArmed(player)) {
            return false;
        }
        PalmThunderHandler.dismissArmed(player, true);
        return true;
    }

    public static boolean isArmed(ServerPlayer player) {
        return ARMED_CHANNELS.containsKey(player.getUUID());
    }

    private static void tickArmed(ServerPlayer player) {
        ArmedState state = ARMED_CHANNELS.get(player.getUUID());
        if (state == null) {
            return;
        }
        String dimension = player.level().dimension().location().toString();
        if (!state.dimension.equals(dimension) || !player.isAlive() || player.isRemoved() || SoulHookHandler.isActionLocked((Entity)player) || SpiritLockHandler.isEntityLocked((Entity)player) || player.hasEffect((MobEffect)ModEffects.MERIDIAN_FROZEN.get())) {
            PalmThunderHandler.dismissArmed(player, false);
            return;
        }
        if (player.tickCount % 4 == 0) {
            CultivationData data = CultivationCapability.get((Player)player).orElse(null);
            if (data == null) {
                PalmThunderHandler.dismissArmed(player, false);
                return;
            }
            long actualDrain = TechniqueBonusHelper.applySpellQiCostMultiplier((Player)player, Spell.PALM_THUNDER, 10L);
            if (data.getCurrentQi() < actualDrain) {
                PalmThunderHandler.dismissArmed(player, false);
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.palm_thunder.no_qi_channel"), true);
                return;
            }
            data.setCurrentQi(data.getCurrentQi() - actualDrain);
            CapabilityEvents.syncToClient(player);
        }
        if (player.tickCount % 6 == 0) {
            PalmThunderHandler.syncChannelVisual(player.serverLevel(), (LivingEntity)player, 18, 1.0f, true);
        }
        if (player.tickCount % 20 == 0) {
            player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.22f, 1.95f);
        }
    }

    private static void dismissArmed(ServerPlayer player, boolean showMessage) {
        if (ARMED_CHANNELS.remove(player.getUUID()) == null) {
            return;
        }
        CHANNEL_COOLDOWN_UNTIL.remove(player.getUUID());
        PalmThunderHandler.syncChannelStop(player.serverLevel(), (LivingEntity)player);
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.45f, 1.35f);
        if (showMessage) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.palm_thunder.dismissed"), true);
        }
    }

    private static void stopMotionAndFlight(LivingEntity entity) {
        Vec3 v = entity.getDeltaMovement();
        entity.setDeltaMovement(0.0, Math.min(v.y - 0.14, -0.12), 0.0);
        if (entity instanceof Mob) {
            Mob mob = (Mob)entity;
            mob.getNavigation().stop();
            mob.setTarget(null);
        }
        if (entity instanceof Player) {
            Player player = (Player)entity;
            if (player.getAbilities().flying) {
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
        }
        entity.hurtMarked = true;
        entity.hasImpulse = true;
    }

    private static Vec3 handPosition(LivingEntity caster, Vec3 look) {
        Vec3 right = new Vec3(-look.z, 0.0, look.x);
        if (right.lengthSqr() < 1.0E-6) {
            right = new Vec3(1.0, 0.0, 0.0);
        }
        right = right.normalize();
        return caster.getEyePosition().add(look.scale(0.82)).add(right.scale(0.28)).add(0.0, -0.22, 0.0);
    }

    private static Vec3 safeDirection(Vec3 direction) {
        if (direction.lengthSqr() < 1.0E-6) {
            return new Vec3(0.0, 0.0, 1.0);
        }
        return direction.normalize();
    }

    private static void syncChannelVisual(ServerLevel level, LivingEntity caster, int durationTicks, float progress, boolean armed) {
        ModNetwork.CHANNEL.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(caster.getX(), caster.getY(), caster.getZ(), 96.0, level.dimension())), (Object)PalmThunderVisualPacket.channel(caster.getId(), durationTicks, true, progress, armed));
    }

    private static void syncChannelStop(ServerLevel level, LivingEntity caster) {
        ModNetwork.CHANNEL.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(caster.getX(), caster.getY(), caster.getZ(), 96.0, level.dimension())), (Object)PalmThunderVisualPacket.channel(caster.getId(), 1, false));
    }

    private static void syncBurstVisual(ServerLevel level, Vec3 center, float radius) {
        ModNetwork.CHANNEL.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(center.x, center.y, center.z, 96.0, level.dimension())), (Object)PalmThunderVisualPacket.burst(center, radius, 18));
    }

    private static final class StunState {
        private final String dimension;
        private final UUID ownerId;
        private final long endTick;
        private final float damagePerTick;
        private long lastDamageTick;

        private StunState(String dimension, @Nullable UUID ownerId, long endTick, float damagePerTick, long lastDamageTick) {
            this.dimension = dimension;
            this.ownerId = ownerId;
            this.endTick = endTick;
            this.damagePerTick = damagePerTick;
            this.lastDamageTick = lastDamageTick;
        }
    }

    private record ArmedState(String dimension) {
    }
}

