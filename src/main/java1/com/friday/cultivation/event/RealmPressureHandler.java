package com.friday.cultivation.event;

import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.RealmPressureVisualPacket;
import com.friday.cultivation.realm.Realm;
import com.friday.cultivation.entity.npc.NpcSpellCaster;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.spell.Spell;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.Optional;

/**
 * 境界压制处理器 - 高境界玩家对低境界生物造成附加压制。
 * 完全照搬原 mod: xiaoxiang.cultivation.event.RealmPressureHandler
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation")
public final class RealmPressureHandler {
    public static final int EFFECT_TICKS = 1200;
    private static final int SHORT_TAP_TICKS = 4;
    private static final int VISUAL_REFRESH_TICKS = 8;
    private static final double TAP_RANGE = 48.0;
    private static final double TARGET_INFLATE = 0.75;
    private static final double AREA_START_RADIUS = 1.5;
    private static final double AREA_RADIUS_PER_TICK = 1.1;
    private static final double AREA_MAX_RADIUS = 72.0;
    private static final int MAX_AREA_TARGETS_PER_TICK = 96;
    private static final String KEY_SUPPRESSED_UNTIL = "xiaoxiang_cultivation.realmPressureUntil";
    private static final String KEY_NEXT_VISUAL = "xiaoxiang_cultivation.realmPressureNextVisual";
    private static final java.util.Map<java.util.UUID, ExpansionState> EXPANSIONS = new java.util.HashMap<java.util.UUID, ExpansionState>();

    private RealmPressureHandler() {
    }

    public static double bodyDefenseMultiplier(LivingEntity entity) {
        return RealmPressureHandler.isSuppressed(entity) ? 0.5 : 1.0;
    }

    public static void beginExpansion(net.minecraft.server.level.ServerPlayer player, com.friday.cultivation.capability.CultivationData data) {
        if (player == null || data == null) {
            return;
        }
        EXPANSIONS.put(player.getUUID(), new ExpansionState(player.level().dimension().location().toString()));
    }

    public static void tickExpansion(net.minecraft.server.level.ServerPlayer player, com.friday.cultivation.capability.CultivationData data) {
        if (player == null || data == null) {
            return;
        }
        int chargingTicks = data.getChargingTicks();
        if (chargingTicks < 4) {
            data.incrementChargingTicks();
            com.friday.cultivation.event.CapabilityEvents.syncToClient(player);
            return;
        }
        ExpansionState state = RealmPressureHandler.EXPANSIONS.computeIfAbsent(player.getUUID(), id -> new ExpansionState(player.level().dimension().location().toString()));
        if (!state.levelKey.equals(player.level().dimension().location().toString())) {
            state.processedIds.clear();
            state.levelKey = player.level().dimension().location().toString();
        }
        double radius = RealmPressureHandler.expansionRadius(chargingTicks);
        Vec3 center = RealmPressureHandler.pressureCenter(player);
        RealmPressureHandler.processExpansionTargets(player, state, center, radius);
        RealmPressureHandler.refreshCasterVisual(player, 12);
        if ((player.tickCount & 1) == 0) {
            ModNetwork.CHANNEL.send(net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), RealmPressureVisualPacket.expansion(player.getId(), (float) radius, 12, true));
        }
        if (player.tickCount % 16 == 0) {
            player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.35f, 0.7f + Math.min(0.55f, (float) radius / 96.0f));
        }
        data.incrementChargingTicks();
        com.friday.cultivation.event.CapabilityEvents.syncToClient(player);
    }

    public static void finishExpansionOrTap(net.minecraft.server.level.ServerPlayer player, com.friday.cultivation.capability.CultivationData data) {
        if (player == null || data == null) {
            return;
        }
        int ticks = data.getChargingTicks();
        RealmPressureHandler.stopExpansionVisual(player);
        RealmPressureHandler.EXPANSIONS.remove(player.getUUID());
        data.clearCharging();
        com.friday.cultivation.event.CapabilityEvents.syncToClient(player);
        if (ticks <= 4) {
            RealmPressureHandler.castTap(player);
        } else {
            player.displayClientMessage(Component.translatable("message.xiaoxiang_cultivation.realm_pressure.area_released"), true);
        }
    }

    /** 境界压制是否生效（照搬原模组 isSuppressed(LivingEntity)）。 */
    public static boolean isSuppressed(LivingEntity entity) {
        if (entity == null || entity.level() == null) {
            return false;
        }
        long until = entity.getPersistentData().getLong(KEY_SUPPRESSED_UNTIL);
        return until > entity.level().getGameTime();
    }

    /** 灵气恢复压制（照搬原模组 applyQiRecoveryPenalty）。 */
    public static long applyQiRecoveryPenalty(LivingEntity entity, long recovery) {
        if (recovery <= 0L || !RealmPressureHandler.isSuppressed(entity)) {
            return recovery;
        }
        return Math.max(0L, recovery / 2L);
    }

    /** 出手伤害倍率（照搬原模组 outgoingDamageMultiplier）。 */
    public static double outgoingDamageMultiplier(LivingEntity entity) {
        return RealmPressureHandler.isSuppressed(entity) ? 0.5 : 1.0;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.isCanceled()) {
            return;
        }
        if (!(event.getEntity().level() instanceof ServerLevel)) {
            return;
        }
        if (event.getAmount() <= 0.0f) {
            return;
        }
        Entity source = event.getSource().getDirectEntity();
        if (!(source instanceof LivingEntity)) {
            return;
        }
        LivingEntity caster = (LivingEntity) source;
        LivingEntity target = event.getEntity();
        if (caster == target) {
            return;
        }
        RealmPressureHandler.applyFrom(caster, target);
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel)) {
            return;
        }
        CompoundTag tag = entity.getPersistentData();
        long now = entity.level().getGameTime();
        long until = tag.getLong(KEY_SUPPRESSED_UNTIL);
        if (until <= 0L) {
            return;
        }
        if (until <= now) {
            tag.remove(KEY_SUPPRESSED_UNTIL);
            tag.remove(KEY_NEXT_VISUAL);
            ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), RealmPressureVisualPacket.target(entity.getId(), 1, false));
            return;
        }
        RealmPressureHandler.forceGrounded(entity);
        long nextVisual = tag.getLong(KEY_NEXT_VISUAL);
        if (nextVisual <= now) {
            tag.putLong(KEY_NEXT_VISUAL, now + 40L);
            ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), RealmPressureVisualPacket.target(entity.getId(), 45, true));
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer() == null || EXPANSIONS.isEmpty()) {
            return;
        }
        java.util.Iterator<java.util.Map.Entry<java.util.UUID, ExpansionState>> it = EXPANSIONS.entrySet().iterator();
        while (it.hasNext()) {
            java.util.Map.Entry<java.util.UUID, ExpansionState> entry = it.next();
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(entry.getKey());
            if (player == null) {
                it.remove();
                continue;
            }
            CultivationData data = CultivationCapability.get(player).orElse(null);
            if (data != null && data.isCharging() && Spell.byId(data.getChargingSpellId()) == Spell.REALM_PRESSURE) {
                continue;
            }
            RealmPressureHandler.stopExpansionVisual(player);
            it.remove();
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        EXPANSIONS.remove(event.getEntity().getUUID());
    }

    // ── 短按施放（照搬原模组 castTap / hasTapTarget）──

    public static boolean hasTapTarget(ServerPlayer player) {
        return RealmPressureHandler.raycastLivingTarget(player, TAP_RANGE) != null;
    }

    public static void castTap(ServerPlayer player) {
        LivingEntity target = RealmPressureHandler.raycastLivingTarget(player, TAP_RANGE);
        if (target == null) {
            player.displayClientMessage(Component.translatableWithFallback("message.friday_cultivation.realm_pressure.no_target", "准星未锁定可施压的生物目标"), true);
            return;
        }
        ApplyResult result = RealmPressureHandler.applyFrom(player, target);
        switch (result) {
            case APPLIED -> {
                player.displayClientMessage(Component.translatableWithFallback("message.friday_cultivation.realm_pressure.applied", target.getName().getString() + " 已被境界压制"), true);
                player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.55f, 1.65f);
            }
            case ALREADY_ACTIVE ->
                player.displayClientMessage(Component.translatableWithFallback("message.friday_cultivation.realm_pressure.already_active", target.getName().getString() + " 已处于境界压制之中"), true);
            default ->
                player.displayClientMessage(Component.translatableWithFallback("message.friday_cultivation.realm_pressure.resisted", target.getName().getString() + " 未被境界压制"), true);
        }
    }

    private static ApplyResult applyFrom(LivingEntity caster, LivingEntity target) {
        if (!(caster.level() instanceof ServerLevel) || target == null || !target.isAlive()) {
            return ApplyResult.RESISTED;
        }
        if (caster == target) {
            return ApplyResult.RESISTED;
        }
        if (!SoulStateHandler.canOrdinaryAffect(caster, target)) {
            return ApplyResult.RESISTED;
        }
        if (!SectCombatHandler.canTargetOffensiveEffect(caster, target)) {
            return ApplyResult.RESISTED;
        }
        CasterProfile casterProfile = RealmPressureHandler.casterProfile(caster);
        if (casterProfile == null) {
            return ApplyResult.RESISTED;
        }
        if (!RealmPressureHandler.qualifies(casterProfile, target)) {
            return ApplyResult.RESISTED;
        }
        long now = target.level().getGameTime();
        CompoundTag tag = target.getPersistentData();
        long existingUntil = tag.getLong(KEY_SUPPRESSED_UNTIL);
        if (existingUntil > now) {
            return ApplyResult.ALREADY_ACTIVE;
        }
        tag.putLong(KEY_SUPPRESSED_UNTIL, now + 1200L);
        tag.putLong(KEY_NEXT_VISUAL, now + 40L);
        RealmPressureHandler.forceGrounded(target);
        RealmPressureHandler.refreshCasterVisual(caster, 46);
        ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> target), RealmPressureVisualPacket.target(target.getId(), 1200, true));
        if (target instanceof ServerPlayer player) {
            player.displayClientMessage(Component.translatableWithFallback("message.friday_cultivation.realm_pressure.suppressed", "你被境界压制，无法飞行或运转法术！"), true);
        }
        return ApplyResult.APPLIED;
    }

    private static boolean qualifies(CasterProfile caster, LivingEntity target) {
        TargetProfile targetProfile = RealmPressureHandler.targetProfile(target);
        if (targetProfile != null) {
            return targetProfile.rank < caster.rank && targetProfile.maxQi < caster.maxQi;
        }
        return (double) caster.maxQi > Math.ceil(target.getMaxHealth());
    }

    private static CasterProfile casterProfile(LivingEntity caster) {
        if (caster instanceof ServerPlayer player) {
            CultivationData data = CultivationCapability.get(player).orElse(null);
            if (data == null || data.getRealm() == Realm.MORTAL || !data.hasSpell(Spell.REALM_PRESSURE)) {
                return null;
            }
            return new CasterProfile(RealmPressureHandler.pressureRank(data.getRealm(), data.getLooseImmortalTribulations()), data.getMaxQi());
        }
        if (caster instanceof WanderingCultivatorEntity npc) {
            if (npc.getRealm() == Realm.MORTAL || !NpcSpellCaster.knows(npc, Spell.REALM_PRESSURE)) {
                return null;
            }
            return new CasterProfile(RealmPressureHandler.pressureRank(npc.getRealm(), RealmPressureHandler.npcLooseLevel(npc)), npc.getMaxQi());
        }
        return null;
    }

    private static TargetProfile targetProfile(LivingEntity target) {
        if (target instanceof ServerPlayer player) {
            CultivationData data = CultivationCapability.get(player).orElse(null);
            if (data == null) {
                return null;
            }
            return new TargetProfile(RealmPressureHandler.pressureRank(data.getRealm(), data.getLooseImmortalTribulations()), data.getMaxQi());
        }
        if (target instanceof WanderingCultivatorEntity npc) {
            return new TargetProfile(RealmPressureHandler.pressureRank(npc.getRealm(), RealmPressureHandler.npcLooseLevel(npc)), npc.getMaxQi());
        }
        return null;
    }

    private static int npcLooseLevel(WanderingCultivatorEntity npc) {
        return npc.getRealm() == Realm.LOOSE_IMMORTAL ? npc.getLooseImmortalTribulations() : 0;
    }

    private static int pressureRank(Realm realm, int looseLevel) {
        if (realm == null) {
            return 0;
        }
        if (realm == Realm.LOOSE_IMMORTAL) {
            int level = Math.max(1, looseLevel);
            if (level <= 2) {
                return 95;
            }
            if (level <= 4) {
                return 100;
            }
            if (level <= 6) {
                return 105;
            }
            return 110;
        }
        if (realm == Realm.TRUE_IMMORTAL) {
            return 100;
        }
        return realm.ordinal() * 10;
    }

    private static LivingEntity raycastLivingTarget(ServerPlayer player, double maxDist) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        if (look.lengthSqr() < 1.0E-6) {
            return null;
        }
        Vec3 end = eye.add(look.scale(maxDist));
        double blockDist = RealmPressureHandler.blockDistance(player, eye, end, maxDist);
        AABB scan = new AABB(eye, end).inflate(2.25);
        LivingEntity best = null;
        double bestDist = blockDist;
        for (LivingEntity entity : player.serverLevel().getEntitiesOfClass(LivingEntity.class, scan, e -> e != player && e.isAlive() && e.isPickable() && SoulStateHandler.canOrdinaryAffect(player, e) && SectCombatHandler.canTargetOffensiveEffect(player, e))) {
            Optional<Vec3> hit = entity.getBoundingBox().inflate(0.75).clip(eye, end);
            if (hit.isEmpty()) {
                continue;
            }
            double dist = eye.distanceToSqr(hit.get());
            if (!(dist < bestDist)) {
                continue;
            }
            bestDist = dist;
            best = entity;
        }
        return best;
    }

    private static double blockDistance(ServerPlayer player, Vec3 eye, Vec3 end, double maxDist) {
        BlockHitResult hit = player.serverLevel().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return hit.getType() == HitResult.Type.MISS ? maxDist : eye.distanceToSqr(hit.getLocation());
    }

    private static void refreshCasterVisual(LivingEntity caster, int durationTicks) {
        ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> caster), RealmPressureVisualPacket.caster(caster.getId(), durationTicks, true));
    }

    /** 强制落地（照搬原模组 forceGrounded(LivingEntity)）。 */
    public static void forceGrounded(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide()) {
            return;
        }
        entity.removeEffect(MobEffects.LEVITATION);
        if (entity instanceof ServerPlayer player) {
            CultivationData data = CultivationCapability.get(player).orElse(null);
            if (data != null) {
                SwordFlightHandler.stopIfActive(player, data, false);
                if (data.isVoidEscapeActive()) {
                    VoidEscapeHandler.tryManualExitIfActive(player);
                }
            }
            NascentSoulOutOfBodyHandler.stopIfActive(player, false);
            if (!player.isCreative() && !player.isSpectator()) {
                if (player.getAbilities().mayfly || player.getAbilities().flying) {
                    player.getAbilities().mayfly = false;
                    player.getAbilities().flying = false;
                    player.onUpdateAbilities();
                }
                player.setNoGravity(false);
                player.noPhysics = false;
            }
        } else {
            entity.setNoGravity(false);
            entity.noPhysics = false;
        }
        if (!entity.onGround()) {
            Vec3 v = entity.getDeltaMovement();
            entity.setDeltaMovement(v.x * 0.65, Math.min(v.y, -0.32), v.z * 0.65);
            entity.hurtMarked = true;
        }
        entity.fallDistance = 0.0f;
    }

    private static Vec3 pressureCenter(net.minecraft.world.entity.LivingEntity entity) {
        return entity.position().add(0.0, (double) entity.getBbHeight() * 0.55, 0.0);
    }

    private static double expansionRadius(int chargingTicks) {
        int effectiveTicks = Math.max(0, chargingTicks - 4 + 1);
        return Math.min(72.0, 1.5 + (double) effectiveTicks * 1.1);
    }

    private static void processExpansionTargets(ServerPlayer player, ExpansionState state, Vec3 center, double radius) {
        AABB box = new AABB(center, center).inflate(radius + 1.25);
        double radiusSq = radius * radius;
        int processedThisTick = 0;
        for (LivingEntity entity : player.serverLevel().getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            double distance;
            if (entity == player || state.processedIds.contains(entity.getId()) || (distance = RealmPressureHandler.distanceToAabb(center, entity.getBoundingBox().inflate(0.1))) * distance > radiusSq) continue;
            state.processedIds.add(entity.getId());
            RealmPressureHandler.applyFrom(player, entity);
            if (++processedThisTick < 96) continue;
            break;
        }
    }

    private static double distanceToAabb(Vec3 point, AABB box) {
        double dx = RealmPressureHandler.axisDistance(point.x, box.minX, box.maxX);
        double dy = RealmPressureHandler.axisDistance(point.y, box.minY, box.maxY);
        double dz = RealmPressureHandler.axisDistance(point.z, box.minZ, box.maxZ);
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

    private static void stopExpansionVisual(ServerPlayer player) {
        ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), RealmPressureVisualPacket.expansion(player.getId(), 0.0f, 1, false));
    }

    private enum ApplyResult {
        APPLIED,
        ALREADY_ACTIVE,
        RESISTED;
    }

    private static final class ExpansionState {
        String levelKey;
        final java.util.Set<Integer> processedIds = new java.util.HashSet<Integer>();

        ExpansionState(String levelKey) {
            this.levelKey = levelKey;
        }
    }

    private record CasterProfile(int rank, long maxQi) {
    }

    private record TargetProfile(int rank, long maxQi) {
    }
}
