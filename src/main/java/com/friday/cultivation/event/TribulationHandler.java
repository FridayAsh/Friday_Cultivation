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
    private static final String TRIBULATION_LIGHTNING_TAG = "friday_cultivation.tribulation_lightning";
    private static final int TARGET_SUB_WAVE_DURATION_TICKS = 30;
    private static final int MIN_SUB_BOLT_INTERVAL_TICKS = 4;
    private static final int MAX_SUB_BOLT_INTERVAL_TICKS = 20;
    private static final double TRIBULATION_BOLT_RANDOM_RADIUS = 1.0;

    private TribulationHandler() {
    }

    public static boolean isTribulationDamage(DamageSource source) {
        if (source == null) {
            return false;
        }
        return TribulationHandler.isTribulationLightning(source.getDirectEntity()) || TribulationHandler.isTribulationLightning(source.getEntity());
    }

    private static boolean isTribulationLightning(Entity entity) {
        LightningBolt bolt;
        return entity instanceof LightningBolt && (bolt = (LightningBolt)entity).getPersistentData().getBoolean(TRIBULATION_LIGHTNING_TAG);
    }

    @SubscribeEvent(priority=EventPriority.HIGH)
    public static void onEntityStruckByTribulationLightning(EntityStruckByLightningEvent event) {
        LightningBolt bolt = event.getLightning();
        if (!bolt.getPersistentData().getBoolean(TRIBULATION_LIGHTNING_TAG)) {
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
        TribulationHandler.beginTribulationInternal(player, data, strikes, boltsPerWave, strikeDamageOverride, false);
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
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.8f, 0.42f);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 1.3f, 0.55f);
        level.sendParticles((ParticleOptions)ParticleTypes.CLOUD, player.getX(), player.getY() + 8.0, player.getZ(), 34, 4.8, 0.28, 3.4, 0.012);
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
        TribulationHandler.spawnTribulationBolt(level, player, strikeDmg);
        if (!data.isInTribulation() || !player.isAlive()) {
            return;
        }
        data.consumePendingTribulationBolt(TribulationHandler.subBoltIntervalTicks(data.getTribulationBoltsPerWave()));
        if (data.hasPendingTribulationBolts()) {
            return;
        }
        data.decrementTribulationStrikes();
        CapabilityEvents.syncToClient(player);
        if (!data.isInTribulation()) {
            TribulationHandler.onTribulationSuccess(player, data);
        }
    }

    private static int currentStrikeDamage(CultivationData data) {
        int strikeDmg = data.getCurrentTribulationStrikeDamage();
        if (data.getSpiritRoot() == SpiritRoot.MUTANT_LIGHTNING) {
            strikeDmg = Math.max(1, strikeDmg / 2);
        }
        return strikeDmg;
    }

    private static void spawnTribulationBolt(ServerLevel level, ServerPlayer player, int strikeDmg) {
        LightningBolt bolt = (LightningBolt)EntityType.LIGHTNING_BOLT.create((Level)level);
        if (bolt != null) {
            Vec3 pos = TribulationHandler.tribulationBoltPosition(level, player);
            bolt.moveTo(pos.x, pos.y, pos.z);
            bolt.setVisualOnly(true);
            bolt.getPersistentData().putBoolean(TRIBULATION_LIGHTNING_TAG, true);
            bolt.setDamage((float)strikeDmg);
            level.addFreshEntity((Entity)bolt);
            TribulationHandler.applyTribulationBoltDamage(level, player, bolt, strikeDmg);
        }
    }

    private static Vec3 tribulationBoltPosition(ServerLevel level, ServerPlayer player) {
        RandomSource random = level.random;
        double angle = random.nextDouble() * Math.PI * 2.0;
        double radius = Math.sqrt(random.nextDouble()) * 1.0;
        return new Vec3(player.getX() + Math.cos(angle) * radius, player.getY(), player.getZ() + Math.sin(angle) * radius);
    }

    private static void applyTribulationBoltDamage(ServerLevel level, ServerPlayer player, LightningBolt bolt, int strikeDmg) {
        if (!player.isAlive() || strikeDmg <= 0) {
            return;
        }
        TribulationHandler.applyTribulationFire((LivingEntity)player);
        Holder.Reference lightningType = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.LIGHTNING_BOLT);
        DamageSource source = new DamageSource((Holder)lightningType, (Entity)bolt);
        float damage = SectProtectionDomeHandler.absorbTribulationDamage(player, strikeDmg);
        if (damage <= 0.0f) {
            player.setRemainingFireTicks(0);
            return;
        }
        player.invulnerableTime = 0;
        player.hurt(source, damage);
    }

    private static void applyTribulationFire(LivingEntity living) {
        living.setRemainingFireTicks(living.getRemainingFireTicks() + 1);
        if (living.getRemainingFireTicks() == 0) {
            living.setSecondsOnFire(8);
        }
    }

    private static void onTribulationSuccess(ServerPlayer player, CultivationData data) {
        if (data.isLooseImmortalTribulationActive()) {
            LooseImmortalHandler.completeTribulationSuccess(player, data);
            TribulationHandler.clearTribulationCloud(player);
            return;
        }
        Realm before = data.getRealm();
        SubStage beforeStage = data.getSubStage();
        TribulationHandler.completeBreakthrough(player, data, before, beforeStage, true);
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
        }
        CapabilityEvents.syncToClient(player);
        return wasInTribulation;
    }

    private static void completeBreakthrough(ServerPlayer player, CultivationData data, Realm before, SubStage beforeStage, boolean fromTribulation) {
        boolean minorBreakthrough;
        data.advanceOnSuccess();
        data.clearTribulation();
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
        double cy = player.getY() + 8.0;
        double cz = player.getZ();
        double radius = 5.6;
        int cloudCount = data.getTribulationCooldown() > 0 ? 8 : 5;
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
        return 60 + intervalCount * 20 + Math.max(0, strikes) * TribulationHandler.subWaveDurationTicks(boltsPerWave) + 80;
    }

    private static int subWaveDurationTicks(int boltsPerWave) {
        return Math.max(0, boltsPerWave - 1) * TribulationHandler.subBoltIntervalTicks(boltsPerWave);
    }

    private static int subBoltIntervalTicks(int boltsPerWave) {
        int bolts = Math.max(1, boltsPerWave);
        if (bolts <= 1) {
            return 0;
        }
        int interval = Math.round(30.0f / (float)(bolts - 1));
        return Math.max(4, Math.min(20, interval));
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

