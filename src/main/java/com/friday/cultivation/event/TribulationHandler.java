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

    public static void beginTribulation(ServerPlayer player, CultivationData data, int strikes) {
        TribulationHandler.beginTribulation(player, data, strikes, 0);
    }

    public static void beginTribulation(ServerPlayer player, CultivationData data, int strikes, int strikeDamageOverride) {
        TribulationHandler.beginTribulation(player, data, strikes, 1, strikeDamageOverride);
    }

    public static void beginTribulation(ServerPlayer player, CultivationData data, int strikes, int boltsPerWave, int strikeDamageOverride) {
        // 综合评判：灵根/体质/功法品质越好 → 渡劫道数与伤害越高（天骄遭天妒）
        double mult = TribulationScalingHelper.difficultyMult(player, data);
        int scaled = Math.max(1, (int) Math.round(strikes * boltsPerWave * mult));
        int scaledWaves = Math.max(1, (int) Math.ceil((double) scaled / Math.max(1, boltsPerWave)));
        int scaledDamage = Math.max(1, (int) Math.round(strikeDamageOverride * mult));
        TribulationHandler.beginTribulationInternal(player, data, scaledWaves, boltsPerWave, scaledDamage, false);
    }

    public static void beginLooseImmortalTribulation(ServerPlayer player, CultivationData data, int strikes, int boltsPerWave, int strikeDamageOverride) {
        TribulationHandler.beginTribulationInternal(player, data, strikes, boltsPerWave, strikeDamageOverride, true);
    }

    private static void beginTribulationInternal(ServerPlayer player, CultivationData data, int strikes, int boltsPerWave, int strikeDamageOverride, boolean looseImmortal) {
        TribulationHandler.anchorTribulationRuntimeState(player, data);
        data.clearCharging();
        if (looseImmortal) {
            data.startLooseImmortalTribulation(strikes, strikeDamageOverride, boltsPerWave);
        } else {
            data.startTribulation(strikes, strikeDamageOverride, boltsPerWave);
        }
        CapabilityEvents.syncToClient(player);
        TribulationHandler.sendTribulationCloud(player, TribulationHandler.estimateCloudDurationTicks(strikes, boltsPerWave));
        ServerLevel level = player.serverLevel();
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, TribulationConstants.CLOUD_SOUND_VOLUME, TribulationConstants.CLOUD_SOUND_PITCH);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, TribulationConstants.THUNDER_SOUND_VOLUME, TribulationConstants.THUNDER_SOUND_PITCH);
        level.sendParticles((ParticleOptions)ParticleTypes.CLOUD, player.getX(), player.getY() + TribulationConstants.CLOUD_Y_OFFSET, player.getZ(), TribulationConstants.CLOUD_PARTICLE_COUNT, TribulationConstants.CLOUD_PARTICLE_RADIUS, TribulationConstants.CLOUD_PARTICLE_HEIGHT, TribulationConstants.CLOUD_PARTICLE_SPREAD, TribulationConstants.CLOUD_PARTICLE_SPEED);
        // 事件钩子：渡劫开始
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new TribulationEvents.Started(player, TribulationHandler.currentSpec(data)));
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
        TribulationEvents.BoltStrike strikeEvent = new TribulationEvents.BoltStrike(player, TribulationHandler.currentSpec(data), strikeDmg);
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
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new TribulationEvents.WaveEnd(player, data.getTribulationStrikesRemaining()));
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

    /** 当前渡劫劫谱（由 Realm 提供或按伤害/道数构造简易 spec；散仙劫用简易 spec） */
    private static TribulationSpec currentSpec(CultivationData data) {
        if (data == null) {
            return TribulationSpec.of(1, 1, 0);
        }
        // 优先使用 Realm 数据驱动劫谱（真实波数/道数/伤害）
        if (!data.isLooseImmortalTribulationActive()) {
            TribulationSpec spec = data.getRealm().tribulationSpec(data.getSubStage());
            if (spec != null) {
                return spec;
            }
        }
        // 散仙劫/临时：按运行时值构造
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
        TribulationHandler.currentSpec(dataOf(player)).type().spawnEffect(level, player, TribulationHandler.currentSpec(dataOf(player)), strikeDmg);
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
        TribulationHandler.currentSpec(data).type().applyDamage(level, player, TribulationHandler.currentSpec(data), Math.round(finalDmg));
    }

    private static void onTribulationSuccess(ServerPlayer player, CultivationData data) {
        if (data.isLooseImmortalTribulationActive()) {
            LooseImmortalHandler.completeTribulationSuccess(player, data);
            TribulationHandler.clearTribulationCloud(player);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new TribulationEvents.Succeeded(player, data.getRealm(), data.getSubStage()));
            return;
        }
        Realm before = data.getRealm();
        SubStage beforeStage = data.getSubStage();
        TribulationHandler.completeBreakthrough(player, data, before, beforeStage, true);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new TribulationEvents.Succeeded(player, data.getRealm(), data.getSubStage()));
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
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new TribulationEvents.Failed(player, before, beforeStage));
        }
        CapabilityEvents.syncToClient(player);
        return wasInTribulation;
    }

    private static void completeBreakthrough(ServerPlayer player, CultivationData data, Realm before, SubStage beforeStage, boolean fromTribulation) {
        boolean minorBreakthrough;
        data.advanceOnSuccess();
        data.clearTribulation();
        // 渡劫成功后按综合评判档位记录隐藏加成百分比（作用于玩家总属性，复利；不显示在雷达图）
        if (fromTribulation) {
            double percent = TribulationScalingHelper.rewardPercent(player, data);
            if (percent > 0.0) {
                data.addTribulationBonus(percent, data.getRealm());
                player.displayClientMessage(Component.translatable("message.friday_cultivation.tribulation.tier_reward",
                        Component.translatable(TribulationScalingHelper.tierTranslationKey(player, data)),
                        Math.round(percent * 100.0)), false);
            }
        }
        // 突破后生命值、灵气值直接设为上限（灵气在 advanceOnSuccess 内已设满）
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

