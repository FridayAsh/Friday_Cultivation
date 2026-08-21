/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.advancements.Advancement
 *  net.minecraft.advancements.AdvancementProgress
 *  net.minecraft.core.Holder
 *  net.minecraft.core.Holder$Reference
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.core.particles.SimpleParticleType
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageTypes
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LightningBolt
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.GameType
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.event.entity.EntityStruckByLightningEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedInEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.network.PacketDistributor
 */
package com.friday.cultivation.event;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.PhysiqueBonusHelper;
import com.friday.cultivation.cultivation.TribulationBonusSnapshot;
import com.friday.cultivation.cultivation.SpiritRoot;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.cultivation.realm.SubStage;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.event.LooseImmortalHandler;
import com.friday.cultivation.event.tribulation.TribulationConstants;
import com.friday.cultivation.event.tribulation.TribulationDefense;
import com.friday.cultivation.event.tribulation.TribulationEvents;
import com.friday.cultivation.event.tribulation.TribulationScalingHelper;
import com.friday.cultivation.event.tribulation.TribulationSpec;
import com.friday.cultivation.event.tribulation.TribulationSession;
import com.friday.cultivation.event.tribulation.TribulationTier;
import com.friday.cultivation.event.tribulation.TribulationType;
import com.friday.cultivation.event.NascentSoulOutOfBodyHandler;
import com.friday.cultivation.event.SectProtectionDomeHandler;
import com.friday.cultivation.event.SpiritLockHandler;
import com.friday.cultivation.event.TimeStasisHandler;
import com.friday.cultivation.event.VoidEscapeHandler;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.TribulationCloudPacket;
import com.friday.cultivation.registry.ModParticles;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class TribulationHandler {
    // 常量已移至 TribulationConstants

    static {
        // 注册渡劫防御链（宗门护盾 / 雷灵根减免）
        TribulationDefense.init();
    }

    private TribulationHandler() {
    }

    public static boolean isTribulationDamage(DamageSource source) {
        if (source == null) {
            return false;
        }
        return TribulationHandler.isTribulationLightning(source.getDirectEntity()) || TribulationHandler.isTribulationLightning(source.getEntity());
    }

    private static boolean isTribulationLightning(Entity entity) {
        return TribulationType.LightningTribulation.isTribulationLightning(entity);
    }

    @SubscribeEvent(priority=EventPriority.HIGH)
    public static void onEntityStruckByTribulationLightning(EntityStruckByLightningEvent event) {
        LightningBolt bolt = event.getLightning();
        if (!bolt.getPersistentData().getBoolean(TribulationType.LightningTribulation.TAG)) {
            return;
        }
        event.setCanceled(true);
    }

    /** 普通路线唯一启动入口：调用者必须先构造完整且已校验的劫谱。 */
    public static void beginTribulation(ServerPlayer player, CultivationData data, TribulationSpec spec) {
        TribulationHandler.beginTribulationInternal(player, data, spec, false);
    }

    /** 散仙路线唯一启动入口：调用者必须先按散仙劫波等级构造完整劫谱。 */
    public static void beginLooseImmortalTribulation(ServerPlayer player, CultivationData data, TribulationSpec spec) {
        TribulationHandler.beginTribulationInternal(player, data, spec, true);
    }

    private static void beginTribulationInternal(ServerPlayer player, CultivationData data, TribulationSpec spec, boolean looseImmortal) {
        TribulationHandler.anchorTribulationRuntimeState(player, data);
        data.clearCharging();
        Realm sourceRealm = data.getRealm();
        SubStage sourceSubStage = data.getSubStage();
        Realm targetRealm = looseImmortal ? sourceRealm : TribulationHandler.nextBreakthroughRealm(sourceRealm, sourceSubStage);
        SubStage targetSubStage = looseImmortal
                ? sourceSubStage
                : TribulationHandler.nextBreakthroughSubStage(sourceRealm, sourceSubStage, targetRealm);
        String tierId = looseImmortal ? "" : TribulationScalingHelper.tier(player, data).name();
        data.startTribulation(spec, looseImmortal, targetRealm, targetSubStage, tierId);
        CapabilityEvents.syncToClient(player);
        TribulationHandler.sendTribulationCloud(player, TribulationHandler.estimateCloudDurationTicks(spec.waves(), spec.boltsPerWave()));
        ServerLevel level = player.serverLevel();
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, TribulationConstants.CLOUD_SOUND_VOLUME, TribulationConstants.CLOUD_SOUND_PITCH);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, TribulationConstants.THUNDER_SOUND_VOLUME, TribulationConstants.THUNDER_SOUND_PITCH);
        level.sendParticles((ParticleOptions)ParticleTypes.CLOUD, player.getX(), player.getY() + TribulationConstants.CLOUD_Y_OFFSET, player.getZ(), TribulationConstants.CLOUD_PARTICLE_COUNT, TribulationConstants.CLOUD_PARTICLE_RADIUS, TribulationConstants.CLOUD_PARTICLE_HEIGHT, TribulationConstants.CLOUD_PARTICLE_SPREAD, TribulationConstants.CLOUD_PARTICLE_SPEED);
        // 事件钩子：渡劫开始
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new TribulationEvents.Started(player,
                TribulationHandler.currentSpec(data), data.getTribulationSession()));
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
        CultivationCapability.get((Player)player2).ifPresent(data -> TribulationHandler.tick(player2, data));
    }

    private static void tick(ServerPlayer player, CultivationData data) {
        if (!data.isInTribulation()) {
            return;
        }
        ServerLevel level = player.serverLevel();
        if (level == null) {
            return;
        }
        TribulationHandler.anchorTribulationRuntimeState(player, data);
        TribulationHandler.spawnTribulationCloudParticles(level, player, data);
        if (data.getTribulationCooldown() > 0) {
            data.decrementTribulationCooldown();
            return;
        }
        if (!data.hasPendingTribulationBolts()) {
            data.beginTribulationWave();
        }
        TribulationHandler.tickPendingTribulationWave(player, data, level);
    }

    private static void tickPendingTribulationWave(ServerPlayer player, CultivationData data, ServerLevel level) {
        if (!data.hasPendingTribulationBolts()) {
            return;
        }
        if (data.getTribulationBoltCooldown() > 0) {
            data.decrementTribulationBoltCooldown();
            return;
        }
        int strikeDmg = TribulationHandler.currentStrikeDamage(data);
        // 事件钩子：雷击前（可修改伤害）
        TribulationEvents.BoltStrike strikeEvent = new TribulationEvents.BoltStrike(player,
                TribulationHandler.currentSpec(data), strikeDmg, data.getTribulationSession());
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(strikeEvent);
        strikeDmg = Math.max(0, (int) strikeEvent.getDamage());
        TribulationHandler.spawnTribulationBolt(level, player, strikeDmg);
        if (!data.isInTribulation() || !player.isAlive()) {
            return;
        }
        data.consumePendingTribulationBolt(TribulationHandler.specBoltInterval(data));
        if (data.hasPendingTribulationBolts()) {
            return;
        }
        data.decrementTribulationStrikes();
        CapabilityEvents.syncToClient(player);
        // 事件钩子：每波结束
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new TribulationEvents.WaveEnd(player,
                data.getTribulationStrikesRemaining(), data.getTribulationSession()));
        if (!data.isInTribulation()) {
            TribulationHandler.onTribulationSuccess(player, data);
        }
    }

    private static int currentStrikeDamage(CultivationData data) {
        if (data == null) {
            return 0;
        }
        // 比例模式：strikeDamage<=0 且 damageRatio>0 时，伤害 = 标准生命值 × 比例
        if (data.getCurrentTribulationStrikeDamage() <= 0 && data.getTribulationDamageRatio() > 0.0) {
            return Math.max(1, (int) Math.round(data.getRealm().standardMaxHealth() * data.getTribulationDamageRatio()));
        }
        return Math.max(0, data.getCurrentTribulationStrikeDamage());
    }

    /** 当前活动劫谱：只读取固定 Session，绝不从当前 Realm 重新构造。 */
    private static TribulationSpec currentSpec(CultivationData data) {
        if (data == null) {
            return TribulationSpec.of(1, 1, 0);
        }
        if (data.getTribulationSession() != null) {
            return data.getTribulationSession().spec();
        }
        // 仅兼容未迁移的旧内存状态；新活动必须在 startTribulation 建立 Session。
        int damage = Math.max(0, data.getCurrentTribulationStrikeDamage());
        int waves = Math.max(1, data.getTribulationStrikesRemaining() <= 0 ? 1 : data.getTribulationStrikesRemaining() + 1);
        int bolts = data.getTribulationBoltsPerWave();
        return new TribulationSpec(waves, bolts, damage, 0.0, 0, data.getTribulationType());
    }

    /** 每波雷击间隔（tick）：优先劫谱，回退旧算法 */
    private static int specBoltInterval(CultivationData data) {
        TribulationSpec spec = TribulationHandler.currentSpec(data);
        return spec.effectiveBoltInterval();
    }

    private static void spawnTribulationBolt(ServerLevel level, ServerPlayer player, int strikeDmg) {
        CultivationData data = dataOf(player);
        TribulationHandler.currentSpec(data).type().spawnEffect(level, player,
                data == null ? null : data.getTribulationSession(), strikeDmg);
        TribulationHandler.applyTribulationBoltDamage(level, player, strikeDmg);
    }

    private static CultivationData dataOf(ServerPlayer player) {
        return CultivationCapability.get((Player)player).orElse(null);
    }

    private static void applyTribulationBoltDamage(ServerLevel level, ServerPlayer player, int strikeDmg) {
        if (!player.isAlive() || strikeDmg <= 0) {
            return;
        }
        CultivationData data = TribulationHandler.dataOf(player);
        // 防御链：宗门护盾 / 雷灵根减免等依次生效
        float finalDmg = TribulationDefense.applyAll(strikeDmg, player, data);
        if (finalDmg <= 0.0f) {
            player.setRemainingFireTicks(0);
            return;
        }
        TribulationHandler.currentSpec(data).type().applyDamage(level, player,
                data == null ? null : data.getTribulationSession(), Math.round(finalDmg));
    }

    private static void onTribulationSuccess(ServerPlayer player, CultivationData data) {
        TribulationSession session = data.getTribulationSession();
        if (data.isLooseImmortalTribulationActive()) {
            LooseImmortalHandler.completeTribulationSuccess(player, data);
            TribulationHandler.clearTribulationCloud(player);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new TribulationEvents.Succeeded(player,
                    data.getRealm(), data.getSubStage(), session));
            return;
        }
        Realm before = data.getRealm();
        SubStage beforeStage = data.getSubStage();
        TribulationHandler.completeBreakthrough(player, data, before, beforeStage, true);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new TribulationEvents.Succeeded(player,
                data.getRealm(), data.getSubStage(), session));
    }

    public static void completeBreakthroughWithoutTribulation(ServerPlayer player, CultivationData data) {
        Realm before = data.getRealm();
        SubStage beforeStage = data.getSubStage();
        TribulationHandler.completeBreakthrough(player, data, before, beforeStage, false);
    }

    public static void abortForLooseImmortalChoice(ServerPlayer player, CultivationData data) {
        TribulationHandler.clearDeathRuntimeStates(player, data);
        data.clearTribulation();
        TribulationHandler.clearTribulationCloud(player);
        CapabilityEvents.syncToClient(player);
    }

    public static boolean failCurrentTribulation(ServerPlayer player, CultivationData data) {
        Realm before = data.getRealm();
        SubStage beforeStage = data.getSubStage();
        TribulationSession session = data.getTribulationSession();
        boolean wasInTribulation = data.isInTribulation();
        boolean looseImmortalTribulation = data.isLooseImmortalTribulationActive();
        TribulationHandler.clearDeathRuntimeStates(player, data);
        data.clearTribulation();
        TribulationHandler.clearTribulationCloud(player);
        if (wasInTribulation) {
            if (looseImmortalTribulation) {
                LooseImmortalHandler.completeTribulationFailure(player, data);
            } else {
                data.demoteOnFailure();
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.tribulation.failure", (Object[])new Object[]{before.displayName(), beforeStage.displayName(), data.getRealm().displayName(), data.getSubStage().displayName()}), false);
            }
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new TribulationEvents.Failed(player,
                    before, beforeStage, session));
        }
        TechniqueEffectHandler.refreshMaxHealth(player);
        CapabilityEvents.syncToClient(player);
        return wasInTribulation;
    }

    private static void completeBreakthrough(ServerPlayer player, CultivationData data, Realm before, SubStage beforeStage, boolean fromTribulation) {
        boolean minorBreakthrough;
        TribulationSession session = fromTribulation ? data.getTribulationSession() : null;
        double rewardPercent = fromTribulation ? TribulationScalingHelper.rewardPercent(session) : 0.0;
        Realm rewardRealm = nextBreakthroughRealm(before, beforeStage);
        SubStage rewardSubStage = nextBreakthroughSubStage(before, beforeStage, rewardRealm);
        TribulationBonusSnapshot rewardSnapshot = fromTribulation
                ? data.captureTribulationBonus(player, rewardPercent, rewardRealm, rewardSubStage, session) : null;
        data.advanceOnSuccess();
        data.clearTribulation();
        // 渡劫成功后写入渡劫前属性计算出的固定快照；同一里程碑覆盖旧记录。
        if (rewardSnapshot != null) {
            data.recordTribulationBonus(rewardSnapshot);
            player.displayClientMessage(Component.translatable("message.friday_cultivation.tribulation.tier_reward",
                    Component.translatable(TribulationScalingHelper.tierTranslationKey(session)),
                    Math.round(rewardPercent * 100.0)), false);
        }
        // 突破后生命值、灵气值直接设为上限（灵气在 advanceOnSuccess 内已设满）
        TechniqueEffectHandler.refreshMaxHealth(player);
        player.setHealth(player.getMaxHealth());
        boolean bl = minorBreakthrough = before == data.getRealm() && beforeStage != data.getSubStage();
        if (minorBreakthrough) {
            PhysiqueBonusHelper.grantChaosBodyMinorBreakthroughSpell(player, data);
        }
        CapabilityEvents.syncToClient(player);
        if (fromTribulation) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.tribulation.success", (Object[])new Object[]{before.displayName(), beforeStage.displayName(), data.getRealm().displayName(), data.getSubStage().displayName()}), false);
        } else {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.breakthrough.success", (Object[])new Object[]{data.getRealm().displayName(), data.getSubStage().displayName()}), false);
        }
        if (player.serverLevel() != null) {
            ServerLevel level = player.serverLevel();
            level.sendParticles((ParticleOptions)((SimpleParticleType)ModParticles.BREAKTHROUGH.get()), player.getX(), player.getY() + 1.0, player.getZ(), 120, 0.5, 1.0, 0.5, 0.25);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.85f, 1.28f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.75f, 0.72f);
        }
        TribulationHandler.clearTribulationCloud(player);
        if (before != data.getRealm()) {
            TribulationHandler.grantRealmAdvancement(player, data.getRealm());
        }
        TribulationHandler.grantTianjiaoAdvancement(player, session);
    }

    private static Realm nextBreakthroughRealm(Realm before, SubStage beforeStage) {
        if (before == null || before == Realm.MORTAL) {
            return Realm.BODY_TEMPERING;
        }
        return beforeStage != null && beforeStage.isPeakFor(before) ? before.next() : before;
    }

    private static SubStage nextBreakthroughSubStage(Realm before, SubStage beforeStage, Realm targetRealm) {
        if (targetRealm == null) {
            return Realm.MORTAL.firstSubStage();
        }
        if (targetRealm != before) {
            return targetRealm.firstSubStage();
        }
        return beforeStage == null ? targetRealm.firstSubStage() : beforeStage.nextFor(targetRealm);
    }

    private static void spawnTribulationCloudParticles(ServerLevel level, ServerPlayer player, CultivationData data) {
        if ((player.tickCount & 3) != 0) {
            return;
        }
        RandomSource random = level.random;
        double cx = player.getX();
        double cy = player.getY() + TribulationConstants.CLOUD_Y_OFFSET;
        double cz = player.getZ();
        double radius = TribulationConstants.CLOUD_RADIUS;
        int cloudCount = data.getTribulationCooldown() > 0 ? TribulationConstants.CLOUD_CLOUD_COUNT_COOLDOWN : TribulationConstants.CLOUD_CLOUD_COUNT_NORMAL;
        for (int i = 0; i < cloudCount; ++i) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double dist = Math.sqrt(random.nextDouble()) * radius;
            double x = cx + Math.cos(angle) * dist;
            double z = cz + Math.sin(angle) * dist;
            double y = cy + random.nextGaussian() * 0.22;
            level.sendParticles((ParticleOptions)ParticleTypes.CLOUD, x, y, z, 1, 0.12, 0.025, 0.12, 0.015);
        }
    }

    private static int estimateCloudDurationTicks(int strikes, int boltsPerWave) {
        int intervalCount = Math.max(0, strikes - 1);
        return TribulationConstants.CLOUD_DURATION_BASE_TICKS + intervalCount * TribulationConstants.CLOUD_DURATION_WAVE_GAP_TICKS
                + Math.max(0, strikes) * TribulationHandler.subWaveDurationTicks(boltsPerWave) + TribulationConstants.CLOUD_DURATION_TAIL_TICKS;
    }

    private static int subWaveDurationTicks(int boltsPerWave) {
        return Math.max(0, boltsPerWave - 1) * TribulationHandler.subBoltIntervalTicks(boltsPerWave);
    }

    private static int subBoltIntervalTicks(int boltsPerWave) {
        int bolts = Math.max(1, boltsPerWave);
        if (bolts <= 1) {
            return 0;
        }
        int interval = Math.round((float) TribulationConstants.SUB_WAVE_DURATION_BASE_TICKS / (float) (bolts - 1));
        return Math.max(TribulationConstants.MIN_SUB_BOLT_INTERVAL_TICKS, Math.min(TribulationConstants.MAX_SUB_BOLT_INTERVAL_TICKS, interval));
    }

    private static void sendTribulationCloud(ServerPlayer player, int durationTicks) {
        ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), (Object)new TribulationCloudPacket(player.getId(), durationTicks));
    }

    private static void clearTribulationCloud(ServerPlayer player) {
        TribulationHandler.sendTribulationCloud(player, 0);
    }

    private static void anchorTribulationRuntimeState(ServerPlayer player, CultivationData data) {
        boolean changed = false;
        if (NascentSoulOutOfBodyHandler.isActive(player)) {
            NascentSoulOutOfBodyHandler.stopIfActive(player, false);
            changed = true;
        }
        if (data.isVoidEscapeActive()) {
            VoidEscapeHandler.exit(player, data, VoidEscapeHandler.ExitReason.TRIBULATION);
            changed = true;
        }
        if (changed) {
            CapabilityEvents.syncToClient(player);
        }
    }

    private static void grantRealmAdvancement(ServerPlayer player, Realm realm) {
        if (realm == null || realm == Realm.MORTAL) {
            return;
        }
        if (player.getServer() == null) {
            return;
        }
        ResourceLocation id = new ResourceLocation("friday_cultivation", "realms/" + realm.id());
        Advancement advancement = player.getServer().getAdvancements().getAdvancement(id);
        if (advancement == null) {
            return;
        }
        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
        if (progress.isDone()) {
            return;
        }
        for (String criterion : progress.getRemainingCriteria()) {
            player.getAdvancements().award(advancement, criterion);
        }
    }

    /** 只有达到最高天骄档位时授予挑战成就；档位来自成功渡劫的 Session。 */
    private static void grantTianjiaoAdvancement(ServerPlayer player, TribulationSession session) {
        if (player == null || session == null || session.looseImmortal()
                || TribulationScalingHelper.tier(session) != TribulationTier.SOVEREIGN_OF_DAOS
                || player.getServer() == null) {
            return;
        }
        ResourceLocation id = new ResourceLocation("friday_cultivation", "talent/sovereign_of_daos");
        Advancement advancement = player.getServer().getAdvancements().getAdvancement(id);
        if (advancement == null) {
            return;
        }
        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
        if (progress.isDone()) {
            return;
        }
        for (String criterion : progress.getRemainingCriteria()) {
            player.getAdvancements().award(advancement, criterion);
        }
    }

    private static void clearDeathRuntimeStates(ServerPlayer player, CultivationData data) {
        data.clearCharging();
        data.setMeditating(false);
        NascentSoulOutOfBodyHandler.stopIfActive(player, false);
        SpiritLockHandler.clearEntityLock((Entity)player);
        TimeStasisHandler.clearEntityRuntime((Entity)player);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        CultivationCapability.get((Player)player2).ifPresent(data -> {
            boolean changed = false;
            if (data.isInTribulation()) {
                if (data.isVoidEscapeActive()) {
                    VoidEscapeHandler.exit(player2, data, VoidEscapeHandler.ExitReason.TRIBULATION);
                    changed = true;
                }
                if (player2.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
                    player2.setGameMode(GameType.SURVIVAL);
                    changed = true;
                }
            }
            if (changed) {
                CapabilityEvents.syncToClient(player2);
            }
        });
    }
}

